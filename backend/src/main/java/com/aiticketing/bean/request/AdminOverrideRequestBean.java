package com.aiticketing.bean.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AdminOverrideRequestBean {

    @Schema(example = "STATUS", description = "CATEGORY | PRIORITY | DUPLICATE_LINK | STATUS | KB_DRAFT | ASSIGNMENT")
    @NotBlank(message = "overrideType cannot be empty")
    public String overrideType;

    //Used for STATUS/CATEGORY/PRIORITY/DUPLICATE_LINK
    @Schema(example = "IN_PROGRESS", description = "New value to set")
    @Size(max = 4000)
    public String newValue;

    //Used for ASSIGNMENT (can be null to UNASSIGN)
    @Schema(example = "12", description = "Target agent userId or null means unassign")
    public Long newAssignedToUserId;
    
    //Used for DUPLICATE_LINK for Override to CONFIRMED 
    @Schema(example = "12", description = "Ticket Id whose duplicate is the current ticket")
    public Long referenceTicketId;
    
    @Schema(example = "Agent confirmed issue is being worked on.", description = "Optional reason for audit trail")
    @Size(max = 4000)
    public String reason;

    @Override
    public String toString() {
        return "AdminOverrideRequestBean [overrideType=" + overrideType + ", newValue=" + newValue + ", newAssignedToUserId=" + newAssignedToUserId + ", referenceTicketId=" + referenceTicketId + "]";
    }
}