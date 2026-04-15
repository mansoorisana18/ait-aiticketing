package com.aiticketing.bean.response;

public class KbSuggestionMetricsResponseBean {

    //stage throughput
    public Long suggestionAttempts;

    //AI suggestion quality
    public Double averageSuggestionConfidence;
    public Double averageSuggestionSimilarity;
    public Double autoSuggestionAcceptanceRate;
    public Double autoSuggestionRejectionRate;

    //manual fallback usage
    public Long manualSuggestionCount;
    public Double manualSuggestionAcceptanceRate;
    public Double manualSuggestionRejectionRate;
}