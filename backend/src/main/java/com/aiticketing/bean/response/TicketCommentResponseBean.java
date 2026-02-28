package com.aiticketing.bean.response;

import java.time.OffsetDateTime;

import com.aiticketing.entity.CommentVisibility;

import io.swagger.v3.oas.annotations.media.Schema;

public class TicketCommentResponseBean {
    public Long commentId;
    public Long ticketId;
    public String body;
    
    @Schema(example= "PUBLIC")
    public CommentVisibility visibility;
    
    public OffsetDateTime createdAt;

    public Long authorUserId;
    public String authorName;
    public String authorEmail;
}