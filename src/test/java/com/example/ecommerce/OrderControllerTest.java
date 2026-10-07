package com.example.ecommerce;

import com.example.ecommerce.order.OrderController;
import com.example.ecommerce.order.OrderService;
import com.example.ecommerce.order.dto.CreateOrderRequest;
import com.example.ecommerce.order.dto.OrderDto;
import com.example.ecommerce.order.OrderStatus;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = OrderController.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                SecurityFilterAutoConfiguration.class,
                OAuth2ClientAutoConfiguration.class
        }
)
@AutoConfigureMockMvc(addFilters = false)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderService orderService;

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
    @DisplayName("POST /api/orders/checkout creates order and returns 200")
    void testCheckoutEndpoint() throws Exception {
        CreateOrderRequest request = CreateOrderRequest.builder()
                .customerEmail("customer@example.com")
                .customerName("John Doe")
                .customerPhone("+91 9876543210")
                .shippingAddress("42 Lotus Blossom Lane")
                .shippingCity("Bengaluru")
                .shippingPostalCode("560038")
                .items(List.of(
                        new CreateOrderRequest.OrderItemRequest(UUID.randomUUID(), 2)
                ))
                .build();

        OrderDto response = OrderDto.builder()
                .id(UUID.randomUUID())
                .orderNumber("SB-9412")
                .customerEmail("customer@example.com")
                .customerName("John Doe")
                .shippingAddress("42 Lotus Blossom Lane")
                .shippingCity("Bengaluru")
                .status(OrderStatus.PENDING)
                .paymentStatus("UNPAID")
                .subtotal(new BigDecimal("598.00"))
                .shippingFee(BigDecimal.ZERO)
                .totalAmount(new BigDecimal("598.00"))
                .createdAt(Instant.now())
                .items(Collections.emptyList())
                .build();

        when(orderService.createOrder(any())).thenReturn(response);

        mockMvc.perform(post("/api/orders/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.orderNumber").value("SB-9412"))
                .andExpect(jsonPath("$.data.customerEmail").value("customer@example.com"));
    }

    @Test
    @DisplayName("GET /api/orders/{id} returns single order")
    void testGetOrderByIdEndpoint() throws Exception {
        UUID orderId = UUID.randomUUID();
        OrderDto response = OrderDto.builder()
                .id(orderId)
                .orderNumber("SB-8750")
                .customerEmail("customer@example.com")
                .customerName("John Doe")
                .shippingAddress("42 Lotus Blossom Lane")
                .shippingCity("Bengaluru")
                .status(OrderStatus.DELIVERED)
                .paymentStatus("PAID")
                .subtotal(new BigDecimal("630.00"))
                .shippingFee(BigDecimal.ZERO)
                .totalAmount(new BigDecimal("630.00"))
                .createdAt(Instant.now())
                .items(Collections.emptyList())
                .build();

        when(orderService.getOrderById(orderId)).thenReturn(response);

        mockMvc.perform(get("/api/orders/" + orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.orderNumber").value("SB-8750"))
                .andExpect(jsonPath("$.data.status").value("DELIVERED"));
    }
}
