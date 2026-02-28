package com.aiticketing.service;

import java.time.OffsetDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aiticketing.bean.request.AdminOverrideRequestBean;
import com.aiticketing.bean.request.CreateTicketRequestBean;
import com.aiticketing.bean.request.TicketCommentRequestBean;
import com.aiticketing.bean.request.UpdateTicketStatusRequestBean;
import com.aiticketing.bean.response.AdminOverrideResponseBean;
import com.aiticketing.bean.response.TicketCommentResponseBean;
import com.aiticketing.bean.response.TicketResponseBean;
import com.aiticketing.bean.response.UserTicketResponseBean;
import com.aiticketing.entity.AdminOverride;
import com.aiticketing.entity.CommentVisibility;
import com.aiticketing.entity.Ticket;
import com.aiticketing.entity.TicketComment;
import com.aiticketing.entity.TicketStatus;
import com.aiticketing.entity.User;
import com.aiticketing.entity.UserRole;
import com.aiticketing.exception.BadRequestException;
import com.aiticketing.exception.NotFoundException;
import com.aiticketing.exception.UnauthorizedException;
import com.aiticketing.repository.AdminOverrideRepository;
import com.aiticketing.repository.TicketCommentRepository;
import com.aiticketing.repository.TicketRepository;
import com.aiticketing.repository.UserRepository;
import com.aiticketing.utilities.Utility;

@Service("TicketServiceImpl")
public class TicketServiceImpl implements TicketService {

	@Autowired
	TicketCommentRepository ticketCommentRepo;
	
	@Autowired
	AdminOverrideRepository adminOverrideRepo;
	
	@Autowired
	TicketRepository ticketRepo;

	@Autowired
	UserRepository userRepo;

	private static final Logger TICKET_SERVICE_LOG = LoggerFactory.getLogger(TicketServiceImpl.class);

	//START - USER ROLE
	@Transactional
	public UserTicketResponseBean createTicket(Long userId, CreateTicketRequestBean createTicketReq) {
		TICKET_SERVICE_LOG.info("TicketServiceImpl :: in createTicket() :: createTicketReq {}",
				createTicketReq.toString());
		User creator = userRepo.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));

		Ticket t = new Ticket();
		t.setTitle(createTicketReq.title.trim());
		t.setDescription(createTicketReq.description.trim());
		t.setStatus(TicketStatus.NEW);
		t.setCreatedBy(creator);

		t.setCurrentTextVersion(1);
		t.setDuplicateState("NONE");

		OffsetDateTime now = OffsetDateTime.now();
		t.setCreatedAt(now);
		t.setUpdatedAt(now);

		Ticket savedResp = ticketRepo.save(t);

		// using fetch join
		Ticket fetchTicket = ticketRepo.findByIdWithUsers(savedResp.getTicketId())
				.orElseThrow(() -> new NotFoundException("Ticket not found after create"));

		UserTicketResponseBean resp = setUserTicketResponseBean(fetchTicket);

		TICKET_SERVICE_LOG.info(
				"TicketServiceImpl :: exit createTicket() :: resp ticketId={} userTicketStatus={} , createdByUserId={}",
				resp.ticketId, resp.userTicketStatus, resp.createdByUserId);
		return resp;
	}

	@Transactional(readOnly = true)
	public UserTicketResponseBean getTicketByIdForUser(Long ticketId, Long userId) {
		TICKET_SERVICE_LOG.info("TicketServiceImpl :: in getTicketByIdForUser() :: ticketId {}", ticketId);
		Ticket t = ticketRepo.findByIdWithUsers(ticketId).orElseThrow(() -> new NotFoundException("Ticket not found"));

		// Ownership check so a user can't fetch someone else's ticket
		if (t.getCreatedBy() == null || t.getCreatedBy().getUserId() == null
				|| !t.getCreatedBy().getUserId().equals(userId)) {
			throw new UnauthorizedException("You are not allowed to view this ticket");
		}

		UserTicketResponseBean resp = setUserTicketResponseBean(t);
		TICKET_SERVICE_LOG.info(
				"TicketServiceImpl :: exit getTicketByIdForUser() :: ticketId={}, userTicketStatus={}, createdBy={}, assignedToName={}",
				resp.ticketId, resp.userTicketStatus, resp.createdByUserId, resp.assignedToName);
		return resp;
	}

	@Transactional(readOnly = true)
	public TicketResponseBean getTicketById(Long ticketId) {
		TICKET_SERVICE_LOG.info("TicketServiceImpl :: in getTicketById() :: ticketId {}", ticketId);
		Ticket t = ticketRepo.findByIdWithUsers(ticketId).orElseThrow(() -> new NotFoundException("Ticket not found"));
		TicketResponseBean resp = setTicketResponseBean(t);
		TICKET_SERVICE_LOG.info(
				"TicketServiceImpl :: exit getTicketById() :: ticketId={}, status={}, createdBy={}, assignedTo={}",
				resp.ticketId, resp.status, resp.createdByUserId, resp.assignedToUserId);
		return resp;
	}

	@Transactional(readOnly = true)
	public List<UserTicketResponseBean> listTicketsForUser(Long userId) {
		TICKET_SERVICE_LOG.info("TicketServiceImpl :: in listTicketsForUser() :: userId {}", userId);
		userRepo.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));
		List<UserTicketResponseBean> list = ticketRepo.findByCreatorWithUsers(userId).stream()
				.map(this::setUserTicketResponseBean).toList();

		TICKET_SERVICE_LOG.info("TicketServiceImpl :: exit listTicketsForUser() :: userId={}, count={}", userId,
				list.size());
		return list;
	}

	@Transactional(readOnly = true)
	public List<TicketResponseBean> listAllTicketsForAdmin() {
		TICKET_SERVICE_LOG.info("TicketServiceImpl :: in listAllTicketsForAdmin()");

		List<TicketResponseBean> list = ticketRepo.findAllWithUsers().stream().map(this::setTicketResponseBean)
				.toList();

		TICKET_SERVICE_LOG.info("TicketServiceImpl :: exit listAllTicketsForAdmin :: count={}", list.size());
		return list;
	}

	@Transactional(readOnly = true)
	public List<TicketResponseBean> listAssignedTicketsForAgent(Long agentUserId) {
		TICKET_SERVICE_LOG.info("TicketServiceImpl :: in listAssignedTicketsForAgent() :: agentUserId {}", agentUserId);
		userRepo.findById(agentUserId).orElseThrow(() -> new NotFoundException("User not found"));
		List<TicketResponseBean> list = ticketRepo.findTicketsAssignedToAgent(agentUserId).stream()
				.map(this::setTicketResponseBean).toList();

		TICKET_SERVICE_LOG.info("TicketServiceImpl :: exit listTicketsForUser() :: agentUserId={}, count={}",
				agentUserId, list.size());
		return list;
	}

	//START - Ticket Comment
	@Transactional
	public TicketCommentResponseBean addTicketComment(Long authorUserId, Long ticketId, TicketCommentRequestBean req) {
	    TICKET_SERVICE_LOG.info("TicketServiceImpl :: in addTicketComment() :: authorUserId={}, ticketId={}", authorUserId, ticketId);

	    User author = userRepo.findById(authorUserId).orElseThrow(() -> new NotFoundException("User not found"));
	    Ticket ticket = ticketRepo.findById(ticketId).orElseThrow(() -> new NotFoundException("Ticket not found"));

	    //Decide visibility
	    CommentVisibility visibility = (req.visibility != null) ? req.visibility : CommentVisibility.PUBLIC;

	    //USER cannot create INTERNAL notes/comments
	    if (author.getRole() == null) {
	        throw new BadRequestException("User role missing");
	    }
	    switch (author.getRole()) {
	        case USER -> visibility = CommentVisibility.PUBLIC;
	        case AGENT, ADMIN -> { /* allow chosen visibility */ }
	    }
	    
	    TicketComment c = new TicketComment();
	    c.setTicket(ticket);
	    c.setAuthor(author);
	    c.setBody(req.body.trim());
	    c.setVisibility(visibility);

	    TicketComment saved = ticketCommentRepo.save(c);

	    TicketComment fetched = ticketCommentRepo.findById(saved.getCommentId())
	            .orElseThrow(() -> new NotFoundException("Comment not found after create"));

	    TicketCommentResponseBean resp = mapToCommentResp(fetched);

	    TICKET_SERVICE_LOG.info("TicketServiceImpl :: exit addTicketComment() :: commentId={}, visibility={}",
	            resp.commentId, resp.visibility);
	    
	    return resp;
	}
	
	@Transactional(readOnly = true)
	public List<TicketCommentResponseBean> listTicketComments(Long viewerUserId, Long ticketId) {
	    TICKET_SERVICE_LOG.info("TicketServiceImpl :: in listTicketComments() :: viewerUserId={}, ticketId={}",viewerUserId, ticketId);

	    User viewer = userRepo.findById(viewerUserId)
	            .orElseThrow(() -> new NotFoundException("User not found"));
	    
	    //ensure ticket exists
	    Ticket t = ticketRepo.findById(ticketId).orElseThrow(() -> new NotFoundException("Ticket not found"));

	    List<TicketComment> comments;
	    if (viewer.getRole() == null) {
	        throw new BadRequestException("User role missing");
	    }

	    if (viewer.getRole() == UserRole.USER) {
	    	if (viewer.getUserId()!=t.getCreatedBy().getUserId())
	    		throw new UnauthorizedException("Unauthorized for this ticket");
	        comments = ticketCommentRepo.findByTicketIdAndVisibilityWithAuthor(ticketId, CommentVisibility.PUBLIC.name());
	    } else {
	        comments = ticketCommentRepo.findByTicketIdWithAuthor(ticketId);
	    }
	    
	    List<TicketCommentResponseBean> resp = comments.stream()
	            .map(this::mapToCommentResp)
	            .toList();

	    TICKET_SERVICE_LOG.info("TicketServiceImpl :: exit listTicketComments() :: count={}", resp.size());
	    return resp;
	}
	//END - Ticket Comment

	//START - ADMIN OVERRIDE
	@Transactional
	public AdminOverrideResponseBean applyAdminOverride(Long adminUserId, Long ticketId, AdminOverrideRequestBean req) {
	    TICKET_SERVICE_LOG.info("TicketServiceImpl :: in applyAdminOverride() :: adminUserId={}, ticketId={}", adminUserId, ticketId);

	    User admin = userRepo.findById(adminUserId).orElseThrow(() -> new NotFoundException("User not found"));
	    Ticket ticket = ticketRepo.findByIdWithUsers(ticketId).orElseThrow(() -> new NotFoundException("Ticket not found"));

	    String type = (req.overrideType == null) ? "" : req.overrideType.trim().toUpperCase();

	    String oldValue = resolveOldValue(ticket, type);
	    String newValueForAudit = null;
	    
	    switch(type) {
			case "STATUS" -> {
		        String newValue = (req.newValue == null) ? "" : req.newValue.trim().toUpperCase();
		        if (newValue.isBlank()) throw new BadRequestException("newValue is required for STATUS");
		        ticket.setStatus(TicketStatus.valueOf(newValue));
		        newValueForAudit = newValue;
		    }
		
		    case "CATEGORY" -> {
		        String newValue = (req.newValue == null) ? "" : req.newValue.trim();
		        if (newValue.isBlank()) throw new BadRequestException("newValue is required for CATEGORY");
		        ticket.setAiCategory(newValue);
		        newValueForAudit = newValue;
		    }
		
		    case "PRIORITY" -> {
		        String newValue = (req.newValue == null) ? "" : req.newValue.trim();
		        if (newValue.isBlank()) throw new BadRequestException("newValue is required for PRIORITY");
		        ticket.setAiPriority(newValue);
		        newValueForAudit = newValue;
		    }
		
		    case "DUPLICATE_LINK" -> {
		        String newValue = (req.newValue == null) ? "" : req.newValue.trim().toUpperCase();
		        if (newValue.isBlank()) throw new BadRequestException("newValue is required for DUPLICATE_LINK");
		        ticket.setDuplicateState(newValue); // NONE/POTENTIAL/CONFIRMED
		        newValueForAudit = newValue;
		    }
		
		    case "ASSIGNMENT" -> {
		        // null => unassign
		        Long assigneeId = req.newAssignedToUserId;
		
		        User assignee = null;
		        if (assigneeId != null) {
		            assignee = userRepo.findById(assigneeId)
		                    .orElseThrow(() -> new NotFoundException("Assignee user not found"));
		
		            // if (assignee.getRole() != UserRole.AGENT) throw new BadRequestException("Assignee must be AGENT");
		        }
		
		        ticket.setAssignedTo(assignee);
		        newValueForAudit = (assignee == null) ? null : String.valueOf(assignee.getUserId());
		    }
		
		    default -> throw new BadRequestException("Unsupported overrideType: " + type);
		}

	    ticket.setUpdatedAt(OffsetDateTime.now());
	    ticketRepo.save(ticket);

	    // Audit trail row
	    AdminOverride ao = new AdminOverride();
	    ao.setTicket(ticket);
	    ao.setOverriddenBy(admin);
	    ao.setOverrideType(type);
	    ao.setOldValue(oldValue);
	    ao.setNewValue(newValueForAudit);
	    ao.setReason(req.reason != null ? req.reason.trim() : null);
	    ao.setCreatedAt(OffsetDateTime.now());

	    AdminOverride saved = adminOverrideRepo.save(ao);

	    AdminOverrideResponseBean resp = mapToOverrideResp(saved);

	    TICKET_SERVICE_LOG.info("TicketServiceImpl :: exit applyAdminOverride() :: overrideId={}", resp.overrideId);
	    return resp;
	}

	@Transactional
	public TicketResponseBean updateTicketStatusByAgent(Long agentUserId, Long ticketId, UpdateTicketStatusRequestBean req) {
	    TICKET_SERVICE_LOG.info("TicketServiceImpl :: in updateTicketStatusByAgent() :: agentUserId={}, ticketId={}, req={}",
	            agentUserId, ticketId, req);

	    userRepo.findById(agentUserId).orElseThrow(() -> new NotFoundException("User not found"));

	    Ticket t = ticketRepo.findByIdWithUsers(ticketId)
	            .orElseThrow(() -> new NotFoundException("Ticket not found"));

	    if (t.getAssignedTo() == null || !t.getAssignedTo().getUserId().equals(agentUserId)) {
	        throw new UnauthorizedException("Ticket is not assigned to this agent");
	    }

	    //Only agent-meaningful statuses allowed
	    if (!isAllowedAgentStatus(req.status)) {
	        throw new BadRequestException("Agent cannot set status to " + req.status);
	    }

	    t.setStatus(req.status);
	    t.setUpdatedAt(OffsetDateTime.now());
	    ticketRepo.save(t);

//	    //Add the note as a comment when status changes
//	    if (req.note != null && !req.note.trim().isEmpty()) {
//	        TicketComment c = new TicketComment();
//	        c.setTicket(t);
//	        c.setAuthor(t.getAssignedTo()); // agent user
//	        c.setBody(req.note.trim());
//	        c.setCreatedAt(OffsetDateTime.now());
//	        ticketCommentRepo.save(c);
//	    }

	    Ticket refreshed = ticketRepo.findByIdWithUsers(ticketId)
	            .orElseThrow(() -> new NotFoundException("Ticket not found after update"));

	    TicketResponseBean resp = setTicketResponseBean(refreshed);

	    TICKET_SERVICE_LOG.info("TicketServiceImpl :: exit updateTicketStatusByAgent() :: ticketId={}, newStatus={}",
	            resp.ticketId, resp.status);
	    return resp;
	}

	private boolean isAllowedAgentStatus(TicketStatus status) {
	    return status == TicketStatus.IN_PROGRESS
	            || status == TicketStatus.RESOLVED
	            || status == TicketStatus.CLOSED;
	}
	
	private String resolveOldValue(Ticket ticket, String type) {
	    return switch (type) {
	        case "STATUS" -> ticket.getStatus() != null ? ticket.getStatus().name() : null;
	        case "CATEGORY" -> ticket.getAiCategory();
	        case "PRIORITY" -> ticket.getAiPriority();
	        case "DUPLICATE_LINK" -> ticket.getDuplicateState();
	        case "ASSIGNMENT" -> ticket.getAssignedTo() != null ? String.valueOf(ticket.getAssignedTo().getUserId()) : null;
	        default -> null;
	    };
	}
	
	private TicketCommentResponseBean mapToCommentResp(TicketComment c) {
	    TicketCommentResponseBean r = new TicketCommentResponseBean();
	    r.commentId = c.getCommentId();
	    r.ticketId = c.getTicket().getTicketId();
	    r.body = c.getBody();
	    r.visibility = c.getVisibility();
	    r.createdAt = c.getCreatedAt();

	    User a = c.getAuthor();
	    if (a != null) {
	        r.authorUserId = a.getUserId();
	        r.authorName = a.getUsername();
	        r.authorEmail = a.getEmail();
	    }
	    return r;
	}

	private AdminOverrideResponseBean mapToOverrideResp(AdminOverride ao) {
	    AdminOverrideResponseBean r = new AdminOverrideResponseBean();
	    r.overrideId = ao.getOverrideId();
	    r.ticketId = ao.getTicket().getTicketId();
	    r.overrideType = ao.getOverrideType();
	    r.oldValue = ao.getOldValue();
	    r.newValue = ao.getNewValue();
	    r.reason = ao.getReason();
	    r.createdAt = ao.getCreatedAt();

	    User u = ao.getOverriddenBy();
	    if (u != null) {
	        r.overriddenByUserId = u.getUserId();
	        r.overriddenByName = u.getUsername();
	        r.overriddenByEmail = u.getEmail();
	    }
	    return r;
	}
	
	private UserTicketResponseBean setUserTicketResponseBean(Ticket t) {
		TICKET_SERVICE_LOG.info("TicketServiceImpl :: in setUserTicketResponseBean()");
		UserTicketResponseBean r = new UserTicketResponseBean();
		r.ticketId = t.getTicketId();
		r.title = t.getTitle();
		r.description = t.getDescription();

		r.userTicketStatus = Utility.mapinternalTicketStatustoUserStatus(
				t.getStatus(), t.getDuplicateState());

		r.createdAt = t.getCreatedAt();
		r.updatedAt = t.getUpdatedAt();

		User creator = t.getCreatedBy();
		if (creator != null) {
			r.createdByUserId = creator.getUserId();
			r.createdByName = creator.getUsername();
			r.createdByEmail = creator.getEmail();
		}

		if (t.getAssignedTo() != null) {
			r.assignedToName = t.getAssignedTo().getUsername();
		}

		TICKET_SERVICE_LOG.info(
				"TicketServiceImpl :: exit setUserTicketResponseBean() :: ticketId={}, userTicketStatus={}, assignedToName={}",
				r.ticketId, r.userTicketStatus, r.assignedToName);
		return r;
	}

	private TicketResponseBean setTicketResponseBean(Ticket t) {
		TICKET_SERVICE_LOG.info("TicketServiceImpl :: in setTicketResponseBean()");
		TicketResponseBean r = new TicketResponseBean();
		r.ticketId = t.getTicketId();
		r.title = t.getTitle();
		r.description = t.getDescription();
		r.status = t.getStatus();

		r.userTicketStatus = Utility.mapinternalTicketStatustoUserStatus(
				t.getStatus(), t.getDuplicateState());

		r.createdAt = t.getCreatedAt();
		r.updatedAt = t.getUpdatedAt();

		User creator = t.getCreatedBy();
		if (creator != null) {
			r.createdByUserId = creator.getUserId();
			r.createdByName = creator.getUsername();
			r.createdByEmail = creator.getEmail();
		}

		//AI fields from DB
		r.aiCategory = t.getAiCategory();
		r.aiPriority = t.getAiPriority();
		r.aiConfidence = t.getAiConfidence();
		r.currentTextVersion = t.getCurrentTextVersion();
		r.duplicateState = t.getDuplicateState();

		User assigned = t.getAssignedTo();
		if (assigned != null) {
			r.assignedToUserId = assigned.getUserId();
			r.assignedToName = assigned.getUsername();
			r.assignedToEmail = assigned.getEmail();
		}

		TICKET_SERVICE_LOG.info(
				"TicketServiceImpl :: exit setTicketResponseBean() :: ticketId={}, status={}, assignedToUserId={}",
				r.ticketId, r.status, r.assignedToUserId);
		return r;
	}

}
