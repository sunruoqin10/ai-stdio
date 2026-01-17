package com.example.oa_system_backend.module.asset.enums;

import lombok.Getter;

/**
 * 资产状态枚举
 */
@Getter
public enum AssetStatusEnum {

    STOCK("stock", "在库"),
    IN_USE("in_use", "使用中"),
    BORROWED("borrowed", "借出"),
    MAINTENANCE("maintenance", "维护中"),
    SCRAPPED("scrapped", "报废");

    private final String code;
    private final String description;

    AssetStatusEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static AssetStatusEnum fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (AssetStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid asset status code: " + code);
    }

    public static String getDescriptionByCode(String code) {
        AssetStatusEnum status = fromCode(code);
        return status != null ? status.getDescription() : "";
    }

    /**
     * 验证状态转换是否合法
     */
    public boolean canTransitionTo(AssetStatusEnum newStatus) {
        if (newStatus == null) {
            return false;
        }
        return switch (this) {
            case STOCK -> newStatus == IN_USE || newStatus == BORROWED
                    || newStatus == MAINTENANCE || newStatus == SCRAPPED;
            case IN_USE -> newStatus == STOCK || newStatus == MAINTENANCE
                    || newStatus == SCRAPPED;
            case BORROWED -> newStatus == STOCK;
            case MAINTENANCE -> newStatus == STOCK || newStatus == IN_USE
                    || newStatus == SCRAPPED;
            case SCRAPPED -> false; // 报废状态不能转换
        };
    }
}
