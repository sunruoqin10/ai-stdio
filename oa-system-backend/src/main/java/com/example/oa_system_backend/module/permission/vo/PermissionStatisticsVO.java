package com.example.oa_system_backend.module.permission.vo;

import lombok.Data;

@Data
public class PermissionStatisticsVO {
    private Integer total;
    private Integer menuCount;
    private Integer buttonCount;
    private Integer apiCount;
    private Integer dataCount;
}