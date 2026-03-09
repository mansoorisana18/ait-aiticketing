package com.aiticketing.ai.dto;

public class RoutingResult {

    public enum Outcome {
        ASSIGNED,
        NO_ELIGIBLE_AGENT
    }

    public Outcome outcome;
    public String department;
    public Long selectedAgentId;
    public Long selectedWorkload;
    public Integer eligibleAgentCount;
}