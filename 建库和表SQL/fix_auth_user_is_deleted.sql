-- 修复 auth_user 表 - 添加 is_deleted 字段
-- 这是最小修改，仅解决登录问题

USE oa_system;

-- 检查并添加 is_deleted 字段
ALTER TABLE `auth_user` ADD COLUMN IF NOT EXISTS `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标记(0-未删除 1-已删除)' AFTER `updated_at`;

-- 添加索引以提升查询性能
ALTER TABLE `auth_user` ADD INDEX IF NOT EXISTS `idx_is_deleted` (`is_deleted`);

-- 同样修复其他认证相关的表
ALTER TABLE `auth_login_log` ADD COLUMN IF NOT EXISTS `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标记' AFTER `updated_at`;
ALTER TABLE `auth_login_log` ADD INDEX IF NOT EXISTS `idx_is_deleted` (`is_deleted`);

ALTER TABLE `auth_user_session` ADD COLUMN IF NOT EXISTS `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标记' AFTER `updated_at`;
ALTER TABLE `auth_user_session` ADD INDEX IF NOT EXISTS `idx_is_deleted` (`is_deleted`);

ALTER TABLE `auth_verification_code` ADD COLUMN IF NOT EXISTS `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标记' AFTER `updated_at`;
ALTER TABLE `auth_verification_code` ADD INDEX IF NOT EXISTS `idx_is_deleted` (`is_deleted`);

SELECT '修复完成！auth_user及相关表已添加is_deleted字段' AS message;
