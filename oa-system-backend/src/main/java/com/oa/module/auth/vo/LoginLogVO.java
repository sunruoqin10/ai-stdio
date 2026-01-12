package com.oa.module.auth.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class LoginLogVO {
    private Long id;
    private String userId;
    private String username;
    private String loginIp;
    private String loginLocation;
    private String deviceInfo;
    private String browser;
    private String os;
    private String status;
    private String failureReason;
    private LocalDateTime loginTime;
}
