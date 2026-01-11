-- OA系统数据库索引创建脚本
-- 版本: v1.0.0
-- 创建日期: 2026-01-11
-- 数据库: MySQL 8.0+

-- =============================================
-- 1. 核心系统表索引
-- =============================================

-- 1.1 员工表索引
CREATE UNIQUE INDEX uk_employee_email ON sys_employee(email, is_deleted);
CREATE UNIQUE INDEX uk_employee_phone ON sys_employee(phone, is_deleted);
CREATE INDEX idx_employee_department ON sys_employee(department_id);
CREATE INDEX idx_employee_manager ON sys_employee(manager_id);
CREATE INDEX idx_employee_status ON sys_employee(status);
CREATE INDEX idx_employee_join_date ON sys_employee(join_date);
CREATE INDEX idx_employee_probation_status ON sys_employee(probation_status);
CREATE INDEX idx_employee_dept_status ON sys_employee(department_id, status);
CREATE INDEX idx_employee_created_at ON sys_employee(created_at);

-- 1.2 部门表索引
CREATE UNIQUE INDEX uk_department_name ON sys_department(name, parent_id, is_deleted);
CREATE INDEX idx_department_parent ON sys_department(parent_id);
CREATE INDEX idx_department_leader ON sys_department(leader_id);
CREATE INDEX idx_department_level ON sys_department(level);
CREATE INDEX idx_department_status ON sys_department(status);
CREATE INDEX idx_department_sort ON sys_department(parent_id, sort);
CREATE FULLTEXT INDEX ft_department_name ON sys_department(name, short_name);

-- 1.3 菜单表索引
CREATE INDEX idx_menu_parent ON sys_menu(parent_id);
CREATE INDEX idx_menu_type ON sys_menu(menu_type);
CREATE INDEX idx_menu_sort ON sys_menu(parent_id, sort);

-- 1.4 字典表索引
CREATE INDEX idx_dict_type_code ON sys_dict_type(code);
CREATE INDEX idx_dict_item_type ON sys_dict_item(type_code);
CREATE INDEX idx_dict_item_status ON sys_dict_item(status);

-- =============================================
-- 2. 认证授权表索引
-- =============================================

-- 2.1 用户表索引
CREATE INDEX idx_user_username ON auth_user(username);
CREATE INDEX idx_user_email ON auth_user(email);
CREATE INDEX idx_user_mobile ON auth_user(mobile);
CREATE INDEX idx_user_status ON auth_user(status);
CREATE INDEX idx_user_last_login ON auth_user(last_login_at);

-- 2.2 会话表索引
CREATE INDEX idx_session_user ON auth_user_session(user_id);
CREATE INDEX idx_session_token ON auth_user_session(token);
CREATE INDEX idx_session_expires ON auth_user_session(expires_at);

-- 2.3 登录日志索引
CREATE INDEX idx_log_user ON auth_login_log(user_id);
CREATE INDEX idx_log_status ON auth_login_log(status);
CREATE INDEX idx_log_created ON auth_login_log(created_at);
CREATE INDEX idx_log_ip ON auth_login_log(ip_address);

-- =============================================
-- 3. 业务表索引
-- =============================================

-- 3.1 资产表索引
CREATE INDEX idx_asset_category ON biz_asset(category);
CREATE INDEX idx_asset_status ON biz_asset(status);
CREATE INDEX idx_asset_user ON biz_asset(user_id);
CREATE INDEX idx_asset_purchase_date ON biz_asset(purchase_date);
CREATE INDEX idx_asset_price ON biz_asset(purchase_price);
CREATE INDEX idx_asset_location ON biz_asset(location);
CREATE INDEX idx_asset_status_category ON biz_asset(status, category);
CREATE INDEX idx_asset_user_status ON biz_asset(user_id, status);
CREATE FULLTEXT INDEX ft_asset_name ON biz_asset(name, brand_model);

-- =============================================
-- 4. 审批表索引
-- =============================================

-- 4.1 请假申请索引
CREATE INDEX idx_leave_applicant ON approval_leave_request(applicant_id);
CREATE INDEX idx_leave_department ON approval_leave_request(department_id);
CREATE INDEX idx_leave_status ON approval_leave_request(status);
CREATE INDEX idx_leave_created ON approval_leave_request(created_at);
CREATE INDEX idx_leave_type ON approval_leave_request(type);
CREATE INDEX idx_leave_start_time ON approval_leave_request(start_time);
CREATE INDEX idx_leave_end_time ON approval_leave_request(end_time);

-- 4.2 请假审批索引
CREATE INDEX idx_leave_approver ON approval_leave_approval(approver_id);
CREATE INDEX idx_leave_approval_status ON approval_leave_approval(status);
CREATE INDEX idx_leave_approval_timestamp ON approval_leave_approval(timestamp);

-- 4.3 年假余额索引
CREATE INDEX idx_leave_balance_year ON approval_leave_balance(year);
CREATE INDEX idx_leave_balance_employee ON approval_leave_balance(employee_id);

-- 4.4 费用报销索引
CREATE INDEX idx_expense_applicant ON approval_expense(applicant_id);
CREATE INDEX idx_expense_department ON approval_expense(department_id);
CREATE INDEX idx_expense_status ON approval_expense(status);
CREATE INDEX idx_expense_apply_date ON approval_expense(apply_date);
CREATE INDEX idx_expense_type ON approval_expense(type);
CREATE INDEX idx_expense_amount ON approval_expense(amount);
CREATE INDEX idx_expense_dept_approver ON approval_expense(dept_approver_id);
CREATE INDEX idx_expense_finance_approver ON approval_expense(finance_approver_id);

-- 4.5 费用明细索引
CREATE INDEX idx_expense_item_expense ON approval_expense_item(expense_id);
CREATE INDEX idx_expense_item_date ON approval_expense_item(expense_date);

-- 4.6 发票索引
CREATE INDEX idx_invoice_expense ON approval_expense_invoice(expense_id);
CREATE INDEX idx_invoice_number ON approval_expense_invoice(invoice_number);
CREATE INDEX idx_invoice_date ON approval_expense_invoice(invoice_date);
CREATE INDEX idx_invoice_verified ON approval_expense_invoice(verified);

-- =============================================
-- 5. 管理表索引
-- =============================================

-- 5.1 会议室索引
CREATE INDEX idx_meeting_room_status ON admin_meeting_room(status);
CREATE INDEX idx_meeting_room_capacity ON admin_meeting_room(capacity);
CREATE INDEX idx_meeting_room_location ON admin_meeting_room(location);

-- 5.2 会议室预定索引
CREATE INDEX idx_booking_room ON admin_meeting_booking(room_id);
CREATE INDEX idx_booking_time ON admin_meeting_booking(start_time, end_time);
CREATE INDEX idx_booking_booker ON admin_meeting_booking(booker_id);
CREATE INDEX idx_booking_status ON admin_meeting_booking(status);
CREATE INDEX idx_booking_start_time ON admin_meeting_booking(start_time);
CREATE INDEX idx_booking_end_time ON admin_meeting_booking(end_time);
