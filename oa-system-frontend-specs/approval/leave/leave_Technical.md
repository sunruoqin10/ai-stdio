# 请假管理模块 - 技术实现规范

> **文档类型**: 技术实现规范 (Technical Specification)
> **模块类型**: 审批流程
> **创建日期**: 2026-01-09
> **版本**: v1.0.0

---

## 1. 数据结构设计

### 1.1 数据库表设计

#### 1.1.1 请假申请表 (leave_requests)

```sql
CREATE TABLE leave_requests (
  id VARCHAR(50) PRIMARY KEY COMMENT '编号:LEAVE+YYYYMMDD+序号',
  applicant_id VARCHAR(50) NOT NULL COMMENT '申请人ID',
  department_id VARCHAR(50) NOT NULL COMMENT '部门ID',
  type ENUM('annual', 'sick', 'personal', 'comp_time', 'marriage', 'maternity') NOT NULL COMMENT '请假类型',
  start_time DATETIME NOT NULL COMMENT '开始时间',
  end_time DATETIME NOT NULL COMMENT '结束时间',
  duration DECIMAL(4,1) NOT NULL COMMENT '请假时长(天)',
  reason TEXT NOT NULL COMMENT '请假事由',
  attachments JSON COMMENT '附件URL数组',
  status ENUM('draft', 'pending', 'approving', 'approved', 'rejected', 'cancelled') NOT NULL DEFAULT 'draft' COMMENT '状态',
  current_approval_level INT DEFAULT 0 COMMENT '当前审批层级',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

  INDEX idx_applicant (applicant_id),
  INDEX idx_department (department_id),
  INDEX idx_status (status),
  INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='请假申请表';
```

#### 1.1.2 审批记录表 (leave_approvals)

```sql
CREATE TABLE leave_approvals (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  request_id VARCHAR(50) NOT NULL COMMENT '申请ID',
  approver_id VARCHAR(50) NOT NULL COMMENT '审批人ID',
  approver_name VARCHAR(100) NOT NULL COMMENT '审批人姓名',
  approval_level INT NOT NULL COMMENT '审批层级(1/2/3)',
  status ENUM('pending', 'approved', 'rejected') NOT NULL DEFAULT 'pending' COMMENT '审批状态',
  opinion TEXT COMMENT '审批意见',
  timestamp TIMESTAMP NULL COMMENT '审批时间',

  UNIQUE KEY uk_request_approver (request_id, approver_id),
  INDEX idx_approver (approver_id),
  INDEX idx_status (status),
  FOREIGN KEY (request_id) REFERENCES leave_requests(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批记录表';
```

#### 1.1.3 年假余额表 (leave_balances)

```sql
CREATE TABLE leave_balances (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  employee_id VARCHAR(50) NOT NULL UNIQUE COMMENT '员工ID',
  year INT NOT NULL COMMENT '年份',
  annual_total DECIMAL(4,1) NOT NULL DEFAULT 0 COMMENT '年假总额(天)',
  annual_used DECIMAL(4,1) NOT NULL DEFAULT 0 COMMENT '已使用(天)',
  annual_remaining DECIMAL(4,1) NOT NULL DEFAULT 0 COMMENT '剩余(天)',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

  UNIQUE KEY uk_employee_year (employee_id, year),
  INDEX idx_year (year)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='年假余额表';
```

#### 1.1.4 年假使用记录表 (leave_usage_logs)

```sql
CREATE TABLE leave_usage_logs (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  employee_id VARCHAR(50) NOT NULL COMMENT '员工ID',
  request_id VARCHAR(50) NOT NULL COMMENT '申请ID',
  type ENUM('annual', 'sick', 'personal', 'comp_time', 'marriage', 'maternity') NOT NULL COMMENT '请假类型',
  duration DECIMAL(4,1) NOT NULL COMMENT '请假时长(天)',
  start_time DATETIME NOT NULL COMMENT '开始时间',
  end_time DATETIME NOT NULL COMMENT '结束时间',
  change_type ENUM('deduct', 'rollback') NOT NULL COMMENT '变动类型:扣减/回退',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '记录时间',

  INDEX idx_employee (employee_id),
  INDEX idx_request (request_id),
  INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='年假使用记录表';
```

#### 1.1.5 节假日表 (holidays)

```sql
CREATE TABLE holidays (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  date DATE NOT NULL UNIQUE COMMENT '日期',
  name VARCHAR(100) NOT NULL COMMENT '节假日名称',
  type ENUM('national', 'company') NOT NULL DEFAULT 'national' COMMENT '类型:国家/公司',
  year INT NOT NULL COMMENT '年份',

  INDEX idx_year (year)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='节假日表';
```

### 1.2 TypeScript 接口定义

```typescript
/**
 * 请假申请
 */
interface LeaveRequest {
  id: string                        // 编号: LEAVE+YYYYMMDD+序号
  applicantId: string               // 申请人ID
  departmentId: string              // 部门ID
  type: LeaveType                   // 请假类型
  startTime: string                 // 开始时间 ISO 8601
  endTime: string                   // 结束时间 ISO 8601
  duration: number                  // 请假时长(天),支持0.5
  reason: string                    // 请假事由
  attachments?: string[]            // 附件URL数组
  status: LeaveStatus               // 状态
  currentApprovalLevel: number      // 当前审批层级(0/1/2/3)
  approvers?: ApprovalRecord[]      // 审批记录(查询时包含)
  createdAt: string                 // 创建时间
  updatedAt: string                 // 更新时间
}

/**
 * 请假类型
 */
type LeaveType = 'annual' | 'sick' | 'personal' | 'comp_time' | 'marriage' | 'maternity'

/**
 * 请假状态
 */
type LeaveStatus = 'draft' | 'pending' | 'approving' | 'approved' | 'rejected' | 'cancelled'

/**
 * 审批记录
 */
interface ApprovalRecord {
  id?: number                       // 记录ID
  requestId: string                 // 申请ID
  approverId: string                // 审批人ID
  approverName: string              // 审批人姓名
  approvalLevel: number             // 审批层级(1/2/3)
  status: 'pending' | 'approved' | 'rejected'
  opinion?: string                  // 审批意见
  timestamp?: string                // 审批时间
}

/**
 * 年假余额
 */
interface LeaveBalance {
  id?: number
  employeeId: string
  year: number
  annualTotal: number               // 年假总额(天)
  annualUsed: number                // 已使用(天)
  annualRemaining: number           // 剩余(天)
  createdAt?: string
  updatedAt?: string
}

/**
 * 节假日
 */
interface Holiday {
  id?: number
  date: string                      // YYYY-MM-DD
  name: string                      // 节假日名称
  type: 'national' | 'company'
  year: number
}

/**
 * 请假统计
 */
interface LeaveStatistics {
  total: number                     // 总请假次数
  totalDuration: number             // 总请假天数
  byType: Record<LeaveType, number> // 按类型统计
  byStatus: Record<LeaveStatus, number> // 按状态统计
  monthlyData: {                    // 月度数据
    month: string
    count: number
    duration: number
  }[]
}
```

---

## 2. API 接口设计

### 2.1 请假申请接口

#### 2.1.1 创建请假申请
```http
POST /api/leave/requests
Content-Type: application/json

{
  "type": "annual",
  "startTime": "2026-01-10T09:00:00Z",
  "endTime": "2026-01-12T18:00:00Z",
  "reason": "家中有事",
  "attachments": ["https://example.com/file1.pdf"]
}

Response 201:
{
  "code": 0,
  "message": "创建成功",
  "data": {
    "id": "LEAVE20260110001",
    "applicantId": "EMP001",
    "departmentId": "DEPT001",
    "type": "annual",
    "startTime": "2026-01-10T09:00:00Z",
    "endTime": "2026-01-12T18:00:00Z",
    "duration": 3,
    "reason": "家中有事",
    "attachments": ["https://example.com/file1.pdf"],
    "status": "draft",
    "currentApprovalLevel": 0,
    "createdAt": "2026-01-09T10:00:00Z",
    "updatedAt": "2026-01-09T10:00:00Z"
  }
}
```

#### 2.1.2 提交请假申请
```http
POST /api/leave/requests/{id}/submit

Response 200:
{
  "code": 0,
  "message": "提交成功",
  "data": {
    "id": "LEAVE20260110001",
    "status": "pending",
    "approvers": [
      {
        "approverId": "USER002",
        "approverName": "张经理",
        "approvalLevel": 1,
        "status": "pending"
      }
    ]
  }
}

Error 400:
{
  "code": 400,
  "message": "年假余额不足",
  "data": {
    "balance": 2,
    "required": 3
  }
}
```

#### 2.1.3 撤销请假申请
```http
POST /api/leave/requests/{id}/cancel

Response 200:
{
  "code": 0,
  "message": "撤销成功",
  "data": {
    "id": "LEAVE20260110001",
    "status": "cancelled"
  }
}

Error 400:
{
  "code": 400,
  "message": "当前状态不允许撤销"
}
```

#### 2.1.4 获取我的请假列表
```http
GET /api/leave/my-requests?status=pending&page=1&pageSize=10

Response 200:
{
  "code": 0,
  "message": "success",
  "data": {
    "total": 25,
    "list": [
      {
        "id": "LEAVE20260110001",
        "type": "annual",
        "startTime": "2026-01-10T09:00:00Z",
        "endTime": "2026-01-12T18:00:00Z",
        "duration": 3,
        "status": "pending",
        "createdAt": "2026-01-09T10:00:00Z"
      }
    ]
  }
}
```

#### 2.1.5 获取请假详情
```http
GET /api/leave/requests/{id}

Response 200:
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "LEAVE20260110001",
    "applicantId": "EMP001",
    "applicantName": "张三",
    "departmentId": "DEPT001",
    "departmentName": "技术部",
    "type": "annual",
    "startTime": "2026-01-10T09:00:00Z",
    "endTime": "2026-01-12T18:00:00Z",
    "duration": 3,
    "reason": "家中有事",
    "attachments": ["https://example.com/file1.pdf"],
    "status": "approving",
    "currentApprovalLevel": 2,
    "approvers": [
      {
        "approverId": "USER002",
        "approverName": "张经理",
        "approvalLevel": 1,
        "status": "approved",
        "opinion": "同意",
        "timestamp": "2026-01-09T11:00:00Z"
      },
      {
        "approverId": "USER003",
        "approverName": "人事小李",
        "approvalLevel": 2,
        "status": "pending"
      }
    ],
    "createdAt": "2026-01-09T10:00:00Z",
    "updatedAt": "2026-01-09T11:00:00Z"
  }
}
```

### 2.2 审批管理接口

#### 2.2.1 获取待审批列表
```http
GET /api/leave/pending-approvals?page=1&pageSize=10

Response 200:
{
  "code": 0,
  "message": "success",
  "data": {
    "total": 8,
    "list": [
      {
        "id": "LEAVE20260110001",
        "applicantName": "张三",
        "departmentName": "技术部",
        "type": "annual",
        "duration": 5,
        "reason": "家中有事",
        "createdAt": "2026-01-09T10:00:00Z"
      }
    ]
  }
}
```

#### 2.2.2 审批操作
```http
POST /api/leave/requests/{id}/approve
Content-Type: application/json

{
  "action": "approve",  // "approve" | "reject"
  "opinion": "同意申请"
}

Response 200:
{
  "code": 0,
  "message": "审批成功",
  "data": {
    "requestId": "LEAVE20260110001",
    "status": "approved",
    "approver": {
      "approverId": "USER002",
      "approverName": "张经理",
      "status": "approved",
      "opinion": "同意申请",
      "timestamp": "2026-01-09T11:00:00Z"
    }
  }
}

Error 400:
{
  "code": 400,
  "message": "无权审批或已审批"
}
```

### 2.3 年假管理接口

#### 2.3.1 获取年假余额
```http
GET /api/leave/balance?year=2026

Response 200:
{
  "code": 0,
  "message": "success",
  "data": {
    "employeeId": "EMP001",
    "year": 2026,
    "annualTotal": 5,
    "annualUsed": 2,
    "annualRemaining": 3
  }
}
```

#### 2.3.2 获取年假使用记录
```http
GET /api/leave/usage-logs?year=2026&page=1&pageSize=10

Response 200:
{
  "code": 0,
  "message": "success",
  "data": {
    "total": 5,
    "list": [
      {
        "requestId": "LEAVE20260105001",
        "type": "annual",
        "duration": 2,
        "startTime": "2026-01-10T09:00:00Z",
        "endTime": "2026-01-11T18:00:00Z",
        "changeType": "deduct",
        "createdAt": "2026-01-05T10:00:00Z"
      }
    ]
  }
}
```

### 2.4 统计报表接口

#### 2.4.1 获取个人请假统计
```http
GET /api/leave/statistics/my?year=2026

Response 200:
{
  "code": 0,
  "message": "success",
  "data": {
    "total": 8,
    "totalDuration": 15,
    "byType": {
      "annual": 5,
      "sick": 2,
      "personal": 1
    },
    "byStatus": {
      "approved": 6,
      "pending": 1,
      "rejected": 1
    },
    "monthlyData": [
      { "month": "2026-01", "count": 2, "duration": 3 },
      { "month": "2026-02", "count": 1, "duration": 2 }
    ]
  }
}
```

#### 2.4.2 获取部门请假统计
```http
GET /api/leave/statistics/department?departmentId=DEPT001&year=2026

Response 200:
{
  "code": 0,
  "message": "success",
  "data": {
    "departmentId": "DEPT001",
    "departmentName": "技术部",
    "total": 45,
    "totalDuration": 120,
    "byType": {
      "annual": 30,
      "sick": 10,
      "personal": 5
    },
    "employeeCount": 15,
    "avgDuration": 8
  }
}
```

### 2.5 计算接口

#### 2.5.1 计算请假时长
```http
POST /api/leave/calculate-duration
Content-Type: application/json

{
  "startTime": "2026-01-06T09:00:00Z",
  "endTime": "2026-01-12T18:00:00Z"
}

Response 200:
{
  "code": 0,
  "message": "success",
  "data": {
    "duration": 5,
    "workdays": [
      "2026-01-06",
      "2026-01-07",
      "2026-01-08",
      "2026-01-09",
      "2026-01-10"
    ],
    "holidays": [
      "2026-01-11",
      "2026-01-12"
    ],
    "weekends": []
  }
}
```

#### 2.5.2 获取节假日列表
```http
GET /api/leave/holidays?year=2026

Response 200:
{
  "code": 0,
  "message": "success",
  "data": {
    "total": 11,
    "list": [
      {
        "date": "2026-01-01",
        "name": "元旦",
        "type": "national"
      },
      {
        "date": "2026-01-28",
        "name": "春节",
        "type": "national"
      }
    ]
  }
}
```

---

## 3. 验证规则

### 3.1 请假申请验证

```typescript
/**
 * 请假申请验证规则
 */
interface LeaveRequestValidation {
  // 必填字段
  type: LeaveType                    // 必须是有效的请假类型
  startTime: string                  // 必须是有效的ISO 8601日期时间
  endTime: string                    // 必须晚于startTime
  reason: string                     // 长度: 5-500字符

  // 业务规则验证
  duration: number                   // 必须 > 0, <= 365
  attachments?: string[]             // 最多5个文件,每个<10MB

  // 状态验证
  status?: LeaveStatus               // 根据当前状态流转

  // 年假专项验证
  annualBalance?: number             // type='annual'时,余额必须充足
}

/**
 * 验证函数
 */
function validateLeaveRequest(data: Partial<LeaveRequest>): ValidationResult {
  const errors: string[] = []

  // 1. 基础字段验证
  if (!data.type) {
    errors.push('请假类型不能为空')
  }

  if (!data.startTime) {
    errors.push('开始时间不能为空')
  } else if (new Date(data.startTime) < new Date()) {
    errors.push('开始时间不能早于当前时间')
  }

  if (!data.endTime) {
    errors.push('结束时间不能为空')
  } else if (data.startTime && new Date(data.endTime) <= new Date(data.startTime)) {
    errors.push('结束时间必须晚于开始时间')
  }

  if (!data.reason || data.reason.trim().length < 5) {
    errors.push('请假事由至少5个字符')
  } else if (data.reason.length > 500) {
    errors.push('请假事由不能超过500个字符')
  }

  // 2. 业务规则验证
  const duration = calculateLeaveDuration(data.startTime!, data.endTime!)
  if (duration <= 0) {
    errors.push('请假时长必须大于0')
  } else if (duration > 365) {
    errors.push('单次请假不能超过365天')
  }

  // 3. 附件验证
  if (data.attachments && data.attachments.length > 5) {
    errors.push('附件不能超过5个')
  }

  // 4. 年假余额验证
  if (data.type === 'annual' && data.annualBalance !== undefined) {
    if (data.annualBalance < duration) {
      errors.push(`年假余额不足,剩余${data.annualBalance}天,需要${duration}天`)
    }
  }

  return {
    valid: errors.length === 0,
    errors
  }
}
```

### 3.2 审批操作验证

```typescript
/**
 * 审批操作验证
 */
function validateApproval(
  request: LeaveRequest,
  approverId: string,
  action: 'approve' | 'reject'
): ValidationResult {
  const errors: string[] = []

  // 1. 状态验证
  if (request.status === 'draft') {
    errors.push('草稿状态的申请不能审批')
  } else if (request.status === 'cancelled') {
    errors.push('已撤销的申请不能审批')
  } else if (request.status === 'approved') {
    errors.push('已通过的申请不能审批')
  } else if (request.status === 'rejected') {
    errors.push('已驳回的申请不能审批')
  }

  // 2. 审批人验证
  const currentApproval = request.approvers?.find(a => a.approverId === approverId)
  if (!currentApproval) {
    errors.push('您不是该申请的审批人')
  } else if (currentApproval.status !== 'pending') {
    errors.push('您已经审批过该申请')
  }

  // 3. 审批顺序验证
  const pendingApprovers = request.approvers?.filter(a => a.status === 'pending') || []
  if (pendingApprovers.length > 0 && pendingApprovers[0].approverId !== approverId) {
    errors.push('请按审批顺序进行审批')
  }

  // 4. 驳回意见验证
  if (action === 'reject' && !currentApproval?.opinion) {
    errors.push('驳回时必须填写审批意见')
  }

  return {
    valid: errors.length === 0,
    errors
  }
}
```

---

## 4. 算法实现

### 4.1 工作日计算算法

```typescript
/**
 * 计算请假时长(工作日)
 */
async function calculateLeaveDuration(
  startTime: string,
  endTime: string,
  holidayCache?: Map<string, Holiday>
): Promise<number> {
  const start = new Date(startTime)
  const end = new Date(endTime)

  // 规范化时间,只取日期部分
  start.setHours(0, 0, 0, 0)
  end.setHours(0, 0, 0, 0)

  let workDays = 0
  let totalDays = 0
  const currentDate = new Date(start)

  while (currentDate <= end) {
    totalDays++
    const dayOfWeek = currentDate.getDay()

    // 排除周末(周六=6, 周日=0)
    if (dayOfWeek !== 0 && dayOfWeek !== 6) {
      // 检查是否为节假日
      const dateStr = currentDate.toISOString().split('T')[0]
      const isHolidayDay = holidayCache
        ? !!holidayCache.get(dateStr)
        : await isHoliday(currentDate)

      if (!isHolidayDay) {
        workDays++
      }
    }

    // 移动到下一天
    currentDate.setDate(currentDate.getDate() + 1)
  }

  // 计算小时部分(支持半天)
  const startHour = new Date(startTime).getHours()
  const endHour = new Date(endTime).getHours()

  // 如果开始时间是下午,减0.5天
  if (startHour >= 12) {
    workDays -= 0.5
  }

  // 如果结束时间是上午,减0.5天
  if (endHour <= 12) {
    workDays -= 0.5
  }

  // 确保不为负数
  return Math.max(0, workDays)
}

/**
 * 判断是否为节假日
 */
async function isHoliday(date: Date): Promise<boolean> {
  const dateStr = date.toISOString().split('T')[0]
  const year = date.getFullYear()

  // 从数据库查询
  const holiday = await db.query(
    'SELECT * FROM holidays WHERE date = ? AND year = ?',
    [dateStr, year]
  )

  return !!holiday
}

/**
 * 批量预加载节假日(优化性能)
 */
async function preloadHolidays(startDate: Date, endDate: Date): Promise<Map<string, Holiday>> {
  const holidays = await db.query(
    'SELECT * FROM holidays WHERE date BETWEEN ? AND ?',
    [
      startDate.toISOString().split('T')[0],
      endDate.toISOString().split('T')[0]
    ]
  )

  const holidayMap = new Map<string, Holiday>()
  holidays.forEach((h: Holiday) => {
    holidayMap.set(h.date, h)
  })

  return holidayMap
}
```

### 4.2 审批流程引擎

```typescript
/**
 * 审批规则配置
 */
interface ApprovalRule {
  maxDays: number                    // 最大天数
  approvers: ApprovalRole[]          // 审批人角色
}

interface ApprovalRole {
  role: string                       // 角色: department_leader | hr | general_manager
  level: number                      // 层级: 1 | 2 | 3
}

const approvalRules: ApprovalRule[] = [
  {
    maxDays: 3,
    approvers: [
      { role: 'department_leader', level: 1 }
    ]
  },
  {
    maxDays: 7,
    approvers: [
      { role: 'department_leader', level: 1 },
      { role: 'hr', level: 2 }
    ]
  },
  {
    maxDays: Infinity,
    approvers: [
      { role: 'department_leader', level: 1 },
      { role: 'hr', level: 2 },
      { role: 'general_manager', level: 3 }
    ]
  }
]

/**
 * 获取审批流程
 */
function getApprovalFlow(duration: number): ApprovalRole[] {
  for (const rule of approvalRules) {
    if (duration <= rule.maxDays) {
      return rule.approvers
    }
  }
  return approvalRules[approvalRules.length - 1].approvers
}

/**
 * 解析审批人(根据角色查找实际用户)
 */
async function resolveApprovers(
  departmentId: string,
  roles: ApprovalRole[]
): Promise<ApprovalRecord[]> {
  const approvers: ApprovalRecord[] = []

  for (const role of roles) {
    let userId: string
    let userName: string

    switch (role.role) {
      case 'department_leader':
        // 获取部门负责人
        const dept = await getDepartment(departmentId)
        userId = dept.leaderId
        userName = dept.leaderName
        break

      case 'hr':
        // 获取人事专员(可以有多个)
        const hrUsers = await getUsersByRole('hr')
        userId = hrUsers[0].id
        userName = hrUsers[0].name
        break

      case 'general_manager':
        // 获取总经理
        const gm = await getUsersByRole('general_manager')
        userId = gm[0].id
        userName = gm[0].name
        break

      default:
        throw new Error(`未知的审批角色: ${role.role}`)
    }

    approvers.push({
      approverId: userId,
      approverName: userName,
      approvalLevel: role.level,
      status: 'pending'
    })
  }

  return approvers
}

/**
 * 构建完整审批流程
 */
async function buildApprovalFlow(
  duration: number,
  departmentId: string
): Promise<ApprovalRecord[]> {
  const roles = getApprovalFlow(duration)
  const approvers = await resolveApprovers(departmentId, roles)
  return approvers
}
```

### 4.3 年假计算算法

```typescript
/**
 * 根据工龄计算年假天数
 */
function calculateAnnualLeaveByYears(joinDate: string): number {
  const join = new Date(joinDate)
  const now = new Date()
  const currentYear = now.getFullYear()

  // 计算工作年限(截至当年12月31日)
  const yearEnd = new Date(currentYear, 11, 31)
  const workYears = Math.floor((yearEnd.getTime() - join.getTime()) / (365.25 * 24 * 60 * 60 * 1000))

  // 根据工龄返回年假天数
  if (workYears < 1) return 0
  if (workYears < 10) return 5
  if (workYears < 20) return 10
  return 15
}

/**
 * 初始化年度年假
 */
async function initializeAnnualLeave(employeeId: string, year: number): Promise<void> {
  // 获取员工信息
  const employee = await getEmployee(employeeId)
  const joinDate = employee.joinDate

  // 计算年假额度
  const annualTotal = calculateAnnualLeaveByYears(joinDate)

  // 创建或更新年假余额
  await db.query(
    `INSERT INTO leave_balances (employee_id, year, annual_total, annual_used, annual_remaining)
     VALUES (?, ?, ?, 0, ?)
     ON DUPLICATE KEY UPDATE
     annual_total = VALUES(annual_total),
     annual_remaining = VALUES(annual_total) - annual_used`,
    [employeeId, year, annualTotal, annualTotal]
  )
}

/**
 * 扣减年假余额
 */
async function deductAnnualLeave(
  employeeId: string,
  year: number,
  duration: number,
  requestId: string
): Promise<void> {
  // 开启事务
  const tx = await db.beginTransaction()

  try {
    // 1. 锁定余额记录
    const balance = await tx.query(
      'SELECT * FROM leave_balances WHERE employee_id = ? AND year = ? FOR UPDATE',
      [employeeId, year]
    )

    if (!balance) {
      throw new Error('年假余额不存在')
    }

    if (balance.annualRemaining < duration) {
      throw new Error(`年假余额不足,剩余${balance.annualRemaining}天,需要${duration}天`)
    }

    // 2. 扣减余额
    await tx.query(
      `UPDATE leave_balances
       SET annual_used = annual_used + ?,
           annual_remaining = annual_remaining - ?
       WHERE employee_id = ? AND year = ?`,
      [duration, duration, employeeId, year]
    )

    // 3. 记录使用日志
    await tx.query(
      `INSERT INTO leave_usage_logs
       (employee_id, request_id, type, duration, start_time, end_time, change_type)
       VALUES (?, ?, 'annual', ?, NOW(), NOW(), 'deduct')`,
      [employeeId, requestId, duration]
    )

    // 提交事务
    await tx.commit()
  } catch (error) {
    // 回滚事务
    await tx.rollback()
    throw error
  }
}

/**
 * 回退年假余额(申请被驳回或撤销时)
 */
async function rollbackAnnualLeave(
  employeeId: string,
  year: number,
  duration: number,
  requestId: string
): Promise<void> {
  // 开启事务
  const tx = await db.beginTransaction()

  try {
    // 1. 增加余额
    await tx.query(
      `UPDATE leave_balances
       SET annual_used = annual_used - ?,
           annual_remaining = annual_remaining + ?
       WHERE employee_id = ? AND year = ?`,
      [duration, duration, employeeId, year]
    )

    // 2. 记录回退日志
    await tx.query(
      `INSERT INTO leave_usage_logs
       (employee_id, request_id, type, duration, start_time, end_time, change_type)
       VALUES (?, ?, 'annual', ?, NOW(), NOW(), 'rollback')`,
      [employeeId, requestId, duration]
    )

    // 提交事务
    await tx.commit()
  } catch (error) {
    // 回滚事务
    await tx.rollback()
    throw error
  }
}
```

### 4.4 通知算法

```typescript
/**
 * 通知审批人
 */
async function notifyApprover(approverId: string, requestId: string): Promise<void> {
  // 获取申请信息
  const request = await getLeaveRequest(requestId)
  const approver = await getUser(approverId)

  // 创建系统通知
  await createNotification({
    userId: approverId,
    type: 'leave_approval',
    title: '新的请假申请',
    content: `${request.applicantName}提交了${request.type === 'annual' ? '年假' : '请假'}申请,时长${request.duration}天`,
    link: `/leave/approval/${requestId}`,
    data: { requestId }
  })

  // 发送邮件(可选)
  if (approver.email && approver.emailNotificationEnabled) {
    await sendEmail({
      to: approver.email,
      subject: '新的请假申请待审批',
      template: 'leave_approval',
      data: {
        approverName: approver.name,
        applicantName: request.applicantName,
        leaveType: request.type,
        duration: request.duration,
        reason: request.reason,
        link: `${process.env.APP_URL}/leave/approval/${requestId}`
      }
    })
  }
}

/**
 * 通知申请人审批结果
 */
async function notifyApplicant(
  applicantId: string,
  status: 'approved' | 'rejected',
  opinion?: string
): Promise<void> {
  const applicant = await getUser(applicantId)

  // 创建系统通知
  await createNotification({
    userId: applicantId,
    type: 'leave_result',
    title: status === 'approved' ? '请假申请已通过' : '请假申请已驳回',
    content: status === 'approved'
      ? '您的请假申请已通过审批'
      : `您的请假申请已被驳回,原因: ${opinion || '无'}`,
    link: '/leave/my-requests',
    data: { status }
  })

  // 发送邮件(可选)
  if (applicant.email && applicant.emailNotificationEnabled) {
    await sendEmail({
      to: applicant.email,
      subject: status === 'approved' ? '请假申请已通过' : '请假申请已驳回',
      template: status === 'approved' ? 'leave_approved' : 'leave_rejected',
      data: {
        applicantName: applicant.name,
        opinion: opinion || '无',
        link: `${process.env.APP_URL}/leave/my-requests`
      }
    })
  }
}

/**
 * 年假余额预警
 */
async function checkAnnualLeaveBalance(employeeId: string): Promise<void> {
  const balance = await getLeaveBalance(employeeId, new Date().getFullYear())

  if (balance.annualRemaining <= 3) {
    // 创建预警通知
    await createNotification({
      userId: employeeId,
      type: 'leave_balance_warning',
      title: '年假余额不足',
      content: `您的年假余额仅剩${balance.annualRemaining}天,请合理安排`,
      link: '/leave/balance',
      data: { balance: balance.annualRemaining }
    })
  }
}
```

---

## 5. 性能优化

### 5.1 数据库优化
- 为常用查询字段添加索引(applicant_id, department_id, status, created_at)
- 使用缓存存储节假日数据
- 批量查询优化,避免N+1问题

### 5.2 缓存策略
```typescript
/**
 * 缓存节假日数据(1天)
 */
const holidayCache = new Cache({
  ttl: 24 * 60 * 60 * 1000,  // 24小时
  key: (year: number) => `holidays:${year}`
})

async function getHolidays(year: number): Promise<Holiday[]> {
  // 先从缓存读取
  const cached = await holidayCache.get(year)
  if (cached) return cached

  // 从数据库查询
  const holidays = await db.query(
    'SELECT * FROM holidays WHERE year = ?',
    [year]
  )

  // 写入缓存
  await holidayCache.set(year, holidays)

  return holidays
}
```

### 5.3 批量操作优化
- 批量加载节假日
- 批量查询审批人信息
- 批量发送通知

---

## 6. Mock数据实现

请假管理模块提供了完整的Mock数据实现,支持前端独立开发和测试。

### 6.1 Mock数据结构

**请假申请数据** (`mockLeaveRequests`):
```typescript
export const mockLeaveRequests: LeaveRequest[] = [
  {
    id: 'LEAVE20260109001',
    applicantId: 'EMP000001',
    applicantName: '张三',
    departmentId: 'DEPT001',
    departmentName: '技术部',
    type: 'annual',
    startTime: '2026-01-15T09:00:00.000Z',
    endTime: '2026-01-17T18:00:00.000Z',
    duration: 3,
    reason: '家中有事,需要请假处理',
    status: 'approved',
    currentApprovalLevel: 1,
    approvers: [
      {
        id: 1,
        requestId: 'LEAVE20260109001',
        approverId: 'EMP000005',
        approverName: '孙经理',
        approvalLevel: 1,
        status: 'approved',
        opinion: '同意',
        timestamp: '2026-01-09T10:30:00.000Z'
      }
    ],
    createdAt: '2026-01-09T09:00:00.000Z',
    updatedAt: '2026-01-09T10:30:00.000Z'
  },
  // ... 更多请假申请数据
]
```

**年假余额数据** (`mockLeaveBalances`):
```typescript
export const mockLeaveBalances: LeaveBalance[] = [
  {
    id: 1,
    employeeId: 'EMP000001',
    employeeName: '张三',
    year: 2026,
    annualTotal: 5,
    annualUsed: 2,
    annualRemaining: 3,
    createdAt: '2026-01-01T00:00:00.000Z',
    updatedAt: '2026-01-09T00:00:00.000Z'
  },
  // ... 更多年假余额数据
]
```

**节假日数据** (`mockHolidays`):
```typescript
export const mockHolidays: Holiday[] = [
  { id: 1, date: '2026-01-01', name: '元旦', type: 'national', year: 2026 },
  { id: 2, date: '2026-01-27', name: '春节', type: 'national', year: 2026 },
  // ... 更多节假日数据
]
```

### 6.2 Mock API实现

**创建请假申请**:
```typescript
export async function createLeaveRequest(data: LeaveForm): Promise<LeaveRequest> {
  await new Promise(resolve => setTimeout(resolve, 300))

  const now = new Date()
  const newRequest: LeaveRequest = {
    id: `LEAVE${now.getFullYear()}${String(now.getMonth() + 1).padStart(2, '0')}${String(now.getDate()).padStart(2, '0')}${String(Math.floor(Math.random() * 1000)).padStart(3, '0')}`,
    applicantId: 'EMP000001',
    applicantName: '张三',
    departmentId: 'DEPT001',
    departmentName: '技术部',
    type: data.type,
    startTime: data.startTime,
    endTime: data.endTime,
    duration: data.duration || 1,
    reason: data.reason,
    attachments: data.attachments,
    status: 'draft',
    currentApprovalLevel: 0,
    createdAt: now.toISOString(),
    updatedAt: now.toISOString()
  }

  mockLeaveRequests.unshift(newRequest)
  return newRequest
}
```

**提交请假申请**:
```typescript
export async function submitLeaveRequest(id: string): Promise<LeaveRequest> {
  await new Promise(resolve => setTimeout(resolve, 400))

  const request = mockLeaveRequests.find(req => req.id === id)
  if (!request) {
    throw new Error('请假申请不存在')
  }

  if (request.status !== 'draft') {
    throw new Error('只能提交草稿状态的申请')
  }

  // 检查年假余额
  if (request.type === 'annual') {
    const balance = mockLeaveBalances.find(b => b.employeeId === request.applicantId && b.year === new Date().getFullYear())
    if (balance && balance.annualRemaining < request.duration) {
      throw new Error(`年假余额不足,剩余${balance.annualRemaining}天,需要${request.duration}天`)
    }
  }

  // 更新状态
  request.status = 'pending'
  request.currentApprovalLevel = 1
  request.updatedAt = new Date().toISOString()

  // 添加审批人
  const levels = Math.min(3, Math.ceil(request.duration / 3))
  request.approvers = Array.from({ length: levels }, (_, i) => ({
    requestId: request.id,
    approverId: ['EMP000005', 'EMP000006', 'EMP000008'][i],
    approverName: ['孙经理', '人事小李', '周总'][i],
    approvalLevel: i + 1,
    status: i === 0 ? 'pending' : 'pending'
  }))

  return request
}
```

**审批请假申请**:
```typescript
export async function approveLeaveRequest(
  id: string,
  approval: ApprovalForm
): Promise<LeaveRequest> {
  await new Promise(resolve => setTimeout(resolve, 400))

  const request = mockLeaveRequests.find(req => req.id === id)
  if (!request) {
    throw new Error('请假申请不存在')
  }

  if (request.status !== 'pending' && request.status !== 'approving') {
    throw new Error('当前状态不允许审批')
  }

  const currentApprover = request.approvers?.find(
    a => a.approvalLevel === request.currentApprovalLevel && a.status === 'pending'
  )

  if (!currentApprover) {
    throw new Error('未找到待审批的记录')
  }

  // 更新审批记录
  currentApprover.status = approval.status
  currentApprover.opinion = approval.opinion
  currentApprover.timestamp = new Date().toISOString()

  // 如果通过
  if (approval.status === 'approved') {
    const totalLevels = request.approvers?.length || 0

    if (request.currentApprovalLevel >= totalLevels) {
      // 所有审批通过
      request.status = 'approved'

      // 如果是年假,扣减余额
      if (request.type === 'annual') {
        const balance = mockLeaveBalances.find(
          b => b.employeeId === request.applicantId && b.year === new Date().getFullYear()
        )
        if (balance) {
          balance.annualUsed += request.duration
          balance.annualRemaining -= request.duration
          balance.updatedAt = new Date().toISOString()
        }
      }
    } else {
      // 进入下一级审批
      request.status = 'approving'
      request.currentApprovalLevel++
    }
  } else {
    // 驳回
    request.status = 'rejected'
  }

  request.updatedAt = new Date().toISOString()

  return request
}
```

### 6.3 工具函数实现

**计算请假时长(工作日)**:
```typescript
export function calculateDuration(
  startTime: string,
  endTime: string,
  holidays: Holiday[] = []
): number {
  const start = new Date(startTime)
  const end = new Date(endTime)

  // 规范化时间,只取日期部分
  start.setHours(0, 0, 0, 0)
  end.setHours(0, 0, 0, 0)

  let workDays = 0
  const currentDate = new Date(start)
  const holidaySet = new Set(holidays.map(h => h.date))

  while (currentDate <= end) {
    const dayOfWeek = currentDate.getDay()

    // 排除周末(周六=6, 周日=0)
    if (dayOfWeek !== 0 && dayOfWeek !== 6) {
      // 检查是否为节假日
      const dateStr = currentDate.toISOString().split('T')[0]
      if (!holidaySet.has(dateStr)) {
        workDays++
      }
    }

    // 移动到下一天
    currentDate.setDate(currentDate.getDate() + 1)
  }

  // 计算小时部分(支持半天)
  const startHour = new Date(startTime).getHours()
  const endHour = new Date(endTime).getHours()

  // 如果开始时间是下午,减0.5天
  if (startHour >= 12) {
    workDays -= 0.5
  }

  // 如果结束时间是上午,减0.5天
  if (endHour <= 12) {
    workDays -= 0.5
  }

  // 确保不为负数
  return Math.max(0, workDays)
}
```

**计算年假额度**:
```typescript
export function calculateAnnualQuota(joinDate: string): number {
  const join = new Date(joinDate)
  const now = new Date()
  const currentYear = now.getFullYear()

  // 计算工作年限(截至当年12月31日)
  const yearEnd = new Date(currentYear, 11, 31)
  const workYears = Math.floor((yearEnd.getTime() - join.getTime()) / (365.25 * 24 * 60 * 60 * 1000))

  // 根据工龄返回年假天数
  if (workYears < 1) return 0
  if (workYears < 10) return 5
  if (workYears < 20) return 10
  return 15
}

export function calculateWorkYears(joinDate: string): number {
  const join = new Date(joinDate)
  const now = new Date()
  const currentYear = now.getFullYear()

  // 计算工作年限(截至当年12月31日)
  const yearEnd = new Date(currentYear, 11, 31)
  return Math.floor((yearEnd.getTime() - join.getTime()) / (365.25 * 24 * 60 * 60 * 1000))
}
```

**获取审批层级**:
```typescript
export function getApprovalLevels(duration: number): number {
  if (duration <= 3) return 1  // 部门负责人
  if (duration <= 7) return 2  // 部门负责人 + 人事
  return 3                     // 部门负责人 + 人事 + 总经理
}

export function isCurrentApprover(request: LeaveRequest, userId: string): boolean {
  if (!request.approvers) return false

  const currentApprover = request.approvers.find(
    a => a.approvalLevel === request.currentApprovalLevel
  )

  return currentApprover?.approverId === userId && currentApprover.status === 'pending'
}
```

**状态判断**:
```typescript
export function canCancel(request: LeaveRequest): boolean {
  return request.status === 'pending'
}

export function canEdit(request: LeaveRequest): boolean {
  return request.status === 'draft' || request.status === 'rejected'
}

export function canDelete(request: LeaveRequest): boolean {
  return request.status === 'draft'
}

export function canResubmit(request: LeaveRequest): boolean {
  return request.status === 'rejected'
}
```

---

**文档版本**: v1.0.0
