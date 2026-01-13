package com.example.oa_system_backend.common.utils;

import com.example.oa_system_backend.module.auth.vo.CaptchaResponse;
import com.wf.captcha.SpecCaptcha;
import com.wf.captcha.base.Captcha;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
public class CaptchaUtils {

    private final StringRedisTemplate redisTemplate;

    public CaptchaUtils(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public String generateCaptchaKey() {
        return "captcha:" + UUID.randomUUID().toString();
    }

    public CaptchaResponse generateCaptcha() {
        // Create captcha: 130x48, 4 characters
        SpecCaptcha captcha = new SpecCaptcha(130, 48, 4);
        captcha.setCharType(Captcha.TYPE_DEFAULT);

        String captchaKey = generateCaptchaKey();
        String captchaCode = captcha.text().toLowerCase();
        String captchaImage = captcha.toBase64();

        // Store in Redis for 5 minutes
        redisTemplate.opsForValue().set(captchaKey, captchaCode, 5, TimeUnit.MINUTES);

        return new CaptchaResponse(captchaKey, captchaImage);
    }

    public boolean validateCaptcha(String captchaKey, String userInput) {
        if (captchaKey == null || userInput == null) {
            return false;
        }

        String storedCode = redisTemplate.opsForValue().get(captchaKey);
        if (storedCode == null) {
            return false;
        }

        boolean isValid = storedCode.equalsIgnoreCase(userInput);

        // Delete captcha after validation (one-time use)
        redisTemplate.delete(captchaKey);

        return isValid;
    }
}
