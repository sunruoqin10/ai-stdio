/**
 * 部门管理工具函数
 * 基于 department_Technical.md 规范实现
 */

import type { Department } from '../types'

// ==================== 部门编号生成 ====================

/**
 * 生成部门编号
 * 格式: DEPT + 4位序号
 */
export function generateDepartmentId(count: number): string {
  return `DEPT${String(count + 1).padStart(4, '0')}`
}

// ==================== 树形数据处理工具 ====================

/**
 * 扁平数组转树形结构
 */
export function buildTree<T extends { id: string; parentId: string | null }>(
  flatList: T[],
  options?: {
    rootId?: string | null
    childrenKey?: string
  }
): T[] {
  const { rootId = null, childrenKey = 'children' } = options || {}

  const map = new Map<string, any>()
  const roots: any[] = []

  // 先建立映射
  flatList.forEach(item => {
    map.set(item.id, { ...item, [childrenKey]: [] })
  })

  // 建立树形关系
  flatList.forEach(item => {
    const node = map.get(item.id)!
    if (item.parentId === rootId) {
      roots.push(node)
    } else {
      const parent = map.get(item.parentId)
      if (parent) {
        parent[childrenKey].push(node)
      }
    }
  })

  return roots
}

/**
 * 树形转扁平数组
 */
export function flattenTree<T extends { children?: T[] }>(
  tree: T[],
  childrenKey = 'children'
): T[] {
  const result: T[] = []

  function traverse(nodes: T[]) {
    nodes.forEach(node => {
      const { [childrenKey]: children, ...rest } = node as any
      result.push(rest)
      if (children?.length > 0) {
        traverse(children)
      }
    })
  }

  traverse(tree)
  return result
}

/**
 * 获取节点路径(从根到当前节点)
 */
export function getNodePath<T extends { id: string; parentId: string | null }>(
  nodeId: string,
  flatList: T[]
): T[] {
  const path: T[] = []
  let current = flatList.find(item => item.id === nodeId)

  while (current) {
    path.unshift(current)
    if (!current.parentId) break
    current = flatList.find(item => item.id === current.parentId)
  }

  return path
}

/**
 * 计算节点层级
 */
export function calculateNodeLevel<T extends { parentId: string | null }>(
  nodeId: string,
  flatList: T[]
): number {
  let level = 1
  let current = flatList.find(item => item.id === nodeId)

  while (current?.parentId) {
    level++
    current = flatList.find(item => item.id === current.parentId)
  }

  return level
}

/**
 * 获取所有子孙节点
 */
export function getAllDescendants<T extends { id: string; children?: T[] }>(
  node: T,
  childrenKey = 'children'
): T[] {
  const descendants: T[] = []
  const children = (node as any)[childrenKey] || []

  children.forEach((child: T) => {
    descendants.push(child)
    descendants.push(...getAllDescendants(child, childrenKey))
  })

  return descendants
}

/**
 * 获取所有子部门(递归)
 */
export function getAllChildDepartments(
  parentId: string,
  departments: Department[]
): Department[] {
  const children: Department[] = []
  const directChildren = departments.filter(d => d.parentId === parentId)

  directChildren.forEach(child => {
    children.push(child)
    children.push(...getAllChildDepartments(child.id, departments))
  })

  return children
}

/**
 * 判断是否为子部门
 */
export function isChildDepartment(
  departmentId: string,
  parentId: string,
  departments: Department[]
): boolean {
  const department = departments.find(d => d.id === parentId)
  if (!department) return false

  if (department.id === departmentId) return true
  if (department.children) {
    return department.children.some((child: Department) =>
      isChildDepartment(departmentId, child.id, departments)
    )
  }

  return false
}

// ==================== 验证函数 ====================

/**
 * 移动部门验证
 */
export function validateMove(
  departmentId: string,
  newParentId: string | null,
  departments: Department[],
  maxLevel: number = 5
): { valid: boolean; message?: string } {
  // 1. 不能移动到自己
  if (departmentId === newParentId) {
    return { valid: false, message: '不能移动到自己' }
  }

  // 2. 不能移动到自己的子部门
  const allChildren = getAllChildDepartments(departmentId, departments)
  if (allChildren.some(c => c.id === newParentId)) {
    return { valid: false, message: '不能移动到自己的子部门' }
  }

  // 3. 检查目标层级是否超限
  let newLevel = 1
  if (newParentId) {
    const newParent = departments.find(d => d.id === newParentId)
    if (newParent) {
      newLevel = newParent.level + 1
    }
  }

  // 获取当前部门的最大深度
  const currentDept = departments.find(d => d.id === departmentId)
  if (currentDept) {
    const allDescendants = getAllChildDepartments(departmentId, departments)
    const maxDescendantLevel = allDescendants.reduce((max, child) => Math.max(max, child.level), 0)
    const depth = maxDescendantLevel - currentDept.level

    if (newLevel + depth > maxLevel) {
      return { valid: false, message: `移动后层级将超过${maxLevel}级` }
    }
  }

  return { valid: true }
}

/**
 * 删除部门前检查
 */
export function validateDelete(
  departmentId: string,
  departments: Department[],
  employees: any[]
): { valid: boolean; message?: string } {
  // 1. 检查是否有子部门
  const children = departments.filter(d => d.parentId === departmentId)
  if (children.length > 0) {
    return { valid: false, message: '请先删除或移动所有子部门' }
  }

  // 2. 检查是否有成员
  const deptEmployees = employees.filter((e: any) => e.departmentId === departmentId)
  if (deptEmployees.length > 0) {
    return { valid: false, message: '请先转移或删除所有部门成员' }
  }

  return { valid: true }
}

// ==================== ECharts 数据转换 ====================

/**
 * 转换部门数据为ECharts Tree格式
 */
export function convertToGraphData(flatDepartments: Department[]) {
  // 构建树形结构
  const deptMap = new Map()
  const roots: any[] = []

  // 先建立映射
  flatDepartments.forEach(dept => {
    deptMap.set(dept.id, {
      name: dept.name,
      value: dept.employeeCount || 0,
      employeeCount: dept.employeeCount || 0,
      leader: dept.leader?.name,
      children: []
    })
  })

  // 建立树形关系
  flatDepartments.forEach(dept => {
    const node = deptMap.get(dept.id)
    if (!dept.parentId) {
      roots.push(node)
    } else {
      const parent = deptMap.get(dept.parentId)
      if (parent) {
        parent.children.push(node)
      }
    }
  })

  // 清理空的children数组
  function cleanEmptyChildren(node: any) {
    if (node.children && node.children.length === 0) {
      delete node.children
    } else if (node.children) {
      node.children.forEach((child: any) => cleanEmptyChildren(child))
    }
  }

  roots.forEach((root) => cleanEmptyChildren(root))

  return roots
}

/**
 * 获取ECharts配置
 */
export function getGraphOption(treeData: any[]) {
  return {
    tooltip: {
      trigger: 'item',
      triggerOn: 'mousemove',
      formatter: (params: any) => {
        if (params.data) {
          return `<div style="padding: 8px;">
            <div style="font-weight: bold; margin-bottom: 4px;">${params.data.name}</div>
            <div>人数: ${params.data.employeeCount}人</div>
            <div>负责人: ${params.data.leader || '未设置'}</div>
          </div>`
        }
        return ''
      }
    },
    series: [
      {
        type: 'tree',
        data: treeData,
        top: '10%',
        left: '10%',
        bottom: '10%',
        right: '20%',
        symbolSize: 120,
        symbol: 'rect',
        expandAndCollapse: true,
        initialTreeDepth: 2,

        label: {
          position: 'inside',
          rotate: 0,
          verticalAlign: 'middle',
          align: 'center',
          fontSize: 14,
          color: '#fff',
          formatter: function(params: any) {
            return params.data.name + '\n' + params.data.employeeCount + '人'
          }
        },

        leaves: {
          label: {
            position: 'right',
            verticalAlign: 'middle',
            align: 'left'
          }
        },

        emphasis: {
          focus: 'descendant'
        },

        itemStyle: {
          color: '#409EFF',
          borderColor: '#409EFF',
          borderWidth: 2,
          borderRadius: 4
        },

        lineStyle: {
          color: '#ccc',
          width: 2,
          curveness: 0.5
        },

        emphasis: {
          itemStyle: {
            color: '#67C23A'
          },
          lineStyle: {
            color: '#67C23A',
            width: 3
          }
        }
      }
    ]
  }
}

// ==================== 部门人数统计 ====================

/**
 * 计算部门人数(从员工列表)
 */
export function calculateEmployeeCount(
  departmentId: string,
  employees: any[]
): number {
  return employees.filter(
    e => e.departmentId === departmentId && e.status === 'active'
  ).length
}

/**
 * 批量更新部门人数
 */
export function batchUpdateEmployeeCount(
  departments: Department[],
  employees: any[]
): Department[] {
  return departments.map(dept => ({
    ...dept,
    employeeCount: calculateEmployeeCount(dept.id, employees)
  }))
}
