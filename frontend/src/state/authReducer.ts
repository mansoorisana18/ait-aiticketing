import type { UserRole } from "../api/types";

export type AuthPayload = {
  userId: number;
  name: string;
  email: string;
  role?: UserRole;
  sessionToken?: string | null;
};

export type AuthState = {
  userId: number | null;
  name: string | null;
  email: string | null;
  role: UserRole;
  sessionToken: string | null;
};

export const initialAuthState: AuthState = {
  userId: null,
  name: null,
  email: null,
  role: "USER",
  sessionToken: null,
};

export type AuthAction =
  | { type: "LOGIN_SUCCESS"; payload: AuthPayload}
  | { type: "LOGOUT" };

export function authReducer(state: AuthState, action: AuthAction): AuthState {
  switch (action.type) {
    case "LOGIN_SUCCESS":
      return {
        userId: action.payload.userId,
        name: action.payload.name,
        email: action.payload.email,
        role: action.payload.role ?? "USER",
        sessionToken: action.payload.sessionToken ?? null,
      };
    case "LOGOUT":
      return { ...initialAuthState };
    default:
      return state;
  }
}