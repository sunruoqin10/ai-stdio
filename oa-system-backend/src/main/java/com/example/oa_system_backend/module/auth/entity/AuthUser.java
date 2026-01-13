package com.example.oa_system_backend.module.auth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("auth_user")
public class AuthUser {

    @TableId(type = IdType.INPUT)
    private String id; // 关联 sys_employee.id

    private String username;

    private String password;

    private String email;

    private String mobile;

    private String status; // active, locked, disabled

    @TableField("failed_login_attempts")
    private Integer loginAttempts;

    private LocalDateTime lockedUntil;

    @TableField("last_login_at")
    private LocalDateTime lastLoginTime;

    @TableField("last_login_ip")
    private String lastLoginIp;

    @TableField("password_changed_at")
    private LocalDateTime passwordChangedAt;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;

    @TableLogic
    @TableField("is_deleted")
    private Integer isDeleted;

    @TableField("deleted_at")
    private LocalDateTime deletedAt;

    @TableField("deleted_by")
    private String deletedBy;
}
