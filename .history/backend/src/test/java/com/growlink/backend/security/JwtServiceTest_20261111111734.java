package com.growlink.backend.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class JwtServiceTest {

    private final JwtService jwtService = new JwtService(
            "VGhpcy1pcy1hLXRlc3Qta2V5LXRoYXQtaXMtbG9uZy1lbm91Z2gtZm9yLUhTMjU2", 900);

    @Test
    void tokenIsValidForItsSubject() {
        String token = jwtService.generateToken("person@example.com");

        assertTrue(jwtService.isValid(token, "person@example.com"));
        assertFalse(jwtService.isValid(token, "other@example.com"));
    }

    @Test
    void malformedTokenIsRejected() {
        assertFalse(jwtService.isValid("not-a-jwt", "person@example.com"));
    }
}
