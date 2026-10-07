package com.example.ecommerce;

import com.example.ecommerce.common.exception.BadRequestException;
import com.example.ecommerce.security.JwtTokenProvider;
import com.example.ecommerce.security.LoginAttemptService;
import com.example.ecommerce.user.*;
import com.example.ecommerce.user.dto.AuthDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private LoginAttemptService loginAttemptService;

    @InjectMocks
    private UserService userService;

    private Role customerRole;

    @BeforeEach
    void setUp() {
        customerRole = Role.builder().id(1L).name(Role.ROLE_CUSTOMER).build();
    }

    @Test
    @DisplayName("Given new user data, when registering, then user is created with ROLE_CUSTOMER and tokens returned")
    void testRegisterNewUserSuccess() {
        AuthDto.RegisterRequest request = new AuthDto.RegisterRequest(
                "customer@example.com", "SecurePass123!", "Jane", "Doe"
        );

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(roleRepository.findByName(Role.ROLE_CUSTOMER)).thenReturn(Optional.of(customerRole));
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPass");

        User savedUser = User.builder()
                .id(UUID.randomUUID())
                .email(request.getEmail())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .status("ACTIVE")
                .roles(Collections.singleton(customerRole))
                .build();

        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(tokenProvider.generateTokenFromUserId(any(), any(), any())).thenReturn("mockJwtToken");
        when(refreshTokenService.createRefreshToken(any(), any(), any())).thenReturn("mockRefreshToken");

        AuthDto.AuthResponse response = userService.register(request, "127.0.0.1", "JUnit-Test");

        assertNotNull(response);
        assertEquals("mockJwtToken", response.getAccessToken());
        assertEquals("mockRefreshToken", response.getRefreshToken());
        assertEquals("customer@example.com", response.getUser().getEmail());
        assertTrue(response.getUser().getRoles().contains("ROLE_CUSTOMER"));
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Given existing email, when registering, then throw BadRequestException")
    void testRegisterDuplicateEmailThrowsException() {
        AuthDto.RegisterRequest request = new AuthDto.RegisterRequest(
                "existing@example.com", "Password123!", "Alice", "Smith"
        );

        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> userService.register(request, "127.0.0.1", "JUnit-Test"));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Given locked account from brute force, login throws BadRequestException")
    void testLoginAccountLocked() {
        AuthDto.LoginRequest request = new AuthDto.LoginRequest("locked@example.com", "Pass123");
        when(loginAttemptService.isBlocked("locked@example.com")).thenReturn(true);
        when(loginAttemptService.getRemainingLockoutSeconds("locked@example.com")).thenReturn(600L);

        assertThrows(BadRequestException.class, () -> userService.login(request, "127.0.0.1", "JUnit-Test"));
        verify(userRepository, never()).findByEmail(any());
    }
}
