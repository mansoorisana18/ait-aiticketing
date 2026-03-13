import { axiosClient } from "../../api/axiosClient";
import type {
  AiSummaryMetricsResponseBean,
  TicketSummaryMetricsResponseBean,
} from "../../api/types";

export async function fetchAdminAiSummary(): Promise<AiSummaryMetricsResponseBean> {
  const res = await axiosClient.get<AiSummaryMetricsResponseBean>("/api/metrics/admin/ai-summary");
  return res.data;
}

export async function fetchAdminTicketSummary(): Promise<TicketSummaryMetricsResponseBean> {
  const res = await axiosClient.get<TicketSummaryMetricsResponseBean>("/api/metrics/admin/ticket-summary");
  return res.data;
}

export async function fetchAgentTicketSummary(): Promise<TicketSummaryMetricsResponseBean> {
  const res = await axiosClient.get<TicketSummaryMetricsResponseBean>("/api/metrics/agent/ticket-summary");
  return res.data;
}