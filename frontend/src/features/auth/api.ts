import { axiosClient } from "../../api/axiosClient";
import type { LoginResponseBean } from "../../api/types";

export type RegisterRequest = { email: string; name: string; password: string };
export type LoginRequest = { email: string; password: string };

export async function registerApi(body: RegisterRequest): Promise<LoginResponseBean> {
  const res = await axiosClient.post<LoginResponseBean>("/api/users/register", body);
  return res.data;
}

export async function loginApi(body: LoginRequest): Promise<LoginResponseBean> {
  const res = await axiosClient.post<LoginResponseBean>("/api/users/login", body);
  return res.data;
}