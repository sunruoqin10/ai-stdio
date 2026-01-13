package com.example.oa_system_backend.module.employee.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志VO
 */
@Data
public class OperationLogVO {

    /**
     * 日志ID
     */
    private Long id;

    /**
     * 员工ID
     */
    private String employeeId;

    /**
     * 操作类型
     */
    private String operation;

    /**
     * 操作人ID
     */
    private String operatorId;

    /**
     * 操作人姓名
     */
    private String operatorName;

    /**
     * 操作时间
     */
    private LocalDateTime operationTime;

    /**
     * 详细信息
     */
    private String details;

    /**
     * IP地址
     */
    private String ipAddress;
}
