/**
 * 请假管理模块工具函数
 * @module leave/utils
 */

import type { LeaveType, LeaveStatus, Holiday } from '../types'

/**
 * 格式化日期
 */
export function formatDate(date: string | Date): string {
  const d = typeof date === 'string' ? new Date(date) : date
  return d.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  })
}

/**
 * 格式化日期时间
 */
export function formatDateTime(date: string | Date): string {
  const d = typeof date === 'string' ? new Date(date) : date
  return d.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

/**
 * 获取请假类型名称
 */
export function getLeaveTypeName(type: LeaveType): string {
  const typeMap: Record<LeaveType, string> = {
    annual: '年假',
    sick: '病假',
    personal: '事假',
    comp_time: '调休',
    marriage: '婚假',
    maternity: '产假'
  }
  return typeMap[type] || type
}

/**
 * 获取请假状态名称
 */
export function getLeaveStatusName(status: LeaveStatus): string {
  const statusMap: Record<LeaveStatus, string> = {
    draft: '待提交',
    pending: '待审批',
    approving: '审批中',
    approved: '已通过',
    rejected: '已驳回',
    cancelled: '已撤销'
  }
  return statusMap[status] || status
}

/**
 * 获取请假类型标签类型(Element Plus tag type)
 */
export function getTypeTagType(type: LeaveType): string {
  const typeMap: Record<LeaveType, string> = {
    annual: 'success',
    sick: 'warning',
    personal: 'info',
    comp_time: 'primary',
    marriage: 'danger',
    maternity: 'danger'
  }
  return typeMap[type] || 'info'
}

/**
 * 获取请假状态类型(Element Plus tag type)
 */
export function getLeaveStatusType(status: LeaveStatus): string {
  const typeMap: Record<LeaveStatus, string> = {
    draft: 'info',
    pending: 'warning',
    approving: 'primary',
    approved: 'success',
    rejected: 'danger',
    cancelled: 'info'
  }
  return typeMap[status] || 'info'
}

/**
 * 判断是否为工作日
 */
export function isWorkday(date: Date, holidays: Holiday[]): boolean {
  const day = date.getDay()
  // 0=周日, 6=周六
  if (day === 0 || day === 6) {
    return false
  }

  // 检查是否为节假日
  const dateStr = date.toISOString().split('T')[0]
  return !holidays.some(h => h.date === dateStr)
}

/**
 * 计算工作日天数
 */
export function calculateWorkDays(startDate: string, endDate: string, holidays: Holiday[]): number {
  const start = new Date(startDate)
  const end = new Date(endDate)

  let count = 0
  let current = new Date(start)

  while (current <= end) {
    if (isWorkday(current, holidays)) {
      count++
    }
    current.setDate(current.getDate() + 1)
  }

  return count
}

/**
 * 根据工作时长计算请假天数
 */
export function calculateDuration(
  startTime: string,
  endTime: string,
  holidays: Holiday[]
): number {
  const start = new Date(startTime)
  const end = new Date(endTime)

  // 如果是同一天
  if (start.toDateString() === end.toDateString()) {
    // 如果是全天(跨越多日的计算会进入else)
    const hours = (end.getTime() - start.getTime()) / (1000 * 60 * 60)
    if (hours >= 8) {
      return 1
    } else if (hours >= 4) {
      return 0.5
    }
  }

  // 计算工作日
  return calculateWorkDays(startTime, endTime, holidays)
}

/**
 * 生成请假申请ID
 */
export function generateLeaveId(date: Date, serialNumber: number): string {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const seq = String(serialNumber).padStart(3, '0')
  return `LEAVE${year}${month}${day}${seq}`
}

/**
 * 判断状态是否可以撤销
 */
export function canCancel(status: LeaveStatus): boolean {
  return status === 'pending'
}

/**
 * 判断状态是否可以编辑
 */
export function canEdit(status: LeaveStatus): boolean {
  return status === 'draft'
}

/**
 * 判断状态是否可以删除
 */
export function canDelete(status: LeaveStatus): boolean {
  return status === 'draft'
}

/**
 * 判断状态是否可以重新提交
 */
export function canResubmit(status: LeaveStatus): boolean {
  return status === 'rejected' || status === 'cancelled'
}

/**
 * 计算年假额度
 */
export function calculateAnnualQuota(workYears: number): number {
  if (workYears < 1) return 0
  if (workYears < 10) return 5
  if (workYears < 20) return 10
  return 15
}

/**
 * 计算工作年限
 */
export function calculateWorkYears(joinDate: string | Date): number {
  const join = typeof joinDate === 'string' ? new Date(joinDate) : joinDate
  const now = new Date()

  let years = now.getFullYear() - join.getFullYear()
  const monthDiff = now.getMonth() - join.getMonth()

  if (monthDiff < 0 || (monthDiff === 0 && now.getDate() < join.getDate())) {
    years--
  }

  return Math.max(0, years)
}

/**
 * 获取年假余额警告类型
 */
export function getBalanceWarningType(balance: number): 'success' | 'warning' | 'error' {
  if (balance === 0) return 'error'
  if (balance <= 3) return 'warning'
  return 'success'
}

/**
 * 获取年假余额警告消息
 */
export function getBalanceWarningMessage(balance: number): string {
  if (balance === 0) return '年假余额已用完'
  if (balance <= 3) return `年假余额仅剩${balance}天,请合理安排`
  return `年假余额${balance}天`
}

/**
 * 格式化请假时长
 */
export function formatDuration(duration: number): string {
  if (Number.isInteger(duration)) {
    return `${duration}天`
  }
  return `${duration}天`
}

/**
 * 判断是否需要附件
 */
export function needsAttachment(type: LeaveType): boolean {
  const needAttachmentTypes: LeaveType[] = ['sick', 'marriage', 'maternity']
  return needAttachmentTypes.includes(type)
}

/**
 * 判断是否扣减年假
 */
export function deductsAnnualLeave(type: LeaveType): boolean {
  return type === 'annual'
}

/**
 * 获取审批层级数
 */
export function getApprovalLevels(duration: number): number {
  if (duration <= 3) return 1
  if (duration <= 7) return 2
  return 3
}

/**
 * 获取审批层级名称
 */
export function getApprovalLevelName(level: number): string {
  const levelNames: Record<number, string> = {
    1: '部门负责人',
    2: '人事专员',
    3: '总经理'
  }
  return levelNames[level] || ''
}

/**
 * 判断是否为当前审批人
 */
export function isCurrentApprover(
  approvalLevel: number,
  currentLevel: number,
  approvers: any[],
  currentUserId: string
): boolean {
  if (approvalLevel !== currentLevel) return false

  const currentApprover = approvers.find(a => a.approvalLevel === currentLevel)
  return currentApprover?.approverId === currentUserId
}

/**
 * 计算进度百分比
 */
export function calculateProgress(currentLevel: number, totalLevels: number): number {
  return Math.round((currentLevel / totalLevels) * 100)
}

/**
 * 获取完整的图片URL
 */
export function getImageUrl(url: string): string {
  if (!url) return ''
  if (url.startsWith('http://') || url.startsWith('https://')) {
    return url
  }
  if (url.startsWith('/uploads/')) {
    return `/api${url}`
  }
  if (url.startsWith('blob:')) {
    return url
  }
  return `/api/uploads/${url}`
}
