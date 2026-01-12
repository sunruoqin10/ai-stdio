package com.oa.module.auth.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class SendCodeRequest {
    @NotBlank(message = "类型不能为空")
    private String type; // email, mobile
    
    @NotBlank(message = "账号不能为空")
    private String account;
    
    @NotBlank(message = "场景不能为空")
    private String scene; // forgot_password, login
}
