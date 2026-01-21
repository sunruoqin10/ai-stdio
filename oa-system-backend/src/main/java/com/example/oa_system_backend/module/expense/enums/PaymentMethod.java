package com.example.oa_system_backend.module.expense.enums;

import lombok.Getter;

@Getter
public enum PaymentMethod {

    BANK_TRANSFER("bank_transfer", "银行转账"),
    CASH("cash", "现金"),
    CHECK("check", "支票");

    private final String code;
    private final String name;

    PaymentMethod(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static PaymentMethod fromCode(String code) {
        for (PaymentMethod method : values()) {
            if (method.code.equals(code)) {
                return method;
            }
        }
        throw new IllegalArgumentException("Unknown payment method: " + code);
    }
}
