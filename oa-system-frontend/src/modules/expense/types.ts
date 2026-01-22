// 费用报销类型定义

/**
 * 报销类型
 */
export type ExpenseType = 'travel' | 'entertainment' | 'office' | 'transport' | 'communication' | 'other'

/**
 * 报销状态
 */
export type ExpenseStatus = 'draft' | 'dept_pending' | 'finance_pending' | 'paid' | 'rejected'

/**
 * 发票类型
 */
export type InvoiceType = 'vat_special' | 'vat_common' | 'electronic' | 'other'

/**
 * 费用分类
 */
export type ExpenseCategory = 'transport' | 'hotel' | 'meal' | 'office' | 'other'

/**
 * 费用明细项
 */
export interface ExpenseItem {
  id: number
  description: string
  amount: number
  date: string
  category: ExpenseCategory
}

/**
 * 发票信息
 */
export interface Invoice {
  id: number
  type: InvoiceType
  number: string
  amount: number
  date: string
  imageUrl: string
  fileList?: any[]
  ocrLoading?: boolean
}

/**
 * 报销表单
 */
export interface ExpenseForm {
  type: ExpenseType
  expenseDate: string
  reason: string
  items: ExpenseItem[]
  invoices: Invoice[]
  version?: number  // 乐观锁版本号，更新时必填
}

/**
 * 报销申请
 */
export interface ExpenseRequest {
  id: string
  serialNumber: string
  applicantId: string
  applicantName: string
  department: string
  type: ExpenseType
  expenseDate: string
  reason: string
  items: ExpenseItem[]
  invoices: Invoice[]
  totalAmount: number
  status: ExpenseStatus
  applyDate: string
  approval?: ApprovalInfo
}

/**
 * 审批信息
 */
export interface ApprovalInfo {
  deptApprover?: string
  deptApprovalDate?: string
  deptApprovalOpinion?: string
  financeApprover?: string
  financeApprovalDate?: string
  financeApprovalOpinion?: string
  paymentDate?: string
  paymentProof?: string
}

/**
 * 报销查询参数
 */
export interface ExpenseQuery {
  type?: ExpenseType | ''
  status?: ExpenseStatus | ''
  keyword?: string
  startDate?: string
  endDate?: string
  page?: number
  pageSize?: number
}

/**
 * 统计数据
 */
export interface ExpenseStatistics {
  totalAmount: number
  totalCount: number
  averageAmount: number
  maxAmount: number
  minAmount: number
  byType: {
    type: ExpenseType
    amount: number
    count: number
    percentage: number
  }[]
  byDepartment: {
    department: string
    amount: number
    count: number
    percentage: number
  }[]
  byMonth: {
    month: string
    amount: number
    count: number
  }[]
}

/**
 * 打款信息
 */
export interface PaymentInfo {
  id: string
  expenseId: string
  expenseSerialNumber: string
  amount: number
  bankAccount: string
  accountHolder: string
  bankName: string
  status: 'pending' | 'completed' | 'failed'
  paymentDate?: string
  proof?: string
  remark?: string
}

/**
 * OCR识别结果
 */
export interface OCRResult {
  invoiceType?: InvoiceType
  invoiceNumber?: string
  amount?: number
  date?: string
  sellerName?: string
  buyerName?: string
  taxNumber?: string
}

/**
 * 报销类型选项
 */
export const EXPENSE_TYPE_OPTIONS = [
  { label: '差旅费', value: 'travel' },
  { label: '招待费', value: 'entertainment' },
  { label: '办公用品', value: 'office' },
  { label: '交通费', value: 'transport' },
  { label: '通信费', value: 'communication' },
  { label: '其他', value: 'other' }
]

/**
 * 报销状态选项
 */
export const EXPENSE_STATUS_OPTIONS = [
  { label: '草稿', value: 'draft' },
  { label: '部门审批', value: 'dept_pending' },
  { label: '财务审批', value: 'finance_pending' },
  { label: '已打款', value: 'paid' },
  { label: '已驳回', value: 'rejected' }
]

/**
 * 发票类型选项
 */
export const INVOICE_TYPE_OPTIONS = [
  { label: '增值税专用发票', value: 'vat_special' },
  { label: '增值税普通发票', value: 'vat_common' },
  { label: '电子发票', value: 'electronic' },
  { label: '其他', value: 'other' }
]

/**
 * 费用分类选项
 */
export const EXPENSE_CATEGORY_OPTIONS = [
  { label: '交通费', value: 'transport' },
  { label: '住宿费', value: 'hotel' },
  { label: '餐费', value: 'meal' },
  { label: '办公费', value: 'office' },
  { label: '其他', value: 'other' }
]
