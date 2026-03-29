package com.aiticketing.service;

import java.util.List;

import com.aiticketing.bean.request.AdminOverrideRequestBean;
import com.aiticketing.bean.request.CreateTicketRequestBean;
import com.aiticketing.bean.request.TicketCommentRequestBean;
import com.aiticketing.bean.request.UpdateTicketStatusRequestBean;
import com.aiticketing.bean.request.UpdateVagueTicketRequestBean;
import com.aiticketing.bean.response.AdminOverrideResponseBean;
import com.aiticketing.bean.response.ConfirmedDuplicateTicketResponseBean;
import com.aiticketing.bean.response.PrimaryLinkedTicketResponseBean;
import com.aiticketing.bean.response.TicketCommentResponseBean;
import com.aiticketing.bean.response.TicketResponseBean;
import com.aiticketing.bean.response.TicketTextVersionResponseBean;
import com.aiticketing.bean.response.UserTicketResponseBean;

public interface TicketService {
	
	//Users Tickets
	UserTicketResponseBean createTicket(Long userId, CreateTicketRequestBean createTicketReq);
	List<UserTicketResponseBean> listTicketsForUser(Long userId);
	UserTicketResponseBean getTicketByIdForUser(Long ticketId, Long userId);
	
	//Agent-Admin Tickets
	List<TicketResponseBean> listAllTicketsForAdmin();
	List<TicketResponseBean> listAssignedTicketsForAgent(Long agentUserId);
	TicketResponseBean getTicketById(Long ticketId);

	TicketCommentResponseBean addTicketComment(Long authorUserId, Long ticketId, TicketCommentRequestBean req);
	List<TicketCommentResponseBean> listTicketComments(Long viewerUserId, Long ticketId);
	AdminOverrideResponseBean applyAdminOverride(Long adminUserId, Long ticketId, AdminOverrideRequestBean req);
	TicketResponseBean updateTicketStatusByAgent(Long agentUserId, Long ticketId, UpdateTicketStatusRequestBean req);
	UserTicketResponseBean clarifyVagueTicket(Long userId, Long ticketId, UpdateVagueTicketRequestBean req);
	List<TicketTextVersionResponseBean> getTicketHistory(Long ticketId);

	List<ConfirmedDuplicateTicketResponseBean> getConfirmedDuplicates(Long primaryTicketId);
	PrimaryLinkedTicketResponseBean getPrimaryLink(Long duplicateTicketId);
}