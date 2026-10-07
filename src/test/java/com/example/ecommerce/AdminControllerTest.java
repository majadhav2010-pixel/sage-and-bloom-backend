package com.example.ecommerce;

import com.example.ecommerce.admin.AdminController;
import com.example.ecommerce.admin.AdminService;
import com.example.ecommerce.admin.dto.DashboardMetricsDto;
import com.example.ecommerce.admin.dto.UpdateUserRoleRequest;
import com.example.ecommerce.admin.AdminAuditLog;
import com.example.ecommerce.product.ProductService;
import com.example.ecommerce.order.OrderService;
import com.example.ecommerce.user.UserService;
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

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = AdminController.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                SecurityFilterAutoConfiguration.class,
                OAuth2ClientAutoConfiguration.class
        }
)
@AutoConfigureMockMvc(addFilters = false)
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AdminService adminService;

    @MockBean
    private ProductService productService;

    @MockBean
    private OrderService orderService;

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
    @DisplayName("GET /api/admin/dashboard returns 200 with aggregated metrics")
    void testGetDashboardMetrics() throws Exception {
        DashboardMetricsDto metrics = DashboardMetricsDto.builder()
                .totalRevenue(new BigDecimal("128450.00"))
                .totalOrders(342)
                .totalCustomers(189)
                .totalProducts(24)
                .lowStockAlerts(3)
                .recentOrders(Collections.emptyList())
                .build();

        when(adminService.getDashboardMetrics()).thenReturn(metrics);

        mockMvc.perform(get("/api/admin/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalRevenue").value(128450.00))
                .andExpect(jsonPath("$.data.totalOrders").value(342));
    }

    @Test
    @DisplayName("GET /api/admin/audit-logs returns 200 with log entries")
    void testGetAuditLogs() throws Exception {
        AdminAuditLog logEntry = AdminAuditLog.builder()
                .id(UUID.randomUUID())
                .actorEmail("admin@sageandbloom.com")
                .action("ORDER_STATUS_UPDATE")
                .resourceType("ORDER")
                .resourceId("SB-9412")
                .ipAddress("127.0.0.1")
                .details("Updated status to DELIVERED")
                .result("SUCCESS")
                .timestamp(Instant.now())
                .build();

        when(adminService.getAuditLogs()).thenReturn(List.of(logEntry));

        mockMvc.perform(get("/api/admin/audit-logs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].action").value("ORDER_STATUS_UPDATE"))
                .andExpect(jsonPath("$.data[0].actorEmail").value("admin@sageandbloom.com"));
    }

    @Test
    @DisplayName("PATCH /api/admin/users/{userId}/roles updates user role")
    void testUpdateUserRole() throws Exception {
        UUID userId = UUID.randomUUID();
        UserDto userDto = UserDto.builder()
                .id(userId)
                .email("clara@example.com")
                .firstName("Clara")
                .lastName("Oswald")
                .roles(Collections.singleton("ROLE_ADMIN"))
                .build();

        when(userService.updateUserRole(any(), any())).thenReturn(userDto);

        mockMvc.perform(patch("/api/admin/users/" + userId + "/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"roleName\":\"ROLE_ADMIN\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("clara@example.com"));
    }
}
