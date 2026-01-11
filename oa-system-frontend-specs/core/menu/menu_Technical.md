# 菜单管理模块 - 技术实现规范

> **文档类型**: 技术实现规范 (Technical Implementation)
> **模块类型**: 核心基础
> **复杂度**: ⭐⭐⭐ (3星)
> **技术栈**: Vue 3 + TypeScript + Element Plus + Pinia
> **创建日期**: 2026-01-10
> **版本**: v1.0.0

---

## 📋 目录

- [1. 技术架构](#1-技术架构)
- [2. 数据库设计](#2-数据库设计)
- [3. API接口设计](#3-api接口设计)
- [4. 前端组件设计](#4-前端组件设计)
- [5. 核心功能实现](#5-核心功能实现)
- [6. 数据字典集成](#6-数据字典集成)
- [7. 权限管理集成](#7-权限管理集成)
- [8. 路由管理集成](#8-路由管理集成)
- [9. 图标管理实现](#9-图标管理实现)
- [10. 性能优化](#10-性能优化)

---

## 1. 技术架构

### 1.1 技术栈选型

| 分类 | 技术 | 版本 | 用途 |
|------|------|------|------|
| 前端框架 | Vue 3 | 3.4+ | 渐进式JavaScript框架 |
| 开发语言 | TypeScript | 5.0+ | 类型安全的JavaScript超集 |
| UI组件库 | Element Plus | 2.5+ | 基于Vue 3的组件库 |
| 状态管理 | Pinia | 2.1+ | Vue官方状态管理库 |
| 路由管理 | Vue Router | 4.2+ | Vue官方路由管理器 |
| HTTP客户端 | Axios | 1.6+ | Promise based HTTP client |
| 图标库 | @element-plus/icons-vue | 2.3+ | Element Plus图标库 |

### 1.2 目录结构

```
src/modules/menu/
├── components/           # 组件目录
│   ├── MenuTree.vue     # 菜单树形表格
│   ├── MenuForm.vue     # 菜单表单
│   ├── IconSelector.vue # 图标选择器
│   └── MenuDetail.vue   # 菜单详情弹窗
├── composables/          # 组合式函数
│   ├── useMenu.ts       # 菜单业务逻辑
│   ├── useMenuTree.ts   # 菜单树处理
│   └── useIconSelector.ts # 图标选择逻辑
├── stores/              # 状态管理
│   └── menuStore.ts     # 菜单状态管理
├── api/                 # API接口
│   └── index.ts         # 菜单API
├── types/               # 类型定义
│   └── index.ts         # 菜单类型定义
└── utils/               # 工具函数
    ├── validator.ts     # 验证函数
    └── transformer.ts   # 数据转换
```

### 1.3 核心依赖

```json
{
  "dependencies": {
    "vue": "^3.4.0",
    "vue-router": "^4.2.0",
    "pinia": "^2.1.0",
    "element-plus": "^2.5.0",
    "@element-plus/icons-vue": "^2.3.0",
    "axios": "^1.6.0"
  }
}
```

---

## 2. 数据库设计

### 2.1 菜单表 (sys_menu)

```sql
CREATE TABLE sys_menu (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '菜单ID',
  menu_code VARCHAR(50) NOT NULL UNIQUE COMMENT '菜单编号(MENU+4位序号)',
  menu_name VARCHAR(100) NOT NULL COMMENT '菜单名称',
  menu_type VARCHAR(20) NOT NULL COMMENT '菜单类型(directory/menu/button)',
  parent_id BIGINT DEFAULT 0 COMMENT '父级菜单ID(0表示一级菜单)',
  menu_level INT NOT NULL DEFAULT 1 COMMENT '菜单层级(1-3)',
  route_path VARCHAR(200) COMMENT '路由路径',
  component_path VARCHAR(200) COMMENT '组件路径',
  redirect_path VARCHAR(200) COMMENT '重定向路径',
  menu_icon VARCHAR(100) COMMENT '菜单图标',
  permission VARCHAR(100) COMMENT '权限标识',
  sort_order INT NOT NULL DEFAULT 0 COMMENT '排序号',
  visible TINYINT(1) DEFAULT 1 COMMENT '是否显示(0隐藏/1显示)',
  status TINYINT(1) DEFAULT 1 COMMENT '状态(0禁用/1启用)',
  is_cache TINYINT(1) DEFAULT 0 COMMENT '是否缓存(0不缓存/1缓存)',
  is_frame TINYINT(1) DEFAULT 0 COMMENT '是否外链(0否/1是)',
  frame_url VARCHAR(500) COMMENT '外链URL',
  menu_target VARCHAR(20) COMMENT '链接打开方式(_self/_blank)',
  is_system TINYINT(1) DEFAULT 0 COMMENT '是否系统菜单(0否/1是)',
  remark VARCHAR(500) COMMENT '备注',
  created_by BIGINT COMMENT '创建人ID',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_by BIGINT COMMENT '更新人ID',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted_at DATETIME COMMENT '删除时间',

  INDEX idx_parent_id (parent_id),
  INDEX idx_menu_type (menu_type),
  INDEX idx_status (status),
  INDEX idx_sort_order (sort_order),
  INDEX idx_deleted_at (deleted_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单管理表';
```

### 2.2 初始化数据

```sql
-- 系统菜单初始化
INSERT INTO sys_menu (menu_code, menu_name, menu_type, parent_id, menu_level, route_path, component_path, menu_icon, sort_order, is_system) VALUES
('MENU0001', '系统管理', 'directory', 0, 1, '/system', NULL, 'Setting', 100, 1),
('MENU0002', '用户管理', 'menu', 1, 2, '/system/user', 'system/user/index', 'User', 1, 1),
('MENU0003', '角色管理', 'menu', 1, 2, '/system/role', 'system/role/index', 'UserFilled', 2, 1),
('MENU0004', '菜单管理', 'menu', 1, 2, '/system/menu', 'system/menu/index', 'Menu', 3, 1),
('MENU0005', '部门管理', 'menu', 1, 2, '/system/dept', 'system/dept/index', 'OfficeBuilding', 4, 1),
('MENU0006', '字典管理', 'menu', 1, 2, '/system/dict', 'system/dict/index', 'Notebook', 5, 1),
('MENU0007', '参数设置', 'menu', 1, 2, '/system/config', 'system/config/index', 'Tools', 6, 1);
```

### 2.3 索引设计

| 索引名 | 字段 | 类型 | 用途 |
|--------|------|------|------|
| PRIMARY | id | PRIMARY KEY | 主键索引 |
| uk_menu_code | menu_code | UNIQUE | 菜单编号唯一索引 |
| idx_parent_id | parent_id | INDEX | 父级菜单查询索引 |
| idx_menu_type | menu_type | INDEX | 菜单类型筛选索引 |
| idx_status | status | INDEX | 状态筛选索引 |
| idx_sort_order | sort_order | INDEX | 排序索引 |
| idx_deleted_at | deleted_at | INDEX | 软删除索引 |

---

## 3. API接口设计

### 3.1 接口列表

| 接口名称 | 请求方式 | 路径 | 描述 |
|---------|---------|------|------|
| 获取菜单列表 | GET | /api/menus | 获取所有菜单(树形结构) |
| 获取菜单详情 | GET | /api/menus/:id | 根据ID获取菜单详情 |
| 创建菜单 | POST | /api/menus | 创建新菜单 |
| 更新菜单 | PUT | /api/menus/:id | 更新菜单信息 |
| 删除菜单 | DELETE | /api/menus/:id | 删除菜单 |
| 获取父级菜单选项 | GET | /api/menus/parent-options | 获取可选择的父级菜单 |
| 切换菜单状态 | PATCH | /api/menus/:id/status | 切换启用/禁用状态 |
| 获取菜单路由 | GET | /api/menus/routes | 获取当前用户的路由菜单 |

### 3.2 Mock数据实现 ⭐ NEW

#### 3.2.1 Mock适配器
开发环境使用Mock数据,生产环境调用真实API:

```typescript
// src/modules/menu/api/index.ts
import * as mockApi from './mock-adapter'

export function getMenuList(params?: MenuQuery) {
  // 开发环境使用mock
  return mockApi.mockMenuApiHandlers.getMenuList()

  // 生产环境使用真实API
  // return request.get<MenuItem[]>({ url: '/menus', method: 'get', params })
}
```

#### 3.2.2 Mock数据结构
Mock数据存储在 `src/modules/menu/mock/data.ts`,包含:
- 菜单列表数据
- 菜单详情数据
- 父级菜单选项
- 路由菜单数据

#### 3.2.3 初始化菜单数据
预置菜单定义在 `src/modules/menu/data/initMenus.ts`:
- 系统管理(目录)
- 员工名录
- 员工详情(隐藏)
- 数据字典管理
- 字典项管理(隐藏)
- 菜单管理
- 权限管理(目录)
- 角色管理
- 权限管理

### 3.3 接口详细设计

### 3.3 接口详细设计

#### 3.2.1 获取菜单列表

**请求**:
```http
GET /api/menus?type=&status=&keyword=
```

**查询参数**:
| 参数 | 类型 | 必填 | 描述 |
|------|------|------|------|
| type | string | 否 | 菜单类型筛选(directory/menu/button) |
| status | number | 否 | 状态筛选(0禁用/1启用) |
| keyword | string | 否 | 搜索关键字(名称/路由) |

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "menuCode": "MENU0001",
      "menuName": "系统管理",
      "menuType": "directory",
      "parentId": 0,
      "menuLevel": 1,
      "routePath": "/system",
      "componentPath": null,
      "menuIcon": "Setting",
      "sortOrder": 100,
      "visible": true,
      "status": true,
      "children": [
        {
          "id": 2,
          "menuCode": "MENU0002",
          "menuName": "用户管理",
          "menuType": "menu",
          "parentId": 1,
          "menuLevel": 2,
          "routePath": "/system/user",
          "componentPath": "system/user/index",
          "menuIcon": "User",
          "permission": "system:user:view",
          "sortOrder": 1,
          "visible": true,
          "status": true,
          "children": []
        }
      ]
    }
  ]
}
```

#### 3.2.2 创建菜单

**请求**:
```http
POST /api/menus
Content-Type: application/json
```

**请求体**:
```json
{
  "menuName": "用户管理",
  "menuType": "menu",
  "parentId": 1,
  "routePath": "/system/user",
  "componentPath": "system/user/index",
  "menuIcon": "User",
  "permission": "system:user:view",
  "sortOrder": 1,
  "visible": true,
  "status": true
}
```

**响应**:
```json
{
  "code": 200,
  "message": "创建成功",
  "data": {
    "id": 2,
    "menuCode": "MENU0002",
    "menuName": "用户管理",
    "menuType": "menu",
    "parentId": 1,
    "menuLevel": 2,
    "routePath": "/system/user",
    "componentPath": "system/user/index",
    "menuIcon": "User",
    "permission": "system:user:view",
    "sortOrder": 1,
    "visible": true,
    "status": true,
    "createdAt": "2026-01-10T10:00:00Z"
  }
}
```

#### 3.2.3 获取菜单路由

**请求**:
```http
GET /api/menus/routes
```

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "path": "/system",
      "name": "System",
      "component": "Layout",
      "meta": {
        "title": "系统管理",
        "icon": "Setting",
        "hidden": false,
        "alwaysShow": true
      },
      "children": [
        {
          "path": "user",
          "name": "User",
          "component": "system/user/index",
          "meta": {
            "title": "用户管理",
            "icon": "User",
            "hidden": false,
            "keepAlive": true,
            "permissions": ["system:user:view"]
          }
        }
      ]
    }
  ]
}
```

---

## 4. 前端组件设计

### 4.1 组件层次结构

```
MenuIndex.vue (主页面)
├── MenuTree.vue (菜单树形表格)
│   ├── MenuDetail.vue (菜单详情弹窗)
│   └── IconSelector.vue (图标选择器)
└── MenuForm.vue (菜单表单弹窗)
    └── IconSelector.vue (图标选择器)
```

### 4.2 类型定义

```typescript
// src/modules/menu/types/index.ts

export interface MenuItem {
  id: number
  menuCode: string
  menuName: string
  menuType: 'directory' | 'menu' | 'button'
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
  menuTarget?: '_self' | '_blank'
  isSystem: boolean
  remark?: string
  createdAt: string
  updatedAt: string
  children?: MenuItem[]
}

export interface MenuForm {
  id?: number
  menuName: string
  menuType: 'directory' | 'menu' | 'button'
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
  menuTarget?: '_self' | '_blank'
  remark?: string
}

export interface MenuQuery {
  type?: string
  status?: boolean | null
  keyword?: string
}

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

export interface IconOption {
  name: string
  title: string
  component: any
}
```

---

## 5. 核心功能实现

### 5.1 菜单API实现

```typescript
// src/modules/menu/api/index.ts

import request from '@/utils/request'
import type { MenuItem, MenuForm, MenuQuery, RouteMenuItem } from '../types'

/**
 * 获取菜单列表(树形结构)
 */
export function getMenuList(params?: MenuQuery) {
  return request<MenuItem[]>({
    url: '/menus',
    method: 'get',
    params
  })
}

/**
 * 获取菜单详情
 */
export function getMenuDetail(id: number) {
  return request<MenuItem>({
    url: `/menus/${id}`,
    method: 'get'
  })
}

/**
 * 创建菜单
 */
export function createMenu(data: MenuForm) {
  return request<MenuItem>({
    url: '/menus',
    method: 'post',
    data
  })
}

/**
 * 更新菜单
 */
export function updateMenu(id: number, data: MenuForm) {
  return request<MenuItem>({
    url: `/menus/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除菜单
 */
export function deleteMenu(id: number) {
  return request({
    url: `/menus/${id}`,
    method: 'delete'
  })
}

/**
 * 获取父级菜单选项
 */
export function getParentOptions() {
  return request<MenuItem[]>({
    url: '/menus/parent-options',
    method: 'get'
  })
}

/**
 * 切换菜单状态
 */
export function toggleMenuStatus(id: number, status: boolean) {
  return request({
    url: `/menus/${id}/status`,
    method: 'patch',
    data: { status }
  })
}

/**
 * 获取菜单路由
 */
export function getMenuRoutes() {
  return request<RouteMenuItem[]>({
    url: '/menus/routes',
    method: 'get'
  })
}
```

### 5.2 菜单状态管理

```typescript
// src/modules/menu/stores/menuStore.ts

import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { MenuItem, MenuForm, MenuQuery } from '../types'
import * as menuApi from '../api'

export const useMenuStore = defineStore('menu', () => {
  // 状态
  const menuList = ref<MenuItem[]>([])
  const currentMenu = ref<MenuItem | null>(null)
  const loading = ref(false)
  const queryParams = ref<MenuQuery>({})

  // 计算属性
  const treeMenuList = computed(() => {
    return buildTree(menuList.value)
  })

  const menuMap = computed(() => {
    const map = new Map<number, MenuItem>()
    const buildMap = (items: MenuItem[]) => {
      items.forEach(item => {
        map.set(item.id, item)
        if (item.children?.length) {
          buildMap(item.children)
        }
      })
    }
    buildMap(menuList.value)
    return map
  })

  // 操作
  async function fetchMenuList(params?: MenuQuery) {
    loading.value = true
    try {
      const data = await menuApi.getMenuList(params)
      menuList.value = data
      queryParams.value = params || {}
      return data
    } finally {
      loading.value = false
    }
  }

  async function fetchMenuDetail(id: number) {
    loading.value = true
    try {
      const data = await menuApi.getMenuDetail(id)
      currentMenu.value = data
      return data
    } finally {
      loading.value = false
    }
  }

  async function createMenu(form: MenuForm) {
    return await menuApi.createMenu(form)
  }

  async function updateMenu(id: number, form: MenuForm) {
    return await menuApi.updateMenu(id, form)
  }

  async function deleteMenu(id: number) {
    return await menuApi.deleteMenu(id)
  }

  async function toggleStatus(id: number, status: boolean) {
    return await menuApi.toggleMenuStatus(id, status)
  }

  function clearCurrentMenu() {
    currentMenu.value = null
  }

  // 工具函数
  function buildTree(items: MenuItem[], parentId = 0): MenuItem[] {
    return items
      .filter(item => item.parentId === parentId)
      .map(item => ({
        ...item,
        children: buildTree(items, item.id)
      }))
      .sort((a, b) => a.sortOrder - b.sortOrder)
  }

  function getMenuPath(menuId: number): MenuItem[] {
    const path: MenuItem[] = []
    let menu = menuMap.value.get(menuId)

    while (menu) {
      path.unshift(menu)
      menu = menu.parentId ? menuMap.value.get(menu.parentId) : undefined
    }

    return path
  }

  return {
    // 状态
    menuList,
    currentMenu,
    loading,
    queryParams,

    // 计算属性
    treeMenuList,
    menuMap,

    // 操作
    fetchMenuList,
    fetchMenuDetail,
    createMenu,
    updateMenu,
    deleteMenu,
    toggleStatus,
    clearCurrentMenu,
    getMenuPath
  }
})
```

### 5.3 菜单业务逻辑

```typescript
// src/modules/menu/composables/useMenu.ts

import { ref, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useMenuStore } from '../stores/menuStore'
import { useAuthStore } from '@/modules/auth/stores/authStore'
import type { MenuForm, MenuItem } from '../types'

export function useMenu() {
  const menuStore = useMenuStore()
  const authStore = useAuthStore()

  const dialogVisible = ref(false)
  const dialogType = ref<'create' | 'edit'>('create')
  const formData = ref<Partial<MenuForm>>({})
  const formRef = ref()

  // 权限检查
  const canCreate = computed(() => authStore.hasPermission('menu:create'))
  const canEdit = computed(() => authStore.hasPermission('menu:edit'))
  const canDelete = computed(() => authStore.hasPermission('menu:delete'))
  const canEnable = computed(() => authStore.hasPermission('menu:enable'))

  // 打开新增对话框
  function openCreateDialog(parentId = 0) {
    dialogType.value = 'create'
    formData.value = {
      parentId,
      menuName: '',
      menuType: 'menu',
      sortOrder: 0,
      visible: true,
      status: true,
      isCache: false,
      isFrame: false
    }
    dialogVisible.value = true
  }

  // 打开编辑对话框
  async function openEditDialog(menu: MenuItem) {
    dialogType.value = 'edit'
    formData.value = { ...menu }
    dialogVisible.value = true
  }

  // 保存菜单
  async function handleSave() {
    try {
      await formRef.value?.validate()

      if (dialogType.value === 'create') {
        await menuStore.createMenu(formData.value as MenuForm)
        ElMessage.success('创建成功')
      } else {
        await menuStore.updateMenu(formData.value!.id!, formData.value as MenuForm)
        ElMessage.success('更新成功')
      }

      dialogVisible.value = false
      await menuStore.fetchMenuList()
    } catch (error) {
      ElMessage.error('保存失败')
      throw error
    }
  }

  // 删除菜单
  async function handleDelete(menu: MenuItem) {
    if (!canDelete.value) {
      ElMessage.warning('您没有删除权限')
      return
    }

    if (menu.isSystem) {
      ElMessage.warning('系统菜单不能删除')
      return
    }

    if (menu.children?.length) {
      ElMessage.warning('请先删除子菜单')
      return
    }

    try {
      await ElMessageBox.confirm('确认删除该菜单吗?', '提示', {
        type: 'warning'
      })

      await menuStore.deleteMenu(menu.id)
      ElMessage.success('删除成功')
      await menuStore.fetchMenuList()
    } catch (error) {
      if (error !== 'cancel') {
        ElMessage.error('删除失败')
      }
    }
  }

  // 切换状态
  async function handleToggleStatus(menu: MenuItem) {
    if (!canEnable.value) {
      ElMessage.warning('您没有操作权限')
      return
    }

    try {
      const newStatus = !menu.status
      await menuStore.toggleStatus(menu.id, newStatus)
      ElMessage.success(newStatus ? '已启用' : '已禁用')
      await menuStore.fetchMenuList()
    } catch (error) {
      ElMessage.error('操作失败')
    }
  }

  return {
    // 状态
    dialogVisible,
    dialogType,
    formData,
    formRef,

    // 权限
    canCreate,
    canEdit,
    canDelete,
    canEnable,

    // 操作
    openCreateDialog,
    openEditDialog,
    handleSave,
    handleDelete,
    handleToggleStatus
  }
}
```

### 5.4 菜单树处理

```typescript
// src/modules/menu/composables/useMenuTree.ts

import type { MenuItem } from '../types'

export function useMenuTree() {
  /**
   * 构建树形结构
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
   */
  function getMenuLevel(items: MenuItem[], menuId: number): number {
    const parents = getParentMenus(items, menuId)
    return parents.length + 1
  }

  /**
   * 检查是否可以设置为父级
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
```

---

## 6. 数据字典集成

### 6.1 字典类型定义

```typescript
// src/modules/menu/config/dict.ts

export const MENU_DICT_TYPES = {
  MENU_TYPE: 'menu_type',        // 菜单类型(目录/菜单/按钮)
  MENU_STATUS: 'menu_status',    // 菜单状态(启用/禁用)
  MENU_TARGET: 'menu_target'     // 链接打开方式(当前页/新页)
} as const
```

### 6.2 字典API调用

```typescript
// src/modules/menu/composables/useMenuDict.ts

import { computed } from 'vue'
import { useDictStore } from '@/modules/dict/stores/dictStore'
import { MENU_DICT_TYPES } from '../config/dict'

export function useMenuDict() {
  const dictStore = useDictStore()

  // 菜单类型
  const menuTypeOptions = computed(() =>
    dictStore.getDictItems(MENU_DICT_TYPES.MENU_TYPE)
  )

  const menuTypeMap = computed(() =>
    dictStore.getDictMap(MENU_DICT_TYPES.MENU_TYPE)
  )

  // 菜单状态
  const menuStatusOptions = computed(() =>
    dictStore.getDictItems(MENU_DICT_TYPES.MENU_STATUS)
  )

  const menuStatusMap = computed(() =>
    dictStore.getDictMap(MENU_DICT_TYPES.MENU_STATUS)
  )

  // 链接目标
  const menuTargetOptions = computed(() =>
    dictStore.getDictItems(MENU_DICT_TYPES.MENU_TARGET)
  )

  // 获取显示文本
  function getMenuTypeLabel(value: string) {
    return menuTypeMap.value.get(value)?.label || value
  }

  function getMenuStatusLabel(value: boolean) {
    const strValue = value ? '1' : '0'
    return menuStatusMap.value.get(strValue)?.label || (value ? '启用' : '禁用')
  }

  function getMenuTargetLabel(value: string) {
    return menuTargetOptions.value.find(item => item.value === value)?.label || value
  }

  return {
    menuTypeOptions,
    menuTypeMap,
    menuStatusOptions,
    menuStatusMap,
    menuTargetOptions,
    getMenuTypeLabel,
    getMenuStatusLabel,
    getMenuTargetLabel
  }
}
```

### 6.3 字典缓存策略

```typescript
// src/modules/menu/config/dictCache.ts

export const MENU_DICT_CACHE = {
  // 预加载字典
  preload: [
    'menu_type',
    'menu_status'
  ],

  // 按需加载字典
  onDemand: [
    'menu_target'
  ],

  // 缓存过期时间: 30分钟
  expireTime: 30 * 60 * 1000
}
```

### 6.4 字典初始化

```typescript
// src/modules/menu/utils/dictInit.ts

import { MENU_DICT_CACHE } from '../config/dictCache'
import { useDictStore } from '@/modules/dict/stores/dictStore'

export async function initMenuDict() {
  const dictStore = useDictStore()

  // 预加载常用字典
  await Promise.all(
    MENU_DICT_CACHE.preload.map(type => dictStore.loadDictItems(type))
  )
}
```

---

## 7. 权限管理集成

### 7.1 权限定义

```typescript
// src/modules/menu/config/permissions.ts

export const MENU_PERMISSIONS = {
  VIEW: 'menu:view',
  VIEW_ALL: 'menu:view_all',
  CREATE: 'menu:create',
  EDIT: 'menu:edit',
  DELETE: 'menu:delete',
  SORT: 'menu:sort',
  ENABLE: 'menu:enable',
  EXPORT: 'menu:export'
} as const

export type MenuPermission = typeof MENU_PERMISSIONS[keyof typeof MENU_PERMISSIONS]
```

### 7.2 权限检查Hook

```typescript
// src/modules/menu/composables/useMenuPermission.ts

import { computed } from 'vue'
import { useAuthStore } from '@/modules/auth/stores/authStore'
import { MENU_PERMISSIONS } from '../config/permissions'

export function useMenuPermission() {
  const authStore = useAuthStore()

  // 单个权限检查
  const hasPermission = (permission: string) => {
    return authStore.hasPermission(permission)
  }

  // 组合权限检查
  const canCreate = computed(() => hasPermission(MENU_PERMISSIONS.CREATE))
  const canEdit = computed(() => hasPermission(MENU_PERMISSIONS.EDIT))
  const canDelete = computed(() => hasPermission(MENU_PERMISSIONS.DELETE))
  const canSort = computed(() => hasPermission(MENU_PERMISSIONS.SORT))
  const canEnable = computed(() => hasPermission(MENU_PERMISSIONS.ENABLE))
  const canExport = computed(() => hasPermission(MENU_PERMISSIONS.EXPORT))
  const canViewAll = computed(() => hasPermission(MENU_PERMISSIONS.VIEW_ALL))

  // 菜单操作权限检查
  const canEditMenu = (menu: any) => {
    if (!canEdit.value) return false
    if (menu.isSystem) return false
    return true
  }

  const canDeleteMenu = (menu: any) => {
    if (!canDelete.value) return false
    if (menu.isSystem) return false
    if (menu.children?.length > 0) return false
    return true
  }

  const canEnableMenu = (menu: any) => {
    if (!canEnable.value) return false
    if (menu.isSystem) return false
    return true
  }

  return {
    hasPermission,
    canCreate,
    canEdit,
    canDelete,
    canSort,
    canEnable,
    canExport,
    canViewAll,
    canEditMenu,
    canDeleteMenu,
    canEnableMenu
  }
}
```

### 7.3 数据权限过滤

```typescript
// src/modules/menu/utils/permissionFilter.ts

import type { MenuItem } from '../types'
import { useAuthStore } from '@/modules/auth/stores/authStore'

export function filterMenusByPermission(menus: MenuItem[]): MenuItem[] {
  const authStore = useAuthStore()

  // 查看所有菜单权限
  if (authStore.hasPermission('menu:view_all')) {
    return menus
  }

  // 根据权限过滤菜单
  return menus
    .filter(menu => {
      // 没有权限标识的菜单默认显示
      if (!menu.permission) return true

      // 检查权限
      return authStore.hasPermission(menu.permission)
    })
    .map(menu => ({
      ...menu,
      children: menu.children ? filterMenusByPermission(menu.children) : undefined
    }))
    .filter(menu => {
      // 如果是目录类型,至少有一个子菜单有权限才显示
      if (menu.menuType === 'directory') {
        return menu.children && menu.children.length > 0
      }
      return true
    })
}
```

---

## 8. 路由管理集成

### 8.1 路由转换器

```typescript
// src/modules/menu/utils/routeTransformer.ts

import type { MenuItem, RouteMenuItem } from '../types'

/**
 * 转换菜单配置为路由配置
 */
export function transformMenuToRoute(menu: MenuItem): RouteMenuItem {
  const route: RouteMenuItem = {
    path: menu.routePath || '',
    name: menu.menuName,
    meta: {
      title: menu.menuName,
      icon: menu.menuIcon,
      hidden: !menu.visible,
      permissions: menu.permission ? [menu.permission] : []
    }
  }

  // 组件路径
  if (menu.componentPath) {
    route.component = menu.componentPath
  }

  // 重定向
  if (menu.redirectPath) {
    route.redirect = menu.redirectPath
  }

  // 缓存配置
  if (menu.isCache) {
    route.meta.keepAlive = true
  }

  // 外链配置
  if (menu.isFrame && menu.frameUrl) {
    route.meta.isFrame = true
    route.meta.frameUrl = menu.frameUrl
    route.meta.frameTarget = menu.menuTarget || '_self'
  }

  // 目录类型配置
  if (menu.menuType === 'directory') {
    route.meta.alwaysShow = true
  }

  return route
}

/**
 * 构建路由树
 */
export function buildRouteTree(menus: MenuItem[]): RouteMenuItem[] {
  return menus
    .filter(menu => menu.menuType !== 'button')
    .map(menu => {
      const route = transformMenuToRoute(menu)

      if (menu.children?.length) {
        route.children = buildRouteTree(menu.children)
      }

      return route
    })
}

/**
 * 扁平化路由
 */
export function flattenRoutes(routes: RouteMenuItem[]): RouteMenuItem[] {
  const result: RouteMenuItem[] = []

  function flatten(routes: RouteMenuItem[]) {
    routes.forEach(route => {
      result.push(route)
      if (route.children?.length) {
        flatten(route.children)
      }
    })
  }

  flatten(routes)
  return result
}
```

### 8.2 动态路由加载

```typescript
// src/modules/menu/utils/routeLoader.ts

import { router } from '@/router'
import { useMenuStore } from '../stores/menuStore'
import { filterMenusByPermission } from './permissionFilter'
import { buildRouteTree } from './routeTransformer'

/**
 * 加载动态路由
 */
export async function loadDynamicRoutes() {
  const menuStore = useMenuStore()

  // 获取菜单列表
  await menuStore.fetchMenuList()

  // 过滤有权限的菜单
  const authorizedMenus = filterMenusByPermission(menuStore.menuList)

  // 转换为路由配置
  const routes = buildRouteTree(authorizedMenus)

  // 动态添加路由
  routes.forEach(route => {
    router.addRoute(route)
  })
}

/**
 * 重置路由
 */
export function resetRoutes() {
  const menuStore = useMenuStore()

  // 删除动态添加的路由
  menuStore.menuList.forEach(menu => {
    if (menu.routePath && router.hasRoute(menu.menuName)) {
      router.removeRoute(menu.menuName)
    }
  })
}
```

### 8.3 路由守卫集成

```typescript
// src/router/guards/menuGuard.ts

import { loadDynamicRoutes } from '@/modules/menu/utils/routeLoader'

export async function setupMenuGuard() {
  // 在路由守卫中加载菜单
  router.beforeEach(async (to, from, next) => {
    const authStore = useAuthStore()

    if (authStore.isAuthenticated) {
      // 首次访问时加载菜单
      if (!authStore.menuLoaded) {
        try {
          await loadDynamicRoutes()
          authStore.menuLoaded = true

          // 重新跳转到目标路由
          next({ ...to, replace: true })
          return
        } catch (error) {
          console.error('加载菜单失败:', error)
          next('/login')
          return
        }
      }
    }

    next()
  })
}
```

---

## 9. 图标管理实现

### 9.1 图标列表

```typescript
// src/modules/menu/config/icons.ts

import * as ElementPlusIcons from '@element-plus/icons-vue'

export const ICON_LIST = Object.keys(ElementPlusIcons).map(name => ({
  name,
  title: name,
  component: (ElementPlusIcons as any)[name]
}))

export function getIconComponent(name: string) {
  return (ElementPlusIcons as any)[name]
}
```

### 9.2 图标选择器组件

```vue
<!-- src/modules/menu/components/IconSelector.vue -->

<template>
  <el-select
    v-model="selectedIcon"
    filterable
    :filter-method="filterIcons"
    placeholder="请选择图标"
    @change="handleChange"
  >
    <el-option
      v-for="icon in filteredIcons"
      :key="icon.name"
      :label="icon.title"
      :value="icon.name"
    >
      <div class="icon-option">
        <el-icon :size="20">
          <component :is="icon.component" />
        </el-icon>
        <span class="icon-label">{{ icon.title }}</span>
      </div>
    </el-option>
  </el-select>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { ICON_LIST } from '../config/icons'

interface Props {
  modelValue: string
}

interface Emits {
  (e: 'update:modelValue', value: string): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const selectedIcon = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const searchText = ref('')

const filteredIcons = computed(() => {
  if (!searchText.value) {
    return ICON_LIST
  }

  const search = searchText.value.toLowerCase()
  return ICON_LIST.filter(icon =>
    icon.name.toLowerCase().includes(search)
  )
})

function filterIcons(query: string) {
  searchText.value = query
}

function handleChange(value: string) {
  emit('update:modelValue', value)
}
</script>

<style scoped>
.icon-option {
  display: flex;
  align-items: center;
  gap: 8px;
}

.icon-label {
  margin-left: 8px;
}
</style>
```

### 9.3 图标预览组件

```vue
<!-- src/modules/menu/components/IconPreview.vue -->

<template>
  <div v-if="icon" class="icon-preview">
    <el-icon :size="size">
      <component :is="getIconComponent(icon)" />
    </el-icon>
  </div>
</template>

<script setup lang="ts">
import { getIconComponent } from '../config/icons'

interface Props {
  icon: string
  size?: number
}

withDefaults(defineProps<Props>(), {
  size: 20
})
</script>

<style scoped>
.icon-preview {
  display: inline-flex;
  align-items: center;
  justify-content: center;
}
</style>
```

---

## 10. 性能优化

### 10.1 列表虚拟滚动

```vue
<template>
  <el-table-v2
    :columns="columns"
    :data="menuList"
    :width="tableWidth"
    :height="600"
    fixed
  />
</template>
```

### 10.2 菜单懒加载

```typescript
// 懒加载子菜单
async function loadChildren(menu: MenuItem) {
  if (menu.childrenLoaded) return

  const children = await menuStore.fetchMenuList({ parentId: menu.id })
  menu.children = children
  menu.childrenLoaded = true
}
```

### 10.3 图标按需加载

```typescript
// 动态导入图标组件
async function loadIconComponent(name: string) {
  return await import(`@element-plus/icons-vue`)
    .then(module => module[name])
}
```

### 10.4 缓存策略

```typescript
// 菜单数据缓存
const MENU_CACHE_KEY = 'menu_cache'
const MENU_CACHE_EXPIRE = 30 * 60 * 1000 // 30分钟

export function getMenuCache() {
  const cached = localStorage.getItem(MENU_CACHE_KEY)
  if (!cached) return null

  const { data, timestamp } = JSON.parse(cached)
  if (Date.now() - timestamp > MENU_CACHE_EXPIRE) {
    localStorage.removeItem(MENU_CACHE_KEY)
    return null
  }

  return data
}

export function setMenuCache(data: any) {
  localStorage.setItem(MENU_CACHE_KEY, JSON.stringify({
    data,
    timestamp: Date.now()
  }))
}
```

---

**文档版本**: v1.0.0
**创建人**: AI开发助手
**最后更新**: 2026-01-10
