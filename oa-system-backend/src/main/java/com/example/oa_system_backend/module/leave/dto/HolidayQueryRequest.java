package com.example.oa_system_backend.module.leave.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class HolidayQueryRequest {

    private Integer year;

    private String type;

    private LocalDate startDate;

    private LocalDate endDate;

    private String keyword;
}
