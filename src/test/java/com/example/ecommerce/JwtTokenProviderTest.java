package com.example.ecommerce;

import com.example.ecommerce.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider tokenProvider;
    private final String testSecret = "9a4f2c8d7e1b5a3f6c8e0d2b4a6f8c1e3d5b7a9f2c4e6a8b0d2f4e6a8c0e2b4a";
    private final long testExpiration = 3600000; // 1 hour

    @BeforeEach
    void setUp() {
        tokenProvider = new JwtTokenProvider(testSecret, testExpiration);
    }

    @Test
    @DisplayName("Generate token and validate claims correctly")
    void testTokenGenerationAndValidation() {
        UUID userId = UUID.randomUUID();
        String email = "test@example.com";
        String roles = "ROLE_CUSTOMER,ROLE_ADMIN";

        String token = tokenProvider.generateTokenFromUserId(userId, email, roles);
        assertNotNull(token);
        assertTrue(token.length() > 20);

        boolean isValid = tokenProvider.validateToken(token);
        assertTrue(isValid);

        UUID extractedUserId = tokenProvider.getUserIdFromJWT(token);
        assertEquals(userId, extractedUserId);
    }

    @Test
    @DisplayName("Invalid token should fail validation")
    void testInvalidTokenValidation() {
        boolean isValid = tokenProvider.validateToken("invalid.jwt.token");
        assertFalse(isValid);
    }
}
