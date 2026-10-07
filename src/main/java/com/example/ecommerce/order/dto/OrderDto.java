package com.example.ecommerce.order.dto;

import com.example.ecommerce.order.Order;
import com.example.ecommerce.order.OrderItem;
import com.example.ecommerce.order.OrderStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderDto {

    private UUID id;
    private String orderNumber;
    private UUID userId;
    private String customerEmail;
    private String customerName;
    private String customerInitials;
    private String customerPhone;
    private String shippingAddress;
    private String shippingCity;
    private String shippingPostalCode;
    private String type;
    private Boolean flagged;
    private String date;
    private OrderStatus status;
    private String paymentStatus;
    private BigDecimal subtotal;
    private BigDecimal shippingFee;
    private BigDecimal totalAmount;
    private String notes;
    private String internalNotes;
    private List<String> productThumbnails;
    private List<OrderItemDto> items;
    private Instant createdAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OrderItemDto {
        private Long id;
        private UUID productId;
        private String productTitle;
        private String title;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal totalPrice;
        private String image;

        public static OrderItemDto fromEntity(OrderItem item) {
            String img = item.getProduct() != null && item.getProduct().getImageUrl() != null ?
                    item.getProduct().getImageUrl() : "/images/slide_1.jpg";
            return OrderItemDto.builder()
                    .id(item.getId())
                    .productId(item.getProduct() != null ? item.getProduct().getId() : null)
                    .productTitle(item.getProductTitle())
                    .title(item.getProductTitle())
                    .quantity(item.getQuantity())
                    .unitPrice(item.getUnitPrice())
                    .totalPrice(item.getTotalPrice())
                    .image(img)
                    .build();
        }
    }

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd")
            .withZone(ZoneId.of("UTC"));

    public static OrderDto fromEntity(Order order) {
        String initials = "SB";
        if (order.getCustomerName() != null && !order.getCustomerName().isBlank()) {
            String[] parts = order.getCustomerName().trim().split("\\s+");
            if (parts.length >= 2) {
                initials = ("" + parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase();
            } else if (parts.length == 1 && parts[0].length() > 0) {
                initials = ("" + parts[0].charAt(0)).toUpperCase();
            }
        }

        String orderType = "Shipping";
        if (order.getShippingAddress() != null &&
                (order.getShippingAddress().toLowerCase().contains("pickup") ||
                 order.getShippingAddress().toLowerCase().contains("hub"))) {
            orderType = "Pickups";
        }

        String formattedDate = "Today";
        if (order.getCreatedAt() != null) {
            try {
                formattedDate = DATE_FORMATTER.format(order.getCreatedAt());
            } catch (Exception e) {
                formattedDate = order.getCreatedAt().toString().substring(0, 10);
            }
        }

        List<OrderItemDto> itemDtos = order.getItems() != null ?
                order.getItems().stream().map(OrderItemDto::fromEntity).collect(Collectors.toList()) :
                new ArrayList<>();

        List<String> thumbnails = itemDtos.stream()
                .map(OrderItemDto::getImage)
                .filter(img -> img != null && !img.isBlank())
                .distinct()
                .collect(Collectors.toList());

        if (thumbnails.isEmpty()) {
            thumbnails = Arrays.asList("/images/slide_1.jpg");
        }

        boolean isFlagged = order.getTotalAmount() != null &&
                order.getTotalAmount().compareTo(BigDecimal.valueOf(300)) > 0;

        return OrderDto.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .userId(order.getUser() != null ? order.getUser().getId() : null)
                .customerEmail(order.getCustomerEmail())
                .customerName(order.getCustomerName())
                .customerInitials(initials)
                .customerPhone(order.getCustomerPhone())
                .shippingAddress(order.getShippingAddress())
                .shippingCity(order.getShippingCity())
                .shippingPostalCode(order.getShippingPostalCode())
                .type(orderType)
                .flagged(isFlagged)
                .date(formattedDate)
                .status(order.getStatus())
                .paymentStatus(order.getPaymentStatus())
                .subtotal(order.getSubtotal())
                .shippingFee(order.getShippingFee())
                .totalAmount(order.getTotalAmount())
                .notes(order.getNotes())
                .internalNotes(order.getNotes())
                .productThumbnails(thumbnails)
                .items(itemDtos)
                .createdAt(order.getCreatedAt())
                .build();
    }
}
