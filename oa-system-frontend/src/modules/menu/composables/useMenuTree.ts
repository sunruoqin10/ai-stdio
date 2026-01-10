/**
 * 菜单管理模块 - 菜单树处理组合函数
 */

import type { MenuItem } from '../types'

export function useMenuTree() {
  /**
   * 构建树形结构
   * @param items 菜单项列表
   * @param parentId 父级ID
   * @returns 树形结构
   */
  function buildTree(items: MenuItem[], parentId = 0): MenuItem[] {
    return items
      .filter(item => item.parentId === parentId)
      .map(item => ({
        ...item,
        children: buildTree(items, item.id)
      }))
      .sort((a, b) => a.sortOrder - b.sortOrder)
  }

  /**
   * 展平树形结构
   * @param items 树形菜单列表
   * @returns 扁平化的菜单列表
   */
  function flattenTree(items: MenuItem[]): MenuItem[] {
    const result: MenuItem[] = []

    function flatten(items: MenuItem[]) {
      items.forEach(item => {
        result.push(item)
        if (item.children?.length) {
          flatten(item.children)
        }
      })
    }

    flatten(items)
    return result
  }

  /**
   * 获取所有父级菜单
   * @param items 菜单项列表
   * @param menuId 菜单ID
   * @returns 父级菜单列表
   */
  function getParentMenus(items: MenuItem[], menuId: number): MenuItem[] {
    const parents: MenuItem[] = []
    const map = new Map(flattenTree(items).map(item => [item.id, item]))

    let current = map.get(menuId)
    while (current?.parentId) {
      const parent = map.get(current.parentId)
      if (parent) {
        parents.unshift(parent)
        current = parent
      } else {
        break
      }
    }

    return parents
  }

  /**
   * 获取菜单层级
   * @param items 菜单项列表
   * @param menuId 菜单ID
   * @returns 层级数字
   */
  function getMenuLevel(items: MenuItem[], menuId: number): number {
    const parents = getParentMenus(items, menuId)
    return parents.length + 1
  }

  /**
   * 检查是否可以设置为父级
   * @param items 菜单项列表
   * @param menuId 当前菜单ID
   * @param targetId 目标菜单ID
   * @returns 是否可以
   */
  function canBeParent(items: MenuItem[], menuId: number, targetId: number): boolean {
    if (menuId === targetId) return false

    const menu = flattenTree(items).find(item => item.id === targetId)
    if (!menu) return true

    const children = menu.children || []
    if (children.some(child => child.id === menuId)) return false

    return children.every(child => canBeParent(items, menuId, child.id))
  }

  /**
   * 获取可选的父级菜单
   * @param items 菜单项列表
   * @param excludeId 排除的菜单ID
   * @returns 可选的父级菜单列表
   */
  function getAvailableParents(items: MenuItem[], excludeId?: number): MenuItem[] {
    const flatItems = flattenTree(items)

    return flatItems
      .filter(item => {
        // 排除自己
        if (excludeId && item.id === excludeId) return false

        // 排除按钮类型
        if (item.menuType === 'button') return false

        // 检查层级(最多3级)
        const level = getMenuLevel(items, item.id)
        if (level >= 3) return false

        // 检查是否是自己的子菜单
        if (excludeId && !canBeParent(items, excludeId, item.id)) return false

        return true
      })
      .map(item => ({
        ...item,
        children: undefined
      }))
  }

  return {
    buildTree,
    flattenTree,
    getParentMenus,
    getMenuLevel,
    canBeParent,
    getAvailableParents
  }
}
