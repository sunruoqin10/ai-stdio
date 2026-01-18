package com.example.oa_system_backend.module.asset.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 资产创建请求
 */
@Data
public class AssetCreateRequest {

    /**
     * 资产名称
     */
    @NotBlank(message = "资产名称不能为空")
    @Size(max = 200, message = "资产名称长度不能超过200字符")
    private String name;

    /**
     * 资产类别
     */
    @NotBlank(message = "资产类别不能为空")
    @Pattern(regexp = "^(electronic|furniture|book|other)$",
             message = "资产类别必须是electronic、furniture、book或other")
    private String category;

    /**
     * 品牌/型号
     */
    @Size(max = 200, message = "品牌/型号长度不能超过200字符")
    private String brandModel;

    /**
     * 购置日期
     */
    @NotNull(message = "购置日期不能为空")
    @PastOrPresent(message = "购置日期不能是未来日期")
    private LocalDate purchaseDate;

    /**
     * 购置金额
     */
    @NotNull(message = "购置金额不能为空")
    @DecimalMin(value = "0.01", message = "购置金额必须大于0")
    @Digits(integer = 8, fraction = 2, message = "购置金额格式不正确")
    private BigDecimal purchasePrice;

    /**
     * 当前价值
     */
    private BigDecimal currentValue;

    /**
     * 使用人ID (可选)
     */
    private String userId;

    /**
     * 存放位置
     */
    @Size(max = 200, message = "存放位置长度不能超过200字符")
    private String location;

    /**
     * 资产图片URL列表
     */
    @Size(max = 10, message = "最多上传10张图片")
    private List<String> images;

    /**
     * 备注
     */
    @Size(max = 1000, message = "备注长度不能超过1000字符")
    private String notes;
}
