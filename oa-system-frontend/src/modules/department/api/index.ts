/**
 * 部门管理模块 API 接口
 * 连接后端 REST API
 */

import type {
  Department,
  DepartmentFilter,
  DepartmentForm,
  MoveDepartmentRequest,
  DepartmentStatistics,
  PaginationResponse,
  ApiResponse
} from '../types'
import { http } from '@/utils/request'

// ==================== 部门 CRUD 接口 ====================

/**
 * 获取部门列表（分页）
 */
export async function getList(
  params: DepartmentFilter & {
    page: number
    pageSize: number
  }
): Promise<PaginationResponse<Department>> {
  // 构建查询参数
  const queryParams: Record<string, any> = {
    page: params.page,
    pageSize: params.pageSize,
  }

  if (params.keyword) queryParams.keyword = params.keyword
  if (params.status) queryParams.status = params.status
  if (params.leaderId) queryParams.leaderId = params.leaderId
  if (params.level) queryParams.level = params.level

  const response = await http.get<{
    code: number
    message: string
    data: {
      records: Department[]
      total: number
      current: number
      size: number
    }
  }>('/departments', { params: queryParams })

  // 转换MyBatis Plus的分页格式为前端格式
  return {
    list: response.data.records,
    total: response.data.total,
    page: response.data.current,
    pageSize: response.data.size,
  }
}

/**
 * 获取部门树
 */
export async function getDepartmentTree(): Promise<Department[]> {
  const response = await http.get<{
    code: number
    message: string
    data: Department[]
  }>('/departments/tree')

  return response.data
}

/**
 * 获取根部门列表
 */
export async function getRootDepartments(): Promise<Department[]> {
  const response = await http.get<{
    code: number
    message: string
    data: Department[]
  }>('/departments/roots')

  return response.data
}

/**
 * 获取子部门列表
 */
export async function getChildren(parentId: string): Promise<Department[]> {
  const response = await http.get<{
    code: number
    message: string
    data: Department[]
  }>(`/departments/${parentId}/children`)

  return response.data
}

/**
 * 获取部门详情
 */
export async function getDetail(id: string): Promise<Department> {
  const response = await http.get<{
    code: number
    message: string
    data: Department
  }>(`/departments/${id}`)

  return response.data
}

/**
 * 获取部门成员
 */
export async function getEmployees(id: string) {
  const response = await http.get<{
    code: number
    message: string
    data: Array<{
      employeeId: string
      employeeName: string
      employeeAvatar?: string
      position?: string
      status: string
      isLeader: boolean
      joinDepartmentDate?: string
    }>
  }>(`/departments/${id}/members`)

  // 转换后端格式为前端格式
  return response.data.map(member => ({
    id: member.employeeId,
    name: member.employeeName,
    avatar: member.employeeAvatar,
    position: member.position,
    status: member.status,
    isLeader: member.isLeader,
    joinDate: member.joinDepartmentDate,
    departmentId: id
  }))
}

/**
 * 创建部门
 */
export async function create(data: DepartmentForm): Promise<{ id: string }> {
  const response = await http.post<{
    code: number
    message: string
    data: Department
  }>('/departments', data)

  return { id: response.data.id }
}

/**
 * 更新部门
 */
export async function update(
  id: string,
  data: Partial<DepartmentForm & { version?: number }>
): Promise<Department> {
  const response = await http.put<{
    code: number
    message: string
    data: Department
  }>(`/departments/${id}`, data)

  return response.data
}

/**
 * 移动部门
 */
export async function move(request: MoveDepartmentRequest & { version?: number }): Promise<void> {
  await http.put<{
    code: number
    message: string
    data: null
  }>(`/departments/${request.departmentId}/move`, {
    newParentId: request.newParentId,
    version: request.version
  })
}

/**
 * 删除部门
 */
export async function remove(id: string): Promise<void> {
  await http.delete<{
    code: number
    message: string
    data: null
  }>(`/departments/${id}`)
}

/**
 * 批量删除部门
 */
export async function batchRemove(ids: string[]): Promise<{
  success: number
  failed: number
  errors?: Array<{ id: string; message: string }>
}> {
  const response = await http.delete<{
    code: number
    message: string
    data: {
      total: number
      success: number
      failed: number
      errors: Array<{ id: string; message: string }>
    }
  }>('/departments/batch', { data: ids })

  return {
    success: response.data.success,
    failed: response.data.failed,
    errors: response.data.errors
  }
}

/**
 * 获取部门统计
 */
export async function getStatistics(): Promise<DepartmentStatistics> {
  const response = await http.get<{
    code: number
    message: string
    data: {
      totalCount: number
      level1Count: number
      level2Count: number
      level3Count: number
      level4Count: number
      maxLevel: number
      withLeaderCount: number
      totalEmployees: number
      activeDepartmentCount: number
      disabledDepartmentCount: number
    }
  }>('/departments/statistics')

  // 转换后端格式为前端格式
  return {
    total: response.data.totalCount,
    level1Count: response.data.level1Count,
    maxLevel: response.data.maxLevel,
    withLeaderCount: response.data.withLeaderCount,
    totalEmployees: response.data.totalEmployees
  }
}

/**
 * 搜索部门
 */
export async function searchDepartments(keyword: string): Promise<Department[]> {
  const response = await http.get<{
    code: number
    message: string
    data: {
      records: Department[]
      total: number
    }
  }>('/departments', {
    params: {
      keyword,
      page: 1,
      pageSize: 20
    }
  })

  return response.data.records
}

/**
 * 检查部门名称是否存在
 */
export async function checkNameExists(
  name: string,
  parentId?: string | null,
  excludeId?: string
): Promise<boolean> {
  const response = await http.get<{
    code: number
    message: string
    data: boolean
  }>('/departments/check-name', {
    params: { name, parentId, excludeId }
  })

  return response.data
}

/**
 * 检查部门是否有子部门
 */
export async function hasChildren(id: string): Promise<boolean> {
  const response = await http.get<{
    code: number
    message: string
    data: boolean
  }>(`/departments/${id}/has-children`)

  return response.data
}

/**
 * 检查部门是否有成员
 */
export async function hasMembers(id: string): Promise<boolean> {
  const response = await http.get<{
    code: number
    message: string
    data: boolean
  }>(`/departments/${id}/has-members`)

  return response.data
}

/**
 * 导出部门列表
 */
export async function exportDepartments(filter?: DepartmentFilter): Promise<Blob> {
  const queryParams: Record<string, any> = {}

  if (filter?.keyword) queryParams.keyword = filter.keyword
  if (filter?.status) queryParams.status = filter.status
  if (filter?.leaderId) queryParams.leaderId = filter.leaderId
  if (filter?.level) queryParams.level = filter.level

  const response = await http.get('/departments/export', {
    params: queryParams,
    responseType: 'blob'
  })

  return response as any
}

// ==================== 数据字典接口 ====================

/**
 * 获取字典列表
 */
export async function getDictList(dictCode: string) {
  // 使用数据字典模块的API
  const { useDictStore } = await import('@/modules/dict/store')
  const dictStore = useDictStore()
  const dictData = await dictStore.fetchDictData(dictCode)
  return dictData.items
}
