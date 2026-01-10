/**
 * 数据字典 Mock API 适配器
 * 用于在开发阶段模拟后端API响应
 */

import type {
  DictType,
  DictItem,
  DictTreeNode,
  PageResponse
} from '../types'
import { mockDictTypes, mockDictItems } from '../mock/data'

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
 * 获取字典类型列表
 */
export async function mockGetDictTypeList(params: {
  page: number
  pageSize: number
  keyword?: string
  category?: 'system' | 'business'
  status?: 'enabled' | 'disabled'
}) {
  let filtered = [...mockDictTypes]

  // 关键词过滤
  if (params.keyword) {
    const keyword = params.keyword.toLowerCase()
    filtered = filtered.filter(
      dt => dt.code.toLowerCase().includes(keyword) ||
             dt.name.toLowerCase().includes(keyword)
    )
  }

  // 类别过滤
  if (params.category) {
    filtered = filtered.filter(dt => dt.category === params.category)
  }

  // 状态过滤
  if (params.status) {
    filtered = filtered.filter(dt => dt.status === params.status)
  }

  // 排序
  filtered.sort((a, b) => (a.sortOrder || 0) - (b.sortOrder || 0))

  // 分页
  const start = (params.page - 1) * params.pageSize
  const end = start + params.pageSize
  const list = filtered.slice(start, end)

  return mockResponse<PageResponse<DictType>>({
    list,
    total: filtered.length,
    page: params.page,
    pageSize: params.pageSize
  })
}

/**
 * 获取字典类型详情
 */
export async function mockGetDictTypeDetail(id: string) {
  const dictType = mockDictTypes.find(dt => dt.id === id)
  if (!dictType) {
    throw new Error('字典类型不存在')
  }
  return mockResponse(dictType)
}

/**
 * 创建字典类型
 */
export async function mockCreateDictType(data: any) {
  const newDictType: DictType = {
    id: `dict_type_${Date.now()}`,
    code: data.code,
    name: data.name,
    description: data.description,
    category: data.category,
    status: data.status || 'enabled',
    sortOrder: data.sortOrder || 0,
    itemCount: 0,
    extProps: data.extProps || {},
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString()
  }
  mockDictTypes.push(newDictType)
  return mockResponse({ id: newDictType.id })
}

/**
 * 更新字典类型
 */
export async function mockUpdateDictType(id: string, data: any) {
  const index = mockDictTypes.findIndex(dt => dt.id === id)
  if (index === -1) {
    throw new Error('字典类型不存在')
  }

  mockDictTypes[index] = {
    ...mockDictTypes[index],
    ...data,
    updatedAt: new Date().toISOString()
  }

  return mockResponse(mockDictTypes[index])
}

/**
 * 删除字典类型
 */
export async function mockDeleteDictType(id: string) {
  const index = mockDictTypes.findIndex(dt => dt.id === id)
  if (index === -1) {
    throw new Error('字典类型不存在')
  }

  // 检查是否有字典项
  const hasItems = mockDictItems.some(item => item.dictTypeId === id)
  if (hasItems) {
    throw new Error('请先删除该字典类型下的所有字典项')
  }

  mockDictTypes.splice(index, 1)
  return mockResponse(null)
}

/**
 * 获取字典项列表
 */
export async function mockGetDictItemList(params: {
  dictTypeCode?: string
  page: number
  pageSize: number
  keyword?: string
  status?: 'enabled' | 'disabled'
}) {
  let filtered = [...mockDictItems]

  // 字典类型过滤
  if (params.dictTypeCode) {
    filtered = filtered.filter(item => item.dictTypeCode === params.dictTypeCode)
  }

  // 关键词过滤
  if (params.keyword) {
    const keyword = params.keyword.toLowerCase()
    filtered = filtered.filter(
      item => item.label.toLowerCase().includes(keyword) ||
              item.value.toLowerCase().includes(keyword)
    )
  }

  // 状态过滤
  if (params.status) {
    filtered = filtered.filter(item => item.status === params.status)
  }

  // 排序
  filtered.sort((a, b) => a.sortOrder - b.sortOrder)

  // 分页
  const start = (params.page - 1) * params.pageSize
  const end = start + params.pageSize
  const list = filtered.slice(start, end)

  return mockResponse<PageResponse<DictItem>>({
    list,
    total: filtered.length,
    page: params.page,
    pageSize: params.pageSize
  })
}

/**
 * 获取字典项详情
 */
export async function mockGetDictItemDetail(id: string) {
  const dictItem = mockDictItems.find(item => item.id === id)
  if (!dictItem) {
    throw new Error('字典项不存在')
  }
  return mockResponse(dictItem)
}

/**
 * 创建字典项
 */
export async function mockCreateDictItem(data: any) {
  const dictType = mockDictTypes.find(dt => dt.id === data.dictTypeId)
  if (!dictType) {
    throw new Error('字典类型不存在')
  }

  const newDictItem: DictItem = {
    id: `dict_item_${Date.now()}`,
    dictTypeId: data.dictTypeId,
    dictTypeCode: dictType.code,
    label: data.label,
    value: data.value,
    colorType: data.colorType,
    sortOrder: data.sortOrder || 10,
    status: data.status || 'enabled',
    extProps: data.extProps || {},
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString()
  }

  mockDictItems.push(newDictItem)

  // 更新字典类型的itemCount
  dictType.itemCount = (dictType.itemCount || 0) + 1

  return mockResponse({ id: newDictItem.id })
}

/**
 * 更新字典项
 */
export async function mockUpdateDictItem(id: string, data: any) {
  const index = mockDictItems.findIndex(item => item.id === id)
  if (index === -1) {
    throw new Error('字典项不存在')
  }

  mockDictItems[index] = {
    ...mockDictItems[index],
    ...data,
    updatedAt: new Date().toISOString()
  }

  return mockResponse(mockDictItems[index])
}

/**
 * 删除字典项
 */
export async function mockDeleteDictItem(id: string) {
  const index = mockDictItems.findIndex(item => item.id === id)
  if (index === -1) {
    throw new Error('字典项不存在')
  }

  const item = mockDictItems[index]
  const dictType = mockDictTypes.find(dt => dt.id === item.dictTypeId)
  if (dictType) {
    dictType.itemCount = Math.max(0, (dictType.itemCount || 0) - 1)
  }

  mockDictItems.splice(index, 1)
  return mockResponse(null)
}

/**
 * 批量更新字典项排序
 */
export async function mockUpdateDictItemSort(items: Array<{ id: string; sortOrder: number }>) {
  items.forEach(({ id, sortOrder }) => {
    const item = mockDictItems.find(i => i.id === id)
    if (item) {
      item.sortOrder = sortOrder
    }
  })

  return mockResponse({ success: true })
}

/**
 * 批量删除字典项
 */
export async function mockBatchDeleteDictItems(ids: string[]) {
  ids.forEach(id => {
    const index = mockDictItems.findIndex(item => item.id === id)
    if (index !== -1) {
      mockDictItems.splice(index, 1)
    }
  })

  return mockResponse(null)
}

/**
 * 批量更新字典项状态
 */
export async function mockBatchUpdateDictItemStatus(
  ids: string[],
  status: 'enabled' | 'disabled'
) {
  ids.forEach(id => {
    const item = mockDictItems.find(i => i.id === id)
    if (item) {
      item.status = status
    }
  })

  return mockResponse(null)
}

/**
 * 获取字典树
 */
export async function mockGetDictTree() {
  const tree: DictTreeNode[] = mockDictTypes.map(dt => ({
    id: dt.id,
    code: dt.code,
    name: dt.name,
    category: dt.category,
    itemCount: dt.itemCount || 0,
    children: mockDictItems
      .filter(item => item.dictTypeCode === dt.code)
      .sort((a, b) => a.sortOrder - b.sortOrder)
  }))

  return mockResponse(tree)
}

/**
 * 检查字典编码是否存在
 */
export async function mockCheckDictCodeExists(code: string, excludeId?: string) {
  const exists = mockDictTypes.some(
    dt => dt.code === code && dt.id !== excludeId
  )
  return mockResponse(exists)
}

/**
 * 检查字典项值是否存在
 */
export async function mockCheckDictValueExists(
  dictTypeCode: string,
  value: string,
  excludeId?: string
) {
  const dictType = mockDictTypes.find(dt => dt.code === dictTypeCode)
  if (!dictType) {
    return mockResponse(false)
  }

  const exists = mockDictItems.some(
    item => item.dictTypeId === dictType.id &&
            item.value === value &&
            item.id !== excludeId
  )

  return mockResponse(exists)
}
