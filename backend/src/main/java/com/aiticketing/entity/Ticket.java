package com.aiticketing.entity;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.aiticketing.entity.enums.TicketPriority;
import com.aiticketing.entity.enums.TicketStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "tickets")
public class Ticket {
	
	@Id
	@Column(name = "ticket_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long ticketId;
	
	@Column(name = "ticket_title", nullable = false, length = 200)
    private String title;

	@Column(name = "ticket_description", nullable = false, columnDefinition = "text")
	private String description;

	@Column(name = "ticket_status", nullable = false, columnDefinition = "ticket_status")
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private TicketStatus status;
	
	//FK: tickets.ticket_created_by -> users.user_id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ticket_created_by", nullable = false)
    private User createdBy;
	
    //FK: tickets.ticket_assigned_to -> users.user_id (nullable)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_assigned_to")
    private User assignedTo;

    @Column(name = "ticket_ai_category", length = 80)
    private String aiCategory;

    @Column(name = "ticket_ai_priority", columnDefinition="ticket_priority")
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private TicketPriority aiPriority;

    @Column(name = "ticket_created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "ticket_updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "ticket_ai_confidence", precision = 4, scale = 3)
    private BigDecimal aiConfidence;

    @Column(name = "ticket_current_text_version", nullable = false)
    private Integer currentTextVersion;

    @Column(name = "ticket_duplicate_state", nullable = false, length = 20)
    private String duplicateState;

    @Column(name = "ticket_ai_failed", nullable = false)
    private Boolean aiFailed;

    @Column(name = "ticket_ai_last_error", columnDefinition = "text")
    private String aiLastError;
    
    //TRIAGE_REQUEST started/claimed by worker at for processing
    @Column(name = "ticket_current_triage_started_at")
    private OffsetDateTime currentTriageStartedAt;
    
    //Successful TRIAGE_REQUEST completed at
    @Column(name = "ticket_ai_triaged_at")
    private OffsetDateTime aiTriagedAt;

    @Column(name = "ticket_vague_count", nullable = false)
    private Integer vagueCount;

    @Column(name = "ticket_last_vague_at")
    private OffsetDateTime lastVagueAt;

    @Column(name = "ticket_first_assigned_at")
    private OffsetDateTime firstAssignedAt;

    @Column(name = "ticket_vague_reason", columnDefinition = "text")
    private String vagueReason;

    @Column(name = "ticket_clarification_prompt", columnDefinition = "text")
    private String clarificationPrompt;
    
	public Long getTicketId() {
		return ticketId;
	}

	public void setTicketId(Long ticketId) {
		this.ticketId = ticketId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public TicketStatus getStatus() {
		return status;
	}

	public void setStatus(TicketStatus status) {
		this.status = status;
	}

	public User getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(User createdBy) {
		this.createdBy = createdBy;
	}

	public User getAssignedTo() {
		return assignedTo;
	}

	public void setAssignedTo(User assignedTo) {
		this.assignedTo = assignedTo;
	}

	public String getAiCategory() {
		return aiCategory;
	}

	public void setAiCategory(String aiCategory) {
		this.aiCategory = aiCategory;
	}

	public TicketPriority getAiPriority() {
		return aiPriority;
	}

	public void setAiPriority(TicketPriority aiPriority) {
		this.aiPriority = aiPriority;
	}

	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(OffsetDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public OffsetDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(OffsetDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

	public BigDecimal getAiConfidence() {
		return aiConfidence;
	}

	public void setAiConfidence(BigDecimal aiConfidence) {
		this.aiConfidence = aiConfidence;
	}

	public Integer getCurrentTextVersion() {
		return currentTextVersion;
	}

	public void setCurrentTextVersion(Integer currentTextVersion) {
		this.currentTextVersion = currentTextVersion;
	}

	public String getDuplicateState() {
		return duplicateState;
	}

	public void setDuplicateState(String duplicateState) {
		this.duplicateState = duplicateState;
	}
	
	public Boolean getAiFailed() {
		return aiFailed;
	}

	public void setAiFailed(Boolean aiFailed) {
		this.aiFailed = aiFailed;
	}

	public String getAiLastError() {
		return aiLastError;
	}

	public void setAiLastError(String aiLastError) {
		this.aiLastError = aiLastError;
	}

	public OffsetDateTime getCurrentTriageStartedAt() {
		return currentTriageStartedAt;
	}

	public void setCurrentTriageStartedAt(OffsetDateTime currentTriageStartedAt) {
		this.currentTriageStartedAt = currentTriageStartedAt;
	}

	public OffsetDateTime getAiTriagedAt() {
		return aiTriagedAt;
	}

	public void setAiTriagedAt(OffsetDateTime aiTriagedAt) {
		this.aiTriagedAt = aiTriagedAt;
	}

	public Integer getVagueCount() {
		return vagueCount;
	}

	public void setVagueCount(Integer vagueCount) {
		this.vagueCount = vagueCount;
	}

	public OffsetDateTime getLastVagueAt() {
		return lastVagueAt;
	}

	public void setLastVagueAt(OffsetDateTime lastVagueAt) {
		this.lastVagueAt = lastVagueAt;
	}

	public OffsetDateTime getFirstAssignedAt() {
		return firstAssignedAt;
	}

	public void setFirstAssignedAt(OffsetDateTime firstAssignedAt) {
		this.firstAssignedAt = firstAssignedAt;
	}

	public String getVagueReason() {
		return vagueReason;
	}

	public void setVagueReason(String vagueReason) {
		this.vagueReason = vagueReason;
	}

	public String getClarificationPrompt() {
		return clarificationPrompt;
	}

	public void setClarificationPrompt(String clarificationPrompt) {
		this.clarificationPrompt = clarificationPrompt;
	}

	@Override
    public String toString() {
        return "Ticket{" +
                "ticketId=" + ticketId +
                ", title='" + title + '\'' +
                ", status='" + status + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
    
}
