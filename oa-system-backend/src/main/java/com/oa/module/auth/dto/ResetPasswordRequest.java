package com.oa.module.auth.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class ResetPasswordRequest {
    @NotBlank(message = "账号不能为空")
    private String account;
    
    @NotBlank(message = "类型不能为空")
    private String type;
    
    @NotBlank(message = "验证码不能为空")
    private String code;
    
    @NotBlank(message = "新密码不能为空")
    private String newPassword;
}
