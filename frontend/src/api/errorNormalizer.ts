import type { ApiFailure } from "./types";
import type { AxiosError } from "axios";

export type NormalizedError =
  | { kind: "validation"; message: string; fieldErrors: Record<string, string> }
  | { kind: "business"; message: string }
  | { kind: "http"; status: number; message: string }
  | { kind: "network"; message: string }
  | { kind: "unknown"; message: string };

export function normalizeWrapperFailure(payload: ApiFailure): NormalizedError {
  if (payload.message === "Validation failed" && Array.isArray(payload.errors)) {
    const fieldErrors: Record<string, string> = {};
    for (const e of payload.errors) {
      if (e?.field) fieldErrors[e.field] = e.message ?? "Invalid";
    }
    return { kind: "validation", message: payload.message, fieldErrors };
  }
  return { kind: "business", message: payload.message || "Request failed" };
}

export function normalizeApiError(err: unknown): NormalizedError {
  // wrapper failure thrown directly
  if (typeof err === "object" && err !== null && "kind" in err) {
    return err as NormalizedError;
  }

  const ax = err as AxiosError<any> | undefined;
  if (ax?.isAxiosError) {
    const status = ax.response?.status ?? 0;
    const payload = ax.response?.data;

    if (payload && typeof payload === "object" && "success" in payload) {
      return normalizeWrapperFailure(payload as ApiFailure);
    }
    if (status === 0) return { kind: "network", message: "Network error. Please try again." };

    return {
      kind: "http",
      status,
      message: payload?.message || ax.message || "Request failed",
    };
  }

  return { kind: "unknown", message: "Something went wrong" };
}