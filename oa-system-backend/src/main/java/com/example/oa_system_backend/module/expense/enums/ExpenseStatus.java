package com.example.oa_system_backend.module.expense.enums;

import lombok.Getter;

/**
 * 报销单状态枚举
 *
 * 状态流转:
 * 草稿 → 待部门审批 → 待财务审批 → 待打款 → 已完成
 * 任何审批阶段都可以 → 已驳回 → 草稿
 *
 * @version 2.0
 * @since 2026-01-24
 */
@Getter
public enum ExpenseStatus {

    /** 草稿状态 - 报销单创建后的初始状态 */
    DRAFT("draft", "草稿"),

    /** 待部门审批 - 等待部门主管审批 */
    DEPT_PENDING("dept_pending", "待部门审批"),

    /** 待财务审批 - 等待财务人员审批 */
    FINANCE_PENDING("finance_pending", "待财务审批"),

    /** 待打款 - 财务审批已完成，等待财务打款 */
    PAID("paid", "待打款"),

    /** 已完成 - 报销流程已全部结束，打款凭证已上传 */
    COMPLETED("completed", "已完成"),

    /** 已拒绝 - 报销单被审批人驳回 */
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
