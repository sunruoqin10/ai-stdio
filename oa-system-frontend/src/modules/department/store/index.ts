/**
 * 部门管理模块 Store
 * 基于 department_Technical.md 规范实现
 */

import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { Department, DepartmentFilter, DepartmentForm, MoveDepartmentRequest } from '../types'
import * as api from '../api'
import { buildTree, flattenTree } from '../utils'

export const useDepartmentStore = defineStore('department', () => {
  // ==================== 状态 ====================

  /** 部门列表(扁平) */
  const list = ref<Department[]>([])

  /** 部门树 */
  const tree = ref<Department[]>([])

  /** 当前选中的部门 */
  const currentDepartment = ref<Department | null>(null)

  /** 加载状态 */
  const loading = ref(false)

  /** 筛选条件 */
  const filter = ref<DepartmentFilter>({})

  // ==================== 计算属性 ====================

  /** 总部门数 */
  const totalCount = computed(() => list.value.length)

  /** 一级部门数 */
  const level1Count = computed(() => list.value.filter(d => d.level === 1).length)

  /** 最大层级深度 */
  const maxLevel = computed(() => {
    if (list.value.length === 0) return 0
    return Math.max(...list.value.map(d => d.level))
  })

  /** 有负责人的部门数 */
  const withLeaderCount = computed(() => list.value.filter(d => d.leaderId).length)

  // ==================== Actions ====================

  /**
   * 加载部门列表
   */
  async function loadList(type: 'tree' | 'flat' = 'flat', page: number = 1, pageSize: number = 1000) {
    loading.value = true
    try {
      if (type === 'tree') {
        // 调用树形API
        const data = await api.getDepartmentTree()
        tree.value = data
        list.value = flattenTree(data)
        return data
      } else {
        // 调用分页API，获取所有数据
        const data = await api.getList({
          ...filter.value,
          page,
          pageSize
        })

        list.value = data.list
        tree.value = buildTree(data.list) as Department[]
        return data
      }
    } finally {
      loading.value = false
    }
  }

  /**
   * 加载部门详情
   */
  async function loadDetail(id: string) {
    loading.value = true
    try {
      const data = await api.getDetail(id)
      if (data) {
        currentDepartment.value = data
      }
      return data
    } finally {
      loading.value = false
    }
  }

  /**
   * 创建部门
   */
  async function create(form: DepartmentForm) {
    loading.value = true
    try {
      const result = await api.create(form)
      await loadList() // 重新加载列表
      return result
    } finally {
      loading.value = false
    }
  }

  /**
   * 更新部门
   */
  async function update(id: string, form: Partial<DepartmentForm>) {
    loading.value = true
    try {
      const result = await api.update(id, form)
      await loadList() // 重新加载列表
      return result
    } finally {
      loading.value = false
    }
  }

  /**
   * 移动部门
   */
  async function move(request: MoveDepartmentRequest) {
    loading.value = true
    try {
      const result = await api.move(request)
      await loadList() // 重新加载列表
      return result
    } finally {
      loading.value = false
    }
  }

  /**
   * 删除部门
   */
  async function remove(id: string) {
    loading.value = true
    try {
      const result = await api.remove(id)
      await loadList() // 重新加载列表
      return result
    } finally {
      loading.value = false
    }
  }

  /**
   * 批量删除部门
   */
  async function batchRemove(ids: string[]) {
    loading.value = true
    try {
      const result = await api.batchRemove(ids)
      await loadList() // 重新加载列表
      return result
    } finally {
      loading.value = false
    }
  }

  /**
   * 导出部门列表
   */
  async function exportList() {
    loading.value = true
    try {
      const blob = await api.exportDepartments(filter.value)

      // 创建下载链接
      const url = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = `部门列表_${new Date().toLocaleDateString()}.xlsx`
      link.click()
      window.URL.revokeObjectURL(url)

      return true
    } finally {
      loading.value = false
    }
  }

  /**
   * 设置筛选条件
   */
  function setFilter(newFilter: DepartmentFilter) {
    filter.value = { ...newFilter }
  }

  /**
   * 重置筛选条件
   */
  function resetFilter() {
    filter.value = {}
  }

  /**
   * 获取部门路径(面包屑)
   */
  function getDepartmentPath(departmentId: string): Department[] {
    const path: Department[] = []
    let current = list.value.find(d => d.id === departmentId)

    while (current) {
      path.unshift(current)
      if (!current.parentId) break
      current = list.value.find(d => d.id === current.parentId)
    }

    return path
  }

  /**
   * 获取子部门列表
   */
  function getChildren(parentId: string): Department[] {
    return list.value.filter(d => d.parentId === parentId)
  }

  /**
   * 搜索部门
   */
  async function search(keyword: string) {
    loading.value = true
    try {
      const results = await api.searchDepartments(keyword)
      return results
    } finally {
      loading.value = false
    }
  }

  /**
   * 获取部门统计
   */
  async function getStatistics() {
    return await api.getStatistics()
  }

  /**
   * 获取部门树(用于选择器)
   */
  async function getDepartmentTree() {
    return await api.getDepartmentTree()
  }

  return {
    // 状态
    list,
    tree,
    currentDepartment,
    loading,
    filter,

    // 计算属性
    totalCount,
    level1Count,
    maxLevel,
    withLeaderCount,

    // Actions
    loadList,
    loadDetail,
    create,
    update,
    move,
    remove,
    batchRemove,
    exportList,
    setFilter,
    resetFilter,
    getDepartmentPath,
    getChildren,
    search,
    getStatistics,
    getDepartmentTree
  }
})
