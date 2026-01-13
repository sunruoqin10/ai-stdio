package com.example.oa_system_backend.module.auth.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserSessionVO {
    private String id;
    private String loginIp;
    private String loginLocation;
    private String deviceInfo;
    private String browser;
    private String os;
    private LocalDateTime loginTime;
    private LocalDateTime lastActiveTime;
    private Boolean isActive;
}
