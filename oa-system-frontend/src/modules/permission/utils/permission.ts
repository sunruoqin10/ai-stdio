/**
 * 权限工具函数
 * @module permission/utils/permission
 */

import type { Permission, UserPermissions } from '../types'
import { PermissionType, DataScope } from '../types'

/**
 * 扁平权限列表转树形结构
 * @param permissions 权限列表
 * @param options 选项
 * @returns 树形权限结构
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
 * @param permissionId 权限ID
 * @param permissions 权限列表
 * @returns 所有子权限ID数组
 */
export function getAllPermissionIds(permissionId: string, permissions: Permission[]): string[] {
  const ids: string[] = [permissionId]
  const children = permissions.filter(p => p.parentId === permissionId)

  children.forEach(child => {
    ids.push(...getAllPermissionIds(child.id, permissions))
  })

  return ids
}

/**
 * 检查用户是否有指定权限
 * @param userPermissions 用户权限
 * @param permissionCode 权限码
 * @returns 是否有权限
 */
export function hasPermission(
  userPermissions: UserPermissions,
  permissionCode: string
): boolean {
  return userPermissions.permissionCodes.includes(permissionCode)
}

/**
 * 检查用户是否有任意一个权限
 * @param userPermissions 用户权限
 * @param permissionCodes 权限码数组
 * @returns 是否有任意权限
 */
export function hasAnyPermission(
  userPermissions: UserPermissions,
  permissionCodes: string[]
): boolean {
  return permissionCodes.some(code => hasPermission(userPermissions, code))
}

/**
 * 检查用户是否有所有权限
 * @param userPermissions 用户权限
 * @param permissionCodes 权限码数组
 * @returns 是否有所有权限
 */
export function hasAllPermissions(
  userPermissions: UserPermissions,
  permissionCodes: string[]
): boolean {
  return permissionCodes.every(code => hasPermission(userPermissions, code))
}

/**
 * 检查用户是否有指定角色
 * @param userPermissions 用户权限
 * @param roleCode 角色码
 * @returns 是否有角色
 */
export function hasRole(userPermissions: UserPermissions, roleCode: string): boolean {
  return userPermissions.roles.some(role => role.code === roleCode)
}

/**
 * 检查用户是否有任意一个角色
 * @param userPermissions 用户权限
 * @param roleCodes 角色码数组
 * @returns 是否有任意角色
 */
export function hasAnyRole(userPermissions: UserPermissions, roleCodes: string[]): boolean {
  return roleCodes.some(code => hasRole(userPermissions, code))
}

/**
 * 过滤菜单权限(根据用户权限)
 * @param allMenus 所有菜单权限
 * @param userPermissions 用户权限
 * @returns 过滤后的菜单权限
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
 * 获取权限类型标签类型
 * @param type 权限类型
 * @returns Element Plus 标签类型
 */
export function getPermissionTypeTag(type: PermissionType): string {
  const tagMap: Record<PermissionType, string> = {
    [PermissionType.MENU]: 'primary',
    [PermissionType.BUTTON]: 'success',
    [PermissionType.API]: 'warning',
    [PermissionType.DATA]: 'danger'
  }
  return tagMap[type] || 'info'
}

/**
 * 获取权限类型名称
 * @param type 权限类型
 * @returns 权限类型名称
 */
export function getPermissionTypeName(type: PermissionType): string {
  const nameMap: Record<PermissionType, string> = {
    [PermissionType.MENU]: '菜单权限',
    [PermissionType.BUTTON]: '按钮权限',
    [PermissionType.API]: '接口权限',
    [PermissionType.DATA]: '数据权限'
  }
  return nameMap[type] || '未知类型'
}

/**
 * 获取数据范围名称
 * @param scope 数据范围
 * @returns 数据范围名称
 */
export function getDataScopeName(scope: DataScope): string {
  const nameMap: Record<DataScope, string> = {
    [DataScope.ALL]: '全部数据',
    [DataScope.DEPT]: '本部门',
    [DataScope.DEPT_AND_SUB]: '本部门及下级',
    [DataScope.SELF]: '仅本人'
  }
  return nameMap[scope] || '未知范围'
}

/**
 * 生成权限编码
 * @param module 模块名
 * @param action 操作名
 * @param resource 资源名
 * @returns 权限编码
 */
export function generatePermissionCode(
  module: string,
  action: string,
  resource: string
): string {
  return `${module}:${action}:${resource}`.toLowerCase()
}

/**
 * 验证权限编码格式
 * @param code 权限编码
 * @returns 是否有效
 */
export function validatePermissionCode(code: string): boolean {
  const pattern = /^[a-z0-9:_]+$/
  return pattern.test(code)
}

/**
 * 搜索过滤权限节点
 * @param value 搜索值
 * @param data 权限数据
 * @returns 是否匹配
 */
export function filterPermissionNode(value: string, data: Permission): boolean {
  if (!value) return true
  return data.name.includes(value) || data.code.includes(value)
}

/**
 * 扁平化权限树
 * @param permissions 树形权限
 * @returns 扁平权限列表
 */
export function flattenPermissionTree(permissions: Permission[]): Permission[] {
  const result: Permission[] = []

  function traverse(nodes: Permission[]) {
    nodes.forEach(node => {
      const { children, ...rest } = node
      result.push(rest as Permission)
      if (children?.length) {
        traverse(children)
      }
    })
  }

  traverse(permissions)
  return result
}

/**
 * 获取权限路径(从根到当前节点)
 * @param permissionId 权限ID
 * @param permissions 权限列表
 * @returns 权限路径
 */
export function getPermissionPath(permissionId: string, permissions: Permission[]): Permission[] {
  const path: Permission[] = []
  let current = permissions.find(p => p.id === permissionId)

  while (current) {
    path.unshift(current)
    if (!current.parentId) break
    current = permissions.find(p => p.id === current.parentId)
  }

  return path
}

/**
 * 检查权限是否为叶子节点
 * @param permission 权限
 * @param permissions 所有权限
 * @returns 是否为叶子节点
 */
export function isLeafPermission(permission: Permission, permissions: Permission[]): boolean {
  return !permissions.some(p => p.parentId === permission.id)
}

/**
 * 获取权限的所有父级ID
 * @param permissionId 权限ID
 * @param permissions 所有权限
 * @returns 父级ID数组
 */
export function getParentPermissionIds(permissionId: string, permissions: Permission[]): string[] {
  const parentIds: string[] = []
  let current = permissions.find(p => p.id === permissionId)

  while (current?.parentId) {
    parentIds.unshift(current.parentId)
    current = permissions.find(p => p.id === current.parentId)
  }

  return parentIds
}

/**
 * 清除权限缓存
 * @param userId 用户ID
 */
export function clearPermissionCache(userId: string): void {
  const cacheKey = `user:permissions:${userId}`
  localStorage.removeItem(cacheKey)
  sessionStorage.removeItem(cacheKey)
}

/**
 * 缓存用户权限
 * @param userId 用户ID
 * @param permissions 用户权限
 */
export function cacheUserPermissions(userId: string, permissions: UserPermissions): void {
  const cacheKey = `user:permissions:${userId}`
  const cacheData = {
    data: permissions,
    timestamp: Date.now()
  }
  sessionStorage.setItem(cacheKey, JSON.stringify(cacheData))
}

/**
 * 获取缓存的用户权限
 * @param userId 用户ID
 * @returns 用户权限或null
 */
export function getCachedPermissions(userId: string): UserPermissions | null {
  const cacheKey = `user:permissions:${userId}`
  const cacheStr = sessionStorage.getItem(cacheKey)

  if (!cacheStr) return null

  try {
    const cache = JSON.parse(cacheStr)
    // 缓存有效期30分钟
    if (Date.now() - cache.timestamp > 30 * 60 * 1000) {
      sessionStorage.removeItem(cacheKey)
      return null
    }
    return cache.data
  } catch {
    return null
  }
}

/**
 * 权限缓存类
 */
export class PermissionCache {
  private cache: Map<string, { data: any; expired: number }> = new Map()
  private defaultTTL = 30 * 60 * 1000 // 30分钟

  /**
   * 设置缓存
   * @param key 缓存键
   * @param value 缓存值
   * @param ttl 过期时间(毫秒)
   */
  set(key: string, value: any, ttl?: number) {
    this.cache.set(key, {
      data: value,
      expired: Date.now() + (ttl || this.defaultTTL)
    })
  }

  /**
   * 获取缓存
   * @param key 缓存键
   * @returns 缓存值或null
   */
  get(key: string) {
    const item = this.cache.get(key)
    if (!item) return null

    if (Date.now() > item.expired) {
      this.cache.delete(key)
      return null
    }

    return item.data
  }

  /**
   * 删除缓存
   * @param key 缓存键
   */
  delete(key: string) {
    this.cache.delete(key)
  }

  /**
   * 清空所有缓存
   */
  clear() {
    this.cache.clear()
  }
}

// 导出单例
export const permissionCache = new PermissionCache()
