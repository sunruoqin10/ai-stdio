/**
 * 菜单管理模块 - 权限配置
 */

/**
 * 菜单管理权限定义
 */
export const MENU_PERMISSIONS = {
  VIEW: 'menu:view',
  VIEW_ALL: 'menu:view_all',
  CREATE: 'menu:create',
  EDIT: 'menu:edit',
  DELETE: 'menu:delete',
  SORT: 'menu:sort',
  ENABLE: 'menu:enable',
  EXPORT: 'menu:export'
} as const

/**
 * 菜单权限类型
 */
export type MenuPermission = typeof MENU_PERMISSIONS[keyof typeof MENU_PERMISSIONS]
