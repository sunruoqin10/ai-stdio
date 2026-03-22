package com.example.oa_system_backend.module.permission.dto;

import lombok.Data;

@Data
public class PermissionQueryRequest {

    private String keyword;

    private String type;

    private String module;

    private Integer page = 1;

    private Integer pageSize = 10;
}