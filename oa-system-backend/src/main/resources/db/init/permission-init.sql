-- 角色初始数据
INSERT INTO `sys_role` (`id`, `name`, `code`, `type`, `sort`, `description`, `status`, `is_preset`, `created_at`, `created_by`, `updated_at`, `updated_by`) VALUES
('ROLE0001', '超级管理员', 'admin', 'system', 1, '拥有系统所有权限', 'active', 1, NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
('ROLE0002', '部门管理员', 'dept_admin', 'custom', 2, '管理本部门相关业务', 'active', 0, NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
('ROLE0003', '普通员工', 'employee', 'custom', 3, '普通员工权限', 'active', 0, NOW(), 'SYSTEM', NOW(), 'SYSTEM');

-- 权限初始数据
INSERT INTO `sys_permission` (`id`, `name`, `code`, `type`, `module`, `parent_id`, `path`, `component`, `icon`, `api_path`, `api_method`, `data_scope`, `sort`, `status`, `created_at`, `created_by`, `updated_at`, `updated_by`) VALUES
-- 系统管理
('PERM001', '系统管理', 'system', 'menu', 'system', NULL, '/system', '@/views/system/Index', 'Setting', NULL, NULL, NULL, 1, 'active', NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
('PERM0011', '用户管理', 'system:user:list', 'menu', 'system', 'PERM001', '/system/user', '@/views/system/user/List', 'User', '/api/users', 'GET', NULL, 1, 'active', NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
('PERM00111', '添加用户', 'system:user:add', 'button', 'system', 'PERM0011', NULL, NULL, NULL, '/api/users', 'POST', NULL, 1, 'active', NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
('PERM00112', '编辑用户', 'system:user:edit', 'button', 'system', 'PERM0011', NULL, NULL, NULL, '/api/users/{id}', 'PUT', NULL, 2, 'active', NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
('PERM00113', '删除用户', 'system:user:delete', 'button', 'system', 'PERM0011', NULL, NULL, NULL, '/api/users/{id}', 'DELETE', NULL, 3, 'active', NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
('PERM0012', '角色管理', 'system:role:list', 'menu', 'system', 'PERM001', '/system/role', '@/views/system/role/List', 'UserFilled', '/api/roles', 'GET', NULL, 2, 'active', NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
('PERM00121', '添加角色', 'system:role:add', 'button', 'system', 'PERM0012', NULL, NULL, NULL, '/api/roles', 'POST', NULL, 1, 'active', NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
('PERM00122', '编辑角色', 'system:role:edit', 'button', 'system', 'PERM0012', NULL, NULL, NULL, '/api/roles/{id}', 'PUT', NULL, 2, 'active', NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
('PERM00123', '删除角色', 'system:role:delete', 'button', 'system', 'PERM0012', NULL, NULL, NULL, '/api/roles/{id}', 'DELETE', NULL, 3, 'active', NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
('PERM0013', '权限管理', 'system:permission:list', 'menu', 'system', 'PERM001', '/system/permission', '@/views/system/permission/List', 'Lock', '/api/permissions', 'GET', NULL, 3, 'active', NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
('PERM00131', '添加权限', 'system:permission:add', 'button', 'system', 'PERM0013', NULL, NULL, NULL, '/api/permissions', 'POST', NULL, 1, 'active', NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
('PERM00132', '编辑权限', 'system:permission:edit', 'button', 'system', 'PERM0013', NULL, NULL, NULL, '/api/permissions/{id}', 'PUT', NULL, 2, 'active', NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
('PERM00133', '删除权限', 'system:permission:delete', 'button', 'system', 'PERM0013', NULL, NULL, NULL, '/api/permissions/{id}', 'DELETE', NULL, 3, 'active', NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
('PERM0014', '字典管理', 'system:dict:list', 'menu', 'system', 'PERM001', '/system/dict', '@/views/system/dict/List', 'Collection', '/api/dict', 'GET', NULL, 4, 'active', NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
('PERM00141', '添加字典', 'system:dict:add', 'button', 'system', 'PERM0014', NULL, NULL, NULL, '/api/dict', 'POST', NULL, 1, 'active', NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
('PERM00142', '编辑字典', 'system:dict:edit', 'button', 'system', 'PERM0014', NULL, NULL, NULL, '/api/dict/{id}', 'PUT', NULL, 2, 'active', NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
('PERM00143', '删除字典', 'system:dict:delete', 'button', 'system', 'PERM0014', NULL, NULL, NULL, '/api/dict/{id}', 'DELETE', NULL, 3, 'active', NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
-- 员工管理
('PERM002', '员工管理', 'employee', 'menu', 'employee', NULL, '/employee', '@/views/employee/Index', 'Avatar', '/api/employees', 'GET', NULL, 2, 'active', NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
('PERM0021', '员工列表', 'employee:view:list', 'menu', 'employee', 'PERM002', '/employee/list', '@/views/employee/List', 'List', '/api/employees', 'GET', NULL, 1, 'active', NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
('PERM00211', '添加员工', 'employee:add', 'button', 'employee', 'PERM0021', NULL, NULL, NULL, '/api/employees', 'POST', NULL, 1, 'active', NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
('PERM00212', '编辑员工', 'employee:edit', 'button', 'employee', 'PERM0021', NULL, NULL, NULL, '/api/employees/{id}', 'PUT', NULL, 2, 'active', NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
('PERM00213', '删除员工', 'employee:delete', 'button', 'employee', 'PERM0021', NULL, NULL, NULL, '/api/employees/{id}', 'DELETE', NULL, 3, 'active', NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
-- 部门管理
('PERM003', '部门管理', 'department', 'menu', 'department', NULL, '/department', '@/views/department/Index', 'OfficeBuilding', '/api/departments', 'GET', NULL, 3, 'active', NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
('PERM0031', '部门列表', 'department:view:list', 'menu', 'department', 'PERM003', '/department/list', '@/views/department/List', 'List', '/api/departments', 'GET', NULL, 1, 'active', NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
('PERM00311', '添加部门', 'department:add', 'button', 'department', 'PERM0031', NULL, NULL, NULL, '/api/departments', 'POST', NULL, 1, 'active', NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
('PERM00312', '编辑部门', 'department:edit', 'button', 'department', 'PERM0031', NULL, NULL, NULL, '/api/departments/{id}', 'PUT', NULL, 2, 'active', NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
('PERM00313', '删除部门', 'department:delete', 'button', 'department', 'PERM0031', NULL, NULL, NULL, '/api/departments/{id}', 'DELETE', NULL, 3, 'active', NOW(), 'SYSTEM', NOW(), 'SYSTEM');

-- 角色权限关联
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `created_at`, `created_by`) VALUES
-- 超级管理员拥有所有权限
('RP0001', 'ROLE0001', 'PERM001', NOW(), 'SYSTEM'),
('RP0002', 'ROLE0001', 'PERM0011', NOW(), 'SYSTEM'),
('RP0003', 'ROLE0001', 'PERM00111', NOW(), 'SYSTEM'),
('RP0004', 'ROLE0001', 'PERM00112', NOW(), 'SYSTEM'),
('RP0005', 'ROLE0001', 'PERM00113', NOW(), 'SYSTEM'),
('RP0006', 'ROLE0001', 'PERM0012', NOW(), 'SYSTEM'),
('RP0007', 'ROLE0001', 'PERM00121', NOW(), 'SYSTEM'),
('RP0008', 'ROLE0001', 'PERM00122', NOW(), 'SYSTEM'),
('RP0009', 'ROLE0001', 'PERM00123', NOW(), 'SYSTEM'),
('RP0010', 'ROLE0001', 'PERM0013', NOW(), 'SYSTEM'),
('RP0011', 'ROLE0001', 'PERM00131', NOW(), 'SYSTEM'),
('RP0012', 'ROLE0001', 'PERM00132', NOW(), 'SYSTEM'),
('RP0013', 'ROLE0001', 'PERM00133', NOW(), 'SYSTEM'),
('RP0014', 'ROLE0001', 'PERM0014', NOW(), 'SYSTEM'),
('RP0015', 'ROLE0001', 'PERM00141', NOW(), 'SYSTEM'),
('RP0016', 'ROLE0001', 'PERM00142', NOW(), 'SYSTEM'),
('RP0017', 'ROLE0001', 'PERM00143', NOW(), 'SYSTEM'),
('RP0018', 'ROLE0001', 'PERM002', NOW(), 'SYSTEM'),
('RP0019', 'ROLE0001', 'PERM0021', NOW(), 'SYSTEM'),
('RP0020', 'ROLE0001', 'PERM00211', NOW(), 'SYSTEM'),
('RP0021', 'ROLE0001', 'PERM00212', NOW(), 'SYSTEM'),
('RP0022', 'ROLE0001', 'PERM00213', NOW(), 'SYSTEM'),
('RP0023', 'ROLE0001', 'PERM003', NOW(), 'SYSTEM'),
('RP0024', 'ROLE0001', 'PERM0031', NOW(), 'SYSTEM'),
('RP0025', 'ROLE0001', 'PERM00311', NOW(), 'SYSTEM'),
('RP0026', 'ROLE0001', 'PERM00312', NOW(), 'SYSTEM'),
('RP0027', 'ROLE0001', 'PERM00313', NOW(), 'SYSTEM');

-- 用户角色关联
INSERT INTO `sys_user_role` (`id`, `user_id`, `role_id`, `created_at`, `created_by`) VALUES
-- 第一个用户是超级管理员
('UR0001', 'EMP0001', 'ROLE0001', NOW(), 'SYSTEM');