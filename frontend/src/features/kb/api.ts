import { axiosClient } from "../../api/axiosClient";
import type {
  KbArticleResponseBean,
  CreateKbArticleRequestBean,
  UpdateKbArticleRequestBean,
  UpdateKbDraftRequestBean,
  KbReviewDecisionRequestBean,
} from "../../api/types";

//Read APIs
//Published KB articles for AGENT / ADMIN
export async function fetchPublishedKbArticles(): Promise<KbArticleResponseBean[]> {
  const res = await axiosClient.get<KbArticleResponseBean[]>("/api/kb");
  return res.data;
}

//All KB articles for ADMIN
export async function fetchAllKbArticlesAdmin(): Promise<KbArticleResponseBean[]> {
  const res = await axiosClient.get<KbArticleResponseBean[]>("/api/kb/admin");
  return res.data;
}

//KB drafts in review for ADMIN
export async function fetchKbDraftsInReviewAdmin(): Promise<KbArticleResponseBean[]> {
  const res = await axiosClient.get<KbArticleResponseBean[]>("/api/kb/admin/review");
  return res.data;
}

//Single KB article by id for USER / AGENT / ADMIN
export async function fetchKbArticleById(kbId: number): Promise<KbArticleResponseBean> {
  const res = await axiosClient.get<KbArticleResponseBean>(`/api/kb/${kbId}`);
  return res.data;
}

//Admin write APIs
export async function createKbArticleAdmin(
  body: CreateKbArticleRequestBean
): Promise<KbArticleResponseBean> {
  const res = await axiosClient.post<KbArticleResponseBean>("/api/kb/admin", body);
  return res.data;
}

export async function updateKbArticleAdmin(
  kbId: number,
  body: UpdateKbArticleRequestBean
): Promise<KbArticleResponseBean> {
  const res = await axiosClient.put<KbArticleResponseBean>(`/api/kb/admin/${kbId}`, body);
  return res.data;
}

//Admin review decision on KB draft
export async function reviewKbDraftDecisionAdmin(
  kbId: number,
  body: KbReviewDecisionRequestBean
): Promise<KbArticleResponseBean> {
  const res = await axiosClient.post<KbArticleResponseBean>(
    `/api/kb/admin/${kbId}/review-decision`,
    body
  );
  return res.data;
}

//Agent write APIs
export async function updateKbDraftAgent(
  kbId: number,
  body: UpdateKbDraftRequestBean
): Promise<KbArticleResponseBean> {
  const res = await axiosClient.put<KbArticleResponseBean>(
    `/api/kb/agent/${kbId}/draft`,
    body
  );
  return res.data;
}

export async function submitKbDraftForReviewAgent(
  kbId: number
): Promise<KbArticleResponseBean> {
  const res = await axiosClient.post<KbArticleResponseBean>(
    `/api/kb/agent/${kbId}/submit-review`
  );
  return res.data;
}