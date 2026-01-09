import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { Employee, EmployeeFilter } from '../types'
import * as employeeApi from '../api'

export const useEmployeeStore = defineStore('employee', () => {
  // 状态
  const employeeList = ref<Employee[]>([])
  const currentEmployee = ref<Employee | null>(null)
  const loading = ref(false)
  const total = ref(0)

  // 当前筛选条件
  const filter = ref<EmployeeFilter>({
    keyword: '',
    status: '',
    departmentIds: [],
    probationStatus: '',
    position: '',
    gender: '',
    entryDateRange: undefined,
  })

  // 分页
  const pagination = ref({
    page: 1,
    pageSize: 20,
  })

  /**
   * 获取员工列表
   */
  async function fetchEmployeeList() {
    loading.value = true
    try {
      const { list, total: totalCount } = await employeeApi.getEmployeeList({
        ...filter.value,
        ...pagination.value,
      })
      employeeList.value = list
      total.value = totalCount
    } catch (error) {
      console.error('获取员工列表失败:', error)
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
      return null
    } finally {
      loading.value = false
    }
  }

  /**
   * 创建员工
   */
  async function createEmployee(data: Partial<Employee>) {
    loading.value = true
    try {
      const newEmployee = await employeeApi.createEmployee(data)
      await fetchEmployeeList()
      return newEmployee
    } catch (error) {
      console.error('创建员工失败:', error)
      return null
    } finally {
      loading.value = false
    }
  }

  /**
   * 更新员工
   */
  async function updateEmployee(id: string, data: Partial<Employee>) {
    loading.value = true
    try {
      const updatedEmployee = await employeeApi.updateEmployee(id, data)
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
      console.error('更新员工失败:', error)
      return null
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
      return false
    } finally {
      loading.value = false
    }
  }

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
    filter.value = {
      keyword: '',
      status: '',
      departmentIds: [],
      probationStatus: '',
      position: '',
      gender: '',
      entryDateRange: undefined,
    }
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

  return {
    // 状态
    employeeList,
    currentEmployee,
    loading,
    total,
    filter,
    pagination,

    // 方法
    fetchEmployeeList,
    fetchEmployeeDetail,
    createEmployee,
    updateEmployee,
    deleteEmployee,
    updateFilter,
    resetFilter,
    changePage,
  }
})
