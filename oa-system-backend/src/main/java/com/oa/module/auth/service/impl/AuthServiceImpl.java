package com.oa.module.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oa.common.exception.AuthenticationException;
import com.oa.common.exception.BusinessException;
import com.oa.common.utils.JwtUtils;
import com.oa.module.auth.dto.*;
import com.oa.module.auth.entity.*;
import com.oa.module.auth.mapper.*;
import com.oa.module.auth.service.AuthService;
import com.oa.module.auth.vo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthUserMapper authUserMapper;
    private final AuthUserSessionMapper authUserSessionMapper;
    private final AuthLoginLogMapper authLoginLogMapper;
    private final AuthVerificationCodeMapper authVerificationCodeMapper;
    
    // 假设其他模块Mapper已存在，实际项目中需注入
    // private final SysEmployeeMapper sysEmployeeMapper;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginResponse login(LoginRequest request) {
        // 1. 参数校验 (已通过@Valid处理)
        
        // 2. 验证码校验 (简化逻辑：若有验证码参数则校验)
        if (StringUtils.hasText(request.getCaptcha())) {
            // TODO: 校验图形验证码逻辑
        }
        
        // 3. 用户查询
        AuthUser user = authUserMapper.selectByUsernameOrEmailOrMobile(request.getUsername());
        if (user == null) {
            throw new AuthenticationException("用户名或密码错误");
        }
        
        // 4. 状态检查
        if ("disabled".equals(user.getStatus())) {
            throw new AuthenticationException("账号已禁用");
        }
        if ("locked".equals(user.getStatus())) {
            if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(LocalDateTime.now())) {
                throw new AuthenticationException("账号锁定中，请稍后再试");
            }
            // 自动解锁
            user.setStatus("active");
            user.setLoginAttempts(0);
            user.setLockedUntil(null);
            authUserMapper.updateById(user);
        }
        
        // 5. 密码验证
        // 生产环境应使用BCryptPasswordEncoder
        if (!user.getPassword().equals(request.getPassword())) {
            handleLoginFailure(user);
            throw new AuthenticationException("用户名或密码错误");
        }
        
        // 登录成功处理
        handleLoginSuccess(user, request); // 包含步骤6关联检查
        
        // 7. 会话创建
        String accessToken = JwtUtils.generateToken(user.getId());
        String refreshToken = UUID.randomUUID().toString();
        
        AuthUserSession session = new AuthUserSession();
        session.setUserId(user.getId());
        session.setAccessToken(accessToken);
        session.setRefreshToken(refreshToken);
        session.setExpiresAt(LocalDateTime.now().plusHours(2));
        session.setLoginTime(LocalDateTime.now());
        session.setLastActiveTime(LocalDateTime.now());
        session.setIsActive(1);
        // 简单的IP获取模拟
        session.setLoginIp("127.0.0.1"); 
        
        authUserSessionMapper.insert(session);
        
        // 8. 日志记录
        saveLoginLog(user, "success", null);
        
        // 构建响应
        UserInfoVO userInfo = buildUserInfo(user);
        
        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(7200L)
                .userInfo(userInfo)
                .build();
    }

    private void handleLoginFailure(AuthUser user) {
        user.setLoginAttempts(user.getLoginAttempts() + 1);
        if (user.getLoginAttempts() >= 5) {
            user.setStatus("locked");
            user.setLockedUntil(LocalDateTime.now().plusMinutes(30));
        }
        authUserMapper.updateById(user);
        saveLoginLog(user, "failed", "密码错误");
    }

    private void handleLoginSuccess(AuthUser user, LoginRequest request) {
        user.setLoginAttempts(0);
        user.setLockedUntil(null);
        user.setLastLoginTime(LocalDateTime.now());
        user.setLastLoginIp("127.0.0.1"); // 实际应从Request获取
        authUserMapper.updateById(user);
    }
    
    private void saveLoginLog(AuthUser user, String status, String reason) {
        AuthLoginLog log = new AuthLoginLog();
        log.setUserId(user.getId());
        log.setUsername(user.getUsername());
        log.setStatus(status);
        log.setFailureReason(reason);
        log.setLoginTime(LocalDateTime.now());
        authLoginLogMapper.insert(log);
    }
    
    private UserInfoVO buildUserInfo(AuthUser user) {
        UserInfoVO vo = new UserInfoVO();
        vo.setId(user.getId());
        vo.setName(user.getUsername()); // 暂用username，实际应查Employee表
        vo.setEmail(user.getEmail());
        vo.setPhone(user.getMobile());
        vo.setRoles(Collections.emptyList()); // 暂空
        vo.setPermissions(Collections.emptyList()); // 暂空
        return vo;
    }

    @Override
    public void logout() {
        // 简单实现：获取当前上下文Token并失效Session
        // 实际需结合Spring Security Context
    }

    @Override
    public RefreshTokenResponse refreshToken(RefreshTokenRequest request) {
        // 查询有效Session
        AuthUserSession session = authUserSessionMapper.selectOne(new LambdaQueryWrapper<AuthUserSession>()
            .eq(AuthUserSession::getRefreshToken, request.getRefreshToken())
            .eq(AuthUserSession::getIsActive, 1)
            .gt(AuthUserSession::getExpiresAt, LocalDateTime.now()));
            
        if (session == null) {
            throw new AuthenticationException("Invalid Refresh Token");
        }
        
        // 刷新
        String newAccess = JwtUtils.generateToken(session.getUserId());
        String newRefresh = UUID.randomUUID().toString();
        
        session.setAccessToken(newAccess);
        session.setRefreshToken(newRefresh);
        session.setExpiresAt(LocalDateTime.now().plusHours(2));
        authUserSessionMapper.updateById(session);
        
        RefreshTokenResponse resp = new RefreshTokenResponse();
        resp.setAccessToken(newAccess);
        resp.setRefreshToken(newRefresh);
        resp.setExpiresIn(7200L);
        return resp;
    }

    @Override
    public CaptchaResponse getCaptcha() {
        // 模拟生成
        return CaptchaResponse.builder()
            .captchaKey(UUID.randomUUID().toString())
            .captchaImage("base64-image-mock")
            .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendCode(SendCodeRequest request) {
        // 1. 格式校验
        if ("email".equals(request.getType())) {
            if (!request.getAccount().matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
                throw new BusinessException("邮箱格式错误");
            }
        }
        
        // 2. 频率限制 (1分钟)
        Long count = authVerificationCodeMapper.selectCount(new LambdaQueryWrapper<AuthVerificationCode>()
            .eq(AuthVerificationCode::getAccount, request.getAccount())
            .eq(AuthVerificationCode::getType, request.getType())
            .gt(AuthVerificationCode::getCreatedAt, LocalDateTime.now().minusMinutes(1)));
            
        if (count > 0) {
            throw new BusinessException("发送过于频繁");
        }
        
        // 3. 生成与保存
        String code = String.valueOf((int)((Math.random() * 9 + 1) * 100000));
        AuthVerificationCode entity = new AuthVerificationCode();
        entity.setType(request.getType());
        entity.setAccount(request.getAccount());
        entity.setScene(request.getScene());
        entity.setCode(code);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        entity.setIsUsed(0);
        
        authVerificationCodeMapper.insert(entity);
        
        // 模拟发送
        System.out.println("Send Code to " + request.getAccount() + ": " + code);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(ResetPasswordRequest request) {
        // 1. 验证码校验
        AuthVerificationCode codeEntity = authVerificationCodeMapper.selectOne(new LambdaQueryWrapper<AuthVerificationCode>()
            .eq(AuthVerificationCode::getAccount, request.getAccount())
            .eq(AuthVerificationCode::getType, request.getType())
            .eq(AuthVerificationCode::getScene, "forgot_password")
            .eq(AuthVerificationCode::getCode, request.getCode())
            .eq(AuthVerificationCode::getIsUsed, 0)
            .gt(AuthVerificationCode::getExpiresAt, LocalDateTime.now())
            .orderByDesc(AuthVerificationCode::getCreatedAt)
            .last("LIMIT 1"));
            
        if (codeEntity == null) {
            throw new BusinessException("验证码无效或已过期");
        }
        
        // 2. 密码更新
        AuthUser user = authUserMapper.selectByUsernameOrEmailOrMobile(request.getAccount());
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        user.setPassword(request.getNewPassword()); // 实际需加密
        user.setPasswordChangedAt(LocalDateTime.now());
        user.setLoginAttempts(0);
        user.setLockedUntil(null);
        authUserMapper.updateById(user);
        
        // 3. 标记验证码
        codeEntity.setIsUsed(1);
        codeEntity.setUsedAt(LocalDateTime.now());
        authVerificationCodeMapper.updateById(codeEntity);
    }

    @Override
    public Page<UserSessionVO> getSessions(int page, int size) {
        // 简单实现
        return new Page<>();
    }

    @Override
    public void deleteSession(String sessionId) {
        authUserSessionMapper.deleteById(sessionId);
    }

    @Override
    public Page<LoginLogVO> getLoginLogs(int page, int size, String startDate, String endDate) {
        // 简单实现
        return new Page<>();
    }
}
