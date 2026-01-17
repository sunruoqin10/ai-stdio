package com.example.oa_system_backend.module.asset.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 资产统计响应
 */
@Data
public class AssetStatisticsResponse {

    /**
     * 总资产数量
     */
    private Long totalCount;

    /**
     * 总购置金额
     */
    private BigDecimal totalPurchaseValue;

    /**
     * 总当前价值
     */
    private BigDecimal totalCurrentValue;

    /**
     * 总折旧金额
     */
    private BigDecimal totalDepreciationAmount;

    /**
     * 按类别统计
     */
    private List<CategoryStatistics> categoryStatistics;

    /**
     * 按状态统计
     */
    private List<StatusStatistics> statusStatistics;

    /**
     * 类别统计内部类
     */
    @Data
    public static class CategoryStatistics {
        private String category;
        private String categoryName;
        private Long count;
        private BigDecimal purchaseValue;
        private BigDecimal currentValue;
    }

    /**
     * 状态统计内部类
     */
    @Data
    public static class StatusStatistics {
        private String status;
        private String statusName;
        private Long count;
        private BigDecimal value;
    }
}
