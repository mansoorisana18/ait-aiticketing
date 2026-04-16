import { axiosClient } from "../../api/axiosClient";
import type { TicketResponseBean, UserTicketResponseBean, AdminOverrideRequestBean, AdminOverrideResponseBean, TicketCommentRequestBean, TicketCommentResponseBean, UpdateTicketStatusRequestBean, UpdateVagueTicketRequestBean, TicketTextVersionResponseBean, ConfirmedDuplicateTicketResponseBean, PrimaryLinkedTicketResponseBean, EligibleAgentResponseBean, KbSuggestionResponseRequestBean, ManualKbSuggestionRequestBean, GenerateKbDraftRequestBean, } from "../../api/types";

export type CreateTicketRequest = { title: string; description: string };

export async function createTicketApi(body: CreateTicketRequest): Promise<UserTicketResponseBean> {
  const res = await axiosClient.post<UserTicketResponseBean>("/api/tickets", body);
  return res.data;
}

//USER tickets
export async function fetchTicketsForUser(): Promise<UserTicketResponseBean[]> {
  const res = await axiosClient.get<UserTicketResponseBean[]>("/api/tickets");
  return res.data;
}

export async function fetchTicketByIdForUser(ticketId: number): Promise<UserTicketResponseBean> {
  const res = await axiosClient.get<UserTicketResponseBean>(`/api/tickets/user/${ticketId}`);
  return res.data;
}

//CLARIFY VAGUE TICKET BY USER
export async function clarifyVagueTicket(
  ticketId: number,
  body: UpdateVagueTicketRequestBean
): Promise<UserTicketResponseBean> {
  const res = await axiosClient.patch<UserTicketResponseBean>(`/api/tickets/user/${ticketId}/clarify`, body);
  return res.data;
}

//AGENT/ADMIN tickets
export async function fetchTicketByIdInternal(ticketId: number): Promise<TicketResponseBean> {
  const res = await axiosClient.get<TicketResponseBean>(`/api/tickets/${ticketId}`);
  return res.data;
}

export async function fetchAllTicketsAdmin(): Promise<TicketResponseBean[]> {
  const res = await axiosClient.get<TicketResponseBean[]>("/api/tickets/admin/all");
  return res.data;
}

export async function fetchTicketsForAgent(): Promise<TicketResponseBean[]> {
  const res = await axiosClient.get<TicketResponseBean[]>("/api/tickets/agent");
  return res.data;
}

//COMMENTS
export async function fetchTicketComments(ticketId: number): Promise<TicketCommentResponseBean[]> {
  const res = await axiosClient.get<TicketCommentResponseBean[]>(`/api/tickets/${ticketId}/comments`);
  return res.data;
}

export async function createTicketComment(
  ticketId: number,
  body: TicketCommentRequestBean
): Promise<TicketCommentResponseBean> {
  const res = await axiosClient.post<TicketCommentResponseBean>(`/api/tickets/${ticketId}/comments`, body);
  return res.data;
}

//ADMIN OVERRIDE
export async function adminOverrideTicket(
  ticketId: number,
  body: AdminOverrideRequestBean
): Promise<AdminOverrideResponseBean> {
  const res = await axiosClient.patch<AdminOverrideResponseBean>(`/api/tickets/${ticketId}/admin/override`, body);
  return res.data;
}

//AGENT TICKET STATUS UPDATE
export async function updateTicketStatusByAgent(
  ticketId: number,
  body: UpdateTicketStatusRequestBean
): Promise<TicketResponseBean> {
  const res = await axiosClient.patch<TicketResponseBean>(`/api/tickets/${ticketId}/agent/status`, body);
  return res.data;
}

//VAGUE TICKET TEXT HISTORY
export async function fetchTicketTextVersionHistory(
  ticketId: number
): Promise<TicketTextVersionResponseBean[]> {
  const res = await axiosClient.get<TicketTextVersionResponseBean[]>(
    `/api/tickets/${ticketId}/text-version-history`
  );
  return res.data;
}

//DUPLICATE TICKET LINKS
export async function fetchConfirmedDuplicates(
  ticketId: number
): Promise<ConfirmedDuplicateTicketResponseBean[]> {
  const res = await axiosClient.get<ConfirmedDuplicateTicketResponseBean[]>(
    `/api/tickets/${ticketId}/confirmed-duplicates`
  );
  return res.data;
}

//fetch primary linked ticket for a duplicate ticket
export async function fetchPrimaryLink(
  ticketId: number
): Promise<PrimaryLinkedTicketResponseBean> {
  const res = await axiosClient.get<PrimaryLinkedTicketResponseBean>(
    `/api/tickets/${ticketId}/primary-link`
  );
  return res.data;
}

//fetch eligible agents for routing a ticket
export async function fetchEligibleAgents(
  ticketId: number
): Promise<EligibleAgentResponseBean[]> {
  const res = await axiosClient.get<EligibleAgentResponseBean[]>(
    `/api/tickets/${ticketId}/eligible-agents`
  );
  return res.data;
}

//Respond to KB suggestion on a ticket by user
export async function respondToKbSuggestion(
  ticketId: number,
  body: KbSuggestionResponseRequestBean
): Promise<UserTicketResponseBean> {
  const res = await axiosClient.post<UserTicketResponseBean>(
    `/api/tickets/user/${ticketId}/kb-response`,
    body
  );
  return res.data;
}

//Manually suggest a KB article for a ticket by agent
export async function suggestKbManually(
  ticketId: number,
  body: ManualKbSuggestionRequestBean
): Promise<TicketResponseBean> {
  const res = await axiosClient.post<TicketResponseBean>(
    `/api/tickets/agent/${ticketId}/kb/manual-suggestion`,
    body
  );
  return res.data;
}

//Generate KB draft from a ticket by agent
export async function generateKbDraft(
  ticketId: number,
  body: GenerateKbDraftRequestBean
): Promise<TicketResponseBean> {
  const res = await axiosClient.post<TicketResponseBean>(
    `/api/tickets/agent/${ticketId}/kb-draft/generate`,
    body
  );
  return res.data;
}