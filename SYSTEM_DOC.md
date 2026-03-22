# OA System 功能文档

## 目录

- [系统概述](#系统概述)
- [后端模块](#后端模块)
  - [认证模块 (Auth)](#认证模块-auth)
  - [员工模块 (Employee)](#员工模块-employee)
  - [部门模块 (Department)](#部门模块-department)
  - [请假模块 (Leave)](#请假模块-leave)
  - [报销模块 (Expense)](#报销模块-expense)
  - [资产模块 (Asset)](#资产模块-asset)
  - [字典模块 (Dict)](#字典模块-dict)
- [前端模块](#前端模块)
  - [认证模块](#认证模块)
  - [员工模块](#员工模块)
  - [部门模块](#部门模块)
  - [请假模块](#请假模块)
  - [报销模块](#报销模块)
  - [资产模块](#资产模块)
  - [字典模块](#字典模块)
  - [会议模块](#会议模块)
  - [菜单模块](#菜单模块)
  - [权限模块](#权限模块)
- [工作流程](#工作流程)
- [技术架构](#技术架构)

---

## 系统概述

OA System 是一个完整的办公自动化系统，包含前后端分离的架构：

| 层级 | 技术栈 |
|------|--------|
| 后端 | Spring Boot 3 + Java 17 + MyBatis Plus 3.5.9 |
| 前端 | Vue 3 + TypeScript + Vite + Pinia + Element Plus |
| 数据库 | MySQL |
| 安全 | JWT Bearer Token + BCrypt |

---

## 后端模块

### 认证模块 (Auth)

**路径**: `module/auth/`

**核心功能**:
- 用户登录/登出
- JWT Token 管理（访问令牌 + 刷新令牌）
- 图形验证码
- 邮箱/手机验证码
- 密码重置
- 会话管理
- 登录日志

**关键实体**:

| 实体 | 用途 |
|------|------|
| AuthUser | 用户账户（用户名、密码、状态、失败登录次数） |
| AuthUserSession | 活跃会话管理 |
| AuthLoginLog | 登录历史追踪 |
| AuthVerificationCode | 邮箱/手机验证码 |

**API 接口**:

| 方法 | 端点 | 描述 |
|------|------|------|
| POST | `/api/auth/login` | 用户登录 |
| POST | `/api/auth/logout` | 登出 |
| POST | `/api/auth/refresh` | 刷新令牌 |
| GET | `/api/auth/captcha` | 获取验证码 |
| POST | `/api/auth/send-code` | 发送验证码 |
| POST | `/api/auth/reset-password` | 重置密码 |
| GET | `/api/auth/sessions` | 活跃会话列表 |
| DELETE | `/api/auth/sessions/{id}` | 注销会话 |
| GET | `/api/auth/login-logs` | 登录日志 |

---

### 员工模块 (Employee)

**路径**: `module/employee/`

**核心功能**:
- 员工 CRUD 操作
- 员工状态管理（在职/离职/停职）
- 员工统计
- 试用期提醒
- 工龄自动更新
- 生日提醒

**关键实体**: `Employee`

| 字段 | 说明 |
|------|------|
| id | 格式: `EMP+YYYYMMDD+序号` |
| name, englishName | 姓名 |
| departmentId | 部门 ID |
| position, level | 职位和级别 |
| managerId | 直属主管 |
| joinDate | 入职日期 |
| probationEndDate | 试用期结束 |
| status | active/resigned/suspended |

**API 接口**:

| 方法 | 端点 | 描述 |
|------|------|------|
| GET | `/api/employees` | 员工列表（分页+筛选） |
| GET | `/api/employees/{id}` | 员工详情 |
| POST | `/api/employees` | 创建员工 |
| PUT | `/api/employees/{id}` | 更新员工 |
| PUT | `/api/employees/{id}/status` | 更新状态 |
| DELETE | `/api/employees/{id}` | 删除员工 |
| GET | `/api/employees/statistics` | 员工统计 |
| GET | `/api/employees/{id}/logs` | 操作日志 |

**定时任务**:
- `WorkYearUpdateTask` - 自动更新工龄
- `ProbationReminderTask` - 试用期提醒
- `BirthdayReminderTask` - 生日提醒

---

### 部门模块 (Department)

**路径**: `module/department/`

**核心功能**:
- 部门树形结构管理
- 多层级部门支持（1-5级）
- 部门移动
- 部门成员查看
- 部门统计

**关键实体**: `Department`

| 字段 | 说明 |
|------|------|
| id | 格式: `DEPT+4位序号` |
| name, shortName | 部门名称 |
| parentId | 上级部门 |
| leaderId | 部门负责人 |
| level | 层级（1-5） |
| sort | 排序 |

**API 接口**:

| 方法 | 端点 | 描述 |
|------|------|------|
| GET | `/api/departments` | 部门列表 |
| GET | `/api/departments/tree` | 完整树结构 |
| GET | `/api/departments/roots` | 根部门 |
| GET | `/api/departments/{parentId}/children` | 子部门 |
| POST | `/api/departments` | 创建部门 |
| PUT | `/api/departments/{id}` | 更新部门 |
| PUT | `/api/departments/{id}/move` | 移动部门 |
| DELETE | `/api/departments/{id}` | 删除部门 |
| GET | `/api/departments/{id}/members` | 部门成员 |
| GET | `/api/departments/statistics` | 部门统计 |

---

### 请假模块 (Leave)

**路径**: `module/leave/`

**核心功能**:
- 请假申请（草稿/提交/审批）
- 多级审批流程
- 年假余额管理
- 节假日管理
- 请假统计

**请假类型**: `annual` | `sick` | `personal` | `comp_time` | `marriage` | `maternity`

**请假状态**: `draft` | `pending` | `approving` | `approved` | `rejected` | `cancelled`

**关键实体**:

| 实体 | 用途 |
|------|------|
| LeaveRequest | 请假申请 |
| LeaveBalance | 年假余额（总额/已用/剩余） |
| LeaveApproval | 审批记录 |
| Holiday | 节假日 |

**API 接口**:

*请假申请* (`/api/leave/requests`):

| 方法 | 端点 | 描述 |
|------|------|------|
| GET | `/api/leave/requests` | 请假列表 |
| GET | `/api/leave/requests/{id}` | 请假详情 |
| POST | `/api/leave/requests` | 创建请假 |
| PUT | `/api/leave/requests/{id}` | 更新请假 |
| POST | `/api/leave/requests/{id}/submit` | 提交审批 |
| POST | `/api/leave/requests/{id}/cancel` | 取消请假 |
| POST | `/api/leave/requests/{id}/resubmit` | 重新提交 |
| DELETE | `/api/leave/requests/{id}` | 删除草稿 |

*审批* (`/api/leave/approvals`):

| 方法 | 端点 | 描述 |
|------|------|------|
| GET | `/api/leave/approvals/pending` | 待审批列表 |
| GET | `/api/leave/approvals/approved` | 已审批列表 |
| POST | `/api/leave/approvals/{requestId}/approve` | 审批 |
| GET | `/api/leave/approvals/{requestId}/history` | 审批历史 |

*余额* (`/api/leave/balance`):

| 方法 | 端点 | 描述 |
|------|------|------|
| GET | `/api/leave/balance` | 查询余额 |
| PUT | `/api/leave/balance` | 更新余额 |
| GET | `/api/leave/balance/statistics` | 余额统计 |

*节假日* (`/api/leave/holidays`):

| 方法 | 端点 | 描述 |
|------|------|------|
| GET | `/api/leave/holidays` | 节假日列表 |
| POST | `/api/leave/holidays` | 创建节假日 |
| PUT | `/api/leave/holidays/{id}` | 更新节假日 |
| DELETE | `/api/leave/holidays/{id}` | 删除节假日 |

---

### 报销模块 (Expense)

**路径**: `module/expense/`

**核心功能**:
- 报销申请（多行项目）
- 二级审批流程（部门+财务）
- 发票 OCR 识别
- 发票验证
- 付款管理
- 统计分析

**报销类型**: `travel` | `hospitality` | `office` | `transport` | `other`

**报销状态**: `draft` | `dept_pending` | `finance_pending` | `paid` | `completed` | `rejected`

**审批阈值**:

| 类型 | 阈值 |
|------|------|
| travel | 5000 |
| hospitality | 2000 |
| office | 1000 |

**关键实体**:

| 实体 | 用途 |
|------|------|
| Expense | 报销单主表 |
| ExpenseItem | 报销明细行 |
| ExpenseInvoice | 发票附件 |
| ExpensePayment | 付款记录 |

**API 接口**:

| 方法 | 端点 | 描述 |
|------|------|------|
| GET | `/api/expense` | 报销列表 |
| GET | `/api/expense/{id}` | 报销详情 |
| POST | `/api/expense` | 创建报销 |
| PUT | `/api/expense/{id}` | 更新报销 |
| DELETE | `/api/expense/{id}` | 删除报销 |
| POST | `/api/expense/{id}/submit` | 提交审批 |
| POST | `/api/expense/{id}/dept-approval` | 部门审批 |
| POST | `/api/expense/{id}/finance-approval` | 财务审批 |
| POST | `/api/expense/{id}/payment` | 创建付款 |
| GET | `/api/expense/pending/dept` | 待部门审批 |
| GET | `/api/expense/pending/finance` | 待财务审批 |
| POST | `/api/expense/invoices/ocr` | 发票 OCR |
| GET | `/api/expense/stats/department` | 部门统计 |
| GET | `/api/expense/stats/type` | 类型统计 |
| GET | `/api/expense/stats/monthly` | 月度统计 |

---

### 资产模块 (Asset)

**路径**: `module/asset/`

**核心功能**:
- 资产 CRUD
- 资产借用/归还
- 折旧计算
- 维护记录
- 资产统计

**资产类别**: `electronic` | `furniture` | `book` | `other`

**资产状态**: `stock` | `in_use` | `borrowed` | `maintenance` | `scrapped`

**借用状态**: `active` | `returned` | `overdue`

**关键实体**:

| 实体 | 用途 |
|------|------|
| Asset | 资产记录 |
| AssetBorrowRecord | 借用记录 |
| AssetMaintenance | 维护记录 |

**API 接口**:

| 方法 | 端点 | 描述 |
|------|------|------|
| GET | `/api/assets` | 资产列表 |
| GET | `/api/assets/{id}` | 资产详情 |
| POST | `/api/assets` | 创建资产 |
| PUT | `/api/assets/{id}` | 更新资产 |
| DELETE | `/api/assets/{id}` | 删除资产 |
| GET | `/api/assets/statistics` | 资产统计 |
| POST | `/api/assets/{id}/borrow` | 借用资产 |
| POST | `/api/assets/{id}/return` | 归还资产 |
| GET | `/api/assets/{id}/borrow-history` | 借用历史 |

---

### 字典模块 (Dict)

**路径**: `module/dict/`

**核心功能**:
- 字典类型管理
- 字典项管理
- 字典树结构
- 缓存管理

**字典分类**: `system` | `business`

**关键实体**:

| 实体 | 用途 |
|------|------|
| DictType | 字典类型 |
| DictItem | 字典项 |

**API 接口**:

*字典类型* (`/api/dict/types`):

| 方法 | 端点 | 描述 |
|------|------|------|
| GET | `/api/dict/types` | 类型列表 |
| GET | `/api/dict/types/{id}` | 类型详情 |
| POST | `/api/dict/types` | 创建类型 |
| PUT | `/api/dict/types/{id}` | 更新类型 |
| DELETE | `/api/dict/types/{id}` | 删除类型 |

*字典项* (`/api/dict/items`):

| 方法 | 端点 | 描述 |
|------|------|------|
| GET | `/api/dict/items` | 项列表 |
| POST | `/api/dict/items` | 创建项 |
| PUT | `/api/dict/items/{id}` | 更新项 |
| DELETE | `/api/dict/items/{id}` | 删除项 |
| DELETE | `/api/dict/items/batch` | 批量删除 |
| PUT | `/api/dict/items/sort` | 批量排序 |

*数据查询*:

| 方法 | 端点 | 描述 |
|------|------|------|
| GET | `/api/dict/tree` | 字典树 |
| GET | `/api/dict/{code}` | 按编码获取 |
| DELETE | `/api/dict/cache/{code}` | 清除缓存 |

---

## 前端模块

### 认证模块

**路径**: `src/modules/auth/`

**页面**: Login, ResetPassword

**功能**:
- 登录（含验证码）
- JWT 令牌管理
- 密码重置

---

### 员工模块

**路径**: `src/modules/employee/`

**页面**: EmployeeList, EmployeeDetail

**组件**: EmployeeForm, FilterPanel, StatisticsPanel

**功能**:
- 员工列表（分页+筛选）
- 员工详情
- 员工统计面板

---

### 部门模块

**路径**: `src/modules/department/`

**页面**: Department index

**组件**: DepartmentForm, DepartmentFilter, DepartmentDetail, DepartmentChart, TreeNode

**功能**:
- 树形视图
- 列表视图
- 可视化图表
- 部门移动

---

### 请假模块

**路径**: `src/modules/leave/`

**页面**: Leave index

**组件**: MyRequests, LeaveRequestForm, LeaveDetail, ApprovalManagement

**功能**:
- 统计卡片（我的申请/待审批/已批准/年假余额）
- Tab 切换（我的申请 / 待审批）
- 请假申请表单
- 多级审批流程可视化
- 附件上传

---

### 报销模块

**路径**: `src/modules/expense/`

**页面**: Expense index

**组件**: MyExpenses, ExpenseForm, ExpenseDetail, ApprovalManagement, ExpenseStatistics, PaymentManagement

**功能**:
- 统计卡片
- Tab 切换（我的报销 / 待审批 / 统计 / 付款）
- 多行报销项目
- 发票 OCR 识别
- 二级审批流程

---

### 资产模块

**路径**: `src/modules/asset/`

**页面**: Asset index

**组件**: AssetForm, AssetDetail, AssetFilter, AssetTableView, AssetKanbanView, AssetGalleryView, BorrowDialog, ReturnDialog

**功能**:
- 多种视图模式（表格/看板/画廊）
- 借用对话框
- 归还处理
- 状态筛选

---

### 字典模块

**路径**: `src/modules/dict/`

**页面**: DictManagement, DictItemManagement

**组件**: DictTypeTable, DictTypeForm, DictTree, DictItemList, DictItemForm, DictColorTag

**功能**:
- 层级树视图
- 颜色标签
- 批量操作
- 缓存管理

---

### 会议模块

**路径**: `src/modules/meeting/`

**页面**: Meeting index

**组件**: MeetingList, MyBookings, ApprovalManagement, RoomManagement

**功能**:
- 统计卡片（可用会议室/进行中/今日/待审批）
- 我的预订
- 会议室管理

**注意**: 前端部分已完成，后端尚未实现

---

### 菜单模块

**路径**: `src/modules/menu/`

**组件**: MenuTree, MenuForm, IconSelector

**Composables**: useMenu, useMenuTree, useMenuDict, useMenuPermission

**功能**:
- 菜单树管理
- 图标选择
- 路由转换
- 权限过滤

---

### 权限模块

**路径**: `src/modules/permission/`

**页面**: PermissionList, RoleList

**组件**: PermissionForm, PermissionConfigDialog, RoleForm, RoleMembersDialog

**指令**: v-auth, v-role, v-super-admin

**功能**:
- 角色管理（CRUD）
- 权限管理
- 角色-权限分配
- 角色成员管理

---

## 工作流程

### 请假申请流程

```
[创建草稿] → [提交] → [一级审批] → [二级审批] → [三级审批] → [批准]
                 ↓           ↓           ↓
               [取消]    [驳回-可重提] [驳回-可重提] → [重新提交]
```

### 报销流程

```
[创建草稿] → [提交] → [部门审批] → [财务审批] → [付款] → [完成]
                  ↓            ↓
                [取消]      [驳回]
```

### 资产借用流程

```
[库存] → [借用申请] → [借用中] → [归还] → [库存/在用]
              ↓
           [逾期]
```

---

## 技术架构

### 包结构（后端）

```
module/{module-name}/
├── controller/      # REST 控制器
├── service/          # 服务接口 + impl/
├── mapper/           # MyBatis 映射器
├── entity/           # 数据库实体
├── dto/              # 数据传输对象
├── vo/               # 视图对象
├── enums/            # 枚举
└── util/             # 工具类
```

### 模块结构（前端）

```
modules/{module-name}/
├── views/            # 页面组件
├── components/       # 可复用组件
├── api/              # API 函数
├── store/            # Pinia 状态
├── types/            # TypeScript 类型
├── utils/            # 工具函数
├── composables/      # Vue 组合式函数
└── index.ts          # 模块入口
```

### 关键设计模式

| 模式 | 说明 |
|------|------|
| 乐观锁 | 使用 `@Version` 注解 |
| 逻辑删除 | 使用 `@TableLogic` |
| DTO/VO 分离 | 实体不直接返回 |
| API 响应包装 | `ApiResponse<T>` |
| 权限控制 | `@PreAuthorize` 注解 |

### ID 生成规则

| 实体 | 格式 |
|------|------|
| 员工 | `EMP+YYYYMMDD+序号` |
| 部门 | `DEPT+4位序号` |
| 请假 | `LEAVE+YYYYMMDD+序号` |
| 报销 | `EXPENSE+序号` |
| 资产 | `ASSET+序号` |
