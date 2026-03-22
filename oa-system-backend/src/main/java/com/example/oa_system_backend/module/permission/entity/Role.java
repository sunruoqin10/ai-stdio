package com.example.oa_system_backend.module.permission.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 角色实体类
 * 对应表: sys_role
 */
@Data
@TableName("sys_role")
public class Role {

    @TableId(type = IdType.INPUT)
    private String id;

    private String name;

    private String code;

    private String type;

    private Integer sort;

    private String description;

    private String status;

    @TableField("is_preset")
    private Boolean isPreset;

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
    private Integer userCount;
}