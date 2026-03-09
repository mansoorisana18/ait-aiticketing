package com.aiticketing.bean.request;

import com.aiticketing.entity.enums.TicketStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public class UpdateTicketStatusRequestBean {

    @Schema(example = "IN_PROGRESS", description = "Allowed: IN_PROGRESS, RESOLVED, CLOSED")
    @NotNull(message = "Ticket Status is required")
    public TicketStatus status;

    @Override
    public String toString() {
        return "UpdateTicketStatusRequestBean [status=" + status + "]";
    }
}