/**
 * 费用报销模块 API 接口
 * @module expense/api
 *
 * 基于后端 API 实现，与后端服务进行通信
 * 所有接口使用真实的 HTTP 请求
 */

import type {
  Expense,
  ExpenseForm,
  PaymentRecord,
  PaymentForm,
  Invoice,
  DepartmentStats,
  TypeStats,
  MonthlyStats,
  ExpenseQueryParams,
  StatsQueryParams,
  PageResponse,
  ApprovalForm
} from '../types'
import { http } from '@/utils/request'

// ==================== 1. 报销单管理接口 ====================

/**
 * 获取报销单列表
 * @param params 查询参数
 * @returns 报销单分页列表
 */
export async function getMyExpenses(params?: ExpenseQueryParams): Promise<PageResponse<Expense>> {
  // 映射前端参数到后端参数
  const mappedParams = {
    ...params,
    applyDateStart: params?.startDate,
    applyDateEnd: params?.endDate,
    // 移除前端特有参数，避免后端无法识别
    startDate: undefined,
    endDate: undefined
  }
  
  const result = await http.get('/expense', { params: mappedParams })
  // 处理后端返回的数据格式
  const data = result.data || result
  return {
    total: data.total || 0,
    list: data.records || data.list || []
  }
}

/**
 * 获取报销详情
 * @param id 报销单号
 * @returns 报销单详情
 */
export async function getExpense(id: string): Promise<Expense> {
  const result = await http.get(`/expense/${id}`)
  // 处理后端返回的数据结构，提取 data 字段
  return result.data || result
}

/**
 * 创建报销单
 * @param data 报销单表单数据
 * @returns 创建的报销单
 */
export async function createExpense(data: ExpenseForm): Promise<Expense> {
  return await http.post('/expense', data)
}

/**
 * 更新报销单
 * @param id 报销单号
 * @param data 报销单表单数据
 * @returns 更新的报销单
 */
export async function updateExpense(id: string, data: Partial<ExpenseForm>): Promise<Expense> {
  return await http.put(`/expense/${id}`, data)
}

/**
 * 删除报销单
 * @param id 报销单号
 */
export async function deleteExpense(id: string): Promise<void> {
  await http.delete(`/expense/${id}`)
}

/**
 * 提交报销单
 * @param id 报销单号
 * @returns 更新后的报销单
 */
export async function submitExpense(id: string): Promise<Expense> {
  return await http.post(`/expense/${id}/submit`)
}

/**
 * 撤销报销单
 * @param id 报销单号
 * @returns 更新后的报销单
 */
export async function cancelExpense(id: string): Promise<Expense> {
  return await http.post(`/expense/${id}/cancel`)
}

// ==================== 2. 审批接口 ====================

/**
 * 获取待审批列表
 * @param params 查询参数
 * @returns 待审批报销单分页列表
 */
export async function getPendingApprovals(params?: {
  departmentId?: string
  approvalType?: 'department' | 'finance'
  page?: number
  size?: number
}): Promise<PageResponse<Expense>> {
  if (params?.approvalType === 'department') {
    const result = await http.get('/expense/pending/dept', { params })
    return {
      total: result.total || 0,
      list: result.list || []
    }
  } else if (params?.approvalType === 'finance') {
    const result = await http.get('/expense/pending/finance', { params })
    return {
      total: result.total || 0,
      list: result.list || []
    }
  }
  
  // 默认返回所有待审批
  const deptPending = await http.get('/expense/pending/dept', { params })
  const financePending = await http.get('/expense/pending/finance', { params })
  
  return {
    total: (deptPending.total || 0) + (financePending.total || 0),
    list: [...(deptPending.list || []), ...(financePending.list || [])]
  }
}

/**
 * 部门审批
 * @param id 报销单号
 * @param approval 审批表单数据
 * @returns 更新后的报销单
 */
export async function departmentApprove(id: string, approval: ApprovalForm): Promise<Expense> {
  return await http.post(`/expense/${id}/dept-approval`, approval)
}

/**
 * 财务审批
 * @param id 报销单号
 * @param approval 审批表单数据
 * @returns 更新后的报销单
 */
export async function financeApprove(id: string, approval: ApprovalForm): Promise<Expense> {
  return await http.post(`/expense/${id}/finance-approval`, approval)
}

// ==================== 3. 发票接口 ====================

/**
 * 验证发票唯一性
 * @param invoiceNumber 发票号码
 * @returns 验证结果
 */
export async function validateInvoice(invoiceNumber: string): Promise<{
  valid: boolean
  message: string
}> {
  return await http.get('/expense/invoices/validate', { params: { number: invoiceNumber } })
}

/**
 * OCR识别发票
 * @param imageUrl 发票图片URL
 * @returns 识别结果
 */
export async function ocrInvoice(imageUrl: string): Promise<Partial<Invoice>> {
  return await http.post('/expense/invoices/ocr', { imageUrl })
}

// ==================== 4. 打款接口 ====================

/**
 * 创建打款记录
 * @param id 报销单号
 * @returns 创建的打款记录
 */
export async function createPayment(id: string): Promise<PaymentRecord> {
  return await http.post(`/expense/${id}/payment`)
}

/**
 * 上传打款凭证
 * @param id 报销单号
 * @param proofUrl 打款凭证URL
 * @returns 更新后的报销单
 */
export async function uploadPaymentProof(
  id: string,
  proofUrl: string
): Promise<PaymentRecord> {
  return await http.post(`/expense/${id}/payment-proof`, null, { params: { proofUrl } })
}

/**
 * 获取打款列表
 * @param params 查询参数
 * @returns 打款记录列表
 */
export async function getPayments(params?: {
  status?: 'pending' | 'completed' | 'failed'
  page?: number
  size?: number
}): Promise<PageResponse<PaymentRecord>> {
  // 暂时返回空数据，后端可能需要添加此接口
  return {
    total: 0,
    list: []
  }
}

// ==================== 5. 统计接口 ====================

/**
 * 按部门统计
 * @param params 统计查询参数
 * @returns 部门统计数据
 */
export async function getDepartmentStats(params: StatsQueryParams): Promise<DepartmentStats[]> {
  return await http.get('/expense/stats/department', { params })
}

/**
 * 按类型统计
 * @param params 统计查询参数
 * @returns 类型统计数据
 */
export async function getTypeStats(params: StatsQueryParams): Promise<TypeStats[]> {
  return await http.get('/expense/stats/type', { params })
}

/**
 * 按月份统计
 * @param year 年份
 * @returns 月度统计数据
 */
export async function getMonthlyStats(year: number): Promise<MonthlyStats[]> {
  return await http.get('/expense/stats/monthly', { params: { year } })
}
