/**
 * 费用报销模块 API 接口
 * @module expense/api
 *
 * 基于 specs/approval/expense/expense_Technical.md 规范实现
 * 所有接口使用Mock数据,返回Promise,延迟300-500ms模拟网络请求
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
import { mockExpenses, mockPayments } from '../mock/data'
import { generateExpenseId } from '../utils'

// ==================== 工具函数 ====================

/**
 * 模拟网络延迟(300-500ms)
 */
function delay(ms: number = 300): Promise<void> {
  const randomDelay = ms + Math.random() * 200
  return new Promise(resolve => setTimeout(resolve, randomDelay))
}

/**
 * 生成模拟延迟(300-500ms随机)
 */
async function mockDelay(): Promise<void> {
  await delay(300 + Math.random() * 200)
}

// ==================== 1. 报销单管理接口 ====================

/**
 * 获取我的报销列表
 * @param params 查询参数
 * @returns 报销单分页列表
 */
export async function getMyExpenses(params?: ExpenseQueryParams): Promise<PageResponse<Expense>> {
  await mockDelay()

  let filtered = [...mockExpenses]

  // 状态筛选
  if (params?.status) {
    filtered = filtered.filter(expense => expense.status === params.status)
  }

  // 类型筛选
  if (params?.type) {
    filtered = filtered.filter(expense => expense.type === params.type)
  }

  // 申请人筛选
  if (params?.applicantId) {
    filtered = filtered.filter(expense => expense.applicantId === params.applicantId)
  }

  // 部门筛选
  if (params?.departmentId) {
    filtered = filtered.filter(expense => expense.departmentId === params.departmentId)
  }

  // 日期范围筛选
  if (params?.startDate) {
    filtered = filtered.filter(expense => expense.applyDate >= params.startDate!)
  }

  if (params?.endDate) {
    filtered = filtered.filter(expense => expense.applyDate <= params.endDate!)
  }

  // 分页
  const page = params?.page || 1
  const size = params?.size || 10
  const start = (page - 1) * size
  const end = start + size

  return {
    total: filtered.length,
    list: filtered.slice(start, end)
  }
}

/**
 * 获取报销详情
 * @param id 报销单号
 * @returns 报销单详情
 */
export async function getExpense(id: string): Promise<Expense> {
  await mockDelay()

  const expense = mockExpenses.find(e => e.id === id)

  if (!expense) {
    throw new Error(`报销单 ${id} 不存在`)
  }

  return expense
}

/**
 * 创建报销单
 * @param data 报销单表单数据
 * @returns 创建的报销单
 */
export async function createExpense(data: ExpenseForm): Promise<Expense> {
  await mockDelay()

  // 计算总金额
  const totalAmount = data.items.reduce((sum, item) => sum + item.amount, 0)

  // 生成报销单号
  const expenseId = generateExpenseId()

  const now = new Date().toISOString()

  const newExpense: Expense = {
    id: expenseId,
    applicantId: 'U001', // 模拟当前用户ID
    applicantName: '张三', // 模拟当前用户姓名
    departmentId: 'D001', // 模拟当前用户部门ID
    departmentName: '销售部', // 模拟当前用户部门名称
    type: data.type,
    amount: totalAmount,
    items: data.items,
    invoices: data.invoices,
    reason: data.reason,
    applyDate: now,
    expenseDate: data.expenseDate,
    status: 'draft',
    createdAt: now,
    updatedAt: now
  }

  // 添加到Mock数据
  mockExpenses.unshift(newExpense)

  return newExpense
}

/**
 * 更新报销单
 * @param id 报销单号
 * @param data 报销单表单数据
 * @returns 更新的报销单
 */
export async function updateExpense(id: string, data: Partial<ExpenseForm>): Promise<Expense> {
  await mockDelay()

  const index = mockExpenses.findIndex(e => e.id === id)

  if (index === -1) {
    throw new Error(`报销单 ${id} 不存在`)
  }

  const expense = mockExpenses[index]

  // 只能编辑草稿状态
  if (expense.status !== 'draft') {
    throw new Error('只能编辑草稿状态的报销单')
  }

  // 更新字段
  if (data.type) {
    expense.type = data.type
  }

  if (data.items) {
    expense.items = data.items
    // 重新计算总金额
    expense.amount = data.items.reduce((sum, item) => sum + item.amount, 0)
  }

  if (data.invoices) {
    expense.invoices = data.invoices
  }

  if (data.reason) {
    expense.reason = data.reason
  }

  if (data.expenseDate) {
    expense.expenseDate = data.expenseDate
  }

  expense.updatedAt = new Date().toISOString()

  return expense
}

/**
 * 删除报销单
 * @param id 报销单号
 */
export async function deleteExpense(id: string): Promise<void> {
  await mockDelay()

  const index = mockExpenses.findIndex(e => e.id === id)

  if (index === -1) {
    throw new Error(`报销单 ${id} 不存在`)
  }

  const expense = mockExpenses[index]

  // 只能删除草稿状态
  if (expense.status !== 'draft') {
    throw new Error('只能删除草稿状态的报销单')
  }

  // 从Mock数据中删除
  mockExpenses.splice(index, 1)
}

/**
 * 提交审批
 * @param id 报销单号
 * @returns 更新后的报销单
 */
export async function submitExpense(id: string): Promise<Expense> {
  await mockDelay()

  const expense = mockExpenses.find(e => e.id === id)

  if (!expense) {
    throw new Error(`报销单 ${id} 不存在`)
  }

  if (expense.status !== 'draft') {
    throw new Error('只能提交草稿状态的报销单')
  }

  // 验证发票
  if (!expense.invoices || expense.invoices.length === 0) {
    throw new Error('请至少上传一张发票')
  }

  // 验证金额一致性
  const invoiceTotal = expense.invoices.reduce((sum, inv) => sum + inv.amount, 0)
  if (Math.abs(invoiceTotal - expense.amount) > 0.01) {
    throw new Error(`发票总金额(${invoiceTotal})与报销金额(${expense.amount})不一致`)
  }

  // 更新状态为部门审批中
  expense.status = 'dept_pending'
  expense.departmentApproval = {
    status: 'pending'
  }
  expense.updatedAt = new Date().toISOString()

  return expense
}

/**
 * 撤销报销单
 * @param id 报销单号
 * @returns 更新后的报销单
 */
export async function cancelExpense(id: string): Promise<Expense> {
  await mockDelay()

  const expense = mockExpenses.find(e => e.id === id)

  if (!expense) {
    throw new Error(`报销单 ${id} 不存在`)
  }

  // 只能撤销待审批状态
  if (expense.status !== 'dept_pending' && expense.status !== 'finance_pending') {
    throw new Error('只能撤销待审批状态的报销单')
  }

  // 恢复为草稿状态
  expense.status = 'draft'
  expense.departmentApproval = undefined
  expense.financeApproval = undefined
  expense.updatedAt = new Date().toISOString()

  return expense
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
  await mockDelay()

  let filtered = mockExpenses.filter(expense =>
    expense.status === 'dept_pending' || expense.status === 'finance_pending'
  )

  // 审批类型筛选
  if (params?.approvalType === 'department') {
    filtered = filtered.filter(expense => expense.status === 'dept_pending')
  } else if (params?.approvalType === 'finance') {
    filtered = filtered.filter(expense => expense.status === 'finance_pending')
  }

  // 部门筛选
  if (params?.departmentId) {
    filtered = filtered.filter(expense => expense.departmentId === params.departmentId)
  }

  // 分页
  const page = params?.page || 1
  const size = params?.size || 10
  const start = (page - 1) * size
  const end = start + size

  return {
    total: filtered.length,
    list: filtered.slice(start, end)
  }
}

/**
 * 部门审批
 * @param id 报销单号
 * @param approval 审批表单数据
 * @returns 更新后的报销单
 */
export async function departmentApprove(id: string, approval: ApprovalForm): Promise<Expense> {
  await mockDelay()

  const expense = mockExpenses.find(e => e.id === id)

  if (!expense) {
    throw new Error(`报销单 ${id} 不存在`)
  }

  if (expense.status !== 'dept_pending') {
    throw new Error('当前状态不允许部门审批')
  }

  const now = new Date().toISOString()

  if (approval.status === 'approved') {
    // 审批通过,进入财务审批
    expense.status = 'finance_pending'
    expense.departmentApproval = {
      approverId: 'U010', // 模拟当前审批人ID
      approverName: '李经理', // 模拟当前审批人姓名
      status: 'approved',
      opinion: approval.opinion,
      timestamp: now
    }
    expense.financeApproval = {
      status: 'pending'
    }
  } else {
    // 驳回
    expense.status = 'rejected'
    expense.departmentApproval = {
      approverId: 'U010',
      approverName: '李经理',
      status: 'rejected',
      opinion: approval.opinion,
      timestamp: now
    }
  }

  expense.updatedAt = now

  return expense
}

/**
 * 财务审批
 * @param id 报销单号
 * @param approval 审批表单数据
 * @returns 更新后的报销单
 */
export async function financeApprove(id: string, approval: ApprovalForm): Promise<Expense> {
  await mockDelay()

  const expense = mockExpenses.find(e => e.id === id)

  if (!expense) {
    throw new Error(`报销单 ${id} 不存在`)
  }

  if (expense.status !== 'finance_pending') {
    throw new Error('当前状态不允许财务审批')
  }

  const now = new Date().toISOString()

  if (approval.status === 'approved') {
    // 审批通过,自动创建打款记录
    expense.status = 'paid'
    expense.financeApproval = {
      approverId: 'U020', // 模拟当前审批人ID
      approverName: '王财务', // 模拟当前审批人姓名
      status: 'approved',
      opinion: approval.opinion,
      timestamp: now
    }

    // 创建打款记录
    const payment: PaymentRecord = {
      id: mockPayments.length + 1,
      expenseId: expense.id,
      amount: expense.amount,
      paymentMethod: 'bank_transfer',
      paymentDate: now,
      bankAccount: '6222021234567890123',
      status: 'pending',
      remark: '财务审批通过,等待打款'
    }
    mockPayments.push(payment)
  } else {
    // 驳回
    expense.status = 'rejected'
    expense.financeApproval = {
      approverId: 'U020',
      approverName: '王财务',
      status: 'rejected',
      opinion: approval.opinion,
      timestamp: now
    }
  }

  expense.updatedAt = now

  return expense
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
  await mockDelay()

  // 检查发票号码格式
  const regex = /^\d{8}$|^\d{20}$/
  if (!regex.test(invoiceNumber)) {
    return {
      valid: false,
      message: '发票号码格式不正确,应为8位或20位数字'
    }
  }

  // 检查是否已被使用
  for (const expense of mockExpenses) {
    for (const invoice of expense.invoices) {
      if (invoice.number === invoiceNumber) {
        return {
          valid: false,
          message: `发票号 ${invoiceNumber} 已被使用,不能重复报销`
        }
      }
    }
  }

  return {
    valid: true,
    message: '发票号可用'
  }
}

/**
 * OCR识别发票(模拟)
 * @param imageUrl 发票图片URL
 * @returns 识别结果
 */
export async function ocrInvoice(imageUrl: string): Promise<Partial<Invoice>> {
  await delay(500) // OCR识别延迟500ms

  // 模拟OCR识别结果
  const mockInvoice: Partial<Invoice> = {
    type: 'vat_common',
    number: Math.floor(Math.random() * 90000000 + 10000000).toString(), // 随机8位数字
    amount: Math.floor(Math.random() * 5000 * 100) / 100, // 随机金额,保留2位小数
    date: new Date().toISOString().split('T')[0],
    imageUrl: imageUrl,
    verified: false
  }

  return mockInvoice
}

// ==================== 4. 打款接口 ====================

/**
 * 创建打款记录
 * @param id 报销单号
 * @returns 创建的打款记录
 */
export async function createPayment(id: string): Promise<PaymentRecord> {
  await mockDelay()

  const expense = mockExpenses.find(e => e.id === id)

  if (!expense) {
    throw new Error(`报销单 ${id} 不存在`)
  }

  if (expense.status !== 'finance_pending') {
    throw new Error('报销单未通过财务审批,无法创建打款记录')
  }

  // 检查是否已存在打款记录
  const existingPayment = mockPayments.find(p => p.expenseId === id)
  if (existingPayment) {
    throw new Error('该报销单已存在打款记录')
  }

  const now = new Date().toISOString()

  const payment: PaymentRecord = {
    id: mockPayments.length + 1,
    expenseId: expense.id,
    amount: expense.amount,
    paymentMethod: 'bank_transfer',
    paymentDate: now.split('T')[0],
    bankAccount: '6222021234567890123',
    status: 'pending',
    remark: '等待打款'
  }

  mockPayments.push(payment)

  return payment
}

/**
 * 上传打款凭证
 * @param paymentId 打款记录ID
 * @param proofUrl 打款凭证URL
 * @returns 更新后的打款记录
 */
export async function uploadPaymentProof(
  paymentId: number,
  proofUrl: string
): Promise<PaymentRecord> {
  await mockDelay()

  const payment = mockPayments.find(p => p.id === paymentId)

  if (!payment) {
    throw new Error(`打款记录 ${paymentId} 不存在`)
  }

  // 更新打款记录
  payment.proof = proofUrl
  payment.status = 'completed'

  // 更新报销单状态
  const expense = mockExpenses.find(e => e.id === payment.expenseId)
  if (expense) {
    expense.status = 'paid'
    expense.paymentDate = payment.paymentDate
    expense.paymentProof = proofUrl
    expense.updatedAt = new Date().toISOString()
  }

  return payment
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
  await mockDelay()

  let filtered = [...mockPayments]

  // 状态筛选
  if (params?.status) {
    filtered = filtered.filter(payment => payment.status === params.status)
  }

  // 分页
  const page = params?.page || 1
  const size = params?.size || 10
  const start = (page - 1) * size
  const end = start + size

  return {
    total: filtered.length,
    list: filtered.slice(start, end)
  }
}

// ==================== 5. 统计接口 ====================

/**
 * 按部门统计
 * @param params 统计查询参数
 * @returns 部门统计数据
 */
export async function getDepartmentStats(params: StatsQueryParams): Promise<DepartmentStats[]> {
  await mockDelay()

  // 筛选日期范围内的报销单
  let filtered = mockExpenses.filter(expense => {
    const applyDate = expense.applyDate.split('T')[0]
    return applyDate >= params.startDate && applyDate <= params.endDate
  })

  // 按部门分组统计
  const deptMap = new Map<string, DepartmentStats>()

  filtered.forEach(expense => {
    const existing = deptMap.get(expense.departmentId)

    if (existing) {
      existing.totalAmount += expense.amount
      existing.count += 1
    } else {
      deptMap.set(expense.departmentId, {
        departmentId: expense.departmentId,
        departmentName: expense.departmentName,
        totalAmount: expense.amount,
        count: 1,
        avgAmount: expense.amount
      })
    }
  })

  // 计算平均金额
  const result = Array.from(deptMap.values())
  result.forEach(stat => {
    stat.avgAmount = stat.totalAmount / stat.count
  })

  // 按总金额降序排序
  result.sort((a, b) => b.totalAmount - a.totalAmount)

  return result
}

/**
 * 按类型统计
 * @param params 统计查询参数
 * @returns 类型统计数据
 */
export async function getTypeStats(params: StatsQueryParams): Promise<TypeStats[]> {
  await mockDelay()

  // 筛选日期范围内的报销单
  let filtered = mockExpenses.filter(expense => {
    const applyDate = expense.applyDate.split('T')[0]
    return applyDate >= params.startDate && applyDate <= params.endDate
  })

  // 类型筛选
  if (params.type) {
    filtered = filtered.filter(expense => expense.type === params.type)
  }

  // 按类型分组统计
  const typeMap = new Map<string, TypeStats>()

  filtered.forEach(expense => {
    const existing = typeMap.get(expense.type)

    if (existing) {
      existing.totalAmount += expense.amount
      existing.count += 1
    } else {
      typeMap.set(expense.type, {
        type: expense.type,
        totalAmount: expense.amount,
        count: 1,
        percentage: 0
      })
    }
  })

  // 计算总金额
  const totalAmount = Array.from(typeMap.values()).reduce((sum, stat) => sum + stat.totalAmount, 0)

  // 计算百分比
  const result = Array.from(typeMap.values())
  result.forEach(stat => {
    stat.percentage = totalAmount > 0 ? (stat.totalAmount / totalAmount) * 100 : 0
  })

  // 按总金额降序排序
  result.sort((a, b) => b.totalAmount - a.totalAmount)

  return result
}

/**
 * 按月份统计
 * @param year 年份
 * @returns 月度统计数据
 */
export async function getMonthlyStats(year: number): Promise<MonthlyStats[]> {
  await mockDelay()

  // 筛选指定年份的报销单
  const filtered = mockExpenses.filter(expense => {
    const applyDate = new Date(expense.applyDate)
    return applyDate.getFullYear() === year
  })

  // 按月份分组统计
  const monthMap = new Map<string, MonthlyStats>()

  filtered.forEach(expense => {
    const applyDate = new Date(expense.applyDate)
    const monthKey = `${year}-${String(applyDate.getMonth() + 1).padStart(2, '0')}`

    const existing = monthMap.get(monthKey)

    if (existing) {
      existing.totalAmount += expense.amount
      existing.count += 1
    } else {
      monthMap.set(monthKey, {
        month: monthKey,
        totalAmount: expense.amount,
        count: 1
      })
    }
  })

  // 按月份排序
  const result = Array.from(monthMap.values())
  result.sort((a, b) => a.month.localeCompare(b.month))

  return result
}
