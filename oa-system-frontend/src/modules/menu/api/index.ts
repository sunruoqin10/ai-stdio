/**
 * 菜单管理模块 API接口
 */

// 开发环境使用Mock API
import * as mockApi from './mock-adapter'

// 生产环境使用真实API
// import request from '@/utils/request'

import type { MenuItem, MenuForm, MenuQuery, RouteMenuItem } from '../types'

/**
 * 获取菜单列表(树形结构)
 * @param params 查询参数
 * @returns 菜单列表
 */
export function getMenuList(params?: MenuQuery) {
  // 开发环境使用mock
  return mockApi.mockMenuApiHandlers.getMenuList()

  // 生产环境使用真实API
  // return request<MenuItem[]>({
  //   url: '/menus',
  //   method: 'get',
  //   params
  // })
}

/**
 * 获取菜单详情
 * @param id 菜单ID
 * @returns 菜单详情
 */
export function getMenuDetail(id: number) {
  return mockApi.mockMenuApiHandlers.getMenuDetail(String(id))

  // 生产环境使用真实API
  // return request<MenuItem>({
  //   url: `/menus/${id}`,
  //   method: 'get'
  // })
}

/**
 * 创建菜单
 * @param data 菜单表单数据
 * @returns 创建的菜单
 */
export function createMenu(data: MenuForm) {
  return mockApi.mockMenuApiHandlers.createMenu(data)

  // 生产环境使用真实API
  // return request<MenuItem>({
  //   url: '/menus',
  //   method: 'post',
  //   data
  // })
}

/**
 * 更新菜单
 * @param id 菜单ID
 * @param data 菜单表单数据
 * @returns 更新的菜单
 */
export function updateMenu(id: number, data: MenuForm) {
  return mockApi.mockMenuApiHandlers.updateMenu(String(id), data)

  // 生产环境使用真实API
  // return request<MenuItem>({
  //   url: `/menus/${id}`,
  //   method: 'put',
  //   data
  // })
}

/**
 * 删除菜单
 * @param id 菜单ID
 * @returns 删除结果
 */
export function deleteMenu(id: number) {
  return mockApi.mockMenuApiHandlers.deleteMenu(String(id))

  // 生产环境使用真实API
  // return request({
  //   url: `/menus/${id}`,
  //   method: 'delete'
  // })
}

/**
 * 获取父级菜单选项
 * @returns 父级菜单列表
 */
export function getParentOptions() {
  return mockApi.mockMenuApiHandlers.getParentOptions()

  // 生产环境使用真实API
  // return request<MenuItem[]>({
  //   url: '/menus/parent-options',
  //   method: 'get'
  // })
}

/**
 * 切换菜单状态
 * @param id 菜单ID
 * @param status 状态
 * @returns 切换结果
 */
export function toggleMenuStatus(id: number, status: boolean) {
  return mockApi.mockMenuApiHandlers.toggleMenuStatus(String(id), { status })

  // 生产环境使用真实API
  // return request({
  //   url: `/menus/${id}/status`,
  //   method: 'patch',
  //   data: { status }
  // })
}

/**
 * 获取菜单路由
 * @returns 路由菜单列表
 */
export function getMenuRoutes() {
  return mockApi.mockMenuApiHandlers.getMenuRoutes()

  // 生产环境使用真实API
  // return request<RouteMenuItem[]>({
  //   url: '/menus/routes',
  //   method: 'get'
  // })
}
