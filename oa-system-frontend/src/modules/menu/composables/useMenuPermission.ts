/**
 * 菜单管理模块 - 权限控制组合函数
 */

import { computed } from 'vue'
import { useAuthStore } from '@/modules/auth/store'
import { MENU_PERMISSIONS } from '../config/permissions'

export function useMenuPermission() {
  const authStore = useAuthStore()

  // 单个权限检查
  const hasPermission = (permission: string) => {
    return authStore.hasPermission(permission)
  }

  // 组合权限检查
  const canCreate = computed(() => hasPermission(MENU_PERMISSIONS.CREATE))
  const canEdit = computed(() => hasPermission(MENU_PERMISSIONS.EDIT))
  const canDelete = computed(() => hasPermission(MENU_PERMISSIONS.DELETE))
  const canSort = computed(() => hasPermission(MENU_PERMISSIONS.SORT))
  const canEnable = computed(() => hasPermission(MENU_PERMISSIONS.ENABLE))
  const canExport = computed(() => hasPermission(MENU_PERMISSIONS.EXPORT))
  const canViewAll = computed(() => hasPermission(MENU_PERMISSIONS.VIEW_ALL))

  // 菜单操作权限检查
  const canEditMenu = (menu: any) => {
    if (!canEdit.value) return false
    if (menu.isSystem) return false
    return true
  }

  const canDeleteMenu = (menu: any) => {
    if (!canDelete.value) return false
    if (menu.isSystem) return false
    if (menu.children?.length > 0) return false
    return true
  }

  const canEnableMenu = (menu: any) => {
    if (!canEnable.value) return false
    if (menu.isSystem) return false
    return true
  }

  return {
    hasPermission,
    canCreate,
    canEdit,
    canDelete,
    canSort,
    canEnable,
    canExport,
    canViewAll,
    canEditMenu,
    canDeleteMenu,
    canEnableMenu
  }
}
