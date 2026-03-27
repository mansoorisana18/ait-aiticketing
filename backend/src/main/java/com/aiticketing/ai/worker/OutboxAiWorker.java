package com.aiticketing.ai.worker;

import java.time.OffsetDateTime;
import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.aiticketing.entity.OutboxEvent;
import com.aiticketing.repository.OutboxEventRepository;

@Component
public class OutboxAiWorker {

	private static final Logger WORKER_LOG = LoggerFactory.getLogger(OutboxAiWorker.class); 
	
    private final boolean enabled;
    private final int batchSize;
    
    private final OutboxEventRepository outboxRepo;
    private final OutboxAiOrchestrator orchestrator;
    private final ExecutorService aiWorkerExecutor;

    public OutboxAiWorker(
            @Value("${aiticketing.ai.worker.enabled}") boolean enabled,
            @Value("${aiticketing.ai.worker.batch-size}") int batchSize,
            OutboxEventRepository outboxRepo,
            OutboxAiOrchestrator orchestrator,
            ExecutorService aiWorkerExecutor
    ) {
        this.enabled = enabled;
        this.batchSize = batchSize;        
        this.outboxRepo = outboxRepo;
        this.orchestrator = orchestrator;
        this.aiWorkerExecutor = aiWorkerExecutor;
    }
   
    //3 seconds polling interval to fetch PENDING rows & submit ask to executor 
    @Scheduled(fixedDelayString = "${aiticketing.ai.worker.poll-ms}")    
    public void poll() {
        if (!enabled) {
        	WORKER_LOG.debug("OutboxAiWorker :: in poll() skipped beacuse worker is disabled");
        	return;
        }

        var now = OffsetDateTime.now();
        var events = outboxRepo.findPendingBatch(now, PageRequest.of(0, batchSize));

        if (events.isEmpty()) {
            WORKER_LOG.debug("OutboxAiWorker :: in poll() noPendingEvents");
            return;
        }
        
        WORKER_LOG.info("OutboxAiWorker :: in poll() fetched :: pending events count={}", events.size());        
        
        for (OutboxEvent event : events) {     
        	aiWorkerExecutor.submit(() -> {
        		try {
                    WORKER_LOG.info("OutboxAiWorker :: in poll() :: dispatching oeId={} eventType={}",
                            event.getOeId(), event.getEventType());

                    orchestrator.processOutboxEvent(event.getOeId());

                    WORKER_LOG.info("OutboxAiWorker :: in poll() :: completed oeId={}", event.getOeId());

                } catch (Exception ex) {
                    WORKER_LOG.error("OutboxAiWorker :: in poll() :: EXCEPTION unexpected failure while processing oeId={} :: {}",
                            event.getOeId(), ex.getMessage(), ex);
                }   
        	});
        }
        WORKER_LOG.info("OutboxAiWorker :: exit poll() batch complete :: count={}", events.size());
    }
}