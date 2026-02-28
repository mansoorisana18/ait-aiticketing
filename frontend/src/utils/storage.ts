import type { AuthState } from "../state/authReducer";

const KEY = "tt_auth";

export function getAuthFromStorage(): unknown | null {
  try {
    const raw = localStorage.getItem(KEY);
    return raw ? JSON.parse(raw) : null;
  } catch {
    return null;
  }
}

export function setAuthToStorage(auth: unknown): void {
  localStorage.setItem(KEY, JSON.stringify(auth));
}

export function clearAuthStorage(): void {
  localStorage.removeItem(KEY);
}