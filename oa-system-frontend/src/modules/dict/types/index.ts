/**
 * 数据字典模块类型定义
 * @module dict/types
 */

/**
 * 字典类别枚举
 */
export enum DictCategory {
  SYSTEM = 'system',      // 系统字典
  BUSINESS = 'business'   // 业务字典
}

/**
 * 字典状态枚举
 */
export enum DictStatus {
  ENABLED = 'enabled',    // 启用
  DISABLED = 'disabled'   // 禁用
}

/**
 * 字典颜色类型枚举
 */
export enum DictColorType {
  PRIMARY = 'primary',    // 主要色
  SUCCESS = 'success',    // 成功色
  WARNING = 'warning',    // 警告色
  DANGER = 'danger',      // 危险色
  INFO = 'info'           // 信息色
}

/**
 * 字典类型
 */
export interface DictType {
  /** 字典类型ID - 唯一标识 */
  id: number

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
export interface DictItem {
  /** 字典项ID - 唯一标识 */
  id: number

  /** 所属字典类型ID */
  dictTypeId: number

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
export interface DictTypeForm {
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
export interface DictItemForm {
  /** 所属字典类型编码 */
  dictTypeCode: string

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
export interface DictFilter {
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
export interface DictTreeNode {
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
export interface DictData {
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

/**
 * 分页响应数据
 */
export interface PageResponse<T> {
  list: T[]
  total: number
  page: number
  pageSize: number
}

/**
 * API响应基础结构
 */
export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
}

/**
 * 字典类型列表响应
 */
export type DictTypeListResponse = ApiResponse<PageResponse<DictType>>

/**
 * 字典项列表响应
 */
export type DictItemListResponse = ApiResponse<PageResponse<DictItem>>

/**
 * 字典树响应
 */
export type DictTreeResponse = ApiResponse<DictTreeNode[]>

/**
 * 字典数据响应
 */
export type DictDataResponse = ApiResponse<DictData>
