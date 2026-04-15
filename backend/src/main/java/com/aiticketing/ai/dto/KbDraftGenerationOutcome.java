package com.aiticketing.ai.dto;

public class KbDraftGenerationOutcome {

    public boolean success;
    public KbDraftGenerationResult result;
    public String error;

    public static KbDraftGenerationOutcome ok(KbDraftGenerationResult result) {
        KbDraftGenerationOutcome outcome = new KbDraftGenerationOutcome();
        outcome.success = true;
        outcome.result = result;
        return outcome;
    }

    public static KbDraftGenerationOutcome fail(String error) {
        KbDraftGenerationOutcome outcome = new KbDraftGenerationOutcome();
        outcome.success = false;
        outcome.error = error;
        return outcome;
    }
}