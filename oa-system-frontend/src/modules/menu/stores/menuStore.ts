/**
 * 菜单管理模块 - Pinia Store
 */

import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { MenuItem, MenuForm, MenuQuery } from '../types'
import * as menuApi from '../api'

export const useMenuStore = defineStore('menu', () => {
  // 状态
  const menuList = ref<MenuItem[]>([])
  const currentMenu = ref<MenuItem | null>(null)
  const loading = ref(false)
  const queryParams = ref<MenuQuery>({})

  // 计算属性
  const treeMenuList = computed(() => {
    return buildTree(menuList.value || [])
  })

  const menuMap = computed(() => {
    const map = new Map<number, MenuItem>()
    const buildMap = (items: MenuItem[]) => {
      items.forEach(item => {
        map.set(item.id, item)
        if (item.children?.length) {
          buildMap(item.children)
        }
      })
    }
    buildMap(menuList.value || [])
    return map
  })

  // 操作
  async function fetchMenuList(params?: MenuQuery) {
    loading.value = true
    try {
      const response = await menuApi.getMenuList(params)
      // Handle different response structures
      let data: MenuItem[] = []
      if (Array.isArray(response)) {
        data = response
      } else if (response && typeof response === 'object') {
        // Response might be { code, data, message } or { data }
        data = (response as any).data || []
      }
      menuList.value = data
      queryParams.value = params || {}
      return data
    } finally {
      loading.value = false
    }
  }

  async function fetchMenuDetail(id: number) {
    loading.value = true
    try {
      const response = await menuApi.getMenuDetail(id)
      const data = (response as any)?.data || response
      currentMenu.value = data
      return data
    } finally {
      loading.value = false
    }
  }

  async function createMenu(form: MenuForm) {
    return await menuApi.createMenu(form)
  }

  async function updateMenu(id: number, form: MenuForm) {
    return await menuApi.updateMenu(id, form)
  }

  async function deleteMenu(id: number) {
    return await menuApi.deleteMenu(id)
  }

  async function toggleStatus(id: number, status: boolean) {
    return await menuApi.toggleMenuStatus(id, status)
  }

  function clearCurrentMenu() {
    currentMenu.value = null
  }

  // 工具函数
  function buildTree(items: MenuItem[] | undefined | null, parentId = 0): MenuItem[] {
    if (!Array.isArray(items)) {
      return []
    }
    return items
      .filter(item => item.parentId === parentId)
      .map(item => ({
        ...item,
        children: buildTree(items, item.id)
      }))
      .sort((a, b) => a.sortOrder - b.sortOrder)
  }

  function getMenuPath(menuId: number): MenuItem[] {
    const path: MenuItem[] = []
    let menu = menuMap.value.get(menuId)

    while (menu) {
      path.unshift(menu)
      menu = menu.parentId ? menuMap.value.get(menu.parentId) : undefined
    }

    return path
  }

  return {
    // 状态
    menuList,
    currentMenu,
    loading,
    queryParams,

    // 计算属性
    treeMenuList,
    menuMap,

    // 操作
    fetchMenuList,
    fetchMenuDetail,
    createMenu,
    updateMenu,
    deleteMenu,
    toggleStatus,
    clearCurrentMenu,
    getMenuPath
  }
})
