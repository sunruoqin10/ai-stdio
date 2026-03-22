package com.example.oa_system_backend.module.permission.dto;

import lombok.Data;

@Data
public class RoleQueryRequest {

    private String keyword;

    private String status;

    private String type;

    private Integer page = 1;

    private Integer pageSize = 10;
}