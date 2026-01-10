/**
 * 权限指令 v-auth
 * 用法: v-auth="'permission.code'" 或 v-auth="['code1', 'code2']"
 * @module permission/directives/auth
 */

import type { Directive, DirectiveBinding } from 'vue'
import { usePermissionStore } from '../store'

/**
 * 权限指令
 */
export const auth: Directive = {
  mounted(el: HTMLElement, binding: DirectiveBinding) {
    const { value } = binding
    const permissionStore = usePermissionStore()

    if (value) {
      const permissionCodes = Array.isArray(value) ? value : [value]
      const hasAuth = permissionStore.checkAnyPermission(permissionCodes)

      if (!hasAuth) {
        // 无权限,移除元素或添加隐藏样式
        el.parentNode?.removeChild(el)
        // 或者使用以下方式隐藏元素(保留占位)
        // el.style.display = 'none'
        // el.setAttribute('data-auth-hidden', 'true')
      }
    } else {
      throw new Error('权限指令需要指定权限码,如 v-auth="\'permission.code\'"')
    }
  },

  updated(el: HTMLElement, binding: DirectiveBinding) {
    const { value } = binding
    const permissionStore = usePermissionStore()

    if (value) {
      const permissionCodes = Array.isArray(value) ? value : [value]
      const hasAuth = permissionStore.checkAnyPermission(permissionCodes)

      // 检查元素是否已被隐藏
      const isHidden = el.getAttribute('data-auth-hidden') === 'true'

      if (hasAuth && isHidden) {
        // 有权限但被隐藏,显示元素
        el.style.display = ''
        el.removeAttribute('data-auth-hidden')
      } else if (!hasAuth && !isHidden) {
        // 无权限且未隐藏,隐藏元素
        el.style.display = 'none'
        el.setAttribute('data-auth-hidden', 'true')
      }
    }
  }
}

/**
 * 角色指令 v-role
 * 用法: v-role="'role.code'" 或 v-role="['role1', 'role2']"
 */
export const role: Directive = {
  mounted(el: HTMLElement, binding: DirectiveBinding) {
    const { value } = binding
    const permissionStore = usePermissionStore()

    if (value) {
      const roleCodes = Array.isArray(value) ? value : [value]
      const hasRole = permissionStore.checkAnyRole(roleCodes)

      if (!hasRole) {
        el.parentNode?.removeChild(el)
      }
    } else {
      throw new Error('角色指令需要指定角色码,如 v-role="\'role.code\'"')
    }
  }
}

/**
 * 超级管理员指令 v-super-admin
 * 用法: v-super-admin
 */
export const superAdmin: Directive = {
  mounted(el: HTMLElement) {
    const permissionStore = usePermissionStore()

    if (!permissionStore.isSuperAdmin) {
      el.parentNode?.removeChild(el)
    }
  }
}

/**
 * 注册权限指令
 * @param app Vue 应用实例
 */
export function setupAuthDirective(app: any) {
  app.directive('auth', auth)
  app.directive('role', role)
  app.directive('superAdmin', superAdmin)
}

/**
 * 权限指令导出(供 main.ts 使用)
 */
export default {
  install(app: any) {
    setupAuthDirective(app)
  }
}
