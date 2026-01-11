# 请假管理模块 - 功能需求规范

> **文档类型**: 功能需求规范 (Functional Specification)
> **模块类型**: 审批流程
> **复杂度**: ⭐⭐⭐⭐ (4星)
> **创建日期**: 2026-01-09
> **版本**: v1.0.0

---

## 1. 功能概述

实现员工请假申请、审批、统计的全流程管理,支持多级审批、自动计算时长、余额提醒等功能。

### 1.1 业务价值
- 规范请假流程,提高审批效率
- 自动计算工作日,减少人工错误
- 多级审批确保请假合理性
- 年假余额管理,保障员工权益
- 数据统计分析,辅助管理决策

---

## 2. 用户故事

### 2.1 普通员工
**作为**一名员工
**我想要**在线提交请假申请
**以便于**快速完成请假流程并获得审批结果

**验收标准:**
- 可以选择多种请假类型
- 可以查看年假余额
- 自动计算请假时长
- 可以随时查看审批进度
- 收到审批结果通知

### 2.2 部门负责人
**作为**部门负责人
**我想要**审批本部门员工的请假申请
**以便于**合理安排部门工作

**验收标准:**
- 查看待审批申请列表
- 查看员工请假详情和历史
- 可以通过或驳回申请
- 可以填写审批意见
- 查看本部门请假统计

### 2.3 人事专员
**作为**人事专员
**我想要**审批所有员工的请假申请
**以便于**监督全公司请假情况

**验收标准:**
- 查看待审批申请列表
- 可以审批跨部门请假
- 查看全公司请假统计报表
- 管理员工年假额度

### 2.4 总经理
**作为**总经理
**我想要**审批长期请假申请
**以便于**掌控公司人员动态

**验收标准:**
- 查看长期请假申请(>7天)
- 审批重要人员的请假
- 查看全公司请假统计

---

## 3. 功能清单

### 3.1 请假申请 (优先级: P0)
- [x] 创建请假申请
- [x] 选择请假类型
- [x] 选择开始/结束时间
- [x] 自动计算请假时长
- [x] 填写请假事由
- [ ] 上传附件(病假证明等)
- [x] 保存草稿
- [x] 提交申请
- [x] 撤销申请(待审批时)
- [x] 查看我的申请列表
- [x] 查看申请详情
- [x] 重新提交被驳回的申请

### 3.2 审批管理 (优先级: P0)
- [x] 查看待审批申请
- [x] 查看申请详情
- [x] 通过申请
- [x] 驳回申请(填写意见)
- [x] 查看审批历史
- [x] 查看已审批列表
- [ ] 批量审批

### 3.3 年假管理 (优先级: P1)
- [x] 查看年假余额
- [ ] 查看年假使用记录
- [x] 年假余额预警(<3天)
- [x] 自动计算年假额度
- [ ] 年假消耗统计

### 3.4 统计报表 (优先级: P1)
- [x] 个人请假统计
- [x] 部门请假统计
- [x] 全公司请假统计
- [x] 请假类型分布
- [x] 请假趋势分析
- [ ] 导出统计报表

### 3.5 日历视图 (优先级: P2)
- [ ] 月度请假日历
- [ ] 按人员筛选
- [ ] 按部门筛选
- [ ] 按类型筛选
- [ ] 查看每日请假详情

### 3.6 Mock数据支持

请假管理模块提供了完整的Mock数据实现,便于前端独立开发和测试:

**Mock数据结构** (`src/modules/leave/mock/data.ts`):
- **6个预置请假申请**,涵盖6种状态(draft/pending/approving/approved/rejected/cancelled)
- **4个年假余额记录**,展示不同工龄员工的年假额度
- **12个节假日数据**(2026年),包含元旦、春节、清明、劳动节、端午节
- **3个审批记录示例**,展示多级审批流程

**Mock API实现** (`src/modules/leave/api/index.ts`):
- `getMyLeaveRequests()`: 获取我的请假列表,支持状态筛选和分页
- `getLeaveRequest()`: 获取请假详情
- `createLeaveRequest()`: 创建请假申请(自动生成LEAVE编号)
- `updateLeaveRequest()`: 更新请假申请(仅草稿状态)
- `deleteLeaveRequest()`: 删除请假申请(仅草稿状态)
- `submitLeaveRequest()`: 提交请假申请,检查年假余额,生成审批流程
- `cancelLeaveRequest()`: 撤销请假申请(仅待审批状态)
- `getPendingApprovals()`: 获取待审批列表
- `getApprovedRequests()`: 获取已审批列表
- `approveLeaveRequest()`: 审批请假申请(通过/驳回),自动扣减年假
- `getLeaveBalance()`: 获取年假余额
- `getLeaveBalances()`: 获取年假余额列表
- `updateLeaveQuota()`: 更新年假额度
- `getLeaveStatistics()`: 获取请假统计
- `getHolidays()`: 获取节假日列表

### 3.7 工具函数实现

请假管理模块提供了丰富的工具函数 (`src/modules/leave/utils/index.ts`):

**日期时间格式化**:
- `formatDate(date)`: 格式化日期为 YYYY-MM-DD
- `formatDateTime(date)`: 格式化日期时间为 YYYY-MM-DD HH:mm
- `formatDateRange(startDate, endDate)`: 格式化日期范围

**类型转换**:
- `getLeaveTypeName(type)`: 获取请假类型中文名称
- `getLeaveStatusName(status)`: 获取请假状态中文名称
- `getLeaveStatusType(status)`: 获取Element Plus Tag类型
- `getApprovalStatusName(status)`: 获取审批状态中文名称

**时长计算**:
- `calculateDuration(startTime, endTime, holidays)`: 计算请假时长(工作日)
  - 只计算工作日(周一到周五)
  - 自动排除周末和节假日
  - 支持半天请假(0.5天)
- `calculateWorkDays(startDate, endDate, holidays)`: 计算工作日数量
- `getWorkDaysBetween(startDate, endDate)`: 获取两个日期之间的工作日列表

**状态判断**:
- `canCancel(request)`: 判断是否可撤销(待审批状态)
- `canEdit(request)`: 判断是否可编辑(草稿状态或已驳回状态)
- `canDelete(request)`: 判断是否可删除(草稿状态)
- `canResubmit(request)`: 判断是否可重新提交(已驳回状态)

**年假相关**:
- `calculateAnnualQuota(joinDate)`: 根据入职日期计算年假额度
  - 工龄<1年: 0天
  - 工龄1-9年: 5天
  - 工龄10-19年: 10天
  - 工龄≥20年: 15天
- `calculateWorkYears(joinDate)`: 计算工作年限

**审批流程**:
- `getApprovalLevels(duration)`: 根据请假时长获取审批层级
  - ≤3天: 1级(部门负责人)
  - 4-7天: 2级(部门负责人 + 人事)
  - >7天: 3级(部门负责人 + 人事 + 总经理)
- `isCurrentApprover(request, userId)`: 判断是否为当前审批人

---

## 4. 请假流程

### 4.1 流程状态图
```
待提交 → 待审批 → 审批中 → 已通过
  ↓        ↓         ↓
已撤销   已驳回   已驳回
```

### 4.2 流程说明

#### 4.2.1 待提交
- 员工创建请假申请,填写信息
- 可以保存为草稿
- 可以修改和删除
- 提交后进入"待审批"状态

#### 4.2.2 待审批
- 等待第一个审批人审批
- 申请人可以撤销申请
- 撤销后回到"待提交"状态

#### 4.2.3 审批中
- 正在多级审批流程中
- 显示当前审批层级
- 显示已审批的记录
- 申请人不能撤销

#### 4.2.4 已通过
- 所有审批人通过
- 如果是年假,自动扣减余额
- 不可修改
- 可以查看详情

#### 4.2.5 已驳回
- 任一审批人驳回
- 整个申请被驳回
- 申请人可以修改后重新提交
- 显示驳回意见

#### 4.2.6 已撤销
- 申请人主动撤销
- 只能在"待审批"状态撤销
- 可以重新提交

---

## 5. 业务规则

### 5.1 请假类型

| 类型 | 编码 | 说明 | 是否需要附件 | 是否扣减年假 |
|------|------|------|-------------|-------------|
| 年假 | annual | 带薪假期 | 否 | 是 |
| 病假 | sick | 因病请假 | 是(建议) | 否 |
| 事假 | personal | 私事请假 | 否 | 否 |
| 调休 | comp_time | 加班调休 | 否 | 否 |
| 婚假 | marriage | 结婚假期 | 是 | 否 |
| 产假 | maternity | 生育假期 | 是 | 否 |

### 5.2 多级审批规则

#### 5.2.1 审批层级

| 请假天数 | 审批人 | 说明 |
|---------|--------|------|
| ≤3天 | 部门负责人 | 一级审批 |
| 4-7天 | 部门负责人 + 人事 | 二级审批 |
| >7天 | 部门负责人 + 人事 + 总经理 | 三级审批 |

#### 5.2.2 审批顺序
1. 第一级: 部门负责人审批
2. 第二级: 人事专员审批
3. 第三级: 总经理审批

**规则说明:**
- 必须按顺序审批,不能跳级
- 任一级驳回,整个申请驳回
- 所有级通过,申请才通过
- 审批人不能审批自己的申请

#### 5.2.3 特殊情况
- 部门负责人请假: 由上级负责人或人事审批
- 人事请假: 由总经理审批
- 总经理请假: 无需审批(系统记录)

### 5.3 请假时长计算

#### 5.3.1 计算规则
- 只计算工作日(周一到周五)
- 排除法定节假日
- 排除公司统一假期
- 半天请假按0.5天计算
- 跨月连续请假累加计算

#### 5.3.2 时间精度
- 支持按天请假
- 支持按半天请假(上午/下午)
- 不支持按小时请假

#### 5.3.3 计算示例

**示例1: 5天连续请假**
- 开始: 2026-01-06 (周一)
- 结束: 2026-01-10 (周五)
- 时长: 5个工作日

**示例2: 跨周末请假**
- 开始: 2026-01-06 (周一)
- 结束: 2026-01-12 (周日)
- 时长: 5个工作日 (排除周末)

**示例3: 跨节假日请假**
- 开始: 2026-01-27 (周一,春节前)
- 结束: 2026-02-05 (周三,春节后)
- 时长: 3个工作日 (排除春节假期)

### 5.4 年假管理规则

#### 5.4.1 年假额度标准

| 工作年限 | 年假天数 | 说明 |
|---------|---------|------|
| <1年 | 0天 | 试用期无年假 |
| 1-9年 | 5天 | 基础年假 |
| 10-19年 | 10天 | 资深员工 |
| ≥20年 | 15天 | 长期服务 |

#### 5.4.2 年假计算
- **计算基准日**: 每年1月1日
- **计算方式**: 按员工入职日期计算工作年限
- **生效时间**: 新员工入职满1年后自动生效
- **清零时间**: 每年12月31日清零,不跨年累积

#### 5.4.3 年假使用规则
- 必须提前申请年假(建议≥1天)
- 年假可以分段使用
- 年假余额不足时不能提交申请
- 离职时未使用年假不予折现

#### 5.4.4 年假预警
- **黄色预警**: 余额≤3天,提示"年假余额不足"
- **红色预警**: 余额=0天,禁止提交年假申请

### 5.5 自动提醒规则

#### 5.5.1 提交提醒
- **触发时机**: 提交申请时
- **提醒对象**: 当前审批人
- **提醒内容**: 收到新的请假申请,请及时审批
- **提醒方式**: 系统通知 + 邮件(可选)

#### 5.5.2 审批结果提醒
- **触发时机**: 审批完成时(通过/驳回)
- **提醒对象**: 申请人
- **提醒内容**: 您的请假申请已通过/已驳回
- **提醒方式**: 系统通知 + 邮件(可选)

#### 5.5.3 余额预警
- **触发时机**: 查看年假余额时
- **提醒对象**: 申请人
- **提醒内容**: 年假余额不足X天,请合理安排
- **提醒方式**: 页面提示

#### 5.5.4 长期请假预警
- **触发时机**: 连续请假>3天时
- **提醒对象**: 申请人
- **提醒内容**: 您的请假超过3天,将需要二级审批
- **提醒方式**: 页面提示

#### 5.5.5 审批超时提醒
- **触发时机**: 申请提交超过24小时未审批
- **提醒对象**: 审批人
- **提醒内容**: 您有X条请假申请待审批,请及时处理
- **提醒方式**: 系统通知

---

### 5.6 字段级权限控制

| 字段 | 普通员工 | 部门负责人 | 人事专员 | 总经理 | 系统管理员 |
|------|---------|-----------|---------|--------|-----------|
| 基本信息(类型/时间/事由) | ✅ 查看/编辑(自己) | ✅ 查看 | ✅ 查看 | ✅ 查看 | ✅ 查看/编辑 |
| 请假时长 | ✅ 查看 | ✅ 查看 | ✅ 查看 | ✅ 查看 | ✅ 查看/编辑 |
| 请假附件 | ✅ 查看/上传(自己) | ✅ 查看 | ✅ 查看 | ✅ 查看 | ✅ 查看/编辑 |
| 年假余额 | ✅ 查看(自己) | ✅ 查看(本部门) | ✅ 查看(所有) | ✅ 查看(所有) | ✅ 查看/编辑 |
| 审批意见 | ✅ 查看 | ✅ 查看/填写 | ✅ 查看/填写 | ✅ 查看/填写 | ✅ 查看/填写 |
| 年假额度 | ❌ | ❌ | ✅ 查看/编辑 | ✅ 查看/编辑 | ✅ 查看/编辑 |

---

### 5.7 数据字典集成

#### 5.7.1 依赖的数据字典类型

请假管理模块依赖以下数据字典类型:

| 字典类型 | 字典编码 | 用途 | 是否必填 |
|---------|---------|------|---------|
| 请假类型 | `leave_type` | 请假类型分类(年假/病假/事假/调休/婚假/产假) | ✅ |
| 请假状态 | `leave_status` | 请假单状态(待提交/待审批/审批中/已通过/已驳回/已撤销) | ✅ |
| 审批状态 | `approval_status` | 审批状态(待审批/已通过/已驳回) | ✅ |
| 时间单位 | `time_unit` | 请假时间单位(天/半天) | ❌ |

#### 5.7.2 数据字典使用场景

**1. 请假类型选择**
```typescript
// 从数据字典加载请假类型选项
const leaveTypeOptions = [
  { label: '年假', value: 'annual', dictCode: 'leave_type' },
  { label: '病假', value: 'sick', dictCode: 'leave_type' },
  { label: '事假', value: 'personal', dictCode: 'leave_type' },
  { label: '调休', value: 'comp_time', dictCode: 'leave_type' },
  { label: '婚假', value: 'marriage', dictCode: 'leave_type' },
  { label: '产假', value: 'maternity', dictCode: 'leave_type' }
]
```

**2. 请假状态筛选**
```typescript
// 从数据字典加载请假状态选项
const statusOptions = [
  { label: '待提交', value: 'draft', dictCode: 'leave_status' },
  { label: '待审批', value: 'pending', dictCode: 'leave_status' },
  { label: '审批中', value: 'approving', dictCode: 'leave_status' },
  { label: '已通过', value: 'approved', dictCode: 'leave_status' },
  { label: '已驳回', value: 'rejected', dictCode: 'leave_status' },
  { label: '已撤销', value: 'cancelled', dictCode: 'leave_status' }
]
```

**3. 状态标签显示**
```typescript
// 从数据字典获取显示文本
const statusText = getDictLabel('leave_status', 'approved') // "已通过"
const leaveTypeText = getDictLabel('leave_type', 'annual') // "年假"
```

**4. 请假类型规则配置**
```typescript
// 从数据字典获取请假类型配置
const leaveTypeConfig = await getDictItem('leave_type', 'annual')
// 返回: { label: '年假', value: 'annual', needAttachment: false, deductAnnual: true }
```

#### 5.7.3 数据字典初始化要求

- **模块加载时**: 预加载请假类型、请假状态字典
- **表单编辑时**: 动态加载时间单位字典
- **筛选面板**: 使用缓存的字典数据
- **字典刷新**: 监听字典变更事件,自动更新界面显示

#### 5.7.4 数据字典缓存策略

```typescript
// 字典数据缓存管理
const dictCache = {
  // 常用字典: 启动时预加载
  preload: ['leave_type', 'leave_status'],

  // 低频字典: 按需加载
  onDemand: ['approval_status', 'time_unit'],

  // 缓存过期时间: 30分钟
  expireTime: 30 * 60 * 1000
}
```

---

### 5.8 权限管理集成

#### 5.8.1 请假管理权限定义

| 权限编码 | 权限名称 | 权限描述 | 依赖角色 |
|---------|---------|---------|---------|
| `leave:create` | 创建请假申请 | 创建新的请假申请 | 所有员工 |
| `leave:view_own` | 查看自己的请假 | 查看自己提交的请假申请 | 所有员工 |
| `leave:view_department` | 查看部门请假 | 查看本部门员工的请假申请 | 部门负责人 |
| `leave:view_all` | 查看所有请假 | 查看公司所有请假申请 | 人事专员/总经理/管理员 |
| `leave:edit_own` | 编辑自己的请假 | 编辑草稿状态的请假申请 | 所有员工 |
| `leave:delete_own` | 删除自己的请假 | 删除草稿状态的请假申请 | 所有员工 |
| `leave:cancel_own` | 撤销请假申请 | 撤销待审批的请假申请 | 所有员工 |
| `leave:dept_approve` | 部门审批 | 部门负责人审批请假 | 部门负责人/管理员 |
| `leave:hr_approve` | 人事审批 | 人事专员审批请假 | 人事专员/管理员 |
| `leave:gm_approve` | 总经理审批 | 总经理审批长期请假 | 总经理/管理员 |
| `leave:view_balance` | 查看年假余额 | 查看年假余额 | 所有员工(自己)/人事(所有) |
| `leave:manage_balance` | 管理年假额度 | 管理员工年假额度 | 人事专员/管理员 |
| `leave:view_statistics` | 查看统计报表 | 查看请假统计数据 | 部门负责人/人事专员/总经理/管理员 |
| `leave:export` | 导出数据 | 导出请假数据 | 人事专员/管理员 |

#### 5.8.2 功能权限矩阵

| 功能 | 普通员工 | 部门负责人 | 人事专员 | 总经理 | 系统管理员 |
|------|---------|-----------|---------|--------|-----------|
| 创建请假申请 | ✅ leave:create | ✅ leave:create | ✅ leave:create | ✅ leave:create | ✅ leave:create |
| 查看自己请假 | ✅ leave:view_own | ✅ leave:view_own | ✅ leave:view_own | ✅ leave:view_own | ✅ leave:view_own |
| 编辑草稿请假 | ✅ leave:edit_own | ✅ leave:edit_own | ✅ leave:edit_own | ✅ leave:edit_own | ✅ leave:edit_own |
| 删除草稿请假 | ✅ leave:delete_own | ✅ leave:delete_own | ✅ leave:delete_own | ✅ leave:delete_own | ✅ leave:delete_own |
| 撤销待审批请假 | ✅ leave:cancel_own | ✅ leave:cancel_own | ✅ leave:cancel_own | ✅ leave:cancel_own | ✅ leave:cancel_own |
| 查看部门请假 | ❌ | ✅ leave:view_department | ❌ | ❌ | ✅ leave:view_department |
| 查看所有请假 | ❌ | ❌ | ✅ leave:view_all | ✅ leave:view_all | ✅ leave:view_all |
| 部门审批 | ❌ | ✅ leave:dept_approve | ❌ | ❌ | ✅ leave:dept_approve |
| 人事审批 | ❌ | ❌ | ✅ leave:hr_approve | ❌ | ✅ leave:hr_approve |
| 总经理审批 | ❌ | ❌ | ❌ | ✅ leave:gm_approve | ✅ leave:gm_approve |
| 查看自己年假余额 | ✅ leave:view_balance | ✅ leave:view_balance | ✅ leave:view_balance | ✅ leave:view_balance | ✅ leave:view_balance |
| 查看部门年假余额 | ❌ | ✅ leave:view_balance | ✅ leave:view_balance | ✅ leave:view_balance | ✅ leave:view_balance |
| 查看所有年假余额 | ❌ | ❌ | ✅ leave:view_balance | ✅ leave:view_balance | ✅ leave:view_balance |
| 管理年假额度 | ❌ | ❌ | ✅ leave:manage_balance | ✅ leave:manage_balance | ✅ leave:manage_balance |
| 查看统计报表 | ❌ | ✅ leave:view_statistics | ✅ leave:view_statistics | ✅ leave:view_statistics | ✅ leave:view_statistics |
| 导出数据 | ❌ | ❌ | ✅ leave:export | ✅ leave:export | ✅ leave:export |

#### 5.8.3 权限检查实现

```typescript
// 权限检查函数
function checkPermission(permission: string): boolean {
  const authStore = useAuthStore()
  return authStore.hasPermission(permission)
}

// 使用示例
const canCreate = computed(() => checkPermission('leave:create'))
const canDeptApprove = computed(() => checkPermission('leave:dept_approve'))
const canHrApprove = computed(() => checkPermission('leave:hr_approve'))

// 数据权限过滤
const filteredLeaves = computed(() => {
  if (checkPermission('leave:view_all')) {
    return leaveList.value // 返回所有请假申请
  } else if (checkPermission('leave:view_department')) {
    // 只返回本部门的请假申请
    return leaveList.value.filter(l => l.departmentId === currentUser.departmentId)
  } else {
    // 只返回自己的请假申请
    return leaveList.value.filter(l => l.applicantId === currentUser.id)
  }
})
```

#### 5.8.4 按钮级权限控制

```vue
<!-- 根据权限显示/隐藏按钮 -->
<el-button
  v-if="hasPermission('leave:create')"
  @click="handleCreate"
>
  新增请假
</el-button>

<el-button
  v-if="hasPermission('leave:dept_approve') && row.status === 'pending' && row.approvalLevel === 1"
  type="primary"
  @click="handleDeptApprove(row)"
>
  部门审批
</el-button>

<el-button
  v-if="hasPermission('leave:hr_approve') && row.status === 'approving' && row.approvalLevel === 2"
  type="success"
  @click="handleHrApprove(row)"
>
  人事审批
</el-button>

<el-button
  v-if="hasPermission('leave:gm_approve') && row.status === 'approving' && row.approvalLevel === 3"
  type="warning"
  @click="handleGmApprove(row)"
>
  总经理审批
</el-button>

<el-button
  v-if="hasPermission('leave:cancel_own') && row.status === 'pending' && row.applicantId === currentUser.id"
  type="danger"
  @click="handleCancel(row)"
>
  撤销申请
</el-button>
```

#### 5.8.5 字段级权限控制

```typescript
// 敏感字段权限判断
const fieldPermissions = {
  annualBalance: {
    visible: computed(() => {
      // 自己的年假余额
      if (leave.value.applicantId === currentUser.id) {
        return checkPermission('leave:view_balance')
      }
      // 部门员工的年假余额
      if (leave.value.departmentId === currentUser.departmentId) {
        return checkPermission('leave:view_department')
      }
      // 所有员工的年假余额
      return checkPermission('leave:view_all')
    })
  },
  annualQuota: {
    editable: computed(() => checkPermission('leave:manage_balance'))
  }
}

// 表单中使用
const showAnnualBalance = computed(() => {
  return fieldPermissions.annualBalance.visible.value
})

const canEditAnnualQuota = computed(() => {
  return checkPermission('leave:manage_balance')
})
```

---

## 6. 功能优先级

### 6.1 P0 - 核心功能 (必须实现)
- 请假申请创建和提交
- 多级审批流程
- 请假时长自动计算
- 审批通过/驳回
- 申请列表和详情查看

### 6.2 P1 - 重要功能 (优先实现)
- 年假余额管理
- 年假额度自动计算
- 请假统计报表
- 年假余额预警
- 审批超时提醒

### 6.3 P2 - 辅助功能 (后续实现)
- 日历视图
- 批量审批
- 导出功能
- 移动端适配

---

## 7. 非功能需求

### 7.1 性能要求
- 页面加载时间 < 2秒
- 审批操作响应时间 < 1秒
- 支持至少100个并发用户

### 7.2 安全要求
- 审批操作需要登录验证
- 审批权限需要角色验证
- 防止越权审批
- 审批记录不可删除,只能作废

### 7.3 可用性要求
- 界面简洁直观
- 操作流程清晰
- 错误提示友好
- 支持移动端访问

---

**文档版本**: v1.0.0
