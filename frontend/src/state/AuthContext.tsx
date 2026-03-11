import React, { createContext, useContext, useEffect, useMemo, useReducer } from "react";
import { authReducer, initialAuthState, type AuthPayload, type AuthState } from "./authReducer";
import { clearAuthStorage, getAuthFromStorage, setAuthToStorage } from "../utils/storage";
import { axiosClient } from "../api/axiosClient";
import { setAccessToken, clearAccessToken } from "../api/authToken";
import type { LoginResponseBean, UserRole } from "../api/types";
import { subscribeSessionExpired } from "./authEvents";
import { useQueryClient } from "@tanstack/react-query";

type StoredProfile = { userId: number; name: string; email: string; role: UserRole };

type AuthContextValue = {
  auth: AuthState;
  loginSuccess: (payload: AuthPayload) => void;
  logout: () => Promise<void>;
};

function isStoredProfile(x: unknown): x is StoredProfile {
  if (!x || typeof x !== "object") return false;
  const o = x as any;
  return typeof o.userId === "number" && typeof o.email === "string" && typeof o.name === "string" && typeof o.role === "string";
}

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [state, dispatch] = useReducer(authReducer, initialAuthState);
  const queryClient = useQueryClient();

  //Bootstrap once per page load: attempt /refresh using HttpOnly cookie
  useEffect(() => {
    let cancelled = false;

    (async () => {
      dispatch({ type: "BOOTSTRAP_START" });

      //read stored profile from storage for UI first, this doesn't mean user is authenticated since we don't have token yet, we need to call /refresh to verify and get token
      const stored = getAuthFromStorage();
      if (isStoredProfile(stored) && !cancelled) {
        //we don't dispatch LOGIN_SUCCESS here because token is missing until refresh succeeds, we just populate profile data for better UX while refresh is in-flight
      }

      try {
        //If refresh cookie exists, backend returns LoginResponseBean with data.token
        const res = await axiosClient.post<LoginResponseBean>("/api/users/refresh", null);
        if (cancelled) return;

        const data = res.data;

        //Clearing any stale cache on app load to prevent showing stale data from previous session
        queryClient.clear();

        //store acccess token in memory
        setAccessToken(data.token);

        //persist profile without access token in localstorage
        setAuthToStorage({ userId: data.userId, email: data.email, name: data.name, role: data.role });

        dispatch({
          type: "LOGIN_SUCCESS",
          payload: { userId: data.userId, email: data.email, name: data.name, role: data.role ?? "USER", token: data.token },
        });
      } catch {
        if (cancelled) return;
        clearAccessToken();
        clearAuthStorage();
        queryClient.clear();
        dispatch({ type: "BOOTSTRAP_DONE" });
      }
    })();

    return () => {
      cancelled = true;
    };
  }, []);

  //Keep authToken.ts synced with current state.token so axios interceptors can attach Authorization
  useEffect(() => {
    setAccessToken(state.token);
  }, [state.token]);

  const loginSuccess = (payload: AuthPayload) => {
    //Wiping previous users cached data on new login to prevent data leakage between users on same tab/device
    queryClient.clear();
    setAccessToken(payload.token);
    setAuthToStorage({ userId: payload.userId, email: payload.email, name: payload.name, role: payload.role });
    dispatch({ type: "LOGIN_SUCCESS", payload });
  };

  const logout = async () => {
    try {
      //authenticated endpoint requires Authorization header
      await axiosClient.post("/api/users/logout", null);
    } finally {
      clearAccessToken();
      clearAuthStorage();
      queryClient.clear();
      dispatch({ type: "LOGOUT" });
    }
  };
  
  useEffect(() => {
    const unsub = subscribeSessionExpired(() => {
      clearAccessToken();
      clearAuthStorage();
      queryClient.clear();
      dispatch({ type: "LOGOUT" });
    });

    return unsub;
  }, []);


  const value = useMemo<AuthContextValue>(
    () => ({ auth: state, loginSuccess, logout }),
    [state]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used within AuthProvider");
  return ctx;
}