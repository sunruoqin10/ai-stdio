/**
 * 费用报销模块 - Mock数据
 *
 * 包含报销单、费用明细、发票、打款记录等测试数据
 * 严格遵循TypeScript类型定义和业务规则
 */

import type {
  Expense,
  ExpenseItem,
  Invoice,
  PaymentRecord,
  ExpenseStatus,
  ExpenseType,
  InvoiceType,
  PaymentMethod,
  PaymentStatus
} from '../types'
import { generateExpenseId } from '../utils'

// ==================== 报销单Mock数据 ====================

/**
 * 报销单Mock数据集(6条,涵盖所有状态)
 */
export const mockExpenses: Expense[] = [
  // 1. 差旅费 - 已打款状态
  {
    id: generateExpenseId(),
    applicantId: 'U001',
    applicantName: '张三',
    departmentId: 'D001',
    departmentName: '销售部',
    type: 'travel',
    amount: 3280.00,
    reason: '上海客户拜访差旅费用',
    applyDate: '2025-12-15T09:30:00.000Z',
    expenseDate: '2025-12-10T00:00:00.000Z',
    status: 'paid',
    departmentApproval: {
      approverId: 'U010',
      approverName: '李经理',
      status: 'approved',
      opinion: '差旅费用合理,同意报销',
      timestamp: '2025-12-15T14:20:00.000Z'
    },
    financeApproval: {
      approverId: 'U020',
      approverName: '王财务',
      status: 'approved',
      opinion: '发票齐全,金额核对无误',
      timestamp: '2025-12-16T10:15:00.000Z'
    },
    paymentDate: '2025-12-17T14:30:00.000Z',
    paymentProof: '/uploads/payments/EXP202512150001_proof.pdf',
    items: [
      {
        id: 1,
        description: '高铁票 北京-上海二等座',
        amount: 553.00,
        date: '2025-12-10T00:00:00.000Z',
        category: '交通费'
      },
      {
        id: 2,
        description: '上海宾馆住宿3晚',
        amount: 1680.00,
        date: '2025-12-10T00:00:00.000Z',
        category: '住宿费'
      },
      {
        id: 3,
        description: '出差餐补3天',
        amount: 180.00,
        date: '2025-12-10T00:00:00.000Z',
        category: '餐补'
      },
      {
        id: 4,
        description: '市内交通费',
        amount: 127.00,
        date: '2025-12-12T00:00:00.000Z',
        category: '交通费'
      },
      {
        id: 5,
        description: '客户招待费',
        amount: 740.00,
        date: '2025-12-11T00:00:00.000Z',
        category: '招待费'
      }
    ],
    invoices: [
      {
        id: 1,
        type: 'electronic',
        number: '011001800111',
        amount: 553.00,
        date: '2025-12-10T00:00:00.000Z',
        imageUrl: '/uploads/invoices/train_ticket_001.jpg',
        verified: true
      },
      {
        id: 2,
        type: 'vat_common',
        number: '031001800222',
        amount: 1680.00,
        date: '2025-12-13T00:00:00.000Z',
        imageUrl: '/uploads/invoices/hotel_invoice_001.pdf',
        verified: true
      },
      {
        id: 3,
        type: 'vat_common',
        number: '031001800333',
        amount: 740.00,
        date: '2025-12-11T00:00:00.000Z',
        imageUrl: '/uploads/invoices/restaurant_001.pdf',
        verified: true
      }
    ],
    createdAt: '2025-12-15T09:30:00.000Z',
    updatedAt: '2025-12-17T14:30:00.000Z'
  },

  // 2. 招待费 - 财务审批中
  {
    id: generateExpenseId(),
    applicantId: 'U002',
    applicantName: '李四',
    departmentId: 'D002',
    departmentName: '市场部',
    type: 'hospitality',
    amount: 1580.00,
    reason: '重要客户商务宴请',
    applyDate: '2025-12-20T10:15:00.000Z',
    expenseDate: '2025-12-18T00:00:00.000Z',
    status: 'finance_pending',
    departmentApproval: {
      approverId: 'U011',
      approverName: '赵总监',
      status: 'approved',
      opinion: '客户项目合作关键期,招待费用合理',
      timestamp: '2025-12-20T15:45:00.000Z'
    },
    financeApproval: {
      status: 'pending'
    },
    items: [
      {
        id: 1,
        description: '海鲜酒包间费',
        amount: 1200.00,
        date: '2025-12-18T00:00:00.000Z',
        category: '餐费'
      },
      {
        id: 2,
        description: '酒水费',
        amount: 380.00,
        date: '2025-12-18T00:00:00.000Z',
        category: '酒水费'
      }
    ],
    invoices: [
      {
        id: 1,
        type: 'vat_common',
        number: '044031900444',
        amount: 1580.00,
        date: '2025-12-18T00:00:00.000Z',
        imageUrl: '/uploads/invoices/restaurant_002.pdf',
        verified: true
      }
    ],
    createdAt: '2025-12-20T10:15:00.000Z',
    updatedAt: '2025-12-20T15:45:00.000Z'
  },

  // 3. 办公用品 - 部门审批中
  {
    id: generateExpenseId(),
    applicantId: 'U003',
    applicantName: '王五',
    departmentId: 'D003',
    departmentName: '行政部',
    type: 'office',
    amount: 2890.50,
    reason: '部门办公设备及耗材采购',
    applyDate: '2025-12-22T14:20:00.000Z',
    expenseDate: '2025-12-21T00:00:00.000Z',
    status: 'dept_pending',
    departmentApproval: {
      status: 'pending'
    },
    items: [
      {
        id: 1,
        description: '无线打印机 HP LaserJet Pro',
        amount: 1280.00,
        date: '2025-12-21T00:00:00.000Z',
        category: '办公设备'
      },
      {
        id: 2,
        description: '打印纸(箱)',
        amount: 180.00,
        date: '2025-12-21T00:00:00.000Z',
        category: '耗材'
      },
      {
        id: 3,
        description: '文件夹、笔等办公用品',
        amount: 95.50,
        date: '2025-12-21T00:00:00.000Z',
        category: '耗材'
      },
      {
        id: 4,
        description: '办公椅(人体工学)',
        amount: 1335.00,
        date: '2025-12-21T00:00:00.000Z',
        category: '办公家具'
      }
    ],
    invoices: [
      {
        id: 1,
        type: 'vat_common',
        number: '065001900555',
        amount: 1455.00,
        date: '2025-12-21T00:00:00.000Z',
        imageUrl: '/uploads/invoices/office_supplies_001.pdf',
        verified: true
      },
      {
        id: 2,
        type: 'vat_common',
        number: '065001900666',
        amount: 1435.50,
        date: '2025-12-21T00:00:00.000Z',
        imageUrl: '/uploads/invoices/office_supplies_002.pdf',
        verified: true
      }
    ],
    createdAt: '2025-12-22T14:20:00.000Z',
    updatedAt: '2025-12-22T14:20:00.000Z'
  },

  // 4. 交通费 - 已驳回
  {
    id: generateExpenseId(),
    applicantId: 'U004',
    applicantName: '赵六',
    departmentId: 'D004',
    departmentName: '研发部',
    type: 'transport',
    amount: 380.00,
    reason: '加班打车费报销',
    applyDate: '2025-12-18T16:30:00.000Z',
    expenseDate: '2025-12-17T00:00:00.000Z',
    status: 'rejected',
    departmentApproval: {
      approverId: 'U012',
      approverName: '孙主管',
      status: 'rejected',
      opinion: '缺少加班审批单,且打车费未使用公司指定网约车平台,不予报销',
      timestamp: '2025-12-19T09:10:00.000Z'
    },
    items: [
      {
        id: 1,
        description: '加班打车费(22:30)',
        amount: 45.00,
        date: '2025-12-15T00:00:00.000Z',
        category: '打车费'
      },
      {
        id: 2,
        description: '加班打车费(23:15)',
        amount: 52.00,
        date: '2025-12-16T00:00:00.000Z',
        category: '打车费'
      },
      {
        id: 3,
        description: '加班打车费(21:45)',
        amount: 38.00,
        date: '2025-12-17T00:00:00.000Z',
        category: '打车费'
      }
    ],
    invoices: [
      {
        id: 1,
        type: 'electronic',
        number: '2025121712345',
        amount: 45.00,
        date: '2025-12-15T00:00:00.000Z',
        imageUrl: '/uploads/invoices/taxi_001.jpg',
        verified: true
      },
      {
        id: 2,
        type: 'electronic',
        number: '2025121613456',
        amount: 52.00,
        date: '2025-12-16T00:00:00.000Z',
        imageUrl: '/uploads/invoices/taxi_002.jpg',
        verified: true
      },
      {
        id: 3,
        type: 'electronic',
        number: '2025121724567',
        amount: 38.00,
        date: '2025-12-17T00:00:00.000Z',
        imageUrl: '/uploads/invoices/taxi_003.jpg',
        verified: true
      }
    ],
    createdAt: '2025-12-18T16:30:00.000Z',
    updatedAt: '2025-12-19T09:10:00.000Z'
  },

  // 5. 其他费用 - 草稿状态
  {
    id: generateExpenseId(),
    applicantId: 'U005',
    applicantName: '孙七',
    departmentId: 'D005',
    departmentName: '人力资源部',
    type: 'other',
    amount: 850.00,
    reason: '员工团建活动费用',
    applyDate: '2025-12-23T11:00:00.000Z',
    expenseDate: '2025-12-20T00:00:00.000Z',
    status: 'draft',
    items: [
      {
        id: 1,
        description: '团建场地租赁费',
        amount: 500.00,
        date: '2025-12-20T00:00:00.000Z',
        category: '活动费'
      },
      {
        id: 2,
        description: '活动零食饮料',
        amount: 200.00,
        date: '2025-12-20T00:00:00.000Z',
        category: '食品费'
      },
      {
        id: 3,
        description: '活动道具采购',
        amount: 150.00,
        date: '2025-12-20T00:00:00.000Z',
        category: '物料费'
      }
    ],
    invoices: [
      {
        id: 1,
        type: 'vat_common',
        number: '075001900777',
        amount: 700.00,
        date: '2025-12-20T00:00:00.000Z',
        imageUrl: '/uploads/invoices/team_building_001.pdf',
        verified: true
      }
    ],
    createdAt: '2025-12-23T11:00:00.000Z',
    updatedAt: '2025-12-23T11:00:00.000Z'
  },

  // 6. 差旅费 - 部门审批中(大额需要总经理加签)
  {
    id: generateExpenseId(),
    applicantId: 'U006',
    applicantName: '周八',
    departmentId: 'D001',
    departmentName: '销售部',
    type: 'travel',
    amount: 12580.00,
    reason: '广州展会出差费用',
    applyDate: '2025-12-21T08:45:00.000Z',
    expenseDate: '2025-12-15T00:00:00.000Z',
    status: 'dept_pending',
    departmentApproval: {
      status: 'pending'
    },
    items: [
      {
        id: 1,
        description: '机票 北京-广州往返',
        amount: 3680.00,
        date: '2025-12-15T00:00:00.000Z',
        category: '交通费'
      },
      {
        id: 2,
        description: '酒店住宿5晚',
        amount: 5500.00,
        date: '2025-12-15T00:00:00.000Z',
        category: '住宿费'
      },
      {
        id: 3,
        description: '出差餐补5天',
        amount: 300.00,
        date: '2025-12-15T00:00:00.000Z',
        category: '餐补'
      },
      {
        id: 4,
        description: '展会注册费',
        amount: 2800.00,
        date: '2025-12-16T00:00:00.000Z',
        category: '业务费'
      },
      {
        id: 5,
        description: '市内交通费',
        amount: 300.00,
        date: '2025-12-17T00:00:00.000Z',
        category: '交通费'
      }
    ],
    invoices: [
      {
        id: 1,
        type: 'vat_special',
        number: '01100190011188888',
        amount: 3680.00,
        date: '2025-12-15T00:00:00.000Z',
        imageUrl: '/uploads/invoices/flight_001.pdf',
        verified: true
      },
      {
        id: 2,
        type: 'vat_special',
        number: '04403190022299999',
        amount: 5500.00,
        date: '2025-12-20T00:00:00.000Z',
        imageUrl: '/uploads/invoices/hotel_002.pdf',
        verified: true
      },
      {
        id: 3,
        type: 'vat_common',
        number: '044031900333',
        amount: 2800.00,
        date: '2025-12-16T00:00:00.000Z',
        imageUrl: '/uploads/invoices/exhibition_001.pdf',
        verified: true
      }
    ],
    createdAt: '2025-12-21T08:45:00.000Z',
    updatedAt: '2025-12-21T08:45:00.000Z'
  }
]

// ==================== 打款记录Mock数据 ====================

/**
 * 打款记录Mock数据集(4条)
 */
export const mockPayments: PaymentRecord[] = [
  // 1. 已完成的银行转账
  {
    id: 1,
    expenseId: mockExpenses[0].id,
    amount: 3280.00,
    paymentMethod: 'bank_transfer',
    paymentDate: '2025-12-17T14:30:00.000Z',
    bankAccount: '6222021234567890123',
    proof: '/uploads/payments/EXP202512150001_proof.pdf',
    status: 'completed',
    remark: '工商银行转账,流水号: TX202512171430001'
  },
  // 2. 待打款的银行转账
  {
    id: 2,
    expenseId: 'EXP202512200002',
    amount: 1580.00,
    paymentMethod: 'bank_transfer',
    paymentDate: '2025-12-24T10:00:00.000Z',
    bankAccount: '6222021234567890124',
    status: 'pending',
    remark: '等待财务审批后打款'
  },
  // 3. 失败的支票打款
  {
    id: 3,
    expenseId: 'EXP202512180003',
    amount: 850.00,
    paymentMethod: 'check',
    paymentDate: '2025-12-19T15:20:00.000Z',
    bankAccount: '6222021234567890125',
    status: 'failed',
    remark: '支票账户余额不足,需重新打款'
  },
  // 4. 已完成的现金打款
  {
    id: 4,
    expenseId: 'EXP202512150004',
    amount: 280.00,
    paymentMethod: 'cash',
    paymentDate: '2025-12-16T11:00:00.000Z',
    proof: '/uploads/payments/cash_001_receipt.pdf',
    status: 'completed',
    remark: '现金领取,已签字确认'
  }
]

// ==================== 按状态分组的报销单 ====================

/**
 * 按状态分组的报销单辅助函数
 */
export const getExpensesByStatus = (status: ExpenseStatus): Expense[] => {
  return mockExpenses.filter(expense => expense.status === status)
}

/**
 * 按类型分组的报销单辅助函数
 */
export const getExpensesByType = (type: ExpenseType): Expense[] => {
  return mockExpenses.filter(expense => expense.type === type)
}

/**
 * 获取待审批报销单
 */
export const getPendingExpenses = (): Expense[] => {
  return mockExpenses.filter(expense =>
    expense.status === 'dept_pending' || expense.status === 'finance_pending'
  )
}

/**
 * 获取待打款报销单
 */
export const getPaymentPendingExpenses = (): Expense[] => {
  return mockExpenses.filter(expense =>
    expense.status === 'finance_pending' &&
    expense.departmentApproval?.status === 'approved'
  )
}

// ==================== 统计数据 ====================

/**
 * 报销单统计数据
 */
export const getExpenseStats = () => {
  const totalAmount = mockExpenses.reduce((sum, expense) => sum + expense.amount, 0)
  const paidAmount = mockExpenses
    .filter(e => e.status === 'paid')
    .reduce((sum, expense) => sum + expense.amount, 0)
  const pendingAmount = mockExpenses
    .filter(e => ['dept_pending', 'finance_pending'].includes(e.status))
    .reduce((sum, expense) => sum + expense.amount, 0)

  return {
    totalCount: mockExpenses.length,
    totalAmount,
    paidCount: mockExpenses.filter(e => e.status === 'paid').length,
    paidAmount,
    pendingCount: mockExpenses.filter(e =>
      ['dept_pending', 'finance_pending'].includes(e.status)
    ).length,
    pendingAmount,
    draftCount: mockExpenses.filter(e => e.status === 'draft').length,
    rejectedCount: mockExpenses.filter(e => e.status === 'rejected').length
  }
}

// ==================== 导出所有数据 ====================

export default {
  mockExpenses,
  mockPayments,
  getExpensesByStatus,
  getExpensesByType,
  getPendingExpenses,
  getPaymentPendingExpenses,
  getExpenseStats
}
