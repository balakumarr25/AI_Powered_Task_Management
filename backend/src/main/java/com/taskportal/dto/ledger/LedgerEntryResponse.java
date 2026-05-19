package com.taskportal.dto.ledger;

import com.taskportal.entity.TaskLedgerEntry;
import java.time.Instant;

public class LedgerEntryResponse {
    private Long id;
    private String eventType;
    private String payloadHash;
    private String previousHash;
    private Long blockIndex;
    private Instant timestamp;

    public static LedgerEntryResponse from(TaskLedgerEntry entry) {
        LedgerEntryResponse r = new LedgerEntryResponse();
        r.id = entry.getId();
        r.eventType = entry.getEventType();
        r.payloadHash = entry.getPayloadHash();
        r.previousHash = entry.getPreviousHash();
        r.blockIndex = entry.getBlockIndex();
        r.timestamp = entry.getTimestamp();
        return r;
    }

    public Long getId() { return id; }
    public String getEventType() { return eventType; }
    public String getPayloadHash() { return payloadHash; }
    public String getPreviousHash() { return previousHash; }
    public Long getBlockIndex() { return blockIndex; }
    public Instant getTimestamp() { return timestamp; }
}
