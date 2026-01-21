package com.example.oa_system_backend.module.expense.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("approval_expense_payment")
public class ExpensePayment {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("expense_id")
    private String expenseId;

    private BigDecimal amount;

    @TableField("payment_method")
    private String paymentMethod;

    @TableField("payment_date")
    private LocalDate paymentDate;

    private String bankAccount;

    private String proof;

    private String status;

    private String remark;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
