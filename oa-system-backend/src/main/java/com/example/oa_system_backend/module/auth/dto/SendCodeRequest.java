package com.example.oa_system_backend.module.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class SendCodeRequest {

    @NotBlank(message = "账号不能为空")
    private String account;

    @NotBlank(message = "场景不能为空")
    private String scene; // forgot_password, login, register
}
