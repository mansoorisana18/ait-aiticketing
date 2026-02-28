import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import type { TicketResponseBean } from "../../api/types";
import { ticketsForAgentApi, allTicketsAdminApi, createTicketApi, ticketByIdApi, ticketsForUserApi, type CreateTicketRequest } from "./api";

export function useCreateTicket() {
  const qc = useQueryClient();
  return useMutation<TicketResponseBean, unknown, CreateTicketRequest>({
    mutationFn: createTicketApi,
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["tickets", "me"] });
      qc.invalidateQueries({ queryKey: ["tickets", "admin", "all"] });
    },
  });
}

export function useTicketsForUser(enabled: boolean) {
  return useQuery<TicketResponseBean[], unknown>({
    queryKey: ["tickets", "me"],
    queryFn: ticketsForUserApi,
    enabled,
    staleTime: 30_000,
  });
}

export function useTicketById(ticketId: number | null, enabled: boolean) {
  return useQuery<TicketResponseBean, unknown>({
    queryKey: ["tickets", "detail", String(ticketId ?? "")],
    queryFn: () => ticketByIdApi(ticketId as number),
    enabled: enabled && typeof ticketId === "number",
    staleTime: 30_000,
  });
}

export function useAllTicketsAdmin(enabled: boolean) {
  return useQuery<TicketResponseBean[], unknown>({
    queryKey: ["tickets", "admin", "all"],
    queryFn: allTicketsAdminApi,
    enabled,
    staleTime: 15_000,
  });
}

export function useTicketsForAgent(enabled: boolean) {
  return useQuery<TicketResponseBean[], unknown>({
    queryKey: ["tickets", "agent"],
    queryFn: ticketsForAgentApi,
    enabled,
    staleTime: 30_000,
  });
}