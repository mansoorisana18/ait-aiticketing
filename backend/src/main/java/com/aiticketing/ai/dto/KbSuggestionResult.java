package com.aiticketing.ai.dto;

import java.math.BigDecimal;

public class KbSuggestionResult {

    public boolean suggestionFound;
    public Long kbId;
    public String kbTitle;
    public String kbPreview;
    public BigDecimal confidence;
    public BigDecimal similarity;
    public BigDecimal threshold;
    public String reason;

    @Override
    public String toString() {
        return "KbSuggestionResult [ suggestionFound=" + suggestionFound + ", kbId=" + kbId + ", confidence="+ confidence + ", similarity=" + similarity + ", threshold=" + threshold + ", reason=" + reason +"]";
    }
}