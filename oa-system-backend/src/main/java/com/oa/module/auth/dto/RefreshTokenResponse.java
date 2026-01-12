package com.oa.module.auth.dto;

import lombok.Data;

@Data
public class RefreshTokenResponse {
    private String accessToken;
    private String refreshToken;
    private Long expiresIn;
}
