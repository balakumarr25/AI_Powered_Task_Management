package com.taskportal.dto.ai;

import com.taskportal.entity.TaskPriority;

public class AiGenerateResponse {
    private String description;
    private TaskPriority suggestedPriority;
    private String estimatedEffort;
    private boolean fallbackUsed;
    private String message;

    public static AiGenerateResponse of(
            String description,
            TaskPriority priority,
            String effort,
            boolean fallback,
            String message
    ) {
        AiGenerateResponse r = new AiGenerateResponse();
        r.description = description;
        r.suggestedPriority = priority;
        r.estimatedEffort = effort;
        r.fallbackUsed = fallback;
        r.message = message;
        return r;
    }

    public String getDescription() { return description; }
    public TaskPriority getSuggestedPriority() { return suggestedPriority; }
    public String getEstimatedEffort() { return estimatedEffort; }
    public boolean isFallbackUsed() { return fallbackUsed; }
    public String getMessage() { return message; }
}
