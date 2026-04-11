package com.aiticketing.bean.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class KbSuggestionResponseRequestBean {

    @NotBlank(message = "action is required")
    @Pattern(
        regexp = "ACCEPTED|REJECTED",
        message = "action must be either ACCEPTED or REJECTED"
    )
    public String action;

    @Override
    public String toString() {
        return "KbSuggestionResponseRequestBean{" +
                "action='" + action + '\'' +
                '}';
    }
}