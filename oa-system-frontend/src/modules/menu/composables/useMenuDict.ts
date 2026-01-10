/**
 * 菜单管理模块 - 数据字典组合函数
 */

import { computed, ref, onMounted } from 'vue'
import { useDictStore } from '@/modules/dict/store'
import { MENU_DICT_TYPES } from '../config/dict'

export function useMenuDict() {
  const dictStore = useDictStore()

  // 字典数据缓存
  const dictDataCache = ref<Record<string, any[]>>({})

  // 加载字典数据
  async function loadDictData(dictCode: string) {
    if (dictDataCache.value[dictCode]) {
      return dictDataCache.value[dictCode]
    }

    try {
      const data = await dictStore.fetchDictData(dictCode)
      const items = data.items || []
      dictDataCache.value[dictCode] = items
      return items
    } catch (error) {
      console.error(`加载字典 ${dictCode} 失败:`, error)
      return []
    }
  }

  // 初始化:预加载常用字典
  onMounted(async () => {
    await loadDictData(MENU_DICT_TYPES.MENU_TYPE)
    await loadDictData(MENU_DICT_TYPES.MENU_STATUS)
  })

  // 菜单类型选项
  const menuTypeOptions = computed(() =>
    dictDataCache.value[MENU_DICT_TYPES.MENU_TYPE] || []
  )

  const menuTypeMap = computed(() => {
    const map = new Map<string, any>()
    menuTypeOptions.value.forEach((item: any) => {
      map.set(item.value, item)
    })
    return map
  })

  // 菜单状态选项
  const menuStatusOptions = computed(() =>
    dictDataCache.value[MENU_DICT_TYPES.MENU_STATUS] || []
  )

  const menuStatusMap = computed(() => {
    const map = new Map<string, any>()
    menuStatusOptions.value.forEach((item: any) => {
      map.set(item.value, item)
    })
    return map
  })

  // 链接目标选项(按需加载)
  async function loadMenuTargetOptions() {
    return await loadDictData(MENU_DICT_TYPES.MENU_TARGET)
  }

  const menuTargetOptions = computed(() =>
    dictDataCache.value[MENU_DICT_TYPES.MENU_TARGET] || []
  )

  // 获取显示文本
  function getMenuTypeLabel(value: string) {
    return menuTypeMap.value.get(value)?.label || value
  }

  function getMenuStatusLabel(value: boolean) {
    const strValue = value ? '1' : '0'
    return menuStatusMap.value.get(strValue)?.label || (value ? '启用' : '禁用')
  }

  function getMenuTargetLabel(value: string) {
    const target = menuTargetOptions.value.find((item: any) => item.value === value)
    return target?.label || value
  }

  return {
    menuTypeOptions,
    menuTypeMap,
    menuStatusOptions,
    menuStatusMap,
    menuTargetOptions,
    loadMenuTargetOptions,
    getMenuTypeLabel,
    getMenuStatusLabel,
    getMenuTargetLabel
  }
}
