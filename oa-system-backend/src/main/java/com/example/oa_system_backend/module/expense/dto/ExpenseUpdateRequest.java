package com.example.oa_system_backend.module.expense.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class ExpenseUpdateRequest {

    @NotNull(message = "版本号不能为空")
    private Integer version;

    @Size(min = 10, max = 500, message = "报销事由长度必须在10-500字符之间")
    private String reason;

    private java.time.LocalDate expenseDate;

    @Valid
    private List<ExpenseCreateRequest.ExpenseItemRequest> items;

    @Valid
    private List<ExpenseCreateRequest.ExpenseInvoiceRequest> invoices;
}
