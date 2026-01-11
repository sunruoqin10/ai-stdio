# 请假审批数据库设计

> **对应前端规范**: [leave_Technical.md](../../../oa-system-frontend-specs/approval/leave/leave_Technical.md)
> **数据库**: MySQL 8.0+
> **表前缀**: `approval_`
> **版本**: v1.0.0

---

## 数据库表设计

### 1. 请假申请表 (approval_leave_request)

#### 1.1 表结构

```sql
CREATE TABLE approval_leave_request (
  -- 主键
  id VARCHAR(50) PRIMARY KEY COMMENT '编号: LEAVE+YYYYMMDD+序号',

  -- 基本信息
  applicant_id VARCHAR(50) NOT NULL COMMENT '申请人ID',
  department_id VARCHAR(50) NOT NULL COMMENT '部门ID',
  type ENUM('annual', 'sick', 'personal', 'comp_time', 'marriage', 'maternity') NOT NULL COMMENT '请假类型',
  start_time DATETIME NOT NULL COMMENT '开始时间',
  end_time DATETIME NOT NULL COMMENT '结束时间',
  duration DECIMAL(4,1) NOT NULL COMMENT '请假时长(天),支持0.5天',
  reason TEXT NOT NULL COMMENT '请假事由',
  attachments JSON COMMENT '附件URL数组',

  -- 审批状态
  status ENUM('draft', 'pending', 'approving', 'approved', 'rejected', 'cancelled') NOT NULL DEFAULT 'draft' COMMENT '状态',
  current_approval_level INT DEFAULT 0 COMMENT '当前审批层级',

  -- 审计字段
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

  -- 索引
  INDEX idx_leave_applicant (applicant_id),
  INDEX idx_leave_department (department_id),
  INDEX idx_leave_status (status),
  INDEX idx_leave_type (type),
  INDEX idx_leave_start_time (start_time),
  INDEX idx_leave_end_time (end_time),
  INDEX idx_leave_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='请假申请表';
```

#### 1.2 外键约束

```sql
-- 申请人外键
ALTER TABLE approval_leave_request
  ADD CONSTRAINT fk_leave_applicant
  FOREIGN KEY (applicant_id)
  REFERENCES sys_employee(id)
  ON DELETE RESTRICT
  ON UPDATE CASCADE;

-- 部门外键
ALTER TABLE approval_leave_request
  ADD CONSTRAINT fk_leave_department
  FOREIGN KEY (department_id)
  REFERENCES sys_department(id)
  ON DELETE RESTRICT
  ON UPDATE CASCADE;
```

#### 1.3 检查约束

```sql
-- 时长必须大于0
ALTER TABLE approval_leave_request
  ADD CONSTRAINT chk_leave_duration
  CHECK (duration > 0);

-- 结束时间必须晚于开始时间
ALTER TABLE approval_leave_request
  ADD CONSTRAINT chk_leave_time
  CHECK (end_time > start_time);
```

---

### 2. 请假审批记录表 (approval_leave_approval)

#### 2.1 表结构

```sql
CREATE TABLE approval_leave_approval (
  id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '记录ID',
  request_id VARCHAR(50) NOT NULL COMMENT '申请ID',
  approver_id VARCHAR(50) NOT NULL COMMENT '审批人ID',
  approver_name VARCHAR(100) NOT NULL COMMENT '审批人姓名(快照)',
  approval_level INT NOT NULL COMMENT '审批层级(1/2/3)',
  status ENUM('pending', 'approved', 'rejected') NOT NULL DEFAULT 'pending' COMMENT '审批状态',
  opinion TEXT COMMENT '审批意见',
  timestamp DATETIME COMMENT '审批时间',

  -- 唯一约束: 每个审批人对每个申请只能审批一次
  UNIQUE KEY uk_leave_request_approver (request_id, approver_id),

  -- 索引
  INDEX idx_leave_approver (approver_id),
  INDEX idx_leave_approval_status (status),
  INDEX idx_leave_approval_timestamp (timestamp),

  -- 外键约束
  FOREIGN KEY (request_id)
    REFERENCES approval_leave_request(id)
    ON DELETE CASCADE
    ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='请假审批记录表';
```

#### 2.2 外键约束

```sql
-- 审批人外键
ALTER TABLE approval_leave_approval
  ADD CONSTRAINT fk_leave_approval_approver
  FOREIGN KEY (approver_id)
  REFERENCES sys_employee(id)
  ON DELETE RESTRICT
  ON UPDATE CASCADE;
```

---

### 3. 年假余额表 (approval_leave_balance)

#### 3.1 表结构

```sql
CREATE TABLE approval_leave_balance (
  id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
  employee_id VARCHAR(50) NOT NULL COMMENT '员工ID',
  year INT NOT NULL COMMENT '年份',
  annual_total DECIMAL(4,1) NOT NULL DEFAULT 0 COMMENT '年假总额(天)',
  annual_used DECIMAL(4,1) NOT NULL DEFAULT 0 COMMENT '已使用(天)',
  annual_remaining DECIMAL(4,1) NOT NULL DEFAULT 0 COMMENT '剩余(天)',

  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

  -- 唯一约束: 每个员工每年只有一条记录
  UNIQUE KEY uk_leave_employee_year (employee_id, year),

  -- 索引
  INDEX idx_leave_balance_year (year),
  INDEX idx_leave_balance_employee (employee_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='年假余额表';
```

#### 3.2 外键约束

```sql
-- 员工外键
ALTER TABLE approval_leave_balance
  ADD CONSTRAINT fk_leave_balance_employee
  FOREIGN KEY (employee_id)
  REFERENCES sys_employee(id)
  ON DELETE CASCADE
  ON UPDATE CASCADE;
```

#### 3.3 检查约束

```sql
-- 年假总额必须大于等于0
ALTER TABLE approval_leave_balance
  ADD CONSTRAINT chk_leave_total
  CHECK (annual_total >= 0);

-- 已使用天数不能大于总额
ALTER TABLE approval_leave_balance
  ADD CONSTRAINT chk_leave_used
  CHECK (annual_used >= 0 AND annual_used <= annual_total);

-- 剩余天数必须等于总额减去已使用
ALTER TABLE approval_leave_balance
  ADD CONSTRAINT chk_leave_remaining
  CHECK (annual_remaining = annual_total - annual_used);
```

---

### 4. 年假使用记录表 (approval_leave_usage_log)

#### 4.1 表结构

```sql
CREATE TABLE approval_leave_usage_log (
  id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '记录ID',
  employee_id VARCHAR(50) NOT NULL COMMENT '员工ID',
  request_id VARCHAR(50) NOT NULL COMMENT '申请ID',
  type ENUM('annual', 'sick', 'personal', 'comp_time', 'marriage', 'maternity') NOT NULL COMMENT '请假类型',
  duration DECIMAL(4,1) NOT NULL COMMENT '请假时长(天)',
  start_time DATETIME NOT NULL COMMENT '开始时间',
  end_time DATETIME NOT NULL COMMENT '结束时间',
  change_type ENUM('deduct', 'rollback') NOT NULL COMMENT '变动类型:扣减/回退',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录时间',

  -- 索引
  INDEX idx_usage_employee (employee_id),
  INDEX idx_usage_request (request_id),
  INDEX idx_usage_created (created_at),
  INDEX idx_usage_type (change_type),

  -- 外键约束
  FOREIGN KEY (employee_id)
    REFERENCES sys_employee(id)
    ON DELETE CASCADE
    ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='年假使用记录表';
```

---

### 5. 节假日表 (approval_holiday)

#### 5.1 表结构

```sql
CREATE TABLE approval_holiday (
  id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
  date DATE NOT NULL UNIQUE COMMENT '日期',
  name VARCHAR(100) NOT NULL COMMENT '节假日名称',
  type ENUM('national', 'company') NOT NULL DEFAULT 'national' COMMENT '类型:国家/公司',
  year INT NOT NULL COMMENT '年份',
  is_workday TINYINT(1) DEFAULT 0 COMMENT '是否为工作日(调休)',

  -- 索引
  INDEX idx_holiday_year (year),
  INDEX idx_holiday_date (date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='节假日表';
```

---

## 6. 触发器设计

### 6.1 请假时长自动计算触发器

```sql
DELIMITER $$

CREATE TRIGGER trg_leave_before_insert
BEFORE INSERT ON approval_leave_request
FOR EACH ROW
BEGIN
  -- 计算请假时长(天数)
  SET NEW.duration = TIMESTAMPDIFF(HOUR, NEW.start_time, NEW.end_time) / 24.0;

  -- 保留一位小数
  SET NEW.duration = ROUND(NEW.duration, 1);
END$$

CREATE TRIGGER trg_leave_before_update
BEFORE UPDATE ON approval_leave_request
FOR EACH ROW
BEGIN
  -- 如果时间变更,重新计算时长
  IF NEW.start_time <> OLD.start_time OR NEW.end_time <> OLD.end_time THEN
    SET NEW.duration = TIMESTAMPDIFF(HOUR, NEW.start_time, NEW.end_time) / 24.0;
    SET NEW.duration = ROUND(NEW.duration, 1);
  END IF;
END$$

DELIMITER ;
```

### 6.2 年假余额自动更新触发器

```sql
DELIMITER $$

-- 请假通过后自动扣减年假
CREATE TRIGGER trg_leave_after_update
AFTER UPDATE ON approval_leave_request
FOR EACH ROW
BEGIN
  -- 当状态从非approved变为approved,且请假类型为年假时
  IF NEW.status = 'approved' AND OLD.status != 'approved' AND NEW.type = 'annual' THEN
    -- 更新年假余额
    UPDATE approval_leave_balance
    SET annual_used = annual_used + NEW.duration,
        annual_remaining = annual_total - (annual_used + NEW.duration),
        updated_at = CURRENT_TIMESTAMP
    WHERE employee_id = NEW.applicant_id
      AND year = YEAR(NEW.start_time);

    -- 记录使用日志
    INSERT INTO approval_leave_usage_log (
      employee_id, request_id, type, duration,
      start_time, end_time, change_type
    ) VALUES (
      NEW.applicant_id, NEW.id, 'annual', NEW.duration,
      NEW.start_time, NEW.end_time, 'deduct'
    );
  END IF;

  -- 当状态从approved变为其他状态时,回退年假
  IF OLD.status = 'approved' AND NEW.status != 'approved' AND NEW.type = 'annual' THEN
    -- 回退年假余额
    UPDATE approval_leave_balance
    SET annual_used = annual_used - NEW.duration,
        annual_remaining = annual_total - (annual_used - NEW.duration),
        updated_at = CURRENT_TIMESTAMP
    WHERE employee_id = NEW.applicant_id
      AND year = YEAR(NEW.start_time);

    -- 记录回退日志
    INSERT INTO approval_leave_usage_log (
      employee_id, request_id, type, duration,
      start_time, end_time, change_type
    ) VALUES (
      NEW.applicant_id, NEW.id, 'annual', NEW.duration,
      NEW.start_time, NEW.end_time, 'rollback'
    );
  END IF;
END$$

DELIMITER ;
```

---

## 7. 存储过程设计

### 7.1 计算工作日时长

```sql
DELIMITER $$

CREATE PROCEDURE sp_calculate_workdays(
  IN p_start_time DATETIME,
  IN p_end_time DATETIME,
  OUT p_duration DECIMAL(4,1)
)
BEGIN
  DECLARE v_days INT DEFAULT 0;
  DECLARE v_hours DECIMAL(10,2);
  DECLARE v_current_date DATE;

  -- 简单计算: 不考虑节假日和调休
  SET v_hours = TIMESTAMPDIFF(HOUR, p_start_time, p_end_time);

  -- 转换为天数(保留一位小数)
  SET p_duration = ROUND(v_hours / 8.0, 1);

  -- 如果时长为负数,说明时间范围错误
  IF p_duration < 0 THEN
    SET p_duration = 0;
  END IF;
END$$

DELIMITER ;
```

### 7.2 初始化年假余额

```sql
DELIMITER $$

CREATE PROCEDURE sp_init_annual_balance(
  IN p_employee_id VARCHAR(50),
  IN p_year INT
)
BEGIN
  DECLARE v_work_years INT;
  DECLARE v_annual_days DECIMAL(4,1);

  -- 获取工龄
  SELECT work_years INTO v_work_years
  FROM sys_employee
  WHERE id = p_employee_id;

  -- 根据工龄计算年假天数
  IF v_work_years < 1 THEN
    SET v_annual_days = 5;
  ELSEIF v_work_years < 10 THEN
    SET v_annual_days = 10;
  ELSEIF v_work_years < 20 THEN
    SET v_annual_days = 15;
  ELSE
    SET v_annual_days = 20;
  END IF;

  -- 插入或更新年假余额
  INSERT INTO approval_leave_balance (
    employee_id, year, annual_total, annual_used, annual_remaining
  ) VALUES (
    p_employee_id, p_year, v_annual_days, 0, v_annual_days
  ) ON DUPLICATE KEY UPDATE
    annual_total = v_annual_days,
    annual_remaining = annual_total - annual_used;
END$$

DELIMITER ;
```

### 7.3 检查年假余额

```sql
DELIMITER $$

CREATE PROCEDURE sp_check_annual_balance(
  IN p_employee_id VARCHAR(50),
  IN p_duration DECIMAL(4,1),
  OUT p_sufficient TINYINT,
  OUT p_remaining DECIMAL(4,1)
)
BEGIN
  SELECT
    annual_remaining >= p_duration,
    annual_remaining
  INTO p_sufficient, p_remaining
  FROM approval_leave_balance
  WHERE employee_id = p_employee_id
    AND year = YEAR(CURDATE());
END$$

DELIMITER ;
```

### 7.4 请假统计

```sql
DELIMITER $$

CREATE PROCEDURE sp_leave_statistics(
  IN p_employee_id VARCHAR(50),
  IN p_year INT
)
BEGIN
  SELECT
    type AS leave_type,
    COUNT(*) AS count,
    SUM(duration) AS total_duration
  FROM approval_leave_request
  WHERE applicant_id = p_employee_id
    AND status = 'approved'
    AND YEAR(start_time) = p_year
  GROUP BY type;
END$$

DELIMITER ;
```

---

## 8. 视图设计

### 8.1 请假申请详情视图

```sql
CREATE VIEW v_leave_request_detail AS
SELECT
  r.id,
  r.applicant_id,
  e.name AS applicant_name,
  e.position AS applicant_position,
  r.department_id,
  d.name AS department_name,
  r.type,
  r.start_time,
  r.end_time,
  r.duration,
  r.reason,
  r.attachments,
  r.status,
  r.current_approval_level,
  r.created_at,
  r.updated_at,

  -- 审批进度
  (SELECT COUNT(*) FROM approval_leave_approval
   WHERE request_id = r.id) AS total_approvers,
  (SELECT COUNT(*) FROM approval_leave_approval
   WHERE request_id = r.id AND status = 'approved') AS approved_count,
  (SELECT COUNT(*) FROM approval_leave_approval
   WHERE request_id = r.id AND status = 'rejected') AS rejected_count

FROM approval_leave_request r
LEFT JOIN sys_employee e ON r.applicant_id = e.id
LEFT JOIN sys_department d ON r.department_id = d.id
WHERE r.is_deleted = 0;
```

### 8.2 待审批请假视图

```sql
CREATE VIEW v_leave_pending_approval AS
SELECT
  r.id,
  r.applicant_id,
  e.name AS applicant_name,
  d.name AS department_name,
  r.type,
  r.start_time,
  r.end_time,
  r.duration,
  r.reason,
  r.current_approval_level,
  r.created_at
FROM approval_leave_request r
INNER JOIN sys_employee e ON r.applicant_id = e.id
INNER JOIN sys_department d ON r.department_id = d.id
WHERE r.status IN ('pending', 'approving')
  AND r.is_deleted = 0
ORDER BY r.created_at ASC;
```

### 8.3 年假余额视图

```sql
CREATE VIEW v_annual_balance AS
SELECT
  b.employee_id,
  e.name AS employee_name,
  d.name AS department_name,
  b.year,
  b.annual_total,
  b.annual_used,
  b.annual_remaining,
  ROUND(b.annual_used / b.annual_total * 100, 2) AS usage_percentage
FROM approval_leave_balance b
LEFT JOIN sys_employee e ON b.employee_id = e.id
LEFT JOIN sys_department d ON e.department_id = d.id
ORDER BY b.year DESC, b.employee_id;
```

---

## 9. 数据字典映射

### 9.1 请假类型字典 (leave_type)

```sql
INSERT INTO sys_dict_type (code, name, category, status) VALUES
('leave_type', '请假类型', 'business', 'enabled');

INSERT INTO sys_dict_item (type_code, label, value, sort, status) VALUES
('leave_type', '年假', 'annual', 1, 'enabled'),
('leave_type', '病假', 'sick', 2, 'enabled'),
('leave_type', '事假', 'personal', 3, 'enabled'),
('leave_type', '调休', 'comp_time', 4, 'enabled'),
('leave_type', '婚假', 'marriage', 5, 'enabled'),
('leave_type', '产假', 'maternity', 6, 'enabled');
```

### 9.2 请假状态字典 (leave_status)

```sql
INSERT INTO sys_dict_type (code, name, category, status) VALUES
('leave_status', '请假状态', 'business', 'enabled');

INSERT INTO sys_dict_item (type_code, label, value, color, sort, status) VALUES
('leave_status', '草稿', 'draft', '#909399', 1, 'enabled'),
('leave_status', '待审批', 'pending', '#E6A23C', 2, 'enabled'),
('leave_status', '审批中', 'approving', '#409EFF', 3, 'enabled'),
('leave_status', '已通过', 'approved', '#67C23A', 4, 'enabled'),
('leave_status', '已拒绝', 'rejected', '#F56C6C', 5, 'enabled'),
('leave_status', '已取消', 'cancelled', '#909399', 6, 'enabled');
```

---

## 10. 初始化数据

```sql
-- 初始化示例节假日数据(2024年)
INSERT INTO approval_holiday (date, name, type, year) VALUES
('2024-01-01', '元旦', 'national', 2024),
('2024-02-10', '春节', 'national', 2024),
('2024-02-11', '春节', 'national', 2024),
('2024-02-12', '春节', 'national', 2024),
('2024-04-04', '清明节', 'national', 2024),
('2024-05-01', '劳动节', 'national', 2024),
('2024-06-10', '端午节', 'national', 2024),
('2024-09-15', '中秋节', 'national', 2024),
('2024-10-01', '国庆节', 'national', 2024),
('2024-10-02', '国庆节', 'national', 2024),
('2024-10-03', '国庆节', 'national', 2024);

-- 初始化年假余额(2024年)
INSERT INTO approval_leave_balance (
  employee_id, year, annual_total, annual_used, annual_remaining
)
SELECT
  id AS employee_id,
  2024 AS year,
  CASE
    WHEN work_years < 1 THEN 5
    WHEN work_years < 10 THEN 10
    WHEN work_years < 20 THEN 15
    ELSE 20
  END AS annual_total,
  0 AS annual_used,
  CASE
    WHEN work_years < 1 THEN 5
    WHEN work_years < 10 THEN 10
    WHEN work_years < 20 THEN 15
    ELSE 20
  END AS annual_remaining
FROM sys_employee
WHERE status = 'active' AND is_deleted = 0;
```

---

## 11. API对应SQL查询

### 11.1 获取请假申请列表

```sql
-- 对应前端: GET /api/leave/requests
SELECT
  r.*,
  e.name AS applicant_name,
  e.position AS applicant_position,
  d.name AS department_name
FROM approval_leave_request r
LEFT JOIN sys_employee e ON r.applicant_id = e.id
LEFT JOIN sys_department d ON r.department_id = d.id
WHERE (? IS NULL OR r.applicant_id = ?)
  AND (? IS NULL OR r.department_id = ?)
  AND (? IS NULL OR r.type = ?)
  AND (? IS NULL OR r.status = ?)
  AND (? IS NULL OR r.start_time >= ?)
  AND (? IS NULL OR r.end_time <= ?)
ORDER BY r.created_at DESC
LIMIT ? OFFSET ?;
```

### 11.2 获取请假申请详情

```sql
-- 对应前端: GET /api/leave/requests/:id
SELECT
  r.*,
  e.name AS applicant_name,
  e.position AS applicant_position,
  e.phone AS applicant_phone,
  e.email AS applicant_email,
  d.name AS department_name,
  m.name AS manager_name
FROM approval_leave_request r
LEFT JOIN sys_employee e ON r.applicant_id = e.id
LEFT JOIN sys_department d ON r.department_id = d.id
LEFT JOIN sys_employee m ON e.manager_id = m.id
WHERE r.id = ?;
```

### 11.3 获取审批记录

```sql
-- 对应前端: GET /api/leave/requests/:id/approvals
SELECT
  a.*,
  e.position AS approver_position,
  d.name AS approver_department
FROM approval_leave_approval a
LEFT JOIN sys_employee e ON a.approver_id = e.id
LEFT JOIN sys_department d ON e.department_id = d.id
WHERE a.request_id = ?
ORDER BY a.approval_level ASC;
```

### 11.4 获取年假余额

```sql
-- 对应前端: GET /api/leave/balance
SELECT
  employee_id,
  year,
  annual_total,
  annual_used,
  annual_remaining,
  annual_total - annual_used AS available
FROM approval_leave_balance
WHERE employee_id = ? AND year = ?;
```

### 11.5 检查时间冲突

```sql
-- 对应前端: 检查请假时间是否冲突
SELECT COUNT(*) AS conflict_count
FROM approval_leave_request
WHERE applicant_id = ?
  AND status IN ('pending', 'approving', 'approved')
  AND (
    (start_time <= ? AND end_time > ?)
    OR (start_time < ? AND end_time >= ?)
    OR (start_time >= ? AND end_time <= ?)
  )
  AND id != ?;  -- 排除当前申请(用于更新时)
```

---

## 12. 业务规则说明

### 12.1 请假时长计算规则

- 工作日计算: 8小时 = 1天
- 支持半天请假: 0.5天
- 跨天请假: 按实际时间计算
- 节假日自动扣除

### 12.2 年假规则

- 工龄 < 1年: 5天
- 1年 ≤ 工龄 < 10年: 10天
- 10年 ≤ 工龄 < 20年: 15天
- 工龄 ≥ 20年: 20天

### 12.3 审批流程

1. **一级审批**: 部门负责人
2. **二级审批**: 人事部门(3天以上)
3. **三级审批**: 公司领导(7天以上)

### 12.4 请假限制

- 年假: 需提前申请,需有余额
- 病假: 需提供医院证明
- 事假: 扣除工资
- 婚假: 3天(晚婚可延长)
- 产假: 98天 + 15天难产 + 15天多胞胎每多一婴

---

**文档版本**: v1.0.0
**创建人**: AI开发助手
**最后更新**: 2026-01-11
