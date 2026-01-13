package com.example.oa_system_backend.module.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "用户名不能为空")
    private String username; // 支持 用户名/邮箱/手机号

    @NotBlank(message = "密码不能为空")
    private String password;

    private String captcha;

    private String captchaKey;

    private Boolean remember;
}
