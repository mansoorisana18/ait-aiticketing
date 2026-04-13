package com.aiticketing.ai.dto;

import java.math.BigDecimal;

public class KbSuggestionCandidate {

    public Long kbId;
    public String title;
    public String bodyPreview;
    public BigDecimal similarity;

    @Override
    public String toString() {
        return "KbSuggestionCandidate [kbId=" + kbId + ", title='" + title + ", similarity=" + similarity +"]";
    }
}