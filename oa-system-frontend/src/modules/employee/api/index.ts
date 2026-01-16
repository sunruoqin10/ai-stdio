/**
 * 员工管理模块 API 接口
 * 基于 employee_Technical.md 规范实现
 * 连接后端 REST API
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
import { http } from '@/utils/request'

// ==================== 员工 CRUD 接口 ====================

/**
 * 获取员工列表
 */
export async function getEmployeeList(params: EmployeeFilter & {
  page: number
  pageSize: number
}): Promise<PaginationResponse<Employee>> {
  // 构建查询参数
  const queryParams: Record<string, any> = {
    page: params.page,
    pageSize: params.pageSize,
  }

  if (params.keyword) queryParams.keyword = params.keyword
  if (params.status) queryParams.status = params.status
  if (params.departmentIds && params.departmentIds.length > 0) {
    queryParams.departmentIds = params.departmentIds.join(',')
  }
  if (params.position) queryParams.position = params.position
  if (params.probationStatus) queryParams.probationStatus = params.probationStatus
  if (params.gender) queryParams.gender = params.gender
  if (params.joinDateRange && params.joinDateRange.length === 2) {
    queryParams.joinDateStart = params.joinDateRange[0]
    queryParams.joinDateEnd = params.joinDateRange[1]
  }

  const response = await http.get<{
    code: number
    message: string
    data: {
      records: Employee[]
      total: number
      current: number
      size: number
    }
  }>('/employees', { params: queryParams })

  // 转换MyBatis Plus的分页格式为前端格式
  return {
    list: response.data.records,
    total: response.data.total,
    page: response.data.current,
    pageSize: response.data.size,
  }
}

/**
 * 获取员工详情
 */
export async function getEmployeeDetail(id: string): Promise<Employee | null> {
  const response = await http.get<{
    code: number
    message: string
    data: Employee
  }>(`/employees/${id}`)

  return response.data
}

/**
 * 创建员工
 */
export async function createEmployee(data: EmployeeForm): Promise<Employee> {
  const response = await http.post<{
    code: number
    message: string
    data: Employee
  }>('/employees', data)

  return response.data
}

/**
 * 更新员工信息
 */
export async function updateEmployee(id: string, data: Partial<EmployeeForm>): Promise<Employee | null> {
  const response = await http.put<{
    code: number
    message: string
    data: Employee
  }>(`/employees/${id}`, data)

  return response.data
}

/**
 * 更新员工状态 (办理离职)
 */
export async function updateEmployeeStatus(
  id: string,
  status: Employee['status'],
  reason?: string
): Promise<Employee | null> {
  const response = await http.put<{
    code: number
    message: string
    data: Employee
  }>(`/employees/${id}/status`, {
    status,
    reason,
  })

  return response.data
}

/**
 * 删除员工
 */
export async function deleteEmployee(id: string): Promise<boolean> {
  await http.delete<{
    code: number
    message: string
    data: null
  }>(`/employees/${id}`)

  return true
}

// ==================== 操作记录接口 ====================

/**
 * 获取操作记录
 */
export async function getOperationLogs(
  employeeId: string,
  page: number = 1,
  pageSize: number = 20
): Promise<OperationLog[]> {
  const response = await http.get<{
    code: number
    message: string
    data: {
      records: OperationLog[]
      total: number
    }
  }>(`/employees/${employeeId}/logs`, {
    params: { page, pageSize },
  })

  return response.data.records
}

// ==================== 统计数据接口 ====================

/**
 * 获取统计数据
 */
export async function getStatistics(): Promise<EmployeeStatistics> {
  const response = await http.get<{
    code: number
    message: string
    data: EmployeeStatistics
  }>('/employees/statistics')

  return response.data
}

// ==================== 唯一性验证接口 ====================

/**
 * 检查邮箱是否存在
 */
export async function checkEmailExists(email: string, excludeId?: string): Promise<boolean> {
  const response = await http.get<{
    code: number
    message: string
    data: boolean
  }>('/employees/check-email', {
    params: { email, excludeId },
  })

  return response.data
}

/**
 * 检查手机号是否存在
 */
export async function checkPhoneExists(phone: string, excludeId?: string): Promise<boolean> {
  const response = await http.get<{
    code: number
    message: string
    data: boolean
  }>('/employees/check-phone', {
    params: { phone, excludeId },
  })

  return response.data
}

// ==================== 部门和职位接口 ====================
// 注意：这些接口需要在department模块实现后才能使用
// 当前暂时使用Mock数据或静态数据

/**
 * 获取部门列表
 * 从 department 模块获取部门树形结构，并转换为平铺列表
 */
export async function getDepartmentList(): Promise<Array<{ id: string; name: string }>> {
  try {
    // 导入 department 模块的 API
    const departmentApi = await import('@/modules/department/api')

    // 获取部门树
    const tree = await departmentApi.getDepartmentTree()

    // 将树形结构转换为平铺列表
    const flattenDepartments = (departments: any[]): Array<{ id: string; name: string }> => {
      const result: Array<{ id: string; name: string }> = []

      departments.forEach(dept => {
        // 添加当前部门
        result.push({
          id: dept.id,
          name: dept.name
        })

        // 递归处理子部门
        if (dept.children && dept.children.length > 0) {
          result.push(...flattenDepartments(dept.children))
        }
      })

      return result
    }

    return flattenDepartments(tree)
  } catch (error) {
    console.error('获取部门列表失败:', error)
    return []
  }
}

/**
 * 获取职位列表
 * 从数据字典 (position_type) 获取职位数据
 */
export async function getPositionList(): Promise<string[]> {
  try {
    // 导入数据字典 store
    const { useDictStore } = await import('@/modules/dict/store')
    const dictStore = useDictStore()

    // 从数据字典获取职位类型数据
    const dictData = await dictStore.fetchDictData('position_type')

    // 提取职位标签列表
    return dictData.items.map(item => item.label)
  } catch (error) {
    console.error('获取职位列表失败:', error)
    // 降级处理: 返回默认职位列表
    return [
      '软件工程师',
      '高级软件工程师',
      '技术经理',
      '产品经理',
      'UI设计师',
      '测试工程师',
      '人力资源专员',
      '财务专员',
      '行政专员',
    ]
  }
}

// ==================== 字典数据接口 ====================

/**
 * 员工字典数据响应类型
 */
export interface EmployeeDictData {
  gender: DictData
  status: DictData
  probationStatus: DictData
  position: DictData
  level: DictData
}

/**
 * 字典数据类型
 */
export interface DictData {
  dictType: {
    id: string
    code: string
    name: string
  }
  items: Array<{
    id: string
    label: string
    value: string
    colorType?: string
    color?: string
    icon?: string
    sortOrder: number
    status: string
  }>
}

/**
 * 获取员工相关字典数据
 */
export async function getEmployeeDictData(): Promise<EmployeeDictData> {
  const response = await http.get<{
    code: number
    message: string
    data: EmployeeDictData
  }>('/employees/dict-data')

  return response.data
}

// ==================== 导入导出接口 ====================
// 注意：导入导出功能需要后端实现对应接口

/**
 * 批量导入员工
 */
export async function importEmployees(file: File): Promise<ImportResult> {
  // TODO: 实现文件上传和导入逻辑
  const formData = new FormData()
  formData.append('file', file)

  // const response = await http.post('/employees/import', formData, {
  //   headers: { 'Content-Type': 'multipart/form-data' }
  // })

  // 临时返回Mock数据
  return {
    success: 0,
    failed: 0,
    errors: [],
  }
}

/**
 * 导出员工列表
 */
export async function exportEmployees(config: ExportConfig): Promise<Blob> {
  // TODO: 实现导出逻辑
  // const response = await http.post('/employees/export', config, {
  //   responseType: 'blob'
  // })
  // return response

  // 临时返回空Blob
  return new Blob()
}
