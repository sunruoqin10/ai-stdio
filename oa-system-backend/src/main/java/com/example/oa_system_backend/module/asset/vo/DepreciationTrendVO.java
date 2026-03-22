package com.example.oa_system_backend.module.asset.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DepreciationTrendVO {
    private String year;
    private String month;
    private BigDecimal originalValue;
    private BigDecimal depreciationAmount;
    private BigDecimal currentValue;
    private BigDecimal depreciationRate;
}