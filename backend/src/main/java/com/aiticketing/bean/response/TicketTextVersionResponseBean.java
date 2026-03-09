package com.aiticketing.bean.response;

import java.time.OffsetDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

public class TicketTextVersionResponseBean {

    @Schema(example = "11")
    public Long versionId;

    @Schema(example = "5")
    public Long ticketId;

    @Schema(example = "2")
    public Integer versionNo;

    @Schema(example = "Unable to log in to payroll portal")
    public String title;

    @Schema(example = "I am using Chrome on Windows 11. When I try to log in to the payroll portal, I get the error message 'Account locked after multiple attempts'.")
    public String description;

    @Schema(example = "1")
    public Long createdByUserId;

    public OffsetDateTime createdAt;
}