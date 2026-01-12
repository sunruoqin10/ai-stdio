package com.oa.common.utils;

import java.util.UUID;

public class JwtUtils {
    // 简单的Token生成模拟，实际需引入jjwt等库
    public static String generateToken(String userId) {
        return "eyJhbGciOiJIUzI1NiJ9." + UUID.randomUUID().toString() + "." + userId;
    }
    
    public static String getUserIdFromToken(String token) {
        // 解析逻辑
        return "mock-user-id";
    }
}
