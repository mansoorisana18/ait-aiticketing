import axios, { type AxiosResponse } from "axios";
import type { ApiResponse } from "./types";
import { normalizeWrapperFailure } from "./errorNormalizer";
import { getAuthFromStorage } from "../utils/storage";

const baseURL = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";

export const axiosClient = axios.create({
  baseURL,
  headers: { "Content-Type": "application/json" },
});

// Attach X-User-Id from storage-backed auth
axiosClient.interceptors.request.use((config) => {
  const auth = getAuthFromStorage();
  if (auth?.userId) config.headers["X-User-Id"] = auth.userId;
  return config;
});

// Unwrap ApiResponse wrapper consistently:
// when success=true => response.data becomes the inner `data`
// when success=false => throw normalized error (validation/business)
axiosClient.interceptors.response.use((response: AxiosResponse<ApiResponse<any>>) => {
  const payload = response.data;

  if (payload && typeof payload === "object" && "success" in payload) {
    if (payload.success === false) {
      throw normalizeWrapperFailure(payload);
    }
    // Return inner data only
    return { ...response, data: payload.data };
  }

  return response;
});