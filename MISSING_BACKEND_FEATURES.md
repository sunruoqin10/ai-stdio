# 待实现功能清单

本文档列出前端已实现但后端尚未实现的功能。

---

## 一、缺少后端模块（完全未实现）

### 1. 会议模块 (meeting) - 严重

**状态**: 前端使用 Mock 数据，后端完全缺失

**功能描述**: 会议室预定系统，包括会议室管理、预订、审批等功能

**缺失 API 接口**:

| 功能 | 接口 | 说明 |
|------|------|------|
| 会议室管理 | `GET /api/meeting/rooms` | 获取会议室列表 |
| | `GET /api/meeting/rooms/{id}` | 获取会议室详情 |
| | `POST /api/meeting/rooms` | 创建会议室 |
| | `PUT /api/meeting/rooms/{id}` | 更新会议室 |
| | `DELETE /api/meeting/rooms/{id}` | 删除会议室 |
| 预订管理 | `GET /api/meeting/bookings` | 获取预订列表 |
| | `GET /api/meeting/bookings/{id}` | 获取预订详情 |
| | `POST /api/meeting/bookings` | 创建预订 |
| | `PUT /api/meeting/bookings/{id}` | 更新预订 |
| | `POST /api/meeting/bookings/{id}/cancel` | 取消预订 |
| 审批管理 | `GET /api/meeting/approvals/pending` | 获取待审批列表 |
| | `POST /api/meeting/approvals/{id}` | 审批预订 |
| 签到功能 | `POST /api/meeting/bookings/{id}/check-in` | 会议签到 |
| | `POST /api/meeting/bookings/{id}/check-out` | 会议签退 |
| | `POST /api/meeting/bookings/{id}/rating` | 评分 |
| 冲突检测 | `GET /api/meeting/rooms/{id}/availability` | 检查会议室可用性 |
| | `POST /api/meeting/bookings/check-conflicts` | 检查时间冲突 |
| 统计分析 | `GET /api/meeting/statistics/rooms` | 会议室使用统计 |
| | `GET /api/meeting/statistics/departments` | 部门使用统计 |
| | `GET /api/meeting/statistics/time-slots` | 时段统计 |
| | `GET /api/meeting/statistics/monthly` | 月度统计 |
| 日历功能 | `GET /api/meeting/calendar/events` | 获取日历事件 |
| | `GET /api/meeting/calendar/resources` | 获取日历资源 |
| 通知功能 | `GET /api/meeting/notifications` | 获取通知 |
| | `PUT /api/meeting/notifications/{id}/read` | 标记已读 |
| | `POST /api/meeting/reminders/send` | 发送会议提醒 |

---

### 2. 菜单模块 (menu) - 严重

**状态**: 前端使用 Mock 适配器，后端完全缺失

**功能描述**: 前端动态菜单管理，支持菜单的增删改查和路由配置

**缺失 API 接口**:

| 功能 | 接口 | 说明 |
|------|------|------|
| 菜单管理 | `GET /api/menus` | 获取菜单列表（树形） |
| | `GET /api/menus/{id}` | 获取菜单详情 |
| | `POST /api/menus` | 创建菜单 |
| | `PUT /api/menus/{id}` | 更新菜单 |
| | `DELETE /api/menus/{id}` | 删除菜单 |
| | `GET /api/menus/parent-options` | 获取父菜单选项 |
| 状态管理 | `PATCH /api/menus/{id}/status` | 启用/禁用菜单 |
| 路由 | `GET /api/menus/routes` | 获取菜单路由 |

**建议的数据结构**:

```java
// 实体: Menu
- id: Long
- name: String
- path: String
- component: String
- parentId: Long
- icon: String
- sort: Integer
- permission: String
- type: Enum(menu/button)
- status: Enum(active/disabled)
- visible: Boolean
```

---

### 3. 权限模块 (permission) - 严重

**状态**: 前端期望调用真实 API，后端完全缺失

**功能描述**: 角色管理和权限分配，支持角色 CRUD、权限树、用户角色分配

**缺失 API 接口**:

| 功能 | 接口 | 说明 |
|------|------|------|
| 角色管理 | `GET /api/roles` | 角色列表 |
| | `GET /api/roles/{id}` | 角色详情 |
| | `POST /api/roles` | 创建角色 |
| | `PUT /api/roles/{id}` | 更新角色 |
| | `DELETE /api/roles/{id}` | 删除角色 |
| | `POST /api/roles/copy` | 复制角色 |
| | `GET /api/roles/statistics` | 角色统计 |
| 角色权限 | `GET /api/roles/{id}/permissions` | 获取角色权限 |
| | `PUT /api/roles/{id}/permissions` | 更新角色权限 |
| 角色成员 | `GET /api/roles/{id}/users` | 获取角色成员 |
| 权限管理 | `GET /api/permissions/tree` | 权限树 |
| | `GET /api/permissions` | 权限列表 |
| | `GET /api/permissions/{id}` | 权限详情 |
| | `POST /api/permissions` | 创建权限 |
| | `PUT /api/permissions/{id}` | 更新权限 |
| | `DELETE /api/permissions/{id}` | 删除权限 |
| | `GET /api/permissions/modules` | 获取所有模块 |
| | `GET /api/permissions/statistics` | 权限统计 |
| 用户角色 | `GET /api/users/{userId}/roles` | 获取用户角色 |
| | `POST /api/users/{userId}/roles` | 分配用户角色 |
| | `DELETE /api/users/{userId}/roles/{roleId}` | 移除用户角色 |
| | `GET /api/users/{userId}/permissions` | 获取用户权限 |

**建议的数据结构**:

```java
// 实体: Role
- id: Long
- name: String
- code: String (unique)
- description: String
- status: Enum(active/disabled)
- sort: Integer

// 实体: Permission
- id: Long
- name: String
- code: String (unique)
- type: Enum(menu/operation)
- parentId: Long
- path: String
- component: String
- icon: String
- sort: Integer
- status: Enum(active/disabled)

// 关联表: role_permission, user_role
```

---

## 二、现有模块缺少的接口

### 1. 认证模块 (auth)

| 缺失接口 | 功能说明 |
|----------|----------|
| `POST /api/auth/change-password` | 登录用户修改密码 |

---

### 2. 部门模块 (department)

| 缺失接口 | 功能说明 |
|----------|----------|
| `GET /api/departments/export` | 导出部门列表 |

---

### 3. 资产模块 (asset)

| 缺失接口 | 功能说明 |
|----------|----------|
| `GET /api/assets/depreciation-trend` | 资产折旧趋势统计 |
| `GET /api/assets/borrow-trend` | 资产借用趋势统计 |

---

### 4. 字典模块 (dict)

| 缺失接口 | 功能说明 |
|----------|----------|
| `POST /api/dict/import` | 导入字典数据 |
| `GET /api/dict/export` | 导出字典数据 |

---

### 5. 员工模块 (employee)

| 缺失接口 | 功能说明 |
|----------|----------|
| `POST /api/employees/import` | 批量导入员工（前端已注释） |
| `GET /api/employees/export` | 导出员工列表（前端已注释） |

---

## 三、优先级汇总

| 优先级 | 模块/功能 | 说明 |
|--------|-----------|------|
| 🔴 高 | meeting | 完整模块缺失，约25+接口 |
| 🔴 高 | menu | 完整模块缺失，约8接口 |
| 🔴 高 | permission | 完整模块缺失，约22接口 |
| 🟡 中 | auth | 缺少修改密码接口 |
| 🟡 中 | department | 缺少导出功能 |
| 🟡 中 | dict | 缺少导入导出 |
| 🟡 中 | employee | 缺少导入导出（前端已注释） |
| 🟢 低 | asset | 缺少统计接口 |

---

## 四、实现建议

### 建议一：按优先级实现

1. **permission 模块** - 因为它影响系统安全，建议最先实现
2. **meeting 模块** - 功能独立，可并行开发
3. **menu 模块** - 可使用动态菜单框架

### 建议二：按依赖关系实现

```
1. permission (角色权限)
   ↓ 依赖
2. menu (菜单需要关联权限)
   ↓ 依赖
3. meeting (会议审批需要权限)
```

### 建议三：使用现有模式

参考已实现的 `leave` 和 `expense` 模块的结构：
- controller/service/mapper/entity/dto/vo 分层
- 使用 `@PreAuthorize` 做权限控制
- 使用乐观锁 `@Version`
- 使用逻辑删除 `@TableLogic`
