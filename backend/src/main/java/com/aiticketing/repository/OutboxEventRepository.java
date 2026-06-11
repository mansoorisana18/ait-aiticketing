package com.aiticketing.repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.aiticketing.entity.OutboxEvent;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

	//DB will lock these rows while this transaction runs so that another worker wont fetch the same pending rows
	//there is a single aiworker that polls the db as of now 
//    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT e FROM OutboxEvent e
        WHERE e.status = 'PENDING'
        AND (e.nextRunAt IS NULL OR e.nextRunAt <= :now)
        ORDER BY e.createdAt ASC
    """)
    List<OutboxEvent> findPendingBatch(@Param("now") OffsetDateTime now, Pageable pageable);

    //For atomic update to a PENDING outbox row
    //As multiple pods can fetch the same event, but only one pod should successfully update it.
    //Modifying: to tell Spring we are modifying the database state
    //flushAutomatically: Flush current transaction's pending entity changes before UPDATE.
    //clearAutomatically: Clear current persistence context after UPDATE to avoid stale entities.
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE OutboxEvent e
        SET e.status = 'PROCESSING',
            e.lastError = null
        WHERE e.oeId = :oeId
          AND e.status = 'PENDING'
          AND (e.nextRunAt IS NULL OR e.nextRunAt <= :now)
    """)
    int claimEventIfPending(@Param("oeId") Long oeId,
                            @Param("now") OffsetDateTime now);
    
    Optional<OutboxEvent> findTopByAggregateIdAndEventTypeAndStatusOrderByOeIdDesc(
            Long aggregateId,
            String eventType,
            String status
    );
}