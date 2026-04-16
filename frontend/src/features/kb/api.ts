import { axiosClient } from "../../api/axiosClient";

export type KbListItemResponseBean = {
  kbId: number;
  title: string;
  status?: string | null;
  sourceTicketId?: number | null;
  updatedAt?: string | null;
  createdByName?: string | null;
};

export type KbDraftReviewResponseBean = {
  kbId: number;
  title: string;
  status?: string | null;
  sourceTicketId?: number | null;
  updatedAt?: string | null;
  createdByName?: string | null;
};

export async function fetchKbArticles(): Promise<KbListItemResponseBean[]> {
  const res = await axiosClient.get<KbListItemResponseBean[]>("/api/kb");
  return res.data;
}

export async function fetchMyKbSubmissions(): Promise<KbListItemResponseBean[]> {
  const res = await axiosClient.get<KbListItemResponseBean[]>("/api/kb/my-submissions");
  return res.data;
}

export async function fetchKbApprovals(): Promise<KbDraftReviewResponseBean[]> {
  const res = await axiosClient.get<KbDraftReviewResponseBean[]>("/api/kb/approvals");
  return res.data;
}