package com.aiticketing.entity;

import java.time.OffsetDateTime;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "outbox_events")
public class OutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "oe_id")
    private Long oeId;

    @Column(name = "oe_event_type", nullable = false, length = 60)
    private String eventType;

    @Column(name = "oe_aggregate_type", nullable = false, length = 40)
    private String aggregateType;

    @Column(name = "oe_aggregate_id", nullable = false)
    private Long aggregateId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "oe_payload", nullable = false, columnDefinition = "jsonb")
    private String payload;

    @Column(name = "oe_status", nullable = false, length = 20)
    private String status;

    @Column(name = "oe_retry_count", nullable = false)
    private Integer retryCount = 0;

    @Column(name = "oe_next_run_at")
    private OffsetDateTime nextRunAt;

    @Column(name = "oe_last_error")
    private String lastError;

    @Column(name = "oe_created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "oe_processed_at")
    private OffsetDateTime processedAt;

	public Long getOeId() {
		return oeId;
	}

	public void setOeId(Long oeId) {
		this.oeId = oeId;
	}

	public String getEventType() {
		return eventType;
	}

	public void setEventType(String eventType) {
		this.eventType = eventType;
	}

	public String getAggregateType() {
		return aggregateType;
	}

	public void setAggregateType(String aggregateType) {
		this.aggregateType = aggregateType;
	}

	public Long getAggregateId() {
		return aggregateId;
	}

	public void setAggregateId(Long aggregateId) {
		this.aggregateId = aggregateId;
	}

	public String getPayload() {
		return payload;
	}

	public void setPayload(String payload) {
		this.payload = payload;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Integer getRetryCount() {
		return retryCount;
	}

	public void setRetryCount(Integer retryCount) {
		this.retryCount = retryCount;
	}

	public OffsetDateTime getNextRunAt() {
		return nextRunAt;
	}

	public void setNextRunAt(OffsetDateTime nextRunAt) {
		this.nextRunAt = nextRunAt;
	}

	public String getLastError() {
		return lastError;
	}

	public void setLastError(String lastError) {
		this.lastError = lastError;
	}

	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(OffsetDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public OffsetDateTime getProcessedAt() {
		return processedAt;
	}

	public void setProcessedAt(OffsetDateTime processedAt) {
		this.processedAt = processedAt;
	}
}