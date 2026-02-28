package com.aiticketing.bean.request;

import com.aiticketing.entity.CommentVisibility;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class TicketCommentRequestBean {

    @Schema(example = "I tried resetting the VPN client and it still fails.")
    @NotBlank(message = "Comment body cannot be empty")
    @Size(max = 5000, message = "Comment too long")
    public String body;

    //USER cannot create INTERNAL, only ADMIN/AGENT can
    @Schema(example = "PUBLIC", allowableValues = {"PUBLIC", "INTERNAL"})
    public CommentVisibility visibility;

    @Override
    public String toString() {
        return "TicketCommentRequestBean [body=" + body + ", visibility=" + visibility + "]";
    }
}