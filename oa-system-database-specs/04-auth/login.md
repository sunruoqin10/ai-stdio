# 登录认证数据库设计

> **对应前端规范**: [login_Technical.md](../../../oa-system-frontend-specs/auth/login/login_Technical.md)
> **数据库**: MySQL 8.0+
> **表前缀**: `auth_`
> **版本**: v1.0.0

---

## 数据库表设计

### 1. 用户表 (auth_user)

#### 1.1 表结构

```sql
CREATE TABLE auth_user (
  -- 主键
  id VARCHAR(20) PRIMARY KEY COMMENT '用户ID(关联sys_employee.id)',

  -- 登录凭证
  username VARCHAR(50) NOT NULL UNIQUE COMMENT '登录用户名',
  password VARCHAR(255) NOT NULL COMMENT '密码(明文存储)',
  email VARCHAR(100) NOT NULL UNIQUE COMMENT '邮箱',
  mobile VARCHAR(20) NOT NULL UNIQUE COMMENT '手机号',

  -- 账号状态
  status ENUM('active', 'locked', 'disabled') NOT NULL DEFAULT 'active' COMMENT '账号状态: active正常 locked锁定 disabled禁用',
  login_attempts INT NOT NULL DEFAULT 0 COMMENT '登录失败次数',
  locked_until TIMESTAMP NULL COMMENT '锁定到期时间',

  -- 登录信息
  last_login_time TIMESTAMP NULL COMMENT '最后登录时间',
  last_login_ip VARCHAR(50) COMMENT '最后登录IP',
  password_changed_at TIMESTAMP NULL COMMENT '密码最后修改时间',

  -- 审计字段
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除(0否1是)',
  deleted_at TIMESTAMP NULL COMMENT '删除时间',
  deleted_by VARCHAR(20) COMMENT '删除人ID'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户认证表';
```

#### 1.2 索引设计

```sql
-- 主键索引
-- PRIMARY KEY (id)

-- 唯一索引
-- UNIQUE KEY (username)
-- UNIQUE KEY (email)
-- UNIQUE KEY (mobile)

-- 普通索引
CREATE INDEX idx_user_status ON auth_user(status) COMMENT '状态索引';
CREATE INDEX idx_user_locked_until ON auth_user(locked_until) COMMENT '锁定时间索引';
CREATE INDEX idx_user_last_login ON auth_user(last_login_time) COMMENT '最后登录时间索引';
```

---

### 2. 用户会话表 (auth_user_session)

#### 2.1 表结构

```sql
CREATE TABLE auth_user_session (
  -- 主键
  id VARCHAR(64) PRIMARY KEY COMMENT '会话ID(UUID)',

  -- 用户信息
  user_id VARCHAR(20) NOT NULL COMMENT '用户ID',

  -- Token信息
  access_token VARCHAR(500) NOT NULL COMMENT '访问Token(JWT)',
  refresh_token VARCHAR(100) NOT NULL UNIQUE COMMENT '刷新Token(UUID)',
  expires_at TIMESTAMP NOT NULL COMMENT '过期时间',

  -- 登录信息
  login_ip VARCHAR(50) COMMENT '登录IP',
  login_location VARCHAR(100) COMMENT '登录地点',
  device_info VARCHAR(200) COMMENT '设备信息',
  browser VARCHAR(100) COMMENT '浏览器',
  os VARCHAR(100) COMMENT '操作系统',

  -- 时间信息
  login_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
  last_active_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后活跃时间',

  -- 状态
  is_active TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否活跃(0否1是)',
  logout_time TIMESTAMP NULL COMMENT '退出时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户会话表';
```

#### 2.2 索引设计

```sql
-- 主键索引
-- PRIMARY KEY (id)

-- 唯一索引
-- UNIQUE KEY (refresh_token)

-- 普通索引
CREATE INDEX idx_session_user_id ON auth_user_session(user_id) COMMENT '用户ID索引';
CREATE INDEX idx_session_refresh_token ON auth_user_session(refresh_token) COMMENT '刷新Token索引';
CREATE INDEX idx_session_expires_at ON auth_user_session(expires_at) COMMENT '过期时间索引';
CREATE INDEX idx_session_is_active ON auth_user_session(is_active) COMMENT '是否活跃索引';
CREATE INDEX idx_session_last_active ON auth_user_session(last_active_time) COMMENT '最后活跃时间索引';

-- 组合索引
CREATE INDEX idx_session_user_active ON auth_user_session(user_id, is_active) COMMENT '用户+活跃状态组合索引';
```

#### 2.3 外键约束

```sql
-- 用户外键
ALTER TABLE auth_user_session
  ADD CONSTRAINT fk_session_user
  FOREIGN KEY (user_id)
  REFERENCES auth_user(id)
  ON DELETE CASCADE
  ON UPDATE CASCADE;
```

---

### 3. 登录日志表 (auth_login_log)

#### 3.1 表结构

```sql
CREATE TABLE auth_login_log (
  -- 主键
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',

  -- 用户信息
  user_id VARCHAR(20) COMMENT '用户ID',
  username VARCHAR(50) COMMENT '登录用户名',

  -- 登录信息
  login_ip VARCHAR(50) COMMENT '登录IP',
  login_location VARCHAR(100) COMMENT '登录地点',
  device_info VARCHAR(200) COMMENT '设备信息',
  browser VARCHAR(100) COMMENT '浏览器',
  os VARCHAR(100) COMMENT '操作系统',

  -- 登录状态
  status ENUM('success', 'failed') NOT NULL COMMENT '登录状态: success成功 failed失败',
  failure_reason VARCHAR(200) COMMENT '失败原因',

  -- 时间
  login_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='登录日志表';
```

#### 3.2 索引设计

```sql
-- 主键索引
-- PRIMARY KEY (id)

-- 普通索引
CREATE INDEX idx_log_user_id ON auth_login_log(user_id) COMMENT '用户ID索引';
CREATE INDEX idx_log_status ON auth_login_log(status) COMMENT '状态索引';
CREATE INDEX idx_log_login_time ON auth_login_log(login_time) COMMENT '登录时间索引';
CREATE INDEX idx_log_login_ip ON auth_login_log(login_ip) COMMENT '登录IP索引';

-- 组合索引
CREATE INDEX idx_log_user_time ON auth_login_log(user_id, login_time) COMMENT '用户+时间组合索引';
CREATE INDEX idx_log_status_time ON auth_login_log(status, login_time) COMMENT '状态+时间组合索引';
```

#### 3.3 外键约束

```sql
-- 用户外键
ALTER TABLE auth_login_log
  ADD CONSTRAINT fk_log_user
  FOREIGN KEY (user_id)
  REFERENCES auth_user(id)
  ON DELETE SET NULL
  ON UPDATE CASCADE;
```

---

### 4. 验证码表 (auth_verification_code)

#### 4.1 表结构

```sql
CREATE TABLE auth_verification_code (
  -- 主键
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '验证码ID',

  -- 验证码信息
  type ENUM('email', 'mobile') NOT NULL COMMENT '类型: email邮箱 mobile手机',
  account VARCHAR(100) NOT NULL COMMENT '邮箱/手机号',
  scene ENUM('forgot_password', 'login', 'register') NOT NULL COMMENT '场景: forgot_password找回密码 login登录 register注册',
  code VARCHAR(10) NOT NULL COMMENT '验证码',

  -- 状态
  is_used TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已使用(0否1是)',
  used_at TIMESTAMP NULL COMMENT '使用时间',

  -- 时间
  expires_at TIMESTAMP NOT NULL COMMENT '过期时间',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='验证码表';
```

#### 4.2 索引设计

```sql
-- 主键索引
-- PRIMARY KEY (id)

-- 普通索引
CREATE INDEX idx_code_type_account ON auth_verification_code(type, account) COMMENT '类型+账号组合索引';
CREATE INDEX idx_code_expires_at ON auth_verification_code(expires_at) COMMENT '过期时间索引';
CREATE INDEX idx_code_scene ON auth_verification_code(scene) COMMENT '场景索引';
CREATE INDEX idx_code_is_used ON auth_verification_code(is_used) COMMENT '是否使用索引';

-- 组合索引
CREATE INDEX idx_code_account_scene ON auth_verification_code(account, scene, is_used) COMMENT '账号+场景+状态组合索引';
```

---

### 5. 密码历史表 (auth_password_history)

#### 5.1 表结构

```sql
CREATE TABLE auth_password_history (
  -- 主键
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '历史ID',

  -- 用户信息
  user_id VARCHAR(20) NOT NULL COMMENT '用户ID',
  password VARCHAR(255) NOT NULL COMMENT '密码(明文存储)',

  -- 时间
  changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='密码历史表';
```

#### 5.2 索引设计

```sql
-- 主键索引
-- PRIMARY KEY (id)

-- 普通索引
CREATE INDEX idx_pwd_history_user_id ON auth_password_history(user_id) COMMENT '用户ID索引';
CREATE INDEX idx_pwd_history_changed_at ON auth_password_history(changed_at) COMMENT '修改时间索引';

-- 组合索引
CREATE INDEX idx_pwd_history_user_time ON auth_password_history(user_id, changed_at) COMMENT '用户+时间组合索引';
```

#### 5.3 外键约束

```sql
-- 用户外键
ALTER TABLE auth_password_history
  ADD CONSTRAINT fk_pwd_history_user
  FOREIGN KEY (user_id)
  REFERENCES auth_user(id)
  ON DELETE CASCADE
  ON UPDATE CASCADE;
```

---

## 6. 触发器设计

### 6.1 用户状态变更触发器

```sql
DELIMITER $$

-- 插入用户前设置初始密码修改时间
CREATE TRIGGER trg_auth_user_before_insert
BEFORE INSERT ON auth_user
FOR EACH ROW
BEGIN
  -- 如果没有设置密码修改时间,则使用当前时间
  IF NEW.password_changed_at IS NULL THEN
    SET NEW.password_changed_at = CURRENT_TIMESTAMP();
  END IF;
END$$

-- 更新用户时重置登录失败次数
CREATE TRIGGER trg_auth_user_before_update
BEFORE UPDATE ON auth_user
FOR EACH ROW
BEGIN
  -- 如果密码被修改,更新密码修改时间并重置失败次数
  IF NEW.password <> OLD.password THEN
    SET NEW.password_changed_at = CURRENT_TIMESTAMP();
    SET NEW.login_attempts = 0;
    SET NEW.locked_until = NULL;
  END IF;

  -- 如果状态从locked变为active,重置失败次数
  IF OLD.status = 'locked' AND NEW.status = 'active' THEN
    SET NEW.login_attempts = 0;
    SET NEW.locked_until = NULL;
  END IF;
END$$

DELIMITER ;
```

---

## 7. 存储过程设计

### 7.1 清理过期会话

```sql
DELIMITER $$

CREATE PROCEDURE sp_clean_expired_sessions()
BEGIN
  -- 删除过期的会话记录
  DELETE FROM auth_user_session
  WHERE expires_at < CURRENT_TIMESTAMP()
    OR (is_active = 0 AND logout_time < DATE_SUB(CURRENT_TIMESTAMP(), INTERVAL 7 DAY));

  -- 返回清理的记录数
  SELECT ROW_COUNT() AS cleaned_count;
END$$

DELIMITER ;
```

### 7.2 清理过期验证码

```sql
DELIMITER $$

CREATE PROCEDURE sp_clean_expired_codes()
BEGIN
  -- 删除过期的验证码
  DELETE FROM auth_verification_code
  WHERE expires_at < CURRENT_TIMESTAMP()
    OR is_used = 1;

  -- 返回清理的记录数
  SELECT ROW_COUNT() AS cleaned_count;
END$$

DELIMITER ;
```

### 7.3 登录统计

```sql
DELIMITER $$

CREATE PROCEDURE sp_login_statistics(IN date_range INT)
BEGIN
  -- 统计指定天数内的登录情况
  SELECT
    DATE(login_time) AS login_date,
    COUNT(*) AS total_attempts,
    SUM(CASE WHEN status = 'success' THEN 1 ELSE 0 END) AS success_count,
    SUM(CASE WHEN status = 'failed' THEN 1 ELSE 0 END) AS failed_count,
    COUNT(DISTINCT user_id) AS unique_users,
    COUNT(DISTINCT login_ip) AS unique_ips
  FROM auth_login_log
  WHERE login_time >= DATE_SUB(CURRENT_DATE(), INTERVAL date_range DAY)
  GROUP BY DATE(login_time)
  ORDER BY login_date DESC;
END$$

DELIMITER ;
```

### 7.4 用户活跃统计

```sql
DELIMITER $$

CREATE PROCEDURE sp_user_activity_statistics(IN days INT)
BEGIN
  -- 统计用户活跃情况
  SELECT
    COUNT(DISTINCT CASE WHEN last_active_time >= DATE_SUB(CURRENT_TIMESTAMP(), INTERVAL 1 DAY) THEN user_id END) AS active_today,
    COUNT(DISTINCT CASE WHEN last_active_time >= DATE_SUB(CURRENT_TIMESTAMP(), INTERVAL 7 DAY) THEN user_id END) AS active_week,
    COUNT(DISTINCT CASE WHEN last_active_time >= DATE_SUB(CURRENT_TIMESTAMP(), INTERVAL 30 DAY) THEN user_id END) AS active_month,
    COUNT(*) AS total_sessions
  FROM auth_user_session
  WHERE is_active = 1;
END$$

DELIMITER ;
```

---

## 8. 视图设计

### 8.1 用户会话详情视图

```sql
CREATE VIEW v_user_session_detail AS
SELECT
  s.id AS session_id,
  s.user_id,
  u.username,
  e.name AS user_name,
  e.email,
  e.mobile,
  s.login_ip,
  s.login_location,
  s.device_info,
  s.browser,
  s.os,
  s.login_time,
  s.last_active_time,
  s.is_active,
  s.expires_at,
  TIMESTAMPDIFF(MINUTE, s.last_active_time, CURRENT_TIMESTAMP()) AS inactive_minutes
FROM auth_user_session s
LEFT JOIN auth_user u ON s.user_id = u.id
LEFT JOIN sys_employee e ON s.user_id = e.id
WHERE s.is_deleted = 0;
```

### 8.2 登录统计视图

```sql
CREATE VIEW v_login_statistics AS
SELECT
  user_id,
  username,
  COUNT(*) AS total_logins,
  SUM(CASE WHEN status = 'success' THEN 1 ELSE 0 END) AS success_count,
  SUM(CASE WHEN status = 'failed' THEN 1 ELSE 0 END) AS failed_count,
  MAX(CASE WHEN status = 'success' THEN login_time END) AS last_success_time,
  MAX(CASE WHEN status = 'failed' THEN login_time END) AS last_failed_time,
  MAX(login_time) AS last_login_time
FROM auth_login_log
GROUP BY user_id, username;
```

---

## 9. 定时任务设计

### 9.1 清理过期会话

```sql
-- 启用事件调度器
SET GLOBAL event_scheduler = ON;

-- 创建定时事件(每小时执行一次)
CREATE EVENT evt_clean_expired_sessions
ON SCHEDULE EVERY 1 HOUR
DO
  CALL sp_clean_expired_sessions();
```

### 9.2 清理过期验证码

```sql
-- 创建定时事件(每30分钟执行一次)
CREATE EVENT evt_clean_expired_codes
ON SCHEDULE EVERY 30 MINUTE
DO
  CALL sp_clean_expired_codes();
```

### 9.3 解锁过期锁定账号

```sql
DELIMITER $$

CREATE PROCEDURE sp_unlock_expired_accounts()
BEGIN
  -- 解锁已过锁定期的账号
  UPDATE auth_user
  SET status = 'active',
      login_attempts = 0,
      locked_until = NULL
  WHERE status = 'locked'
    AND locked_until IS NOT NULL
    AND locked_until < CURRENT_TIMESTAMP();

  -- 返回解锁的账号数
  SELECT ROW_COUNT() AS unlocked_count;
END$$

DELIMITER ;

-- 创建定时事件(每10分钟执行一次)
CREATE EVENT evt_unlock_accounts
ON SCHEDULE EVERY 10 MINUTE
DO
  CALL sp_unlock_expired_accounts();
```

### 9.4 归档登录日志

```sql
DELIMITER $$

CREATE PROCEDURE sp_archive_login_logs()
BEGIN
  -- 归档90天前的登录日志
  -- 注意: 实际使用时需要创建归档表
  INSERT INTO auth_login_log_archive
  SELECT * FROM auth_login_log
  WHERE login_time < DATE_SUB(CURRENT_TIMESTAMP(), INTERVAL 90 DAY);

  -- 删除已归档的日志
  DELETE FROM auth_login_log
  WHERE login_time < DATE_SUB(CURRENT_TIMESTAMP(), INTERVAL 90 DAY);

  -- 返回归档的记录数
  SELECT ROW_COUNT() AS archived_count;
END$$

DELIMITER ;

-- 创建定时事件(每天凌晨2点执行)
CREATE EVENT evt_archive_logs
ON SCHEDULE EVERY 1 DAY
STARTS (TIMESTAMP(CURRENT_DATE) + INTERVAL 2 DAY + INTERVAL 2 HOUR)
DO
  CALL sp_archive_login_logs();
```

---

## 10. 数据完整性保障

### 10.1 检查约束

```sql
-- 邮箱格式检查
ALTER TABLE auth_user
  ADD CONSTRAINT chk_auth_email_format
  CHECK (email REGEXP '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$');

-- 手机号格式检查
ALTER TABLE auth_user
  ADD CONSTRAINT chk_auth_phone_format
  CHECK (mobile REGEXP '^1[3-9][0-9]{9}$');

-- 登录失败次数不能为负数
ALTER TABLE auth_user
  ADD CONSTRAINT chk_login_attempts
  CHECK (login_attempts >= 0);

-- 密码不能为空
ALTER TABLE auth_user
  ADD CONSTRAINT chk_password_not_empty
  CHECK (password IS NOT NULL AND password != '');

-- 会话过期时间必须晚于创建时间
ALTER TABLE auth_user_session
  ADD CONSTRAINT chk_session_expires
  CHECK (expires_at > login_time);

-- 验证码过期时间必须晚于创建时间
ALTER TABLE auth_verification_code
  ADD CONSTRAINT chk_code_expires
  CHECK (expires_at > created_at);
```

---

## 11. 性能优化建议

### 11.1 分区策略

```sql
-- 按登录时间分区登录日志表(按月分区)
ALTER TABLE auth_login_log
PARTITION BY RANGE (TO_DAYS(login_time)) (
  PARTITION p202401 VALUES LESS THAN (TO_DAYS('2024-02-01')),
  PARTITION p202402 VALUES LESS THAN (TO_DAYS('2024-03-01')),
  PARTITION p202403 VALUES LESS THAN (TO_DAYS('2024-04-01')),
  PARTITION p202404 VALUES LESS THAN (TO_DAYS('2024-05-01')),
  PARTITION p202405 VALUES LESS THAN (TO_DAYS('2024-06-01')),
  PARTITION p202406 VALUES LESS THAN (TO_DAYS('2024-07-01')),
  PARTITION p202407 VALUES LESS THAN (TO_DAYS('2024-08-01')),
  PARTITION p202408 VALUES LESS THAN (TO_DAYS('2024-09-01')),
  PARTITION p202409 VALUES LESS THAN (TO_DAYS('2024-10-01')),
  PARTITION p202410 VALUES LESS THAN (TO_DAYS('2024-11-01')),
  PARTITION p202411 VALUES LESS THAN (TO_DAYS('2024-12-01')),
  PARTITION p202412 VALUES LESS THAN (TO_DAYS('2025-01-01')),
  PARTITION p_future VALUES LESS THAN MAXVALUE
);
```

### 11.2 查询优化

```sql
-- 使用EXPLAIN分析查询
EXPLAIN SELECT * FROM auth_login_log
WHERE user_id = 'EMP20260109001' AND login_time >= DATE_SUB(NOW(), INTERVAL 7 DAY);

-- 优化建议:
-- 1. 确保索引 idx_log_user_time 存在
-- 2. 避免SELECT *, 只查询需要的字段
-- 3. 使用覆盖索引
-- 4. 定期ANALYZE TABLE更新统计信息
```

---

## 12. 初始化数据

### 12.1 初始化测试用户

```sql
-- 插入测试用户(密码: 123456)
INSERT INTO auth_user (
  id, username, password, email, mobile, status
) VALUES
('EMP20260109001', 'zhangsan', '123456', 'zhangsan@company.com', '13800138000', 'active'),
('EMP20260109002', 'lisi', '123456', 'lisi@company.com', '13900139000', 'active'),
('EMP20260109003', 'wangwu', '123456', 'wangwu@company.com', '13700137000', 'active'),
('EMP20260109004', 'zhaoliu', '123456', 'zhaoliu@company.com', '13600136000', 'active');
```

### 12.2 初始化登录日志(示例)

```sql
-- 插入示例登录日志
INSERT INTO auth_login_log (
  user_id, username, login_ip, login_location, device_info, browser, os, status, login_time
) VALUES
('EMP20260109001', 'zhangsan', '192.168.1.100', '北京市', 'Windows PC', 'Chrome 120.0', 'Windows 10', 'success', '2026-01-10 09:00:00'),
('EMP20260109002', 'lisi', '192.168.1.101', '上海市', 'MacBook', 'Safari 17.0', 'macOS 14', 'success', '2026-01-10 09:15:00'),
('EMP20260109001', 'zhangsan', '192.168.1.100', '北京市', 'iPhone', 'Safari 17.0', 'iOS 17', 'failed', '2026-01-10 08:55:00');
```

---

## 13. API对应SQL查询

### 13.1 用户登录

```sql
-- 对应前端: POST /api/auth/login
-- 步骤1: 查询用户信息
SELECT
  u.id,
  u.username,
  u.password,
  u.email,
  u.mobile,
  u.status,
  u.login_attempts,
  u.locked_until,
  e.id AS employee_id,
  e.name,
  e.avatar,
  e.department_id,
  d.name AS department_name,
  e.position
FROM auth_user u
LEFT JOIN sys_employee e ON u.id = e.id
LEFT JOIN sys_department d ON e.department_id = d.id
WHERE u.username = ?
  AND u.is_deleted = 0;

-- 步骤2: 创建会话
INSERT INTO auth_user_session (
  id, user_id, access_token, refresh_token, expires_at,
  login_ip, login_location, device_info, browser, os
) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);

-- 步骤3: 更新用户登录信息
UPDATE auth_user
SET last_login_time = CURRENT_TIMESTAMP(),
    last_login_ip = ?,
    login_attempts = 0
WHERE id = ?;

-- 步骤4: 记录登录日志
INSERT INTO auth_login_log (
  user_id, username, login_ip, login_location, device_info, browser, os, status
) VALUES (?, ?, ?, ?, ?, ?, ?, 'success');
```

### 13.2 刷新Token

```sql
-- 对应前端: POST /api/auth/refresh
SELECT
  s.id,
  s.user_id,
  s.expires_at,
  s.is_active,
  u.status AS user_status
FROM auth_user_session s
LEFT JOIN auth_user u ON s.user_id = u.id
WHERE s.refresh_token = ?
  AND s.is_active = 1
  AND s.expires_at > CURRENT_TIMESTAMP();
```

### 13.3 获取活跃会话

```sql
-- 对应前端: GET /api/auth/sessions
SELECT
  s.id,
  s.user_id,
  s.login_ip,
  s.login_location,
  s.device_info,
  s.browser,
  s.os,
  s.login_time,
  s.last_active_time,
  s.is_active,
  TIMESTAMPDIFF(MINUTE, s.last_active_time, CURRENT_TIMESTAMP()) AS inactive_minutes
FROM auth_user_session s
WHERE s.user_id = ?
  AND s.is_active = 1
ORDER BY s.last_active_time DESC;
```

### 13.4 获取登录日志

```sql
-- 对应前端: GET /api/auth/login-logs
SELECT
  id,
  user_id,
  username,
  login_ip,
  login_location,
  device_info,
  browser,
  os,
  status,
  failure_reason,
  login_time
FROM auth_login_log
WHERE user_id = ?
  AND (? IS NULL OR status = ?)
  AND (? IS NULL OR login_time BETWEEN ? AND ?)
ORDER BY login_time DESC
LIMIT ? OFFSET ?;
```

### 13.5 退出登录

```sql
-- 对应前端: POST /api/auth/logout
UPDATE auth_user_session
SET is_active = 0,
    logout_time = CURRENT_TIMESTAMP()
WHERE id = ? AND user_id = ?;
```

### 13.6 获取验证码

```sql
-- 对应前端: POST /api/auth/send-code
-- 检查发送频率(1分钟内)
SELECT COUNT(*) AS send_count
FROM auth_verification_code
WHERE account = ?
  AND type = ?
  AND created_at > DATE_SUB(NOW(), INTERVAL 1 MINUTE);
```

### 13.7 重置密码

```sql
-- 对应前端: POST /api/auth/reset-password
-- 步骤1: 验证验证码
SELECT id, code, is_used, expires_at
FROM auth_verification_code
WHERE account = ?
  AND type = ?
  AND scene = 'forgot_password'
  AND is_used = 0
  AND expires_at > CURRENT_TIMESTAMP()
ORDER BY created_at DESC
LIMIT 1;

-- 步骤2: 更新密码
UPDATE auth_user
SET password = ?,
    password_changed_at = CURRENT_TIMESTAMP(),
    login_attempts = 0,
    locked_until = NULL
WHERE id = ?;

-- 步骤3: 标记验证码已使用
UPDATE auth_verification_code
SET is_used = 1,
    used_at = CURRENT_TIMESTAMP()
WHERE id = ?;

-- 步骤4: 保存密码历史
INSERT INTO auth_password_history (user_id, password)
VALUES (?, ?);
```

---

## 14. 安全特性说明

### 14.1 密码安全

- **存储方式**: 明文存储(开发阶段)
- **密码策略**:
  - 最小长度8位
  - 必须包含大小写字母和数字
  - 不能与最近3次密码相同
  - 90天过期提醒

**注意**: 当前为开发阶段使用明文存储,生产环境建议使用bcrypt加密

### 14.2 账号锁定

- **锁定条件**: 登录失败5次
- **锁定时长**: 30分钟
- **自动解锁**: 到期后自动解锁

### 14.3 Token管理

- **Access Token**: 有效期2小时(JWT)
- **Refresh Token**: 有效期7天(UUID)
- **单次使用**: Refresh Token使用后失效

### 14.4 验证码

- **图形验证码**: 5分钟过期
- **短信/邮箱验证码**: 10分钟过期
- **发送频率**: 同一账号1分钟内只能发送1次

### 14.5 会话管理

- **自动过期**: Session到期自动失效
- **活跃检测**: 最后活跃时间更新
- **多点登录**: 支持多个设备同时登录
- **强制退出**: 管理员可强制用户退出

---

**文档版本**: v1.0.0
**创建人**: AI开发助手
**最后更新**: 2026-01-12
