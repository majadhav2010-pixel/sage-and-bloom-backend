package com.example.ecommerce.product.dto;

import com.example.ecommerce.product.Product;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductDto {

    private UUID id;

    @JsonProperty("title")
    @JsonAlias({"name", "productName", "product_name"})
    @NotBlank(message = "Product title is required")
    private String title;

    private String subtitle;
    private String slug;

    @JsonProperty("categoryId")
    @JsonAlias({"category_id"})
    private Long categoryId;

    @JsonProperty("categoryName")
    @JsonAlias({"category", "cat"})
    private String categoryName;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private BigDecimal price;

    @JsonProperty("originalPrice")
    @JsonAlias({"original_price"})
    private BigDecimal originalPrice;

    @JsonProperty("weightGrams")
    @JsonAlias({"weight", "weight_grams"})
    private Integer weightGrams;

    @JsonProperty("stockQuantity")
    @JsonAlias({"stock", "stock_quantity", "stockCount"})
    @Min(value = 0, message = "Stock quantity cannot be negative")
    @Builder.Default
    private Integer stockQuantity = 50;

    @JsonProperty("imageUrl")
    @JsonAlias({"img", "image", "image_url"})
    @NotBlank(message = "Image URL is required")
    private String imageUrl;

    @JsonProperty("badge")
    @JsonAlias({"tag"})
    private String badge;

    private BigDecimal rating;
    private Integer reviewCount;
    private String scentProfile;
    private String ingredients;
    private String skinType;
    private String description;

    @JsonProperty("isActive")
    @JsonAlias({"active", "is_active"})
    private Boolean isActive;

    private Instant createdAt;

    public static ProductDto fromEntity(Product product) {
        return ProductDto.builder()
                .id(product.getId())
                .title(product.getTitle())
                .subtitle(product.getSubtitle())
                .slug(product.getSlug())
                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .price(product.getPrice())
                .originalPrice(product.getOriginalPrice())
                .weightGrams(product.getWeightGrams())
                .stockQuantity(product.getStockQuantity())
                .imageUrl(product.getImageUrl())
                .badge(product.getBadge())
                .rating(product.getRating())
                .reviewCount(product.getReviewCount())
                .scentProfile(product.getScentProfile())
                .ingredients(product.getIngredients())
                .skinType(product.getSkinType())
                .description(product.getDescription())
                .isActive(product.getIsActive())
                .createdAt(product.getCreatedAt())
                .build();
    }
}
