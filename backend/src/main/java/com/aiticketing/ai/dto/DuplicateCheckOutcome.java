package com.aiticketing.ai.dto;

public class DuplicateCheckOutcome {
    
	public boolean success;
    public String error;
    public DuplicateCheckResult result;

    public static DuplicateCheckOutcome ok(DuplicateCheckResult result) {
        DuplicateCheckOutcome outcome = new DuplicateCheckOutcome();
        outcome.success = true;
        outcome.result = result;
        return outcome;
    }

    public static DuplicateCheckOutcome fail(String error) {
        DuplicateCheckOutcome outcome = new DuplicateCheckOutcome();
        outcome.success = false;
        outcome.error = error;
        return outcome;
    }
}