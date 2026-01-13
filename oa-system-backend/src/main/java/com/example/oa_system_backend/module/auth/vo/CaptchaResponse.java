package com.example.oa_system_backend.module.auth.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CaptchaResponse {
    private String captchaKey;
    private String captchaImage; // Base64 encoded image
}
