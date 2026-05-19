package com.taskportal.repository;

import com.taskportal.entity.Task;
import com.taskportal.entity.TaskStatus;
import com.taskportal.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByOwnerOrderByCreatedAtDesc(User owner);
    Optional<Task> findByIdAndOwner(Long id, User owner);
    long countByOwnerAndStatus(User owner, TaskStatus status);
    List<Task> findByOwnerAndTitleIgnoreCase(User owner, String title);
}
