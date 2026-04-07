import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import type {
  TicketResponseBean,
  UserTicketResponseBean,
  AdminOverrideRequestBean,
  AdminOverrideResponseBean,
  TicketCommentRequestBean,
  TicketCommentResponseBean,
  UpdateTicketStatusRequestBean,
  UpdateVagueTicketRequestBean,
  TicketTextVersionResponseBean,
  ConfirmedDuplicateTicketResponseBean,
  PrimaryLinkedTicketResponseBean,
  EligibleAgentResponseBean,
} from "../../api/types";
import {
  fetchTicketsForAgent,
  fetchAllTicketsAdmin,
  createTicketApi,
  fetchTicketByIdForUser,
  fetchTicketByIdInternal,
  fetchTicketsForUser,
  createTicketComment,
  fetchTicketComments,
  adminOverrideTicket,
  updateTicketStatusByAgent,
  clarifyVagueTicket,
  fetchTicketTextVersionHistory,
  fetchConfirmedDuplicates,
  fetchPrimaryLink,
  fetchEligibleAgents,
  type CreateTicketRequest,
} from "./api";

export function useCreateTicket() {
  const qc = useQueryClient();
  return useMutation<UserTicketResponseBean, unknown, CreateTicketRequest>({
    mutationFn: createTicketApi,
    onSuccess: async () => {
      await qc.invalidateQueries({ queryKey: ["tickets", "user"] });
      await qc.invalidateQueries({ queryKey: ["tickets", "admin", "all"] });
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

//Vague ticket clarification by user
export function useClarifyVagueTicket(ticketId: number) {
  const qc = useQueryClient();

  return useMutation<UserTicketResponseBean, unknown, UpdateVagueTicketRequestBean>({
    mutationFn: (body) => clarifyVagueTicket(ticketId, body),
    onSuccess: async () => {
      await qc.invalidateQueries({ queryKey: ["ticket", "user", ticketId] });
      await qc.invalidateQueries({ queryKey: ["tickets", "user"] });
    },
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

//Text version history for agent/admin
export function useTicketTextVersionHistory(ticketId: number | null, enabled = true) {
  return useQuery<TicketTextVersionResponseBean[]>({
    queryKey: ["ticket", ticketId, "text-history"],
    queryFn: () => fetchTicketTextVersionHistory(ticketId as number),
    enabled: enabled && typeof ticketId === "number",
    staleTime: 10_000,
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
  return useMutation<TicketCommentResponseBean, unknown, TicketCommentRequestBean>({
    mutationFn: (body) => createTicketComment(ticketId, body),
    onSuccess: async () => {
      await qc.invalidateQueries({ queryKey: ["ticket", ticketId, "comments"] });
    },
  });
}

//Admin override
export function useAdminOverride(ticketId: number) {
  const qc = useQueryClient();
  return useMutation<AdminOverrideResponseBean, unknown, AdminOverrideRequestBean>({
    mutationFn: (body) => adminOverrideTicket(ticketId, body),
    onSuccess: async () => {
      await qc.invalidateQueries({ queryKey: ["ticket", "internal", ticketId] });
      await qc.invalidateQueries({ queryKey: ["tickets", "admin", "all"] });
      await qc.invalidateQueries({ queryKey: ["tickets", "agent"] });
      await qc.invalidateQueries({ queryKey: ["ticket", ticketId, "eligible-agents"] });
      await qc.invalidateQueries({ queryKey: ["ticket", ticketId, "primary-link"] });
      await qc.invalidateQueries({ queryKey: ["ticket", ticketId, "confirmed-duplicates"] });
      await qc.invalidateQueries({ queryKey: ["metrics", "admin", "ticket-summary"] });
      await qc.invalidateQueries({ queryKey: ["metrics", "admin", "ai-summary"] });
    },
  });
}

//Agent status update
export function useAgentUpdateStatus(ticketId: number) {
  const qc = useQueryClient();
  return useMutation<TicketResponseBean, unknown, UpdateTicketStatusRequestBean>({
    mutationFn: (body) => updateTicketStatusByAgent(ticketId, body),
    onSuccess: async () => {
      await qc.invalidateQueries({ queryKey: ["ticket", "internal", ticketId] });
      await qc.invalidateQueries({ queryKey: ["tickets", "agent"] });
      await qc.invalidateQueries({ queryKey: ["tickets", "admin", "all"] });
    },
  });
}

export function useConfirmedDuplicates(ticketId: number | null, enabled = true) {
  return useQuery<ConfirmedDuplicateTicketResponseBean[]>({
    queryKey: ["ticket", ticketId, "confirmed-duplicates"],
    queryFn: () => fetchConfirmedDuplicates(ticketId as number),
    enabled: enabled && typeof ticketId === "number",
    staleTime: 10_000,
  });
}

export function usePrimaryLink(ticketId: number | null, enabled = true) {
  return useQuery<PrimaryLinkedTicketResponseBean>({
    queryKey: ["ticket", ticketId, "primary-link"],
    queryFn: () => fetchPrimaryLink(ticketId as number),
    enabled: enabled && typeof ticketId === "number",
    staleTime: 10_000,
  });
}

export function useEligibleAgents(ticketId: number | null, enabled = true) {
  return useQuery<EligibleAgentResponseBean[]>({
    queryKey: ["ticket", ticketId, "eligible-agents"],
    queryFn: () => fetchEligibleAgents(ticketId as number),
    enabled: enabled && typeof ticketId === "number",
    staleTime: 10_000,
  });
}