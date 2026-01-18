package com.example.oa_system_backend.module.leave.enums;

import lombok.Getter;

@Getter
public enum LeaveStatus {

    DRAFT("draft", "草稿"),
    PENDING("pending", "待审批"),
    APPROVING("approving", "审批中"),
    APPROVED("approved", "已通过"),
    REJECTED("rejected", "已拒绝"),
    CANCELLED("cancelled", "已取消");

    private final String code;
    private final String name;

    LeaveStatus(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static LeaveStatus fromCode(String code) {
        for (LeaveStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown leave status: " + code);
    }
}
