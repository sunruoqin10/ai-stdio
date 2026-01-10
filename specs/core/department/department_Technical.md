# 部门管理模块 - 技术实现规范

> **文档类型**: 技术实现规范
> **模块类型**: 核心基础
> **技术栈**: Vue 3 + TypeScript + Element Plus + ECharts
> **参考模块**: `src/modules/employee/`
> **创建日期**: 2026-01-09
> **最后更新**: 2026-01-09

---

## 📋 目录

- [1. 数据结构](#1-数据结构)
- [2. API接口](#2-api接口)
- [3. 验证规则](#3-验证规则)
- [4. 算法实现](#4-算法实现)
- [5. 自动化功能](#5-自动化功能)

---

## 1. 数据结构

### 1.1 TypeScript类型定义

```typescript
/**
 * 部门信息
 */
interface Department {
  /** 部门编号 - 唯一标识 */
  id: string

  /** 部门名称 */
  name: string

  /** 部门简称 */
  shortName?: string

  /** 上级部门ID */
  parentId?: string | null

  /** 部门负责人ID */
  leaderId: string

  /** 部门级数(从1开始) */
  level: number

  /** 排序号 */
  sort: number

  /** 成立时间 */
  establishedDate?: string

  /** 部门描述 */
  description?: string

  /** 部门图标URL */
  icon?: string

  /** 状态: active-正常 disabled-停用 */
  status: 'active' | 'disabled'

  /** 创建时间 */
  createdAt: string

  /** 更新时间 */
  updatedAt: string

  /** 子部门列表(树形结构时使用) */
  children?: Department[]

  /** 部门人数(虚拟字段,从员工表汇总) */
  employeeCount?: number

  /** 部门负责人信息(关联查询) */
  leader?: Employee
}

/**
 * 部门筛选条件
 */
interface DepartmentFilter {
  /** 关键词搜索(名称/简称) */
  keyword?: string

  /** 状态筛选 */
  status?: 'active' | 'disabled'

  /** 负责人筛选 */
  leaderId?: string

  /** 层级筛选 */
  level?: number
}

/**
 * 部门表单数据
 */
interface DepartmentForm {
  /** 部门名称 */
  name: string

  /** 部门简称 */
  shortName?: string

  /** 上级部门ID */
  parentId?: string | null

  /** 部门负责人ID */
  leaderId: string

  /** 排序号 */
  sort?: number

  /** 成立时间 */
  establishedDate?: string

  /** 部门描述 */
  description?: string

  /** 部门图标 */
  icon?: string
}

/**
 * 部门移动请求
 */
interface MoveDepartmentRequest {
  /** 部门ID */
  departmentId: string

  /** 新的上级部门ID */
  newParentId: string | null
}

/**
 * 部门统计数据
 */
interface DepartmentStatistics {
  /** 总部门数 */
  total: number

  /** 一级部门数 */
  level1Count: number

  /** 最大层级深度 */
  maxLevel: number

  /** 有负责人的部门数 */
  withLeaderCount: number

  /** 总员工数(去重) */
  totalEmployees: number
}
```

### 1.2 字段说明

| 字段名 | 类型 | 必填 | 默认值 | 说明 | 示例 |
|-------|------|------|--------|------|------|
| id | string | ✅ | - | 唯一标识,格式: DEPT+序号 | DEPT001 |
| name | string | ✅ | - | 部门全称 | 技术部 |
| shortName | string | ❌ | - | 部门简称,用于快速检索 | 技术部 |
| parentId | string \| null | ❌ | null | 上级部门ID,null表示顶级部门 | DEPT001 |
| leaderId | string | ✅ | - | 部门负责人ID,关联员工表 | EMP001 |
| level | number | ✅ | - | 部门层级,顶级部门为1 | 2 |
| sort | number | ❌ | 0 | 排序号,同级部门按此排序 | 1 |
| establishedDate | string | ❌ | - | 成立日期 | 2020-01-01 |
| description | string | ❌ | - | 部门职责说明 | 负责产品研发 |
| icon | string | ❌ | - | 部门图标URL | /uploads/icons/dept1.png |
| status | string | ✅ | active | 状态: active正常, disabled停用 | active |
| createdAt | string | ✅ | - | 创建时间 | 2026-01-09 10:00:00 |
| updatedAt | string | ✅ | - | 更新时间 | 2026-01-09 10:00:00 |

### 1.3 枚举类型

```typescript
/**
 * 部门状态枚举
 */
enum DepartmentStatus {
  ACTIVE = 'active',      // 正常
  DISABLED = 'disabled'   // 停用
}
```

---

## 2. API接口

### 2.1 接口列表

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | /api/departments | 获取部门列表(树形或扁平) | 所有用户 |
| GET | /api/departments/:id | 获取部门详情 | 所有用户 |
| GET | /api/departments/:id/children | 获取子部门列表 | 所有用户 |
| GET | /api/departments/:id/employees | 获取部门成员 | 所有用户 |
| POST | /api/departments | 创建部门 | 管理员 |
| PUT | /api/departments/:id | 更新部门信息 | 管理员 |
| PUT | /api/departments/:id/move | 移动部门 | 管理员 |
| DELETE | /api/departments/:id | 删除部门 | 管理员 |
| GET | /api/departments/statistics | 获取部门统计 | 管理员 |

### 2.2 请求/响应示例

#### 2.2.1 获取部门列表

**请求**:
```typescript
GET /api/departments?type=tree&status=active
```

**参数**:
- type: 'tree' | 'flat' - 返回树形或扁平数据
- status: 'active' | 'disabled' - 状态筛选

**响应**:
```typescript
interface DepartmentListResponse {
  code: number
  message: string
  data: Department[]  // type=tree时返回树形结构
}

interface FlatListResponse {
  code: number
  message: string
  data: {
    list: Department[]
    total: number
  }
}
```

#### 2.2.2 获取部门详情

**请求**:
```typescript
GET /api/departments/:id
```

**响应**:
```typescript
interface DepartmentDetailResponse {
  code: number
  message: string
  data: Department & {
    leader?: Employee
    parent?: Department
    children?: Department[]
    employees?: Employee[]
  }
}
```

#### 2.2.3 创建部门

**请求**:
```typescript
POST /api/departments
{
  "name": "技术部",
  "shortName": "技术",
  "parentId": "DEPT001",
  "leaderId": "EMP001",
  "sort": 1,
  "description": "负责产品研发"
}
```

**响应**:
```typescript
interface CreateDepartmentResponse {
  code: number
  message: string
  data: {
    id: string  // 新创建的部门ID
  }
}
```

#### 2.2.4 移动部门

**请求**:
```typescript
PUT /api/departments/:id/move
{
  "newParentId": "DEPT002"  // null表示移动到顶级
}
```

**响应**:
```typescript
interface MoveDepartmentResponse {
  code: number
  message: string
  data: Department  // 更新后的部门信息
}
```

#### 2.2.5 删除部门

**请求**:
```typescript
DELETE /api/departments/:id
```

**响应**:
```typescript
interface DeleteDepartmentResponse {
  code: number
  message: string
}
```

### 2.3 API实现要求

```typescript
// src/modules/department/api/index.ts
import request from '@/utils/request'
import type {
  Department,
  DepartmentFilter,
  DepartmentForm,
  MoveDepartmentRequest,
  DepartmentStatistics
} from '../types'

/**
 * 获取部门列表
 * @param params 查询参数
 */
export function getList(params?: DepartmentFilter & { type?: 'tree' | 'flat' }) {
  return request.get<{
    list?: Department[]
    total?: number
  } | Department[]>('/api/departments', { params })
}

/**
 * 获取部门详情
 * @param id 部门ID
 */
export function getDetail(id: string) {
  return request.get<Department>(`/api/departments/${id}`)
}

/**
 * 获取子部门列表
 * @param id 部门ID
 */
export function getChildren(id: string) {
  return request.get<Department[]>(`/api/departments/${id}/children`)
}

/**
 * 获取部门成员
 * @param id 部门ID
 */
export function getEmployees(id: string) {
  return request.get(`/api/departments/${id}/employees`)
}

/**
 * 创建部门
 * @param data 表单数据
 */
export function create(data: DepartmentForm) {
  return request.post<{ id: string }>('/api/departments', data)
}

/**
 * 更新部门
 * @param id 部门ID
 * @param data 表单数据
 */
export function update(id: string, data: Partial<DepartmentForm>) {
  return request.put<Department>(`/api/departments/${id}`, data)
}

/**
 * 移动部门
 * @param data 移动请求
 */
export function move(data: MoveDepartmentRequest) {
  return request.put<Department>(
    `/api/departments/${data.departmentId}/move`,
    { newParentId: data.newParentId }
  )
}

/**
 * 删除部门
 * @param id 部门ID
 */
export function remove(id: string) {
  return request.delete(`/api/departments/${id}`)
}

/**
 * 获取部门统计
 */
export function getStatistics() {
  return request.get<DepartmentStatistics>('/api/departments/statistics')
}
```

---

## 3. 验证规则

### 3.1 前端验证

#### 3.1.1 表单验证规则

```typescript
// src/modules/department/components/DepartmentForm.vue
const rules = {
  name: [
    { required: true, message: '请输入部门名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' },
    {
      validator: async (rule: any, value: string, callback: any) => {
        // 异步验证部门名称唯一性
        if (value && value !== originalName.value) {
          const exists = await checkDepartmentNameExists(value)
          if (exists) {
            callback(new Error('部门名称已存在'))
          } else {
            callback()
          }
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  shortName: [
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  leaderId: [
    { required: true, message: '请选择部门负责人', trigger: 'change' }
  ],
  parentId: [
    {
      validator: (rule: any, value: string, callback: any) => {
        // 不能选择自己或自己的子部门作为上级部门
        if (value && value === currentDepartmentId.value) {
          callback(new Error('不能选择自己作为上级部门'))
        } else if (isChildDepartment(value)) {
          callback(new Error('不能选择子部门作为上级部门'))
        } else {
          callback()
        }
      },
      trigger: 'change'
    }
  ],
  sort: [
    { type: 'number', min: 0, message: '排序号必须大于等于0', trigger: 'blur' }
  ]
}
```

#### 3.1.2 业务逻辑验证

```typescript
// 验证部门名称唯一性
async function checkDepartmentNameExists(name: string): Promise<boolean> {
  const { data } = await api.getList({ keyword: name })
  return data.some((dept: Department) => dept.name === name)
}

// 判断是否为子部门
function isChildDepartment(parentId: string): boolean {
  // 递归检查所有子部门
  const allChildren = getAllChildDepartments(currentDepartmentId.value)
  return allChildren.some((child: Department) => child.id === parentId)
}

// 获取所有子部门(递归)
function getAllChildDepartments(parentId: string): Department[] {
  const children: Department[] = []
  const directChildren = departmentStore.list.filter(d => d.parentId === parentId)

  directChildren.forEach(child => {
    children.push(child)
    children.push(...getAllChildDepartments(child.id))
  })

  return children
}
```

### 3.2 后端验证

- [ ] 部门名称唯一性(同级部门内)
- [ ] 上级部门存在性检查
- [ ] 不能选择自己或子部门作为上级部门
- [ ] 部门层级限制(最多3-5级)
- [ ] 删除前检查是否有子部门
- [ ] 删除前检查是否有成员
- [ ] 负责人必须是有效员工

---

## 4. 算法实现

### 4.1 树形数据处理工具

```typescript
// src/utils/tree.ts
/**
 * 扁平数组转树形结构
 */
export function buildTree<T extends { id: string; parentId: string | null }>(
  flatList: T[],
  options?: {
    rootId?: string | null
    childrenKey?: string
  }
): T[] {
  const { rootId = null, childrenKey = 'children' } = options || {}

  const map = new Map<string, T>()
  const roots: T[] = []

  // 先建立映射
  flatList.forEach(item => {
    map.set(item.id, { ...item, [childrenKey]: [] })
  })

  // 建立树形关系
  flatList.forEach(item => {
    const node = map.get(item.id)!
    if (item.parentId === rootId) {
      roots.push(node)
    } else {
      const parent = map.get(item.parentId)
      if (parent) {
        ;(parent as any)[childrenKey].push(node)
      }
    }
  })

  return roots
}

/**
 * 树形转扁平数组
 */
export function flattenTree<T extends { children?: T[] }>(
  tree: T[],
  childrenKey = 'children'
): T[] {
  const result: T[] = []

  function traverse(nodes: T[]) {
    nodes.forEach(node => {
      const { [childrenKey]: children, ...rest } = node as any
      result.push(rest)
      if (children?.length > 0) {
        traverse(children)
      }
    })
  }

  traverse(tree)
  return result
}

/**
 * 获取节点路径(从根到当前节点)
 */
export function getNodePath<T extends { id: string; parentId: string | null }>(
  nodeId: string,
  flatList: T[]
): T[] {
  const path: T[] = []
  let current = flatList.find(item => item.id === nodeId)

  while (current) {
    path.unshift(current)
    if (!current.parentId) break
    current = flatList.find(item => item.id === current.parentId)
  }

  return path
}

/**
 * 计算节点层级
 */
export function calculateNodeLevel<T extends { parentId: string | null }>(
  nodeId: string,
  flatList: T[]
): number {
  let level = 1
  let current = flatList.find(item => item.id === nodeId)

  while (current?.parentId) {
    level++
    current = flatList.find(item => item.id === current.parentId)
  }

  return level
}

/**
 * 获取所有子孙节点
 */
export function getAllDescendants<T extends { id: string; children?: T[] }>(
  node: T,
  childrenKey = 'children'
): T[] {
  const descendants: T[] = []
  const children = (node as any)[childrenKey] || []

  children.forEach((child: T) => {
    descendants.push(child)
    descendants.push(...getAllDescendants(child, childrenKey))
  })

  return descendants
}
```

### 4.2 业务逻辑算法

#### 4.2.1 部门编号生成

```typescript
/**
 * 生成部门编号
 * 格式: DEPT + 4位序号
 */
async function generateDepartmentId(): Promise<string> {
  const count = await getNextDepartmentSequence()
  return `DEPT${String(count).padStart(4, '0')}`
}

/**
 * 获取下一个部门序号
 */
async function getNextDepartmentSequence(): Promise<number> {
  // 从数据库获取当前最大序号
  const maxId = await db.department.findFirst({
    where: { id: { startsWith: 'DEPT' } },
    orderBy: { id: 'desc' }
  })

  if (!maxId) return 1

  const currentNum = parseInt(maxId.id.replace('DEPT', ''))
  return currentNum + 1
}
```

#### 4.2.2 部门层级计算

```typescript
/**
 * 计算部门层级
 */
async function calculateLevel(parentId: string | null): Promise<number> {
  if (!parentId) {
    return 1  // 顶级部门
  }

  const parent = await getDepartment(parentId)
  return parent.level + 1
}

/**
 * 更新部门层级(级联更新子部门)
 */
async function updateDepartmentLevel(
  departmentId: string,
  newLevel: number
): Promise<void> {
  // 更新当前部门
  await db.department.update({
    where: { id: departmentId },
    data: { level: newLevel }
  })

  // 获取所有子部门
  const children = await getChildren(departmentId)

  // 递归更新子部门层级
  for (const child of children) {
    await updateDepartmentLevel(child.id, newLevel + 1)
  }
}
```

#### 4.2.3 部门人数统计

```typescript
/**
 * 计算部门人数
 */
async function calculateEmployeeCount(
  departmentId: string
): Promise<number> {
  const employees = await getEmployeesByDepartment(departmentId)
  return employees.filter(e => e.status === 'active').length
}

/**
 * 批量更新部门人数(定时任务)
 */
async function updateAllDepartmentCounts(): Promise<void> {
  const departments = await db.department.findMany()

  for (const dept of departments) {
    const count = await calculateEmployeeCount(dept.id)
    await db.department.update({
      where: { id: dept.id },
      data: { employeeCount: count }
    })
  }
}
```

### 4.3 验证算法

#### 4.3.1 移动验证

```typescript
/**
 * 移动部门验证
 */
async function validateMove(
  departmentId: string,
  newParentId: string | null
): Promise<boolean> {
  // 1. 不能移动到自己
  if (departmentId === newParentId) {
    throw new Error('不能移动到自己')
  }

  // 2. 不能移动到自己的子部门
  const allChildren = getAllChildDepartments(departmentId)
  if (allChildren.some(c => c.id === newParentId)) {
    throw new Error('不能移动到自己的子部门')
  }

  // 3. 检查目标层级是否超限
  const newLevel = newParentId
    ? (await getDepartment(newParentId)).level + 1
    : 1

  if (newLevel > MAX_DEPARTMENT_LEVEL) {
    throw new Error(`移动后层级将超过${MAX_DEPARTMENT_LEVEL}级`)
  }

  return true
}
```

#### 4.3.2 删除验证

```typescript
/**
 * 删除部门前检查
 */
async function validateDelete(departmentId: string): Promise<boolean> {
  // 1. 检查是否有子部门
  const children = await getChildren(departmentId)
  if (children.length > 0) {
    throw new Error('请先删除或移动所有子部门')
  }

  // 2. 检查是否有成员
  const employees = await getEmployees(departmentId)
  if (employees.length > 0) {
    throw new Error('请先转移或删除所有部门成员')
  }

  return true
}
```

---

## 5. 自动化功能

### 5.1 自动编号

```typescript
/**
 * 创建部门时自动生成编号
 */
async function beforeCreateDepartment(data: DepartmentForm) {
  // 自动生成部门ID
  const id = await generateDepartmentId()

  // 自动计算层级
  const level = await calculateLevel(data.parentId)

  return {
    ...data,
    id,
    level,
    sort: data.sort || 0
  }
}
```

### 5.2 自动层级更新

```typescript
/**
 * 移动部门时自动更新层级
 */
async function afterMoveDepartment(
  departmentId: string,
  newParentId: string | null
) {
  // 计算新层级
  const newLevel = await calculateLevel(newParentId)

  // 级联更新所有子部门层级
  await updateDepartmentLevel(departmentId, newLevel)
}
```

### 5.3 自动统计更新

```typescript
/**
 * 员工部门变更时自动更新统计
 */
async function afterEmployeeDepartmentChange(
  employeeId: string,
  oldDepartmentId: string,
  newDepartmentId: string
) {
  // 更新旧部门人数
  await updateDepartmentCount(oldDepartmentId)

  // 更新新部门人数
  await updateDepartmentCount(newDepartmentId)
}

/**
 * 定时任务: 每天凌晨更新所有部门统计
 */
cron.schedule('0 0 * * *', async () => {
  console.log('开始更新部门统计数据...')
  await updateAllDepartmentCounts()
  console.log('部门统计更新完成')
})
```

### 5.4 ECharts集成

```typescript
/**
 * 转换部门数据为ECharts Graph格式
 */
function convertToGraphData(departments: Department[]) {
  const nodes = departments.map(dept => ({
    id: dept.id,
    name: dept.name,
    value: dept.employeeCount || 0,
    category: dept.level
  }))

  const links: Array<{ source: string; target: string }> = []

  departments.forEach(dept => {
    if (dept.parentId) {
      links.push({
        source: dept.parentId,
        target: dept.id
      })
    }
  })

  return { nodes, links }
}

/**
 * ECharts配置
 */
function getGraphOption(data: any) {
  return {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c}人'
    },
    series: [{
      type: 'graph',
      layout: 'tree',
      symbol: 'rect',
      symbolSize: [120, 60],
      roam: true,
      label: {
        show: true,
        position: 'inside',
        fontSize: 14,
        formatter: '{b}\n{c}人'
      },
      edgeSymbol: ['circle', 'arrow'],
      edgeSymbolSize: [4, 10],
      data: data.nodes,
      links: data.links,
      itemStyle: {
        color: '#1890FF',
        borderColor: '#1890FF'
      },
      lineStyle: {
        color: '#ccc',
        curveness: 0.3
      },
      emphasis: {
        focus: 'adjacency',
        lineStyle: {
          width: 3
        }
      }
    }]
  }
}
```

---

## 6. 性能优化

### 6.1 数据缓存

```typescript
/**
 * 部门列表缓存
 */
class DepartmentCache {
  private cache: Map<string, any> = new Map()
  private ttl: number = 5 * 60 * 1000  // 5分钟

  set(key: string, value: any) {
    this.cache.set(key, {
      value,
      expired: Date.now() + this.ttl
    })
  }

  get(key: string) {
    const item = this.cache.get(key)
    if (!item) return null

    if (Date.now() > item.expired) {
      this.cache.delete(key)
      return null
    }

    return item.value
  }

  clear() {
    this.cache.clear()
  }
}

export const departmentCache = new DepartmentCache()
```

### 6.2 懒加载

```typescript
/**
 * 树形表格懒加载子部门
 */
async function loadChildDepartments(parentId: string) {
  // 检查缓存
  const cacheKey = `children:${parentId}`
  const cached = departmentCache.get(cacheKey)
  if (cached) return cached

  // 从数据库加载
  const children = await getChildren(parentId)

  // 缓存结果
  departmentCache.set(cacheKey, children)

  return children
}
```

### 6.3 批量操作

```typescript
/**
 * 批量更新部门层级
 */
async function batchUpdateLevel(departments: Department[]) {
  const updates = departments.map(dept =>
    db.department.update({
      where: { id: dept.id },
      data: { level: dept.level }
    })
  )

  await Promise.all(updates)
}
```

---

## 7. 错误处理

### 7.1 错误码定义

```typescript
enum DepartmentErrorCode {
  NAME_DUPLICATE = 'DEPT_001',           // 部门名称重复
  PARENT_NOT_FOUND = 'DEPT_002',          // 上级部门不存在
  LEVEL_EXCEEDED = 'DEPT_003',            // 层级超限
  HAS_CHILDREN = 'DEPT_004',              // 有子部门
  HAS_EMPLOYEES = 'DEPT_005',             // 有成员
  INVALID_PARENT = 'DEPT_006',            // 无效的上级部门
  LEADER_NOT_FOUND = 'DEPT_007'           // 负责人不存在
}
```

### 7.2 错误处理中间件

```typescript
/**
 * 部门操作错误处理
 */
function handleDepartmentError(error: any) {
  if (error.code === DepartmentErrorCode.NAME_DUPLICATE) {
    return {
      code: 400,
      message: '部门名称已存在,请使用其他名称'
    }
  }

  if (error.code === DepartmentErrorCode.LEVEL_EXCEEDED) {
    return {
      code: 400,
      message: '部门层级不能超过5级'
    }
  }

  // 其他错误...
  return {
    code: 500,
    message: '操作失败,请稍后重试'
  }
}
```

---

## 8. 数据字典集成实现

### 8.1 数据字典API封装

```typescript
// src/modules/dict/api/index.ts
import { http } from '@/utils/request'

/**
 * 数据字典项
 */
interface DictItem {
  label: string
  value: string
  color?: string
  icon?: string
  sort?: number
}

/**
 * 获取字典列表
 * @param dictCode 字典编码
 */
export function getDictList(dictCode: string): Promise<DictItem[]> {
  return http.get<DictItem[]>(`/api/dict/${dictCode}`)
}

/**
 * 批量获取字典
 * @param dictCodes 字典编码数组
 */
export function getDictBatch(dictCodes: string[]): Promise<Record<string, DictItem[]>> {
  return http.post<Record<string, DictItem[]>>('/api/dict/batch', { dictCodes })
}

/**
 * 获取字典标签
 * @param dictCode 字典编码
 * @param value 字典值
 */
export function getDictLabel(dictCode: string, value: string): string {
  const dictStore = useDictStore()
  return dictStore.getLabel(dictCode, value)
}
```

### 8.2 Pinia字典Store

```typescript
// src/modules/dict/store/index.ts
import { defineStore } from 'pinia'
import { getDictBatch } from '../api'
import type { DictItem } from '../types'

export const useDictStore = defineStore('dict', () => {
  const dictData = ref<Record<string, DictItem[]>>({})
  const cacheTime = ref<Record<string, number>>({})

  const CACHE_DURATION = 30 * 60 * 1000 // 30分钟

  /**
   * 批量加载字典
   */
  async function loadDicts(dictCodes: string[]): Promise<void> {
    const now = Date.now()
    const needLoad = dictCodes.filter(code => {
      const cached = cacheTime.value[code]
      return !cached || (now - cached > CACHE_DURATION)
    })

    if (needLoad.length === 0) return

    const data = await getDictBatch(needLoad)

    for (const [code, items] of Object.entries(data)) {
      dictData.value[code] = items
      cacheTime.value[code] = now
    }
  }

  /**
   * 获取字典列表
   */
  function getDictList(dictCode: string): DictItem[] {
    return dictData.value[dictCode] || []
  }

  /**
   * 获取字典标签
   */
  function getLabel(dictCode: string, value: string): string {
    const list = getDictList(dictCode)
    const item = list.find(d => d.value === value)
    return item?.label || value
  }

  /**
   * 刷新字典
   */
  async function refreshDict(dictCode: string): Promise<void> {
    delete cacheTime.value[dictCode]
    await loadDicts([dictCode])
  }

  return {
    dictData,
    loadDicts,
    getDictList,
    getLabel,
    refreshDict
  }
})
```

### 8.3 部门模块中使用字典

```typescript
// src/modules/department/composables/useDict.ts
import { useDictStore } from '@/modules/dict/store'

export function useDepartmentDict() {
  const dictStore = useDictStore()

  // 预加载部门模块所需字典
  onMounted(async () => {
    await dictStore.loadDicts([
      'department_status'
    ])
  })

  // 获取字典选项
  const statusOptions = computed(() => dictStore.getDictList('department_status'))

  // 动态加载部门类型字典
  async function loadTypeOptions() {
    await dictStore.loadDicts(['department_type'])
    return dictStore.getDictList('department_type')
  }

  // 动态加载部门层级字典
  async function loadLevelOptions() {
    await dictStore.loadDicts(['department_level'])
    return dictStore.getDictList('department_level')
  }

  // 获取状态显示文本
  function getStatusLabel(value: string): string {
    return dictStore.getLabel('department_status', value)
  }

  function getTypeLabel(value: string): string {
    return dictStore.getLabel('department_type', value)
  }

  function getLevelLabel(value: string): string {
    return dictStore.getLabel('department_level', value)
  }

  return {
    statusOptions,
    loadTypeOptions,
    loadLevelOptions,
    getStatusLabel,
    getTypeLabel,
    getLevelLabel
  }
}
```

### 8.4 筛选面板中使用字典

```vue
<!-- src/modules/department/components/DepartmentFilter.vue -->
<script setup lang="ts">
import { useDepartmentDict } from '../composables/useDict'

const {
  statusOptions,
  loadTypeOptions
} = useDepartmentDict()

const filterForm = ref({
  status: '',
  type: ''
})

// 加载类型选项
onMounted(async () => {
  typeOptions.value = await loadTypeOptions()
})
</script>

<template>
  <el-form :model="filterForm">
    <el-form-item label="部门状态">
      <el-select v-model="filterForm.status" clearable>
        <el-option
          v-for="item in statusOptions"
          :key="item.value"
          :label="item.label"
          :value="item.value"
        />
      </el-select>
    </el-form-item>

    <el-form-item label="部门类型">
      <el-select v-model="filterForm.type" clearable>
        <el-option
          v-for="item in typeOptions"
          :key="item.value"
          :label="item.label"
          :value="item.value"
        />
      </el-select>
    </el-form-item>
  </el-form>
</template>
```

---

## 9. 权限管理集成实现

### 9.1 权限Store扩展

```typescript
// src/modules/auth/store/index.ts (扩展)
import { useAuthStore } from '@/modules/auth/store'

export function useDepartmentPermission() {
  const authStore = useAuthStore()

  /**
   * 检查部门管理权限
   */
  function hasPermission(permission: string): boolean {
    return authStore.hasPermission(permission)
  }

  /**
   * 检查是否可以编辑指定部门
   */
  function canEditDepartment(targetDepartment: Department): boolean {
    // 系统管理员可以编辑所有部门
    if (hasPermission('department:edit_all')) {
      return true
    }

    // 部门管理员可以编辑本部门
    if (hasPermission('department:edit')) {
      const currentUser = authStore.userInfo
      return targetDepartment.id === currentUser?.departmentId
    }

    return false
  }

  /**
   * 检查是否可以查看指定部门
   */
  function canViewDepartment(targetDepartment: Department): boolean {
    // 系统管理员可以查看所有部门
    if (hasPermission('department:view_all')) {
      return true
    }

    // 部门管理员可以查看本部门及下级部门
    if (hasPermission('department:view_department')) {
      const currentUser = authStore.userInfo
      return isChildDepartment(targetDepartment.id, currentUser?.departmentId)
    }

    // 普通员工只能查看本部门
    const currentUser = authStore.userInfo
    return targetDepartment.id === currentUser?.departmentId
  }

  /**
   * 过滤部门列表(数据权限)
   */
  function filterDepartmentList(departments: Department[]): Department[] {
    if (hasPermission('department:view_all')) {
      return departments
    }

    if (hasPermission('department:view_department')) {
      const currentUser = authStore.userInfo
      // 返回本部门及所有下级部门
      return departments.filter(d =>
        d.id === currentUser?.departmentId ||
        isChildDepartment(d.id, currentUser?.departmentId)
      )
    }

    // 仅本部门
    const currentUser = authStore.userInfo
    return departments.filter(d => d.id === currentUser?.departmentId)
  }

  /**
   * 判断是否为子部门
   */
  function isChildDepartment(departmentId: string, parentId: string): boolean {
    // 递归检查部门树结构
    const department = departmentStore.list.find(d => d.id === parentId)
    if (!department) return false

    if (department.id === departmentId) return true
    if (department.children) {
      return department.children.some((child: Department) =>
        isChildDepartment(departmentId, child.id)
      )
    }

    return false
  }

  return {
    hasPermission,
    canEditDepartment,
    canViewDepartment,
    filterDepartmentList
  }
}
```

### 9.2 列表页权限控制

```vue
<!-- src/modules/department/views/DepartmentList.vue -->
<script setup lang="ts">
import { useDepartmentPermission } from '../composables/useDepartmentPermission'

const {
  hasPermission,
  filterDepartmentList
} = useDepartmentPermission()

// 权限判断
const canCreate = computed(() => hasPermission('department:create'))
const canExport = computed(() => hasPermission('department:export'))
const canDelete = computed(() => hasPermission('department:delete'))

// 原始数据
const allDepartments = ref<Department[]>([])

// 应用数据权限过滤
const departments = computed(() => filterDepartmentList(allDepartments.value))
</script>

<template>
  <div class="department-list">
    <!-- 操作栏 -->
    <div class="toolbar">
      <el-button
        v-if="canCreate"
        type="primary"
        @click="handleCreate"
      >
        新增部门
      </el-button>

      <el-button
        v-if="canExport"
        @click="handleExport"
      >
        导出列表
      </el-button>
    </div>

    <!-- 部门树形表格 -->
    <el-table :data="departments" row-key="id" :tree-props="{ children: 'children' }">
      <el-table-column prop="name" label="部门名称" />
      <el-table-column prop="shortName" label="简称" />
      <el-table-column prop="leaderName" label="负责人" />
      <el-table-column prop="level" label="层级" />
      <el-table-column prop="employeeCount" label="人数" />

      <el-table-column label="操作" fixed="right">
        <template #default="{ row }">
          <el-button
            v-if="hasPermission('department:edit')"
            link
            @click="handleEdit(row)"
          >
            编辑
          </el-button>

          <el-button
            v-if="hasPermission('department:move')"
            link
            @click="handleMove(row)"
          >
            移动
          </el-button>

          <el-button
            v-if="canDelete"
            link
            type="danger"
            @click="handleDelete(row)"
          >
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>
```

### 9.3 表单权限控制

```vue
<!-- src/modules/department/components/DepartmentForm.vue -->
<script setup lang="ts">
import { useDepartmentPermission } from '../composables/useDepartmentPermission'

const {
  canEditDepartment
} = useDepartmentPermission()

const isEdit = ref(false)
const department = ref<Department>(null)

// 表单权限
const formDisabled = computed(() => {
  if (isEdit.value) {
    return !canEditDepartment(department.value)
  }
  return false
})

// 字段级权限
const canEditSort = computed(() => hasPermission('department:edit_all'))
const canEditLevel = computed(() => hasPermission('department:edit_all'))
</script>

<template>
  <el-form :disabled="formDisabled">
    <!-- 基本信息 -->
    <el-form-item label="部门名称">
      <el-input v-model="form.name" />
    </el-form-item>

    <el-form-item label="部门简称">
      <el-input v-model="form.shortName" />
    </el-form-item>

    <el-form-item label="部门负责人">
      <el-select v-model="form.leaderId" filterable>
        <el-option
          v-for="emp in employeeList"
          :key="emp.id"
          :label="emp.name"
          :value="emp.id"
        />
      </el-select>
    </el-form-item>

    <!-- 敏感字段: 仅系统管理员可编辑 -->
    <el-form-item v-if="canEditLevel" label="层级">
      <el-input-number v-model="form.level" :min="1" :max="5" />
    </el-form-item>

    <el-form-item v-if="canEditSort" label="排序号">
      <el-input-number v-model="form.sort" :min="0" />
    </el-form-item>
  </el-form>
</template>
```

### 9.4 API请求权限拦截

```typescript
// src/utils/request.ts (扩展)
import { useAuthStore } from '@/modules/auth/store'

service.interceptors.request.use(
  (config) => {
    const authStore = useAuthStore()

    // 自动添加Token
    if (authStore.accessToken) {
      config.headers.Authorization = `Bearer ${authStore.accessToken}`
    }

    // 记录请求权限(用于审计)
    config.metadata = {
      permission: getPermissionFromUrl(config.url),
      userId: authStore.userInfo?.id,
      timestamp: Date.now()
    }

    return config
  },
  (error) => Promise.reject(error)
)

// 根据URL推断所需权限
function getPermissionFromUrl(url: string): string {
  if (url.includes('/departments')) {
    if (url.includes('POST')) return 'department:create'
    if (url.includes('PUT') && url.includes('/move')) return 'department:move'
    if (url.includes('PUT')) return 'department:edit'
    if (url.includes('DELETE')) return 'department:delete'
    return 'department:view'
  }
  return ''
}
```

---

**文档版本**: v1.1.0
**创建人**: AI开发助手
**最后更新**: 2026-01-10
