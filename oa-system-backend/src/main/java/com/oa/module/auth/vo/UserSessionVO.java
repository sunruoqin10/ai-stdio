package com.oa.module.auth.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserSessionVO {
    private String id;
    private String userId;
    private String loginIp;
    private String loginLocation;
    private String deviceInfo;
    private String browser;
    private String os;
    private LocalDateTime loginTime;
    private LocalDateTime lastActiveTime;
    private Integer isActive;
    private Long inactiveMinutes;
}
