/**
 * 会议室预定模块 - Mock数据
 *
 * 用于前端开发和测试
 */

import type {
  MeetingRoom,
  MeetingBooking,
  Equipment,
  Attendee
} from '../types'

// ==================== 会议室数据 ====================

export const mockMeetingRooms: MeetingRoom[] = [
  {
    id: 'MR001',
    name: '第一会议室',
    location: '3楼东侧',
    capacity: 20,
    floor: 3,
    area: 60,
    equipments: [
      { id: 'EQ001', name: '高清投影仪', type: 'projector', quantity: 1, available: true },
      { id: 'EQ002', name: '120寸投影幕', type: 'screen', quantity: 1, available: true },
      { id: 'EQ003', name: '白板', type: 'whiteboard', quantity: 2, available: true },
      { id: 'EQ004', name: '音响系统', type: 'audio', quantity: 1, available: true }
    ],
    status: 'available',
    images: [],
    description: '适合中型会议,配备齐全的会议设备',
    createdAt: '2026-01-01T00:00:00Z',
    updatedAt: '2026-01-01T00:00:00Z'
  },
  {
    id: 'MR002',
    name: '第二会议室',
    location: '3楼西侧',
    capacity: 10,
    floor: 3,
    area: 35,
    equipments: [
      { id: 'EQ005', name: '投影仪', type: 'projector', quantity: 1, available: true },
      { id: 'EQ006', name: '白板', type: 'whiteboard', quantity: 1, available: true }
    ],
    status: 'available',
    images: [],
    description: '适合小型会议和团队讨论',
    createdAt: '2026-01-01T00:00:00Z',
    updatedAt: '2026-01-01T00:00:00Z'
  },
  {
    id: 'MR003',
    name: '多功能厅',
    location: '5楼',
    capacity: 100,
    floor: 5,
    area: 200,
    equipments: [
      { id: 'EQ007', name: '高清投影仪', type: 'projector', quantity: 2, available: true },
      { id: 'EQ008', name: 'LED大屏', type: 'screen', quantity: 1, available: true },
      { id: 'EQ009', name: '专业音响', type: 'audio', quantity: 1, available: true },
      { id: 'EQ010', name: '视频会议系统', type: 'video', quantity: 1, available: true },
      { id: 'EQ011', name: '白板', type: 'whiteboard', quantity: 4, available: true }
    ],
    status: 'available',
    images: [],
    description: '大型会议厅,适合全员大会、培训等活动',
    createdAt: '2026-01-01T00:00:00Z',
    updatedAt: '2026-01-01T00:00:00Z'
  },
  {
    id: 'MR004',
    name: 'VIP接待室',
    location: '6楼',
    capacity: 8,
    floor: 6,
    area: 40,
    equipments: [
      { id: 'EQ012', name: '投影仪', type: 'projector', quantity: 1, available: true },
      { id: 'EQ013', name: '白板', type: 'whiteboard', quantity: 1, available: true },
      { id: 'EQ014', name: '视频会议系统', type: 'video', quantity: 1, available: true }
    ],
    status: 'available',
    images: [],
    description: 'VIP接待室,环境优雅',
    createdAt: '2026-01-01T00:00:00Z',
    updatedAt: '2026-01-01T00:00:00Z'
  },
  {
    id: 'MR005',
    name: '培训室',
    location: '4楼',
    capacity: 30,
    floor: 4,
    area: 80,
    equipments: [
      { id: 'EQ015', name: '投影仪', type: 'projector', quantity: 1, available: true },
      { id: 'EQ016', name: '投影幕', type: 'screen', quantity: 1, available: true },
      { id: 'EQ017', name: '白板', type: 'whiteboard', quantity: 3, available: true },
      { id: 'EQ018', name: '音响设备', type: 'audio', quantity: 1, available: true },
      { id: 'EQ019', name: '计算机', type: 'computer', quantity: 30, available: true }
    ],
    status: 'maintenance',
    images: [],
    description: '专业培训室,每人配备电脑',
    createdAt: '2026-01-01T00:00:00Z',
    updatedAt: '2026-01-01T00:00:00Z'
  }
]

// ==================== 会议预定数据 ====================

const mockAttendees: Attendee[] = [
  { userId: 'U001', userName: '张三', department: '技术部', email: 'zhangsan@company.com', phone: '13800138001', required: true, status: 'accepted' },
  { userId: 'U002', userName: '李四', department: '技术部', email: 'lisi@company.com', phone: '13800138002', required: true, status: 'accepted' },
  { userId: 'U003', userName: '王五', department: '产品部', email: 'wangwu@company.com', phone: '13800138003', required: false, status: 'pending' }
]

export const mockMeetingBookings: MeetingBooking[] = [
  {
    id: 'MB20260110001',
    title: '产品需求评审会',
    organizerId: 'U001',
    organizerName: '张三',
    organizerPhone: '13800138001',
    departmentId: 'D001',
    departmentName: '产品部',
    roomId: 'MR001',
    roomName: '第一会议室',
    startTime: '2026-01-15T09:00:00Z',
    endTime: '2026-01-15T11:00:00Z',
    duration: 120,
    attendees: [
      { userId: 'U001', userName: '张三', department: '产品部', required: true, status: 'accepted' },
      { userId: 'U002', userName: '李四', department: '技术部', required: true, status: 'accepted' },
      { userId: 'U004', userName: '赵六', department: '设计部', required: true, status: 'pending' }
    ],
    agenda: '讨论Q1产品需求',
    level: 'important',
    isPrivate: false,
    reminder: '30min',
    status: 'approved',
    approval: {
      approverId: 'ADMIN',
      approverName: '管理员',
      status: 'approved',
      opinion: '同意',
      timestamp: '2026-01-10T10:00:00Z'
    },
    createdAt: '2026-01-10T09:00:00Z',
    updatedAt: '2026-01-10T10:00:00Z',
    createdBy: 'U001'
  },
  {
    id: 'MB20260110002',
    title: '技术方案讨论',
    organizerId: 'U002',
    organizerName: '李四',
    organizerPhone: '13800138002',
    departmentId: 'D002',
    departmentName: '技术部',
    roomId: 'MR002',
    roomName: '第二会议室',
    startTime: '2026-01-16T14:00:00Z',
    endTime: '2026-01-16T16:00:00Z',
    duration: 120,
    attendees: [
      { userId: 'U002', userName: '李四', department: '技术部', required: true, status: 'accepted' },
      { userId: 'U005', userName: '孙七', department: '技术部', required: true, status: 'accepted' }
    ],
    agenda: '讨论新架构方案',
    level: 'normal',
    isPrivate: false,
    reminder: '15min',
    status: 'pending',
    createdAt: '2026-01-10T11:00:00Z',
    updatedAt: '2026-01-10T11:00:00Z',
    createdBy: 'U002'
  },
  {
    id: 'MB20260110003',
    title: '全员大会',
    organizerId: 'ADMIN',
    organizerName: '管理员',
    organizerPhone: '13800138000',
    departmentId: 'D000',
    departmentName: '行政部',
    roomId: 'MR003',
    roomName: '多功能厅',
    startTime: '2026-01-20T09:30:00Z',
    endTime: '2026-01-20T11:30:00Z',
    duration: 120,
    attendees: mockAttendees,
    agenda: '年度总结大会',
    level: 'urgent',
    isPrivate: false,
    reminder: '1day',
    status: 'approved',
    approval: {
      approverId: 'ADMIN',
      approverName: '管理员',
      status: 'approved',
      opinion: '同意',
      timestamp: '2026-01-10T12:00:00Z'
    },
    createdAt: '2026-01-10T12:00:00Z',
    updatedAt: '2026-01-10T12:00:00Z',
    createdBy: 'ADMIN'
  },
  {
    id: 'MB20260110004',
    title: '客户洽谈',
    organizerId: 'U003',
    organizerName: '王五',
    organizerPhone: '13800138003',
    departmentId: 'D003',
    departmentName: '销售部',
    roomId: 'MR004',
    roomName: 'VIP接待室',
    startTime: '2026-01-12T10:00:00Z',
    endTime: '2026-01-12T12:00:00Z',
    duration: 120,
    attendees: [
      { userId: 'U003', userName: '王五', department: '销售部', required: true, status: 'accepted' }
    ],
    agenda: '与重要客户洽谈合作事宜',
    level: 'important',
    isPrivate: true,
    reminder: '1hour',
    status: 'approved',
    approval: {
      approverId: 'ADMIN',
      approverName: '管理员',
      status: 'approved',
      opinion: '同意',
      timestamp: '2026-01-09T15:00:00Z'
    },
    actualStartTime: '2026-01-12T10:05:00Z',
    actualEndTime: '2026-01-12T11:55:00Z',
    checkInUser: 'U003',
    checkOutUser: 'U003',
    rating: 5,
    feedback: '会议室很好用,设备齐全',
    createdAt: '2026-01-09T14:00:00Z',
    updatedAt: '2026-01-12T11:55:00Z',
    createdBy: 'U003'
  },
  {
    id: 'MB20260110005',
    title: '周例会',
    organizerId: 'U001',
    organizerName: '张三',
    organizerPhone: '13800138001',
    departmentId: 'D001',
    departmentName: '产品部',
    roomId: 'MR001',
    roomName: '第一会议室',
    startTime: '2026-01-08T10:00:00Z',
    endTime: '2026-01-08T11:00:00Z',
    duration: 60,
    attendees: [
      { userId: 'U001', userName: '张三', department: '产品部', required: true, status: 'accepted' },
      { userId: 'U002', userName: '李四', department: '技术部', required: false, status: 'declined' }
    ],
    agenda: '产品部周例会',
    level: 'normal',
    isPrivate: false,
    reminder: '30min',
    status: 'cancelled',
    createdAt: '2026-01-08T09:00:00Z',
    updatedAt: '2026-01-08T16:00:00Z',
    createdBy: 'U001'
  },
  {
    id: 'MB20260110006',
    title: '面试会议',
    organizerId: 'HR001',
    organizerName: '人事专员',
    organizerPhone: '13800138010',
    departmentId: 'D004',
    departmentName: '人事部',
    roomId: 'MR002',
    roomName: '第二会议室',
    startTime: '2026-01-17T14:00:00Z',
    endTime: '2026-01-17T15:00:00Z',
    duration: 60,
    attendees: [
      { userId: 'HR001', userName: '人事专员', department: '人事部', required: true, status: 'accepted' },
      { userId: 'U001', userName: '张三', department: '产品部', required: true, status: 'pending' }
    ],
    agenda: '候选人面试',
    level: 'normal',
    isPrivate: true,
    reminder: '30min',
    status: 'rejected',
    rejectionReason: '该时间段会议室已被占用',
    createdAt: '2026-01-10T13:00:00Z',
    updatedAt: '2026-01-10T14:00:00Z',
    createdBy: 'HR001'
  },
  {
    id: 'MB20260110007',
    title: '项目启动会',
    organizerId: 'U002',
    organizerName: '李四',
    organizerPhone: '13800138002',
    departmentId: 'D002',
    departmentName: '技术部',
    roomId: 'MR001',
    roomName: '第一会议室',
    startTime: '2026-01-22T09:00:00Z',
    endTime: '2026-01-22T12:00:00Z',
    duration: 180,
    attendees: [
      { userId: 'U002', userName: '李四', department: '技术部', required: true, status: 'accepted' },
      { userId: 'U001', userName: '张三', department: '产品部', required: true, status: 'accepted' },
      { userId: 'U004', userName: '赵六', department: '设计部', required: true, status: 'pending' }
    ],
    agenda: '新项目启动',
    level: 'important',
    isPrivate: false,
    reminder: '1day',
    status: 'pending',
    createdAt: '2026-01-10T15:00:00Z',
    updatedAt: '2026-01-10T15:00:00Z',
    createdBy: 'U002'
  }
]

// ==================== 设备数据 ====================

export const mockEquipments: Equipment[] = [
  { id: 'EQ001', name: '高清投影仪', type: 'projector', quantity: 1, available: true },
  { id: 'EQ002', name: '120寸投影幕', type: 'screen', quantity: 1, available: true },
  { id: 'EQ003', name: '白板', type: 'whiteboard', quantity: 2, available: true },
  { id: 'EQ004', name: '音响系统', type: 'audio', quantity: 1, available: true },
  { id: 'EQ005', name: '投影仪', type: 'projector', quantity: 1, available: true },
  { id: 'EQ006', name: '白板', type: 'whiteboard', quantity: 1, available: true },
  { id: 'EQ007', name: '高清投影仪', type: 'projector', quantity: 2, available: true },
  { id: 'EQ008', name: 'LED大屏', type: 'screen', quantity: 1, available: true },
  { id: 'EQ009', name: '专业音响', type: 'audio', quantity: 1, available: true },
  { id: 'EQ010', name: '视频会议系统', type: 'video', quantity: 1, available: true },
  { id: 'EQ011', name: '白板', type: 'whiteboard', quantity: 4, available: true },
  { id: 'EQ012', name: '投影仪', type: 'projector', quantity: 1, available: true },
  { id: 'EQ013', name: '白板', type: 'whiteboard', quantity: 1, available: true },
  { id: 'EQ014', name: '视频会议系统', type: 'video', quantity: 1, available: true },
  { id: 'EQ015', name: '投影仪', type: 'projector', quantity: 1, available: true },
  { id: 'EQ016', name: '投影幕', type: 'screen', quantity: 1, available: true },
  { id: 'EQ017', name: '白板', type: 'whiteboard', quantity: 3, available: true },
  { id: 'EQ018', name: '音响设备', type: 'audio', quantity: 1, available: true },
  { id: 'EQ019', name: '计算机', type: 'computer', quantity: 30, available: true }
]

// ==================== 通知数据 ====================

export const mockNotifications = [
  {
    id: 'N001',
    type: 'reminder' as const,
    bookingId: 'MB20260110001',
    title: '会议提醒',
    message: '您预约的"产品需求评审会"将在30分钟后开始',
    recipientId: 'U001',
    isRead: false,
    createdAt: '2026-01-15T08:30:00Z'
  },
  {
    id: 'N002',
    type: 'approved' as const,
    bookingId: 'MB20260110001',
    title: '审批通过',
    message: '您预约的"产品需求评审会"已通过审批',
    recipientId: 'U001',
    isRead: true,
    createdAt: '2026-01-10T10:00:00Z'
  },
  {
    id: 'N003',
    type: 'rejected' as const,
    bookingId: 'MB20260110006',
    title: '审批驳回',
    message: '您预约的"面试会议"未通过审批:该时间段会议室已被占用',
    recipientId: 'HR001',
    isRead: true,
    createdAt: '2026-01-10T14:00:00Z'
  }
]

// ==================== 统计数据 ====================

export const mockRoomUsageStats = [
  {
    roomId: 'MR001',
    roomName: '第一会议室',
    totalBookings: 25,
    totalHours: 50,
    utilizationRate: 75,
    avgAttendees: 12
  },
  {
    roomId: 'MR002',
    roomName: '第二会议室',
    totalBookings: 30,
    totalHours: 45,
    utilizationRate: 68,
    avgAttendees: 6
  },
  {
    roomId: 'MR003',
    roomName: '多功能厅',
    totalBookings: 10,
    totalHours: 30,
    utilizationRate: 45,
    avgAttendees: 50
  },
  {
    roomId: 'MR004',
    roomName: 'VIP接待室',
    totalBookings: 15,
    totalHours: 25,
    utilizationRate: 38,
    avgAttendees: 5
  }
]

export const mockDepartmentUsageStats = [
  {
    departmentId: 'D001',
    departmentName: '产品部',
    totalBookings: 20,
    totalHours: 40,
    cancellationRate: 5
  },
  {
    departmentId: 'D002',
    departmentName: '技术部',
    totalBookings: 35,
    totalHours: 70,
    cancellationRate: 8
  },
  {
    departmentId: 'D003',
    departmentName: '销售部',
    totalBookings: 15,
    totalHours: 20,
    cancellationRate: 10
  }
]

export const mockTimeSlotStats = [
  { hour: 9, bookingCount: 8, utilizationRate: 80 },
  { hour: 10, bookingCount: 12, utilizationRate: 100 },
  { hour: 11, bookingCount: 10, utilizationRate: 90 },
  { hour: 14, bookingCount: 9, utilizationRate: 85 },
  { hour: 15, bookingCount: 11, utilizationRate: 95 },
  { hour: 16, bookingCount: 7, utilizationRate: 70 }
]

export const mockMonthlyStats = [
  {
    month: '2025-10',
    totalBookings: 85,
    totalHours: 180,
    uniqueUsers: 35,
    avgDuration: 75
  },
  {
    month: '2025-11',
    totalBookings: 92,
    totalHours: 195,
    uniqueUsers: 38,
    avgDuration: 78
  },
  {
    month: '2025-12',
    totalBookings: 78,
    totalHours: 165,
    uniqueUsers: 32,
    avgDuration: 72
  }
]

// ==================== 导出所有数据 ====================

export const mockData = {
  rooms: mockMeetingRooms,
  bookings: mockMeetingBookings,
  equipments: mockEquipments,
  notifications: mockNotifications,
  stats: {
    roomUsage: mockRoomUsageStats,
    departmentUsage: mockDepartmentUsageStats,
    timeSlot: mockTimeSlotStats,
    monthly: mockMonthlyStats
  }
}
