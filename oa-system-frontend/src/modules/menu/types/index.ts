/**
 * 菜单管理模块类型定义
 */

/**
 * 菜单类型枚举
 */
export enum MenuType {
  DIRECTORY = 'directory', // 目录
  MENU = 'menu',          // 菜单
  BUTTON = 'button'       // 按钮
}

/**
 * 菜单状态枚举
 */
export enum MenuStatus {
  DISABLED = 0, // 禁用
  ENABLED = 1   // 启用
}

/**
 * 菜单目标枚举
 */
export enum MenuTarget {
  SELF = '_self',   // 当前页
  BLANK = '_blank'  // 新页
}

/**
 * 菜单数据接口
 */
export interface MenuItem {
  id: number
  menuCode: string
  menuName: string
  menuType: MenuType
  parentId: number
  menuLevel: number
  routePath?: string
  componentPath?: string
  redirectPath?: string
  menuIcon?: string
  permission?: string
  sortOrder: number
  visible: boolean
  status: boolean
  isCache: boolean
  isFrame: boolean
  frameUrl?: string
  menuTarget?: MenuTarget
  isSystem: boolean
  remark?: string
  createdAt: string
  updatedAt: string
  children?: MenuItem[]
}

/**
 * 菜单表单接口
 */
export interface MenuForm {
  id?: number
  menuName: string
  menuType: MenuType
  parentId: number
  routePath?: string
  componentPath?: string
  redirectPath?: string
  menuIcon?: string
  permission?: string
  sortOrder: number
  visible: boolean
  status: boolean
  isCache: boolean
  isFrame: boolean
  frameUrl?: string
  menuTarget?: MenuTarget
  remark?: string
}

/**
 * 菜单查询参数接口
 */
export interface MenuQuery {
  type?: MenuType | ''
  status?: boolean | null
  keyword?: string
}

/**
 * 路由菜单项接口
 */
export interface RouteMenuItem {
  path: string
  name: string
  component?: string
  redirect?: string
  meta: {
    title: string
    icon?: string
    hidden?: boolean
    alwaysShow?: boolean
    keepAlive?: boolean
    permissions?: string[]
  }
  children?: RouteMenuItem[]
}

/**
 * 图标选项接口
 */
export interface IconOption {
  name: string
  title: string
  component: any
}

/**
 * 菜单字典项接口
 */
export interface MenuDictItem {
  label: string
  value: string
  dictCode: string
  sort?: number
  remark?: string
}
