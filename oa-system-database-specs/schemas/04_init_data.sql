-- OA系统初始数据脚本
-- 版本: v1.0.0
-- 创建日期: 2026-01-11
-- 数据库: MySQL 8.0+

SET NAMES utf8mb4;

-- =============================================
-- 1. 部门初始数据
-- =============================================

INSERT INTO sys_department (
  id, name, short_name, parent_id, leader_id, level, sort, status
) VALUES
('DEPT0001', 'XX科技有限公司', '总公司', NULL, 'EMP20230115001', 1, 1, 'active'),
('DEPT0002', '技术部', '技术', 'DEPT0001', 'EMP20230115001', 2, 1, 'active'),
('DEPT0003', '产品部', '产品', 'DEPT0001', 'EMP20230115002', 2, 2, 'active'),
('DEPT0004', '设计部', '设计', 'DEPT0001', 'EMP20230115003', 2, 3, 'active'),
('DEPT0005', '前端开发组', '前端', 'DEPT0002', 'EMP20230115004', 3, 1, 'active'),
('DEPT0006', '后端开发组', '后端', 'DEPT0002', 'EMP20230115001', 3, 2, 'active'),
('DEPT0007', '测试组', '测试', 'DEPT0002', 'EMP20230115001', 3, 3, 'active');

-- =============================================
-- 2. 员工初始数据
-- =============================================

INSERT INTO sys_employee (
  id, name, gender, phone, email, department_id, position, join_date, status
) VALUES
('EMP20230115001', '张三', 'male', '13800138000', 'zhangsan@company.com',
 'DEPT0001', '技术经理', '2023-01-15', 'active'),
('EMP20230115002', '李四', 'male', '13900139000', 'lisi@company.com',
 'DEPT0003', '产品经理', '2023-02-20', 'active'),
('EMP20230115003', '王五', 'female', '13700137000', 'wangwu@company.com',
 'DEPT0004', 'UI设计师', '2023-03-10', 'active'),
('EMP20230115004', '赵六', 'male', '13600136000', 'zhaoliu@company.com',
 'DEPT0005', '前端工程师', '2024-01-05', 'active');

-- =============================================
-- 3. 用户账号初始数据
-- =============================================

-- 密码均为: 123456 (使用bcrypt哈希)
INSERT INTO auth_user (
  id, username, password_hash, email, mobile, status
) VALUES
('EMP20230115001', 'zhangsan', '$2b$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
 'zhangsan@company.com', '13800138000', 'active'),
('EMP20230115002', 'lisi', '$2b$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
 'lisi@company.com', '13900139000', 'active'),
('EMP20230115003', 'wangwu', '$2b$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
 'wangwu@company.com', '13700137000', 'active'),
('EMP20230115004', 'zhaoliu', '$2b$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
 'zhaoliu@company.com', '13600136000', 'active');

-- =============================================
-- 4. 菜单初始数据
-- =============================================

INSERT INTO sys_menu (menu_code, menu_name, menu_type, parent_id, menu_level, route_path, component_path, icon, sort, status) VALUES
-- 一级菜单
('MENU0001', '系统管理', 'directory', 0, 1, '/system', NULL, 'Setting', 1, 'enabled'),
('MENU0002', '员工管理', 'directory', 0, 1, '/employee', NULL, 'User', 2, 'enabled'),
('MENU0003', '资产管理', 'directory', 0, 1, '/asset', NULL, 'Box', 3, 'enabled'),
('MENU0004', '审批管理', 'directory', 0, 1, '/approval', NULL, 'Document', 4, 'enabled'),
('MENU0005', '会议管理', 'directory', 0, 1, '/meeting', NULL, 'Calendar', 5, 'enabled'),

-- 二级菜单 - 系统管理
('MENU0006', '菜单管理', 'menu', 1, 2, '/system/menu', 'system/menu/index', 'Menu', 1, 'enabled'),
('MENU0007', '数据字典', 'menu', 1, 2, '/system/dict', 'system/dict/index', 'Dictionary', 2, 'enabled'),
('MENU0008', '部门管理', 'menu', 1, 2, '/system/dept', 'system/dept/index', 'OfficeBuilding', 3, 'enabled'),

-- 二级菜单 - 员工管理
('MENU0009', '员工列表', 'menu', 2, 2, '/employee/list', 'employee/list/index', 'UserFilled', 1, 'enabled'),

-- 二级菜单 - 资产管理
('MENU0010', '资产列表', 'menu', 3, 2, '/asset/list', 'asset/list/index', 'Box', 1, 'enabled'),
('MENU0011', '借用记录', 'menu', 3, 2, '/asset/borrow', 'asset/borrow/index', 'Notebook', 2, 'enabled'),
('MENU0012', '维护记录', 'menu', 3, 2, '/asset/maintenance', 'asset/maintenance/index', 'Tools', 3, 'enabled'),

-- 二级菜单 - 审批管理
('MENU0013', '请假申请', 'menu', 4, 2, '/approval/leave', 'approval/leave/index', 'Calendar', 1, 'enabled'),
('MENU0014', '费用报销', 'menu', 4, 2, '/approval/expense', 'approval/expense/index', 'Money', 2, 'enabled'),
('MENU0015', '我的申请', 'menu', 4, 2, '/approval/my', 'approval/my/index', 'Document', 3, 'enabled'),

-- 二级菜单 - 会议管理
('MENU0016', '会议室管理', 'menu', 5, 2, '/meeting/room', 'meeting/room/index', 'OfficeBuilding', 1, 'enabled'),
('MENU0017', '会议预定', 'menu', 5, 2, '/meeting/booking', 'meeting/booking/index', 'Calendar', 2, 'enabled'),
('MENU0018', '我的会议', 'menu', 5, 2, '/meeting/my', 'meeting/my/index', 'Date', 3, 'enabled');

-- =============================================
-- 5. 数据字典初始数据
-- =============================================

-- 字典类型
INSERT INTO sys_dict_type (code, name, category, status) VALUES
('gender', '性别', 'system', 'enabled'),
('employee_status', '员工状态', 'business', 'enabled'),
('probation_status', '试用期状态', 'business', 'enabled'),
('position_type', '职位类型', 'business', 'enabled'),
('employee_level', '员工职级', 'business', 'enabled'),
('department_status', '部门状态', 'business', 'enabled'),
('asset_category', '资产类别', 'business', 'enabled'),
('asset_status', '资产状态', 'business', 'enabled'),
('leave_type', '请假类型', 'business', 'enabled'),
('leave_status', '请假状态', 'business', 'enabled'),
('expense_type', '报销类型', 'business', 'enabled'),
('expense_status', '报销状态', 'business', 'enabled');

-- 性别字典
INSERT INTO sys_dict_item (type_code, label, value, sort, status) VALUES
('gender', '男', 'male', 1, 'enabled'),
('gender', '女', 'female', 2, 'enabled');

-- 员工状态字典
INSERT INTO sys_dict_item (type_code, label, value, color, sort, status) VALUES
('employee_status', '在职', 'active', '#67C23A', 1, 'enabled'),
('employee_status', '离职', 'resigned', '#909399', 2, 'enabled'),
('employee_status', '停薪留职', 'suspended', '#E6A23C', 3, 'enabled');

-- 试用期状态字典
INSERT INTO sys_dict_item (type_code, label, value, color, sort, status) VALUES
('probation_status', '试用期内', 'probation', '#409EFF', 1, 'enabled'),
('probation_status', '已转正', 'regular', '#67C23A', 2, 'enabled'),
('probation_status', '已离职', 'resigned', '#909399', 3, 'enabled');

-- 职位类型字典
INSERT INTO sys_dict_item (type_code, label, value, sort, status) VALUES
('position_type', '技术经理', '技术经理', 1, 'enabled'),
('position_type', '产品经理', '产品经理', 2, 'enabled'),
('position_type', '前端工程师', '前端工程师', 3, 'enabled'),
('position_type', '后端工程师', '后端工程师', 4, 'enabled'),
('position_type', '全栈工程师', '全栈工程师', 5, 'enabled'),
('position_type', 'UI设计师', 'UI设计师', 6, 'enabled'),
('position_type', 'UX设计师', 'UX设计师', 7, 'enabled'),
('position_type', '测试工程师', '测试工程师', 8, 'enabled'),
('position_type', '运维工程师', '运维工程师', 9, 'enabled'),
('position_type', 'HR专员', 'HR专员', 10, 'enabled'),
('position_type', 'HR经理', 'HR经理', 11, 'enabled'),
('position_type', '财务专员', '财务专员', 12, 'enabled'),
('position_type', '财务经理', '财务经理', 13, 'enabled'),
('position_type', '市场专员', '市场专员', 14, 'enabled'),
('position_type', '市场经理', '市场经理', 15, 'enabled');

-- 员工职级字典
INSERT INTO sys_dict_item (type_code, label, value, sort, status) VALUES
('employee_level', 'P1', 'P1', 1, 'enabled'),
('employee_level', 'P2', 'P2', 2, 'enabled'),
('employee_level', 'P3', 'P3', 3, 'enabled'),
('employee_level', 'P4', 'P4', 4, 'enabled'),
('employee_level', 'P5', 'P5', 5, 'enabled'),
('employee_level', 'P6', 'P6', 6, 'enabled'),
('employee_level', 'P7', 'P7', 7, 'enabled'),
('employee_level', 'P8', 'P8', 8, 'enabled'),
('employee_level', 'P9', 'P9', 9, 'enabled'),
('employee_level', 'P10', 'P10', 10, 'enabled');

-- 部门状态字典
INSERT INTO sys_dict_item (type_code, label, value, color, sort, status) VALUES
('department_status', '正常', 'active', '#67C23A', 1, 'enabled'),
('department_status', '停用', 'disabled', '#909399', 2, 'enabled');

-- 资产类别字典
INSERT INTO sys_dict_item (type_code, label, value, sort, status) VALUES
('asset_category', '电子设备', 'electronic', 1, 'enabled'),
('asset_category', '家具', 'furniture', 2, 'enabled'),
('asset_category', '图书', 'book', 3, 'enabled'),
('asset_category', '其他', 'other', 4, 'enabled');

-- 资产状态字典
INSERT INTO sys_dict_item (type_code, label, value, color, sort, status) VALUES
('asset_status', '在库', 'stock', '#67C23A', 1, 'enabled'),
('asset_status', '使用中', 'in_use', '#409EFF', 2, 'enabled'),
('asset_status', '借出', 'borrowed', '#E6A23C', 3, 'enabled'),
('asset_status', '维护中', 'maintenance', '#F56C6C', 4, 'enabled'),
('asset_status', '报废', 'scrapped', '#909399', 5, 'enabled');

-- 请假类型字典
INSERT INTO sys_dict_item (type_code, label, value, sort, status) VALUES
('leave_type', '年假', 'annual', 1, 'enabled'),
('leave_type', '病假', 'sick', 2, 'enabled'),
('leave_type', '事假', 'personal', 3, 'enabled'),
('leave_type', '调休', 'comp_time', 4, 'enabled'),
('leave_type', '婚假', 'marriage', 5, 'enabled'),
('leave_type', '产假', 'maternity', 6, 'enabled');

-- 请假状态字典
INSERT INTO sys_dict_item (type_code, label, value, color, sort, status) VALUES
('leave_status', '草稿', 'draft', '#909399', 1, 'enabled'),
('leave_status', '待审批', 'pending', '#E6A23C', 2, 'enabled'),
('leave_status', '审批中', 'approving', '#409EFF', 3, 'enabled'),
('leave_status', '已通过', 'approved', '#67C23A', 4, 'enabled'),
('leave_status', '已拒绝', 'rejected', '#F56C6C', 5, 'enabled'),
('leave_status', '已取消', 'cancelled', '#909399', 6, 'enabled');

-- 报销类型字典
INSERT INTO sys_dict_item (type_code, label, value, sort, status) VALUES
('expense_type', '差旅费', 'travel', 1, 'enabled'),
('expense_type', '招待费', 'hospitality', 2, 'enabled'),
('expense_type', '办公费', 'office', 3, 'enabled'),
('expense_type', '交通费', 'transport', 4, 'enabled'),
('expense_type', '其他', 'other', 5, 'enabled');

-- 报销状态字典
INSERT INTO sys_dict_item (type_code, label, value, color, sort, status) VALUES
('expense_status', '草稿', 'draft', '#909399', 1, 'enabled'),
('expense_status', '待部门审批', 'dept_pending', '#E6A23C', 2, 'enabled'),
('expense_status', '待财务审批', 'finance_pending', '#409EFF', 3, 'enabled'),
('expense_status', '已打款', 'paid', '#67C23A', 4, 'enabled'),
('expense_status', '已拒绝', 'rejected', '#F56C6C', 5, 'enabled');

-- =============================================
-- 6. 资产初始数据
-- =============================================

INSERT INTO biz_asset (
  id, name, category, brand_model, purchase_date, purchase_price, current_value, status, location
) VALUES
('ASSET000001', 'MacBook Pro 16寸', 'electronic', 'Apple M3 Max', '2024-01-15', 24999.00, 21249.15, 'stock', 'A座3楼办公室'),
('ASSET000002', 'Dell XPS 15', 'electronic', 'Dell XPS 9530', '2024-02-20', 15999.00, 13599.15, 'in_use', 'A座3楼办公室'),
('ASSET000003', '人体工学椅', 'furniture', 'Herman Miller', '2023-06-10', 8999.00, 5759.36, 'in_use', 'A座3楼办公室'),
('ASSET000004', 'ThinkPad X1 Carbon', 'electronic', 'Lenovo', '2024-03-05', 12999.00, 11049.15, 'stock', 'A座3楼办公室'),
('ASSET000005', 'JavaScript高级程序设计', 'book', '第4版', '2023-08-15', 99.00, 99.00, 'stock', 'A座2楼图书角');

-- =============================================
-- 7. 会议室初始数据
-- =============================================

INSERT INTO admin_meeting_room (
  id, name, location, capacity, facilities, status
) VALUES
('ROOM001', '大会议室', 'A座5楼', 50,
 '["投影仪", '音响系统', '白板', '视频会议设备']', 'available'),
('ROOM002', '中会议室A', 'A座4楼', 20,
 '["投影仪", '白板"]', 'available'),
('ROOM003', '中会议室B', 'A座4楼', 20,
 '["投影仪", '白板", '视频会议设备']', 'available'),
('ROOM004', '小会议室A', 'A座3楼', 10,
 '["白板"]', 'available'),
('ROOM005', '小会议室B', 'A座3楼', 10,
 '["白板", '视频会议设备']', 'available');

-- =============================================
-- 8. 年假余额初始数据(2024年)
-- =============================================

INSERT INTO approval_leave_balance (
  employee_id, year, annual_total, annual_used, annual_remaining
) VALUES
('EMP20230115001', 2024, 15.0, 5.0, 10.0),
('EMP20230115002', 2024, 15.0, 3.0, 12.0),
('EMP20230115003', 2024, 15.0, 0.0, 15.0),
('EMP20230115004', 2024, 10.0, 1.0, 9.0);
