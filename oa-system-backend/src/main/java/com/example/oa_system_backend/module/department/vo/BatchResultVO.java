package com.example.oa_system_backend.module.department.vo;

import lombok.Data;

/**
 * 批量操作结果VO
 */
@Data
public class BatchResultVO {
    /**
     * 总数
     */
    private Integer total;

    /**
     * 成功数
     */
    private Integer success;

    /**
     * 失败数
     */
    private Integer failed;

    /**
     * 错误列表
     */
    private java.util.List<BatchError> errors;

    @Data
    public static class BatchError {
        /**
         * ID
         */
        private String id;

        /**
         * 错误信息
         */
        private String message;
    }
}
