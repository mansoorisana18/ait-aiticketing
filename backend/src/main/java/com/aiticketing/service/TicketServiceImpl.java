package com.aiticketing.service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

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
import com.aiticketing.bean.response.ConfirmedDuplicateTicketResponseBean;
import com.aiticketing.bean.response.EligibleAgentResponseBean;
import com.aiticketing.bean.response.PrimaryLinkedTicketResponseBean;
import com.aiticketing.bean.response.TicketCommentResponseBean;
import com.aiticketing.bean.response.TicketResponseBean;
import com.aiticketing.bean.response.TicketTextVersionResponseBean;
import com.aiticketing.bean.response.UserTicketResponseBean;
import com.aiticketing.entity.AdminOverride;
import com.aiticketing.entity.OutboxEvent;
import com.aiticketing.entity.Ticket;
import com.aiticketing.entity.TicketComment;
import com.aiticketing.entity.TicketDuplicateLink;
import com.aiticketing.entity.TicketTextVersion;
import com.aiticketing.entity.User;
import com.aiticketing.entity.enums.AggregateType;
import com.aiticketing.entity.enums.AiDecisionType;
import com.aiticketing.entity.enums.CommentVisibility;
import com.aiticketing.entity.enums.DuplicateLinkStatus;
import com.aiticketing.entity.enums.DuplicateLinkType;
import com.aiticketing.entity.enums.DuplicateState;
import com.aiticketing.entity.enums.OutboxEventType;
import com.aiticketing.entity.enums.TicketPriority;
import com.aiticketing.entity.enums.TicketStatus;
import com.aiticketing.entity.enums.UserRole;
import com.aiticketing.exception.BadRequestException;
import com.aiticketing.exception.NotFoundException;
import com.aiticketing.exception.UnauthorizedException;
import com.aiticketing.repository.AdminOverrideRepository;
import com.aiticketing.repository.AiDecisionRepository;
import com.aiticketing.repository.OutboxEventRepository;
import com.aiticketing.repository.TicketCommentRepository;
import com.aiticketing.repository.TicketDuplicateLinkRepository;
import com.aiticketing.repository.TicketRepository;
import com.aiticketing.repository.TicketTextVersionRepository;
import com.aiticketing.repository.UserRepository;
import com.aiticketing.utilities.Utility;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service("TicketServiceImpl")
public class TicketServiceImpl implements TicketService {
	
	@Autowired
	AiDecisionRepository aiDecisionRepo;
	
	@Autowired
	TicketDuplicateLinkRepository duplicateLinkRepo;

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
		t.setCurrentTriageStartedAt(null);
		t.setAiTriagedAt(null);
		t.setVagueCount(0);
		t.setLastVagueAt(null);
		t.setFirstAssignedAt(null);
		t.setVagueReason(null);
		t.setClarificationPrompt(null);

		t.setCurrentTextVersion(1);
		t.setDuplicateState(DuplicateState.NONE.name());

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
	
	@Transactional(readOnly = true)
	public List<EligibleAgentResponseBean> getEligibleAgentsForTicket(Long ticketId) {
	    TICKET_SERVICE_LOG.info("TicketServiceImpl :: in getEligibleAgentsForTicket() :: ticketId={}", ticketId);

	    Ticket ticket = ticketRepo.findById(ticketId)
	            .orElseThrow(() -> new NotFoundException("Ticket not found"));

	    String category = ticket.getAiCategory();
	    if (category == null || category.isBlank()) {
	        throw new BadRequestException("Ticket category is not available for eligible-agent lookup");
	    }

	    //Only AGENT users from the same department/category are eligible
	    List<User> agents = userRepo.findByRoleAndDepartment(UserRole.AGENT, category);

	    List<EligibleAgentResponseBean> resp = agents.stream()
	            .map(this::mapToEligibleAgentResp)
	            .collect(Collectors.toList());

	    TICKET_SERVICE_LOG.info("TicketServiceImpl :: exit getEligibleAgentsForTicket() :: ticketId={}, count={}",
	            ticketId, resp.size());

	    return resp;
	}
	
	//START - DUPLICATE CHECK
	@Transactional(readOnly = true)
	public List<ConfirmedDuplicateTicketResponseBean> getConfirmedDuplicates(Long primaryTicketId) {
	    TICKET_SERVICE_LOG.info("TicketServiceImpl :: in getConfirmedDuplicates() :: primaryTicketId={}", primaryTicketId);

	    ticketRepo.findById(primaryTicketId)
	            .orElseThrow(() -> new NotFoundException("Primary ticket not found"));

	    List<TicketDuplicateLink> links =
	            duplicateLinkRepo.findByPrimaryTicket_TicketIdAndDuplicateTypeAndLinkStatus(
	                    primaryTicketId,
	                    DuplicateLinkType.CONFIRMED.name(),
	                    DuplicateLinkStatus.ACTIVE.name()
	            );

	    List<ConfirmedDuplicateTicketResponseBean> resp = links.stream()
	            .map(link -> mapToConfirmedDuplicateResp(link.getDuplicateTicket(), link))
	            .toList();

	    TICKET_SERVICE_LOG.info("TicketServiceImpl :: exit getConfirmedDuplicates() :: primaryTicketId={}, count={}",
	            primaryTicketId, resp.size());

	    return resp;
	}
	
	@Transactional(readOnly = true)
	public PrimaryLinkedTicketResponseBean getPrimaryLink(Long duplicateTicketId) {
	    TICKET_SERVICE_LOG.info("TicketServiceImpl :: in getPrimaryLink() :: duplicateTicketId={}", duplicateTicketId);

	    ticketRepo.findById(duplicateTicketId)
	            .orElseThrow(() -> new NotFoundException("Ticket not found"));

	    TicketDuplicateLink link = duplicateLinkRepo
	            .findFirstByDuplicateTicket_TicketIdAndDuplicateTypeAndLinkStatus(
	                    duplicateTicketId,
	                    DuplicateLinkType.CONFIRMED.name(),
	                    DuplicateLinkStatus.ACTIVE.name()
	            )
	            .orElseThrow(() -> new NotFoundException("No active confirmed primary link found"));

	    PrimaryLinkedTicketResponseBean resp = mapToPrimaryLinkedResp(link);

	    TICKET_SERVICE_LOG.info("TicketServiceImpl :: exit getPrimaryLink() :: duplicateTicketId={}, primaryTicketId={}",
	            duplicateTicketId, resp.primaryTicketId);

	    return resp;
	}
	//END - DUPLICATE CHECK

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
		        newValueForAudit = handleStatusOverride(ticket, req);
		    }
		
		    case "CATEGORY" -> {		        
		        newValueForAudit = handleCategoryOverride(ticket, req);
		    }
		
		    case "PRIORITY" -> {		        
		        newValueForAudit = handlePriorityOverride(ticket, req);		    
		    }
		
		    //Allowed Overrides: POTENTIAL -> NONE, POTENTIAL -> CONFIRMED, CONFIRMED -> NONE
		    case "DUPLICATE_LINK" -> {
		        String newValue = (req.newValue == null) ? "" : req.newValue.trim().toUpperCase();
		        if (newValue.isBlank()) throw new BadRequestException("newValue is required for DUPLICATE_LINK");
		        handleDuplicateOverride(ticket, req);
		        newValueForAudit = newValue;
		    }
		
		    case "ASSIGNMENT" -> {		        
		        newValueForAudit = handleAssignmentOverride(ticket, req);
		    }
		
		    default -> throw new BadRequestException("Unsupported overrideType: " + type);
		}

	    ticket.setUpdatedAt(OffsetDateTime.now());
	    ticketRepo.save(ticket);

	    //Audit trail row
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
	    
	    //DUPLICATE ticket final resolution status propagation through Primary ticket 	    
	    if (req.status == TicketStatus.RESOLVED || req.status == TicketStatus.CLOSED) {
	        propagateFinalStatusToConfirmedDuplicates(t, req.status, t.getAssignedTo());
	    }

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
	
	//If primary ticket status is changed by agent to (RESOLVED/CLOSED), we also set the same user status of its linked duplicate tickets. The internal status is DUPLICATE only of those for ADMIN/AGENT
	//Also add a system PUBLIC comment for the duplicates
	private void propagateFinalStatusToConfirmedDuplicates(Ticket primaryTicket, TicketStatus finalStatus, User actingUser) {
		TICKET_SERVICE_LOG.info("TicketServiceImpl :: in propagateFinalStatusToConfirmedDuplicates() :: agentUserId={}, ticketId={}", actingUser.getUserId(), primaryTicket.getTicketId());
		List<TicketDuplicateLink> confirmedLinks =
	            duplicateLinkRepo.findByPrimaryTicket_TicketIdAndDuplicateTypeAndLinkStatusAndPropagateResolution(
	                    primaryTicket.getTicketId(),
	                    DuplicateLinkType.CONFIRMED.name(),
	                    DuplicateLinkStatus.ACTIVE.name(),
	                    true
	            );

	    if (confirmedLinks.isEmpty()) {
	    	TICKET_SERVICE_LOG.info("TicketServiceImpl :: exit propagateFinalStatusToConfirmedDuplicates() :: No DUPLICATEs found for ticketId={}", primaryTicket.getTicketId());
	        return;
	    }

	    OffsetDateTime now = OffsetDateTime.now();

	    for (TicketDuplicateLink link : confirmedLinks) {
	        Ticket duplicateTicket = link.getDuplicateTicket();
	        duplicateTicket.setStatus(finalStatus);
	        duplicateTicket.setUpdatedAt(now);
	        ticketRepo.save(duplicateTicket);
	        
	        if (finalStatus == TicketStatus.RESOLVED) {
	        	TicketComment publicComment = new TicketComment();
	        	publicComment.setTicket(duplicateTicket);
	        	publicComment.setAuthor(actingUser);
	        	publicComment.setBody("This ticket was resolved as part of an existing related issue.");
	        	publicComment.setCreatedAt(now);
	        	publicComment.setVisibility(CommentVisibility.PUBLIC);
		        ticketCommentRepo.save(publicComment);
	        	
	        	TicketComment internalComment = new TicketComment();
	        	internalComment.setTicket(duplicateTicket);
	        	internalComment.setAuthor(actingUser);
	        	internalComment.setBody("This ticket was " + finalStatus.name().toLowerCase()
		                + " through linked primary ticket #" + primaryTicket.getTicketId() + ".");
	        	internalComment.setCreatedAt(now);
	        	internalComment.setVisibility(CommentVisibility.INTERNAL);
		        ticketCommentRepo.save(internalComment);
	        }	        
	    }
	    TICKET_SERVICE_LOG.info("TicketServiceImpl :: exit propagateFinalStatusToConfirmedDuplicates() :: Updated status & comment for the {} DUPLICATEs of ticketId={}", confirmedLinks.size(), primaryTicket.getTicketId());
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
	
	//For DUPLICATE CONFIRMED state tickets the user status will be same as that of its primary ticket. However, ADMIN can view DUPLICATE_REVIEW POTENTIAL state & NONE
	private String resolveUserTicketStatus(Ticket ticket) {
	    if (ticket == null) {
	        return "OPEN";
	    }

	    if (ticket.getStatus() == TicketStatus.DUPLICATE
	            && DuplicateState.CONFIRMED.name().equalsIgnoreCase(ticket.getDuplicateState())) {

	        TicketDuplicateLink activeLink = duplicateLinkRepo
	                .findByDuplicateTicket_TicketIdAndLinkStatus(
	                        ticket.getTicketId(),
	                        DuplicateLinkStatus.ACTIVE.name()
	                )
	                .stream()
	                .filter(link -> DuplicateLinkType.CONFIRMED.name().equalsIgnoreCase(link.getDuplicateType()))
	                .findFirst()
	                .orElse(null);

	        if (activeLink != null && activeLink.getPrimaryTicket() != null) {
	            Ticket primaryTicket = ticketRepo.findById(activeLink.getPrimaryTicket().getTicketId())
	                    .orElse(null);

	            if (primaryTicket != null) {
	                return Utility.mapinternalTicketStatustoUserStatus(
	                        primaryTicket.getStatus(),
	                        primaryTicket.getDuplicateState()
	                );
	            }
	        }
	    }

	    return Utility.mapinternalTicketStatustoUserStatus(
	            ticket.getStatus(),
	            ticket.getDuplicateState()
	    );
	}
	
	private String handleStatusOverride(Ticket ticket, AdminOverrideRequestBean req) {
	    TICKET_SERVICE_LOG.info("TicketServiceImpl :: in handleStatusOverride() :: ticketId={}, overrideType={}, newValue={}", ticket.getTicketId(), req.overrideType, req.newValue);
		String newValue = (req.newValue == null) ? "" : req.newValue.trim().toUpperCase();
	    OffsetDateTime now = OffsetDateTime.now();

	    if (newValue.isBlank()) {
	        throw new BadRequestException("newValue is required for STATUS");
	    }

	    TicketStatus newStatus;
	    try {
	        newStatus = TicketStatus.valueOf(newValue);
	    } catch (IllegalArgumentException ex) {
	        throw new BadRequestException("Invalid status value");
	    }

	    TicketStatus currentStatus = ticket.getStatus();

	    //These are set by AI pipeline, so arent allowed as override by admin
	    if (newStatus == TicketStatus.NEW
	            || newStatus == TicketStatus.AI_PROCESSING
	            || newStatus == TicketStatus.VAGUE
	            || newStatus == TicketStatus.DUPLICATE_REVIEW
	            || newStatus == TicketStatus.DUPLICATE) {
	        throw new BadRequestException("STATUS override to " + newStatus.name() + " is not allowed");
	    }

	    //Allowing only forward moving states
	    boolean allowed =
	    		//Admin clearing VAGUE status and resuming the AI pipeline
	            (currentStatus == TicketStatus.VAGUE && newStatus == TicketStatus.READY)
	            //Normal status transitions
	            || (currentStatus == TicketStatus.READY && newStatus == TicketStatus.IN_PROGRESS && ticket.getAssignedTo() != null)
	            || (currentStatus == TicketStatus.READY && newStatus == TicketStatus.RESOLVED)
	            || (currentStatus == TicketStatus.READY && newStatus == TicketStatus.CLOSED)
	            || (currentStatus == TicketStatus.IN_PROGRESS && newStatus == TicketStatus.RESOLVED)
	            || (currentStatus == TicketStatus.IN_PROGRESS && newStatus == TicketStatus.CLOSED)
	            || (currentStatus == TicketStatus.RESOLVED && newStatus == TicketStatus.CLOSED);

	    if (!allowed) {
	        throw new BadRequestException("Invalid STATUS override transition from " + currentStatus + " to " + newStatus);
	    }

	    ticket.setStatus(newStatus);

	    //Admin is overriding AI VAGUE decision, so we resume the next pipeline stage by adding DUPLICATE_CHECK_REQUESTED row in outbox table
	    if (currentStatus == TicketStatus.VAGUE && newStatus == TicketStatus.READY) {
	        OutboxEvent duplicateEvent = new OutboxEvent();
	        duplicateEvent.setEventType(OutboxEventType.DUPLICATE_CHECK_REQUESTED.name());
	        duplicateEvent.setAggregateType(AggregateType.TICKET.name());
	        duplicateEvent.setAggregateId(ticket.getTicketId());
	        try {
	        	duplicateEvent.setPayload(objectMapper.writeValueAsString(java.util.Map.of("textVersion", ticket.getCurrentTextVersion())));
    	    } catch (Exception e) {
    	    	duplicateEvent.setPayload("{}");
    	    }
	        duplicateEvent.setStatus("PENDING");
	        duplicateEvent.setRetryCount(0);
	        duplicateEvent.setCreatedAt(now);
	        outboxEventRepo.save(duplicateEvent);
	    }
	    
	    TICKET_SERVICE_LOG.info("TicketServiceImpl :: exit handleStatusOverride() :: ticketId={}", ticket.getTicketId());
	    return newStatus.name();
	}
	
	private String handleCategoryOverride(Ticket ticket, AdminOverrideRequestBean req) {
		TICKET_SERVICE_LOG.info("TicketServiceImpl :: in handleCategoryOverride() :: ticketId={}, overrideType={}, newValue={}", ticket.getTicketId(), req.overrideType, req.newValue);
		String newCategory = req.newValue == null ? null : req.newValue.trim();
	    OffsetDateTime now = OffsetDateTime.now();

	    if (newCategory == null || newCategory.isBlank()) {
	        throw new BadRequestException("newValue is required for CATEGORY");
	    }

	    //Don't allow category changes for terminal or duplicate-related states
	    //Because these are when: workflow is completed, potential duplicate review is in progress or it is already consolidated duplicate 
	    if (ticket.getStatus() == TicketStatus.CLOSED
	            || ticket.getStatus() == TicketStatus.DUPLICATE_REVIEW
	            || ticket.getStatus() == TicketStatus.DUPLICATE) {
	        throw new BadRequestException("CATEGORY override is not allowed for current ticket status");
	    }

	    // Update category (this affects routing)
	    ticket.setAiCategory(newCategory);

	    //If ticket was already assigned, that assignment will now be invalid because department is derived from category
	    //So 1. clearing assignment to avoid inconsistent state
	    if (ticket.getAssignedTo() != null) {
	        ticket.setAssignedTo(null);
	    }

	    //2. Trigger routing again based on new category, insert ROUTING_REQUESTED row in oe table
	    OutboxEvent routingEvent = new OutboxEvent();
	    routingEvent.setEventType(OutboxEventType.ROUTING_REQUESTED.name());
	    routingEvent.setAggregateType(AggregateType.TICKET.name());
	    routingEvent.setAggregateId(ticket.getTicketId());
	    try {
        	routingEvent.setPayload(objectMapper.writeValueAsString(java.util.Map.of("textVersion", ticket.getCurrentTextVersion())));
	    } catch (Exception e) {
	    	routingEvent.setPayload("{}");
	    }
	    routingEvent.setStatus("PENDING");
	    routingEvent.setRetryCount(0);
	    routingEvent.setCreatedAt(now);

	    outboxEventRepo.save(routingEvent);
	    
	    TICKET_SERVICE_LOG.info("TicketServiceImpl :: exit handleCategoryOverride() :: ticketId={}", ticket.getTicketId());
	    return newCategory;
	}
	
	private String handlePriorityOverride(Ticket ticket, AdminOverrideRequestBean req) {
		TICKET_SERVICE_LOG.info("TicketServiceImpl :: in handlePriorityOverride() :: ticketId={}, overrideType={}, newValue={}", ticket.getTicketId(), req.overrideType, req.newValue);
		String newValue = Taxonomy.normalize(req.newValue);

	    if (newValue == null || newValue.isBlank()) {
	        throw new BadRequestException("newValue is required for PRIORITY");
	    }

	    //No point in modifying closed tickets, so not allowed
	    if (ticket.getStatus() == TicketStatus.CLOSED) {
	        throw new BadRequestException("PRIORITY override is not allowed for CLOSED tickets");
	    }

	    TicketPriority priority;
	    try {
	        priority = TicketPriority.valueOf(newValue);
	    } catch (IllegalArgumentException ex) {
	        throw new BadRequestException("Invalid priority. Allowed: LOW, MEDIUM, HIGH, URGENT");
	    }

	    ticket.setAiPriority(priority);

	    TICKET_SERVICE_LOG.info("TicketServiceImpl :: exit handlePriorityOverride() :: ticketId={}", ticket.getTicketId());
	    return priority.name();
	}
	
	private String handleAssignmentOverride(Ticket ticket, AdminOverrideRequestBean req) {
		TICKET_SERVICE_LOG.info("TicketServiceImpl :: in handleAssignmentOverride() :: ticketId={}, overrideType={}, newValue={}", ticket.getTicketId(), req.overrideType, req.newValue);
		Long assigneeId = req.newAssignedToUserId;

	    //Assignment is not allowed for below ticket statuses:
	    //VAGUE → needs clarification first
	    //DUPLICATE_REVIEW → under admin decision
	    //DUPLICATE → not actionable as it is already linked to primary
	    //CLOSED → terminal state
	    if (ticket.getStatus() == TicketStatus.VAGUE
	            || ticket.getStatus() == TicketStatus.DUPLICATE_REVIEW
	            || ticket.getStatus() == TicketStatus.DUPLICATE
	            || ticket.getStatus() == TicketStatus.CLOSED) {
	        throw new BadRequestException("ASSIGNMENT override is not allowed for current ticket status");
	    }

	    //Allowing unassign
	    if (assigneeId == null) {
	        ticket.setAssignedTo(null);
	        return null;
	    }

	    User assignee = userRepo.findById(assigneeId)
	            .orElseThrow(() -> new NotFoundException("Assignee user not found"));

	    //Ensuring only agents can be assigned
	    if (assignee.getRole() != UserRole.AGENT) {
	        throw new BadRequestException("Assigned user must have AGENT role");
	    }

	    //Preventing cross-department agent assignment errors
	    String ticketCategory = ticket.getAiCategory();
	    String agentDepartment = assignee.getDepartment();

	    if (ticketCategory != null && agentDepartment != null
	            && !ticketCategory.equalsIgnoreCase(agentDepartment)) {
	        throw new BadRequestException("Agent department must match ticket category");
	    }

	    ticket.setAssignedTo(assignee);

	    TICKET_SERVICE_LOG.info("TicketServiceImpl :: exit handleAssignmentOverride() :: ticketId={}", ticket.getTicketId());
	    return String.valueOf(assignee.getUserId());
	}
	
	private void handleDuplicateOverride(Ticket ticket, AdminOverrideRequestBean req) {
	    String newDuplicateState = req.newValue == null ? null : req.newValue.trim().toUpperCase();
	    String currentDuplicateState = ticket.getDuplicateState();
	    TicketStatus currentStatus = ticket.getStatus();
	    OffsetDateTime now = OffsetDateTime.now();

	    TICKET_SERVICE_LOG.info("TicketServiceImpl :: in handleDuplicateOverride() :: ticketId={}, currentDuplicateState={}, currentStatus={}, requestedNewDuplicateState={}, primaryTicketId={}",
	            ticket.getTicketId(), currentDuplicateState, currentStatus, newDuplicateState, req.referenceTicketId);
	    
	    if (newDuplicateState == null || newDuplicateState.isBlank()) {
	        throw new BadRequestException("New duplicate state is required for DUPLICATE_LINK override");
	    }

	    //Allowed Overrides: POTENTIAL -> NONE, POTENTIAL -> CONFIRMED, CONFIRMED -> NONE
	    if (!DuplicateState.NONE.name().equals(newDuplicateState)
	            && !DuplicateState.CONFIRMED.name().equals(newDuplicateState)) {
	        throw new BadRequestException("Duplicate override only supports NONE or CONFIRMED");
	    }
	    
	    if (currentStatus != TicketStatus.DUPLICATE_REVIEW && currentStatus != TicketStatus.DUPLICATE) {
	        throw new BadRequestException("Duplicate override is allowed only for DUPLICATE_REVIEW or DUPLICATE tickets");
	    }

	    if (DuplicateState.POTENTIAL.name().equals(currentDuplicateState)) {
	        if (!DuplicateState.NONE.name().equals(newDuplicateState)
	                && !DuplicateState.CONFIRMED.name().equals(newDuplicateState)) {
	            throw new BadRequestException("POTENTIAL duplicate can only be changed to NONE or CONFIRMED");
	        }
	    } else if (DuplicateState.CONFIRMED.name().equals(currentDuplicateState)) {
	        if (!DuplicateState.NONE.name().equals(newDuplicateState)) {
	            throw new BadRequestException("CONFIRMED duplicate can only be changed to NONE");
	        }
	    } else {
	        throw new BadRequestException("DUPLICATE_LINK override is not supported when current duplicate state is " + currentDuplicateState);
	    }

	    //Fetch current active link for this duplicate ticket. Only one active row
	    List<TicketDuplicateLink> activeLinks =
	            duplicateLinkRepo.findByDuplicateTicket_TicketIdAndLinkStatus(
	                    ticket.getTicketId(),
	                    DuplicateLinkStatus.ACTIVE.name()
	            );

	    //CONFIRMED -> NONE & POTENTIAL -> NONE
	    if (DuplicateState.NONE.name().equals(newDuplicateState)) {
	    	
	    	//Invalidate current ACTIVE row to REJECTED
	    	for (TicketDuplicateLink link : activeLinks) {
		        link.setLinkStatus(DuplicateLinkStatus.REJECTED.name());
		    }
		    if (!activeLinks.isEmpty()) {
		        duplicateLinkRepo.saveAll(activeLinks);
		    }
		    		    
	        ticket.setDuplicateState(DuplicateState.NONE.name());
	        ticket.setStatus(TicketStatus.READY);

	        /*
	         * If the ticket was previously waiting in DUPLICATE_REVIEW or had been
	         * incorrectly marked DUPLICATE, it now becomes actionable again.
	         * Route it only if it is still unassigned
	         */
	        if (ticket.getAssignedTo() == null) {
	            OutboxEvent routingEvent = new OutboxEvent();
	            routingEvent.setEventType(OutboxEventType.ROUTING_REQUESTED.name());
	            routingEvent.setAggregateType(AggregateType.TICKET.name());
	            routingEvent.setAggregateId(ticket.getTicketId());
	            try {
	            	routingEvent.setPayload(objectMapper.writeValueAsString(java.util.Map.of("textVersion", ticket.getCurrentTextVersion())));
	    	    } catch (Exception e) {
	    	    	routingEvent.setPayload("{}");
	    	    }
//	            routingEvent.setPayload(toJson(Map.of("textVersion", ticket.getCurrentTextVersion())));
	            routingEvent.setStatus("PENDING");
	            routingEvent.setRetryCount(0);
	            routingEvent.setCreatedAt(now);
	            outboxEventRepo.save(routingEvent);
	        }

	    } else if (DuplicateState.CONFIRMED.name().equals(newDuplicateState)) {
	        //POTENTIAL -> CONFIRMED
	    	Long primaryTicketId = req.referenceTicketId;
	        if (primaryTicketId == null) {
	            throw new BadRequestException("Primary ticket id is required when overriding duplicate to CONFIRMED");
	        }

	        Ticket primaryTicket = ticketRepo.findById(primaryTicketId)
	                .orElseThrow(() -> new NotFoundException("Primary ticket not found"));

	        if (primaryTicket.getTicketId().equals(ticket.getTicketId())) {
	            throw new BadRequestException("A ticket cannot be a duplicate of itself");
	        }
	        
	        //To ensure primary ticket is only in READY or IN_PROGRESS status
	        if (primaryTicket.getStatus() != TicketStatus.READY
	                && primaryTicket.getStatus() != TicketStatus.IN_PROGRESS) {
	            throw new BadRequestException("Primary ticket must be in READY or IN_PROGRESS state");
	        }
	        
	        //1. Invalidate current ACTIVE row to REJECTED
	    	for (TicketDuplicateLink link : activeLinks) {
		        link.setLinkStatus(DuplicateLinkStatus.REJECTED.name());
		    }
		    if (!activeLinks.isEmpty()) {
		        duplicateLinkRepo.saveAll(activeLinks);
		    }
		    
		    //2. Fetch the old link row of this Primary-Duplicate pair
		    TicketDuplicateLink link = duplicateLinkRepo
	                .findByPrimaryTicket_TicketIdAndDuplicateTicket_TicketId(
	                        primaryTicket.getTicketId(),
	                        ticket.getTicketId()
	                )
	                .orElse(null);

		    //create a row if one does not exist
	        if (link == null) {
	        	link = new TicketDuplicateLink();
		        link.setPrimaryTicket(primaryTicket);
		        link.setDuplicateTicket(ticket);
		        link.setSimilarity(null);
		        link.setCreatedAt(now);
	        }
	         
	        //3. Now update this row with CONFIRMED and ACTIVE link status
	        link.setDuplicateType(DuplicateLinkType.CONFIRMED.name());
	        link.setLinkStatus(DuplicateLinkStatus.ACTIVE.name());
	        link.setPropagateResolution(true);
	        duplicateLinkRepo.save(link);

	        ticket.setDuplicateState(DuplicateState.CONFIRMED.name());
	        ticket.setStatus(TicketStatus.DUPLICATE);
	        ticket.setAssignedTo(null);
	    }
	    
	    TICKET_SERVICE_LOG.info("TicketServiceImpl :: exit handleDuplicateOverride() :: ticketId={}, updatedDuplicateState={}, updatedStatus={}",
	            ticket.getTicketId(), ticket.getDuplicateState(), ticket.getStatus());
	}
	
	private EligibleAgentResponseBean mapToEligibleAgentResp(User user) {
	    EligibleAgentResponseBean resp = new EligibleAgentResponseBean();
	    resp.userId = user.getUserId();
	    resp.username = user.getUsername();
	    resp.email = user.getEmail();
	    resp.department = user.getDepartment();
	    return resp;
	}
	
	private ConfirmedDuplicateTicketResponseBean mapToConfirmedDuplicateResp(Ticket duplicateTicket, TicketDuplicateLink link) {
	    ConfirmedDuplicateTicketResponseBean r = new ConfirmedDuplicateTicketResponseBean();

	    r.ticketId = duplicateTicket.getTicketId();
	    r.title = duplicateTicket.getTitle();

	    User creator = duplicateTicket.getCreatedBy();
	    if (creator != null) {
	        r.createdByUserId = creator.getUserId();
	        r.createdByName = creator.getUsername();
	        r.createdByEmail = creator.getEmail();
	    }

	    r.internalStatus = duplicateTicket.getStatus() == null ? null : duplicateTicket.getStatus().name();
	    r.userTicketStatus = resolveUserTicketStatus(duplicateTicket);
	    r.createdAt = duplicateTicket.getCreatedAt();
	    r.propagateResolution = link.getPropagateResolution();

	    return r;
	}
	
	private PrimaryLinkedTicketResponseBean mapToPrimaryLinkedResp(TicketDuplicateLink link) {
	    PrimaryLinkedTicketResponseBean r = new PrimaryLinkedTicketResponseBean();

	    Ticket primaryTicket = link.getPrimaryTicket();

	    r.primaryTicketId = primaryTicket.getTicketId();
	    r.primaryTicketTitle = primaryTicket.getTitle();
	    r.primaryInternalStatus = primaryTicket.getStatus() == null ? null : primaryTicket.getStatus().name();
	    r.primaryUserTicketStatus = resolveUserTicketStatus(primaryTicket);

	    User assigned = primaryTicket.getAssignedTo();
	    if (assigned != null) {
	        r.assignedAgentUserId = assigned.getUserId();
	        r.assignedAgentName = assigned.getUsername();
	        r.assignedAgentEmail = assigned.getEmail();
	    }

	    r.duplicateType = link.getDuplicateType();
	    r.linkStatus = link.getLinkStatus();
	    r.propagateResolution = link.getPropagateResolution();

	    return r;
	}
	
	private void populateDuplicateDetails(Ticket ticket, TicketResponseBean resp) {
		TICKET_SERVICE_LOG.debug("TicketServiceImpl :: in populateDuplicateDetails() :: ticketId={}", resp.ticketId);
		resp.duplicateState = ticket.getDuplicateState();

	    aiDecisionRepo.findFirstByTicketIdAndDecisionTypeOrderByCreatedAtDesc(
	            ticket.getTicketId(),
	            AiDecisionType.DUPLICATE_CHECK.name()
	    ).ifPresent(ad -> {
	        resp.duplicateConfidence = ad.getConfidence() == null ? null : ad.getConfidence().doubleValue();
	        resp.duplicateSimilarity = ad.getSimilarity() == null ? null : ad.getSimilarity().doubleValue();

	        try {
	            JsonNode node = objectMapper.readTree(ad.getOutputJson());
	            resp.duplicateReason = textOrNull(node, "reason");
	        } catch (Exception ex) {
	            TICKET_SERVICE_LOG.warn(
	                    "TicketServiceImpl :: populateDuplicateDetails() :: failed to parse DUPLICATE_CHECK outputJson :: ticketId={}",
	                    ticket.getTicketId(),
	                    ex
	            );
	        }
	    });

	    duplicateLinkRepo.findFirstByDuplicateTicket_TicketIdAndLinkStatus(
	            ticket.getTicketId(),
	            DuplicateLinkStatus.ACTIVE.name()
	    ).ifPresent(link -> {
	        if (link.getPrimaryTicket() != null) {
	            resp.primaryTicketId = link.getPrimaryTicket().getTicketId();
	            resp.primaryTicketTitle = link.getPrimaryTicket().getTitle();
	        }

	        resp.duplicateLinkType = link.getDuplicateType();
	        resp.duplicateLinkStatus = link.getLinkStatus();
	        resp.propagateResolution = link.getPropagateResolution();
	    });
	    TICKET_SERVICE_LOG.debug("TicketServiceImpl :: exit populateDuplicateDetails() :: ticketId={}", resp.ticketId);
	}
	
	private String textOrNull(JsonNode node, String field) {
	    if (node == null || !node.has(field) || node.get(field).isNull()) {
	        return null;
	    }

	    String value = node.get(field).asText(null);
	    return value == null ? null : value.trim();
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

//		r.userTicketStatus = Utility.mapinternalTicketStatustoUserStatus(t.getStatus(), t.getDuplicateState());

		r.userTicketStatus = resolveUserTicketStatus(t);
		
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

//		r.userTicketStatus = Utility.mapinternalTicketStatustoUserStatus(t.getStatus(), t.getDuplicateState());
		r.userTicketStatus = resolveUserTicketStatus(t);

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

		populateDuplicateDetails(t, r);
		
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