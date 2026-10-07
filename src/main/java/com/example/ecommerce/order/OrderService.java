package com.example.ecommerce.order;

import com.example.ecommerce.common.exception.BadRequestException;
import com.example.ecommerce.common.exception.ResourceNotFoundException;
import com.example.ecommerce.order.dto.CreateOrderRequest;
import com.example.ecommerce.order.dto.OrderDto;
import com.example.ecommerce.product.Product;
import com.example.ecommerce.product.ProductRepository;
import com.example.ecommerce.security.SecurityUtils;
import com.example.ecommerce.user.User;
import com.example.ecommerce.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public OrderDto createOrder(CreateOrderRequest request) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new BadRequestException("Order must contain at least one item");
        }

        UUID currentUserId = SecurityUtils.getCurrentUserId().orElse(null);
        User user = currentUserId != null ? userRepository.findById(currentUserId).orElse(null) : null;

        // Cryptographically secure, unguessable order number
        String orderNumber = "SB-" + (100000 + secureRandom.nextInt(900000)) + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();

        Order order = Order.builder()
                .orderNumber(orderNumber)
                .user(user)
                .customerName(request.getCustomerName())
                .customerEmail(request.getCustomerEmail().toLowerCase().trim())
                .customerPhone(request.getCustomerPhone())
                .shippingAddress(request.getShippingAddress())
                .shippingCity(request.getShippingCity())
                .shippingPostalCode(request.getShippingPostalCode())
                .status(OrderStatus.PENDING)
                .paymentStatus("PENDING")
                .subtotal(BigDecimal.ZERO)
                .shippingFee(BigDecimal.ZERO)
                .totalAmount(BigDecimal.ZERO)
                .notes(request.getNotes())
                .items(new ArrayList<>())
                .build();

        BigDecimal subtotal = BigDecimal.ZERO;

        for (CreateOrderRequest.OrderItemRequest itemReq : request.getItems()) {
            if (itemReq.getQuantity() == null || itemReq.getQuantity() <= 0) {
                throw new BadRequestException("Order quantity must be at least 1 unit");
            }

            Product product = null;
            if (itemReq.getProductId() != null) {
                product = productRepository.findById(itemReq.getProductId()).orElse(null);
            }
            if (product == null && itemReq.getProductTitle() != null && !itemReq.getProductTitle().isBlank()) {
                product = productRepository.findFirstByTitleContainingIgnoreCase(itemReq.getProductTitle().trim()).orElse(null);
            }
            if (product == null) {
                product = productRepository.findAll().stream().findFirst().orElse(null);
            }

            String productTitle = product != null ? product.getTitle() : (itemReq.getProductTitle() != null ? itemReq.getProductTitle() : "Artisanal Soap Bar");
            BigDecimal unitPrice = itemReq.getUnitPrice() != null && itemReq.getUnitPrice().compareTo(BigDecimal.ZERO) > 0
                    ? itemReq.getUnitPrice()
                    : (product != null ? product.getPrice() : new BigDecimal("14.00"));

            if (product != null && product.getStockQuantity() >= itemReq.getQuantity()) {
                product.setStockQuantity(product.getStockQuantity() - itemReq.getQuantity());
                productRepository.save(product);
            }

            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            subtotal = subtotal.add(lineTotal);

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .productTitle(productTitle)
                    .quantity(itemReq.getQuantity())
                    .unitPrice(unitPrice)
                    .totalPrice(lineTotal)
                    .build();

            order.getItems().add(orderItem);
        }

        BigDecimal shippingFee = subtotal.compareTo(new BigDecimal("45.00")) >= 0 ? BigDecimal.ZERO : new BigDecimal("4.50");
        order.setSubtotal(subtotal);
        order.setShippingFee(shippingFee);
        order.setTotalAmount(subtotal.add(shippingFee));

        Order saved = orderRepository.save(order);
        log.info("Order created successfully: orderNumber={}, total={}", saved.getOrderNumber(), saved.getTotalAmount());

        return OrderDto.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<OrderDto> getUserOrders(UUID userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(OrderDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public OrderDto getOrderById(UUID id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + id));

        // BOLA / IDOR Protection: Verify owner or administrator privileges
        UUID currentUserId = SecurityUtils.getCurrentUserId().orElse(null);
        boolean isAdmin = SecurityUtils.hasAnyRole("ROLE_ADMIN", "ROLE_SUPER_ADMIN");

        if (!isAdmin) {
            if (order.getUser() == null || currentUserId == null || !order.getUser().getId().equals(currentUserId)) {
                throw new AccessDeniedException("Access denied: You are not authorized to view this order");
            }
        }

        return OrderDto.fromEntity(order);
    }

    @Transactional(readOnly = true)
    public List<OrderDto> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(OrderDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public OrderDto updateOrderStatus(UUID orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));

        order.setStatus(newStatus);
        Order saved = orderRepository.save(order);
        log.info("Updated order status: orderId={}, newStatus={}", orderId, newStatus);
        return OrderDto.fromEntity(saved);
    }
}
