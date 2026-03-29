package com.aiticketing.bean.response;

public class PrimaryLinkedTicketResponseBean {
    public Long primaryTicketId;
    public String primaryTicketTitle;

    public String primaryInternalStatus;
    public String primaryUserTicketStatus;

    public Long assignedAgentUserId;
    public String assignedAgentName;
    public String assignedAgentEmail;

    public String duplicateType;
    public String linkStatus;
    public Boolean propagateResolution;
}