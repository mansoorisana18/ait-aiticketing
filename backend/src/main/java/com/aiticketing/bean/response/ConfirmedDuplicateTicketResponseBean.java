package com.aiticketing.bean.response;

import java.time.OffsetDateTime;

public class ConfirmedDuplicateTicketResponseBean {
    public Long ticketId;
    public String title;

    public Long createdByUserId;
    public String createdByName;
    public String createdByEmail;

    public String internalStatus;
    public String userTicketStatus;

    public OffsetDateTime createdAt;
    public Boolean propagateResolution;
}