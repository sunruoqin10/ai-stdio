package com.example.oa_system_backend.module.leave.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class LeaveDetailVO {

    private String id;

    private String applicantId;

    private String applicantName;

    private String applicantPosition;

    private String applicantPhone;

    private String applicantEmail;

    private String applicantAvatar;

    private String departmentId;

    private String departmentName;

    private String managerId;

    private String managerName;

    private String type;

    private String typeLabel;

    private String typeName;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private BigDecimal duration;

    private String reason;

    private String attachments;

    private String status;

    private String statusLabel;

    private String statusName;

    private Integer currentApprovalLevel;

    private Integer totalApprovalLevels;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private List<ApprovalRecordVO> approvals;

    private String rejectReason;

    private LocalDateTime rejectTime;
}
