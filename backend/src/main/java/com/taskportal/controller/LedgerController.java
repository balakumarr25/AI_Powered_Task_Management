package com.taskportal.controller;

import com.taskportal.dto.ledger.LedgerEntryResponse;
import com.taskportal.entity.Task;
import com.taskportal.exception.ResourceNotFoundException;
import com.taskportal.repository.TaskRepository;
import com.taskportal.service.BlockchainLedgerService;
import com.taskportal.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks/{taskId}/ledger")
@Tag(name = "Blockchain Ledger")
@SecurityRequirement(name = "bearerAuth")
public class LedgerController {

    private final BlockchainLedgerService ledgerService;
    private final TaskRepository taskRepository;
    private final SecurityUtils securityUtils;

    public LedgerController(
            BlockchainLedgerService ledgerService,
            TaskRepository taskRepository,
            SecurityUtils securityUtils
    ) {
        this.ledgerService = ledgerService;
        this.taskRepository = taskRepository;
        this.securityUtils = securityUtils;
    }

    @GetMapping
    @Operation(summary = "Get immutable task history ledger (mock blockchain)")
    public List<LedgerEntryResponse> getLedger(@PathVariable Long taskId) {
        Task task = taskRepository.findByIdAndOwner(taskId, securityUtils.getCurrentUser())
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
        return ledgerService.getLedger(task);
    }
}
