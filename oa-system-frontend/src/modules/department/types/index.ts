/**
 * 部门管理模块类型定义
 * 基于 department_Technical.md 规范
 */

// ==================== 枚举类型 ====================

/**
 * 部门状态枚举
 */
export enum DepartmentStatus {
  ACTIVE = 'active',        // 正常
  DISABLED = 'disabled'     // 停用
}

// ==================== 部门相关类型 ====================

/**
 * 部门信息
 */
export interface Department {
  /** 部门编号 - 唯一标识,格式: DEPT+序号 */
  id: string

  /** 部门名称 */
  name: string

  /** 部门简称 */
  shortName?: string

  /** 上级部门ID */
  parentId?: string | null

  /** 部门负责人ID */
  leaderId?: string

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

  /** 状态 */
  status: DepartmentStatus

  /** 创建时间 */
  createdAt: string

  /** 更新时间 */
  updatedAt: string

  /** 子部门列表(树形结构时使用) */
  children?: Department[]

  /** 部门人数(虚拟字段,从员工表汇总) */
  employeeCount?: number

  /** 部门负责人信息(关联查询) */
  leader?: {
    id: string
    name: string
    avatar?: string
  }
}

/**
 * 部门筛选条件
 */
export interface DepartmentFilter {
  /** 关键词搜索(名称/简称) */
  keyword?: string

  /** 状态筛选 */
  status?: DepartmentStatus

  /** 负责人筛选 */
  leaderId?: string

  /** 层级筛选 */
  level?: number
}

/**
 * 部门表单数据
 */
export interface DepartmentForm {
  /** 部门名称 */
  name: string

  /** 部门简称 */
  shortName?: string

  /** 上级部门ID */
  parentId?: string | null

  /** 部门负责人ID */
  leaderId?: string

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
export interface MoveDepartmentRequest {
  /** 部门ID */
  departmentId: string

  /** 新的上级部门ID */
  newParentId: string | null
}

/**
 * 部门统计数据
 */
export interface DepartmentStatistics {
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

// ==================== API 响应类型 ====================

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
 * API 响应基础结构
 */
export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
}

// ==================== 员工类型(用于关联) ====================

/**
 * 员工信息(简化版)
 */
export interface Employee {
  id: string
  name: string
  departmentId?: string
  avatar?: string
}

// ==================== 数据字典类型 ====================

/**
 * 数据字典项
 */
export interface DictItem {
  label: string
  value: string
  color?: string
  icon?: string
  sort?: number
}

// ==================== 错误码类型 ====================

/**
 * 部门错误码
 */
export enum DepartmentErrorCode {
  NAME_DUPLICATE = 'DEPT_001',           // 部门名称重复
  PARENT_NOT_FOUND = 'DEPT_002',          // 上级部门不存在
  LEVEL_EXCEEDED = 'DEPT_003',            // 层级超限
  HAS_CHILDREN = 'DEPT_004',              // 有子部门
  HAS_EMPLOYEES = 'DEPT_005',             // 有成员
  INVALID_PARENT = 'DEPT_006',            // 无效的上级部门
  LEADER_NOT_FOUND = 'DEPT_007'           // 负责人不存在
}

// ==================== 权限类型 ====================

/**
 * 部门权限配置
 */
export interface DepartmentPermissionConfig {
  canViewAll: boolean              // 查看所有部门
  canViewDepartment: boolean       // 查看本部门及下级部门
  canCreate: boolean               // 创建部门
  canEdit: boolean                 // 编辑部门
  canEditAll: boolean              // 编辑所有信息(含层级排序)
  canDelete: boolean               // 删除部门
  canMove: boolean                 // 移动部门
  canExport: boolean               // 导出列表
  canManageMembers: boolean        // 管理成员
}

// ==================== ECharts 数据类型 ====================

/**
 * ECharts 图节点
 */
export interface GraphNode {
  id: string
  name: string
  value: number
  category: number
  [key: string]: any
}

/**
 * ECharts 图链接
 */
export interface GraphLink {
  source: string
  target: string
}

/**
 * ECharts 图数据
 */
export interface GraphData {
  nodes: GraphNode[]
  links: GraphLink[]
}
