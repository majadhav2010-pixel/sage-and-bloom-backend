package com.example.ecommerce;

import com.example.ecommerce.common.dto.ApiResponse;
import com.example.ecommerce.user.AuthController;
import com.example.ecommerce.user.UserService;
import com.example.ecommerce.user.dto.AuthDto;
import com.example.ecommerce.user.dto.UserDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = AuthController.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                SecurityFilterAutoConfiguration.class,
                OAuth2ClientAutoConfiguration.class
        }
)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private com.example.ecommerce.security.JwtTokenProvider jwtTokenProvider;

    @MockBean
    private com.example.ecommerce.security.JwtAuthEntryPoint jwtAuthEntryPoint;

    @MockBean
    private com.example.ecommerce.security.JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private com.example.ecommerce.security.CustomOAuth2UserService customOAuth2UserService;

    @MockBean
    private com.example.ecommerce.security.OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

    @Test
    @DisplayName("POST /api/auth/register returns 200 with tokens")
    void testRegisterEndpoint() throws Exception {
        AuthDto.RegisterRequest request = new AuthDto.RegisterRequest(
                "test@example.com", "Password123!", "Jane", "Doe"
        );

        UserDto userDto = UserDto.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .firstName("Jane")
                .lastName("Doe")
                .roles(Collections.singleton("ROLE_CUSTOMER"))
                .build();

        AuthDto.AuthResponse authResponse = AuthDto.AuthResponse.builder()
                .accessToken("mock-jwt-token")
                .refreshToken("mock-refresh-token")
                .tokenType("Bearer")
                .expiresIn(86400000)
                .user(userDto)
                .build();

        when(userService.register(any(), any(), any())).thenReturn(authResponse);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("mock-jwt-token"))
                .andExpect(jsonPath("$.data.refreshToken").value("mock-refresh-token"))
                .andExpect(jsonPath("$.data.user.email").value("test@example.com"));
    }

    @Test
    @DisplayName("POST /api/auth/login returns 200 with tokens")
    void testLoginEndpoint() throws Exception {
        AuthDto.LoginRequest request = new AuthDto.LoginRequest(
                "test@example.com", "Password123!"
        );

        UserDto userDto = UserDto.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .firstName("Jane")
                .lastName("Doe")
                .roles(Collections.singleton("ROLE_CUSTOMER"))
                .build();

        AuthDto.AuthResponse authResponse = AuthDto.AuthResponse.builder()
                .accessToken("mock-jwt-token")
                .refreshToken("mock-refresh-token")
                .tokenType("Bearer")
                .expiresIn(86400000)
                .user(userDto)
                .build();

        when(userService.login(any(), any(), any())).thenReturn(authResponse);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("mock-jwt-token"));
    }
}
