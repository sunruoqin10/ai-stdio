package com.oa.module.auth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("auth_user_session")
public class AuthUserSession {
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    private String userId;
    private String accessToken;
    private String refreshToken;
    private LocalDateTime expiresAt;
    private String loginIp;
    private String loginLocation;
    private String deviceInfo;
    private String browser;
    private String os;
    private LocalDateTime loginTime;
    private LocalDateTime lastActiveTime;
    private Integer isActive;
    private LocalDateTime logoutTime;
}
