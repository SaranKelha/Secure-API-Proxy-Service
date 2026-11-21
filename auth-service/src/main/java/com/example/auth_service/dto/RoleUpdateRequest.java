package com.example.auth_service.dto;

import com.example.auth_service.entity.Role;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoleUpdateRequest {
    @NotNull(message = "Role is required")
    private Role role;

}
