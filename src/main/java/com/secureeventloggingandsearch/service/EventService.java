package com.secureeventloggingandsearch.service;

import com.secureeventloggingandsearch.dto.EventRequest;
import com.secureeventloggingandsearch.dto.EventResponse;
import com.secureeventloggingandsearch.model.Event;
import com.secureeventloggingandsearch.repository.EventRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EventService {

    private final EventRepository eventRepository;

    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public EventResponse createEvent(EventRequest request) {
        Event event = new Event(
                request.getType(),
                request.getTimestamp() != null ? request.getTimestamp() : Instant.now(),
                request.getPayload()
        );
        Event saved = eventRepository.save(event);
        return toResponse(saved);
    }

    public List<EventResponse> getAllEvents() {
        return eventRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private EventResponse toResponse(Event event) {
        return new EventResponse(
                event.getId(),
                event.getType(),
                event.getTimestamp(),
                event.getPayload()
        );
    }
}
