/**
 * 数据字典 Pinia Store
 * @module dict/store
 */

import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type {
  DictType,
  DictItem,
  DictFilter,
  DictTreeNode,
  DictData
} from '../types'
import * as dictApi from '../api'
import { dictCacheManager } from '../utils/cache'

export const useDictStore = defineStore('dict', () => {
  // 状态
  const dictTypes = ref<DictType[]>([])
  const dictItems = ref<DictItem[]>([])
  const dictTree = ref<DictTreeNode[]>([])
  const currentDictType = ref<DictType | null>(null)
  const loading = ref(false)
  const total = ref(0)
  const currentPage = ref(1)
  const pageSize = ref(20)

  // 计算属性
  const enabledDictTypes = computed(() =>
    dictTypes.value.filter(dt => dt.status === 'enabled')
  )

  const systemDictTypes = computed(() =>
    dictTypes.value.filter(dt => dt.category === 'system')
  )

  const businessDictTypes = computed(() =>
    dictTypes.value.filter(dt => dt.category === 'business')
  )

  const currentDictItems = computed(() => {
    if (!currentDictType.value) return []
    return dictItems.value.filter(
      item => item.dictTypeCode === currentDictType.value!.code
    )
  })

  // 字典类型操作
  async function fetchDictTypes(params?: DictFilter & { page?: number; pageSize?: number }) {
    loading.value = true
    try {
      const { data } = await dictApi.getDictTypeList({
        page: params?.page || currentPage.value,
        pageSize: params?.pageSize || pageSize.value,
        keyword: params?.keyword,
        category: params?.category,
        status: params?.status
      })

      dictTypes.value = data.list
      total.value = data.total
      currentPage.value = data.page
      pageSize.value = data.pageSize

      return data
    } finally {
      loading.value = false
    }
  }

  async function fetchDictTypeDetail(id: string) {
    loading.value = true
    try {
      const { data } = await dictApi.getDictTypeDetail(id)
      return data
    } finally {
      loading.value = false
    }
  }

  async function createDictType(form: DictTypeForm) {
    const { data } = await dictApi.createDictType(form)
    await fetchDictTypes()
    return data.id
  }

  async function updateDictType(id: string, form: Partial<DictTypeForm>) {
    await dictApi.updateDictType(id, form)
    await fetchDictTypes()
    // 清除相关缓存
    dictCacheManager.clear(form.code)
  }

  async function deleteDictType(id: string) {
    await dictApi.deleteDictType(id)
    await fetchDictTypes()
  }

  // 字典项操作
  async function fetchDictItems(params?: DictFilter & {
    dictTypeCode?: string
    page?: number
    pageSize?: number
  }) {
    loading.value = true
    try {
      const { data } = await dictApi.getDictItemList({
        dictTypeCode: params?.dictTypeCode,
        page: params?.page || currentPage.value,
        pageSize: params?.pageSize || pageSize.value,
        keyword: params?.keyword,
        status: params?.status
      })

      dictItems.value = data.list
      total.value = data.total
      currentPage.value = data.page
      pageSize.value = data.pageSize

      return data
    } finally {
      loading.value = false
    }
  }

  async function fetchDictItemDetail(id: string) {
    loading.value = true
    try {
      const { data } = await dictApi.getDictItemDetail(id)
      return data
    } finally {
      loading.value = false
    }
  }

  async function createDictItem(form: DictItemForm) {
    const { data } = await dictApi.createDictItem(form)
    await fetchDictItems({ dictTypeCode: currentDictType.value?.code })
    // 清除相关缓存
    dictCacheManager.clear(currentDictType.value?.code)
    return data.id
  }

  async function updateDictItem(id: string, form: Partial<DictItemForm>) {
    await dictApi.updateDictItem(id, form)
    await fetchDictItems({ dictTypeCode: currentDictType.value?.code })
    // 清除相关缓存
    dictCacheManager.clear(currentDictType.value?.code)
  }

  async function deleteDictItem(id: string) {
    await dictApi.deleteDictItem(id)
    await fetchDictItems({ dictTypeCode: currentDictType.value?.code })
    // 清除相关缓存
    dictCacheManager.clear(currentDictType.value?.code)
  }

  async function updateDictItemSort(items: Array<{ id: string; sortOrder: number }>) {
    const dictTypeId = currentDictType.value?.id
    if (!dictTypeId) {
      throw new Error('未选择字典类型')
    }
    await dictApi.updateDictItemSort(dictTypeId, items)
    await fetchDictItems({ dictTypeCode: currentDictType.value?.code })
  }

  async function batchDeleteDictItems(ids: string[]) {
    await dictApi.batchDeleteDictItems(ids)
    await fetchDictItems({ dictTypeCode: currentDictType.value?.code })
    // 清除相关缓存
    dictCacheManager.clear(currentDictType.value?.code)
  }

  async function batchUpdateDictItemStatus(
    ids: string[],
    status: 'enabled' | 'disabled'
  ) {
    await dictApi.batchUpdateDictItemStatus(ids, status)
    await fetchDictItems({ dictTypeCode: currentDictType.value?.code })
    // 清除相关缓存
    dictCacheManager.clear(currentDictType.value?.code)
  }

  // 字典树操作
  async function fetchDictTree() {
    loading.value = true
    try {
      const { data } = await dictApi.getDictTree()
      dictTree.value = data
      return data
    } finally {
      loading.value = false
    }
  }

  // 字典数据操作(带缓存)
  async function fetchDictData(dictTypeCode: string): Promise<DictData> {
    // 尝试从缓存获取
    const cached = dictCacheManager.get(dictTypeCode)
    if (cached) {
      return cached
    }

    // 缓存未命中,请求API
    const { data } = await dictApi.getDictData(dictTypeCode)

    // 设置缓存
    dictCacheManager.set(dictTypeCode, data)

    return data
  }

  // 导入导出
  async function importDict(file: File) {
    const { data } = await dictApi.importDict(file)
    await fetchDictTypes()
    return data
  }

  async function exportDict(dictTypeCodes?: string[]) {
    return await dictApi.exportDict(dictTypeCodes)
  }

  // 缓存管理
  async function clearDictCache(dictTypeCode?: string) {
    await dictApi.clearDictCache(dictTypeCode)
    dictCacheManager.clear(dictTypeCode)
  }

  // 唯一性检查
  async function checkDictCodeExists(code: string, excludeId?: string): Promise<boolean> {
    const { data } = await dictApi.checkDictCodeExists(code, excludeId)
    return data
  }

  async function checkDictValueExists(
    dictTypeId: string,
    value: string,
    excludeId?: string
  ): Promise<boolean> {
    const { data } = await dictApi.checkDictValueExists(dictTypeId, value, excludeId)
    return data
  }

  // 当前操作
  function setCurrentDictType(dictType: DictType | null) {
    currentDictType.value = dictType
  }

  // 分页操作
  function setPage(page: number) {
    currentPage.value = page
  }

  function setPageSize(size: number) {
    pageSize.value = size
    currentPage.value = 1
  }

  // 重置状态
  function reset() {
    dictTypes.value = []
    dictItems.value = []
    dictTree.value = []
    currentDictType.value = null
    loading.value = false
    total.value = 0
    currentPage.value = 1
    pageSize.value = 20
  }

  return {
    // 状态
    dictTypes,
    dictItems,
    dictTree,
    currentDictType,
    loading,
    total,
    currentPage,
    pageSize,

    // 计算属性
    enabledDictTypes,
    systemDictTypes,
    businessDictTypes,
    currentDictItems,

    // 字典类型操作
    fetchDictTypes,
    fetchDictTypeDetail,
    createDictType,
    updateDictType,
    deleteDictType,

    // 字典项操作
    fetchDictItems,
    fetchDictItemDetail,
    createDictItem,
    updateDictItem,
    deleteDictItem,
    updateDictItemSort,
    batchDeleteDictItems,
    batchUpdateDictItemStatus,

    // 字典树操作
    fetchDictTree,

    // 字典数据操作
    fetchDictData,

    // 导入导出
    importDict,
    exportDict,

    // 缓存管理
    clearDictCache,

    // 唯一性检查
    checkDictCodeExists,
    checkDictValueExists,

    // 当前操作
    setCurrentDictType,

    // 分页操作
    setPage,
    setPageSize,

    // 重置状态
    reset
  }
})
