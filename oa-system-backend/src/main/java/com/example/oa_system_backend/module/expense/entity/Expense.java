package com.example.oa_system_backend.module.expense.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("approval_expense")
public class Expense {

    @TableId(type = IdType.INPUT)
    private String id;

    @TableField("applicant_id")
    private String applicantId;

    @TableField("department_id")
    private String departmentId;

    private String type;

    private BigDecimal amount;

    private String reason;

    @TableField("apply_date")
    private LocalDate applyDate;

    @TableField("expense_date")
    private LocalDate expenseDate;

    private String status;

    @TableField("dept_approver_id")
    private String deptApproverId;

    @TableField("dept_approver_name")
    private String deptApproverName;

    @TableField("dept_approval_status")
    private String deptApprovalStatus;

    @TableField("dept_approval_opinion")
    private String deptApprovalOpinion;

    @TableField("dept_approval_time")
    private LocalDateTime deptApprovalTime;

    @TableField("finance_approver_id")
    private String financeApproverId;

    @TableField("finance_approver_name")
    private String financeApproverName;

    @TableField("finance_approval_status")
    private String financeApprovalStatus;

    @TableField("finance_approval_opinion")
    private String financeApprovalOpinion;

    @TableField("finance_approval_time")
    private LocalDateTime financeApprovalTime;

    @TableField("payment_date")
    private LocalDate paymentDate;

    @TableField("payment_proof")
    private String paymentProof;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;

    @TableField("deleted_at")
    private LocalDateTime deletedAt;

    @Version
    private Integer version;

    @TableField(exist = false)
    private String applicantName;

    @TableField(exist = false)
    private String applicantPosition;

    @TableField(exist = false)
    private String departmentName;

    @TableField(exist = false)
    private Integer itemCount;

    @TableField(exist = false)
    private Integer invoiceCount;
}
