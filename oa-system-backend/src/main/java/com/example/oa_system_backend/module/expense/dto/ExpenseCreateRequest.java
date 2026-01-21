package com.example.oa_system_backend.module.expense.dto;

import com.example.oa_system_backend.module.expense.enums.ExpenseType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class ExpenseCreateRequest {

    @NotBlank(message = "报销类型不能为空")
    private String type;

    @NotBlank(message = "报销事由不能为空")
    @Size(min = 10, max = 500, message = "报销事由长度必须在10-500字符之间")
    private String reason;

    @NotNull(message = "费用发生日期不能为空")
    @PastOrPresent(message = "费用发生日期不能晚于今天")
    private LocalDate expenseDate;

    @NotEmpty(message = "费用明细不能为空")
    @Size(min = 1, message = "至少需要一条费用明细")
    private List<ExpenseItemRequest> items;

    @NotEmpty(message = "发票不能为空")
    @Size(min = 1, message = "至少需要一张发票")
    private List<ExpenseInvoiceRequest> invoices;

    @Data
    public static class ExpenseItemRequest {

        @NotBlank(message = "费用说明不能为空")
        @Size(max = 500, message = "费用说明不能超过500字符")
        private String description;

        @NotNull(message = "金额不能为空")
        @DecimalMin(value = "0.01", message = "金额必须大于0")
        @Digits(integer = 8, fraction = 2, message = "金额格式不正确")
        private BigDecimal amount;

        @NotNull(message = "发生日期不能为空")
        @PastOrPresent(message = "发生日期不能晚于今天")
        private LocalDate expenseDate;

        private String category;
    }

    @Data
    public static class ExpenseInvoiceRequest {

        @NotBlank(message = "发票类型不能为空")
        private String invoiceType;

        @NotBlank(message = "发票号码不能为空")
        @Pattern(regexp = "^\\d{8}$|^\\d{20}$", message = "发票号码格式不正确,应为8位或20位数字")
        private String invoiceNumber;

        @NotNull(message = "发票金额不能为空")
        @DecimalMin(value = "0.01", message = "发票金额必须大于0")
        @Digits(integer = 8, fraction = 2, message = "发票金额格式不正确")
        private BigDecimal amount;

        @NotNull(message = "开票日期不能为空")
        @PastOrPresent(message = "开票日期不能晚于今天")
        private LocalDate invoiceDate;

        private String imageUrl;
    }
}
