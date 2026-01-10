/**
 * 权限管理模块路由配置
 * @module router/permission.routes
 */

import type { RouteRecordRaw } from 'vue-router'

const permissionRoutes: RouteRecordRaw[] = [
  // 角色管理页面（独立路由，便于开发测试）
  {
    path: '/permission/role',
    name: 'RoleList',
    component: () => import('@/modules/permission/views/RoleList.vue'),
    meta: {
      title: '角色管理',
      icon: 'User',
      permission: 'system:role:list' // 需要的权限码
    }
  },
  // 权限管理页面（独立路由，便于开发测试）
  {
    path: '/permission/permission',
    name: 'PermissionList',
    component: () => import('@/modules/permission/views/PermissionList.vue'),
    meta: {
      title: '权限管理',
      icon: 'Key',
      permission: 'system:permission:list' // 需要的权限码
    }
  },
  // 如果需要父级布局，可以取消注释以下代码
  // {
  //   path: '/permission',
  //   name: 'Permission',
  //   component: () => import('@/layouts/LayoutMain.vue'),
  //   meta: {
  //     title: '权限管理',
  //     icon: 'Lock',
  //     permission: 'system:permission:view'
  //   },
  //   children: [
  //     {
  //       path: 'role',
  //       name: 'RoleList',
  //       component: () => import('@/modules/permission/views/RoleList.vue'),
  //       meta: {
  //         title: '角色管理',
  //         icon: 'User',
  //         permission: 'system:role:list'
  //       }
  //     },
  //     {
  //       path: 'permission',
  //       name: 'PermissionList',
  //       component: () => import('@/modules/permission/views/PermissionList.vue'),
  //       meta: {
  //         title: '权限管理',
  //         icon: 'Key',
  //         permission: 'system:permission:list'
  //       }
  //     }
  //   ]
  // }
]

export default permissionRoutes
