import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import type { UserResponseBean } from "../../api/types";
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

  return useMutation<UserResponseBean, unknown, number>({
    mutationFn: (userId) => updateToAgentByAdmin(userId),
    onSuccess: async () => {
      await qc.invalidateQueries({ queryKey: ["admin", "users"] });
    },
  });
}