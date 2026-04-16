package com.aiticketing.ai.worker;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.aiticketing.ai.dto.ClaimedOutboxWork;
import com.aiticketing.ai.dto.DuplicateCheckResult;
import com.aiticketing.ai.dto.KbDraftGenerationResult;
import com.aiticketing.ai.dto.KbSuggestionResult;
import com.aiticketing.ai.dto.RoutingResult;
import com.aiticketing.ai.dto.TriageResult;
import com.aiticketing.entity.AiDecision;
import com.aiticketing.entity.KbArticle;
import com.aiticketing.entity.KbSuggestion;
import com.aiticketing.entity.OutboxEvent;
import com.aiticketing.entity.Ticket;
import com.aiticketing.entity.TicketDuplicateLink;
import com.aiticketing.entity.User;
import com.aiticketing.entity.enums.AggregateType;
import com.aiticketing.entity.enums.AiDecisionType;
import com.aiticketing.entity.enums.DuplicateLinkStatus;
import com.aiticketing.entity.enums.DuplicateState;
import com.aiticketing.entity.enums.KbArticleStatus;
import com.aiticketing.entity.enums.KbSuggestionSource;
import com.aiticketing.entity.enums.KbSuggestionStatus;
import com.aiticketing.entity.enums.OutboxEventType;
import com.aiticketing.entity.enums.TicketStatus;
import com.aiticketing.repository.AiDecisionRepository;
import com.aiticketing.repository.KbArticleRepository;
import com.aiticketing.repository.KbSuggestionRepository;
import com.aiticketing.repository.OutboxEventRepository;
import com.aiticketing.repository.TicketDuplicateLinkRepository;
import com.aiticketing.repository.TicketRepository;
import com.aiticketing.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

/*
 * This class is to separate our transaction boundaries.
 * 1) Mark claim event ie. outbox state to PROCESSING
 * 2) Persist TRIAGE (category, priority, vague) SUCCESS
 * 3) Persist DUPLICATE (similar tickets with duplicateState) SUCCESS
 * 4) Persist KB SUGGESTION (appropriate KB article for ticket resolution) SUCCESS
 * 5) Persist ROUTING (assignment based on aiCategory & least agent active workload) SUCCESS
 * 6) Persist KB DRAFT (KB out of ticket's PUBLIC comments) SUCCESS
 * 7) Persist failure Steps so that we can persist retry details in outbox
*/

@Service
public class OutboxAiPersistenceService {
	private static final Logger PERSISTENCE_LOG = LoggerFactory.getLogger(OutboxAiPersistenceService.class);

	private final OutboxEventRepository outboxRepo;
	private final UserRepository userRepo;
	private final TicketRepository ticketRepo;
	private final AiDecisionRepository aiDecisionRepo;
	private final TicketDuplicateLinkRepository duplicateLinkRepo;
    private final KbArticleRepository kbArticleRepo;
    private final KbSuggestionRepository kbSuggestionRepo;
    private final ObjectMapper objectMapper;

    public OutboxAiPersistenceService(OutboxEventRepository outboxRepo, UserRepository userRepo,
			TicketRepository ticketRepo, AiDecisionRepository aiDecisionRepo,
			TicketDuplicateLinkRepository duplicateLinkRepo, KbArticleRepository kbArticleRepo,
            KbSuggestionRepository kbSuggestionRepo, ObjectMapper objectMapper) {
		this.outboxRepo = outboxRepo;
		this.userRepo = userRepo;
		this.ticketRepo = ticketRepo;
		this.aiDecisionRepo = aiDecisionRepo;
		this.duplicateLinkRepo = duplicateLinkRepo;
		this.kbArticleRepo = kbArticleRepo;
		this.kbSuggestionRepo = kbSuggestionRepo;
		this.objectMapper = objectMapper;
	}

	//1) CLAIM the PENDING outbox row in a new transaction & store it in
	//ClaimedOutboxWork dto
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public ClaimedOutboxWork claimEvent(Long oeId) {
		OutboxEvent outbox = outboxRepo.findById(oeId)
				.orElseThrow(() -> new IllegalStateException("OutboxEvent not found: " + oeId));

		if (!"PENDING".equalsIgnoreCase(outbox.getStatus())) {
			PERSISTENCE_LOG.debug("OutboxAiPersistenceService :: in claimEvent() skipping oeId={} because status={}",
					oeId, outbox.getStatus());
			return null;
		}

		Ticket ticket = ticketRepo.findById(outbox.getAggregateId())
				.orElseThrow(() -> new IllegalStateException("Ticket not found: " + outbox.getAggregateId()));

		OffsetDateTime now = OffsetDateTime.now();

		//1.1) mark outbox row as PROCESSING
		outbox.setStatus("PROCESSING");
		outbox.setLastError(null);
		outboxRepo.save(outbox);

		//1.2) mark the ticket row as AI_PROCESSING
		if (OutboxEventType.TRIAGE_REQUESTED.name().equals(outbox.getEventType())) {
			ticket.setStatus(TicketStatus.AI_PROCESSING);
			ticket.setAiFailed(false);
			ticket.setAiLastError(null);
			ticket.setCurrentTriageStartedAt(now);
			ticket.setUpdatedAt(now);
			ticketRepo.save(ticket);
		} else if (OutboxEventType.DUPLICATE_CHECK_REQUESTED.name().equals(outbox.getEventType())) {
			//Ticket status will be READY as TRIAGE succeeded
			ticket.setAiLastError(null);
			ticket.setCurrentDuplicateCheckStartedAt(now);
			ticket.setUpdatedAt(now);
			ticketRepo.save(ticket);
		} else if (OutboxEventType.KB_SUGGESTION_REQUESTED.name().equals(outbox.getEventType())) {
            ticket.setAiLastError(null);
            ticket.setUpdatedAt(now);
            ticketRepo.save(ticket);
        } else if (OutboxEventType.KB_DRAFT_REQUESTED.name().equals(outbox.getEventType())) {
            ticket.setAiLastError(null);
            ticket.setUpdatedAt(now);
            ticketRepo.save(ticket);
        }

		PERSISTENCE_LOG.debug(
				"OutboxAiPersistenceService :: in claimEvent() :: start marked PROCESSING :: oeId={} retryCount={}",
				outbox.getOeId(), outbox.getRetryCount());

		//Save row in-memory dto
		ClaimedOutboxWork work = new ClaimedOutboxWork();
		work.outboxId = outbox.getOeId();
		work.eventType = outbox.getEventType();
		work.aggregateId = outbox.getAggregateId();
		work.textVersion = payloadTextVersion(outbox.getPayload());
		work.ticketTitle = ticket.getTitle();
		work.ticketDescription = ticket.getDescription();
		work.ticketAiCategory = ticket.getAiCategory();
		work.selectedCommentIds = payloadSelectedCommentIds(outbox.getPayload());

		if (OutboxEventType.KB_DRAFT_REQUESTED.name().equals(work.eventType)) {
		    PERSISTENCE_LOG.debug("OutboxAiPersistenceService :: claimEvent() :: KB_DRAFT_REQUESTED selectedCommentIds={}",
		            work.selectedCommentIds);
		}
		
		PERSISTENCE_LOG.info("OutboxAiPersistenceService :: exit claimEvent() :: oeId={} eventType={} ticketId={}",
				work.outboxId, work.eventType, work.aggregateId);

		return work;
	}

	//2) Persist TRIAGE (category, priority, vague) SUCCESS in a fresh transaction
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void persistTriageSuccess(ClaimedOutboxWork work, TriageResult result) {
		OutboxEvent outbox = outboxRepo.findById(work.outboxId)
				.orElseThrow(() -> new IllegalStateException("OutboxEvent not found: " + work.outboxId));

		Ticket ticket = ticketRepo.findById(work.aggregateId)
				.orElseThrow(() -> new IllegalStateException("Ticket not found: " + work.aggregateId));

		OffsetDateTime now = OffsetDateTime.now();

		PERSISTENCE_LOG.info("OutboxAiPersistenceService :: in persistTriageSuccess() :: oeId={} eventType={} ticketId={}",
				work.outboxId, work.eventType, work.aggregateId);

		//2.2) update ticket with successful AI triage results & status as READY
		ticket.setAiCategory(result.category);
		ticket.setAiPriority(result.priority);
		ticket.setAiConfidence(result.confidence);
		ticket.setAiTriagedAt(now);
		ticket.setAiFailed(false);
		ticket.setAiLastError(null);
		ticket.setUpdatedAt(now);

		if (Boolean.TRUE.equals(result.isVague)) {
			PERSISTENCE_LOG.debug("OutboxAiPersistenceService :: in persistTriageSuccess() :: Ticket is VAGUE :: oeId={} ticketId={}",
					work.outboxId, work.aggregateId);
			ticket.setStatus(TicketStatus.VAGUE);
			ticket.setVagueCount(ticket.getVagueCount() == null ? 1 : ticket.getVagueCount() + 1);
			ticket.setLastVagueAt(now);
			ticket.setVagueReason(result.vagueReason);
			ticket.setClarificationPrompt(result.clarificationPrompt);
		} else {
			PERSISTENCE_LOG.debug("OutboxAiPersistenceService :: in persistTriageSuccess() :: Ticket is READY :: oeId={} ticketId={}",
					work.outboxId, work.aggregateId);
			ticket.setStatus(TicketStatus.READY);
			ticket.setVagueReason(null);
			ticket.setClarificationPrompt(null);
		}

		ticketRepo.save(ticket);

		//4.1) Persist AI decisions for audit + dashboards in ai_decisions table (for classification)
		aiDecisionRepo.save(buildDecision(work.aggregateId, work.textVersion, AiDecisionType.CLASSIFICATION.name(),
				Map.of("category", result.category), result.confidence, null, null));

		//4.2) Persist AI decisions for audit + dashboards in ai_decisions table (for Priority)
		aiDecisionRepo.save(buildDecision(work.aggregateId, work.textVersion, AiDecisionType.PRIORITY.name(),
				Map.of("priority", result.priority.name()), result.confidence, null, null));

		//4.3) Persist AI decisions for audit + dashboards in ai_decisions table (for Vague)
		Map<String, Object> vagueJson = new LinkedHashMap<>();
		vagueJson.put("isVague", result.isVague);
		vagueJson.put("vagueReason", result.vagueReason);
		vagueJson.put("clarificationPrompt", result.clarificationPrompt);
		aiDecisionRepo.save(buildDecision(work.aggregateId, work.textVersion, AiDecisionType.VAGUE_CHECK.name(),
				vagueJson, result.confidence, null, null));

		PERSISTENCE_LOG.debug("OutboxAiPersistenceService :: in persistTriageSuccess() :: Added TRIAGE steps in ai_decisions table :: oeId={} ticketId={}",
				work.outboxId, work.aggregateId);

		//Insert event_type DUPLICATE_CHECK_REQUESTED as PENDING in outbox table if ticket is not VAGUE
		if (!Boolean.TRUE.equals(result.isVague)) {
			PERSISTENCE_LOG.debug("OutboxAiPersistenceService :: in persistTriageSuccess() :: Inserting DUPLICATE_CHECK_REQUESTED in outbox :: oeId={} ticketId={}",
					work.outboxId, work.aggregateId);
			OutboxEvent duplicateEvent = new OutboxEvent();
			duplicateEvent.setEventType(OutboxEventType.DUPLICATE_CHECK_REQUESTED.name());
			duplicateEvent.setAggregateType(AggregateType.TICKET.name());
			duplicateEvent.setAggregateId(work.aggregateId);
			duplicateEvent.setPayload(toJson(Map.of("textVersion", work.textVersion)));
			duplicateEvent.setStatus("PENDING");
			duplicateEvent.setRetryCount(0);
			duplicateEvent.setCreatedAt(now);
			outboxRepo.save(duplicateEvent);
		}

		//5.1)mark outbox row as DONE on success
		outbox.setStatus("DONE");
		outbox.setProcessedAt(now);
		outbox.setNextRunAt(null);
		outbox.setLastError(null);
		outboxRepo.save(outbox);

		PERSISTENCE_LOG.info("OutboxAiPersistenceService :: exit persistTriageSuccess() :: oeId={} eventType={} ticketId={} isVague={}",
				work.outboxId, work.eventType, work.aggregateId, result.isVague);
	}

	//3) Persist DUPLICATE CHECK SUCCESS in a fresh transaction
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void persistDuplicateCheckSuccess(ClaimedOutboxWork work, DuplicateCheckResult result) {
		OutboxEvent outbox = outboxRepo.findById(work.outboxId)
				.orElseThrow(() -> new IllegalStateException("OutboxEvent not found: " + work.outboxId));

		Ticket ticket = ticketRepo.findById(work.aggregateId)
				.orElseThrow(() -> new IllegalStateException("Ticket not found: " + work.aggregateId));

		OffsetDateTime now = OffsetDateTime.now();

		PERSISTENCE_LOG.info("OutboxAiPersistenceService :: in persistDuplicateCheckSuccess() :: oeId={} eventType={} ticketId={} duplicateState={}",
				work.outboxId, work.eventType, work.aggregateId, result.duplicateState);

		/*
		 * 3.1) Update ticket with duplicate-check completion details. We keep duplicate
		 * tracking separate from TRIAGE tracking: - ticket_duplicate_checked_at is for
		 * duplicate step completion - ticket_ai_triaged_at remains only for TRIAGE
		 * success
		 */

		ticket.setDuplicateState(result.duplicateState);
		ticket.setDuplicateCheckedAt(now);
		ticket.setAiLastError(null);
		ticket.setUpdatedAt(now);

		/*
		 * 3.2) Apply ticket business status based on duplicate outcome: NONE -> ticket
		 * remains actionable and can proceed to routing POTENTIAL -> ticket waits in
		 * DUPLICATE_REVIEW for admin confirmation CONFIRMED -> ticket becomes DUPLICATE
		 * and should not be routed
		 */

		if (DuplicateState.NONE.name().equals(result.duplicateState)) {
			ticket.setStatus(TicketStatus.READY);
		} else if (DuplicateState.POTENTIAL.name().equals(result.duplicateState)) {
			ticket.setStatus(TicketStatus.DUPLICATE_REVIEW);
		} else if (DuplicateState.CONFIRMED.name().equals(result.duplicateState)) {
			ticket.setStatus(TicketStatus.DUPLICATE);
			// if somehow this ticket was ever assigned earlier, we clear assignment because
			// confirmed duplicates should not remain independently assigned
			ticket.setAssignedTo(null);
		}

		ticketRepo.save(ticket);

		//3.3) Persist DUPLICATE_CHECK in ai_decisions table
		Map<String, Object> duplicateJson = new LinkedHashMap<>();
		duplicateJson.put("duplicateState", result.duplicateState);
		duplicateJson.put("primaryTicketId", result.primaryTicketId);
		duplicateJson.put("reason", result.reason);

		PERSISTENCE_LOG.debug("OutboxAiPersistenceService :: in persistDuplicateCheckSuccess() :: before saving DUPLICATE_CHECK decision in ai_decisions :: ticketId={}",
				work.aggregateId);
		aiDecisionRepo.save(buildDecision(work.aggregateId, work.textVersion, AiDecisionType.DUPLICATE_CHECK.name(),
				duplicateJson, result.confidence, result.similarity, result.threshold));

		//3.4) For POTENTIAL / CONFIRMED outcomes, insert the current active duplicate link in ticket_duplicate_links table
		if (!DuplicateState.NONE.name().equals(result.duplicateState) && result.primaryTicketId != null) {
			Ticket primaryTicket = ticketRepo.findById(result.primaryTicketId).orElseThrow(
					() -> new IllegalStateException("Primary ticket not found: " + result.primaryTicketId));
			
			PERSISTENCE_LOG.debug("OutboxAiPersistenceService :: in persistDuplicateCheckSuccess() :: inserted active duplicate link :: duplicateTicketId={} primaryTicketId={} duplicateType={}",
	                ticket.getTicketId(), result.primaryTicketId, result.duplicateState);
			
			TicketDuplicateLink link = new TicketDuplicateLink();
			link.setPrimaryTicket(primaryTicket);
			link.setDuplicateTicket(ticket);
			link.setSimilarity(result.similarity);
			link.setCreatedAt(now);
			link.setDuplicateType(result.duplicateState);
			link.setLinkStatus(DuplicateLinkStatus.ACTIVE.name());
			link.setPropagateResolution(DuplicateState.CONFIRMED.name().equals(result.duplicateState));
			duplicateLinkRepo.save(link);
			
			PERSISTENCE_LOG.debug("OutboxAiPersistenceService :: in persistDuplicateCheckSuccess() :: inserted active duplicate link :: duplicateTicketId={} primaryTicketId={} duplicateType={}",
	                ticket.getTicketId(), result.primaryTicketId, result.duplicateState);
		}

		//3.5) Ticket is not DUPLICATE as outcome is NONE, so insert ROUTING_REQUESTED in outbox
		//POTENTIAL waits for admin review not kb_suggested & not routed, CONFIRMED stops here and is not kb_suggested & not routed
		if (DuplicateState.NONE.name().equals(result.duplicateState)) {
			PERSISTENCE_LOG.debug("OutboxAiPersistenceService :: in persistDuplicateCheckSuccess() :: no duplicate link :: duplicateType={}", result.duplicateState);
			OutboxEvent kbEvent = new OutboxEvent();
			kbEvent.setEventType(OutboxEventType.KB_SUGGESTION_REQUESTED.name());
			kbEvent.setAggregateType(AggregateType.TICKET.name()); //This a part of Ticket lifecycle as we are suggesting KB for a ticketId 
			kbEvent.setAggregateId(work.aggregateId);
			kbEvent.setPayload(toJson(Map.of("textVersion", work.textVersion)));
			kbEvent.setStatus("PENDING");
			kbEvent.setRetryCount(0);
			kbEvent.setCreatedAt(now);
			outboxRepo.save(kbEvent);
		}

		//3.6) mark outbox row of DUPLICATE_CHECK as DONE on success
		outbox.setStatus("DONE");
		outbox.setProcessedAt(now);
		outbox.setNextRunAt(null);
		outbox.setLastError(null);
		outboxRepo.save(outbox);

		PERSISTENCE_LOG.info("OutboxAiPersistenceService :: exit persistDuplicateCheckSuccess() :: DUPLICATE done :: oeId={} ticketId={} duplicateState={} primaryTicketId={}",
				outbox.getOeId(), work.aggregateId, result.duplicateState, result.primaryTicketId);
	}
	
	//4) Persist KB SUGGESTION (appropriate KB article for ticket resolution) SUCCESS in a fresh transaction even
	@Transactional(propagation = Propagation.REQUIRES_NEW)
    public void persistKbSuggestionSuccess(ClaimedOutboxWork work, KbSuggestionResult result) {
        OutboxEvent outbox = outboxRepo.findById(work.outboxId)
                .orElseThrow(() -> new IllegalStateException("OutboxEvent not found: " + work.outboxId));

        Ticket ticket = ticketRepo.findById(work.aggregateId)
                .orElseThrow(() -> new IllegalStateException("Ticket not found: " + work.aggregateId));

        OffsetDateTime now = OffsetDateTime.now();

        PERSISTENCE_LOG.info("OutboxAiPersistenceService :: in persistKbSuggestionSuccess() :: oeId={} eventType={} ticketId={} suggestionFound={}",
                work.outboxId, work.eventType, work.aggregateId, result.suggestionFound);

        Map<String, Object> kbSuggestionJson = new LinkedHashMap<>();
        kbSuggestionJson.put("suggestionFound", result.suggestionFound);
        kbSuggestionJson.put("kbId", result.kbId);
        kbSuggestionJson.put("kbTitle", result.kbTitle);
        kbSuggestionJson.put("reason", result.reason);

        aiDecisionRepo.save(buildDecision(
                work.aggregateId,
                work.textVersion,
                AiDecisionType.KB_SUGGESTION.name(),
                kbSuggestionJson,
                result.confidence,
                result.similarity,
                result.threshold
        ));

        if (Boolean.TRUE.equals(result.suggestionFound) && result.kbId != null) {
            KbArticle kbArticle = kbArticleRepo.findById(result.kbId)
                    .orElseThrow(() -> new IllegalStateException("KB article not found: " + result.kbId));

            KbSuggestion suggestion = new KbSuggestion();
            suggestion.setTicket(ticket);
            suggestion.setKbArticle(kbArticle);
            suggestion.setSimilarity(result.similarity);
            suggestion.setSource(KbSuggestionSource.AI.name());
            suggestion.setStatus(KbSuggestionStatus.SUGGESTED.name());
            suggestion.setCreatedAt(now);
            suggestion.setRespondedAt(null);
            kbSuggestionRepo.save(suggestion);

            ticket.setStatus(TicketStatus.KB_SUGGESTED);
            ticket.setUpdatedAt(now);
            ticketRepo.save(ticket);
        } else {
            ticket.setStatus(TicketStatus.READY);
            ticket.setUpdatedAt(now);
            ticketRepo.save(ticket);

            OutboxEvent routingEvent = new OutboxEvent();
            routingEvent.setEventType(OutboxEventType.ROUTING_REQUESTED.name());
            routingEvent.setAggregateType(AggregateType.TICKET.name());
            routingEvent.setAggregateId(work.aggregateId);
            routingEvent.setPayload(toJson(Map.of("textVersion", work.textVersion)));
            routingEvent.setStatus("PENDING");
            routingEvent.setRetryCount(0);
            routingEvent.setCreatedAt(now);
            outboxRepo.save(routingEvent);
        }

        outbox.setStatus("DONE");
        outbox.setProcessedAt(now);
        outbox.setNextRunAt(null);
        outbox.setLastError(null);
        outboxRepo.save(outbox);

        PERSISTENCE_LOG.info("OutboxAiPersistenceService :: exit persistKbSuggestionSuccess() :: oeId={} ticketId={} suggestionFound={} kbId={}",
                outbox.getOeId(), work.aggregateId, result.suggestionFound, result.kbId);
    }

	//5) Persist ROUTING (assignment) SUCCESS in a fresh transaction even
	//NO_ELIGIBLE_AGENT is still a successful completion of the routing step
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void persistRoutingSuccess(ClaimedOutboxWork work, RoutingResult routingResult) {
		OutboxEvent outbox = outboxRepo.findById(work.outboxId)
				.orElseThrow(() -> new IllegalStateException("OutboxEvent not found: " + work.outboxId));

		Ticket ticket = ticketRepo.findById(work.aggregateId)
				.orElseThrow(() -> new IllegalStateException("Ticket not found: " + work.aggregateId));

		OffsetDateTime now = OffsetDateTime.now();

		if (routingResult.selectedAgentId != null && ticket.getFirstAssignedAt() == null) {
			User agent = userRepo.findById(routingResult.selectedAgentId)
					.orElseThrow(() -> new IllegalArgumentException(
							"Selected Agent Id not found: " + routingResult.selectedAgentId));
			ticket.setAssignedTo(agent);

			if (ticket.getFirstAssignedAt() == null)
				ticket.setFirstAssignedAt(now);
		}

		PERSISTENCE_LOG.info("OutboxAiPersistenceService :: in persistRoutingSuccess() :: oeId={} eventType={} ticketId={}",
				work.outboxId, work.eventType, work.aggregateId);

		ticket.setUpdatedAt(now);
		ticketRepo.save(ticket);

		PERSISTENCE_LOG.debug("OutboxAiPersistenceService :: in persistRoutingSuccess() :: before saving ROUTING decision in ai_decisions :: ticketId={}",
				work.aggregateId);
		
		Map<String, Object> routingJson = new LinkedHashMap<>();
		routingJson.put("department", routingResult.department);
		routingJson.put("outcome", routingResult.outcome == null ? null : routingResult.outcome.name());
		routingJson.put("selectedAgentId", routingResult.selectedAgentId);
		routingJson.put("selectedWorkload", routingResult.selectedWorkload);
		routingJson.put("eligibleAgentCount", routingResult.eligibleAgentCount);
		
		aiDecisionRepo.save(buildDecision(work.aggregateId, work.textVersion, AiDecisionType.ROUTING.name(),
				routingJson,
				ticket.getAiConfidence(),
				null,
				null
				));

		//5.1)mark outbox row of ROUTING_REQUESTED as DONE on success
		outbox.setStatus("DONE");
		outbox.setProcessedAt(OffsetDateTime.now());
		outbox.setNextRunAt(null);
		outbox.setLastError(null);
		outboxRepo.save(outbox);

		PERSISTENCE_LOG.info("OutboxAiPersistenceService :: exit persistRoutingSuccess() :: ROUTING done :: oeId={} ticketId={} outcome={} selectedAgentId={}",
				outbox.getOeId(), work.aggregateId, routingResult.outcome, routingResult.selectedAgentId);
	}
	
	//6) Persist KB DRAFT (KB out of ticket's PUBLIC comments) SUCCESS in a fresh transaction even
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void persistKbDraftSuccess(ClaimedOutboxWork work, KbDraftGenerationResult result) {
	    OutboxEvent outbox = outboxRepo.findById(work.outboxId)
	            .orElseThrow(() -> new IllegalStateException("OutboxEvent not found: " + work.outboxId));

	    Ticket ticket = ticketRepo.findById(work.aggregateId)
	            .orElseThrow(() -> new IllegalStateException("Ticket not found: " + work.aggregateId));

	    OffsetDateTime now = OffsetDateTime.now();

	    PERSISTENCE_LOG.info("OutboxAiPersistenceService :: in persistKbDraftSuccess() :: oeId={} eventType={} ticketId={} draftTitle={}",
	            work.outboxId, work.eventType, work.aggregateId, result.title);

	    //6.1) Persist AI audit in ai_decisions
	    Object rawJsonObject;
	    try {
	        rawJsonObject = objectMapper.readValue(result.rawOutputJson, Map.class);
	    } catch (Exception e) {
	        rawJsonObject = Map.of(
	                "title", result.title,
	                "body", result.body,
	                "confidence", result.confidence,
	                "reason", result.reason
	        );
	    }

	    aiDecisionRepo.save(buildDecision(
            work.aggregateId,
            work.textVersion,
            AiDecisionType.KB_DRAFT.name(),
            rawJsonObject,
            result.confidence,
            null,
            null
	    ));

	    //6.2) Create editable DRAFT row in kb_articles
	    if (ticket.getAssignedTo() == null) {
	        throw new IllegalStateException("Resolved ticket has no assigned agent for KB draft creation");
	    }

	    KbArticle draft = new KbArticle();
	    draft.setTitle(result.title);
	    draft.setBody(result.body);
	    draft.setStatus(KbArticleStatus.DRAFT.name());
	    draft.setCreatedBy(ticket.getAssignedTo());
	    draft.setSourceTicket(ticket);
	    draft.setLastModifiedBy(ticket.getAssignedTo());
	    draft.setApprovedBy(null);
	    draft.setAiGenerated(true);
	    draft.setCreatedAt(now);
	    draft.setUpdatedAt(now);
	    draft.setAgentSubmittedAt(null);
	    draft.setApprovedAt(null);

	    kbArticleRepo.save(draft);

	    //6.3) Mark outbox done
	    outbox.setStatus("DONE");
	    outbox.setProcessedAt(now);
	    outbox.setNextRunAt(null);
	    outbox.setLastError(null);
	    outboxRepo.save(outbox);

	    PERSISTENCE_LOG.info("OutboxAiPersistenceService :: exit persistKbDraftSuccess() :: oeId={} ticketId={} kbDraftTitle={}",
	            outbox.getOeId(), work.aggregateId, result.title);
	}

	//7) Persist step FAILURE in a fresh transaction so that retry state is not lost after rollback-only errors
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void persistStepFailure(ClaimedOutboxWork work, String error, int maxRetries) {
		OutboxEvent outbox = outboxRepo.findById(work.outboxId)
				.orElseThrow(() -> new IllegalStateException("OutboxEvent not found: " + work.outboxId));

		Ticket ticket = ticketRepo.findById(work.aggregateId)
				.orElseThrow(() -> new IllegalStateException("Ticket not found: " + work.aggregateId));

		int nextRetryCount = (outbox.getRetryCount() == null ? 0 : outbox.getRetryCount()) + 1;
		String truncatedError = deriveErrorMessage(error);

		PERSISTENCE_LOG.info("OutboxAiPersistenceService :: in persistStepFailure() :: oeId={} ticketId={} truncatedError={}",
				outbox.getOeId(), work.aggregateId, truncatedError);
		outbox.setRetryCount(nextRetryCount);
		outbox.setLastError(truncatedError);

		if (nextRetryCount >= maxRetries) {
			OffsetDateTime now = OffsetDateTime.now();
			//3B.1) Permanent Failure i.e. 5 retries done still failed. Mark outbox row as FAILED
			outbox.setStatus("FAILED");
			outbox.setProcessedAt(now);
			outbox.setNextRunAt(null);
			outboxRepo.save(outbox);

			//Only TRIAGE failure should mark high-level AI failure on ticket table's column ticket_ai_failed
			if (OutboxEventType.TRIAGE_REQUESTED.name().equals(outbox.getEventType())) {
				//3B.2) Mark ticket as READY though ai_failed TRUE for indicating manual triage needed
				ticket.setAiFailed(true);
				ticket.setAiLastError(truncatedError);
				ticket.setStatus(TicketStatus.READY);
				ticket.setUpdatedAt(now);
				ticketRepo.save(ticket);
			} else if (OutboxEventType.DUPLICATE_CHECK_REQUESTED.name().equals(work.eventType)
					|| OutboxEventType.KB_DRAFT_REQUESTED.name().equals(work.eventType)) {
				//Don't mark ticket_ai_fialed for non-triage AI stage failures
				ticket.setUpdatedAt(now);
				ticket.setAiLastError(truncatedError);
				ticketRepo.save(ticket);
			} else if (OutboxEventType.KB_SUGGESTION_REQUESTED.name().equals(work.eventType)) {
				//KB suggestion is optional. If it permanently fails, we continue the pipeline with ROUTING
		        //so the ticket does not get stuck after duplicate stage.
		        ticket.setStatus(TicketStatus.READY);
		        ticket.setUpdatedAt(now);
		        ticket.setAiLastError(truncatedError);
		        ticketRepo.save(ticket);

		        enqueueRoutingFallback(ticket, work.textVersion, now);

		        PERSISTENCE_LOG.warn(
		                "OutboxAiPersistenceService :: persistStepFailure() :: KB suggestion permanently failed, routing fallback queued :: oeId={} ticketId={} eventType={} retryCount={} err={}",
		                outbox.getOeId(), work.aggregateId, outbox.getEventType(), nextRetryCount, truncatedError
		        );
			}
			else if (OutboxEventType.ROUTING_REQUESTED.name().equals(work.eventType)) {
				//ROUTING failure won't erase TRIAGE success/fail or mark AI failure in tickets table
				ticket.setUpdatedAt(now);
				ticket.setAiLastError(truncatedError);
				ticketRepo.save(ticket);
			}

			PERSISTENCE_LOG.error("OutboxAiPersistenceService :: exit persistStepFailure() permanent FAILED :: oeId={} ticketId={} eventType={} retryCount={} err={}",
					outbox.getOeId(), work.aggregateId, outbox.getEventType(), nextRetryCount, truncatedError);
		} else {
			//3C.1) Retry the outbox row change status for PROCESSING -> PENDING again
			outbox.setStatus("PENDING");
			outbox.setNextRunAt(OffsetDateTime.now().plusSeconds(backoffSeconds(nextRetryCount)));
			outboxRepo.save(outbox);

			//TRIAGE retry means still processing initial AI triage so mark ticket AI_PROCESSING
			if (OutboxEventType.TRIAGE_REQUESTED.name().equals(work.eventType)) {
				ticket.setAiFailed(false);
				ticket.setAiLastError(truncatedError);
				ticket.setStatus(TicketStatus.AI_PROCESSING);
				ticket.setUpdatedAt(OffsetDateTime.now());
				ticketRepo.save(ticket);
			} else if (OutboxEventType.DUPLICATE_CHECK_REQUESTED.name().equals(work.eventType) 
					|| OutboxEventType.KB_SUGGESTION_REQUESTED.name().equals(work.eventType)
					|| OutboxEventType.KB_DRAFT_REQUESTED.name().equals(work.eventType)) {
				ticket.setUpdatedAt(OffsetDateTime.now());
				ticket.setAiLastError(truncatedError);
				ticketRepo.save(ticket);
			} else if (OutboxEventType.ROUTING_REQUESTED.name().equals(work.eventType)) {
				//ROUTING retry should not change READY business status
				ticket.setUpdatedAt(OffsetDateTime.now());
				ticketRepo.save(ticket);
			}
			PERSISTENCE_LOG.warn("OutboxAiPersistenceService :: exit persistStepFailure() retry scheduled :: oeId={} ticketId={} eventType={} retryCount={} nextRunAt={} err={}",
					outbox.getOeId(), work.aggregateId, outbox.getEventType(), nextRetryCount, outbox.getNextRunAt(), truncatedError);
		}
	}
	
	//Added if KB_SUGGESTION step failed
	private void enqueueRoutingFallback(Ticket ticket, Integer textVersion, OffsetDateTime now) {
	    OutboxEvent routingEvent = new OutboxEvent();
	    routingEvent.setEventType(OutboxEventType.ROUTING_REQUESTED.name());
	    routingEvent.setAggregateType(AggregateType.TICKET.name());
	    routingEvent.setAggregateId(ticket.getTicketId());
	    routingEvent.setPayload(toJson(Map.of("textVersion", textVersion == null ? ticket.getCurrentTextVersion() : textVersion)));
	    routingEvent.setStatus("PENDING");
	    routingEvent.setRetryCount(0);
	    routingEvent.setCreatedAt(now);
	    outboxRepo.save(routingEvent);
	}

	private AiDecision buildDecision(long ticketId, int textVersion, String type, Object jsonObj, BigDecimal confidence,
			BigDecimal similarity, BigDecimal threshold) {
		AiDecision ad = new AiDecision();
		ad.setTicketId(ticketId);
		ad.setTextVersion(textVersion);
		ad.setDecisionType(type);
		ad.setConfidence(confidence);
		ad.setSimilarity(similarity);
		ad.setThreshold(threshold);
		ad.setCreatedAt(OffsetDateTime.now());
		ad.setOutputJson(toJson(jsonObj));
		return ad;
	}

	private int payloadTextVersion(String payload) {
		try {
			if (payload == null || payload.isBlank())
				return 1;
			Map<?, ?> map = objectMapper.readValue(payload, Map.class);
			Object v = map.get("textVersion");
			return v == null ? 1 : Integer.parseInt(v.toString());
		} catch (Exception e) {
			return 1;
		}
	}

	private String toJson(Object obj) {
		try {
			return objectMapper.writeValueAsString(obj);
		} catch (Exception e) {
			return "{}";
		}
	}

	//Backoff scheduling
	private long backoffSeconds(int retryCount) {
		return switch (retryCount) {
		case 1 -> 5;
		case 2 -> 15;
		case 3 -> 30;
		case 4 -> 60;
		default -> 120;
		};
	}

	//If the error message is null like a null pointer exception then set a custom message to be persisted
	private String deriveErrorMessage(String error) {
		if (error == null || error.isBlank())
			return "Unknown processing error";
		return truncate(error, 2000);
	}

	//Shorten the exception message
	private String truncate(String s, int max) {
		if (s == null)
			return null;
		return s.length() <= max ? s : s.substring(0, max);
	}

	private List<Long> payloadSelectedCommentIds(String payload) {
	    try {
	        if (payload == null || payload.isBlank()) {
	            return Collections.emptyList();
	        }

	        Map<?, ?> map = objectMapper.readValue(payload, Map.class);
	        Object raw = map.get("selectedCommentIds");

	        if (!(raw instanceof List<?> rawList)) {
	            return Collections.emptyList();
	        }

	        List<Long> ids = new ArrayList<>();
	        for (Object item : rawList) {
	            if (item != null) {
	                ids.add(Long.parseLong(item.toString()));
	            }
	        }
	        return ids;

	    } catch (Exception e) {
	        return Collections.emptyList();
	    }
	}
}
