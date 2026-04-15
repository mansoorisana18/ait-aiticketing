package com.aiticketing.ai.dto;

import java.math.BigDecimal;

public class KbDraftGenerationResult {

    public String title;
    public String body;
    public BigDecimal confidence;
    public String reason;

    //For preserving exact raw AI JSON for auditing/persistence
    public String rawOutputJson;

    @Override
    public String toString() {
        return "KbDraftGenerationResult{" +
                "title='" + title + '\'' +
                ", confidence=" + confidence +
                ", reason='" + reason + '\'' +
                '}';
    }
}