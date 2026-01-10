/**
 * 路由权限验证
 * @module router/permission
 */

import type { Router } from 'vue-router'
import { usePermissionStore } from '@/modules/permission/store'
// import { useUserStore } from '@/stores/user'

// 白名单路由(不需要权限)
const whiteList = ['/login', '/404', '/403']

/**
 * 设置路由权限守卫
 * @param router Vue Router 实例
 */
export function setupPermissionGuard(router: Router) {
  router.beforeEach(async (to, from, next) => {
    // const userStore = useUserStore()
    const permissionStore = usePermissionStore()

    // 检查是否登录
    const userId = localStorage.getItem('userId')
    const isLoggedIn = !!userId

    if (isLoggedIn) {
      if (to.path === '/login') {
        // 已登录,跳转到首页
        next({ path: '/' })
      } else {
        // 检查是否已加载权限
        if (!permissionStore.hasLoadedPermissions) {
          try {
            // 加载用户权限
            await permissionStore.loadUserPermissions()

            // 动态生成可访问路由
            // const accessRoutes = await permissionStore.generateRoutes()

            // 添加路由
            // accessRoutes.forEach(route => {
            //   router.addRoute(route)
            // })

            // 确保addRoute完成后跳转
            next({ ...to, replace: true })
          } catch (error) {
            // 权限加载失败,退出登录
            console.error('权限加载失败:', error)
            localStorage.removeItem('userId')
            next(`/login?redirect=${to.path}`)
          }
        } else {
          // 检查路由权限
          if (hasRoutePermission(to, permissionStore.permissionCodes)) {
            next()
          } else {
            // 无权限,跳转到403
            next('/403')
          }
        }
      }
    } else {
      // 未登录
      if (whiteList.includes(to.path)) {
        // 在白名单中,直接放行
        next()
      } else {
        // 不在白名单,跳转到登录页
        next(`/login?redirect=${to.path}`)
      }
    }
  })
}

/**
 * 检查是否有路由访问权限
 * @param route 路由对象
 * @param permissionCodes 用户权限码列表
 * @returns 是否有权限
 */
function hasRoutePermission(route: any, permissionCodes: string[]): boolean {
  // 路由未配置权限,默认允许
  if (!route.meta?.permission) {
    return true
  }

  // 检查是否有权限
  const requiredPermissions = Array.isArray(route.meta.permission)
    ? route.meta.permission
    : [route.meta.permission]

  return requiredPermissions.some((perm: string) => permissionCodes.includes(perm))
}

/**
 * 生成动态路由
 * @param permissions 用户权限
 * @returns 路由配置数组
 */
export function generateDynamicRoutes(permissions: any) {
  const routes: any[] = []

  // TODO: 根据用户权限动态生成路由
  // 这里需要根据实际的路由配置来实现

  return routes
}

/**
 * 过滤异步路由
 * @param routes 异步路由数组
 * @param permissionCodes 用户权限码列表
 * @returns 过滤后的路由
 */
export function filterAsyncRoutes(routes: any[], permissionCodes: string[]) {
  const res: any[] = []

  routes.forEach(route => {
    const tmp = { ...route }

    if (hasRoutePermission(tmp, permissionCodes)) {
      if (tmp.children) {
        tmp.children = filterAsyncRoutes(tmp.children, permissionCodes)
      }
      res.push(tmp)
    }
  })

  return res
}

/**
 * 重置路由
 * @param router Vue Router 实例
 */
export function resetRouter(router: Router) {
  const newRouter = router.constructor?.()
  if (newRouter) {
    (router as any).matcher = (newRouter as any).matcher
  }
}

export default {
  setupPermissionGuard,
  generateDynamicRoutes,
  filterAsyncRoutes,
  resetRouter
}
