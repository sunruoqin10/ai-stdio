/**
 * 菜单管理 Mock API 适配器
 * 用于在开发阶段模拟后端API响应
 */

import type { InternalAxiosRequestConfig, AxiosResponse } from 'axios'
import type { MenuItem, MenuForm, RouteMenuItem } from '../types'
import * as mockData from '../mock/data'

// 模拟延迟
const delay = (ms: number) => new Promise(resolve => setTimeout(resolve, ms))

/**
 * 模拟API响应
 */
function mockResponse<T>(data: T, delayMs: number = 300) {
  return delay(delayMs).then(() => ({
    code: 200,
    message: 'success',
    data
  }))
}

/**
 * 获取菜单列表(树形结构)
 */
async function mockGetMenuList() {
  const tree = mockData.getMenuTree(0)
  return mockResponse(tree)
}

/**
 * 获取菜单详情
 */
async function mockGetMenuDetail(id: string) {
  const menuId = parseInt(id)
  const menu = mockData.findMenuItem(menuId)
  if (!menu) {
    throw new Error('菜单不存在')
  }
  return mockResponse(menu)
}

/**
 * 创建菜单
 */
async function mockCreateMenu(data: MenuForm) {
  const newId = mockData.getNextId()
  const newMenu: MenuItem = {
    id: newId,
    menuCode: data.menuCode || `MENU_${String(newId).padStart(3, '0')}`,
    menuName: data.menuName,
    menuType: data.menuType,
    parentId: data.parentId,
    menuLevel: data.parentId === 0 ? 1 : 2, // 简化逻辑，实际应该计算层级
    routePath: data.routePath,
    componentPath: data.componentPath,
    redirectPath: data.redirectPath,
    menuIcon: data.menuIcon,
    permission: data.permission,
    sortOrder: data.sortOrder,
    visible: data.visible,
    status: data.status,
    isCache: data.isCache,
    isFrame: data.isFrame,
    frameUrl: data.frameUrl,
    menuTarget: data.menuTarget,
    isSystem: false,
    remark: data.remark,
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString()
  }

  mockData.addMenuItem(newMenu)
  return mockResponse(newMenu)
}

/**
 * 更新菜单
 */
async function mockUpdateMenu(id: string, data: MenuForm) {
  const menuId = parseInt(id)
  const menu = mockData.findMenuItem(menuId)
  if (!menu) {
    throw new Error('菜单不存在')
  }

  const updated = mockData.updateMenuItem(menuId, {
    menuName: data.menuName,
    menuType: data.menuType,
    parentId: data.parentId,
    routePath: data.routePath,
    componentPath: data.componentPath,
    redirectPath: data.redirectPath,
    menuIcon: data.menuIcon,
    permission: data.permission,
    sortOrder: data.sortOrder,
    visible: data.visible,
    status: data.status,
    isCache: data.isCache,
    isFrame: data.isFrame,
    frameUrl: data.frameUrl,
    menuTarget: data.menuTarget,
    remark: data.remark
  })

  if (!updated) {
    throw new Error('更新菜单失败')
  }

  return mockResponse(mockData.findMenuItem(menuId))
}

/**
 * 删除菜单
 */
async function mockDeleteMenu(id: string) {
  const menuId = parseInt(id)
  const deleted = mockData.deleteMenuItem(menuId)
  if (!deleted) {
    throw new Error('菜单不存在')
  }
  return mockResponse({ success: true })
}

/**
 * 获取父级菜单选项
 */
async function mockGetParentOptions() {
  const options = mockData.getParentOptions()
  return mockResponse(options)
}

/**
 * 切换菜单状态
 */
async function mockToggleMenuStatus(id: string, statusData: { status: boolean }) {
  const menuId = parseInt(id)
  const updated = mockData.updateMenuItem(menuId, {
    status: statusData.status
  })
  if (!updated) {
    throw new Error('菜单不存在')
  }
  return mockResponse({ success: true })
}

/**
 * 获取菜单路由
 */
async function mockGetMenuRoutes() {
  const menus = mockData.getMenuTree(0)
  const routes: RouteMenuItem[] = convertMenusToRoutes(menus)
  return mockResponse(routes)
}

/**
 * 将菜单转换为路由格式
 */
function convertMenusToRoutes(menus: MenuItem[]): RouteMenuItem[] {
  return menus
    .filter(menu => menu.status && menu.visible) // 只返回启用且可见的菜单
    .map(menu => {
      const route: RouteMenuItem = {
        path: menu.routePath || '',
        name: menu.menuCode,
        component: menu.componentPath,
        redirect: menu.redirectPath,
        meta: {
          title: menu.menuName,
          icon: menu.menuIcon,
          hidden: !menu.visible,
          keepAlive: menu.isCache
        }
      }

      // 递归处理子菜单
      const children = menu.children ? convertMenusToRoutes(menu.children) : []
      if (children.length > 0) {
        route.children = children
      }

      return route
    })
}

/**
 * 导出所有的 mock API handlers
 */
export const mockMenuApiHandlers = {
  getMenuList: mockGetMenuList,
  getMenuDetail: mockGetMenuDetail,
  createMenu: mockCreateMenu,
  updateMenu: mockUpdateMenu,
  deleteMenu: mockDeleteMenu,
  getParentOptions: mockGetParentOptions,
  toggleMenuStatus: mockToggleMenuStatus,
  getMenuRoutes: mockGetMenuRoutes
}

/**
 * 创建菜单 Mock 适配器
 */
export function createMenuMockAdapter() {
  return {
    /**
     * 请求拦截器 - 判断是否应该返回Mock数据
     */
    handler: async (config: InternalAxiosRequestConfig): Promise<AxiosResponse> | null => {
      // 只在开发环境且请求路径匹配时返回Mock数据
      if (!import.meta.env.DEV) {
        return null
      }

      const url = config.url || ''

      // 匹配获取菜单列表接口
      if (url.match(/\/api\/menus$/) && config.method === 'get') {
        const data = await mockMenuApiHandlers.getMenuList()
        return createMockResponse(data)
      }

      // 匹配获取菜单详情接口
      if (url.match(/\/api\/menus\/\d+$/) && config.method === 'get') {
        const id = url.split('/').pop() || ''
        const data = await mockMenuApiHandlers.getMenuDetail(id)
        return createMockResponse(data)
      }

      // 匹配创建菜单接口
      if (url.match(/\/api\/menus$/) && config.method === 'post') {
        const data = await mockMenuApiHandlers.createMenu(config.data)
        return createMockResponse(data)
      }

      // 匹配更新菜单接口
      if (url.match(/\/api\/menus\/\d+$/) && config.method === 'put') {
        const id = url.split('/').pop() || ''
        const data = await mockMenuApiHandlers.updateMenu(id, config.data)
        return createMockResponse(data)
      }

      // 匹配删除菜单接口
      if (url.match(/\/api\/menus\/\d+$/) && config.method === 'delete') {
        const id = url.split('/').pop() || ''
        const data = await mockMenuApiHandlers.deleteMenu(id)
        return createMockResponse(data)
      }

      // 匹配获取父级菜单选项接口
      if (url.match(/\/api\/menus\/parent-options$/) && config.method === 'get') {
        const data = await mockMenuApiHandlers.getParentOptions()
        return createMockResponse(data)
      }

      // 匹配切换菜单状态接口
      if (url.match(/\/api\/menus\/\d+\/status$/) && config.method === 'patch') {
        const id = url.split('/').slice(0, -1).pop() || ''
        const data = await mockMenuApiHandlers.toggleMenuStatus(id, config.data)
        return createMockResponse(data)
      }

      // 匹配获取菜单路由接口
      if (url.match(/\/api\/menus\/routes$/) && config.method === 'get') {
        const data = await mockMenuApiHandlers.getMenuRoutes()
        return createMockResponse(data)
      }

      // 其他请求不处理
      return null
    }
  }
}

/**
 * 创建Mock响应
 */
function createMockResponse(data: any): AxiosResponse {
  return {
    data,
    status: 200,
    statusText: 'OK',
    headers: {},
    config: {} as any,
  } as AxiosResponse
}

/**
 * 安装Mock适配器到axios实例
 */
export function installMenuMockAdapter(axiosInstance: any) {
  const adapter = createMenuMockAdapter()

  // 请求拦截器
  axiosInstance.interceptors.request.use(
    async (config: InternalAxiosRequestConfig) => {
      const mockResponse = await adapter.handler(config)

      if (mockResponse) {
        // 返回一个会立即resolve的Promise
        return Promise.reject({
          $mock: true,
          response: mockResponse,
        })
      }

      return config
    },
    (error) => Promise.reject(error)
  )

  // 响应拦截器 - 处理Mock响应
  axiosInstance.interceptors.response.use(
    (response) => response,
    (error) => {
      // 如果是Mock响应,直接返回
      if (error.$mock) {
        return error.response
      }
      return Promise.reject(error)
    }
  )
}
