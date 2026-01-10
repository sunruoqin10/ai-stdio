/**
 * 员工管理模块 API 接口
 * 基于 employee_Technical.md 规范实现
 */

import type {
  Employee,
  EmployeeFilter,
  EmployeeForm,
  OperationLog,
  EmployeeStatistics,
  PaginationResponse,
  ImportResult,
  ExportConfig
} from '../types'
import {
  mockEmployees,
  mockOperationLogs,
  mockDepartments,
  mockPositions,
} from '../mock/data'
import { calculateWorkYears, generateEmployeeId } from '../utils'

// ==================== 工具函数 ====================

// 模拟延迟
const delay = (ms: number = 300) => new Promise(resolve => setTimeout(resolve, ms))

// ==================== 员工 CRUD 接口 ====================

/**
 * 获取员工列表
 */
export async function getEmployeeList(params: EmployeeFilter & {
  page: number
  pageSize: number
}): Promise<PaginationResponse<Employee>> {
  await delay()

  let filteredList = [...mockEmployees]

  // 关键词搜索
  if (params.keyword) {
    const keyword = params.keyword.toLowerCase()
    filteredList = filteredList.filter(
      emp =>
        emp.name.toLowerCase().includes(keyword) ||
        emp.id.toLowerCase().includes(keyword) ||
        emp.phone.includes(keyword) ||
        emp.email.toLowerCase().includes(keyword)
    )
  }

  // 状态筛选
  if (params.status) {
    filteredList = filteredList.filter(emp => emp.status === params.status)
  }

  // 部门筛选
  if (params.departmentIds && params.departmentIds.length > 0) {
    filteredList = filteredList.filter(emp =>
      params.departmentIds!.includes(emp.departmentId)
    )
  }

  // 试用期状态筛选
  if (params.probationStatus) {
    filteredList = filteredList.filter(emp => emp.probationStatus === params.probationStatus)
  }

  // 职位筛选
  if (params.position) {
    filteredList = filteredList.filter(emp => emp.position === params.position)
  }

  // 性别筛选
  if (params.gender) {
    filteredList = filteredList.filter(emp => emp.gender === params.gender)
  }

  // 入职日期范围筛选
  if (params.joinDateRange && params.joinDateRange.length === 2) {
    const [startDate, endDate] = params.joinDateRange
    filteredList = filteredList.filter(emp => {
      const joinDate = new Date(emp.joinDate)
      return joinDate >= new Date(startDate) && joinDate <= new Date(endDate)
    })
  }

  // 分页
  const start = (params.page - 1) * params.pageSize
  const end = start + params.pageSize
  const list = filteredList.slice(start, end)

  return {
    list,
    total: filteredList.length,
    page: params.page,
    pageSize: params.pageSize,
  }
}

/**
 * 获取员工详情
 */
export async function getEmployeeDetail(id: string): Promise<Employee | null> {
  await delay()
  return mockEmployees.find(emp => emp.id === id) || null
}

/**
 * 创建员工
 */
export async function createEmployee(data: EmployeeForm): Promise<Employee> {
  await delay()

  // 自动生成员工编号
  const joinDate = data.joinDate
  const todayCount = mockEmployees.filter(emp => emp.joinDate === joinDate).length
  const employeeId = generateEmployeeId(joinDate, todayCount + 1)

  // 计算试用期结束日期 (默认3个月)
  const probationEndDate = new Date(joinDate)
  probationEndDate.setMonth(probationEndDate.getMonth() + 3)

  const newEmployee: Employee = {
    id: employeeId,
    name: data.name,
    englishName: data.englishName,
    gender: data.gender,
    birthDate: data.birthDate,
    phone: data.phone,
    email: data.email,
    avatar: data.avatar || `https://api.dicebear.com/7.x/avataaars/svg?seed=${encodeURIComponent(data.name)}`,
    departmentId: data.departmentId,
    departmentName: mockDepartments.find(d => d.id === data.departmentId)?.name,
    position: data.position,
    managerId: data.managerId,
    managerName: data.managerId ? mockEmployees.find(e => e.id === data.managerId)?.name : undefined,
    joinDate: data.joinDate,
    probationStatus: data.probationEndDate ? 'regular' : 'probation',
    probationEndDate: data.probationEndDate || probationEndDate.toISOString().split('T')[0],
    workYears: calculateWorkYears(data.joinDate),
    status: 'active',
    officeLocation: data.officeLocation,
    emergencyContact: data.emergencyContact,
    emergencyPhone: data.emergencyPhone,
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString(),
  }

  mockEmployees.push(newEmployee)
  return newEmployee
}

/**
 * 更新员工信息
 */
export async function updateEmployee(id: string, data: Partial<EmployeeForm>): Promise<Employee | null> {
  await delay()
  const index = mockEmployees.findIndex(emp => emp.id === id)
  if (index === -1) return null

  mockEmployees[index] = {
    ...mockEmployees[index],
    ...data,
    departmentName: data.departmentId ? mockDepartments.find(d => d.id === data.departmentId)?.name : mockEmployees[index].departmentName,
    managerName: data.managerId ? mockEmployees.find(e => e.id === data.managerId)?.name : mockEmployees[index].managerName,
    workYears: data.joinDate ? calculateWorkYears(data.joinDate) : mockEmployees[index].workYears,
    updatedAt: new Date().toISOString(),
  }

  return mockEmployees[index]
}

/**
 * 更新员工状态 (办理离职)
 */
export async function updateEmployeeStatus(
  id: string,
  status: Employee['status'],
  reason?: string
): Promise<Employee | null> {
  await delay()
  const index = mockEmployees.findIndex(emp => emp.id === id)
  if (index === -1) return null

  mockEmployees[index] = {
    ...mockEmployees[index],
    status,
    probationStatus: status === 'resigned' ? 'resigned' : mockEmployees[index].probationStatus,
    updatedAt: new Date().toISOString(),
  }

  // 记录操作日志
  if (status === 'resigned') {
    const logs = mockOperationLogs[id] || []
    logs.push({
      id: `log_${Date.now()}`,
      employeeId: id,
      operation: '办理离职',
      operator: '系统管理员',
      timestamp: new Date().toISOString(),
      details: reason || '无',
    })
  }

  return mockEmployees[index]
}

/**
 * 删除员工
 */
export async function deleteEmployee(id: string): Promise<boolean> {
  await delay()
  const index = mockEmployees.findIndex(emp => emp.id === id)
  if (index === -1) return false

  mockEmployees.splice(index, 1)
  return true
}

// ==================== 操作记录接口 ====================

/**
 * 获取操作记录
 */
export async function getOperationLogs(employeeId: string): Promise<OperationLog[]> {
  await delay()
  return mockOperationLogs[employeeId] || []
}

// ==================== 统计数据接口 ====================

/**
 * 获取统计数据
 */
export async function getStatistics(): Promise<EmployeeStatistics> {
  await delay()

  const total = mockEmployees.length
  const active = mockEmployees.filter(e => e.status === 'active').length
  const resigned = mockEmployees.filter(e => e.status === 'resigned').length
  const probation = mockEmployees.filter(e => e.probationStatus === 'probation').length

  // 计算本月新入职
  const now = new Date()
  const thisMonth = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`
  const newThisMonth = mockEmployees.filter(e => e.joinDate.startsWith(thisMonth)).length

  // 按部门统计
  const deptMap = new Map<string, number>()
  mockEmployees.forEach(emp => {
    const count = deptMap.get(emp.departmentId) || 0
    deptMap.set(emp.departmentId, count + 1)
  })

  const byDepartment = Array.from(deptMap.entries()).map(([deptId, count]) => ({
    departmentId: deptId,
    departmentName: mockDepartments.find(d => d.id === deptId)?.name || deptId,
    count,
  }))

  return {
    total,
    active,
    resigned,
    probation,
    newThisMonth,
    byDepartment,
  }
}

// ==================== 部门和职位接口 ====================

/**
 * 获取部门列表
 */
export async function getDepartmentList(): Promise<Array<{ id: string; name: string }>> {
  await delay()
  return mockDepartments
}

/**
 * 获取职位列表
 */
export async function getPositionList(): Promise<string[]> {
  await delay()
  return mockPositions
}

// ==================== 导入导出接口 ====================

/**
 * 批量导入员工
 */
export async function importEmployees(file: File): Promise<ImportResult> {
  await delay(1000)

  // 模拟导入结果
  // 实际项目中需要使用 xlsx 库解析 Excel 文件
  const success = Math.floor(Math.random() * 10) + 1
  const failed = Math.floor(Math.random() * 3)
  const errors: string[] = []

  for (let i = 0; i < failed; i++) {
    errors.push(`第${i + 2}行: 邮箱格式不正确`)
  }

  return {
    success,
    failed,
    errors,
  }
}

/**
 * 导出员工列表
 */
export async function exportEmployees(config: ExportConfig): Promise<Blob> {
  await delay(1000)

  // 模拟导出 Excel
  // 实际项目中需要使用 xlsx 库生成 Excel 文件
  const data = {
    mime: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    data: new ArrayBuffer(0),
  }

  return new Blob([data.data], { type: data.mime })
}
