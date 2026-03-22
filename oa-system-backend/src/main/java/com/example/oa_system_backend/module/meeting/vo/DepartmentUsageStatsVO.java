package com.example.oa_system_backend.module.meeting.vo;

import lombok.Data;

@Data
public class DepartmentUsageStatsVO {
    private String departmentId;
    private String departmentName;
    private Integer bookingCount;
    private Integer totalHours;
}
