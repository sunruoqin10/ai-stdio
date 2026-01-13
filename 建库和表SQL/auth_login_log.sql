DROP TABLE IF EXISTS `auth_login_log`;
CREATE TABLE `auth_login_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `user_id` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '用户ID',
  `username` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '用户名',
  `login_type` enum('web','mobile','api') COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '登录类型',
  `status` enum('success','failed') COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '登录状态',
  `failure_reason` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '失败原因',
  `ip_address` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'IP地址',
  `location` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '登录地点',
  `device_type` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '设备类型',
  `user_agent` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '用户代理',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
  PRIMARY KEY (`id`),
  KEY `idx_log_user` (`user_id`),
  KEY `idx_log_status` (`status`),
  KEY `idx_log_created` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='登录日志表';
