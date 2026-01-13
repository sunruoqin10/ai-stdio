-- Data for table: sys_permission
-- Generated on: 2026-01-13

INSERT INTO `sys_permission` (`id`, `permission_code`, `permission_name`, `permission_type`, `module`, `parent_id`, `route_path`, `component_path`, `icon`, `api_path`, `api_method`, `data_scope`, `sort_order`, `status`, `created_at`, `updated_at`, `deleted_at`) VALUES
(1, 'user:list', '查看用户列表', 'api', 'system', 0, NULL, NULL, NULL, '/api/users', 'GET', NULL, 0, 1, '2026-01-12 13:38:04', '2026-01-12 13:38:04', NULL),
(2, 'user:create', '创建用户', 'api', 'system', 0, NULL, NULL, NULL, '/api/users', 'POST', NULL, 0, 1, '2026-01-12 13:38:04', '2026-01-12 13:38:04', NULL),
(3, 'user:update', '更新用户', 'api', 'system', 0, NULL, NULL, NULL, '/api/users', 'PUT', NULL, 0, 1, '2026-01-12 13:38:04', '2026-01-12 13:38:04', NULL),
(4, 'user:delete', '删除用户', 'api', 'system', 0, NULL, NULL, NULL, '/api/users', 'DELETE', NULL, 0, 1, '2026-01-12 13:38:04', '2026-01-12 13:38:04', NULL);
