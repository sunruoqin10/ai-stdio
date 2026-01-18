package com.example.oa_system_backend.module.asset.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 资产视图对象
 */
@Data
public class AssetVO {

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
     * 存放位置
     */
    private String location;

    /**
     * 资产图片URL数组
     */
    private List<String> images;

    /**
     * 借出日期
     */
    private LocalDate borrowDate;

    /**
     * 预计归还日期
     */
    private LocalDate expectedReturnDate;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 是否超期
     */
    private Boolean isOverdue;

    /**
     * 乐观锁版本号
     */
    private Integer version;
}
