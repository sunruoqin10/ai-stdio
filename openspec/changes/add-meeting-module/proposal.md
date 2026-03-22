# Change: 添加会议模块

## Why
系统需要提供会议室预订、会议管理功能，包括会议室 CRUD、预定管理、审批流程、签到签退、统计报表等完整功能。

## What Changes
- 新增 `meeting` 模块后端（实体、Mapper、Service、Controller）
- 新增会议室管理 API（CRUD、可用性查询）
- 新增会议预定管理 API（CRUD、审批、签到签退、评价）
- 新增统计报表 API
- 新增日历视图 API
- 修改前端 `api/index.ts`，从 mock 数据切换到真实后端 API

## Impact
- 新增规范: `specs/meeting/spec.md`
- 新增代码: `oa-system-backend/src/main/java/com/example/oa_system_backend/module/meeting/`
- 修改代码: `oa-system-frontend/src/modules/meeting/api/index.ts`

## Database Tables
- `admin_meeting_room` - 会议室表
- `admin_meeting_booking` - 会议预定表
- `meeting_attendee` - 会议参与者表
- `meeting_notification` - 会议通知表
