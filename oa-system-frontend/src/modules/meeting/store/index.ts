/**
 * 会议室预定模块 - Pinia状态管理
 *
 * 基于 meeting_Technical.md 规范实现
 */

import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type {
  MeetingRoom,
  MeetingBooking,
  BookingForm,
  MeetingApprovalForm,
  CheckInForm,
  RatingForm,
  BookingQueryParams,
  RoomQueryParams,
  PageResponse
} from '../types'
import * as meetingApi from '../api'

export const useMeetingStore = defineStore('meeting', () => {
  // ==================== 状态 ====================

  // 会议室列表
  const rooms = ref<MeetingRoom[]>([])
  const roomsLoading = ref(false)
  const roomsTotal = ref(0)

  // 当前选中的会议室
  const currentRoom = ref<MeetingRoom | null>(null)

  // 会议预定列表
  const bookings = ref<MeetingBooking[]>([])
  const bookingsLoading = ref(false)
  const bookingsTotal = ref(0)

  // 当前选中的预定
  const currentBooking = ref<MeetingBooking | null>(null)

  // 待审批列表
  const pendingApprovals = ref<MeetingBooking[]>([])
  const pendingApprovalsLoading = ref(false)

  // 我的预定
  const myBookings = ref<MeetingBooking[]>([])
  const myBookingsLoading = ref(false)

  // 通知列表
  const notifications = ref<any[]>([])
  const unreadCount = ref(0)

  // 统计数据
  const stats = ref<any>(null)
  const statsLoading = ref(false)

  // ==================== 计算属性 ====================

  // 可用会议室数量
  const availableRoomsCount = computed(() => {
    return rooms.value.filter(r => r.status === 'available').length
  })

  // 进行中的会议数量
  const ongoingMeetingsCount = computed(() => {
    const now = new Date()
    return bookings.value.filter(b => {
      return b.status === 'approved' &&
        new Date(b.startTime) <= now &&
        new Date(b.endTime) >= now &&
        !b.actualEndTime
    }).length
  })

  // 今天的会议数量
  const todayMeetingsCount = computed(() => {
    const today = new Date().toDateString()
    return bookings.value.filter(b => {
      return b.status === 'approved' &&
        new Date(b.startTime).toDateString() === today
    }).length
  })

  // 待审批数量
  const pendingCount = computed(() => {
    return pendingApprovals.value.length
  })

  // 我的待审批数量
  const myPendingCount = computed(() => {
    return myBookings.value.filter(b => b.status === 'pending').length
  })

  // 会议室使用率
  const roomUtilizationRate = computed(() => {
    if (rooms.value.length === 0) return 0
    const available = rooms.value.filter(r => r.status === 'available').length
    return Math.round(((rooms.value.length - available) / rooms.value.length) * 100)
  })

  // ==================== 会议室管理方法 ====================

  /**
   * 加载会议室列表
   */
  async function loadRooms(params?: RoomQueryParams) {
    roomsLoading.value = true
    try {
      const response: PageResponse<MeetingRoom> = await meetingApi.getMeetingRooms(params)
      rooms.value = response.list
      roomsTotal.value = response.total
    } catch (error) {
      console.error('加载会议室列表失败:', error)
      throw error
    } finally {
      roomsLoading.value = false
    }
  }

  /**
   * 加载会议室详情
   */
  async function loadRoomDetail(roomId: string) {
    try {
      const room = await meetingApi.getMeetingRoomDetail(roomId)
      currentRoom.value = room
      return room
    } catch (error) {
      console.error('加载会议室详情失败:', error)
      throw error
    }
  }

  /**
   * 创建会议室
   */
  async function createRoom(form: any) {
    try {
      const room = await meetingApi.createMeetingRoom(form)
      rooms.value.push(room)
      return room
    } catch (error) {
      console.error('创建会议室失败:', error)
      throw error
    }
  }

  /**
   * 更新会议室
   */
  async function updateRoom(roomId: string, form: any) {
    try {
      const room = await meetingApi.updateMeetingRoom(roomId, form)
      if (room) {
        const index = rooms.value.findIndex(r => r.id === roomId)
        if (index !== -1) {
          rooms.value[index] = room
        }
        if (currentRoom.value?.id === roomId) {
          currentRoom.value = room
        }
      }
      return room
    } catch (error) {
      console.error('更新会议室失败:', error)
      throw error
    }
  }

  /**
   * 删除会议室
   */
  async function deleteRoom(roomId: string) {
    try {
      const success = await meetingApi.deleteMeetingRoom(roomId)
      if (success) {
        const index = rooms.value.findIndex(r => r.id === roomId)
        if (index !== -1) {
          rooms.value.splice(index, 1)
        }
        if (currentRoom.value?.id === roomId) {
          currentRoom.value = null
        }
      }
      return success
    } catch (error) {
      console.error('删除会议室失败:', error)
      throw error
    }
  }

  // ==================== 会议预定管理方法 ====================

  /**
   * 加载会议预定列表
   */
  async function loadBookings(params?: BookingQueryParams) {
    bookingsLoading.value = true
    try {
      const response = await meetingApi.getMeetingBookings(params)
      bookings.value = response.list
      bookingsTotal.value = response.total
    } catch (error) {
      console.error('加载会议预定列表失败:', error)
      throw error
    } finally {
      bookingsLoading.value = false
    }
  }

  /**
   * 加载我的预定
   */
  async function loadMyBookings(params?: BookingQueryParams) {
    myBookingsLoading.value = true
    try {
      const response = await meetingApi.getMeetingBookings({
        ...params,
        organizerId: 'CURRENT_USER' // TODO: 从用户store获取
      })
      myBookings.value = response.list
    } catch (error) {
      console.error('加载我的预定失败:', error)
      throw error
    } finally {
      myBookingsLoading.value = false
    }
  }

  /**
   * 加载会议预定详情
   */
  async function loadBookingDetail(bookingId: string) {
    try {
      const booking = await meetingApi.getMeetingBookingDetail(bookingId)
      currentBooking.value = booking
      return booking
    } catch (error) {
      console.error('加载会议预定详情失败:', error)
      throw error
    }
  }

  /**
   * 创建会议预定
   */
  async function createBooking(form: BookingForm) {
    try {
      const booking = await meetingApi.createMeetingBooking(form)
      bookings.value.unshift(booking)
      myBookings.value.unshift(booking)
      return booking
    } catch (error) {
      console.error('创建会议预定失败:', error)
      throw error
    }
  }

  /**
   * 更新会议预定
   */
  async function updateBooking(bookingId: string, form: Partial<BookingForm>) {
    try {
      const booking = await meetingApi.updateMeetingBooking(bookingId, form)
      if (booking) {
        const index = bookings.value.findIndex(b => b.id === bookingId)
        if (index !== -1) {
          bookings.value[index] = booking
        }
        const myIndex = myBookings.value.findIndex(b => b.id === bookingId)
        if (myIndex !== -1) {
          myBookings.value[myIndex] = booking
        }
        if (currentBooking.value?.id === bookingId) {
          currentBooking.value = booking
        }
      }
      return booking
    } catch (error) {
      console.error('更新会议预定失败:', error)
      throw error
    }
  }

  /**
   * 取消会议预定
   */
  async function cancelBooking(bookingId: string) {
    try {
      const success = await meetingApi.cancelMeetingBooking(bookingId)
      if (success) {
        const index = bookings.value.findIndex(b => b.id === bookingId)
        if (index !== -1) {
          bookings.value[index].status = 'cancelled'
        }
        const myIndex = myBookings.value.findIndex(b => b.id === bookingId)
        if (myIndex !== -1) {
          myBookings.value[myIndex].status = 'cancelled'
        }
        if (currentBooking.value?.id === bookingId) {
          currentBooking.value.status = 'cancelled'
        }
      }
      return success
    } catch (error) {
      console.error('取消会议预定失败:', error)
      throw error
    }
  }

  // ==================== 审批管理方法 ====================

  /**
   * 加载待审批列表
   */
  async function loadPendingApprovals() {
    pendingApprovalsLoading.value = true
    try {
      const list = await meetingApi.getPendingApprovals()
      pendingApprovals.value = list
    } catch (error) {
      console.error('加载待审批列表失败:', error)
      throw error
    } finally {
      pendingApprovalsLoading.value = false
    }
  }

  /**
   * 审批会议预定
   */
  async function approveBooking(bookingId: string, approval: MeetingApprovalForm) {
    try {
      const booking = await meetingApi.approveMeetingBooking(bookingId, approval)
      if (booking) {
        const index = bookings.value.findIndex(b => b.id === bookingId)
        if (index !== -1) {
          bookings.value[index] = booking
        }
        const pendingIndex = pendingApprovals.value.findIndex(b => b.id === bookingId)
        if (pendingIndex !== -1) {
          pendingApprovals.value.splice(pendingIndex, 1)
        }
        if (currentBooking.value?.id === bookingId) {
          currentBooking.value = booking
        }
      }
      return booking
    } catch (error) {
      console.error('审批会议预定失败:', error)
      throw error
    }
  }

  // ==================== 签到签退方法 ====================

  /**
   * 会议签到
   */
  async function checkIn(form: CheckInForm) {
    try {
      const booking = await meetingApi.checkInMeeting(form)
      if (booking) {
        const index = bookings.value.findIndex(b => b.id === form.bookingId)
        if (index !== -1) {
          bookings.value[index] = booking
        }
        if (currentBooking.value?.id === form.bookingId) {
          currentBooking.value = booking
        }
      }
      return booking
    } catch (error) {
      console.error('会议签到失败:', error)
      throw error
    }
  }

  /**
   * 会议签退
   */
  async function checkOut(bookingId: string) {
    try {
      const booking = await meetingApi.checkOutMeeting(bookingId)
      if (booking) {
        const index = bookings.value.findIndex(b => b.id === bookingId)
        if (index !== -1) {
          bookings.value[index] = booking
        }
        if (currentBooking.value?.id === bookingId) {
          currentBooking.value = booking
        }
      }
      return booking
    } catch (error) {
      console.error('会议签退失败:', error)
      throw error
    }
  }

  // ==================== 评价方法 ====================

  /**
   * 提交评价
   */
  async function submitRating(form: RatingForm) {
    try {
      const booking = await meetingApi.submitMeetingRating(form)
      if (booking) {
        const index = bookings.value.findIndex(b => b.id === form.bookingId)
        if (index !== -1) {
          bookings.value[index] = booking
        }
        if (currentBooking.value?.id === form.bookingId) {
          currentBooking.value = booking
        }
      }
      return booking
    } catch (error) {
      console.error('提交评价失败:', error)
      throw error
    }
  }

  // ==================== 统计分析方法 ====================

  /**
   * 加载统计数据
   */
  async function loadStats(startDate: string, endDate: string) {
    statsLoading.value = true
    try {
      const [roomUsage, deptUsage] = await Promise.all([
        meetingApi.getRoomUsageStats(startDate, endDate),
        meetingApi.getDepartmentUsageStats(startDate, endDate)
      ])
      stats.value = {
        roomUsage,
        deptUsage
      }
    } catch (error) {
      console.error('加载统计数据失败:', error)
      throw error
    } finally {
      statsLoading.value = false
    }
  }

  // ==================== 通知方法 ====================

  /**
   * 加载通知
   */
  async function loadNotifications() {
    try {
      const list = await meetingApi.getNotifications('CURRENT_USER')
      notifications.value = list
      unreadCount.value = list.filter(n => !n.isRead).length
    } catch (error) {
      console.error('加载通知失败:', error)
      throw error
    }
  }

  /**
   * 标记通知为已读
   */
  async function markNotificationRead(notificationId: string) {
    try {
      const success = await meetingApi.markNotificationRead(notificationId)
      if (success) {
        const notification = notifications.value.find(n => n.id === notificationId)
        if (notification) {
          notification.isRead = true
          unreadCount.value = Math.max(0, unreadCount.value - 1)
        }
      }
      return success
    } catch (error) {
      console.error('标记通知已读失败:', error)
      throw error
    }
  }

  // ==================== 工具方法 ====================

  /**
   * 重置状态
   */
  function resetState() {
    rooms.value = []
    roomsTotal.value = 0
    currentRoom.value = null
    bookings.value = []
    bookingsTotal.value = 0
    currentBooking.value = null
    pendingApprovals.value = []
    myBookings.value = []
    notifications.value = []
    unreadCount.value = 0
    stats.value = null
  }

  /**
   * 初始化
   */
  async function initialize() {
    try {
      await Promise.all([
        loadRooms(),
        loadBookings(),
        loadMyBookings(),
        loadPendingApprovals(),
        loadNotifications()
      ])
    } catch (error) {
      console.error('初始化会议室模块失败:', error)
      throw error
    }
  }

  return {
    // 状态
    rooms,
    roomsLoading,
    roomsTotal,
    currentRoom,
    bookings,
    bookingsLoading,
    bookingsTotal,
    currentBooking,
    pendingApprovals,
    pendingApprovalsLoading,
    myBookings,
    myBookingsLoading,
    notifications,
    unreadCount,
    stats,
    statsLoading,

    // 计算属性
    availableRoomsCount,
    ongoingMeetingsCount,
    todayMeetingsCount,
    pendingCount,
    myPendingCount,
    roomUtilizationRate,

    // 会议室方法
    loadRooms,
    loadRoomDetail,
    createRoom,
    updateRoom,
    deleteRoom,

    // 会议预定方法
    loadBookings,
    loadMyBookings,
    loadBookingDetail,
    createBooking,
    updateBooking,
    cancelBooking,

    // 审批方法
    loadPendingApprovals,
    approveBooking,

    // 签到签退方法
    checkIn,
    checkOut,

    // 评价方法
    submitRating,

    // 统计方法
    loadStats,

    // 通知方法
    loadNotifications,
    markNotificationRead,

    // 工具方法
    resetState,
    initialize
  }
})
