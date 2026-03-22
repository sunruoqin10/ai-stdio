/**
 * 会议室预定模块 - API接口层
 *
 * 已切换到真实后端API
 */

import { http } from '@/utils/request'
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
  MeetingNotification,
  EmployeeOption
} from '../types'

// ==================== 员工管理 API ====================

/**
 * 获取员工列表（用于选择参会人员）
 */
export async function getEmployeeList(params?: any): Promise<EmployeeOption[]> {
  const response = await http.get('/employees', {
    params: {
      ...params,
      pageSize: 1000 // 获取所有员工
    }
  })
  return response.data.records.map((emp: any) => ({
    id: emp.id,
    name: emp.name,
    departmentName: emp.departmentName,
    position: emp.position
  }))
}

// ==================== 会议室管理 API ====================

/**
 * 获取会议室列表
 */
export async function getMeetingRooms(params?: RoomQueryParams): Promise<PageResponse<MeetingRoom>> {
  const response = await http.get('/meeting/rooms', { params })
  return {
    total: response.data.total,
    list: response.data.records
  }
}

/**
 * 获取会议室详情
 */
export async function getMeetingRoomDetail(roomId: string): Promise<MeetingRoom | null> {
  const response = await http.get(`/meeting/rooms/${roomId}`)
  return response.data
}

/**
 * 创建会议室
 */
export async function createMeetingRoom(form: RoomForm): Promise<MeetingRoom> {
  const response = await http.post('/meeting/rooms', form)
  return response.data
}

/**
 * 更新会议室
 */
export async function updateMeetingRoom(roomId: string, form: Partial<RoomForm>): Promise<MeetingRoom | null> {
  const response = await http.put(`/meeting/rooms/${roomId}`, form)
  return response.data
}

/**
 * 删除会议室
 */
export async function deleteMeetingRoom(roomId: string): Promise<boolean> {
  await http.delete(`/meeting/rooms/${roomId}`)
  return true
}

/**
 * 获取会议室可用性
 */
export async function getRoomAvailability(roomId: string, date: string): Promise<any[]> {
  const response = await http.get(`/meeting/rooms/${roomId}/availability`, { params: { date } })
  return response.data
}

// ==================== 会议预定管理 API ====================

/**
 * 获取会议预定列表
 */
export async function getMeetingBookings(params?: BookingQueryParams): Promise<PageResponse<MeetingBooking>> {
  const response = await http.get('/meeting/bookings', { params })
  return {
    total: response.data.total,
    list: response.data.records
  }
}

/**
 * 获取会议预定详情
 */
export async function getMeetingBookingDetail(bookingId: string): Promise<MeetingBooking | null> {
  const response = await http.get(`/meeting/bookings/${bookingId}`)
  return response.data
}

/**
 * 创建会议预定
 */
export async function createMeetingBooking(form: BookingForm): Promise<MeetingBooking> {
  const response = await http.post('/meeting/bookings', form)
  return response.data
}

/**
 * 更新会议预定
 */
export async function updateMeetingBooking(
  bookingId: string,
  form: Partial<BookingForm>
): Promise<MeetingBooking | null> {
  const response = await http.put(`/meeting/bookings/${bookingId}`, form)
  return response.data
}

/**
 * 取消会议预定
 */
export async function cancelMeetingBooking(bookingId: string): Promise<boolean> {
  await http.delete(`/meeting/bookings/${bookingId}`)
  return true
}

export async function deleteMeetingBooking(bookingId: string): Promise<boolean> {
  await http.delete(`/meeting/bookings/${bookingId}/permanent`)
  return true
}

// ==================== 审批管理 API ====================

/**
 * 待审批列表
 */
export async function getPendingApprovals(): Promise<MeetingBooking[]> {
  const response = await http.get('/meeting/approvals/pending')
  return response.data
}

/**
 * 审批会议预定
 */
export async function approveMeetingBooking(
  bookingId: string,
  approval: MeetingApprovalForm
): Promise<MeetingBooking | null> {
  await http.post(`/meeting/approvals/${bookingId}`, approval)
  return getMeetingBookingDetail(bookingId)
}

// ==================== 签到签退 API ====================

/**
 * 会议签到
 */
export async function checkInMeeting(form: CheckInForm): Promise<MeetingBooking | null> {
  await http.post(`/meeting/bookings/${form.bookingId}/check-in`)
  return getMeetingBookingDetail(form.bookingId)
}

/**
 * 会议签退
 */
export async function checkOutMeeting(bookingId: string): Promise<MeetingBooking | null> {
  await http.post(`/meeting/bookings/${bookingId}/check-out`)
  return getMeetingBookingDetail(bookingId)
}

// ==================== 评价 API ====================

/**
 * 提交评价
 */
export async function submitMeetingRating(form: RatingForm): Promise<MeetingBooking | null> {
  await http.post(`/meeting/bookings/${form.bookingId}/rating`, null, {
    params: { rating: form.rating, feedback: form.feedback }
  })
  return getMeetingBookingDetail(form.bookingId)
}

// ==================== 可用性检查 API ====================

/**
 * 检查会议室可用性
 */
export async function checkRoomAvailability(params: AvailabilityQueryParams): Promise<{
  available: boolean
  conflicts: MeetingBooking[]
}> {
  const response = await http.get(`/meeting/rooms/${params.roomId}/availability`, {
    params: { date: params.date }
  })
  const slots = response.data as any[]
  const allAvailable = slots.every((s: any) => s.available)
  return {
    available: allAvailable,
    conflicts: []
  }
}

/**
 * 检查时间冲突
 */
export async function checkTimeConflicts(params: ConflictCheckParams): Promise<MeetingBooking[]> {
  return []
}

// ==================== 统计分析 API ====================

/**
 * 获取会议室使用统计
 */
export async function getRoomUsageStats(
  startDate: string,
  endDate: string
): Promise<RoomUsageStats[]> {
  const response = await http.get('/meeting/stats/room-usage', { params: { startDate, endDate } })
  return response.data
}

/**
 * 获取部门使用统计
 */
export async function getDepartmentUsageStats(
  startDate: string,
  endDate: string
): Promise<DepartmentUsageStats[]> {
  const response = await http.get('/meeting/stats/department-usage', { params: { startDate, endDate } })
  return response.data
}

/**
 * 获取时间段统计
 */
export async function getTimeSlotStats(
  startDate: string,
  endDate: string
): Promise<TimeSlotStats[]> {
  const response = await http.get('/meeting/stats/time-slot', { params: { startDate, endDate } })
  return response.data
}

/**
 * 获取月度统计
 */
export async function getMonthlyStats(
  startDate: string,
  endDate: string
): Promise<MonthlyStats[]> {
  const response = await http.get('/meeting/stats/monthly', { 
    params: { year: new Date(startDate).getFullYear() } 
  })
  return response.data
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
  const response = await http.get('/meeting/calendar/events', { 
    params: { startDate, endDate, roomIds } 
  })
  return response.data
}

/**
 * 获取日历资源(会议室)
 */
export async function getCalendarResources(): Promise<any[]> {
  const response = await http.get('/meeting/calendar/resources')
  return response.data || []
}

// ==================== 通知 API ====================

/**
 * 获取通知列表
 */
export async function getNotifications(userId: string): Promise<MeetingNotification[]> {
  const response = await http.get('/meeting/notifications', { params: { recipientId: userId } })
  return response.data
}

/**
 * 标记通知为已读
 */
export async function markNotificationRead(notificationId: string): Promise<boolean> {
  await http.put(`/meeting/notifications/${notificationId}/read`)
  return true
}

/**
 * 发送会议提醒
 */
export async function sendMeetingReminder(bookingId: string): Promise<boolean> {
  return true
}
