package com.example.oa_system_backend.module.asset.vo;

import lombok.Data;

@Data
public class BorrowTrendVO {
    private String year;
    private String month;
    private Integer borrowCount;
    private Integer returnCount;
    private Integer activeCount;
    private Integer overdueCount;
}