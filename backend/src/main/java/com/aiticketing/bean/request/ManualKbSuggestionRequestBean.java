package com.aiticketing.bean.request;

import jakarta.validation.constraints.NotNull;

public class ManualKbSuggestionRequestBean {

    @NotNull(message = "kbId is required")
    public Long kbId;

    @Override
    public String toString() {
        return "ManualKbSuggestionRequestBean{" +
                "kbId=" + kbId +
                '}';
    }
}