## Implementation Tasks

### Phase 1: Database Setup
- [ ] 1.1 创建 MySQL 表（admin_meeting_room, admin_meeting_booking, meeting_attendee, meeting_notification）
- [ ] 1.2 插入示例会议室数据

### Phase 2: Backend Implementation
- [ ] 2.1 创建 Enums（MeetingRoomStatus, BookingStatus, MeetingLevel, RecurrenceType, ReminderTime, EquipmentType）
- [ ] 2.2 创建 Entities（MeetingRoom, MeetingBooking, MeetingAttendee, MeetingNotification）
- [ ] 2.3 创建 Mappers（MeetingRoomMapper, MeetingBookingMapper, MeetingAttendeeMapper, MeetingNotificationMapper）
- [ ] 2.4 创建 DTOs（RoomForm, BookingForm, ApprovalForm, CheckInForm, RatingForm, ConflictCheckRequest, AvailabilityQueryRequest, BookingQueryRequest）
- [ ] 2.5 创建 VOs（RoomVO, RoomDetailVO, BookingVO, BookingDetailVO, AttendeeVO, AvailabilityVO, TimeSlotVO, CalendarEventVO, CalendarResourceVO, NotificationVO, RoomUsageStatsVO, DepartmentUsageStatsVO, TimeSlotStatsVO, MonthlyStatsVO, ConflictVO）
- [ ] 2.6 创建 Services（MeetingRoomService, MeetingBookingService, MeetingStatisticsService, MeetingNotificationService）
- [ ] 2.7 创建 Controllers（MeetingRoomController, MeetingBookingController, MeetingApprovalController, MeetingStatisticsController, MeetingCalendarController, MeetingNotificationController）
- [ ] 2.8 创建 ID Generators（MeetingRoomIdGenerator, MeetingIdGenerator）

### Phase 3: Frontend Integration
- [ ] 3.1 修改 `api/index.ts`，将 mock 调用替换为真实 HTTP 调用
- [ ] 3.2 更新类型定义以匹配后端响应
- [ ] 3.3 移除硬编码的用户 ID
- [ ] 3.4 测试前后端集成
