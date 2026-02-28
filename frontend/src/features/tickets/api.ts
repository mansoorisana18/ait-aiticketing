import { axiosClient } from "../../api/axiosClient";
import type { TicketResponseBean } from "../../api/types";

export type CreateTicketRequest = { title: string; description: string };

export async function createTicketApi(body: CreateTicketRequest): Promise<TicketResponseBean> {
  const res = await axiosClient.post<TicketResponseBean>("/api/tickets", body);
  return res.data;
}

export async function ticketsForUserApi(): Promise<TicketResponseBean[]> {
  const res = await axiosClient.get<TicketResponseBean[]>("/api/tickets");
  return res.data;
}

export async function ticketByIdApi(ticketId: number): Promise<TicketResponseBean> {
  const res = await axiosClient.get<TicketResponseBean>(`/api/tickets/${ticketId}`);
  return res.data;
}

export async function allTicketsAdminApi(): Promise<TicketResponseBean[]> {
  const res = await axiosClient.get<TicketResponseBean[]>("/api/tickets/admin/all");
  return res.data;
}

export async function ticketsForAgentApi(): Promise<TicketResponseBean[]> {
  const res = await axiosClient.get<TicketResponseBean[]>("/api/tickets/agent");
  return res.data;
}