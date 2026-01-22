/**
 * 费用报销模块 - TypeScript类型定义
 *
 * 基于 expense_Technical.md 规范实现
 */

// ==================== 基础类型 ====================

/**
 * 报销类型
 */
export type ExpenseType = 'travel' | 'hospitality' | 'office' | 'transport' | 'other'

/**
 * 报销状态
 */
export type ExpenseStatus = 'draft' | 'dept_pending' | 'finance_pending' | 'paid' | 'rejected'

/**
 * 发票类型
 */
export type InvoiceType = 'vat_special' | 'vat_common' | 'electronic'

/**
 * 打款方式
 */
export type PaymentMethod = 'bank_transfer' | 'cash' | 'check'

/**
 * 打款状态
 */
export type PaymentStatus = 'pending' | 'completed' | 'failed'

/**
 * 审批状态
 */
export type ApprovalStatus = 'pending' | 'approved' | 'rejected'

// ==================== 核心实体 ====================

/**
 * 报销单实体
 */
export interface Expense {
  id: string
  applicantId: string
  applicantName: string
  departmentId: string
  departmentName: string
  type: ExpenseType
  amount: number
  items: ExpenseItem[]
  invoices: Invoice[]
  reason: string
  applyDate: string
  expenseDate: string

  // 状态
  status: ExpenseStatus

  // 部门审批
  departmentApproval?: ApprovalRecord

  // 财务审批
  financeApproval?: ApprovalRecord

  // 打款信息
  paymentDate?: string
  paymentProof?: string

  // 审计字段
  createdAt: string
  updatedAt: string
}

/**
 * 费用明细
 */
export interface ExpenseItem {
  id?: number
  description: string
  amount: number
  date: string
  category: string
}

/**
 * 发票
 */
export interface Invoice {
  id?: number
  type: InvoiceType
  number: string
  amount: number
  date: string
  imageUrl: string
  verified: boolean
  // 后端字段名
  invoiceType?: string
  invoiceNumber?: string
  invoiceDate?: string
  fileList?: any[]
  ocrLoading?: boolean
}

/**
 * 审批记录
 */
export interface ApprovalRecord {
  approverId?: string
  approverName?: string
  status: ApprovalStatus
  opinion?: string
  timestamp?: string
}

/**
 * 打款记录
 */
export interface PaymentRecord {
  id?: number
  expenseId: string
  amount: number
  paymentMethod: PaymentMethod
  paymentDate: string
  bankAccount?: string
  proof?: string
  status: PaymentStatus
  remark?: string
}

// ==================== 表单类型 ====================

/**
 * 报销单表单
 */
export interface ExpenseForm {
  type: ExpenseType
  items: ExpenseItem[]
  invoices: Invoice[]
  reason: string
  expenseDate: string
}

/**
 * 审批表单
 */
export interface ApprovalForm {
  status: 'approved' | 'rejected'
  opinion: string
}

/**
 * 打款表单
 */
export interface PaymentForm {
  paymentMethod: PaymentMethod
  paymentDate: string
  bankAccount: string
  proof: string
  remark: string
}

// ==================== 统计类型 ====================

/**
 * 部门统计
 */
export interface DepartmentStats {
  departmentId: string
  departmentName: string
  totalAmount: number
  count: number
  avgAmount: number
}

/**
 * 类型统计
 */
export interface TypeStats {
  type: ExpenseType
  totalAmount: number
  count: number
  percentage: number
}

/**
 * 月度统计
 */
export interface MonthlyStats {
  month: string
  totalAmount: number
  count: number
}

/**
 * 员工统计
 */
export interface EmployeeStats {
  employeeId: string
  employeeName: string
  totalAmount: number
  count: number
}

// ==================== 查询参数类型 ====================

/**
 * 报销单查询参数
 */
export interface ExpenseQueryParams {
  page?: number
  size?: number
  type?: ExpenseType
  status?: ExpenseStatus
  applicantId?: string
  departmentId?: string
  startDate?: string
  endDate?: string
}

/**
 * 统计查询参数
 */
export interface StatsQueryParams {
  startDate: string
  endDate: string
  departmentId?: string
  type?: ExpenseType
}

// ==================== 分页类型 ====================

/**
 * 分页响应
 */
export interface PageResponse<T> {
  total: number
  list: T[]
}

/**
 * 分页参数
 */
export interface PageParams {
  page: number
  pageSize: number
}
