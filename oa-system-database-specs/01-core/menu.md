# 菜单管理数据库设计

> **对应前端规范**: [menu_Technical.md](../../../oa-system-frontend-specs/core/menu/menu_Technical.md)
> **数据库**: MySQL 8.0+
> **表前缀**: `sys_`
> **版本**: v1.0.0

---

## 数据库表设计

### 1. 菜单表 (sys_menu)

#### 1.1 表结构

```sql
CREATE TABLE sys_menu (
  -- 主键
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '菜单ID',

  -- 基本信息
  menu_code VARCHAR(50) NOT NULL UNIQUE COMMENT '菜单编号(MENU+4位序号)',
  menu_name VARCHAR(100) NOT NULL COMMENT '菜单名称',
  menu_type ENUM('directory', 'menu', 'button') NOT NULL DEFAULT 'menu' COMMENT '菜单类型: directory目录 menu菜单 button按钮',
  parent_id BIGINT NOT NULL DEFAULT 0 COMMENT '父级菜单ID(0表示一级菜单)',
  menu_level INT NOT NULL DEFAULT 1 COMMENT '菜单层级(1-3)',

  -- 路由信息
  route_path VARCHAR(200) COMMENT '路由路径',
  component_path VARCHAR(200) COMMENT '组件路径',
  redirect_path VARCHAR(200) COMMENT '重定向路径',

  -- 显示信息
  menu_icon VARCHAR(100) COMMENT '菜单图标',
  permission VARCHAR(100) COMMENT '权限标识',
  sort_order INT NOT NULL DEFAULT 0 COMMENT '排序号',

  -- 状态控制
  visible TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否显示(0隐藏1显示)',
  status TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态(0禁用1启用)',
  is_cache TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否缓存(0不缓存1缓存)',
  is_frame TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否外链(0否1是)',
  frame_url VARCHAR(500) COMMENT '外链URL',
  menu_target VARCHAR(20) COMMENT '链接打开方式(_self/_blank)',

  -- 系统标识
  is_system TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否系统菜单(0否1是)',
  remark VARCHAR(500) COMMENT '备注',

  -- 审计字段
  created_by BIGINT COMMENT '创建人ID',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_by BIGINT COMMENT '更新人ID',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted_at DATETIME COMMENT '删除时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='菜单管理表';
```

#### 1.2 索引设计

```sql
-- 主键索引
-- PRIMARY KEY (id)

-- 唯一索引
-- UNIQUE KEY (menu_code)

-- 普通索引
CREATE INDEX idx_menu_parent_id ON sys_menu(parent_id) COMMENT '父级菜单ID索引';
CREATE INDEX idx_menu_type ON sys_menu(menu_type) COMMENT '菜单类型索引';
CREATE INDEX idx_menu_status ON sys_menu(status) COMMENT '状态索引';
CREATE INDEX idx_menu_sort_order ON sys_menu(sort_order) COMMENT '排序索引';
CREATE INDEX idx_menu_deleted_at ON sys_menu(deleted_at) COMMENT '删除时间索引';

-- 组合索引
CREATE INDEX idx_menu_parent_status ON sys_menu(parent_id, status) COMMENT '父级+状态组合索引';
CREATE INDEX idx_menu_type_status ON sys_menu(menu_type, status) COMMENT '类型+状态组合索引';
```

---

### 2. 角色菜单关联表 (sys_role_menu)

#### 2.1 表结构

```sql
CREATE TABLE sys_role_menu (
  -- 主键
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '关联ID',

  -- 关联信息
  role_id BIGINT NOT NULL COMMENT '角色ID',
  menu_id BIGINT NOT NULL COMMENT '菜单ID',

  -- 审计字段
  created_by BIGINT COMMENT '创建人ID',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

  -- 唯一约束
  UNIQUE KEY uk_role_menu (role_id, menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色菜单关联表';
```

#### 2.2 索引设计

```sql
-- 主键索引
-- PRIMARY KEY (id)

-- 唯一索引
-- UNIQUE KEY uk_role_menu (role_id, menu_id)

-- 普通索引
CREATE INDEX idx_role_menu_role_id ON sys_role_menu(role_id) COMMENT '角色ID索引';
CREATE INDEX idx_role_menu_menu_id ON sys_role_menu(menu_id) COMMENT '菜单ID索引';
```

#### 2.3 外键约束

```sql
-- 角色外键
ALTER TABLE sys_role_menu
  ADD CONSTRAINT fk_role_menu_role
  FOREIGN KEY (role_id)
  REFERENCES sys_role(id)
  ON DELETE CASCADE
  ON UPDATE CASCADE;

-- 菜单外键
ALTER TABLE sys_role_menu
  ADD CONSTRAINT fk_role_menu_menu
  FOREIGN KEY (menu_id)
  REFERENCES sys_menu(id)
  ON DELETE CASCADE
  ON UPDATE CASCADE;
```

---

## 3. 触发器设计

### 3.1 菜单层级自动更新触发器

```sql
DELIMITER $$

-- 插入菜单前计算层级
CREATE TRIGGER trg_menu_before_insert
BEFORE INSERT ON sys_menu
FOR EACH ROW
BEGIN
  -- 一级菜单
  IF NEW.parent_id = 0 THEN
    SET NEW.menu_level = 1;
  ELSE
    -- 计算父菜单层级并加1
    DECLARE parent_level INT;
    SELECT menu_level INTO parent_level FROM sys_menu WHERE id = NEW.parent_id;
    SET NEW.menu_level = COALESCE(parent_level, 0) + 1;
  END IF;
END$$

-- 更新菜单时重新计算层级
CREATE TRIGGER trg_menu_before_update
BEFORE UPDATE ON sys_menu
FOR EACH ROW
BEGIN
  -- 父级菜单变更时重新计算层级
  IF NEW.parent_id <> OLD.parent_id THEN
    IF NEW.parent_id = 0 THEN
      SET NEW.menu_level = 1;
    ELSE
      DECLARE parent_level INT;
      SELECT menu_level INTO parent_level FROM sys_menu WHERE id = NEW.parent_id;
      SET NEW.menu_level = COALESCE(parent_level, 0) + 1;
    END IF;
  END IF;
END$$

DELIMITER ;
```

---

## 4. 存储过程设计

### 4.1 获取菜单树

```sql
DELIMITER $$

CREATE PROCEDURE sp_get_menu_tree(IN p_status TINYINT)
BEGIN
  -- 获取菜单列表(按层级和排序)
  SELECT
    id,
    menu_code,
    menu_name,
    menu_type,
    parent_id,
    menu_level,
    route_path,
    component_path,
    redirect_path,
    menu_icon,
    permission,
    sort_order,
    visible,
    status,
    is_cache,
    is_frame,
    frame_url,
    menu_target,
    is_system,
    remark,
    created_at,
    updated_at
  FROM sys_menu
  WHERE deleted_at IS NULL
    AND (p_status IS NULL OR status = p_status)
  ORDER BY parent_id, sort_order, id;
END$$

DELIMITER ;
```

### 4.2 获取用户菜单

```sql
DELIMITER $$

CREATE PROCEDURE sp_get_user_menus(IN p_user_id BIGINT, IN p_status TINYINT)
BEGIN
  -- 获取用户有权限的菜单列表
  SELECT DISTINCT
    m.id,
    m.menu_code,
    m.menu_name,
    m.menu_type,
    m.parent_id,
    m.menu_level,
    m.route_path,
    m.component_path,
    m.redirect_path,
    m.menu_icon,
    m.permission,
    m.sort_order,
    m.visible,
    m.status,
    m.is_cache,
    m.is_frame,
    m.frame_url,
    m.menu_target
  FROM sys_menu m
  INNER JOIN sys_role_menu rm ON m.id = rm.menu_id
  INNER JOIN sys_user_role ur ON rm.role_id = ur.role_id
  WHERE ur.user_id = p_user_id
    AND m.deleted_at IS NULL
    AND m.status = COALESCE(p_status, m.status)
    AND m.visible = 1
  ORDER BY m.parent_id, m.sort_order, m.id;
END$$

DELIMITER ;
```

### 4.3 获取菜单路径

```sql
DELIMITER $$

CREATE PROCEDURE sp_get_menu_path(IN p_menu_id BIGINT)
BEGIN
  -- 递归查询菜单路径
  WITH RECURSIVE menu_path AS (
    -- 当前菜单
    SELECT
      id,
      menu_name,
      parent_id,
      1 AS level
    FROM sys_menu
    WHERE id = p_menu_id

    UNION ALL

    -- 递归查询父级菜单
    SELECT
      m.id,
      m.menu_name,
      m.parent_id,
      mp.level + 1
    FROM sys_menu m
    INNER JOIN menu_path mp ON m.id = mp.parent_id
    WHERE m.parent_id != 0
  )
  SELECT
    menu_name,
    parent_id
  FROM menu_path
  WHERE parent_id != 0
  ORDER BY level DESC;
END$$

DELIMITER ;
```

### 4.4 检查菜单是否有子菜单

```sql
DELIMITER $$

CREATE PROCEDURE sp_check_menu_children(IN p_menu_id BIGINT)
BEGIN
  SELECT
    COUNT(*) AS child_count,
    COUNT(CASE WHEN menu_type = 'menu' THEN 1 END) AS menu_count,
    COUNT(CASE WHEN menu_type = 'button' THEN 1 END) AS button_count
  FROM sys_menu
  WHERE parent_id = p_menu_id
    AND deleted_at IS NULL;
END$$

DELIMITER ;
```

---

## 5. 视图设计

### 5.1 菜单树视图

```sql
CREATE VIEW v_menu_tree AS
SELECT
  m.id,
  m.menu_code,
  m.menu_name,
  m.menu_type,
  m.parent_id,
  m.menu_level,
  m.route_path,
  m.component_path,
  m.redirect_path,
  m.menu_icon,
  m.permission,
  m.sort_order,
  m.visible,
  m.status,
  m.is_cache,
  m.is_frame,
  m.frame_url,
  m.menu_target,
  m.is_system,
  m.remark,
  m.created_at,
  m.updated_at,
  -- 父菜单名称
  p.menu_name AS parent_name,
  p.menu_code AS parent_code,
  -- 子菜单数量
  (SELECT COUNT(*) FROM sys_menu WHERE parent_id = m.id AND deleted_at IS NULL) AS child_count
FROM sys_menu m
LEFT JOIN sys_menu p ON m.parent_id = p.id
WHERE m.deleted_at IS NULL;
```

### 5.2 角色菜单视图

```sql
CREATE VIEW v_role_menu AS
SELECT
  r.id AS role_id,
  r.role_code,
  r.role_name,
  m.id AS menu_id,
  m.menu_code,
  m.menu_name,
  m.menu_type,
  m.route_path,
  m.sort_order
FROM sys_role r
INNER JOIN sys_role_menu rm ON r.id = rm.role_id
INNER JOIN sys_menu m ON rm.menu_id = m.id
WHERE r.deleted_at IS NULL
  AND m.deleted_at IS NULL
ORDER BY m.parent_id, m.sort_order;
```

### 5.3 用户菜单视图

```sql
CREATE VIEW v_user_menu AS
SELECT
  u.id AS user_id,
  u.username,
  m.id AS menu_id,
  m.menu_code,
  m.menu_name,
  m.menu_type,
  m.parent_id,
  m.menu_level,
  m.route_path,
  m.component_path,
  m.menu_icon,
  m.permission,
  m.sort_order,
  m.visible,
  r.id AS role_id,
  r.role_code
FROM sys_user u
INNER JOIN sys_user_role ur ON u.id = ur.user_id
INNER JOIN sys_role r ON ur.role_id = r.id
INNER JOIN sys_role_menu rm ON r.id = rm.role_id
INNER JOIN sys_menu m ON rm.menu_id = m.id
WHERE u.deleted_at IS NULL
  AND r.deleted_at IS NULL
  AND m.deleted_at IS NULL
ORDER BY m.parent_id, m.sort_order;
```

---

## 6. 数据完整性保障

### 6.1 检查约束

```sql
-- 菜单类型检查
ALTER TABLE sys_menu
  ADD CONSTRAINT chk_menu_type
  CHECK (menu_type IN ('directory', 'menu', 'button'));

-- 菜单层级检查(1-3级)
ALTER TABLE sys_menu
  ADD CONSTRAINT chk_menu_level
  CHECK (menu_level BETWEEN 1 AND 3);

-- 排序号非负
ALTER TABLE sys_menu
  ADD CONSTRAINT chk_sort_order
  CHECK (sort_order >= 0);

-- 链接打开方式检查
ALTER TABLE sys_menu
  ADD CONSTRAINT chk_menu_target
  CHECK (menu_target IS NULL OR menu_target IN ('_self', '_blank'));

-- 父级菜单不能是自己
ALTER TABLE sys_menu
  ADD CONSTRAINT chk_parent_not_self
  CHECK (parent_id != id);
```

### 6.2 菜单层级限制

```sql
-- 检查是否超过3级
DELIMITER $$

CREATE TRIGGER trg_menu_check_level
BEFORE INSERT ON sys_menu
FOR EACH ROW
BEGIN
  DECLARE v_level INT;

  IF NEW.parent_id != 0 THEN
    SELECT menu_level INTO v_level FROM sys_menu WHERE id = NEW.parent_id;

    IF v_level >= 3 THEN
      SIGNAL SQLSTATE '45000'
      SET MESSAGE_TEXT = '菜单层级不能超过3级';
    END IF;
  END IF;
END$$

DELIMITER ;
```

---

## 7. 初始化数据

### 7.1 系统菜单初始化

```sql
-- 一级菜单(目录)
INSERT INTO sys_menu (
  menu_code, menu_name, menu_type, parent_id, menu_level,
  route_path, menu_icon, sort_order, is_system, status, visible
) VALUES
('MENU0001', '系统管理', 'directory', 0, 1, '/system', 'Setting', 100, 1, 1, 1),
('MENU0002', '员工管理', 'directory', 0, 1, '/employee', 'User', 200, 1, 1, 1),
('MENU0003', '资产管理', 'directory', 0, 1, '/asset', 'Management', 300, 1, 1, 1),
('MENU0004', '审批管理', 'directory', 0, 1, '/approval', 'DocumentChecked', 400, 1, 1, 1),
('MENU0005', '会议管理', 'directory', 0, 1, '/meeting', 'Calendar', 500, 1, 1, 1);

-- 系统管理子菜单
INSERT INTO sys_menu (
  menu_code, menu_name, menu_type, parent_id, menu_level,
  route_path, component_path, menu_icon, permission, sort_order, is_system
) VALUES
('MENU0006', '用户管理', 'menu', 1, 2, '/system/user', 'system/user/index', 'User', 'system:user:view', 1, 1),
('MENU0007', '角色管理', 'menu', 1, 2, '/system/role', 'system/role/index', 'UserFilled', 'system:role:view', 2, 1),
('MENU0008', '菜单管理', 'menu', 1, 2, '/system/menu', 'system/menu/index', 'Menu', 'system:menu:view', 3, 1),
('MENU0009', '部门管理', 'menu', 1, 2, '/system/dept', 'system/dept/index', 'OfficeBuilding', 'system:dept:view', 4, 1),
('MENU0010', '字典管理', 'menu', 1, 2, '/system/dict', 'system/dict/index', 'Notebook', 'system:dict:view', 5, 1);

-- 员工管理子菜单
INSERT INTO sys_menu (
  menu_code, menu_name, menu_type, parent_id, menu_level,
  route_path, component_path, menu_icon, permission, sort_order, is_system
) VALUES
('MENU0011', '员工名录', 'menu', 2, 2, '/employee/list', 'employee/list/index', 'List', 'employee:list:view', 1, 1),
('MENU0012', '员工详情', 'menu', 2, 2, '/employee/detail', 'employee/detail/index', 'Document', 'employee:detail:view', 2, 1);

-- 资产管理子菜单
INSERT INTO sys_menu (
  menu_code, menu_name, menu_type, parent_id, menu_level,
  route_path, component_path, menu_icon, permission, sort_order, is_system
) VALUES
('MENU0013', '资产列表', 'menu', 3, 2, '/asset/list', 'asset/list/index', 'Tickets', 'asset:list:view', 1, 1),
('MENU0014', '资产登记', 'menu', 3, 2, '/asset/register', 'asset/register/index', 'CirclePlus', 'asset:register:create', 2, 1),
('MENU0015', '资产领用', 'menu', 3, 2, '/asset/assign', 'asset/assign/index', 'Download', 'asset:assign:create', 3, 1);

-- 审批管理子菜单
INSERT INTO sys_menu (
  menu_code, menu_name, menu_type, parent_id, menu_level,
  route_path, component_path, menu_icon, permission, sort_order, is_system
) VALUES
('MENU0016', '请假申请', 'menu', 4, 2, '/approval/leave', 'approval/leave/index', 'Calendar', 'leave:apply:create', 1, 1),
('MENU0017', '报销申请', 'menu', 4, 2, '/approval/expense', 'approval/expense/index', 'Money', 'expense:apply:create', 2, 1),
('MENU0018', '我的审批', 'menu', 4, 2, '/approval/my', 'approval/my/index', 'CircleCheck', 'approval:my:view', 3, 1);

-- 会议管理子菜单
INSERT INTO sys_menu (
  menu_code, menu_name, menu_type, parent_id, menu_level,
  route_path, component_path, menu_icon, permission, sort_order, is_system
) VALUES
('MENU0019', '会议预约', 'menu', 5, 2, '/meeting/booking', 'meeting/booking/index', 'Calendar', 'meeting:booking:create', 1, 1),
('MENU0020', '会议记录', 'menu', 5, 2, '/meeting/record', 'meeting/record/index', 'Document', 'meeting:record:view', 2, 1),
('MENU0021', '会议统计', 'menu', 5, 2, '/meeting/statistics', 'meeting/statistics/index', 'DataAnalysis', 'meeting:statistics:view', 3, 1);

-- 按钮权限
INSERT INTO sys_menu (
  menu_code, menu_name, menu_type, parent_id, menu_level,
  permission, sort_order, is_system, visible
) VALUES
('MENU0022', '新增', 'button', 6, 3, 'system:user:create', 1, 1, 0),
('MENU0023', '编辑', 'button', 6, 3, 'system:user:edit', 2, 1, 0),
('MENU0024', '删除', 'button', 6, 3, 'system:user:delete', 3, 1, 0),
('MENU0025', '重置密码', 'button', 6, 3, 'system:user:reset', 4, 1, 0);
```

### 7.2 超级管理员角色菜单关联

```sql
-- 假设超级管理员角色ID为1
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, id FROM sys_menu WHERE deleted_at IS NULL;
```

---

## 8. API对应SQL查询

### 8.1 获取菜单列表

```sql
-- 对应前端: GET /api/menus
SELECT
  id,
  menu_code,
  menu_name,
  menu_type,
  parent_id,
  menu_level,
  route_path,
  component_path,
  redirect_path,
  menu_icon,
  permission,
  sort_order,
  visible,
  status,
  is_cache,
  is_frame,
  frame_url,
  menu_target,
  is_system,
  remark,
  created_at,
  updated_at
FROM sys_menu
WHERE deleted_at IS NULL
  AND (? IS NULL OR menu_type = ?)
  AND (? IS NULL OR status = ?)
  AND (? IS NULL OR menu_name LIKE CONCAT('%', ?, '%') OR route_path LIKE CONCAT('%', ?, '%'))
ORDER BY parent_id, sort_order, id;
```

### 8.2 获取菜单树

```sql
-- 对应前端: GET /api/menus/tree
WITH RECURSIVE menu_tree AS (
  -- 根菜单
  SELECT
    id,
    menu_code,
    menu_name,
    menu_type,
    parent_id,
    menu_level,
    route_path,
    component_path,
    redirect_path,
    menu_icon,
    permission,
    sort_order,
    visible,
    status,
    is_cache,
    is_frame,
    frame_url,
    menu_target,
    is_system,
    1 AS depth
  FROM sys_menu
  WHERE parent_id = 0 AND deleted_at IS NULL AND (? IS NULL OR status = ?)

  UNION ALL

  -- 递归子菜单
  SELECT
    m.id,
    m.menu_code,
    m.menu_name,
    m.menu_type,
    m.parent_id,
    m.menu_level,
    m.route_path,
    m.component_path,
    m.redirect_path,
    m.menu_icon,
    m.permission,
    m.sort_order,
    m.visible,
    m.status,
    m.is_cache,
    m.is_frame,
    m.frame_url,
    m.menu_target,
    m.is_system,
    mt.depth + 1
  FROM sys_menu m
  INNER JOIN menu_tree mt ON m.parent_id = mt.id
  WHERE m.deleted_at IS NULL
    AND (? IS NULL OR m.status = ?)
)
SELECT * FROM menu_tree
ORDER BY parent_id, sort_order, id;
```

### 8.3 获取菜单详情

```sql
-- 对应前端: GET /api/menus/:id
SELECT
  id,
  menu_code,
  menu_name,
  menu_type,
  parent_id,
  menu_level,
  route_path,
  component_path,
  redirect_path,
  menu_icon,
  permission,
  sort_order,
  visible,
  status,
  is_cache,
  is_frame,
  frame_url,
  menu_target,
  is_system,
  remark,
  created_by,
  created_at,
  updated_by,
  updated_at
FROM sys_menu
WHERE id = ? AND deleted_at IS NULL;
```

### 8.4 获取父级菜单选项

```sql
-- 对应前端: GET /api/menus/parent-options
SELECT
  id,
  menu_code,
  menu_name,
  menu_type,
  menu_level,
  parent_id
FROM sys_menu
WHERE deleted_at IS NULL
  AND status = 1
  AND menu_type IN ('directory', 'menu')
  AND menu_level < 3
  AND (? IS NULL OR id != ?)
ORDER BY parent_id, sort_order, id;
```

### 8.5 获取用户路由菜单

```sql
-- 对应前端: GET /api/menus/routes
WITH RECURSIVE user_menu_tree AS (
  -- 根菜单
  SELECT
    m.id,
    m.menu_name,
    m.menu_type,
    m.parent_id,
    m.menu_level,
    m.route_path,
    m.component_path,
    m.redirect_path,
    m.menu_icon,
    m.permission,
    m.sort_order,
    m.visible,
    m.is_cache,
    1 AS depth
  FROM sys_menu m
  INNER JOIN sys_role_menu rm ON m.id = rm.menu_id
  INNER JOIN sys_user_role ur ON rm.role_id = ur.role_id
  WHERE ur.user_id = ?
    AND m.parent_id = 0
    AND m.deleted_at IS NULL
    AND m.status = 1
    AND m.visible = 1

  UNION ALL

  -- 递归子菜单
  SELECT
    m.id,
    m.menu_name,
    m.menu_type,
    m.parent_id,
    m.menu_level,
    m.route_path,
    m.component_path,
    m.redirect_path,
    m.menu_icon,
    m.permission,
    m.sort_order,
    m.visible,
    m.is_cache,
    umt.depth + 1
  FROM sys_menu m
  INNER JOIN sys_role_menu rm ON m.id = rm.menu_id
  INNER JOIN sys_user_role ur ON rm.role_id = ur.role_id
  INNER JOIN user_menu_tree umt ON m.parent_id = umt.id
  WHERE ur.user_id = ?
    AND m.deleted_at IS NULL
    AND m.status = 1
    AND m.visible = 1
)
SELECT * FROM user_menu_tree
WHERE menu_type != 'button'
ORDER BY parent_id, sort_order, id;
```

### 8.6 创建菜单

```sql
-- 对应前端: POST /api/menus
INSERT INTO sys_menu (
  menu_code,
  menu_name,
  menu_type,
  parent_id,
  route_path,
  component_path,
  redirect_path,
  menu_icon,
  permission,
  sort_order,
  visible,
  status,
  is_cache,
  is_frame,
  frame_url,
  menu_target,
  remark,
  created_by
) VALUES (
  ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?
);
```

### 8.7 更新菜单

```sql
-- 对应前端: PUT /api/menus/:id
UPDATE sys_menu
SET
  menu_name = ?,
  menu_type = ?,
  parent_id = ?,
  route_path = ?,
  component_path = ?,
  redirect_path = ?,
  menu_icon = ?,
  permission = ?,
  sort_order = ?,
  visible = ?,
  status = ?,
  is_cache = ?,
  is_frame = ?,
  frame_url = ?,
  menu_target = ?,
  remark = ?,
  updated_by = ?,
  updated_at = CURRENT_TIMESTAMP
WHERE id = ? AND deleted_at IS NULL;
```

### 8.8 删除菜单

```sql
-- 对应前端: DELETE /api/menus/:id
-- 软删除
UPDATE sys_menu
SET deleted_at = CURRENT_TIMESTAMP
WHERE id = ? AND is_system = 0;

-- 或硬删除(需确保没有子菜单)
DELETE FROM sys_menu
WHERE id = ? AND is_system = 0;
```

### 8.9 切换菜单状态

```sql
-- 对应前端: PATCH /api/menus/:id/status
UPDATE sys_menu
SET status = ?,
    updated_by = ?,
    updated_at = CURRENT_TIMESTAMP
WHERE id = ? AND is_system = 0;
```

---

## 9. 性能优化建议

### 9.1 查询优化

```sql
-- 使用EXPLAIN分析查询
EXPLAIN SELECT * FROM sys_menu
WHERE parent_id = 0 AND status = 1;

-- 优化建议:
-- 1. 确保索引 idx_menu_parent_status 存在
-- 2. 避免SELECT *, 只查询需要的字段
-- 3. 使用覆盖索引
-- 4. 定期ANALYZE TABLE更新统计信息
```

### 9.2 菜单缓存策略

```sql
-- 创建菜单缓存表(可选)
CREATE TABLE sys_menu_cache (
  cache_key VARCHAR(100) PRIMARY KEY COMMENT '缓存键',
  cache_data LONGTEXT COMMENT '缓存数据(JSON)',
  expire_at DATETIME NOT NULL COMMENT '过期时间',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_expire_at (expire_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单缓存表';

-- 清理过期缓存
DELETE FROM sys_menu_cache WHERE expire_at < CURRENT_TIMESTAMP;
```

---

## 10. 数据字典映射

### 10.1 菜单类型字典 (menu_type)

```sql
-- 字典类型: menu_type
INSERT INTO sys_dict_type (code, name, category, status) VALUES
('menu_type', '菜单类型', 'system', 'enabled');

INSERT INTO sys_dict_item (type_code, label, value, color, sort, status) VALUES
('menu_type', '目录', 'directory', '#409EFF', 1, 'enabled'),
('menu_type', '菜单', 'menu', '#67C23A', 2, 'enabled'),
('menu_type', '按钮', 'button', '#909399', 3, 'enabled');
```

### 10.2 菜单状态字典 (menu_status)

```sql
-- 字典类型: menu_status
INSERT INTO sys_dict_type (code, name, category, status) VALUES
('menu_status', '菜单状态', 'system', 'enabled');

INSERT INTO sys_dict_item (type_code, label, value, color, sort, status) VALUES
('menu_status', '启用', '1', '#67C23A', 1, 'enabled'),
('menu_status', '禁用', '0', '#909399', 2, 'enabled');
```

### 10.3 链接打开方式字典 (menu_target)

```sql
-- 字典类型: menu_target
INSERT INTO sys_dict_type (code, name, category, status) VALUES
('menu_target', '链接打开方式', 'system', 'enabled');

INSERT INTO sys_dict_item (type_code, label, value, sort, status) VALUES
('menu_target', '当前页', '_self', 1, 'enabled'),
('menu_target', '新页', '_blank', 2, 'enabled');
```

---

**文档版本**: v1.0.0
**创建人**: AI开发助手
**最后更新**: 2026-01-12
