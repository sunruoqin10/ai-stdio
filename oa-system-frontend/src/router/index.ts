import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    redirect: '/employee',
  },
  {
    path: '/employee',
    name: 'Employee',
    component: () => import('@/modules/employee/views/EmployeeList.vue'),
    meta: { title: '员工名录' },
  },
  {
    path: '/employee/:id',
    name: 'EmployeeDetail',
    component: () => import('@/modules/employee/views/EmployeeDetail.vue'),
    meta: { title: '员工详情' },
  },
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

// 路由守卫 - 更新页面标题
router.beforeEach((to, _from, next) => {
  document.title = `${to.meta.title || 'OA系统'} - OA系统`
  next()
})

export default router
