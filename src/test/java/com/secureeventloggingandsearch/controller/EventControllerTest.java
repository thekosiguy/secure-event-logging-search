package com.secureeventloggingandsearch.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.secureeventloggingandsearch.dto.EventResponse;
import com.secureeventloggingandsearch.dto.PagedResponse;
import com.secureeventloggingandsearch.exception.EventNotFoundException;
import com.secureeventloggingandsearch.exception.GlobalExceptionHandler;
import com.secureeventloggingandsearch.service.EventService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class EventControllerTest {

    @Mock
    private EventService eventService;

    @InjectMocks
    private EventController eventController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private JsonNode samplePayload;
    private UUID sampleId;
    private EventResponse sampleResponse;

    @BeforeEach
    void setUp() throws Exception {
        mockMvc = MockMvcBuilders.standaloneSetup(eventController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
        objectMapper = new ObjectMapper();
        samplePayload = objectMapper.readTree("{\"user\":\"john\"}");
        sampleId = UUID.randomUUID();
        sampleResponse = new EventResponse(sampleId, "LOGIN", Instant.now(), samplePayload);
    }

    @Test
    @DisplayName("POST /api/v1/events - returns 201 with valid request")
    @WithMockUser(roles = "ADMIN")
    void createEvent_returns201() throws Exception {
        when(eventService.createEvent(any())).thenReturn(sampleResponse);

        mockMvc.perform(post("/api/v1/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"LOGIN\",\"payload\":{\"user\":\"john\"}}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("LOGIN"))
                .andExpect(jsonPath("$.id").value(sampleId.toString()));

        verify(eventService, times(1)).createEvent(any());
    }

    @Test
    @DisplayName("POST /api/v1/events - returns 400 when type is blank")
    @WithMockUser(roles = "ADMIN")
    void createEvent_returns400WhenTypeBlank() throws Exception {
        mockMvc.perform(post("/api/v1/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"\",\"payload\":{\"user\":\"john\"}}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Failed"));
    }

    @Test
    @DisplayName("POST /api/v1/events - returns 400 when payload is null")
    @WithMockUser(roles = "ADMIN")
    void createEvent_returns400WhenPayloadNull() throws Exception {
        mockMvc.perform(post("/api/v1/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"LOGIN\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("POST /api/v1/events - returns 400 when body is malformed")
    @WithMockUser(roles = "ADMIN")
    void createEvent_returns400WhenBodyMalformed() throws Exception {
        mockMvc.perform(post("/api/v1/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("not json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Malformed Request"));
    }

    @Test
    @DisplayName("GET /api/v1/events - returns 200 with paginated response")
    @WithMockUser(roles = "USER")
    void getAllEvents_returns200() throws Exception {
        PagedResponse<EventResponse> pagedResponse = new PagedResponse<>(
                new PageImpl<>(List.of(sampleResponse), PageRequest.of(0, 20), 1));
        when(eventService.getAllEvents(any(), any(), any(), any())).thenReturn(pagedResponse);

        mockMvc.perform(get("/api/v1/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].type").value("LOGIN"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("GET /api/v1/events - returns 400 for invalid sort field")
    @WithMockUser(roles = "USER")
    void getAllEvents_returns400ForInvalidSort() throws Exception {
        mockMvc.perform(get("/api/v1/events").param("sort", "payload,desc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/v1/events/{id} - returns 200 when found")
    @WithMockUser(roles = "USER")
    void getEventById_returns200() throws Exception {
        when(eventService.getEventById(sampleId)).thenReturn(sampleResponse);

        mockMvc.perform(get("/api/v1/events/{id}", sampleId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(sampleId.toString()))
                .andExpect(jsonPath("$.type").value("LOGIN"));
    }

    @Test
    @DisplayName("GET /api/v1/events/{id} - returns 404 when not found")
    @WithMockUser(roles = "USER")
    void getEventById_returns404() throws Exception {
        when(eventService.getEventById(sampleId)).thenThrow(new EventNotFoundException(sampleId));

        mockMvc.perform(get("/api/v1/events/{id}", sampleId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"));
    }
}
