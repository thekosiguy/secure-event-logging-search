package com.secureeventloggingandsearch.dto;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public class EventRequest {

    @NotBlank(message = "Event type must not be blank")
    private String type;

    private Instant timestamp;

    @NotNull(message = "Payload must not be null")
    private JsonNode payload;

    public EventRequest() {}

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }

    public JsonNode getPayload() { return payload; }
    public void setPayload(JsonNode payload) { this.payload = payload; }
}
