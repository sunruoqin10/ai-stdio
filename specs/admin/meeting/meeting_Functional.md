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

**文档版本**: v1.0.0
**最后更新**: 2026-01-09
