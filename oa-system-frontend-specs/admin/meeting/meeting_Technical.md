# 会议室预定模块 - 技术实现规范

> **模块类型**: 行政协同
> **文档类型**: 技术实现规范
> **创建日期**: 2026-01-09
> **版本**: v1.0.0

---

## 1. 技术架构

### 1.1 技术栈
- **前端框架**: Vue 3 + TypeScript
- **UI组件库**: Element Plus
- **日历组件**: FullCalendar
- **状态管理**: Pinia
- **HTTP客户端**: Axios
- **日期处理**: Day.js / date-fns
- **后端**: Node.js / Express (假设)

### 1.2 架构模式
- **前端架构**: 组件化设计
- **数据流**: 单向数据流
- **代码组织**: 按功能模块划分

---

## 2. 数据结构定义

### 2.1 核心数据类型

```typescript
// 会议预定
interface MeetingBooking {
  id: string                        // 预定编号
  title: string                     // 会议主题
  roomId: string                    // 会议室ID
  roomName: string                  // 会议室名称
  startTime: string                 // 开始时间 ISO格式
  endTime: string                   // 结束时间 ISO格式
  bookerId: string                  // 预定人ID
  bookerName: string                // 预定人姓名
  participantCount: number          // 预计人数
  participantIds: string[]          // 参会人员ID列表
  agenda?: string                   // 会议议程
  equipment?: string[]              // 需要的设备
  status: 'booked' | 'in_progress' | 'completed' | 'cancelled'
  recurringRule?: string            // 循环规则(未来扩展)
  createdAt: string
  updatedAt: string
}

// 会议室
interface MeetingRoom {
  id: string
  name: string                      // 会议室A/B/C/培训室
  capacity: number                  // 容纳人数
  location: string                  // 位置(如: 3楼东侧)
  equipment: string[]               // 设备列表
  imageUrl?: string                 // 会议室图片
  available: boolean                // 是否可用
}

// 查询过滤器
interface BookingFilter {
  roomId?: string
  bookerId?: string
  dateRange?: { start: string; end: string }
  status?: string
  keyword?: string                  // 搜索会议主题
}

// 循环规则(未来扩展)
interface RecurringRule {
  frequency: 'daily' | 'weekly' | 'monthly'
  interval: number
  until?: string
  count?: number
  days?: number[]
}
```

---

## 3. 核心算法实现

### 3.1 冲突检测算法

```typescript
/**
 * 检测时间冲突
 * @param existingBookings 现有预定
 * @param newBooking 新预定
 * @returns 是否存在冲突
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
```

### 3.2 可用时段查找算法

```typescript
/**
 * 查找可用时间段
 * @param roomId 会议室ID
 * @param date 日期
 * @param duration 会议时长(分钟)
 * @returns 可用时段列表
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

### 3.3 预定验证算法

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

/**
 * 验证预定规则
 * @param startTime 开始时间
 * @param endTime 结束时间
 * @throws Error 违反规则时抛出异常
 */
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

---

## 4. 自动化任务实现

### 4.1 会议提醒系统

```typescript
/**
 * 检查并发送会议提醒
 */
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
```

### 4.2 状态自动更新

```typescript
/**
 * 更新会议状态
 */
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

/**
 * 定时任务调度器(每5分钟执行一次)
 */
setInterval(() => {
  checkMeetingReminders()
  updateMeetingStatus()
}, 5 * 60 * 1000)
```

---

## 5. API接口设计

### 5.1 会议室接口

```typescript
// 获取所有会议室
GET /api/meeting-rooms
Response: MeetingRoom[]

// 获取会议室详情
GET /api/meeting-rooms/:id
Response: MeetingRoom

// 创建会议室(管理员)
POST /api/meeting-rooms
Body: Partial<MeetingRoom>
Response: MeetingRoom

// 更新会议室(管理员)
PUT /api/meeting-rooms/:id
Body: Partial<MeetingRoom>
Response: MeetingRoom

// 删除会议室(管理员)
DELETE /api/meeting-rooms/:id
Response: { success: boolean }
```

### 5.2 预定接口

```typescript
// 获取预定列表
GET /api/meeting-bookings?roomId=xxx&dateRange=xxx
Response: MeetingBooking[]

// 获取预定详情
GET /api/meeting-bookings/:id
Response: MeetingBooking

// 创建预定
POST /api/meeting-bookings
Body: Partial<MeetingBooking>
Response: MeetingBooking

// 更新预定
PUT /api/meeting-bookings/:id
Body: Partial<MeetingBooking>
Response: MeetingBooking

// 取消/删除预定
DELETE /api/meeting-bookings/:id
Response: { success: boolean }

// 检查冲突
POST /api/meeting-bookings/check-conflict
Body: { roomId, startTime, endTime }
Response: { hasConflict: boolean; conflictBooking?: MeetingBooking }

// 获取可用时段
GET /api/meeting-bookings/available-slots?roomId=xxx&date=xxx&duration=xxx
Response: { start: string; end: string }[]
```

---

## 6. 前端组件设计

### 6.1 组件结构

```
meeting/
├── components/
│   ├── MeetingCalendar.vue       # 日历视图组件
│   ├── MeetingTimeline.vue       # 时间轴视图组件
│   ├── MeetingList.vue           # 列表视图组件
│   ├── BookingDialog.vue         # 预定对话框
│   ├── BookingDetail.vue         # 预定详情
│   └── RoomSelector.vue          # 会议室选择器
├── composables/
│   ├── useBooking.ts             # 预定逻辑
│   ├── useConflictCheck.ts       # 冲突检测
│   └── useMeetingReminders.ts    # 会议提醒
└── types/
    └── index.ts                  # 类型定义
```

### 6.2 日历组件实现

```vue
<!-- MeetingCalendar.vue -->
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
    daysOfWeek: [1, 2, 3, 4, 5],
    startTime: '08:00',
    endTime: '18:00'
  },
  events: [] as EventInput[],
  selectable: true,
  selectMirror: true,
  dayMaxEvents: true,
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
</script>

<template>
  <FullCalendar :options="calendarOptions" />
</template>
```

---

## 7. 循环会议实现(未来扩展)

```typescript
/**
 * 生成循环会议实例
 */
function generateRecurringInstances(
  baseBooking: MeetingBooking,
  rule: RecurringRule
): MeetingBooking[] {
  const instances: MeetingBooking[] = []
  const startDate = new Date(baseBooking.startTime)
  const endDate = new Date(baseBooking.endTime)

  let count = 0
  const maxCount = rule.count || 10
  const until = rule.until ? new Date(rule.until) : null

  while (count < maxCount) {
    if (until && startDate > until) break

    instances.push({
      ...baseBooking,
      id: `${baseBooking.id}-${count}`,
      startTime: formatDateTime(startDate),
      endTime: formatDateTime(endDate)
    })

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

## 8. 性能优化

### 8.1 前端优化
- 虚拟滚动(列表视图)
- 懒加载(会议详情)
- 防抖/节流(搜索、拖拽)
- 缓存策略(会议室列表)

### 8.2 后端优化
- 索引优化(roomId, startTime, endTime)
- 分页查询
- 查询结果缓存
- 批量操作

---

## 9. 安全考虑

### 9.1 权限验证
- 前端路由守卫
- 后端接口鉴权
- 操作权限校验

### 9.2 数据验证
- 输入参数验证
- 时间范围验证
- 容量限制验证
- XSS防护

---

**文档版本**: v1.0.0
**最后更新**: 2026-01-09
