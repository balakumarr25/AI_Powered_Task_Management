package com.taskportal.service;

import com.taskportal.entity.Task;
import com.taskportal.entity.User;
import com.taskportal.repository.TaskLedgerRepository;
import com.taskportal.repository.TaskRepository;
import com.taskportal.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@SpringBootTest
@Transactional
class BlockchainLedgerServiceTest {

    @Autowired
    private BlockchainLedgerService ledgerService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskLedgerRepository ledgerRepository;

    @Test
    void recordsChainedHashes() {
        User user = new User();
        user.setEmail("ledger@test.com");
        user.setFullName("Ledger Test");
        user.setPasswordHash("hash");
        user = userRepository.save(user);

        Task task = new Task();
        task.setTitle("Test");
        task.setOwner(user);
        task = taskRepository.save(task);

        ledgerService.recordEvent(task, "TASK_CREATED", "payload1");
        ledgerService.recordEvent(task, "STATUS_CHANGED", "TODO");

        var entries = ledgerRepository.findByTaskOrderByBlockIndexAsc(task);
        assertEquals(2, entries.size());
        assertEquals(0L, entries.get(0).getBlockIndex());
        assertEquals(1L, entries.get(1).getBlockIndex());
        assertNotEquals(entries.get(0).getPayloadHash(), entries.get(1).getPayloadHash());
        assertEquals(entries.get(0).getPayloadHash(), entries.get(1).getPreviousHash());
    }
}
