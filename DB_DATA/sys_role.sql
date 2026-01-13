-- 角色表数据
INSERT INTO `sys_role` (`id`, `role_code`, `role_name`, `role_type`, `description`, `sort_order`, `status`, `is_preset`, `created_at`, `updated_at`, `deleted_at`) VALUES
(1, 'ROLE_ADMIN', '超级管理员', 'system', '系统所有权限', 1, 1, 1, '2026-01-12 13:38:04', '2026-01-12 13:38:04', NULL),
(2, 'ROLE_USER', '普通用户', 'system', '基础权限', 2, 1, 1, '2026-01-12 13:38:04', '2026-01-12 13:38:04', NULL),
(3, 'ROLE_HR', '人事专员', 'custom', '员工管理与考勤权限', 3, 1, 0, '2026-01-12 13:38:04', '2026-01-12 13:38:04', NULL),
(4, 'ROLE_FINANCE', '财务专员', 'custom', '报销与资产权限', 4, 1, 0, '2026-01-12 13:38:04', '2026-01-12 13:38:04', NULL);
