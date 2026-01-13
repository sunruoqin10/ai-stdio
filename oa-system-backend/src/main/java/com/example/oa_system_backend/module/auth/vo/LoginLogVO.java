package com.example.oa_system_backend.module.auth.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LoginLogVO {
    private Long id;
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
