/**
 * 会议室预定模块 - TypeScript类型定义
 *
 * 基于 meeting_Technical.md 规范实现
 */

// ==================== 基础类型 ====================

/**
 * 预定状态
 */
export type BookingStatus = 'pending' | 'approved' | 'rejected' | 'cancelled'

/**
 * 会议室状态
 */
export type RoomStatus = 'available' | 'occupied' | 'maintenance'

/**
 * 重复类型
 */
export type RecurrenceType = 'none' | 'daily' | 'weekly' | 'monthly'

/**
 * 提醒时间
 */
export type ReminderTime = 'none' | '15min' | '30min' | '1hour' | '1day'

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
  floor: number
  area: number // 平方米

  // 设备
  equipments: Equipment[]

  // 状态
  status: RoomStatus

  // 图片
  images?: string[]

  // 描述
  description?: string

  // 价格(如果是外部会议室)
  hourlyRate?: number

  // 审计字段
  createdAt: string
  updatedAt: string
}

/**
 * 设备
 */
export interface Equipment {
  id: string
  name: string
  type: EquipmentType
  quantity: number
  available: boolean
}

/**
 * 设备类型
 */
export type EquipmentType = 'projector' | 'screen' | 'whiteboard' | 'audio' | 'video' | 'computer' | 'other'

/**
 * 会议预定实体
 */
export interface MeetingBooking {
  id: string

  // 基本信息
  title: string
  organizerId: string
  organizerName: string
  organizerPhone: string
  departmentId: string
  departmentName: string

  // 会议室
  roomId: string
  roomName: string

  // 时间
  startTime: string
  endTime: string
  duration: number // 分钟

  // 参会人员
  attendees: Attendee[]

  // 会议详情
  agenda?: string
  level: MeetingLevel
  isPrivate: boolean

  // 重复
  recurrence?: Recurrence

  // 提醒
  reminder: ReminderTime

  // 状态
  status: BookingStatus
  approvalRemark?: string
  rejectionReason?: string

  // 审批记录
  approval?: ApprovalRecord

  // 实际使用
  actualStartTime?: string
  actualEndTime?: string
  checkInUser?: string
  checkOutUser?: string

  // 评价
  rating?: number
  feedback?: string

  // 审计字段
  createdAt: string
  updatedAt: string
  createdBy: string
}

/**
 * 参会人员
 */
export interface Attendee {
  userId: string
  userName: string
  department?: string
  email?: string
  phone?: string
  required: boolean // 是否必须参加
  status: 'pending' | 'accepted' | 'declined'
}

/**
 * 重复规则
 */
export interface Recurrence {
  type: RecurrenceType
  interval: number // 间隔
  endDate: string // 结束日期
  occurrences?: number // 重复次数
}

/**
 * 审批记录
 */
export interface ApprovalRecord {
  approverId?: string
  approverName?: string
  status: 'approved' | 'rejected'
  opinion?: string
  timestamp?: string
}

/**
 * 冲突信息
 */
export interface ConflictInfo {
  type: 'time_conflict' | 'overbooking' | 'equipment_unavailable'
  conflictBooking?: MeetingBooking
  message: string
}

// ==================== 表单类型 ====================

/**
 * 预定表单
 */
export interface BookingForm {
  title: string
  roomId: string
  date: string // YYYY-MM-DD
  startTime: string // HH:mm
  endTime: string // HH:mm
  organizerPhone: string
  agenda?: string
  level: MeetingLevel
  isPrivate: boolean
  attendeeIds: string[]
  recurrence?: Recurrence
  reminder: ReminderTime
}

/**
 * 审批表单
 */
export interface MeetingApprovalForm {
  status: 'approved' | 'rejected'
  opinion: string
}

/**
 * 签到表单
 */
export interface CheckInForm {
  bookingId: string
  actualStartTime: string
}

/**
 * 评价表单
 */
export interface RatingForm {
  bookingId: string
  rating: number
  feedback: string
}

/**
 * 会议室表单
 */
export interface RoomForm {
  name: string
  location: string
  capacity: number
  floor: number
  area: number
  equipments: string[] // equipment IDs
  description?: string
  hourlyRate?: number
}

// ==================== 查询参数类型 ====================

/**
 * 预定查询参数
 */
export interface BookingQueryParams {
  page?: number
  size?: number
  roomId?: string
  status?: BookingStatus
  organizerId?: string
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
  equipmentTypes?: EquipmentType[]
  status?: RoomStatus
}

/**
 * 可用性查询参数
 */
export interface AvailabilityQueryParams {
  roomId: string
  date: string // YYYY-MM-DD
  startTime?: string // HH:mm
  endTime?: string // HH:mm
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
  totalBookings: number
  totalHours: number
  utilizationRate: number // 使用率
  avgAttendees: number
}

/**
 * 部门使用统计
 */
export interface DepartmentUsageStats {
  departmentId: string
  departmentName: string
  totalBookings: number
  totalHours: number
  cancellationRate: number
}

/**
 * 时间段统计
 */
export interface TimeSlotStats {
  hour: number // 0-23
  bookingCount: number
  utilizationRate: number
}

/**
 * 月度统计
 */
export interface MonthlyStats {
  month: string // YYYY-MM
  totalBookings: number
  totalHours: number
  uniqueUsers: number
  avgDuration: number
}

// ==================== 日历类型 ====================

/**
 * 日历事件
 */
export interface CalendarEvent {
  id: string
  title: string
  start: string // ISO datetime
  end: string // ISO datetime
  allDay?: boolean
  resourceId?: string // roomId
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
 * 通知类型
 */
export type NotificationType = 'reminder' | 'cancelled' | 'approved' | 'rejected' | 'updated'

/**
 * 会议通知
 */
export interface MeetingNotification {
  id: string
  type: NotificationType
  bookingId: string
  title: string
  message: string
  recipientId: string
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

/**
 * 分页参数
 */
export interface PageParams {
  page: number
  pageSize: number
}
