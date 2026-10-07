package com.example.ecommerce;

import com.example.ecommerce.common.exception.BadRequestException;
import com.example.ecommerce.security.JwtTokenProvider;
import com.example.ecommerce.user.*;
import com.example.ecommerce.user.dto.AuthDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    private User testUser;
    private Role customerRole;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(refreshTokenService, "refreshExpirationInMs", 604800000L);
        customerRole = Role.builder().id(1L).name("ROLE_CUSTOMER").build();
        testUser = User.builder()
                .id(UUID.randomUUID())
                .email("customer@example.com")
                .roles(Collections.singleton(customerRole))
                .status("ACTIVE")
                .build();
    }

    @Test
    @DisplayName("Create Refresh Token generates opaque token and saves hash")
    void testCreateRefreshToken() {
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(i -> i.getArgument(0));

        String rawToken = refreshTokenService.createRefreshToken(testUser, "127.0.0.1", "Test-Agent");
        assertNotNull(rawToken);
        assertFalse(rawToken.isBlank());

        verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("Rotating valid token revokes old token and issues new token pair")
    void testRotateRefreshTokenSuccess() {
        RefreshToken storedToken = RefreshToken.builder()
                .id(UUID.randomUUID())
                .user(testUser)
                .tokenHash("sampleHash")
                .expiryDate(Instant.now().plus(7, ChronoUnit.DAYS))
                .revoked(false)
                .build();

        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(storedToken));
        when(jwtTokenProvider.generateTokenFromUserId(any(), any(), any())).thenReturn("newJwtAccessToken");
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(i -> i.getArgument(0));

        AuthDto.AuthResponse response = refreshTokenService.rotateRefreshToken("rawToken123", "127.0.0.1", "Test-Agent");

        assertNotNull(response);
        assertEquals("newJwtAccessToken", response.getAccessToken());
        assertNotNull(response.getRefreshToken());
        assertTrue(storedToken.isRevoked(), "Old token must be marked revoked upon rotation");
    }

    @Test
    @DisplayName("Reusing a revoked refresh token triggers breach detection and revokes all user tokens")
    void testRevokedTokenReuseTriggersBreachDetection() {
        RefreshToken revokedToken = RefreshToken.builder()
                .id(UUID.randomUUID())
                .user(testUser)
                .tokenHash("sampleHash")
                .expiryDate(Instant.now().plus(7, ChronoUnit.DAYS))
                .revoked(true) // already revoked!
                .build();

        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(revokedToken));

        assertThrows(BadRequestException.class, () ->
                refreshTokenService.rotateRefreshToken("compromisedToken", "127.0.0.1", "Hacker-Agent")
        );

        verify(refreshTokenRepository, times(1)).revokeAllUserTokens(testUser.getId());
    }
}
