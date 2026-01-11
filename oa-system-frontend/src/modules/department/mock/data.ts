/**
 * 部门管理模块 Mock 数据
 * 基于 department_Technical.md 规范实现
 */

import type { Department } from '../types'

// ==================== Mock 数据 ====================

/**
 * 模拟部门数据
 */
export const mockDepartments: Department[] = [
  {
    id: 'DEPT0001',
    name: '总公司',
    shortName: '总部',
    parentId: null,
    leaderId: 'EMP20240101001',
    level: 1,
    sort: 1,
    establishedDate: '2020-01-01',
    description: '公司总部',
    icon: '',
    status: 'active',
    createdAt: '2024-01-01 00:00:00',
    updatedAt: '2024-01-01 00:00:00',
    employeeCount: 5,
    leader: {
      id: 'EMP20240101001',
      name: '张总',
      avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=%E5%BC%A0%E6%80%BB'
    }
  },
  {
    id: 'DEPT0002',
    name: '研发中心',
    shortName: '研发',
    parentId: 'DEPT0001',
    leaderId: 'EMP20240101002',
    level: 2,
    sort: 1,
    establishedDate: '2020-01-15',
    description: '负责产品研发和技术支持',
    icon: '',
    status: 'active',
    createdAt: '2024-01-01 00:00:00',
    updatedAt: '2024-01-01 00:00:00',
    employeeCount: 25,
    leader: {
      id: 'EMP20240101002',
      name: '李研发',
      avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=%E6%9D%8E%E7%A0%94%E5%8F%91'
    }
  },
  {
    id: 'DEPT0003',
    name: '技术部',
    shortName: '技术',
    parentId: 'DEPT0002',
    leaderId: 'EMP20240101003',
    level: 3,
    sort: 1,
    establishedDate: '2020-02-01',
    description: '负责技术开发和维护',
    icon: '',
    status: 'active',
    createdAt: '2024-01-01 00:00:00',
    updatedAt: '2024-01-01 00:00:00',
    employeeCount: 15,
    leader: {
      id: 'EMP20240101003',
      name: '王技术',
      avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=%E7%8E%8B%E6%8A%80%E6%9C%AF'
    }
  },
  {
    id: 'DEPT0004',
    name: '前端组',
    shortName: '前端',
    parentId: 'DEPT0003',
    leaderId: 'EMP20240101004',
    level: 4,
    sort: 1,
    establishedDate: '2020-03-01',
    description: '负责前端开发',
    icon: '',
    status: 'active',
    createdAt: '2024-01-01 00:00:00',
    updatedAt: '2024-01-01 00:00:00',
    employeeCount: 8,
    leader: {
      id: 'EMP20240101004',
      name: '赵前端',
      avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=%E8%B5%B5%E5%89%8D%E7%AB%AF'
    }
  },
  {
    id: 'DEPT0005',
    name: '后端组',
    shortName: '后端',
    parentId: 'DEPT0003',
    leaderId: 'EMP20240101005',
    level: 4,
    sort: 2,
    establishedDate: '2020-03-01',
    description: '负责后端开发',
    icon: '',
    status: 'active',
    createdAt: '2024-01-01 00:00:00',
    updatedAt: '2024-01-01 00:00:00',
    employeeCount: 7,
    leader: {
      id: 'EMP20240101005',
      name: '钱后端',
      avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=%E9%92%B1%E5%90%8E%E7%AB%AF'
    }
  },
  {
    id: 'DEPT0006',
    name: '产品部',
    shortName: '产品',
    parentId: 'DEPT0002',
    leaderId: 'EMP20240101006',
    level: 3,
    sort: 2,
    establishedDate: '2020-02-01',
    description: '负责产品设计和规划',
    icon: '',
    status: 'active',
    createdAt: '2024-01-01 00:00:00',
    updatedAt: '2024-01-01 00:00:00',
    employeeCount: 10,
    leader: {
      id: 'EMP20240101006',
      name: '孙产品',
      avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=%E5%AD%99%E4%BA%A7%E5%93%81'
    }
  },
  {
    id: 'DEPT0007',
    name: '市场中心',
    shortName: '市场',
    parentId: 'DEPT0001',
    leaderId: 'EMP20240101007',
    level: 2,
    sort: 2,
    establishedDate: '2020-01-15',
    description: '负责市场营销和销售',
    icon: '',
    status: 'active',
    createdAt: '2024-01-01 00:00:00',
    updatedAt: '2024-01-01 00:00:00',
    employeeCount: 20,
    leader: {
      id: 'EMP20240101007',
      name: '周市场',
      avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=%E5%91%A8%E5%B8%82%E5%9C%BA'
    }
  },
  {
    id: 'DEPT0008',
    name: '销售部',
    shortName: '销售',
    parentId: 'DEPT0007',
    leaderId: 'EMP20240101008',
    level: 3,
    sort: 1,
    establishedDate: '2020-02-01',
    description: '负责销售业务',
    icon: '',
    status: 'active',
    createdAt: '2024-01-01 00:00:00',
    updatedAt: '2024-01-01 00:00:00',
    employeeCount: 12,
    leader: {
      id: 'EMP20240101008',
      name: '吴销售',
      avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=%E5%90%B4%E9%94%80%E5%94%AE'
    }
  },
  {
    id: 'DEPT0009',
    name: '市场部',
    shortName: '市场',
    parentId: 'DEPT0007',
    leaderId: 'EMP20240101009',
    level: 3,
    sort: 2,
    establishedDate: '2020-02-01',
    description: '负责市场推广',
    icon: '',
    status: 'active',
    createdAt: '2024-01-01 00:00:00',
    updatedAt: '2024-01-01 00:00:00',
    employeeCount: 8,
    leader: {
      id: 'EMP20240101009',
      name: '郑市场',
      avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=%E9%83%91%E5%B8%82%E5%9C%BA'
    }
  },
  {
    id: 'DEPT0010',
    name: '人力资源中心',
    shortName: 'HR',
    parentId: 'DEPT0001',
    leaderId: 'EMP20240101010',
    level: 2,
    sort: 3,
    establishedDate: '2020-01-15',
    description: '负责人力资源管理',
    icon: '',
    status: 'active',
    createdAt: '2024-01-01 00:00:00',
    updatedAt: '2024-01-01 00:00:00',
    employeeCount: 8,
    leader: {
      id: 'EMP20240101010',
      name: '冯HR',
      avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=%E5%86%AFHR'
    }
  },
  {
    id: 'DEPT0011',
    name: '招聘组',
    shortName: '招聘',
    parentId: 'DEPT0010',
    leaderId: 'EMP20240101011',
    level: 3,
    sort: 1,
    establishedDate: '2020-03-01',
    description: '负责人员招聘',
    icon: '',
    status: 'active',
    createdAt: '2024-01-01 00:00:00',
    updatedAt: '2024-01-01 00:00:00',
    employeeCount: 4,
    leader: {
      id: 'EMP20240101011',
      name: '陈招聘',
      avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=%E9%99%88%E6%8B%9B%E8%81%98'
    }
  },
  {
    id: 'DEPT0012',
    name: '培训组',
    shortName: '培训',
    parentId: 'DEPT0010',
    leaderId: 'EMP20240101012',
    level: 3,
    sort: 2,
    establishedDate: '2020-03-01',
    description: '负责员工培训',
    icon: '',
    status: 'active',
    createdAt: '2024-01-01 00:00:00',
    updatedAt: '2024-01-01 00:00:00',
    employeeCount: 4,
    leader: {
      id: 'EMP20240101012',
      name: '褚培训',
      avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=%E8%A4%9A%E5%9F%B9%E8%AE%AD'
    }
  }
]

/**
 * 模拟员工数据(用于关联)
 */
export const mockEmployees = [
  {
    id: 'EMP20240101001',
    name: '张总',
    avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=%E5%BC%A0%E6%80%BB',
    departmentId: 'DEPT0001',
    status: 'active'
  },
  {
    id: 'EMP20240101002',
    name: '李研发',
    avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=%E6%9D%8E%E7%A0%94%E5%8F%91',
    departmentId: 'DEPT0002',
    status: 'active'
  },
  {
    id: 'EMP20240101003',
    name: '王技术',
    avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=%E7%8E%8B%E6%8A%80%E6%9C%AF',
    departmentId: 'DEPT0003',
    status: 'active'
  },
  {
    id: 'EMP20240101004',
    name: '赵前端',
    avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=%E8%B5%B5%E5%89%8D%E7%AB%AF',
    departmentId: 'DEPT0004',
    status: 'active'
  },
  {
    id: 'EMP20240101005',
    name: '钱后端',
    avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=%E9%92%B1%E5%90%8E%E7%AB%AF',
    departmentId: 'DEPT0005',
    status: 'active'
  },
  {
    id: 'EMP20240101006',
    name: '孙产品',
    avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=%E5%AD%99%E4%BA%A7%E5%93%81',
    departmentId: 'DEPT0006',
    status: 'active'
  },
  {
    id: 'EMP20240101007',
    name: '周市场',
    avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=%E5%91%A8%E5%B8%82%E5%9C%BA',
    departmentId: 'DEPT0007',
    status: 'active'
  },
  {
    id: 'EMP20240101008',
    name: '吴销售',
    avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=%E5%90%B4%E9%94%80%E5%94%AE',
    departmentId: 'DEPT0008',
    status: 'active'
  },
  {
    id: 'EMP20240101009',
    name: '郑市场',
    avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=%E9%83%91%E5%B8%82%E5%9C%BA',
    departmentId: 'DEPT0009',
    status: 'active'
  },
  {
    id: 'EMP20240101010',
    name: '冯HR',
    avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=%E5%86%AFHR',
    departmentId: 'DEPT0010',
    status: 'active'
  },
  {
    id: 'EMP20240101011',
    name: '陈招聘',
    avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=%E9%99%88%E6%8B%9B%E8%81%98',
    departmentId: 'DEPT0011',
    status: 'active'
  },
  {
    id: 'EMP20240101012',
    name: '褚培训',
    avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=%E8%A4%9A%E5%9F%B9%E8%AE%AD',
    departmentId: 'DEPT0012',
    status: 'active'
  }
]

/**
 * 模拟数据字典
 */
export const mockDictData = {
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
