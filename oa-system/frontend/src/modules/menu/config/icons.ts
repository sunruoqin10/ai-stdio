/**
 * 菜单管理模块 - 图标配置
 */

import * as ElementPlusIcons from '@element-plus/icons-vue'

/**
 * 图标选项接口
 */
export interface IconOption {
  name: string
  title: string
  component: any
}

/**
 * Element Plus 图标列表
 */
export const ICON_LIST: IconOption[] = Object.keys(ElementPlusIcons).map(name => ({
  name,
  title: name,
  component: (ElementPlusIcons as any)[name]
}))

/**
 * 获取图标组件
 * @param name 图标名称
 * @returns 图标组件
 */
export function getIconComponent(name: string) {
  return (ElementPlusIcons as any)[name]
}

/**
 * 常用图标列表
 */
export const COMMON_ICONS = [
  'Home',
  'Setting',
  'User',
  'UserFilled',
  'Menu',
  'Document',
  'DocumentCopy',
  'Folder',
  'FolderOpened',
  'Files',
  'OfficeBuilding',
  'Notebook',
  'Tools',
  'Lock',
  'Unlock',
  'Monitor',
  'Mouse',
  'Phone',
  'Location',
  'LocationFilled',
  'Notification',
  'Bell',
  'Calendar',
  'Clock',
  'Timer',
  'Operation',
  'PieChart',
  'DataLine',
  'TrendCharts'
] as const
