import { useMutation } from "@tanstack/react-query";
import { loginApi, registerApi, type LoginRequest, type RegisterRequest } from "./api";
import type { LoginResponseBean } from "../../api/types";

export function useRegister() {
  return useMutation<LoginResponseBean, unknown, RegisterRequest>({ mutationFn: registerApi });
}

export function useLogin() {
  return useMutation<LoginResponseBean, unknown, LoginRequest>({ mutationFn: loginApi });
}