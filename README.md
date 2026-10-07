# Sage & Bloom E-Commerce Platform — Backend

Enterprise-grade e-commerce backend built with **Java 21**, **Spring Boot 3.3.4**, **Spring Security + OAuth 2.0 / OIDC**, **PostgreSQL**, **Flyway**, and **Docker**.

---

## Technology Stack

| Layer | Technology |
| :--- | :--- |
| **Runtime & Language** | Java 21 (LTS) |
| **Framework** | Spring Boot 3.3.4 |
| **Security & Auth** | Spring Security 6, OAuth 2.0 / OpenID Connect (Google), Stateless JWT |
| **Authorization** | Strict Role-Based Access Control (`CUSTOMER`, `ADMIN`, `SUPER_ADMIN`) |
| **Database** | PostgreSQL 16 |
| **ORM & Data** | Spring Data JPA + Hibernate |
| **Migrations** | Flyway (`db/migration/`) |
| **API Docs** | Springdoc OpenAPI 3 / Swagger UI |
| **Testing** | JUnit 5, Mockito, Testcontainers |
| **Build & Packaging** | Maven & Multi-Stage Docker |
| **CI/CD** | GitHub Actions |

---

## Project Structure

```
backend/
├── src/main/java/com/example/ecommerce/
│   ├── EcommerceApplication.java
│   │
│   ├── config/
│   │   ├── SecurityConfig.java         # RBAC, Stateless JWT & OAuth2
│   │   ├── CorsConfig.java             # Cross-origin policy
│   │   └── OpenApiConfig.java          # Swagger documentation setup
│   │
│   ├── security/
│   │   ├── CustomOAuth2UserService.java # Provision Google OAuth users
│   │   ├── OAuth2LoginSuccessHandler.java # Issue JWT & redirect
│   │   ├── JwtTokenProvider.java       # Sign & validate tokens
│   │   ├── JwtAuthenticationFilter.java # Populate SecurityContext
│   │   ├── CustomUserPrincipal.java    # UserDetails + OAuth2User
│   │   └── SecurityUtils.java          # Context helpers
│   │
│   ├── user/
│   │   ├── User.java, Role.java, OAuthAccount.java
│   │   ├── UserRepository.java, RoleRepository.java
│   │   ├── UserService.java, AuthController.java, UserController.java
│   │   └── dto/ (AuthDto, UserDto)
│   │
│   ├── product/
│   │   ├── Product.java, Category.java
│   │   ├── ProductRepository.java, CategoryRepository.java
│   │   ├── ProductService.java, ProductController.java
│   │   └── dto/ (ProductDto, CategoryDto)
│   │
│   ├── order/
│   │   ├── Order.java, OrderItem.java, OrderStatus.java
│   │   ├── OrderRepository.java, OrderItemRepository.java
│   │   ├── OrderService.java, OrderController.java
│   │   └── dto/ (OrderDto, CreateOrderRequest)
│   │
│   ├── admin/
│   │   ├── AdminAuditLog.java, AdminAuditLogRepository.java
│   │   ├── AdminService.java, AdminController.java
│   │   └── dto/ (DashboardMetricsDto, UpdateUserRoleRequest)
│   │
│   └── common/
│       ├── exception/ (GlobalExceptionHandler, ResourceNotFoundException, ...)
│       └── dto/ (ApiResponse, PageResponse)
│
├── src/main/resources/
│   ├── application.yml
│   └── db/migration/
│       ├── V1__create_users.sql
│       ├── V2__create_roles_and_user_roles.sql
│       ├── V3__create_oauth_accounts.sql
│       ├── V4__create_categories_and_products.sql
│       ├── V5__create_orders_and_order_items.sql
│       ├── V6__create_admin_audit_logs.sql
│       └── V7__seed_initial_data.sql
│
├── Dockerfile
└── pom.xml
```

---

## Quick Start with Docker

To spin up PostgreSQL, Spring Boot Backend, and Frontend Nginx in one command:

```bash
docker-compose up --build
```

- **Frontend & Admin Panel**: `http://localhost:3000` (or `http://localhost`)
- **Backend REST API**: `http://localhost:8080/api`
- **Swagger Interactive API Documentation**: `http://localhost:8080/swagger-ui.html`

---

## API Endpoints Matrix

### Public Endpoints
- `POST /api/auth/register` — Register a customer account
- `POST /api/auth/login` — Login with email/password
- `GET /api/products` — Browse products (supports search parameter `?search=lavender`)
- `GET /api/products/{id}` — Get single product
- `GET /api/products/slug/{slug}` — Get product by slug
- `GET /api/categories` — List botanical collections
- `POST /api/orders/checkout` — Submit order

### Authenticated Customer Endpoints
- `GET /api/users/me` — Current profile
- `GET /api/orders/my-orders` — Customer order history

### Admin Endpoints (`ROLE_ADMIN`, `ROLE_SUPER_ADMIN`)
- `GET /api/admin/dashboard` — Live KPI metrics & recent orders
- `GET /api/admin/products` — All products including inactive
- `POST /api/admin/products` — Create new product
- `PUT /api/admin/products/{id}` — Update product details
- `DELETE /api/admin/products/{id}` — Deactivate/archive product
- `POST /api/admin/categories` — Create category
- `GET /api/admin/orders` — All orders
- `PATCH /api/admin/orders/{id}/status` — Transition order lifecycle
- `GET /api/admin/users` — List all registered users
- `PATCH /api/admin/users/{id}/roles` — Update user roles (Requires `SUPER_ADMIN`)
- `PATCH /api/admin/users/{id}/status` — Suspend/activate account (Requires `SUPER_ADMIN`)
- `GET /api/admin/audit-logs` — Immutable audit log trail

---

## Running Tests

```bash
mvn clean test
```
