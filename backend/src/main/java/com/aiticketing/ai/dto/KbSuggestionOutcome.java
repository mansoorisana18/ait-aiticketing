package com.aiticketing.ai.dto;

public class KbSuggestionOutcome {

    public boolean success;
    public KbSuggestionResult result;
    public String error;

    public static KbSuggestionOutcome ok(KbSuggestionResult result) {
        KbSuggestionOutcome outcome = new KbSuggestionOutcome();
        outcome.success = true;
        outcome.result = result;
        return outcome;
    }

    public static KbSuggestionOutcome fail(String error) {
        KbSuggestionOutcome outcome = new KbSuggestionOutcome();
        outcome.success = false;
        outcome.error = error;
        return outcome;
    }
}