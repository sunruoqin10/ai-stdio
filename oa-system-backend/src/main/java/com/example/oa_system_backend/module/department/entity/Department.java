package com.example.oa_system_backend.module.department.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 部门实体类
 * 对应表: sys_department
 */
@Data
@TableName("sys_department")
public class Department {

    /**
     * 部门ID (格式: DEPT + 4位序号)
     */
    @TableId(type = IdType.INPUT)
    private String id;

    /**
     * 部门名称
     */
    private String name;

    /**
     * 部门简称
     */
    @TableField("short_name")
    private String shortName;

    /**
     * 上级部门ID
     */
    @TableField("parent_id")
    private String parentId;

    /**
     * 部门负责人ID
     */
    @TableField("leader_id")
    private String leaderId;

    /**
     * 部门层级 (1-5)
     */
    private Integer level;

    /**
     * 排序号
     */
    private Integer sort;

    /**
     * 成立时间
     */
    @TableField("established_date")
    private LocalDate establishedDate;

    /**
     * 部门描述
     */
    private String description;

    /**
     * 部门图标URL
     */
    private String icon;

    /**
     * 状态: active-正常, disabled-停用
     */
    private String status;

    /**
     * 创建时间
     */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /**
     * 创建人ID
     */
    @TableField("created_by")
    private String createdBy;

    /**
     * 更新时间
     */
    @TableField("updated_at")
    private LocalDateTime updatedAt;

    /**
     * 更新人ID
     */
    @TableField("updated_by")
    private String updatedBy;

    /**
     * 是否删除 (0-否, 1-是)
     */
    @TableLogic
    @TableField("is_deleted")
    private Integer isDeleted;

    /**
     * 删除时间
     */
    @TableField("deleted_at")
    private LocalDateTime deletedAt;

    /**
     * 删除人ID
     */
    @TableField("deleted_by")
    private String deletedBy;

    /**
     * 乐观锁版本号
     */
    // @Version  // 暂时注释，修复MyBatis-Plus乐观锁参数绑定问题
    private Integer version;

    // ==================== 非数据库字段 ====================

    /**
     * 部门负责人姓名 (关联查询)
     */
    @TableField(exist = false)
    private String leaderName;

    /**
     * 上级部门名称 (关联查询)
     */
    @TableField(exist = false)
    private String parentName;

    /**
     * 员工数量 (统计查询)
     */
    @TableField(exist = false)
    private Integer employeeCount;

    /**
     * 子部门数量 (统计查询)
     */
    @TableField(exist = false)
    private Integer childCount;
}
