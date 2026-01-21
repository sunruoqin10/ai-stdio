package com.example.oa_system_backend.module.expense.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ExpenseDetailVO extends ExpenseVO {

    private String applicantPosition;

    private String applicantPhone;

    private String applicantEmail;

    private String deptApprovalOpinion;

    private String financeApprovalOpinion;

    private String paymentProof;

    private List<ExpenseItemVO> items;

    private List<ExpenseInvoiceVO> invoices;

    private ExpensePaymentVO payment;

    @Data
    public static class ExpenseItemVO {
        private Long id;
        private String description;
        private BigDecimal amount;
        private LocalDate expenseDate;
        private String category;
    }

    @Data
    public static class ExpenseInvoiceVO {
        private Long id;
        private String invoiceType;
        private String invoiceTypeName;
        private String invoiceNumber;
        private BigDecimal amount;
        private LocalDate invoiceDate;
        private String imageUrl;
        private Boolean verified;
    }

    @Data
    public static class ExpensePaymentVO {
        private Long id;
        private BigDecimal amount;
        private String paymentMethod;
        private String paymentMethodName;
        private LocalDate paymentDate;
        private String bankAccount;
        private String proof;
        private String status;
        private String statusName;
        private String remark;
    }
}
