# 资产管理模块 - 技术实现规范

> **文档类型**: 技术实现规范
> **模块**: 资产管理
> **创建日期**: 2026-01-09
> **版本**: v1.0.0

---

## 1. 数据结构

### 1.1 资产实体 (Asset)

```typescript
interface Asset {
  id: string                    // 资产编号: ASSET+序号
  name: string                  // 资产名称
  category: AssetCategory       // 类别
  brandModel?: string           // 品牌/型号
  purchaseDate: string          // 购置日期 (ISO 8601格式)
  purchasePrice: number         // 购置金额 (单位: 元)
  currentValue?: number         // 当前价值(自动计算)
  status: AssetStatus           // 资产状态
  userId?: string               // 使用人/保管人ID
  location?: string             // 存放位置
  borrowDate?: string           // 借出日期
  expectedReturnDate?: string   // 预计归还日期
  actualReturnDate?: string     // 实际归还日期
  maintenanceRecord?: string    // 维护记录 (JSON字符串)
  images?: string[]             // 资产图片URL数组
  notes?: string                // 备注信息
  createdAt: string             // 创建时间
  updatedAt: string             // 更新时间
  createdBy?: string            // 创建人ID
  updatedBy?: string            // 更新人ID
}

type AssetCategory = 'electronic' | 'furniture' | 'book' | 'other'

type AssetStatus = 'stock' | 'in_use' | 'borrowed' | 'maintenance' | 'scrapped'

interface AssetFilter {
  keyword?: string              // 关键字搜索(名称/编号)
  category?: AssetCategory      // 类别筛选
  status?: AssetStatus          // 状态筛选
  userId?: string               // 使用人筛选
  startDate?: string            // 购置开始日期
  endDate?: string              // 购置结束日期
  minPrice?: number             // 最低价格
  maxPrice?: number             // 最高价格
}
```

### 1.2 借还记录 (BorrowRecord)

```typescript
interface BorrowRecord {
  id: string                    // 记录ID
  assetId: string               // 资产ID
  assetName: string             // 资产名称(快照)
  borrowerId: string            // 借用人ID
  borrowerName: string          // 借用人姓名(快照)
  borrowDate: string            // 借出日期
  expectedReturnDate: string    // 预计归还日期
  actualReturnDate?: string     // 实际归还日期
  status: 'active' | 'returned' | 'overdue'  // 记录状态
  notes?: string                // 备注
  createdAt: string
  updatedAt: string
}
```

### 1.3 维护记录 (MaintenanceRecord)

```typescript
interface MaintenanceRecord {
  id: string                    // 记录ID
  assetId: string               // 资产ID
  type: 'repair' | 'maintenance' | 'check'  // 类型
  description: string           // 描述
  cost?: number                 // 费用
  startDate: string             // 开始日期
  endDate?: string              // 结束日期
  operatorId: string            // 操作人ID
  operatorName: string          // 操作人姓名
  createdAt: string
}
```

---

## 1.4 Mock数据实现

资产管理模块提供了完整的Mock数据实现,便于前端独立开发和测试。

**Mock数据位置**:
```
src/modules/asset/mock/
└── data.ts          # Mock资产数据
```

**Mock数据结构**:

```typescript
// src/modules/asset/mock/data.ts
import { mockEmployees } from '@/modules/employee/mock/data'

export const mockAssets: Asset[] = [
  {
    id: 'ASSET000001',
    name: 'MacBook Pro 16寸',
    category: 'electronic',
    brandModel: 'Apple M3 Max',
    purchaseDate: '2024-01-15T00:00:00.000Z',
    purchasePrice: 24999,
    currentValue: 21249.15,  // 自动计算折旧
    status: 'stock',
    location: 'A座3楼办公室',
    images: ['https://placehold.co/400x300'],
    createdAt: '2024-01-15T00:00:00.000Z',
    updatedAt: '2024-01-15T00:00:00.000Z'
  },
  // ... 12个预置资产,涵盖4种类别
]

export const mockBorrowRecords: BorrowRecord[] = [
  {
    id: 'BR1704931200000',
    assetId: 'ASSET000005',
    assetName: 'ThinkPad X1 Carbon',
    borrowerId: 'EMP20250111001',
    borrowerName: '张三',
    borrowDate: '2025-01-10T00:00:00.000Z',
    expectedReturnDate: '2025-01-20T00:00:00.000Z',
    status: 'active',
    createdAt: '2025-01-10T00:00:00.000Z',
    updatedAt: '2025-01-10T00:00:00.000Z'
  },
  // ... 借还记录数据
]

export const mockStatistics: AssetStatistics = {
  total: 12,
  byCategory: {
    electronic: 5,
    furniture: 3,
    book: 2,
    other: 2
  },
  byStatus: {
    stock: 6,
    in_use: 2,
    borrowed: 2,
    maintenance: 1,
    scrapped: 1
  },
  totalValue: 150000,
  currentValue: 135000,
  borrowedCount: 2,
  maintenanceCount: 1
}
```

**Mock API实现示例**:

```typescript
// src/modules/asset/api/index.ts
import { mockAssets, mockBorrowRecords, mockStatistics } from '../mock/data'
import { calculateCurrentValue, generateAssetId } from '../utils'

// 模拟延迟
function delay(ms: number = 500): Promise<void> {
  return new Promise(resolve => setTimeout(resolve, ms))
}

/**
 * 获取资产列表
 */
export async function getAssets(
  params?: AssetFilter & { page?: number; pageSize?: number }
): Promise<PaginationResponse<Asset> | Asset[]> {
  await delay(300)

  let filteredAssets = [...mockAssets]

  // 筛选
  if (params?.keyword) {
    const keyword = params.keyword.toLowerCase()
    filteredAssets = filteredAssets.filter(
      asset =>
        asset.name.toLowerCase().includes(keyword) ||
        asset.id.toLowerCase().includes(keyword)
    )
  }

  if (params?.category) {
    filteredAssets = filteredAssets.filter(asset => asset.category === params.category)
  }

  if (params?.status) {
    filteredAssets = filteredAssets.filter(asset => asset.status === params.status)
  }

  // 分页
  if (params?.page !== undefined && params?.pageSize !== undefined) {
    const start = (params.page - 1) * params.pageSize
    const end = start + params.pageSize
    const paginatedList = filteredAssets.slice(start, end)

    return {
      list: paginatedList,
      total: filteredAssets.length,
      page: params.page,
      pageSize: params.pageSize
    }
  }

  return filteredAssets
}

/**
 * 创建资产
 */
export async function createAsset(data: AssetForm): Promise<Asset> {
  await delay(400)

  // 生成新ID
  const newId = generateAssetId(mockAssets.length + 1)

  const newAsset: Asset = {
    id: newId,
    name: data.name!,
    category: data.category!,
    brandModel: data.brandModel,
    purchaseDate: data.purchaseDate!,
    purchasePrice: data.purchasePrice!,
    currentValue: calculateCurrentValue(data.purchasePrice!, data.purchaseDate!),
    status: 'stock',
    location: data.location,
    images: data.images,
    notes: data.notes,
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString()
  }

  mockAssets.unshift(newAsset)
  return newAsset
}

/**
 * 借出资产
 */
export async function borrowAsset(
  id: string,
  data: BorrowForm
): Promise<{ asset: Asset; borrowRecord: BorrowRecord }> {
  await delay(400)

  const index = mockAssets.findIndex(a => a.id === id)
  if (index === -1) {
    throw new Error('资产不存在')
  }

  const asset = mockAssets[index]
  if (asset.status !== 'stock') {
    throw new Error('资产状态不允许借用')
  }

  // 更新资产状态
  const updatedAsset: Asset = {
    ...asset,
    status: 'borrowed',
    userId: data.borrowerId,
    borrowDate: new Date().toISOString(),
    expectedReturnDate: data.expectedReturnDate,
    updatedAt: new Date().toISOString()
  }

  mockAssets[index] = updatedAsset

  // 创建借还记录
  const borrowRecord: BorrowRecord = {
    id: `BR${Date.now()}`,
    assetId: asset.id,
    assetName: asset.name,
    borrowerId: data.borrowerId,
    borrowerName: data.borrowerId, // 实际应该从用户服务获取
    borrowDate: new Date().toISOString(),
    expectedReturnDate: data.expectedReturnDate,
    status: 'active',
    notes: data.notes,
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString()
  }

  mockBorrowRecords.unshift(borrowRecord)

  return { asset: updatedAsset, borrowRecord }
}

/**
 * 获取统计数据
 */
export async function getStatistics(): Promise<AssetStatistics> {
  await delay(300)
  return { ...mockStatistics }
}

/**
 * 获取折旧趋势
 */
export async function getDepreciationTrend(months: number = 12): Promise<DepreciationTrend> {
  await delay(300)

  const monthData: string[] = []
  const values: number[] = []

  const now = new Date()
  for (let i = months - 1; i >= 0; i--) {
    const date = new Date(now.getFullYear(), now.getMonth() - i, 1)
    monthData.push(`${date.getMonth() + 1}月`)
    values.push(Math.round(100000 + Math.random() * 50000))
  }

  return { months: monthData, values }
}
```

**Mock数据特点**:
- 12个预置资产,涵盖4种类别(电子设备、办公家具、图书资料、其他)
- 5种资产状态(库存中、使用中、已借出、维修中、已报废)
- 完整的借还记录数据
- 统计数据预置
- 支持完整CRUD操作
- 实现搜索和筛选功能
- 支持分页
- 完整的状态转换验证
- 自动折旧计算
- 到期提醒功能
- 逾期检测
- 统计图表数据

---

## 2. API接口设计

### 2.1 资产管理接口

#### 获取资产列表
```http
GET /api/assets
Query Parameters:
  - keyword?: string
  - category?: AssetCategory
  - status?: AssetStatus
  - userId?: string
  - page: number (default: 1)
  - pageSize: number (default: 20)
  - view: 'table' | 'kanban' | 'gallery'

Response:
{
  "success": true,
  "data": {
    "list": Asset[],
    "total": number,
    "page": number,
    "pageSize": number
  }
}
```

#### 获取资产详情
```http
GET /api/assets/:id

Response:
{
  "success": true,
  "data": Asset
}
```

#### 创建资产
```http
POST /api/assets
Body:
{
  "name": string,
  "category": AssetCategory,
  "brandModel"?: string,
  "purchaseDate": string,
  "purchasePrice": number,
  "location"?: string,
  "images"?: string[],
  "notes"?: string
}

Response:
{
  "success": true,
  "data": Asset  // 返回自动生成的id和currentValue
}
```

#### 更新资产
```http
PUT /api/assets/:id
Body:
{
  "name"?: string,
  "category"?: AssetCategory,
  "brandModel"?: string,
  "purchaseDate"?: string,
  "purchasePrice"?: number,
  "location"?: string,
  "images"?: string[],
  "notes"?: string
}

Response:
{
  "success": true,
  "data": Asset
}
```

#### 删除资产
```http
DELETE /api/assets/:id

Response:
{
  "success": true,
  "message": "删除成功"
}
```

#### 批量导入资产
```http
POST /api/assets/import
Content-Type: multipart/form-data
Body: FormData with file (Excel/CSV)

Response:
{
  "success": true,
  "data": {
    "successCount": number,
    "failCount": number,
    "errors": Array<{row: number, message: string}>
  }
}
```

### 2.2 借还管理接口

#### 借出资产
```http
POST /api/assets/:id/borrow
Body:
{
  "borrowerId": string,
  "expectedReturnDate": string,  // ISO 8601格式
  "notes"?: string
}

Response:
{
  "success": true,
  "data": {
    "asset": Asset,
    "borrowRecord": BorrowRecord
  }
}
```

#### 归还资产
```http
POST /api/assets/:id/return
Body:
{
  "notes"?: string,
  "condition": 'good' | 'damaged' | 'lost'
}

Response:
{
  "success": true,
  "data": {
    "asset": Asset,
    "borrowRecord": BorrowRecord
  }
}
```

#### 获取借还历史
```http
GET /api/assets/:id/borrow-history
Query Parameters:
  - page?: number
  - pageSize?: number

Response:
{
  "success": true,
  "data": {
    "list": BorrowRecord[],
    "total": number
  }
}
```

#### 获取即将到期资产
```http
GET /api/assets/due-soon
Query Parameters:
  - days: number (default: 3)

Response:
{
  "success": true,
  "data": Asset[]
}
```

#### 获取逾期资产
```http
GET /api/assets/overdue

Response:
{
  "success": true,
  "data": Asset[]
}
```

### 2.3 状态管理接口

#### 更新资产状态
```http
PATCH /api/assets/:id/status
Body:
{
  "status": AssetStatus,
  "notes"?: string
}

Response:
{
  "success": true,
  "data": Asset
}
```

#### 批量更新状态
```http
PATCH /api/assets/status
Body:
{
  "ids": string[],
  "status": AssetStatus,
  "notes"?: string
}

Response:
{
  "success": true,
  "data": {
    "updatedCount": number
  }
}
```

### 2.4 统计分析接口

#### 获取资产统计
```http
GET /api/assets/statistics
Query Parameters:
  - startDate?: string
  - endDate?: string

Response:
{
  "success": true,
  "data": {
    "total": number,
    "byCategory": {
      "electronic": number,
      "furniture": number,
      "book": number,
      "other": number
    },
    "byStatus": {
      "stock": number,
      "in_use": number,
      "borrowed": number,
      "maintenance": number,
      "scrapped": number
    },
    "totalValue": number,
    "currentValue": number,
    "borrowedCount": number,
    "maintenanceCount": number
  }
}
```

#### 获取折旧趋势
```http
GET /api/assets/depreciation-trend
Query Parameters:
  - months: number (default: 12)

Response:
{
  "success": true,
  "data": {
    "months": string[],
    "values": number[]
  }
}
```

#### 获取借出趋势
```http
GET /api/assets/borrow-trend
Query Parameters:
  - months: number (default: 12)

Response:
{
  "success": true,
  "data": {
    "months": string[],
    "counts": number[]
  }
}
```

---

## 3. 验证规则

### 3.1 前端验证 (使用Element Plus Form)

```typescript
const assetFormRules = {
  name: [
    { required: true, message: '请输入资产名称', trigger: 'blur' },
    { min: 2, max: 100, message: '长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  category: [
    { required: true, message: '请选择资产类别', trigger: 'change' }
  ],
  purchaseDate: [
    { required: true, message: '请选择购置日期', trigger: 'change' },
    {
      validator: (rule, value, callback) => {
        if (new Date(value) > new Date()) {
          callback(new Error('购置日期不能晚于今天'))
        } else {
          callback()
        }
      },
      trigger: 'change'
    }
  ],
  purchasePrice: [
    { required: true, message: '请输入购置金额', trigger: 'blur' },
    {
      type: 'number',
      min: 0.01,
      message: '金额必须大于0',
      trigger: 'blur'
    }
  ],
  borrowDate: [
    { required: true, message: '请选择借出日期', trigger: 'change' }
  ],
  expectedReturnDate: [
    { required: true, message: '请选择预计归还日期', trigger: 'change' },
    {
      validator: (rule, value, callback) => {
        const borrowDate = formData.borrowDate
        if (borrowDate && new Date(value) <= new Date(borrowDate)) {
          callback(new Error('归还日期必须晚于借出日期'))
        } else {
          const maxDate = new Date(borrowDate)
          maxDate.setDate(maxDate.getDate() + 30)
          if (new Date(value) > maxDate) {
            callback(new Error('借用期限最长为30天'))
          } else {
            callback()
          }
        }
      },
      trigger: 'change'
    }
  ]
}
```

### 3.2 后端验证 (Node.js + Joi示例)

```typescript
import Joi from 'joi'

// 创建资产验证
const createAssetSchema = Joi.object({
  name: Joi.string().min(2).max(100).required(),
  category: Joi.string().valid('electronic', 'furniture', 'book', 'other').required(),
  brandModel: Joi.string().max(100).optional(),
  purchaseDate: Joi.date().max('now').required(),
  purchasePrice: Joi.number().positive().required(),
  location: Joi.string().max(200).optional(),
  images: Joi.array().items(Joi.string().uri()).optional(),
  notes: Joi.string().max(500).optional()
})

// 借出资产验证
const borrowAssetSchema = Joi.object({
  borrowerId: Joi.string().required(),
  expectedReturnDate: Joi.date().greater(Joi.ref('borrowDate')).required(),
  notes: Joi.string().max(200).optional()
})

// 更新状态验证
const updateStatusSchema = Joi.object({
  status: Joi.string().valid('stock', 'in_use', 'borrowed', 'maintenance', 'scrapped').required(),
  notes: Joi.string().max(500).optional()
}).custom((value, helpers) => {
  // 验证状态转换是否合法
  const currentStatus = helpers.state.ancestors[0].status
  if (!isValidStatusTransition(currentStatus, value.status)) {
    throw new Error(`不能从状态 ${currentStatus} 变更为 ${value.status}`)
  }
  return value
})
```

---

## 4. 算法实现

### 4.1 折旧计算算法

```typescript
/**
 * 计算资产当前价值
 * @param purchasePrice 购置金额
 * @param purchaseDate 购置日期
 * @param depreciationRate 月折旧率 (默认1%)
 * @returns 当前价值
 */
function calculateCurrentValue(
  purchasePrice: number,
  purchaseDate: string,
  depreciationRate: number = 0.01
): number {
  const purchase = new Date(purchaseDate)
  const now = new Date()

  // 计算月份差异
  const monthsDiff = (now.getFullYear() - purchase.getFullYear()) * 12 +
                     (now.getMonth() - purchase.getMonth())

  // 购置当月不计折旧
  const months = Math.max(0, monthsDiff - 1)

  // 计算折旧
  const currentValue = purchasePrice * (1 - depreciationRate * months)

  // 不会出现负值,保留两位小数
  return Math.max(0, Math.round(currentValue * 100) / 100)
}

/**
 * 批量计算资产当前价值
 */
function batchCalculateCurrentValue(assets: Asset[]): Asset[] {
  return assets.map(asset => ({
    ...asset,
    currentValue: calculateCurrentValue(
      asset.purchasePrice,
      asset.purchaseDate
    )
  }))
}
```

### 4.2 到期提醒算法

```typescript
/**
 * 检查是否需要提醒归还
 * @param asset 资产对象
 * @param reminderDays 提前提醒天数 (默认3天)
 * @returns 是否需要提醒
 */
function checkReturnReminder(
  asset: Asset,
  reminderDays: number = 3
): boolean {
  // 只有已借出且有预计归还日期的资产才需要检查
  if (asset.status !== 'borrowed' || !asset.expectedReturnDate) {
    return false
  }

  const today = new Date()
  today.setHours(0, 0, 0, 0)

  const dueDate = new Date(asset.expectedReturnDate)
  dueDate.setHours(0, 0, 0, 0)

  // 计算距离到期的天数
  const daysUntilDue = Math.floor((dueDate.getTime() - today.getTime()) / (1000 * 60 * 60 * 24))

  // 在提醒范围内(0到reminderDays天)
  return daysUntilDue >= 0 && daysUntilDue <= reminderDays
}

/**
 * 获取需要提醒的资产列表
 */
function getAssetsNeedingReminder(
  assets: Asset[],
  reminderDays: number = 3
): Asset[] {
  return assets.filter(asset => checkReturnReminder(asset, reminderDays))
}

/**
 * 检查是否逾期
 */
function checkOverdue(asset: Asset): boolean {
  if (asset.status !== 'borrowed' || !asset.expectedReturnDate) {
    return false
  }

  const today = new Date()
  today.setHours(0, 0, 0, 0)

  const dueDate = new Date(asset.expectedReturnDate)
  dueDate.setHours(0, 0, 0, 0)

  return today > dueDate
}

/**
 * 获取逾期资产列表
 */
function getOverdueAssets(assets: Asset[]): Asset[] {
  return assets.filter(asset => checkOverdue(asset))
}
```

### 4.3 资产编号生成算法

```typescript
/**
 * 生成资产编号
 * @param serialNumber 序号
 * @returns 资产编号 (格式: ASSET000001)
 */
function generateAssetId(serialNumber: number): string {
  const prefix = 'ASSET'
  const paddedNumber = String(serialNumber).padStart(6, '0')
  return `${prefix}${paddedNumber}`
}

/**
 * 批量生成资产编号
 */
async function batchGenerateAssetId(count: number): Promise<string[]> {
  // 获取当前最大序号
  const lastAsset = await Asset.findOne().sort({ id: -1 })
  const lastSerialNumber = lastAsset
    ? parseInt(lastAsset.id.replace('ASSET', ''))
    : 0

  const ids: string[] = []
  for (let i = 1; i <= count; i++) {
    ids.push(generateAssetId(lastSerialNumber + i))
  }

  return ids
}
```

---

## 5. 自动化功能

### 5.1 定时任务设计

```typescript
/**
 * 每日到期提醒任务
 * 每天上午9点执行
 */
schedule.scheduleJob('0 9 * * *', async () => {
  console.log('开始执行资产到期提醒任务...')

  // 获取所有已借出的资产
  const borrowedAssets = await Asset.find({ status: 'borrowed' })

  // 筛选需要提醒的资产
  const assetsNeedingReminder = getAssetsNeedingReminder(borrowedAssets, 3)

  // 发送提醒
  for (const asset of assetsNeedingReminder) {
    await sendReminderNotification(asset)
  }

  console.log(`提醒任务完成,共提醒 ${assetsNeedingReminder.length} 条资产`)
})

/**
 * 每日逾期检查任务
 * 每天上午10点执行
 */
schedule.scheduleJob('0 10 * * *', async () => {
  console.log('开始执行资产逾期检查任务...')

  // 获取所有已借出的资产
  const borrowedAssets = await Asset.find({ status: 'borrowed' })

  // 筛选逾期资产
  const overdueAssets = getOverdueAssets(borrowedAssets)

  // 更新借还记录状态
  for (const asset of overdueAssets) {
    await BorrowRecord.updateOne(
      { assetId: asset.id, status: 'active' },
      { status: 'overdue' }
    )

    // 通知管理员
    await sendOverdueNotification(asset)
  }

  console.log(`逾期检查完成,共 ${overdueAssets.length} 条资产逾期`)
})

/**
 * 每日自动计算折旧
 * 每天凌晨1点执行
 */
schedule.scheduleJob('0 1 * * *', async () => {
  console.log('开始执行资产折旧计算任务...')

  // 获取所有未报废的资产
  const assets = await Asset.find({ status: { $ne: 'scrapped' } })

  // 更新当前价值
  for (const asset of assets) {
    const currentValue = calculateCurrentValue(
      asset.purchasePrice,
      asset.purchaseDate
    )

    await Asset.updateOne(
      { id: asset.id },
      { currentValue }
    )
  }

  console.log(`折旧计算完成,共更新 ${assets.length} 条资产`)
})
```

### 5.2 通知服务实现

```typescript
interface NotificationService {
  sendReminderNotification(asset: Asset): Promise<void>
  sendOverdueNotification(asset: Asset): Promise<void>
  sendBorrowNotification(asset: Asset, borrowerId: string): Promise<void>
  sendReturnNotification(asset: Asset): Promise<void>
}

class AssetNotificationService implements NotificationService {
  async sendReminderNotification(asset: Asset): Promise<void> {
    const borrower = await User.findById(asset.userId)
    const daysLeft = Math.ceil(
      (new Date(asset.expectedReturnDate!).getTime() - Date.now()) /
      (1000 * 60 * 60 * 24)
    )

    await Notification.create({
      userId: asset.userId,
      title: '资产归还提醒',
      content: `您借用的资产【${asset.name}】将在${daysLeft}天后到期,请及时归还`,
      type: 'reminder',
      relatedId: asset.id
    })

    // 发送邮件或短信
    await this.sendEmail(borrower.email, '资产归还提醒', `...`)
  }

  async sendOverdueNotification(asset: Asset): Promise<void> {
    // 获取所有管理员
    const admins = await User.find({ role: 'admin' })

    for (const admin of admins) {
      await Notification.create({
        userId: admin.id,
        title: '资产逾期警告',
        content: `资产【${asset.name}】已逾期未归还,借用人: ${asset.userId}`,
        type: 'warning',
        relatedId: asset.id
      })
    }
  }

  async sendBorrowNotification(asset: Asset, borrowerId: string): Promise<void> {
    const borrower = await User.findById(borrowerId)

    await Notification.create({
      userId: borrowerId,
      title: '资产借用成功',
      content: `您已成功借用资产【${asset.name}】,请于${asset.expectedReturnDate}前归还`,
      type: 'info',
      relatedId: asset.id
    })
  }

  async sendReturnNotification(asset: Asset): Promise<void> {
    await Notification.create({
      userId: asset.userId!,
      title: '资产归还成功',
      content: `资产【${asset.name}】已成功归还`,
      type: 'success',
      relatedId: asset.id
    })
  }

  private async sendEmail(to: string, subject: string, content: string): Promise<void> {
    // 实现邮件发送逻辑
  }
}
```

---

## 6. 数据库索引设计

```typescript
// 资产集合索引
AssetSchema.index({ id: 1 }, { unique: true })
AssetSchema.index({ status: 1 })
AssetSchema.index({ category: 1 })
AssetSchema.index({ userId: 1 })
AssetSchema.index({ purchaseDate: -1 })
AssetSchema.index({ expectedReturnDate: 1 })
AssetSchema.index({ name: 'text', brandModel: 'text' }) // 全文搜索

// 借还记录索引
BorrowRecordSchema.index({ assetId: 1 })
BorrowRecordSchema.index({ borrowerId: 1 })
BorrowRecordSchema.index({ borrowDate: -1 })
BorrowRecordSchema.index({ status: 1 })

// 复合索引
AssetSchema.index({ status: 1, expectedReturnDate: 1 }) // 查询即将到期资产
```

---

## 7. 性能优化建议

### 7.1 缓存策略
- 资产列表缓存15分钟
- 统计数据缓存1小时
- 用户权限缓存30分钟

### 7.2 分页优化
- 默认每页20条,最大100条
- 使用游标分页处理大数据集

### 7.3 查询优化
- 避免使用正则表达式前缀通配符
- 热点数据使用Redis缓存
- 统计查询使用预聚合

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

### 8.3 资产模块中使用字典

```typescript
// src/modules/asset/composables/useDict.ts
import { useDictStore } from '@/modules/dict/store'

export function useAssetDict() {
  const dictStore = useDictStore()

  // 预加载资产模块所需字典
  onMounted(async () => {
    await dictStore.loadDicts([
      'asset_category',
      'asset_status'
    ])
  })

  // 获取字典选项
  const categoryOptions = computed(() => dictStore.getDictList('asset_category'))
  const statusOptions = computed(() => dictStore.getDictList('asset_status'))

  // 动态加载归还状态字典
  async function loadConditionOptions() {
    await dictStore.loadDicts(['asset_condition'])
    return dictStore.getDictList('asset_condition')
  }

  // 获取显示文本
  function getCategoryLabel(value: string): string {
    return dictStore.getLabel('asset_category', value)
  }

  function getStatusLabel(value: string): string {
    return dictStore.getLabel('asset_status', value)
  }

  function getConditionLabel(value: string): string {
    return dictStore.getLabel('asset_condition', value)
  }

  return {
    categoryOptions,
    statusOptions,
    loadConditionOptions,
    getCategoryLabel,
    getStatusLabel,
    getConditionLabel
  }
}
```

### 8.4 筛选面板中使用字典

```vue
<!-- src/modules/asset/components/AssetFilter.vue -->
<script setup lang="ts">
import { useAssetDict } from '../composables/useDict'

const {
  categoryOptions,
  statusOptions
} = useAssetDict()

const filterForm = ref({
  category: '',
  status: ''
})
</script>

<template>
  <el-form :model="filterForm">
    <el-form-item label="资产类别">
      <el-select v-model="filterForm.category" clearable>
        <el-option
          v-for="item in categoryOptions"
          :key="item.value"
          :label="item.label"
          :value="item.value"
        />
      </el-select>
    </el-form-item>

    <el-form-item label="资产状态">
      <el-select v-model="filterForm.status" clearable>
        <el-option
          v-for="item in statusOptions"
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

export function useAssetPermission() {
  const authStore = useAuthStore()

  /**
   * 检查资产管理权限
   */
  function hasPermission(permission: string): boolean {
    return authStore.hasPermission(permission)
  }

  /**
   * 过滤资产列表(数据权限 - 隐藏价格)
   */
  function filterAssetList(assets: Asset[]): Asset[] {
    if (hasPermission('asset:view_all')) {
      return assets // 返回所有资产(含价格)
    } else {
      // 普通员工:隐藏价格信息
      return assets.map(asset => ({
        ...asset,
        purchasePrice: undefined,
        currentValue: undefined
      }))
    }
  }

  /**
   * 检查是否可以编辑资产
   */
  function canEditAsset(asset: Asset): boolean {
    if (!hasPermission('asset:edit')) {
      return false
    }

    // 资产管理员可以编辑所有资产
    return true
  }

  /**
   * 检查是否可以借用资产
   */
  function canBorrowAsset(asset: Asset): boolean {
    if (!hasPermission('asset:borrow')) {
      return false
    }

    // 只有库存中的资产可以借用
    return asset.status === 'stock'
  }

  /**
   * 检查是否可以查看价格
   */
  function canViewPrice(): boolean {
    return hasPermission('asset:view_price')
  }

  return {
    hasPermission,
    filterAssetList,
    canEditAsset,
    canBorrowAsset,
    canViewPrice
  }
}
```

### 9.2 列表页权限控制

```vue
<!-- src/modules/asset/views/AssetList.vue -->
<script setup lang="ts">
import { useAssetPermission } from '../composables/useAssetPermission'

const {
  hasPermission,
  filterAssetList,
  canViewPrice
} = useAssetPermission()

// 权限判断
const canCreate = computed(() => hasPermission('asset:create'))
const canImport = computed(() => hasPermission('asset:import'))
const canExport = computed(() => hasPermission('asset:export'))
const canDelete = computed(() => hasPermission('asset:delete'))
const showPrice = computed(() => canViewPrice())

// 原始数据
const allAssets = ref<Asset[]>([])

// 应用数据权限过滤(隐藏价格)
const assets = computed(() => filterAssetList(allAssets.value))
</script>

<template>
  <div class="asset-list">
    <!-- 操作栏 -->
    <div class="toolbar">
      <el-button
        v-if="canCreate"
        type="primary"
        @click="handleCreate"
      >
        新增资产
      </el-button>

      <el-button
        v-if="canImport"
        @click="handleImport"
      >
        批量导入
      </el-button>

      <el-button
        v-if="canExport"
        @click="handleExport"
      >
        导出列表
      </el-button>
    </div>

    <!-- 资产表格 -->
    <el-table :data="assets">
      <el-table-column prop="name" label="资产名称" />
      <el-table-column prop="category" label="类别" />
      <el-table-column prop="brandModel" label="品牌型号" />
      <el-table-column prop="status" label="状态" />

      <!-- 价格列: 根据权限显示 -->
      <el-table-column v-if="showPrice" prop="purchasePrice" label="购置金额">
        <template #default="{ row }">
          ¥{{ row.purchasePrice?.toFixed(2) }}
        </template>
      </el-table-column>

      <el-table-column v-if="showPrice" prop="currentValue" label="当前价值">
        <template #default="{ row }">
          ¥{{ row.currentValue?.toFixed(2) }}
        </template>
      </el-table-column>

      <el-table-column label="操作" fixed="right">
        <template #default="{ row }">
          <el-button
            v-if="hasPermission('asset:borrow')"
            link
            @click="handleBorrow(row)"
            :disabled="row.status !== 'stock'"
          >
            借用
          </el-button>

          <el-button
            v-if="hasPermission('asset:edit')"
            link
            @click="handleEdit(row)"
          >
            编辑
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

### 9.3 详情页权限控制

```vue
<!-- src/modules/asset/views/AssetDetail.vue -->
<script setup lang="ts">
import { useAssetPermission } from '../composables/useAssetPermission'

const {
  canEditAsset,
  canViewPrice
} = useAssetPermission()

const asset = ref<Asset>(null)

// 权限判断
const canEdit = computed(() => asset.value && canEditAsset(asset.value))
const canMaintain = computed(() => hasPermission('asset:maintain'))
const canScrap = computed(() => hasPermission('asset:scrap'))
const showPriceInfo = computed(() => canViewPrice())
</script>

<template>
  <div class="asset-detail">
    <div class="detail-header">
      <h1>{{ asset?.name }}</h1>
      <div class="actions">
        <el-button
          v-if="canEdit"
          @click="handleEdit"
        >
          编辑
        </el-button>

        <el-button
          v-if="canMaintain"
          type="warning"
          @click="handleMaintain"
        >
          维护记录
        </el-button>

        <el-button
          v-if="canScrap"
          type="danger"
          @click="handleScrap"
        >
          报废
        </el-button>
      </div>
    </div>

    <!-- 基本信息 -->
    <el-card title="基本信息">
      <el-descriptions>
        <el-descriptions-item label="资产编号">{{ asset?.id }}</el-descriptions-item>
        <el-descriptions-item label="资产名称">{{ asset?.name }}</el-descriptions-item>
        <el-descriptions-item label="资产类别">{{ asset?.category }}</el-descriptions-item>
        <el-descriptions-item label="品牌型号">{{ asset?.brandModel }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ asset?.status }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 价格信息: 需要权限 -->
    <el-card v-if="showPriceInfo" title="价格信息">
      <el-descriptions>
        <el-descriptions-item label="购置金额">
          ¥{{ asset?.purchasePrice?.toFixed(2) }}
        </el-descriptions-item>
        <el-descriptions-item label="当前价值">
          ¥{{ asset?.currentValue?.toFixed(2) }}
        </el-descriptions-item>
        <el-descriptions-item label="购置日期">
          {{ asset?.purchaseDate }}
        </el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>
```

### 9.4 表单权限控制

```vue
<!-- src/modules/asset/components/AssetForm.vue -->
<script setup lang="ts">
import { useAssetPermission } from '../composables/useAssetPermission'

const {
  canViewPrice
} = useAssetPermission()

// 字段级权限
const showPriceFields = computed(() => canViewPrice())
const canEditPrice = computed(() => hasPermission('asset:edit'))
</script>

<template>
  <el-form>
    <!-- 基本信息 -->
    <el-form-item label="资产名称">
      <el-input v-model="form.name" />
    </el-form-item>

    <el-form-item label="资产类别">
      <el-select v-model="form.category">
        <el-option
          v-for="item in categoryOptions"
          :key="item.value"
          :label="item.label"
          :value="item.value"
        />
      </el-select>
    </el-form-item>

    <!-- 价格字段: 需要权限才能查看和编辑 -->
    <template v-if="showPriceFields">
      <el-form-item label="购置金额">
        <el-input-number
          v-model="form.purchasePrice"
          :disabled="!canEditPrice"
          :min="0"
          :precision="2"
        />
      </el-form-item>

      <el-form-item label="购置日期">
        <el-date-picker
          v-model="form.purchaseDate"
          :disabled="!canEditPrice"
        />
      </el-form-item>
    </template>
  </el-form>
</template>
```

### 9.5 API请求权限拦截

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
  if (url.includes('/assets')) {
    if (url.includes('POST')) return 'asset:create'
    if (url.includes('/borrow')) return 'asset:borrow'
    if (url.includes('/return')) return 'asset:return'
    if (url.includes('/scrap')) return 'asset:scrap'
    if (url.includes('/maintenance')) return 'asset:maintain'
    if (url.includes('PUT')) return 'asset:edit'
    if (url.includes('DELETE')) return 'asset:delete'
    if (url.includes('/import')) return 'asset:import'
    if (url.includes('/export')) return 'asset:export'
    if (url.includes('/statistics')) return 'asset:view_statistics'
    return 'asset:view'
  }
  return ''
}
```

---

**文档版本**: v1.1.0
**创建人**: AI开发助手
**最后更新**: 2026-01-10
