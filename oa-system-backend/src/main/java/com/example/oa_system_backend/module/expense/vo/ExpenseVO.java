package com.example.oa_system_backend.module.expense.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ExpenseVO {

    private String id;

    private String applicantId;

    private String applicantName;

    private String departmentId;

    private String departmentName;

    private String type;

    private String typeName;

    private BigDecimal amount;

    private String reason;

    private LocalDate applyDate;

    private LocalDate expenseDate;

    private String status;

    private String statusName;

    private String deptApproverName;

    private String deptApprovalStatus;

    private LocalDateTime deptApprovalTime;

    private String financeApproverName;

    private String financeApprovalStatus;

    private LocalDateTime financeApprovalTime;

    private LocalDate paymentDate;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Integer itemCount;

    private Integer invoiceCount;

    // 乐观锁版本号
    private Integer version;
}
