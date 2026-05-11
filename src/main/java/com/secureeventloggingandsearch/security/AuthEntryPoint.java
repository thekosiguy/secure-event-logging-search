package com.secureeventloggingandsearch.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;

@Component
public class AuthEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");

        String timestamp = Instant.now().toString();
        String json = String.format(
                "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Authentication required to access this resource\",\"timestamp\":\"%s\"}",
                timestamp
        );

        response.getWriter().write(json);
    }
}
