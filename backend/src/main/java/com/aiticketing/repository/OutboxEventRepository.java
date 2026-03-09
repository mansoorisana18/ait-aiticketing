package com.aiticketing.repository;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.aiticketing.entity.OutboxEvent;

import jakarta.persistence.LockModeType;

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

}