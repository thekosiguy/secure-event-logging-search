package com.secureeventloggingandsearch.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.secureeventloggingandsearch.dto.EventRequest;
import com.secureeventloggingandsearch.dto.EventResponse;
import com.secureeventloggingandsearch.dto.PagedResponse;
import com.secureeventloggingandsearch.exception.EventNotFoundException;
import com.secureeventloggingandsearch.model.Event;
import com.secureeventloggingandsearch.repository.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private EventService eventService;

    private ObjectMapper objectMapper;
    private JsonNode samplePayload;
    private Event sampleEvent;
    private UUID sampleId;

    @BeforeEach
    void setUp() throws Exception {
        objectMapper = new ObjectMapper();
        samplePayload = objectMapper.readTree("{\"user\":\"john\",\"ip\":\"192.168.1.1\"}");
        sampleId = UUID.randomUUID();
        sampleEvent = new Event("LOGIN", Instant.now(), samplePayload);
    }

    @Test
    @DisplayName("createEvent - saves event with uppercase type and returns response")
    void createEvent_savesWithUppercaseType() {
        EventRequest request = new EventRequest();
        request.setType("login");
        request.setPayload(samplePayload);

        Event savedEvent = new Event("LOGIN", Instant.now(), samplePayload);
        when(eventRepository.save(any(Event.class))).thenReturn(savedEvent);

        EventResponse response = eventService.createEvent(request);

        assertNotNull(response);
        assertEquals("LOGIN", response.getType());
        assertEquals(samplePayload, response.getPayload());
        verify(eventRepository, times(1)).save(any(Event.class));
    }

    @Test
    @DisplayName("createEvent - uses current timestamp when not provided")
    void createEvent_defaultsTimestampToNow() {
        EventRequest request = new EventRequest();
        request.setType("LOGOUT");
        request.setPayload(samplePayload);
        request.setTimestamp(null);

        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EventResponse response = eventService.createEvent(request);

        assertNotNull(response.getTimestamp());
        verify(eventRepository).save(argThat(event -> event.getTimestamp() != null));
    }

    @Test
    @DisplayName("createEvent - uses provided timestamp when given")
    void createEvent_usesProvidedTimestamp() {
        Instant customTime = Instant.parse("2026-01-01T00:00:00Z");
        EventRequest request = new EventRequest();
        request.setType("LOGIN");
        request.setTimestamp(customTime);
        request.setPayload(samplePayload);

        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EventResponse response = eventService.createEvent(request);

        assertEquals(customTime, response.getTimestamp());
    }

    @Test
    @DisplayName("getAllEvents - returns paginated response")
    void getAllEvents_returnsPaginatedResponse() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Event> page = new PageImpl<>(List.of(sampleEvent), pageable, 1);
        when(eventRepository.findByFilters(any(), any(), any(), any())).thenReturn(page);

        PagedResponse<EventResponse> response = eventService.getAllEvents(null, null, null, pageable);

        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
        assertEquals(1, response.getContent().size());
        assertEquals("LOGIN", response.getContent().get(0).getType());
    }

    @Test
    @DisplayName("getAllEvents - normalises type filter to uppercase")
    void getAllEvents_normalisesTypeToUppercase() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Event> page = new PageImpl<>(List.of(), pageable, 0);
        when(eventRepository.findByFilters(eq("LOGIN"), any(), any(), any())).thenReturn(page);

        eventService.getAllEvents("login", null, null, pageable);

        verify(eventRepository).findByFilters(eq("LOGIN"), isNull(), isNull(), any());
    }

    @Test
    @DisplayName("getAllEvents - passes null type when not provided")
    void getAllEvents_passesNullTypeWhenNotProvided() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Event> page = new PageImpl<>(List.of(), pageable, 0);
        when(eventRepository.findByFilters(isNull(), any(), any(), any())).thenReturn(page);

        eventService.getAllEvents(null, null, null, pageable);

        verify(eventRepository).findByFilters(isNull(), isNull(), isNull(), any());
    }

    @Test
    @DisplayName("getEventById - returns event when found")
    void getEventById_returnsEventWhenFound() {
        when(eventRepository.findById(sampleId)).thenReturn(Optional.of(sampleEvent));

        EventResponse response = eventService.getEventById(sampleId);

        assertNotNull(response);
        assertEquals("LOGIN", response.getType());
        assertEquals(samplePayload, response.getPayload());
    }

    @Test
    @DisplayName("getEventById - throws EventNotFoundException when not found")
    void getEventById_throwsWhenNotFound() {
        when(eventRepository.findById(sampleId)).thenReturn(Optional.empty());

        assertThrows(EventNotFoundException.class, () -> eventService.getEventById(sampleId));
    }

    @Test
    @DisplayName("getEventById - throws IllegalArgumentException when id is null")
    void getEventById_throwsWhenIdIsNull() {
        assertThrows(IllegalArgumentException.class, () -> eventService.getEventById(null));
    }
}
