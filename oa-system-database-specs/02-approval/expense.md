# 费用报销数据库设计

> **对应前端规范**: [expense_Technical.md](../../../oa-system-frontend-specs/approval/expense/expense_Technical.md)
> **数据库**: MySQL 8.0+
> **表前缀**: `approval_`
> **版本**: v1.0.0

---

## 数据库表设计

### 1. 费用报销表 (approval_expense)

#### 1.1 表结构

```sql
CREATE TABLE approval_expense (
  -- 主键
  id VARCHAR(32) PRIMARY KEY COMMENT '报销单号: EXP+YYYYMMDD+序号',

  -- 基本信息
  applicant_id VARCHAR(32) NOT NULL COMMENT '报销人ID',
  department_id VARCHAR(32) NOT NULL COMMENT '部门ID',
  type VARCHAR(50) NOT NULL COMMENT '报销类型: travel/hospitality/office/transport/other',
  amount DECIMAL(10,2) NOT NULL COMMENT '总金额',
  reason TEXT NOT NULL COMMENT '报销事由',
  apply_date DATE NOT NULL COMMENT '申请日期',
  expense_date DATE NOT NULL COMMENT '费用发生日期',

  -- 审批状态
  status VARCHAR(20) NOT NULL DEFAULT 'draft' COMMENT '状态: draft/dept_pending/finance_pending/paid/rejected',

  -- 部门审批
  dept_approver_id VARCHAR(32) COMMENT '部门审批人ID',
  dept_approver_name VARCHAR(100) COMMENT '部门审批人姓名',
  dept_approval_status VARCHAR(20) COMMENT '部门审批状态: pending/approved/rejected',
  dept_approval_opinion TEXT COMMENT '部门审批意见',
  dept_approval_time DATETIME COMMENT '部门审批时间',

  -- 财务审批
  finance_approver_id VARCHAR(32) COMMENT '财务审批人ID',
  finance_approver_name VARCHAR(100) COMMENT '财务审批人姓名',
  finance_approval_status VARCHAR(20) COMMENT '财务审批状态: pending/approved/rejected',
  finance_approval_opinion TEXT COMMENT '财务审批意见',
  finance_approval_time DATETIME COMMENT '财务审批时间',

  -- 打款信息
  payment_date DATE COMMENT '打款时间',
  payment_proof VARCHAR(500) COMMENT '打款凭证URL',

  -- 审计字段
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted_at DATETIME COMMENT '删除时间',

  -- 索引
  INDEX idx_expense_applicant (applicant_id),
  INDEX idx_expense_department (department_id),
  INDEX idx_expense_status (status),
  INDEX idx_expense_type (type),
  INDEX idx_expense_apply_date (apply_date),
  INDEX idx_expense_amount (amount),
  INDEX idx_expense_dept_approver (dept_approver_id),
  INDEX idx_expense_finance_approver (finance_approver_id),
  INDEX idx_expense_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='费用报销单表';
```

#### 1.2 外键约束

```sql
-- 报销人外键
ALTER TABLE approval_expense
  ADD CONSTRAINT fk_expense_applicant
  FOREIGN KEY (applicant_id)
  REFERENCES sys_employee(id)
  ON DELETE RESTRICT
  ON UPDATE CASCADE;

-- 部门外键
ALTER TABLE approval_expense
  ADD CONSTRAINT fk_expense_department
  FOREIGN KEY (department_id)
  REFERENCES sys_department(id)
  ON DELETE RESTRICT
  ON UPDATE CASCADE;

-- 部门审批人外键
ALTER TABLE approval_expense
  ADD CONSTRAINT fk_expense_dept_approver
  FOREIGN KEY (dept_approver_id)
  REFERENCES sys_employee(id)
  ON DELETE SET NULL
  ON UPDATE CASCADE;

-- 财务审批人外键
ALTER TABLE approval_expense
  ADD CONSTRAINT fk_expense_finance_approver
  FOREIGN KEY (finance_approver_id)
  REFERENCES sys_employee(id)
  ON DELETE SET NULL
  ON UPDATE CASCADE;
```

#### 1.3 检查约束

```sql
-- 金额必须大于等于0
ALTER TABLE approval_expense
  ADD CONSTRAINT chk_expense_amount
  CHECK (amount >= 0);

-- 申请日期不能晚于今天
ALTER TABLE approval_expense
  ADD CONSTRAINT chk_expense_apply_date
  CHECK (apply_date <= CURDATE());

-- 费用发生日期不能晚于申请日期
ALTER TABLE approval_expense
  ADD CONSTRAINT chk_expense_date
  CHECK (expense_date <= apply_date);
```

---

### 2. 费用明细表 (approval_expense_item)

#### 2.1 表结构

```sql
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

  -- 索引
  INDEX idx_expense_item_expense (expense_id),
  INDEX idx_expense_item_date (expense_date),

  -- 外键约束
  FOREIGN KEY (expense_id)
    REFERENCES approval_expense(id)
    ON DELETE CASCADE
    ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='费用明细表';
```

#### 2.2 检查约束

```sql
-- 明细金额必须大于等于0
ALTER TABLE approval_expense_item
  ADD CONSTRAINT chk_expense_item_amount
  CHECK (amount >= 0);
```

---

### 3. 发票表 (approval_expense_invoice)

#### 3.1 表结构

```sql
CREATE TABLE approval_expense_invoice (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  expense_id VARCHAR(32) NOT NULL COMMENT '报销单号',
  invoice_type VARCHAR(20) NOT NULL COMMENT '发票类型: vat_special/vat_common/electronic',
  invoice_number VARCHAR(50) NOT NULL COMMENT '发票号码',
  amount DECIMAL(10,2) NOT NULL COMMENT '发票金额',
  invoice_date DATE NOT NULL COMMENT '开票日期',
  image_url VARCHAR(500) COMMENT '发票图片URL',
  verified BOOLEAN DEFAULT FALSE COMMENT '是否已验真',

  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

  -- 唯一约束: 发票号码全国唯一
  UNIQUE KEY uk_invoice_number (invoice_number),

  -- 索引
  INDEX idx_invoice_expense (expense_id),
  INDEX idx_invoice_number (invoice_number),
  INDEX idx_invoice_date (invoice_date),
  INDEX idx_invoice_verified (verified),

  -- 外键约束
  FOREIGN KEY (expense_id)
    REFERENCES approval_expense(id)
    ON DELETE CASCADE
    ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='发票表';
```

#### 3.2 检查约束

```sql
-- 发票金额必须大于等于0
ALTER TABLE approval_expense_invoice
  ADD CONSTRAINT chk_invoice_amount
  CHECK (amount >= 0);

-- 开票日期不能晚于今天
ALTER TABLE approval_expense_invoice
  ADD CONSTRAINT chk_invoice_date
  CHECK (invoice_date <= CURDATE());
```

---

### 4. 打款记录表 (approval_expense_payment)

#### 4.1 表结构

```sql
CREATE TABLE approval_expense_payment (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  expense_id VARCHAR(32) NOT NULL COMMENT '报销单号',
  amount DECIMAL(10,2) NOT NULL COMMENT '打款金额',
  payment_method VARCHAR(20) NOT NULL DEFAULT 'bank_transfer' COMMENT '打款方式: bank_transfer/cash/check',
  payment_date DATE NOT NULL COMMENT '打款日期',
  bank_account VARCHAR(100) COMMENT '收款账号',
  proof VARCHAR(500) COMMENT '打款凭证URL',
  status VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT '状态: pending/completed/failed',
  remark TEXT COMMENT '备注',

  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

  -- 索引
  INDEX idx_payment_expense (expense_id),
  INDEX idx_payment_status (status),
  INDEX idx_payment_date (payment_date),

  -- 外键约束
  FOREIGN KEY (expense_id)
    REFERENCES approval_expense(id)
    ON DELETE CASCADE
    ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='打款记录表';
```

#### 4.2 检查约束

```sql
-- 打款金额必须大于等于0
ALTER TABLE approval_expense_payment
  ADD CONSTRAINT chk_payment_amount
  CHECK (amount >= 0);

-- 打款日期不能晚于今天
ALTER TABLE approval_expense_payment
  ADD CONSTRAINT chk_payment_date
  CHECK (payment_date <= CURDATE());
```

---

## 5. 触发器设计

### 5.1 报销金额自动汇总触发器

```sql
DELIMITER $$

-- 明细新增时更新总金额
CREATE TRIGGER trg_expense_item_after_insert
AFTER INSERT ON approval_expense_item
FOR EACH ROW
BEGIN
  UPDATE approval_expense
  SET amount = (
    SELECT COALESCE(SUM(amount), 0)
    FROM approval_expense_item
    WHERE expense_id = NEW.expense_id
  )
  WHERE id = NEW.expense_id;
END$$

-- 明细删除时更新总金额
CREATE TRIGGER trg_expense_item_after_delete
AFTER DELETE ON approval_expense_item
FOR EACH ROW
BEGIN
  UPDATE approval_expense
  SET amount = (
    SELECT COALESCE(SUM(amount), 0)
    FROM approval_expense_item
    WHERE expense_id = OLD.expense_id
  )
  WHERE id = OLD.expense_id;
END$$

-- 明细更新时更新总金额
CREATE TRIGGER trg_expense_item_after_update
AFTER UPDATE ON approval_expense_item
FOR EACH ROW
BEGIN
  IF NEW.amount <> OLD.amount THEN
    UPDATE approval_expense
    SET amount = (
      SELECT COALESCE(SUM(amount), 0)
      FROM approval_expense_item
      WHERE expense_id = NEW.expense_id
    )
    WHERE id = NEW.expense_id;
  END IF;
END$$

DELIMITER ;
```

### 5.2 状态变更触发器

```sql
DELIMITER $$

-- 部门审批通过后,自动进入财务审批
CREATE TRIGGER trg_expense_after_update
AFTER UPDATE ON approval_expense
FOR EACH ROW
BEGIN
  -- 当部门审批状态变为approved,且财务审批还未开始时
  IF NEW.dept_approval_status = 'approved'
    AND OLD.dept_approval_status != 'approved'
    AND NEW.status = 'dept_pending' THEN
    UPDATE approval_expense
    SET status = 'finance_pending'
    WHERE id = NEW.id;
  END IF;

  -- 部门审批被拒绝
  IF NEW.dept_approval_status = 'rejected'
    AND OLD.dept_approval_status != 'rejected' THEN
    UPDATE approval_expense
    SET status = 'rejected'
    WHERE id = NEW.id;
  END IF;

  -- 财务审批通过
  IF NEW.finance_approval_status = 'approved'
    AND OLD.finance_approval_status != 'approved' THEN
    UPDATE approval_expense
    SET status = 'paid'
    WHERE id = NEW.id;
  END IF;

  -- 财务审批被拒绝
  IF NEW.finance_approval_status = 'rejected'
    AND OLD.finance_approval_status != 'rejected' THEN
    UPDATE approval_expense
    SET status = 'rejected'
    WHERE id = NEW.id;
  END IF;
END$$

DELIMITER ;
```

---

## 6. 存储过程设计

### 6.1 提交报销申请

```sql
DELIMITER $$

CREATE PROCEDURE sp_submit_expense(
  IN p_expense_id VARCHAR(32)
)
BEGIN
  DECLARE v_applicant_id VARCHAR(32);
  DECLARE v_dept_leader_id VARCHAR(32);

  -- 获取申请人信息
  SELECT applicant_id INTO v_applicant_id
  FROM approval_expense
  WHERE id = p_expense_id;

  -- 获取部门负责人
  SELECT leader_id INTO v_dept_leader_id
  FROM sys_department d
  INNER JOIN approval_expense e ON e.department_id = d.id
  WHERE e.id = p_expense_id;

  -- 更新状态为待部门审批
  UPDATE approval_expense
  SET status = 'dept_pending',
      dept_approver_id = v_dept_leader_id,
      dept_approver_name = (SELECT name FROM sys_employee WHERE id = v_dept_leader_id),
      dept_approval_status = 'pending'
  WHERE id = p_expense_id;
END$$

DELIMITER ;
```

### 6.2 部门审批

```sql
DELIMITER $$

CREATE PROCEDURE sp_dept_approve_expense(
  IN p_expense_id VARCHAR(32),
  IN p_approver_id VARCHAR(32),
  IN p_status VARCHAR(20),
  IN p_opinion TEXT
)
BEGIN
  DECLARE v_approver_name VARCHAR(100);

  -- 获取审批人姓名
  SELECT name INTO v_approver_name
  FROM sys_employee
  WHERE id = p_approver_id;

  -- 更新部门审批信息
  UPDATE approval_expense
  SET dept_approver_id = p_approver_id,
      dept_approver_name = v_approver_name,
      dept_approval_status = p_status,
      dept_approval_opinion = p_opinion,
      dept_approval_time = NOW(),
      -- 如果通过,状态改为待财务审批;如果拒绝,状态改为已拒绝
      status = CASE
        WHEN p_status = 'approved' THEN 'finance_pending'
        WHEN p_status = 'rejected' THEN 'rejected'
        ELSE status
      END
  WHERE id = p_expense_id;
END$$

DELIMITER ;
```

### 6.3 财务审批

```sql
DELIMITER $$

CREATE PROCEDURE sp_finance_approve_expense(
  IN p_expense_id VARCHAR(32),
  IN p_approver_id VARCHAR(32),
  IN p_status VARCHAR(20),
  IN p_opinion TEXT
)
BEGIN
  DECLARE v_approver_name VARCHAR(100);

  -- 获取审批人姓名
  SELECT name INTO v_approver_name
  FROM sys_employee
  WHERE id = p_approver_id;

  -- 更新财务审批信息
  UPDATE approval_expense
  SET finance_approver_id = p_approver_id,
      finance_approver_name = v_approver_name,
      finance_approval_status = p_status,
      finance_approval_opinion = p_opinion,
      finance_approval_time = NOW(),
      -- 如果通过,状态改为已打款;如果拒绝,状态改为已拒绝
      status = CASE
        WHEN p_status = 'approved' THEN 'paid'
        WHEN p_status = 'rejected' THEN 'rejected'
        ELSE status
      END
  WHERE id = p_expense_id;
END$$

DELIMITER ;
```

### 6.4 报销统计

```sql
DELIMITER $$

CREATE PROCEDURE sp_expense_statistics(
  IN p_department_id VARCHAR(32),
  IN p_start_date DATE,
  IN p_end_date DATE
)
BEGIN
  SELECT
    type,
    COUNT(*) AS count,
    SUM(amount) AS total_amount,
    SUM(CASE WHEN status = 'paid' THEN amount ELSE 0 END) AS paid_amount,
    SUM(CASE WHEN status = 'rejected' THEN amount ELSE 0 END) AS rejected_amount
  FROM approval_expense
  WHERE (p_department_id IS NULL OR department_id = p_department_id)
    AND (p_start_date IS NULL OR apply_date >= p_start_date)
    AND (p_end_date IS NULL OR apply_date <= p_end_date)
    AND deleted_at IS NULL
  GROUP BY type;
END$$

DELIMITER ;
```

---

## 7. 视图设计

### 7.1 报销单详情视图

```sql
CREATE VIEW v_expense_detail AS
SELECT
  e.id,
  e.applicant_id,
  emp.name AS applicant_name,
  emp.position AS applicant_position,
  emp.phone AS applicant_phone,
  emp.email AS applicant_email,
  e.department_id,
  d.name AS department_name,
  e.type,
  e.amount,
  e.reason,
  e.apply_date,
  e.expense_date,
  e.status,
  e.dept_approver_name,
  e.dept_approval_status,
  e.dept_approval_opinion,
  e.dept_approval_time,
  e.finance_approver_name,
  e.finance_approval_status,
  e.finance_approval_opinion,
  e.finance_approval_time,
  e.payment_date,
  e.payment_proof,
  e.created_at,
  e.updated_at,

  -- 明细数量
  (SELECT COUNT(*) FROM approval_expense_item WHERE expense_id = e.id) AS item_count,

  -- 发票数量
  (SELECT COUNT(*) FROM approval_expense_invoice WHERE expense_id = e.id) AS invoice_count,

  -- 已验真发票数量
  (SELECT COUNT(*) FROM approval_expense_invoice
   WHERE expense_id = e.id AND verified = TRUE) AS verified_invoice_count

FROM approval_expense e
LEFT JOIN sys_employee emp ON e.applicant_id = emp.id
LEFT JOIN sys_department d ON e.department_id = d.id
WHERE e.deleted_at IS NULL;
```

### 7.2 待审批报销视图

```sql
CREATE VIEW v_expense_pending_dept AS
SELECT
  e.id,
  e.applicant_id,
  emp.name AS applicant_name,
  d.name AS department_name,
  e.type,
  e.amount,
  e.reason,
  e.apply_date,
  e.created_at
FROM approval_expense e
LEFT JOIN sys_employee emp ON e.applicant_id = emp.id
LEFT JOIN sys_department d ON e.department_id = d.id
WHERE e.status = 'dept_pending'
  AND e.deleted_at IS NULL
ORDER BY e.apply_date ASC;

CREATE VIEW v_expense_pending_finance AS
SELECT
  e.id,
  e.applicant_id,
  emp.name AS applicant_name,
  d.name AS department_name,
  e.type,
  e.amount,
  e.reason,
  e.apply_date,
  e.dept_approver_name,
  e.dept_approval_time,
  e.created_at
FROM approval_expense e
LEFT JOIN sys_employee emp ON e.applicant_id = emp.id
LEFT JOIN sys_department d ON e.department_id = d.id
WHERE e.status = 'finance_pending'
  AND e.deleted_at IS NULL
ORDER BY e.dept_approval_time ASC;
```

---

## 8. 数据字典映射

### 8.1 报销类型字典 (expense_type)

```sql
INSERT INTO sys_dict_type (code, name, category, status) VALUES
('expense_type', '报销类型', 'business', 'enabled');

INSERT INTO sys_dict_item (type_code, label, value, sort, status) VALUES
('expense_type', '差旅费', 'travel', 1, 'enabled'),
('expense_type', '招待费', 'hospitality', 2, 'enabled'),
('expense_type', '办公费', 'office', 3, 'enabled'),
('expense_type', '交通费', 'transport', 4, 'enabled'),
('expense_type', '其他', 'other', 5, 'enabled');
```

### 8.2 报销状态字典 (expense_status)

```sql
INSERT INTO sys_dict_type (code, name, category, status) VALUES
('expense_status', '报销状态', 'business', 'enabled');

INSERT INTO sys_dict_item (type_code, label, value, color, sort, status) VALUES
('expense_status', '草稿', 'draft', '#909399', 1, 'enabled'),
('expense_status', '待部门审批', 'dept_pending', '#E6A23C', 2, 'enabled'),
('expense_status', '待财务审批', 'finance_pending', '#409EFF', 3, 'enabled'),
('expense_status', '已打款', 'paid', '#67C23A', 4, 'enabled'),
('expense_status', '已拒绝', 'rejected', '#F56C6C', 5, 'enabled');
```

### 8.3 发票类型字典 (invoice_type)

```sql
INSERT INTO sys_dict_type (code, name, category, status) VALUES
('invoice_type', '发票类型', 'business', 'enabled');

INSERT INTO sys_dict_item (type_code, label, value, sort, status) VALUES
('invoice_type', '增值税专用发票', 'vat_special', 1, 'enabled'),
('invoice_type', '增值税普通发票', 'vat_common', 2, 'enabled'),
('invoice_type', '电子发票', 'electronic', 3, 'enabled');
```

### 8.4 打款方式字典 (payment_method)

```sql
INSERT INTO sys_dict_type (code, name, category, status) VALUES
('payment_method', '打款方式', 'business', 'enabled');

INSERT INTO sys_dict_item (type_code, label, value, sort, status) VALUES
('payment_method', '银行转账', 'bank_transfer', 1, 'enabled'),
('payment_method', '现金', 'cash', 2, 'enabled'),
('payment_method', '支票', 'check', 3, 'enabled');
```

---

## 9. 初始化数据

```sql
-- 插入示例报销数据
INSERT INTO approval_expense (
  id, applicant_id, department_id, type, amount,
  reason, apply_date, expense_date, status
) VALUES
('EXP20240101001', 'EMP20230115001', 'DEPT0002', 'travel', 3500.00,
 '北京出差交通费', '2024-01-15', '2024-01-10', 'paid'),
('EXP20240102001', 'EMP20230115002', 'DEPT0003', 'hospitality', 800.00,
 '客户招待费', '2024-01-20', '2024-01-18', 'paid'),
('EXP20240103001', 'EMP20230115003', 'DEPT0004', 'office', 250.00,
 '办公用品采购', '2024-01-25', '2024-01-23', 'dept_pending');

-- 插入示例明细数据
INSERT INTO approval_expense_item (
  expense_id, description, amount, expense_date, category
) VALUES
('EXP20240101001', '高铁票', 800.00, '2024-01-08', '交通费'),
('EXP20240101001', '住宿费', 600.00, '2024-01-08', '住宿费'),
('EXP20240101001', '餐费', 300.00, '2024-01-09', '餐饮费'),
('EXP20240101001', '交通费', 1800.00, '2024-01-10', '交通费');

-- 插入示例发票数据
INSERT INTO approval_expense_invoice (
  expense_id, invoice_type, invoice_number, amount, invoice_date
) VALUES
('EXP20240101001', 'vat_common', '12345678', 800.00, '2024-01-08'),
('EXP20240101001', 'electronic', '87654321', 600.00, '2024-01-08');
```

---

## 10. API对应SQL查询

### 10.1 获取报销列表

```sql
-- 对应前端: GET /api/expense
SELECT
  e.*,
  emp.name AS applicant_name,
  d.name AS department_name
FROM approval_expense e
LEFT JOIN sys_employee emp ON e.applicant_id = emp.id
LEFT JOIN sys_department d ON e.department_id = d.id
WHERE (? IS NULL OR e.applicant_id = ?)
  AND (? IS NULL OR e.department_id = ?)
  AND (? IS NULL OR e.type = ?)
  AND (? IS NULL OR e.status = ?)
  AND (? IS NULL OR e.apply_date >= ?)
  AND (? IS NULL OR e.apply_date <= ?)
  AND e.deleted_at IS NULL
ORDER BY e.created_at DESC
LIMIT ? OFFSET ?;
```

### 10.2 获取报销详情

```sql
-- 对应前端: GET /api/expense/:id
SELECT
  e.*,
  emp.name AS applicant_name,
  emp.position AS applicant_position,
  emp.phone AS applicant_phone,
  emp.email AS applicant_email,
  d.name AS department_name,
  dl.name AS dept_leader_name,
  fl.name AS finance_leader_name
FROM approval_expense e
LEFT JOIN sys_employee emp ON e.applicant_id = emp.id
LEFT JOIN sys_department d ON e.department_id = d.id
LEFT JOIN sys_employee dl ON e.dept_approver_id = dl.id
LEFT JOIN sys_employee fl ON e.finance_approver_id = fl.id
WHERE e.id = ? AND e.deleted_at IS NULL;
```

### 10.3 获取费用明细

```sql
-- 对应前端: GET /api/expense/:id/items
SELECT *
FROM approval_expense_item
WHERE expense_id = ?
ORDER BY sort_order ASC, expense_date ASC;
```

### 10.4 获取发票列表

```sql
-- 对应前端: GET /api/expense/:id/invoices
SELECT *
FROM approval_expense_invoice
WHERE expense_id = ?
ORDER BY invoice_date DESC;
```

---

## 11. 业务规则说明

### 11.1 报销金额限制

- 差旅费: 单次不超过5000元
- 招待费: 单次不超过2000元
- 办公费: 单次不超过1000元
- 超过限制需要特殊审批

### 11.2 审批流程

1. **部门审批**: 部门负责人审批
2. **财务审批**: 财务部门审批(金额>3000元)
3. **打款**: 审批通过后3个工作日内打款

### 11.3 发票要求

- 所有报销必须提供发票
- 发票金额必须>=报销金额
- 电子发票需要验真
- 增值税专用发票需要提供公司信息

### 11.4 打款规则

- 打款时间: 审批通过后3个工作日内
- 打款方式: 优先银行转账
- 打款账户: 使用员工工资卡账户

---

**文档版本**: v1.0.0
**创建人**: AI开发助手
**最后更新**: 2026-01-11
