package com.aiticketing.bean.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreateKbArticleRequestBean {

    @NotBlank(message = "title is required")
    @Size(max = 200, message = "title must be at most 200 characters")
    public String title;

    @NotBlank(message = "body is required")
    public String body;

    @NotNull(message = "publishNow is required")
    public Boolean publishNow;

    @Override
    public String toString() {
        return "CreateKbArticleRequestBean [ title=" + title + ", publishNow=" + publishNow + "]";
    }
}