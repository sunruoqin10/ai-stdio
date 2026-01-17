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
  PaginationResponse
} from '../types'
import { http } from '@/utils/request'

/**
 * API响应接口
 */
interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
}

/**
 * 分页响应接口
 */
interface PageResponse<T> {
  records: T[]
  total: number
  current: number
  size: number
}

/**
 * 获取资产列表
 */
export async function getAssets(
  params?: AssetFilter & { pageNum?: number; pageSize?: number }
): Promise<PaginationResponse<Asset>> {
  const response = await http.get<ApiResponse<PageResponse<Asset>>>('/assets', {
    params: {
      pageNum: params?.pageNum || 1,
      pageSize: params?.pageSize || 10,
      keyword: params?.keyword,
      category: params?.category,
      status: params?.status,
      userId: params?.userId,
      purchaseDateStart: params?.purchaseDateStart,
      purchaseDateEnd: params?.purchaseDateEnd,
      purchasePriceMin: params?.purchasePriceMin,
      purchasePriceMax: params?.purchasePriceMax,
      location: params?.location,
      sortField: params?.sortField || 'created_at',
      sortOrder: params?.sortOrder || 'desc'
    }
  })

  // 处理列表数据，将 images 从 JSON 字符串转换为数组
  const list = response.data.records.map(asset => ({
    ...asset,
    images: asset.images ? (typeof asset.images === 'string' ? JSON.parse(asset.images) : asset.images) : []
  }))

  return {
    list: list,
    total: response.data.total,
    page: response.data.current,
    pageSize: response.data.size
  }
}

/**
 * 获取资产详情
 */
export async function getAsset(id: string): Promise<Asset> {
  const response = await http.get<ApiResponse<Asset>>(`/assets/${id}`)
  return response.data
}

/**
 * 创建资产
 */
export async function createAsset(data: AssetForm): Promise<Asset> {
  const response = await http.post<ApiResponse<Asset>>('/assets', data)
  return response.data
}

/**
 * 更新资产
 */
export async function updateAsset(id: string, data: AssetForm): Promise<Asset> {
  const response = await http.put<ApiResponse<Asset>>(`/assets/${id}`, data)
  return response.data
}

/**
 * 删除资产
 */
export async function deleteAsset(id: string): Promise<void> {
  await http.delete<ApiResponse<void>>(`/assets/${id}`)
}

/**
 * 借出资产
 */
export async function borrowAsset(
  id: string,
  data: BorrowForm
): Promise<Asset> {
  const response = await http.post<ApiResponse<Asset>>(`/assets/${id}/borrow`, data)
  return response.data
}

/**
 * 归还资产
 */
export async function returnAsset(
  id: string,
  data: ReturnForm
): Promise<Asset> {
  const response = await http.post<ApiResponse<Asset>>(`/assets/${id}/return`, data)
  return response.data
}

/**
 * 获取借还历史
 */
export async function getBorrowHistory(
  assetId: string,
  pageNum: number = 1,
  pageSize: number = 10
): Promise<{ list: BorrowRecord[]; total: number }> {
  const response = await http.get<ApiResponse<PageResponse<BorrowRecord>>>(
    `/assets/${assetId}/borrow-history`,
    {
      params: { pageNum, pageSize }
    }
  )

  return {
    list: response.data.records,
    total: response.data.total
  }
}

/**
 * 获取资产统计
 */
export async function getStatistics(): Promise<AssetStatistics> {
  const response = await http.get<ApiResponse<AssetStatistics>>('/assets/statistics')
  return response.data
}

/**
 * 获取折旧趋势（暂未实现后端接口，使用前端计算）
 */
export async function getDepreciationTrend(months: number = 12): Promise<DepreciationTrend> {
  // TODO: 后端可以添加 /assets/depreciation-trend 接口
  // 目前使用统计数据计算
  const statistics = await getStatistics()
  const monthData: string[] = []
  const values: number[] = []

  const now = new Date()
  for (let i = months - 1; i >= 0; i--) {
    const date = new Date(now.getFullYear(), now.getMonth() - i, 1)
    monthData.push(`${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}`)
    // 简单模拟，实际应该从后端获取历史数据
    values.push(statistics.totalDepreciationAmount / months)
  }

  return { months: monthData, values }
}

/**
 * 获取借出趋势（暂未实现后端接口，使用前端计算）
 */
export async function getBorrowTrend(months: number = 12): Promise<BorrowTrend> {
  // TODO: 后端可以添加 /assets/borrow-trend 接口
  const monthData: string[] = []
  const counts: number[] = []

  const now = new Date()
  for (let i = months - 1; i >= 0; i--) {
    const date = new Date(now.getFullYear(), now.getMonth() - i, 1)
    monthData.push(`${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}`)
    // 简单模拟，实际应该从后端获取历史数据
    counts.push(Math.floor(Math.random() * 10))
  }

  return { months: monthData, counts }
}

/**
 * 上传文件
 */
export async function uploadFile(file: File): Promise<string> {
  const formData = new FormData()
  formData.append('file', file)

  const response = await http.post<{
    code: number
    message: string
    url: string
  }>('/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })

  return response.url
}
