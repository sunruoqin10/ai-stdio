/**
 * 数据字典缓存管理工具
 * @module dict/utils/cache
 */

import type { DictData } from '../types'

/**
 * 字典缓存项
 */
interface CacheItem {
  data: DictData
  expires: number
}

/**
 * 字典缓存管理类
 */
class DictCacheManager {
  private cache: Map<string, CacheItem> = new Map()
  private readonly CACHE_TTL = 3600000 // 1小时
  private cleanupTimer: number | null = null

  constructor() {
    // 启动定时清理过期缓存
    this.startCleanup()
  }

  /**
   * 获取缓存数据
   * @param dictTypeCode 字典类型编码
   * @returns 字典数据或null
   */
  get(dictTypeCode: string): DictData | null {
    const cacheKey = `dict:${dictTypeCode}`
    const cached = this.cache.get(cacheKey)

    if (!cached) {
      return null
    }

    // 检查是否过期
    if (Date.now() > cached.expires) {
      this.cache.delete(cacheKey)
      return null
    }

    return cached.data
  }

  /**
   * 设置缓存
   * @param dictTypeCode 字典类型编码
   * @param data 字典数据
   */
  set(dictTypeCode: string, data: DictData): void {
    const cacheKey = `dict:${dictTypeCode}`
    this.cache.set(cacheKey, {
      data,
      expires: Date.now() + this.CACHE_TTL
    })
  }

  /**
   * 清除缓存
   * @param dictTypeCode 字典类型编码(不传则清除全部)
   */
  clear(dictTypeCode?: string): void {
    if (dictTypeCode) {
      const cacheKey = `dict:${dictTypeCode}`
      this.cache.delete(cacheKey)
    } else {
      this.cache.clear()
    }
  }

  /**
   * 批量清除多个字典类型的缓存
   * @param dictTypeCodes 字典类型编码列表
   */
  clearBatch(dictTypeCodes: string[]): void {
    dictTypeCodes.forEach(code => {
      this.clear(code)
    })
  }

  /**
   * 清除所有过期缓存
   */
  clearExpired(): void {
    const now = Date.now()
    for (const [key, value] of this.cache.entries()) {
      if (now > value.expires) {
        this.cache.delete(key)
      }
    }
  }

  /**
   * 检查缓存是否存在且未过期
   * @param dictTypeCode 字典类型编码
   * @returns 是否有效
   */
  has(dictTypeCode: string): boolean {
    return this.get(dictTypeCode) !== null
  }

  /**
   * 获取缓存大小
   * @returns 缓存项数量
   */
  size(): number {
    return this.cache.size
  }

  /**
   * 启动定时清理过期缓存
   */
  private startCleanup(): void {
    // 每分钟清理一次过期缓存
    this.cleanupTimer = window.setInterval(() => {
      this.clearExpired()
    }, 60000)
  }

  /**
   * 停止定时清理
   */
  destroy(): void {
    if (this.cleanupTimer !== null) {
      clearInterval(this.cleanupTimer)
      this.cleanupTimer = null
    }
    this.cache.clear()
  }

  /**
   * 获取所有缓存键
   * @returns 缓存键列表
   */
  keys(): string[] {
    return Array.from(this.cache.keys())
  }

  /**
   * 获取缓存统计信息
   * @returns 缓存统计
   */
  getStats(): {
    size: number
    keys: string[]
    expiredCount: number
  } {
    const now = Date.now()
    let expiredCount = 0

    for (const value of this.cache.values()) {
      if (now > value.expires) {
        expiredCount++
      }
    }

    return {
      size: this.cache.size,
      keys: this.keys(),
      expiredCount
    }
  }
}

// 导出单例
export const dictCacheManager = new DictCacheManager()

/**
 * 获取字典数据(带缓存)
 * @param dictTypeCode 字典类型编码
 * @param fetchFn 获取数据的函数
 * @returns 字典数据
 */
export async function getDictDataWithCache(
  dictTypeCode: string,
  fetchFn: () => Promise<DictData>
): Promise<DictData> {
  // 尝试从缓存获取
  const cached = dictCacheManager.get(dictTypeCode)
  if (cached) {
    return cached
  }

  // 缓存未命中,请求API
  const data = await fetchFn()

  // 设置缓存
  dictCacheManager.set(dictTypeCode, data)

  return data
}
