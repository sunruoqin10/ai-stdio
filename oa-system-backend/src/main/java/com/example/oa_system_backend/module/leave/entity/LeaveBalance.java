package com.example.oa_system_backend.module.leave.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("approval_leave_balance")
public class LeaveBalance {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("employee_id")
    private String employeeId;

    private Integer year;

    @TableField("annual_total")
    private BigDecimal annualTotal;

    @TableField("annual_used")
    private BigDecimal annualUsed;

    @TableField("annual_remaining")
    private BigDecimal annualRemaining;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;

    @TableField(exist = false)
    private String employeeName;

    @TableField(exist = false)
    private String employeeAvatar;

    @TableField(exist = false)
    private String departmentId;

    @TableField(exist = false)
    private String departmentName;

    @TableField(exist = false)
    private BigDecimal usagePercentage;

    @TableField(exist = false)
    private String warningLevel;
}
