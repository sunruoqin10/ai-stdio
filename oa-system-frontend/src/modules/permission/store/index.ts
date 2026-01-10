/**
 * 权限管理 Pinia Store
 * @module permission/store
 */

import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { Permission, Role, UserPermissions } from '../types'
import * as permissionApi from '../api'
import {
  buildPermissionTree,
  filterMenuPermissions,
  hasPermission,
  hasAnyPermission,
  hasRole,
  getCachedPermissions,
  cacheUserPermissions,
  permissionCache
} from '../utils/permission'
import { ElMessage } from 'element-plus'

export const usePermissionStore = defineStore('permission', () => {
  // ==================== 状态 ====================
  const userPermissions = ref<UserPermissions>({
    userId: '',
    roles: [],
    permissions: [],
    permissionCodes: [],
    buttonPermissions: [],
    apiPermissions: [],
    menuPermissions: []
  })

  const hasLoadedPermissions = ref(false)
  const allPermissions = ref<Permission[]>([])
  const allRoles = ref<Role[]>([])

  // ==================== 计算属性 ====================

  /**
   * 菜单权限树
   */
  const menuTree = computed(() => {
    return buildPermissionTree(userPermissions.value.menuPermissions, {
      type: 'menu' as any
    })
  })

  /**
   * 所有权限码
   */
  const permissionCodes = computed(() => {
    return userPermissions.value.permissionCodes
  })

  /**
   * 用户角色列表
   */
  const userRoles = computed(() => {
    return userPermissions.value.roles
  })

  /**
   * 是否为超级管理员
   */
  const isSuperAdmin = computed(() => {
    return hasRole(userPermissions.value, 'admin')
  })

  // ==================== 方法 ====================

  /**
   * 加载用户权限
   * @param forceRefresh 是否强制刷新(忽略缓存)
   */
  async function loadUserPermissions(forceRefresh = false) {
    try {
      const userId = localStorage.getItem('userId') || 'USER001'

      // 检查缓存
      if (!forceRefresh) {
        const cached = getCachedPermissions(userId)
        if (cached) {
          userPermissions.value = cached
          hasLoadedPermissions.value = true
          return
        }
      }

      // 开发环境使用 Mock 数据
      if (import.meta.env.DEV) {
        const mockData = permissionApi.getMockUserPermissions()
        userPermissions.value = mockData
      } else {
        const response = await permissionApi.getUserPermissions(userId)
        userPermissions.value = response.data
      }

      // 缓存权限
      cacheUserPermissions(userId, userPermissions.value)
      hasLoadedPermissions.value = true
    } catch (error: any) {
      ElMessage.error('加载用户权限失败: ' + error.message)
      throw error
    }
  }

  /**
   * 加载所有权限
   * @param forceRefresh 是否强制刷新
   */
  async function loadAllPermissions(forceRefresh = false) {
    try {
      // 检查缓存
      if (!forceRefresh) {
        const cached = permissionCache.get('all_permissions')
        if (cached) {
          allPermissions.value = cached
          return
        }
      }

      // 开发环境使用 Mock 数据
      if (import.meta.env.DEV) {
        const mockData = permissionApi.getMockPermissionTree()
        allPermissions.value = mockData
      } else {
        allPermissions.value = await permissionApi.getPermissionTree()
      }

      // 缓存权限树
      permissionCache.set('all_permissions', allPermissions.value)
    } catch (error: any) {
      ElMessage.error('加载权限列表失败: ' + error.message)
      throw error
    }
  }

  /**
   * 加载角色列表
   * @param forceRefresh 是否强制刷新
   */
  async function loadRoles(forceRefresh = false) {
    try {
      // 检查缓存
      if (!forceRefresh) {
        const cached = permissionCache.get('all_roles')
        if (cached) {
          allRoles.value = cached
          return
        }
      }

      // 开发环境使用 Mock 数据
      if (import.meta.env.DEV) {
        const mockData = permissionApi.getMockRoleList()
        allRoles.value = mockData.list
      } else {
        const response = await permissionApi.getRoleList()
        allRoles.value = response.data.list
      }

      // 缓存角色列表
      permissionCache.set('all_roles', allRoles.value)
    } catch (error: any) {
      ElMessage.error('加载角色列表失败: ' + error.message)
      throw error
    }
  }

  /**
   * 检查是否有指定权限
   * @param code 权限码
   * @returns 是否有权限
   */
  function checkPermission(code: string): boolean {
    return hasPermission(userPermissions.value, code)
  }

  /**
   * 检查是否有任意一个权限
   * @param codes 权限码数组
   * @returns 是否有任意权限
   */
  function checkAnyPermission(codes: string[]): boolean {
    return hasAnyPermission(userPermissions.value, codes)
  }

  /**
   * 检查是否有所有权限
   * @param codes 权限码数组
   * @returns 是否有所有权限
   */
  function checkAllPermissions(codes: string[]): boolean {
    return codes.every(code => checkPermission(code))
  }

  /**
   * 检查是否有指定角色
   * @param roleCode 角色码
   * @returns 是否有角色
   */
  function checkRole(roleCode: string): boolean {
    return hasRole(userPermissions.value, roleCode)
  }

  /**
   * 检查是否有任意一个角色
   * @param roleCodes 角色码数组
   * @returns 是否有任意角色
   */
  function checkAnyRole(roleCodes: string[]): boolean {
    return roleCodes.some(code => checkRole(code))
  }

  /**
   * 清除权限状态
   */
  function clearPermissions() {
    userPermissions.value = {
      userId: '',
      roles: [],
      permissions: [],
      permissionCodes: [],
      buttonPermissions: [],
      apiPermissions: [],
      menuPermissions: []
    }
    hasLoadedPermissions.value = false
  }

  /**
   * 清除所有缓存
   */
  function clearCache() {
    clearPermissions()
    permissionCache.clear()
    const userId = localStorage.getItem('userId')
    if (userId) {
      localStorage.removeItem(`user:permissions:${userId}`)
      sessionStorage.removeItem(`user:permissions:${userId}`)
    }
  }

  /**
   * 刷新权限
   */
  async function refreshPermissions() {
    clearCache()
    await loadUserPermissions(true)
    await loadAllPermissions(true)
    await loadRoles(true)
  }

  /**
   * 获取权限统计
   */
  function getPermissionStats() {
    const permissions = allPermissions.value
    return {
      total: permissions.length,
      menu: permissions.filter(p => p.type === 'menu').length,
      button: permissions.filter(p => p.type === 'button').length,
      api: permissions.filter(p => p.type === 'api').length,
      data: permissions.filter(p => p.type === 'data').length
    }
  }

  /**
   * 获取角色统计
   */
  function getRoleStats() {
    const roles = allRoles.value
    return {
      total: roles.length,
      system: roles.filter(r => r.type === 'system').length,
      custom: roles.filter(r => r.type === 'custom').length,
      active: roles.filter(r => r.status === 'active').length
    }
  }

  return {
    // 状态
    userPermissions,
    hasLoadedPermissions,
    allPermissions,
    allRoles,

    // 计算属性
    menuTree,
    permissionCodes,
    userRoles,
    isSuperAdmin,

    // 方法
    loadUserPermissions,
    loadAllPermissions,
    loadRoles,
    checkPermission,
    checkAnyPermission,
    checkAllPermissions,
    checkRole,
    checkAnyRole,
    clearPermissions,
    clearCache,
    refreshPermissions,
    getPermissionStats,
    getRoleStats
  }
})

/**
 * 权限组合式函数 (便于在组件中使用)
 */
export function usePermission() {
  const store = usePermissionStore()

  return {
    // 状态
    permissions: store.userPermissions,
    menuTree: store.menuTree,
    roles: store.userRoles,
    isSuperAdmin: store.isSuperAdmin,

    // 方法
    hasPermission: store.checkPermission,
    hasAnyPermission: store.checkAnyPermission,
    hasAllPermissions: store.checkAllPermissions,
    hasRole: store.checkRole,
    hasAnyRole: store.checkAnyRole,

    // 操作
    loadPermissions: store.loadUserPermissions,
    refreshPermissions: store.refreshPermissions,
    clearPermissions: store.clearPermissions
  }
}
