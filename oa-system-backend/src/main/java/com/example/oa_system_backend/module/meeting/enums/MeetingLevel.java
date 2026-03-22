package com.example.oa_system_backend.module.meeting.enums;

public enum MeetingLevel {
    NORMAL("normal", "普通"),
    IMPORTANT("important", "重要"),
    URGENT("urgent", "紧急");

    private final String code;
    private final String name;

    MeetingLevel(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static MeetingLevel fromCode(String code) {
        for (MeetingLevel level : values()) {
            if (level.code.equals(code)) {
                return level;
            }
        }
        return null;
    }
}
