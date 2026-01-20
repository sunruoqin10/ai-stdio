/**
 * 请假管理模块 Store
 * @module leave/store
 */

import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type {
  LeaveRequest,
  LeaveForm,
  LeaveBalance,
  LeaveStatistics,
  LeaveFilter
} from '../types'
import * as leaveApi from '../api'

export const useLeaveStore = defineStore('leave', () => {
  // ==================== 状态 ====================

  const myRequests = ref<LeaveRequest[]>([])
  const currentRequest = ref<LeaveRequest | null>(null)
  const pendingApprovals = ref<LeaveRequest[]>([])
  const approvedRequests = ref<LeaveRequest[]>([])
  const leaveBalance = ref<LeaveBalance | null>(null)
  const statistics = ref<LeaveStatistics | null>(null)
  const loading = ref(false)

  // 筛选条件
  const filter = ref<LeaveFilter>({})

  // 分页
  const pagination = ref({
    page: 1,
    pageSize: 10,
    total: 0
  })

  // 当前标签页 (my-requests | approvals | statistics)
  const currentTab = ref<'my-requests' | 'approvals' | 'statistics'>('my-requests')

  // ==================== 计算属性 ====================

  // 草稿数量
  const draftCount = computed(() => {
    return myRequests.value.filter(req => req.status === 'draft').length
  })

  // 待审批数量
  const pendingCount = computed(() => {
    return pendingApprovals.value.filter(req => req.status === 'pending').length
  })

  // 已通过数量
  const approvedCount = computed(() => {
    return myRequests.value.filter(req => req.status === 'approved').length
  })

  // 已驳回数量
  const rejectedCount = computed(() => {
    return myRequests.value.filter(req => req.status === 'rejected').length
  })

  // ==================== Actions ====================

  /**
   * 加载我的请假列表
   */
  async function loadMyRequests(params?: { status?: string; page?: number; pageSize?: number }) {
    loading.value = true
    try {
      const result = await leaveApi.getMyLeaveRequests({
        ...filter.value,
        ...params,
        page: params?.page || pagination.value.page,
        pageSize: params?.pageSize || pagination.value.pageSize
      })

      myRequests.value = result.list
      pagination.value.total = result.total
      pagination.value.page = result.page
      pagination.value.pageSize = result.pageSize
    } finally {
      loading.value = false
    }
  }

  /**
   * 加载请假申请详情
   */
  async function loadRequest(id: string) {
    loading.value = true
    try {
      currentRequest.value = await leaveApi.getLeaveRequest(id)
      return currentRequest.value
    } finally {
      loading.value = false
    }
  }

  /**
   * 创建请假申请
   */
  async function createRequest(data: LeaveForm) {
    loading.value = true
    try {
      const newRequest = await leaveApi.createLeaveRequest(data)
      myRequests.value.unshift(newRequest)
      return newRequest
    } finally {
      loading.value = false
    }
  }

  /**
   * 更新请假申请
   */
  async function updateRequest(id: string, data: Partial<LeaveForm>) {
    loading.value = true
    try {
      const updatedRequest = await leaveApi.updateLeaveRequest(id, data)
      const index = myRequests.value.findIndex(req => req.id === id)
      if (index !== -1) {
        myRequests.value[index] = updatedRequest
      }
      if (currentRequest.value?.id === id) {
        currentRequest.value = updatedRequest
      }
      return updatedRequest
    } finally {
      loading.value = false
    }
  }

  /**
   * 删除请假申请
   */
  async function deleteRequest(id: string) {
    loading.value = true
    try {
      await leaveApi.deleteLeaveRequest(id)
      myRequests.value = myRequests.value.filter(req => req.id !== id)
      if (currentRequest.value?.id === id) {
        currentRequest.value = null
      }
    } finally {
      loading.value = false
    }
  }

  /**
   * 提交请假申请
   */
  async function submitRequest(id: string) {
    loading.value = true
    try {
      await leaveApi.submitLeaveRequest(id)
      // 如果需要更新本地状态，可以重新获取数据
      // 这里简单处理，不直接更新，让用户手动刷新或通过其他方式更新
      return null
    } finally {
      loading.value = false
    }
  }

  /**
   * 撤销请假申请
   */
  async function cancelRequest(id: string) {
    loading.value = true
    try {
      await leaveApi.cancelLeaveRequest(id)
      // 如果需要更新本地状态，可以重新获取数据
      // 这里简单处理，不直接更新，让用户手动刷新或通过其他方式更新
      return null
    } finally {
      loading.value = false
    }
  }

  /**
   * 加载待审批列表
   */
  async function loadPendingApprovals(params?: { departmentId?: string; page?: number; pageSize?: number }) {
    loading.value = true
    try {
      const result = await leaveApi.getPendingApprovals(params)
      pendingApprovals.value = result.list
      return result
    } finally {
      loading.value = false
    }
  }

  /**
   * 加载已审批列表
   */
  async function loadApprovedRequests(params?: { approverId?: string; page?: number; pageSize?: number }) {
    loading.value = true
    try {
      const result = await leaveApi.getApprovedRequests(params)
      approvedRequests.value = result.list
      return result
    } finally {
      loading.value = false
    }
  }

  /**
   * 审批请假申请
   */
  async function approveRequest(id: string, approval: { status: 'approved' | 'rejected'; opinion?: string }) {
    loading.value = true
    try {
      const updatedRequest = await leaveApi.approveLeaveRequest(id, approval)

      // 更新待审批列表
      const pendingIndex = pendingApprovals.value.findIndex(req => req.id === id)
      if (pendingIndex !== -1) {
        if (approval.status === 'approved' && updatedRequest.status === 'approving') {
          // 继续审批流程,保留在待审批列表
          pendingApprovals.value[pendingIndex] = updatedRequest
        } else {
          // 审批完成或驳回,从待审批列表移除
          pendingApprovals.value.splice(pendingIndex, 1)
        }
      }

      return updatedRequest
    } finally {
      loading.value = false
    }
  }

  /**
   * 加载年假余额
   */
  async function loadLeaveBalance(employeeId: string, year?: number) {
    loading.value = true
    try {
      leaveBalance.value = await leaveApi.getLeaveBalance(employeeId, year)
      return leaveBalance.value
    } finally {
      loading.value = false
    }
  }

  /**
   * 更新年假额度
   */
  async function updateLeaveQuota(employeeId: string, year: number, quota: number) {
    loading.value = true
    try {
      const updatedBalance = await leaveApi.updateLeaveQuota(employeeId, year, quota)
      if (leaveBalance.value?.employeeId === employeeId && leaveBalance.value?.year === year) {
        leaveBalance.value = updatedBalance
      }
      return updatedBalance
    } finally {
      loading.value = false
    }
  }

  /**
   * 加载统计数据
   */
  async function loadStatistics(params?: { departmentId?: string; year?: number; applicantId?: string }) {
    loading.value = true
    try {
      statistics.value = await leaveApi.getLeaveStatistics(params)
      return statistics.value
    } finally {
      loading.value = false
    }
  }

  /**
   * 设置筛选条件
   */
  function setFilter(newFilter: LeaveFilter) {
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
   * 切换标签页
   */
  function setTab(tab: 'my-requests' | 'approvals' | 'statistics') {
    currentTab.value = tab
  }

  return {
    // 状态
    myRequests,
    currentRequest,
    pendingApprovals,
    approvedRequests,
    leaveBalance,
    statistics,
    loading,
    filter,
    pagination,
    currentTab,

    // 计算属性
    draftCount,
    pendingCount,
    approvedCount,
    rejectedCount,

    // Actions
    loadMyRequests,
    loadRequest,
    createRequest,
    updateRequest,
    deleteRequest,
    submitRequest,
    cancelRequest,
    loadPendingApprovals,
    loadApprovedRequests,
    approveRequest,
    loadLeaveBalance,
    updateLeaveQuota,
    loadStatistics,
    setFilter,
    resetFilter,
    setTab
  }
})
