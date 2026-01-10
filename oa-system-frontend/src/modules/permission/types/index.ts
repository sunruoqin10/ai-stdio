/**
 * 权限控制模块 - TypeScript 类型定义
 * @module permission/types
 */

/**
 * 角色
 */
export interface Role {
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

  /** 成员数量(虚拟字段) */
  userCount?: number

  /** 创建时间 */
  createdAt: string

  /** 更新时间 */
  updatedAt: string
}

/**
 * 权限
 */
export interface Permission {
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
export interface UserRole {
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
export interface RolePermission {
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
export interface RoleForm {
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
 * 权限表单数据 */
export interface PermissionForm {
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
export interface AssignRoleForm {
  /** 用户ID */
  userId: string

  /** 角色ID列表 */
  roleIds: string[]
}

/**
 * 权限检查结果
 */
export interface PermissionCheck {
  /** 是否有权限 */
  hasPermission: boolean

  /** 权限来源角色 */
  roles?: string[]
}

/**
 * 用户权限汇总
 */
export interface UserPermissions {
  /** 用户ID */
  userId: string

  /** 用户角色列表 */
  roles: Role[]

  /** 权限列表(去重) */
  permissions: Permission[]

  /** 权限码集合(用于快速判断) */
  permissionCodes: string[]

  /** 菜单权限树 */
  menuPermissions: Permission[]

  /** 按钮权限码 */
  buttonPermissions: string[]

  /** API权限码 */
  apiPermissions: string[]

  /** 数据权限(按模块) */
  dataPermissions?: Record<string, string>
}

/**
 * 角色筛选条件
 */
export interface RoleFilter {
  /** 关键词搜索(名称/编码) */
  keyword?: string

  /** 状态筛选 */
  status?: 'active' | 'disabled'

  /** 角色类型筛选 */
  type?: 'system' | 'custom'
}

/**
 * 权限筛选条件
 */
export interface PermissionFilter {
  /** 关键词搜索(名称/编码) */
  keyword?: string

  /** 权限类型筛选 */
  type?: 'menu' | 'button' | 'api' | 'data'

  /** 所属模块筛选 */
  module?: string
}

/**
 * 角色统计数据
 */
export interface RoleStatistics {
  /** 总角色数 */
  total: number

  /** 系统角色数 */
  systemCount: number

  /** 自定义角色数 */
  customCount: number

  /** 启用的角色数 */
  activeCount: number
}

/**
 * 权限统计数据
 */
export interface PermissionStatistics {
  /** 总权限数 */
  total: number

  /** 菜单权限数 */
  menuCount: number

  /** 按钮权限数 */
  buttonCount: number

  /** 接口权限数 */
  apiCount: number

  /** 数据权限数 */
  dataCount: number
}

/**
 * 枚举: 角色类型
 */
export enum RoleType {
  SYSTEM = 'system',      // 系统角色(预置)
  CUSTOM = 'custom'       // 自定义角色
}

/**
 * 枚举: 权限类型
 */
export enum PermissionType {
  MENU = 'menu',          // 菜单权限
  BUTTON = 'button',      // 按钮权限
  API = 'api',           // 接口权限
  DATA = 'data'          // 数据权限
}

/**
 * 枚举: 数据范围
 */
export enum DataScope {
  ALL = 'all',                    // 全部数据
  DEPT = 'dept',                  // 本部门数据
  DEPT_AND_SUB = 'dept_and_sub',  // 本部门及下级部门数据
  SELF = 'self'                   // 仅本人数据
}

/**
 * 枚举: 请求方式
 */
export enum ApiMethod {
  GET = 'GET',
  POST = 'POST',
  PUT = 'PUT',
  DELETE = 'DELETE',
  PATCH = 'PATCH'
}

/**
 * API 响应类型
 */
export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
}

/**
 * 列表响应类型
 */
export interface ListResponse<T> {
  list: T[]
  total: number
}
