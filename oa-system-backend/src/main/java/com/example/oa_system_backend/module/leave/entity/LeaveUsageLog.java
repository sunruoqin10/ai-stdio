package com.example.oa_system_backend.module.leave.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("approval_leave_usage_log")
public class LeaveUsageLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("employee_id")
    private String employeeId;

    @TableField("request_id")
    private String requestId;

    private String type;

    private BigDecimal duration;

    @TableField("start_time")
    private LocalDateTime startTime;

    @TableField("end_time")
    private LocalDateTime endTime;

    @TableField("change_type")
    private String changeType;

    @TableField("created_at")
    private LocalDateTime createdAt;
}
