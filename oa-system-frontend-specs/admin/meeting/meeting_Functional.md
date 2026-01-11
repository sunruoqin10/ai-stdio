# 会议室预定模块 - 功能需求规范

> **模块类型**: 行政协同
> **文档类型**: 功能需求规范
> **创建日期**: 2026-01-09
> **版本**: v1.0.0

---

## 1. 功能概述

实现会议室预定管理,支持日历视图、时间轴视图、冲突检测、会议提醒等功能,为企业提供高效的会议室资源管理解决方案。

### 1.1 业务目标
- 提高会议室利用率
- 减少预定冲突
- 提供直观的可视化界面
- 自动化会议管理流程

---

## 2. 核心功能需求

### 2.1 视图展示功能

#### 2.1.1 日历视图
- **月视图**: 展示整月的会议安排概览
- **周视图**: 显示一周的详细时间安排
- **日视图**: 精确到小时的详细视图
- **支持切换**: 用户可自由切换不同视图
- **颜色标识**: 不同会议室使用不同颜色区分

#### 2.1.2 时间轴视图
- 按会议室分栏,时间横向展示(8:00-18:00)
- 直观显示各会议室的使用情况
- 时间轴上显示会议块,包含会议名称和时间
- 快速识别空闲时间段

#### 2.1.3 列表视图
- 表格形式展示所有预定
- 支持多维度筛选(会议室、预定人、时间范围、状态)
- 支持关键字搜索(会议主题)
- 支持排序(时间、会议室、状态)

### 2.2 预定功能

#### 2.2.1 基本信息录入
- **会议室选择**: A/B/C/培训室
- **时间选择**: 开始/结束时间(精确到分钟)
- **会议主题**: 必填,简洁明了
- **预定人**: 默认当前用户,可修改

#### 2.2.2 参会信息
- **参会人员**: 支持多选,从用户列表选择
- **预计人数**: 数字输入,用于检查会议室容量

#### 2.2.3 详细信息
- **会议议程**: 可选,文本框输入
- **设备需求**: 多选(投影仪/白板/视频会议/饮水机)

### 2.3 冲突检测功能

#### 2.3.1 实时冲突检测
- 用户选择时间段时,系统自动检测冲突
- 冲突时显示已预定的会议信息
- 阻止保存冲突的预定

#### 2.3.2 可用时段推荐
- 根据会议时长自动计算可用时段
- 显示当天所有可用时间段
- 一键选择推荐时段

### 2.4 管理功能

#### 2.4.1 预定管理
- **查看详情**: 查看完整预定信息
- **修改预定**: 修改时间、参会人等信息(需重新检测冲突)
- **取消预定**: 取消自己的预定
- **删除权限**: 管理员可删除任何预定

#### 2.4.2 状态管理
- **booked**: 已预定
- **in_progress**: 进行中(会议开始自动更新)
- **completed**: 已结束(会议结束自动更新)
- **cancelled**: 已取消

### 2.5 自动化功能

#### 2.5.1 会议提醒
- **提醒时机**: 会议开始前15分钟
- **提醒对象**: 所有参会人员
- **提醒方式**: 系统内通知
- **提醒内容**: 会议主题、时间、地点

#### 2.5.2 状态自动更新
- **开始检测**: 系统定时检查会议状态
- **自动开始**: 到达开始时间自动更新为"进行中"
- **自动结束**: 到达结束时间自动更新为"已完成"

---

## 3. 业务规则

### 3.1 预定规则

#### 3.1.1 时间限制
- **最小时长**: 15分钟
- **最大时长**: 4小时
- **最远预定**: 提前30天
- **最提前预定**: 不限制(可即时预定)

#### 3.1.2 工作时间
- **工作日**: 周一到周五
- **工作时段**: 8:00-18:00
- **非工作时间**: 不支持预定

#### 3.1.3 会议室规则
- **容量限制**: 参会人数不能超过会议室容量
- **设备限制**: 只能选择该会议室配备的设备
- **可用状态**: 不可用的会议室不能预定

### 3.2 权限规则

#### 3.2.1 普通用户
- 创建预定
- 查看所有预定
- 修改/取消自己的预定
- 查看会议室详情

#### 3.2.2 管理员
- 所有普通用户权限
- 修改/取消任何预定
- 管理会议室信息
- 设置会议室可用状态

#### 3.2.3 字段级权限控制

| 字段 | 普通用户 | 管理员 |
|------|---------|--------|
| 基本信息(主题/时间/参会人) | ✅ 查看/编辑(自己) | ✅ 查看/编辑 |
| 会议议程 | ✅ 查看/编辑(自己) | ✅ 查看/编辑 |
| 设备需求 | ✅ 查看(自己) | ✅ 查看/编辑 |
| 预定人信息 | ✅ 查看(自己) | ✅ 查看 |
| 会议室状态 | ✅ 查看 | ✅ 查看/编辑 |
| 会议室配置(容量/设备) | ✅ 查看 | ✅ 查看/编辑 |
| 取消原因 | ✅ 查看/填写(自己) | ✅ 查看/填写 |

---

### 3.3 数据字典集成

#### 3.3.1 依赖的数据字典类型

会议室预定模块依赖以下数据字典类型:

| 字典类型 | 字典编码 | 用途 | 是否必填 |
|---------|---------|------|---------|
| 预定状态 | `booking_status` | 预定状态(已预定/进行中/已完成/已取消) | ✅ |
| 会议室类型 | `room_type` | 会议室分类(大会议室/小会议室/培训室) | ✅ |
| 设备类型 | `equipment_type` | 会议室设备(投影仪/白板/视频会议/饮水机) | ❌ |
| 时间段 | `time_slot` | 时间段选项(按小时/按半天) | ❌ |

#### 3.3.2 数据字典使用场景

**1. 预定状态筛选**
```typescript
// 从数据字典加载预定状态选项
const statusOptions = [
  { label: '已预定', value: 'booked', dictCode: 'booking_status' },
  { label: '进行中', value: 'in_progress', dictCode: 'booking_status' },
  { label: '已完成', value: 'completed', dictCode: 'booking_status' },
  { label: '已取消', value: 'cancelled', dictCode: 'booking_status' }
]
```

**2. 会议室类型选择**
```typescript
// 从数据字典加载会议室类型选项
const roomTypeOptions = await getDictList('room_type')
// 返回: [{ label: '大会议室', value: 'large' }, { label: '小会议室', value: 'small' }, ...]
```

**3. 设备类型选择**
```typescript
// 从数据字典加载设备类型选项
const equipmentOptions = await getDictList('equipment_type')
// 返回: [{ label: '投影仪', value: 'projector' }, { label: '白板', value: 'whiteboard' }, ...]
```

**4. 状态标签显示**
```typescript
// 从数据字典获取显示文本
const statusText = getDictLabel('booking_status', 'booked') // "已预定"
```

#### 3.3.3 数据字典初始化要求

- **模块加载时**: 预加载预定状态、会议室类型字典
- **表单编辑时**: 动态加载设备类型字典
- **筛选面板**: 使用缓存的字典数据
- **字典刷新**: 监听字典变更事件,自动更新界面显示

#### 3.3.4 数据字典缓存策略

```typescript
// 字典数据缓存管理
const dictCache = {
  // 常用字典: 启动时预加载
  preload: ['booking_status', 'room_type'],

  // 低频字典: 按需加载
  onDemand: ['equipment_type', 'time_slot'],

  // 缓存过期时间: 30分钟
  expireTime: 30 * 60 * 1000
}
```

---

### 3.4 权限管理集成

#### 3.4.1 会议室管理权限定义

| 权限编码 | 权限名称 | 权限描述 | 依赖角色 |
|---------|---------|---------|---------|
| `meeting:book` | 创建预定 | 创建会议室预定 | 所有用户 |
| `meeting:view_own` | 查看自己的预定 | 查看自己创建的预定 | 所有用户 |
| `meeting:view_all` | 查看所有预定 | 查看所有会议室预定 | 所有用户 |
| `meeting:edit_own` | 编辑自己的预定 | 编辑自己的预定(需重新检测冲突) | 所有用户 |
| `meeting:cancel_own` | 取消自己的预定 | 取消自己的预定 | 所有用户 |
| `meeting:edit_all` | 编辑任何预定 | 编辑任何用户的预定 | 管理员 |
| `meeting:cancel_all` | 取消任何预定 | 取消任何用户的预定 | 管理员 |
| `meeting:manage_room` | 管理会议室 | 管理会议室信息、容量、设备、可用状态 | 管理员 |
| `meeting:view_statistics` | 查看统计报表 | 查看会议室使用统计 | 管理员 |

#### 3.4.2 功能权限矩阵

| 功能 | 普通用户 | 管理员 |
|------|---------|--------|
| 创建预定 | ✅ meeting:book | ✅ meeting:book |
| 查看所有预定 | ✅ meeting:view_all | ✅ meeting:view_all |
| 编辑自己的预定 | ✅ meeting:edit_own | ✅ meeting:edit_own |
| 取消自己的预定 | ✅ meeting:cancel_own | ✅ meeting:cancel_own |
| 编辑任何预定 | ❌ | ✅ meeting:edit_all |
| 取消任何预定 | ❌ | ✅ meeting:cancel_all |
| 管理会议室信息 | ❌ | ✅ meeting:manage_room |
| 设置会议室可用状态 | ❌ | ✅ meeting:manage_room |
| 查看使用统计 | ❌ | ✅ meeting:view_statistics |

#### 3.4.3 权限检查实现

```typescript
// 权限检查函数
function checkPermission(permission: string): boolean {
  const authStore = useAuthStore()
  return authStore.hasPermission(permission)
}

// 使用示例
const canBook = computed(() => checkPermission('meeting:book'))
const canManageRoom = computed(() => checkPermission('meeting:manage_room'))

// 数据权限过滤
const filteredBookings = computed(() => {
  // 所有预定都可以查看
  return bookingList.value
})

// 操作权限过滤
const canEditBooking = (booking: any) => {
  return booking.bookerId === currentUser.id && checkPermission('meeting:edit_own') ||
         checkPermission('meeting:edit_all')
}

const canCancelBooking = (booking: any) => {
  return booking.bookerId === currentUser.id && checkPermission('meeting:cancel_own') ||
         checkPermission('meeting:cancel_all')
}
```

#### 3.4.4 按钮级权限控制

```vue
<!-- 根据权限显示/隐藏按钮 -->
<el-button
  v-if="hasPermission('meeting:book')"
  type="primary"
  @click="handleBook"
>
  预定会议室
</el-button>

<el-button
  v-if="canEditBooking(row)"
  size="small"
  @click="handleEdit(row)"
>
  编辑
</el-button>

<el-button
  v-if="canCancelBooking(row)"
  type="danger"
  size="small"
  @click="handleCancel(row)"
>
  取消预定
</el-button>

<el-button
  v-if="hasPermission('meeting:manage_room')"
  @click="handleManageRoom"
>
  管理会议室
</el-button>
```

#### 3.4.5 字段级权限控制

```typescript
// 敏感字段权限判断
const fieldPermissions = {
  roomStatus: {
    editable: computed(() => checkPermission('meeting:manage_room'))
  },
  roomCapacity: {
    editable: computed(() => checkPermission('meeting:manage_room'))
  },
  roomEquipment: {
    editable: computed(() => checkPermission('meeting:manage_room'))
  }
}

// 表单中使用
const canEditRoomInfo = computed(() => {
  return checkPermission('meeting:manage_room')
})
```

---

### 3.5 冲突处理规则

#### 3.3.1 冲突定义
- 同一会议室
- 时间段重叠(包含开始/结束时间)

#### 3.3.2 重叠判断
```
两个会议A和B存在冲突,当且仅当:
(A.startTime < B.endTime) && (B.startTime < A.endTime)
```

#### 3.3.3 冲突处理
- 阻止保存冲突预定
- 显示冲突会议信息
- 提供可用时段建议

---

## 4. 用户故事

### 4.1 预定会议室
**作为**员工
**我想要**预定会议室
**以便**安排我的会议

**验收标准:**
- 能选择会议室和时间
- 能填写会议信息
- 系统检测并防止冲突
- 预定成功后收到确认

### 4.2 查看会议安排
**作为**员工
**我想要**查看会议室的使用情况
**以便**了解何时有空闲会议室

**验收标准:**
- 能以多种视图查看(日历/时间轴/列表)
- 能筛选特定会议室
- 能搜索特定会议

### 4.3 管理我的预定
**作为**员工
**我想要**修改或取消我的预定
**以便**调整我的计划

**验收标准:**
- 能查看我的所有预定
- 能修改时间、参会人等信息
- 能取消预定
- 修改时系统重新检测冲突

### 4.4 接收会议提醒
**作为**参会人员
**我想要**在会议开始前收到提醒
**以便**准时参加会议

**验收标准:**
- 会议前15分钟收到提醒
- 提醒包含会议关键信息
- 能快速查看会议详情

---

## 5. 数据需求

### 5.1 会议预定数据
- 预定编号
- 会议主题
- 会议室信息
- 开始/结束时间
- 预定人信息
- 参会人员列表
- 预计人数
- 会议议程
- 设备需求
- 预定状态
- 创建/更新时间

### 5.2 会议室数据
- 会议室ID
- 会议室名称
- 容纳人数
- 位置描述
- 配备设备
- 会议室图片
- 可用状态

---

## 6. 非功能性需求

### 6.1 易用性
- 界面直观,操作简单
- 响应式设计,支持移动端
- 清晰的错误提示
- 智能的默认值设置

### 6.2 可用性
- 系统可用率 > 99%
- 并发用户数 > 100
- 响应时间 < 2秒

### 6.3 可扩展性
- 支持未来增加会议室
- 预留循环会议接口
- 支持与其他模块集成

---

## 7. Mock数据支持

会议室预定模块提供了完整的Mock数据实现,便于前端独立开发和测试:

**Mock数据结构** (`src/modules/meeting/mock/data.ts`):
- **5个预置会议室**:
  - 第一会议室: 容量20人,3楼东侧,配备投影仪、投影幕、白板、音响
  - 第二会议室: 容量10人,3楼西侧,配备投影仪、白板
  - 多功能厅: 容量100人,5楼,配备高清投影仪、LED大屏、专业音响、视频会议系统、白板
  - VIP接待室: 容量8人,6楼,配备投影仪、白板、视频会议系统
  - 培训室: 容量30人,4楼,配备投影仪、投影幕、白板、音响、30台计算机(维护中)
- **7个会议预定记录**,涵盖所有状态:
  - 2个已通过(产品需求评审会、全员大会)
  - 2个待审批(技术方案讨论、项目启动会)
  - 1个已取消(周例会)
  - 1个已驳回(面试会议)
  - 1个已完成且已评价(客户洽谈,包含签到签退记录)
- **4个通知记录**:
  - 会议提醒(提前30分钟)
  - 审批通过通知
  - 审批驳回通知
- **统计数据**:
  - 会议室使用统计(4个会议室)
  - 部门使用统计(产品部、技术部、销售部)
  - 时间段统计(9:00-16:00)
  - 月度统计(10-12月)

**Mock API实现** (`src/modules/meeting/api/index.ts`):

**会议室管理接口**:
- `getMeetingRooms()`: 获取会议室列表,支持容量、楼层、设备类型、状态筛选
- `getMeetingRoomDetail()`: 获取会议室详情
- `createMeetingRoom()`: 创建会议室
- `updateMeetingRoom()`: 更新会议室信息
- `deleteMeetingRoom()`: 删除会议室

**会议预定管理接口**:
- `getMeetingBookings()`: 获取会议预定列表,支持会议室、状态、预定人、部门、日期范围、关键词筛选和分页
- `getMeetingBookingDetail()`: 获取会议预定详情
- `createMeetingBooking()`: 创建会议预定,自动生成MB编号
- `updateMeetingBooking()`: 更新会议预定(仅待审批状态)
- `cancelMeetingBooking()`: 取消会议预定(待审批或已通过状态)

**审批管理接口**:
- `getPendingApprovals()`: 获取待审批列表
- `approveMeetingBooking()`: 审批会议预定(通过/驳回)

**签到签退接口**:
- `checkInMeeting()`: 会议签到
- `checkOutMeeting()`: 会议签退

**评价接口**:
- `submitMeetingRating()`: 提交会议评价

**可用性检查接口**:
- `checkRoomAvailability()`: 检查会议室可用性,返回冲突列表
- `checkTimeConflicts()`: 检查时间冲突

**统计分析接口**:
- `getRoomUsageStats()`: 获取会议室使用统计
- `getDepartmentUsageStats()`: 获取部门使用统计
- `getTimeSlotStats()`: 获取时间段统计
- `getMonthlyStats()`: 获取月度统计

**日历接口**:
- `getCalendarEvents()`: 获取日历事件,支持日期范围和会议室筛选
- `getCalendarResources()`: 获取日历资源(会议室列表)

**通知接口**:
- `getNotifications()`: 获取用户通知列表
- `markNotificationRead()`: 标记通知为已读
- `sendMeetingReminder()`: 发送会议提醒

---

## 8. 工具函数实现

会议室预定模块提供了丰富的工具函数 (`src/modules/meeting/utils/index.ts`):

**格式化函数**:
- `formatDate(date)`: 格式化日期为 YYYY-MM-DD
- `formatDateTime(date)`: 格式化日期时间为 YYYY-MM-DD HH:mm
- `formatTime(date)`: 格式化时间为 HH:mm
- `formatDuration(minutes)`: 格式化时长(分钟 → X小时X分钟)

**类型转换函数**:
- `getBookingStatusName(status)`: 获取预定状态中文名称
  - pending → 待审批
  - approved → 已通过
  - rejected → 已驳回
  - cancelled → 已取消
- `getBookingStatusType(status)`: 获取Element Plus Tag类型
- `getRoomStatusName(status)`: 获取会议室状态中文名称(空闲/使用中/维护中)
- `getRoomStatusType(status)`: 获取会议室状态标签类型
- `getRecurrenceTypeName(type)`: 获取重复类型中文名称(不重复/每天/每周/每月)
- `getReminderTimeName(time)`: 获取提醒时间中文名称(不提醒/提前15分钟/30分钟/1小时/1天)
- `getMeetingLevelName(level)`: 获取会议等级中文名称(普通/重要/紧急)
- `getMeetingLevelType(level)`: 获取会议等级标签类型
- `getEquipmentTypeName(type)`: 获取设备类型中文名称(投影仪/投影幕/白板/音响设备/视频设备/计算机/其他)

**状态判断函数**:
- `canEdit(status)`: 判断是否可编辑(仅待审批状态)
- `canCancel(status)`: 判断是否可取消(待审批或已通过状态)
- `canApprove(status)`: 判断是否可审批(待审批状态)
- `canCheckIn(booking)`: 判断是否可签到(已通过且在会议时间内且未签到)
- `canCheckOut(booking)`: 判断是否可签退(已签到且未签退)
- `canRate(booking)`: 判断是否可评价(已通过且已结束且未评价)

**时间计算函数**:
- `calculateDuration(startTime, endTime)`: 计算会议时长(分钟)
- `isWorkingTime(date)`: 判断是否在工作时间(工作日9:00-18:00)
- `calculateReminderTime(startTime, reminder)`: 计算提醒时间
- `generateRecurrenceDates(startDate, recurrenceType, interval, endDate)`: 生成重复会议日期

**冲突检测函数**:
- `hasTimeConflict(booking1, booking2)`: 检查两个预定是否有时间冲突
- `getConflictingBookings(roomId, startTime, endTime, existingBookings, excludeBookingId)`: 获取冲突的预定列表
- `isRoomAvailable(room, bookings, startTime, endTime)`: 检查会议室是否可用

**工作日计算**:
- `calculateWorkingDays(startDate, endDate)`: 计算工作日天数(排除周末)
- `isWorkingDay(date)`: 判断是否为工作日(周一到周五)

**验证函数**:
- `validateTimeFormat(time)`: 验证时间格式(HH:mm)
- `validateTimeRange(startTime, endTime)`: 验证时间范围(开始时间必须早于结束时间)
- `validateDuration(startTime, endTime)`: 验证会议时长(不超过8小时)
- `validatePhoneNumber(phone)`: 验证手机号格式(1开头的11位数字)

**筛选函数**:
- `filterByStatus(bookings, status)`: 按状态筛选
- `filterByRoom(bookings, roomId)`: 按会议室筛选
- `filterByDepartment(bookings, departmentId)`: 按部门筛选
- `filterByDateRange(bookings, startDate, endDate)`: 按日期范围筛选
- `filterByKeyword(bookings, keyword)`: 按关键词筛选(标题、预定人、议程)

**排序函数**:
- `sortByStartTime(bookings, order)`: 按开始时间排序
- `sortByCreatedAt(bookings, order)`: 按创建时间排序

**统计函数**:
- `calculateUtilizationRate(bookings, roomCapacity)`: 计算会议室使用率
- `calculateAvgAttendees(bookings)`: 计算平均参会人数
- `calculateCancellationRate(bookings)`: 计算取消率

**工具提示函数**:
- `getStatusTip(status)`: 获取状态提示信息
- `getLevelTip(level)`: 获取会议等级提示
- `getEquipmentTip(equipments)`: 获取设备提示

**ID生成**:
- `generateBookingId()`: 生成预定ID (MB+YYYYMMDD+4位随机数)
- `generateRoomId()`: 生成会议室ID (MR+3位随机数)

**颜色工具**:
- `getStatusColor(status)`: 获取状态颜色(十六进制)
- `getLevelColor(level)`: 获取等级颜色(十六进制)

---

**文档版本**: v1.0.0
**最后更新**: 2026-01-11
