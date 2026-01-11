# 数据字典技术实现规范

> **规范类型**: 第二层 - 技术实现规范
> **模块**: 数据字典
> **版本**: v1.0.0
> **创建日期**: 2026-01-09

---

## 📋 目录

- [1. 数据结构](#1-数据结构)
- [2. API接口](#2-api接口)
- [3. 验证规则](#3-验证规则)
- [4. 缓存机制](#4-缓存机制)
- [5. 性能优化](#5-性能优化)

---

## 1. 数据结构

### 1.1 TypeScript类型定义

```typescript
/**
 * 字典类型
 */
interface DictType {
  /** 字典类型ID - 唯一标识 */
  id: string

  /** 字典编码 - 唯一,格式: module_entity_property */
  code: string

  /** 字典名称 */
  name: string

  /** 字典描述 */
  description?: string

  /** 字典类别: system-系统字典, business-业务字典 */
  category: 'system' | 'business'

  /** 字典项数量 */
  itemCount?: number

  /** 状态: enabled-启用, disabled-禁用 */
  status: 'enabled' | 'disabled'

  /** 排序序号 */
  sortOrder?: number

  /** 扩展属性(JSON格式) */
  extProps?: Record<string, any>

  /** 创建时间 */
  createdAt: string

  /** 更新时间 */
  updatedAt: string
}

/**
 * 字典项
 */
interface DictItem {
  /** 字典项ID - 唯一标识 */
  id: string

  /** 所属字典类型ID */
  dictTypeId: string

  /** 字典类型编码(冗余字段,方便查询) */
  dictTypeCode: string

  /** 项标签 - 显示文本 */
  label: string

  /** 项值 - 实际值 */
  value: string

  /** 颜色类型: primary/success/warning/danger/info */
  colorType?: 'primary' | 'success' | 'warning' | 'danger' | 'info'

  /** 排序序号 */
  sortOrder: number

  /** 状态: enabled-启用, disabled-禁用 */
  status: 'enabled' | 'disabled'

  /** 扩展属性(JSON格式) */
  extProps?: Record<string, any>

  /** 创建时间 */
  createdAt: string

  /** 更新时间 */
  updatedAt: string
}

/**
 * 字典类型表单数据
 */
interface DictTypeForm {
  /** 字典编码 */
  code: string

  /** 字典名称 */
  name: string

  /** 字典描述 */
  description?: string

  /** 字典类别 */
  category: 'system' | 'business'

  /** 状态 */
  status?: 'enabled' | 'disabled'

  /** 排序序号 */
  sortOrder?: number

  /** 扩展属性 */
  extProps?: Record<string, any>
}

/**
 * 字典项表单数据
 */
interface DictItemForm {
  /** 所属字典类型ID */
  dictTypeId: string

  /** 项标签 */
  label: string

  /** 项值 */
  value: string

  /** 颜色类型 */
  colorType?: 'primary' | 'success' | 'warning' | 'danger' | 'info'

  /** 排序序号 */
  sortOrder?: number

  /** 状态 */
  status?: 'enabled' | 'disabled'

  /** 扩展属性 */
  extProps?: Record<string, any>
}

/**
 * 字典筛选条件
 */
interface DictFilter {
  /** 关键词搜索(编码/名称/标签/值) */
  keyword?: string

  /** 字典类别 */
  category?: 'system' | 'business'

  /** 字典类型编码 */
  dictTypeCode?: string

  /** 状态 */
  status?: 'enabled' | 'disabled'
}

/**
 * 字典树节点
 */
interface DictTreeNode {
  id: string
  code: string
  name: string
  category: 'system' | 'business'
  itemCount: number
  children?: DictItem[]
}

/**
 * 字典数据(用于前端使用)
 */
interface DictData {
  /** 字典类型编码 */
  dictType: string

  /** 字典项列表 */
  items: Array<{
    label: string
    value: string
    colorType?: string
    extProps?: Record<string, any>
  }>
}
```

### 1.2 字段说明

**DictType 字典类型表**:

| 字段名 | 类型 | 必填 | 默认值 | 说明 | 示例 |
|-------|------|------|--------|------|------|
| id | string | ✅ | - | 字典类型ID | dict_type_001 |
| code | string | ✅ | - | 字典编码(唯一) | employee_status |
| name | string | ✅ | - | 字典名称 | 员工状态 |
| description | string | ❌ | - | 字典描述 | 员工在职状态 |
| category | string | ✅ | - | 字典类别 | system |
| itemCount | number | ❌ | - | 字典项数量 | 3 |
| status | string | ✅ | enabled | 状态 | enabled |
| sortOrder | number | ❌ | 0 | 排序序号 | 100 |
| extProps | object | ❌ | - | 扩展属性 | {} |
| createdAt | string | ✅ | - | 创建时间 | 2026-01-09 10:00:00 |
| updatedAt | string | ✅ | - | 更新时间 | 2026-01-09 10:00:00 |

**DictItem 字典项表**:

| 字段名 | 类型 | 必填 | 默认值 | 说明 | 示例 |
|-------|------|------|--------|------|------|
| id | string | ✅ | - | 字典项ID | dict_item_001 |
| dictTypeId | string | ✅ | - | 所属字典类型ID | dict_type_001 |
| dictTypeCode | string | ✅ | - | 字典类型编码 | employee_status |
| label | string | ✅ | - | 项标签 | 在职 |
| value | string | ✅ | - | 项值 | active |
| colorType | string | ❌ | - | 颜色类型 | success |
| sortOrder | number | ✅ | - | 排序序号 | 10 |
| status | string | ✅ | enabled | 状态 | enabled |
| extProps | object | ❌ | - | 扩展属性 | {} |
| createdAt | string | ✅ | - | 创建时间 | 2026-01-09 10:00:00 |
| updatedAt | string | ✅ | - | 更新时间 | 2026-01-09 10:00:00 |

### 1.3 枚举类型

```typescript
/**
 * 字典类别枚举
 */
enum DictCategory {
  SYSTEM = 'system',      // 系统字典
  BUSINESS = 'business'   // 业务字典
}

/**
 * 状态枚举
 */
enum DictStatus {
  ENABLED = 'enabled',    // 启用
  DISABLED = 'disabled'   // 禁用
}

/**
 * 颜色类型枚举
 */
enum DictColorType {
  PRIMARY = 'primary',    // 主要色
  SUCCESS = 'success',    // 成功色
  WARNING = 'warning',    // 警告色
  DANGER = 'danger',      // 危险色
  INFO = 'info'           // 信息色
}
```

---

## 2. API接口

### 2.1 接口列表

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | /api/dict/types | 获取字典类型列表 | 所有用户 |
| GET | /api/dict/types/:id | 获取字典类型详情 | 所有用户 |
| POST | /api/dict/types | 创建字典类型 | 管理员 |
| PUT | /api/dict/types/:id | 更新字典类型 | 管理员 |
| DELETE | /api/dict/types/:id | 删除字典类型 | 管理员 |
| GET | /api/dict/items | 获取字典项列表 | 所有用户 |
| GET | /api/dict/items/:id | 获取字典项详情 | 所有用户 |
| POST | /api/dict/items | 创建字典项 | 管理员 |
| PUT | /api/dict/items/:id | 更新字典项 | 管理员 |
| DELETE | /api/dict/items/:id | 删除字典项 | 管理员 |
| PUT | /api/dict/items/:id/sort | 更新字典项排序 | 管理员 |
| GET | /api/dict/:code | 获取字典数据(缓存) | 所有用户 |
| GET | /api/dict/tree | 获取字典树 | 所有用户 |
| POST | /api/dict/import | 导入字典 | 管理员 |
| GET | /api/dict/export | 导出字典 | 管理员 |
| DELETE | /api/dict/cache | 清除字典缓存 | 管理员 |

### 2.2 请求/响应示例

#### 2.2.1 获取字典类型列表

**请求**:
```typescript
GET /api/dict/types?page=1&pageSize=20&keyword=员工&category=system
```

**参数**:
- page: 页码
- pageSize: 每页数量
- keyword: 关键词(编码/名称)
- category: 字典类别
- status: 状态

**响应**:
```typescript
interface DictTypeListResponse {
  code: number
  message: string
  data: {
    list: DictType[]
    total: number
    page: number
    pageSize: number
  }
}
```

#### 2.2.2 创建字典类型

**请求**:
```typescript
POST /api/dict/types
{
  "code": "employee_status",
  "name": "员工状态",
  "description": "员工在职状态",
  "category": "system",
  "status": "enabled"
}
```

**响应**:
```typescript
interface CreateDictTypeResponse {
  code: number
  message: string
  data: {
    id: string  // 新创建的字典类型ID
  }
}
```

#### 2.2.3 获取字典项列表

**请求**:
```typescript
GET /api/dict/items?dictTypeCode=employee_status&status=enabled
```

**参数**:
- dictTypeCode: 字典类型编码
- status: 状态
- keyword: 关键词(标签/值)
- page: 页码
- pageSize: 每页数量

**响应**:
```typescript
interface DictItemListResponse {
  code: number
  message: string
  data: {
    list: DictItem[]
    total: number
    page: number
    pageSize: number
  }
}
```

#### 2.2.4 创建字典项

**请求**:
```typescript
POST /api/dict/items
{
  "dictTypeId": "dict_type_001",
  "label": "在职",
  "value": "active",
  "colorType": "success",
  "sortOrder": 10,
  "status": "enabled"
}
```

**响应**:
```typescript
interface CreateDictItemResponse {
  code: number
  message: string
  data: {
    id: string  // 新创建的字典项ID
  }
}
```

#### 2.2.5 更新字典项排序

**请求**:
```typescript
PUT /api/dict/items/sort
{
  "items": [
    { "id": "item_001", "sortOrder": 10 },
    { "id": "item_002", "sortOrder": 20 },
    { "id": "item_003", "sortOrder": 30 }
  ]
}
```

**响应**:
```typescript
interface UpdateSortResponse {
  code: number
  message: string
  data: {
    success: boolean
  }
}
```

#### 2.2.6 获取字典数据(前端使用)

**请求**:
```typescript
GET /api/dict/employee_status
```

**响应**:
```typescript
interface DictDataResponse {
  code: number
  message: string
  data: DictData
}
```

#### 2.2.7 获取字典树

**请求**:
```typescript
GET /api/dict/tree
```

**响应**:
```typescript
interface DictTreeResponse {
  code: number
  message: string
  data: DictTreeNode[]
}
```

### 2.3 API实现要求

```typescript
// src/modules/dict/api/index.ts
import request from '@/utils/request'
import type {
  DictType,
  DictItem,
  DictTypeForm,
  DictItemForm,
  DictFilter,
  DictTreeNode,
  DictData
} from '../types'

/**
 * 获取字典类型列表
 */
export function getDictTypeList(params: DictFilter & {
  page: number
  pageSize: number
}) {
  return request.get<{
    list: DictType[]
    total: number
    page: number
    pageSize: number
  }>('/api/dict/types', { params })
}

/**
 * 获取字典类型详情
 */
export function getDictTypeDetail(id: string) {
  return request.get<DictType>(`/api/dict/types/${id}`)
}

/**
 * 创建字典类型
 */
export function createDictType(data: DictTypeForm) {
  return request.post<{ id: string }>('/api/dict/types', data)
}

/**
 * 更新字典类型
 */
export function updateDictType(id: string, data: Partial<DictTypeForm>) {
  return request.put<DictType>(`/api/dict/types/${id}`, data)
}

/**
 * 删除字典类型
 */
export function deleteDictType(id: string) {
  return request.delete(`/api/dict/types/${id}`)
}

/**
 * 获取字典项列表
 */
export function getDictItemList(params: DictFilter & {
  dictTypeCode?: string
  page: number
  pageSize: number
}) {
  return request.get<{
    list: DictItem[]
    total: number
    page: number
    pageSize: number
  }>('/api/dict/items', { params })
}

/**
 * 获取字典项详情
 */
export function getDictItemDetail(id: string) {
  return request.get<DictItem>(`/api/dict/items/${id}`)
}

/**
 * 创建字典项
 */
export function createDictItem(data: DictItemForm) {
  return request.post<{ id: string }>('/api/dict/items', data)
}

/**
 * 更新字典项
 */
export function updateDictItem(id: string, data: Partial<DictItemForm>) {
  return request.put<DictItem>(`/api/dict/items/${id}`, data)
}

/**
 * 删除字典项
 */
export function deleteDictItem(id: string) {
  return request.delete(`/api/dict/items/${id}`)
}

/**
 * 批量更新字典项排序
 */
export function updateDictItemSort(items: Array<{ id: string; sortOrder: number }>) {
  return request.put<{ success: boolean }>('/api/dict/items/sort', { items })
}

/**
 * 获取字典数据(缓存)
 */
export function getDictData(dictTypeCode: string) {
  return request.get<DictData>(`/api/dict/${dictTypeCode}`)
}

/**
 * 获取字典树
 */
export function getDictTree() {
  return request.get<DictTreeNode[]>('/api/dict/tree')
}

/**
 * 导入字典
 */
export function importDict(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post<{ success: number; failed: number }>('/api/dict/import', formData)
}

/**
 * 导出字典
 */
export function exportDict(dictTypeCodes?: string[]) {
  return request.get('/api/dict/export', {
    params: { dictTypeCodes },
    responseType: 'blob'
  })
}

/**
 * 清除字典缓存
 */
export function clearDictCache(dictTypeCode?: string) {
  return request.delete('/api/dict/cache', {
    params: { dictTypeCode }
  })
}
```

---

## 3. 验证规则

### 3.1 前端验证

#### 3.1.1 字典类型表单验证

```typescript
// src/modules/dict/components/DictTypeForm.vue
const dictTypeRules = {
  code: [
    { required: true, message: '请输入字典编码', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' },
    {
      pattern: /^[a-z][a-z0-9_]*$/,
      message: '只能包含小写字母、数字和下划线,且以字母开头',
      trigger: 'blur'
    },
    {
      validator: async (rule: any, value: string, callback: any) => {
        // 验证编码唯一性
        if (value && value !== originalCode.value) {
          const exists = await checkDictCodeExists(value)
          if (exists) {
            callback(new Error('该字典编码已存在'))
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
  name: [
    { required: true, message: '请输入字典名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  description: [
    { max: 200, message: '最多 200 个字符', trigger: 'blur' }
  ],
  category: [
    { required: true, message: '请选择字典类别', trigger: 'change' }
  ]
}
```

#### 3.1.2 字典项表单验证

```typescript
// src/modules/dict/components/DictItemForm.vue
const dictItemRules = {
  dictTypeId: [
    { required: true, message: '请选择字典类型', trigger: 'change' }
  ],
  label: [
    { required: true, message: '请输入项标签', trigger: 'blur' },
    { min: 1, max: 50, message: '长度在 1 到 50 个字符', trigger: 'blur' }
  ],
  value: [
    { required: true, message: '请输入项值', trigger: 'blur' },
    { min: 1, max: 50, message: '长度在 1 到 50 个字符', trigger: 'blur' },
    {
      validator: async (rule: any, value: string, callback: any) => {
        // 验证项值在同一字典类型下的唯一性
        if (value && formData.value.dictTypeId) {
          const exists = await checkDictValueExists(
            formData.value.dictTypeId,
            value,
            originalValue.value
          )
          if (exists) {
            callback(new Error('该字典类型下已存在相同的项值'))
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
  sortOrder: [
    { required: true, message: '请输入排序序号', trigger: 'blur' },
    { type: 'number', message: '排序序号必须为数字', trigger: 'blur' }
  ]
}
```

#### 3.1.3 唯一性验证

```typescript
// 验证字典编码唯一性
async function checkDictCodeExists(code: string): Promise<boolean> {
  const { data } = await getDictTypeList({ code, page: 1, pageSize: 1 })
  return data.list.length > 0
}

// 验证字典项值唯一性
async function checkDictValueExists(
  dictTypeId: string,
  value: string,
  excludeId?: string
): Promise<boolean> {
  const { data } = await getDictItemList({
    dictTypeId,
    value,
    page: 1,
    pageSize: 1
  })

  if (data.list.length === 0) return false
  if (excludeId && data.list[0].id === excludeId) return false
  return true
}
```

### 3.2 后端验证

- [ ] 字典编码唯一性验证
- [ ] 字典编码格式验证(小写字母+下划线)
- [ ] 字典项值唯一性验证(同一字典类型下)
- [ ] 删除字典类型时检查是否包含字典项
- [ ] 删除字典项时检查是否被业务数据引用
- [ ] 字典类别枚举值验证
- [ ] 颜色类型枚举值验证

---

## 4. 缓存机制

### 4.1 缓存策略

```typescript
// src/modules/dict/utils/cache.ts

/**
 * 字典缓存管理
 */
class DictCacheManager {
  private cache: Map<string, { data: DictData; expires: number }> = new Map()
  private CACHE_TTL = 3600000 // 1小时

  /**
   * 获取缓存数据
   */
  get(dictTypeCode: string): DictData | null {
    const cached = this.cache.get(`dict:${dictTypeCode}`)
    if (!cached) return null

    // 检查是否过期
    if (Date.now() > cached.expires) {
      this.cache.delete(`dict:${dictTypeCode}`)
      return null
    }

    return cached.data
  }

  /**
   * 设置缓存
   */
  set(dictTypeCode: string, data: DictData): void {
    this.cache.set(`dict:${dictTypeCode}`, {
      data,
      expires: Date.now() + this.CACHE_TTL
    })
  }

  /**
   * 清除缓存
   */
  clear(dictTypeCode?: string): void {
    if (dictTypeCode) {
      this.cache.delete(`dict:${dictTypeCode}`)
    } else {
      this.cache.clear()
    }
  }

  /**
   * 清除所有过期缓存
   */
  clearExpired(): void {
    const now = Date.now()
    for (const [key, value] of this.cache.entries()) {
      if (now > value.expires) {
        this.cache.delete(key)
      }
    }
  }
}

export const dictCacheManager = new DictCacheManager()
```

### 4.2 缓存使用

```typescript
// src/modules/dict/api/index.ts

/**
 * 获取字典数据(带缓存)
 */
export async function getDictData(dictTypeCode: string): Promise<DictData> {
  // 尝试从缓存获取
  const cached = dictCacheManager.get(dictTypeCode)
  if (cached) {
    return cached
  }

  // 缓存未命中,请求API
  const response = await request.get<DictData>(`/api/dict/${dictTypeCode}`)

  // 设置缓存
  dictCacheManager.set(dictTypeCode, response.data)

  return response.data
}

/**
 * 清除字典缓存
 */
export function clearDictCache(dictTypeCode?: string) {
  dictCacheManager.clear(dictTypeCode)
  return request.delete('/api/dict/cache', {
    params: { dictTypeCode }
  })
}
```

### 4.3 缓存失效时机

- 字典类型更新时: 清除该类型下所有字典项缓存
- 字典项新增/更新/删除时: 清除该字典项所属类型的缓存
- 批量操作时: 清除相关类型的所有缓存
- 手动刷新时: 清除指定类型或全部缓存

---

## 5. 性能优化

### 5.1 数据导入导出

```typescript
// 导入字典
async function importDict(file: File): Promise<{
  success: number
  failed: number
  errors: string[]
}> {
  const data = await readExcelFile(file)

  let success = 0
  let failed = 0
  const errors: string[] = []

  for (let i = 0; i < data.length; i++) {
    try {
      // 验证数据
      validateDictData(data[i])

      // 创建字典
      await createDictType(data[i])
      success++
    } catch (error) {
      failed++
      errors.push(`第${i + 2}行: ${error.message}`)
    }
  }

  return { success, failed, errors }
}

// 导出到Excel
async function exportDict(dictTypeCodes?: string[]): Promise<Blob> {
  const dictTypes = await getDictTypeList({ dictTypeCodes })

  // 转换为Excel格式
  const excelData = dictTypes.map(dt => ({
    '字典编码': dt.code,
    '字典名称': dt.name,
    '字典描述': dt.description,
    '字典类别': dt.category === 'system' ? '系统字典' : '业务字典',
    '状态': dt.status === 'enabled' ? '启用' : '禁用'
  }))

  // 生成Excel文件
  return generateExcel(excelData, '字典列表')
}
```

### 5.2 性能优化策略

```typescript
// 1. 虚拟滚动(大量字典项)
import { useVirtualList } from '@vueuse/core'

const { list, containerProps, wrapperProps } = useVirtualList(
  dictItemList,
  { itemHeight: 40 }
)

// 2. 防抖搜索
const handleSearch = debounce(async (keyword: string) => {
  loading.value = true
  try {
    await fetchDictList({ keyword })
  } finally {
    loading.value = false
  }
}, 300)

// 3. 计算属性缓存
const enabledDictTypes = computed(() =>
  dictTypeList.value.filter(dt => dt.status === 'enabled')
)

// 4. 定时清理过期缓存
setInterval(() => {
  dictCacheManager.clearExpired()
}, 60000) // 每分钟清理一次
```

### 5.3 权限控制实现

```typescript
// 数据权限
const dictPermissions = {
  // 系统管理员: 完全权限
  admin: {
    canView: 'all',
    canEdit: 'all',
    canDelete: true,
    canImportExport: true
  },

  // 部门管理员: 仅查看
  department_manager: {
    canView: 'all',
    canEdit: false,
    canDelete: false,
    canImportExport: false
  },

  // 普通用户: 仅查看
  user: {
    canView: 'all',
    canEdit: false,
    canDelete: false,
    canImportExport: false
  }
}

// 权限判断函数
function canEditDictType(currentUser: User, dictType: DictType): boolean {
  const permission = dictPermissions[currentUser.role]

  if (permission.canEdit === 'all') return true
  if (permission.canEdit === false) return false

  return false
}

function canDeleteDictType(currentUser: User, dictType: DictType): boolean {
  // 系统字典不可删除
  if (dictType.category === 'system') return false

  const permission = dictPermissions[currentUser.role]
  return permission.canDelete === true
}
```

---

## 附录

### A. 工具函数

```typescript
// src/utils/format.ts
import { formatDate, debounce, throttle } from '@/utils/format'

// 日期格式化
formatDate(new Date(), 'YYYY-MM-DD HH:mm:ss')  // 2026-01-09 10:30:00

// 防抖函数
debounce(fn, delay)

// 节流函数
throttle(fn, delay)
```

### B. 开发检查清单

**编码阶段**:
- [ ] 完成DictType和DictItem类型定义
- [ ] 完成API接口封装
- [ ] 完成Store实现
- [ ] 完成字典类型列表页
- [ ] 完成字典项列表页
- [ ] 完成字典表单组件
- [ ] 完成字典树组件
- [ ] 完成缓存机制

**测试阶段**:
- [ ] 字典类型CRUD功能测试
- [ ] 字典项CRUD功能测试
- [ ] 搜索筛选测试
- [ ] 排序功能测试
- [ ] 缓存机制测试
- [ ] 权限控制测试
- [ ] 导入导出测试

**性能测试**:
- [ ] 1000+字典项数据测试
- [ ] 缓存命中率测试
- [ ] 大数据量排序测试

---

**文档版本**: v1.0.0
**创建人**: AI开发助手
**最后更新**: 2026-01-09
