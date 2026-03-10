import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import type { PromoteToAgentRequestBean, UserResponseBean } from "../../api/types";
import { fetchUsersForAdmin, updateToAgentByAdmin } from "./api";

export function useUsersForAdmin(enabled = true) {
  return useQuery<UserResponseBean[]>({
    queryKey: ["admin", "users"],
    queryFn: fetchUsersForAdmin,
    enabled,
    staleTime: 30_000,
  });
}

export function useMakeAgentByAdmin() {
  const qc = useQueryClient();

  return useMutation<UserResponseBean, unknown, { userId: number; body: PromoteToAgentRequestBean }>({
    mutationFn: ({ userId, body }) => updateToAgentByAdmin(userId, body),
    onSuccess: async () => {
      await qc.invalidateQueries({ queryKey: ["admin", "users"] });
    },
  });
}