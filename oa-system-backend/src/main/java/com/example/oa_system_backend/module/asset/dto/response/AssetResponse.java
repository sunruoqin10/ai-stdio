package com.example.oa_system_backend.module.asset.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 资产响应
 */
@Data
public class AssetResponse {

    /**
     * 资产ID
     */
    private String id;

    /**
     * 资产名称
     */
    private String name;

    /**
     * 资产类别
     */
    private String category;

    /**
     * 资产类别名称
     */
    private String categoryName;

    /**
     * 品牌/型号
     */
    private String brandModel;

    /**
     * 购置日期
     */
    private LocalDate purchaseDate;

    /**
     * 购置金额
     */
    private BigDecimal purchasePrice;

    /**
     * 当前价值
     */
    private BigDecimal currentValue;

    /**
     * 折旧金额
     */
    private BigDecimal depreciationAmount;

    /**
     * 折旧率
     */
    private BigDecimal depreciationRate;

    /**
     * 资产状态
     */
    private String status;

    /**
     * 资产状态名称
     */
    private String statusName;

    /**
     * 使用人ID
     */
    private String userId;

    /**
     * 使用人姓名
     */
    private String userName;

    /**
     * 使用人头像
     */
    private String userAvatar;

    /**
     * 部门ID
     */
    private String departmentId;

    /**
     * 部门名称
     */
    private String departmentName;

    /**
     * 存放位置
     */
    private String location;

    /**
     * 借出日期
     */
    private LocalDate borrowDate;

    /**
     * 预计归还日期
     */
    private LocalDate expectedReturnDate;

    /**
     * 实际归还日期
     */
    private LocalDate actualReturnDate;

    /**
     * 资产图片URL列表
     */
    private List<String> images;

    /**
     * 备注
     */
    private String notes;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 是否超期
     */
    private Boolean isOverdue;

    /**
     * 超期天数
     */
    private Integer overdueDays;

    /**
     * 持有年限
     */
    private Integer ownedYears;
}
