package com.secureeventloggingandsearch.model;

import com.fasterxml.jackson.databind.JsonNode;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import org.hibernate.annotations.Type;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "events")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "type", nullable = false)
    private String type;

    @Column(name = "timestamp", nullable = false)
    private Instant timestamp;

    @Type(JsonBinaryType.class)
    @Column(name = "payload", columnDefinition = "jsonb")
    private JsonNode payload;

    public Event() {}

    public Event(String type, Instant timestamp, JsonNode payload) {
        this.type = type;
        this.timestamp = timestamp;
        this.payload = payload;
    }

    public UUID getId() { return id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }

    public JsonNode getPayload() { return payload; }
    public void setPayload(JsonNode payload) { this.payload = payload; }
}
