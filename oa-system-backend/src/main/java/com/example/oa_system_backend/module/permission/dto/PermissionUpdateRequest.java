package com.example.oa_system_backend.module.permission.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PermissionUpdateRequest {

    @Size(max = 50, message = "权限名称最多50个字符")
    private String name;

    @Size(max = 100, message = "权限编码最多100个字符")
    private String code;

    private String type;

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

    private String status;
}