package com.taskportal.dto.common;

import java.time.Instant;
import java.util.List;

public class ApiError {
    private Instant timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
    private List<String> details;

    public static ApiError of(int status, String error, String message, String path, List<String> details) {
        ApiError e = new ApiError();
        e.timestamp = Instant.now();
        e.status = status;
        e.error = error;
        e.message = message;
        e.path = path;
        e.details = details;
        return e;
    }

    public Instant getTimestamp() { return timestamp; }
    public int getStatus() { return status; }
    public String getError() { return error; }
    public String getMessage() { return message; }
    public String getPath() { return path; }
    public List<String> getDetails() { return details; }
}
