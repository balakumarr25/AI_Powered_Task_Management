package com.taskportal.dto.task;

import com.taskportal.entity.Task;
import com.taskportal.entity.TaskPriority;
import com.taskportal.entity.TaskStatus;
import java.time.Instant;
import java.time.LocalDate;

public class TaskResponse {
    private Long id;
    private String title;
    private String description;
    private TaskPriority priority;
    private LocalDate dueDate;
    private TaskStatus status;
    private Instant createdAt;
    private Instant updatedAt;

    public static TaskResponse from(Task task) {
        TaskResponse r = new TaskResponse();
        r.id = task.getId();
        r.title = task.getTitle();
        r.description = task.getDescription();
        r.priority = task.getPriority();
        r.dueDate = task.getDueDate();
        r.status = task.getStatus();
        r.createdAt = task.getCreatedAt();
        r.updatedAt = task.getUpdatedAt();
        return r;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public TaskPriority getPriority() { return priority; }
    public LocalDate getDueDate() { return dueDate; }
    public TaskStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
