package com.aiticketing.bean.response;

import java.time.OffsetDateTime;

import com.aiticketing.entity.TicketStatus;

import io.swagger.v3.oas.annotations.media.Schema;

public class UserTicketResponseBean {

	@Schema(example = "101")
    public Long ticketId;

    @Schema(example = "VPN not working")
    public String title;

    @Schema(example = "Cannot connect to VPN from home.")
    public String description;

    @Schema(example = "OPEN")
    public String userTicketStatus;

    public OffsetDateTime createdAt;
    public OffsetDateTime updatedAt;

    public Long createdByUserId;
    public String createdByName;
    public String createdByEmail;
    
    public String assignedToName;
    
	@Override
	public String toString() {
		return "TicketResponseBean [ticketId=" + ticketId + ", title=" + title + ", description=" + description
				+ ", userTicketStatus=" + userTicketStatus + ", createdAt=" + createdAt + ", updatedAt=" + updatedAt + ", createdByUserId="
				+ createdByUserId + ", createdByName=" + createdByName + ", createdByEmail=" + createdByEmail
				+ ", assignedToName=" + assignedToName + "]";
	}
}
