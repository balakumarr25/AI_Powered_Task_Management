package com.taskportal.service;

import com.taskportal.dto.ledger.LedgerEntryResponse;
import com.taskportal.entity.Task;
import com.taskportal.entity.TaskLedgerEntry;
import com.taskportal.repository.TaskLedgerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;

/**
 * Lightweight mock blockchain: chained SHA-256 hashes per task for audit trail.
 */
@Service
public class BlockchainLedgerService {

    private static final String GENESIS_HASH = "0".repeat(64);

    private final TaskLedgerRepository ledgerRepository;

    public BlockchainLedgerService(TaskLedgerRepository ledgerRepository) {
        this.ledgerRepository = ledgerRepository;
    }

    @Transactional
    public void recordEvent(Task task, String eventType, String payload) {
        String previousHash = ledgerRepository.findTopByTaskOrderByBlockIndexDesc(task)
                .map(TaskLedgerEntry::getPayloadHash)
                .orElse(GENESIS_HASH);

        long nextIndex = ledgerRepository.findTopByTaskOrderByBlockIndexDesc(task)
                .map(e -> e.getBlockIndex() + 1)
                .orElse(0L);

        String payloadHash = hash(eventType + "|" + payload + "|" + previousHash + "|" + nextIndex);

        TaskLedgerEntry entry = new TaskLedgerEntry();
        entry.setTask(task);
        entry.setEventType(eventType);
        entry.setPayloadHash(payloadHash);
        entry.setPreviousHash(previousHash);
        entry.setBlockIndex(nextIndex);

        ledgerRepository.save(entry);
    }

    @Transactional(readOnly = true)
    public List<LedgerEntryResponse> getLedger(Task task) {
        return ledgerRepository.findByTaskOrderByBlockIndexAsc(task).stream()
                .map(this::toResponse)
                .toList();
    }

    private LedgerEntryResponse toResponse(TaskLedgerEntry entry) {
        return LedgerEntryResponse.from(entry);
    }

    private String hash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
