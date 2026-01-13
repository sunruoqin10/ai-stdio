package com.example.oa_system_backend.module.auth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("auth_login_log")
public class AuthLoginLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private String userId;

    private String username;

    @TableField("ip_address")
    private String loginIp;

    private String location;

    @TableField("device_type")
    private String deviceInfo;

    @TableField(exist = false)
    private String browser;

    @TableField(exist = false)
    private String os;

    private String status; // success, failed

    private String failureReason;

    @TableField(exist = false)
    private LocalDateTime loginTime;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableLogic
    @TableField("is_deleted")
    private Integer isDeleted;
}
