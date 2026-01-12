# 数据字典数据库设计

> **对应前端规范**: [dict_Technical.md](../../../oa-system-frontend-specs/core/dict/dict_Technical.md)
> **数据库**: MySQL 8.0+
> **表前缀**: `sys_`
> **版本**: v1.0.0

---

## 数据库表设计

### 1. 字典类型表 (sys_dict_type)

#### 1.1 表结构

```sql
CREATE TABLE sys_dict_type (
  -- 主键
  id VARCHAR(50) PRIMARY KEY COMMENT '字典类型ID',

  -- 基本信息
  code VARCHAR(100) NOT NULL UNIQUE COMMENT '字典编码(唯一,格式: module_entity_property)',
  name VARCHAR(100) NOT NULL COMMENT '字典名称',
  description VARCHAR(500) COMMENT '字典描述',

  -- 分类信息
  category ENUM('system', 'business') NOT NULL DEFAULT 'business' COMMENT '字典类别: system系统字典 business业务字典',

  -- 统计信息
  item_count INT NOT NULL DEFAULT 0 COMMENT '字典项数量',

  -- 状态控制
  status ENUM('enabled', 'disabled') NOT NULL DEFAULT 'enabled' COMMENT '状态: enabled启用 disabled禁用',
  sort_order INT NOT NULL DEFAULT 0 COMMENT '排序序号',

  -- 扩展信息
  ext_props JSON COMMENT '扩展属性(JSON格式)',
  remark VARCHAR(500) COMMENT '备注',

  -- 审计字段
  created_by VARCHAR(20) COMMENT '创建人ID',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_by VARCHAR(20) COMMENT '更新人ID',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除(0否1是)',
  deleted_at DATETIME COMMENT '删除时间',
  deleted_by VARCHAR(20) COMMENT '删除人ID'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='字典类型表';
```

#### 1.2 索引设计

```sql
-- 主键索引
-- PRIMARY KEY (id)

-- 唯一索引
-- UNIQUE KEY (code)

-- 普通索引
CREATE INDEX idx_dict_type_category ON sys_dict_type(category) COMMENT '字典类别索引';
CREATE INDEX idx_dict_type_status ON sys_dict_type(status) COMMENT '状态索引';
CREATE INDEX idx_dict_type_sort ON sys_dict_type(sort_order) COMMENT '排序索引';
CREATE INDEX idx_dict_type_deleted_at ON sys_dict_type(deleted_at) COMMENT '删除时间索引';

-- 组合索引
CREATE INDEX idx_dict_type_category_status ON sys_dict_type(category, status) COMMENT '类别+状态组合索引';
```

---

### 2. 字典项表 (sys_dict_item)

#### 2.1 表结构

```sql
CREATE TABLE sys_dict_item (
  -- 主键
  id VARCHAR(50) PRIMARY KEY COMMENT '字典项ID',

  -- 关联信息
  dict_type_id VARCHAR(50) NOT NULL COMMENT '所属字典类型ID',
  dict_type_code VARCHAR(100) NOT NULL COMMENT '字典类型编码(冗余字段,方便查询)',

  -- 字典项信息
  label VARCHAR(200) NOT NULL COMMENT '项标签(显示文本)',
  value VARCHAR(200) NOT NULL COMMENT '项值(实际值)',

  -- 显示信息
  color_type ENUM('primary', 'success', 'warning', 'danger', 'info') COMMENT '颜色类型',
  color VARCHAR(50) COMMENT '自定义颜色(如:#409EFF)',
  icon VARCHAR(100) COMMENT '图标',

  -- 排序和状态
  sort_order INT NOT NULL DEFAULT 0 COMMENT '排序序号',
  status ENUM('enabled', 'disabled') NOT NULL DEFAULT 'enabled' COMMENT '状态: enabled启用 disabled禁用',

  -- 扩展信息
  ext_props JSON COMMENT '扩展属性(JSON格式)',
  remark VARCHAR(500) COMMENT '备注',

  -- 审计字段
  created_by VARCHAR(20) COMMENT '创建人ID',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_by VARCHAR(20) COMMENT '更新人ID',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除(0否1是)',
  deleted_at DATETIME COMMENT '删除时间',
  deleted_by VARCHAR(20) COMMENT '删除人ID',

  -- 唯一约束
  UNIQUE KEY uk_dict_item (dict_type_id, value)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='字典项表';
```

#### 2.2 索引设计

```sql
-- 主键索引
-- PRIMARY KEY (id)

-- 唯一索引
-- UNIQUE KEY uk_dict_item (dict_type_id, value)

-- 普通索引
CREATE INDEX idx_dict_item_type_id ON sys_dict_item(dict_type_id) COMMENT '字典类型ID索引';
CREATE INDEX idx_dict_item_type_code ON sys_dict_item(dict_type_code) COMMENT '字典类型编码索引';
CREATE INDEX idx_dict_item_status ON sys_dict_item(status) COMMENT '状态索引';
CREATE INDEX idx_dict_item_sort ON sys_dict_item(sort_order) COMMENT '排序索引';
CREATE INDEX idx_dict_item_deleted_at ON sys_dict_item(deleted_at) COMMENT '删除时间索引';

-- 组合索引
CREATE INDEX idx_dict_item_type_status ON sys_dict_item(dict_type_id, status) COMMENT '类型+状态组合索引';
CREATE INDEX idx_dict_item_type_sort ON sys_dict_item(dict_type_id, sort_order) COMMENT '类型+排序组合索引';
```

#### 2.3 外键约束

```sql
-- 字典类型外键
ALTER TABLE sys_dict_item
  ADD CONSTRAINT fk_dict_item_type
  FOREIGN KEY (dict_type_id)
  REFERENCES sys_dict_type(id)
  ON DELETE CASCADE
  ON UPDATE CASCADE;
```

---

## 3. 触发器设计

### 3.1 字典项数量更新触发器

```sql
DELIMITER $$

-- 插入字典项时更新字典项数量
CREATE TRIGGER trg_dict_item_after_insert
AFTER INSERT ON sys_dict_item
FOR EACH ROW
BEGIN
  UPDATE sys_dict_type
  SET item_count = item_count + 1
  WHERE id = NEW.dict_type_id;
END$$

-- 删除字典项时更新字典项数量
CREATE TRIGGER trg_dict_item_after_delete
AFTER DELETE ON sys_dict_item
FOR EACH ROW
BEGIN
  UPDATE sys_dict_type
  SET item_count = item_count - 1
  WHERE id = OLD.dict_type_id;
END$$

-- 软删除字典项时更新字典项数量
CREATE TRIGGER trg_dict_item_after_update
AFTER UPDATE ON sys_dict_item
FOR EACH ROW
BEGIN
  -- 状态从删除变为正常
  IF NEW.is_deleted = 0 AND OLD.is_deleted = 1 THEN
    UPDATE sys_dict_type
    SET item_count = item_count + 1
    WHERE id = NEW.dict_type_id;
  END IF;

  -- 状态从正常变为删除
  IF NEW.is_deleted = 1 AND OLD.is_deleted = 0 THEN
    UPDATE sys_dict_type
    SET item_count = item_count - 1
    WHERE id = NEW.dict_type_id;
  END IF;
END$$

DELIMITER ;
```

---

## 4. 存储过程设计

### 4.1 获取字典数据

```sql
DELIMITER $$

CREATE PROCEDURE sp_get_dict_data(IN p_dict_type_code VARCHAR(100))
BEGIN
  SELECT
    id,
    dict_type_id,
    dict_type_code,
    label,
    value,
    color_type,
    color,
    icon,
    sort_order,
    status,
    ext_props
  FROM sys_dict_item
  WHERE dict_type_code = p_dict_type_code
    AND is_deleted = 0
    AND status = 'enabled'
  ORDER BY sort_order, id;
END$$

DELIMITER ;
```

### 4.2 获取字典树

```sql
DELIMITER $$

CREATE PROCEDURE sp_get_dict_tree(IN p_category VARCHAR(20), IN p_status VARCHAR(20))
BEGIN
  SELECT
    t.id,
    t.code,
    t.name,
    t.description,
    t.category,
    t.item_count,
    t.status,
    t.sort_order,
    t.remark,
    t.created_at,
    t.updated_at,
    -- 字典项列表
    (SELECT JSON_ARRAYAGG(
      JSON_OBJECT(
        'id', i.id,
        'label', i.label,
        'value', i.value,
        'colorType', i.color_type,
        'color', i.color,
        'icon', i.icon,
        'sortOrder', i.sort_order,
        'status', i.status,
        'extProps', i.ext_props
      )
    ) FROM sys_dict_item i
    WHERE i.dict_type_id = t.id
      AND i.is_deleted = 0
      AND i.status = 'enabled'
    ORDER BY i.sort_order, i.id
  ) AS items
  FROM sys_dict_type t
  WHERE t.is_deleted = 0
    AND (p_category IS NULL OR t.category = p_category)
    AND (p_status IS NULL OR t.status = p_status)
  ORDER BY t.sort_order, t.id;
END$$

DELIMITER ;
```

### 4.3 批量更新字典项排序

```sql
DELIMITER $$

CREATE PROCEDURE sp_update_item_sort(
  IN p_dict_type_id VARCHAR(50),
  IN p_item_ids TEXT,
  IN p_sort_orders TEXT
)
BEGIN
  DECLARE v_index INT DEFAULT 0;
  DECLARE v_item_id VARCHAR(50);
  DECLARE v_sort_order INT;

  -- 解析JSON数组
  WHILE v_index < JSON_LENGTH(p_item_ids) DO
    SET v_item_id = JSON_UNQUOTE(JSON_EXTRACT(p_item_ids, CONCAT('$[', v_index, ']')));
    SET v_sort_order = JSON_EXTRACT(p_sort_orders, CONCAT('$[', v_index, ']'));

    UPDATE sys_dict_item
    SET sort_order = v_sort_order,
        updated_at = CURRENT_TIMESTAMP
    WHERE id = v_item_id
      AND dict_type_id = p_dict_type_id;

    SET v_index = v_index + 1;
  END WHILE;

  SELECT ROW_COUNT() AS updated_count;
END$$

DELIMITER ;
```

### 4.4 清理未使用的字典

```sql
DELIMITER $$

CREATE PROCEDURE sp_clean_unused_dict()
BEGIN
  -- 创建临时表存储使用的字典类型
  CREATE TEMPORARY TABLE tmp_used_dict (
    dict_type_code VARCHAR(100) PRIMARY KEY
  );

  -- 这里需要根据实际业务逻辑添加使用的字典类型
  -- 示例: 从员工表插入使用的字典
  INSERT INTO tmp_used_dict (dict_type_code)
  SELECT DISTINCT 'gender' FROM sys_employee WHERE is_deleted = 0
  UNION
  SELECT DISTINCT 'employee_status' FROM sys_employee WHERE is_deleted = 0
  UNION
  SELECT DISTINCT 'probation_status' FROM sys_employee WHERE is_deleted = 0;

  -- 输出未使用的字典类型
  SELECT
    t.id,
    t.code,
    t.name,
    t.item_count,
    t.updated_at
  FROM sys_dict_type t
  WHERE t.is_deleted = 0
    AND t.category = 'business'
    AND t.code NOT IN (SELECT dict_type_code FROM tmp_used_dict)
    AND t.updated_at < DATE_SUB(NOW(), INTERVAL 90 DAY);

  -- 清理临时表
  DROP TEMPORARY TABLE tmp_used_dict;
END$$

DELIMITER ;
```

---

## 5. 视图设计

### 5.1 字典完整视图

```sql
CREATE VIEW v_dict_full AS
SELECT
  t.id AS type_id,
  t.code AS type_code,
  t.name AS type_name,
  t.description AS type_description,
  t.category,
  t.item_count,
  t.status AS type_status,
  t.sort_order AS type_sort,
  t.remark AS type_remark,
  i.id AS item_id,
  i.label,
  i.value,
  i.color_type,
  i.color,
  i.icon,
  i.sort_order AS item_sort,
  i.status AS item_status,
  i.remark AS item_remark,
  i.ext_props AS item_ext_props
FROM sys_dict_type t
LEFT JOIN sys_dict_item i ON t.id = i.dict_type_id AND i.is_deleted = 0
WHERE t.is_deleted = 0;
```

### 5.2 字典统计视图

```sql
CREATE VIEW v_dict_statistics AS
SELECT
  t.id,
  t.code,
  t.name,
  t.category,
  COUNT(i.id) AS total_items,
  SUM(CASE WHEN i.status = 'enabled' THEN 1 ELSE 0 END) AS enabled_items,
  SUM(CASE WHEN i.status = 'disabled' THEN 1 ELSE 0 END) AS disabled_items,
  MAX(i.updated_at) AS last_item_update
FROM sys_dict_type t
LEFT JOIN sys_dict_item i ON t.id = i.dict_type_id AND i.is_deleted = 0
WHERE t.is_deleted = 0
GROUP BY t.id, t.code, t.name, t.category;
```

---

## 6. 数据完整性保障

### 6.1 检查约束

```sql
-- 字典类别检查
ALTER TABLE sys_dict_type
  ADD CONSTRAINT chk_dict_category
  CHECK (category IN ('system', 'business'));

-- 字典状态检查
ALTER TABLE sys_dict_type
  ADD CONSTRAINT chk_dict_type_status
  CHECK (status IN ('enabled', 'disabled'));

-- 字典项状态检查
ALTER TABLE sys_dict_item
  ADD CONSTRAINT chk_dict_item_status
  CHECK (status IN ('enabled', 'disabled'));

-- 颜色类型检查
ALTER TABLE sys_dict_item
  ADD CONSTRAINT chk_color_type
  CHECK (color_type IN ('primary', 'success', 'warning', 'danger', 'info'));

-- 字典项数量非负
ALTER TABLE sys_dict_type
  ADD CONSTRAINT chk_item_count
  CHECK (item_count >= 0);

-- 排序序号非负
ALTER TABLE sys_dict_type
  ADD CONSTRAINT chk_type_sort_order
  CHECK (sort_order >= 0);

ALTER TABLE sys_dict_item
  ADD CONSTRAINT chk_item_sort_order
  CHECK (sort_order >= 0);
```

---

## 7. 初始化数据

### 7.1 系统字典类型初始化

```sql
-- 性别字典
INSERT INTO sys_dict_type (id, code, name, description, category, status, sort_order) VALUES
('DICT_001', 'gender', '性别', '用户性别', 'system', 'enabled', 1);

-- 菜单类型字典
INSERT INTO sys_dict_type (id, code, name, description, category, status, sort_order) VALUES
('DICT_002', 'menu_type', '菜单类型', '菜单类型(目录/菜单/按钮)', 'system', 'enabled', 2);

-- 菜单状态字典
INSERT INTO sys_dict_type (id, code, name, description, category, status, sort_order) VALUES
('DICT_003', 'menu_status', '菜单状态', '菜单状态(启用/禁用)', 'system', 'enabled', 3);

-- 链接打开方式字典
INSERT INTO sys_dict_type (id, code, name, description, category, status, sort_order) VALUES
('DICT_004', 'menu_target', '链接打开方式', '链接打开方式', 'system', 'enabled', 4);

-- 通用状态字典
INSERT INTO sys_dict_type (id, code, name, description, category, status, sort_order) VALUES
('DICT_005', 'common_status', '通用状态', '通用状态(启用/禁用)', 'system', 'enabled', 5);

-- 是否字典
INSERT INTO sys_dict_type (id, code, name, description, category, status, sort_order) VALUES
('DICT_006', 'yes_no', '是否', '是否选项', 'system', 'enabled', 6);
```

### 7.2 系统字典项初始化

```sql
-- 性别字典项
INSERT INTO sys_dict_item (id, dict_type_id, dict_type_code, label, value, color_type, sort_order) VALUES
('ITEM_0011', 'DICT_001', 'gender', '男', 'male', 'primary', 1),
('ITEM_0012', 'DICT_001', 'gender', '女', 'female', 'danger', 2);

-- 菜单类型字典项
INSERT INTO sys_dict_item (id, dict_type_id, dict_type_code, label, value, color_type, sort_order) VALUES
('ITEM_0021', 'DICT_002', 'menu_type', '目录', 'directory', '#409EFF', 1),
('ITEM_0022', 'DICT_002', 'menu_type', '菜单', 'menu', '#67C23A', 2),
('ITEM_0023', 'DICT_002', 'menu_type', '按钮', 'button', '#909399', 3);

-- 菜单状态字典项
INSERT INTO sys_dict_item (id, dict_type_id, dict_type_code, label, value, color_type, sort_order) VALUES
('ITEM_0031', 'DICT_003', 'menu_status', '启用', '1', '#67C23A', 1),
('ITEM_0032', 'DICT_003', 'menu_status', '禁用', '0', '#909399', 2);

-- 链接打开方式字典项
INSERT INTO sys_dict_item (id, dict_type_id, dict_type_code, label, value, sort_order) VALUES
('ITEM_0041', 'DICT_004', 'menu_target', '当前页', '_self', 1),
('ITEM_0042', 'DICT_004', 'menu_target', '新页', '_blank', 2);

-- 通用状态字典项
INSERT INTO sys_dict_item (id, dict_type_id, dict_type_code, label, value, color_type, sort_order) VALUES
('ITEM_0051', 'DICT_005', 'common_status', '启用', '1', 'success', 1),
('ITEM_0052', 'DICT_005', 'common_status', '禁用', '0', 'info', 2);

-- 是否字典项
INSERT INTO sys_dict_item (id, dict_type_id, dict_type_code, label, value, color_type, sort_order) VALUES
('ITEM_0061', 'DICT_006', 'yes_no', '是', '1', 'success', 1),
('ITEM_0062', 'DICT_006', 'yes_no', '否', '0', 'info', 2);
```

### 7.3 业务字典类型初始化

```sql
-- 员工状态字典
INSERT INTO sys_dict_type (id, code, name, description, category, status, sort_order) VALUES
('DICT_101', 'employee_status', '员工状态', '员工在职状态', 'business', 'enabled', 101);

-- 试用期状态字典
INSERT INTO sys_dict_type (id, code, name, description, category, status, sort_order) VALUES
('DICT_102', 'probation_status', '试用期状态', '员工试用期状态', 'business', 'enabled', 102);

-- 部门状态字典
INSERT INTO sys_dict_type (id, code, name, description, category, status, sort_order) VALUES
('DICT_103', 'department_status', '部门状态', '部门状态', 'business', 'enabled', 103);

-- 资产类别字典
INSERT INTO sys_dict_type (id, code, name, description, category, status, sort_order) VALUES
('DICT_104', 'asset_category', '资产类别', '固定资产类别', 'business', 'enabled', 104);

-- 资产状态字典
INSERT INTO sys_dict_type (id, code, name, description, category, status, sort_order) VALUES
('DICT_105', 'asset_status', '资产状态', '资产使用状态', 'business', 'enabled', 105);

-- 请假类型字典
INSERT INTO sys_dict_type (id, code, name, description, category, status, sort_order) VALUES
('DICT_106', 'leave_type', '请假类型', '员工请假类型', 'business', 'enabled', 106);

-- 请假状态字典
INSERT INTO sys_dict_type (id, code, name, description, category, status, sort_order) VALUES
('DICT_107', 'leave_status', '请假状态', '请假审批状态', 'business', 'enabled', 107);

-- 报销类型字典
INSERT INTO sys_dict_type (id, code, name, description, category, status, sort_order) VALUES
('DICT_108', 'expense_type', '报销类型', '费用报销类型', 'business', 'enabled', 108);

-- 报销状态字典
INSERT INTO sys_dict_type (id, code, name, description, category, status, sort_order) VALUES
('DICT_109', 'expense_status', '报销状态', '报销审批状态', 'business', 'enabled', 109);

-- 审批状态字典
INSERT INTO sys_dict_type (id, code, name, description, category, status, sort_order) VALUES
('DICT_110', 'approval_status', '审批状态', '通用审批状态', 'business', 'enabled', 110);
```

### 7.4 业务字典项初始化

```sql
-- 员工状态字典项
INSERT INTO sys_dict_item (id, dict_type_id, dict_type_code, label, value, color_type, sort_order) VALUES
('ITEM_1011', 'DICT_101', 'employee_status', '在职', 'active', '#67C23A', 1),
('ITEM_1012', 'DICT_101', 'employee_status', '离职', 'resigned', '#909399', 2),
('ITEM_1013', 'DICT_101', 'employee_status', '停薪留职', 'suspended', '#E6A23C', 3);

-- 试用期状态字典项
INSERT INTO sys_dict_item (id, dict_type_id, dict_type_code, label, value, color_type, sort_order) VALUES
('ITEM_1021', 'DICT_102', 'probation_status', '试用期内', 'probation', '#409EFF', 1),
('ITEM_1022', 'DICT_102', 'probation_status', '已转正', 'regular', '#67C23A', 2),
('ITEM_1023', 'DICT_102', 'probation_status', '已离职', 'resigned', '#909399', 3);

-- 部门状态字典项
INSERT INTO sys_dict_item (id, dict_type_id, dict_type_code, label, value, color_type, sort_order) VALUES
('ITEM_1031', 'DICT_103', 'department_status', '正常', 'active', '#67C23A', 1),
('ITEM_1032', 'DICT_103', 'department_status', '禁用', 'disabled', '#909399', 2);

-- 资产类别字典项
INSERT INTO sys_dict_item (id, dict_type_id, dict_type_code, label, value, sort_order) VALUES
('ITEM_1041', 'DICT_104', 'asset_category', '办公设备', 'office_equipment', 1),
('ITEM_1042', 'DICT_104', 'asset_category', '电子设备', 'electronic_equipment', 2),
('ITEM_1043', 'DICT_104', 'asset_category', '交通工具', 'vehicle', 3),
('ITEM_1044', 'DICT_104', 'asset_category', '办公家具', 'office_furniture', 4),
('ITEM_1045', 'DICT_104', 'asset_category', '其他', 'other', 5);

-- 资产状态字典项
INSERT INTO sys_dict_item (id, dict_type_id, dict_type_code, label, value, color_type, sort_order) VALUES
('ITEM_1051', 'DICT_105', 'asset_status', '在用', 'in_use', '#67C23A', 1),
('ITEM_1052', 'DICT_105', 'asset_status', '闲置', 'idle', '#E6A23C', 2),
('ITEM_1053', 'DICT_105', 'asset_status', '维修中', 'repair', '#F56C6C', 3),
('ITEM_1054', 'DICT_105', 'asset_status', '报废', 'scrapped', '#909399', 4);

-- 请假类型字典项
INSERT INTO sys_dict_item (id, dict_type_id, dict_type_code, label, value, sort_order) VALUES
('ITEM_1061', 'DICT_106', 'leave_type', '事假', 'personal', 1),
('ITEM_1062', 'DICT_106', 'leave_type', '病假', 'sick', 2),
('ITEM_1063', 'DICT_106', 'leave_type', '年假', 'annual', 3),
('ITEM_1064', 'DICT_106', 'leave_type', '调休', 'compensatory', 4),
('ITEM_1065', 'DICT_106', 'leave_type', '其他', 'other', 5);

-- 请假状态字典项
INSERT INTO sys_dict_item (id, dict_type_id, dict_type_code, label, value, color_type, sort_order) VALUES
('ITEM_1071', 'DICT_107', 'leave_status', '待审批', 'pending', '#409EFF', 1),
('ITEM_1072', 'DICT_107', 'leave_status', '已批准', 'approved', '#67C23A', 2),
('ITEM_1073', 'DICT_107', 'leave_status', '已拒绝', 'rejected', '#F56C6C', 3),
('ITEM_1074', 'DICT_107', 'leave_status', '已撤销', 'cancelled', '#909399', 4);

-- 报销类型字典项
INSERT INTO sys_dict_item (id, dict_type_id, dict_type_code, label, value, sort_order) VALUES
('ITEM_1081', 'DICT_108', 'expense_type', '交通费', 'transportation', 1),
('ITEM_1082', 'DICT_108', 'expense_type', '餐饮费', 'meal', 2),
('ITEM_1083', 'DICT_108', 'expense_type', '住宿费', 'accommodation', 3),
('ITEM_1084', 'DICT_108', 'expense_type', '办公用品', 'office_supplies', 4),
('ITEM_1085', 'DICT_108', 'expense_type', '其他', 'other', 5);

-- 报销状态字典项
INSERT INTO sys_dict_item (id, dict_type_id, dict_type_code, label, value, color_type, sort_order) VALUES
('ITEM_1091', 'DICT_109', 'expense_status', '待审批', 'pending', '#409EFF', 1),
('ITEM_1092', 'DICT_109', 'expense_status', '已批准', 'approved', '#67C23A', 2),
('ITEM_1093', 'DICT_109', 'expense_status', '已拒绝', 'rejected', '#F56C6C', 3),
('ITEM_1094', 'DICT_109', 'expense_status', '已支付', 'paid', '#67C23A', 4);

-- 审批状态字典项
INSERT INTO sys_dict_item (id, dict_type_id, dict_type_code, label, value, color_type, sort_order) VALUES
('ITEM_1101', 'DICT_110', 'approval_status', '待审批', 'pending', '#409EFF', 1),
('ITEM_1102', 'DICT_110', 'approval_status', '审批中', 'processing', '#E6A23C', 2),
('ITEM_1103', 'DICT_110', 'approval_status', '已批准', 'approved', '#67C23A', 3),
('ITEM_1104', 'DICT_110', 'approval_status', '已拒绝', 'rejected', '#F56C6C', 4),
('ITEM_1105', 'DICT_110', 'approval_status', '已撤销', 'cancelled', '#909399', 5);
```

---

## 8. API对应SQL查询

### 8.1 获取字典类型列表

```sql
-- 对应前端: GET /api/dict/types
SELECT
  id,
  code,
  name,
  description,
  category,
  item_count,
  status,
  sort_order,
  remark,
  created_at,
  updated_at
FROM sys_dict_type
WHERE is_deleted = 0
  AND (? IS NULL OR category = ?)
  AND (? IS NULL OR status = ?)
  AND (? IS NULL OR code LIKE CONCAT('%', ?, '%') OR name LIKE CONCAT('%', ?, '%'))
ORDER BY sort_order, id;
```

### 8.2 获取字典类型详情

```sql
-- 对应前端: GET /api/dict/types/:id
SELECT
  id,
  code,
  name,
  description,
  category,
  item_count,
  status,
  sort_order,
  ext_props,
  remark,
  created_by,
  created_at,
  updated_by,
  updated_at
FROM sys_dict_type
WHERE id = ? AND is_deleted = 0;
```

### 8.3 获取字典项列表

```sql
-- 对应前端: GET /api/dict/items
SELECT
  id,
  dict_type_id,
  dict_type_code,
  label,
  value,
  color_type,
  color,
  icon,
  sort_order,
  status,
  ext_props,
  remark,
  created_at,
  updated_at
FROM sys_dict_item
WHERE is_deleted = 0
  AND (? IS NULL OR dict_type_id = ?)
  AND (? IS NULL OR dict_type_code = ?)
  AND (? IS NULL OR status = ?)
  AND (? IS NULL OR label LIKE CONCAT('%', ?, '%') OR value LIKE CONCAT('%', ?, '%'))
ORDER BY sort_order, id;
```

### 8.4 获取字典数据(缓存)

```sql
-- 对应前端: GET /api/dict/:code
SELECT
  id,
  dict_type_id,
  label,
  value,
  color_type,
  color,
  icon,
  sort_order,
  ext_props
FROM sys_dict_item
WHERE dict_type_code = ?
  AND is_deleted = 0
  AND status = 'enabled'
ORDER BY sort_order, id;
```

### 8.5 获取字典树

```sql
-- 对应前端: GET /api/dict/tree
SELECT
  t.id,
  t.code,
  t.name,
  t.description,
  t.category,
  t.item_count,
  t.status,
  t.sort_order,
  t.ext_props,
  t.remark,
  t.created_at,
  t.updated_at
FROM sys_dict_type t
WHERE t.is_deleted = 0
  AND (? IS NULL OR t.category = ?)
  AND (? IS NULL OR t.status = ?)
ORDER BY t.sort_order, t.id;
```

### 8.6 创建字典类型

```sql
-- 对应前端: POST /api/dict/types
INSERT INTO sys_dict_type (
  id,
  code,
  name,
  description,
  category,
  status,
  sort_order,
  ext_props,
  remark,
  created_by
) VALUES (
  ?, ?, ?, ?, ?, ?, ?, ?, ?, ?
);
```

### 8.7 更新字典类型

```sql
-- 对应前端: PUT /api/dict/types/:id
UPDATE sys_dict_type
SET
  name = ?,
  description = ?,
  category = ?,
  status = ?,
  sort_order = ?,
  ext_props = ?,
  remark = ?,
  updated_by = ?,
  updated_at = CURRENT_TIMESTAMP
WHERE id = ? AND is_deleted = 0;
```

### 8.8 删除字典类型

```sql
-- 对应前端: DELETE /api/dict/types/:id
-- 软删除
UPDATE sys_dict_type
SET is_deleted = 1,
    deleted_at = CURRENT_TIMESTAMP,
    deleted_by = ?
WHERE id = ?;

-- 级联删除字典项(软删除)
UPDATE sys_dict_item
SET is_deleted = 1,
    deleted_at = CURRENT_TIMESTAMP,
    deleted_by = ?
WHERE dict_type_id = ?;
```

### 8.9 创建字典项

```sql
-- 对应前端: POST /api/dict/items
INSERT INTO sys_dict_item (
  id,
  dict_type_id,
  dict_type_code,
  label,
  value,
  color_type,
  color,
  icon,
  sort_order,
  status,
  ext_props,
  remark,
  created_by
) VALUES (
  ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?
);
```

### 8.10 更新字典项

```sql
-- 对应前端: PUT /api/dict/items/:id
UPDATE sys_dict_item
SET
  label = ?,
  value = ?,
  color_type = ?,
  color = ?,
  icon = ?,
  sort_order = ?,
  status = ?,
  ext_props = ?,
  remark = ?,
  updated_by = ?,
  updated_at = CURRENT_TIMESTAMP
WHERE id = ? AND is_deleted = 0;
```

### 8.11 删除字典项

```sql
-- 对应前端: DELETE /api/dict/items/:id
UPDATE sys_dict_item
SET is_deleted = 1,
    deleted_at = CURRENT_TIMESTAMP,
    deleted_by = ?
WHERE id = ?;
```

### 8.12 批量更新排序

```sql
-- 对应前端: PUT /api/dict/items/sort
-- 使用存储过程
CALL sp_update_item_sort(?, ?, ?);
```

---

## 9. 性能优化建议

### 9.1 查询优化

```sql
-- 使用EXPLAIN分析查询
EXPLAIN SELECT * FROM sys_dict_item
WHERE dict_type_code = 'employee_status'
  AND status = 'enabled';

-- 优化建议:
-- 1. 确保索引 idx_dict_item_type_code 存在
-- 2. 避免SELECT *, 只查询需要的字段
-- 3. 使用覆盖索引
-- 4. 定期ANALYZE TABLE更新统计信息
```

### 9.2 缓存策略

```sql
-- 创建字典缓存表(可选)
CREATE TABLE sys_dict_cache (
  cache_key VARCHAR(100) PRIMARY KEY COMMENT '缓存键(格式: dict_{dict_type_code})',
  cache_data LONGTEXT COMMENT '缓存数据(JSON)',
  expire_at DATETIME NOT NULL COMMENT '过期时间',
  version INT NOT NULL DEFAULT 1 COMMENT '版本号',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_expire_at (expire_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字典缓存表';

-- 清理过期缓存
DELETE FROM sys_dict_cache WHERE expire_at < CURRENT_TIMESTAMP;

-- 更新缓存
REPLACE INTO sys_dict_cache (cache_key, cache_data, expire_at, version)
VALUES (
  ?,
  ?,
  DATE_ADD(NOW(), INTERVAL 30 MINUTE),
  1
);
```

### 9.3 分区策略(可选)

```sql
-- 字典项表按类型分区(适用于数据量大的情况)
ALTER TABLE sys_dict_item
PARTITION BY HASH (dict_type_id)
PARTITIONS 16;
```

---

## 10. 数据导入导出

### 10.1 导出字典数据

```sql
-- 导出所有字典数据为JSON
SELECT
  JSON_OBJECT(
    'dictType', t.code,
    'dictName', t.name,
    'items', (
      SELECT JSON_ARRAYAGG(
        JSON_OBJECT(
          'label', i.label,
          'value', i.value,
          'colorType', i.color_type,
          'sortOrder', i.sort_order
        )
      )
      FROM sys_dict_item i
      WHERE i.dict_type_id = t.id
        AND i.is_deleted = 0
        AND i.status = 'enabled'
      ORDER BY i.sort_order
    )
  ) AS dict_data
FROM sys_dict_type t
WHERE t.is_deleted = 0
  AND t.status = 'enabled'
ORDER BY t.sort_order;
```

### 10.2 导入字典数据

```sql
-- 从JSON导入字典数据
-- 需要应用层解析JSON后执行INSERT语句
```

---

**文档版本**: v1.0.0
**创建人**: AI开发助手
**最后更新**: 2026-01-12
