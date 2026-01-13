-- 部门信息表数据
INSERT INTO `sys_department` (`id`, `name`, `short_name`, `parent_id`, `leader_id`, `level`, `sort`, `established_date`, `description`, `icon`, `status`, `created_at`, `created_by`, `updated_at`, `updated_by`, `is_deleted`, `deleted_at`, `deleted_by`, `version`) VALUES
('DEPT0001', 'XX科技有限公司', '总公司', NULL, 'EMP20230115001', 1, 1, NULL, NULL, NULL, 'active', '2026-01-12 13:16:11', NULL, '2026-01-12 13:16:11', NULL, 0, NULL, NULL, 0),
('DEPT0002', '技术部', '技术', 'DEPT0001', 'EMP20230115001', 2, 1, NULL, NULL, NULL, 'active', '2026-01-12 13:16:11', NULL, '2026-01-12 13:16:11', NULL, 0, NULL, NULL, 0),
('DEPT0003', '产品部', '产品', 'DEPT0001', 'EMP20230115002', 2, 2, NULL, NULL, NULL, 'active', '2026-01-12 13:16:11', NULL, '2026-01-12 13:16:11', NULL, 0, NULL, NULL, 0),
('DEPT0004', '设计部', '设计', 'DEPT0001', 'EMP20230115003', 2, 3, NULL, NULL, NULL, 'active', '2026-01-12 13:16:11', NULL, '2026-01-12 13:16:11', NULL, 0, NULL, NULL, 0),
('DEPT0005', '前端开发组', '前端', 'DEPT0002', 'EMP20230115004', 3, 1, NULL, NULL, NULL, 'active', '2026-01-12 13:16:11', NULL, '2026-01-12 13:16:11', NULL, 0, NULL, NULL, 0),
('DEPT0006', '后端开发组', '后端', 'DEPT0002', 'EMP20230115001', 3, 2, NULL, NULL, NULL, 'active', '2026-01-12 13:16:11', NULL, '2026-01-12 13:16:11', NULL, 0, NULL, NULL, 0),
('DEPT0007', '测试组', '测试', 'DEPT0002', 'EMP20230115001', 3, 3, NULL, NULL, NULL, 'active', '2026-01-12 13:16:11', NULL, '2026-01-12 13:16:11', NULL, 0, NULL, NULL, 0);
