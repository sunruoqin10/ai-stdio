/**
 * 权限管理模块 API
 * @module permission/api
 */

import request from '@/utils/request'
import type {
  Role,
  Permission,
  RoleForm,
  PermissionForm,
  AssignRoleForm,
  UserPermissions,
  RoleFilter,
  PermissionFilter,
  RoleStatistics,
  PermissionStatistics,
  ApiResponse,
  ListResponse
} from '../types'

/**
 * ==================== 角色管理接口 ====================
 */

/**
 * 获取角色列表
 * @param params 查询参数
 */
export function getRoleList(params?: RoleFilter) {
  return request.get<ListResponse<Role>>('/api/roles', { params })
}

/**
 * 获取角色详情
 * @param id 角色ID
 */
export function getRoleDetail(id: string) {
  return request.get<Role>(`/api/roles/${id}`)
}

/**
 * 创建角色
 * @param data 表单数据
 */
export function createRole(data: RoleForm) {
  return request.post<{ id: string }>('/api/roles', data)
}

/**
 * 更新角色
 * @param id 角色ID
 * @param data 表单数据
 */
export function updateRole(id: string, data: Partial<RoleForm>) {
  return request.put<Role>(`/api/roles/${id}`, data)
}

/**
 * 删除角色
 * @param id 角色ID
 */
export function deleteRole(id: string) {
  return request.delete(`/api/roles/${id}`)
}

/**
 * 获取角色权限
 * @param id 角色ID
 */
export function getRolePermissions(id: string) {
  return request.get<string[]>(`/api/roles/${id}/permissions`)
}

/**
 * 更新角色权限
 * @param id 角色ID
 * @param permissionIds 权限ID列表
 */
export function updateRolePermissions(id: string, permissionIds: string[]) {
  return request.put(`/api/roles/${id}/permissions`, { permissionIds })
}

/**
 * 获取角色成员
 * @param id 角色ID
 */
export function getRoleUsers(id: string) {
  return request.get(`/api/roles/${id}/users`)
}

/**
 * 复制角色
 * @param sourceId 源角色ID
 * @param name 新角色名称
 */
export function copyRole(sourceId: string, name: string) {
  return request.post<{ id: string }>('/api/roles/copy', { sourceId, name })
}

/**
 * 获取角色统计
 */
export function getRoleStatistics() {
  return request.get<RoleStatistics>('/api/roles/statistics')
}

/**
 * ==================== 权限管理接口 ====================
 */

/**
 * 获取权限树
 * @param params 查询参数
 */
export function getPermissionTree(params?: PermissionFilter) {
  return request.get<Permission[]>('/api/permissions/tree', { params })
}

/**
 * 获取权限列表
 * @param params 查询参数
 */
export function getPermissionList(params?: PermissionFilter) {
  return request.get<ListResponse<Permission>>('/api/permissions', { params })
}

/**
 * 获取权限详情
 * @param id 权限ID
 */
export function getPermissionDetail(id: string) {
  return request.get<Permission>(`/api/permissions/${id}`)
}

/**
 * 创建权限
 * @param data 表单数据
 */
export function createPermission(data: PermissionForm) {
  return request.post<{ id: string }>('/api/permissions', data)
}

/**
 * 更新权限
 * @param id 权限ID
 * @param data 表单数据
 */
export function updatePermission(id: string, data: Partial<PermissionForm>) {
  return request.put<Permission>(`/api/permissions/${id}`, data)
}

/**
 * 删除权限
 * @param id 权限ID
 */
export function deletePermission(id: string) {
  return request.delete(`/api/permissions/${id}`)
}

/**
 * 获取所有模块
 */
export function getPermissionModules() {
  return request.get<string[]>('/api/permissions/modules')
}

/**
 * 获取权限统计
 */
export function getPermissionStatistics() {
  return request.get<PermissionStatistics>('/api/permissions/statistics')
}

/**
 * ==================== 用户角色接口 ====================
 */

/**
 * 获取用户角色
 * @param userId 用户ID
 */
export function getUserRoles(userId: string) {
  return request.get<Role[]>(`/api/users/${userId}/roles`)
}

/**
 * 分配用户角色
 * @param userId 用户ID
 * @param data 角色分配数据
 */
export function assignUserRoles(userId: string, data: AssignRoleForm) {
  return request.post(`/api/users/${userId}/roles`, data)
}

/**
 * 移除用户角色
 * @param userId 用户ID
 * @param roleId 角色ID
 */
export function removeUserRole(userId: string, roleId: string) {
  return request.delete(`/api/users/${userId}/roles/${roleId}`)
}

/**
 * 获取用户所有权限
 * @param userId 用户ID
 */
export function getUserPermissions(userId: string): Promise<ApiResponse<UserPermissions>> {
  return request.get<UserPermissions>(`/api/users/${userId}/permissions`)
}

/**
 * ==================== Mock 数据接口 (开发用) ====================
 */

/**
 * 获取 Mock 角色列表
 */
export function getMockRoleList(): ListResponse<Role> {
  return {
    list: [
      {
        id: 'ROLE0001',
        name: '超级管理员',
        code: 'admin',
        type: 'system',
        sort: 1,
        description: '拥有系统所有权限',
        status: 'active',
        isPreset: true,
        userCount: 2,
        createdAt: '2026-01-01 10:00:00',
        updatedAt: '2026-01-01 10:00:00'
      },
      {
        id: 'ROLE0002',
        name: '部门管理员',
        code: 'dept_admin',
        type: 'custom',
        sort: 2,
        description: '管理本部门相关业务',
        status: 'active',
        isPreset: false,
        userCount: 5,
        createdAt: '2026-01-05 10:00:00',
        updatedAt: '2026-01-05 10:00:00'
      },
      {
        id: 'ROLE0003',
        name: '普通员工',
        code: 'employee',
        type: 'custom',
        sort: 3,
        description: '普通员工权限',
        status: 'active',
        isPreset: false,
        userCount: 20,
        createdAt: '2026-01-10 10:00:00',
        updatedAt: '2026-01-10 10:00:00'
      }
    ],
    total: 3
  }
}

/**
 * 获取 Mock 权限树
 */
export function getMockPermissionTree(): Permission[] {
  return [
    {
      id: 'PERM001',
      name: '系统管理',
      code: 'system',
      type: 'menu',
      module: 'system',
      parentId: null,
      path: '/system',
      component: '@/views/system/Index',
      icon: 'Setting',
      sort: 1,
      status: 'active',
      createdAt: '2026-01-01 10:00:00',
      updatedAt: '2026-01-01 10:00:00',
      children: [
        {
          id: 'PERM0011',
          name: '用户管理',
          code: 'system:user:list',
          type: 'menu',
          module: 'system',
          parentId: 'PERM001',
          path: '/system/user',
          component: '@/views/system/user/List',
          icon: 'User',
          sort: 1,
          status: 'active',
          createdAt: '2026-01-01 10:00:00',
          updatedAt: '2026-01-01 10:00:00',
          children: [
            {
              id: 'PERM00111',
              name: '添加用户',
              code: 'system:user:add',
              type: 'button',
              module: 'system',
              parentId: 'PERM0011',
              sort: 1,
              status: 'active',
              createdAt: '2026-01-01 10:00:00',
              updatedAt: '2026-01-01 10:00:00'
            },
            {
              id: 'PERM00112',
              name: '编辑用户',
              code: 'system:user:edit',
              type: 'button',
              module: 'system',
              parentId: 'PERM0011',
              sort: 2,
              status: 'active',
              createdAt: '2026-01-01 10:00:00',
              updatedAt: '2026-01-01 10:00:00'
            },
            {
              id: 'PERM00113',
              name: '删除用户',
              code: 'system:user:delete',
              type: 'button',
              module: 'system',
              parentId: 'PERM0011',
              sort: 3,
              status: 'active',
              createdAt: '2026-01-01 10:00:00',
              updatedAt: '2026-01-01 10:00:00'
            }
          ]
        },
        {
          id: 'PERM0012',
          name: '角色管理',
          code: 'system:role:list',
          type: 'menu',
          module: 'system',
          parentId: 'PERM001',
          path: '/system/role',
          component: '@/views/system/role/List',
          icon: 'UserFilled',
          sort: 2,
          status: 'active',
          createdAt: '2026-01-01 10:00:00',
          updatedAt: '2026-01-01 10:00:00'
        }
      ]
    },
    {
      id: 'PERM002',
      name: '员工管理',
      code: 'employee',
      type: 'menu',
      module: 'employee',
      parentId: null,
      path: '/employee',
      component: '@/views/employee/Index',
      icon: 'Avatar',
      sort: 2,
      status: 'active',
      createdAt: '2026-01-01 10:00:00',
      updatedAt: '2026-01-01 10:00:00',
      children: [
        {
          id: 'PERM0021',
          name: '员工列表',
          code: 'employee:view:list',
          type: 'menu',
          module: 'employee',
          parentId: 'PERM002',
          path: '/employee/list',
          component: '@/views/employee/List',
          icon: 'List',
          sort: 1,
          status: 'active',
          createdAt: '2026-01-01 10:00:00',
          updatedAt: '2026-01-01 10:00:00',
          children: [
            {
              id: 'PERM00211',
              name: '添加员工',
              code: 'employee:add',
              type: 'button',
              module: 'employee',
              parentId: 'PERM0021',
              sort: 1,
              status: 'active',
              createdAt: '2026-01-01 10:00:00',
              updatedAt: '2026-01-01 10:00:00'
            },
            {
              id: 'PERM00212',
              name: '编辑员工',
              code: 'employee:edit',
              type: 'button',
              module: 'employee',
              parentId: 'PERM0021',
              sort: 2,
              status: 'active',
              createdAt: '2026-01-01 10:00:00',
              updatedAt: '2026-01-01 10:00:00'
            },
            {
              id: 'PERM00213',
              name: '删除员工',
              code: 'employee:delete',
              type: 'button',
              module: 'employee',
              parentId: 'PERM0021',
              sort: 3,
              status: 'active',
              createdAt: '2026-01-01 10:00:00',
              updatedAt: '2026-01-01 10:00:00'
            }
          ]
        }
      ]
    },
    {
      id: 'PERM003',
      name: '部门管理',
      code: 'department',
      type: 'menu',
      module: 'department',
      parentId: null,
      path: '/department',
      component: '@/views/department/Index',
      icon: 'OfficeBuilding',
      sort: 3,
      status: 'active',
      createdAt: '2026-01-01 10:00:00',
      updatedAt: '2026-01-01 10:00:00'
    }
  ]
}

/**
 * 获取 Mock 用户权限
 */
export function getMockUserPermissions(): UserPermissions {
  return {
    userId: 'USER001',
    roles: [
      {
        id: 'ROLE0001',
        name: '超级管理员',
        code: 'admin',
        type: 'system',
        sort: 1,
        description: '拥有系统所有权限',
        status: 'active',
        isPreset: true,
        createdAt: '2026-01-01 10:00:00',
        updatedAt: '2026-01-01 10:00:00'
      }
    ],
    permissions: getMockPermissionTree(),
    permissionCodes: ['system', 'system:user:list', 'system:user:add', 'system:user:edit', 'system:user:delete', 'system:role:list', 'system:role:add', 'system:role:edit', 'system:role:delete', 'system:permission:list', 'system:permission:add', 'system:permission:edit', 'system:permission:delete', 'system:dict:list', 'system:menu:list', 'employee', 'employee:view:list', 'employee:add', 'employee:edit', 'employee:delete', 'department'],
    buttonPermissions: ['system:user:add', 'system:user:edit', 'system:user:delete', 'employee:add', 'employee:edit', 'employee:delete'],
    apiPermissions: [],
    menuPermissions: getMockPermissionTree().filter(p => p.type === 'menu'),
    dataPermissions: {
      employee: 'all',
      department: 'all'
    }
  }
}
