package com.example.oa_system_backend.module.asset.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 资产实体类
 * 对应表: biz_asset
 */
@Data
@TableName("biz_asset")
public class Asset {

    /**
     * 资产编号 (格式: ASSET+序号)
     */
    @TableId(value = "id", type = IdType.INPUT)
    private String id;

    /**
     * 资产名称
     */
    private String name;

    /**
     * 资产类别: electronic(电子设备), furniture(家具), book(图书), other(其他)
     */
    private String category;

    /**
     * 品牌/型号
     */
    private String brandModel;

    /**
     * 购置日期
     */
    private LocalDate purchaseDate;

    /**
     * 购置金额(元)
     */
    private BigDecimal purchasePrice;

    /**
     * 当前价值(自动计算折旧)
     */
    private BigDecimal currentValue;

    /**
     * 资产状态: stock(在库), in_use(使用中), borrowed(借出), maintenance(维护中), scrapped(报废)
     */
    private String status;

    /**
     * 使用人/保管人ID
     */
    private String userId;

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
     * 维护记录(JSON)
     */
    private String maintenanceRecord;

    /**
     * 资产图片URL数组(JSON)
     */
    private String images;

    /**
     * 备注信息
     */
    private String notes;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 创建人ID
     */
    @TableField(fill = FieldFill.INSERT)
    private String createdBy;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /**
     * 更新人ID
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updatedBy;

    /**
     * 是否删除(0否1是)
     */
    @TableLogic
    private Integer isDeleted;

    /**
     * 删除时间
     */
    private LocalDateTime deletedAt;

    /**
     * 删除人ID
     */
    private String deletedBy;

    /**
     * 乐观锁版本号
     */
    @Version
    private Integer version;
}
