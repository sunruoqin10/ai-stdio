-- OA系统数据库外键约束创建脚本
-- 版本: v1.0.0
-- 创建日期: 2026-01-11
-- 数据库: MySQL 8.0+

-- =============================================
-- 1. 核心系统表外键
-- =============================================

-- 1.1 员工表外键
ALTER TABLE sys_employee
  ADD CONSTRAINT fk_employee_department
  FOREIGN KEY (department_id)
  REFERENCES sys_department(id)
  ON DELETE RESTRICT
  ON UPDATE CASCADE;

ALTER TABLE sys_employee
  ADD CONSTRAINT fk_employee_manager
  FOREIGN KEY (manager_id)
  REFERENCES sys_employee(id)
  ON DELETE SET NULL
  ON UPDATE CASCADE;

-- 1.2 部门表外键
ALTER TABLE sys_department
  ADD CONSTRAINT fk_department_parent
  FOREIGN KEY (parent_id)
  REFERENCES sys_department(id)
  ON DELETE RESTRICT
  ON UPDATE CASCADE;

ALTER TABLE sys_department
  ADD CONSTRAINT fk_department_leader
  FOREIGN KEY (leader_id)
  REFERENCES sys_employee(id)
  ON DELETE RESTRICT
  ON UPDATE CASCADE;

-- =============================================
-- 2. 认证授权表外键
-- =============================================

-- 2.1 用户表外键
ALTER TABLE auth_user
  ADD CONSTRAINT fk_user_employee
  FOREIGN KEY (id)
  REFERENCES sys_employee(id)
  ON DELETE CASCADE
  ON UPDATE CASCADE;

-- 2.2 用户会话表外键
ALTER TABLE auth_user_session
  ADD CONSTRAINT fk_session_user
  FOREIGN KEY (user_id)
  REFERENCES auth_user(id)
  ON DELETE CASCADE
  ON UPDATE CASCADE;

-- 2.3 登录日志表外键
ALTER TABLE auth_login_log
  ADD CONSTRAINT fk_log_user
  FOREIGN KEY (user_id)
  REFERENCES auth_user(id)
  ON DELETE SET NULL
  ON UPDATE CASCADE;

-- =============================================
-- 3. 业务表外键
-- =============================================

-- 3.1 资产表外键
ALTER TABLE biz_asset
  ADD CONSTRAINT fk_asset_user
  FOREIGN KEY (user_id)
  REFERENCES sys_employee(id)
  ON DELETE SET NULL
  ON UPDATE CASCADE;

-- 3.2 资产借还记录表外键
ALTER TABLE biz_asset_borrow_record
  ADD CONSTRAINT fk_borrow_asset
  FOREIGN KEY (asset_id)
  REFERENCES biz_asset(id)
  ON DELETE RESTRICT
  ON UPDATE CASCADE;

ALTER TABLE biz_asset_borrow_record
  ADD CONSTRAINT fk_borrow_borrower
  FOREIGN KEY (borrower_id)
  REFERENCES sys_employee(id)
  ON DELETE RESTRICT
  ON UPDATE CASCADE;

-- 3.3 资产维护记录表外键
ALTER TABLE biz_asset_maintenance
  ADD CONSTRAINT fk_maint_asset
  FOREIGN KEY (asset_id)
  REFERENCES biz_asset(id)
  ON DELETE RESTRICT
  ON UPDATE CASCADE;

ALTER TABLE biz_asset_maintenance
  ADD CONSTRAINT fk_maint_operator
  FOREIGN KEY (operator_id)
  REFERENCES sys_employee(id)
  ON DELETE RESTRICT
  ON UPDATE CASCADE;

-- =============================================
-- 4. 审批表外键
-- =============================================

-- 4.1 请假申请表外键
ALTER TABLE approval_leave_request
  ADD CONSTRAINT fk_leave_applicant
  FOREIGN KEY (applicant_id)
  REFERENCES sys_employee(id)
  ON DELETE RESTRICT
  ON UPDATE CASCADE;

ALTER TABLE approval_leave_request
  ADD CONSTRAINT fk_leave_department
  FOREIGN KEY (department_id)
  REFERENCES sys_department(id)
  ON DELETE RESTRICT
  ON UPDATE CASCADE;

-- 4.2 请假审批记录表外键(已在建表时创建)
-- FOREIGN KEY (request_id) REFERENCES approval_leave_request(id) ON DELETE CASCADE

ALTER TABLE approval_leave_approval
  ADD CONSTRAINT fk_leave_approver
  FOREIGN KEY (approver_id)
  REFERENCES sys_employee(id)
  ON DELETE RESTRICT
  ON UPDATE CASCADE;

-- 4.3 年假余额表外键
ALTER TABLE approval_leave_balance
  ADD CONSTRAINT fk_leave_balance_employee
  FOREIGN KEY (employee_id)
  REFERENCES sys_employee(id)
  ON DELETE CASCADE
  ON UPDATE CASCADE;

-- 4.4 费用报销表外键
ALTER TABLE approval_expense
  ADD CONSTRAINT fk_expense_applicant
  FOREIGN KEY (applicant_id)
  REFERENCES sys_employee(id)
  ON DELETE RESTRICT
  ON UPDATE CASCADE;

ALTER TABLE approval_expense
  ADD CONSTRAINT fk_expense_department
  FOREIGN KEY (department_id)
  REFERENCES sys_department(id)
  ON DELETE RESTRICT
  ON UPDATE CASCADE;

ALTER TABLE approval_expense
  ADD CONSTRAINT fk_expense_dept_approver
  FOREIGN KEY (dept_approver_id)
  REFERENCES sys_employee(id)
  ON DELETE SET NULL
  ON UPDATE CASCADE;

ALTER TABLE approval_expense
  ADD CONSTRAINT fk_expense_finance_approver
  FOREIGN KEY (finance_approver_id)
  REFERENCES sys_employee(id)
  ON DELETE SET NULL
  ON UPDATE CASCADE;

-- 4.5 费用明细表外键(已在建表时创建)
 FOREIGN KEY (expense_id) REFERENCES approval_expense(id) ON DELETE CASCADE

-- 4.6 发票表外键(已在建表时创建)
 FOREIGN KEY (expense_id) REFERENCES approval_expense(id) ON DELETE CASCADE

-- =============================================
-- 5. 管理表外键
-- =============================================

-- 5.1 会议室预定表外键(已在建表时创建)
 FOREIGN KEY (room_id) REFERENCES admin_meeting_room(id)

ALTER TABLE admin_meeting_booking
  ADD CONSTRAINT fk_booking_booker
  FOREIGN KEY (booker_id)
  REFERENCES sys_employee(id)
  ON DELETE RESTRICT
  ON UPDATE CASCADE;

-- =============================================
-- 6. 检查约束
-- =============================================

-- 6.1 员工表检查约束
ALTER TABLE sys_employee
  ADD CONSTRAINT chk_email_format
  CHECK (email REGEXP '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$');

ALTER TABLE sys_employee
  ADD CONSTRAINT chk_phone_format
  CHECK (phone REGEXP '^1[3-9][0-9]{9}$');

ALTER TABLE sys_employee
  ADD CONSTRAINT chk_join_date
  CHECK (join_date <= CURDATE());

ALTER TABLE sys_employee
  ADD CONSTRAINT chk_probation_date
  CHECK (probation_end_date IS NULL OR probation_end_date > join_date);

-- 6.2 部门表检查约束
ALTER TABLE sys_department
  ADD CONSTRAINT chk_dept_level
  CHECK (level BETWEEN 1 AND 5);

-- 6.3 资产表检查约束
ALTER TABLE biz_asset
  ADD CONSTRAINT chk_asset_price
  CHECK (purchase_price >= 0 AND current_value >= 0);

-- 6.4 请假申请检查约束
ALTER TABLE approval_leave_request
  ADD CONSTRAINT chk_leave_duration
  CHECK (duration > 0);

ALTER TABLE approval_leave_request
  ADD CONSTRAINT chk_leave_time
  CHECK (end_time > start_time);

-- 6.5 费用报销检查约束
ALTER TABLE approval_expense
  ADD CONSTRAINT chk_expense_amount
  CHECK (amount >= 0);

ALTER TABLE approval_expense_item
  ADD CONSTRAINT chk_expense_item_amount
  CHECK (amount >= 0);

-- 6.6 会议室预定检查约束
ALTER TABLE admin_meeting_booking
  ADD CONSTRAINT chk_booking_time
  CHECK (end_time > start_time);

ALTER TABLE admin_meeting_room
  ADD CONSTRAINT chk_room_capacity
  CHECK (capacity > 0);
