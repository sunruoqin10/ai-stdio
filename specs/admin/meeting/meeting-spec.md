# 会议室预定模块开发规范

> **模块类型**: 行政协同
> **复杂度**: ⭐⭐⭐⭐ (4星)
> **预计工期**: 1-1.5天 (AI辅助)
> **创建日期**: 2026-01-09

---

## 1. 功能概述

实现会议室预定管理,支持日历视图、时间轴视图、冲突检测、会议提醒等功能。

## 2. 核心功能

### 2.1 视图展示
- **日历视图**: FullCalendar/el-calendar展示所有会议
- **时间轴视图**: 按会议室分栏,时间横向展示
- **列表视图**: 表格展示,支持筛选排序

### 2.2 预定功能
- 选择会议室(A/B/C/培训室)
- 选择开始/结束时间(精确到分钟)
- 填写会议主题
- 选择参会人员(多选)
- 预计参会人数
- 会议议程描述
- 需要的设备(投影仪/白板/视频会议/饮水机)

### 2.3 冲突检测算法

```typescript
interface Booking {
  id: string
  roomId: string
  startTime: string
  endTime: string
}

/**
 * 检测时间冲突
 * @param existingBookings 现有预定
 * @param newBooking 新预定
 */
function checkConflict(
  existingBookings: Booking[],
  newBooking: Pick<Booking, 'roomId' | 'startTime' | 'endTime'>
): boolean {
  // 筛选同一会议室的预定
  const sameRoomBookings = existingBookings.filter(
    b => b.roomId === newBooking.roomId
  )

  // 检查时间重叠
  // 重叠条件: (StartA < EndB) && (StartB < EndA)
  const newStart = new Date(newBooking.startTime)
  const newEnd = new Date(newBooking.endTime)

  return sameRoomBookings.some(booking => {
    const existStart = new Date(booking.startTime)
    const existEnd = new Date(booking.endTime)

    return (
      existStart < newEnd &&
      newStart < existEnd
    )
  })
}

/**
 * 查找可用时间段
 * @param roomId 会议室ID
 * @param date 日期
 * @param duration 会议时长(分钟)
 */
function findAvailableSlots(
  roomId: string,
  date: string,
  duration: number
): { start: string; end: string }[] {
  const bookings = getBookingsByRoomAndDate(roomId, date)
  const slots: { start: string; end: string }[] = []

  // 工作时间: 8:00 - 18:00
  const workStart = new Date(`${date}T08:00:00`)
  const workEnd = new Date(`${date}T18:00:00`)

  let currentTime = new Date(workStart)

  bookings.forEach(booking => {
    const bookingStart = new Date(booking.startTime)
    const bookingEnd = new Date(booking.endTime)

    // 计算当前时间段到会议开始的时间差
    const gap = (bookingStart.getTime() - currentTime.getTime()) / (1000 * 60)

    // 如果时间差>=所需时长,这是一个可用时段
    if (gap >= duration) {
      slots.push({
        start: formatTime(currentTime),
        end: formatTime(bookingStart)
      })
    }

    currentTime = new Date(bookingEnd)
  })

  // 检查最后一个会议之后的时间
  const remainingGap = (workEnd.getTime() - currentTime.getTime()) / (1000 * 60)
  if (remainingGap >= duration) {
    slots.push({
      start: formatTime(currentTime),
      end: formatTime(workEnd)
    })
  }

  return slots
}
```

### 2.4 自动化功能

```typescript
// 会议开始前15分钟提醒
async function checkMeetingReminders(): Promise<void> {
  const now = new Date()
  const in15Minutes = new Date(now.getTime() + 15 * 60 * 1000)

  const upcomingMeetings = await getBookingsInTimeRange(now, in15Minutes)

  for (const meeting of upcomingMeetings) {
    // 发送提醒给参会人员
    for (const participant of meeting.participants) {
      await sendNotification({
        to: participant.id,
        type: 'meeting_reminder',
        title: '会议提醒',
        message: `您预定的会议"${meeting.title}"将在15分钟后开始`,
        data: {
          meetingId: meeting.id,
          roomId: meeting.roomId,
          startTime: meeting.startTime
        }
      })
    }
  }
}

// 会议结束后自动更新状态
async function updateMeetingStatus(): Promise<void> {
  const now = new Date()
  const activeMeetings = await getActiveMeetings()

  for (const meeting of activeMeetings) {
    if (new Date(meeting.endTime) < now) {
      await updateBooking(meeting.id, {
        status: 'completed'
      })
    }
  }
}

// 定时任务(每5分钟执行一次)
setInterval(() => {
  checkMeetingReminders()
  updateMeetingStatus()
}, 5 * 60 * 1000)
```

## 3. 数据结构

```typescript
interface MeetingBooking {
  id: string                        // 预定编号
  title: string                     // 会议主题
  roomId: string                    // 会议室ID
  roomName: string                  // 会议室名称
  startTime: string                 // 开始时间
  endTime: string                   // 结束时间
  bookerId: string                  // 预定人ID
  bookerName: string                // 预定人姓名
  participantCount: number          // 预计人数
  participantIds: string[]          // 参会人员ID列表
  agenda?: string                   // 会议议程
  equipment?: string[]              // 需要的设备: projector/whiteboard/video/water
  status: 'booked' | 'in_progress' | 'completed' | 'cancelled'
  recurringRule?: string            // 循环规则(未来扩展)
  createdAt: string
  updatedAt: string
}

interface MeetingRoom {
  id: string
  name: string                      // 会议室A/B/C/培训室
  capacity: number                  // 容纳人数
  location: string                  // 位置(如: 3楼东侧)
  equipment: string[]               // 设备列表
  imageUrl?: string                 // 会议室图片
  available: boolean                // 是否可用
}

interface BookingFilter {
  roomId?: string
  bookerId?: string
  dateRange?: { start: string; end: string }
  status?: string
  keyword?: string                  // 搜索会议主题
}
```

## 4. 日历视图实现

### 4.1 使用FullCalendar

```vue
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import FullCalendar from '@fullcalendar/vue3'
import dayGridPlugin from '@fullcalendar/daygrid'
import timeGridPlugin from '@fullcalendar/timegrid'
import interactionPlugin from '@fullcalendar/interaction'
import type { EventInput } from '@fullcalendar/common'

const calendarOptions = ref({
  plugins: [dayGridPlugin, timeGridPlugin, interactionPlugin],
  initialView: 'timeGridWeek',
  headerToolbar: {
    left: 'prev,next today',
    center: 'title',
    right: 'dayGridMonth,timeGridWeek,timeGridDay'
  },
  buttonText: {
    today: '今天',
    month: '月',
    week: '周',
    day: '日'
  },
  firstDay: 1,  // 周一开始
  businessHours: {
    daysOfWeek: [1, 2, 3, 4, 5],  // 周一到周五
    startTime: '08:00',
    endTime: '18:00'
  },
  events: [] as EventInput[],
  selectable: true,
  selectMirror: true,
  dayMaxEvents: true,  // 事件过多时显示"更多"
  editable: true,
  eventDrop: handleEventDrop,
  eventResize: handleEventResize,
  select: handleDateSelect,
  eventClick: handleEventClick
})

// 加载会议数据
onMounted(async () => {
  const bookings = await getBookings()
  calendarOptions.value.events = bookings.map(b => ({
    id: b.id,
    title: `${b.title} - ${b.roomName}`,
    start: b.startTime,
    end: b.endTime,
    backgroundColor: getRoomColor(b.roomId),
    borderColor: getRoomColor(b.roomId),
    extendedProps: {
      roomId: b.roomId,
      bookerName: b.bookerName
    }
  }))
})

// 选择时间段创建预定
async function handleDateSelect(selectInfo: any) {
  const startTime = selectInfo.start
  const endTime = selectInfo.end

  // 打开预定对话框
  openBookingDialog({
    startTime: formatDateTime(startTime),
    endTime: formatDateTime(endTime)
  })
}

// 拖拽调整时间
async function handleEventDrop(eventInfo: any) {
  const bookingId = eventInfo.event.id
  const newStart = eventInfo.event.start
  const newEnd = eventInfo.event.end

  try {
    // 检查冲突
    const booking = await getBooking(bookingId)
    const allBookings = await getBookings()

    const hasConflict = checkConflict(
      allBookings.filter(b => b.id !== bookingId),
      {
        roomId: booking.roomId,
        startTime: formatDateTime(newStart),
        endTime: formatDateTime(newEnd)
      }
    )

    if (hasConflict) {
      eventInfo.revert()
      ElMessage.error('该时间段已被预定')
      return
    }

    // 更新预定
    await updateBooking(bookingId, {
      startTime: formatDateTime(newStart),
      endTime: formatDateTime(newEnd)
    })

    ElMessage.success('预定时间已更新')
  } catch (error) {
    eventInfo.revert()
    ElMessage.error('更新失败')
  }
}

// 调整会议时长
async function handleEventResize(eventInfo: any) {
  // 类似handleEventDrop
}
</script>

<template>
  <FullCalendar :options="calendarOptions" />
</template>
```

### 4.2 时间轴视图

```vue
<template>
  <div class="timeline-view">
    <!-- X轴: 时间 (8:00-18:00) -->
    <div class="timeline-header">
      <div class="room-header"></div>
      <div
        v-for="hour in hours"
        :key="hour"
        class="time-slot"
        :style="{ left: calculateLeft(hour) + 'px' }"
      >
        {{ hour }}:00
      </div>
    </div>

    <!-- Y轴: 会议室 -->
    <div
      v-for="room in rooms"
      :key="room.id"
      class="timeline-row"
    >
      <div class="room-label">{{ room.name }}</div>
      <div class="timeline-content">
        <!-- 会议块 -->
        <div
          v-for="booking in getRoomBookings(room.id)"
          :key="booking.id"
          class="booking-block"
          :style="{
            left: calculateLeft(booking.startTime) + 'px',
            width: calculateWidth(booking.startTime, booking.endTime) + 'px',
            backgroundColor: getBookingColor(booking.status)
          }"
          @click="viewBooking(booking)"
        >
          <div class="booking-title">{{ booking.title }}</div>
          <div class="booking-time">
            {{ formatTime(booking.startTime) }} - {{ formatTime(booking.endTime) }}
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
const hours = [8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18]

function calculateLeft(time: string): number {
  const hour = parseInt(time.split(':')[0])
  const minute = parseInt(time.split(':')[1])
  return ((hour - 8) * 60 + minute) * PIXEL_PER_MINUTE
}

function calculateWidth(startTime: string, endTime: string): number {
  const start = new Date(startTime)
  const end = new Date(endTime)
  const minutes = (end.getTime() - start.getTime()) / (1000 * 60)
  return minutes * PIXEL_PER_MINUTE
}

const PIXEL_PER_MINUTE = 2  // 1分钟 = 2像素
</script>
```

## 5. 预定规则

```typescript
// 预定规则配置
const BOOKING_RULES = {
  // 最小预定时长: 15分钟
  MIN_DURATION: 15 * 60 * 1000,

  // 最大预定时长: 4小时
  MAX_DURATION: 4 * 60 * 60 * 1000,

  // 最提前预定: 不限制
  MIN_ADVANCE: 0,

  // 最远预定: 30天
  MAX_ADVANCE: 30 * 24 * 60 * 60 * 1000,

  // 工作时间: 8:00-18:00
  WORK_HOURS: {
    start: { hour: 8, minute: 0 },
    end: { hour: 18, minute: 0 }
  }
}

// 验证预定
function validateBooking(startTime: string, endTime: string): void {
  const start = new Date(startTime)
  const end = new Date(endTime)
  const now = new Date()

  // 1. 不能预定过去时间
  if (end < now) {
    throw new Error('不能预定过去的时间')
  }

  // 2. 检查时长
  const duration = end.getTime() - start.getTime()
  if (duration < BOOKING_RULES.MIN_DURATION) {
    throw new Error(`会议时长不能少于${BOOKING_RULES.MIN_DURATION / 60000}分钟`)
  }
  if (duration > BOOKING_RULES.MAX_DURATION) {
    throw new Error(`会议时长不能超过${BOOKING_RULES.MAX_DURATION / 3600000}小时`)
  }

  // 3. 检查提前量
  const advance = start.getTime() - now.getTime()
  if (advance > BOOKING_RULES.MAX_ADVANCE) {
    throw new Error(`只能预定${BOOKING_RULES.MAX_ADVANCE / (24 * 3600000)}天内的会议`)
  }

  // 4. 检查工作时间
  const startHour = start.getHours()
  const endHour = end.getHours()
  if (startHour < BOOKING_RULES.WORK_HOURS.start.hour ||
      endHour > BOOKING_RULES.WORK_HOURS.end.hour) {
    throw new Error(`预定时间必须在${BOOKING_RULES.WORK_HOURS.start.hour}:00-${BOOKING_RULES.WORK_HOURS.end.hour}:00之间`)
  }
}
```

## 6. 循环会议(未来扩展)

```typescript
interface RecurringRule {
  frequency: 'daily' | 'weekly' | 'monthly'  // 频率
  interval: number                          // 间隔(每周/每月几次)
  until?: string                            // 结束日期
  count?: number                            // 重复次数
  days?: number[]                           // 星期几(0-6)
}

// 生成循环会议实例
function generateRecurringInstances(
  baseBooking: MeetingBooking,
  rule: RecurringRule
): MeetingBooking[] {
  const instances: MeetingBooking[] = []
  const startDate = new Date(baseBooking.startTime)
  const endDate = new Date(baseBooking.endTime)

  let count = 0
  const maxCount = rule.count || 10  // 默认最多10次
  const until = rule.until ? new Date(rule.until) : null

  while (count < maxCount) {
    // 检查是否超过结束日期
    if (until && startDate > until) break

    // 生成实例
    instances.push({
      ...baseBooking,
      id: `${baseBooking.id}-${count}`,
      startTime: formatDateTime(startDate),
      endTime: formatDateTime(endDate)
    })

    // 计算下一次
    switch (rule.frequency) {
      case 'daily':
        startDate.setDate(startDate.getDate() + rule.interval)
        endDate.setDate(endDate.getDate() + rule.interval)
        break
      case 'weekly':
        startDate.setDate(startDate.getDate() + (7 * rule.interval))
        endDate.setDate(endDate.getDate() + (7 * rule.interval))
        break
      case 'monthly':
        startDate.setMonth(startDate.getMonth() + rule.interval)
        endDate.setMonth(endDate.getMonth() + rule.interval)
        break
    }

    count++
  }

  return instances
}
```

---

**文档版本**: v1.0.0
