package com.taskportal.controller;

import com.taskportal.dto.task.TaskRequest;
import com.taskportal.dto.task.TaskResponse;
import com.taskportal.entity.TaskStatus;
import com.taskportal.exception.BadRequestException;
import com.taskportal.service.TaskService;
import com.taskportal.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
@Tag(name = "Tasks")
@SecurityRequirement(name = "bearerAuth")
public class TaskController {

    private final TaskService taskService;
    private final SecurityUtils securityUtils;

    public TaskController(TaskService taskService, SecurityUtils securityUtils) {
        this.taskService = taskService;
        this.securityUtils = securityUtils;
    }

    @GetMapping
    @Operation(summary = "List all tasks for current user")
    public List<TaskResponse> list() {
        return taskService.getAllTasks(securityUtils.getCurrentUser());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get task by ID")
    public TaskResponse get(@PathVariable Long id) {
        return taskService.getTask(id, securityUtils.getCurrentUser());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new task")
    public TaskResponse create(@Valid @RequestBody TaskRequest request) {
        return taskService.createTask(request, securityUtils.getCurrentUser());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a task")
    public TaskResponse update(@PathVariable Long id, @Valid @RequestBody TaskRequest request) {
        return taskService.updateTask(id, request, securityUtils.getCurrentUser());
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update task status only")
    public TaskResponse updateStatus(@PathVariable Long id, @RequestBody Map<String, TaskStatus> body) {
        TaskStatus status = body.get("status");
        if (status == null) {
            throw new BadRequestException("status is required");
        }
        return taskService.updateStatus(id, status, securityUtils.getCurrentUser());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a task")
    public void delete(@PathVariable Long id) {
        taskService.deleteTask(id, securityUtils.getCurrentUser());
    }
}
