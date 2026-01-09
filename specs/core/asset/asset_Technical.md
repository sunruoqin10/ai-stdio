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

**文档版本**: v1.0.0
