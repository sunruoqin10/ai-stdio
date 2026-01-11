# 部门管理数据库设计

> **对应前端规范**: [department_Technical.md](../../oa-system-frontend-specs/core/department/department_Technical.md)
> **数据库**: MySQL 8.0+
> **表前缀**: `sys_`
> **版本**: v1.0.0

---

## 数据库表设计

### 1. 部门表 (sys_department)

#### 1.1 表结构

```sql
CREATE TABLE sys_department (
  -- 主键
  id VARCHAR(20) PRIMARY KEY COMMENT '部门编号(格式: DEPT+4位序号)',

  -- 基本信息
  name VARCHAR(100) NOT NULL COMMENT '部门名称',
  short_name VARCHAR(50) COMMENT '部门简称',

  -- 层级关系
  parent_id VARCHAR(20) DEFAULT NULL COMMENT '上级部门ID(NULL表示顶级)',
  leader_id VARCHAR(20) NOT NULL COMMENT '部门负责人ID',
  level INT NOT NULL DEFAULT 1 COMMENT '部门层级(从1开始)',
  sort INT NOT NULL DEFAULT 0 COMMENT '排序号',

  -- 其他信息
  established_date DATE COMMENT '成立日期',
  description TEXT COMMENT '部门描述',
  icon VARCHAR(500) COMMENT '部门图标URL',

  -- 状态
  status ENUM('active', 'disabled') NOT NULL DEFAULT 'active' COMMENT '状态: active正常 disabled停用',

  -- 审计字段
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  created_by VARCHAR(20) COMMENT '创建人ID',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  updated_by VARCHAR(20) COMMENT '更新人ID',
  is_deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除(0否1是)',
  deleted_at DATETIME COMMENT '删除时间',
  deleted_by VARCHAR(20) COMMENT '删除人ID',
  version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='部门信息表';
```

#### 1.2 索引设计

```sql
-- 主键索引
-- PRIMARY KEY (id)

-- 唯一索引
CREATE UNIQUE INDEX uk_department_name ON sys_department(name, parent_id, is_deleted) COMMENT '部门名称唯一索引(同级内唯一)';

-- 普通索引
CREATE INDEX idx_department_parent ON sys_department(parent_id) COMMENT '上级部门ID索引';
CREATE INDEX idx_department_leader ON sys_department(leader_id) COMMENT '负责人ID索引';
CREATE INDEX idx_department_level ON sys_department(level) COMMENT '层级索引';
CREATE INDEX idx_department_status ON sys_department(status) COMMENT '状态索引';
CREATE INDEX idx_department_sort ON sys_department(parent_id, sort) COMMENT '排序组合索引';

-- 全文索引(用于搜索)
CREATE FULLTEXT INDEX ft_department_name ON sys_department(name, short_name) COMMENT '部门名称全文索引';
```

#### 1.3 外键约束

```sql
-- 上级部门外键(自关联)
ALTER TABLE sys_department
  ADD CONSTRAINT fk_department_parent
  FOREIGN KEY (parent_id)
  REFERENCES sys_department(id)
  ON DELETE RESTRICT
  ON UPDATE CASCADE;

-- 负责人外键
ALTER TABLE sys_department
  ADD CONSTRAINT fk_department_leader
  FOREIGN KEY (leader_id)
  REFERENCES sys_employee(id)
  ON DELETE RESTRICT
  ON UPDATE CASCADE;
```

---

### 2. 部门成员关系表 (sys_department_member)

> 用于快速查询部门成员,避免每次都关联查询员工表

#### 2.1 表结构

```sql
CREATE TABLE sys_department_member (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '关系ID',
  department_id VARCHAR(20) NOT NULL COMMENT '部门ID',
  employee_id VARCHAR(20) NOT NULL COMMENT '员工ID',
  is_leader TINYINT(1) DEFAULT 0 COMMENT '是否为负责人(0否1是)',
  join_date DATE NOT NULL COMMENT '加入部门日期',
  leave_date DATE COMMENT '离开部门日期',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='部门成员关系表';
```

#### 2.2 索引设计

```sql
-- 主键索引
-- PRIMARY KEY (id)

-- 唯一索引(防止重复关联)
CREATE UNIQUE INDEX uk_dept_member ON sys_department_member(department_id, employee_id, leave_date) COMMENT '部门成员唯一索引';

-- 普通索引
CREATE INDEX idx_dept_member_emp ON sys_department_member(employee_id) COMMENT '员工ID索引';
CREATE INDEX idx_dept_member_date ON sys_department_member(join_date, leave_date) COMMENT '日期索引';
```

---

## 3. 触发器设计

### 3.1 部门层级自动更新触发器

```sql
DELIMITER $$

-- 创建触发器: 新增部门时自动计算层级
CREATE TRIGGER trg_department_before_insert
BEFORE INSERT ON sys_department
FOR EACH ROW
BEGIN
  DECLARE parent_level INT;

  -- 如果有上级部门,计算层级
  IF NEW.parent_id IS NOT NULL THEN
    SELECT level INTO parent_level
    FROM sys_department
    WHERE id = NEW.parent_id AND is_deleted = 0;

    SET NEW.level = parent_level + 1;
  ELSE
    SET NEW.level = 1;
  END IF;
END$$

-- 创建触发器: 更新上级部门时自动计算层级
CREATE TRIGGER trg_department_before_update
BEFORE UPDATE ON sys_department
FOR EACH ROW
BEGIN
  DECLARE parent_level INT;

  -- 如果上级部门变更,重新计算层级
  IF NEW.parent_id <> OLD.parent_id THEN
    IF NEW.parent_id IS NOT NULL THEN
      SELECT level INTO parent_level
      FROM sys_department
      WHERE id = NEW.parent_id AND is_deleted = 0;

      SET NEW.level = parent_level + 1;
    ELSE
      SET NEW.level = 1;
    END IF;
  END IF;
END$$

DELIMITER ;
```

### 3.2 部门人数更新触发器

```sql
DELIMITER $$

-- 创建触发器: 员工创建时更新部门人数
CREATE TRIGGER trg_employee_after_insert
AFTER INSERT ON sys_employee
FOR EACH ROW
BEGIN
  INSERT INTO sys_department_member (department_id, employee_id, is_leader, join_date)
  VALUES (NEW.department_id, NEW.id, 0, NEW.join_date);
END$$

-- 创建触发器: 员工部门变更时更新部门人数
CREATE TRIGGER trg_employee_after_update
AFTER UPDATE ON sys_employee
FOR EACH ROW
BEGIN
  -- 如果部门变更
  IF NEW.department_id <> OLD.department_id THEN
    -- 更新旧关系的离开日期
    UPDATE sys_department_member
    SET leave_date = NEW.join_date
    WHERE employee_id = NEW.id AND leave_date IS NULL;

    -- 创建新关系
    INSERT INTO sys_department_member (department_id, employee_id, is_leader, join_date)
    VALUES (NEW.department_id, NEW.id, 0, NEW.join_date);
  END IF;
END$$

DELIMITER ;
```

---

## 4. 存储过程设计

### 4.1 获取部门树形结构

```sql
DELIMITER $$

CREATE PROCEDURE sp_department_tree(
  IN p_status VARCHAR(20)
)
BEGIN
  -- 递归查询部门树
  WITH RECURSIVE dept_tree AS (
    -- 根节点
    SELECT
      id,
      name,
      short_name,
      parent_id,
      leader_id,
      level,
      sort,
      status,
      CAST(name AS CHAR(1000)) AS path,
      1 AS depth
    FROM sys_department
    WHERE parent_id IS NULL AND is_deleted = 0
      AND (p_status IS NULL OR status = p_status)

    UNION ALL

    -- 递归子节点
    SELECT
      d.id,
      d.name,
      d.short_name,
      d.parent_id,
      d.leader_id,
      d.level,
      d.sort,
      d.status,
      CONCAT(dt.path, ' > ', d.name) AS path,
      dt.depth + 1
    FROM sys_department d
    INNER JOIN dept_tree dt ON d.parent_id = dt.id
    WHERE d.is_deleted = 0 AND (p_status IS NULL OR d.status = p_status)
  )
  SELECT * FROM dept_tree ORDER BY level, sort;
END$$

DELIMITER ;

-- 调用示例
-- CALL sp_department_tree('active');
```

### 4.2 获取部门及其所有子部门ID

```sql
DELIMITER $$

CREATE PROCEDURE sp_department_children(
  IN p_department_id VARCHAR(20)
)
BEGIN
  WITH RECURSIVE dept_children AS (
    -- 当前部门
    SELECT id, parent_id, name
    FROM sys_department
    WHERE id = p_department_id AND is_deleted = 0

    UNION ALL

    -- 递归子部门
    SELECT d.id, d.parent_id, d.name
    FROM sys_department d
    INNER JOIN dept_children dc ON d.parent_id = dc.id
    WHERE d.is_deleted = 0
  )
  SELECT id FROM dept_children;
END$$

DELIMITER ;
```

### 4.3 移动部门

```sql
DELIMITER $$

CREATE PROCEDURE sp_move_department(
  IN p_department_id VARCHAR(20),
  IN p_new_parent_id VARCHAR(20),
  OUT p_result INT
)
BEGIN
  DECLARE v_current_level INT;
  DECLARE v_new_level INT;
  DECLARE v_max_level INT DEFAULT 5;
  DECLARE v_error_msg VARCHAR(200);

  -- 开启事务
  START TRANSACTION;

  -- 1. 检查部门是否存在
  SELECT level INTO v_current_level
  FROM sys_department
  WHERE id = p_department_id AND is_deleted = 0;

  IF v_current_level IS NULL THEN
    SET p_result = -1;
    ROLLBACK;
    LEAVE sp_move_department;
  END IF;

  -- 2. 不能移动到自己
  IF p_department_id = p_new_parent_id THEN
    SET p_result = -2;
    ROLLBACK;
    LEAVE sp_move_department;
  END IF;

  -- 3. 不能移动到自己的子部门
  IF p_new_parent_id IS NOT NULL THEN
    WITH RECURSIVE dept_tree AS (
      SELECT id, parent_id FROM sys_department WHERE id = p_department_id
      UNION ALL
      SELECT d.id, d.parent_id
      FROM sys_department d
      INNER JOIN dept_tree dt ON d.parent_id = dt.id
    )
    SELECT COUNT(*) INTO @child_count
    FROM dept_tree
    WHERE id = p_new_parent_id;

    IF @child_count > 0 THEN
      SET p_result = -3;
      ROLLBACK;
      LEAVE sp_move_department;
    END IF;
  END IF;

  -- 4. 计算新层级
  IF p_new_parent_id IS NULL THEN
    SET v_new_level = 1;
  ELSE
    SELECT level INTO v_new_level
    FROM sys_department
    WHERE id = p_new_parent_id AND is_deleted = 0;

    SET v_new_level = v_new_level + 1;
  END IF;

  -- 5. 检查层级是否超限
  IF v_new_level > v_max_level THEN
    SET p_result = -4;
    ROLLBACK;
    LEAVE sp_move_department;
  END IF;

  -- 6. 更新部门及其所有子部门的层级
  WITH RECURSIVE dept_tree AS (
    SELECT id, parent_id, 0 AS level_offset
    FROM sys_department
    WHERE id = p_department_id

    UNION ALL

    SELECT d.id, d.parent_id, dt.level_offset + 1
    FROM sys_department d
    INNER JOIN dept_tree dt ON d.parent_id = dt.id
  )
  UPDATE sys_department s
  INNER JOIN (
    SELECT
      t.id,
      (v_new_level - v_current_level + t.level_offset) AS new_level
    FROM dept_tree t
  ) calc ON s.id = calc.id
  SET s.level = calc.new_level;

  -- 7. 更新上级部门
  UPDATE sys_department
  SET parent_id = p_new_parent_id
  WHERE id = p_department_id;

  COMMIT;
  SET p_result = 1;
END$$

DELIMITER ;
```

### 4.4 删除部门前检查

```sql
DELIMITER $$

CREATE PROCEDURE sp_check_department_delete(
  IN p_department_id VARCHAR(20),
  OUT p_can_delete TINYINT,
  OUT p_error_msg VARCHAR(200)
)
BEGIN
  DECLARE v_child_count INT;
  DECLARE v_employee_count INT;

  -- 检查是否有子部门
  SELECT COUNT(*) INTO v_child_count
  FROM sys_department
  WHERE parent_id = p_department_id AND is_deleted = 0;

  IF v_child_count > 0 THEN
    SET p_can_delete = 0;
    SET p_error_msg = '该部门下还有子部门,请先删除或移动子部门';
    LEAVE sp_check_department_delete;
  END IF;

  -- 检查是否有成员
  SELECT COUNT(*) INTO v_employee_count
  FROM sys_employee
  WHERE department_id = p_department_id AND is_deleted = 0;

  IF v_employee_count > 0 THEN
    SET p_can_delete = 0;
    SET p_error_msg = CONCAT('该部门下还有 ', v_employee_count, ' 名员工,请先转移或删除员工');
    LEAVE sp_check_department_delete;
  END IF;

  SET p_can_delete = 1;
  SET p_error_msg = '';
END$$

DELIMITER ;
```

### 4.5 部门统计

```sql
DELIMITER $$

CREATE PROCEDURE sp_department_statistics()
BEGIN
  SELECT
    COUNT(*) AS total,
    SUM(CASE WHEN level = 1 THEN 1 ELSE 0 END) AS level1_count,
    MAX(level) AS max_level,
    SUM(CASE WHEN leader_id IS NOT NULL THEN 1 ELSE 0 END) AS with_leader_count,
    (SELECT COUNT(DISTINCT department_id) FROM sys_employee WHERE is_deleted = 0) AS has_employee_count
  FROM sys_department
  WHERE is_deleted = 0;
END$$

DELIMITER ;
```

---

## 5. 数据字典映射

### 5.1 部门状态字典 (department_status)

```sql
-- 字典类型: department_status
INSERT INTO sys_dict_type (code, name, category, status) VALUES
('department_status', '部门状态', 'business', 'enabled');

INSERT INTO sys_dict_item (type_code, label, value, color, sort, status) VALUES
('department_status', '正常', 'active', '#67C23A', 1, 'enabled'),
('department_status', '停用', 'disabled', '#909399', 2, 'enabled');
```

### 5.2 部门类型字典 (department_type)

```sql
-- 字典类型: department_type
INSERT INTO sys_dict_type (code, name, category, status) VALUES
('department_type', '部门类型', 'business', 'enabled');

INSERT INTO sys_dict_item (type_code, label, value, sort, status) VALUES
('department_type', '公司', 'company', 1, 'enabled'),
('department_type', '部门', 'department', 2, 'enabled'),
('department_type', '团队', 'team', 3, 'enabled'),
('department_type', '小组', 'group', 4, 'enabled');
```

---

## 6. 视图设计

### 6.1 部门详情视图

```sql
CREATE VIEW v_department_detail AS
SELECT
  d.id,
  d.name,
  d.short_name,
  d.parent_id,
  p.name AS parent_name,
  d.leader_id,
  e.name AS leader_name,
  e.position AS leader_position,
  d.level,
  d.sort,
  d.established_date,
  d.description,
  d.icon,
  d.status,
  (SELECT COUNT(*) FROM sys_employee WHERE department_id = d.id AND is_deleted = 0) AS employee_count,
  d.created_at,
  d.updated_at
FROM sys_department d
LEFT JOIN sys_department p ON d.parent_id = p.id
LEFT JOIN sys_employee e ON d.leader_id = e.id
WHERE d.is_deleted = 0;
```

### 6.2 部门成员视图

```sql
CREATE VIEW v_department_members AS
SELECT
  d.id AS department_id,
  d.name AS department_name,
  e.id AS employee_id,
  e.name AS employee_name,
  e.position,
  e.status AS employee_status,
  dm.is_leader,
  dm.join_date,
  dm.leave_date
FROM sys_department d
INNER JOIN sys_department_member dm ON d.id = dm.department_id
INNER JOIN sys_employee e ON dm.employee_id = e.id
WHERE dm.leave_date IS NULL
  AND d.is_deleted = 0
  AND e.is_deleted = 0;
```

### 6.3 部门树形视图

```sql
CREATE VIEW v_department_tree AS
WITH RECURSIVE dept_tree AS (
  -- 根节点
  SELECT
    id,
    name,
    short_name,
    parent_id,
    leader_id,
    level,
    sort,
    status,
    (SELECT COUNT(*) FROM sys_employee WHERE department_id = sys_department.id AND is_deleted = 0) AS employee_count,
    CAST(name AS CHAR(1000)) AS path
  FROM sys_department
  WHERE parent_id IS NULL AND is_deleted = 0 AND status = 'active'

  UNION ALL

  -- 递归子节点
  SELECT
    d.id,
    d.name,
    d.short_name,
    d.parent_id,
    d.leader_id,
    d.level,
    d.sort,
    d.status,
    (SELECT COUNT(*) FROM sys_employee WHERE department_id = d.id AND is_deleted = 0) AS employee_count,
    CONCAT(dt.path, ' > ', d.name) AS path
  FROM sys_department d
  INNER JOIN dept_tree dt ON d.parent_id = dt.id
  WHERE d.is_deleted = 0 AND d.status = 'active'
)
SELECT * FROM dept_tree ORDER BY level, sort;
```

---

## 7. 数据完整性保障

### 7.1 检查约束

```sql
-- 层级不能超过5级
ALTER TABLE sys_department
  ADD CONSTRAINT chk_dept_level
  CHECK (level BETWEEN 1 AND 5);

-- 不能选择自己作为上级部门(触发器处理)
-- 参见 trg_department_before_update 触发器

-- 部门名称在同一级内唯一
-- 参见 uk_department_name 唯一索引
```

---

## 8. 性能优化建议

### 8.1 物化视图(使用定时任务更新)

```sql
-- 创建部门统计表
CREATE TABLE sys_department_stats (
  department_id VARCHAR(20) PRIMARY KEY,
  employee_count INT DEFAULT 0,
  active_employee_count INT DEFAULT 0,
  probation_count INT DEFAULT 0,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='部门统计表(定时更新)';

-- 创建定时更新存储过程
DELIMITER $$

CREATE PROCEDURE sp_update_department_stats()
BEGIN
  INSERT INTO sys_department_stats (department_id, employee_count, active_employee_count, probation_count)
  SELECT
    d.id,
    COUNT(e.id),
    SUM(CASE WHEN e.status = 'active' THEN 1 ELSE 0 END),
    SUM(CASE WHEN e.probation_status = 'probation' THEN 1 ELSE 0 END)
  FROM sys_department d
  LEFT JOIN sys_employee e ON d.id = e.department_id AND e.is_deleted = 0
  WHERE d.is_deleted = 0
  GROUP BY d.id
  ON DUPLICATE KEY UPDATE
    employee_count = VALUES(employee_count),
    active_employee_count = VALUES(active_employee_count),
    probation_count = VALUES(probation_count),
    updated_at = CURRENT_TIMESTAMP;
END$$

DELIMITER ;

-- 创建定时事件(每小时更新一次)
CREATE EVENT evt_update_department_stats
ON SCHEDULE EVERY 1 HOUR
DO CALL sp_update_department_stats();
```

### 8.2 查询优化

```sql
-- 使用EXPLAIN分析查询
EXPLAIN SELECT * FROM sys_department
WHERE parent_id IS NULL AND status = 'active';

-- 优化建议:
-- 1. 确保索引 idx_department_parent 存在
-- 2. 使用覆盖索引
-- 3. 避免递归查询过深(限制层级)
```

---

## 9. 初始化数据

```sql
-- 插入示例部门数据
INSERT INTO sys_department (
  id, name, short_name, parent_id, leader_id, level, sort, status
) VALUES
('DEPT0001', 'XX科技有限公司', '总公司', NULL, 'EMP20230115001', 1, 1, 'active'),
('DEPT0002', '技术部', '技术', 'DEPT0001', 'EMP20230115001', 2, 1, 'active'),
('DEPT0003', '产品部', '产品', 'DEPT0001', 'EMP20230115002', 2, 2, 'active'),
('DEPT0004', '设计部', '设计', 'DEPT0001', 'EMP20230115003', 2, 3, 'active'),
('DEPT0005', '前端开发组', '前端', 'DEPT0002', 'EMP20230115004', 3, 1, 'active'),
('DEPT0006', '后端开发组', '后端', 'DEPT0002', 'EMP20230115001', 3, 2, 'active'),
('DEPT0007', '测试组', '测试', 'DEPT0002', 'EMP20230115001', 3, 3, 'active');
```

---

## 10. API对应SQL查询

### 10.1 获取部门列表(树形)

```sql
-- 对应前端: GET /api/departments?type=tree
WITH RECURSIVE dept_tree AS (
  SELECT
    d.*,
    (SELECT COUNT(*) FROM sys_employee WHERE department_id = d.id AND is_deleted = 0) AS employee_count,
    e.name AS leader_name,
    CAST(NULL AS JSON) AS children
  FROM sys_department d
  LEFT JOIN sys_employee e ON d.leader_id = e.id
  WHERE d.parent_id IS NULL AND d.is_deleted = 0
    AND (? IS NULL OR d.status = ?)

  UNION ALL

  SELECT
    d.*,
    (SELECT COUNT(*) FROM sys_employee WHERE department_id = d.id AND is_deleted = 0) AS employee_count,
    e.name AS leader_name,
    NULL AS children
  FROM sys_department d
  LEFT JOIN sys_employee e ON d.leader_id = e.id
  INNER JOIN dept_tree dt ON d.parent_id = dt.id
  WHERE d.is_deleted = 0 AND (? IS NULL OR d.status = ?)
)
SELECT * FROM dept_tree ORDER BY level, sort;
```

### 10.2 获取部门列表(扁平)

```sql
-- 对应前端: GET /api/departments?type=flat
SELECT
  d.*,
  (SELECT COUNT(*) FROM sys_employee WHERE department_id = d.id AND is_deleted = 0) AS employee_count,
  e.name AS leader_name,
  p.name AS parent_name
FROM sys_department d
LEFT JOIN sys_employee e ON d.leader_id = e.id
LEFT JOIN sys_department p ON d.parent_id = p.id
WHERE d.is_deleted = 0
  AND (? IS NULL OR d.status = ?)
  AND (? IS NULL OR d.leader_id = ?)
  AND (? IS NULL OR d.level = ?)
  AND (? IS NULL OR
       d.name LIKE CONCAT('%', ?, '%')
       OR d.short_name LIKE CONCAT('%', ?, '%')
      )
ORDER BY d.level, d.sort;
```

### 10.3 获取部门详情

```sql
-- 对应前端: GET /api/departments/:id
SELECT
  d.*,
  (SELECT COUNT(*) FROM sys_employee WHERE department_id = d.id AND is_deleted = 0) AS employee_count,
  e.name AS leader_name,
  e.position AS leader_position,
  e.phone AS leader_phone,
  e.email AS leader_email,
  p.name AS parent_name
FROM sys_department d
LEFT JOIN sys_employee e ON d.leader_id = e.id
LEFT JOIN sys_department p ON d.parent_id = p.id
WHERE d.id = ? AND d.is_deleted = 0;
```

### 10.4 获取部门成员

```sql
-- 对应前端: GET /api/departments/:id/employees
SELECT
  e.*,
  dm.is_leader,
  dm.join_date AS join_department_date
FROM sys_employee e
INNER JOIN sys_department_member dm ON e.id = dm.employee_id
WHERE dm.department_id = ?
  AND dm.leave_date IS NULL
  AND e.is_deleted = 0
ORDER BY dm.is_leader DESC, e.join_date;
```

### 10.5 获取子部门

```sql
-- 对应前端: GET /api/departments/:id/children
SELECT
  d.*,
  (SELECT COUNT(*) FROM sys_employee WHERE department_id = d.id AND is_deleted = 0) AS employee_count,
  e.name AS leader_name
FROM sys_department d
LEFT JOIN sys_employee e ON d.leader_id = e.id
WHERE d.parent_id = ? AND d.is_deleted = 0
ORDER BY d.sort;
```

---

**文档版本**: v1.0.0
**创建人**: AI开发助手
**最后更新**: 2026-01-11
