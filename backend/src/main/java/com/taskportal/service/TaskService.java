package com.taskportal.service;

import com.taskportal.dto.task.TaskRequest;
import com.taskportal.dto.task.TaskResponse;
import com.taskportal.entity.Task;
import com.taskportal.entity.TaskPriority;
import com.taskportal.entity.TaskStatus;
import com.taskportal.entity.User;
import com.taskportal.exception.ResourceNotFoundException;
import com.taskportal.repository.TaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final BlockchainLedgerService ledgerService;

    public TaskService(TaskRepository taskRepository, BlockchainLedgerService ledgerService) {
        this.taskRepository = taskRepository;
        this.ledgerService = ledgerService;
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getAllTasks(User owner) {
        return taskRepository.findByOwnerOrderByCreatedAtDesc(owner).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TaskResponse getTask(Long id, User owner) {
        return toResponse(findOwnedTask(id, owner));
    }

    @Transactional
    public TaskResponse createTask(TaskRequest request, User owner) {
        Task task = new Task();
        task.setTitle(request.getTitle().trim());
        task.setDescription(request.getDescription());
        task.setPriority(request.getPriority() != null ? request.getPriority() : TaskPriority.MEDIUM);
        task.setDueDate(request.getDueDate());
        task.setStatus(request.getStatus() != null ? request.getStatus() : TaskStatus.TODO);
        task.setOwner(owner);

        task = taskRepository.save(task);
        ledgerService.recordEvent(task, "TASK_CREATED", serializeTask(task));
        return toResponse(task);
    }

    @Transactional
    public TaskResponse updateTask(Long id, TaskRequest request, User owner) {
        Task task = findOwnedTask(id, owner);
        task.setTitle(request.getTitle().trim());
        task.setDescription(request.getDescription());
        if (request.getPriority() != null) {
            task.setPriority(request.getPriority());
        }
        task.setDueDate(request.getDueDate());
        if (request.getStatus() != null) {
            task.setStatus(request.getStatus());
        }

        task = taskRepository.save(task);
        ledgerService.recordEvent(task, "TASK_UPDATED", serializeTask(task));
        return toResponse(task);
    }

    @Transactional
    public TaskResponse updateStatus(Long id, TaskStatus status, User owner) {
        Task task = findOwnedTask(id, owner);
        task.setStatus(status);
        task = taskRepository.save(task);
        ledgerService.recordEvent(task, "STATUS_CHANGED", status.name());
        return toResponse(task);
    }

    @Transactional
    public void deleteTask(Long id, User owner) {
        Task task = findOwnedTask(id, owner);
        ledgerService.recordEvent(task, "TASK_DELETED", task.getId().toString());
        taskRepository.delete(task);
    }

    private Task findOwnedTask(Long id, User owner) {
        return taskRepository.findByIdAndOwner(id, owner)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
    }

    private String serializeTask(Task task) {
        return String.format(
                "id=%d,title=%s,status=%s,priority=%s",
                task.getId(),
                task.getTitle(),
                task.getStatus(),
                task.getPriority()
        );
    }

    private TaskResponse toResponse(Task task) {
        return TaskResponse.from(task);
    }
}
