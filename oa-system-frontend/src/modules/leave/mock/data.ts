/**
 * 请假管理模块 Mock 数据
 * @module leave/mock
 */

import type { LeaveRequest, LeaveBalance, Holiday, ApprovalRecord } from '../types'

/**
 * Mock 节假日数据
 */
export const mockHolidays: Holiday[] = [
  { id: 1, date: '2026-01-01', name: '元旦', type: 'national', year: 2026 },
  { id: 2, date: '2026-01-27', name: '春节', type: 'national', year: 2026 },
  { id: 3, date: '2026-01-28', name: '春节', type: 'national', year: 2026 },
  { id: 4, date: '2026-01-29', name: '春节', type: 'national', year: 2026 },
  { id: 5, date: '2026-01-30', name: '春节', type: 'national', year: 2026 },
  { id: 6, date: '2026-01-31', name: '春节', type: 'national', year: 2026 },
  { id: 7, date: '2026-02-01', name: '春节', type: 'national', year: 2026 },
  { id: 8, date: '2026-02-02', name: '春节', type: 'national', year: 2026 },
  { id: 9, date: '2026-04-04', name: '清明节', type: 'national', year: 2026 },
  { id: 10, date: '2026-05-01', name: '劳动节', type: 'national', year: 2026 },
  { id: 11, date: '2026-05-02', name: '劳动节', type: 'national', year: 2026 },
  { id: 12, date: '2026-06-09', name: '端午节', type: 'national', year: 2026 }
]

/**
 * Mock 年假余额数据
 */
export const mockLeaveBalances: LeaveBalance[] = [
  {
    id: 1,
    employeeId: 'EMP000001',
    employeeName: '张三',
    year: 2026,
    annualTotal: 5,
    annualUsed: 2,
    annualRemaining: 3,
    createdAt: '2026-01-01T00:00:00.000Z',
    updatedAt: '2026-01-09T00:00:00.000Z'
  },
  {
    id: 2,
    employeeId: 'EMP000002',
    employeeName: '李四',
    year: 2026,
    annualTotal: 10,
    annualUsed: 5,
    annualRemaining: 5,
    createdAt: '2026-01-01T00:00:00.000Z',
    updatedAt: '2026-01-08T00:00:00.000Z'
  },
  {
    id: 3,
    employeeId: 'EMP000003',
    employeeName: '王五',
    year: 2026,
    annualTotal: 5,
    annualUsed: 0,
    annualRemaining: 5,
    createdAt: '2026-01-01T00:00:00.000Z',
    updatedAt: '2026-01-01T00:00:00.000Z'
  },
  {
    id: 4,
    employeeId: 'EMP000004',
    employeeName: '赵六',
    year: 2026,
    annualTotal: 15,
    annualUsed: 8,
    annualRemaining: 7,
    createdAt: '2026-01-01T00:00:00.000Z',
    updatedAt: '2026-01-07T00:00:00.000Z'
  }
]

/**
 * Mock 审批记录数据
 */
export const mockApprovals: ApprovalRecord[] = [
  {
    id: 1,
    requestId: 'LEAVE20260109001',
    approverId: 'EMP000005',
    approverName: '孙经理',
    approvalLevel: 1,
    status: 'approved',
    opinion: '同意',
    timestamp: '2026-01-09T10:30:00.000Z'
  },
  {
    id: 2,
    requestId: 'LEAVE20260109002',
    approverId: 'EMP000005',
    approverName: '孙经理',
    approvalLevel: 1,
    status: 'pending',
    opinion: undefined,
    timestamp: undefined
  },
  {
    id: 3,
    requestId: 'LEAVE20260108001',
    approverId: 'EMP000006',
    approverName: '人事小李',
    approvalLevel: 2,
    status: 'approved',
    opinion: '同意',
    timestamp: '2026-01-08T15:00:00.000Z'
  }
]

/**
 * Mock 请假申请数据
 */
export const mockLeaveRequests: LeaveRequest[] = [
  {
    id: 'LEAVE20260109001',
    applicantId: 'EMP000001',
    applicantName: '张三',
    departmentId: 'DEPT001',
    departmentName: '技术部',
    type: 'annual',
    startTime: '2026-01-15T09:00:00.000Z',
    endTime: '2026-01-17T18:00:00.000Z',
    duration: 3,
    reason: '家中有事,需要请假处理',
    status: 'approved',
    currentApprovalLevel: 1,
    approvers: [
      {
        id: 1,
        requestId: 'LEAVE20260109001',
        approverId: 'EMP000005',
        approverName: '孙经理',
        approvalLevel: 1,
        status: 'approved',
        opinion: '同意',
        timestamp: '2026-01-09T10:30:00.000Z'
      }
    ],
    createdAt: '2026-01-09T09:00:00.000Z',
    updatedAt: '2026-01-09T10:30:00.000Z'
  },
  {
    id: 'LEAVE20260109002',
    applicantId: 'EMP000002',
    applicantName: '李四',
    departmentId: 'DEPT002',
    departmentName: '销售部',
    type: 'annual',
    startTime: '2026-01-20T09:00:00.000Z',
    endTime: '2026-01-24T18:00:00.000Z',
    duration: 5,
    reason: '回家探亲',
    status: 'pending',
    currentApprovalLevel: 1,
    approvers: [
      {
        id: 2,
        requestId: 'LEAVE20260109002',
        approverId: 'EMP000005',
        approverName: '孙经理',
        approvalLevel: 1,
        status: 'pending'
      }
    ],
    createdAt: '2026-01-09T11:00:00.000Z',
    updatedAt: '2026-01-09T11:00:00.000Z'
  },
  {
    id: 'LEAVE20260109003',
    applicantId: 'EMP000001',
    applicantName: '张三',
    departmentId: 'DEPT001',
    departmentName: '技术部',
    type: 'sick',
    startTime: '2026-01-10T09:00:00.000Z',
    endTime: '2026-01-10T18:00:00.000Z',
    duration: 1,
    reason: '身体不适,需要去医院检查',
    status: 'approving',
    currentApprovalLevel: 2,
    approvers: [
      {
        id: 3,
        requestId: 'LEAVE20260109003',
        approverId: 'EMP000005',
        approverName: '孙经理',
        approvalLevel: 1,
        status: 'approved',
        opinion: '同意',
        timestamp: '2026-01-09T14:00:00.000Z'
      },
      {
        id: 4,
        requestId: 'LEAVE20260109003',
        approverId: 'EMP000006',
        approverName: '人事小李',
        approvalLevel: 2,
        status: 'pending'
      }
    ],
    createdAt: '2026-01-09T13:00:00.000Z',
    updatedAt: '2026-01-09T14:00:00.000Z'
  },
  {
    id: 'LEAVE20260108001',
    applicantId: 'EMP000003',
    applicantName: '王五',
    departmentId: 'DEPT001',
    departmentName: '技术部',
    type: 'personal',
    startTime: '2026-01-05T09:00:00.000Z',
    endTime: '2026-01-05T18:00:00.000Z',
    duration: 1,
    reason: '私事请假',
    status: 'rejected',
    currentApprovalLevel: 2,
    approvers: [
      {
        id: 5,
        requestId: 'LEAVE20260108001',
        approverId: 'EMP000005',
        approverName: '孙经理',
        approvalLevel: 1,
        status: 'approved',
        opinion: '同意',
        timestamp: '2026-01-08T10:00:00.000Z'
      },
      {
        id: 6,
        requestId: 'LEAVE20260108001',
        approverId: 'EMP000006',
        approverName: '人事小李',
        approvalLevel: 2,
        status: 'rejected',
        opinion: '请假事由不够详细,请补充说明',
        timestamp: '2026-01-08T15:00:00.000Z'
      }
    ],
    createdAt: '2026-01-08T09:00:00.000Z',
    updatedAt: '2026-01-08T15:00:00.000Z'
  },
  {
    id: 'LEAVE20260107001',
    applicantId: 'EMP000004',
    applicantName: '赵六',
    departmentId: 'DEPT003',
    departmentName: '市场部',
    type: 'annual',
    startTime: '2026-01-25T09:00:00.000Z',
    endTime: '2026-02-02T18:00:00.000Z',
    duration: 7,
    reason: '春节休假',
    status: 'approving',
    currentApprovalLevel: 3,
    approvers: [
      {
        id: 7,
        requestId: 'LEAVE20260107001',
        approverId: 'EMP000007',
        approverName: '钱总监',
        approvalLevel: 1,
        status: 'approved',
        opinion: '同意',
        timestamp: '2026-01-07T10:00:00.000Z'
      },
      {
        id: 8,
        requestId: 'LEAVE20260107001',
        approverId: 'EMP000006',
        approverName: '人事小李',
        approvalLevel: 2,
        status: 'approved',
        opinion: '同意',
        timestamp: '2026-01-07T14:00:00.000Z'
      },
      {
        id: 9,
        requestId: 'LEAVE20260107001',
        approverId: 'EMP000008',
        approverName: '周总',
        approvalLevel: 3,
        status: 'pending'
      }
    ],
    createdAt: '2026-01-07T09:00:00.000Z',
    updatedAt: '2026-01-07T14:00:00.000Z'
  },
  {
    id: 'LEAVE20260106001',
    applicantId: 'EMP000002',
    applicantName: '李四',
    departmentId: 'DEPT002',
    departmentName: '销售部',
    type: 'comp_time',
    startTime: '2026-01-12T09:00:00.000Z',
    endTime: '2026-01-12T18:00:00.000Z',
    duration: 1,
    reason: '调休,上周加班一天',
    status: 'draft',
    currentApprovalLevel: 0,
    createdAt: '2026-01-06T09:00:00.000Z',
    updatedAt: '2026-01-06T09:00:00.000Z'
  }
]
