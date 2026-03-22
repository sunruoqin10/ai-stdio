package com.example.oa_system_backend.module.meeting.enums;

public enum MeetingRoomStatus {
    AVAILABLE("available", "可用"),
    UNAVAILABLE("unavailable", "不可用"),
    DISABLED("disabled", "已禁用");

    private final String code;
    private final String name;

    MeetingRoomStatus(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static MeetingRoomStatus fromCode(String code) {
        for (MeetingRoomStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return null;
    }
}
