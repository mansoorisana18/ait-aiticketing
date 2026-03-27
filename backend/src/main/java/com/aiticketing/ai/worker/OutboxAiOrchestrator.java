package com.aiticketing.ai.worker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.aiticketing.ai.dto.ClaimedOutboxWork;
import com.aiticketing.ai.dto.RoutingResult;
import com.aiticketing.ai.dto.TriageOutcome;
import com.aiticketing.ai.dto.TriageResult;
import com.aiticketing.ai.service.AiTriageService;
import com.aiticketing.ai.service.DuplicateDetectionService;
import com.aiticketing.ai.service.RoutingService;
import com.aiticketing.entity.Ticket;
import com.aiticketing.entity.enums.OutboxEventType;
import com.aiticketing.repository.TicketRepository;

@Component
public class OutboxAiOrchestrator {

    private static final Logger ORCHESTRATOR_LOG = LoggerFactory.getLogger(OutboxAiOrchestrator.class);

    private final int maxRetries;

    private final OutboxAiPersistenceService persistenceService;
    private final DuplicateDetectionService duplicateDetectionService;
    private final AiTriageService triageService;
    private final RoutingService routingService;
    private final TicketRepository ticketRepo;

    public OutboxAiOrchestrator(
            @Value("${aiticketing.ai.worker.max-retries}") int maxRetries,
            DuplicateDetectionService duplicateDetectionService,
            OutboxAiPersistenceService persistenceService,
            AiTriageService triageService,
            RoutingService routingService,
            TicketRepository ticketRepo
    ) {
        this.maxRetries = maxRetries;
        this.duplicateDetectionService = duplicateDetectionService;
        this.persistenceService = persistenceService;
        this.triageService = triageService;
        this.routingService = routingService;
        this.ticketRepo = ticketRepo;
    }

    public void processOutboxEvent(Long oeId) {
    	//1) Mark outbox row as PENDING - Transaction 1
    	ORCHESTRATOR_LOG.info("OutboxAiOrchestrator :: in processOutboxEvent() start :: oeId={}", oeId);
        ClaimedOutboxWork work = persistenceService.claimEvent(oeId);

        if (work == null) {
            ORCHESTRATOR_LOG.debug("OutboxAiOrchestrator :: in processOutboxEvent() skipped oeId={} because not claimable", oeId);
            return;
        }
        
        try {
            if (OutboxEventType.TRIAGE_REQUESTED.name().equals(work.eventType)) {            	
                handleTriage(work);
            } else if (OutboxEventType.DUPLICATE_CHECK_REQUESTED.name().equals(work.eventType)) {
            	handleDuplicateCheck(work);
            } else if (OutboxEventType.ROUTING_REQUESTED.name().equals(work.eventType)) {
                handleRouting(work);
            } else {
                persistenceService.persistStepFailure(work,
                        "Unsupported event type: " + work.eventType,
                        maxRetries);
            }
        } catch (Exception ex) {
        	//3B) Caught & call persistFailure - Transaction 2B
        	String err = (ex.getMessage() != null && !ex.getMessage().isBlank())
                    ? ex.getMessage()
                    : ex.toString();
            persistenceService.persistStepFailure(work, err, maxRetries);
        }
    }

    private void handleTriage(ClaimedOutboxWork work) {
        ORCHESTRATOR_LOG.info("OutboxAiOrchestrator :: in handleTriage() start :: oeId={} ticketId={} textVersion={}",
                work.outboxId, work.aggregateId, work.textVersion);

        //2) LLM call outside a Transaction
        TriageOutcome outcome = triageService.triage(
                work.aggregateId,
                work.textVersion,
                work.ticketTitle,
                work.ticketDescription
        );

        if (!outcome.success) {
        	//3B) Caught & call persistFailure 
            throw new RuntimeException(outcome.error);
        }

        TriageResult result = outcome.result;

        //3A) Persist success - Transaction 2A
        persistenceService.persistTriageSuccess(work, result);

        ORCHESTRATOR_LOG.info("OutboxAiOrchestrator :: handleTriage() success :: oeId={} ticketId={} category={} priority={} isVague={}",
                work.outboxId, work.aggregateId, result.category, result.priority, result.isVague);
    }
    
    private void handleDuplicateCheck(ClaimedOutboxWork work) {
        ORCHESTRATOR_LOG.info("OutboxAiOrchestrator :: in handleDuplicateCheck() start :: oeId={} ticketId={} textVersion={}",
                work.outboxId, work.aggregateId, work.textVersion);
       
        var outcome = duplicateDetectionService.checkDuplicate(
                work.aggregateId,
                work.textVersion,
                work.ticketTitle,
                work.ticketDescription
        );

        if (!outcome.success) {
        	//Caught & call persistFailure 
            throw new RuntimeException(outcome.error);
        }
        
        //Persist success - Transaction
        persistenceService.persistDuplicateCheckSuccess(work, outcome.result);

        ORCHESTRATOR_LOG.info("OutboxAiOrchestrator :: handleDuplicateCheck() success :: oeId={} ticketId={} duplicateState={} primaryTicketId={}",
                work.outboxId, work.aggregateId, outcome.result.duplicateState, outcome.result.primaryTicketId);
    }

    private void handleRouting(ClaimedOutboxWork work) {
        ORCHESTRATOR_LOG.info("OutboxAiOrchestrator :: in handleRouting() start :: oeId={} ticketId={} category={}",
                work.outboxId, work.aggregateId, work.ticketAiCategory);

        Ticket ticket = ticketRepo.findById(work.aggregateId)
                .orElseThrow(() -> new IllegalStateException("Ticket not found: " + work.aggregateId));

        if (ticket.getAiCategory() == null || ticket.getAiCategory().isBlank()) {
            throw new IllegalStateException("Cannot route ticket without aiCategory");
        }

        //2)Automated ROUTING logic - Transaction 1
        RoutingResult routingResult = routingService.assignIfPossible(ticket, ticket.getAiCategory());

        persistenceService.persistRoutingSuccess(work, routingResult);

        ORCHESTRATOR_LOG.info("OutboxAiOrchestrator :: handleRouting() success :: oeId={} ticketId={} outcome={} selectedAgentId={}",
                work.outboxId, work.aggregateId, routingResult.outcome, routingResult.selectedAgentId);
    }
}