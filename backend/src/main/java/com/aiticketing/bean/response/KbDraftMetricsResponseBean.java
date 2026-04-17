package com.aiticketing.bean.response;

public class KbDraftMetricsResponseBean {

    //stage throughput
    public long draftGenerationAttempts;
    public double draftGenerationSuccessRate;    

    //workflow progression
    public long submittedForReviewCount;
    public double draftApprovalRate;
    public double draftRejectionRate;
    public Double averageReviewTurnaroundHours;

    //impact
    public long publishedAiDraftCount;
}