package com.example.oa_system_backend.module.auth.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.oa_system_backend.common.vo.ApiResponse;
import com.example.oa_system_backend.module.auth.dto.*;
import com.example.oa_system_backend.module.auth.service.AuthService;
import com.example.oa_system_backend.module.auth.vo.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request,
                                             HttpServletRequest httpRequest) {
        String ip = getClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");
        LoginResponse response = authService.login(request, ip, userAgent);
        return ApiResponse.success(response);
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        String accessToken = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            accessToken = authHeader.substring(7);
        }
        authService.logout(accessToken);
        return ApiResponse.success("退出成功", null);
    }

    @PostMapping("/refresh")
    public ApiResponse<RefreshTokenResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        RefreshTokenResponse response = authService.refreshToken(request);
        return ApiResponse.success(response);
    }

    @GetMapping("/captcha")
    public ApiResponse<CaptchaResponse> getCaptcha() {
        CaptchaResponse response = authService.getCaptcha();
        return ApiResponse.success(response);
    }

    @PostMapping("/send-code")
    public ApiResponse<Void> sendCode(@Valid @RequestBody SendCodeRequest request) {
        authService.sendCode(request);
        return ApiResponse.success("验证码发送成功", null);
    }

    @PostMapping("/reset-password")
    public ApiResponse<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ApiResponse.success("密码重置成功", null);
    }

    @GetMapping("/sessions")
    public ApiResponse<Page<UserSessionVO>> getActiveSessions(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<UserSessionVO> sessions = authService.getActiveSessions(page, size);
        return ApiResponse.success(sessions);
    }

    @DeleteMapping("/sessions/{id}")
    public ApiResponse<Void> revokeSession(@PathVariable String id) {
        authService.revokeSession(id);
        return ApiResponse.success("会话已撤销", null);
    }

    @GetMapping("/login-logs")
    public ApiResponse<Page<LoginLogVO>> getLoginLogs(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        Page<LoginLogVO> logs = authService.getLoginLogs(page, size, startDate, endDate);
        return ApiResponse.success(logs);
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
