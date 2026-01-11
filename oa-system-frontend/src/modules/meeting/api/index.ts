/**
 * 会议室预定模块 - API接口层
 *
 * 基于 meeting_Technical.md 规范实现
 */

import type {
  MeetingRoom,
  MeetingBooking,
  BookingForm,
  MeetingApprovalForm,
  CheckInForm,
  RatingForm,
  RoomForm,
  BookingQueryParams,
  RoomQueryParams,
  AvailabilityQueryParams,
  ConflictCheckParams,
  PageResponse,
  RoomUsageStats,
  DepartmentUsageStats,
  TimeSlotStats,
  MonthlyStats,
  CalendarEvent,
  MeetingNotification
} from '../types'
import { mockData } from '../mock/data'
import { generateBookingId, generateRoomId } from '../utils'

// ==================== 延迟模拟工具 ====================

function delay(ms: number = 300): Promise<void> {
  return new Promise(resolve => setTimeout(resolve, ms))
}

// ==================== 会议室管理 API ====================

/**
 * 获取会议室列表
 */
export async function getMeetingRooms(params?: RoomQueryParams): Promise<PageResponse<MeetingRoom>> {
  await delay()

  let rooms = [...mockData.rooms]

  // 筛选
  if (params?.capacity) {
    rooms = rooms.filter(r => r.capacity >= params.capacity!)
  }
  if (params?.floor) {
    rooms = rooms.filter(r => r.floor === params.floor)
  }
  if (params?.status) {
    rooms = rooms.filter(r => r.status === params.status)
  }
  if (params?.equipmentTypes && params.equipmentTypes.length > 0) {
    rooms = rooms.filter(r =>
      params.equipmentTypes!.some(type =>
        r.equipments.some(eq => eq.type === type && eq.available)
      )
    )
  }

  return {
    total: rooms.length,
    list: rooms
  }
}

/**
 * 获取会议室详情
 */
export async function getMeetingRoomDetail(roomId: string): Promise<MeetingRoom | null> {
  await delay()
  return mockData.rooms.find(r => r.id === roomId) || null
}

/**
 * 创建会议室
 */
export async function createMeetingRoom(form: RoomForm): Promise<MeetingRoom> {
  await delay()

  const newRoom: MeetingRoom = {
    id: generateRoomId(),
    name: form.name,
    location: form.location,
    capacity: form.capacity,
    floor: form.floor,
    area: form.area,
    equipments: form.equipments.map(eqId => ({
      id: eqId,
      name: '',
      type: 'other',
      quantity: 1,
      available: true
    })),
    status: 'available',
    description: form.description,
    hourlyRate: form.hourlyRate,
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString()
  }

  mockData.rooms.push(newRoom)
  return newRoom
}

/**
 * 更新会议室
 */
export async function updateMeetingRoom(roomId: string, form: Partial<RoomForm>): Promise<MeetingRoom | null> {
  await delay()

  const index = mockData.rooms.findIndex(r => r.id === roomId)
  if (index === -1) return null

  mockData.rooms[index] = {
    ...mockData.rooms[index],
    ...form,
    updatedAt: new Date().toISOString()
  }

  return mockData.rooms[index]
}

/**
 * 删除会议室
 */
export async function deleteMeetingRoom(roomId: string): Promise<boolean> {
  await delay()

  const index = mockData.rooms.findIndex(r => r.id === roomId)
  if (index === -1) return false

  mockData.rooms.splice(index, 1)
  return true
}

// ==================== 会议预定管理 API ====================

/**
 * 获取会议预定列表
 */
export async function getMeetingBookings(params?: BookingQueryParams): Promise<PageResponse<MeetingBooking>> {
  await delay()

  let bookings = [...mockData.bookings]

  // 筛选
  if (params?.roomId) {
    bookings = bookings.filter(b => b.roomId === params.roomId)
  }
  if (params?.status) {
    bookings = bookings.filter(b => b.status === params.status)
  }
  if (params?.organizerId) {
    bookings = bookings.filter(b => b.organizerId === params.organizerId)
  }
  if (params?.departmentId) {
    bookings = bookings.filter(b => b.departmentId === params.departmentId)
  }
  if (params?.startDate || params?.endDate) {
    bookings = bookings.filter(b => {
      const bookingDate = new Date(b.startTime)
      if (params?.startDate && bookingDate < new Date(params.startDate)) return false
      if (params?.endDate && bookingDate > new Date(params.endDate)) return false
      return true
    })
  }
  if (params?.keyword) {
    const keyword = params.keyword.toLowerCase()
    bookings = bookings.filter(b =>
      b.title.toLowerCase().includes(keyword) ||
      b.organizerName.toLowerCase().includes(keyword) ||
      b.agenda?.toLowerCase().includes(keyword)
    )
  }

  // 排序
  bookings.sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())

  // 分页
  const page = params?.page || 1
  const size = params?.size || 10
  const start = (page - 1) * size
  const list = bookings.slice(start, start + size)

  return {
    total: bookings.length,
    list
  }
}

/**
 * 获取会议预定详情
 */
export async function getMeetingBookingDetail(bookingId: string): Promise<MeetingBooking | null> {
  await delay()
  return mockData.bookings.find(b => b.id === bookingId) || null
}

/**
 * 创建会议预定
 */
export async function createMeetingBooking(form: BookingForm): Promise<MeetingBooking> {
  await delay()

  // 构建开始和结束时间
  const startDateTime = new Date(`${form.date} ${form.startTime}`)
  const endDateTime = new Date(`${form.date} ${form.endTime}`)
  const duration = Math.round((endDateTime.getTime() - startDateTime.getTime()) / 60000)

  const room = mockData.rooms.find(r => r.id === form.roomId)

  const newBooking: MeetingBooking = {
    id: generateBookingId(),
    title: form.title,
    organizerId: 'CURRENT_USER', // TODO: 从用户store获取
    organizerName: '当前用户',
    organizerPhone: form.organizerPhone,
    departmentId: 'CURRENT_DEPT',
    departmentName: '当前部门',
    roomId: form.roomId,
    roomName: room?.name || '',
    startTime: startDateTime.toISOString(),
    endTime: endDateTime.toISOString(),
    duration,
    attendees: form.attendeeIds.map(userId => ({
      userId,
      userName: '用户', // TODO: 从用户服务获取
      required: true,
      status: 'pending' as const
    })),
    agenda: form.agenda,
    level: form.level,
    isPrivate: form.isPrivate,
    reminder: form.reminder,
    status: 'pending',
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString(),
    createdBy: 'CURRENT_USER'
  }

  // 如果有重复规则,添加到预定数据中
  if (form.recurrence) {
    newBooking.recurrence = form.recurrence
  }

  mockData.bookings.push(newBooking)
  return newBooking
}

/**
 * 更新会议预定
 */
export async function updateMeetingBooking(
  bookingId: string,
  form: Partial<BookingForm>
): Promise<MeetingBooking | null> {
  await delay()

  const index = mockData.bookings.findIndex(b => b.id === bookingId)
  if (index === -1) return null

  const booking = mockData.bookings[index]
  if (booking.status !== 'pending') {
    throw new Error('只有待审批的预定才能修改')
  }

  // 更新字段
  if (form.title) booking.title = form.title
  if (form.organizerPhone) booking.organizerPhone = form.organizerPhone
  if (form.agenda !== undefined) booking.agenda = form.agenda
  if (form.level) booking.level = form.level
  if (form.isPrivate !== undefined) booking.isPrivate = form.isPrivate
  if (form.reminder) booking.reminder = form.reminder

  if (form.date && form.startTime && form.endTime) {
    const startDateTime = new Date(`${form.date} ${form.startTime}`)
    const endDateTime = new Date(`${form.date} ${form.endTime}`)
    booking.startTime = startDateTime.toISOString()
    booking.endTime = endDateTime.toISOString()
    booking.duration = Math.round((endDateTime.getTime() - startDateTime.getTime()) / 60000)
  }

  booking.updatedAt = new Date().toISOString()

  return booking
}

/**
 * 取消会议预定
 */
export async function cancelMeetingBooking(bookingId: string): Promise<boolean> {
  await delay()

  const booking = mockData.bookings.find(b => b.id === bookingId)
  if (!booking) return false

  if (booking.status !== 'pending' && booking.status !== 'approved') {
    throw new Error('只有待审批或已通过的预定才能取消')
  }

  booking.status = 'cancelled'
  booking.updatedAt = new Date().toISOString()
  return true
}

// ==================== 审批管理 API ====================

/**
 * 待审批列表
 */
export async function getPendingApprovals(): Promise<MeetingBooking[]> {
  await delay()
  return mockData.bookings.filter(b => b.status === 'pending')
}

/**
 * 审批会议预定
 */
export async function approveMeetingBooking(
  bookingId: string,
  approval: MeetingApprovalForm
): Promise<MeetingBooking | null> {
  await delay()

  const booking = mockData.bookings.find(b => b.id === bookingId)
  if (!booking) return null

  if (booking.status !== 'pending') {
    throw new Error('只有待审批的预定才能审批')
  }

  booking.status = approval.status
  booking.approval = {
    approverId: 'ADMIN',
    approverName: '管理员',
    status: approval.status,
    opinion: approval.opinion,
    timestamp: new Date().toISOString()
  }

  if (approval.status === 'rejected') {
    booking.rejectionReason = approval.opinion
  }

  booking.updatedAt = new Date().toISOString()

  return booking
}

// ==================== 签到签退 API ====================

/**
 * 会议签到
 */
export async function checkInMeeting(form: CheckInForm): Promise<MeetingBooking | null> {
  await delay()

  const booking = mockData.bookings.find(b => b.id === form.bookingId)
  if (!booking) return null

  if (booking.status !== 'approved') {
    throw new Error('只有已通过的预定才能签到')
  }

  if (booking.actualStartTime) {
    throw new Error('已经签到过了')
  }

  booking.actualStartTime = form.actualStartTime
  booking.checkInUser = 'CURRENT_USER'
  booking.updatedAt = new Date().toISOString()

  return booking
}

/**
 * 会议签退
 */
export async function checkOutMeeting(bookingId: string): Promise<MeetingBooking | null> {
  await delay()

  const booking = mockData.bookings.find(b => b.id === bookingId)
  if (!booking) return null

  if (!booking.actualStartTime) {
    throw new Error('请先签到')
  }

  if (booking.actualEndTime) {
    throw new Error('已经签退过了')
  }

  booking.actualEndTime = new Date().toISOString()
  booking.checkOutUser = 'CURRENT_USER'
  booking.updatedAt = new Date().toISOString()

  return booking
}

// ==================== 评价 API ====================

/**
 * 提交评价
 */
export async function submitMeetingRating(form: RatingForm): Promise<MeetingBooking | null> {
  await delay()

  const booking = mockData.bookings.find(b => b.id === form.bookingId)
  if (!booking) return null

  if (!booking.actualEndTime) {
    throw new Error('会议结束后才能评价')
  }

  booking.rating = form.rating
  booking.feedback = form.feedback
  booking.updatedAt = new Date().toISOString()

  return booking
}

// ==================== 可用性检查 API ====================

/**
 * 检查会议室可用性
 */
export async function checkRoomAvailability(params: AvailabilityQueryParams): Promise<{
  available: boolean
  conflicts: MeetingBooking[]
}> {
  await delay()

  const bookings = mockData.bookings.filter(b => {
    if (b.roomId !== params.roomId) return false
    if (b.status === 'cancelled' || b.status === 'rejected') return false

    const bookingDate = new Date(b.startTime).toDateString()
    const queryDate = new Date(params.date).toDateString()
    if (bookingDate !== queryDate) return false

    return true
  })

  const conflicts: MeetingBooking[] = []

  if (params.startTime && params.endTime) {
    const queryStart = new Date(`${params.date} ${params.startTime}`)
    const queryEnd = new Date(`${params.date} ${params.endTime}`)

    bookings.forEach(booking => {
      const bookingStart = new Date(booking.startTime)
      const bookingEnd = new Date(booking.endTime)

      if (queryStart < bookingEnd && queryEnd > bookingStart) {
        conflicts.push(booking)
      }
    })
  }

  return {
    available: conflicts.length === 0,
    conflicts
  }
}

/**
 * 检查时间冲突
 */
export async function checkTimeConflicts(params: ConflictCheckParams): Promise<MeetingBooking[]> {
  await delay()

  const conflicts: MeetingBooking[] = []

  mockData.bookings.forEach(booking => {
    if (booking.roomId !== params.roomId) return
    if (booking.status === 'cancelled' || booking.status === 'rejected') return
    if (params.excludeBookingId && booking.id === params.excludeBookingId) return

    const bookingStart = new Date(booking.startTime)
    const bookingEnd = new Date(booking.endTime)
    const queryStart = new Date(params.startTime)
    const queryEnd = new Date(params.endTime)

    if (queryStart < bookingEnd && queryEnd > bookingStart) {
      conflicts.push(booking)
    }
  })

  return conflicts
}

// ==================== 统计分析 API ====================

/**
 * 获取会议室使用统计
 */
export async function getRoomUsageStats(
  startDate: string,
  endDate: string
): Promise<RoomUsageStats[]> {
  await delay()
  return mockData.stats.roomUsage
}

/**
 * 获取部门使用统计
 */
export async function getDepartmentUsageStats(
  startDate: string,
  endDate: string
): Promise<DepartmentUsageStats[]> {
  await delay()
  return mockData.stats.departmentUsage
}

/**
 * 获取时间段统计
 */
export async function getTimeSlotStats(
  startDate: string,
  endDate: string
): Promise<TimeSlotStats[]> {
  await delay()
  return mockData.stats.timeSlot
}

/**
 * 获取月度统计
 */
export async function getMonthlyStats(
  startDate: string,
  endDate: string
): Promise<MonthlyStats[]> {
  await delay()
  return mockData.stats.monthly
}

// ==================== 日历 API ====================

/**
 * 获取日历事件
 */
export async function getCalendarEvents(
  startDate: string,
  endDate: string,
  roomIds?: string[]
): Promise<CalendarEvent[]> {
  await delay()

  const events: CalendarEvent[] = []

  mockData.bookings.forEach(booking => {
    if (booking.status === 'cancelled' || booking.status === 'rejected') return

    const bookingDate = new Date(booking.startTime)
    const start = new Date(startDate)
    const end = new Date(endDate)

    if (bookingDate < start || bookingDate > end) return

    if (roomIds && roomIds.length > 0 && !roomIds.includes(booking.roomId)) return

    events.push({
      id: booking.id,
      title: booking.title,
      start: booking.startTime,
      end: booking.endTime,
      resourceId: booking.roomId,
      extendedProps: {
        status: booking.status,
        organizer: booking.organizerName,
        department: booking.departmentName,
        level: booking.level,
        attendeeCount: booking.attendees.length
      }
    })
  })

  return events
}

/**
 * 获取日历资源(会议室)
 */
export async function getCalendarResources(): Promise<any[]> {
  await delay()

  return mockData.rooms.map(room => ({
    id: room.id,
    title: room.name,
    capacity: room.capacity,
    location: room.location
  }))
}

// ==================== 通知 API ====================

/**
 * 获取通知列表
 */
export async function getNotifications(userId: string): Promise<MeetingNotification[]> {
  await delay()
  return mockData.notifications.filter(n => n.recipientId === userId)
}

/**
 * 标记通知为已读
 */
export async function markNotificationRead(notificationId: string): Promise<boolean> {
  await delay()

  const notification = mockData.notifications.find(n => n.id === notificationId)
  if (!notification) return false

  notification.isRead = true
  return true
}

/**
 * 发送会议提醒
 */
export async function sendMeetingReminder(bookingId: string): Promise<boolean> {
  await delay()
  // TODO: 实现发送提醒逻辑
  return true
}
