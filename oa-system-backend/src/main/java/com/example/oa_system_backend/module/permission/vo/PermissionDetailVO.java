package com.example.oa_system_backend.module.permission.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PermissionDetailVO {
    private String id;
    private String name;
    private String code;
    private String type;
    private String module;
    private String parentId;
    private String parentName;
    private String path;
    private String component;
    private String icon;
    private String apiPath;
    private String apiMethod;
    private String dataScope;
    private Integer sort;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}