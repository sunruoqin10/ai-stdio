package com.example.oa_system_backend.module.asset.dto.request;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 资产查询请求
 */
@Data
public class AssetQueryRequest {

    /**
     * 页码 (从1开始)
     */
    private Integer pageNum = 1;

    /**
     * 每页大小
     */
    private Integer pageSize = 10;

    /**
     * 关键词搜索 (资产名称/ID/品牌型号)
     */
    private String keyword;

    /**
     * 资产类别
     */
    private String category;

    /**
     * 资产状态
     */
    private String status;

    /**
     * 使用人ID
     */
    private String userId;

    /**
     * 购置日期-开始
     */
    private LocalDate purchaseDateStart;

    /**
     * 购置日期-结束
     */
    private LocalDate purchaseDateEnd;

    /**
     * 购置金额-最小值
     */
    private BigDecimal purchasePriceMin;

    /**
     * 购置金额-最大值
     */
    private BigDecimal purchasePriceMax;

    /**
     * 存放位置
     */
    private String location;

    /**
     * 排序字段
     */
    private String sortField = "created_at";

    /**
     * 排序方向 (asc/desc)
     */
    private String sortOrder = "desc";
}
