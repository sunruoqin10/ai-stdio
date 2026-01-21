package com.example.oa_system_backend.module.expense.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("approval_expense_invoice")
public class ExpenseInvoice {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("expense_id")
    private String expenseId;

    @TableField("invoice_type")
    private String invoiceType;

    @TableField("invoice_number")
    private String invoiceNumber;

    private BigDecimal amount;

    @TableField("invoice_date")
    private LocalDate invoiceDate;

    @TableField("image_url")
    private String imageUrl;

    private Boolean verified;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
