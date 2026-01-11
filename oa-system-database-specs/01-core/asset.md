# 资产管理数据库设计

> **对应前端规范**: [asset_Technical.md](../../oa-system-frontend-specs/core/asset/asset_Technical.md)
> **数据库**: MySQL 8.0+
> **表前缀**: `biz_`
> **版本**: v1.0.0

---

## 数据库表设计

### 1. 资产表 (biz_asset)

#### 1.1 表结构

```sql
CREATE TABLE biz_asset (
  -- 主键
  id VARCHAR(20) PRIMARY KEY COMMENT '资产编号(格式: ASSET+序号)',

  -- 基本信息
  name VARCHAR(200) NOT NULL COMMENT '资产名称',
  category ENUM('electronic', 'furniture', 'book', 'other') NOT NULL COMMENT '资产类别',
  brand_model VARCHAR(200) COMMENT '品牌/型号',
  purchase_date DATE NOT NULL COMMENT '购置日期',
  purchase_price DECIMAL(10,2) NOT NULL COMMENT '购置金额(元)',
  current_value DECIMAL(10,2) COMMENT '当前价值(自动计算折旧)',

  -- 状态与位置
  status ENUM('stock', 'in_use', 'borrowed', 'maintenance', 'scrapped') NOT NULL DEFAULT 'stock' COMMENT '资产状态',
  user_id VARCHAR(20) COMMENT '使用人/保管人ID',
  location VARCHAR(200) COMMENT '存放位置',

  -- 借用信息
  borrow_date DATE COMMENT '借出日期',
  expected_return_date DATE COMMENT '预计归还日期',
  actual_return_date DATE COMMENT '实际归还日期',

  -- 其他信息
  maintenance_record JSON COMMENT '维护记录(JSON数组)',
  images JSON COMMENT '资产图片URL数组',
  notes TEXT COMMENT '备注信息',

  -- 审计字段
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  created_by VARCHAR(20) COMMENT '创建人ID',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  updated_by VARCHAR(20) COMMENT '更新人ID',
  is_deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除(0否1是)',
  deleted_at DATETIME COMMENT '删除时间',
  deleted_by VARCHAR(20) COMMENT '删除人ID',
  version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='资产信息表';
```

#### 1.2 索引设计

```sql
-- 主键索引
-- PRIMARY KEY (id)

-- 唯一索引
-- 无

-- 普通索引
CREATE INDEX idx_asset_category ON biz_asset(category) COMMENT '类别索引';
CREATE INDEX idx_asset_status ON biz_asset(status) COMMENT '状态索引';
CREATE INDEX idx_asset_user ON biz_asset(user_id) COMMENT '使用人索引';
CREATE INDEX idx_asset_purchase_date ON biz_asset(purchase_date) COMMENT '购置日期索引';
CREATE INDEX idx_asset_price ON biz_asset(purchase_price) COMMENT '价格索引';
CREATE INDEX idx_asset_location ON biz_asset(location) COMMENT '位置索引';

-- 组合索引
CREATE INDEX idx_asset_status_category ON biz_asset(status, category) COMMENT '状态+类别组合索引';
CREATE INDEX idx_asset_user_status ON biz_asset(user_id, status) COMMENT '使用人+状态组合索引';

-- 全文索引
CREATE FULLTEXT INDEX ft_asset_name ON biz_asset(name, brand_model) COMMENT '资产名称全文索引';
```

#### 1.3 外键约束

```sql
-- 使用人外键
ALTER TABLE biz_asset
  ADD CONSTRAINT fk_asset_user
  FOREIGN KEY (user_id)
  REFERENCES sys_employee(id)
  ON DELETE SET NULL
  ON UPDATE CASCADE;
```

---

### 2. 借还记录表 (biz_asset_borrow_record)

#### 2.1 表结构

```sql
CREATE TABLE biz_asset_borrow_record (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
  asset_id VARCHAR(20) NOT NULL COMMENT '资产ID',
  asset_name VARCHAR(200) NOT NULL COMMENT '资产名称(快照)',
  borrower_id VARCHAR(20) NOT NULL COMMENT '借用人ID',
  borrower_name VARCHAR(50) NOT NULL COMMENT '借用人姓名(快照)',
  borrow_date DATE NOT NULL COMMENT '借出日期',
  expected_return_date DATE COMMENT '预计归还日期',
  actual_return_date DATE COMMENT '实际归还日期',
  status ENUM('active', 'returned', 'overdue') NOT NULL DEFAULT 'active' COMMENT '记录状态',
  notes TEXT COMMENT '备注',

  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

  INDEX idx_borrow_asset ON biz_asset_borrow_record(asset_id) COMMENT '资产ID索引',
  INDEX idx_borrow_borrower ON biz_asset_borrow_record(borrower_id) COMMENT '借用人索引',
  INDEX idx_borrow_status ON biz_asset_borrow_record(status) COMMENT '状态索引',
  INDEX idx_borrow_date ON biz_asset_borrow_record(borrow_date) COMMENT '借出日期索引',

  FOREIGN KEY (asset_id) REFERENCES biz_asset(id) ON DELETE RESTRICT,
  FOREIGN KEY (borrower_id) REFERENCES sys_employee(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='资产借还记录表';
```

---

### 3. 维护记录表 (biz_asset_maintenance)

#### 3.1 表结构

```sql
CREATE TABLE biz_asset_maintenance (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
  asset_id VARCHAR(20) NOT NULL COMMENT '资产ID',
  type ENUM('repair', 'maintenance', 'check') NOT NULL COMMENT '类型: 修理/保养/检查',
  description TEXT NOT NULL COMMENT '描述',
  cost DECIMAL(10,2) COMMENT '费用',
  start_date DATE NOT NULL COMMENT '开始日期',
  end_date DATE COMMENT '结束日期',
  operator_id VARCHAR(20) NOT NULL COMMENT '操作人ID',
  operator_name VARCHAR(50) NOT NULL COMMENT '操作人姓名',

  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

  INDEX idx_maint_asset ON biz_asset_maintenance(asset_id) COMMENT '资产ID索引',
  INDEX idx_maint_type ON biz_asset_maintenance(type) COMMENT '类型索引',
  INDEX idx_maint_date ON biz_asset_maintenance(start_date) COMMENT '开始日期索引',

  FOREIGN KEY (asset_id) REFERENCES biz_asset(id) ON DELETE RESTRICT,
  FOREIGN KEY (operator_id) REFERENCES sys_employee(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='资产维护记录表';
```

---

## 4. 触发器设计

### 4.1 资产状态更新触发器

```sql
DELIMITER $$

-- 借出时更新资产状态
CREATE TRIGGER trg_borrow_after_insert
AFTER INSERT ON biz_asset_borrow_record
FOR EACH ROW
BEGIN
  UPDATE biz_asset
  SET status = 'borrowed',
      user_id = NEW.borrower_id,
      borrow_date = NEW.borrow_date,
      expected_return_date = NEW.expected_return_date
  WHERE id = NEW.asset_id;
END$$

-- 归还时更新资产状态
CREATE TRIGGER trg_borrow_after_update
AFTER UPDATE ON biz_asset_borrow_record
FOR EACH ROW
BEGIN
  -- 如果归还状态变更
  IF NEW.status = 'returned' AND OLD.status != 'returned' THEN
    UPDATE biz_asset
    SET status = 'stock',
        user_id = NULL,
        actual_return_date = NEW.actual_return_date
    WHERE id = NEW.asset_id;
  END IF;
END$$

DELIMITER ;
```

---

## 5. 存储过程设计

### 5.1 计算资产当前价值(折旧)

```sql
DELIMITER $$

CREATE PROCEDURE sp_calculate_asset_value()
BEGIN
  -- 电子设备: 3年折旧, 每年33%
  UPDATE biz_asset
  SET current_value = GREATEST(0, purchase_price * POWER(0.67, TIMESTAMPDIFF(YEAR, purchase_date, CURDATE())))
  WHERE category = 'electronic' AND status != 'scrapped';

  -- 家具: 5年折旧, 每年20%
  UPDATE biz_asset
  SET current_value = GREATEST(0, purchase_price * POWER(0.8, TIMESTAMPDIFF(YEAR, purchase_date, CURDATE())))
  WHERE category = 'furniture' AND status != 'scrapped';

  -- 书籍: 不折旧
  UPDATE biz_asset
  SET current_value = purchase_price
  WHERE category = 'book' AND status != 'scrapped';

  -- 其他: 3年折旧
  UPDATE biz_asset
  SET current_value = GREATEST(0, purchase_price * POWER(0.67, TIMESTAMPDIFF(YEAR, purchase_date, CURDATE())))
  WHERE category = 'other' AND status != 'scrapped';
END$$

DELIMITER ;
```

### 5.2 资产统计

```sql
DELIMITER $$

CREATE PROCEDURE sp_asset_statistics()
BEGIN
  SELECT
    COUNT(*) AS total,
    SUM(CASE WHEN category = 'electronic' THEN 1 ELSE 0 END) AS electronic_count,
    SUM(CASE WHEN category = 'furniture' THEN 1 ELSE 0 END) AS furniture_count,
    SUM(CASE WHEN category = 'book' THEN 1 ELSE 0 END) AS book_count,
    SUM(CASE WHEN category = 'other' THEN 1 ELSE 0 END) AS other_count,
    SUM(CASE WHEN status = 'stock' THEN 1 ELSE 0 END) AS stock_count,
    SUM(CASE WHEN status = 'in_use' THEN 1 ELSE 0 END) AS in_use_count,
    SUM(CASE WHEN status = 'borrowed' THEN 1 ELSE 0 END) AS borrowed_count,
    SUM(CASE WHEN status = 'maintenance' THEN 1 ELSE 0 END) AS maintenance_count,
    SUM(CASE WHEN status = 'scrapped' THEN 1 ELSE 0 END) AS scrapped_count,
    SUM(purchase_price) AS total_purchase_value,
    SUM(current_value) AS total_current_value
  FROM biz_asset
  WHERE is_deleted = 0;
END$$

DELIMITER ;
```

### 5.3 超期借用检查

```sql
DELIMITER $$

CREATE PROCEDURE sp_check_overdue_borrows()
BEGIN
  -- 更新超期记录状态
  UPDATE biz_asset_borrow_record
  SET status = 'overdue'
  WHERE expected_return_date < CURDATE()
    AND status = 'active';

  -- 返回超期记录
  SELECT
    r.id,
    r.asset_id,
    r.asset_name,
    r.borrower_id,
    r.borrower_name,
    r.borrow_date,
    r.expected_return_date,
    DATEDIFF(CURDATE(), r.expected_return_date) AS overdue_days,
    e.phone AS borrower_phone,
    e.email AS borrower_email
  FROM biz_asset_borrow_record r
  LEFT JOIN sys_employee e ON r.borrower_id = e.id
  WHERE r.status = 'overdue'
  ORDER BY overdue_days DESC;
END$$

DELIMITER ;
```

---

## 6. 定时任务设计

### 6.1 自动计算折旧

```sql
-- 创建定时事件(每月1号凌晨执行)
SET GLOBAL event_scheduler = ON;

CREATE EVENT evt_calculate_asset_value
ON SCHEDULE EVERY 1 MONTH
STARTS (TIMESTAMP(DATE_FORMAT(CURDATE(), '%Y-%m-01')) + INTERVAL 1 MONTH + INTERVAL 1 HOUR)
DO CALL sp_calculate_asset_value();
```

### 6.2 超期检查提醒

```sql
-- 创建定时事件(每天早上9点执行)
CREATE EVENT evt_check_overdue_borrows
ON SCHEDULE EVERY 1 DAY
STARTS (TIMESTAMP(CURRENT_DATE) + INTERVAL 9 HOUR)
DO CALL sp_check_overdue_borrows();
```

---

## 7. 视图设计

### 7.1 资产详情视图

```sql
CREATE VIEW v_asset_detail AS
SELECT
  a.*,
  e.name AS user_name,
  e.department_id,
  d.name AS department_name,
  TIMESTAMPDIFF(YEAR, a.purchase_date, CURDATE()) AS owned_years
FROM biz_asset a
LEFT JOIN sys_employee e ON a.user_id = e.id
LEFT JOIN sys_department d ON e.department_id = d.id
WHERE a.is_deleted = 0;
```

### 7.2 借还记录视图

```sql
CREATE VIEW v_asset_borrow_record AS
SELECT
  r.*,
  e.department_id,
  d.name AS department_name,
  e.phone AS borrower_phone,
  e.email AS borrower_email,
  DATEDIFF(IFNULL(r.actual_return_date, CURDATE()), r.borrow_date) AS borrowed_days,
  CASE
    WHEN r.actual_return_date IS NOT NULL AND r.actual_return_date > r.expected_return_date THEN DATEDIFF(r.actual_return_date, r.expected_return_date)
    WHEN r.actual_return_date IS NULL AND CURDATE() > r.expected_return_date THEN DATEDIFF(CURDATE(), r.expected_return_date)
    ELSE 0
  END AS overdue_days
FROM biz_asset_borrow_record r
LEFT JOIN sys_employee e ON r.borrower_id = e.id
LEFT JOIN sys_department d ON e.department_id = d.id;
```

---

## 8. 数据字典映射

### 8.1 资产类别字典 (asset_category)

```sql
INSERT INTO sys_dict_type (code, name, category, status) VALUES
('asset_category', '资产类别', 'business', 'enabled');

INSERT INTO sys_dict_item (type_code, label, value, sort, status) VALUES
('asset_category', '电子设备', 'electronic', 1, 'enabled'),
('asset_category', '家具', 'furniture', 2, 'enabled'),
('asset_category', '图书', 'book', 3, 'enabled'),
('asset_category', '其他', 'other', 4, 'enabled');
```

### 8.2 资产状态字典 (asset_status)

```sql
INSERT INTO sys_dict_type (code, name, category, status) VALUES
('asset_status', '资产状态', 'business', 'enabled');

INSERT INTO sys_dict_item (type_code, label, value, color, sort, status) VALUES
('asset_status', '在库', 'stock', '#67C23A', 1, 'enabled'),
('asset_status', '使用中', 'in_use', '#409EFF', 2, 'enabled'),
('asset_status', '借出', 'borrowed', '#E6A23C', 3, 'enabled'),
('asset_status', '维护中', 'maintenance', '#F56C6C', 4, 'enabled'),
('asset_status', '报废', 'scrapped', '#909399', 5, 'enabled');
```

---

## 9. 初始化数据

```sql
-- 插入示例资产数据
INSERT INTO biz_asset (
  id, name, category, brand_model, purchase_date, purchase_price, current_value, status, location
) VALUES
('ASSET000001', 'MacBook Pro 16寸', 'electronic', 'Apple M3 Max', '2024-01-15', 24999.00, 21249.15, 'stock', 'A座3楼办公室'),
('ASSET000002', 'Dell XPS 15', 'electronic', 'Dell XPS 9530', '2024-02-20', 15999.00, 13599.15, 'in_use', 'A座3楼办公室'),
('ASSET000003', '人体工学椅', 'furniture', 'Herman Miller', '2023-06-10', 8999.00, 5759.36, 'in_use', 'A座3楼办公室'),
('ASSET000004', 'ThinkPad X1 Carbon', 'electronic', 'Lenovo', '2024-03-05', 12999.00, 11049.15, 'borrowed', 'A座3楼办公室'),
('ASSET000005', 'JavaScript高级程序设计', 'book', '第4版', '2023-08-15', 99.00, 99.00, 'stock', 'A座2楼图书角');
```

---

## 10. API对应SQL查询

### 10.1 获取资产列表

```sql
-- 对应前端: GET /api/assets
SELECT
  a.*,
  e.name AS user_name,
  d.name AS department_name
FROM biz_asset a
LEFT JOIN sys_employee e ON a.user_id = e.id
LEFT JOIN sys_department d ON e.department_id = d.id
WHERE a.is_deleted = 0
  AND (? IS NULL OR a.category = ?)
  AND (? IS NULL OR a.status = ?)
  AND (? IS NULL OR a.user_id = ?)
  AND (? IS NULL OR a.purchase_date >= ?)
  AND (? IS NULL OR a.purchase_date <= ?)
  AND (? IS NULL OR a.purchase_price >= ?)
  AND (? IS NULL OR a.purchase_price <= ?)
  AND (? IS NULL OR
       a.name LIKE CONCAT('%', ?, '%')
       OR a.id LIKE CONCAT('%', ?, '%')
       OR a.brand_model LIKE CONCAT('%', ?, '%')
      )
ORDER BY a.created_at DESC
LIMIT ? OFFSET ?;
```

### 10.2 获取借还记录

```sql
-- 对应前端: GET /api/assets/borrow-records
SELECT
  r.*,
  e.phone AS borrower_phone,
  e.email AS borrower_email
FROM biz_asset_borrow_record r
LEFT JOIN sys_employee e ON r.borrower_id = e.id
WHERE (? IS NULL OR r.borrower_id = ?)
  AND (? IS NULL OR r.status = ?)
ORDER BY r.borrow_date DESC
LIMIT ? OFFSET ?;
```

---

**文档版本**: v1.0.0
**创建人**: AI开发助手
**最后更新**: 2026-01-11
