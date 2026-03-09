package com.aiticketing.service;

import java.time.OffsetDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aiticketing.ai.Taxonomy;
import com.aiticketing.bean.request.AdminOverrideRequestBean;
import com.aiticketing.bean.request.CreateTicketRequestBean;
import com.aiticketing.bean.request.TicketCommentRequestBean;
import com.aiticketing.bean.request.UpdateTicketStatusRequestBean;
import com.aiticketing.bean.request.UpdateVagueTicketRequestBean;
import com.aiticketing.bean.response.AdminOverrideResponseBean;
import com.aiticketing.bean.response.TicketCommentResponseBean;
import com.aiticketing.bean.response.TicketResponseBean;
import com.aiticketing.bean.response.TicketTextVersionResponseBean;
import com.aiticketing.bean.response.UserTicketResponseBean;
import com.aiticketing.entity.AdminOverride;
import com.aiticketing.entity.OutboxEvent;
import com.aiticketing.entity.Ticket;
import com.aiticketing.entity.TicketComment;
import com.aiticketing.entity.TicketTextVersion;
import com.aiticketing.entity.User;
import com.aiticketing.entity.enums.AggregateType;
import com.aiticketing.entity.enums.CommentVisibility;
import com.aiticketing.entity.enums.OutboxEventType;
import com.aiticketing.entity.enums.TicketPriority;
import com.aiticketing.entity.enums.TicketStatus;
import com.aiticketing.entity.enums.UserRole;
import com.aiticketing.exception.BadRequestException;
import com.aiticketing.exception.NotFoundException;
import com.aiticketing.exception.UnauthorizedException;
import com.aiticketing.repository.AdminOverrideRepository;
import com.aiticketing.repository.OutboxEventRepository;
import com.aiticketing.repository.TicketCommentRepository;
import com.aiticketing.repository.TicketRepository;
import com.aiticketing.repository.TicketTextVersionRepository;
import com.aiticketing.repository.UserRepository;
import com.aiticketing.utilities.Utility;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service("TicketServiceImpl")
public class TicketServiceImpl implements TicketService {

	@Autowired
	TicketTextVersionRepository ticketTextVersionRepo;

	@Autowired
	OutboxEventRepository outboxEventRepo;

	@Autowired
	ObjectMapper objectMapper;
	
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
		
		//Initial outbox TRIAGE values
		t.setAiFailed(false);
		t.setAiLastError(null);
		t.setAiTriagedAt(null);
		t.setVagueCount(0);
		t.setLastVagueAt(null);
		t.setFirstAssignedAt(null);
		t.setVagueReason(null);
		t.setClarificationPrompt(null);

		t.setCurrentTextVersion(1);
		t.setDuplicateState("NONE");

		OffsetDateTime now = OffsetDateTime.now();
		t.setCreatedAt(now);
		t.setUpdatedAt(now);

		Ticket savedResp = ticketRepo.save(t);

		//1) Insert in ticket_text_versions snapshot v1
	    TicketTextVersion tv = new TicketTextVersion();
	    tv.setTicketId(savedResp.getTicketId());
	    tv.setVersionNo(1);
	    tv.setTicketTitle(savedResp.getTitle());
	    tv.setTicketDescription(savedResp.getDescription());
	    tv.setCreatedByUserId(creator.getUserId());
	    tv.setCreatedAt(now);
	    ticketTextVersionRepo.save(tv);

	    //2)Insert in outbox event table
	    OutboxEvent oe = new OutboxEvent();
	    oe.setEventType(OutboxEventType.TRIAGE_REQUESTED.name());
	    oe.setAggregateType(AggregateType.TICKET.name());
	    oe.setAggregateId(savedResp.getTicketId());
	    oe.setStatus("PENDING");
	    oe.setRetryCount(0);
	    oe.setCreatedAt(now);
	    try {
	        oe.setPayload(objectMapper.writeValueAsString(java.util.Map.of("textVersion", 1)));
	    } catch (Exception e) {
	        oe.setPayload("{\"textVersion\":1}");
	    }
	    outboxEventRepo.save(oe);
	    
		//using fetch join for db response
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
		        String newValue = Taxonomy.normalize(req.newValue);
		        if (newValue == null || newValue.isBlank()) throw new BadRequestException("newValue is required for PRIORITY");
		        
		        TicketPriority p;
		        try {
		            p = TicketPriority.valueOf(newValue);
		        } catch (IllegalArgumentException ex) {
		            throw new BadRequestException("Invalid priority. Allowed: LOW, MEDIUM, HIGH, URGENT");
		        }

		        ticket.setAiPriority(p);
		        newValueForAudit = p.name();		    
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
	        case "PRIORITY" -> ticket.getAiPriority()!= null ? ticket.getAiPriority().name() : null;
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
		
		r.vagueReason = t.getVagueReason();
		r.clarificationPrompt =t.getClarificationPrompt();

		TICKET_SERVICE_LOG.info(
				"TicketServiceImpl :: exit setUserTicketResponseBean() :: ticketId={}, userTicketStatus={}, assignedToName={}, vagueReason={}",
				r.ticketId, r.userTicketStatus, r.assignedToName, r.vagueReason);
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
		
		r.aiFailed = t.getAiFailed();
		r.aiLastError = t.getAiLastError();
		r.aiTriagedAt = t.getAiTriagedAt();
		r.vagueCount = t.getVagueCount();
		r.lastVagueAt = t.getLastVagueAt();
		r.firstAssignedAt = t.getFirstAssignedAt();
		r.vagueReason = t.getVagueReason();
		r.clarificationPrompt = t.getClarificationPrompt();
		
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

	@Transactional
	public UserTicketResponseBean clarifyVagueTicket(Long userId, Long ticketId, UpdateVagueTicketRequestBean req) {
	    TICKET_SERVICE_LOG.info("TicketServiceImpl :: in clarifyVagueTicket() :: userId={} ticketId={}", userId, ticketId);

	    Ticket ticket = ticketRepo.findByIdWithUsers(ticketId)
	            .orElseThrow(() -> new NotFoundException("Ticket not found"));

	    if (ticket.getCreatedBy() == null || !ticket.getCreatedBy().getUserId().equals(userId)) {
	        throw new UnauthorizedException("You are not allowed to update this ticket");
	    }

	    if (ticket.getStatus() != TicketStatus.VAGUE) {
	        throw new BadRequestException("Only vague tickets can be clarified through this endpoint");
	    }

	    OffsetDateTime now = OffsetDateTime.now();

	    //Optional title update
	    String updatedTitle = (req.title == null || req.title.trim().isBlank())
	            ? ticket.getTitle()
	            : req.title.trim();

	    String oldDescription = ticket.getDescription() == null ? "" : ticket.getDescription().trim();
	    String clarificationAnswer = req.clarificationAnswer.trim();

	    //Visible latest description stored in ticket + version table.
	    //adding new line before appending each clarification answer to keep it readable for user/admin, LLM flattening happens later in AiTriageService.safe().
	    String updatedDescription;
	    if (oldDescription.isBlank()) {
	        updatedDescription = clarificationAnswer;
	    } else {
	        updatedDescription = oldDescription + "\n\n" + clarificationAnswer;
	    }

	    ticket.setTitle(updatedTitle);
	    ticket.setDescription(updatedDescription);
	    ticket.setCurrentTextVersion(ticket.getCurrentTextVersion() + 1);
	    ticket.setStatus(TicketStatus.NEW); // waiting for re-triage
	    ticket.setUpdatedAt(now);

	    //Clear current vague guidance so stale prompt is not shown
	    ticket.setVagueReason(null);
	    ticket.setClarificationPrompt(null);
	    ticket.setAiLastError(null);
	    ticket.setAiFailed(false);

	    Ticket saved = ticketRepo.save(ticket);

	    //insert new text version snapshot
	    TicketTextVersion tv = new TicketTextVersion();
	    tv.setTicketId(saved.getTicketId());
	    tv.setVersionNo(saved.getCurrentTextVersion());
	    tv.setTicketTitle(saved.getTitle());
	    tv.setTicketDescription(saved.getDescription());
	    tv.setCreatedByUserId(userId);
	    tv.setCreatedAt(now);
	    ticketTextVersionRepo.save(tv);

	    //enqueue fresh triage request
	    OutboxEvent oe = new OutboxEvent();
	    oe.setEventType("TRIAGE_REQUESTED");
	    oe.setAggregateType("TICKET");
	    oe.setAggregateId(saved.getTicketId());
	    oe.setStatus("PENDING");
	    oe.setRetryCount(0);
	    oe.setCreatedAt(now);

	    try {
	        oe.setPayload(objectMapper.writeValueAsString(java.util.Map.of("textVersion", saved.getCurrentTextVersion())));
	    } catch (Exception e) {
	        oe.setPayload("{\"textVersion\":" + saved.getCurrentTextVersion() + "}");
	    }

	    outboxEventRepo.save(oe);

	    Ticket refreshed = ticketRepo.findByIdWithUsers(saved.getTicketId())
	            .orElseThrow(() -> new NotFoundException("Ticket not found after update"));

	    UserTicketResponseBean resp = setUserTicketResponseBean(refreshed);

	    TICKET_SERVICE_LOG.info("TicketServiceImpl :: exit clarifyVagueTicket() :: ticketId={} version={}",
	            resp.ticketId, refreshed.getCurrentTextVersion());

	    return resp;
	}

	@Transactional(readOnly = true)
	public List<TicketTextVersionResponseBean> getTicketHistory(Long ticketId) {
	    TICKET_SERVICE_LOG.info("TicketServiceImpl :: in getTicketHistory() :: ticketId={}", ticketId);

	    ticketRepo.findById(ticketId)
	            .orElseThrow(() -> new NotFoundException("Ticket not found"));

	    List<TicketTextVersionResponseBean> resp = ticketTextVersionRepo
	            .findByTicketIdOrderByVersionNoDesc(ticketId)
	            .stream()
	            .map(this::mapToTicketTextVersionResp)
	            .toList();

	    TICKET_SERVICE_LOG.info("TicketServiceImpl :: exit getTicketHistory() :: count={}", resp.size());
	    return resp;
	}
	
	private TicketTextVersionResponseBean mapToTicketTextVersionResp(TicketTextVersion ttv) {
	    TicketTextVersionResponseBean r = new TicketTextVersionResponseBean();
	    r.versionId = ttv.getVersionId();
	    r.ticketId = ttv.getTicketId();
	    r.versionNo = ttv.getVersionNo();
	    r.title = ttv.getTicketTitle();
	    r.description = ttv.getTicketDescription();
	    r.createdByUserId = ttv.getCreatedByUserId();
	    r.createdAt = ttv.getCreatedAt();
	    return r;
	}
}