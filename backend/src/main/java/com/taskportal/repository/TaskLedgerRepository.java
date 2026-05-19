package com.taskportal.repository;

import com.taskportal.entity.Task;
import com.taskportal.entity.TaskLedgerEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TaskLedgerRepository extends JpaRepository<TaskLedgerEntry, Long> {
    List<TaskLedgerEntry> findByTaskOrderByBlockIndexAsc(Task task);
    Optional<TaskLedgerEntry> findTopByTaskOrderByBlockIndexDesc(Task task);
}
