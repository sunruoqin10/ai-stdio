/**
 * 请假管理模块类型定义
 * @module leave/types
 */

/**
 * 请假类型
 */
export type LeaveType = 'annual' | 'sick' | 'personal' | 'comp_time' | 'marriage' | 'maternity'

/**
 * 请假状态
 */
export type LeaveStatus = 'draft' | 'pending' | 'approving' | 'approved' | 'rejected' | 'cancelled'

/**
 * 审批状态
 */
export type ApprovalStatus = 'pending' | 'approved' | 'rejected'

/**
 * 时间单位
 */
export type TimeUnit = 'day' | 'morning' | 'afternoon'

/**
 * 请假申请
 */
export interface LeaveRequest {
  id: string                        // 编号: LEAVE+YYYYMMDD+序号
  applicantId: string               // 申请人ID
  applicantName: string             // 申请人姓名
  departmentId: string              // 部门ID
  departmentName: string            // 部门名称
  type: LeaveType                   // 请假类型
  startTime: string                 // 开始时间 ISO 8601
  endTime: string                   // 结束时间 ISO 8601
  duration: number                  // 请假时长(天),支持0.5
  reason: string                    // 请假事由
  attachments?: string[]            // 附件URL数组
  status: LeaveStatus               // 状态
  currentApprovalLevel: number      // 当前审批层级(0/1/2/3)
  totalApprovalLevels?: number      // 总审批层级
  approvers?: ApprovalRecord[]      // 审批记录(查询时包含)
  version: number                   // 版本号(乐观锁)
  createdAt: string                 // 创建时间
  updatedAt: string                 // 更新时间
}

/**
 * 请假申请详情（包含更多信息）
 */
export interface LeaveDetail extends LeaveRequest {
  applicantPosition?: string         // 申请人职位
  applicantPhone?: string            // 申请人电话
  applicantEmail?: string            // 申请人邮箱
  applicantAvatar?: string           // 申请人头像
  managerName?: string               // 直属主管姓名
  approvals?: ApprovalRecord[]      // 审批记录
}

/**
 * 请假申请表单
 */
export interface LeaveForm {
  type: LeaveType
  startTime: string
  endTime: string
  duration?: number
  reason: string
  attachments?: string[]
  version?: number                  // 版本号(编辑时需要)
}

/**
 * 审批记录
 */
export interface ApprovalRecord {
  id?: number                       // 记录ID
  requestId: string                 // 申请ID
  approverId: string                // 审批人ID
  approverName: string              // 审批人姓名
  approvalLevel: number             // 审批层级(1/2/3)
  status: ApprovalStatus            // 审批状态
  opinion?: string                  // 审批意见
  timestamp?: string                // 审批时间
}

/**
 * 审批表单
 */
export interface ApprovalForm {
  status: 'approved' | 'rejected'
  opinion?: string
}

/**
 * 年假余额
 */
export interface LeaveBalance {
  id?: number
  employeeId: string
  employeeName: string
  year: number
  annualTotal: number               // 年假总额(天)
  annualUsed: number                // 已使用(天)
  annualRemaining: number           // 剩余(天)
  createdAt?: string
  updatedAt?: string
}

/**
 * 节假日
 */
export interface Holiday {
  id?: number
  date: string                      // YYYY-MM-DD
  name: string                      // 节假日名称
  type: 'national' | 'company'
  year: number
}

/**
 * 请假统计
 */
export interface LeaveStatistics {
  total: number                     // 总请假次数
  totalDuration: number             // 总请假天数
  byType: Record<LeaveType, number> // 按类型统计
  byStatus: Record<LeaveStatus, number> // 按状态统计
  monthlyData: {                    // 月度数据
    month: string
    count: number
    duration: number
  }[]
  departmentData: {                 // 部门数据
    department: string
    count: number
    duration: number
  }[]
}

/**
 * 请假筛选条件
 */
export interface LeaveFilter {
  type?: LeaveType
  status?: LeaveStatus
  departmentId?: string
  applicantId?: string
  startDate?: string
  endDate?: string
  keyword?: string
}

/**
 * 分页响应
 */
export interface PaginationResponse<T> {
  list: T[]
  total: number
  page: number
  pageSize: number
}
