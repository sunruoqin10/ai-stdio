package com.example.oa_system_backend.module.leave.dto;

import lombok.Data;

@Data
public class BalanceQueryRequest {

    private String employeeId;

    private Integer year;

    private String departmentId;

    private Integer page = 1;

    private Integer pageSize = 10;
}
