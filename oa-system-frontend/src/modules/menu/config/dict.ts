/**
 * 菜单管理模块 - 数据字典配置
 */

/**
 * 菜单管理模块依赖的字典类型
 */
export const MENU_DICT_TYPES = {
  MENU_TYPE: 'menu_type',        // 菜单类型(目录/菜单/按钮)
  MENU_STATUS: 'menu_status',    // 菜单状态(启用/禁用)
  MENU_TARGET: 'menu_target'     // 链接打开方式(当前页/新页)
} as const

/**
 * 字典缓存策略
 */
export const MENU_DICT_CACHE = {
  // 预加载字典
  preload: [
    'menu_type',
    'menu_status'
  ],

  // 按需加载字典
  onDemand: [
    'menu_target'
  ],

  // 缓存过期时间: 30分钟
  expireTime: 30 * 60 * 1000
} as const
