DROP TABLE IF EXISTS `auth_user`;
CREATE TABLE `auth_user` (
  `id` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户ID(关联employees.id)',
  `username` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '登录用户名',
  `password` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码',
  `email` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '邮箱',
  `mobile` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '手机号',
  `status` enum('active','locked','disabled') COLLATE utf8mb4_unicode_ci DEFAULT 'active' COMMENT '账号状态',
  `failed_login_attempts` int DEFAULT '0' COMMENT '登录失败次数',
  `locked_until` datetime DEFAULT NULL COMMENT '锁定到期时间',
  `password_changed_at` datetime DEFAULT NULL COMMENT '密码最后修改时间',
  `last_login_at` datetime DEFAULT NULL COMMENT '最后登录时间',
  `last_login_ip` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '最后登录IP',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除标记(0-未删除 1-已删除)',
  `login_attempts` int DEFAULT '0' COMMENT '登录失败次数',
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`),
  UNIQUE KEY `email` (`email`),
  UNIQUE KEY `mobile` (`mobile`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户账号表';
