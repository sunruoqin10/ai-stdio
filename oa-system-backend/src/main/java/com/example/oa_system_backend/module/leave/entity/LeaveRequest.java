package com.example.oa_system_backend.module.leave.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("approval_leave_request")
public class LeaveRequest {

    @TableId(type = IdType.INPUT)
    private String id;

    @TableField("applicant_id")
    private String applicantId;

    @TableField("department_id")
    private String departmentId;

    private String type;

    @TableField("start_time")
    private LocalDateTime startTime;

    @TableField("end_time")
    private LocalDateTime endTime;

    private BigDecimal duration;

    private String reason;

    private String attachments;

    private String status;

    @TableField("current_approval_level")
    private Integer currentApprovalLevel;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;

    @TableField(exist = false)
    private String applicantName;

    @TableField(exist = false)
    private String applicantPosition;

    @TableField(exist = false)
    private String applicantAvatar;

    @TableField(exist = false)
    private String departmentName;

    @TableField(exist = false)
    private Integer totalApprovalLevels;
}
