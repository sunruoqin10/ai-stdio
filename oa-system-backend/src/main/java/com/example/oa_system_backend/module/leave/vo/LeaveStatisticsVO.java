package com.example.oa_system_backend.module.leave.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class LeaveStatisticsVO {

    private Integer totalRequests;

    private BigDecimal totalDuration;

    private Map<String, Integer> byType;

    private Map<String, Integer> byStatus;

    private List<MonthlyData> monthlyData;

    private List<DepartmentData> departmentData;

    private List<EmployeeData> employeeData;

    @Data
    public static class MonthlyData {
        private String month;
        private Integer count;
        private BigDecimal duration;
    }

    @Data
    public static class DepartmentData {
        private String departmentId;
        private String departmentName;
        private Integer count;
        private BigDecimal duration;
    }

    @Data
    public static class EmployeeData {
        private String employeeId;
        private String employeeName;
        private Integer count;
        private BigDecimal duration;
    }
}
