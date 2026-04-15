package com.aiticketing.bean.request;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public class GenerateKbDraftRequestBean {

    @NotNull(message = "selectedCommentIds is required")
    @NotEmpty(message = "At least one public comment must be selected")
    public List<Long> selectedCommentIds;

    @Override
    public String toString() {
        return "GenerateKbDraftRequestBean [selectedCommentIds=" + selectedCommentIds + "]";
    }
}