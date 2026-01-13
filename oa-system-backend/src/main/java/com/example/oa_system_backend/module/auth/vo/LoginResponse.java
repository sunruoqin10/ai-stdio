package com.example.oa_system_backend.module.auth.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
    private Long expiresIn; // 秒
    private UserInfoVO userInfo;
}
