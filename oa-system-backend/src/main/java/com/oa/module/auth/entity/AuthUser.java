package com.oa.module.auth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
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
    private Integer loginAttempts;
    private LocalDateTime lockedUntil;
    private LocalDateTime lastLoginTime;
    private String lastLoginIp;
    private LocalDateTime passwordChangedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer isDeleted;
    private LocalDateTime deletedAt;
    private String deletedBy;
}
