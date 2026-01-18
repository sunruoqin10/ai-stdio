/**
 * 资产管理模块类型定义
 * @module asset/types
 */

/**
 * 资产类别
 */
export type AssetCategory = 'electronic' | 'furniture' | 'book' | 'other'

/**
 * 资产状态
 */
export type AssetStatus = 'stock' | 'in_use' | 'borrowed' | 'maintenance' | 'scrapped'

/**
 * 资产实体
 */
export interface Asset {
  id: string // 资产编号: ASSET+序号
  name: string // 资产名称
  category: AssetCategory // 类别
  brandModel?: string // 品牌/型号
  purchaseDate: string // 购置日期 (ISO 8601格式)
  purchasePrice: number // 购置金额 (单位: 元)
  currentValue?: number // 当前价值(自动计算)
  status: AssetStatus // 资产状态
  userId?: string // 使用人/保管人ID
  userName?: string // 使用人/保管人姓名
  userAvatar?: string // 使用人头像
  location?: string // 存放位置
  borrowDate?: string // 借出日期
  expectedReturnDate?: string // 预计归还日期
  actualReturnDate?: string // 实际归还日期
  maintenanceRecord?: string // 维护记录 (JSON字符串)
  images?: string[] // 资产图片URL数组
  notes?: string // 备注信息
  createdAt: string // 创建时间
  updatedAt: string // 更新时间
  createdBy?: string // 创建人ID
  updatedBy?: string // 更新人ID
  version?: number // 乐观锁版本号
}

/**
 * 资产筛选条件
 */
export interface AssetFilter {
  keyword?: string // 关键字搜索(名称/编号)
  category?: AssetCategory // 类别筛选
  status?: AssetStatus // 状态筛选
  userId?: string // 使用人筛选
  startDate?: string // 购置开始日期
  endDate?: string // 购置结束日期
  minPrice?: number // 最低价格
  maxPrice?: number // 最高价格
}

/**
 * 分页响应
 */
export interface PaginationResponse<T> {
  list: T[]
  total: number
  page: number
  pageSize: number
}

/**
 * 资产表单
 */
export interface AssetForm {
  id?: string
  name?: string
  category?: AssetCategory
  status?: AssetStatus
  brandModel?: string
  purchaseDate?: string
  purchasePrice?: number
  currentValue?: number
  location?: string
  images?: string[]
  notes?: string
  version?: number
}

/**
 * 借还记录
 */
export interface BorrowRecord {
  id: string // 记录ID
  assetId: string // 资产ID
  assetName: string // 资产名称(快照)
  borrowerId: string // 借用人ID
  borrowerName: string // 借用人姓名(快照)
  borrowDate: string // 借出日期
  expectedReturnDate: string // 预计归还日期
  actualReturnDate?: string // 实际归还日期
  status: 'active' | 'returned' | 'overdue' // 记录状态
  condition?: 'good' | 'damaged' | 'lost' // 归还时资产状态
  notes?: string // 备注
  createdAt: string
  updatedAt: string
}

/**
 * 借用表单
 */
export interface BorrowForm {
  borrowerId: string
  borrowDate: string // 借出日期
  expectedReturnDate: string
  notes?: string
}

/**
 * 归还表单
 */
export interface ReturnForm {
  actualReturnDate: string // 实际归还日期
  notes?: string
}

/**
 * 维护记录
 */
export interface MaintenanceRecord {
  id: string // 记录ID
  assetId: string // 资产ID
  type: 'repair' | 'maintenance' | 'check' // 类型
  description: string // 描述
  cost?: number // 费用
  startDate: string // 开始日期
  endDate?: string // 结束日期
  operatorId: string // 操作人ID
  operatorName: string // 操作人姓名
  createdAt: string
}

/**
 * 资产统计
 */
export interface AssetStatistics {
  total: number // 总资产数
  byCategory: {
    electronic: number
    furniture: number
    book: number
    other: number
  }
  byStatus: {
    stock: number
    in_use: number
    borrowed: number
    maintenance: number
    scrapped: number
  }
  totalValue: number // 总购置金额
  currentValue: number // 总当前价值
  borrowedCount: number // 借出数量
  maintenanceCount: number // 维修中数量
}

/**
 * 折旧趋势
 */
export interface DepreciationTrend {
  months: string[]
  values: number[]
}

/**
 * 借出趋势
 */
export interface BorrowTrend {
  months: string[]
  counts: number[]
}

/**
 * 批量导入结果
 */
export interface ImportResult {
  successCount: number
  failCount: number
  errors: Array<{ row: number; message: string }>
}

/**
 * 视图类型
 */
export type AssetViewType = 'table' | 'kanban' | 'gallery'
