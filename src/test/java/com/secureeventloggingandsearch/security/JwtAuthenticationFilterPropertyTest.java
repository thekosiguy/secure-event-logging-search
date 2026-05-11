package com.secureeventloggingandsearch.security;

import net.jqwik.api.*;
import net.jqwik.api.constraints.AlphaChars;
import net.jqwik.api.constraints.StringLength;
import net.jqwik.api.lifecycle.BeforeProperty;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.*;

@Label("Feature: jwt-authentication")
class JwtAuthenticationFilterPropertyTest {

    private static final String SECRET = "test-secret-key-that-is-at-least-32-bytes-long!!";
    private static final long EXPIRATION_MS = 86400000L;

    private JwtProvider jwtProvider;
    private JwtAuthenticationFilter filter;

    @BeforeProperty
    void setUp() {
        jwtProvider = new JwtProvider(SECRET, EXPIRATION_MS);
        filter = new JwtAuthenticationFilter(jwtProvider);
        SecurityContextHolder.clearContext();
    }

    @Property(tries = 100)
    @Label("Property 8: Filter populates SecurityContext from valid token")
    void filterPopulatesSecurityContextFromValidToken(
            @ForAll @AlphaChars @StringLength(min = 1, max = 50) String username,
            @ForAll("validRoles") String role) throws Exception {

        // Clear context before each iteration
        SecurityContextHolder.clearContext();

        String token = jwtProvider.generateToken(username, role);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        filter.doFilterInternal(request, response, filterChain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth, "SecurityContext should contain authentication");
        assertEquals(username, auth.getPrincipal());
        assertTrue(auth.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals(role)),
                "Authorities should contain the role: " + role);
    }

    @Property(tries = 100)
    @Label("Property 8b: Filter does not populate SecurityContext for invalid token")
    void filterDoesNotPopulateSecurityContextForInvalidToken(
            @ForAll @AlphaChars @StringLength(min = 1, max = 50) String invalidToken) throws Exception {

        SecurityContextHolder.clearContext();

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + invalidToken);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        filter.doFilterInternal(request, response, filterChain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNull(auth, "SecurityContext should not contain authentication for invalid token");
    }

    @Provide
    Arbitrary<String> validRoles() {
        return Arbitraries.of("ROLE_ADMIN", "ROLE_USER");
    }
}
