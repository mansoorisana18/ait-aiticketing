package com.aiticketing.bean.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class KbReviewDecisionRequestBean {

    @NotBlank(message = "action is required")
    @Pattern(
        regexp = "APPROVE|REJECT",
        message = "action must be either APPROVE or REJECT"
    )
    public String action;

    @Override
    public String toString() {
        return "KbReviewDecisionRequestBean [ action=" + action + "]";
    }
}