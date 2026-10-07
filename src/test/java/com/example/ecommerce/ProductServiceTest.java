package com.example.ecommerce;

import com.example.ecommerce.product.*;
import com.example.ecommerce.product.dto.ProductDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    @DisplayName("When fetching active products, returns product DTO list")
    void testGetAllActiveProducts() {
        Product mockProduct = Product.builder()
                .id(UUID.randomUUID())
                .title("Woodland Lavender Bar")
                .slug("woodland-lavender-bar")
                .price(new BigDecimal("14.00"))
                .stockQuantity(30)
                .imageUrl("/images/slide_2.jpg")
                .isActive(true)
                .build();

        when(productRepository.findByIsActiveTrue()).thenReturn(Collections.singletonList(mockProduct));

        List<ProductDto> products = productService.getAllActiveProducts();

        assertEquals(1, products.size());
        assertEquals("Woodland Lavender Bar", products.get(0).getTitle());
        verify(productRepository, times(1)).findByIsActiveTrue();
    }
}
