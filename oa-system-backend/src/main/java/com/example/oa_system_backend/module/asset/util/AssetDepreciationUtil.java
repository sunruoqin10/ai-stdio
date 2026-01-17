package com.example.oa_system_backend.module.asset.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period;

/**
 * 资产折旧计算工具类
 */
public class AssetDepreciationUtil {

    /**
     * 折旧配置
     */
    private static final class DepreciationConfig {
        /**
         * 电子设备: 3年折旧, 每年33%
         */
        static final BigDecimal ELECTRONIC_RATE = new BigDecimal("0.33");

        /**
         * 家具: 5年折旧, 每年20%
         */
        static final BigDecimal FURNITURE_RATE = new BigDecimal("0.20");

        /**
         * 图书: 不折旧
         */
        static final BigDecimal BOOK_RATE = BigDecimal.ZERO;

        /**
         * 其他: 3年折旧, 每年33%
         */
        static final BigDecimal OTHER_RATE = new BigDecimal("0.33");
    }

    /**
     * 计算资产折旧后的当前价值
     *
     * @param category     资产类别
     * @param purchaseDate 购置日期
     * @param purchasePrice 购置价格
     * @return 当前价值
     */
    public static BigDecimal calculateDepreciation(String category,
                                                    LocalDate purchaseDate,
                                                    BigDecimal purchasePrice) {
        if (purchasePrice == null || purchasePrice.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        if (purchaseDate == null) {
            return purchasePrice;
        }

        // 计算已使用年限
        int years = Period.between(purchaseDate, LocalDate.now()).getYears();

        return switch (category) {
            case "electronic" -> calculateDepreciationByRate(
                    purchasePrice, years, DepreciationConfig.ELECTRONIC_RATE
            );
            case "furniture" -> calculateDepreciationByRate(
                    purchasePrice, years, DepreciationConfig.FURNITURE_RATE
            );
            case "book" -> purchasePrice; // 图书不折旧
            case "other" -> calculateDepreciationByRate(
                    purchasePrice, years, DepreciationConfig.OTHER_RATE
            );
            default -> purchasePrice;
        };
    }

    /**
     * 按折旧率计算当前价值
     *
     * @param purchasePrice 购置价格
     * @param years        使用年限
     * @param rate         年折旧率
     * @return 当前价值
     */
    private static BigDecimal calculateDepreciationByRate(BigDecimal purchasePrice,
                                                          int years,
                                                          BigDecimal rate) {
        if (years <= 0) {
            return purchasePrice;
        }

        // 公式: current_value = purchase_price * (1 - rate) ^ years
        BigDecimal remainingRate = BigDecimal.ONE.subtract(rate);
        BigDecimal depreciationFactor = remainingRate.pow(years);

        BigDecimal currentValue = purchasePrice.multiply(depreciationFactor)
                .setScale(2, RoundingMode.HALF_UP);

        // 确保不为负数
        return currentValue.max(BigDecimal.ZERO);
    }
}
