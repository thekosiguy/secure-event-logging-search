package com.secureeventloggingandsearch.security;

import com.secureeventloggingandsearch.controller.AuthController;
import com.secureeventloggingandsearch.controller.EventController;
import com.secureeventloggingandsearch.controller.HealthController;
import com.secureeventloggingandsearch.service.AuthService;
import com.secureeventloggingandsearch.service.EventService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({AuthController.class, EventController.class, HealthController.class})
@Import({SecurityConfig.class, AuthEntryPoint.class, AccessDeniedHandlerImpl.class,
        JwtAuthenticationFilter.class, JwtProvider.class})
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private EventService eventService;

    @Test
    @DisplayName("Health endpoint is accessible without authentication")
    void healthEndpoint_isPublic() throws Exception {
        mockMvc.perform(get("/api/v1/health"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Auth endpoints are accessible without authentication")
    void authEndpoints_arePublic() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"test\",\"password\":\"password123\"}"))
                .andExpect(status().isOk())
                .andReturn();
    }

    @Test
    @DisplayName("Protected endpoint returns 401 without token")
    void protectedEndpoint_returns401WithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/events"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    @DisplayName("POST /api/v1/events returns 401 without token")
    void postEvents_returns401WithoutToken() throws Exception {
        mockMvc.perform(post("/api/v1/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"LOGIN\",\"payload\":{}}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("ADMIN can POST events")
    @WithMockUser(roles = "ADMIN")
    void admin_canPostEvents() throws Exception {
        mockMvc.perform(post("/api/v1/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"LOGIN\",\"payload\":{\"user\":\"john\"}}"))
                .andExpect(status().isCreated())
                .andReturn();
    }

    @Test
    @DisplayName("USER cannot POST events - returns 403")
    @WithMockUser(roles = "USER")
    void user_cannotPostEvents() throws Exception {
        mockMvc.perform(post("/api/v1/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"LOGIN\",\"payload\":{\"user\":\"john\"}}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }

    @Test
    @DisplayName("USER can GET events")
    @WithMockUser(roles = "USER")
    void user_canGetEvents() throws Exception {
        mockMvc.perform(get("/api/v1/events"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("ADMIN can GET events")
    @WithMockUser(roles = "ADMIN")
    void admin_canGetEvents() throws Exception {
        mockMvc.perform(get("/api/v1/events"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Session management is stateless - no JSESSIONID cookie")
    void sessionManagement_isStateless() throws Exception {
        mockMvc.perform(get("/api/v1/health"))
                .andExpect(status().isOk())
                .andExpect(cookie().doesNotExist("JSESSIONID"));
    }
}
