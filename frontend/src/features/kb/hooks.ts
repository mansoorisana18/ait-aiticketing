import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import type {
  KbArticleResponseBean,
  CreateKbArticleRequestBean,
  UpdateKbArticleRequestBean,
  UpdateKbDraftRequestBean,
  KbReviewDecisionRequestBean,
} from "../../api/types";
import {
  fetchPublishedKbArticles,
  fetchAllKbArticlesAdmin,
  fetchKbDraftsInReviewAdmin,
  fetchKbArticleById,
  createKbArticleAdmin,
  updateKbArticleAdmin,
  reviewKbDraftDecisionAdmin,
  updateKbDraftAgent,
  submitKbDraftForReviewAgent,
} from "./api";

//Read hooks
//Published KB list for AGENT / ADMIN
export function usePublishedKbArticles(enabled = true) {
  return useQuery<KbArticleResponseBean[]>({
    queryKey: ["kb", "published"],
    queryFn: fetchPublishedKbArticles,
    enabled,
    staleTime: 30_000,
  });
}

//Full KB list for ADMIN
export function useAllKbArticlesAdmin(enabled = true) {
  return useQuery<KbArticleResponseBean[]>({
    queryKey: ["kb", "admin", "all"],
    queryFn: fetchAllKbArticlesAdmin,
    enabled,
    staleTime: 30_000,
  });
}

//Drafts in review for ADMIN
export function useKbDraftsInReviewAdmin(enabled = true) {
  return useQuery<KbArticleResponseBean[]>({
    queryKey: ["kb", "admin", "review"],
    queryFn: fetchKbDraftsInReviewAdmin,
    enabled,
    staleTime: 15_000,
  });
}

//Single KB article by id
export function useKbArticleById(kbId: number | null, enabled = true) {
  return useQuery<KbArticleResponseBean>({
    queryKey: ["kb", kbId],
    queryFn: () => fetchKbArticleById(kbId as number),
    enabled: enabled && typeof kbId === "number",
    staleTime: 30_000,
  });
}

//Admin write hooks
export function useCreateKbArticleAdmin() {
  const qc = useQueryClient();

  return useMutation<KbArticleResponseBean, unknown, CreateKbArticleRequestBean>({
    mutationFn: createKbArticleAdmin,
    onSuccess: async () => {
      await qc.invalidateQueries({ queryKey: ["kb", "admin", "all"] });
      await qc.invalidateQueries({ queryKey: ["kb", "published"] });
    },
  });
}

export function useUpdateKbArticleAdmin(kbId: number) {
  const qc = useQueryClient();

  return useMutation<KbArticleResponseBean, unknown, UpdateKbArticleRequestBean>({
    mutationFn: (body) => updateKbArticleAdmin(kbId, body),
    onSuccess: async () => {
      await qc.invalidateQueries({ queryKey: ["kb", kbId] });
      await qc.invalidateQueries({ queryKey: ["kb", "admin", "all"] });
      await qc.invalidateQueries({ queryKey: ["kb", "published"] });
      await qc.invalidateQueries({ queryKey: ["kb", "admin", "review"] });
    },
  });
}

export function useReviewKbDraftDecisionAdmin(kbId: number) {
  const qc = useQueryClient();

  return useMutation<KbArticleResponseBean, unknown, KbReviewDecisionRequestBean>({
    mutationFn: (body) => reviewKbDraftDecisionAdmin(kbId, body),
    onSuccess: async () => {
      await qc.invalidateQueries({ queryKey: ["kb", kbId] });
      await qc.invalidateQueries({ queryKey: ["kb", "admin", "all"] });
      await qc.invalidateQueries({ queryKey: ["kb", "published"] });
      await qc.invalidateQueries({ queryKey: ["kb", "admin", "review"] });
      await qc.invalidateQueries({ queryKey: ["metrics", "admin", "ai-summary"] });
    },
  });
}

//Agent write hooks
export function useUpdateKbDraftAgent(kbId: number) {
  const qc = useQueryClient();

  return useMutation<KbArticleResponseBean, unknown, UpdateKbDraftRequestBean>({
    mutationFn: (body) => updateKbDraftAgent(kbId, body),
    onSuccess: async () => {
      await qc.invalidateQueries({ queryKey: ["kb", kbId] });
      await qc.invalidateQueries({ queryKey: ["kb", "published"] });
      await qc.invalidateQueries({ queryKey: ["kb", "admin", "review"] });
    },
  });
}

export function useSubmitKbDraftForReviewAgent(kbId: number) {
  const qc = useQueryClient();

  return useMutation<KbArticleResponseBean, unknown, void>({
    mutationFn: () => submitKbDraftForReviewAgent(kbId),
    onSuccess: async () => {
      await qc.invalidateQueries({ queryKey: ["kb", kbId] });
      await qc.invalidateQueries({ queryKey: ["kb", "published"] });
      await qc.invalidateQueries({ queryKey: ["kb", "admin", "review"] });
      await qc.invalidateQueries({ queryKey: ["metrics", "admin", "ai-summary"] });
    },
  });
}