/**
 * 请假管理模块 API 接口
 * @module leave/api
 */

import type {
  LeaveRequest,
  LeaveForm,
  LeaveBalance,
  Holiday,
  LeaveStatistics,
  LeaveFilter,
  PaginationResponse,
  ApprovalForm
} from '../types'
import { mockLeaveRequests, mockLeaveBalances, mockHolidays } from '../mock/data'

// ==================== 请假申请接口 ====================

/**
 * 获取我的请假列表
 */
export async function getMyLeaveRequests(params?: {
  status?: string
  page?: number
  pageSize?: number
}): Promise<PaginationResponse<LeaveRequest>> {
  // 模拟API调用
  await new Promise(resolve => setTimeout(resolve, 300))

  let filtered = [...mockLeaveRequests]

  // 状态筛选
  if (params?.status) {
    filtered = filtered.filter(req => req.status === params.status)
  }

  const page = params?.page || 1
  const pageSize = params?.pageSize || 10
  const start = (page - 1) * pageSize
  const end = start + pageSize

  return {
    list: filtered.slice(start, end),
    total: filtered.length,
    page,
    pageSize
  }
}

/**
 * 获取请假申请详情
 */
export async function getLeaveRequest(id: string): Promise<LeaveRequest> {
  await new Promise(resolve => setTimeout(resolve, 200))

  const request = mockLeaveRequests.find(req => req.id === id)
  if (!request) {
    throw new Error('请假申请不存在')
  }

  return request
}

/**
 * 创建请假申请
 */
export async function createLeaveRequest(data: LeaveForm): Promise<LeaveRequest> {
  await new Promise(resolve => setTimeout(resolve, 300))

  const now = new Date()
  const newRequest: LeaveRequest = {
    id: `LEAVE${now.getFullYear()}${String(now.getMonth() + 1).padStart(2, '0')}${String(now.getDate()).padStart(2, '0')}${String(Math.floor(Math.random() * 1000)).padStart(3, '0')}`,
    applicantId: 'EMP000001',
    applicantName: '张三',
    departmentId: 'DEPT001',
    departmentName: '技术部',
    type: data.type,
    startTime: data.startTime,
    endTime: data.endTime,
    duration: data.duration || 1,
    reason: data.reason,
    attachments: data.attachments,
    status: 'draft',
    currentApprovalLevel: 0,
    createdAt: now.toISOString(),
    updatedAt: now.toISOString()
  }

  mockLeaveRequests.unshift(newRequest)
  return newRequest
}

/**
 * 更新请假申请
 */
export async function updateLeaveRequest(id: string, data: Partial<LeaveForm>): Promise<LeaveRequest> {
  await new Promise(resolve => setTimeout(resolve, 300))

  const index = mockLeaveRequests.findIndex(req => req.id === id)
  if (index === -1) {
    throw new Error('请假申请不存在')
  }

  const request = mockLeaveRequests[index]

  // 只能编辑草稿状态
  if (request.status !== 'draft') {
    throw new Error('只能编辑草稿状态的申请')
  }

  // 更新字段
  Object.assign(request, data, {
    updatedAt: new Date().toISOString()
  })

  return request
}

/**
 * 删除请假申请
 */
export async function deleteLeaveRequest(id: string): Promise<void> {
  await new Promise(resolve => setTimeout(resolve, 200))

  const index = mockLeaveRequests.findIndex(req => req.id === id)
  if (index === -1) {
    throw new Error('请假申请不存在')
  }

  const request = mockLeaveRequests[index]

  // 只能删除草稿状态
  if (request.status !== 'draft') {
    throw new Error('只能删除草稿状态的申请')
  }

  mockLeaveRequests.splice(index, 1)
}

/**
 * 提交请假申请
 */
export async function submitLeaveRequest(id: string): Promise<LeaveRequest> {
  await new Promise(resolve => setTimeout(resolve, 400))

  const request = mockLeaveRequests.find(req => req.id === id)
  if (!request) {
    throw new Error('请假申请不存在')
  }

  if (request.status !== 'draft') {
    throw new Error('只能提交草稿状态的申请')
  }

  // 检查年假余额
  if (request.type === 'annual') {
    const balance = mockLeaveBalances.find(b => b.employeeId === request.applicantId && b.year === new Date().getFullYear())
    if (balance && balance.annualRemaining < request.duration) {
      throw new Error(`年假余额不足,剩余${balance.annualRemaining}天,需要${request.duration}天`)
    }
  }

  // 更新状态
  request.status = 'pending'
  request.currentApprovalLevel = 1
  request.updatedAt = new Date().toISOString()

  // 添加审批人
  const levels = Math.min(3, Math.ceil(request.duration / 3))
  request.approvers = Array.from({ length: levels }, (_, i) => ({
    requestId: request.id,
    approverId: ['EMP000005', 'EMP000006', 'EMP000008'][i],
    approverName: ['孙经理', '人事小李', '周总'][i],
    approvalLevel: i + 1,
    status: i === 0 ? 'pending' : 'pending'
  }))

  return request
}

/**
 * 撤销请假申请
 */
export async function cancelLeaveRequest(id: string): Promise<LeaveRequest> {
  await new Promise(resolve => setTimeout(resolve, 300))

  const request = mockLeaveRequests.find(req => req.id === id)
  if (!request) {
    throw new Error('请假申请不存在')
  }

  if (request.status !== 'pending') {
    throw new Error('只能撤销待审批状态的申请')
  }

  request.status = 'cancelled'
  request.updatedAt = new Date().toISOString()

  return request
}

// ==================== 审批接口 ====================

/**
 * 获取待审批列表
 */
export async function getPendingApprovals(params?: {
  departmentId?: string
  page?: number
  pageSize?: number
}): Promise<PaginationResponse<LeaveRequest>> {
  await new Promise(resolve => setTimeout(resolve, 300))

  let filtered = mockLeaveRequests.filter(req => req.status === 'pending' || req.status === 'approving')

  // 部门筛选
  if (params?.departmentId) {
    filtered = filtered.filter(req => req.departmentId === params.departmentId)
  }

  const page = params?.page || 1
  const pageSize = params?.pageSize || 10
  const start = (page - 1) * pageSize
  const end = start + pageSize

  return {
    list: filtered.slice(start, end),
    total: filtered.length,
    page,
    pageSize
  }
}

/**
 * 获取已审批列表
 */
export async function getApprovedRequests(params?: {
  approverId?: string
  page?: number
  pageSize?: number
}): Promise<PaginationResponse<LeaveRequest>> {
  await new Promise(resolve => setTimeout(resolve, 300))

  let filtered = mockLeaveRequests.filter(req =>
    req.status === 'approved' || req.status === 'rejected'
  )

  const page = params?.page || 1
  const pageSize = params?.pageSize || 10
  const start = (page - 1) * pageSize
  const end = start + pageSize

  return {
    list: filtered.slice(start, end),
    total: filtered.length,
    page,
    pageSize
  }
}

/**
 * 审批请假申请
 */
export async function approveLeaveRequest(
  id: string,
  approval: ApprovalForm
): Promise<LeaveRequest> {
  await new Promise(resolve => setTimeout(resolve, 400))

  const request = mockLeaveRequests.find(req => req.id === id)
  if (!request) {
    throw new Error('请假申请不存在')
  }

  if (request.status !== 'pending' && request.status !== 'approving') {
    throw new Error('当前状态不允许审批')
  }

  const currentApprover = request.approvers?.find(
    a => a.approvalLevel === request.currentApprovalLevel && a.status === 'pending'
  )

  if (!currentApprover) {
    throw new Error('未找到待审批的记录')
  }

  // 更新审批记录
  currentApprover.status = approval.status
  currentApprover.opinion = approval.opinion
  currentApprover.timestamp = new Date().toISOString()

  // 如果通过
  if (approval.status === 'approved') {
    const totalLevels = request.approvers?.length || 0

    if (request.currentApprovalLevel >= totalLevels) {
      // 所有审批通过
      request.status = 'approved'

      // 如果是年假,扣减余额
      if (request.type === 'annual') {
        const balance = mockLeaveBalances.find(
          b => b.employeeId === request.applicantId && b.year === new Date().getFullYear()
        )
        if (balance) {
          balance.annualUsed += request.duration
          balance.annualRemaining -= request.duration
          balance.updatedAt = new Date().toISOString()
        }
      }
    } else {
      // 进入下一级审批
      request.status = 'approving'
      request.currentApprovalLevel++
    }
  } else {
    // 驳回
    request.status = 'rejected'
  }

  request.updatedAt = new Date().toISOString()

  return request
}

// ==================== 年假管理接口 ====================

/**
 * 获取年假余额
 */
export async function getLeaveBalance(employeeId: string, year?: number): Promise<LeaveBalance> {
  await new Promise(resolve => setTimeout(resolve, 200))

  const currentYear = year || new Date().getFullYear()
  const balance = mockLeaveBalances.find(
    b => b.employeeId === employeeId && b.year === currentYear
  )

  if (!balance) {
    // 如果没有记录,创建默认记录
    const newBalance: LeaveBalance = {
      employeeId,
      employeeName: '张三',
      year: currentYear,
      annualTotal: 5,
      annualUsed: 0,
      annualRemaining: 5,
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString()
    }
    mockLeaveBalances.push(newBalance)
    return newBalance
  }

  return balance
}

/**
 * 获取年假余额列表
 */
export async function getLeaveBalances(year?: number): Promise<LeaveBalance[]> {
  await new Promise(resolve => setTimeout(resolve, 200))

  const currentYear = year || new Date().getFullYear()
  return mockLeaveBalances.filter(b => b.year === currentYear)
}

/**
 * 更新年假额度
 */
export async function updateLeaveQuota(
  employeeId: string,
  year: number,
  quota: number
): Promise<LeaveBalance> {
  await new Promise(resolve => setTimeout(resolve, 300))

  const balance = mockLeaveBalances.find(
    b => b.employeeId === employeeId && b.year === year
  )

  if (!balance) {
    throw new Error('年假记录不存在')
  }

  const used = balance.annualUsed
  balance.annualTotal = quota
  balance.annualRemaining = quota - used
  balance.updatedAt = new Date().toISOString()

  return balance
}

// ==================== 统计接口 ====================

/**
 * 获取请假统计
 */
export async function getLeaveStatistics(params?: {
  departmentId?: string
  year?: number
  applicantId?: string
}): Promise<LeaveStatistics> {
  await new Promise(resolve => setTimeout(resolve, 400))

  let filtered = [...mockLeaveRequests]

  // 部门筛选
  if (params?.departmentId) {
    filtered = filtered.filter(req => req.departmentId === params.departmentId)
  }

  // 申请人筛选
  if (params?.applicantId) {
    filtered = filtered.filter(req => req.applicantId === params.applicantId)
  }

  // 统计
  const byType: Record<string, number> = {
    annual: 0,
    sick: 0,
    personal: 0,
    comp_time: 0,
    marriage: 0,
    maternity: 0
  }

  const byStatus: Record<string, number> = {
    draft: 0,
    pending: 0,
    approving: 0,
    approved: 0,
    rejected: 0,
    cancelled: 0
  }

  let totalDuration = 0

  filtered.forEach(req => {
    byType[req.type]++
    byStatus[req.status]++
    totalDuration += req.duration
  })

  return {
    total: filtered.length,
    totalDuration,
    byType: byType as any,
    byStatus: byStatus as any,
    monthlyData: [],
    departmentData: []
  }
}

// ==================== 节假日接口 ====================

/**
 * 获取节假日列表
 */
export async function getHolidays(year?: number): Promise<Holiday[]> {
  await new Promise(resolve => setTimeout(resolve, 200))

  const currentYear = year || new Date().getFullYear()
  return mockHolidays.filter(h => h.year === currentYear)
}
