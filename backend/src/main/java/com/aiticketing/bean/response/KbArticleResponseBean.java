package com.aiticketing.bean.response;

import java.time.OffsetDateTime;

public class KbArticleResponseBean {

    public Long kbId;
    public String title;
    public String body;
    public String status;

    public Boolean isAiGenerated;

    public Long sourceTicketId;

    public Long createdByUserId;
    public String createdByName;
    public String createdByEmail;

    public Long lastModifiedByUserId;
    public String lastModifiedByName;
    public String lastModifiedByEmail;

    public Long approvedByUserId;
    public String approvedByName;
    public String approvedByEmail;

    public OffsetDateTime createdAt;
    public OffsetDateTime updatedAt;
    public OffsetDateTime agentSubmittedAt;
    public OffsetDateTime approvedAt;
}