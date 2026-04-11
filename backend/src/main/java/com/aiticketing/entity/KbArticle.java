package com.aiticketing.entity;

import java.time.OffsetDateTime;

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
@Table(name = "kb_articles")
public class KbArticle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "kba_kb_id")
    private Long kbId;

    @Column(name = "kba_title", nullable = false, length = 200)
    private String title;

    @Column(name = "kba_body", nullable = false, columnDefinition = "text")
    private String body;

    @Column(name = "kba_status", nullable = false, length = 20)
    private String status;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "kba_created_by", nullable = false)
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kba_source_ticket_id")
    private Ticket sourceTicket;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kba_last_modified_by")
    private User lastModifiedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kba_approved_by")
    private User approvedBy;

    @Column(name = "kba_is_ai_generated", nullable = false)
    private Boolean aiGenerated = false;

    @Column(name = "kba_created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "kba_updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "kba_agent_submitted_at")
    private OffsetDateTime agentSubmittedAt;

    @Column(name = "kba_approved_at")
    private OffsetDateTime approvedAt;

    public Long getKbId() {
        return kbId;
    }

    public void setKbId(Long kbId) {
        this.kbId = kbId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public Ticket getSourceTicket() {
        return sourceTicket;
    }

    public void setSourceTicket(Ticket sourceTicket) {
        this.sourceTicket = sourceTicket;
    }

    public User getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(User lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    public User getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(User approvedBy) {
        this.approvedBy = approvedBy;
    }

    public Boolean getAiGenerated() {
        return aiGenerated;
    }

    public void setAiGenerated(Boolean aiGenerated) {
        this.aiGenerated = aiGenerated;
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

    public OffsetDateTime getAgentSubmittedAt() {
        return agentSubmittedAt;
    }

    public void setAgentSubmittedAt(OffsetDateTime agentSubmittedAt) {
        this.agentSubmittedAt = agentSubmittedAt;
    }

    public OffsetDateTime getApprovedAt() {
        return approvedAt;
    }

    public void setApprovedAt(OffsetDateTime approvedAt) {
        this.approvedAt = approvedAt;
    }

    @Override
    public String toString() {
        return "KbArticle{" +
                "kbId=" + kbId +
                ", title='" + title + '\'' +
                ", status='" + status + '\'' +
                ", aiGenerated=" + aiGenerated +
                ", createdAt=" + createdAt +
                '}';
    }
}