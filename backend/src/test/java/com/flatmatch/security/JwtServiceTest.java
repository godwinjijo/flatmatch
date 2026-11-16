package com.flatmatch.security;

import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private final JwtService jwt = new JwtService(
            "test-secret-key-that-is-long-enough-for-hmac-sha-256!!", 86_400_000);

    @Test
    @DisplayName("generate + parse round-trips the userId")
    void roundTrip() {
        String token = jwt.generate(42L);
        assertEquals(42L, jwt.parseUserId(token));
    }

    @Test
    @DisplayName("tampered tokens are rejected")
    void tamperedTokenRejected() {
        String token = jwt.generate(42L);
        String tampered = token.substring(0, token.length() - 3) + "xxx";
        assertThrows(JwtException.class, () -> jwt.parseUserId(tampered));
    }

    @Test
    @DisplayName("expired tokens are rejected")
    void expiredTokenRejected() {
        JwtService shortLived = new JwtService(
                "test-secret-key-that-is-long-enough-for-hmac-sha-256!!", -1000);
        String token = shortLived.generate(42L);
        assertThrows(JwtException.class, () -> shortLived.parseUserId(token));
    }
}
