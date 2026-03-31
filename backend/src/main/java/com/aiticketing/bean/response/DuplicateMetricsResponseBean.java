package com.aiticketing.bean.response;

public class DuplicateMetricsResponseBean {
    
	//operational working
	public long duplicateChecksAttempted;
    public double duplicateCheckSuccessRate;
    public double averageDuplicateCheckTimeSeconds;
    
    //efficiency/usefulness
    public double autoConfirmedRate;
    public double autoConfirmedAcceptanceRate;
    public long duplicateReviewQueueSize;
    public double averagePotentialReviewTimeMinutes;
    public double potentialConfirmationRate;
    public long duplicateWorkSavedCount;
    public long resolvedThroughPrimaryCount;
}