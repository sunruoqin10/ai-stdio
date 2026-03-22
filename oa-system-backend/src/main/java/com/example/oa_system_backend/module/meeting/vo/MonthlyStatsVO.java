package com.example.oa_system_backend.module.meeting.vo;

import lombok.Data;

@Data
public class MonthlyStatsVO {
    private Integer year;
    private Integer month;
    private Integer bookingCount;
    private Integer totalHours;
    private Double growthRate;
}
