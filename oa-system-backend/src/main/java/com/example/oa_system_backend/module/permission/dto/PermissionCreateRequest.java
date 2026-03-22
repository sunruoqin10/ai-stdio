package com.example.oa_system_backend.module.permission.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PermissionCreateRequest {

    @NotBlank(message = "权限名称不能为空")
    @Size(max = 50, message = "权限名称最多50个字符")
    private String name;

    @NotBlank(message = "权限编码不能为空")
    @Size(max = 100, message = "权限编码最多100个字符")
    private String code;

    @NotBlank(message = "权限类型不能为空")
    private String type;

    @NotBlank(message = "所属模块不能为空")
    private String module;

    private String parentId;

    @Size(max = 200, message = "路由路径最多200个字符")
    private String path;

    @Size(max = 200, message = "组件路径最多200个字符")
    private String component;

    @Size(max = 100, message = "图标最多100个字符")
    private String icon;

    @Size(max = 200, message = "接口地址最多200个字符")
    private String apiPath;

    private String apiMethod;

    private String dataScope;

    private Integer sort;
}