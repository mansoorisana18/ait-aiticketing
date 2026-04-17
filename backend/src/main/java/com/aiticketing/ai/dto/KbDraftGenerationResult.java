package com.aiticketing.ai.dto;

public class KbDraftGenerationResult {

    public String title;
    public String body;
    public String reason;

    //For preserving exact raw AI JSON for auditing/persistence
    public String rawOutputJson;

    @Override
    public String toString() {
        return "KbDraftGenerationResult [ title=" + title + ", reason=" + reason + "]";
    }
}