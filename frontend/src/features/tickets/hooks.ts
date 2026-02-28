import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import type { TicketResponseBean, UserTicketResponseBean, AdminOverrideRequestBean, AdminOverrideResponseBean, TicketCommentRequestBean, TicketCommentResponseBean, UpdateTicketStatusRequestBean } from "../../api/types";
import { fetchTicketsForAgent, fetchAllTicketsAdmin, createTicketApi, fetchTicketByIdForUser, fetchTicketByIdInternal, fetchTicketsForUser, 
  createTicketComment, fetchTicketComments, adminOverrideTicket, updateTicketStatusByAgent, type CreateTicketRequest } from "./api";

export function useCreateTicket() {
  const qc = useQueryClient();
  return useMutation<TicketResponseBean, unknown, CreateTicketRequest>({
    mutationFn: createTicketApi,
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["tickets", "user"] });
      qc.invalidateQueries({ queryKey: ["tickets", "admin", "all"] });
    },
  });
}

export function useTicketsForUser(enabled = true) {
  return useQuery<UserTicketResponseBean[]>({
    queryKey: ["tickets", "user"],
    queryFn: fetchTicketsForUser,
    enabled,
    staleTime: 30_000,
  });
}

//Ticket Details for user
export function useTicketDetailsUser(ticketId: number | null, enabled = true) {
  return useQuery<UserTicketResponseBean>({
    queryKey: ["ticket", "user", ticketId],
    queryFn: () => fetchTicketByIdForUser(ticketId as number),
    enabled: enabled && typeof ticketId === "number",
    staleTime: 10_000,
  });
}

//Ticket Details for agent/admin
export function useTicketDetailsInternal(ticketId: number | null, enabled = true) {
  return useQuery<TicketResponseBean>({
    queryKey: ["ticket", "internal", ticketId],
    queryFn: () => fetchTicketByIdInternal(ticketId as number),
    enabled: enabled && typeof ticketId === "number",
    staleTime: 10_000,
  });
}

export function useAllTicketsAdmin(enabled = true) {
  return useQuery<TicketResponseBean[]>({
    queryKey: ["tickets", "admin", "all"],
    queryFn: fetchAllTicketsAdmin,
    enabled,
    staleTime: 15_000,
  });
}

export function useTicketsForAgent(enabled = true) {
  return useQuery<TicketResponseBean[]>({
    queryKey: ["tickets", "agent"],
    queryFn: fetchTicketsForAgent,
    enabled,
    staleTime: 15_000,
  });
}

//Comments
export function useTicketComments(ticketId: number | null, enabled = true) {
  return useQuery<TicketCommentResponseBean[]>({
    queryKey: ["ticket", ticketId, "comments"],
    queryFn: () => fetchTicketComments(ticketId as number),
    enabled: enabled && typeof ticketId === "number",
    staleTime: 5_000,
  });
}

export function useCreateTicketComment(ticketId: number) {
  const qc = useQueryClient();
  return useMutation<TicketCommentResponseBean, any, TicketCommentRequestBean>({
    mutationFn: (body) => createTicketComment(ticketId, body),
    onSuccess: async () => {
      await qc.invalidateQueries({ queryKey: ["ticket", ticketId, "comments"] });
    },
  });
}

//Admin override
export function useAdminOverride(ticketId: number) {
  const qc = useQueryClient();
  return useMutation<AdminOverrideResponseBean, any, AdminOverrideRequestBean>({
    mutationFn: (body) => adminOverrideTicket(ticketId, body),
    onSuccess: async () => {
      await qc.invalidateQueries({ queryKey: ["ticket", "internal", ticketId] });
      await qc.invalidateQueries({ queryKey: ["tickets", "admin", "all"] });
      await qc.invalidateQueries({ queryKey: ["tickets", "agent"] });
    },
  });
}

//Agent status update
export function useAgentUpdateStatus(ticketId: number) {
  const qc = useQueryClient();
  return useMutation<TicketResponseBean, any, UpdateTicketStatusRequestBean>({
    mutationFn: (body) => updateTicketStatusByAgent(ticketId, body),
    onSuccess: async () => {
      await qc.invalidateQueries({ queryKey: ["ticket", "internal", ticketId] });
      await qc.invalidateQueries({ queryKey: ["tickets", "agent"] });
      await qc.invalidateQueries({ queryKey: ["tickets", "admin", "all"] });
    },
  });
}
