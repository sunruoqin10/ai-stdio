package com.oa.module.auth.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class RefreshTokenRequest {
    @NotBlank(message = "刷新Token不能为空")
    private String refreshToken;
}
