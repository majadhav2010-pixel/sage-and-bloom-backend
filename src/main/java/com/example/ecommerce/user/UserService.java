package com.example.ecommerce.user;

import com.example.ecommerce.common.exception.BadRequestException;
import com.example.ecommerce.common.exception.ResourceNotFoundException;
import com.example.ecommerce.security.JwtTokenProvider;
import com.example.ecommerce.security.LoginAttemptService;
import com.example.ecommerce.user.dto.AuthDto;
import com.example.ecommerce.user.dto.UserDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final LoginAttemptService loginAttemptService;

    @Transactional
    public AuthDto.AuthResponse register(AuthDto.RegisterRequest request, String ipAddress, String userAgent) {
        String cleanEmail = request.getEmail().toLowerCase().trim();
        if (userRepository.existsByEmail(cleanEmail)) {
            throw new BadRequestException("Email already registered: " + cleanEmail);
        }

        Role customerRole = roleRepository.findByName(Role.ROLE_CUSTOMER)
                .orElseGet(() -> roleRepository.save(Role.builder().name(Role.ROLE_CUSTOMER).build()));

        User user = User.builder()
                .email(cleanEmail)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .status("ACTIVE")
                .roles(Collections.singleton(customerRole))
                .build();

        user = userRepository.save(user);

        String roles = user.getRoles().stream().map(Role::getName).collect(Collectors.joining(","));
        String accessToken = tokenProvider.generateTokenFromUserId(user.getId(), user.getEmail(), roles);
        String refreshToken = refreshTokenService.createRefreshToken(user, ipAddress, userAgent);

        return AuthDto.AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(86400000)
                .user(UserDto.fromEntity(user))
                .build();
    }

    // Overload for backward compatibility / tests
    @Transactional
    public AuthDto.AuthResponse register(AuthDto.RegisterRequest request) {
        return register(request, "127.0.0.1", "API-Client");
    }

    @Transactional
    public AuthDto.AuthResponse login(AuthDto.LoginRequest request, String ipAddress, String userAgent) {
        String cleanEmail = request.getEmail().toLowerCase().trim();

        // Check Brute Force Protection
        if (loginAttemptService.isBlocked(cleanEmail)) {
            long remaining = loginAttemptService.getRemainingLockoutSeconds(cleanEmail);
            throw new BadRequestException("Account temporarily locked due to repeated failed login attempts. Please try again in " + (remaining / 60 + 1) + " minutes.");
        }

        User user = userRepository.findByEmail(cleanEmail)
                .orElseThrow(() -> {
                    loginAttemptService.loginFailed(cleanEmail);
                    return new BadRequestException("Invalid email or password");
                });

        if ("SUSPENDED".equalsIgnoreCase(user.getStatus())) {
            throw new BadRequestException("This account has been suspended by administration.");
        }

        if (user.getPasswordHash() == null || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            loginAttemptService.loginFailed(cleanEmail);
            throw new BadRequestException("Invalid email or password");
        }

        // Login succeeded - reset failure count
        loginAttemptService.loginSucceeded(cleanEmail);

        String roles = user.getRoles().stream().map(Role::getName).collect(Collectors.joining(","));
        String accessToken = tokenProvider.generateTokenFromUserId(user.getId(), user.getEmail(), roles);
        String refreshToken = refreshTokenService.createRefreshToken(user, ipAddress, userAgent);

        return AuthDto.AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(86400000)
                .user(UserDto.fromEntity(user))
                .build();
    }

    // Overload for backward compatibility / tests
    @Transactional
    public AuthDto.AuthResponse login(AuthDto.LoginRequest request) {
        return login(request, "127.0.0.1", "API-Client");
    }

    @Transactional
    public AuthDto.AuthResponse refreshToken(AuthDto.RefreshTokenRequest request, String ipAddress, String userAgent) {
        return refreshTokenService.rotateRefreshToken(request.getRefreshToken(), ipAddress, userAgent);
    }

    @Transactional
    public void logout(String refreshToken) {
        refreshTokenService.revokeToken(refreshToken);
    }

    @Transactional(readOnly = true)
    public UserDto getUserProfile(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        return UserDto.fromEntity(user);
    }

    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserDto updateUserRole(UUID userId, String newRoleName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        Role role = roleRepository.findByName(newRoleName)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + newRoleName));

        user.getRoles().clear();
        user.getRoles().add(role);
        user = userRepository.save(user);

        log.info("Updated role for user {} to {}", userId, newRoleName);
        return UserDto.fromEntity(user);
    }

    @Transactional
    public UserDto updateUserStatus(UUID userId, String status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        user.setStatus(status.toUpperCase());
        user = userRepository.save(user);
        return UserDto.fromEntity(user);
    }
}
