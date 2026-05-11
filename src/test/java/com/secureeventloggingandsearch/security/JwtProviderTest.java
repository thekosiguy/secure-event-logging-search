package com.secureeventloggingandsearch.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtProviderTest {

    private static final String SECRET = "test-secret-key-that-is-at-least-32-bytes-long!!";
    private static final long EXPIRATION_MS = 86400000L; // 24 hours

    private JwtProvider jwtProvider;

    @BeforeEach
    void setUp() {
        jwtProvider = new JwtProvider(SECRET, EXPIRATION_MS);
    }

    @Test
    @DisplayName("generateToken produces a valid JWT string")
    void generateToken_producesValidJwtString() {
        String token = jwtProvider.generateToken("testuser", "ROLE_ADMIN");

        assertNotNull(token);
        assertFalse(token.isBlank());
        // JWT has 3 parts separated by dots
        assertEquals(3, token.split("\\.").length);
    }

    @Test
    @DisplayName("generateToken sets correct claims")
    void generateToken_setsCorrectClaims() {
        String token = jwtProvider.generateToken("alice", "ROLE_USER");

        assertEquals("alice", jwtProvider.getUsernameFromToken(token));
        assertEquals("ROLE_USER", jwtProvider.getRoleFromToken(token));
    }

    @Test
    @DisplayName("Token expiration is configurable")
    void tokenExpiration_isConfigurable() {
        // Short expiration
        JwtProvider shortLived = new JwtProvider(SECRET, 1000L); // 1 second
        String token = shortLived.generateToken("user", "ROLE_USER");

        assertTrue(shortLived.validateToken(token));

        // Long expiration
        JwtProvider longLived = new JwtProvider(SECRET, 3600000L); // 1 hour
        String longToken = longLived.generateToken("user", "ROLE_USER");

        assertTrue(longLived.validateToken(longToken));
    }

    @Test
    @DisplayName("Token uses HMAC-SHA algorithm appropriate for key size")
    void token_usesHmacShaAlgorithm() {
        String token = jwtProvider.generateToken("user", "ROLE_ADMIN");

        // Parse the token header to verify algorithm is HMAC-based
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes());
        String algorithm = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getHeader()
                .getAlgorithm();

        // jjwt selects HS256/HS384/HS512 based on key length
        assertTrue(algorithm.startsWith("HS"), "Expected HMAC-SHA algorithm but got: " + algorithm);
    }

    @Test
    @DisplayName("Expired token fails validation")
    void expiredToken_failsValidation() {
        // Create a provider with 0ms expiration (token is immediately expired)
        JwtProvider expiredProvider = new JwtProvider(SECRET, 0L);
        String token = expiredProvider.generateToken("user", "ROLE_USER");

        // Token should be invalid since it expired immediately
        assertFalse(expiredProvider.validateToken(token));
    }

    @Test
    @DisplayName("Tampered token fails validation")
    void tamperedToken_failsValidation() {
        String token = jwtProvider.generateToken("user", "ROLE_ADMIN");

        // Tamper with the token by modifying a character in the signature
        String tampered = token.substring(0, token.length() - 1) + "X";

        assertFalse(jwtProvider.validateToken(tampered));
    }

    @Test
    @DisplayName("Token signed with different key fails validation")
    void differentKey_failsValidation() {
        // Generate token with a different secret
        JwtProvider otherProvider = new JwtProvider(
                "another-secret-key-that-is-at-least-32-bytes-long!!", EXPIRATION_MS);
        String token = otherProvider.generateToken("user", "ROLE_USER");

        // Validate with original provider (different key)
        assertFalse(jwtProvider.validateToken(token));
    }

    @Test
    @DisplayName("Random string fails validation")
    void randomString_failsValidation() {
        assertFalse(jwtProvider.validateToken("not.a.jwt"));
        assertFalse(jwtProvider.validateToken(""));
        assertFalse(jwtProvider.validateToken(null));
        assertFalse(jwtProvider.validateToken("random-garbage-string"));
    }

    @Test
    @DisplayName("validateToken returns true for valid token")
    void validateToken_returnsTrueForValidToken() {
        String token = jwtProvider.generateToken("testuser", "ROLE_ADMIN");

        assertTrue(jwtProvider.validateToken(token));
    }

    @Test
    @DisplayName("Token with manually crafted expired date fails validation")
    void manuallyExpiredToken_failsValidation() {
        // Create a token that's already expired using raw Jwts builder
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes());
        String expiredToken = Jwts.builder()
                .subject("user")
                .claim("role", "ROLE_USER")
                .issuedAt(new Date(System.currentTimeMillis() - 200000))
                .expiration(new Date(System.currentTimeMillis() - 100000))
                .signWith(key)
                .compact();

        assertFalse(jwtProvider.validateToken(expiredToken));
    }
}
