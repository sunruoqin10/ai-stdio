-- OA系统数据库表创建脚本
-- 版本: v1.0.0
-- 创建日期: 2026-01-11
-- 数据库: MySQL 8.0+

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =============================================
-- 1. 核心系统表 (sys_ 前缀)
-- =============================================

-- 1.1 员工表
DROP TABLE IF EXISTS sys_employee;
CREATE TABLE sys_employee (
  id VARCHAR(20) PRIMARY KEY COMMENT '员工编号(格式: EMP+YYYYMMDD+序号)',
  name VARCHAR(50) NOT NULL COMMENT '姓名',
  english_name VARCHAR(50) COMMENT '英文名',
  gender ENUM('male', 'female') NOT NULL COMMENT '性别',
  birth_date DATE COMMENT '出生日期',
  phone VARCHAR(20) NOT NULL COMMENT '联系电话',
  email VARCHAR(100) NOT NULL COMMENT '邮箱',
  avatar VARCHAR(500) COMMENT '头像URL',
  department_id VARCHAR(20) NOT NULL COMMENT '部门ID',
  position VARCHAR(100) NOT NULL COMMENT '职位',
  level VARCHAR(50) COMMENT '职级',
  manager_id VARCHAR(20) COMMENT '直属上级ID',
  join_date DATE NOT NULL COMMENT '入职日期',
  probation_status ENUM('probation', 'regular', 'resigned') DEFAULT 'regular' COMMENT '试用期状态',
  probation_end_date DATE COMMENT '试用期结束日期',
  work_years INT DEFAULT 0 COMMENT '工龄(年)',
  status ENUM('active', 'resigned', 'suspended') NOT NULL DEFAULT 'active' COMMENT '员工状态',
  office_location VARCHAR(200) COMMENT '办公位置',
  emergency_contact VARCHAR(50) COMMENT '紧急联系人',
  emergency_phone VARCHAR(20) COMMENT '紧急联系电话',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  created_by VARCHAR(20) COMMENT '创建人ID',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  updated_by VARCHAR(20) COMMENT '更新人ID',
  is_deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除(0否1是)',
  deleted_at DATETIME COMMENT '删除时间',
  deleted_by VARCHAR(20) COMMENT '删除人ID',
  version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='员工信息表';

-- 1.2 部门表
DROP TABLE IF EXISTS sys_department;
CREATE TABLE sys_department (
  id VARCHAR(20) PRIMARY KEY COMMENT '部门编号(格式: DEPT+4位序号)',
  name VARCHAR(100) NOT NULL COMMENT '部门名称',
  short_name VARCHAR(50) COMMENT '部门简称',
  parent_id VARCHAR(20) DEFAULT NULL COMMENT '上级部门ID(NULL表示顶级)',
  leader_id VARCHAR(20) NOT NULL COMMENT '部门负责人ID',
  level INT NOT NULL DEFAULT 1 COMMENT '部门层级(从1开始)',
  sort INT NOT NULL DEFAULT 0 COMMENT '排序号',
  established_date DATE COMMENT '成立日期',
  description TEXT COMMENT '部门描述',
  icon VARCHAR(500) COMMENT '部门图标URL',
  status ENUM('active', 'disabled') NOT NULL DEFAULT 'active' COMMENT '状态',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  created_by VARCHAR(20) COMMENT '创建人ID',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  updated_by VARCHAR(20) COMMENT '更新人ID',
  is_deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除(0否1是)',
  deleted_at DATETIME COMMENT '删除时间',
  deleted_by VARCHAR(20) COMMENT '删除人ID',
  version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='部门信息表';

-- 1.3 菜单表
DROP TABLE IF EXISTS sys_menu;
CREATE TABLE sys_menu (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '菜单ID',
  menu_code VARCHAR(50) NOT NULL UNIQUE COMMENT '菜单编号(MENU+4位序号)',
  menu_name VARCHAR(100) NOT NULL COMMENT '菜单名称',
  menu_type ENUM('directory', 'menu', 'button') NOT NULL COMMENT '菜单类型',
  parent_id BIGINT DEFAULT 0 COMMENT '父级菜单ID(0表示一级菜单)',
  menu_level INT NOT NULL DEFAULT 1 COMMENT '菜单层级(1-3)',
  route_path VARCHAR(200) COMMENT '路由路径',
  component_path VARCHAR(200) COMMENT '组件路径',
  icon VARCHAR(100) COMMENT '菜单图标',
  sort INT DEFAULT 0 COMMENT '排序号',
  visible TINYINT(1) DEFAULT 1 COMMENT '是否可见(0否1是)',
  status ENUM('enabled', 'disabled') DEFAULT 'enabled' COMMENT '状态',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='菜单管理表';

-- 1.4 数据字典类型表
DROP TABLE IF EXISTS sys_dict_type;
CREATE TABLE sys_dict_type (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '字典类型ID',
  code VARCHAR(50) NOT NULL UNIQUE COMMENT '字典编码',
  name VARCHAR(100) NOT NULL COMMENT '字典名称',
  description VARCHAR(500) COMMENT '字典描述',
  category ENUM('system', 'business') NOT NULL COMMENT '字典类别',
  status ENUM('enabled', 'disabled') NOT NULL DEFAULT 'enabled' COMMENT '状态',
  sort INT DEFAULT 0 COMMENT '排序号',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='数据字典类型表';

-- 1.5 数据字典项表
DROP TABLE IF EXISTS sys_dict_item;
CREATE TABLE sys_dict_item (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '字典项ID',
  type_code VARCHAR(50) NOT NULL COMMENT '字典类型编码',
  label VARCHAR(100) NOT NULL COMMENT '字典标签',
  value VARCHAR(100) NOT NULL COMMENT '字典值',
  color VARCHAR(20) COMMENT '显示颜色',
  icon VARCHAR(100) COMMENT '显示图标',
  sort INT DEFAULT 0 COMMENT '排序号',
  status ENUM('enabled', 'disabled') NOT NULL DEFAULT 'enabled' COMMENT '状态',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY uk_type_value (type_code, value)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='数据字典项表';

-- =============================================
-- 2. 认证授权表 (auth_ 前缀)
-- =============================================

-- 2.1 用户表
DROP TABLE IF EXISTS auth_user;
CREATE TABLE auth_user (
  id VARCHAR(20) PRIMARY KEY COMMENT '用户ID(关联employees.id)',
  username VARCHAR(50) NOT NULL UNIQUE COMMENT '登录用户名',
  password VARCHAR(255) NOT NULL COMMENT '密码(明文存储)',
  email VARCHAR(100) NOT NULL UNIQUE COMMENT '邮箱',
  mobile VARCHAR(20) NOT NULL UNIQUE COMMENT '手机号',
  status ENUM('active', 'locked', 'disabled') DEFAULT 'active' COMMENT '账号状态',
  failed_login_attempts INT DEFAULT 0 COMMENT '登录失败次数',
  locked_until DATETIME COMMENT '锁定到期时间',
  password_changed_at DATETIME COMMENT '密码最后修改时间',
  last_login_at DATETIME COMMENT '最后登录时间',
  last_login_ip VARCHAR(50) COMMENT '最后登录IP',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户账号表';

-- 2.2 用户会话表
DROP TABLE IF EXISTS auth_user_session;
CREATE TABLE auth_user_session (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '会话ID',
  user_id VARCHAR(20) NOT NULL COMMENT '用户ID',
  token VARCHAR(500) NOT NULL UNIQUE COMMENT 'JWT Token',
  refresh_token VARCHAR(500) COMMENT '刷新Token',
  device_type VARCHAR(50) COMMENT '设备类型',
  device_name VARCHAR(100) COMMENT '设备名称',
  ip_address VARCHAR(50) COMMENT 'IP地址',
  user_agent VARCHAR(500) COMMENT '用户代理',
  expires_at DATETIME NOT NULL COMMENT '过期时间',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

  INDEX idx_session_user (user_id),
  INDEX idx_session_token (token),
  INDEX idx_session_expires (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户会话表';

-- 2.3 登录日志表
DROP TABLE IF EXISTS auth_login_log;
CREATE TABLE auth_login_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',
  user_id VARCHAR(20) COMMENT '用户ID',
  username VARCHAR(50) COMMENT '用户名',
  login_type ENUM('web', 'mobile', 'api') NOT NULL COMMENT '登录类型',
  status ENUM('success', 'failed') NOT NULL COMMENT '登录状态',
  failure_reason VARCHAR(200) COMMENT '失败原因',
  ip_address VARCHAR(50) COMMENT 'IP地址',
  location VARCHAR(200) COMMENT '登录地点',
  device_type VARCHAR(50) COMMENT '设备类型',
  user_agent VARCHAR(500) COMMENT '用户代理',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',

  INDEX idx_log_user (user_id),
  INDEX idx_log_status (status),
  INDEX idx_log_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='登录日志表';

-- =============================================
-- 3. 业务表 (biz_ 前缀)
-- =============================================

-- 3.1 资产表
DROP TABLE IF EXISTS biz_asset;
CREATE TABLE biz_asset (
  id VARCHAR(20) PRIMARY KEY COMMENT '资产编号(格式: ASSET+序号)',
  name VARCHAR(200) NOT NULL COMMENT '资产名称',
  category ENUM('electronic', 'furniture', 'book', 'other') NOT NULL COMMENT '资产类别',
  brand_model VARCHAR(200) COMMENT '品牌/型号',
  purchase_date DATE NOT NULL COMMENT '购置日期',
  purchase_price DECIMAL(10,2) NOT NULL COMMENT '购置金额(元)',
  current_value DECIMAL(10,2) COMMENT '当前价值',
  status ENUM('stock', 'in_use', 'borrowed', 'maintenance', 'scrapped') NOT NULL DEFAULT 'stock' COMMENT '资产状态',
  user_id VARCHAR(20) COMMENT '使用人ID',
  location VARCHAR(200) COMMENT '存放位置',
  borrow_date DATE COMMENT '借出日期',
  expected_return_date DATE COMMENT '预计归还日期',
  actual_return_date DATE COMMENT '实际归还日期',
  maintenance_record JSON COMMENT '维护记录',
  images JSON COMMENT '资产图片URL数组',
  notes TEXT COMMENT '备注信息',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  created_by VARCHAR(20) COMMENT '创建人ID',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  updated_by VARCHAR(20) COMMENT '更新人ID',
  is_deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除',
  deleted_at DATETIME COMMENT '删除时间',
  deleted_by VARCHAR(20) COMMENT '删除人ID',
  version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='资产信息表';

-- 3.2 资产借还记录表
DROP TABLE IF EXISTS biz_asset_borrow_record;
CREATE TABLE biz_asset_borrow_record (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
  asset_id VARCHAR(20) NOT NULL COMMENT '资产ID',
  asset_name VARCHAR(200) NOT NULL COMMENT '资产名称',
  borrower_id VARCHAR(20) NOT NULL COMMENT '借用人ID',
  borrower_name VARCHAR(50) NOT NULL COMMENT '借用人姓名',
  borrow_date DATE NOT NULL COMMENT '借出日期',
  expected_return_date DATE COMMENT '预计归还日期',
  actual_return_date DATE COMMENT '实际归还日期',
  status ENUM('active', 'returned', 'overdue') NOT NULL DEFAULT 'active' COMMENT '记录状态',
  notes TEXT COMMENT '备注',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

  INDEX idx_borrow_asset (asset_id),
  INDEX idx_borrow_borrower (borrower_id),
  INDEX idx_borrow_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='资产借还记录表';

-- 3.3 资产维护记录表
DROP TABLE IF EXISTS biz_asset_maintenance;
CREATE TABLE biz_asset_maintenance (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
  asset_id VARCHAR(20) NOT NULL COMMENT '资产ID',
  type ENUM('repair', 'maintenance', 'check') NOT NULL COMMENT '类型',
  description TEXT NOT NULL COMMENT '描述',
  cost DECIMAL(10,2) COMMENT '费用',
  start_date DATE NOT NULL COMMENT '开始日期',
  end_date DATE COMMENT '结束日期',
  operator_id VARCHAR(20) NOT NULL COMMENT '操作人ID',
  operator_name VARCHAR(50) NOT NULL COMMENT '操作人姓名',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

  INDEX idx_maint_asset (asset_id),
  INDEX idx_maint_type (type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='资产维护记录表';

-- =============================================
-- 4. 审批表 (approval_ 前缀)
-- =============================================

-- 4.1 请假申请表
DROP TABLE IF EXISTS approval_leave_request;
CREATE TABLE approval_leave_request (
  id VARCHAR(50) PRIMARY KEY COMMENT '编号: LEAVE+YYYYMMDD+序号',
  applicant_id VARCHAR(50) NOT NULL COMMENT '申请人ID',
  department_id VARCHAR(50) NOT NULL COMMENT '部门ID',
  type ENUM('annual', 'sick', 'personal', 'comp_time', 'marriage', 'maternity') NOT NULL COMMENT '请假类型',
  start_time DATETIME NOT NULL COMMENT '开始时间',
  end_time DATETIME NOT NULL COMMENT '结束时间',
  duration DECIMAL(4,1) NOT NULL COMMENT '请假时长(天)',
  reason TEXT NOT NULL COMMENT '请假事由',
  attachments JSON COMMENT '附件URL数组',
  status ENUM('draft', 'pending', 'approving', 'approved', 'rejected', 'cancelled') NOT NULL DEFAULT 'draft' COMMENT '状态',
  current_approval_level INT DEFAULT 0 COMMENT '当前审批层级',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

  INDEX idx_leave_applicant (applicant_id),
  INDEX idx_leave_department (department_id),
  INDEX idx_leave_status (status),
  INDEX idx_leave_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='请假申请表';

-- 4.2 请假审批记录表
DROP TABLE IF EXISTS approval_leave_approval;
CREATE TABLE approval_leave_approval (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  request_id VARCHAR(50) NOT NULL COMMENT '申请ID',
  approver_id VARCHAR(50) NOT NULL COMMENT '审批人ID',
  approver_name VARCHAR(100) NOT NULL COMMENT '审批人姓名',
  approval_level INT NOT NULL COMMENT '审批层级',
  status ENUM('pending', 'approved', 'rejected') NOT NULL DEFAULT 'pending' COMMENT '审批状态',
  opinion TEXT COMMENT '审批意见',
  timestamp DATETIME COMMENT '审批时间',

  UNIQUE KEY uk_leave_request_approver (request_id, approver_id),
  INDEX idx_leave_approver (approver_id),
  INDEX idx_leave_approval_status (status),
  FOREIGN KEY (request_id) REFERENCES approval_leave_request(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='请假审批记录表';

-- 4.3 年假余额表
DROP TABLE IF EXISTS approval_leave_balance;
CREATE TABLE approval_leave_balance (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  employee_id VARCHAR(50) NOT NULL UNIQUE COMMENT '员工ID',
  year INT NOT NULL COMMENT '年份',
  annual_total DECIMAL(4,1) NOT NULL DEFAULT 0 COMMENT '年假总额(天)',
  annual_used DECIMAL(4,1) NOT NULL DEFAULT 0 COMMENT '已使用(天)',
  annual_remaining DECIMAL(4,1) NOT NULL DEFAULT 0 COMMENT '剩余(天)',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

  UNIQUE KEY uk_leave_employee_year (employee_id, year),
  INDEX idx_leave_balance_year (year)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='年假余额表';

-- 4.4 费用报销表
DROP TABLE IF EXISTS approval_expense;
CREATE TABLE approval_expense (
  id VARCHAR(32) PRIMARY KEY COMMENT '报销单号: EXP+YYYYMMDD+序号',
  applicant_id VARCHAR(32) NOT NULL COMMENT '报销人ID',
  department_id VARCHAR(32) NOT NULL COMMENT '部门ID',
  type VARCHAR(50) NOT NULL COMMENT '报销类型',
  amount DECIMAL(10,2) NOT NULL COMMENT '总金额',
  reason TEXT NOT NULL COMMENT '报销事由',
  apply_date DATE NOT NULL COMMENT '申请日期',
  expense_date DATE NOT NULL COMMENT '费用发生日期',
  status VARCHAR(20) NOT NULL DEFAULT 'draft' COMMENT '状态',

  -- 部门审批
  dept_approver_id VARCHAR(32) COMMENT '部门审批人ID',
  dept_approver_name VARCHAR(100) COMMENT '部门审批人姓名',
  dept_approval_status VARCHAR(20) COMMENT '部门审批状态',
  dept_approval_opinion TEXT COMMENT '部门审批意见',
  dept_approval_time DATETIME COMMENT '部门审批时间',

  -- 财务审批
  finance_approver_id VARCHAR(32) COMMENT '财务审批人ID',
  finance_approver_name VARCHAR(100) COMMENT '财务审批人姓名',
  finance_approval_status VARCHAR(20) COMMENT '财务审批状态',
  finance_approval_opinion TEXT COMMENT '财务审批意见',
  finance_approval_time DATETIME COMMENT '财务审批时间',

  -- 打款信息
  payment_date DATE COMMENT '打款时间',
  payment_proof VARCHAR(500) COMMENT '打款凭证URL',

  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted_at DATETIME COMMENT '删除时间',

  INDEX idx_expense_applicant (applicant_id),
  INDEX idx_expense_department (department_id),
  INDEX idx_expense_status (status),
  INDEX idx_expense_apply_date (apply_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='费用报销单表';

-- 4.5 费用明细表
DROP TABLE IF EXISTS approval_expense_item;
CREATE TABLE approval_expense_item (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  expense_id VARCHAR(32) NOT NULL COMMENT '报销单号',
  description VARCHAR(500) NOT NULL COMMENT '费用说明',
  amount DECIMAL(10,2) NOT NULL COMMENT '金额',
  expense_date DATE NOT NULL COMMENT '发生日期',
  category VARCHAR(100) COMMENT '费用分类',
  sort_order INT DEFAULT 0 COMMENT '排序序号',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

  INDEX idx_expense_item_expense (expense_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='费用明细表';

-- 4.6 发票表
DROP TABLE IF EXISTS approval_expense_invoice;
CREATE TABLE approval_expense_invoice (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  expense_id VARCHAR(32) NOT NULL COMMENT '报销单号',
  invoice_type VARCHAR(20) NOT NULL COMMENT '发票类型',
  invoice_number VARCHAR(50) NOT NULL COMMENT '发票号码',
  amount DECIMAL(10,2) NOT NULL COMMENT '发票金额',
  invoice_date DATE NOT NULL COMMENT '开票日期',
  image_url VARCHAR(500) COMMENT '发票图片URL',
  verified BOOLEAN DEFAULT FALSE COMMENT '是否已验真',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

  UNIQUE KEY uk_invoice_number (invoice_number),
  INDEX idx_invoice_expense (expense_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='发票表';

-- =============================================
-- 5. 管理表 (admin_ 前缀)
-- =============================================

-- 5.1 会议室表
DROP TABLE IF EXISTS admin_meeting_room;
CREATE TABLE admin_meeting_room (
  id VARCHAR(20) PRIMARY KEY COMMENT '会议室ID',
  name VARCHAR(100) NOT NULL COMMENT '会议室名称',
  location VARCHAR(200) NOT NULL COMMENT '位置',
  capacity INT NOT NULL COMMENT '容纳人数',
  facilities JSON COMMENT '设施设备',
  description TEXT COMMENT '描述',
  status ENUM('available', 'maintenance', 'disabled') NOT NULL DEFAULT 'available' COMMENT '状态',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会议室表';

-- 5.2 会议室预定表
DROP TABLE IF EXISTS admin_meeting_booking;
CREATE TABLE admin_meeting_booking (
  id VARCHAR(50) PRIMARY KEY COMMENT '预定ID',
  title VARCHAR(200) NOT NULL COMMENT '会议主题',
  room_id VARCHAR(20) NOT NULL COMMENT '会议室ID',
  room_name VARCHAR(100) COMMENT '会议室名称',
  start_time DATETIME NOT NULL COMMENT '开始时间',
  end_time DATETIME NOT NULL COMMENT '结束时间',
  booker_id VARCHAR(20) NOT NULL COMMENT '预定人ID',
  booker_name VARCHAR(50) COMMENT '预定人姓名',
  participant_count INT COMMENT '参会人数',
  participant_ids JSON COMMENT '参会人员ID数组',
  agenda TEXT COMMENT '会议议程',
  equipment JSON COMMENT '所需设备',
  status ENUM('booked', 'in_progress', 'completed', 'cancelled') NOT NULL DEFAULT 'booked' COMMENT '状态',
  recurring_rule VARCHAR(200) COMMENT '循环规则',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

  INDEX idx_booking_room (room_id),
  INDEX idx_booking_time (start_time, end_time),
  INDEX idx_booking_booker (booker_id),
  INDEX idx_booking_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会议室预定表';

SET FOREIGN_KEY_CHECKS = 1;
