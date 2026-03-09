package com.aiticketing.entity;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.*;

@Entity
@Table(name = "ai_decisions")
public class AiDecision {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ai_decision_id")
    private Long id;

    @Column(name = "ad_ticket_id", nullable = false)
    private Long ticketId;

    @Column(name = "ad_text_version", nullable = false)
    private Integer textVersion;

    @Column(name = "ad_decision_type", nullable = false, length = 40)
    private String decisionType; // CLASSIFICATION / PRIORITY

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "ad_output_json", nullable = false, columnDefinition = "jsonb")
    private String outputJson;

    @Column(name = "ad_confidence", precision = 4, scale = 3)
    private BigDecimal confidence;

    @Column(name = "ad_created_at", nullable = false)
    private OffsetDateTime createdAt;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getTicketId() {
		return ticketId;
	}

	public void setTicketId(Long ticketId) {
		this.ticketId = ticketId;
	}

	public Integer getTextVersion() {
		return textVersion;
	}

	public void setTextVersion(Integer textVersion) {
		this.textVersion = textVersion;
	}

	public String getDecisionType() {
		return decisionType;
	}

	public void setDecisionType(String decisionType) {
		this.decisionType = decisionType;
	}

	public String getOutputJson() {
		return outputJson;
	}

	public void setOutputJson(String outputJson) {
		this.outputJson = outputJson;
	}

	public BigDecimal getConfidence() {
		return confidence;
	}

	public void setConfidence(BigDecimal confidence) {
		this.confidence = confidence;
	}

	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(OffsetDateTime createdAt) {
		this.createdAt = createdAt;
	}

}