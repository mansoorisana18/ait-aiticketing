package com.aiticketing.bean.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema
public class CreateTicketRequestBean {

	@Schema(example = "VPN not working")
    @NotBlank(message = "Ticket title cannot be empty")
    @Size(max = 200)
    public String title;

    @Schema(example = "Cannot connect to VPN from home Wi-Fi.")
    @NotBlank(message = "Ticket description cannot be empty")
    public String description;

	@Override
	public String toString() {
		return "CreateTicketRequestBean [title=" + title + ", description=" + description + "]";
	}	
}