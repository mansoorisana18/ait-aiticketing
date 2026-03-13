import { useQuery } from "@tanstack/react-query";
import {
  fetchAdminAiSummary,
  fetchAdminTicketSummary,
  fetchAgentTicketSummary,
} from "./api";
import type {
  AiSummaryMetricsResponseBean,
  TicketSummaryMetricsResponseBean,
} from "../../api/types";

const STALE_TIME = 30_000;
const REFRESH_INTERVAL = 60_000;

export function useAdminAiSummary(enabled = true) {
  return useQuery<AiSummaryMetricsResponseBean>({
    queryKey: ["metrics", "admin", "ai-summary"],
    queryFn: fetchAdminAiSummary,
    enabled,
    staleTime: STALE_TIME,
    refetchInterval: REFRESH_INTERVAL,
    refetchOnWindowFocus: true,
  });
}

export function useAdminTicketSummary(enabled = true) {
  return useQuery<TicketSummaryMetricsResponseBean>({
    queryKey: ["metrics", "admin", "ticket-summary"],
    queryFn: fetchAdminTicketSummary,
    enabled,
    staleTime: STALE_TIME,
    refetchInterval: REFRESH_INTERVAL,
  });
}

export function useAgentTicketSummary(enabled = true) {
  return useQuery<TicketSummaryMetricsResponseBean>({
    queryKey: ["metrics", "agent", "ticket-summary"],
    queryFn: fetchAgentTicketSummary,
    enabled,
    staleTime: STALE_TIME,
    refetchInterval: REFRESH_INTERVAL,
  });
}