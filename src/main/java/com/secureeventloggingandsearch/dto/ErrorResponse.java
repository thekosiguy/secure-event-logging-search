package com.secureeventloggingandsearch.dto;

import java.time.Instant;
import java.util.List;

public class ErrorResponse {

    private int status;
    private String error;
    private String message;
    private Instant timestamp;
    private List<String> details;

    public ErrorResponse(int status, String error, String message) {
        this.status = status;
        this.error = error;
        this.message = message;
        this.timestamp = Instant.now();
    }

    public ErrorResponse(int status, String error, String message, List<String> details) {
        this(status, error, message);
        this.details = details;
    }

    public int getStatus() { return status; }
    public String getError() { return error; }
    public String getMessage() { return message; }
    public Instant getTimestamp() { return timestamp; }
    public List<String> getDetails() { return details; }
}
