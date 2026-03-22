package com.example.oa_system_backend.module.meeting.enums;

public enum RecurrenceType {
    NONE("none", "不重复"),
    DAILY("daily", "每天"),
    WEEKLY("weekly", "每周"),
    MONTHLY("monthly", "每月");

    private final String code;
    private final String name;

    RecurrenceType(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static RecurrenceType fromCode(String code) {
        for (RecurrenceType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return null;
    }
}
