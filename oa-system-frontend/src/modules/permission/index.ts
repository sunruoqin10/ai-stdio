/**
 * 权限控制模块 - 统一导出
 * @module permission
 */

// API
export * from './api'

// Store
export { usePermissionStore, usePermission } from './store'

// Types
export * from './types'

// Utils
export * from './utils/permission'

// Directives
export { auth, role, superAdmin, setupAuthDirective } from './directives/auth'

// Components
export { default as RoleForm } from './components/RoleForm.vue'
export { default as PermissionForm } from './components/PermissionForm.vue'
export { default as PermissionConfigDialog } from './components/PermissionConfigDialog.vue'
export { default as RoleMembersDialog } from './components/RoleMembersDialog.vue'

// Views
export { default as RoleList } from './views/RoleList.vue'
export { default as PermissionList } from './views/PermissionList.vue'
