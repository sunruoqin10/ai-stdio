/**
 * 数据字典 API 服务
 * @module dict/api
 */

// 生产环境使用真实API
import { http } from '@/utils/request'

// 开发环境使用Mock API
// import * as mockApi from './mock'

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
import { parseExtProps } from '../utils'

// MyBatis-Plus IPage 接口
interface IPage<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

// 将 MyBatis-Plus IPage 转换为前端 PageResponse
function convertIPageToPageResponse<T>(iPage: IPage<T>): PageResponse<T> {
  return {
    list: iPage.records,
    total: iPage.total,
    page: iPage.current,
    pageSize: iPage.size
  }
}

// 转换字典类型数据，将 extProps 字符串转换为对象
function convertDictType(dictType: any): DictType {
  return {
    ...dictType,
    extProps: parseExtProps(dictType.extProps)
  }
}

// 转换字典项数据，将 extProps 字符串转换为对象
function convertDictItem(dictItem: any): DictItem {
  return {
    ...dictItem,
    extProps: parseExtProps(dictItem.extProps)
  }
}

/**
 * 获取字典类型列表
 */
export async function getDictTypeList(params: DictFilter & {
  page: number
  pageSize: number
}) {
  const result = await http.get<{
    code: number
    message: string
    data: IPage<DictType>
  }>('/dict/types', { params })
  return {
    ...result,
    data: {
      ...convertIPageToPageResponse(result.data),
      list: result.data.records.map(convertDictType)
    }
  }
}

/**
 * 获取字典类型详情
 */
export async function getDictTypeDetail(id: string) {
  const result = await http.get<{
    code: number
    message: string
    data: DictType
  }>(`/dict/types/${id}`)
  return {
    ...result,
    data: convertDictType(result.data)
  }
}

/**
 * 创建字典类型
 */
export function createDictType(data: DictTypeForm) {
  return http.post<{
    code: number
    message: string
    data: DictType
  }>('/dict/types', data)
}

/**
 * 更新字典类型
 */
export function updateDictType(id: string, data: Partial<DictTypeForm>) {
  return http.put<{
    code: number
    message: string
    data: DictType
  }>(`/dict/types/${id}`, data)
}

/**
 * 删除字典类型
 */
export function deleteDictType(id: string) {
  return http.delete<{
    code: number
    message: string
    data: null
  }>(`/dict/types/${id}`)
}

/**
 * 获取字典项列表
 */
export async function getDictItemList(params: DictFilter & {
  dictTypeCode?: string
  page: number
  pageSize: number
}) {
  const result = await http.get<{
    code: number
    message: string
    data: IPage<DictItem>
  }>('/dict/items', { params })
  return {
    ...result,
    data: {
      ...convertIPageToPageResponse(result.data),
      list: result.data.records.map(convertDictItem)
    }
  }
}

/**
 * 获取字典项详情
 */
export async function getDictItemDetail(id: string) {
  const result = await http.get<{
    code: number
    message: string
    data: DictItem
  }>(`/dict/items/${id}`)
  return {
    ...result,
    data: convertDictItem(result.data)
  }
}

/**
 * 创建字典项
 */
export function createDictItem(data: DictItemForm) {
  return http.post<{
    code: number
    message: string
    data: DictItem
  }>('/dict/items', data)
}

/**
 * 更新字典项
 */
export function updateDictItem(id: string, data: Partial<DictItemForm>) {
  return http.put<{
    code: number
    message: string
    data: DictItem
  }>(`/dict/items/${id}`, data)
}

/**
 * 删除字典项
 */
export function deleteDictItem(id: string) {
  return http.delete<{
    code: number
    message: string
    data: null
  }>(`/dict/items/${id}`)
}

/**
 * 批量更新字典项排序
 */
export function updateDictItemSort(dictTypeId: string, items: Array<{ id: string; sortOrder: number }>) {
  return http.put<{
    code: number
    message: string
    data: null
  }>('/dict/items/sort', { dictTypeId, items })
}

/**
 * 批量删除字典项
 */
export function batchDeleteDictItems(ids: string[]) {
  return http.delete<{
    code: number
    message: string
    data: null
  }>('/dict/items/batch', { data: { ids } })
}

/**
 * 批量启用/禁用字典项
 */
export function batchUpdateDictItemStatus(ids: string[], status: 'enabled' | 'disabled') {
  return http.put<{
    code: number
    message: string
    data: null
  }>('/dict/items/batch/status', { ids, status })
}

/**
 * 获取字典树
 */
export function getDictTree() {
  return http.get<{
    code: number
    message: string
    data: DictTreeNode[]
  }>('/dict/tree')
}

/**
 * 检查字典编码是否存在
 */
export function checkDictCodeExists(code: string, excludeId?: string) {
  return http.get<{
    code: number
    message: string
    data: boolean
  }>('/dict/types/check-code', {
    params: { code, excludeId }
  })
}

/**
 * 检查字典项值是否存在
 */
export function checkDictValueExists(dictTypeId: string, value: string, excludeId?: string) {
  return http.get<{
    code: number
    message: string
    data: boolean
  }>('/dict/items/check-value', {
    params: { dictTypeId, value, excludeId }
  })
}

/**
 * 根据字典类型编码获取字典数据
 */
export function getDictData(dictTypeCode: string) {
  return http.get<{
    code: number
    message: string
    data: DictData
  }>(`/dict/${dictTypeCode}`)
}

/**
 * 清除字典缓存
 */
export function clearDictCache(dictTypeCode?: string) {
  return http.delete<{
    code: number
    message: string
    data: null
  }>(dictTypeCode ? `/dict/cache/${dictTypeCode}` : '/dict/cache')
}

/**
 * 导入字典数据
 */
export function importDict(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return http.post<{
    code: number
    message: string
    data: any
  }>('/dict/import', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 导出字典数据
 */
export function exportDict(dictTypeCodes?: string[]) {
  return http.get<Blob>('/dict/export', {
    params: { dictTypeCodes: dictTypeCodes?.join(',') },
    responseType: 'blob'
  })
}
