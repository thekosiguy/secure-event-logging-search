package com.secureeventloggingandsearch.dto;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;
import java.util.UUID;

public class EventResponse {

    private UUID id;
    private String type;
    private Instant timestamp;
    private JsonNode payload;

    public EventResponse() {}

    public EventResponse(UUID id, String type, Instant timestamp, JsonNode payload) {
        this.id = id;
        this.type = type;
        this.timestamp = timestamp;
        this.payload = payload;
    }

    public UUID getId() { return id; }
    public String getType() { return type; }
    public Instant getTimestamp() { return timestamp; }
    public JsonNode getPayload() { return payload; }
}
