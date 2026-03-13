package com.aiticketing.bean.response;

public class TicketSummaryMetricsResponseBean {

	//Active workload (not in RESOLVED, CLOSED)
    public Long totalTickets;
    public Long newCount;
    public Long aiProcessingCount;
    public Long vagueCount;
    public Long readyCount;
    public Long inProgressCount;
    public Long assignedCount;
    public Long unassignedCount;
    public Long highPriorityCount;
    public Long urgentPriorityCount;
    //All completed tickets in the system
    public Long resolvedCount;
    public Long closedCount;
}