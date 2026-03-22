package com.example.oa_system_backend.module.meeting.enums;

public enum ReminderTime {
    NONE("none", "不提醒"),
    MINUTES_5("5", "提前5分钟"),
    MINUTES_10("10", "提前10分钟"),
    MINUTES_15("15", "提前15分钟"),
    MINUTES_30("30", "提前30分钟"),
    HOUR_1("60", "提前1小时"),
    DAY_1("1440", "提前1天");

    private final String code;
    private final String name;

    ReminderTime(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static ReminderTime fromCode(String code) {
        for (ReminderTime reminder : values()) {
            if (reminder.code.equals(code)) {
                return reminder;
            }
        }
        return null;
    }
}
