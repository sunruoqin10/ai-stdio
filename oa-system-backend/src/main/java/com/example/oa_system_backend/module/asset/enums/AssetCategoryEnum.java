package com.example.oa_system_backend.module.asset.enums;

import lombok.Getter;

/**
 * 资产类别枚举
 */
@Getter
public enum AssetCategoryEnum {

    ELECTRONIC("electronic", "电子设备"),
    FURNITURE("furniture", "家具"),
    BOOK("book", "图书"),
    OTHER("other", "其他");

    private final String code;
    private final String description;

    AssetCategoryEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static AssetCategoryEnum fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (AssetCategoryEnum category : values()) {
            if (category.getCode().equals(code)) {
                return category;
            }
        }
        throw new IllegalArgumentException("Invalid asset category code: " + code);
    }

    public static String getDescriptionByCode(String code) {
        AssetCategoryEnum category = fromCode(code);
        return category != null ? category.getDescription() : "";
    }
}
