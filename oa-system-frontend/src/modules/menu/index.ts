/**
 * 菜单管理模块 - 统一导出
 */

// 导出组件
export { default as MenuTree } from './components/MenuTree'
export { default as MenuForm } from './components/MenuForm'
export { default as IconSelector } from './components/IconSelector'

// 导出组合式函数
export { useMenu } from './composables/useMenu'
export { useMenuTree } from './composables/useMenuTree'
export { useMenuDict } from './composables/useMenuDict'
export { useMenuPermission } from './composables/useMenuPermission'

// 导出Store
export { useMenuStore } from './stores/menuStore'

// 导出API
export * from './api'

// 导出类型
export * from './types'

// 导出配置
export * from './config/dict'
export * from './config/permissions'
export * from './config/icons'

// 导出工具函数
export * from './utils/routeTransformer'
export * from './utils/permissionFilter'
