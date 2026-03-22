package com.example.oa_system_backend.module.permission.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class AssignRoleRequest {

    @NotNull(message = "用户ID不能为空")
    private String userId;

    @NotEmpty(message = "角色ID列表不能为空")
    private List<String> roleIds;
}