package com.example.oa_system_backend.module.expense.enums;

import lombok.Getter;

@Getter
public enum InvoiceType {

    VAT_SPECIAL("vat_special", "增值税专用发票"),
    VAT_COMMON("vat_common", "增值税普通发票"),
    ELECTRONIC("electronic", "电子发票");

    private final String code;
    private final String name;

    InvoiceType(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static InvoiceType fromCode(String code) {
        for (InvoiceType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown invoice type: " + code);
    }
}
