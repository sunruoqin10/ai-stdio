-- 插入测试员工数据
-- 注意：需要先有部门数据，这里假设部门ID为 'DEPT001'

USE oa_system;

-- 清理测试数据（可选）
-- DELETE FROM sys_employee WHERE id LIKE 'EMP%';
-- DELETE FROM sys_employee_operation_log WHERE employee_id LIKE 'EMP%';

-- 插入测试员工数据
INSERT INTO sys_employee (
    id, name, english_name, gender, birth_date, phone, email, avatar,
    department_id, position, level, manager_id, join_date,
    probation_status, probation_end_date, work_years, status,
    office_location, emergency_contact, emergency_phone,
    created_at, created_by, updated_at, updated_by, version
) VALUES
(
    'EMP20250113001', '张三', 'Zhang San', 'male', '1990-05-15',
    '13800138001', 'zhangsan@example.com',
    'https://api.dicebear.com/7.x/avataaars/svg?seed=ZhangSan',
    'DEPT001', '软件工程师', 'P3', NULL, '2024-01-15',
    'regular', '2024-04-15', 1, 'active',
    'A栋301', '李四', '13900139001',
    '2024-01-15 10:00:00', 'ADMIN', '2024-01-15 10:00:00', 'ADMIN', 1
),
(
    'EMP20250113002', '李四', 'Li Si', 'female', '1992-08-22',
    '13800138002', 'lisi@example.com',
    'https://api.dicebear.com/7.x/avataaars/svg?seed=LiSi',
    'DEPT001', '高级软件工程师', 'P4', 'EMP20250113001', '2023-06-01',
    'regular', '2023-09-01', 2, 'active',
    'A栋302', '王五', '13900139002',
    '2023-06-01 10:00:00', 'ADMIN', '2023-06-01 10:00:00', 'ADMIN', 1
),
(
    'EMP20250113003', '王五', 'Wang Wu', 'male', '1995-03-10',
    '13800138003', 'wangwu@example.com',
    'https://api.dicebear.com/7.x/avataaars/svg?seed=WangWu',
    'DEPT001', '产品经理', 'P4', NULL, '2023-03-15',
    'regular', '2023-06-15', 2, 'active',
    'B栋201', '赵六', '13900139003',
    '2023-03-15 10:00:00', 'ADMIN', '2023-03-15 10:00:00', 'ADMIN', 1
),
(
    'EMP20250113004', '赵六', 'Zhao Liu', 'female', '1998-11-28',
    '13800138004', 'zhaoliu@example.com',
    'https://api.dicebear.com/7.x/avataaars/svg?seed=ZhaoLiu',
    'DEPT001', 'UI设计师', 'P3', 'EMP20250113003', '2024-09-01',
    'probation', '2024-12-01', 0, 'active',
    'B栋202', '孙七', '13900139004',
    '2024-09-01 10:00:00', 'ADMIN', '2024-09-01 10:00:00', 'ADMIN', 1
),
(
    'EMP20250113005', '孙七', 'Sun Qi', 'male', '1991-07-05',
    '13800138005', 'sunqi@example.com',
    'https://api.dicebear.com/7.x/avataaars/svg?seed=SunQi',
    'DEPT001', '测试工程师', 'P3', 'EMP20250113001', '2024-11-01',
    'probation', '2025-02-01', 0, 'active',
    'A栋303', '周八', '13900139005',
    '2024-11-01 10:00:00', 'ADMIN', '2024-11-01 10:00:00', 'ADMIN', 1
),
(
    'EMP20250113006', '周八', 'Zhou Ba', 'female', '1993-04-18',
    '13800138006', 'zhouba@example.com',
    'https://api.dicebear.com/7.x/avataaars/svg?seed=ZhouBa',
    'DEPT001', '技术经理', 'P5', NULL, '2022-01-10',
    'regular', '2022-04-10', 3, 'active',
    'C栋101', '吴九', '13900139006',
    '2022-01-10 10:00:00', 'ADMIN', '2022-01-10 10:00:00', 'ADMIN', 1
);

-- 插入操作日志
INSERT INTO sys_employee_operation_log (
    employee_id, operation, operator_id, operator_name, operation_time, details, ip_address, user_agent, created_at
) VALUES
('EMP20250113001', 'CREATE', 'ADMIN', '系统管理员', '2024-01-15 10:00:00', '创建员工: 张三', '127.0.0.1', 'Mozilla/5.0', '2024-01-15 10:00:00'),
('EMP20250113002', 'CREATE', 'ADMIN', '系统管理员', '2023-06-01 10:00:00', '创建员工: 李四', '127.0.0.1', 'Mozilla/5.0', '2023-06-01 10:00:00'),
('EMP20250113003', 'CREATE', 'ADMIN', '系统管理员', '2023-03-15 10:00:00', '创建员工: 王五', '127.0.0.1', 'Mozilla/5.0', '2023-03-15 10:00:00'),
('EMP20250113004', 'CREATE', 'ADMIN', '系统管理员', '2024-09-01 10:00:00', '创建员工: 赵六', '127.0.0.1', 'Mozilla/5.0', '2024-09-01 10:00:00'),
('EMP20250113005', 'CREATE', 'ADMIN', '系统管理员', '2024-11-01 10:00:00', '创建员工: 孙七', '127.0.0.1', 'Mozilla/5.0', '2024-11-01 10:00:00'),
('EMP20250113006', 'CREATE', 'ADMIN', '系统管理员', '2022-01-10 10:00:00', '创建员工: 周八', '127.0.0.1', 'Mozilla/5.0', '2022-01-10 10:00:00');

-- 查询插入的数据
SELECT
    id AS '员工编号',
    name AS '姓名',
    gender AS '性别',
    phone AS '电话',
    email AS '邮箱',
    position AS '职位',
    status AS '状态',
    probation_status AS '试用期状态',
    join_date AS '入职日期',
    work_years AS '工龄'
FROM sys_employee
WHERE is_deleted = 0
ORDER BY join_date DESC;
