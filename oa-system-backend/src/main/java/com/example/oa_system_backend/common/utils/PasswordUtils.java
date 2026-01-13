package com.example.oa_system_backend.common.utils;

import org.springframework.stereotype.Component;

@Component
public class PasswordUtils {

    /**
     * 密码编码 - 暂时使用明文存储
     * TODO: 生产环境应该使用BCrypt等加密算法
     */
    public static String encode(String rawPassword) {
        return rawPassword;
    }

    /**
     * 密码匹配 - 暂时使用明文比较
     * TODO: 生产环境应该使用BCrypt等加密算法
     */
    public static boolean matches(String rawPassword, String encodedPassword) {
        return rawPassword != null && rawPassword.equals(encodedPassword);
    }

    /**
     * 验证密码强度
     */
    public static boolean isValidPassword(String password) {
        if (password == null || password.length() < 6) {
            return false;
        }
        return true;
    }
}
