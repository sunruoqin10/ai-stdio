package com.oa.module.auth.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CaptchaResponse {
    private String captchaKey;
    private String captchaImage; // Base64
}
