package com.example.oa_system_backend.module.meeting.enums;

public enum BookingStatus {
    PENDING("pending", "待审批"),
    APPROVED("approved", "已通过"),
    REJECTED("rejected", "已拒绝"),
    CANCELLED("cancelled", "已取消"),
    CHECKED_IN("checked_in", "已签到"),
    CHECKED_OUT("checked_out", "已签退"),
    COMPLETED("completed", "已完成");

    private final String code;
    private final String name;

    BookingStatus(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static BookingStatus fromCode(String code) {
        for (BookingStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return null;
    }
}
