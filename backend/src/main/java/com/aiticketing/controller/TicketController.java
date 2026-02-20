package com.aiticketing.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aiticketing.bean.request.CreateTicketRequestBean;
import com.aiticketing.bean.response.ApiResponseBean;
import com.aiticketing.bean.response.TicketResponseBean;
import com.aiticketing.service.TicketService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
	public ResponseEntity<ApiResponseBean<TicketResponseBean>> createTicket(
			@Parameter(description = "User ID (temporary dev header, replace with JWT later)",
            required = true) 
			@RequestHeader("X-User-Id") Long userId,
			@Valid @RequestBody CreateTicketRequestBean createTicketReq) {
		
		TICKET_CONTROLLER_LOG.info("TicketController :: in createTicket()");
		TicketResponseBean resp = ticketService.createTicket(userId, createTicketReq);
		TICKET_CONTROLLER_LOG.info("TicketController :: exit createTicket()");
		return ResponseEntity.status(201).body(ApiResponseBean.success("Ticket created", resp));
	}

	@Operation(summary = "List Tickets for User",
	        description = "Returns all tickets created by the given user.")
	@ApiResponses(value = {
	        @ApiResponse(responseCode = "200", description = "Tickets fetched successfully")
	    })
	@GetMapping
	public ResponseEntity<ApiResponseBean<List<TicketResponseBean>>> listTicketsForUser(
			@Parameter(description = "User ID (temporary dev header, replace with JWT later)",
		    required = true)
			@RequestHeader("X-User-Id") Long userId) {
		TICKET_CONTROLLER_LOG.info("TicketController :: in listTicketsForUser()");
		List<TicketResponseBean> list = ticketService.listTicketsForUser(userId);
		TICKET_CONTROLLER_LOG.info("TicketController :: exit listTicketsForUser()");
		return ResponseEntity.ok(ApiResponseBean.success(list));
	}

	@Operation(summary = "Get ticket by id",
	        description = "Fetch a single ticket including assignment details."
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

	@Operation(summary = "list all tickets for admin")
	@GetMapping("/admin/all")
	public ResponseEntity<ApiResponseBean<List<TicketResponseBean>>> listAllTicketsForAdmin() {
		TICKET_CONTROLLER_LOG.info("TicketController :: in listAllTicketsForAdmin()");
		List<TicketResponseBean> list = ticketService.listAllTicketsForAdmin();
		TICKET_CONTROLLER_LOG.info("TicketController :: exit listAllTicketsForAdmin()");
		return ResponseEntity.ok(ApiResponseBean.success(list));
	}
	
	@Operation(summary = "list assigned tickets for agent")
	@GetMapping("/agent")
	public ResponseEntity<ApiResponseBean<List<TicketResponseBean>>> listAssignedTicketsForAgent(
			@Parameter(description = "User ID (temporary dev header, replace with JWT later)",
            required = true) 
			@RequestHeader("X-User-Id") Long userId) {
		TICKET_CONTROLLER_LOG.info("TicketController :: in listAssignedTicketsForAgent()");
		List<TicketResponseBean> list = ticketService.listAssignedTicketsForAgent(userId);
		TICKET_CONTROLLER_LOG.info("TicketController :: exit listAssignedTicketsForAgent()");
		return ResponseEntity.ok(ApiResponseBean.success(list));
	}
}
