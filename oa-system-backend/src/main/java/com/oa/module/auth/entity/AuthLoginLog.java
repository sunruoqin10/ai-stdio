package com.oa.module.auth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("auth_login_log")
public class AuthLoginLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String userId;
    private String username;
    private String loginIp;
    private String loginLocation;
    private String deviceInfo;
    private String browser;
    private String os;
    private String status; // success, failed
    private String failureReason;
    private LocalDateTime loginTime;
}
