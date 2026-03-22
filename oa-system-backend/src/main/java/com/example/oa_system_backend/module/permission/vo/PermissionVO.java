package com.example.oa_system_backend.module.permission.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class PermissionVO {
    private String id;
    private String name;
    private String code;
    private String type;
    private String module;
    private String parentId;
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
    private List<PermissionVO> children;
}