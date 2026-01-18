package com.example.oa_system_backend.module.leave.enums;

import lombok.Getter;

@Getter
public enum ChangeType {

    DEDUCT("deduct", "扣减"),
    ROLLBACK("rollback", "回退");

    private final String code;
    private final String name;

    ChangeType(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static ChangeType fromCode(String code) {
        for (ChangeType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown change type: " + code);
    }
}
