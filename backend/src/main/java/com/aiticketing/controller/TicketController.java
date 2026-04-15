package com.aiticketing.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aiticketing.bean.request.AdminOverrideRequestBean;
import com.aiticketing.bean.request.CreateTicketRequestBean;
import com.aiticketing.bean.request.GenerateKbDraftRequestBean;
import com.aiticketing.bean.request.KbSuggestionResponseRequestBean;
import com.aiticketing.bean.request.ManualKbSuggestionRequestBean;
import com.aiticketing.bean.request.TicketCommentRequestBean;
import com.aiticketing.bean.request.UpdateTicketStatusRequestBean;
import com.aiticketing.bean.request.UpdateVagueTicketRequestBean;
import com.aiticketing.bean.response.AdminOverrideResponseBean;
import com.aiticketing.bean.response.ApiResponseBean;
import com.aiticketing.bean.response.ConfirmedDuplicateTicketResponseBean;
import com.aiticketing.bean.response.EligibleAgentResponseBean;
import com.aiticketing.bean.response.PrimaryLinkedTicketResponseBean;
import com.aiticketing.bean.response.TicketCommentResponseBean;
import com.aiticketing.bean.response.TicketResponseBean;
import com.aiticketing.bean.response.TicketTextVersionResponseBean;
import com.aiticketing.bean.response.UserTicketResponseBean;
import com.aiticketing.security.AuthUserPrincipal;
import com.aiticketing.service.TicketService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

	@Autowired
	TicketService ticketService;

	private static final Logger TICKET_CONTROLLER_LOG = LoggerFactory.getLogger(TicketController.class);

	//USER: Create ticket with user ticket response fields
	@Operation(summary = "Create ticket",
			description = "Creates a new ticket for the given user")
	@ApiResponses(value = {
	        @ApiResponse(
	            responseCode = "201",
	            description = "Ticket created successfully",
	            content = @Content(
	                mediaType = "application/json",
	                schema = @Schema(implementation = ApiResponseBean.class)
	            )
	        ),
	        @ApiResponse(responseCode = "400", description = "Validation error"),
	        @ApiResponse(responseCode = "404", description = "User not found")
	    })
	@PostMapping
	public ResponseEntity<ApiResponseBean<UserTicketResponseBean>> createTicket(
			@AuthenticationPrincipal AuthUserPrincipal principal,
			@Valid @RequestBody CreateTicketRequestBean createTicketReq) {
		
		TICKET_CONTROLLER_LOG.info("TicketController :: in createTicket()");
		Long userId = principal.getUserId();
		UserTicketResponseBean resp = ticketService.createTicket(userId, createTicketReq);
		TICKET_CONTROLLER_LOG.info("TicketController :: exit createTicket()");
		return ResponseEntity.status(201).body(ApiResponseBean.success("Ticket created", resp));
	}
	
	@Operation(
	    summary = "User: clarify a vague ticket",
	    description = "Allows the ticket owner to answer the clarification prompt for a ticket previously marked as VAGUE. A new text version is created and AI triage is requested again."
	)
	@ApiResponses(value = {
	    @ApiResponse(responseCode = "200", description = "Ticket updated and triage re-requested"),
	    @ApiResponse(responseCode = "400", description = "Ticket is not vague or request is invalid"),
	    @ApiResponse(responseCode = "401", description = "Unauthorized"),
	    @ApiResponse(responseCode = "404", description = "Ticket not found")
	})
	@PatchMapping("/user/{ticketId}/clarify")
	public ResponseEntity<ApiResponseBean<UserTicketResponseBean>> clarifyVagueTicket(
	        @PathVariable Long ticketId,
	        @AuthenticationPrincipal AuthUserPrincipal principal,
	        @Valid @RequestBody UpdateVagueTicketRequestBean req) {

	    TICKET_CONTROLLER_LOG.info("TicketController :: in clarifyVagueTicket() :: ticketId={}", ticketId);

	    Long userId = principal.getUserId();
	    UserTicketResponseBean resp = ticketService.clarifyVagueTicket(userId, ticketId, req);

	    TICKET_CONTROLLER_LOG.info("TicketController :: exit clarifyVagueTicket()");
	    return ResponseEntity.ok(ApiResponseBean.success("Ticket updated and triage re-requested", resp));
	}

	//USER: Lists all tickets for a user with user ticket response fields
	@Operation(summary = "List Tickets for User",
	        description = "Returns all tickets created by the given user.")
	@ApiResponses(value = {
	        @ApiResponse(responseCode = "200", description = "Tickets fetched successfully")
	    })
	@GetMapping
	public ResponseEntity<ApiResponseBean<List<UserTicketResponseBean>>> listTicketsForUser(
			@AuthenticationPrincipal AuthUserPrincipal principal) {
		TICKET_CONTROLLER_LOG.info("TicketController :: in listTicketsForUser()");
		Long userId = principal.getUserId();
		List<UserTicketResponseBean> list = ticketService.listTicketsForUser(userId);
		TICKET_CONTROLLER_LOG.info("TicketController :: exit listTicketsForUser()");
		return ResponseEntity.ok(ApiResponseBean.success(list));
	}

	//USER: Get his particular ticket details with user ticket response fields
	@Operation(summary = "Get ticket by id for Users",
	        description = "Fetch a single ticket including assignment details."
	        )
	@ApiResponses(value = {
	        @ApiResponse(responseCode = "200", description = "Ticket fetched successfully"),
	        @ApiResponse(responseCode = "404", description = "Ticket not found")
	    })
	@GetMapping("/user/{ticketId}")
	public ResponseEntity<ApiResponseBean<UserTicketResponseBean>> getTicketByIdForUser(
			@PathVariable Long ticketId,
			@AuthenticationPrincipal AuthUserPrincipal principal) {
		TICKET_CONTROLLER_LOG.info("TicketController :: in getTicketByIdForUser()");
		Long userId = principal.getUserId();
		UserTicketResponseBean resp = ticketService.getTicketByIdForUser(ticketId, userId);
		TICKET_CONTROLLER_LOG.info("TicketController :: exit getTicketByIdForUser()");
		return ResponseEntity.ok(ApiResponseBean.success(resp));
	}

	//AGENT/ADMIN: Get a particular ticket details with internal fields
	@Operation(summary = "Get ticket by id for Admin Agent",
	        description = "Fetch a single ticket including alls details."
	        )
	@ApiResponses(value = {
	        @ApiResponse(responseCode = "200", description = "Ticket fetched successfully"),
	        @ApiResponse(responseCode = "404", description = "Ticket not found")
	    })
	@GetMapping("/{ticketId}")
	public ResponseEntity<ApiResponseBean<TicketResponseBean>> getTicket(@PathVariable Long ticketId) {
		TICKET_CONTROLLER_LOG.info("TicketController :: in getTicket()");
		TicketResponseBean resp = ticketService.getTicketById(ticketId);
		TICKET_CONTROLLER_LOG.info("TicketController :: exit getTicket()");
		return ResponseEntity.ok(ApiResponseBean.success(resp));
	}
	
	//ADMIN: Lists all tickets with internal fields
	@Operation(summary = "list all tickets for admin")
	@GetMapping("/admin/all")
	public ResponseEntity<ApiResponseBean<List<TicketResponseBean>>> listAllTicketsForAdmin() {
		TICKET_CONTROLLER_LOG.info("TicketController :: in listAllTicketsForAdmin()");
		List<TicketResponseBean> list = ticketService.listAllTicketsForAdmin();
		TICKET_CONTROLLER_LOG.info("TicketController :: exit listAllTicketsForAdmin()");
		return ResponseEntity.ok(ApiResponseBean.success(list));
	}
	
	//AGENT: Lists all tickets assigned to him with internal fields
	@Operation(summary = "list assigned tickets for agent")
	@GetMapping("/agent")
	public ResponseEntity<ApiResponseBean<List<TicketResponseBean>>> listAssignedTicketsForAgent(
			@AuthenticationPrincipal AuthUserPrincipal principal) {
		TICKET_CONTROLLER_LOG.info("TicketController :: in listAssignedTicketsForAgent()");
		Long userId = principal.getUserId();
		List<TicketResponseBean> list = ticketService.listAssignedTicketsForAgent(userId);
		TICKET_CONTROLLER_LOG.info("TicketController :: exit listAssignedTicketsForAgent()");
		return ResponseEntity.ok(ApiResponseBean.success(list));
	}
	
	//ADMIN: Get agents list based on ticket category for ASSIGNMENT override
	@Operation(
	    summary = "Admin: get eligible agents for ticket assignment",
	    description = "Returns agent users whose department matches the current ticket category. Used by admin assignment override dropdown."
	)
	@ApiResponses(value = {
	    @ApiResponse(
	        responseCode = "200",
	        description = "Eligible agents fetched successfully",
	        content = @Content(
	            mediaType = "application/json",
	            schema = @Schema(implementation = ApiResponseBean.class)
	        )
	    ),
	    @ApiResponse(responseCode = "401", description = "Unauthorized"),
	    @ApiResponse(responseCode = "403", description = "Forbidden"),
	    @ApiResponse(responseCode = "404", description = "Ticket not found")
	})
	@GetMapping("/{ticketId}/eligible-agents")
	public ResponseEntity<ApiResponseBean<List<EligibleAgentResponseBean>>> getEligibleAgentsForTicket(
	        @PathVariable Long ticketId) {

	    TICKET_CONTROLLER_LOG.info("TicketController :: in getEligibleAgentsForTicket() :: ticketId={}", ticketId);

	    List<EligibleAgentResponseBean> resp = ticketService.getEligibleAgentsForTicket(ticketId);

	    TICKET_CONTROLLER_LOG.info("TicketController :: exit getEligibleAgentsForTicket()");
	    return ResponseEntity.ok(ApiResponseBean.success(resp));
	}
	
	@Operation(summary = "Create ticket comment")
	@PostMapping("/{ticketId}/comments")
	public ResponseEntity<ApiResponseBean<TicketCommentResponseBean>> addComment(
			@AuthenticationPrincipal AuthUserPrincipal principal,
	        @PathVariable Long ticketId,
	        @Valid @RequestBody TicketCommentRequestBean req) {

	    TICKET_CONTROLLER_LOG.info("TicketController :: in addComment()");
	    Long userId = principal.getUserId();
	    TicketCommentResponseBean resp = ticketService.addTicketComment(userId, ticketId, req);
	    TICKET_CONTROLLER_LOG.info("TicketController :: exit addComment()");
	    return ResponseEntity.status(201).body(ApiResponseBean.success("Comment added", resp));
	}
	
	@Operation(summary = "List ticket comments")
	@GetMapping("/{ticketId}/comments")
	public ResponseEntity<ApiResponseBean<List<TicketCommentResponseBean>>> listComments(
			@AuthenticationPrincipal AuthUserPrincipal principal,
			@PathVariable Long ticketId) {
	    TICKET_CONTROLLER_LOG.info("TicketController :: in listComments()");
	    Long userId = principal.getUserId();
	    List<TicketCommentResponseBean> list = ticketService.listTicketComments(userId, ticketId);
	    TICKET_CONTROLLER_LOG.info("TicketController :: exit listComments()");
	    return ResponseEntity.ok(ApiResponseBean.success(list));
	}
	
	@Operation(summary = "Admin: override ticket fields",
	        description = "Writes audit row in admin_overrides and updates ticket (STATUS/CATEGORY/PRIORITY/DUPLICATE_LINK/ASSIGNMENT)")
	@PatchMapping("/{ticketId}/admin/override")
	public ResponseEntity<ApiResponseBean<AdminOverrideResponseBean>> adminOverride(
			@AuthenticationPrincipal AuthUserPrincipal principal,
	        @PathVariable Long ticketId,
	        @Valid @RequestBody AdminOverrideRequestBean req) {

	    TICKET_CONTROLLER_LOG.info("TicketController :: in adminOverride()");
	    Long adminUserId = principal.getUserId();
	    AdminOverrideResponseBean resp = ticketService.applyAdminOverride(adminUserId, ticketId, req);
	    TICKET_CONTROLLER_LOG.info("TicketController :: exit adminOverride()");
	    return ResponseEntity.ok(ApiResponseBean.success("Override applied", resp));
	}
	
	@Operation(summary = "Agent: update ticket status",
	        description = "Agent updates status for a ticket assigned to them (e.g., IN_PROGRESS, RESOLVED, CLOSED).")
	@PatchMapping("/{ticketId}/agent/status")
	public ResponseEntity<ApiResponseBean<TicketResponseBean>> updateTicketStatusByAgent(
			@AuthenticationPrincipal AuthUserPrincipal principal,
	        @PathVariable Long ticketId,
	        @Valid @RequestBody UpdateTicketStatusRequestBean req) {

	    TICKET_CONTROLLER_LOG.info("TicketController :: in updateTicketStatusByAgent() :: ticketId={}", ticketId);
	    Long agentUserId = principal.getUserId();
	    TicketResponseBean resp = ticketService.updateTicketStatusByAgent(agentUserId, ticketId, req);
	    TICKET_CONTROLLER_LOG.info("TicketController :: exit updateTicketStatusByAgent()");
	    return ResponseEntity.ok(ApiResponseBean.success("Status updated", resp));
	}
	
	@Operation(
	    summary = "Admin/Agent: get ticket text version history",
	    description = "Returns the version history of a ticket's title and description for audit and review."
	)
	@ApiResponses(value = {
	    @ApiResponse(responseCode = "200", description = "Ticket history fetched successfully"),
	    @ApiResponse(responseCode = "401", description = "Unauthorized"),
	    @ApiResponse(responseCode = "404", description = "Ticket not found")
	})
	@GetMapping("/{ticketId}/text-version-history")
	public ResponseEntity<ApiResponseBean<List<TicketTextVersionResponseBean>>> getTicketHistory(
	        @PathVariable Long ticketId) {

	    TICKET_CONTROLLER_LOG.info("TicketController :: in getTicketHistory() :: ticketId={}", ticketId);

	    List<TicketTextVersionResponseBean> resp = ticketService.getTicketHistory(ticketId);

	    TICKET_CONTROLLER_LOG.info("TicketController :: exit getTicketHistory()");
	    return ResponseEntity.ok(ApiResponseBean.success(resp));
	}
	
	@Operation(
	    summary = "Admin/Agent: get confirmed duplicate tickets for a primary ticket",
	    description = "Returns all active confirmed duplicate tickets linked to the given primary ticket. Useful for admins and agents working on the primary ticket to review all related duplicate reports."
	)
	@ApiResponses(value = {
	    @ApiResponse(
	        responseCode = "200",
	        description = "Confirmed duplicate tickets fetched successfully",
	        content = @Content(
	            mediaType = "application/json",
	            schema = @Schema(implementation = ApiResponseBean.class)
	        )
	    ),
	    @ApiResponse(responseCode = "401", description = "Unauthorized"),
	    @ApiResponse(responseCode = "403", description = "Forbidden"),
	    @ApiResponse(responseCode = "404", description = "Primary ticket not found")
	})
	@GetMapping("/{ticketId}/confirmed-duplicates")
	public ResponseEntity<ApiResponseBean<List<ConfirmedDuplicateTicketResponseBean>>> getConfirmedDuplicates(
	        @PathVariable Long ticketId) {

	    TICKET_CONTROLLER_LOG.info("TicketController :: in getConfirmedDuplicates() :: ticketId={}", ticketId);

	    List<ConfirmedDuplicateTicketResponseBean> resp = ticketService.getConfirmedDuplicates(ticketId);

	    TICKET_CONTROLLER_LOG.info("TicketController :: exit getConfirmedDuplicates()");
	    return ResponseEntity.ok(ApiResponseBean.success(resp));
	}
	
	@Operation(
	    summary = "Admin/Agent: get primary linked ticket for a confirmed duplicate",
	    description = "Returns the active confirmed primary ticket link for the given duplicate ticket. Useful for viewing the master ticket that the duplicate is linked to."
	)
	@ApiResponses(value = {
	    @ApiResponse(
	        responseCode = "200",
	        description = "Primary linked ticket fetched successfully",
	        content = @Content(
	            mediaType = "application/json",
	            schema = @Schema(implementation = ApiResponseBean.class)
	        )
	    ),
	    @ApiResponse(responseCode = "401", description = "Unauthorized"),
	    @ApiResponse(responseCode = "403", description = "Forbidden"),
	    @ApiResponse(responseCode = "404", description = "Ticket not found or no active confirmed primary link found")
	})
	@GetMapping("/{ticketId}/primary-link")
	public ResponseEntity<ApiResponseBean<PrimaryLinkedTicketResponseBean>> getPrimaryLink(
	        @PathVariable Long ticketId) {

	    TICKET_CONTROLLER_LOG.info("TicketController :: in getPrimaryLink() :: ticketId={}", ticketId);

	    PrimaryLinkedTicketResponseBean resp = ticketService.getPrimaryLink(ticketId);

	    TICKET_CONTROLLER_LOG.info("TicketController :: exit getPrimaryLink()");
	    return ResponseEntity.ok(ApiResponseBean.success(resp));
	}
	
	@Operation(
	    summary = "User: respond to suggested KB article",
	    description = "Allows the ticket owner to respond to the currently suggested KB article for a ticket in KB_SUGGESTED state. ACCEPTED resolves the ticket through self-service. REJECTED resumes the ticket workflow and triggers routing."
	)
	@ApiResponses(value = {
	    @ApiResponse(
	        responseCode = "200",
	        description = "KB response recorded successfully",
	        content = @Content(
	            mediaType = "application/json",
	            schema = @Schema(implementation = ApiResponseBean.class)
	        )
	    ),
	    @ApiResponse(responseCode = "400", description = "Ticket is not in KB_SUGGESTED state or request is invalid"),
	    @ApiResponse(responseCode = "401", description = "Unauthorized"),
	    @ApiResponse(responseCode = "404", description = "Ticket not found or no active KB suggestion found")
	})
	@PostMapping("/user/{ticketId}/kb-response")
	public ResponseEntity<ApiResponseBean<UserTicketResponseBean>> respondToKbSuggestion(
	        @PathVariable Long ticketId,
	        @Valid @RequestBody KbSuggestionResponseRequestBean request,
	        @AuthenticationPrincipal AuthUserPrincipal principal) {

		TICKET_CONTROLLER_LOG.info("TicketController :: in respondToKbSuggestion() :: ticketId={}", ticketId);
	    Long userId = principal.getUserId();

	    UserTicketResponseBean response =
	            ticketService.respondToKbSuggestion(userId, ticketId, request);
	    TICKET_CONTROLLER_LOG.info("TicketController :: exit respondToKbSuggestion() :: ticketId={}", ticketId);
	    return ResponseEntity.ok(ApiResponseBean.success("KB response recorded", response));
	}
	
	@Operation(
	    summary = "Agent: manually suggest KB article for ticket",
	    description = "Allows the assigned agent to manually suggest a published KB article for a ticket. The ticket moves to KB_SUGGESTED and waits for the user's response."
	)
	@ApiResponses(value = {
	    @ApiResponse(
	        responseCode = "200",
	        description = "KB article suggested successfully",
	        content = @Content(
	            mediaType = "application/json",
	            schema = @Schema(implementation = ApiResponseBean.class)
	        )
	    ),
	    @ApiResponse(responseCode = "400", description = "KB already suggested for this ticket, KB is not published, or request is invalid"),
	    @ApiResponse(responseCode = "401", description = "Unauthorized"),
	    @ApiResponse(responseCode = "404", description = "Ticket or KB article not found")
	})
	@PostMapping("/agent/{ticketId}/kb/manual-suggestion")
	public ResponseEntity<ApiResponseBean<TicketResponseBean>> suggestKbManually(
	        @PathVariable Long ticketId,
	        @Valid @RequestBody ManualKbSuggestionRequestBean request,
	        @AuthenticationPrincipal AuthUserPrincipal principal) {

		TICKET_CONTROLLER_LOG.info("TicketController :: in suggestKbManually() :: ticketId={}", ticketId);
	    Long agentUserId = principal.getUserId();

	    TicketResponseBean response =
	            ticketService.suggestKbManuallyByAgent(agentUserId, ticketId, request);
	    
	    TICKET_CONTROLLER_LOG.info("TicketController :: exit suggestKbManually() :: ticketId={}", ticketId);
	    return ResponseEntity.ok(ApiResponseBean.success("KB article suggested", response));
	}
	
	@Operation(
	    summary = "Agent: generate KB draft from resolved ticket",
	    description = "Allows the assigned agent to request AI generation of a KB draft using the resolved ticket and selected public comments as the source material."
	)
	@ApiResponses(value = {
	    @ApiResponse(
	        responseCode = "200",
	        description = "KB draft generation requested successfully",
	        content = @Content(
	            mediaType = "application/json",
	            schema = @Schema(implementation = ApiResponseBean.class)
	        )
	    ),
	    @ApiResponse(responseCode = "400", description = "Ticket is not eligible for KB draft generation or request is invalid"),
	    @ApiResponse(responseCode = "401", description = "Unauthorized"),
	    @ApiResponse(responseCode = "404", description = "Ticket not found")
	})
	@PostMapping("/agent/{ticketId}/kb-draft/generate")
	public ResponseEntity<ApiResponseBean<TicketResponseBean>> requestKbDraftGeneration(
	        @PathVariable Long ticketId,
	        @AuthenticationPrincipal AuthUserPrincipal principal,
	        @Valid @RequestBody GenerateKbDraftRequestBean request) {

	    TICKET_CONTROLLER_LOG.info("TicketController :: in requestKbDraftGeneration() :: ticketId={}", ticketId);

	    Long agentUserId = principal.getUserId();
	    TicketResponseBean response =
	            ticketService.requestKbDraftGeneration(agentUserId, ticketId, request);

	    TICKET_CONTROLLER_LOG.info("TicketController :: exit requestKbDraftGeneration() :: ticketId={}", ticketId);
	    return ResponseEntity.ok(ApiResponseBean.success("KB draft generation requested", response));
	}
}
