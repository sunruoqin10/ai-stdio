package com.example.oa_system_backend.module.asset.enums;

import lombok.Getter;

/**
 * 借还状态枚举
 */
@Getter
public enum BorrowStatusEnum {

    ACTIVE("active", "借出中"),
    RETURNED("returned", "已归还"),
    OVERDUE("overdue", "已超期");

    private final String code;
    private final String description;

    BorrowStatusEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static BorrowStatusEnum fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (BorrowStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid borrow status code: " + code);
    }

    public static String getDescriptionByCode(String code) {
        BorrowStatusEnum status = fromCode(code);
        return status != null ? status.getDescription() : "";
    }
}
