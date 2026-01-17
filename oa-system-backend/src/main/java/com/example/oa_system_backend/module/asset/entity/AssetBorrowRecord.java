package com.example.oa_system_backend.module.asset.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 资产借还记录实体类
 * 对应表: biz_asset_borrow_record
 */
@Data
@TableName("biz_asset_borrow_record")
public class AssetBorrowRecord {

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
     * 资产名称(快照)
     */
    private String assetName;

    /**
     * 借用人ID
     */
    private String borrowerId;

    /**
     * 借用人姓名(快照)
     */
    private String borrowerName;

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
     * 记录状态: active(借出中), returned(已归还), overdue(已超期)
     */
    private String status;

    /**
     * 备注
     */
    private String notes;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
