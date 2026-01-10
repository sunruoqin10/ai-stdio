/**
 * 菜单管理模块 - 路由转换工具
 */

import type { MenuItem, RouteMenuItem } from '../types'

/**
 * 转换菜单配置为路由配置
 * @param menu 菜单项
 * @returns 路由配置
 */
export function transformMenuToRoute(menu: MenuItem): RouteMenuItem {
  const route: RouteMenuItem = {
    path: menu.routePath || '',
    name: menu.menuName,
    meta: {
      title: menu.menuName,
      icon: menu.menuIcon,
      hidden: !menu.visible,
      permissions: menu.permission ? [menu.permission] : []
    }
  }

  // 组件路径
  if (menu.componentPath) {
    route.component = menu.componentPath
  }

  // 重定向
  if (menu.redirectPath) {
    route.redirect = menu.redirectPath
  }

  // 缓存配置
  if (menu.isCache) {
    route.meta.keepAlive = true
  }

  // 外链配置
  if (menu.isFrame && menu.frameUrl) {
    (route.meta as any).isFrame = true
    ;(route.meta as any).frameUrl = menu.frameUrl
    ;(route.meta as any).frameTarget = menu.menuTarget || '_self'
  }

  // 目录类型配置
  if (menu.menuType === 'directory') {
    route.meta.alwaysShow = true
  }

  return route
}

/**
 * 构建路由树
 * @param menus 菜单列表
 * @returns 路由树
 */
export function buildRouteTree(menus: MenuItem[]): RouteMenuItem[] {
  return menus
    .filter(menu => menu.menuType !== 'button')
    .map(menu => {
      const route = transformMenuToRoute(menu)

      if (menu.children?.length) {
        route.children = buildRouteTree(menu.children)
      }

      return route
    })
}

/**
 * 扁平化路由
 * @param routes 路由列表
 * @returns 扁平化的路由列表
 */
export function flattenRoutes(routes: RouteMenuItem[]): RouteMenuItem[] {
  const result: RouteMenuItem[] = []

  function flatten(routes: RouteMenuItem[]) {
    routes.forEach(route => {
      result.push(route)
      if (route.children?.length) {
        flatten(route.children)
      }
    })
  }

  flatten(routes)
  return result
}
