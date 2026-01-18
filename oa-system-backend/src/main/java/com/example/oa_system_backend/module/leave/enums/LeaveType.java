package com.example.oa_system_backend.module.leave.enums;

import lombok.Getter;

@Getter
public enum LeaveType {

    ANNUAL("annual", "年假"),
    SICK("sick", "病假"),
    PERSONAL("personal", "事假"),
    COMP_TIME("comp_time", "调休"),
    MARRIAGE("marriage", "婚假"),
    MATERNITY("maternity", "产假");

    private final String code;
    private final String name;

    LeaveType(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static LeaveType fromCode(String code) {
        for (LeaveType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown leave type: " + code);
    }
}
