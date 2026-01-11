/**
 * 菜单管理 Mock 数据
 */

import type { MenuItem } from '../types'
import { MenuType } from '../types'
import { getStoredMenus, saveMenusToStorage } from '../data/initMenus'

/**
 * 默认初始菜单数据
 * 当本地存储中没有数据时使用
 */
const defaultMenus: MenuItem[] = [
  {
    id: 1,
    menuCode: 'MENU_001',
    menuName: '员工名录',
    menuType: MenuType.MENU,
    parentId: 0,
    menuLevel: 1,
    routePath: '/employee',
    componentPath: '@/modules/employee/views/EmployeeList.vue',
    permission: 'system:employee:list',
    menuIcon: 'User',
    sortOrder: 1,
    visible: true,
    status: true,
    isCache: true,
    isFrame: false,
    isSystem: false,
    createdAt: '2024-01-01T00:00:00.000Z',
    updatedAt: '2024-01-01T00:00:00.000Z'
  },
  {
    id: 2,
    menuCode: 'MENU_002',
    menuName: '数据字典管理',
    menuType: MenuType.MENU,
    parentId: 0,
    menuLevel: 1,
    routePath: '/dict',
    componentPath: '@/modules/dict/views/DictManagement.vue',
    permission: 'system:dict:list',
    menuIcon: 'Notebook',
    sortOrder: 2,
    visible: true,
    status: true,
    isCache: true,
    isFrame: false,
    isSystem: false,
    createdAt: '2024-01-01T00:00:00.000Z',
    updatedAt: '2024-01-01T00:00:00.000Z'
  },
  {
    id: 3,
    menuCode: 'MENU_003',
    menuName: '菜单管理',
    menuType: MenuType.MENU,
    parentId: 0,
    menuLevel: 1,
    routePath: '/menu',
    componentPath: '@/modules/menu/views/index.vue',
    permission: 'system:menu:list',
    menuIcon: 'Menu',
    sortOrder: 3,
    visible: true,
    status: true,
    isCache: true,
    isFrame: false,
    isSystem: false,
    createdAt: '2024-01-01T00:00:00.000Z',
    updatedAt: '2024-01-01T00:00:00.000Z'
  },
  {
    id: 4,
    menuCode: 'DIR_001',
    menuName: '权限管理',
    menuType: MenuType.DIRECTORY,
    parentId: 0,
    menuLevel: 1,
    routePath: '/permission',
    redirectPath: '/permission/role',
    menuIcon: 'Lock',
    sortOrder: 4,
    visible: true,
    status: true,
    isCache: false,
    isFrame: false,
    isSystem: false,
    createdAt: '2024-01-01T00:00:00.000Z',
    updatedAt: '2024-01-01T00:00:00.000Z'
  },
  {
    id: 5,
    menuCode: 'MENU_004',
    menuName: '角色管理',
    menuType: MenuType.MENU,
    parentId: 4,
    menuLevel: 2,
    routePath: '/permission/role',
    componentPath: '@/modules/permission/views/RoleList.vue',
    permission: 'system:role:list',
    menuIcon: 'User',
    sortOrder: 1,
    visible: true,
    status: true,
    isCache: true,
    isFrame: false,
    isSystem: false,
    createdAt: '2024-01-01T00:00:00.000Z',
    updatedAt: '2024-01-01T00:00:00.000Z'
  },
  {
    id: 6,
    menuCode: 'MENU_005',
    menuName: '权限管理',
    menuType: MenuType.MENU,
    parentId: 4,
    menuLevel: 2,
    routePath: '/permission/permission',
    componentPath: '@/modules/permission/views/PermissionList.vue',
    permission: 'system:permission:list',
    menuIcon: 'Key',
    sortOrder: 2,
    visible: true,
    status: true,
    isCache: true,
    isFrame: false,
    isSystem: false,
    createdAt: '2024-01-01T00:00:00.000Z',
    updatedAt: '2024-01-01T00:00:00.000Z'
  }
]

// 全局菜单数据存储
let menuDataStore: MenuItem[] = []

/**
 * 初始化菜单数据
 * 从本地存储加载或使用默认数据
 */
function initMenuData() {
  const stored = getStoredMenus()
  if (stored.length > 0) {
    menuDataStore = stored
  } else {
    menuDataStore = [...defaultMenus]
  }
}

// 初始化数据
initMenuData()

/**
 * 获取菜单数据
 */
export function getMockMenus(): MenuItem[] {
  return menuDataStore
}

/**
 * 导出 mockMenus（保持向后兼容）
 */
export const mockMenus: MenuItem[] = menuDataStore

// 用于生成新ID的计数器
let nextId = 100

/**
 * 获取下一个ID
 */
export function getNextId(): number {
  const maxId = menuDataStore.reduce((max, menu) => Math.max(max, menu.id), 0)
  return Math.max(nextId, maxId + 1)
}

/**
 * 保存到本地存储
 */
function syncToStorage() {
  saveMenusToStorage(menuDataStore)
}

/**
 * 查找菜单项
 */
export function findMenuItem(id: number): MenuItem | undefined {
  return menuDataStore.find(m => m.id === id)
}

/**
 * 查找子菜单
 */
export function findChildMenus(parentId: number): MenuItem[] {
  return menuDataStore.filter(m => m.parentId === parentId)
}

/**
 * 添加菜单项
 */
export function addMenuItem(menu: MenuItem): void {
  menuDataStore.push(menu)
  syncToStorage()
}

/**
 * 更新菜单项
 */
export function updateMenuItem(id: number, updates: Partial<MenuItem>): boolean {
  const index = menuDataStore.findIndex(m => m.id === id)
  if (index !== -1) {
    menuDataStore[index] = {
      ...menuDataStore[index],
      ...updates,
      updatedAt: new Date().toISOString()
    }
    syncToStorage()
    return true
  }
  return false
}

/**
 * 删除菜单项
 */
export function deleteMenuItem(id: number): boolean {
  const index = menuDataStore.findIndex(m => m.id === id)
  if (index !== -1) {
    // 检查是否有子菜单
    const hasChildren = menuDataStore.some(m => m.parentId === id)
    if (hasChildren) {
      throw new Error('该菜单存在子菜单，无法删除')
    }
    menuDataStore.splice(index, 1)
    syncToStorage()
    return true
  }
  return false
}

/**
 * 获取菜单树
 */
export function getMenuTree(parentId: number = 0): MenuItem[] {
  const buildTree = (pid: number): MenuItem[] => {
    return menuDataStore
      .filter(m => m.parentId === pid)
      .map(menu => ({
        ...menu,
        children: buildTree(menu.id)
      }))
      .sort((a, b) => a.sortOrder - b.sortOrder)
  }
  return buildTree(parentId)
}

/**
 * 获取父级菜单选项
 */
export function getParentOptions(): MenuItem[] {
  return menuDataStore
    .filter(m => m.menuType === MenuType.DIRECTORY || m.menuType === MenuType.MENU)
    .map(menu => ({
      ...menu,
      children: undefined
    }))
    .sort((a, b) => a.sortOrder - b.sortOrder)
}
