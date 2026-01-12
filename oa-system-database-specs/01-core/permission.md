# 权限管理数据库设计

> **对应前端规范**: [permission_Technical.md](../../../oa-system-frontend-specs/core/permission/permission_Technical.md)
> **数据库**: MySQL 8.0+
> **表前缀**: `sys_`
> **版本**: v1.0.0

---

## 数据库表设计

### 1. 角色表 (sys_role)

#### 1.1 表结构

```sql
CREATE TABLE sys_role (
  -- 主键
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '角色ID',

  -- 基本信息
  role_code VARCHAR(50) NOT NULL UNIQUE COMMENT '角色编码',
  role_name VARCHAR(50) NOT NULL COMMENT '角色名称',
  role_type ENUM('system', 'custom') NOT NULL DEFAULT 'custom' COMMENT '角色类型: system系统角色 custom自定义角色',
  
  -- 描述信息
  description VARCHAR(200) COMMENT '角色描述',
  sort_order INT NOT NULL DEFAULT 0 COMMENT '排序号',
  
  -- 状态控制
  status TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态(1正常 0停用)',
  is_preset TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否预置角色(0否 1是)',

  -- 审计字段
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted_at DATETIME COMMENT '删除时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';
```

#### 1.2 索引设计

```sql
-- 主键索引
-- PRIMARY KEY (id)

-- 唯一索引
-- UNIQUE KEY (role_code)

-- 普通索引
CREATE INDEX idx_role_status ON sys_role(status) COMMENT '状态索引';
CREATE INDEX idx_role_type ON sys_role(role_type) COMMENT '类型索引';
CREATE INDEX idx_role_sort ON sys_role(sort_order) COMMENT '排序索引';
```

---

### 2. 权限表 (sys_permission)

> **注意**: 此表用于存储所有类型的权限（菜单、按钮、API、数据）。
> 菜单和按钮权限可能与 `sys_menu` 表存在重叠，实际应用中可视情况进行整合或通过 `sys_menu` 视图映射。

#### 2.1 表结构

```sql
CREATE TABLE sys_permission (
  -- 主键
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '权限ID',

  -- 基本信息
  permission_code VARCHAR(100) NOT NULL UNIQUE COMMENT '权限编码',
  permission_name VARCHAR(100) NOT NULL COMMENT '权限名称',
  permission_type ENUM('menu', 'button', 'api', 'data') NOT NULL COMMENT '权限类型',
  module VARCHAR(50) NOT NULL COMMENT '所属模块',
  
  -- 层级关系
  parent_id BIGINT NOT NULL DEFAULT 0 COMMENT '父级权限ID',
  
  -- 菜单/路由相关 (type=menu)
  route_path VARCHAR(200) COMMENT '路由路径',
  component_path VARCHAR(200) COMMENT '组件路径',
  icon VARCHAR(100) COMMENT '图标',
  
  -- API相关 (type=api)
  api_path VARCHAR(200) COMMENT '接口地址',
  api_method ENUM('GET', 'POST', 'PUT', 'DELETE', 'PATCH') COMMENT '请求方式',
  
  -- 数据权限相关 (type=data)
  data_scope ENUM('all', 'dept', 'dept_and_sub', 'self') COMMENT '数据范围',

  -- 其他信息
  sort_order INT NOT NULL DEFAULT 0 COMMENT '排序号',
  status TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态(1正常 0停用)',

  -- 审计字段
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted_at DATETIME COMMENT '删除时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='权限表';
```

#### 2.2 索引设计

```sql
-- 主键索引
-- PRIMARY KEY (id)

-- 唯一索引
-- UNIQUE KEY (permission_code)

-- 普通索引
CREATE INDEX idx_perm_parent_id ON sys_permission(parent_id) COMMENT '父级ID索引';
CREATE INDEX idx_perm_type ON sys_permission(permission_type) COMMENT '类型索引';
CREATE INDEX idx_perm_module ON sys_permission(module) COMMENT '模块索引';
CREATE INDEX idx_perm_status ON sys_permission(status) COMMENT '状态索引';
```

---

### 3. 用户角色关联表 (sys_user_role)

#### 3.1 表结构

```sql
CREATE TABLE sys_user_role (
  -- 主键
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '关联ID',

  -- 关联信息
  user_id VARCHAR(20) NOT NULL COMMENT '用户ID',
  role_id BIGINT NOT NULL COMMENT '角色ID',
  
  -- 有效期控制
  start_time DATETIME COMMENT '开始时间',
  end_time DATETIME COMMENT '结束时间',

  -- 审计字段
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  
  -- 唯一约束
  UNIQUE KEY uk_user_role (user_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';
```

#### 3.2 索引设计

```sql
-- 主键索引
-- PRIMARY KEY (id)

-- 唯一索引
-- UNIQUE KEY uk_user_role (user_id, role_id)

-- 普通索引
CREATE INDEX idx_user_role_user_id ON sys_user_role(user_id) COMMENT '用户ID索引';
CREATE INDEX idx_user_role_role_id ON sys_user_role(role_id) COMMENT '角色ID索引';
```

#### 3.3 外键约束

```sql
-- 用户外键
ALTER TABLE sys_user_role
  ADD CONSTRAINT fk_user_role_user
  FOREIGN KEY (user_id)
  REFERENCES auth_user(id)
  ON DELETE CASCADE
  ON UPDATE CASCADE;

-- 角色外键
ALTER TABLE sys_user_role
  ADD CONSTRAINT fk_user_role_role
  FOREIGN KEY (role_id)
  REFERENCES sys_role(id)
  ON DELETE CASCADE
  ON UPDATE CASCADE;
```

---

### 4. 角色权限关联表 (sys_role_permission)

#### 4.1 表结构

```sql
CREATE TABLE sys_role_permission (
  -- 主键
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '关联ID',

  -- 关联信息
  role_id BIGINT NOT NULL COMMENT '角色ID',
  permission_id BIGINT NOT NULL COMMENT '权限ID',

  -- 审计字段
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  
  -- 唯一约束
  UNIQUE KEY uk_role_permission (role_id, permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色权限关联表';
```

#### 4.2 索引设计

```sql
-- 主键索引
-- PRIMARY KEY (id)

-- 唯一索引
-- UNIQUE KEY uk_role_permission (role_id, permission_id)

-- 普通索引
CREATE INDEX idx_role_perm_role_id ON sys_role_permission(role_id) COMMENT '角色ID索引';
CREATE INDEX idx_role_perm_perm_id ON sys_role_permission(permission_id) COMMENT '权限ID索引';
```

#### 4.3 外键约束

```sql
-- 角色外键
ALTER TABLE sys_role_permission
  ADD CONSTRAINT fk_role_perm_role
  FOREIGN KEY (role_id)
  REFERENCES sys_role(id)
  ON DELETE CASCADE
  ON UPDATE CASCADE;

-- 权限外键
ALTER TABLE sys_role_permission
  ADD CONSTRAINT fk_role_perm_perm
  FOREIGN KEY (permission_id)
  REFERENCES sys_permission(id)
  ON DELETE CASCADE
  ON UPDATE CASCADE;
```

---

## 5. 视图设计

### 5.1 用户权限汇总视图 (v_user_permissions)

```sql
CREATE VIEW v_user_permissions AS
SELECT 
  u.id AS user_id,
  u.username,
  r.role_code,
  p.permission_code,
  p.permission_type,
  p.api_path,
  p.api_method,
  p.data_scope
FROM auth_user u
JOIN sys_user_role ur ON u.id = ur.user_id
JOIN sys_role r ON ur.role_id = r.id
JOIN sys_role_permission rp ON r.id = rp.role_id
JOIN sys_permission p ON rp.permission_id = p.id
WHERE u.status = 'active'
  AND r.status = 1
  AND p.status = 1
  AND u.is_deleted = 0
  AND r.deleted_at IS NULL
  AND p.deleted_at IS NULL
  AND (ur.start_time IS NULL OR ur.start_time <= NOW())
  AND (ur.end_time IS NULL OR ur.end_time >= NOW());
```

---

## 6. 初始化数据

### 6.1 系统角色初始化

```sql
INSERT INTO sys_role (role_code, role_name, role_type, description, sort_order, is_preset) VALUES
('ROLE_ADMIN', '超级管理员', 'system', '系统所有权限', 1, 1),
('ROLE_USER', '普通用户', 'system', '基础权限', 2, 1),
('ROLE_HR', '人事专员', 'custom', '员工管理与考勤权限', 3, 0),
('ROLE_FINANCE', '财务专员', 'custom', '报销与资产权限', 4, 0);
```

### 6.2 权限数据示例 (API权限)

```sql
INSERT INTO sys_permission (
  permission_code, permission_name, permission_type, module, 
  api_path, api_method, status
) VALUES
('user:list', '查看用户列表', 'api', 'system', '/api/users', 'GET', 1),
('user:create', '创建用户', 'api', 'system', '/api/users', 'POST', 1),
('user:update', '更新用户', 'api', 'system', '/api/users', 'PUT', 1),
('user:delete', '删除用户', 'api', 'system', '/api/users', 'DELETE', 1);
```

---

**文档版本**: v1.0.0
**创建人**: AI开发助手
**最后更新**: 2026-01-12
