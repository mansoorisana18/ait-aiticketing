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
@Table(name = "admin_overrides")
public class AdminOverride {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ao_override_id")
    private Long overrideId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ao_ticket_id", nullable = false)
    private Ticket ticket;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ao_overridden_by", nullable = false)
    private User overriddenBy;

    @Column(name = "ao_override_type", nullable = false, length = 40)
    private String overrideType; // CATEGORY / PRIORITY / STATUS / DUPLICATE_LINK / KB_DRAFT

    @Column(name = "ao_old_value", columnDefinition = "text")
    private String oldValue;

    @Column(name = "ao_new_value", columnDefinition = "text")
    private String newValue;

    @Column(name = "ao_reason", columnDefinition = "text")
    private String reason;

    @Column(name = "ao_created_at")
    private OffsetDateTime createdAt;

	public Long getOverrideId() {
		return overrideId;
	}

	public void setOverrideId(Long overrideId) {
		this.overrideId = overrideId;
	}

	public Ticket getTicket() {
		return ticket;
	}

	public void setTicket(Ticket ticket) {
		this.ticket = ticket;
	}

	public User getOverriddenBy() {
		return overriddenBy;
	}

	public void setOverriddenBy(User overriddenBy) {
		this.overriddenBy = overriddenBy;
	}

	public String getOverrideType() {
		return overrideType;
	}

	public void setOverrideType(String overrideType) {
		this.overrideType = overrideType;
	}

	public String getOldValue() {
		return oldValue;
	}

	public void setOldValue(String oldValue) {
		this.oldValue = oldValue;
	}

	public String getNewValue() {
		return newValue;
	}

	public void setNewValue(String newValue) {
		this.newValue = newValue;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(OffsetDateTime createdAt) {
		this.createdAt = createdAt;
	}
    
}
