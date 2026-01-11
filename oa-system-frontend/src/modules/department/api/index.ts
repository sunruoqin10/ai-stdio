/**
 * 部门管理模块 API 接口
 * 基于 department_Technical.md 规范实现
 */

import type {
  Department,
  DepartmentFilter,
  DepartmentForm,
  MoveDepartmentRequest,
  DepartmentStatistics,
  PaginationResponse
} from '../types'
import {
  mockDepartments,
  mockEmployees
} from '../mock/data'
import {
  buildTree,
  flattenTree,
  generateDepartmentId,
  validateMove,
  validateDelete,
  calculateNodeLevel,
  getAllChildDepartments,
  batchUpdateEmployeeCount
} from '../utils'

// ==================== 工具函数 ====================

// 模拟延迟
const delay = (ms: number = 300) => new Promise(resolve => setTimeout(resolve, ms))

// ==================== 部门 CRUD 接口 ====================

/**
 * 获取部门列表
 */
export async function getList(
  params?: DepartmentFilter & { type?: 'tree' | 'flat' }
): Promise<Department[] | PaginationResponse<Department>> {
  await delay()

  let filteredList = [...mockDepartments]

  // 关键词搜索
  if (params?.keyword) {
    const keyword = params.keyword.toLowerCase()
    filteredList = filteredList.filter(
      dept =>
        dept.name.toLowerCase().includes(keyword) ||
        dept.id.toLowerCase().includes(keyword) ||
        (dept.shortName && dept.shortName.toLowerCase().includes(keyword))
    )
  }

  // 状态筛选
  if (params?.status) {
    filteredList = filteredList.filter(dept => dept.status === params.status)
  }

  // 负责人筛选
  if (params?.leaderId) {
    filteredList = filteredList.filter(dept => dept.leaderId === params.leaderId)
  }

  // 层级筛选
  if (params?.level) {
    filteredList = filteredList.filter(dept => dept.level === params.level)
  }

  // 更新人数统计
  filteredList = batchUpdateEmployeeCount(filteredList, mockEmployees)

  // 返回树形或扁平数据
  if (params?.type === 'tree') {
    const tree = buildTree(filteredList)
    return tree as Department[]
  }

  return filteredList
}

/**
 * 获取部门详情
 */
export async function getDetail(id: string): Promise<Department | null> {
  await delay()
  const dept = mockDepartments.find(d => d.id === id)

  if (!dept) return null

  // 获取子部门
  const children = mockDepartments.filter(d => d.parentId === id)

  // 获取部门成员
  const employees = mockEmployees.filter(e => e.departmentId === id)

  return {
    ...dept,
    employeeCount: calculateEmployeeCount(id),
    children
  }
}

/**
 * 计算部门人数
 */
function calculateEmployeeCount(departmentId: string): number {
  return mockEmployees.filter(
    e => e.departmentId === departmentId && e.status === 'active'
  ).length
}

/**
 * 获取子部门列表
 */
export async function getChildren(id: string): Promise<Department[]> {
  await delay()
  return mockDepartments.filter(d => d.parentId === id)
}

/**
 * 获取部门成员
 */
export async function getEmployees(id: string) {
  await delay()
  return mockEmployees.filter(e => e.departmentId === id)
}

/**
 * 创建部门
 */
export async function create(data: DepartmentForm): Promise<{ id: string }> {
  await delay()

  // 自动生成部门ID
  const id = generateDepartmentId(mockDepartments.length)

  // 自动计算层级
  const level = data.parentId
    ? mockDepartments.find(d => d.id === data.parentId)!.level + 1
    : 1

  // 查找负责人信息
  const leader = mockEmployees.find(e => e.id === data.leaderId)

  const newDepartment: Department = {
    id,
    name: data.name,
    shortName: data.shortName,
    parentId: data.parentId || null,
    leaderId: data.leaderId,
    leader: leader ? {
      id: leader.id,
      name: leader.name,
      avatar: leader.avatar
    } : undefined,
    level,
    sort: data.sort || 0,
    establishedDate: data.establishedDate,
    description: data.description,
    icon: data.icon,
    status: 'active',
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString(),
    employeeCount: 0
  }

  mockDepartments.push(newDepartment)

  return { id }
}

/**
 * 更新部门
 */
export async function update(
  id: string,
  data: Partial<DepartmentForm>
): Promise<Department | null> {
  await delay()
  const index = mockDepartments.findIndex(d => d.id === id)
  if (index === -1) return null

  // 查找负责人信息
  const leader = data.leaderId
    ? mockEmployees.find(e => e.id === data.leaderId)
    : undefined

  mockDepartments[index] = {
    ...mockDepartments[index],
    ...data,
    leader: leader ? {
      id: leader.id,
      name: leader.name,
      avatar: leader.avatar
    } : mockDepartments[index].leader,
    updatedAt: new Date().toISOString()
  }

  return mockDepartments[index]
}

/**
 * 移动部门
 */
export async function move(request: MoveDepartmentRequest): Promise<Department | null> {
  await delay()

  const { departmentId, newParentId } = request

  // 验证移动合法性
  const validation = validateMove(departmentId, newParentId, mockDepartments)
  if (!validation.valid) {
    throw new Error(validation.message)
  }

  const index = mockDepartments.findIndex(d => d.id === departmentId)
  if (index === -1) return null

  // 计算新层级
  const newLevel = newParentId
    ? mockDepartments.find(d => d.id === newParentId)!.level + 1
    : 1

  // 更新部门
  mockDepartments[index] = {
    ...mockDepartments[index],
    parentId: newParentId,
    level: newLevel,
    updatedAt: new Date().toISOString()
  }

  // 级联更新子部门层级
  await updateChildrenLevel(departmentId, newLevel)

  return mockDepartments[index]
}

/**
 * 级联更新子部门层级
 */
async function updateChildrenLevel(parentId: string, parentLevel: number) {
  const children = mockDepartments.filter(d => d.parentId === parentId)

  for (const child of children) {
    child.level = parentLevel + 1
    child.updatedAt = new Date().toISOString()
    await updateChildrenLevel(child.id, child.level)
  }
}

/**
 * 删除部门
 */
export async function remove(id: string): Promise<boolean> {
  await delay()

  // 验证删除合法性
  const validation = validateDelete(id, mockDepartments, mockEmployees)
  if (!validation.valid) {
    throw new Error(validation.message)
  }

  const index = mockDepartments.findIndex(d => d.id === id)
  if (index === -1) return false

  mockDepartments.splice(index, 1)
  return true
}

/**
 * 获取部门统计
 */
export async function getStatistics(): Promise<DepartmentStatistics> {
  await delay()

  const total = mockDepartments.length
  const level1Count = mockDepartments.filter(d => d.level === 1).length
  const maxLevel = Math.max(...mockDepartments.map(d => d.level))
  const withLeaderCount = mockDepartments.filter(d => d.leaderId).length

  // 统计总员工数(去重)
  const totalEmployees = mockEmployees.filter(e => e.status === 'active').length

  return {
    total,
    level1Count,
    maxLevel,
    withLeaderCount,
    totalEmployees
  }
}

/**
 * 获取部门树(用于选择器)
 */
export async function getDepartmentTree(): Promise<Department[]> {
  await delay()
  const tree = buildTree(mockDepartments)
  return tree as Department[]
}

/**
 * 搜索部门
 */
export async function searchDepartments(keyword: string): Promise<Department[]> {
  await delay()

  if (!keyword) return []

  return mockDepartments.filter(
    dept =>
      dept.name.toLowerCase().includes(keyword.toLowerCase()) ||
      dept.id.toLowerCase().includes(keyword.toLowerCase()) ||
      (dept.shortName && dept.shortName.toLowerCase().includes(keyword.toLowerCase()))
  )
}

/**
 * 批量删除部门
 */
export async function batchRemove(ids: string[]): Promise<{ success: number; failed: number }> {
  await delay()

  let success = 0
  let failed = 0

  for (const id of ids) {
    try {
      const validation = validateDelete(id, mockDepartments, mockEmployees)
      if (!validation.valid) {
        failed++
        continue
      }

      const index = mockDepartments.findIndex(d => d.id === id)
      if (index !== -1) {
        mockDepartments.splice(index, 1)
        success++
      } else {
        failed++
      }
    } catch {
      failed++
    }
  }

  return { success, failed }
}

/**
 * 导出部门列表
 */
export async function exportDepartments(filter?: DepartmentFilter): Promise<Blob> {
  await delay(1000)

  // 模拟导出 Excel
  // 实际项目中需要使用 xlsx 库生成 Excel 文件
  const data = {
    mime: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    data: new ArrayBuffer(0),
  }

  return new Blob([data.data], { type: data.mime })
}

// ==================== 数据字典接口 ====================

/**
 * 获取字典列表
 */
export async function getDictList(dictCode: string) {
  await delay()

  const mockDictData: any = {
    department_status: [
      { label: '正常', value: 'active', color: '#67C23A' },
      { label: '停用', value: 'disabled', color: '#909399' }
    ],
    department_type: [
      { label: '总部', value: 'headquarters', sort: 1 },
      { label: '分公司', value: 'branch', sort: 2 },
      { label: '部门', value: 'department', sort: 3 },
      { label: '小组', value: 'team', sort: 4 }
    ],
    department_level: [
      { label: '一级部门', value: '1', sort: 1 },
      { label: '二级部门', value: '2', sort: 2 },
      { label: '三级部门', value: '3', sort: 3 },
      { label: '四级部门', value: '4', sort: 4 },
      { label: '五级部门', value: '5', sort: 5 }
    ]
  }

  return mockDictData[dictCode] || []
}
