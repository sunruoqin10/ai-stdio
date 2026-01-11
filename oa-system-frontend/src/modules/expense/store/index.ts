/**
 * 费用报销模块 Store
 * @module expense/store
 */

import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type {
  Expense,
  ExpenseForm,
  PaymentRecord,
  PaymentForm,
  ApprovalForm,
  ExpenseQueryParams
} from '../types'
import * as expenseApi from '../api'

export const useExpenseStore = defineStore('expense', () => {
  // ==================== 状态 ====================

  const myExpenses = ref<Expense[]>([])
  const currentExpense = ref<Expense | null>(null)
  const pendingApprovals = ref<Expense[]>([])
  const paymentList = ref<PaymentRecord[]>([])
  const loading = ref(false)

  // 筛选条件
  const filter = ref<ExpenseQueryParams>({})

  // 分页
  const pagination = ref({
    page: 1,
    pageSize: 10,
    total: 0
  })

  // 当前标签页 (my-expenses | approvals | payments)
  const currentTab = ref<'my-expenses' | 'approvals' | 'payments'>('my-expenses')

  // ==================== 计算属性 ====================

  // 草稿数量
  const draftCount = computed(() => {
    return myExpenses.value.filter(exp => exp.status === 'draft').length
  })

  // 待审批数量
  const pendingCount = computed(() => {
    return pendingApprovals.value.filter(
      exp => exp.status === 'dept_pending' || exp.status === 'finance_pending'
    ).length
  })

  // 已通过数量
  const approvedCount = computed(() => {
    return myExpenses.value.filter(exp => exp.status === 'paid').length
  })

  // 总金额
  const totalAmount = computed(() => {
    return myExpenses.value.reduce((sum, exp) => sum + exp.amount, 0)
  })

  // ==================== Actions ====================

  /**
   * 加载我的报销列表
   */
  async function loadMyExpenses(params?: ExpenseQueryParams) {
    loading.value = true
    try {
      const result = await expenseApi.getMyExpenses({
        ...filter.value,
        ...params,
        page: params?.page || pagination.value.page,
        size: params?.size || pagination.value.pageSize
      })

      myExpenses.value = result.list
      pagination.value.total = result.total
      pagination.value.page = params?.page || pagination.value.page
      pagination.value.pageSize = params?.size || pagination.value.pageSize
    } finally {
      loading.value = false
    }
  }

  /**
   * 加载报销单详情
   */
  async function loadExpense(id: string) {
    loading.value = true
    try {
      currentExpense.value = await expenseApi.getExpense(id)
      return currentExpense.value
    } finally {
      loading.value = false
    }
  }

  /**
   * 创建报销单
   */
  async function createExpense(data: ExpenseForm) {
    loading.value = true
    try {
      const newExpense = await expenseApi.createExpense(data)
      myExpenses.value.unshift(newExpense)
      return newExpense
    } finally {
      loading.value = false
    }
  }

  /**
   * 更新报销单
   */
  async function updateExpense(id: string, data: Partial<ExpenseForm>) {
    loading.value = true
    try {
      const updatedExpense = await expenseApi.updateExpense(id, data)
      const index = myExpenses.value.findIndex(exp => exp.id === id)
      if (index !== -1) {
        myExpenses.value[index] = updatedExpense
      }
      if (currentExpense.value?.id === id) {
        currentExpense.value = updatedExpense
      }
      return updatedExpense
    } finally {
      loading.value = false
    }
  }

  /**
   * 删除报销单
   */
  async function deleteExpense(id: string) {
    loading.value = true
    try {
      await expenseApi.deleteExpense(id)
      myExpenses.value = myExpenses.value.filter(exp => exp.id !== id)
      if (currentExpense.value?.id === id) {
        currentExpense.value = null
      }
    } finally {
      loading.value = false
    }
  }

  /**
   * 提交报销单
   */
  async function submitExpense(id: string) {
    loading.value = true
    try {
      const updatedExpense = await expenseApi.submitExpense(id)
      const index = myExpenses.value.findIndex(exp => exp.id === id)
      if (index !== -1) {
        myExpenses.value[index] = updatedExpense
      }
      if (currentExpense.value?.id === id) {
        currentExpense.value = updatedExpense
      }
      return updatedExpense
    } finally {
      loading.value = false
    }
  }

  /**
   * 撤销报销单
   */
  async function cancelExpense(id: string) {
    loading.value = true
    try {
      const updatedExpense = await expenseApi.cancelExpense(id)
      const index = myExpenses.value.findIndex(exp => exp.id === id)
      if (index !== -1) {
        myExpenses.value[index] = updatedExpense
      }
      if (currentExpense.value?.id === id) {
        currentExpense.value = updatedExpense
      }
      return updatedExpense
    } finally {
      loading.value = false
    }
  }

  /**
   * 加载待审批列表
   */
  async function loadPendingApprovals(params?: {
    departmentId?: string
    approvalType?: 'department' | 'finance'
    page?: number
    size?: number
  }) {
    loading.value = true
    try {
      const result = await expenseApi.getPendingApprovals(params)
      pendingApprovals.value = result.list
      return result
    } finally {
      loading.value = false
    }
  }

  /**
   * 部门审批
   */
  async function departmentApprove(id: string, approval: ApprovalForm) {
    loading.value = true
    try {
      const updatedExpense = await expenseApi.departmentApprove(id, approval)

      // 更新待审批列表
      const pendingIndex = pendingApprovals.value.findIndex(exp => exp.id === id)
      if (pendingIndex !== -1) {
        if (approval.status === 'approved' && updatedExpense.status === 'finance_pending') {
          // 继续审批流程,保留在待审批列表
          pendingApprovals.value[pendingIndex] = updatedExpense
        } else {
          // 审批完成或驳回,从待审批列表移除
          pendingApprovals.value.splice(pendingIndex, 1)
        }
      }

      // 更新我的报销列表
      const myIndex = myExpenses.value.findIndex(exp => exp.id === id)
      if (myIndex !== -1) {
        myExpenses.value[myIndex] = updatedExpense
      }

      if (currentExpense.value?.id === id) {
        currentExpense.value = updatedExpense
      }

      return updatedExpense
    } finally {
      loading.value = false
    }
  }

  /**
   * 财务审批
   */
  async function financeApprove(id: string, approval: ApprovalForm) {
    loading.value = true
    try {
      const updatedExpense = await expenseApi.financeApprove(id, approval)

      // 更新待审批列表
      const pendingIndex = pendingApprovals.value.findIndex(exp => exp.id === id)
      if (pendingIndex !== -1) {
        // 财务审批完成,从待审批列表移除
        pendingApprovals.value.splice(pendingIndex, 1)
      }

      // 更新我的报销列表
      const myIndex = myExpenses.value.findIndex(exp => exp.id === id)
      if (myIndex !== -1) {
        myExpenses.value[myIndex] = updatedExpense
      }

      if (currentExpense.value?.id === id) {
        currentExpense.value = updatedExpense
      }

      return updatedExpense
    } finally {
      loading.value = false
    }
  }

  /**
   * 加载打款列表
   */
  async function loadPayments(params?: {
    status?: 'pending' | 'completed' | 'failed'
    page?: number
    size?: number
  }) {
    loading.value = true
    try {
      const result = await expenseApi.getPayments(params)
      paymentList.value = result.list
      return result
    } finally {
      loading.value = false
    }
  }

  /**
   * 上传打款凭证
   */
  async function uploadPaymentProof(paymentId: number, proofUrl: string) {
    loading.value = true
    try {
      const updatedPayment = await expenseApi.uploadPaymentProof(paymentId, proofUrl)

      // 更新打款列表
      const index = paymentList.value.findIndex(p => p.id === paymentId)
      if (index !== -1) {
        paymentList.value[index] = updatedPayment
      }

      // 更新报销单状态
      const expenseIndex = myExpenses.value.findIndex(exp => exp.id === updatedPayment.expenseId)
      if (expenseIndex !== -1) {
        const expense = myExpenses.value[expenseIndex]
        expense.status = 'paid'
        expense.paymentDate = updatedPayment.paymentDate
        expense.paymentProof = proofUrl
      }

      if (currentExpense.value?.id === updatedPayment.expenseId) {
        currentExpense.value.status = 'paid'
        currentExpense.value.paymentDate = updatedPayment.paymentDate
        currentExpense.value.paymentProof = proofUrl
      }

      return updatedPayment
    } finally {
      loading.value = false
    }
  }

  /**
   * 验证发票
   */
  async function validateInvoice(invoiceNumber: string) {
    return await expenseApi.validateInvoice(invoiceNumber)
  }

  /**
   * OCR识别发票
   */
  async function ocrInvoice(imageUrl: string) {
    return await expenseApi.ocrInvoice(imageUrl)
  }

  /**
   * 设置筛选条件
   */
  function setFilter(newFilter: ExpenseQueryParams) {
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
  function setTab(tab: 'my-expenses' | 'approvals' | 'payments') {
    currentTab.value = tab
  }

  return {
    // 状态
    myExpenses,
    currentExpense,
    pendingApprovals,
    paymentList,
    loading,
    filter,
    pagination,
    currentTab,

    // 计算属性
    draftCount,
    pendingCount,
    approvedCount,
    totalAmount,

    // Actions
    loadMyExpenses,
    loadExpense,
    createExpense,
    updateExpense,
    deleteExpense,
    submitExpense,
    cancelExpense,
    loadPendingApprovals,
    departmentApprove,
    financeApprove,
    loadPayments,
    uploadPaymentProof,
    validateInvoice,
    ocrInvoice,
    setFilter,
    resetFilter,
    setTab
  }
})
