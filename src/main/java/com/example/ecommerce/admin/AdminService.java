package com.example.ecommerce.admin;

import com.example.ecommerce.admin.dto.DashboardMetricsDto;
import com.example.ecommerce.order.Order;
import com.example.ecommerce.order.OrderRepository;
import com.example.ecommerce.order.dto.OrderDto;
import com.example.ecommerce.product.ProductRepository;
import com.example.ecommerce.security.SecurityUtils;
import com.example.ecommerce.user.User;
import com.example.ecommerce.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final AdminAuditLogRepository auditLogRepository;

    @Transactional(readOnly = true)
    public DashboardMetricsDto getDashboardMetrics() {
        BigDecimal totalRevenue = orderRepository.calculateTotalRevenue();
        long totalOrders = orderRepository.countTotalOrders();
        long totalCustomers = userRepository.countTotalUsers();
        long totalProducts = productRepository.countActiveProducts();
        long lowStockAlerts = productRepository.countLowStockProducts();

        List<OrderDto> recentOrders = orderRepository.findRecentOrders().stream()
                .limit(10)
                .map(OrderDto::fromEntity)
                .collect(Collectors.toList());

        return DashboardMetricsDto.builder()
                .totalRevenue(totalRevenue != null ? totalRevenue : BigDecimal.ZERO)
                .totalOrders(totalOrders)
                .totalCustomers(totalCustomers)
                .totalProducts(totalProducts)
                .lowStockAlerts(lowStockAlerts)
                .recentOrders(recentOrders)
                .build();
    }

    @Transactional(readOnly = true)
    public List<AdminAuditLog> getAuditLogs() {
        return auditLogRepository.findTop50ByOrderByTimestampDesc();
    }

    @Transactional
    public void recordAuditLog(String action, String resourceType, String resourceId, String ipAddress, String details) {
        String actorEmail = SecurityUtils.getCurrentUserEmail().orElse("SYSTEM");
        UUID actorId = SecurityUtils.getCurrentUserId().orElse(null);
        User actor = actorId != null ? userRepository.findById(actorId).orElse(null) : null;

        AdminAuditLog logEntry = AdminAuditLog.builder()
                .actor(actor)
                .actorEmail(actorEmail)
                .action(action)
                .resourceType(resourceType)
                .resourceId(resourceId)
                .ipAddress(ipAddress)
                .details(details)
                .result("SUCCESS")
                .build();

        auditLogRepository.save(logEntry);
        log.info("AUDIT: [{} by {}] on {} {}: {}", action, actorEmail, resourceType, resourceId, details);
    }
}
