package com.secureeventloggingandsearch.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import net.jqwik.api.*;
import net.jqwik.api.constraints.AlphaChars;
import net.jqwik.api.constraints.StringLength;

import javax.crypto.SecretKey;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@Label("Feature: jwt-authentication")
class JwtProviderPropertyTest {

    private static final String SECRET = "test-secret-key-that-is-at-least-32-bytes-long!!";
    private static final long EXPIRATION_MS = 86400000L;

    private final JwtProvider jwtProvider = new JwtProvider(SECRET, EXPIRATION_MS);

    @Property(tries = 100)
    @Label("Property 1: Token claim round-trip")
    void tokenClaimRoundTrip(
            @ForAll @AlphaChars @StringLength(min = 1, max = 50) String username,
            @ForAll("validRoles") String role) {

        String token = jwtProvider.generateToken(username, role);

        assertNotNull(token);
        assertTrue(jwtProvider.validateToken(token));
        assertEquals(username, jwtProvider.getUsernameFromToken(token));
        assertEquals(role, jwtProvider.getRoleFromToken(token));
    }

    @Property(tries = 100)
    @Label("Property 9: Invalid tokens are rejected")
    void invalidTokensAreRejected(
            @ForAll("invalidTokens") String invalidToken) {

        assertFalse(jwtProvider.validateToken(invalidToken));
    }

    @Property(tries = 100)
    @Label("Property 9b: Tokens signed with different keys are rejected")
    void tokensSignedWithDifferentKeysAreRejected(
            @ForAll @AlphaChars @StringLength(min = 1, max = 50) String username,
            @ForAll("validRoles") String role) {

        // Generate token with a different key
        JwtProvider otherProvider = new JwtProvider(
                "a-completely-different-secret-key-32-bytes-long!!", EXPIRATION_MS);
        String token = otherProvider.generateToken(username, role);

        assertFalse(jwtProvider.validateToken(token));
    }

    @Property(tries = 100)
    @Label("Property 9c: Expired tokens are rejected")
    void expiredTokensAreRejected(
            @ForAll @AlphaChars @StringLength(min = 1, max = 50) String username,
            @ForAll("validRoles") String role) {

        // Create an already-expired token
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes());
        String expiredToken = Jwts.builder()
                .subject(username)
                .claim("role", role)
                .issuedAt(new Date(System.currentTimeMillis() - 200000))
                .expiration(new Date(System.currentTimeMillis() - 100000))
                .signWith(key)
                .compact();

        assertFalse(jwtProvider.validateToken(expiredToken));
    }

    @Provide
    Arbitrary<String> validRoles() {
        return Arbitraries.of("ROLE_ADMIN", "ROLE_USER");
    }

    @Provide
    Arbitrary<String> invalidTokens() {
        return Arbitraries.oneOf(
                // Random alphanumeric strings
                Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(100),
                // Strings with dots but not valid JWT
                Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(30)
                        .map(s -> s + "." + s + "." + s),
                // Constant invalid values
                Arbitraries.of("not.a.jwt", "abc", "x.y.z", "header.payload.signature")
        );
    }
}
