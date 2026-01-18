package com.example.oa_system_backend.module.leave.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("approval_leave_approval")
public class LeaveApproval {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("request_id")
    private String requestId;

    @TableField("approver_id")
    private String approverId;

    @TableField("approver_name")
    private String approverName;

    @TableField("approval_level")
    private Integer approvalLevel;

    private String status;

    private String opinion;

    private LocalDateTime timestamp;

    @TableField(exist = false)
    private String approverPosition;

    @TableField(exist = false)
    private String approverAvatar;

    @TableField(exist = false)
    private String approverDepartment;

    @TableField(exist = false)
    private String approvalLevelName;

    @TableField(exist = false)
    private Boolean isCurrent;
}
