import type { Employee, EmployeeFilter, OperationLog } from '../types'
import {
  mockEmployees,
  mockOperationLogs,
  mockDepartments,
  mockPositions,
  mockStatistics,
} from '../mock/data'

// 模拟延迟
const delay = (ms: number = 300) => new Promise(resolve => setTimeout(resolve, ms))

/**
 * 获取员工列表
 */
export async function getEmployeeList(params: EmployeeFilter & {
  page: number
  pageSize: number
}): Promise<{
  list: Employee[]
  total: number
}> {
  await delay()

  let filteredList = [...mockEmployees]

  // 关键词搜索
  if (params.keyword) {
    const keyword = params.keyword.toLowerCase()
    filteredList = filteredList.filter(
      emp =>
        emp.name.toLowerCase().includes(keyword) ||
        emp.employeeNo.toLowerCase().includes(keyword) ||
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
  if (params.entryDateRange && params.entryDateRange.length === 2) {
    const [startDate, endDate] = params.entryDateRange
    filteredList = filteredList.filter(emp => {
      const entryDate = new Date(emp.entryDate)
      return entryDate >= new Date(startDate) && entryDate <= new Date(endDate)
    })
  }

  // 分页
  const start = (params.page - 1) * params.pageSize
  const end = start + params.pageSize
  const list = filteredList.slice(start, end)

  return {
    list,
    total: filteredList.length,
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
export async function createEmployee(data: Partial<Employee>): Promise<Employee> {
  await delay()
  const newEmployee: Employee = {
    id: String(mockEmployees.length + 1),
    employeeNo: data.employeeNo || `EMP${String(mockEmployees.length + 1).padStart(3, '0')}`,
    name: data.name || '',
    englishName: data.englishName,
    gender: data.gender || 'male',
    avatar: data.avatar || `https://api.dicebear.com/7.x/avataaars/svg?seed=${data.name}`,
    department: data.department || '',
    departmentId: data.departmentId || '',
    position: data.position || '',
    positionId: data.positionId,
    phone: data.phone || '',
    email: data.email || '',
    status: data.status || 'active',
    probationStatus: data.probationStatus || 'probation',
    entryDate: data.entryDate || new Date().toISOString().split('T')[0],
    regularDate: data.regularDate,
    birthDate: data.birthDate,
    officeLocation: data.officeLocation,
    emergencyContact: data.emergencyContact,
    emergencyPhone: data.emergencyPhone,
    superior: data.superior,
    superiorId: data.superiorId,
    createTime: new Date().toISOString(),
    updateTime: new Date().toISOString(),
  } as Employee

  mockEmployees.push(newEmployee)
  return newEmployee
}

/**
 * 更新员工
 */
export async function updateEmployee(id: string, data: Partial<Employee>): Promise<Employee | null> {
  await delay()
  const index = mockEmployees.findIndex(emp => emp.id === id)
  if (index === -1) return null

  mockEmployees[index] = {
    ...mockEmployees[index],
    ...data,
    updateTime: new Date().toISOString(),
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

/**
 * 获取操作记录
 */
export async function getOperationLogs(employeeId: string): Promise<OperationLog[]> {
  await delay()
  return mockOperationLogs[employeeId] || []
}

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

/**
 * 获取统计数据
 */
export async function getStatistics(): Promise<typeof mockStatistics> {
  await delay()
  return mockStatistics
}
