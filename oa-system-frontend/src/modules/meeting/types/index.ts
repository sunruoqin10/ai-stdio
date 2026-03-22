/**
 * 会议室预定模块 - TypeScript类型定义
 * 
 * 与后端API匹配
 */

// ==================== 基础类型 ====================

/**
 * 员工选项（用于下拉选择）
 */
export interface EmployeeOption {
  id: string
  name: string
  departmentName?: string
  position?: string
}

/**
 * 预定状态
 */
export type BookingStatus = 'pending' | 'approved' | 'rejected' | 'cancelled' | 'checked_in' | 'checked_out' | 'completed'

/**
 * 会议室状态
 */
export type RoomStatus = 'available' | 'occupied' | 'unavailable' | 'disabled'

/**
 * 重复类型
 */
export type RecurrenceType = 'none' | 'daily' | 'weekly' | 'monthly'

/**
 * 提醒时间
 */
export type ReminderTime = 'none' | '5' | '10' | '15' | '30' | '60' | '1440'

/**
 * 会议等级
 */
export type MeetingLevel = 'normal' | 'important' | 'urgent'

// ==================== 核心实体 ====================

/**
 * 会议室实体
 */
export interface MeetingRoom {
  id: string
  name: string
  location: string
  capacity: number
  floor?: number
  area?: number
  equipment: string[]
  description?: string
  images?: string
  status: RoomStatus
  statusName: string
  bookingCount7Days?: number
  createdAt: string
  updatedAt: string
}

/**
 * 会议预定实体
 */
export interface MeetingBooking {
  id: string
  title: string
  roomId: string
  roomName: string
  roomLocation?: string
  startTime: string
  endTime: string
  duration?: number
  bookerId: string
  bookerName: string
  bookerPosition?: string
  departmentId?: string
  departmentName?: string
  participantCount?: number
  attendees?: Attendee[]
  agenda?: string
  equipment?: string[]
  level: MeetingLevel
  levelName?: string
  reminder?: ReminderTime
  status: BookingStatus
  statusName?: string
  recurringRule?: string
  actualStartTime?: string
  actualEndTime?: string
  checkInUser?: string
  checkOutUser?: string
  rating?: number
  feedback?: string
  approverId?: string
  approverName?: string
  approvalOpinion?: string
  approvalTime?: string
  rejectionReason?: string
  version?: number
  createdAt: string
  updatedAt: string
}

/**
 * 参会人员
 */
export interface Attendee {
  userId: string
  userName: string
  departmentName?: string
  required?: boolean
  status?: 'pending' | 'accepted' | 'declined'
  responseTime?: string
}

// ==================== 表单类型 ====================

/**
 * 预定表单
 */
export interface BookingForm {
  title: string
  roomId: string
  startTime: string
  endTime: string
  bookerId?: string
  bookerName?: string
  departmentId?: string
  departmentName?: string
  agenda?: string
  participantCount?: number
  participantIds?: string[]
  equipment?: string[]
  level?: MeetingLevel
  reminder?: ReminderTime
  recurringRule?: string
}

/**
 * 审批表单
 */
export interface MeetingApprovalForm {
  status: 'approved' | 'rejected'
  opinion?: string
}

/**
 * 签到表单
 */
export interface CheckInForm {
  bookingId: string
  actualStartTime?: string
}

/**
 * 评价表单
 */
export interface RatingForm {
  bookingId: string
  rating: number
  feedback?: string
}

/**
 * 会议室表单
 */
export interface RoomForm {
  name: string
  location: string
  capacity: number
  floor?: number
  area?: number
  equipment?: string[]
  description?: string
  images?: string
  status?: RoomStatus
}

// ==================== 查询参数类型 ====================

/**
 * 预定查询参数
 */
export interface BookingQueryParams {
  page?: number
  size?: number
  roomId?: string
  status?: string
  bookerId?: string
  departmentId?: string
  startDate?: string
  endDate?: string
  keyword?: string
}

/**
 * 会议室查询参数
 */
export interface RoomQueryParams {
  page?: number
  size?: number
  capacity?: number
  floor?: number
  status?: string
}

/**
 * 可用性查询参数
 */
export interface AvailabilityQueryParams {
  roomId: string
  date: string
  startTime?: string
  endTime?: string
}

/**
 * 冲突检查参数
 */
export interface ConflictCheckParams {
  roomId: string
  startTime: string
  endTime: string
  excludeBookingId?: string
}

// ==================== 统计类型 ====================

/**
 * 会议室使用统计
 */
export interface RoomUsageStats {
  roomId: string
  roomName: string
  bookingCount: number
  totalHours: number
  utilizationRate?: number
}

/**
 * 部门使用统计
 */
export interface DepartmentUsageStats {
  departmentId: string
  departmentName: string
  bookingCount: number
  totalHours: number
}

/**
 * 时间段统计
 */
export interface TimeSlotStats {
  timeSlot?: string
  morningCount?: number
  afternoonCount?: number
  eveningCount?: number
  hour?: number
  bookingCount?: number
  utilizationRate?: number
}

/**
 * 月度统计
 */
export interface MonthlyStats {
  year: number
  month: number
  bookingCount: number
  totalHours: number
  growthRate?: number
}

// ==================== 日历类型 ====================

/**
 * 日历事件
 */
export interface CalendarEvent {
  id: string
  title: string
  start: string
  end: string
  resourceId?: string
  status?: BookingStatus
  organizer?: string
  department?: string
  level?: MeetingLevel
  attendeeCount?: number
  allDay?: boolean
  backgroundColor?: string
  borderColor?: string
  extendedProps?: {
    status: BookingStatus
    organizer: string
    department: string
    level: MeetingLevel
    attendeeCount: number
  }
}

/**
 * 日历资源(会议室)
 */
export interface CalendarResource {
  id: string
  title: string
  capacity: number
  location: string
}

// ==================== 通知类型 ====================

/**
 * 会议通知
 */
export interface MeetingNotification {
  id: string
  type: string
  title: string
  content?: string
  bookingId?: string
  isRead: boolean
  createdAt: string
}

// ==================== 分页类型 ====================

/**
 * 分页响应
 */
export interface PageResponse<T> {
  total: number
  list: T[]
}
