/**
 * 资产管理工具函数
 * @module asset/utils
 */

import type { Asset } from '../types'

/**
 * 计算资产当前价值
 * @param purchasePrice 购置金额
 * @param purchaseDate 购置日期
 * @param depreciationRate 月折旧率 (默认1%)
 * @returns 当前价值
 */
export function calculateCurrentValue(
  purchasePrice: number,
  purchaseDate: string,
  depreciationRate: number = 0.01
): number {
  const purchase = new Date(purchaseDate)
  const now = new Date()

  // 计算月份差异
  const monthsDiff = (now.getFullYear() - purchase.getFullYear()) * 12 + (now.getMonth() - purchase.getMonth())

  // 购置当月不计折旧
  const months = Math.max(0, monthsDiff - 1)

  // 计算折旧
  const currentValue = purchasePrice * (1 - depreciationRate * months)

  // 不会出现负值,保留两位小数
  return Math.max(0, Math.round(currentValue * 100) / 100)
}

/**
 * 检查是否需要提醒归还
 * @param asset 资产对象
 * @param reminderDays 提前提醒天数 (默认3天)
 * @returns 是否需要提醒
 */
export function checkReturnReminder(
  asset: Asset,
  reminderDays: number = 3
): boolean {
  // 只有已借出且有预计归还日期的资产才需要检查
  if (asset.status !== 'borrowed' || !asset.expectedReturnDate) {
    return false
  }

  const today = new Date()
  today.setHours(0, 0, 0, 0)

  const dueDate = new Date(asset.expectedReturnDate)
  dueDate.setHours(0, 0, 0, 0)

  // 计算距离到期的天数
  const daysUntilDue = Math.floor((dueDate.getTime() - today.getTime()) / (1000 * 60 * 60 * 24))

  // 在提醒范围内(0到reminderDays天)
  return daysUntilDue >= 0 && daysUntilDue <= reminderDays
}

/**
 * 检查是否逾期
 * @param asset 资产对象
 * @returns 是否逾期
 */
export function checkOverdue(asset: Asset): boolean {
  if (asset.status !== 'borrowed' || !asset.expectedReturnDate) {
    return false
  }

  const today = new Date()
  today.setHours(0, 0, 0, 0)

  const dueDate = new Date(asset.expectedReturnDate)
  dueDate.setHours(0, 0, 0, 0)

  return today > dueDate
}

/**
 * 生成资产编号
 * @param serialNumber 序号
 * @returns 资产编号 (格式: ASSET000001)
 */
export function generateAssetId(serialNumber: number): string {
  const prefix = 'ASSET'
  const paddedNumber = String(serialNumber).padStart(6, '0')
  return `${prefix}${paddedNumber}`
}

/**
 * 格式化日期
 * @param date 日期字符串
 * @returns 格式化后的日期字符串
 */
export function formatDate(date: string): string {
  const d = new Date(date)
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

/**
 * 格式化金额
 * @param amount 金额
 * @returns 格式化后的金额字符串
 */
export function formatAmount(amount: number): string {
  return `¥${amount.toFixed(2)}`
}

/**
 * 获取资产类别名称
 * @param category 资产类别
 * @returns 类别名称
 */
export function getCategoryName(category: string): string {
  const categoryMap: Record<string, string> = {
    electronic: '电子设备',
    furniture: '办公家具',
    book: '图书资料',
    other: '其他'
  }
  return categoryMap[category] || category
}

/**
 * 获取资产状态名称
 * @param status 资产状态
 * @returns 状态名称
 */
export function getStatusName(status: string): string {
  const statusMap: Record<string, string> = {
    stock: '库存中',
    in_use: '使用中',
    borrowed: '已借出',
    maintenance: '维修中',
    scrapped: '已报废'
  }
  return statusMap[status] || status
}

/**
 * 获取资产状态类型(Element Plus Tag类型)
 * @param status 资产状态
 * @returns Tag类型
 */
export function getStatusType(status: string): string {
  const typeMap: Record<string, string> = {
    stock: 'success',
    in_use: 'primary',
    borrowed: 'warning',
    maintenance: 'danger',
    scrapped: 'info'
  }
  return typeMap[status] || 'info'
}

/**
 * 验证状态转换是否合法
 * @param currentStatus 当前状态
 * @param newStatus 新状态
 * @returns 是否合法
 */
export function isValidStatusTransition(currentStatus: string, newStatus: string): boolean {
  const validTransitions: Record<string, string[]> = {
    stock: ['in_use', 'borrowed', 'maintenance', 'scrapped'],
    in_use: ['stock', 'maintenance', 'scrapped'],
    borrowed: ['stock'],
    maintenance: ['stock', 'in_use', 'scrapped'],
    scrapped: []
  }

  return validTransitions[currentStatus]?.includes(newStatus) ?? false
}

/**
 * 按状态分组资产
 * @param assets 资产列表
 * @returns 按状态分组的资产
 */
export function groupAssetsByStatus(assets: Asset[]): Record<string, Asset[]> {
  return assets.reduce((acc, asset) => {
    if (!acc[asset.status]) {
      acc[asset.status] = []
    }
    acc[asset.status].push(asset)
    return acc
  }, {} as Record<string, Asset[]>)
}

/**
 * 获取图片的完整URL
 * @param url 图片URL (可能是相对路径)
 * @returns 完整的图片URL
 */
export function getImageUrl(url?: string): string {
  if (!url) {
    return ''
  }

  if (url.startsWith('http://') || url.startsWith('https://')) {
    return url
  }

  if (url.startsWith('/api/uploads/')) {
    return url
  }

  if (url.startsWith('/uploads/')) {
    return `/api${url}`
  }

  return `/api/uploads/${url}`
}

/**
 * 获取图片URL数组
 * @param urls 图片URL数组
 * @returns 完整的图片URL数组
 */
export function getImageUrls(urls?: string[]): string[] {
  if (!urls || !Array.isArray(urls)) {
    return []
  }
  return urls.map(url => getImageUrl(url))
}
