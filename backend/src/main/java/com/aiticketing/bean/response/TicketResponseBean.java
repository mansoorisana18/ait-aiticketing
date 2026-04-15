package com.aiticketing.bean.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import com.aiticketing.entity.enums.TicketPriority;
import com.aiticketing.entity.enums.TicketStatus;

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
    public TicketPriority aiPriority;

    @Schema(example = "0.923")
    public BigDecimal aiConfidence;
    
    //For triage (category, priority, vague)
    public Boolean aiFailed;
    public String aiLastError;
    public OffsetDateTime aiTriagedAt;
    public Integer vagueCount;
    public OffsetDateTime lastVagueAt;
    public OffsetDateTime firstAssignedAt;
    public String vagueReason;
    public String clarificationPrompt;

    @Schema(example = "1")
    public Integer currentTextVersion;
    
    public String duplicateState;
    public String duplicateReason;
    public Double duplicateConfidence;
    public Double duplicateSimilarity;
    public Long primaryTicketId;
    public String primaryTicketTitle;
    public String duplicateLinkType;
    public String duplicateLinkStatus;
    public Boolean propagateResolution;
    
    //KB suggestion preview/internal details
    public Long suggestedKbId;
    public String suggestedKbTitle;
    public String suggestedKbPreview;
    public String kbSuggestionStatus;
    public String kbSuggestionSource;
    public Double suggestedKbSimilarity;
    
    //KB draft summary
    public Long draftKbId;
    public String draftKbTitle;
    public String draftKbStatus;   //DRAFT | IN_REVIEW | PUBLISHED | REJECTED
    public Boolean kbDraftExists;
    public Boolean kbDraftAiGenerated;
    public OffsetDateTime draftKbUpdatedAt;

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
				+ ", aiPriority=" + aiPriority + ", aiConfidence=" + aiConfidence + ", aiFailed=" + aiFailed
				+ ", aiLastError=" + aiLastError + ", aiTriagedAt=" + aiTriagedAt + ", vagueCount=" + vagueCount
				+ ", lastVagueAt=" + lastVagueAt + ", firstAssignedAt=" + firstAssignedAt + ", vagueReason="
				+ vagueReason + ", clarificationPrompt=" + clarificationPrompt + ", currentTextVersion="
				+ currentTextVersion + ", duplicateState=" + duplicateState + ", suggestedKbId=" 
				+ suggestedKbId + ", suggestedKbTitle=" + suggestedKbTitle + ", kbSuggestionStatus=" 
				+ kbSuggestionStatus + ", kbSuggestionSource=" + kbSuggestionSource + ", createdAt=" + createdAt
				+ ", updatedAt=" + updatedAt + ", createdByUserId=" + createdByUserId + ", createdByName="
				+ createdByName + ", createdByEmail=" + createdByEmail + ", assignedToUserId=" + assignedToUserId
				+ ", assignedToName=" + assignedToName + ", assignedToEmail=" + assignedToEmail + "]";
	}
    
}
