package com.example.oa_system_backend.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Caffeine缓存配置
 *
 * @author OA System
 * @since 2026-01-15
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * 缓存管理器
     */
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();

        // 配置Caffeine缓存（不调用build()）
        cacheManager.setCaffeine(Caffeine.newBuilder()
                // 初始容量
                .initialCapacity(50)
                // 最大容量
                .maximumSize(500)
                // 写入后过期时间 - 5分钟
                .expireAfterWrite(5, TimeUnit.MINUTES)
                // 开启统计
                .recordStats());

        return cacheManager;
    }
}
