import { useQuery } from "@tanstack/react-query";
import {
  fetchKbApprovals,
  fetchKbArticles,
  fetchMyKbSubmissions,
  type KbDraftReviewResponseBean,
  type KbListItemResponseBean,
} from "./api";

export function useKbArticles(enabled = true) {
  return useQuery<KbListItemResponseBean[]>({
    queryKey: ["kb", "all"],
    queryFn: fetchKbArticles,
    enabled,
    staleTime: 30_000,
  });
}

export function useMyKbSubmissions(enabled = true) {
  return useQuery<KbListItemResponseBean[]>({
    queryKey: ["kb", "my-submissions"],
    queryFn: fetchMyKbSubmissions,
    enabled,
    staleTime: 30_000,
  });
}

export function useKbApprovals(enabled = true) {
  return useQuery<KbDraftReviewResponseBean[]>({
    queryKey: ["kb", "approvals"],
    queryFn: fetchKbApprovals,
    enabled,
    staleTime: 30_000,
  });
}