package com.example.oa_system_backend.module.permission.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RoleCreateRequest {

    @NotBlank(message = "角色名称不能为空")
    @Size(max = 50, message = "角色名称最多50个字符")
    private String name;

    @NotBlank(message = "角色编码不能为空")
    @Size(max = 50, message = "角色编码最多50个字符")
    private String code;

    @NotBlank(message = "角色类型不能为空")
    private String type;

    private Integer sort;

    @Size(max = 500, message = "角色描述最多500个字符")
    private String description;

    private String status;
}