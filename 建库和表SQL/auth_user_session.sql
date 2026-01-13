DROP TABLE IF EXISTS `auth_user_session`;
CREATE TABLE `auth_user_session` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '会话ID',
  `user_id` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户ID',
  `token` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'JWT Token',
  `refresh_token` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '刷新Token',
  `device_type` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '设备类型',
  `device_name` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '设备名称',
  `ip_address` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'IP地址',
  `user_agent` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '用户代理',
  `expires_at` datetime NOT NULL COMMENT '过期时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除标记',
  PRIMARY KEY (`id`),
  UNIQUE KEY `token` (`token`),
  KEY `idx_session_user` (`user_id`),
  KEY `idx_session_token` (`token`),
  KEY `idx_session_expires` (`expires_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户会话表';
