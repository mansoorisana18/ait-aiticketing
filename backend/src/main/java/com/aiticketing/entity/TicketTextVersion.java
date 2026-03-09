package com.aiticketing.entity;

import java.time.OffsetDateTime;
import jakarta.persistence.*;

@Entity
@Table(name = "ticket_text_versions")
public class TicketTextVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ttv_version_id")
    private Long versionId;

    @Column(name = "ttv_ticket_id", nullable = false)
    private Long ticketId;

    @Column(name = "ttv_version_no", nullable = false)
    private Integer versionNo;

    @Column(name = "ttv_ticket_title", nullable = false, length = 200)
    private String ticketTitle;

    @Column(name = "ttv_ticket_description", nullable = false, columnDefinition = "text")
    private String ticketDescription;

    @Column(name = "ttv_ticket_created_by", nullable = false)
    private Long createdByUserId;

    @Column(name = "ttv_ticket_created_at", nullable = false)
    private OffsetDateTime createdAt;

	public Long getVersionId() {
		return versionId;
	}

	public void setVersionId(Long versionId) {
		this.versionId = versionId;
	}

	public Long getTicketId() {
		return ticketId;
	}

	public void setTicketId(Long ticketId) {
		this.ticketId = ticketId;
	}

	public Integer getVersionNo() {
		return versionNo;
	}

	public void setVersionNo(Integer versionNo) {
		this.versionNo = versionNo;
	}

	public String getTicketTitle() {
		return ticketTitle;
	}

	public void setTicketTitle(String ticketTitle) {
		this.ticketTitle = ticketTitle;
	}

	public String getTicketDescription() {
		return ticketDescription;
	}

	public void setTicketDescription(String ticketDescription) {
		this.ticketDescription = ticketDescription;
	}

	public Long getCreatedByUserId() {
		return createdByUserId;
	}

	public void setCreatedByUserId(Long createdByUserId) {
		this.createdByUserId = createdByUserId;
	}

	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(OffsetDateTime createdAt) {
		this.createdAt = createdAt;
	}
  
}