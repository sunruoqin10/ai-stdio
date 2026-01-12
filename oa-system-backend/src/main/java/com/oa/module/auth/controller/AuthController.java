package com.oa.module.auth.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oa.common.vo.ApiResponse;
import com.oa.module.auth.dto.*;
import com.oa.module.auth.service.AuthService;
import com.oa.module.auth.vo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@RequestBody @Valid LoginRequest request) {
        return ApiResponse.success(authService.login(request));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout() {
        authService.logout();
        return ApiResponse.success();
    }

    @PostMapping("/refresh")
    public ApiResponse<RefreshTokenResponse> refreshToken(@RequestBody @Valid RefreshTokenRequest request) {
        return ApiResponse.success(authService.refreshToken(request));
    }

    @GetMapping("/captcha")
    public ApiResponse<CaptchaResponse> getCaptcha() {
        return ApiResponse.success(authService.getCaptcha());
    }

    @PostMapping("/send-code")
    public ApiResponse<Void> sendCode(@RequestBody @Valid SendCodeRequest request) {
        authService.sendCode(request);
        return ApiResponse.success();
    }

    @PostMapping("/reset-password")
    public ApiResponse<Void> resetPassword(@RequestBody @Valid ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ApiResponse.success();
    }

    @GetMapping("/sessions")
    public ApiResponse<Page<UserSessionVO>> getSessions(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(authService.getSessions(page, size));
    }

    @DeleteMapping("/sessions/{id}")
    public ApiResponse<Void> deleteSession(@PathVariable String id) {
        authService.deleteSession(id);
        return ApiResponse.success();
    }

    @GetMapping("/login-logs")
    public ApiResponse<Page<LoginLogVO>> getLoginLogs(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return ApiResponse.success(authService.getLoginLogs(page, size, startDate, endDate));
    }
}
