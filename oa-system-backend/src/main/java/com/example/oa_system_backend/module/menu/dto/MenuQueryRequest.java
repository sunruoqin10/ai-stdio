package com.example.oa_system_backend.module.menu.dto;

import lombok.Data;

@Data
public class MenuQueryRequest {

    private Integer page = 1;

    private Integer pageSize = 10;

    private String type;

    private String status;

    private String keyword;
}
