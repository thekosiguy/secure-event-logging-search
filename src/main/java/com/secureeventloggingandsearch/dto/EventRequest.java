package com.secureeventloggingandsearch.dto;

import java.time.Instant;

public class EventRequest {

    private String type;
    private Instant timestamp;
    private String payload;

    public EventRequest() {}

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }
}
