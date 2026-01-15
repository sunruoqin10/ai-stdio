/**
 * 数据字典工具函数
 * @module dict/utils
 */

import type { DictItem } from '../types'

type DictColorType = 'primary' | 'success' | 'warning' | 'danger' | 'info'

/**
 * 字典颜色类型对应的Element Plus颜色
 */
export const DICT_COLOR_MAP: Record<DictColorType, string> = {
  primary: '#409EFF',
  success: '#67C23A',
  warning: '#E6A23C',
  danger: '#F56C6C',
  info: '#909399'
}

/**
 * 获取字典颜色值
 * @param colorType 颜色类型
 * @returns 颜色值
 */
export function getDictColor(colorType?: DictColorType): string {
  return colorType ? DICT_COLOR_MAP[colorType] : DICT_COLOR_MAP.info
}

/**
 * 获取字典颜色样式
 * @param colorType 颜色类型
 * @returns 颜色样式对象
 */
export function getDictColorStyle(colorType?: DictColorType): {
  backgroundColor: string
  color: string
} {
  return {
    backgroundColor: getDictColor(colorType),
    color: '#fff'
  }
}

/**
 * 高亮搜索关键词
 * @param text 原文本
 * @param keyword 关键词
 * @returns 高亮后的HTML
 */
export function highlightKeyword(text: string, keyword: string): string {
  if (!keyword) return text

  const regex = new RegExp(`(${keyword})`, 'gi')
  return text.replace(regex, '<mark>$1</mark>')
}

/**
 * 根据值查找字典项
 * @param items 字典项列表
 * @param value 字典项值
 * @returns 字典项或undefined
 */
export function findDictItemByValue(
  items: DictItem[],
  value: string
): DictItem | undefined {
  return items.find(item => item.value === value)
}

/**
 * 根据值获取字典项标签
 * @param items 字典项列表
 * @param value 字典项值
 * @returns 字典项标签
 */
export function getDictItemLabel(items: DictItem[], value: string): string {
  const item = findDictItemByValue(items, value)
  return item?.label || value
}

/**
 * 过滤启用的字典项
 * @param items 字典项列表
 * @returns 启用的字典项列表
 */
export function filterEnabledDictItems(items: DictItem[]): DictItem[] {
  return items.filter(item => item.status === 'enabled')
}

/**
 * 按排序序号排序字典项
 * @param items 字典项列表
 * @returns 排序后的字典项列表
 */
export function sortDictItems(items: DictItem[]): DictItem[] {
  return [...items].sort((a, b) => a.sortOrder - b.sortOrder)
}

/**
 * 将字典项转换为选项列表
 * @param items 字典项列表
 * @returns 选项列表
 */
export function dictItemsToOptions(
  items: DictItem[]
): Array<{ label: string; value: string; colorType?: 'primary' | 'success' | 'warning' | 'danger' | 'info' }> {
  return items.map(item => ({
    label: item.label,
    value: item.value,
    colorType: item.colorType
  }))
}

/**
 * 验证字典编码格式
 * @param code 字典编码
 * @returns 是否有效
 */
export function isValidDictCode(code: string): boolean {
  return /^[a-z][a-z0-9_]*$/.test(code)
}

/**
 * 格式化字典类别文本
 * @param category 字典类别
 * @returns 格式化后的文本
 */
export function formatDictCategory(category: 'system' | 'business'): string {
  return category === 'system' ? '系统字典' : '业务字典'
}

/**
 * 格式化字典状态文本
 * @param status 字典状态
 * @returns 格式化后的文本
 */
export function formatDictStatus(status: 'enabled' | 'disabled'): string {
  return status === 'enabled' ? '启用' : '禁用'
}

/**
 * 防抖函数
 * @param fn 要执行的函数
 * @param delay 延迟时间(ms)
 * @returns 防抖后的函数
 */
export function debounce<T extends (...args: any[]) => any>(
  fn: T,
  delay: number
): (...args: Parameters<T>) => void {
  let timeoutId: number | null = null

  return function (this: any, ...args: Parameters<T>) {
    if (timeoutId !== null) {
      clearTimeout(timeoutId)
    }

    timeoutId = window.setTimeout(() => {
      fn.apply(this, args)
      timeoutId = null
    }, delay)
  }
}

/**
 * 生成字典项排序序号
 * @param existingItems 现有字典项列表
 * @returns 新的排序序号
 */
export function generateSortOrder(existingItems: DictItem[]): number {
  if (existingItems.length === 0) {
    return 10
  }

  const maxSortOrder = Math.max(...existingItems.map(item => item.sortOrder))
  return maxSortOrder + 10
}

/**
 * 将 extProps JSON 字符串转换为对象
 * @param extPropsString extProps JSON 字符串
 * @returns extProps 对象
 */
export function parseExtProps(extPropsString: string | null | undefined): Record<string, any> {
  if (!extPropsString) {
    return {}
  }
  try {
    return JSON.parse(extPropsString)
  } catch (error) {
    console.error('Failed to parse extProps:', error)
    return {}
  }
}

/**
 * 将 extProps 对象转换为 JSON 字符串
 * @param extProps extProps 对象
 * @returns extProps JSON 字符串
 */
export function stringifyExtProps(extProps: Record<string, any> | null | undefined): string | null {
  if (!extProps || Object.keys(extProps).length === 0) {
    return null
  }
  try {
    return JSON.stringify(extProps)
  } catch (error) {
    console.error('Failed to stringify extProps:', error)
    return null
  }
}
