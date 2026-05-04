package com.secureeventloggingandsearch.exception;

import java.util.UUID;

public class EventNotFoundException extends RuntimeException {

    public EventNotFoundException(UUID id) {
        super("Event not found with id: " + id);
    }
}
