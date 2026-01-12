package com.oa.module.auth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
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
    private Integer isUsed;
    private LocalDateTime usedAt;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
}
