## ADDED Requirements

### Requirement: Meeting Room Management
系统 SHALL 提供会议室的完整管理功能，包括创建、查看、更新、删除会议室，以及查询会议室可用性。

#### Scenario: 获取会议室列表
- **WHEN** 用户请求会议室列表（支持分页、状态筛选）
- **THEN** 返回会议室列表及总数

#### Scenario: 获取会议室详情
- **WHEN** 用户请求某会议室详情
- **THEN** 返回会议室详细信息及未来7天内的预定概览

#### Scenario: 创建会议室
- **WHEN** 管理员提交有效的会议室信息
- **THEN** 系统创建会议室并返回创建结果

#### Scenario: 更新会议室
- **WHEN** 管理员更新会议室信息
- **THEN** 系统更新会议室并返回更新结果

#### Scenario: 删除会议室
- **WHEN** 管理员删除会议室（仅当没有待处理预定）
- **THEN** 系统软删除会议室（状态设为 disabled）

#### Scenario: 查询会议室可用性
- **WHEN** 用户查询某会议室某日期的时间段可用性
- **THEN** 返回每个小时的可用状态

### Requirement: Meeting Booking Management
系统 SHALL 提供会议预定的完整管理功能。

#### Scenario: 创建会议预定
- **WHEN** 用户提交有效的预定信息（会议室、时间、参会人等）
- **THEN** 系统创建预定并返回预定信息

#### Scenario: 获取预定列表
- **WHEN** 用户请求预定列表（支持分页、状态、会议室、关键字筛选）
- **THEN** 返回预定列表及总数

#### Scenario: 获取预定详情
- **WHEN** 用户请求某预定详情
- **THEN** 返回预定详细信息

#### Scenario: 更新会议预定
- **WHEN** 用户更新自己的待审批预定
- **THEN** 系统更新预定信息

#### Scenario: 取消会议预定
- **WHEN** 用户取消自己的待审批或已通过预定
- **THEN** 系统将预定状态改为 cancelled

### Requirement: Meeting Approval Workflow
系统 SHALL 提供会议审批功能，支持通过或拒绝预定申请。

#### Scenario: 审批预定
- **WHEN** 审批人提交审批意见（approved/rejected）
- **THEN** 系统更新预定状态并记录审批信息

#### Scenario: 获取待审批列表
- **WHEN** 审批人请求待审批列表
- **THEN** 返回所有待审批的预定

### Requirement: Meeting Check-in/Check-out
系统 SHALL 提供会议签到签退功能。

#### Scenario: 会议签到
- **WHEN** 会议组织者在会议开始后签到
- **THEN** 系统记录实际开始时间

#### Scenario: 会议签退
- **WHEN** 会议组织者在会议结束后签退
- **THEN** 系统记录实际结束时间

### Requirement: Meeting Rating
系统 SHALL 提供会议评价功能。

#### Scenario: 提交评价
- **WHEN** 会议结束后用户提交评分和反馈
- **THEN** 系统记录评价信息

### Requirement: Meeting Statistics
系统 SHALL 提供会议室使用统计功能。

#### Scenario: 获取会议室使用统计
- **WHEN** 管理员请求会议室使用统计
- **THEN** 返回各会议室的使用时长、预定次数等

#### Scenario: 获取部门使用统计
- **WHEN** 管理员请求部门使用统计
- **THEN** 返回各部门的会议数量、使用时长等

#### Scenario: 获取时间段统计
- **WHEN** 管理员请求时间段使用统计
- **THEN** 返回各时间段的会议分布情况

### Requirement: Meeting Calendar
系统 SHALL 提供日历视图所需的 API。

#### Scenario: 获取日历事件
- **WHEN** 用户请求指定日期范围的日历事件
- **THEN** 返回该范围内的所有会议事件

#### Scenario: 获取日历资源
- **WHEN** 用户请求日历资源（会议室列表）
- **THEN** 返回会议室列表作为日历资源

### Requirement: Meeting Notifications
系统 SHALL 提供会议通知功能。

#### Scenario: 获取通知列表
- **WHEN** 用户请求自己的会议通知
- **THEN** 返回该用户的未读通知列表

#### Scenario: 标记通知已读
- **WHEN** 用户标记某通知为已读
- **THEN** 系统更新通知状态

## API Endpoints

### Meeting Room
- `GET /api/meeting/rooms` - 获取会议室列表
- `GET /api/meeting/rooms/{id}` - 获取会议室详情
- `POST /api/meeting/rooms` - 创建会议室
- `PUT /api/meeting/rooms/{id}` - 更新会议室
- `DELETE /api/meeting/rooms/{id}` - 删除会议室
- `GET /api/meeting/rooms/{id}/availability` - 获取可用性

### Meeting Booking
- `GET /api/meeting/bookings` - 获取预定列表
- `GET /api/meeting/bookings/{id}` - 获取预定详情
- `POST /api/meeting/bookings` - 创建预定
- `PUT /api/meeting/bookings/{id}` - 更新预定
- `DELETE /api/meeting/bookings/{id}` - 取消预定
- `POST /api/meeting/bookings/{id}/check-in` - 签到
- `POST /api/meeting/bookings/{id}/check-out` - 签退
- `POST /api/meeting/bookings/{id}/rating` - 评价

### Meeting Approval
- `GET /api/meeting/approvals/pending` - 获取待审批列表
- `POST /api/meeting/approvals/{id}` - 审批

### Meeting Statistics
- `GET /api/meeting/stats/room-usage` - 会议室使用统计
- `GET /api/meeting/stats/department-usage` - 部门使用统计
- `GET /api/meeting/stats/time-slot` - 时间段统计
- `GET /api/meeting/stats/monthly` - 月度统计

### Meeting Calendar
- `GET /api/meeting/calendar/events` - 获取日历事件
- `GET /api/meeting/calendar/resources` - 获取日历资源

### Meeting Notifications
- `GET /api/meeting/notifications` - 获取通知列表
- `PUT /api/meeting/notifications/{id}/read` - 标记已读
