/**
 * 员工管理模块 Pinia Store
 * 基于 employee_Technical.md 规范实现
 */

import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { Employee, EmployeeFilter, EmployeeForm, EmployeeStatistics } from '../types'
import * as employeeApi from '../api'

export const useEmployeeStore = defineStore('employee', () => {
  // ==================== 状态 ====================

  // 员工列表数据
  const employeeList = ref<Employee[]>([])
  const currentEmployee = ref<Employee | null>(null)
  const loading = ref(false)
  const total = ref(0)

  // 统计数据
  const statistics = ref<EmployeeStatistics | null>(null)
  const statisticsLoading = ref(false)

  // 当前筛选条件
  const filter = ref<EmployeeFilter>({})

  // 分页
  const pagination = ref({
    page: 1,
    pageSize: 20,
  })

  // 部门列表 (用于筛选)
  const departmentList = ref<Array<{ id: string; name: string }>>([])
  const positionList = ref<string[]>([])

  // ==================== 计算属性 ====================

  // 在职员工数
  const activeCount = computed(() => employeeList.value.filter(e => e.status === 'active').length)

  // 试用期员工数
  const probationCount = computed(() =>
    employeeList.value.filter(e => e.probationStatus === 'probation').length
  )

  // 离职员工数
  const resignedCount = computed(() => employeeList.value.filter(e => e.status === 'resigned').length)

  // ==================== 员工 CRUD 方法 ====================

  /**
   * 获取员工列表
   */
  async function fetchEmployeeList() {
    loading.value = true
    try {
      const result = await employeeApi.getEmployeeList({
        ...filter.value,
        ...pagination.value,
      })

      console.log('Store接收到的数据:', result)
      console.log('列表长度:', result.list.length)
      console.log('总数:', result.total)

      const { list, total: totalCount } = result

      // 后端已经返回了 positionLabel，直接使用
      // 如果需要前端补充职位字典，可以在这里添加
      employeeList.value = list

      console.log('设置后的total值:', totalCount)
      total.value = totalCount
    } catch (error) {
      console.error('获取员工列表失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 获取员工详情
   */
  async function fetchEmployeeDetail(id: string) {
    loading.value = true
    try {
      const employee = await employeeApi.getEmployeeDetail(id)
      currentEmployee.value = employee
      return employee
    } catch (error) {
      console.error('获取员工详情失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 创建员工
   */
  async function createEmployee(data: EmployeeForm) {
    loading.value = true
    try {
      const newEmployee = await employeeApi.createEmployee(data)
      // 刷新列表
      await fetchEmployeeList()
      return newEmployee
    } catch (error) {
      console.error('创建员工失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 更新员工
   */
  async function updateEmployee(id: string, data: Partial<EmployeeForm>) {
    loading.value = true
    try {
      const updatedEmployee = await employeeApi.updateEmployee(id, data)
      if (updatedEmployee) {
        // 重新获取列表以确保数据完整
        await fetchEmployeeList()
      }
      return updatedEmployee
    } catch (error) {
      console.error('更新员工失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 更新员工状态 (办理离职)
   */
  async function updateEmployeeStatus(id: string, status: Employee['status'], reason?: string) {
    loading.value = true
    try {
      const updatedEmployee = await employeeApi.updateEmployeeStatus(id, status, reason)
      if (updatedEmployee) {
        // 更新列表中的数据
        const index = employeeList.value.findIndex(emp => emp.id === id)
        if (index !== -1) {
          employeeList.value[index] = updatedEmployee
        }
        // 更新当前员工
        if (currentEmployee.value?.id === id) {
          currentEmployee.value = updatedEmployee
        }
      }
      return updatedEmployee
    } catch (error) {
      console.error('更新员工状态失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 删除员工
   */
  async function deleteEmployee(id: string) {
    loading.value = true
    try {
      const success = await employeeApi.deleteEmployee(id)
      if (success) {
        employeeList.value = employeeList.value.filter(emp => emp.id !== id)
        total.value--
      }
      return success
    } catch (error) {
      console.error('删除员工失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  // ==================== 统计方法 ====================

  /**
   * 获取统计数据
   */
  async function fetchStatistics() {
    statisticsLoading.value = true
    try {
      const stats = await employeeApi.getStatistics()
      statistics.value = stats
      return stats
    } catch (error) {
      console.error('获取统计数据失败:', error)
      throw error
    } finally {
      statisticsLoading.value = false
    }
  }

  // ==================== 筛选和分页方法 ====================

  /**
   * 更新筛选条件
   */
  function updateFilter(newFilter: Partial<EmployeeFilter>) {
    filter.value = { ...filter.value, ...newFilter }
    pagination.value.page = 1 // 重置到第一页
  }

  /**
   * 重置筛选条件
   */
  function resetFilter() {
    filter.value = {}
    pagination.value.page = 1
  }

  /**
   * 更改分页
   */
  function changePage(page: number, pageSize?: number) {
    pagination.value.page = page
    if (pageSize) {
      pagination.value.pageSize = pageSize
    }
  }

  // ==================== 辅助数据方法 ====================

  /**
   * 获取部门列表
   */
  async function fetchDepartmentList() {
    try {
      departmentList.value = await employeeApi.getDepartmentList()
    } catch (error) {
      console.error('获取部门列表失败:', error)
    }
  }

  /**
   * 获取职位列表
   */
  async function fetchPositionList() {
    try {
      positionList.value = await employeeApi.getPositionList()
    } catch (error) {
      console.error('获取职位列表失败:', error)
    }
  }

  /**
   * 初始化辅助数据
   */
  async function initHelperData() {
    await Promise.all([
      fetchDepartmentList(),
      fetchPositionList(),
    ])
  }

  // ==================== 导入导出方法 ====================

  /**
   * 导入员工
   */
  async function importEmployees(file: File) {
    loading.value = true
    try {
      const result = await employeeApi.importEmployees(file)
      if (result.success > 0) {
        // 刷新列表
        await fetchEmployeeList()
      }
      return result
    } catch (error) {
      console.error('导入员工失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 导出员工
   */
  async function exportEmployees(filter?: EmployeeFilter) {
    try {
      const blob = await employeeApi.exportEmployees({ filter })
      // 创建下载链接
      const url = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = `员工列表_${new Date().toISOString().split('T')[0]}.xlsx`
      link.click()
      window.URL.revokeObjectURL(url)
    } catch (error) {
      console.error('导出员工失败:', error)
      throw error
    }
  }

  // ==================== 返回 ====================

  return {
    // 状态
    employeeList,
    currentEmployee,
    loading,
    total,
    statistics,
    statisticsLoading,
    filter,
    pagination,
    departmentList,
    positionList,

    // 计算属性
    activeCount,
    probationCount,
    resignedCount,

    // 员工 CRUD 方法
    fetchEmployeeList,
    fetchEmployeeDetail,
    createEmployee,
    updateEmployee,
    updateEmployeeStatus,
    deleteEmployee,

    // 统计方法
    fetchStatistics,

    // 筛选和分页方法
    updateFilter,
    resetFilter,
    changePage,

    // 辅助数据方法
    fetchDepartmentList,
    fetchPositionList,
    initHelperData,

    // 导入导出方法
    importEmployees,
    exportEmployees,
  }
})
