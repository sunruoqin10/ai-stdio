package com.example.oa_system_backend.module.dict.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * 更新字典项请求DTO
 */
@Data
public class DictItemUpdateRequest {

    @Size(min = 1, max = 200, message = "项标签长度在1-200个字符之间")
    private String label;

    @Size(min = 1, max = 200, message = "项值长度在1-200个字符之间")
    private String value;

    @Pattern(regexp = "^(primary|success|warning|danger|info)?$",
             message = "颜色类型必须是primary、success、warning、danger或info")
    private String colorType;

    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$",
             message = "自定义颜色格式不正确,应为#开头的6位十六进制颜色值")
    private String color;

    @Size(max = 100, message = "图标长度不能超过100个字符")
    private String icon;

    @Min(value = 0, message = "排序序号不能小于0")
    private Integer sortOrder;

    @Pattern(regexp = "^(enabled|disabled)$", message = "状态必须是enabled或disabled")
    private String status;

    private String extProps;

    @Size(max = 500, message = "备注长度不能超过500个字符")
    private String remark;
}
