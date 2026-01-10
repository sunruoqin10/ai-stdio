/**
 * 数据字典 API 服务
 * @module dict/api
 */

// 开发环境使用Mock API
import * as mockApi from './mock'

// 生产环境使用真实API
// import request from '@/utils/request'

import type {
  DictType,
  DictItem,
  DictTypeForm,
  DictItemForm,
  DictFilter,
  DictTreeNode,
  DictData,
  PageResponse
} from '../types'

/**
 * 获取字典类型列表
 */
export function getDictTypeList(params: DictFilter & {
  page: number
  pageSize: number
}) {
  // 开发环境使用mock
  return mockApi.mockGetDictTypeList(params)

  // 生产环境使用真实API
  // return request.get<PageResponse<DictType>>('/api/dict/types', { params })
}

/**
 * 获取字典类型详情
 */
export function getDictTypeDetail(id: string) {
  return mockApi.mockGetDictTypeDetail(id)
}

/**
 * 创建字典类型
 */
export function createDictType(data: DictTypeForm) {
  return mockApi.mockCreateDictType(data)
}

/**
 * 更新字典类型
 */
export function updateDictType(id: string, data: Partial<DictTypeForm>) {
  return mockApi.mockUpdateDictType(id, data)
}

/**
 * 删除字典类型
 */
export function deleteDictType(id: string) {
  return mockApi.mockDeleteDictType(id)
}

/**
 * 获取字典项列表
 */
export function getDictItemList(params: DictFilter & {
  dictTypeCode?: string
  page: number
  pageSize: number
}) {
  return mockApi.mockGetDictItemList(params)
}

/**
 * 获取字典项详情
 */
export function getDictItemDetail(id: string) {
  return mockApi.mockGetDictItemDetail(id)
}

/**
 * 创建字典项
 */
export function createDictItem(data: DictItemForm) {
  return mockApi.mockCreateDictItem(data)
}

/**
 * 更新字典项
 */
export function updateDictItem(id: string, data: Partial<DictItemForm>) {
  return mockApi.mockUpdateDictItem(id, data)
}

/**
 * 删除字典项
 */
export function deleteDictItem(id: string) {
  return mockApi.mockDeleteDictItem(id)
}

/**
 * 批量更新字典项排序
 */
export function updateDictItemSort(items: Array<{ id: string; sortOrder: number }>) {
  return mockApi.mockUpdateDictItemSort(items)
}

/**
 * 批量删除字典项
 */
export function batchDeleteDictItems(ids: string[]) {
  return mockApi.mockBatchDeleteDictItems(ids)
}

/**
 * 批量启用/禁用字典项
 */
export function batchUpdateDictItemStatus(ids: string[], status: 'enabled' | 'disabled') {
  return mockApi.mockBatchUpdateDictItemStatus(ids, status)
}

/**
 * 获取字典树
 */
export function getDictTree() {
  return mockApi.mockGetDictTree()
}

/**
 * 检查字典编码是否存在
 */
export function checkDictCodeExists(code: string, excludeId?: string) {
  return mockApi.mockCheckDictCodeExists(code, excludeId)
}

/**
 * 检查字典项值是否存在
 */
export function checkDictValueExists(dictTypeCode: string, value: string, excludeId?: string) {
  return mockApi.mockCheckDictValueExists(dictTypeCode, value, excludeId)
}
