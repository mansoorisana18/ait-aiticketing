package com.aiticketing.bean.response;

import java.time.OffsetDateTime;

import com.aiticketing.entity.TicketStatus;

import io.swagger.v3.oas.annotations.media.Schema;

public class TicketResponseBean {

	@Schema(example = "101")
    public Long ticketId;

    @Schema(example = "VPN not working")
    public String title;

    @Schema(example = "Cannot connect to VPN from home.")
    public String description;

    @Schema(example = "OPEN")
    public TicketStatus status;

    public OffsetDateTime createdAt;
    public OffsetDateTime updatedAt;

    public Long createdByUserId;
    public String createdByName;
    public String createdByEmail;
    
    public Long assignedToUserId;
    public String assignedToName;
    public String assignedToEmail;
	@Override
	public String toString() {
		return "TicketResponseBean [ticketId=" + ticketId + ", title=" + title + ", description=" + description
				+ ", status=" + status + ", createdAt=" + createdAt + ", updatedAt=" + updatedAt + ", createdByUserId="
				+ createdByUserId + ", createdByName=" + createdByName + ", createdByEmail=" + createdByEmail
				+ ", assignedToUserId=" + assignedToUserId + ", assignedToName=" + assignedToName + ", assignedToEmail="
				+ assignedToEmail + "]";
	}
}
