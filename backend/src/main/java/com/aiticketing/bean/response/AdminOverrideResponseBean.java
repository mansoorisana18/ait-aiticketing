package com.aiticketing.bean.response;

import java.time.OffsetDateTime;

public class AdminOverrideResponseBean {
    public Long overrideId;
    public Long ticketId;

    public String overrideType;
    public String oldValue;
    public String newValue;
    public String reason;

    public OffsetDateTime createdAt;

    public Long overriddenByUserId;
    public String overriddenByName;
    public String overriddenByEmail;
}