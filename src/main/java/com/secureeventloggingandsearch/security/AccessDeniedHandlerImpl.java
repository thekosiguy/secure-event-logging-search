package com.secureeventloggingandsearch.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;

@Component
public class AccessDeniedHandlerImpl implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");

        String timestamp = Instant.now().toString();
        String json = String.format(
                "{\"status\":403,\"error\":\"Forbidden\",\"message\":\"Access denied: insufficient permissions\",\"timestamp\":\"%s\"}",
                timestamp
        );

        response.getWriter().write(json);
    }
}
