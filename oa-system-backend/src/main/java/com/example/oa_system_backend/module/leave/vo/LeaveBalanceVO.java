package com.example.oa_system_backend.module.leave.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class LeaveBalanceVO {

    private Long id;

    private String employeeId;

    private String employeeName;

    private String employeeAvatar;

    private String departmentId;

    private String departmentName;

    private Integer year;

    private BigDecimal annualTotal;

    private BigDecimal annualUsed;

    private BigDecimal annualRemaining;

    private BigDecimal usagePercentage;

    private String warningLevel;

    private String warningLevelLabel;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
