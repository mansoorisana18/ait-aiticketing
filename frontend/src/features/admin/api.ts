import { axiosClient } from "../../api/axiosClient";
import type { UserResponseBean } from "../../api/types";

export async function fetchUsersForAdmin(): Promise<UserResponseBean[]> {
  const res = await axiosClient.get<UserResponseBean[]>("/api/users/admin");
  return res.data;
}

export async function updateToAgentByAdmin(userId: number): Promise<UserResponseBean> {
  const res = await axiosClient.patch<UserResponseBean>(`/api/users/admin/update-role/${userId}`);
  return res.data;
}