package com.secureeventloggingandsearch.service;

import com.secureeventloggingandsearch.dto.AuthResponse;
import com.secureeventloggingandsearch.dto.LoginRequest;
import com.secureeventloggingandsearch.dto.RegisterRequest;
import com.secureeventloggingandsearch.dto.RegisterResponse;
import com.secureeventloggingandsearch.model.Role;
import com.secureeventloggingandsearch.model.User;
import com.secureeventloggingandsearch.repository.UserRepository;
import com.secureeventloggingandsearch.security.JwtProvider;
import net.jqwik.api.*;
import net.jqwik.api.constraints.AlphaChars;
import net.jqwik.api.constraints.StringLength;
import net.jqwik.api.lifecycle.BeforeProperty;
import org.mockito.Mockito;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Label("Feature: jwt-authentication")
class AuthServicePropertyTest {

    private UserRepository userRepository;
    private JwtProvider jwtProvider;
    private PasswordEncoder passwordEncoder;
    private AuthService authService;
    private Map<String, User> userStore;

    @BeforeProperty
    void setUp() {
        userRepository = Mockito.mock(UserRepository.class);
        jwtProvider = new JwtProvider(
                "test-secret-key-that-is-at-least-32-bytes-long!!", 86400000L);
        passwordEncoder = new BCryptPasswordEncoder();
        authService = new AuthService(userRepository, jwtProvider, passwordEncoder);
        userStore = new HashMap<>();

        // Mock repository behavior using in-memory store
        when(userRepository.existsByUsername(any())).thenAnswer(
                invocation -> userStore.containsKey(invocation.getArgument(0)));
        when(userRepository.findByUsername(any())).thenAnswer(
                invocation -> Optional.ofNullable(userStore.get(invocation.getArgument(0))));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            userStore.put(user.getUsername(), user);
            return user;
        });
    }

    @Property(tries = 100)
    @Label("Property 3: Valid registration produces correct response")
    void validRegistrationProducesCorrectResponse(
            @ForAll @AlphaChars @StringLength(min = 3, max = 50) String username,
            @ForAll @StringLength(min = 8, max = 50) String password,
            @ForAll("validRoles") Role role) {

        // Ensure username is unique for this test
        Assume.that(!userStore.containsKey(username));

        RegisterRequest request = new RegisterRequest();
        request.setUsername(username);
        request.setPassword(password);
        request.setRole(role);

        RegisterResponse response = authService.register(request);

        assertEquals(username, response.getUsername());
        assertEquals(role.name(), response.getRole());
    }

    @Property(tries = 100)
    @Label("Property 4: Duplicate registration is rejected")
    void duplicateRegistrationIsRejected(
            @ForAll @AlphaChars @StringLength(min = 3, max = 50) String username,
            @ForAll @StringLength(min = 8, max = 50) String password,
            @ForAll("validRoles") Role role) {

        // Ensure username is unique before first registration
        Assume.that(!userStore.containsKey(username));

        RegisterRequest request = new RegisterRequest();
        request.setUsername(username);
        request.setPassword(password);
        request.setRole(role);

        // First registration should succeed
        RegisterResponse response = authService.register(request);
        assertNotNull(response);

        // Second registration with same username should fail with 409
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authService.register(request));

        assertEquals(409, exception.getStatusCode().value());
    }

    @Property(tries = 100)
    @Label("Property 6: Valid credentials yield a valid token")
    void validCredentialsYieldValidToken(
            @ForAll @AlphaChars @StringLength(min = 3, max = 50) String username,
            @ForAll @StringLength(min = 8, max = 50) String password,
            @ForAll("validRoles") Role role) {

        // Ensure username is unique
        Assume.that(!userStore.containsKey(username));

        // Register user first
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername(username);
        registerRequest.setPassword(password);
        registerRequest.setRole(role);
        authService.register(registerRequest);

        // Login with correct credentials
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(username);
        loginRequest.setPassword(password);

        AuthResponse authResponse = authService.login(loginRequest);

        assertNotNull(authResponse.getToken());
        assertTrue(jwtProvider.validateToken(authResponse.getToken()));
        assertEquals(username, jwtProvider.getUsernameFromToken(authResponse.getToken()));
        assertEquals(role.name(), jwtProvider.getRoleFromToken(authResponse.getToken()));
    }

    @Property(tries = 100)
    @Label("Property 7: Invalid credentials are rejected")
    void invalidCredentialsAreRejected(
            @ForAll @AlphaChars @StringLength(min = 3, max = 50) String username,
            @ForAll @StringLength(min = 8, max = 50) String correctPassword,
            @ForAll @StringLength(min = 8, max = 50) String wrongPassword,
            @ForAll("validRoles") Role role) {

        Assume.that(!correctPassword.equals(wrongPassword));
        Assume.that(!userStore.containsKey(username));

        // Register user
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername(username);
        registerRequest.setPassword(correctPassword);
        registerRequest.setRole(role);
        authService.register(registerRequest);

        // Login with wrong password
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(username);
        loginRequest.setPassword(wrongPassword);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authService.login(loginRequest));

        assertEquals(401, exception.getStatusCode().value());
    }

    @Provide
    Arbitrary<Role> validRoles() {
        return Arbitraries.of(Role.ROLE_ADMIN, Role.ROLE_USER);
    }
}
