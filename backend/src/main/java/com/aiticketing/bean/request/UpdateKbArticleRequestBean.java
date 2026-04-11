package com.aiticketing.bean.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class UpdateKbArticleRequestBean {

    @NotBlank(message = "title is required")
    @Size(max = 200, message = "title must be at most 200 characters")
    public String title;

    @NotBlank(message = "body is required")
    public String body;

    //If true, article becomes PUBLISHED after update.
    //If false, keep current non-published state as DRAFT.
    @NotNull(message = "publishNow is required")
    public Boolean publishNow;

    @Override
    public String toString() {
        return "UpdateKbArticleRequestBean [title=" + title + ", publishNow=" + publishNow + "]";
    }
}