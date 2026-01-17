package com.example.oa_system_backend.module.asset.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 资产维护记录实体类
 * 对应表: biz_asset_maintenance
 */
@Data
@TableName("biz_asset_maintenance")
public class AssetMaintenance {

    /**
     * 记录ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 资产ID
     */
    private String assetId;

    /**
     * 类型: repair(修理), maintenance(保养), check(检查)
     */
    private String type;

    /**
     * 描述
     */
    private String description;

    /**
     * 费用
     */
    private BigDecimal cost;

    /**
     * 开始日期
     */
    private LocalDate startDate;

    /**
     * 结束日期
     */
    private LocalDate endDate;

    /**
     * 操作人ID
     */
    private String operatorId;

    /**
     * 操作人姓名
     */
    private String operatorName;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
