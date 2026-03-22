package com.example.oa_system_backend.module.permission.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class RoleVO {
    private String id;
    private String name;
    private String code;
    private String type;
    private Integer sort;
    private String description;
    private String status;
    private Boolean isPreset;
    private Integer userCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}