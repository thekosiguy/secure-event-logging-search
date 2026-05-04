package com.secureeventloggingandsearch.model;

import jakarta.persistence.*;
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

    @Column(name = "payload", columnDefinition = "TEXT")
    private String payload;

    // Default constructor required by JPA
    public Event() {}

    public Event(String type, Instant timestamp, String payload) {
        this.type = type;
        this.timestamp = timestamp;
        this.payload = payload;
    }

    public UUID getId() {
        return id;
    }

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
