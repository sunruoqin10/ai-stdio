package com.example.oa_system_backend.module.expense.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ExpenseStatisticsVO {

    private BigDecimal totalAmount;

    private Integer count;

    private BigDecimal avgAmount;

    private BigDecimal paidAmount;

    private BigDecimal rejectedAmount;

    private Double percentage;
}
