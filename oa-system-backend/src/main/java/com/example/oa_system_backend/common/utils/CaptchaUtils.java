package com.example.oa_system_backend.common.utils;

import com.example.oa_system_backend.module.auth.vo.CaptchaResponse;
import com.wf.captcha.SpecCaptcha;
import com.wf.captcha.base.Captcha;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class CaptchaUtils {

    // 验证码内存缓存
    private final ConcurrentHashMap<String, CaptchaData> captchaCache = new ConcurrentHashMap<>();

    private static final long CAPTCHA_EXPIRE_MINUTES = 5;

    // 验证码数据内部类
    private static class CaptchaData {
        private final String code;
        private final LocalDateTime expireTime;

        public CaptchaData(String code, LocalDateTime expireTime) {
            this.code = code;
            this.expireTime = expireTime;
        }

        public String getCode() {
            return code;
        }

        public LocalDateTime getExpireTime() {
            return expireTime;
        }
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

        // Store in memory cache for 5 minutes
        LocalDateTime expireTime = LocalDateTime.now().plusMinutes(CAPTCHA_EXPIRE_MINUTES);
        captchaCache.put(captchaKey, new CaptchaData(captchaCode, expireTime));

        return new CaptchaResponse(captchaKey, captchaImage);
    }

    public boolean validateCaptcha(String captchaKey, String userInput) {
        if (captchaKey == null || userInput == null) {
            return false;
        }

        CaptchaData captchaData = captchaCache.get(captchaKey);
        if (captchaData == null) {
            return false;
        }

        // Check if expired
        if (LocalDateTime.now().isAfter(captchaData.getExpireTime())) {
            captchaCache.remove(captchaKey);
            return false;
        }

        boolean isValid = captchaData.getCode().equalsIgnoreCase(userInput);

        // Delete captcha after validation (one-time use)
        captchaCache.remove(captchaKey);

        return isValid;
    }
}
