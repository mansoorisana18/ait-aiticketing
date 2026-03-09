package com.aiticketing.ai.dto;

public class TriageOutcome {
    public final boolean success;
    public final TriageResult result;
    public final String error;

    private TriageOutcome(boolean success, TriageResult result, String error) {
        this.success = success;
        this.result = result;
        this.error = error;
    }

    public static TriageOutcome ok(TriageResult r) {
        return new TriageOutcome(true, r, null);
    }

    public static TriageOutcome fail(String err) {
        return new TriageOutcome(false, null, err);
    }
}