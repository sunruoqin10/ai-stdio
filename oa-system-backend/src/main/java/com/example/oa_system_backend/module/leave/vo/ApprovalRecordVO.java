package com.example.oa_system_backend.module.leave.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ApprovalRecordVO {

    private Long id;

    private String requestId;

    private String approverId;

    private String approverName;

    private String approverPosition;

    private String approverAvatar;

    private String approverDepartment;

    private Integer approvalLevel;

    private String approvalLevelName;

    private String status;

    private String statusLabel;

    private String statusName;

    private String opinion;

    private LocalDateTime timestamp;

    private Boolean isCurrent;
}
