package com.example.oa_system_backend.module.expense.enums;

import java.math.BigDecimal;
import lombok.Getter;

@Getter
public enum ExpenseType {

    TRAVEL("travel", "差旅费"),
    HOSPITALITY("hospitality", "招待费"),
    OFFICE("office", "办公费"),
    TRANSPORT("transport", "交通费"),
    OTHER("other", "其他");

    private final String code;
    private final String name;

    ExpenseType(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static ExpenseType fromCode(String code) {
        for (ExpenseType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown expense type: " + code);
    }

    public BigDecimal getApprovalThreshold() {
        return switch (this) {
            case TRAVEL -> new BigDecimal("5000");
            case HOSPITALITY -> new BigDecimal("2000");
            case OFFICE -> new BigDecimal("1000");
            default -> BigDecimal.ZERO;
        };
    }
}
