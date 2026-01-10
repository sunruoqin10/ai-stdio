# 权限控制模块 - 技术实现规范

> **文档类型**: 技术实现规范
> **模块类型**: 核心基础
> **技术栈**: Vue 3 + TypeScript + Element Plus + Pinia
> **参考模块**: `src/modules/employee/`
> **创建日期**: 2026-01-10
> **最后更新**: 2026-01-10

---

## 📋 目录

- [1. 数据结构](#1-数据结构)
- [2. API接口](#2-api接口)
- [3. 验证规则](#3-验证规则)
- [4. 算法实现](#4-算法实现)
- [5. 权限验证](#5-权限验证)

---

## 1. 数据结构

### 1.1 TypeScript类型定义

```typescript
/**
 * 角色
 */
interface Role {
  /** 角色编号 - 唯一标识 */
  id: string

  /** 角色名称 */
  name: string

  /** 角色编码 */
  code: string

  /** 角色类型: system-系统角色 custom-自定义角色 */
  type: 'system' | 'custom'

  /** 排序号 */
  sort: number

  /** 角色描述 */
  description?: string

  /** 状态: active-正常 disabled-停用 */
  status: 'active' | 'disabled'

  /** 是否为预置角色(不可删除) */
  isPreset: boolean

  /** 创建时间 */
  createdAt: string

  /** 更新时间 */
  updatedAt: string
}

/**
 * 权限
 */
interface Permission {
  /** 权限编号 - 唯一标识 */
  id: string

  /** 权限名称 */
  name: string

  /** 权限编码 */
  code: string

  /** 权限类型: menu-菜单 button-按钮 api-接口 data-数据 */
  type: 'menu' | 'button' | 'api' | 'data'

  /** 所属模块 */
  module: string

  /** 上级权限ID */
  parentId?: string | null

  /** 路由路径(menu类型) */
  path?: string

  /** 组件路径(menu类型) */
  component?: string

  /** 图标 */
  icon?: string

  /** 接口地址(api类型) */
  apiPath?: string

  /** 请求方式(api类型) */
  apiMethod?: 'GET' | 'POST' | 'PUT' | 'DELETE' | 'PATCH'

  /** 数据范围(data类型): all-全部 dept-本部门 dept_and_sub-本部门及下级 self-本人 */
  dataScope?: 'all' | 'dept' | 'dept_and_sub' | 'self'

  /** 排序号 */
  sort: number

  /** 状态: active-正常 disabled-停用 */
  status: 'active' | 'disabled'

  /** 创建时间 */
  createdAt: string

  /** 更新时间 */
  updatedAt: string

  /** 子权限列表(树形结构时使用) */
  children?: Permission[]
}

/**
 * 用户角色关联
 */
interface UserRole {
  /** 关联ID */
  id: string

  /** 用户ID */
  userId: string

  /** 角色ID */
  roleId: string

  /** 角色信息(关联查询) */
  role?: Role

  /** 开始时间(可选,用于临时角色) */
  startTime?: string

  /** 结束时间(可选,用于临时角色) */
  endTime?: string

  /** 创建时间 */
  createdAt: string
}

/**
 * 角色权限关联
 */
interface RolePermission {
  /** 关联ID */
  id: string

  /** 角色ID */
  roleId: string

  /** 权限ID */
  permissionId: string

  /** 创建时间 */
  createdAt: string
}

/**
 * 角色表单数据
 */
interface RoleForm {
  /** 角色名称 */
  name: string

  /** 角色编码 */
  code: string

  /** 角色类型 */
  type: 'system' | 'custom'

  /** 排序号 */
  sort?: number

  /** 角色描述 */
  description?: string

  /** 权限ID列表 */
  permissionIds?: string[]
}

/**
 * 权限表单数据
 */
interface PermissionForm {
  /** 权限名称 */
  name: string

  /** 权限编码 */
  code: string

  /** 权限类型 */
  type: 'menu' | 'button' | 'api' | 'data'

  /** 所属模块 */
  module: string

  /** 上级权限ID */
  parentId?: string | null

  /** 路由路径 */
  path?: string

  /** 组件路径 */
  component?: string

  /** 图标 */
  icon?: string

  /** 接口地址 */
  apiPath?: string

  /** 请求方式 */
  apiMethod?: 'GET' | 'POST' | 'PUT' | 'DELETE' | 'PATCH'

  /** 数据范围 */
  dataScope?: 'all' | 'dept' | 'dept_and_sub' | 'self'

  /** 排序号 */
  sort?: number
}

/**
 * 用户角色分配表单
 */
interface AssignRoleForm {
  /** 用户ID */
  userId: string

  /** 角色ID列表 */
  roleIds: string[]
}

/**
 * 权限检查结果
 */
interface PermissionCheck {
  /** 是否有权限 */
  hasPermission: boolean

  /** 权限来源角色 */
  roles?: string[]
}

/**
 * 用户权限汇总
 */
interface UserPermissions {
  /** 用户ID */
  userId: string

  /** 用户角色列表 */
  roles: Role[]

  /** 权限列表(去重) */
  permissions: Permission[]

  /** 权限码集合(用于快速判断) */
  permissionCodes: Set<string>

  /** 菜单权限树 */
  menuPermissions: Permission[]

  /** 按钮权限码 */
  buttonPermissions: string[]

  /** API权限码 */
  apiPermissions: string[]

  /** 数据权限 */
  dataPermissions: Map<string, string>
}
```

### 1.2 字段说明

#### 角色表(roles)

| 字段名 | 类型 | 必填 | 默认值 | 说明 | 示例 |
|-------|------|------|--------|------|------|
| id | string | ✅ | - | 唯一标识,格式: ROLE+序号 | ROLE0001 |
| name | string | ✅ | - | 角色名称 | 系统管理员 |
| code | string | ✅ | - | 角色编码(唯一) | admin |
| type | string | ✅ | custom | 类型: system系统角色, custom自定义 | system |
| sort | number | ❌ | 0 | 排序号 | 1 |
| description | string | ❌ | - | 角色描述 | 拥有所有权限 |
| status | string | ✅ | active | 状态: active正常, disabled停用 | active |
| isPreset | boolean | ✅ | false | 是否预置角色 | true |
| createdAt | string | ✅ | - | 创建时间 | 2026-01-10 10:00:00 |
| updatedAt | string | ✅ | - | 更新时间 | 2026-01-10 10:00:00 |

#### 权限表(permissions)

| 字段名 | 类型 | 必填 | 默认值 | 说明 | 示例 |
|-------|------|------|--------|------|------|
| id | string | ✅ | - | 唯一标识,格式: PERM+序号 | PERM0001 |
| name | string | ✅ | - | 权限名称 | 员工管理 |
| code | string | ✅ | - | 权限编码(唯一) | employee:view:list |
| type | string | ✅ | - | 类型: menu/button/api/data | menu |
| module | string | ✅ | - | 所属模块 | employee |
| parentId | string \| null | ❌ | null | 上级权限ID | PERM0001 |
| path | string | ❌ | - | 路由路径 | /employee |
| component | string | ❌ | - | 组件路径 | @/views/employee/List |
| icon | string | ❌ | - | 图标 | User |
| apiPath | string | ❌ | - | 接口地址 | /api/employees |
| apiMethod | string | ❌ | - | 请求方式 | GET |
| dataScope | string | ❌ | - | 数据范围 | all |
| sort | number | ❌ | 0 | 排序号 | 1 |
| status | string | ✅ | active | 状态: active正常, disabled停用 | active |
| createdAt | string | ✅ | - | 创建时间 | 2026-01-10 10:00:00 |
| updatedAt | string | ✅ | - | 更新时间 | 2026-01-10 10:00:00 |

### 1.3 枚举类型

```typescript
/**
 * 角色类型枚举
 */
enum RoleType {
  SYSTEM = 'system',      // 系统角色(预置)
  CUSTOM = 'custom'       // 自定义角色
}

/**
 * 权限类型枚举
 */
enum PermissionType {
  MENU = 'menu',          // 菜单权限
  BUTTON = 'button',      // 按钮权限
  API = 'api',           // 接口权限
  DATA = 'data'          // 数据权限
}

/**
 * 数据范围枚举
 */
enum DataScope {
  ALL = 'all',                    // 全部数据
  DEPT = 'dept',                  // 本部门数据
  DEPT_AND_SUB = 'dept_and_sub',  // 本部门及下级部门数据
  SELF = 'self'                   // 仅本人数据
}
```

---

## 2. API接口

### 2.1 角色管理接口

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | /api/roles | 获取角色列表 | 系统管理员 |
| GET | /api/roles/:id | 获取角色详情 | 系统管理员 |
| POST | /api/roles | 创建角色 | 系统管理员 |
| PUT | /api/roles/:id | 更新角色信息 | 系统管理员 |
| DELETE | /api/roles/:id | 删除角色 | 系统管理员 |
| GET | /api/roles/:id/permissions | 获取角色权限 | 系统管理员 |
| PUT | /api/roles/:id/permissions | 更新角色权限 | 系统管理员 |
| GET | /api/roles/:id/users | 获取角色成员 | 系统管理员 |
| POST | /api/roles/copy | 复制角色 | 系统管理员 |

### 2.2 权限管理接口

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | /api/permissions | 获取权限列表(树形) | 系统管理员 |
| GET | /api/permissions/:id | 获取权限详情 | 系统管理员 |
| POST | /api/permissions | 创建权限 | 系统管理员 |
| PUT | /api/permissions/:id | 更新权限信息 | 系统管理员 |
| DELETE | /api/permissions/:id | 删除权限 | 系统管理员 |
| GET | /api/permissions/tree | 获取权限树 | 系统管理员 |
| GET | /api/permissions/modules | 获取所有模块 | 系统管理员 |

### 2.3 用户角色接口

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | /api/users/:userId/roles | 获取用户角色 | 系统管理员 |
| POST | /api/users/:userId/roles | 分配用户角色 | 系统管理员 |
| DELETE | /api/users/:userId/roles/:roleId | 移除用户角色 | 系统管理员 |
| GET | /api/users/:userId/permissions | 获取用户所有权限 | 登录用户 |

### 2.4 请求/响应示例

#### 2.4.1 获取角色列表

**请求**:
```typescript
GET /api/roles?status=active&type=custom
```

**响应**:
```typescript
interface RoleListResponse {
  code: number
  message: string
  data: {
    list: Role[]
    total: number
  }
}
```

#### 2.4.2 获取权限树

**请求**:
```typescript
GET /api/permissions/tree?type=menu
```

**响应**:
```typescript
interface PermissionTreeResponse {
  code: number
  message: string
  data: Permission[]  // 树形结构
}
```

#### 2.4.3 创建角色

**请求**:
```typescript
POST /api/roles
{
  "name": "部门管理员",
  "code": "dept_admin",
  "type": "custom",
  "sort": 2,
  "description": "管理本部门相关业务",
  "permissionIds": ["PERM0001", "PERM0002"]
}
```

**响应**:
```typescript
interface CreateRoleResponse {
  code: number
  message: string
  data: {
    id: string  // 新创建的角色ID
  }
}
```

#### 2.4.4 更新角色权限

**请求**:
```typescript
PUT /api/roles/:id/permissions
{
  "permissionIds": ["PERM0001", "PERM0002", "PERM0003"]
}
```

**响应**:
```typescript
interface UpdateRolePermissionsResponse {
  code: number
  message: string
  data: Role
}
```

#### 2.4.5 分配用户角色

**请求**:
```typescript
POST /api/users/:userId/roles
{
  "roleIds": ["ROLE0001", "ROLE0002"]
}
```

**响应**:
```typescript
interface AssignRolesResponse {
  code: number
  message: string
}
```

#### 2.4.6 获取用户权限

**请求**:
```typescript
GET /api/users/:userId/permissions
```

**响应**:
```typescript
interface UserPermissionsResponse {
  code: number
  message: string
  data: {
    roles: Role[]
    permissions: Permission[]
    permissionCodes: string[]
    menuPermissions: Permission[]
  }
}
```

### 2.5 API实现要求

```typescript
// src/modules/permission/api/index.ts
import request from '@/utils/request'
import type {
  Role,
  Permission,
  RoleForm,
  PermissionForm,
  AssignRoleForm,
  UserPermissions
} from '../types'

/**
 * 获取角色列表
 */
export function getRoleList(params?: { status?: string; type?: string }) {
  return request.get<{
    list: Role[]
    total: number
  }>('/api/roles', { params })
}

/**
 * 获取角色详情
 */
export function getRoleDetail(id: string) {
  return request.get<Role>(`/api/roles/${id}`)
}

/**
 * 创建角色
 */
export function createRole(data: RoleForm) {
  return request.post<{ id: string }>('/api/roles', data)
}

/**
 * 更新角色
 */
export function updateRole(id: string, data: Partial<RoleForm>) {
  return request.put<Role>(`/api/roles/${id}`, data)
}

/**
 * 删除角色
 */
export function deleteRole(id: string) {
  return request.delete(`/api/roles/${id}`)
}

/**
 * 获取角色权限
 */
export function getRolePermissions(id: string) {
  return request.get<Permission[]>(`/api/roles/${id}/permissions`)
}

/**
 * 更新角色权限
 */
export function updateRolePermissions(id: string, permissionIds: string[]) {
  return request.put(`/api/roles/${id}/permissions`, { permissionIds })
}

/**
 * 获取角色成员
 */
export function getRoleUsers(id: string) {
  return request.get(`/api/roles/${id}/users`)
}

/**
 * 复制角色
 */
export function copyRole(id: string, name: string) {
  return request.post<{ id: string }>('/api/roles/copy', { sourceId: id, name })
}

/**
 * 获取权限树
 */
export function getPermissionTree(params?: { type?: string }) {
  return request.get<Permission[]>('/api/permissions/tree', { params })
}

/**
 * 获取权限列表
 */
export function getPermissionList(params?: { module?: string; type?: string }) {
  return request.get<{
    list: Permission[]
    total: number
  }>('/api/permissions', { params })
}

/**
 * 创建权限
 */
export function createPermission(data: PermissionForm) {
  return request.post<{ id: string }>('/api/permissions', data)
}

/**
 * 更新权限
 */
export function updatePermission(id: string, data: Partial<PermissionForm>) {
  return request.put<Permission>(`/api/permissions/${id}`, data)
}

/**
 * 删除权限
 */
export function deletePermission(id: string) {
  return request.delete(`/api/permissions/${id}`)
}

/**
 * 获取所有模块
 */
export function getPermissionModules() {
  return request.get<string[]>('/api/permissions/modules')
}

/**
 * 获取用户角色
 */
export function getUserRoles(userId: string) {
  return request.get<Role[]>(`/api/users/${userId}/roles`)
}

/**
 * 分配用户角色
 */
export function assignUserRoles(userId: string, data: AssignRoleForm) {
  return request.post(`/api/users/${userId}/roles`, data)
}

/**
 * 移除用户角色
 */
export function removeUserRole(userId: string, roleId: string) {
  return request.delete(`/api/users/${userId}/roles/${roleId}`)
}

/**
 * 获取用户权限
 */
export function getUserPermissions(userId: string): Promise<UserPermissions> {
  return request.get<UserPermissions>(`/api/users/${userId}/permissions`).then(res => res.data)
}
```

---

## 3. 验证规则

### 3.1 前端验证

#### 3.1.1 角色表单验证

```typescript
// src/modules/permission/components/RoleForm.vue
const rules = {
  name: [
    { required: true, message: '请输入角色名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' },
    {
      validator: async (rule: any, value: string, callback: any) => {
        // 异步验证角色名称唯一性
        if (value && value !== originalName.value) {
          const exists = await checkRoleNameExists(value)
          if (exists) {
            callback(new Error('角色名称已存在'))
          } else {
            callback()
          }
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  code: [
    { required: true, message: '请输入角色编码', trigger: 'blur' },
    {
      pattern: /^[a-zA-Z0-9_]+$/,
      message: '只能包含字母、数字和下划线',
      trigger: 'blur'
    },
    {
      validator: async (rule: any, value: string, callback: any) => {
        // 验证角色编码唯一性
        if (value && value !== originalCode.value) {
          const exists = await checkRoleCodeExists(value)
          if (exists) {
            callback(new Error('角色编码已存在'))
          } else {
            callback()
          }
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  type: [
    { required: true, message: '请选择角色类型', trigger: 'change' }
  ],
  sort: [
    { type: 'number', min: 0, message: '排序号必须大于等于0', trigger: 'blur' }
  ]
}
```

#### 3.1.2 权限表单验证

```typescript
const permissionRules = {
  name: [
    { required: true, message: '请输入权限名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入权限编码', trigger: 'blur' },
    {
      pattern: /^[a-z0-9:_]+$/,
      message: '格式: 模块:操作:对象,如 employee:view:list',
      trigger: 'blur'
    },
    {
      validator: async (rule: any, value: string, callback: any) => {
        if (value && value !== originalCode.value) {
          const exists = await checkPermissionCodeExists(value)
          if (exists) {
            callback(new Error('权限编码已存在'))
          } else {
            callback()
          }
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  type: [
    { required: true, message: '请选择权限类型', trigger: 'change' }
  ],
  module: [
    { required: true, message: '请选择所属模块', trigger: 'change' }
  ],
  apiPath: [
    {
      validator: (rule: any, value: string, callback: any) => {
        // API类型权限必须填写接口地址
        if (formData.type === 'api' && !value) {
          callback(new Error('请输入接口地址'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  dataScope: [
    {
      validator: (rule: any, value: string, callback: any) => {
        // 数据权限必须选择数据范围
        if (formData.type === 'data' && !value) {
          callback(new Error('请选择数据范围'))
        } else {
          callback()
        }
      },
      trigger: 'change'
    }
  ]
}
```

### 3.2 后端验证

- [ ] 角色名称唯一性验证
- [ ] 角色编码唯一性验证(格式:字母数字下划线)
- [ ] 权限编码唯一性验证(格式:模块:操作:对象)
- [ ] 系统预置角色不可删除
- [ ] 有成员的角色不可删除
- [ ] 被引用的权限不可删除
- [ ] 权限编码格式验证
- [ ] 角色分配验证(不能重复分配)

---

## 4. 算法实现

### 4.1 权限树构建

```typescript
// src/utils/permission.ts
/**
 * 扁平权限列表转树形结构
 */
export function buildPermissionTree(
  permissions: Permission[],
  options?: {
    rootId?: string | null
    type?: PermissionType
  }
): Permission[] {
  const { rootId = null, type } = options || {}

  // 过滤类型
  let filtered = permissions
  if (type) {
    filtered = permissions.filter(p => p.type === type)
  }

  // 构建树形结构
  const map = new Map<string, Permission>()
  const roots: Permission[] = []

  // 先建立映射
  filtered.forEach(item => {
    map.set(item.id, { ...item, children: [] })
  })

  // 建立树形关系
  filtered.forEach(item => {
    const node = map.get(item.id)!
    if (item.parentId === rootId) {
      roots.push(node)
    } else {
      const parent = map.get(item.parentId!)
      if (parent) {
        if (!parent.children) parent.children = []
        parent.children.push(node)
      }
    }
  })

  return roots
}

/**
 * 获取所有子权限ID(递归)
 */
export function getAllPermissionIds(permissionId: string, permissions: Permission[]): string[] {
  const ids: string[] = [permissionId]
  const children = permissions.filter(p => p.parentId === permissionId)

  children.forEach(child => {
    ids.push(...getAllPermissionIds(child.id, permissions))
  })

  return ids
}
```

### 4.2 权限验证算法

```typescript
/**
 * 检查用户是否有指定权限
 */
export function hasPermission(
  userPermissions: UserPermissions,
  permissionCode: string
): boolean {
  return userPermissions.permissionCodes.has(permissionCode)
}

/**
 * 检查用户是否有任意一个权限
 */
export function hasAnyPermission(
  userPermissions: UserPermissions,
  permissionCodes: string[]
): boolean {
  return permissionCodes.some(code => hasPermission(userPermissions, code))
}

/**
 * 检查用户是否有所有权限
 */
export function hasAllPermissions(
  userPermissions: UserPermissions,
  permissionCodes: string[]
): boolean {
  return permissionCodes.every(code => hasPermission(userPermissions, code))
}

/**
 * 检查用户是否有指定角色
 */
export function hasRole(userPermissions: UserPermissions, roleCode: string): boolean {
  return userPermissions.roles.some(role => role.code === roleCode)
}

/**
 * 检查用户是否有任意一个角色
 */
export function hasAnyRole(userPermissions: UserPermissions, roleCodes: string[]): boolean {
  return roleCodes.some(code => hasRole(userPermissions, code))
}

/**
 * 过滤菜单权限(根据用户权限)
 */
export function filterMenuPermissions(
  allMenus: Permission[],
  userPermissions: UserPermissions
): Permission[] {
  const result: Permission[] = []

  function traverse(menus: Permission[]) {
    menus.forEach(menu => {
      // 检查是否有该菜单权限
      if (hasPermission(userPermissions, menu.code)) {
        const filteredMenu = { ...menu }
        if (menu.children && menu.children.length > 0) {
          // 递归过滤子菜单
          const filteredChildren = filterMenuPermissions(menu.children, userPermissions)
          if (filteredChildren.length > 0) {
            filteredMenu.children = filteredChildren
          }
        }
        result.push(filteredMenu)
      }
    })
  }

  traverse(allMenus)
  return result
}

/**
 * 过滤数据权限(根据用户数据范围)
 */
export function filterDataByScope<T extends { departmentId?: string; userId?: string }>(
  data: T[],
  userPermissions: UserPermissions,
  userDeptId: string,
  userId: string
): T[] {
  // 获取用户最大数据权限
  const maxScope = getMaxDataScope(userPermissions)

  switch (maxScope) {
    case 'all':
      // 全部数据
      return data

    case 'dept_and_sub':
      // 本部门及下级部门数据
      const subDeptIds = getSubDepartmentIds(userDeptId)
      return data.filter(item =>
        item.departmentId && subDeptIds.includes(item.departmentId)
      )

    case 'dept':
      // 本部门数据
      return data.filter(item => item.departmentId === userDeptId)

    case 'self':
      // 仅本人数据
      return data.filter(item => item.userId === userId)

    default:
      return []
  }
}

/**
 * 获取用户最大数据权限范围
 */
function getMaxDataScope(userPermissions: UserPermissions): DataScope {
  const scopes = userPermissions.dataPermissions.values()

  // 优先级: all > dept_and_sub > dept > self
  if (Array.from(scopes).includes('all')) return 'all'
  if (Array.from(scopes).includes('dept_and_sub')) return 'dept_and_sub'
  if (Array.from(scopes).includes('dept')) return 'dept'
  return 'self'
}

/**
 * 获取下级部门ID列表(递归)
 */
function getSubDepartmentIds(deptId: string): string[] {
  // TODO: 实现获取下级部门逻辑
  // 需要调用部门管理模块的API
  return [deptId]
}
```

### 4.3 角色权限分配

```typescript
/**
 * 分配角色权限
 */
export async function assignRolePermissions(
  roleId: string,
  permissionIds: string[]
): Promise<void> {
  // 1. 获取所有子权限ID(包含父权限的所有子权限)
  const allPermissionIds = new Set<string>()

  for (const pid of permissionIds) {
    const childIds = await getAllPermissionIds(pid, await getAllPermissions())
    childIds.forEach(id => allPermissionIds.add(id))
  }

  // 2. 删除旧的角色权限关联
  await deleteRolePermissions(roleId)

  // 3. 批量插入新的权限关联
  await insertRolePermissions(roleId, Array.from(allPermissionIds))

  // 4. 清除相关用户的权限缓存
  const users = await getUsersByRole(roleId)
  for (const user of users) {
    await clearUserPermissionCache(user.id)
  }
}
```

---

## 5. 权限验证

### 5.1 路由级权限验证

```typescript
// src/router/permission.ts
import router from './index'
import { useUserStore } from '@/stores/user'
import { usePermissionStore } from '@/stores/permission'

// 白名单路由(不需要权限)
const whiteList = ['/login', '/404', '/403']

router.beforeEach(async (to, from, next) => {
  const userStore = useUserStore()
  const permissionStore = usePermissionStore()

  // 检查是否登录
  if (userStore.isLoggedIn) {
    if (to.path === '/login') {
      // 已登录,跳转到首页
      next({ path: '/' })
    } else {
      // 检查是否已加载权限
      if (!permissionStore.hasLoadedPermissions) {
        try {
          // 加载用户权限
          await permissionStore.loadUserPermissions()

          // 动态生成可访问路由
          const accessRoutes = await permissionStore.generateRoutes()

          // 添加路由
          accessRoutes.forEach(route => {
            router.addRoute(route)
          })

          // 确保addRoute完成后跳转
          next({ ...to, replace: true })
        } catch (error) {
          // 权限加载失败,退出登录
          await userStore.logout()
          next(`/login?redirect=${to.path}`)
        }
      } else {
        // 检查路由权限
        if (hasRoutePermission(to, permissionStore.permissions)) {
          next()
        } else {
          // 无权限,跳转到403
          next('/403')
        }
      }
    }
  } else {
    // 未登录
    if (whiteList.includes(to.path)) {
      // 在白名单中,直接放行
      next()
    } else {
      // 不在白名单,跳转到登录页
      next(`/login?redirect=${to.path}`)
    }
  }
})

/**
 * 检查是否有路由访问权限
 */
function hasRoutePermission(route: Route, permissions: UserPermissions): boolean {
  // 路由未配置权限,默认允许
  if (!route.meta?.permission) {
    return true
  }

  // 检查是否有权限
  return hasPermission(permissions, route.meta.permission)
}
```

### 5.2 按钮级权限控制

```typescript
// src/directives/auth.ts
import type { Directive, DirectiveBinding } from 'vue'
import { usePermissionStore } from '@/stores/permission'

/**
 * 权限指令 v-auth
 * 用法: v-auth="'employee:add'" 或 v-auth="['employee:add', 'employee:edit']"
 */
export const auth: Directive = {
  mounted(el: HTMLElement, binding: DirectiveBinding) {
    const { value } = binding
    const permissionStore = usePermissionStore()

    if (value) {
      const permissionCodes = Array.isArray(value) ? value : [value]
      const hasAuth = hasAnyPermission(permissionStore.userPermissions, permissionCodes)

      if (!hasAuth) {
        // 无权限,移除元素
        el.parentNode?.removeChild(el)
      }
    } else {
      throw new Error('权限指令需要指定权限码,如 v-auth="\'permission.code\'"')
    }
  }
}

/**
 * 注册指令
 */
export function setupAuthDirective(app: App) {
  app.directive('auth', auth)
}
```

### 5.3 Pinia Store实现

```typescript
// src/stores/permission.ts
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { UserPermissions, Permission } from '@/modules/permission/types'
import * as permissionApi from '@/modules/permission/api'
import { buildPermissionTree, filterMenuPermissions, hasPermission } from '@/utils/permission'

export const usePermissionStore = defineStore('permission', () => {
  // 状态
  const userPermissions = ref<UserPermissions>({
    userId: '',
    roles: [],
    permissions: [],
    permissionCodes: new Set(),
    menuPermissions: [],
    buttonPermissions: [],
    apiPermissions: [],
    dataPermissions: new Map()
  })

  const hasLoadedPermissions = ref(false)
  const allPermissions = ref<Permission[]>([])

  // 计算属性
  const menuTree = computed(() => {
    return buildPermissionTree(userPermissions.value.menuPermissions, { type: 'menu' })
  })

  const permissionCodes = computed(() => {
    return Array.from(userPermissions.value.permissionCodes)
  })

  // 方法
  async function loadUserPermissions() {
    const userId = localStorage.getItem('userId')
    if (!userId) {
      throw new Error('用户未登录')
    }

    const data = await permissionApi.getUserPermissions(userId)

    userPermissions.value = {
      ...data,
      permissionCodes: new Set(data.permissions.map(p => p.code)),
      buttonPermissions: data.permissions
        .filter(p => p.type === 'button')
        .map(p => p.code),
      apiPermissions: data.permissions
        .filter(p => p.type === 'api')
        .map(p => p.code),
      dataPermissions: new Map(
        data.permissions
          .filter(p => p.type === 'data')
          .map(p => [p.module, p.dataScope || 'self'])
      )
    }

    hasLoadedPermissions.value = true
  }

  async function loadAllPermissions() {
    const data = await permissionApi.getPermissionTree()
    allPermissions.value = data
  }

  async function generateRoutes() {
    // 获取所有菜单权限
    await loadAllPermissions()

    // 根据用户权限过滤菜单
    const filteredMenus = filterMenuPermissions(
      allPermissions.value,
      userPermissions.value
    )

    // 将权限转换为路由配置
    return convertPermissionsToRoutes(filteredMenus)
  }

  function checkPermission(code: string): boolean {
    return hasPermission(userPermissions.value, code)
  }

  function checkAnyPermission(codes: string[]): boolean {
    return codes.some(code => checkPermission(code))
  }

  function checkRole(roleCode: string): boolean {
    return userPermissions.value.roles.some(r => r.code === roleCode)
  }

  function clearPermissions() {
    userPermissions.value = {
      userId: '',
      roles: [],
      permissions: [],
      permissionCodes: new Set(),
      menuPermissions: [],
      buttonPermissions: [],
      apiPermissions: [],
      dataPermissions: new Map()
    }
    hasLoadedPermissions.value = false
  }

  return {
    // 状态
    userPermissions,
    hasLoadedPermissions,
    allPermissions,
    // 计算属性
    menuTree,
    permissionCodes,
    // 方法
    loadUserPermissions,
    loadAllPermissions,
    generateRoutes,
    checkPermission,
    checkAnyPermission,
    checkRole,
    clearPermissions
  }
})

/**
 * 将权限转换为路由配置
 */
function convertPermissionsToRoutes(permissions: Permission[]): RouteRecordRaw[] {
  const routes: RouteRecordRaw[] = []

  permissions.forEach(perm => {
    if (perm.type === 'menu' && perm.path) {
      const route: RouteRecordRaw = {
        path: perm.path,
        name: perm.code,
        component: () => import(/* @vite-ignore */ perm.component || ''),
        meta: {
          title: perm.name,
          icon: perm.icon,
          permission: perm.code,
          hidden: false
        }
      }

      if (perm.children && perm.children.length > 0) {
        route.children = convertPermissionsToRoutes(perm.children)
      }

      routes.push(route)
    }
  })

  return routes
}
```

### 5.4 后端权限验证中间件

```typescript
// src/middleware/permission.ts
import { Request, Response, NextFunction } from 'express'
import { getUserPermissions } from '../modules/permission/api'

/**
 * 权限验证中间件工厂函数
 * @param permissionCode 所需权限码
 * @param needLogin 是否需要登录(默认true)
 */
export function requirePermission(permissionCode?: string, needLogin = true) {
  return async (req: Request, res: Response, next: NextFunction) => {
    try {
      // 获取用户ID
      const userId = req.user?.id
      if (!userId) {
        if (needLogin) {
          return res.status(401).json({
            code: 401,
            message: '未登录或登录已过期'
          })
        } else {
          // 不需要登录,直接放行
          return next()
        }
      }

      // 获取用户权限
      const userPermissions = await getUserPermissions(userId)

      // 如果指定了权限码,检查权限
      if (permissionCode) {
        const hasPermission = userPermissions.permissionCodes.includes(permissionCode)

        if (!hasPermission) {
          return res.status(403).json({
            code: 403,
            message: '无权限访问'
          })
        }
      }

      // 权限验证通过,继续处理请求
      next()
    } catch (error) {
      console.error('权限验证错误:', error)
      res.status(500).json({
        code: 500,
        message: '权限验证失败'
      })
    }
  }
}

/**
 * 使用示例
 */
// app.get('/api/employees', requirePermission('employee:view:list'), employeeController.getList)
// app.post('/api/employees', requirePermission('employee:add'), employeeController.create)
```

---

## 6. 性能优化

### 6.1 权限缓存

```typescript
// 权限缓存类
class PermissionCache {
  private cache: Map<string, { data: any; expired: number }> = new Map()
  private defaultTTL = 30 * 60 * 1000 // 30分钟

  set(key: string, value: any, ttl?: number) {
    this.cache.set(key, {
      data: value,
      expired: Date.now() + (ttl || this.defaultTTL)
    })
  }

  get(key: string) {
    const item = this.cache.get(key)
    if (!item) return null

    if (Date.now() > item.expired) {
      this.cache.delete(key)
      return null
    }

    return item.data
  }

  delete(key: string) {
    this.cache.delete(key)
  }

  clear() {
    this.cache.clear()
  }
}

export const permissionCache = new PermissionCache()
```

### 6.2 权限预加载

```typescript
// 预加载常用权限
export async function preloadPermissions() {
  // 预加载所有权限树
  const allPermissions = await getPermissionTree()
  permissionCache.set('all_permissions', allPermissions)

  // 预加载模块列表
  const modules = await getPermissionModules()
  permissionCache.set('permission_modules', modules)
}
```

---

**文档版本**: v1.0.0
**创建人**: AI开发助手
**最后更新**: 2026-01-10
