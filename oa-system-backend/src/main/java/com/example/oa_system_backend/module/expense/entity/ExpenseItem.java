package com.example.oa_system_backend.module.expense.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("approval_expense_item")
public class ExpenseItem {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("expense_id")
    private String expenseId;

    private String description;

    private BigDecimal amount;

    @TableField("expense_date")
    private LocalDate expenseDate;

    private String category;

    @TableField("sort_order")
    private Integer sortOrder;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
