package com.example.oa_system_backend.module.permission.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class UpdateRolePermissionsRequest {

    @NotNull(message = "权限ID列表不能为空")
    private List<String> permissionIds;
}