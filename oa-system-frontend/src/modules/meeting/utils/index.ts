/**
 * 会议室预定模块 - 工具函数库
 *
 * 基于 meeting_Technical.md 规范实现
 */

import type {
  BookingStatus,
  RoomStatus,
  RecurrenceType,
  ReminderTime,
  MeetingLevel,
  EquipmentType,
  MeetingBooking,
  MeetingRoom
} from '../types'

// ==================== 格式化函数 ====================

/**
 * 格式化日期
 */
export function formatDate(date: string): string {
  if (!date) return ''
  return date.split('T')[0]
}

/**
 * 格式化日期时间
 */
export function formatDateTime(date: string): string {
  if (!date) return ''
  const d = new Date(date)
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hour = String(d.getHours()).padStart(2, '0')
  const minute = String(d.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day} ${hour}:${minute}`
}

/**
 * 格式化时间(HH:mm)
 */
export function formatTime(date: string): string {
  if (!date) return ''
  const d = new Date(date)
  const hour = String(d.getHours()).padStart(2, '0')
  const minute = String(d.getMinutes()).padStart(2, '0')
  return `${hour}:${minute}`
}

/**
 * 格式化时长(分钟 -> 小时分钟)
 */
export function formatDuration(minutes: number): string {
  const hours = Math.floor(minutes / 60)
  const mins = minutes % 60
  if (hours > 0 && mins > 0) {
    return `${hours}小时${mins}分钟`
  } else if (hours > 0) {
    return `${hours}小时`
  } else {
    return `${mins}分钟`
  }
}

// ==================== 类型转换函数 ====================

/**
 * 获取预定状态名称
 */
export function getBookingStatusName(status: BookingStatus): string {
  const statusMap: Record<BookingStatus, string> = {
    pending: '待审批',
    approved: '已通过',
    rejected: '已驳回',
    cancelled: '已取消'
  }
  return statusMap[status] || status
}

/**
 * 获取预定状态标签类型(Element Plus tag type)
 */
export function getBookingStatusType(status: BookingStatus): string {
  const typeMap: Record<BookingStatus, string> = {
    pending: 'warning',
    approved: 'success',
    rejected: 'danger',
    cancelled: 'info'
  }
  return typeMap[status] || 'info'
}

/**
 * 获取会议室状态名称
 */
export function getRoomStatusName(status: RoomStatus): string {
  const statusMap: Record<RoomStatus, string> = {
    available: '空闲',
    occupied: '使用中',
    maintenance: '维护中'
  }
  return statusMap[status] || status
}

/**
 * 获取会议室状态标签类型
 */
export function getRoomStatusType(status: RoomStatus): string {
  const typeMap: Record<RoomStatus, string> = {
    available: 'success',
    occupied: 'danger',
    maintenance: 'info'
  }
  return typeMap[status] || 'info'
}

/**
 * 获取重复类型名称
 */
export function getRecurrenceTypeName(type: RecurrenceType): string {
  const typeMap: Record<RecurrenceType, string> = {
    none: '不重复',
    daily: '每天',
    weekly: '每周',
    monthly: '每月'
  }
  return typeMap[type] || type
}

/**
 * 获取提醒时间名称
 */
export function getReminderTimeName(time: ReminderTime): string {
  const timeMap: Record<ReminderTime, string> = {
    none: '不提醒',
    '15min': '提前15分钟',
    '30min': '提前30分钟',
    '1hour': '提前1小时',
    '1day': '提前1天'
  }
  return timeMap[time] || time
}

/**
 * 获取会议等级名称
 */
export function getMeetingLevelName(level: MeetingLevel): string {
  const levelMap: Record<MeetingLevel, string> = {
    normal: '普通',
    important: '重要',
    urgent: '紧急'
  }
  return levelMap[level] || level
}

/**
 * 获取会议等级标签类型
 */
export function getMeetingLevelType(level: MeetingLevel): string {
  const typeMap: Record<MeetingLevel, string> = {
    normal: 'info',
    important: 'warning',
    urgent: 'danger'
  }
  return typeMap[level] || 'info'
}

/**
 * 获取设备类型名称
 */
export function getEquipmentTypeName(type: EquipmentType): string {
  const typeMap: Record<EquipmentType, string> = {
    projector: '投影仪',
    screen: '投影幕',
    whiteboard: '白板',
    audio: '音响设备',
    video: '视频设备',
    computer: '计算机',
    other: '其他'
  }
  return typeMap[type] || type
}

// ==================== 状态判断函数 ====================

/**
 * 是否可编辑
 */
export function canEdit(status: BookingStatus): boolean {
  return status === 'pending'
}

/**
 * 是否可取消
 */
export function canCancel(status: BookingStatus): boolean {
  return status === 'pending' || status === 'approved'
}

/**
 * 是否可审批
 */
export function canApprove(status: BookingStatus): boolean {
  return status === 'pending'
}

/**
 * 是否可签到
 */
export function canCheckIn(booking: MeetingBooking): boolean {
  if (booking.status !== 'approved') return false
  const now = new Date()
  const startTime = new Date(booking.startTime)
  const endTime = new Date(booking.endTime)
  return now >= startTime && now <= endTime && !booking.actualStartTime
}

/**
 * 是否可签退
 */
export function canCheckOut(booking: MeetingBooking): boolean {
  return !!booking.actualStartTime && !booking.actualEndTime
}

/**
 * 是否可评价
 */
export function canRate(booking: MeetingBooking): boolean {
  return booking.status === 'approved' && !!booking.actualEndTime && !booking.rating
}

// ==================== 时间计算函数 ====================

/**
 * 计算会议时长(分钟)
 */
export function calculateDuration(startTime: string, endTime: string): number {
  const start = new Date(startTime)
  const end = new Date(endTime)
  return Math.round((end.getTime() - start.getTime()) / 60000)
}

/**
 * 判断是否在工作时间
 */
export function isWorkingTime(date: Date): boolean {
  const hour = date.getHours()
  const day = date.getDay()
  // 工作日 9:00-18:00
  if (day === 0 || day === 6) return false
  return hour >= 9 && hour < 18
}

/**
 * 计算提醒时间
 */
export function calculateReminderTime(startTime: string, reminder: ReminderTime): Date | null {
  if (reminder === 'none') return null

  const start = new Date(startTime)
  const reminderMap: Record<ReminderTime, number> = {
    '15min': 15 * 60 * 1000,
    '30min': 30 * 60 * 1000,
    '1hour': 60 * 60 * 1000,
    '1day': 24 * 60 * 60 * 1000,
    'none': 0
  }

  return new Date(start.getTime() - reminderMap[reminder])
}

/**
 * 生成重复会议日期
 */
export function generateRecurrenceDates(
  startDate: string,
  recurrenceType: RecurrenceType,
  interval: number,
  endDate: string
): string[] {
  const dates: string[] = []
  const current = new Date(startDate)
  const end = new Date(endDate)

  while (current <= end) {
    dates.push(current.toISOString())
    switch (recurrenceType) {
      case 'daily':
        current.setDate(current.getDate() + interval)
        break
      case 'weekly':
        current.setDate(current.getDate() + 7 * interval)
        break
      case 'monthly':
        current.setMonth(current.getMonth() + interval)
        break
    }
  }

  return dates
}

// ==================== 冲突检测函数 ====================

/**
 * 检查时间冲突
 */
export function hasTimeConflict(
  booking1: { startTime: string; endTime: string },
  booking2: { startTime: string; endTime: string }
): boolean {
  const start1 = new Date(booking1.startTime)
  const end1 = new Date(booking1.endTime)
  const start2 = new Date(booking2.startTime)
  const end2 = new Date(booking2.endTime)

  return start1 < end2 && end1 > start2
}

/**
 * 获取冲突的预定
 */
export function getConflictingBookings(
  roomId: string,
  startTime: string,
  endTime: string,
  existingBookings: MeetingBooking[],
  excludeBookingId?: string
): MeetingBooking[] {
  return existingBookings.filter(booking => {
    if (booking.roomId !== roomId) return false
    if (booking.status === 'cancelled' || booking.status === 'rejected') return false
    if (excludeBookingId && booking.id === excludeBookingId) return false
    return hasTimeConflict(
      { startTime, endTime },
      { startTime: booking.startTime, endTime: booking.endTime }
    )
  })
}

/**
 * 检查会议室可用性
 */
export function isRoomAvailable(
  room: MeetingRoom,
  bookings: MeetingBooking[],
  startTime: string,
  endTime: string
): boolean {
  if (room.status !== 'available') return false
  const conflicts = getConflictingBookings(room.id, startTime, endTime, bookings)
  return conflicts.length === 0
}

// ==================== 工作日计算函数 ====================

/**
 * 计算工作日(排除周末和节假日)
 */
export function calculateWorkingDays(startDate: string, endDate: string): number {
  const start = new Date(startDate)
  const end = new Date(endDate)
  let count = 0
  let current = new Date(start)

  while (current <= end) {
    const day = current.getDay()
    if (day !== 0 && day !== 6) {
      count++
    }
    current.setDate(current.getDate() + 1)
  }

  return count
}

/**
 * 是否为工作日
 */
export function isWorkingDay(date: Date): boolean {
  const day = date.getDay()
  return day !== 0 && day !== 6
}

// ==================== 验证函数 ====================

/**
 * 验证时间格式
 */
export function validateTimeFormat(time: string): boolean {
  const regex = /^([01]\d|2[0-3]):([0-5]\d)$/
  return regex.test(time)
}

/**
 * 验证时间范围(开始时间必须早于结束时间)
 */
export function validateTimeRange(startTime: string, endTime: string): boolean {
  return new Date(startTime) < new Date(endTime)
}

/**
 * 验证会议时长(不超过8小时)
 */
export function validateDuration(startTime: string, endTime: string): boolean {
  const duration = calculateDuration(startTime, endTime)
  return duration > 0 && duration <= 8 * 60
}

/**
 * 验证手机号格式
 */
export function validatePhoneNumber(phone: string): boolean {
  const regex = /^1[3-9]\d{9}$/
  return regex.test(phone)
}

// ==================== 筛选函数 ====================

/**
 * 按状态筛选
 */
export function filterByStatus(bookings: MeetingBooking[], status: BookingStatus): MeetingBooking[] {
  return bookings.filter(b => b.status === status)
}

/**
 * 按会议室筛选
 */
export function filterByRoom(bookings: MeetingBooking[], roomId: string): MeetingBooking[] {
  return bookings.filter(b => b.roomId === roomId)
}

/**
 * 按部门筛选
 */
export function filterByDepartment(bookings: MeetingBooking[], departmentId: string): MeetingBooking[] {
  return bookings.filter(b => b.departmentId === departmentId)
}

/**
 * 按日期范围筛选
 */
export function filterByDateRange(
  bookings: MeetingBooking[],
  startDate: string,
  endDate: string
): MeetingBooking[] {
  return bookings.filter(b => {
    const bookingDate = new Date(b.startTime)
    return bookingDate >= new Date(startDate) && bookingDate <= new Date(endDate)
  })
}

/**
 * 按关键词筛选
 */
export function filterByKeyword(bookings: MeetingBooking[], keyword: string): MeetingBooking[] {
  const lowerKeyword = keyword.toLowerCase()
  return bookings.filter(b =>
    b.title.toLowerCase().includes(lowerKeyword) ||
    b.organizerName.toLowerCase().includes(lowerKeyword) ||
    b.agenda?.toLowerCase().includes(lowerKeyword)
  )
}

// ==================== 排序函数 ====================

/**
 * 按开始时间排序
 */
export function sortByStartTime(bookings: MeetingBooking[], order: 'asc' | 'desc' = 'asc'): MeetingBooking[] {
  return [...bookings].sort((a, b) => {
    const timeA = new Date(a.startTime).getTime()
    const timeB = new Date(b.startTime).getTime()
    return order === 'asc' ? timeA - timeB : timeB - timeA
  })
}

/**
 * 按创建时间排序
 */
export function sortByCreatedAt(bookings: MeetingBooking[], order: 'asc' | 'desc' = 'desc'): MeetingBooking[] {
  return [...bookings].sort((a, b) => {
    const timeA = new Date(a.createdAt).getTime()
    const timeB = new Date(b.createdAt).getTime()
    return order === 'asc' ? timeA - timeB : timeB - timeA
  })
}

// ==================== 统计函数 ====================

/**
 * 计算会议室使用率
 */
export function calculateUtilizationRate(bookings: MeetingBooking[], roomCapacity: number): number {
  if (bookings.length === 0) return 0

  let totalMinutes = 0
  bookings.forEach(booking => {
    totalMinutes += booking.duration
  })

  // 假设工作时间为每天9小时,每月22个工作日
  const workingMinutesPerMonth = 9 * 60 * 22
  return Math.min((totalMinutes / workingMinutesPerMonth) * 100, 100)
}

/**
 * 计算平均参会人数
 */
export function calculateAvgAttendees(bookings: MeetingBooking[]): number {
  if (bookings.length === 0) return 0

  const totalAttendees = bookings.reduce((sum, b) => sum + b.attendees.length, 0)
  return Math.round(totalAttendees / bookings.length)
}

/**
 * 计算取消率
 */
export function calculateCancellationRate(bookings: MeetingBooking[]): number {
  if (bookings.length === 0) return 0

  const cancelled = bookings.filter(b => b.status === 'cancelled').length
  return Math.round((cancelled / bookings.length) * 100)
}

// ==================== 工具提示函数 ====================

/**
 * 获取状态提示信息
 */
export function getStatusTip(status: BookingStatus): string {
  const tipMap: Record<BookingStatus, string> = {
    pending: '等待管理员审批,审批通过后方可使用会议室',
    approved: '审批通过,请按时参加会议',
    rejected: '审批未通过,请查看驳回原因后重新申请',
    cancelled: '已取消的会议室预定'
  }
  return tipMap[status] || ''
}

/**
 * 获取会议等级提示
 */
export function getLevelTip(level: MeetingLevel): string {
  const tipMap: Record<MeetingLevel, string> = {
    normal: '普通会议',
    important: '重要会议,需要提前做好准备',
    urgent: '紧急会议,需要优先处理'
  }
  return tipMap[level] || ''
}

/**
 * 获取设备提示
 */
export function getEquipmentTip(equipments: any[]): string {
  if (!equipments || equipments.length === 0) return '无特殊设备'

  const names = equipments.map(e => e.name).join('、')
  return `配备设备: ${names}`
}

// ==================== 预定ID生成 ====================

/**
 * 生成预定ID MB+YYYYMMDD+4位随机数
 */
export function generateBookingId(): string {
  const now = new Date()
  const dateStr = now.toISOString().slice(0, 10).replace(/-/g, '')
  const random = Math.floor(Math.random() * 10000).toString().padStart(4, '0')
  return `MB${dateStr}${random}`
}

/**
 * 生成会议室ID MR+3位随机数
 */
export function generateRoomId(): string {
  const random = Math.floor(Math.random() * 1000).toString().padStart(3, '0')
  return `MR${random}`
}

// ==================== 颜色工具 ====================

/**
 * 获取状态颜色
 */
export function getStatusColor(status: BookingStatus): string {
  const colorMap: Record<BookingStatus, string> = {
    pending: '#E6A23C',
    approved: '#67C23A',
    rejected: '#F56C6C',
    cancelled: '#909399'
  }
  return colorMap[status] || '#409EFF'
}

/**
 * 获取等级颜色
 */
export function getLevelColor(level: MeetingLevel): string {
  const colorMap: Record<MeetingLevel, string> = {
    normal: '#409EFF',
    important: '#E6A23C',
    urgent: '#F56C6C'
  }
  return colorMap[level] || '#409EFF'
}
