import type { UserRole } from "../api/types";

export type AuthState = {
  userId: number | null;
  email: string | null;
  name: string | null;
  role: UserRole | null;
  token: string | null;       //access token in memory
  isBootstrapping: boolean;   //true while we are calling /refresh on app startup
};

export type AuthPayload = {
  userId: number;
  email: string;
  name: string;
  role: UserRole;
  token: string;
};

export type AuthAction =
  | { type: "BOOTSTRAP_START" }
  | { type: "BOOTSTRAP_DONE" }
  | { type: "LOGIN_SUCCESS"; payload: AuthPayload }
  | { type: "LOGOUT" };

export const initialAuthState: AuthState = {
  userId: null,
  email: null,
  name: null,
  role: null,
  token: null,
  isBootstrapping: true,
};

export function authReducer( state: AuthState, action: AuthAction): AuthState {
  switch (action.type) {
    case "BOOTSTRAP_START":
      return { ...state, isBootstrapping: true };

    case "BOOTSTRAP_DONE":
      return { ...state, isBootstrapping: false };

    case "LOGIN_SUCCESS":
      return {
        userId: action.payload.userId,
        email: action.payload.email,
        name: action.payload.name,
        role: action.payload.role,
        token: action.payload.token,
        isBootstrapping: false,
      };

    case "LOGOUT":
      return { ...initialAuthState, isBootstrapping: false };

    default:
      return state;
  }
}