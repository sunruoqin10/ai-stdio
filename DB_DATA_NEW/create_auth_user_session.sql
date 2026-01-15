-- 用户会话表
CREATE TABLE `auth_user_session` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` varchar(50) NOT NULL COMMENT '用户ID',
  `token` text NOT NULL COMMENT '访问令牌（JWT）',
  `refresh_token` text DEFAULT NULL COMMENT '刷新令牌（JWT）',
  `device_type` varchar(50) DEFAULT NULL COMMENT '设备类型（Computer/Mobile/Tablet等）',
  `device_name` varchar(100) DEFAULT NULL COMMENT '设备名称',
  `ip_address` varchar(50) DEFAULT NULL COMMENT 'IP地址',
  `user_agent` varchar(500) DEFAULT NULL COMMENT '用户代理（浏览器信息）',
  `expires_at` datetime DEFAULT NULL COMMENT '过期时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '是否删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_token` (`token`(255)),
  KEY `idx_expires_at` (`expires_at`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户会话表';
