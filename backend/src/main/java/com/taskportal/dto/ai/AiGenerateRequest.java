package com.taskportal.dto.ai;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AiGenerateRequest {

    @NotBlank @Size(max = 200)
    private String title;

    /** Optional: "openai" | "gemini" | "huggingface" | "auto" */
    private String provider;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
}
