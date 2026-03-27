package com.aiticketing.entity;

import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

@Entity
@Table(name = "ticket_embeddings")
@IdClass(TicketEmbeddingId.class)
public class TicketEmbedding {

    @Id
    @Column(name = "te_ticket_id")
    private Long ticketId;

    @Id
    @Column(name = "te_text_version")
    private Integer textVersion;

    @Column(name = "te_embedding", columnDefinition = "vector", nullable = false)
    private String embedding;

    @Column(name = "te_created_at", nullable = false)
    private OffsetDateTime createdAt;

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

	public String getEmbedding() {
		return embedding;
	}

	public void setEmbedding(String embedding) {
		this.embedding = embedding;
	}

	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(OffsetDateTime createdAt) {
		this.createdAt = createdAt;
	}

}