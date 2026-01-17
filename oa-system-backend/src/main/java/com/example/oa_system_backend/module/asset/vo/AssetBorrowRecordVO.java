package com.example.oa_system_backend.module.asset.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 资产借还记录视图对象
 */
@Data
public class AssetBorrowRecordVO {

    /**
     * 记录ID
     */
    private Long id;

    /**
     * 资产ID
     */
    private String assetId;

    /**
     * 资产名称
     */
    private String assetName;

    /**
     * 借用人ID
     */
    private String borrowerId;

    /**
     * 借用人姓名
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
     * 记录状态
     */
    private String status;

    /**
     * 记录状态名称
     */
    private String statusName;

    /**
     * 备注
     */
    private String notes;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
