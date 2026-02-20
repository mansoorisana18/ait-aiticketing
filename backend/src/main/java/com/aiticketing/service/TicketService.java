package com.aiticketing.service;

import java.util.List;

import com.aiticketing.bean.request.CreateTicketRequestBean;
import com.aiticketing.bean.response.TicketResponseBean;

public interface TicketService {
	
	TicketResponseBean createTicket(Long userId, CreateTicketRequestBean createTicketReq);
	List<TicketResponseBean> listTicketsForUser(Long userId);
	TicketResponseBean getTicketById( Long ticketId);
	List<TicketResponseBean> listAllTicketsForAdmin();
	List<TicketResponseBean> listAssignedTicketsForAgent(Long agentUserId);
	
}