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
  ApprovalForm,
  LeaveDetail
} from '../types'
import { http } from '@/utils/request'

interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

interface PageData<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

// ==================== 请假申请接口 ====================

/**
 * 获取我的请假列表
 */
export async function getMyLeaveRequests(params?: {
  status?: string
  type?: string
  startDate?: string
  endDate?: string
  keyword?: string
  page?: number
  pageSize?: number
}): Promise<PaginationResponse<LeaveRequest>> {
  const res = await http.get<ApiResponse<PageData<LeaveRequest>>>(
    '/leave/requests',
    { params }
  )
  
  return {
    list: res.data.records,
    total: res.data.total,
    page: res.data.current,
    pageSize: res.data.size
  }
}

/**
 * 获取请假申请详情
 */
export async function getLeaveRequest(id: string): Promise<LeaveDetail> {
  const res = await http.get<ApiResponse<LeaveDetail>>(`/leave/requests/${id}`)
  return res.data
}

/**
 * 创建请假申请
 */
export async function createLeaveRequest(data: LeaveForm): Promise<LeaveRequest> {
  const res = await http.post<ApiResponse<LeaveRequest>>('/leave/requests', data)
  return res.data
}

/**
 * 更新请假申请
 */
export async function updateLeaveRequest(id: string, data: Partial<LeaveForm>): Promise<LeaveRequest> {
  const res = await http.put<ApiResponse<LeaveRequest>>(`/leave/requests/${id}`, data)
  return res.data
}

/**
 * 删除请假申请
 */
export async function deleteLeaveRequest(id: string): Promise<void> {
  await http.delete(`/leave/requests/${id}`)
}

/**
 * 提交请假申请
 */
export async function submitLeaveRequest(id: string): Promise<void> {
  await http.post(`/leave/requests/${id}/submit`)
}

/**
 * 撤销请假申请
 */
export async function cancelLeaveRequest(id: string): Promise<void> {
  await http.post(`/leave/requests/${id}/cancel`)
}

/**
 * 重新提交请假申请
 */
export async function resubmitLeaveRequest(id: string, data: Partial<LeaveForm>): Promise<void> {
  await http.post(`/leave/requests/${id}/resubmit`, data)
}

// ==================== 审批接口 ====================

/**
 * 获取待审批列表
 */
export async function getPendingApprovals(params?: {
  departmentId?: string
  type?: string
  startDate?: string
  endDate?: string
  keyword?: string
  page?: number
  pageSize?: number
}): Promise<PaginationResponse<LeaveRequest>> {
  const res = await http.get<ApiResponse<PageData<LeaveRequest>>>(
    '/leave/approvals/pending',
    { params }
  )
  
  return {
    list: res.data.records,
    total: res.data.total,
    page: res.data.current,
    pageSize: res.data.size
  }
}

/**
 * 获取已审批列表
 */
export async function getApprovedRequests(params?: {
  approverId?: string
  status?: string
  type?: string
  startDate?: string
  endDate?: string
  keyword?: string
  page?: number
  pageSize?: number
}): Promise<PaginationResponse<LeaveRequest>> {
  const res = await http.get<ApiResponse<PageData<LeaveRequest>>>(
    '/leave/approvals/approved',
    { params }
  )
  
  return {
    list: res.data.records,
    total: res.data.total,
    page: res.data.current,
    pageSize: res.data.size
  }
}

/**
 * 审批请假申请
 */
export async function approveLeaveRequest(
  id: string,
  approval: ApprovalForm
): Promise<LeaveDetail> {
  const res = await http.post<ApiResponse<LeaveDetail>>(
    `/leave/approvals/${id}/approve`,
    approval
  )
  return res.data
}

// ==================== 年假管理接口 ====================

/**
 * 获取年假余额
 */
export async function getLeaveBalance(employeeId: string, year?: number): Promise<LeaveBalance> {
  const res = await http.get<ApiResponse<LeaveBalance>>(
    `/leave/balance/${employeeId}`,
    { params: { year } }
  )
  return res.data
}

/**
 * 获取年假余额列表
 */
export async function getLeaveBalances(params?: {
  year?: number
  departmentId?: string
  keyword?: string
  page?: number
  pageSize?: number
}): Promise<PaginationResponse<LeaveBalance>> {
  const res = await http.get<ApiResponse<PageData<LeaveBalance>>>(
    '/leave/balance',
    { params }
  )
  
  return {
    list: res.data.records,
    total: res.data.total,
    page: res.data.current,
    pageSize: res.data.size
  }
}

/**
 * 更新年假余额
 */
export async function updateLeaveQuota(
  employeeId: string,
  year: number,
  quota: number
): Promise<LeaveBalance> {
  const res = await http.post<ApiResponse<LeaveBalance>>(
    `/leave/balance/${employeeId}`,
    { employeeId, year, annualTotal: quota }
  )
  return res.data
}

/**
 * 初始化年假余额
 */
export async function initLeaveBalance(employeeId: string, year?: number): Promise<LeaveBalance> {
  const res = await http.post<ApiResponse<LeaveBalance>>(
    `/leave/balance/init/${employeeId}`
  )
  return res.data
}

// ==================== 统计接口 ====================

/**
 * 获取员工请假统计
 */
export async function getEmployeeStatistics(
  employeeId: string,
  startDate?: string,
  endDate?: string
): Promise<LeaveStatistics> {
  const res = await http.get<ApiResponse<LeaveStatistics>>(
    `/leave/statistics/employee/${employeeId}`,
    { params: { startDate, endDate } }
  )
  return res.data
}

/**
 * 获取部门请假统计
 */
export async function getDepartmentStatistics(
  departmentId: string,
  startDate?: string,
  endDate?: string
): Promise<LeaveStatistics> {
  const res = await http.get<ApiResponse<LeaveStatistics>>(
    `/leave/statistics/department/${departmentId}`,
    { params: { startDate, endDate } }
  )
  return res.data
}

/**
 * 获取全局请假统计
 */
export async function getGlobalStatistics(
  startDate?: string,
  endDate?: string
): Promise<LeaveStatistics> {
  const res = await http.get<ApiResponse<LeaveStatistics>>(
    '/leave/statistics/global',
    { params: { startDate, endDate } }
  )
  return res.data
}

/**
 * 获取请假统计（兼容旧接口）
 */
export async function getLeaveStatistics(params?: {
  departmentId?: string
  year?: number
  applicantId?: string
}): Promise<LeaveStatistics> {
  if (params?.applicantId) {
    return getEmployeeStatistics(params.applicantId)
  } else if (params?.departmentId) {
    return getDepartmentStatistics(params.departmentId)
  } else {
    return getGlobalStatistics()
  }
}

// ==================== 节假日接口 ====================

/**
 * 获取节假日列表
 */
export async function getHolidays(params?: {
  year?: number
  type?: string
  startDate?: string
  endDate?: string
  keyword?: string
}): Promise<Holiday[]> {
  const res = await http.get<ApiResponse<Holiday[]>>('/leave/holidays', { params })
  return res.data
}

/**
 * 根据日期范围获取节假日
 */
export async function getHolidaysByDateRange(startDate: string, endDate: string): Promise<Holiday[]> {
  const res = await http.get<ApiResponse<Holiday[]>>('/leave/holidays/date-range', {
    params: { startDate, endDate }
  })
  return res.data
}
