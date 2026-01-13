DROP TABLE IF EXISTS `auth_password_history`;
CREATE TABLE `auth_password_history` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '历史ID',
  `user_id` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户ID',
  `password` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码(明文存储)',
  `changed_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='密码历史表';
