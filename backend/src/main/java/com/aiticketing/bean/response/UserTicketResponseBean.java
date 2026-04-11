package com.aiticketing.bean.response;

import java.time.OffsetDateTime;

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
    
    public String vagueReason;
    public String clarificationPrompt;
    
    //KB suggestion preview fields
    public Long suggestedKbId;
    public String suggestedKbTitle;
    public String suggestedKbPreview;
    public String kbSuggestionStatus;
    
	@Override
	public String toString() {
		return "UserTicketResponseBean [ticketId=" + ticketId + ", title=" + title + ", description=" + description
				+ ", userTicketStatus=" + userTicketStatus + ", createdAt=" + createdAt + ", updatedAt=" + updatedAt
				+ ", createdByUserId=" + createdByUserId + ", createdByName=" + createdByName + ", createdByEmail="
				+ createdByEmail + ", assignedToName=" + assignedToName + ", vagueReason=" + vagueReason
				+ ", clarificationPrompt=" + clarificationPrompt + ", suggestedKbId=" + suggestedKbId
				+ ", suggestedKbTitle=" + suggestedKbTitle + ", kbSuggestionStatus=" + kbSuggestionStatus + "]";
	}
}
