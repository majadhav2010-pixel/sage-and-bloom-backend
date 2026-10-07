package com.example.ecommerce.admin;

import com.example.ecommerce.admin.dto.DashboardMetricsDto;
import com.example.ecommerce.admin.dto.UpdateUserRoleRequest;
import com.example.ecommerce.common.dto.ApiResponse;
import com.example.ecommerce.order.OrderService;
import com.example.ecommerce.order.OrderStatus;
import com.example.ecommerce.order.dto.OrderDto;
import com.example.ecommerce.product.ProductService;
import com.example.ecommerce.product.dto.CategoryDto;
import com.example.ecommerce.product.dto.ProductDto;
import com.example.ecommerce.user.UserService;
import com.example.ecommerce.user.dto.UserDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin Operations", description = "Endpoints for platform administration and store management")
public class AdminController {

    private final AdminService adminService;
    private final ProductService productService;
    private final OrderService orderService;
    private final UserService userService;

    // --- Dashboard & Analytics ---
    @GetMapping("/dashboard")
    @Operation(summary = "Get admin dashboard KPI metrics")
    public ResponseEntity<ApiResponse<DashboardMetricsDto>> getDashboard() {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getDashboardMetrics()));
    }

    // --- Product Management ---
    @GetMapping("/products")
    @Operation(summary = "Get all products including inactive/archived")
    public ResponseEntity<ApiResponse<List<ProductDto>>> getAllProducts() {
        return ResponseEntity.ok(ApiResponse.ok(productService.getAllProductsAdmin()));
    }

    @PostMapping("/products")
    @Operation(summary = "Create new product in catalog")
    public ResponseEntity<ApiResponse<ProductDto>> createProduct(
            @Valid @RequestBody ProductDto dto,
            HttpServletRequest request
    ) {
        ProductDto created = productService.createProduct(dto);
        adminService.recordAuditLog("PRODUCT_CREATED", "PRODUCT", created.getId().toString(),
                request.getRemoteAddr(), "Created product: " + created.getTitle());
        return ResponseEntity.ok(ApiResponse.ok("Product created successfully", created));
    }

    @PutMapping("/products/{id}")
    @Operation(summary = "Update existing product")
    public ResponseEntity<ApiResponse<ProductDto>> updateProduct(
            @PathVariable UUID id,
            @Valid @RequestBody ProductDto dto,
            HttpServletRequest request
    ) {
        ProductDto updated = productService.updateProduct(id, dto);
        adminService.recordAuditLog("PRODUCT_UPDATED", "PRODUCT", id.toString(),
                request.getRemoteAddr(), "Updated product: " + updated.getTitle());
        return ResponseEntity.ok(ApiResponse.ok("Product updated successfully", updated));
    }

    @DeleteMapping("/products/{id}")
    @Operation(summary = "Archive/deactivate product")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(
            @PathVariable UUID id,
            HttpServletRequest request
    ) {
        productService.deleteProduct(id);
        adminService.recordAuditLog("PRODUCT_DEACTIVATED", "PRODUCT", id.toString(),
                request.getRemoteAddr(), "Deactivated product UUID: " + id);
        return ResponseEntity.ok(ApiResponse.ok("Product deactivated successfully", null));
    }

    @DeleteMapping("/products/{id}/permanent")
    @Operation(summary = "Permanently delete product from database")
    public ResponseEntity<ApiResponse<Void>> hardDeleteProduct(
            @PathVariable UUID id,
            HttpServletRequest request
    ) {
        productService.hardDeleteProduct(id);
        adminService.recordAuditLog("PRODUCT_PERMANENTLY_DELETED", "PRODUCT", id.toString(),
                request.getRemoteAddr(), "Permanently removed product UUID: " + id);
        return ResponseEntity.ok(ApiResponse.ok("Product permanently removed from database", null));
    }

    // --- Categories ---
    @PostMapping("/categories")
    @Operation(summary = "Create new product category")
    public ResponseEntity<ApiResponse<CategoryDto>> createCategory(
            @Valid @RequestBody CategoryDto dto,
            HttpServletRequest request
    ) {
        CategoryDto created = productService.createCategory(dto);
        adminService.recordAuditLog("CATEGORY_CREATED", "CATEGORY", created.getId().toString(),
                request.getRemoteAddr(), "Created category: " + created.getName());
        return ResponseEntity.ok(ApiResponse.ok("Category created successfully", created));
    }

    // --- Order Management ---
    @GetMapping("/orders")
    @Operation(summary = "Get all store orders")
    public ResponseEntity<ApiResponse<List<OrderDto>>> getAllOrders() {
        return ResponseEntity.ok(ApiResponse.ok(orderService.getAllOrders()));
    }

    @GetMapping("/orders/{id}")
    @Operation(summary = "Get single order details")
    public ResponseEntity<ApiResponse<OrderDto>> getOrder(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(orderService.getOrderById(id)));
    }

    @PatchMapping("/orders/{id}/status")
    @Operation(summary = "Update order delivery and fulfillment status")
    public ResponseEntity<ApiResponse<OrderDto>> updateOrderStatus(
            @PathVariable UUID id,
            @RequestParam OrderStatus status,
            HttpServletRequest request
    ) {
        OrderDto updated = orderService.updateOrderStatus(id, status);
        adminService.recordAuditLog("ORDER_STATUS_UPDATED", "ORDER", id.toString(),
                request.getRemoteAddr(), "Status changed to: " + status);
        return ResponseEntity.ok(ApiResponse.ok("Order status updated", updated));
    }

    // --- User & Role Management (SUPER_ADMIN only for role changes) ---
    @GetMapping("/users")
    @Operation(summary = "List all customer and staff accounts")
    public ResponseEntity<ApiResponse<List<UserDto>>> getAllUsers() {
        return ResponseEntity.ok(ApiResponse.ok(userService.getAllUsers()));
    }

    @PatchMapping("/users/{id}/roles")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Modify user role (Requires SUPER_ADMIN)")
    public ResponseEntity<ApiResponse<UserDto>> updateUserRole(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserRoleRequest requestDto,
            HttpServletRequest request
    ) {
        UserDto updated = userService.updateUserRole(id, requestDto.getRoleName());
        adminService.recordAuditLog("USER_ROLE_CHANGED", "USER", id.toString(),
                request.getRemoteAddr(), "Role updated to: " + requestDto.getRoleName());
        return ResponseEntity.ok(ApiResponse.ok("User role updated successfully", updated));
    }

    @PatchMapping("/users/{id}/status")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Suspend or activate user account (Requires SUPER_ADMIN)")
    public ResponseEntity<ApiResponse<UserDto>> updateUserStatus(
            @PathVariable UUID id,
            @RequestParam String status,
            HttpServletRequest request
    ) {
        UserDto updated = userService.updateUserStatus(id, status);
        adminService.recordAuditLog("USER_STATUS_CHANGED", "USER", id.toString(),
                request.getRemoteAddr(), "Status changed to: " + status);
        return ResponseEntity.ok(ApiResponse.ok("User status updated successfully", updated));
    }

    // --- Security Audit Logs ---
    @GetMapping("/audit-logs")
    @Operation(summary = "Retrieve recent administrative security audit logs")
    public ResponseEntity<ApiResponse<List<AdminAuditLog>>> getAuditLogs() {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getAuditLogs()));
    }
}
