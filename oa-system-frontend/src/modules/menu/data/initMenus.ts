/**
 * 菜单初始化数据
 * 从当前系统的路由配置中提取的菜单数据
 */

import type { MenuForm, MenuItem } from '../types'
import { MenuType } from '../types'

/**
 * 初始化菜单数据
 * 对应系统中的所有主要功能模块
 */
export const initMenuData: MenuForm[] = [
  // 一级菜单 - 系统管理
  {
    menuName: '系统管理',
    menuType: MenuType.DIRECTORY,
    parentId: 0,
    routePath: '/system',
    redirectPath: '/employee',
    menuIcon: 'Setting',
    sortOrder: 1,
    visible: true,
    status: true,
    isCache: false,
    isFrame: false,
    remark: '系统管理目录'
  },

  // 员工管理
  {
    menuName: '员工名录',
    menuType: MenuType.MENU,
    parentId: 0,
    routePath: '/employee',
    componentPath: '@/modules/employee/views/EmployeeList.vue',
    permission: 'system:employee:list',
    menuIcon: 'User',
    sortOrder: 1,
    visible: true,
    status: true,
    isCache: true,
    isFrame: false,
    remark: '员工信息管理'
  },
  {
    menuName: '员工详情',
    menuType: MenuType.MENU,
    parentId: 0, // 将在初始化时动态关联
    routePath: '/employee/:id',
    componentPath: '@/modules/employee/views/EmployeeDetail.vue',
    sortOrder: 2,
    visible: false, // 详情页不显示在菜单中
    status: true,
    isCache: false,
    isFrame: false,
    remark: '员工详情页'
  },

  // 数据字典管理
  {
    menuName: '数据字典管理',
    menuType: MenuType.MENU,
    parentId: 0,
    routePath: '/dict',
    componentPath: '@/modules/dict/views/DictManagement.vue',
    permission: 'system:dict:list',
    menuIcon: 'Notebook',
    sortOrder: 2,
    visible: true,
    status: true,
    isCache: true,
    isFrame: false,
    remark: '数据字典管理'
  },
  {
    menuName: '字典项管理',
    menuType: MenuType.MENU,
    parentId: 0, // 将在初始化时动态关联
    routePath: '/dict/items/:dictTypeCode',
    componentPath: '@/modules/dict/views/DictItemManagement.vue',
    sortOrder: 3,
    visible: false, // 详情页不显示在菜单中
    status: true,
    isCache: false,
    isFrame: false,
    remark: '字典项管理页'
  },

  // 菜单管理
  {
    menuName: '菜单管理',
    menuType: MenuType.MENU,
    parentId: 0,
    routePath: '/menu',
    componentPath: '@/modules/menu/views/index.vue',
    permission: 'system:menu:list',
    menuIcon: 'Menu',
    sortOrder: 3,
    visible: true,
    status: true,
    isCache: true,
    isFrame: false,
    remark: '系统菜单管理'
  },

  // 权限管理目录
  {
    menuName: '权限管理',
    menuType: MenuType.DIRECTORY,
    parentId: 0,
    routePath: '/permission',
    menuIcon: 'Lock',
    sortOrder: 4,
    visible: true,
    status: true,
    isCache: false,
    isFrame: false,
    remark: '权限管理目录'
  },

  // 角色管理
  {
    menuName: '角色管理',
    menuType: MenuType.MENU,
    parentId: 0, // 将在初始化时关联到权限管理
    routePath: '/permission/role',
    componentPath: '@/modules/permission/views/RoleList.vue',
    permission: 'system:role:list',
    menuIcon: 'User',
    sortOrder: 1,
    visible: true,
    status: true,
    isCache: true,
    isFrame: false,
    remark: '角色信息管理'
  },

  // 权限管理
  {
    menuName: '权限管理',
    menuType: MenuType.MENU,
    parentId: 0, // 将在初始化时关联到权限管理
    routePath: '/permission/permission',
    componentPath: '@/modules/permission/views/PermissionList.vue',
    permission: 'system:permission:list',
    menuIcon: 'Key',
    sortOrder: 2,
    visible: true,
    status: true,
    isCache: true,
    isFrame: false,
    remark: '权限资源管理'
  },
]

/**
 * 获取带菜单编码的初始化数据
 * 为每个菜单生成唯一的 menuCode
 */
export function getInitMenusWithCode(): MenuForm[] {
  return initMenuData.map((menu, index) => ({
    ...menu,
    menuCode: generateMenuCode(menu.menuName, menu.menuType, index)
  }))
}

/**
 * 生成菜单编码
 * @param name 菜单名称
 * @param type 菜单类型
 * @param index 索引
 */
function generateMenuCode(name: string, type: MenuType, index: number): string {
  // 简化名称，移除特殊字符
  const simplified = name
    .replace(/[管理列表页详情]/g, '')
    .trim()

  // 转换为英文或拼音编码（这里使用简化方案）
  const typePrefix = {
    [MenuType.DIRECTORY]: 'DIR',
    [MenuType.MENU]: 'MENU',
    [MenuType.BUTTON]: 'BTN'
  }[type]

  // 使用索引保证唯一性
  return `${typePrefix}_${String(index + 1).padStart(3, '0')}`
}

/**
 * 检查菜单是否已初始化
 * 通过检查本地存储来判断
 */
export function isMenuInitialized(): boolean {
  const flag = localStorage.getItem('oa_menu_initialized')
  return flag === 'true'
}

/**
 * 设置菜单初始化标记
 */
export function setMenuInitialized(flag: boolean = true): void {
  localStorage.setItem('oa_menu_initialized', String(flag))
}

/**
 * 从本地存储获取已初始化的菜单
 */
export function getStoredMenus(): MenuItem[] {
  const stored = localStorage.getItem('oa_menu_data')
  if (stored) {
    try {
      return JSON.parse(stored)
    } catch (e) {
      console.error('解析本地存储的菜单数据失败:', e)
    }
  }
  return []
}

/**
 * 保存菜单到本地存储
 */
export function saveMenusToStorage(menus: MenuItem[]): void {
  try {
    localStorage.setItem('oa_menu_data', JSON.stringify(menus))
  } catch (e) {
    console.error('保存菜单数据到本地存储失败:', e)
  }
}

/**
 * 菜单父子关系映射
 * 用于建立菜单之间的父子关系
 */
export const menuParentRelations: Record<string, string> = {
  '员工详情': '员工名录',
  '字典项管理': '数据字典管理',
  '角色管理': '权限管理',
  '权限管理_子': '权限管理' // 区分权限管理目录和权限管理菜单
}

/**
 * 调整菜单的父子关系
 * @param menus 菜单列表
 */
export function adjustParentRelations(menus: MenuForm[]): MenuForm[] {
  const menuMap = new Map<string, MenuForm>()
  const codeMap = new Map<number, string>()

  // 建立菜单名称到菜单的映射
  menus.forEach(menu => {
    menuMap.set(menu.menuName, menu)
    if (menu.id) {
      codeMap.set(menu.id, menu.menuName)
    }
  })

  // 调整父子关系
  return menus.map(menu => {
    const parentName = menuParentRelations[menu.menuName]
    if (parentName) {
      const parent = menuMap.get(parentName)
      if (parent && parent.id) {
        return {
          ...menu,
          parentId: parent.id
        }
      }
    }
    return menu
  })
}
