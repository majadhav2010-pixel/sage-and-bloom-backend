package com.example.ecommerce.user;

import com.example.ecommerce.common.exception.BadRequestException;
import com.example.ecommerce.security.JwtTokenProvider;
import com.example.ecommerce.user.dto.AuthDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.jwt.refresh-expiration-ms:604800000}") // Default 7 days
    private long refreshExpirationInMs;

    @Transactional
    public String createRefreshToken(User user, String ipAddress, String userAgent) {
        byte[] randomBytes = new byte[64];
        secureRandom.nextBytes(randomBytes);
        String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);

        String tokenHash = hashToken(rawToken);
        Instant expiryDate = Instant.now().plus(refreshExpirationInMs, ChronoUnit.MILLIS);

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .tokenHash(tokenHash)
                .expiryDate(expiryDate)
                .revoked(false)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .build();

        refreshTokenRepository.save(refreshToken);
        return rawToken;
    }

    @Transactional
    public AuthDto.AuthResponse rotateRefreshToken(String rawRefreshToken, String ipAddress, String userAgent) {
        String tokenHash = hashToken(rawRefreshToken);
        RefreshToken storedToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new BadRequestException("Invalid refresh token"));

        // Breach Detection: If a revoked token is presented, compromise is suspected
        if (storedToken.isRevoked()) {
            log.warn("SECURITY ALERT: Revoked refresh token reuse detected for user {}. Revoking all active tokens!", storedToken.getUser().getId());
            refreshTokenRepository.revokeAllUserTokens(storedToken.getUser().getId());
            throw new BadRequestException("Refresh token was previously revoked. All sessions terminated for security.");
        }

        if (storedToken.isExpired()) {
            storedToken.setRevoked(true);
            refreshTokenRepository.save(storedToken);
            throw new BadRequestException("Refresh token has expired. Please log in again.");
        }

        // Revoke the old token as part of rotation
        storedToken.setRevoked(true);
        refreshTokenRepository.save(storedToken);

        User user = storedToken.getUser();
        if ("SUSPENDED".equalsIgnoreCase(user.getStatus())) {
            throw new BadRequestException("This account is suspended.");
        }

        // Issue new Access Token and new rotated Refresh Token
        String roles = user.getRoles().stream().map(Role::getName).collect(Collectors.joining(","));
        String newAccessToken = jwtTokenProvider.generateTokenFromUserId(user.getId(), user.getEmail(), roles);
        String newRefreshToken = createRefreshToken(user, ipAddress, userAgent);

        return AuthDto.AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(86400000)
                .user(com.example.ecommerce.user.dto.UserDto.fromEntity(user))
                .build();
    }

    @Transactional
    public void revokeToken(String rawRefreshToken) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) return;
        String tokenHash = hashToken(rawRefreshToken);
        refreshTokenRepository.findByTokenHash(tokenHash).ifPresent(token -> {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
        });
    }

    private String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
}
