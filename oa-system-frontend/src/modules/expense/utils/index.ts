/**
 * 费用报销模块 - 工具函数库
 *
 * 基于 expense_Technical.md 规范实现
 */

import type {
  ExpenseType,
  ExpenseStatus,
  InvoiceType,
  PaymentMethod,
  PaymentStatus
} from '../types/index'

// ==================== 格式化函数 ====================

/**
 * 格式化日期
 */
export function formatDate(date: string | undefined): string {
  if (!date) return ''
  return date.split('T')[0]
}

/**
 * 格式化日期时间
 */
export function formatDateTime(date: string): string {
  if (!date) return ''
  const d = new Date(date)
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hour = String(d.getHours()).padStart(2, '0')
  const minute = String(d.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day} ${hour}:${minute}`
}

/**
 * 格式化金额
 */
export function formatAmount(amount: number): string {
  if (amount === undefined || amount === null) {
    return '¥0.00'
  }
  return `¥${amount.toFixed(2)}`
}

// ==================== 类型转换函数 ====================

/**
 * 获取报销类型名称
 */
export function getExpenseTypeName(type: ExpenseType): string {
  const typeMap: Record<ExpenseType, string> = {
    travel: '差旅费',
    hospitality: '招待费',
    office: '办公费',
    transport: '交通费',
    other: '其他'
  }
  return typeMap[type] || type
}

/**
 * 获取报销状态名称
 */
export function getExpenseStatusName(status: ExpenseStatus): string {
  const statusMap: Record<ExpenseStatus, string> = {
    draft: '草稿',
    dept_pending: '部门审批',
    finance_pending: '财务审批',
    paid: '待打款',
    completed: '已完成',
    rejected: '已驳回'
  }
  return statusMap[status] || status
}

/**
 * 获取报销状态标签类型(Element Plus tag type)
 */
export function getExpenseStatusType(status: ExpenseStatus): string {
  const typeMap: Record<ExpenseStatus, string> = {
    draft: 'info',
    dept_pending: 'warning',
    finance_pending: 'primary',
    paid: 'success',
    completed: 'success',
    rejected: 'danger'
  }
  return typeMap[status] || 'info'
}

/**
 * 获取发票类型名称
 */
export function getInvoiceTypeName(type: InvoiceType): string {
  const typeMap: Record<string, string> = {
    vat_special: '增值税专用发票',
    vat_common: '增值税普通发票',
    electronic: '电子发票',
    other: '其他'
  }
  return typeMap[type] || type
}

/**
 * 获取打款方式名称
 */
export function getPaymentMethodName(method: PaymentMethod): string {
  const methodMap: Record<PaymentMethod, string> = {
    bank_transfer: '银行转账',
    cash: '现金',
    check: '支票'
  }
  return methodMap[method] || method
}

/**
 * 获取打款状态名称
 */
export function getPaymentStatusName(status: PaymentStatus): string {
  const statusMap: Record<PaymentStatus, string> = {
    pending: '待打款',
    completed: '已完成',
    failed: '打款失败'
  }
  return statusMap[status] || status
}

// ==================== 状态判断函数 ====================

/**
 * 是否可编辑
 */
export function canEdit(status: ExpenseStatus): boolean {
  return status === 'draft'
}

/**
 * 是否可删除
 */
export function canDelete(status: ExpenseStatus): boolean {
  return status === 'draft'
}

/**
 * 是否可提交
 */
export function canSubmit(status: ExpenseStatus): boolean {
  return status === 'draft' || status === 'rejected'
}

/**
 * 是否可撤回
 */
export function canCancel(status: ExpenseStatus): boolean {
  return status === 'dept_pending' || status === 'finance_pending'
}

/**
 * 是否可部门审批
 */
export function canDeptApprove(status: ExpenseStatus): boolean {
  return status === 'dept_pending'
}

/**
 * 是否可财务审批
 */
export function canFinanceApprove(status: ExpenseStatus): boolean {
  return status === 'finance_pending'
}

/**
 * 是否可打款
 */
export function canPayment(status: ExpenseStatus): boolean {
  return status === 'paid'
}

/**
 * 是否可重新提交
 */
export function canResubmit(status: ExpenseStatus): boolean {
  return status === 'rejected'
}

// ==================== 发票验证函数 ====================

/**
 * 验证发票号码格式(8位或20位数字)
 */
export function validateInvoiceNumberFormat(invoiceNumber: string): boolean {
  const regex = /^\d{8}$|^\d{20}$/
  return regex.test(invoiceNumber)
}

/**
 * 验证发票金额一致性
 */
export function validateInvoiceAmount(invoices: any[], totalAmount: number): boolean {
  const invoiceTotal = invoices.reduce((sum, inv) => sum + inv.amount, 0)
  return Math.abs(invoiceTotal - totalAmount) < 0.01
}

// ==================== 大额加签规则 ====================

/**
 * 检查单笔金额加签
 */
export function checkSingleAmountApproval(amount: number): number {
  if (amount > 10000) return 3 // 总经理+特别审批人
  if (amount > 5000) return 2 // 总经理
  return 1 // 默认审批层级
}

/**
 * 获取审批层级名称
 */
export function getApprovalLevelName(level: number): string {
  const levelMap: Record<number, string> = {
    1: '基础审批',
    2: '加签审批(总经理)',
    3: '特别加签(总经理+特别审批人)'
  }
  return levelMap[level] || '基础审批'
}

// ==================== 报销单号生成 ====================

/**
 * 生成报销单号 EXP+YYYYMMDD+4位随机数
 */
export function generateExpenseId(): string {
  const now = new Date()
  const dateStr = now.toISOString().slice(0, 10).replace(/-/g, '')
  const random = Math.floor(Math.random() * 10000).toString().padStart(4, '0')
  return `EXP${dateStr}${random}`
}

// ==================== 计算函数 ====================

/**
 * 计算费用明细总金额
 */
export function calculateItemsTotal(items: any[]): number {
  return items.reduce((sum, item) => sum + item.amount, 0)
}

/**
 * 计算发票总金额
 */
export function calculateInvoicesTotal(invoices: any[]): number {
  return invoices.reduce((sum, invoice) => sum + invoice.amount, 0)
}

// ==================== 筛选函数 ====================

/**
 * 按部门筛选
 */
export function filterByDepartment(expenses: any[], departmentId: string): any[] {
  return expenses.filter(e => e.departmentId === departmentId)
}

/**
 * 按类型筛选
 */
export function filterByType(expenses: any[], type: ExpenseType): any[] {
  return expenses.filter(e => e.type === type)
}

/**
 * 按状态筛选
 */
export function filterByStatus(expenses: any[], status: ExpenseStatus): any[] {
  return expenses.filter(e => e.status === status)
}

/**
 * 按日期范围筛选
 */
export function filterByDateRange(expenses: any[], startDate: string, endDate: string): any[] {
  return expenses.filter(e => {
    const expenseDate = new Date(e.applyDate)
    return expenseDate >= new Date(startDate) && expenseDate <= new Date(endDate)
  })
}

// ==================== 排序函数 ====================

/**
 * 按金额排序
 */
export function sortByAmount(expenses: any[], order: 'asc' | 'desc' = 'desc'): any[] {
  return [...expenses].sort((a, b) => {
    return order === 'asc' ? a.amount - b.amount : b.amount - a.amount
  })
}

/**
 * 按日期排序
 */
export function sortByDate(expenses: any[], order: 'asc' | 'desc' = 'desc'): any[] {
  return [...expenses].sort((a, b) => {
    const dateA = new Date(a.applyDate).getTime()
    const dateB = new Date(b.applyDate).getTime()
    return order === 'asc' ? dateA - dateB : dateB - dateA
  })
}

// ==================== 工具提示函数 ====================

/**
 * 获取状态提示信息
 */
export function getStatusTip(status: ExpenseStatus): string {
  const tipMap: Record<ExpenseStatus, string> = {
    draft: '草稿状态的报销单可以编辑和删除',
    dept_pending: '等待部门主管审批',
    finance_pending: '等待财务人员审批',
    paid: '审批已完成，等待财务打款',
    completed: '报销流程已完成，打款凭证已上传',
    rejected: '报销已被驳回,可以修改后重新提交'
  }
  return tipMap[status] || ''
}

/**
 * 获取类型提示信息
 */
export function getTypeTip(type: ExpenseType): string {
  const tipMap: Record<ExpenseType, string> = {
    travel: '出差交通、住宿、餐补等费用',
    hospitality: '客户招待、商务宴请等费用',
    office: '办公设备、耗材等费用',
    transport: '市内交通、打车等费用',
    other: '其他类型的费用'
  }
  return tipMap[type] || ''
}
