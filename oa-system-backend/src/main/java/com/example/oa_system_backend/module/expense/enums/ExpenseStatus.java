package com.example.oa_system_backend.module.expense.enums;

import lombok.Getter;

@Getter
public enum ExpenseStatus {

    DRAFT("draft", "草稿"),
    DEPT_PENDING("dept_pending", "待部门审批"),
    FINANCE_PENDING("finance_pending", "待财务审批"),
    PAID("paid", "待打款"),
    COMPLETED("completed", "已完成"),
    REJECTED("rejected", "已拒绝");

    private final String code;
    private final String name;

    ExpenseStatus(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static ExpenseStatus fromCode(String code) {
        for (ExpenseStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown expense status: " + code);
    }

    public boolean canEdit() {
        return this == DRAFT;
    }

    public boolean canSubmit() {
        return this == DRAFT || this == REJECTED;
    }

    public boolean canCancel() {
        return this == DEPT_PENDING || this == FINANCE_PENDING;
    }

    public boolean canDelete() {
        return this == DRAFT;
    }
}
