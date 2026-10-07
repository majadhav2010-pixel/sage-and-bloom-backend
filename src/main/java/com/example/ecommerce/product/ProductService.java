package com.example.ecommerce.product;

import com.example.ecommerce.common.exception.BadRequestException;
import com.example.ecommerce.common.exception.ResourceNotFoundException;
import com.example.ecommerce.product.dto.CategoryDto;
import com.example.ecommerce.product.dto.ProductDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    private static final Pattern NONLATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");

    public static String toSlug(String input) {
        String nowhitespace = WHITESPACE.matcher(input).replaceAll("-");
        String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);
        String slug = NONLATIN.matcher(normalized).replaceAll("");
        return slug.toLowerCase(Locale.ENGLISH);
    }

    @Transactional(readOnly = true)
    public List<ProductDto> getAllActiveProducts() {
        return productRepository.findByIsActiveTrue().stream()
                .map(ProductDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProductDto> getAllProductsAdmin() {
        return productRepository.findAll().stream()
                .map(ProductDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProductDto getProductById(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
        return ProductDto.fromEntity(product);
    }

    @Transactional(readOnly = true)
    public ProductDto getProductBySlug(String slug) {
        Product product = productRepository.findBySlugAndIsActiveTrue(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with slug: " + slug));
        return ProductDto.fromEntity(product);
    }

    @Transactional(readOnly = true)
    public List<ProductDto> searchProducts(String query) {
        if (query == null || query.isBlank()) {
            return getAllActiveProducts();
        }
        return productRepository.searchProducts(query).stream()
                .map(ProductDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProductDto createProduct(ProductDto dto) {
        String baseSlug = dto.getSlug() != null && !dto.getSlug().isBlank() ?
                toSlug(dto.getSlug()) : toSlug(dto.getTitle());
        String uniqueSlug = baseSlug + "-" + UUID.randomUUID().toString().substring(0, 8);

        Category category = null;
        if (dto.getCategoryId() != null) {
            category = categoryRepository.findById(dto.getCategoryId()).orElse(null);
        }
        if (category == null && dto.getCategoryName() != null && !dto.getCategoryName().isBlank()) {
            category = categoryRepository.findByName(dto.getCategoryName())
                    .orElseGet(() -> categoryRepository.findBySlug(toSlug(dto.getCategoryName()))
                            .orElse(null));
        }
        if (category == null && dto.getCategoryName() != null && !dto.getCategoryName().isBlank()) {
            // Auto-create category if new category name is used
            try {
                Category newCat = Category.builder()
                        .name(dto.getCategoryName().trim())
                        .slug(toSlug(dto.getCategoryName().trim()))
                        .description("Handcrafted botanical " + dto.getCategoryName().trim())
                        .build();
                category = categoryRepository.save(newCat);
            } catch (Exception e) {
                log.warn("Category creation fallback on conflict: {}", e.getMessage());
                category = categoryRepository.findAll().stream().findFirst().orElse(null);
            }
        }
        if (category == null) {
            category = categoryRepository.findAll().stream().findFirst().orElse(null);
        }

        Product product = Product.builder()
                .title(dto.getTitle())
                .subtitle(dto.getSubtitle())
                .slug(uniqueSlug)
                .category(category)
                .price(dto.getPrice())
                .originalPrice(dto.getOriginalPrice())
                .weightGrams(dto.getWeightGrams() != null ? dto.getWeightGrams() : 200)
                .stockQuantity(dto.getStockQuantity() != null ? dto.getStockQuantity() : 50)
                .imageUrl(dto.getImageUrl())
                .badge(dto.getBadge())
                .rating(dto.getRating() != null ? dto.getRating() : java.math.BigDecimal.valueOf(5.0))
                .reviewCount(dto.getReviewCount() != null ? dto.getReviewCount() : 0)
                .scentProfile(dto.getScentProfile())
                .ingredients(dto.getIngredients())
                .skinType(dto.getSkinType())
                .description(dto.getDescription())
                .isActive(dto.getIsActive() != null ? dto.getIsActive() : true)
                .build();

        product = productRepository.save(product);
        log.info("Created new product in database: id={}, title={}, category={}", 
                product.getId(), product.getTitle(), category != null ? category.getName() : "None");
        return ProductDto.fromEntity(product);
    }

    @Transactional
    public ProductDto updateProduct(UUID id, ProductDto dto) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));

        Category category = null;
        if (dto.getCategoryId() != null) {
            category = categoryRepository.findById(dto.getCategoryId()).orElse(null);
        }
        if (category == null && dto.getCategoryName() != null && !dto.getCategoryName().isBlank()) {
            category = categoryRepository.findByName(dto.getCategoryName())
                    .orElseGet(() -> categoryRepository.findBySlug(toSlug(dto.getCategoryName()))
                            .orElse(null));
        }
        if (category != null) {
            product.setCategory(category);
        }

        if (dto.getTitle() != null && !dto.getTitle().isBlank()) product.setTitle(dto.getTitle());
        if (dto.getSubtitle() != null) product.setSubtitle(dto.getSubtitle());
        if (dto.getPrice() != null) product.setPrice(dto.getPrice());
        if (dto.getOriginalPrice() != null) product.setOriginalPrice(dto.getOriginalPrice());
        if (dto.getWeightGrams() != null) product.setWeightGrams(dto.getWeightGrams());
        if (dto.getStockQuantity() != null) product.setStockQuantity(dto.getStockQuantity());
        if (dto.getImageUrl() != null && !dto.getImageUrl().isBlank()) product.setImageUrl(dto.getImageUrl());
        if (dto.getBadge() != null) product.setBadge(dto.getBadge());
        if (dto.getScentProfile() != null) product.setScentProfile(dto.getScentProfile());
        if (dto.getIngredients() != null) product.setIngredients(dto.getIngredients());
        if (dto.getSkinType() != null) product.setSkinType(dto.getSkinType());
        if (dto.getDescription() != null) product.setDescription(dto.getDescription());
        if (dto.getIsActive() != null) {
            product.setIsActive(dto.getIsActive());
        }

        product = productRepository.save(product);
        log.info("Updated product in database: id={}", product.getId());
        return ProductDto.fromEntity(product);
    }

    @Transactional
    public void deleteProduct(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
        // Soft delete / deactivate
        product.setIsActive(false);
        productRepository.save(product);
        log.info("Deactivated product: id={}", id);
    }

    @Transactional
    public void hardDeleteProduct(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
        productRepository.delete(product);
        log.info("Permanently removed product from database: id={}, title={}", id, product.getTitle());
    }

    @Transactional(readOnly = true)
    public List<CategoryDto> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(CategoryDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public CategoryDto createCategory(CategoryDto dto) {
        String slug = dto.getSlug() != null && !dto.getSlug().isBlank() ?
                toSlug(dto.getSlug()) : toSlug(dto.getName());

        Category category = Category.builder()
                .name(dto.getName())
                .slug(slug)
                .description(dto.getDescription())
                .build();

        category = categoryRepository.save(category);
        return CategoryDto.fromEntity(category);
    }
}
