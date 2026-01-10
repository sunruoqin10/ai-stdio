/**
 * 菜单管理模块 - 权限过滤工具
 */

import type { MenuItem } from '../types'

/**
 * 根据权限过滤菜单
 * @param menus 菜单列表
 * @param hasPermission 权限检查函数
 * @returns 过滤后的菜单列表
 */
export function filterMenusByPermission(
  menus: MenuItem[],
  hasPermission: (permission: string) => boolean
): MenuItem[] {
  return menus
    .filter(menu => {
      // 没有权限标识的菜单默认显示
      if (!menu.permission) return true

      // 检查权限
      return hasPermission(menu.permission)
    })
    .map(menu => ({
      ...menu,
      children: menu.children
        ? filterMenusByPermission(menu.children, hasPermission)
        : undefined
    }))
    .filter(menu => {
      // 如果是目录类型,至少有一个子菜单有权限才显示
      if (menu.menuType === 'directory') {
        return menu.children && menu.children.length > 0
      }
      return true
    })
}
