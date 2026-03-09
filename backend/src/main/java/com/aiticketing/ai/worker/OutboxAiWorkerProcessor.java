package com.aiticketing.ai.worker;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.aiticketing.ai.dto.RoutingResult;
import com.aiticketing.ai.dto.TriageOutcome;
import com.aiticketing.ai.dto.TriageResult;
import com.aiticketing.ai.service.AiTriageService;
import com.aiticketing.ai.service.RoutingService;
import com.aiticketing.entity.AiDecision;
import com.aiticketing.entity.OutboxEvent;
import com.aiticketing.entity.Ticket;
import com.aiticketing.entity.enums.AggregateType;
import com.aiticketing.entity.enums.AiDecisionType;
import com.aiticketing.entity.enums.OutboxEventType;
import com.aiticketing.entity.enums.TicketStatus;
import com.aiticketing.repository.AiDecisionRepository;
import com.aiticketing.repository.OutboxEventRepository;
import com.aiticketing.repository.TicketRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class OutboxAiWorkerProcessor {
	private static final Logger PROCESSOR_LOG = LoggerFactory.getLogger(OutboxAiWorkerProcessor.class); 
	
    private final int maxRetries;

    private final OutboxEventRepository outboxRepo;
    private final TicketRepository ticketRepo;
    private final AiDecisionRepository aiDecisionRepo;
    private final AiTriageService triageService;
    private final ObjectMapper objectMapper;
    private final RoutingService routingService;

    public OutboxAiWorkerProcessor (
            @Value("${aiticketing.ai.worker.max-retries}") int maxRetries,
            OutboxEventRepository outboxRepo,
            TicketRepository ticketRepo,
            AiDecisionRepository aiDecisionRepo,
            AiTriageService triageService,
            ObjectMapper objectMapper,
            RoutingService routingService
    ) {
        this.maxRetries = maxRetries;
        this.outboxRepo = outboxRepo;
        this.ticketRepo = ticketRepo;
        this.aiDecisionRepo = aiDecisionRepo;
        this.triageService = triageService;
        this.objectMapper = objectMapper;
        this.routingService = routingService;
    }
    
    //each outbox_events db pending row will be a separate transaction
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleOneTransactional(Long oeId) {
    	OutboxEvent outbox = outboxRepo.findById(oeId)
                .orElseThrow(() -> new IllegalStateException("OutboxEvent not found: " + oeId));

        if (!"PENDING".equalsIgnoreCase(outbox.getStatus())) {
            PROCESSOR_LOG.debug("OutboxAiWorkerProcessor :: in handleOneTransactional() skipping oeId={} because status={}", oeId, outbox.getStatus());
            return;
        }
        
    	long ticketId = outbox.getAggregateId();
        int textVersion = payloadTextVersion(outbox.getPayload());
        
        PROCESSOR_LOG.info("OutboxAiWorkerProcessor :: in handleOneTransactional() start :: oeId={} ticketId={} textVersion={} retryCount={}",
        		outbox.getOeId(), ticketId, textVersion, outbox.getRetryCount());
        
        //1.1) mark outbox row as PROCESSING
    	outbox.setStatus("PROCESSING");
    	outbox.setLastError(null);
    	outboxRepo.save(outbox);
    	
        try {
        	String eventType = outbox.getEventType();

            if (OutboxEventType.TRIAGE_REQUESTED.name().equals(eventType)) {
                processTriage(outbox, ticketId, textVersion);
            } else if (OutboxEventType.ROUTING_REQUESTED.name().equals(eventType)) {
                processRouting(outbox, ticketId, textVersion);
            } else {
                outbox.setStatus("DONE");
                outbox.setProcessedAt(OffsetDateTime.now());
                outboxRepo.save(outbox);
            }    

        } catch (Exception ex) {
        	PROCESSOR_LOG.info("OutboxAiWorkerProcessor :: exit handleOneTransactional() :: EXCEPTION :: oeId={} ticketId={} textVersion={} retryCount={}",
            		outbox.getOeId(), ticketId, textVersion, outbox.getRetryCount());
        	handleFailure(outbox, ticketId, ex);
        }
    }

    private void processTriage(OutboxEvent outbox, long ticketId, int textVersion) {
    	Ticket ticket = ticketRepo.findById(ticketId)
                .orElseThrow(() -> new IllegalStateException("Ticket not found: " + ticketId));

    	//1.2) mark the ticket row as AI_PROCESSING
        ticket.setStatus(TicketStatus.AI_PROCESSING);
        ticket.setAiFailed(false);
        ticket.setAiLastError(null);
        ticket.setUpdatedAt(OffsetDateTime.now());
        ticketRepo.save(ticket);

        PROCESSOR_LOG.info("OutboxAiWorkerProcessor :: in processTriage() triage start :: ticketId={} titleLen={} descLen={}",
                ticketId,
                (ticket.getTitle() == null ? 0 : ticket.getTitle().length()),
                (ticket.getDescription() == null ? 0 : ticket.getDescription().length()));

        //2.1) Call AiTriageService to call LLM & set response
        TriageOutcome outcome = triageService.triage(ticketId, textVersion, ticket.getTitle(), ticket.getDescription());

        if (!outcome.success) {
            PROCESSOR_LOG.warn("OutboxAiWorkerProcessor :: in processTriage() triage failed :: ticketId={} error={}", ticketId, outcome.error);
            throw new RuntimeException(outcome.error);
        }

        TriageResult r = outcome.result;

        //2.2) update ticket with successful AI triage results & status as READY
        ticket.setAiCategory(r.category);
        ticket.setAiPriority(r.priority);
        ticket.setAiConfidence(r.confidence);
        ticket.setAiFailed(false);
        ticket.setAiLastError(null);
        ticket.setStatus(TicketStatus.READY);
        ticket.setUpdatedAt(OffsetDateTime.now());
        ticketRepo.save(ticket);

        //4.1) Persist AI decisions for audit + dashboards in ai_decisions table (for classification)
        aiDecisionRepo.save(buildDecision(ticketId, textVersion, AiDecisionType.CLASSIFICATION.name(),
                Map.of("category", r.category), r.confidence));

        //4.2) Persist AI decisions for audit + dashboards in ai_decisions table (for Priority)
        aiDecisionRepo.save(buildDecision(ticketId, textVersion, AiDecisionType.PRIORITY.name(),
                Map.of("priority", r.priority.name()), r.confidence));
        
        //Enqueue next event_type step 2. routing in outbox table
        OutboxEvent routingEvent = new OutboxEvent();
        routingEvent.setEventType(OutboxEventType.ROUTING_REQUESTED.name());
        routingEvent.setAggregateType(AggregateType.TICKET.name());
        routingEvent.setAggregateId(ticketId);
        routingEvent.setPayload(toJson(Map.of("textVersion", textVersion)));
        routingEvent.setStatus("PENDING");
        routingEvent.setRetryCount(0);
        routingEvent.setCreatedAt(OffsetDateTime.now());
        outboxRepo.save(routingEvent);
        
        //5.1)mark outbox row as DONE on success
        outbox.setStatus("DONE");
        outbox.setProcessedAt(OffsetDateTime.now());
        outbox.setNextRunAt(null);
        outbox.setLastError(null);
        outboxRepo.save(outbox);

        PROCESSOR_LOG.info("OutboxAiWorkerProcessor :: exit processTriage() :: TRIAGE done :: oeId={} ticketId={} category={} priority={} confidence={}", outbox.getOeId(), ticketId, r.category, r.priority, r.confidence);
    }
    
    private void processRouting(OutboxEvent outbox, long ticketId, int textVersion) {
    	PROCESSOR_LOG.info("OutboxAiWorkerProcessor :: in processRouting() routing start :: ticketId={}",ticketId);

    	Ticket ticket = ticketRepo.findById(ticketId)
                .orElseThrow(() -> new IllegalStateException("Ticket not found: " + ticketId));
    	
        if (ticket.getAiCategory() == null || ticket.getAiCategory().isBlank()) {
            throw new IllegalStateException("Cannot route ticket without aiCategory");
        }
 
    	//3A.1) do routing/assignment based on ai suggested category == department & least workload
        RoutingResult routing = routingService.assignIfPossible(ticket, ticket.getAiCategory());
        ticketRepo.save(ticket);

        PROCESSOR_LOG.debug("OutboxAiWorkerProcessor :: in processRouting() :: before saving ROUTING decision in ai_decisions :: ticketId={}",ticketId);       
        aiDecisionRepo.save(buildDecision(
                ticketId,
                textVersion,
                AiDecisionType.ROUTING.name(),
                Map.of(
                        "department", routing.department,
                        "outcome", routing.outcome.name(),
                        "selectedAgentId", routing.selectedAgentId,
                        "selectedWorkload", routing.selectedWorkload,
                        "eligibleAgentCount", routing.eligibleAgentCount
                ),
                ticket.getAiConfidence()
        ));
        
        //5.1)mark outbox row as DONE on success
        outbox.setStatus("DONE");
        outbox.setProcessedAt(OffsetDateTime.now());
        outbox.setNextRunAt(null);
        outbox.setLastError(null);
        outboxRepo.save(outbox);

        PROCESSOR_LOG.info("OutboxAiWorkerProcessor :: exit processRouting() :: ROUTING done :: oeId={} ticketId={} outcome={} selectedAgentId={}", outbox.getOeId(), ticketId, routing.outcome, routing.selectedAgentId);
    }

    private void handleFailure(OutboxEvent outbox, long ticketId, Exception ex) {
    	PROCESSOR_LOG.info("OutboxAiWorkerProcessor :: in handleFailure() :: oeId={} ticketId={}", outbox.getOeId(), ticketId);
    	
    	int nextRetryCount = (outbox.getRetryCount() == null ? 0 : outbox.getRetryCount()) + 1;

        outbox.setRetryCount(nextRetryCount);
        outbox.setLastError(truncate(ex.getMessage(), 2000));

        if (nextRetryCount >= maxRetries) {
        	//3B.1) Permanent Failure i.e. 5 retries done still failed. Mark outbox row as FAILED
        	outbox.setStatus("FAILED");
        	outbox.setProcessedAt(OffsetDateTime.now());
        	outbox.setNextRunAt(null);
        	outboxRepo.save(outbox);
        	
        	//Only TRIAGE failure should mark ticket_ai_failed
            if (OutboxEventType.TRIAGE_REQUESTED.name().equals(outbox.getEventType())) {
                Ticket ticket = ticketRepo.findById(ticketId).orElse(null);
                if (ticket != null) {
		        	//3B.2) Mark ticket as READY though ai_failed TRUE for indicating manual triage needed
                	ticket.setAiFailed(true);
                	ticket.setAiLastError(truncate(ex.getMessage(), 2000));
                	ticket.setStatus(TicketStatus.READY);
                	ticket.setUpdatedAt(OffsetDateTime.now());
		            ticketRepo.save(ticket);
                }
            }
        	
            PROCESSOR_LOG.error("OutboxAiWorkerProcessor :: exit handleFailure() permanent FAILED :: oeId={} ticketId={} eventType={} retryCount={} err={}",
            		outbox.getOeId(), ticketId, outbox.getEventType(), nextRetryCount, ex.getMessage());
        } else {
        	//3C.1) Retry the outbox row change status for PROCESSING -> PENDING again
        	outbox.setStatus("PENDING");
        	outbox.setNextRunAt(OffsetDateTime.now().plusSeconds(backoffSeconds(nextRetryCount)));
            outboxRepo.save(outbox);
            
        	PROCESSOR_LOG.warn("OutboxAiWorkerProcessor :: exit handleFailure() retry scheduled :: oeId={} ticketId={} eventType={} retryCount={} nextRunAt={} err={}",
            		outbox.getOeId(), ticketId, outbox.getEventType(), nextRetryCount, outbox.getNextRunAt(), ex.getMessage());
        }
    }
    
    private AiDecision buildDecision(long ticketId, int textVersion, String type, Object jsonObj, BigDecimal confidence) {
        AiDecision ad = new AiDecision();
        ad.setTicketId(ticketId);
        ad.setTextVersion(textVersion);
        ad.setDecisionType(type);
        ad.setConfidence(confidence);
        ad.setCreatedAt(OffsetDateTime.now());
        ad.setOutputJson(toJson(jsonObj));
        return ad;
    }

    private int payloadTextVersion(String payload) {
        try {
            if (payload == null || payload.isBlank()) 
            	return 1;
            Map<?,?> map = objectMapper.readValue(payload, Map.class);
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
    
    //Shorten the exception message
    private String truncate(String s, int max) {
    	if(s==null) 
    		return null;
    	return s.length() <= max ? s : s.substring(0,max);
    }

}
