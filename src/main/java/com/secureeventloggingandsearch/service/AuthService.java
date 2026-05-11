package com.secureeventloggingandsearch.service;

import com.secureeventloggingandsearch.dto.AuthResponse;
import com.secureeventloggingandsearch.dto.LoginRequest;
import com.secureeventloggingandsearch.dto.RegisterRequest;
import com.secureeventloggingandsearch.dto.RegisterResponse;
import com.secureeventloggingandsearch.model.User;
import com.secureeventloggingandsearch.repository.UserRepository;
import com.secureeventloggingandsearch.security.JwtProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, JwtProvider jwtProvider, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.jwtProvider = jwtProvider;
        this.passwordEncoder = passwordEncoder;
    }

    public RegisterResponse register(RegisterRequest request) {
        log.info("Registering user: {}", request.getUsername());

        if (userRepository.existsByUsername(request.getUsername())) {
            log.warn("Registration failed: username '{}' is already taken", request.getUsername());
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Username '" + request.getUsername() + "' is already taken");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());

        userRepository.save(user);
        log.info("User registered successfully: {}", request.getUsername());

        return new RegisterResponse(user.getUsername(), user.getRole().name());
    }

    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for user: {}", request.getUsername());

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> {
                    log.warn("Login failed: user '{}' not found", request.getUsername());
                    return new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                            "Invalid username or password");
                });

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Login failed: invalid password for user '{}'", request.getUsername());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "Invalid username or password");
        }

        String token = jwtProvider.generateToken(user.getUsername(), user.getRole().name());
        log.info("User logged in successfully: {}", request.getUsername());

        return new AuthResponse(token);
    }
}
