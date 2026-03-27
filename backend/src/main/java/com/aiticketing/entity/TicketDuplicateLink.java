package com.aiticketing.entity;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "ticket_duplicate_links")
public class TicketDuplicateLink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tdl_link_id")
    private Long linkId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tdl_primary_ticket_id", nullable = false)
    private Ticket primaryTicket;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tdl_duplicate_ticket_id", nullable = false)
    private Ticket duplicateTicket;

    @Column(name = "tdl_similarity", precision = 6, scale = 5)
    private BigDecimal similarity;

    @Column(name = "tdl_created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "tdl_duplicate_type", nullable = false, length = 20)
    private String duplicateType;

    @Column(name = "tdl_link_status", nullable = false, length = 20)
    private String linkStatus;

    @Column(name = "tdl_propagate_resolution", nullable = false)
    private Boolean propagateResolution;

	public Long getLinkId() {
		return linkId;
	}

	public void setLinkId(Long linkId) {
		this.linkId = linkId;
	}

	public Ticket getPrimaryTicket() {
		return primaryTicket;
	}

	public void setPrimaryTicket(Ticket primaryTicket) {
		this.primaryTicket = primaryTicket;
	}

	public Ticket getDuplicateTicket() {
		return duplicateTicket;
	}

	public void setDuplicateTicket(Ticket duplicateTicket) {
		this.duplicateTicket = duplicateTicket;
	}

	public BigDecimal getSimilarity() {
		return similarity;
	}

	public void setSimilarity(BigDecimal similarity) {
		this.similarity = similarity;
	}

	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(OffsetDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public String getDuplicateType() {
		return duplicateType;
	}

	public void setDuplicateType(String duplicateType) {
		this.duplicateType = duplicateType;
	}

	public String getLinkStatus() {
		return linkStatus;
	}

	public void setLinkStatus(String linkStatus) {
		this.linkStatus = linkStatus;
	}

	public Boolean getPropagateResolution() {
		return propagateResolution;
	}

	public void setPropagateResolution(Boolean propagateResolution) {
		this.propagateResolution = propagateResolution;
	}

}