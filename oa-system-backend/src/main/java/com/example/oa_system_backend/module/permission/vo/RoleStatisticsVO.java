package com.example.oa_system_backend.module.permission.vo;

import lombok.Data;

@Data
public class RoleStatisticsVO {
    private Integer total;
    private Integer systemCount;
    private Integer customCount;
    private Integer activeCount;
}