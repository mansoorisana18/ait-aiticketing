package com.aiticketing.ai.worker;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.aiticketing.ai.dto.ClaimedOutboxWork;
import com.aiticketing.ai.dto.RoutingResult;
import com.aiticketing.ai.dto.TriageResult;
import com.aiticketing.entity.AiDecision;
import com.aiticketing.entity.OutboxEvent;
import com.aiticketing.entity.Ticket;
import com.aiticketing.entity.User;
import com.aiticketing.entity.enums.AggregateType;
import com.aiticketing.entity.enums.AiDecisionType;
import com.aiticketing.entity.enums.OutboxEventType;
import com.aiticketing.entity.enums.TicketStatus;
import com.aiticketing.repository.AiDecisionRepository;
import com.aiticketing.repository.OutboxEventRepository;
import com.aiticketing.repository.TicketRepository;
import com.aiticketing.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;


/*
 * This class is to separate our transaction boundaries.
 * 1) Mark claim event ie. outbox state to PROCESSING
 * 2) Persist TRIAGE (category, priority, vague) SUCCESS
 * 3) Persist ROUTING (assignment based on aiCategory & least agent active workload) SUCCESS
 * 4) Persist failure Steps so that we can persist retry details in outbox
*/

@Service
public class OutboxAiPersistenceService {
	private static final Logger PERSISTENCE_LOG = LoggerFactory.getLogger(OutboxAiPersistenceService.class); 

    private final OutboxEventRepository outboxRepo;
    private final UserRepository userRepo;
    private final TicketRepository ticketRepo;
    private final AiDecisionRepository aiDecisionRepo;
    private final ObjectMapper objectMapper;

    public OutboxAiPersistenceService (           
            OutboxEventRepository outboxRepo,
            UserRepository userRepo,
            TicketRepository ticketRepo,
            AiDecisionRepository aiDecisionRepo,           
            ObjectMapper objectMapper
    ) {       
        this.outboxRepo = outboxRepo;
        this.userRepo = userRepo;
        this.ticketRepo = ticketRepo;
        this.aiDecisionRepo = aiDecisionRepo;       
        this.objectMapper = objectMapper;
    }
    
    //1) CLAIM the PENDING outbox row in a new transaction & store it in ClaimedOutboxWork dto 
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ClaimedOutboxWork claimEvent(Long oeId) {
    	OutboxEvent outbox = outboxRepo.findById(oeId)
                .orElseThrow(() -> new IllegalStateException("OutboxEvent not found: " + oeId));

        if (!"PENDING".equalsIgnoreCase(outbox.getStatus())) {
            PERSISTENCE_LOG.debug("OutboxAiPersistenceService :: in claimEvent() skipping oeId={} because status={}", oeId, outbox.getStatus());
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
            ticket.setUpdatedAt(now);
            ticketRepo.save(ticket);
        }
    	
        PERSISTENCE_LOG.debug("OutboxAiPersistenceService :: in claimEvent() :: start marked PROCESSING :: oeId={} retryCount={}",
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
                Map.of("category", result.category), result.confidence));

        //4.2) Persist AI decisions for audit + dashboards in ai_decisions table (for Priority)
        aiDecisionRepo.save(buildDecision(work.aggregateId, work.textVersion, AiDecisionType.PRIORITY.name(),
                Map.of("priority", result.priority.name()), result.confidence));
        
        //4.3) Persist AI decisions for audit + dashboards in ai_decisions table (for Vague)
        //map.of doesnt allow null keys or values
        Map<String, Object> vagueJson = new LinkedHashMap<>();
        vagueJson.put("isVague", result.isVague);
        vagueJson.put("vagueReason", result.vagueReason);
        vagueJson.put("clarificationPrompt", result.clarificationPrompt);        
        aiDecisionRepo.save(buildDecision(work.aggregateId, work.textVersion, AiDecisionType.VAGUE_CHECK.name(),
        		vagueJson, result.confidence));
        
        PERSISTENCE_LOG.debug("OutboxAiPersistenceService :: in persistTriageSuccess() :: Added TRIAGE steps in ai_decisions table :: oeId={} ticketId={}", 
    			work.outboxId, work.aggregateId);
        
        //Insert event_type ROUTNG_REQUESTED as PENDING in outbox table if ticket is not VAGUE
        if (!Boolean.TRUE.equals(result.isVague)) {
        	PERSISTENCE_LOG.debug("OutboxAiPersistenceService :: in persistTriageSuccess() :: Inserting ROUTING_REQUESTED in outbox :: oeId={} ticketId={}", 
        			work.outboxId, work.aggregateId);
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
        
        //5.1)mark outbox row as DONE on success
        outbox.setStatus("DONE");
        outbox.setProcessedAt(now);
        outbox.setNextRunAt(null);
        outbox.setLastError(null);
        outboxRepo.save(outbox);

        PERSISTENCE_LOG.info("OutboxAiPersistenceService :: exit persistTriageSuccess() :: oeId={} eventType={} ticketId={} isVague={}",
                work.outboxId, work.eventType, work.aggregateId, result.isVague);
    }
    
    //3) Persist ROUTING (assignment) SUCCESS in a fresh transaction even NO_ELIGIBLE_AGENT is still a successful completion of the routing step
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void persistRoutingSuccess(ClaimedOutboxWork work, RoutingResult routingResult) {
    	OutboxEvent outbox = outboxRepo.findById(work.outboxId)
                .orElseThrow(() -> new IllegalStateException("OutboxEvent not found: " + work.outboxId));
        
        Ticket ticket = ticketRepo.findById(work.aggregateId)
                .orElseThrow(() -> new IllegalStateException("Ticket not found: " + work.aggregateId));
        
        OffsetDateTime now = OffsetDateTime.now();
        
        if (routingResult.selectedAgentId != null && ticket.getFirstAssignedAt() == null) {
        	User agent = userRepo.findById(routingResult.selectedAgentId).orElseThrow(() -> new IllegalArgumentException("Selected Agent Id not found: "+routingResult.selectedAgentId));
        	ticket.setAssignedTo(agent);
        	
        	if (ticket.getFirstAssignedAt() == null)
        		ticket.setFirstAssignedAt(now);
        }
       
        PERSISTENCE_LOG.info("OutboxAiPersistenceService :: in persistRoutingSuccess() :: oeId={} eventType={} ticketId={}",
                work.outboxId, work.eventType, work.aggregateId);
        
        ticket.setUpdatedAt(now);
        ticketRepo.save(ticket);

        PERSISTENCE_LOG.debug("OutboxAiPersistenceService :: in persistRoutingSuccess() :: before saving ROUTING decision in ai_decisions :: ticketId={}",work.aggregateId);       
        aiDecisionRepo.save(buildDecision(
                work.aggregateId,
                work.textVersion,
                AiDecisionType.ROUTING.name(),
                Map.of(
                        "department", routingResult.department,
                        "outcome", routingResult.outcome.name(),
                        "selectedAgentId", routingResult.selectedAgentId,
                        "selectedWorkload", routingResult.selectedWorkload,
                        "eligibleAgentCount", routingResult.eligibleAgentCount
                ),
                ticket.getAiConfidence()
        ));
        
        //5.1)mark outbox row as DONE on success
        outbox.setStatus("DONE");
        outbox.setProcessedAt(OffsetDateTime.now());
        outbox.setNextRunAt(null);
        outbox.setLastError(null);
        outboxRepo.save(outbox);

        PERSISTENCE_LOG.info("OutboxAiPersistenceService :: exit persistRoutingSuccess() :: ROUTING done :: oeId={} ticketId={} outcome={} selectedAgentId={}", outbox.getOeId(), work.aggregateId, routingResult.outcome, routingResult.selectedAgentId);               
    }
    
    //4) Persist step FAILURE in a fresh transaction so that retry state is not lost after rollback-only errors
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void persistStepFailure(ClaimedOutboxWork work, String error, int maxRetries) {
    	OutboxEvent outbox = outboxRepo.findById(work.outboxId)
                .orElseThrow(() -> new IllegalStateException("OutboxEvent not found: " + work.outboxId));
        
        Ticket ticket = ticketRepo.findById(work.aggregateId)
                .orElseThrow(() -> new IllegalStateException("Ticket not found: " + work.aggregateId));
    	
    	int nextRetryCount = (outbox.getRetryCount() == null ? 0 : outbox.getRetryCount()) + 1;
    	String truncatedError = deriveErrorMessage(error);
    	
    	PERSISTENCE_LOG.info("OutboxAiPersistenceService :: in persistStepFailure() :: oeId={} ticketId={} truncatedError={}", outbox.getOeId(), work.aggregateId, truncatedError);
        outbox.setRetryCount(nextRetryCount);
        outbox.setLastError(truncatedError);

        if (nextRetryCount >= maxRetries) {
        	//3B.1) Permanent Failure i.e. 5 retries done still failed. Mark outbox row as FAILED
        	outbox.setStatus("FAILED");
        	outbox.setProcessedAt(OffsetDateTime.now());
        	outbox.setNextRunAt(null);
        	outboxRepo.save(outbox);
        	
        	//Only TRIAGE failure should mark high-level AI failure on ticket table's column ticket_ai_failed
            if (OutboxEventType.TRIAGE_REQUESTED.name().equals(outbox.getEventType())) {        
	        	//3B.2) Mark ticket as READY though ai_failed TRUE for indicating manual triage needed
            	ticket.setAiFailed(true);
            	ticket.setAiLastError(truncatedError);
            	ticket.setStatus(TicketStatus.READY);
            	ticket.setUpdatedAt(OffsetDateTime.now());
	            ticketRepo.save(ticket);                
            } else if (OutboxEventType.ROUTING_REQUESTED.name().equals(work.eventType)) {
            	//ROUTING failure won't erase TRIAGE success/fail or mark AI failure in tickets table
            	ticket.setUpdatedAt(OffsetDateTime.now());
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
            } else if (OutboxEventType.ROUTING_REQUESTED.name().equals(work.eventType)) {
                //ROUTING retry should not change READY business status
                ticket.setUpdatedAt(OffsetDateTime.now());
                ticketRepo.save(ticket);
            }
        	PERSISTENCE_LOG.warn("OutboxAiPersistenceService :: exit persistStepFailure() retry scheduled :: oeId={} ticketId={} eventType={} retryCount={} nextRunAt={} err={}",
            		outbox.getOeId(), work.aggregateId, outbox.getEventType(), nextRetryCount, outbox.getNextRunAt(), truncatedError);
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
    
    //If the error message is null like a null pointer exception then set a custom message to be persisted
    private String deriveErrorMessage(String error) {
    	if(error==null || error.isBlank())  
    		return "Unknown processing error";
    	return truncate(error,2000);
    }
    
    //Shorten the exception message
    private String truncate(String s, int max) {
    	if(s==null) 
    		return null;
    	return s.length() <= max ? s : s.substring(0,max);
    }

}
