/**
 * 员工管理模块工具函数
 * 基于 employee_Technical.md 规范实现
 */

import type { Employee } from '../types'

// ==================== 工龄计算 ====================

/**
 * 计算工龄
 * @param joinDate 入职日期
 * @returns 工龄(年)
 *
 * 计算规则:
 * - 计算当前日期与入职日期的年份差
 * - 如果当前月份小于入职月份,则减1年
 * - 如果月份相同但当前日期小于入职日期,则减1年
 * - 最小值为0年
 */
export function calculateWorkYears(joinDate: string): number {
  const join = new Date(joinDate)
  const now = new Date()

  // 计算年份差
  let years = now.getFullYear() - join.getFullYear()

  // 如果还没到入职月份,减1年
  if (now.getMonth() < join.getMonth()) {
    years--
  }
  // 如果是入职月份但还没到入职日,减1年
  else if (now.getMonth() === join.getMonth() && now.getDate() < join.getDate()) {
    years--
  }

  return Math.max(0, years)
}

/**
 * 计算工龄文本描述
 * @param joinDate 入职日期
 * @returns 工龄描述 (如: "2年3个月")
 */
export function getWorkYearsText(joinDate: string): string {
  const join = new Date(joinDate)
  const now = new Date()

  let years = now.getFullYear() - join.getFullYear()
  let months = now.getMonth() - join.getMonth()

  if (months < 0) {
    years--
    months += 12
  }

  if (years <= 0) {
    return `${months}个月`
  }

  return months > 0 ? `${years}年${months}个月` : `${years}年`
}

// ==================== 员工编号生成 ====================

/**
 * 生成员工编号
 * 格式: EMP + YYYYMMDD + 序号(3位)
 * 示例: EMP20260109001
 *
 * @param joinDate 入职日期
 * @param sequence 当天入职序号 (默认从1开始)
 * @returns 员工编号
 */
export function generateEmployeeId(joinDate: string, sequence: number = 1): string {
  const dateStr = joinDate.replace(/-/g, '')
  const sequenceStr = String(sequence).padStart(3, '0')
  return `EMP${dateStr}${sequenceStr}`
}

/**
 * 从员工编号中提取日期
 * @param employeeId 员工编号 (如: EMP20260109001)
 * @returns 日期字符串 (YYYY-MM-DD) 或 null
 */
export function extractDateFromEmployeeId(employeeId: string): string | null {
  // 移除 EMP 前缀
  const dateStr = employeeId.replace(/^EMP/i, '')

  // 提取日期部分 (前8位)
  if (dateStr.length >= 8) {
    const year = dateStr.substring(0, 4)
    const month = dateStr.substring(4, 6)
    const day = dateStr.substring(6, 8)
    return `${year}-${month}-${day}`
  }

  return null
}

// ==================== 试用期计算 ====================

/**
 * 计算试用期结束日期
 * 默认试用期3个月
 *
 * @param joinDate 入职日期
 * @param months 试用期月数(默认3)
 * @returns 试用期结束日期 (YYYY-MM-DD)
 */
export function calculateProbationEndDate(joinDate: string, months: number = 3): string {
  const join = new Date(joinDate)
  const endDate = new Date(join)

  endDate.setMonth(endDate.getMonth() + months)

  const year = endDate.getFullYear()
  const month = String(endDate.getMonth() + 1).padStart(2, '0')
  const day = String(endDate.getDate()).padStart(2, '0')

  return `${year}-${month}-${day}`
}

/**
 * 判断是否在试用期内
 * @param joinDate 入职日期
 * @param probationEndDate 试用期结束日期
 * @returns 是否在试用期内
 */
export function isProbation(joinDate: string, probationEndDate?: string): boolean {
  if (!probationEndDate) {
    // 如果没有提供试用期结束日期,默认3个月
    probationEndDate = calculateProbationEndDate(joinDate)
  }

  const now = new Date()
  const endDate = new Date(probationEndDate)

  return now < endDate
}

/**
 * 计算距离试用期结束天数
 * @param probationEndDate 试用期结束日期
 * @returns 剩余天数 (负数表示已过期)
 */
export function getProbationRemainingDays(probationEndDate: string): number {
  const now = new Date()
  const endDate = new Date(probationEndDate)

  const diffTime = endDate.getTime() - now.getTime()
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24))

  return diffDays
}

// ==================== 数据验证 ====================

/**
 * 验证手机号格式
 * @param phone 手机号
 * @returns 是否有效
 */
export function isValidPhone(phone: string): boolean {
  return /^1[3-9]\d{9}$/.test(phone)
}

/**
 * 验证邮箱格式
 * @param email 邮箱
 * @returns 是否有效
 */
export function isValidEmail(email: string): boolean {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)
}

/**
 * 验证身份证号格式
 * @param idCard 身份证号
 * @returns 是否有效
 */
export function isValidIdCard(idCard: string): boolean {
  // 18位身份证号正则
  const reg = /^\d{17}[\dXx]$/
  return reg.test(idCard)
}

// ==================== 数据脱敏 ====================

/**
 * 手机号脱敏
 * @param phone 手机号
 * @returns 脱敏后的手机号 (如: 138****8000)
 */
export function maskPhone(phone: string): string {
  if (!phone || phone.length !== 11) return phone
  return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
}

/**
 * 邮箱脱敏
 * @param email 邮箱
 * @returns 脱敏后的邮箱 (如: zha***@company.com)
 */
export function maskEmail(email: string): string {
  if (!email || !email.includes('@')) return email
  const [username, domain] = email.split('@')
  if (username.length <= 3) return email
  const maskedUsername = username.substring(0, 3) + '***'
  return `${maskedUsername}@${domain}`
}

/**
 * 身份证号脱敏
 * @param idCard 身份证号
 * @returns 脱敏后的身份证号 (如: 110101********1234)
 */
export function maskIdCard(idCard: string): string {
  if (!idCard || idCard.length !== 18) return idCard
  return idCard.replace(/^(.{6})(.{8})(.{4})$/, '$1********$3')
}

// ==================== 员工状态判断 ====================

/**
 * 判断员工是否在职
 * @param employee 员工信息
 * @returns 是否在职
 */
export function isActiveEmployee(employee: Employee): boolean {
  return employee.status === 'active'
}

/**
 * 判断员工是否试用期
 * @param employee 员工信息
 * @returns 是否试用期
 */
export function isProbationEmployee(employee: Employee): boolean {
  return employee.probationStatus === 'probation' || isProbation(employee.joinDate, employee.probationEndDate)
}

/**
 * 获取员工状态文本
 * @param status 员工状态
 * @returns 状态文本
 */
export function getEmployeeStatusText(status: string): string {
  const statusMap: Record<string, string> = {
    active: '在职',
    resigned: '离职',
    suspended: '停薪留职',
  }
  return statusMap[status] || status
}

/**
 * 获取试用期状态文本
 * @param probationStatus 试用期状态
 * @returns 状态文本
 */
export function getProbationStatusText(probationStatus: string): string {
  const statusMap: Record<string, string> = {
    probation: '试用期内',
    regular: '已转正',
    resigned: '已离职',
  }
  return statusMap[probationStatus] || probationStatus
}

/**
 * 获取性别文本
 * @param gender 性别
 * @returns 性别文本
 */
export function getGenderText(gender: string): string {
  const genderMap: Record<string, string> = {
    male: '男',
    female: '女',
  }
  return genderMap[gender] || gender
}

// ==================== 排序和筛选 ====================

/**
 * 按入职日期排序员工列表
 * @param employees 员工列表
 * @param order 排序方式 ('asc' | 'desc')
 * @returns 排序后的员工列表
 */
export function sortEmployeesByJoinDate(employees: Employee[], order: 'asc' | 'desc' = 'desc'): Employee[] {
  return [...employees].sort((a, b) => {
    const dateA = new Date(a.joinDate).getTime()
    const dateB = new Date(b.joinDate).getTime()
    return order === 'asc' ? dateA - dateB : dateB - dateA
  })
}

/**
 * 按部门分组员工
 * @param employees 员工列表
 * @returns 按部门分组的员工
 */
export function groupEmployeesByDepartment(employees: Employee[]): Record<string, Employee[]> {
  return employees.reduce((acc, employee) => {
    const deptId = employee.departmentId
    if (!acc[deptId]) {
      acc[deptId] = []
    }
    acc[deptId].push(employee)
    return acc
  }, {} as Record<string, Employee[]>)
}

// ==================== 权限控制 ====================

/**
 * 判断当前用户是否可以编辑员工
 * @param currentUser 当前用户
 * @param targetEmployee 目标员工
 * @returns 是否可以编辑
 */
export function canEditEmployee(
  currentUser: { id: string; role: string; departmentId?: string },
  targetEmployee: Employee
): boolean {
  // 系统管理员可以编辑所有员工
  if (currentUser.role === 'admin') {
    return true
  }

  // 部门管理员可以编辑本部门员工的基本信息
  if (currentUser.role === 'department_manager') {
    return currentUser.departmentId === targetEmployee.departmentId
  }

  // 普通员工只能编辑自己的基本信息
  return currentUser.id === targetEmployee.id
}

/**
 * 判断当前用户是否可以删除员工
 * @param currentUser 当前用户
 * @returns 是否可以删除
 */
export function canDeleteEmployee(currentUser: { role: string }): boolean {
  // 只有系统管理员可以删除员工
  return currentUser.role === 'admin'
}

// ==================== 其他工具 ====================

/**
 * 获取员工头像
 * @param employee 员工信息
 * @returns 头像URL (如果没有则使用默认头像)
 */
export function getEmployeeAvatar(employee: Employee): string {
  if (employee.avatar) {
    return employee.avatar
  }

  // 使用 DiceBear API 生成默认头像
  return `https://api.dicebear.com/7.x/avataaars/svg?seed=${encodeURIComponent(employee.name)}`
}

/**
 * 获取员工完整名称
 * @param employee 员工信息
 * @returns 完整名称 (英文名 + 中文名 或 中文名)
 */
export function getEmployeeFullName(employee: Employee): string {
  if (employee.englishName) {
    return `${employee.englishName} (${employee.name})`
  }
  return employee.name
}

/**
 * 格式化员工信息用于显示
 * @param employee 员工信息
 * @returns 格式化后的信息对象
 */
export function formatEmployeeForDisplay(employee: Employee) {
  return {
    ...employee,
    workYearsText: getWorkYearsText(employee.joinDate),
    statusText: getEmployeeStatusText(employee.status),
    probationStatusText: getProbationStatusText(employee.probationStatus || 'probation'),
    genderText: getGenderText(employee.gender),
    maskedPhone: maskPhone(employee.phone),
    maskedEmail: maskEmail(employee.email),
    fullName: getEmployeeFullName(employee),
    avatar: getEmployeeAvatar(employee),
  }
}
