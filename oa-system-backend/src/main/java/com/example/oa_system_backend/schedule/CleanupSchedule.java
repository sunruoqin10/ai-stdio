package com.example.oa_system_backend.schedule;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.oa_system_backend.module.auth.entity.AuthUserSession;
import com.example.oa_system_backend.module.auth.entity.AuthVerificationCode;
import com.example.oa_system_backend.module.auth.entity.AuthUser;
import com.example.oa_system_backend.module.auth.mapper.AuthUserSessionMapper;
import com.example.oa_system_backend.module.auth.mapper.AuthVerificationCodeMapper;
import com.example.oa_system_backend.module.auth.mapper.AuthUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class CleanupSchedule {

    private final AuthUserSessionMapper authUserSessionMapper;
    private final AuthVerificationCodeMapper authVerificationCodeMapper;
    private final AuthUserMapper authUserMapper;

    /**
     * Clean expired sessions - runs every hour
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void cleanExpiredSessions() {
        log.info("Starting cleanup of expired sessions...");

        QueryWrapper<AuthUserSession> wrapper = new QueryWrapper<>();
        wrapper.lt("expires_at", LocalDateTime.now());

        int deletedCount = authUserSessionMapper.delete(wrapper);
        log.info("Cleaned up {} expired sessions", deletedCount);
    }

    /**
     * Clean expired verification codes - runs every 30 minutes
     */
    @Scheduled(cron = "0 */30 * * * ?")
    public void cleanExpiredCodes() {
        log.info("Starting cleanup of expired verification codes...");

        QueryWrapper<AuthVerificationCode> wrapper = new QueryWrapper<>();
        wrapper.lt("expires_at", LocalDateTime.now());

        int deletedCount = authVerificationCodeMapper.delete(wrapper);
        log.info("Cleaned up {} expired verification codes", deletedCount);
    }

    /**
     * Unlock accounts - runs every 10 minutes
     * Auto-unlock accounts whose lock period has expired
     */
    @Scheduled(cron = "0 */10 * * * ?")
    public void unlockAccounts() {
        log.info("Starting account unlock check...");

        QueryWrapper<AuthUser> wrapper = new QueryWrapper<>();
        wrapper.eq("status", "locked")
                .lt("locked_until", LocalDateTime.now());

        AuthUser updateEntity = new AuthUser();
        updateEntity.setStatus("active");
        updateEntity.setLoginAttempts(0);
        updateEntity.setLockedUntil(null);

        int updatedCount = authUserMapper.update(updateEntity, wrapper);
        log.info("Unlocked {} accounts", updatedCount);
    }
}
