package com.aiticketing.bean.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UpdateVagueTicketRequestBean {

    @Schema(example = "Order status tracking not visible", description = "Optional updated title. If omitted or blank, existing title is kept.")
    @Size(max = 200, message = "title cannot exceed 200 characters")
    public String title;

    @Schema(
        example = "I placed my order on 7th March 2026.",
        description = "The user's answer to the clarification prompt."
    )
    @NotBlank(message = "clarificationAnswer cannot be empty")
    @Size(max = 5000, message = "clarificationAnswer cannot exceed 5000 characters")
    public String clarificationAnswer;
}