import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { setupPermissionGuard } from './permission'

// 导入权限路由
import permissionRoutes from './permission.routes'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    redirect: '/employee',
  },
  // 登录相关路由
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/modules/auth/views/Login.vue'),
    meta: { title: '用户登录', requiresAuth: false },
  },
  {
    path: '/reset-password',
    name: 'ResetPassword',
    component: () => import('@/modules/auth/views/ResetPassword.vue'),
    meta: { title: '找回密码', requiresAuth: false },
  },
  {
    path: '/employee',
    name: 'Employee',
    component: () => import('@/modules/employee/views/EmployeeList.vue'),
    meta: { title: '员工名录', requiresAuth: true },
  },
  {
    path: '/employee/:id',
    name: 'EmployeeDetail',
    component: () => import('@/modules/employee/views/EmployeeDetail.vue'),
    meta: { title: '员工详情' },
  },
  {
    path: '/dict',
    name: 'DictManagement',
    component: () => import('@/modules/dict/views/DictManagement.vue'),
    meta: { title: '数据字典管理' },
  },
  {
    path: '/dict/items/:dictTypeCode',
    name: 'DictItemManagement',
    component: () => import('@/modules/dict/views/DictItemManagement.vue'),
    meta: { title: '字典项管理' },
  },
  {
    path: '/menu',
    name: 'MenuManagement',
    component: () => import('@/modules/menu/views/index.vue'),
    meta: { title: '菜单管理', requiresAuth: true },
  },

  // 权限管理路由
  ...permissionRoutes,
  // 暂时注释掉未实现的模块
  // {
  //   path: '/department/org',
  //   name: 'OrgStructure',
  //   component: () => import('@/modules/department/views/OrgStructure.vue'),
  //   meta: { title: '组织架构图' },
  // },
  // {
  //   path: '/department/list',
  //   name: 'DepartmentList',
  //   component: () => import('@/modules/department/views/DepartmentList.vue'),
  //   meta: { title: '部门列表' },
  // },
  // {
  //   path: '/asset',
  //   name: 'Asset',
  //   component: () => import('@/modules/asset/views/AssetList.vue'),
  //   meta: { title: '资产列表' },
  // },
  // {
  //   path: '/asset/:id',
  //   name: 'AssetDetail',
  //   component: () => import('@/modules/asset/views/AssetDetail.vue'),
  //   meta: { title: '资产详情' },
  // },
  // {
  //   path: '/asset/statistics',
  //   name: 'AssetStatistics',
  //   component: () => import('@/modules/asset/views/AssetStatistics.vue'),
  //   meta: { title: '资产统计' },
  // },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

// 设置路由权限守卫
setupPermissionGuard(router)

// 路由守卫 - 更新页面标题
router.beforeEach((to, _from, next) => {
  document.title = `${to.meta.title || 'OA系统'} - OA系统`
  next()
})

export default router
