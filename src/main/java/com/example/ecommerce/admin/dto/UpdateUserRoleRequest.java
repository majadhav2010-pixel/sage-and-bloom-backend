package com.example.ecommerce.admin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRoleRequest {

    @NotBlank(message = "Role name is required (e.g., ROLE_CUSTOMER, ROLE_ADMIN, ROLE_SUPER_ADMIN)")
    private String roleName;
}
