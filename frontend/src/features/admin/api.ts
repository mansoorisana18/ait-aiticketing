import { axiosClient } from "../../api/axiosClient";
import type { PromoteToAgentRequestBean, UserResponseBean } from "../../api/types";

export async function fetchUsersForAdmin(): Promise<UserResponseBean[]> {
  const res = await axiosClient.get<UserResponseBean[]>("/api/users/admin");
  return res.data;
}

export async function updateToAgentByAdmin(
  userId: number,
  body: PromoteToAgentRequestBean
): Promise<UserResponseBean> {
  const res = await axiosClient.patch<UserResponseBean>(`/api/users/admin/update-role/${userId}`, body);
  return res.data;
}