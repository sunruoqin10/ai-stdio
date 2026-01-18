package com.example.oa_system_backend.module.leave.enums;

import lombok.Getter;

@Getter
public enum HolidayType {

    NATIONAL("national", "法定节假日"),
    COMPANY("company", "公司节假日");

    private final String code;
    private final String name;

    HolidayType(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static HolidayType fromCode(String code) {
        for (HolidayType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown holiday type: " + code);
    }
}
