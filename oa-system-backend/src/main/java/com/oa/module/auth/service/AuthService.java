package com.oa.module.auth.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oa.common.vo.ApiResponse;
import com.oa.module.auth.dto.*;
import com.oa.module.auth.vo.*;

public interface AuthService {
    LoginResponse login(LoginRequest request);
    void logout();
    RefreshTokenResponse refreshToken(RefreshTokenRequest request);
    CaptchaResponse getCaptcha();
    void sendCode(SendCodeRequest request);
    void resetPassword(ResetPasswordRequest request);
    Page<UserSessionVO> getSessions(int page, int size);
    void deleteSession(String sessionId);
    Page<LoginLogVO> getLoginLogs(int page, int size, String startDate, String endDate);
}
