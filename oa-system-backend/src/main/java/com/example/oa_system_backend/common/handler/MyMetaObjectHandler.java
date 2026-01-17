package com.example.oa_system_backend.common.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus 元数据处理器
 * 自动填充 created_at, updated_at, created_by, updated_by 字段
 */
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    /**
     * 插入时自动填充
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "createdAt", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());

        // 从 Spring Security 上下文中获取当前用户 ID
        String userId = getCurrentUserId();
        this.strictInsertFill(metaObject, "createdBy", String.class, userId);
        this.strictInsertFill(metaObject, "updatedBy", String.class, userId);
    }

    /**
     * 更新时自动填充
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());

        // 从 Spring Security 上下文中获取当前用户 ID
        String userId = getCurrentUserId();
        this.strictUpdateFill(metaObject, "updatedBy", String.class, userId);
    }

    /**
     * 获取当前登录用户 ID
     * TODO: 集成 Spring Security 后从 SecurityContext 中获取
     */
    private String getCurrentUserId() {
        // 暂时返回默认值，等集成认证模块后从 SecurityContext 获取
        try {
            org.springframework.security.core.context.SecurityContext context =
                org.springframework.security.core.context.SecurityContextHolder.getContext();
            if (context != null && context.getAuthentication() != null) {
                Object principal = context.getAuthentication().getPrincipal();
                if (principal instanceof String) {
                    return (String) principal;
                }
                // 如果是自定义 UserDetails 对象，需要调整这里的代码
            }
        } catch (Exception e) {
            // 忽略异常，返回默认值
        }
        return "system";
    }
}
