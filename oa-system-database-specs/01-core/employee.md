# 员工管理数据库设计

> **对应前端规范**: [employee_Technical.md](../../oa-system-frontend-specs/core/employee/employee_Technical.md)
> **数据库**: MySQL 8.0+
> **表前缀**: `sys_`
> **版本**: v1.0.0

---

## 数据库表设计

### 1. 员工表 (sys_employee)

#### 1.1 表结构

```sql
CREATE TABLE sys_employee (
  -- 主键
  id VARCHAR(20) PRIMARY KEY COMMENT '员工编号(格式: EMP+YYYYMMDD+序号)',

  -- 基本信息
  name VARCHAR(50) NOT NULL COMMENT '姓名',
  english_name VARCHAR(50) COMMENT '英文名',
  gender ENUM('male', 'female') NOT NULL COMMENT '性别: male男 female女',
  birth_date DATE COMMENT '出生日期',
  phone VARCHAR(20) NOT NULL COMMENT '联系电话',
  email VARCHAR(100) NOT NULL COMMENT '邮箱',
  avatar VARCHAR(500) COMMENT '头像URL',

  -- 工作信息
  department_id VARCHAR(20) NOT NULL COMMENT '部门ID',
  position VARCHAR(100) NOT NULL COMMENT '职位',
  level VARCHAR(50) COMMENT '职级',
  manager_id VARCHAR(20) COMMENT '直属上级ID',
  join_date DATE NOT NULL COMMENT '入职日期',
  probation_status ENUM('probation', 'regular', 'resigned') DEFAULT 'regular' COMMENT '试用期状态',
  probation_end_date DATE COMMENT '试用期结束日期',
  work_years INT DEFAULT 0 COMMENT '工龄(年)',

  -- 状态
  status ENUM('active', 'resigned', 'suspended') NOT NULL DEFAULT 'active' COMMENT '员工状态: active在职 resigned离职 suspended停薪留职',

  -- 其他信息
  office_location VARCHAR(200) COMMENT '办公位置',
  emergency_contact VARCHAR(50) COMMENT '紧急联系人',
  emergency_phone VARCHAR(20) COMMENT '紧急联系电话',

  -- 审计字段
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  created_by VARCHAR(20) COMMENT '创建人ID',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  updated_by VARCHAR(20) COMMENT '更新人ID',
  is_deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除(0否1是)',
  deleted_at DATETIME COMMENT '删除时间',
  deleted_by VARCHAR(20) COMMENT '删除人ID',
  version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='员工信息表';
```

#### 1.2 索引设计

```sql
-- 主键索引
-- PRIMARY KEY (id)

-- 唯一索引
CREATE UNIQUE INDEX uk_employee_email ON sys_employee(email, is_deleted) COMMENT '邮箱唯一索引';
CREATE UNIQUE INDEX uk_employee_phone ON sys_employee(phone, is_deleted) COMMENT '手机号唯一索引';

-- 普通索引
CREATE INDEX idx_employee_department ON sys_employee(department_id) COMMENT '部门ID索引';
CREATE INDEX idx_employee_manager ON sys_employee(manager_id) COMMENT '上级ID索引';
CREATE INDEX idx_employee_status ON sys_employee(status) COMMENT '状态索引';
CREATE INDEX idx_employee_join_date ON sys_employee(join_date) COMMENT '入职日期索引';
CREATE INDEX idx_employee_probation_status ON sys_employee(probation_status) COMMENT '试用期状态索引';

-- 组合索引(常用查询)
CREATE INDEX idx_employee_dept_status ON sys_employee(department_id, status) COMMENT '部门+状态组合索引';
CREATE INDEX idx_employee_created_at ON sys_employee(created_at) COMMENT '创建时间索引';
```

#### 1.3 外键约束

```sql
-- 部门外键
ALTER TABLE sys_employee
  ADD CONSTRAINT fk_employee_department
  FOREIGN KEY (department_id)
  REFERENCES sys_department(id)
  ON DELETE RESTRICT
  ON UPDATE CASCADE;

-- 上级外键(自关联)
ALTER TABLE sys_employee
  ADD CONSTRAINT fk_employee_manager
  FOREIGN KEY (manager_id)
  REFERENCES sys_employee(id)
  ON DELETE SET NULL
  ON UPDATE CASCADE;
```

---

### 2. 员工操作日志表 (sys_employee_operation_log)

#### 2.1 表结构

```sql
CREATE TABLE sys_employee_operation_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',
  employee_id VARCHAR(20) NOT NULL COMMENT '员工ID',
  operation VARCHAR(50) NOT NULL COMMENT '操作类型',
  operator VARCHAR(20) NOT NULL COMMENT '操作人ID',
  operator_name VARCHAR(50) COMMENT '操作人姓名',
  timestamp DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  details TEXT COMMENT '详细信息',
  ip_address VARCHAR(50) COMMENT 'IP地址',
  user_agent VARCHAR(500) COMMENT '用户代理'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='员工操作日志表';
```

#### 2.2 索引设计

```sql
CREATE INDEX idx_log_employee ON sys_employee_operation_log(employee_id) COMMENT '员工ID索引';
CREATE INDEX idx_log_operator ON sys_employee_operation_log(operator) COMMENT '操作人索引';
CREATE INDEX idx_log_operation ON sys_employee_operation_log(operation) COMMENT '操作类型索引';
CREATE INDEX idx_log_timestamp ON sys_employee_operation_log(timestamp) COMMENT '操作时间索引';
```

---

### 3. 触发器设计

#### 3.1 工龄自动更新触发器

```sql
DELIMITER $$

CREATE TRIGGER trg_employee_before_insert
BEFORE INSERT ON sys_employee
FOR EACH ROW
BEGIN
  -- 计算工龄
  IF NEW.join_date IS NOT NULL THEN
    SET NEW.work_years = TIMESTAMPDIFF(YEAR, NEW.join_date, CURDATE());
  END IF;
END$$

CREATE TRIGGER trg_employee_before_update
BEFORE UPDATE ON sys_employee
FOR EACH ROW
BEGIN
  -- 入职日期变更时重新计算工龄
  IF NEW.join_date <> OLD.join_date THEN
    SET NEW.work_years = TIMESTAMPDIFF(YEAR, NEW.join_date, CURDATE());
  END IF;
END$$

DELIMITER ;
```

---

### 4. 存储过程设计

#### 4.1 员工统计存储过程

```sql
DELIMITER $$

CREATE PROCEDURE sp_employee_statistics()
BEGIN
  SELECT
    COUNT(*) AS total,
    SUM(CASE WHEN status = 'active' THEN 1 ELSE 0 END) AS active_count,
    SUM(CASE WHEN status = 'resigned' THEN 1 ELSE 0 END) AS resigned_count,
    SUM(CASE WHEN status = 'suspended' THEN 1 ELSE 0 END) AS suspended_count,
    SUM(CASE WHEN probation_status = 'probation' THEN 1 ELSE 0 END) AS probation_count,
    SUM(CASE WHEN DATE(join_date) = CURDATE() THEN 1 ELSE 0 END) AS new_today
  FROM sys_employee
  WHERE is_deleted = 0;
END$$

DELIMITER ;
```

#### 4.2 部门员工统计存储过程

```sql
DELIMITER $$

CREATE PROCEDURE sp_employee_by_department()
BEGIN
  SELECT
    d.id AS department_id,
    d.name AS department_name,
    COUNT(e.id) AS count
  FROM sys_department d
  LEFT JOIN sys_employee e ON d.id = e.department_id AND e.is_deleted = 0 AND e.status = 'active'
  WHERE d.is_deleted = 0
  GROUP BY d.id, d.name
  ORDER BY count DESC;
END$$

DELIMITER ;
```

---

## 5. 数据字典映射

### 5.1 性别字典 (gender)

```sql
-- 字典类型: gender
INSERT INTO sys_dict_type (code, name, category, status) VALUES
('gender', '性别', 'system', 'enabled');

INSERT INTO sys_dict_item (type_code, label, value, sort, status) VALUES
('gender', '男', 'male', 1, 'enabled'),
('gender', '女', 'female', 2, 'enabled');
```

### 5.2 员工状态字典 (employee_status)

```sql
-- 字典类型: employee_status
INSERT INTO sys_dict_type (code, name, category, status) VALUES
('employee_status', '员工状态', 'business', 'enabled');

INSERT INTO sys_dict_item (type_code, label, value, color, sort, status) VALUES
('employee_status', '在职', 'active', '#67C23A', 1, 'enabled'),
('employee_status', '离职', 'resigned', '#909399', 2, 'enabled'),
('employee_status', '停薪留职', 'suspended', '#E6A23C', 3, 'enabled');
```

### 5.3 试用期状态字典 (probation_status)

```sql
-- 字典类型: probation_status
INSERT INTO sys_dict_type (code, name, category, status) VALUES
('probation_status', '试用期状态', 'business', 'enabled');

INSERT INTO sys_dict_item (type_code, label, value, color, sort, status) VALUES
('probation_status', '试用期内', 'probation', '#409EFF', 1, 'enabled'),
('probation_status', '已转正', 'regular', '#67C23A', 2, 'enabled'),
('probation_status', '已离职', 'resigned', '#909399', 3, 'enabled');
```

---

## 6. 视图设计

### 6.1 员工详情视图

```sql
CREATE VIEW v_employee_detail AS
SELECT
  e.id,
  e.name,
  e.english_name,
  e.gender,
  e.birth_date,
  e.phone,
  e.email,
  e.avatar,
  e.department_id,
  d.name AS department_name,
  e.position,
  e.level,
  e.manager_id,
  m.name AS manager_name,
  e.join_date,
  e.probation_status,
  e.probation_end_date,
  e.work_years,
  e.status,
  e.office_location,
  e.emergency_contact,
  e.emergency_phone,
  e.created_at,
  e.updated_at
FROM sys_employee e
LEFT JOIN sys_department d ON e.department_id = d.id
LEFT JOIN sys_employee m ON e.manager_id = m.id
WHERE e.is_deleted = 0;
```

### 6.2 员工统计视图

```sql
CREATE VIEW v_employee_statistics AS
SELECT
  d.id AS department_id,
  d.name AS department_name,
  COUNT(e.id) AS total_count,
  SUM(CASE WHEN e.status = 'active' THEN 1 ELSE 0 END) AS active_count,
  SUM(CASE WHEN e.probation_status = 'probation' THEN 1 ELSE 0 END) AS probation_count,
  SUM(CASE WHEN DATE(e.join_date) >= DATE_SUB(CURDATE(), INTERVAL 1 MONTH) THEN 1 ELSE 0 END) AS new_this_month
FROM sys_department d
LEFT JOIN sys_employee e ON d.id = e.department_id AND e.is_deleted = 0
WHERE d.is_deleted = 0
GROUP BY d.id, d.name;
```

---

## 7. 定时任务设计

### 7.1 工龄自动更新任务

```sql
-- 创建定时事件(每天凌晨1点执行)
SET GLOBAL event_scheduler = ON;

CREATE EVENT evt_update_work_years
ON SCHEDULE EVERY 1 DAY
STARTS (TIMESTAMP(CURRENT_DATE) + INTERVAL 1 DAY + INTERVAL 1 HOUR)
DO
UPDATE sys_employee
SET work_years = TIMESTAMPDIFF(YEAR, join_date, CURDATE())
WHERE is_deleted = 0;
```

### 7.2 生日提醒任务

```sql
-- 创建生日提醒存储过程
DELIMITER $$

CREATE PROCEDURE sp_birthday_reminder()
BEGIN
  -- 查询当天生日的员工
  SELECT
    id,
    name,
    email,
    DATE_FORMAT(birth_date, '%m-%d') AS birthday
  FROM sys_employee
  WHERE DATE_FORMAT(birth_date, '%m-%d') = DATE_FORMAT(CURDATE(), '%m-%d')
    AND status = 'active'
    AND is_deleted = 0;
END$$

DELIMITER ;

-- 创建定时事件(每天早上9点执行)
CREATE EVENT evt_birthday_reminder
ON SCHEDULE EVERY 1 DAY
STARTS (TIMESTAMP(CURRENT_DATE) + INTERVAL 9 HOUR)
DO CALL sp_birthday_reminder();
```

### 7.3 转正提醒任务

```sql
-- 创建转正提醒存储过程
DELIMITER $$

CREATE PROCEDURE sp_probation_reminder()
BEGIN
  -- 查询7天内试用期到期的员工
  SELECT
    e.id,
    e.name,
    e.email,
    e.probation_end_date,
    d.name AS department_name,
    m.name AS manager_name,
    m.email AS manager_email
  FROM sys_employee e
  LEFT JOIN sys_department d ON e.department_id = d.id
  LEFT JOIN sys_employee m ON e.manager_id = m.id
  WHERE e.probation_status = 'probation'
    AND e.probation_end_date BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL 7 DAY)
    AND e.is_deleted = 0;
END$$

DELIMITER ;

-- 创建定时事件(每天早上9点执行)
CREATE EVENT evt_probation_reminder
ON SCHEDULE EVERY 1 DAY
STARTS (TIMESTAMP(CURRENT_DATE) + INTERVAL 9 HOUR)
DO CALL sp_probation_reminder();
```

---

## 8. 数据完整性保障

### 8.1 检查约束

```sql
-- 邮箱格式检查
ALTER TABLE sys_employee
  ADD CONSTRAINT chk_email_format
  CHECK (email REGEXP '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$');

-- 手机号格式检查
ALTER TABLE sys_employee
  ADD CONSTRAINT chk_phone_format
  CHECK (phone REGEXP '^1[3-9][0-9]{9}$');

-- 入职日期不能晚于今天
ALTER TABLE sys_employee
  ADD CONSTRAINT chk_join_date
  CHECK (join_date <= CURDATE());

-- 试用期结束日期必须晚于入职日期
ALTER TABLE sys_employee
  ADD CONSTRAINT chk_probation_date
  CHECK (probation_end_date IS NULL OR probation_end_date > join_date);
```

---

## 9. 性能优化建议

### 9.1 分区策略

```sql
-- 按入职年份分区(适用于数据量大的情况)
ALTER TABLE sys_employee
PARTITION BY RANGE (YEAR(join_date)) (
  PARTITION p2020 VALUES LESS THAN (2021),
  PARTITION p2021 VALUES LESS THAN (2022),
  PARTITION p2022 VALUES LESS THAN (2023),
  PARTITION p2023 VALUES LESS THAN (2024),
  PARTITION p2024 VALUES LESS THAN (2025),
  PARTITION p2025 VALUES LESS THAN (2026),
  PARTITION p_future VALUES LESS THAN MAXVALUE
);
```

### 9.2 查询优化

```sql
-- 使用EXPLAIN分析查询
EXPLAIN SELECT * FROM sys_employee
WHERE department_id = 'DEPT001' AND status = 'active';

-- 优化建议:
-- 1. 确保索引 idx_employee_dept_status 存在
-- 2. 避免SELECT *, 只查询需要的字段
-- 3. 使用覆盖索引
```

---

## 10. 初始化数据

```sql
-- 插入示例员工数据
INSERT INTO sys_employee (
  id, name, english_name, gender, phone, email,
  department_id, position, join_date, status
) VALUES
('EMP20230115001', '张三', 'Tom', 'male', '13800138000', 'zhangsan@company.com',
 'DEPT0001', '技术经理', '2023-01-15', 'active'),
('EMP20230115002', '李四', 'Jerry', 'male', '13900139000', 'lisi@company.com',
 'DEPT0002', '产品经理', '2023-02-20', 'active'),
('EMP20230115003', '王五', 'Alice', 'female', '13700137000', 'wangwu@company.com',
 'DEPT0003', 'UI设计师', '2023-03-10', 'active'),
('EMP20230115004', '赵六', 'Bob', 'male', '13600136000', 'zhaoliu@company.com',
 'DEPT0001', '前端工程师', '2024-01-05', 'active');
```

---

## 11. API对应SQL查询

### 11.1 获取员工列表

```sql
-- 对应前端: GET /api/employees
SELECT
  e.*,
  d.name AS department_name,
  m.name AS manager_name
FROM sys_employee e
LEFT JOIN sys_department d ON e.department_id = d.id
LEFT JOIN sys_employee m ON e.manager_id = m.id
WHERE e.is_deleted = 0
  AND (? IS NULL OR e.status = ?)
  AND (? IS NULL OR e.department_id IN (?))
  AND (? IS NULL OR e.position = ?)
  AND (? IS NULL OR e.probation_status = ?)
  AND (? IS NULL OR e.gender = ?)
  AND (? IS NULL OR e.join_date BETWEEN ? AND ?)
  AND (? IS NULL OR
       e.name LIKE CONCAT('%', ?, '%')
       OR e.id LIKE CONCAT('%', ?, '%')
       OR e.phone LIKE CONCAT('%', ?, '%')
      )
ORDER BY e.created_at DESC
LIMIT ? OFFSET ?;
```

### 11.2 获取员工详情

```sql
-- 对应前端: GET /api/employees/:id
SELECT
  e.*,
  d.id AS department_id,
  d.name AS department_name,
  m.id AS manager_id,
  m.name AS manager_name,
  m.position AS manager_position
FROM sys_employee e
LEFT JOIN sys_department d ON e.department_id = d.id
LEFT JOIN sys_employee m ON e.manager_id = m.id
WHERE e.id = ? AND e.is_deleted = 0;
```

### 11.3 获取操作记录

```sql
-- 对应前端: GET /api/employees/:id/logs
SELECT
  l.*,
  o.name AS operator_name
FROM sys_employee_operation_log l
LEFT JOIN sys_employee o ON l.operator = o.id
WHERE l.employee_id = ?
ORDER BY l.timestamp DESC
LIMIT ? OFFSET ?;
```

---

**文档版本**: v1.0.0
**创建人**: AI开发助手
**最后更新**: 2026-01-11
