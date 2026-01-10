/**
 * 数据字典 Mock 数据
 * @module dict/mock/data
 */

import type { DictType, DictItem, DictTreeNode } from '../types'

/**
 * Mock 字典类型数据
 */
export const mockDictTypes: DictType[] = [
  {
    id: 'dict_type_001',
    code: 'employee_status',
    name: '员工状态',
    description: '员工在职状态管理',
    category: 'system',
    itemCount: 3,
    status: 'enabled',
    sortOrder: 100,
    extProps: {},
    createdAt: '2026-01-01 10:00:00',
    updatedAt: '2026-01-01 10:00:00'
  },
  {
    id: 'dict_type_002',
    code: 'employee_gender',
    name: '性别',
    description: '员工性别信息',
    category: 'system',
    itemCount: 2,
    status: 'enabled',
    sortOrder: 110,
    extProps: {},
    createdAt: '2026-01-01 10:00:00',
    updatedAt: '2026-01-01 10:00:00'
  },
  {
    id: 'dict_type_003',
    code: 'asset_status',
    name: '资产状态',
    description: '资产使用状态管理',
    category: 'system',
    itemCount: 5,
    status: 'enabled',
    sortOrder: 120,
    extProps: {},
    createdAt: '2026-01-01 10:00:00',
    updatedAt: '2026-01-01 10:00:00'
  },
  {
    id: 'dict_type_004',
    code: 'approval_status',
    name: '审批状态',
    description: '审批流程状态管理',
    category: 'system',
    itemCount: 3,
    status: 'enabled',
    sortOrder: 130,
    extProps: {},
    createdAt: '2026-01-01 10:00:00',
    updatedAt: '2026-01-01 10:00:00'
  },
  {
    id: 'dict_type_005',
    code: 'project_priority',
    name: '项目优先级',
    description: '项目优先级分类',
    category: 'business',
    itemCount: 3,
    status: 'enabled',
    sortOrder: 200,
    extProps: {},
    createdAt: '2026-01-01 10:00:00',
    updatedAt: '2026-01-01 10:00:00'
  },
  {
    id: 'dict_type_006',
    code: 'customer_level',
    name: '客户等级',
    description: '客户等级分类',
    category: 'business',
    itemCount: 4,
    status: 'enabled',
    sortOrder: 210,
    extProps: {},
    createdAt: '2026-01-01 10:00:00',
    updatedAt: '2026-01-01 10:00:00'
  }
]

/**
 * Mock 字典项数据
 */
export const mockDictItems: DictItem[] = [
  // 员工状态
  {
    id: 'dict_item_001',
    dictTypeId: 'dict_type_001',
    dictTypeCode: 'employee_status',
    label: '在职',
    value: 'active',
    colorType: 'success',
    sortOrder: 10,
    status: 'enabled',
    extProps: {},
    createdAt: '2026-01-01 10:00:00',
    updatedAt: '2026-01-01 10:00:00'
  },
  {
    id: 'dict_item_002',
    dictTypeId: 'dict_type_001',
    dictTypeCode: 'employee_status',
    label: '离职',
    value: 'resigned',
    colorType: 'info',
    sortOrder: 20,
    status: 'enabled',
    extProps: {},
    createdAt: '2026-01-01 10:00:00',
    updatedAt: '2026-01-01 10:00:00'
  },
  {
    id: 'dict_item_003',
    dictTypeId: 'dict_type_001',
    dictTypeCode: 'employee_status',
    label: '试用期',
    value: 'probation',
    colorType: 'warning',
    sortOrder: 30,
    status: 'enabled',
    extProps: {},
    createdAt: '2026-01-01 10:00:00',
    updatedAt: '2026-01-01 10:00:00'
  },
  // 性别
  {
    id: 'dict_item_004',
    dictTypeId: 'dict_type_002',
    dictTypeCode: 'employee_gender',
    label: '男',
    value: 'male',
    colorType: 'primary',
    sortOrder: 10,
    status: 'enabled',
    extProps: {},
    createdAt: '2026-01-01 10:00:00',
    updatedAt: '2026-01-01 10:00:00'
  },
  {
    id: 'dict_item_005',
    dictTypeId: 'dict_type_002',
    dictTypeCode: 'employee_gender',
    label: '女',
    value: 'female',
    colorType: 'danger',
    sortOrder: 20,
    status: 'enabled',
    extProps: {},
    createdAt: '2026-01-01 10:00:00',
    updatedAt: '2026-01-01 10:00:00'
  },
  // 资产状态
  {
    id: 'dict_item_006',
    dictTypeId: 'dict_type_003',
    dictTypeCode: 'asset_status',
    label: '库存中',
    value: 'in_stock',
    colorType: 'info',
    sortOrder: 10,
    status: 'enabled',
    extProps: {},
    createdAt: '2026-01-01 10:00:00',
    updatedAt: '2026-01-01 10:00:00'
  },
  {
    id: 'dict_item_007',
    dictTypeId: 'dict_type_003',
    dictTypeCode: 'asset_status',
    label: '使用中',
    value: 'in_use',
    colorType: 'success',
    sortOrder: 20,
    status: 'enabled',
    extProps: {},
    createdAt: '2026-01-01 10:00:00',
    updatedAt: '2026-01-01 10:00:00'
  },
  {
    id: 'dict_item_008',
    dictTypeId: 'dict_type_003',
    dictTypeCode: 'asset_status',
    label: '借出',
    value: 'borrowed',
    colorType: 'warning',
    sortOrder: 30,
    status: 'enabled',
    extProps: {},
    createdAt: '2026-01-01 10:00:00',
    updatedAt: '2026-01-01 10:00:00'
  },
  {
    id: 'dict_item_009',
    dictTypeId: 'dict_type_003',
    dictTypeCode: 'asset_status',
    label: '维修中',
    value: 'maintenance',
    colorType: 'danger',
    sortOrder: 40,
    status: 'enabled',
    extProps: {},
    createdAt: '2026-01-01 10:00:00',
    updatedAt: '2026-01-01 10:00:00'
  },
  {
    id: 'dict_item_010',
    dictTypeId: 'dict_type_003',
    dictTypeCode: 'asset_status',
    label: '报废',
    value: 'scrapped',
    colorType: 'info',
    sortOrder: 50,
    status: 'disabled',
    extProps: {},
    createdAt: '2026-01-01 10:00:00',
    updatedAt: '2026-01-01 10:00:00'
  },
  // 审批状态
  {
    id: 'dict_item_011',
    dictTypeId: 'dict_type_004',
    dictTypeCode: 'approval_status',
    label: '待审批',
    value: 'pending',
    colorType: 'warning',
    sortOrder: 10,
    status: 'enabled',
    extProps: {},
    createdAt: '2026-01-01 10:00:00',
    updatedAt: '2026-01-01 10:00:00'
  },
  {
    id: 'dict_item_012',
    dictTypeId: 'dict_type_004',
    dictTypeCode: 'approval_status',
    label: '已通过',
    value: 'approved',
    colorType: 'success',
    sortOrder: 20,
    status: 'enabled',
    extProps: {},
    createdAt: '2026-01-01 10:00:00',
    updatedAt: '2026-01-01 10:00:00'
  },
  {
    id: 'dict_item_013',
    dictTypeId: 'dict_type_004',
    dictTypeCode: 'approval_status',
    label: '已拒绝',
    value: 'rejected',
    colorType: 'danger',
    sortOrder: 30,
    status: 'enabled',
    extProps: {},
    createdAt: '2026-01-01 10:00:00',
    updatedAt: '2026-01-01 10:00:00'
  },
  // 项目优先级
  {
    id: 'dict_item_014',
    dictTypeId: 'dict_type_005',
    dictTypeCode: 'project_priority',
    label: '高',
    value: 'high',
    colorType: 'danger',
    sortOrder: 10,
    status: 'enabled',
    extProps: {},
    createdAt: '2026-01-01 10:00:00',
    updatedAt: '2026-01-01 10:00:00'
  },
  {
    id: 'dict_item_015',
    dictTypeId: 'dict_type_005',
    dictTypeCode: 'project_priority',
    label: '中',
    value: 'medium',
    colorType: 'warning',
    sortOrder: 20,
    status: 'enabled',
    extProps: {},
    createdAt: '2026-01-01 10:00:00',
    updatedAt: '2026-01-01 10:00:00'
  },
  {
    id: 'dict_item_016',
    dictTypeId: 'dict_type_005',
    dictTypeCode: 'project_priority',
    label: '低',
    value: 'low',
    colorType: 'info',
    sortOrder: 30,
    status: 'enabled',
    extProps: {},
    createdAt: '2026-01-01 10:00:00',
    updatedAt: '2026-01-01 10:00:00'
  },
  // 客户等级
  {
    id: 'dict_item_017',
    dictTypeId: 'dict_type_006',
    dictTypeCode: 'customer_level',
    label: 'VIP客户',
    value: 'vip',
    colorType: 'danger',
    sortOrder: 10,
    status: 'enabled',
    extProps: {},
    createdAt: '2026-01-01 10:00:00',
    updatedAt: '2026-01-01 10:00:00'
  },
  {
    id: 'dict_item_018',
    dictTypeId: 'dict_type_006',
    dictTypeCode: 'customer_level',
    label: '重要客户',
    value: 'important',
    colorType: 'warning',
    sortOrder: 20,
    status: 'enabled',
    extProps: {},
    createdAt: '2026-01-01 10:00:00',
    updatedAt: '2026-01-01 10:00:00'
  },
  {
    id: 'dict_item_019',
    dictTypeId: 'dict_type_006',
    dictTypeCode: 'customer_level',
    label: '普通客户',
    value: 'normal',
    colorType: 'primary',
    sortOrder: 30,
    status: 'enabled',
    extProps: {},
    createdAt: '2026-01-01 10:00:00',
    updatedAt: '2026-01-01 10:00:00'
  },
  {
    id: 'dict_item_020',
    dictTypeId: 'dict_type_006',
    dictTypeCode: 'customer_level',
    label: '潜在客户',
    value: 'potential',
    colorType: 'info',
    sortOrder: 40,
    status: 'enabled',
    extProps: {},
    createdAt: '2026-01-01 10:00:00',
    updatedAt: '2026-01-01 10:00:00'
  }
]

/**
 * Mock 字典树数据
 */
export const mockDictTree: DictTreeNode[] = mockDictTypes.map(dt => ({
  id: dt.id,
  code: dt.code,
  name: dt.name,
  category: dt.category,
  itemCount: dt.itemCount || 0,
  children: mockDictItems
    .filter(item => item.dictTypeCode === dt.code)
    .sort((a, b) => a.sortOrder - b.sortOrder)
}))
