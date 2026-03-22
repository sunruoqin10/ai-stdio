package com.example.oa_system_backend.module.permission.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CopyRoleRequest {

    @NotBlank(message = "源角色ID不能为空")
    private String sourceId;

    @NotBlank(message = "新角色名称不能为空")
    @Size(max = 50, message = "角色名称最多50个字符")
    private String name;
}