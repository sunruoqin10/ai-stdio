/**
 * 菜单管理模块 API接口
 */

import { http } from '@/utils/request'
import type { MenuItem, MenuForm, MenuQuery, RouteMenuItem } from '../types'

/**
 * 获取菜单列表(树形结构)
 * @param params 查询参数
 * @returns 菜单列表
 */
export function getMenuList(params?: MenuQuery) {
  return http.get<MenuItem[]>('/menus', { params })
}

/**
 * 获取菜单详情
 * @param id 菜单ID
 * @returns 菜单详情
 */
export function getMenuDetail(id: number) {
  return http.get<MenuItem>(`/menus/${id}`)
}

/**
 * 创建菜单
 * @param data 菜单表单数据
 * @returns 创建的菜单
 */
export function createMenu(data: MenuForm) {
  return http.post<MenuItem>('/menus', data)
}

/**
 * 更新菜单
 * @param id 菜单ID
 * @param data 菜单表单数据
 * @returns 更新的菜单
 */
export function updateMenu(id: number, data: MenuForm) {
  return http.put<MenuItem>(`/menus/${id}`, data)
}

/**
 * 删除菜单
 * @param id 菜单ID
 * @returns 删除结果
 */
export function deleteMenu(id: number) {
  return http.delete(`/menus/${id}`)
}

/**
 * 获取父级菜单选项
 * @returns 父级菜单列表
 */
export function getParentOptions() {
  return http.get<MenuItem[]>('/menus/parent-options')
}

/**
 * 切换菜单状态
 * @param id 菜单ID
 * @param status 状态
 * @returns 切换结果
 */
export function toggleMenuStatus(id: number, status: boolean) {
  return http.patch(`/menus/${id}/status`, { status })
}

/**
 * 获取菜单路由
 * @returns 路由菜单列表
 */
export function getMenuRoutes() {
  return http.get<RouteMenuItem[]>('/menus/routes')
}
