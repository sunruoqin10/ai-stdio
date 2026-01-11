# 会议室预定模块规范

> **模块类型**: 行政协同
> **复杂度**: ⭐⭐⭐⭐
> **开发状态**: ✅ 已实现
> **版本**: v1.0.0
> **更新日期**: 2026-01-11

---

## 📂 文件列表

| 文件名 | 说明 | 目标读者 |
|--------|------|---------|
| [meeting-spec.md](./meeting-spec.md) | 原始规范文档(单层版) | 所有人 |
| [meeting_Functional.md](./meeting_Functional.md) | 功能需求规范(三层) ⭐ | 产品经理、业务分析师 |
| [meeting_Technical.md](./meeting_Technical.md) | 技术实现规范(三层) ⭐ | 前后端开发工程师 |
| [meeting_Design.md](./meeting_Design.md) | UI/UX设计规范(三层) ⭐ | UI设计师、前端开发 |

---

## 🎯 核心功能

- ✅ 日历视图(FullCalendar)
- ✅ 时间轴视图
- ✅ 冲突检测算法
- ✅ 可用时段查找
- ✅ 自动提醒(会议开始前15分钟)
- ✅ 循环会议支持(未来扩展)
- ⭐ **Mock数据支持**
- ⭐ **完整的TypeScript类型系统**
- ⭐ **丰富的工具函数库**
- ⭐ **审批流程**
- ⭐ **签到签退功能**
- ⭐ **会议评价功能**

---

## 📖 模块简介

实现会议室预定管理,支持日历视图、时间轴视图、冲突检测、会议提醒等功能,为企业提供高效的会议室资源管理解决方案。

### 实现亮点

**1. 完整的Mock数据支持**
- 5个预置会议室(第一会议室、第二会议室、多功能厅、VIP接待室、培训室)
- 7个会议预定记录(涵盖所有状态:待审批/已通过/已取消/已驳回)
- 4个通知记录(提醒/审批通过/审批驳回)
- 完整的统计数据(会议室使用统计、部门使用统计、时间段统计、月度统计)

**2. 完整的TypeScript类型系统**
- 10+基础类型(BookingStatus, RoomStatus, RecurrenceType, ReminderTime, MeetingLevel等)
- 核心实体(MeetingRoom, MeetingBooking, Equipment, Attendee, ApprovalRecord等)
- 表单类型(BookingForm, MeetingApprovalForm, CheckInForm, RatingForm, RoomForm)
- 查询参数类型(BookingQueryParams, RoomQueryParams, AvailabilityQueryParams等)
- 统计类型(RoomUsageStats, DepartmentUsageStats, TimeSlotStats, MonthlyStats)
- 日历类型(CalendarEvent, CalendarResource)
- 通知类型(MeetingNotification)

**3. 丰富的工具函数库** (50+个函数)
- **格式化函数**: formatDate(), formatDateTime(), formatTime(), formatDuration()
- **类型转换函数**: getBookingStatusName(), getRoomStatusName(), getRecurrenceTypeName(), getMeetingLevelName(), getEquipmentTypeName()
- **状态判断函数**: canEdit(), canCancel(), canApprove(), canCheckIn(), canCheckOut(), canRate()
- **时间计算函数**: calculateDuration(), isWorkingTime(), calculateReminderTime(), generateRecurrenceDates()
- **冲突检测函数**: hasTimeConflict(), getConflictingBookings(), isRoomAvailable()
- **工作日计算**: calculateWorkingDays(), isWorkingDay()
- **验证函数**: validateTimeFormat(), validateTimeRange(), validateDuration(), validatePhoneNumber()
- **筛选函数**: filterByStatus(), filterByRoom(), filterByDepartment(), filterByDateRange(), filterByKeyword()
- **排序函数**: sortByStartTime(), sortByCreatedAt()
- **统计函数**: calculateUtilizationRate(), calculateAvgAttendees(), calculateCancellationRate()
- **ID生成**: generateBookingId() (MB+YYYYMMDD+4位随机数), generateRoomId() (MR+3位随机数)

**4. 审批流程**
- 预定创建后默认为待审批状态
- 管理员审批(通过/驳回)
- 审批意见记录
- 审批时间记录

**5. 签到签退功能**
- 会议开始后可签到
- 会议结束前可签退
- 记录实际使用时间
- 记录签到签退用户

**6. 会议评价功能**
- 会议结束后可评价
- 星级评分(1-5星)
- 反馈意见

**7. 可用性检查**
- 检查会议室可用性
- 时间冲突检测
- 返回冲突列表

**8. 日历集成**
- 支持FullCalendar
- 日历事件转换
- 日历资源管理

**9. 通知系统**
- 会议提醒(15分钟/30分钟/1小时/1天前)
- 审批结果通知
- 预定取消通知

**10. 统计报表**
- 会议室使用统计(预定次数、总时长、使用率、平均参会人数)
- 部门使用统计(预定次数、总时长、取消率)
- 时间段统计(按小时统计)
- 月度统计(预定次数、总时长、独立用户数、平均时长)

---

## 🔗 相关链接

- [返回上级目录](../)
