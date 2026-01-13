DROP TABLE IF EXISTS `auth_verification_code`;
CREATE TABLE `auth_verification_code` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '验证码ID',
  `type` enum('email','mobile') COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '类型: email邮箱 mobile手机',
  `account` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '邮箱/手机号',
  `scene` enum('forgot_password','login','register') COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '场景: forgot_password找回密码 login登录 register注册',
  `code` varchar(10) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '验证码',
  `is_used` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否已使用(0否1是)',
  `used_at` timestamp NULL DEFAULT NULL COMMENT '使用时间',
  `expires_at` timestamp NOT NULL COMMENT '过期时间',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='验证码表';
