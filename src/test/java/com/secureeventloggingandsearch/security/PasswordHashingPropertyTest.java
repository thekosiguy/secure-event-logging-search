package com.secureeventloggingandsearch.security;

import net.jqwik.api.*;
import net.jqwik.api.constraints.StringLength;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

@Label("Feature: jwt-authentication")
class PasswordHashingPropertyTest {

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Property(tries = 100)
    @Label("Property 2: Password hashing round-trip")
    void passwordHashingRoundTrip(
            @ForAll @StringLength(min = 8, max = 72) String password) {

        String hash = passwordEncoder.encode(password);

        // Hash verifies correctly
        assertTrue(passwordEncoder.matches(password, hash),
                "BCrypt hash should verify the original password");

        // Hash is not equal to plaintext
        assertNotEquals(password, hash,
                "BCrypt hash should not equal the plaintext password");
    }

    @Property(tries = 100)
    @Label("Property 2b: Different passwords produce different hashes")
    void differentPasswordsProduceDifferentHashes(
            @ForAll @StringLength(min = 8, max = 72) String password) {

        String hash1 = passwordEncoder.encode(password);
        String hash2 = passwordEncoder.encode(password);

        // BCrypt uses random salt, so same password produces different hashes
        assertNotEquals(hash1, hash2,
                "BCrypt should produce different hashes for the same password due to random salt");

        // But both should still verify
        assertTrue(passwordEncoder.matches(password, hash1));
        assertTrue(passwordEncoder.matches(password, hash2));
    }

    @Property(tries = 100)
    @Label("Property 2c: Wrong password does not verify")
    void wrongPasswordDoesNotVerify(
            @ForAll @StringLength(min = 8, max = 72) String password,
            @ForAll @StringLength(min = 8, max = 72) String wrongPassword) {

        Assume.that(!password.equals(wrongPassword));

        String hash = passwordEncoder.encode(password);

        assertFalse(passwordEncoder.matches(wrongPassword, hash),
                "BCrypt hash should not verify a different password");
    }
}
