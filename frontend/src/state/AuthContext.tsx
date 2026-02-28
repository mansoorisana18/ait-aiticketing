import React, { createContext, useContext, useEffect, useMemo, useReducer, useState } from "react";
import { authReducer, initialAuthState, type AuthPayload, type AuthState } from "./authReducer";
import { clearAuthStorage, getAuthFromStorage, setAuthToStorage } from "../utils/storage";

type AuthContextValue = {
  auth: AuthState;
  isAuthReady: boolean;
  loginSuccess: (payload: AuthPayload) => void;
  logout: () => void;
};

// Type guard: ensures stored object is safe to use as AuthPayload
function isAuthPayload(x: unknown): x is AuthPayload {
  if (!x || typeof x !== "object") return false;
  const o = x as any;
  return typeof o.userId === "number" && typeof o.email === "string" && typeof o.name === "string";
}

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [state, dispatch] = useReducer(authReducer, initialAuthState);
  const [isAuthReady, setIsAuthReady] = useState(false); 

  useEffect(() => {
    const stored = getAuthFromStorage(); // returns AuthState | null (or unknown depending on your typing)
    if (isAuthPayload(stored)) {
      dispatch({ type: "LOGIN_SUCCESS", payload: stored });
    }
    // Mark hydration complete whether we had data or not
    setIsAuthReady(true);
  }, []);

  useEffect(() => {
    if (!isAuthReady) return; //preventing premature clear

    if (state.userId) setAuthToStorage(state);
    else clearAuthStorage();
  }, [state, isAuthReady]);

  const value = useMemo<AuthContextValue>(
    () => ({
      auth: state,
      isAuthReady,
      loginSuccess: (payload) => dispatch({ type: "LOGIN_SUCCESS", payload }),
      logout: () => dispatch({ type: "LOGOUT" }),
    }),
    [state, isAuthReady]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used within AuthProvider");
  return ctx;
}