package com.aiticketing.bean.response;

import java.math.BigDecimal;
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

    //Internal ticket status
    @Schema(example = "IN_PROGRESS")
    public TicketStatus status;
    
    //Ticket Status visible to the end user
    public String userTicketStatus;
    
    @Schema(example = "TECHNICAL SUPPORT")
    public String aiCategory;

    @Schema(example = "HIGH")
    public String aiPriority;

    @Schema(example = "0.923")
    public BigDecimal aiConfidence;

    @Schema(example = "1")
    public Integer currentTextVersion;
    
    public String duplicateState;

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
				+ ", status=" + status + ", userTicketStatus=" + userTicketStatus + ", aiCategory=" + aiCategory
				+ ", aiPriority=" + aiPriority + ", aiConfidence=" + aiConfidence + ", currentTextVersion="
				+ currentTextVersion + ", duplicateState=" + duplicateState + ", createdAt=" + createdAt
				+ ", updatedAt=" + updatedAt + ", createdByUserId=" + createdByUserId + ", createdByName="
				+ createdByName + ", createdByEmail=" + createdByEmail + ", assignedToUserId=" + assignedToUserId
				+ ", assignedToName=" + assignedToName + ", assignedToEmail=" + assignedToEmail + "]";
	}
    
}
