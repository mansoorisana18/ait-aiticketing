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

export function useAdminAiSummary(enabled: boolean) {
  return useQuery<AiSummaryMetricsResponseBean>({
    queryKey: ["metrics", "admin", "ai-summary"],
    queryFn: fetchAdminAiSummary,
    enabled,
    staleTime: 0,
    refetchOnMount: "always",
    refetchOnWindowFocus: true,
    refetchInterval: enabled ? 20000 : undefined, 
  });
}

export function useAdminTicketSummary(enabled: boolean) {
  return useQuery<TicketSummaryMetricsResponseBean>({
    queryKey: ["metrics", "admin", "ticket-summary"],
    queryFn: fetchAdminTicketSummary,
    enabled,
    staleTime: 0,
    refetchOnMount: "always",
    refetchOnWindowFocus: true,
    refetchInterval: enabled ? 20000 : undefined,
  });
}

export function useAgentTicketSummary(enabled: boolean) {
  return useQuery<TicketSummaryMetricsResponseBean>({
    queryKey: ["metrics", "agent", "ticket-summary"],
    queryFn: fetchAgentTicketSummary,
    enabled,
    staleTime: 0,
    refetchOnMount: "always",
    refetchOnWindowFocus: true,
    refetchInterval: enabled ? 20000 : undefined,
  });
}