/**
 * 员工管理模块类型定义
 * 基于 employee_Technical.md 规范
 */

// ==================== 枚举类型 ====================

/**
 * 性别枚举
 */
export enum Gender {
  MALE = 'male',      // 男
  FEMALE = 'female'   // 女
}

/**
 * 员工状态枚举
 */
export enum EmployeeStatus {
  ACTIVE = 'active',        // 在职
  RESIGNED = 'resigned',    // 离职
  SUSPENDED = 'suspended'   // 停薪留职
}

/**
 * 试用期状态枚举
 */
export enum ProbationStatus {
  PROBATION = 'probation',  // 试用期内
  REGULAR = 'regular',      // 已转正
  RESIGNED = 'resigned'     // 已离职
}

// ==================== 员工相关类型 ====================

/**
 * 员工信息
 */
export interface Employee {
  /** 员工编号 - 唯一标识,格式: EMP+YYYYMMDD+序号 */
  id: string

  /** 基本信息 */
  name: string                    // 姓名
  englishName?: string            // 英文名
  gender: Gender                  // 性别
  birthDate?: string              // 出生日期
  phone: string                   // 联系电话
  email: string                   // 邮箱
  avatar?: string                 // 头像URL

  /** 工作信息 */
  departmentId: string            // 部门ID
  departmentName?: string         // 部门名称(关联查询)
  position: string                // 职位
  level?: string                  // 职级
  managerId?: string              // 直属上级ID
  managerName?: string            // 直属上级姓名(关联查询)
  joinDate: string                // 入职日期
  probationStatus?: ProbationStatus  // 试用期状态
  probationEndDate?: string       // 试用期结束日期
  workYears?: number              // 工龄(自动计算)

  /** 状态 */
  status: EmployeeStatus          // 员工状态

  /** 其他信息 */
  officeLocation?: string         // 办公位置
  emergencyContact?: string       // 紧急联系人
  emergencyPhone?: string         // 紧急联系电话

  /** 系统字段 */
  createdAt: string
  updatedAt: string
}

/**
 * 员工筛选条件
 */
export interface EmployeeFilter {
  /** 关键词搜索(姓名/工号/手机号) */
  keyword?: string

  /** 员工状态 */
  status?: EmployeeStatus

  /** 部门筛选(多选) */
  departmentIds?: string[]

  /** 职位筛选 */
  position?: string

  /** 试用期状态 */
  probationStatus?: ProbationStatus

  /** 性别筛选 */
  gender?: Gender

  /** 入职时间范围 */
  joinDateRange?: [string, string]
}

/**
 * 员工表单数据
 */
export interface EmployeeForm {
  /** 基本信息(必填) */
  name: string
  gender: Gender
  englishName?: string
  phone: string
  email: string
  departmentId: string
  position: string
  joinDate: string

  /** 详细信息(可选) */
  birthDate?: string
  officeLocation?: string
  emergencyContact?: string
  emergencyPhone?: string
  managerId?: string
  avatar?: string
  probationEndDate?: string
}

// ==================== 操作记录类型 ====================

/**
 * 操作记录
 */
export interface OperationLog {
  id: string
  employeeId: string
  operation: string              // 操作类型
  operator: string               // 操作人
  timestamp: string              // 操作时间
  details?: string               // 详细信息
}

// ==================== 统计类型 ====================

/**
 * 员工统计数据
 */
export interface EmployeeStatistics {
  /** 总员工数 */
  total: number

  /** 在职人数 */
  active: number

  /** 离职人数 */
  resigned: number

  /** 试用期人数 */
  probation: number

  /** 本月新入职 */
  newThisMonth: number

  /** 部门分布 */
  byDepartment: {
    departmentId: string
    departmentName: string
    count: number
  }[]
}

// ==================== 部门和职位类型 ====================

/**
 * 部门信息
 */
export interface Department {
  id: string
  name: string
  parentId?: string
}

/**
 * 职位信息
 */
export interface Position {
  id: string
  name: string
  level?: string
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

// ==================== 表单验证规则 ====================

/**
 * 表单验证规则配置
 */
export interface FormRuleConfig {
  /** 是否必填 */
  required?: boolean
  /** 最小长度 */
  min?: number
  /** 最大长度 */
  max?: number
  /** 正则表达式 */
  pattern?: RegExp
  /** 错误提示 */
  message?: string
  /** 触发方式 */
  trigger?: 'blur' | 'change'
}

// ==================== 导入导出类型 ====================

/**
 * 导入结果
 */
export interface ImportResult {
  success: number
  failed: number
  errors: string[]
}

/**
 * 导出配置
 */
export interface ExportConfig {
  filter?: EmployeeFilter
  fields?: string[]
  format?: 'xlsx' | 'csv'
}

// ==================== 权限类型 ====================

/**
 * 用户角色
 */
export enum UserRole {
  ADMIN = 'admin',                  // 系统管理员
  DEPARTMENT_MANAGER = 'department_manager',  // 部门管理员
  EMPLOYEE = 'employee'             // 普通员工
}

/**
 * 权限配置
 */
export interface PermissionConfig {
  canView: 'all' | 'department' | 'self_only'
  canEdit: 'all' | 'basic_info_only' | 'self_basic_info'
  canDelete: boolean
}

/**
 * 当前用户信息
 */
export interface CurrentUser {
  id: string
  name: string
  role: UserRole
  departmentId?: string
}
