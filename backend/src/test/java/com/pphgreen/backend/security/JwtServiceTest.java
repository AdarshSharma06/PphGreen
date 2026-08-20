package com.pphgreen.backend.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class JwtServiceTest {

    private static final String SECRET = "test-secret-which-is-at-least-32-bytes-long-for-hs256";

    @Test
    void generateAndValidateToken() {
        JwtService jwtService = new JwtService(SECRET, 3600000);

        String token = jwtService.generateToken("test@example.com", "MEMBER");

        assertTrue(jwtService.isValid(token));
        assertEquals("test@example.com", jwtService.extractSubject(token));
        assertEquals("MEMBER", jwtService.extractRole(token));
    }

    @Test
    void expiredTokenIsInvalid() {
        JwtService jwtService = new JwtService(SECRET, -1000);

        String token = jwtService.generateToken("test@example.com", "MEMBER");

        assertFalse(jwtService.isValid(token));
    }

    @Test
    void tamperedTokenIsInvalid() {
        JwtService jwtService = new JwtService(SECRET, 3600000);

        String token = jwtService.generateToken("test@example.com", "MEMBER");
        String[] parts = token.split("\\.");
        String signature = parts[2];
        String tamperedSignature = (signature.charAt(0) == 'A' ? "B" : "A") + signature.substring(1);

        String tampered = parts[0] + "." + parts[1] + "." + tamperedSignature;

        assertFalse(jwtService.isValid(tampered));
    }

    @Test
    void tokenSignedWithDifferentSecretIsInvalid() {
        JwtService issuer = new JwtService(SECRET, 3600000);
        JwtService verifier = new JwtService("another-secret-which-is-also-at-least-32-bytes-long", 3600000);

        String token = issuer.generateToken("test@example.com", "MEMBER");

        assertFalse(verifier.isValid(token));
    }

    @Test
    void rejectsWeakSecret() {
        assertThrows(IllegalArgumentException.class, () -> new JwtService("too-short", 3600000));
    }
}