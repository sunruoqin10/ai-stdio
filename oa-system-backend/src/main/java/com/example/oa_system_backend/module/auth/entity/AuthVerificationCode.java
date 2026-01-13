package com.example.oa_system_backend.module.auth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("auth_verification_code")
public class AuthVerificationCode {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String type; // email, mobile

    private String account;

    private String scene; // forgot_password, login, register

    private String code;

    @TableField("is_used")
    private Integer isUsed;

    @TableField("used_at")
    private LocalDateTime usedAt;

    @TableField("expires_at")
    private LocalDateTime expiresAt;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableLogic
    @TableField("is_deleted")
    private Integer isDeleted;
}
