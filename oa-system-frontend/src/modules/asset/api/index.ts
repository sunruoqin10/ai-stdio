/**
 * 资产管理模块 API
 * @module asset/api
 */

import type {
  Asset,
  AssetFilter,
  AssetForm,
  BorrowRecord,
  BorrowForm,
  ReturnForm,
  AssetStatistics,
  DepreciationTrend,
  BorrowTrend,
  ImportResult,
  PaginationResponse
} from '../types'
import { mockAssets, mockBorrowRecords, mockStatistics } from '../mock/data'
import { calculateCurrentValue, generateAssetId } from '../utils'

/**
 * 模拟延迟
 */
function delay(ms: number = 500): Promise<void> {
  return new Promise(resolve => setTimeout(resolve, ms))
}

/**
 * 获取资产列表
 */
export async function getAssets(
  params?: AssetFilter & { page?: number; pageSize?: number; view?: string }
): Promise<PaginationResponse<Asset> | Asset[]> {
  await delay(300)

  let filteredAssets = [...mockAssets]

  // 筛选
  if (params?.keyword) {
    const keyword = params.keyword.toLowerCase()
    filteredAssets = filteredAssets.filter(
      asset =>
        asset.name.toLowerCase().includes(keyword) ||
        asset.id.toLowerCase().includes(keyword)
    )
  }

  if (params?.category) {
    filteredAssets = filteredAssets.filter(asset => asset.category === params.category)
  }

  if (params?.status) {
    filteredAssets = filteredAssets.filter(asset => asset.status === params.status)
  }

  if (params?.userId) {
    filteredAssets = filteredAssets.filter(asset => asset.userId === params.userId)
  }

  // 分页
  if (params?.page !== undefined && params?.pageSize !== undefined) {
    const start = (params.page - 1) * params.pageSize
    const end = start + params.pageSize
    const paginatedList = filteredAssets.slice(start, end)

    return {
      list: paginatedList,
      total: filteredAssets.length,
      page: params.page,
      pageSize: params.pageSize
    }
  }

  return filteredAssets
}

/**
 * 获取资产详情
 */
export async function getAsset(id: string): Promise<Asset> {
  await delay(200)
  const asset = mockAssets.find(a => a.id === id)
  if (!asset) {
    throw new Error('资产不存在')
  }
  return asset
}

/**
 * 创建资产
 */
export async function createAsset(data: AssetForm): Promise<Asset> {
  await delay(400)

  // 生成新ID
  const newId = generateAssetId(mockAssets.length + 1)

  const newAsset: Asset = {
    id: newId,
    name: data.name!,
    category: data.category!,
    brandModel: data.brandModel,
    purchaseDate: data.purchaseDate!,
    purchasePrice: data.purchasePrice!,
    currentValue: calculateCurrentValue(data.purchasePrice!, data.purchaseDate!),
    status: 'stock',
    location: data.location,
    images: data.images,
    notes: data.notes,
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString()
  }

  mockAssets.unshift(newAsset)
  return newAsset
}

/**
 * 更新资产
 */
export async function updateAsset(id: string, data: AssetForm): Promise<Asset> {
  await delay(400)

  const index = mockAssets.findIndex(a => a.id === id)
  if (index === -1) {
    throw new Error('资产不存在')
  }

  const updatedAsset: Asset = {
    ...mockAssets[index],
    ...data,
    currentValue: data.purchasePrice
      ? calculateCurrentValue(data.purchasePrice, data.purchaseDate!)
      : mockAssets[index].currentValue,
    updatedAt: new Date().toISOString()
  }

  mockAssets[index] = updatedAsset
  return updatedAsset
}

/**
 * 删除资产
 */
export async function deleteAsset(id: string): Promise<void> {
  await delay(300)

  const index = mockAssets.findIndex(a => a.id === id)
  if (index === -1) {
    throw new Error('资产不存在')
  }

  mockAssets.splice(index, 1)
}

/**
 * 借出资产
 */
export async function borrowAsset(
  id: string,
  data: BorrowForm
): Promise<{ asset: Asset; borrowRecord: BorrowRecord }> {
  await delay(400)

  const index = mockAssets.findIndex(a => a.id === id)
  if (index === -1) {
    throw new Error('资产不存在')
  }

  const asset = mockAssets[index]
  if (asset.status !== 'stock') {
    throw new Error('资产状态不允许借用')
  }

  // 更新资产状态
  const updatedAsset: Asset = {
    ...asset,
    status: 'borrowed',
    userId: data.borrowerId,
    borrowDate: new Date().toISOString(),
    expectedReturnDate: data.expectedReturnDate,
    updatedAt: new Date().toISOString()
  }

  mockAssets[index] = updatedAsset

  // 创建借还记录
  const borrowRecord: BorrowRecord = {
    id: `BR${Date.now()}`,
    assetId: asset.id,
    assetName: asset.name,
    borrowerId: data.borrowerId,
    borrowerName: data.borrowerId, // 实际应该从用户服务获取
    borrowDate: new Date().toISOString(),
    expectedReturnDate: data.expectedReturnDate,
    status: 'active',
    notes: data.notes,
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString()
  }

  mockBorrowRecords.unshift(borrowRecord)

  return { asset: updatedAsset, borrowRecord }
}

/**
 * 归还资产
 */
export async function returnAsset(
  id: string,
  data: ReturnForm
): Promise<{ asset: Asset; borrowRecord: BorrowRecord }> {
  await delay(400)

  const index = mockAssets.findIndex(a => a.id === id)
  if (index === -1) {
    throw new Error('资产不存在')
  }

  const asset = mockAssets[index]
  if (asset.status !== 'borrowed') {
    throw new Error('资产状态不允许归还')
  }

  // 更新资产状态
  const updatedAsset: Asset = {
    ...asset,
    status: 'stock',
    userId: undefined,
    userName: undefined,
    userAvatar: undefined,
    borrowDate: undefined,
    expectedReturnDate: undefined,
    actualReturnDate: new Date().toISOString(),
    updatedAt: new Date().toISOString()
  }

  mockAssets[index] = updatedAsset

  // 更新借还记录
  const recordIndex = mockBorrowRecords.findIndex(
    r => r.assetId === id && r.status === 'active'
  )
  if (recordIndex !== -1) {
    mockBorrowRecords[recordIndex] = {
      ...mockBorrowRecords[recordIndex],
      actualReturnDate: new Date().toISOString(),
      status: 'returned',
      condition: data.condition,
      notes: data.notes,
      updatedAt: new Date().toISOString()
    }
  }

  return { asset: updatedAsset, borrowRecord: mockBorrowRecords[recordIndex] }
}

/**
 * 获取借还历史
 */
export async function getBorrowHistory(
  assetId: string,
  page?: number,
  pageSize?: number
): Promise<{ list: BorrowRecord[]; total: number }> {
  await delay(200)

  let records = mockBorrowRecords.filter(r => r.assetId === assetId)

  const total = records.length

  if (page !== undefined && pageSize !== undefined) {
    const start = (page - 1) * pageSize
    const end = start + pageSize
    records = records.slice(start, end)
  }

  return { list: records, total }
}

/**
 * 获取资产统计
 */
export async function getStatistics(): Promise<AssetStatistics> {
  await delay(300)
  return { ...mockStatistics }
}

/**
 * 获取折旧趋势
 */
export async function getDepreciationTrend(months: number = 12): Promise<DepreciationTrend> {
  await delay(300)

  const monthData: string[] = []
  const values: number[] = []

  const now = new Date()
  for (let i = months - 1; i >= 0; i--) {
    const date = new Date(now.getFullYear(), now.getMonth() - i, 1)
    monthData.push(`${date.getMonth() + 1}月`)
    values.push(Math.round(100000 + Math.random() * 50000))
  }

  return { months: monthData, values }
}

/**
 * 获取借出趋势
 */
export async function getBorrowTrend(months: number = 12): Promise<BorrowTrend> {
  await delay(300)

  const monthData: string[] = []
  const counts: number[] = []

  const now = new Date()
  for (let i = months - 1; i >= 0; i--) {
    const date = new Date(now.getFullYear(), now.getMonth() - i, 1)
    monthData.push(`${date.getMonth() + 1}月`)
    counts.push(Math.floor(Math.random() * 20))
  }

  return { months: monthData, counts }
}
