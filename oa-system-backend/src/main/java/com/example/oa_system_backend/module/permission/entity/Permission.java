package com.example.oa_system_backend.module.permission.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 权限实体类
 * 对应表: sys_permission
 */
@Data
@TableName("sys_permission")
public class Permission {

    @TableId(type = IdType.INPUT)
    private String id;

    private String name;

    private String code;

    private String type;

    private String module;

    @TableField("parent_id")
    private String parentId;

    private String path;

    private String component;

    private String icon;

    @TableField("api_path")
    private String apiPath;

    @TableField("api_method")
    private String apiMethod;

    @TableField("data_scope")
    private String dataScope;

    private Integer sort;

    private String status;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("created_by")
    private String createdBy;

    @TableField("updated_at")
    private LocalDateTime updatedAt;

    @TableField("updated_by")
    private String updatedBy;

    @TableLogic
    @TableField("is_deleted")
    private Integer isDeleted;

    @TableField("deleted_at")
    private LocalDateTime deletedAt;

    @TableField("deleted_by")
    private String deletedBy;

    private Integer version;

    @TableField(exist = false)
    private java.util.List<Permission> children;
}