package com.example.oa_system_backend.module.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.oa_system_backend.common.exception.*;
import com.example.oa_system_backend.common.utils.*;
import com.example.oa_system_backend.module.auth.dto.*;
import com.example.oa_system_backend.module.auth.entity.*;
import com.example.oa_system_backend.module.auth.mapper.*;
import com.example.oa_system_backend.module.auth.service.AuthService;
import com.example.oa_system_backend.module.auth.vo.*;
import com.example.oa_system_backend.module.department.entity.Department;
import com.example.oa_system_backend.module.department.mapper.DepartmentMapper;
import com.example.oa_system_backend.module.employee.entity.Employee;
import com.example.oa_system_backend.module.employee.mapper.EmployeeMapper;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthUserMapper authUserMapper;
    private final AuthUserSessionMapper authUserSessionMapper;
    private final AuthLoginLogMapper authLoginLogMapper;
    private final AuthVerificationCodeMapper authVerificationCodeMapper;
    private final JwtUtils jwtUtils;
    private final JavaMailSender mailSender;
    private final CaptchaUtils captchaUtils;
    private final UserAgentUtils userAgentUtils;
    private final EmployeeMapper employeeMapper;
    private final DepartmentMapper departmentMapper;

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request, String ip, String userAgent) {
        // 1. Parameter validation
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            throw new BusinessException("用户名不能为空");
        }
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new BusinessException("密码不能为空");
        }

        // 2. User query
        AuthUser user = findUser(request.getUsername());
        if (user == null) {
            throw new AuthenticationException("用户名或密码错误");
        }

        // 3. Status check
        checkUserStatus(user);

        // 4. Captcha validation (if failed > 3 times)
        if (user.getLoginAttempts() != null && user.getLoginAttempts() > 3) {
            if (request.getCaptcha() == null || request.getCaptcha().trim().isEmpty()) {
                throw new CaptchaException("请输入验证码");
            }
            if (!captchaUtils.validateCaptcha(request.getCaptchaKey(), request.getCaptcha())) {
                throw new CaptchaException("验证码错误或已过期");
            }
        }

        // 5. Password validation
        if (!PasswordUtils.matches(request.getPassword(), user.getPassword())) {
            handleFailedLogin(user, ip, userAgent);
            throw new AuthenticationException("用户名或密码错误");
        }

        // 6. Success handling
        handleSuccessfulLogin(user, ip);

        // 7. Generate tokens
        String accessToken = jwtUtils.generateToken(user.getId(), user.getUsername());
        String refreshToken = jwtUtils.generateRefreshToken(user.getId());

        // 8. Create session
        AuthUserSession session = createSession(user, accessToken, refreshToken, ip, userAgent);
        authUserSessionMapper.insert(session);

        // 9. Build user info
        UserInfoVO userInfo = buildUserInfo(user);

        // 10. Log success
        saveLoginLog(user, ip, userAgent, "success", null);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtUtils.getExpiration())
                .userInfo(userInfo)
                .build();
    }

    @Override
    @Transactional
    public void logout(String accessToken) {
        if (accessToken != null) {
            Claims claims = jwtUtils.parseToken(accessToken);
            String userId = claims.get("userId", String.class);

            QueryWrapper<AuthUserSession> wrapper = new QueryWrapper<>();
            wrapper.eq("user_id", userId)
                    .eq("token", accessToken);

            AuthUserSession session = authUserSessionMapper.selectOne(wrapper);
            if (session != null) {
                // Use logical delete (sets is_deleted to 1)
                authUserSessionMapper.deleteById(session.getId());
            }
        }
    }

    @Override
    @Transactional
    public RefreshTokenResponse refreshToken(RefreshTokenRequest request) {
        if (request.getRefreshToken() == null || request.getRefreshToken().trim().isEmpty()) {
            throw new TokenInvalidException("刷新令牌不能为空");
        }

        // Validate refresh token
        if (!jwtUtils.validateToken(request.getRefreshToken())) {
            throw new TokenInvalidException("刷新令牌无效或已过期");
        }

        Claims claims = jwtUtils.parseToken(request.getRefreshToken());
        String type = claims.get("type", String.class);
        if (!"refresh".equals(type)) {
            throw new TokenInvalidException("令牌类型错误");
        }

        String userId = claims.get("userId", String.class);

        // Check active session
        AuthUserSession session = authUserSessionMapper.selectActiveSessionByRefreshToken(request.getRefreshToken());
        if (session == null) {
            throw new TokenInvalidException("会话不存在或已失效");
        }

        // Generate new tokens
        AuthUser user = authUserMapper.selectById(userId);
        if (user == null) {
            throw new AuthenticationException("用户不存在");
        }

        String newAccessToken = jwtUtils.generateToken(user.getId(), user.getUsername());
        String newRefreshToken = jwtUtils.generateRefreshToken(user.getId());

        // Update session
        session.setAccessToken(newAccessToken);
        session.setRefreshToken(newRefreshToken);
        session.setExpiresAt(LocalDateTime.now().plus(Duration.ofMillis(jwtUtils.getExpiration() * 1000)));
        authUserSessionMapper.updateById(session);

        return new RefreshTokenResponse(newAccessToken, newRefreshToken, "Bearer", jwtUtils.getExpiration());
    }

    @Override
    public CaptchaResponse getCaptcha() {
        return captchaUtils.generateCaptcha();
    }

    @Override
    @Transactional
    public void sendCode(SendCodeRequest request) {
        String account = request.getAccount();
        String scene = request.getScene();

        // Validate account format
        boolean isEmail = isValidEmail(account);
        boolean isMobile = isValidMobile(account);

        if (!isEmail && !isMobile) {
            throw new BusinessException("账号格式不正确");
        }

        // Check frequency limit
        int recentCount = authVerificationCodeMapper.countRecentCodesByAccount(account, 1);
        if (recentCount > 0) {
            throw new BusinessException("发送过于频繁，请稍后再试");
        }

        // Generate verification code
        String code = generateVerificationCode();
        String type = isEmail ? "email" : "mobile";

        // Calculate expiration
        LocalDateTime expiresAt = isEmail ?
                LocalDateTime.now().plusMinutes(10) :
                LocalDateTime.now().plusMinutes(5);

        // Save to database
        AuthVerificationCode verificationCode = new AuthVerificationCode();
        verificationCode.setType(type);
        verificationCode.setAccount(account);
        verificationCode.setScene(scene);
        verificationCode.setCode(code);
        verificationCode.setIsUsed(0);
        verificationCode.setExpiresAt(expiresAt);
        verificationCode.setCreatedAt(LocalDateTime.now());
        authVerificationCodeMapper.insert(verificationCode);

        // Send code
        if (isEmail) {
            sendEmailCode(account, code, scene);
        } else {
            // TODO: Implement SMS sending
            log.info("Send SMS code to {}: {}", account, code);
        }
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException("两次密码输入不一致");
        }

        // Validate verification code
        AuthVerificationCode verificationCode = authVerificationCodeMapper.selectValidCode(
                request.getAccount(), request.getCode(), "forgot_password");

        if (verificationCode == null) {
            throw new BusinessException("验证码无效或已过期");
        }

        // Check if code is expired
        if (LocalDateTime.now().isAfter(verificationCode.getExpiresAt())) {
            throw new BusinessException("验证码已过期");
        }

        // Find user
        AuthUser user = findUser(request.getAccount());
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // Validate password strength
        if (!PasswordUtils.isValidPassword(request.getNewPassword())) {
            throw new BusinessException("密码强度不足，必须包含大小写字母和数字，且长度不少于8位");
        }

        // TODO: Check password history (auth_password_history table)

        // Update password
        user.setPassword(PasswordUtils.encode(request.getNewPassword()));
        user.setPasswordChangedAt(LocalDateTime.now());
        user.setLoginAttempts(0);
        user.setStatus("active");
        user.setLockedUntil(null);
        authUserMapper.updateById(user);

        // Mark verification code as used
        verificationCode.setIsUsed(1);
        verificationCode.setUsedAt(LocalDateTime.now());
        authVerificationCodeMapper.updateById(verificationCode);

        // TODO: Insert into auth_password_history

        log.info("Password reset successfully for user: {}", user.getUsername());
    }

    @Override
    public Page<UserSessionVO> getActiveSessions(int page, int size) {
        // TODO: Get current user from security context
        // For now, return empty page
        return new Page<>(page, size);
    }

    @Override
    @Transactional
    public void revokeSession(String sessionId) {
        AuthUserSession session = authUserSessionMapper.selectById(sessionId);
        if (session != null) {
            // Since we don't have is_active and logout_time fields, we'll delete the session
            authUserSessionMapper.deleteById(sessionId);
        }
    }

    @Override
    public Page<LoginLogVO> getLoginLogs(int page, int size, String startDate, String endDate) {
        // TODO: Get current user from security context
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime start = startDate != null ? LocalDateTime.parse(startDate, formatter) : null;
        LocalDateTime end = endDate != null ? LocalDateTime.parse(endDate, formatter) : null;

        Page<AuthLoginLog> pageParam = new Page<>(page, size);
        Page<AuthLoginLog> logPage = authLoginLogMapper.selectPage(pageParam, null);

        Page<LoginLogVO> voPage = new Page<>(page, size);
        voPage.setRecords(logPage.getRecords().stream().map(this::convertToLoginLogVO).toList());
        voPage.setTotal(logPage.getTotal());

        return voPage;
    }

    // Helper methods

    private AuthUser findUser(String username) {
        return authUserMapper.selectByUsernameOrEmailOrMobile(username, username, username);
    }

    private void checkUserStatus(AuthUser user) {
        if ("disabled".equals(user.getStatus())) {
            throw new AccountDisabledException("账号已禁用");
        }

        if ("locked".equals(user.getStatus())) {
            if (user.getLockedUntil() != null && LocalDateTime.now().isBefore(user.getLockedUntil())) {
                long minutes = Duration.between(LocalDateTime.now(), user.getLockedUntil()).toMinutes();
                throw new AccountLockedException("账号锁定中，请于" + minutes + "分钟后重试");
            } else {
                // Auto unlock
                user.setStatus("active");
                user.setLoginAttempts(0);
                user.setLockedUntil(null);
                authUserMapper.updateById(user);
            }
        }
    }

    private void handleFailedLogin(AuthUser user, String ip, String userAgent) {
        int attempts = user.getLoginAttempts() == null ? 1 : user.getLoginAttempts() + 1;
        user.setLoginAttempts(attempts);

        if (attempts >= 5) {
            user.setStatus("locked");
            user.setLockedUntil(LocalDateTime.now().plusMinutes(30));
        }

        authUserMapper.updateById(user);
        saveLoginLog(user, ip, userAgent, "failed", "密码错误");
    }

    private void handleSuccessfulLogin(AuthUser user, String ip) {
        user.setLoginAttempts(0);
        user.setLockedUntil(null);
        user.setLastLoginTime(LocalDateTime.now());
        user.setLastLoginIp(ip);
        authUserMapper.updateById(user);
    }

    private AuthUserSession createSession(AuthUser user, String accessToken, String refreshToken, String ip, String userAgent) {
        AuthUserSession session = new AuthUserSession();
        session.setUserId(user.getId());
        session.setAccessToken(accessToken);
        session.setRefreshToken(refreshToken);
        session.setExpiresAt(LocalDateTime.now().plusSeconds(jwtUtils.getExpiration()));
        session.setIpAddress(ip);
        session.setCreatedAt(LocalDateTime.now());

        // Parse user agent
        DeviceInfo deviceInfo = userAgentUtils.parseUserAgent(userAgent);
        session.setDeviceType(deviceInfo.getDeviceType());
        session.setDeviceName(deviceInfo.getDeviceType() + " " + deviceInfo.getOs());
        session.setUserAgent(userAgent);

        return session;
    }

    private UserInfoVO buildUserInfo(AuthUser user) {
        UserInfoVO userInfo = new UserInfoVO();
        userInfo.setId(user.getId());
        
        // 从员工表获取详细信息
        Employee employee = employeeMapper.selectById(user.getId());
        if (employee != null) {
            userInfo.setEmployeeNo(employee.getId() != null ? employee.getId() : "");
            userInfo.setName(employee.getName() != null ? employee.getName() : user.getUsername());
            userInfo.setEmail(employee.getEmail() != null ? employee.getEmail() : user.getEmail());
            userInfo.setPhone(employee.getPhone() != null ? employee.getPhone() : user.getMobile());
            userInfo.setAvatar(employee.getAvatar() != null ? employee.getAvatar() : "");
            userInfo.setDepartmentId(employee.getDepartmentId() != null ? employee.getDepartmentId() : "");
            userInfo.setPosition(employee.getPosition() != null ? employee.getPosition() : "");
            
            // 获取部门名称
            if (employee.getDepartmentId() != null && !employee.getDepartmentId().isEmpty()) {
                Department department = departmentMapper.selectById(employee.getDepartmentId());
                if (department != null) {
                    userInfo.setDepartmentName(department.getName());
                } else {
                    userInfo.setDepartmentName("");
                }
            } else {
                userInfo.setDepartmentName("");
            }
        } else {
            // 如果员工信息不存在,使用 auth_user 表的信息
            userInfo.setEmployeeNo("");
            userInfo.setName(user.getUsername());
            userInfo.setEmail(user.getEmail());
            userInfo.setPhone(user.getMobile());
            userInfo.setAvatar("");
            userInfo.setDepartmentId("");
            userInfo.setDepartmentName("");
            userInfo.setPosition("");
        }
        
        // TODO: 获取角色和权限信息
        userInfo.setRoles(java.util.List.of());
        userInfo.setPermissions(java.util.List.of());
        return userInfo;
    }

    private void saveLoginLog(AuthUser user, String ip, String userAgent, String status, String failureReason) {
        AuthLoginLog log = new AuthLoginLog();
        log.setUserId(user.getId());
        log.setUsername(user.getUsername());
        log.setLoginIp(ip);
        log.setLocation("Unknown"); // TODO: Implement IP location lookup
        log.setCreatedAt(LocalDateTime.now());

        // Parse user agent for device info
        DeviceInfo deviceInfo = userAgentUtils.parseUserAgent(userAgent);
        log.setDeviceInfo(deviceInfo.getDeviceType());

        log.setStatus(status);
        log.setFailureReason(failureReason);
        authLoginLogMapper.insert(log);
    }

    private LoginLogVO convertToLoginLogVO(AuthLoginLog log) {
        LoginLogVO vo = new LoginLogVO();
        vo.setId(log.getId());
        vo.setUsername(log.getUsername());
        vo.setLoginIp(log.getLoginIp());
        vo.setLoginLocation(log.getLocation());
        vo.setDeviceInfo(log.getDeviceInfo());
        vo.setBrowser(""); // Not stored in DB
        vo.setOs(""); // Not stored in DB
        vo.setStatus(log.getStatus());
        vo.setFailureReason(log.getFailureReason());
        vo.setLoginTime(log.getCreatedAt());
        return vo;
    }

    private boolean isValidEmail(String email) {
        String regex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    private boolean isValidMobile(String mobile) {
        String regex = "^1[3-9][0-9]{9}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(mobile);
        return matcher.matches();
    }

    private String generateVerificationCode() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(1000000));
    }

    private void sendEmailCode(String email, String code, String scene) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("验证码");

            String sceneText = switch (scene) {
                case "forgot_password" -> "重置密码";
                case "login" -> "登录";
                case "register" -> "注册";
                default -> "验证";
            };

            message.setText("您的" + sceneText + "验证码是: " + code + "，验证码有效期为10分钟。");
            mailSender.send(message);
            log.info("Email sent to {} with code: {}", email, code);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", email, e.getMessage());
            throw new BusinessException("邮件发送失败，请稍后再试");
        }
    }
}
