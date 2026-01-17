/**
 * 资产管理模块 Store
 * @module asset/store
 */

import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type {
  Asset,
  AssetFilter,
  AssetForm,
  BorrowForm,
  BorrowRecord,
  ReturnForm,
  AssetStatistics
} from '../types'
import * as assetApi from '../api'

export const useAssetStore = defineStore('asset', () => {
  // ==================== 状态 ====================

  const assets = ref<Asset[]>([])
  const currentAsset = ref<Asset | null>(null)
  const borrowRecords = ref<BorrowRecord[]>([])
  const statistics = ref<AssetStatistics | null>(null)
  const loading = ref(false)

  // 筛选条件
  const filter = ref<AssetFilter>({})

  // 分页
  const pagination = ref({
    page: 1,
    pageSize: 20,
    total: 0
  })

  // 当前视图
  const currentView = ref<'table' | 'kanban' | 'gallery'>('table')

  // ==================== 计算属性 ====================

  // 按状态分组的资产(用于看板视图)
  const assetsByStatus = computed(() => {
    const groups: Record<string, Asset[]> = {
      stock: [],
      in_use: [],
      borrowed: [],
      maintenance: [],
      scrapped: []
    }

    assets.value.forEach(asset => {
      if (groups[asset.status]) {
        groups[asset.status].push(asset)
      }
    })

    return groups
  })

  // ==================== Actions ====================

  /**
   * 加载资产列表
   */
  async function loadList(params?: AssetFilter & { page?: number; pageSize?: number }) {
    loading.value = true
    try {
      const result = await assetApi.getAssets({
        ...filter.value,
        ...params,
        page: params?.page || pagination.value.page,
        pageSize: params?.pageSize || pagination.value.pageSize
      })

      if (Array.isArray(result)) {
        assets.value = result
      } else {
        assets.value = result.list
        pagination.value.total = result.total
        pagination.value.page = result.page
        pagination.value.pageSize = result.pageSize
      }
    } finally {
      loading.value = false
    }
  }

  /**
   * 加载资产详情
   */
  async function loadDetail(id: string) {
    loading.value = true
    try {
      currentAsset.value = await assetApi.getAsset(id)
      return currentAsset.value
    } finally {
      loading.value = false
    }
  }

  /**
   * 创建资产
   */
  async function create(data: AssetForm) {
    loading.value = true
    try {
      const newAsset = await assetApi.createAsset(data)
      assets.value.unshift(newAsset)
      return newAsset
    } finally {
      loading.value = false
    }
  }

  /**
   * 更新资产
   */
  async function update(id: string, data: AssetForm) {
    loading.value = true
    try {
      const updatedAsset = await assetApi.updateAsset(id, data)
      const index = assets.value.findIndex(a => a.id === id)
      if (index !== -1) {
        assets.value[index] = updatedAsset
      }
      if (currentAsset.value?.id === id) {
        currentAsset.value = updatedAsset
      }
      return updatedAsset
    } finally {
      loading.value = false
    }
  }

  /**
   * 删除资产
   */
  async function remove(id: string) {
    loading.value = true
    try {
      await assetApi.deleteAsset(id)
      assets.value = assets.value.filter(a => a.id !== id)
      if (currentAsset.value?.id === id) {
        currentAsset.value = null
      }
    } finally {
      loading.value = false
    }
  }

  /**
   * 借出资产
   */
  async function borrow(id: string, data: BorrowForm) {
    loading.value = true
    try {
      const result = await assetApi.borrowAsset(id, data)
      const index = assets.value.findIndex(a => a.id === id)
      if (index !== -1) {
        assets.value[index] = result
      }
      return result
    } finally {
      loading.value = false
    }
  }

  /**
   * 归还资产
   */
  async function returnAsset(id: string, data: ReturnForm) {
    loading.value = true
    try {
      const result = await assetApi.returnAsset(id, data)
      const index = assets.value.findIndex(a => a.id === id)
      if (index !== -1) {
        assets.value[index] = result
      }
      return result
    } finally {
      loading.value = false
    }
  }

  /**
   * 加载借还历史
   */
  async function loadBorrowHistory(assetId: string) {
    loading.value = true
    try {
      const result = await assetApi.getBorrowHistory(assetId)
      borrowRecords.value = result.list
      return result
    } finally {
      loading.value = false
    }
  }

  /**
   * 加载统计数据
   */
  async function loadStatistics() {
    loading.value = true
    try {
      statistics.value = await assetApi.getStatistics()
      return statistics.value
    } finally {
      loading.value = false
    }
  }

  /**
   * 加载折旧趋势
   */
  async function loadDepreciationTrend(months: number = 6) {
    loading.value = true
    try {
      return await assetApi.getDepreciationTrend(months)
    } finally {
      loading.value = false
    }
  }

  /**
   * 加载借出趋势
   */
  async function loadBorrowTrend(months: number = 6) {
    loading.value = true
    try {
      return await assetApi.getBorrowTrend(months)
    } finally {
      loading.value = false
    }
  }

  /**
   * 设置筛选条件
   */
  function setFilter(newFilter: AssetFilter) {
    filter.value = { ...newFilter }
    pagination.value.page = 1
  }

  /**
   * 重置筛选条件
   */
  function resetFilter() {
    filter.value = {}
    pagination.value.page = 1
  }

  /**
   * 切换视图
   */
  function setView(view: 'table' | 'kanban' | 'gallery') {
    currentView.value = view
  }

  return {
    // 状态
    assets,
    currentAsset,
    borrowRecords,
    statistics,
    loading,
    filter,
    pagination,
    currentView,

    // 计算属性
    assetsByStatus,

    // Actions
    loadList,
    loadDetail,
    create,
    update,
    remove,
    borrow,
    returnAsset,
    loadBorrowHistory,
    loadStatistics,
    loadDepreciationTrend,
    loadBorrowTrend,
    setFilter,
    resetFilter,
    setView
  }
})
