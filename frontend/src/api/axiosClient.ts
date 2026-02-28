import axios, { type AxiosError, type AxiosInstance, type AxiosResponse, type InternalAxiosRequestConfig, } from "axios";
import type { ApiResponse } from "./types";
import { normalizeWrapperFailure } from "./errorNormalizer";
import { getAccessToken, setAccessToken, clearAccessToken } from "./authToken";
import { emitSessionExpired } from "../state/authEvents";

const baseURL = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";

//public and auth endpoints where we dont attach Authorization header
const AUTH_EXCLUDE = ["/api/users/login", "/api/users/register", "/api/users/refresh"];

//to check if request is excluded for auth header
function isAuthExcluded(url?: string) {
  if (!url) return false;
  return AUTH_EXCLUDE.some((p) => url.includes(p));
}

//Refresh queue concurrency control to handle multiple requests during token refresh so that even if multiple requests fails due to expird token we queue thm and call refresh only once and then retry them all 
let isRefreshing = false;
let refreshPromise: Promise<string> | null = null;

type Queued = {
  resolve: (token: string) => void;
  reject: (err: unknown) => void;
};
let queue: Queued[] = [];

function flushQueue(error: unknown, token: string | null) {
  queue.forEach((p) => (token ? p.resolve(token) : p.reject(error)));
  queue = [];
}

async function doRefresh(client: AxiosInstance): Promise<string> {
  //refresh endpoint uses HttpOnly cookie, no Authorization header required but must send credentials
  const res = await client.post("/api/users/refresh", null, {
    withCredentials: true,
  });

  //response interceptor unwraps ApiResponse => res.data is the inner `data`
  //we expect res.data.token
  const token = (res.data as any)?.token as string | undefined;
  if (!token) throw new Error("Refresh succeeded but token missing in response");
  return token;
}

//Create axios instance
export const axiosClient: AxiosInstance = axios.create({
  baseURL,
  headers: { "Content-Type": "application/json" },
  withCredentials: true, //Required so that refresh_token is sent
});

//Request interceptor for attaching Authorization header for protected endpoints
axiosClient.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  if (!isAuthExcluded(config.url)) {
    const token = getAccessToken();
    if (token) {
      config.headers = config.headers ?? {};
      config.headers.Authorization = `Bearer ${token}`;
    }
  }
  return config;
});

//Response interceptor for unwrapping ApiResponse wrapper + refresh on 401 once
//success=true => return response.data = inner payload.data
//success=false => throw normalized error
axiosClient.interceptors.response.use(
  (response: AxiosResponse<ApiResponse<unknown>>) => {
    const payload = response.data;

    if (payload && typeof payload === "object" && "success" in payload) {
      if (payload.success === false) throw normalizeWrapperFailure(payload);
      return { ...response, data: payload.data } as AxiosResponse<unknown>;
    }
    return response;
  },
  async (error: AxiosError) => {
    const original = error.config as (InternalAxiosRequestConfig & { _retry?: boolean }) | undefined;

    //If we don't have a config, can't retry.
    if (!original) return Promise.reject(error);

    const status = (error as any).response?.status as number | undefined;

    //Do NOT refresh if:
    //- not 401
    //- excluded endpoints (login/register/refresh)
    //- already retried once
    if (status !== 401 || isAuthExcluded(original.url) || original._retry) {
      return Promise.reject(error);
    }

    original._retry = true;

    //if refresh is already happening, queue this request and resolve/reject after refresh is done so that only one refresh happens at a time and all requests during refresh are retried after refresh with new token
    if (isRefreshing && refreshPromise) {
      return new Promise((resolve, reject) => {
        queue.push({
          resolve: (token) => {
            original.headers = original.headers ?? {};
            original.headers.Authorization = `Bearer ${token}`;
            resolve(axiosClient(original));
          },
          reject,
        });
      });
    }

    //Start refresh
    isRefreshing = true;
    refreshPromise = doRefresh(axiosClient);

    try {
      const newToken = await refreshPromise;
      setAccessToken(newToken);
      flushQueue(null, newToken);

      //retry original request with new token
      original.headers = original.headers ?? {};
      original.headers.Authorization = `Bearer ${newToken}`;

      return axiosClient(original);
    } catch (refreshErr) {
      //refresh failed -> clear access token & UI should be redirected to login
      clearAccessToken();
      setAccessToken(null);
      flushQueue(refreshErr, null);
      emitSessionExpired();
      return Promise.reject(refreshErr);
    } finally {
      isRefreshing = false;
      refreshPromise = null;
    }
  }
);