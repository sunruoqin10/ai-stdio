package com.example.oa_system_backend.module.employee.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 员工操作日志实体
 * 对应表: sys_employee_operation_log
 */
@Data
@TableName("sys_employee_operation_log")
public class EmployeeOperationLog {

    /**
     * 日志ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 员工ID
     */
    @TableField("employee_id")
    private String employeeId;

    /**
     * 操作类型
     * CREATE-创建, UPDATE-更新, DELETE-删除, RESIGN-离职, etc.
     */
    private String operation;

    /**
     * 操作人ID
     */
    @TableField("operator_id")
    private String operatorId;

    /**
     * 操作人姓名
     */
    @TableField("operator_name")
    private String operatorName;

    /**
     * 操作时间
     */
    @TableField("operation_time")
    private LocalDateTime operationTime;

    /**
     * 详细信息(JSON格式)
     */
    private String details;

    /**
     * IP地址
     */
    @TableField("ip_address")
    private String ipAddress;

    /**
     * 用户代理
     */
    @TableField("user_agent")
    private String userAgent;

    /**
     * 创建时间
     */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /**
     * 逻辑删除标记
     */
    @TableLogic
    @TableField("is_deleted")
    private Integer isDeleted;
}
