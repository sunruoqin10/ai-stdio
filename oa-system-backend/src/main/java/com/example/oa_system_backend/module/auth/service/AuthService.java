package com.example.oa_system_backend.module.auth.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.oa_system_backend.module.auth.dto.*;
import com.example.oa_system_backend.module.auth.vo.*;

public interface AuthService {

    LoginResponse login(LoginRequest request, String ip, String userAgent);

    void logout(String accessToken);

    RefreshTokenResponse refreshToken(RefreshTokenRequest request);

    CaptchaResponse getCaptcha();

    void sendCode(SendCodeRequest request);

    void resetPassword(ResetPasswordRequest request);

    Page<UserSessionVO> getActiveSessions(int page, int size);

    void revokeSession(String sessionId);

    Page<LoginLogVO> getLoginLogs(int page, int size, String startDate, String endDate);
}
