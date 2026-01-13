-- 添加 is_deleted 字段到所有需要的表
-- 用于 MyBatis Plus 逻辑删除功能

-- auth_user 表
ALTER TABLE `auth_user` ADD COLUMN `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标记(0-未删除 1-已删除)' AFTER `updated_at`;
ALTER TABLE `auth_user` ADD INDEX `idx_is_deleted` (`is_deleted`);

-- auth_login_log 表
ALTER TABLE `auth_login_log` ADD COLUMN `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标记(0-未删除 1-已删除)' AFTER `updated_at`;
ALTER TABLE `auth_login_log` ADD INDEX `idx_is_deleted` (`is_deleted`);

-- auth_user_session 表
ALTER TABLE `auth_user_session` ADD COLUMN `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标记(0-未删除 1-已删除)' AFTER `updated_at`;
ALTER TABLE `auth_user_session` ADD INDEX `idx_is_deleted` (`is_deleted`);

-- auth_verification_code 表
ALTER TABLE `auth_verification_code` ADD COLUMN `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标记(0-未删除 1-已删除)' AFTER `updated_at`;
ALTER TABLE `auth_verification_code` ADD INDEX `idx_is_deleted` (`is_deleted`);

-- auth_password_history 表
ALTER TABLE `auth_password_history` ADD COLUMN `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标记(0-未删除 1-已删除)' AFTER `updated_at`;
ALTER TABLE `auth_password_history` ADD INDEX `idx_is_deleted` (`is_deleted`);

-- sys_department 表
ALTER TABLE `sys_department` ADD COLUMN `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标记(0-未删除 1-已删除)' AFTER `updated_at`;
ALTER TABLE `sys_department` ADD INDEX `idx_is_deleted` (`is_deleted`);

-- sys_department_member 表
ALTER TABLE `sys_department_member` ADD COLUMN `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标记(0-未删除 1-已删除)' AFTER `updated_at`;
ALTER TABLE `sys_department_member` ADD INDEX `idx_is_deleted` (`is_deleted`);

-- sys_dict_type 表
ALTER TABLE `sys_dict_type` ADD COLUMN `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标记(0-未删除 1-已删除)' AFTER `updated_at`;
ALTER TABLE `sys_dict_type` ADD INDEX `idx_is_deleted` (`is_deleted`);

-- sys_dict_item 表
ALTER TABLE `sys_dict_item` ADD COLUMN `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标记(0-未删除 1-已删除)' AFTER `updated_at`;
ALTER TABLE `sys_dict_item` ADD INDEX `idx_is_deleted` (`is_deleted`);

-- sys_employee 表
ALTER TABLE `sys_employee` ADD COLUMN `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标记(0-未删除 1-已删除)' AFTER `updated_at`;
ALTER TABLE `sys_employee` ADD INDEX `idx_is_deleted` (`is_deleted`);

-- sys_employee_operation_log 表
ALTER TABLE `sys_employee_operation_log` ADD COLUMN `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标记(0-未删除 1-已删除)' AFTER `updated_at`;
ALTER TABLE `sys_employee_operation_log` ADD INDEX `idx_is_deleted` (`is_deleted`);

-- sys_menu 表
ALTER TABLE `sys_menu` ADD COLUMN `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标记(0-未删除 1-已删除)' AFTER `updated_at`;
ALTER TABLE `sys_menu` ADD INDEX `idx_is_deleted` (`is_deleted`);

-- sys_permission 表
ALTER TABLE `sys_permission` ADD COLUMN `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标记(0-未删除 1-已删除)' AFTER `updated_at`;
ALTER TABLE `sys_permission` ADD INDEX `idx_is_deleted` (`is_deleted`);

-- sys_role 表
ALTER TABLE `sys_role` ADD COLUMN `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标记(0-未删除 1-已删除)' AFTER `updated_at`;
ALTER TABLE `sys_role` ADD INDEX `idx_is_deleted` (`is_deleted`);

-- sys_role_menu 表
ALTER TABLE `sys_role_menu` ADD COLUMN `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标记(0-未删除 1-已删除)' AFTER `updated_at`;
ALTER TABLE `sys_role_menu` ADD INDEX `idx_is_deleted` (`is_deleted`);

-- sys_role_permission 表
ALTER TABLE `sys_role_permission` ADD COLUMN `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标记(0-未删除 1-已删除)' AFTER `updated_at`;
ALTER TABLE `sys_role_permission` ADD INDEX `idx_is_deleted` (`is_deleted`);

-- sys_user_role 表
ALTER TABLE `sys_user_role` ADD COLUMN `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标记(0-未删除 1-已删除)' AFTER `updated_at`;
ALTER TABLE `sys_user_role` ADD INDEX `idx_is_deleted` (`is_deleted`);

-- biz_asset 表
ALTER TABLE `biz_asset` ADD COLUMN `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标记(0-未删除 1-已删除)' AFTER `updated_at`;
ALTER TABLE `biz_asset` ADD INDEX `idx_is_deleted` (`is_deleted`);

-- biz_asset_borrow_record 表
ALTER TABLE `biz_asset_borrow_record` ADD COLUMN `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标记(0-未删除 1-已删除)' AFTER `updated_at`;
ALTER TABLE `biz_asset_borrow_record` ADD INDEX `idx_is_deleted` (`is_deleted`);

-- biz_asset_maintenance 表
ALTER TABLE `biz_asset_maintenance` ADD COLUMN `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标记(0-未删除 1-已删除)' AFTER `updated_at`;
ALTER TABLE `biz_asset_maintenance` ADD INDEX `idx_is_deleted` (`is_deleted`);

-- approval_leave_request 表
ALTER TABLE `approval_leave_request` ADD COLUMN `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标记(0-未删除 1-已删除)' AFTER `updated_at`;
ALTER TABLE `approval_leave_request` ADD INDEX `idx_is_deleted` (`is_deleted`);

-- approval_leave_approval 表
ALTER TABLE `approval_leave_approval` ADD COLUMN `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标记(0-未删除 1-已删除)' AFTER `updated_at`;
ALTER TABLE `approval_leave_approval` ADD INDEX `idx_is_deleted` (`is_deleted`);

-- approval_leave_balance 表
ALTER TABLE `approval_leave_balance` ADD COLUMN `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标记(0-未删除 1-已删除)' AFTER `updated_at`;
ALTER TABLE `approval_leave_balance` ADD INDEX `idx_is_deleted` (`is_deleted`);

-- approval_leave_usage_log 表
ALTER TABLE `approval_leave_usage_log` ADD COLUMN `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标记(0-未删除 1-已删除)' AFTER `updated_at`;
ALTER TABLE `approval_leave_usage_log` ADD INDEX `idx_is_deleted` (`is_deleted`);

-- approval_holiday 表
ALTER TABLE `approval_holiday` ADD COLUMN `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标记(0-未删除 1-已删除)' AFTER `updated_at`;
ALTER TABLE `approval_holiday` ADD INDEX `idx_is_deleted` (`is_deleted`);

-- approval_expense 表
ALTER TABLE `approval_expense` ADD COLUMN `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标记(0-未删除 1-已删除)' AFTER `updated_at`;
ALTER TABLE `approval_expense` ADD INDEX `idx_is_deleted` (`is_deleted`);

-- approval_expense_item 表
ALTER TABLE `approval_expense_item` ADD COLUMN `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标记(0-未删除 1-已删除)' AFTER `updated_at`;
ALTER TABLE `approval_expense_item` ADD INDEX `idx_is_deleted` (`is_deleted`);

-- approval_expense_invoice 表
ALTER TABLE `approval_expense_invoice` ADD COLUMN `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标记(0-未删除 1-已删除)' AFTER `updated_at`;
ALTER TABLE `approval_expense_invoice` ADD INDEX `idx_is_deleted` (`is_deleted`);

-- approval_expense_payment 表
ALTER TABLE `approval_expense_payment` ADD COLUMN `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标记(0-未删除 1-已删除)' AFTER `updated_at`;
ALTER TABLE `approval_expense_payment` ADD INDEX `idx_is_deleted` (`is_deleted`);

-- admin_meeting_room 表
ALTER TABLE `admin_meeting_room` ADD COLUMN `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标记(0-未删除 1-已删除)' AFTER `updated_at`;
ALTER TABLE `admin_meeting_room` ADD INDEX `idx_is_deleted` (`is_deleted`);

-- admin_meeting_booking 表
ALTER TABLE `admin_meeting_booking` ADD COLUMN `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标记(0-未删除 1-已删除)' AFTER `updated_at`;
ALTER TABLE `admin_meeting_booking` ADD INDEX `idx_is_deleted` (`is_deleted`);
