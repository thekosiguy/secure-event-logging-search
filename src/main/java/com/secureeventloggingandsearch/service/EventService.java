package com.secureeventloggingandsearch.service;

import com.secureeventloggingandsearch.dto.EventRequest;
import com.secureeventloggingandsearch.dto.EventResponse;
import com.secureeventloggingandsearch.dto.PagedResponse;
import com.secureeventloggingandsearch.exception.EventNotFoundException;
import com.secureeventloggingandsearch.model.Event;
import com.secureeventloggingandsearch.repository.EventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class EventService {

    private static final Logger log = LoggerFactory.getLogger(EventService.class);

    private final EventRepository eventRepository;

    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public EventResponse createEvent(EventRequest request) {
        log.info("Creating event of type: {}", request.getType());
        Event event = new Event(
                request.getType().toUpperCase(),
                request.getTimestamp() != null ? request.getTimestamp() : Instant.now(),
                request.getPayload()
        );
        Event saved = eventRepository.save(event);
        log.info("Event created with id: {}", saved.getId());
        return toResponse(saved);
    }

    public PagedResponse<EventResponse> getAllEvents(String type, Instant from, Instant to, Pageable pageable) {
        log.info("Fetching events - page: {}, size: {}, type: {}, from: {}, to: {}",
                pageable.getPageNumber(), pageable.getPageSize(), type, from, to);
        String normalisedType = type != null ? type.toUpperCase() : null;
        Page<EventResponse> page = eventRepository.findByFilters(normalisedType, from, to, pageable)
                .map(this::toResponse);
        return new PagedResponse<>(page);
    }

    public EventResponse getEventById(UUID id) {
        log.info("Fetching event with id: {}", id);
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Event not found with id: {}", id);
                    return new EventNotFoundException(id);
                });
        return toResponse(event);
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
