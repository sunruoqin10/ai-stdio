package com.example.oa_system_backend.module.expense.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ExpenseQueryRequest {

    private Integer page = 1;

    private Integer pageSize = 10;

    private String applicantId;

    private String departmentId;

    private String type;

    private String status;

    private LocalDate applyDateStart;

    private LocalDate applyDateEnd;

    private String keyword;
}
