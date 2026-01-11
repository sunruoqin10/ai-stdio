# 菜单管理模块

> **模块类型**: 核心基础模块
> **复杂度**: ⭐⭐⭐ (3星)
> **开发状态**: ✅ 已实现
> **版本**: v1.0.0
> **最后更新**: 2026-01-11

---

## 📖 模块简介

菜单管理模块是OA系统的核心基础模块,负责管理系统后台菜单配置。支持树形结构的菜单层级关系(最多3级),提供菜单CRUD、图标配置、权限关联、路由配置等完整功能。

### 核心特性

- ✅ **树形结构管理**: 支持最多3级菜单嵌套
- ✅ **可视化图标选择**: 内置Element Plus图标库
- ✅ **动态路由注册**: 菜单配置自动转换为路由
- ✅ **权限集成**: 基于权限的菜单动态显示
- ✅ **系统菜单保护**: 防止误删系统核心菜单
- ✅ **灵活配置**: 支持外链、缓存、重定向等高级配置
- ⭐ **一键初始化**: 预置系统菜单,快速部署
- ⭐ **开发环境优化**: Mock数据支持,权限检查优化

---

## 📋 文档索引

### 规范文档

| 文档名称 | 描述 | 链接 |
|---------|------|------|
| 功能需求规范 | 详细的功能需求和业务规则 | [menu_Functional.md](./menu_Functional.md) |
| 技术实现规范 | 技术架构和实现细节 | [menu_Technical.md](./menu_Technical.md) |
| UI设计规范 | 界面设计和交互规范 | [menu_Design.md](./menu_Design.md) |

### 快速导航

- [功能概述](#-功能概述)
- [技术架构](#-技术架构)
- [数据模型](#-数据模型)
- [核心功能](#-核心功能)
- [使用指南](#-使用指南)
- [开发指南](#-开发指南)
- [常见问题](#-常见问题)

---

## 🎯 功能概述

### 菜单类型

| 类型 | 说明 | 可配置项 |
|------|------|---------|
| **目录 (Directory)** | 用于菜单分组,不含实际页面 | 图标、名称、排序 |
| **菜单 (Menu)** | 可点击的菜单项,跳转到具体页面 | 路由、组件、权限、图标 |
| **按钮 (Button)** | 页面内的操作按钮权限标识 | 权限标识、名称 |

### 菜单层级

```
一级菜单 (目录)
├── 二级菜单 (菜单)
│   ├── 三级菜单 (按钮权限)
│   └── 三级菜单 (按钮权限)
└── 二级菜单 (菜单)
    └── 三级菜单 (按钮权限)
```

### 核心功能

#### 1. 菜单管理
- 创建、编辑、删除菜单
- 树形结构展示
- 拖拽排序 (P2功能)
- 批量操作

#### 2. 图标管理
- 图标选择器
- 图标搜索
- 图标预览
- 自定义图标上传 (P2功能)

#### 3. 权限控制
- 菜单权限关联
- 动态菜单显示
- 字段级权限控制
- 操作权限控制

#### 4. 路由管理
- 路由自动注册
- 动态路由加载
- 路由缓存配置
- 外链路由支持

---

## 🏗️ 技术架构

### 技术栈

| 分类 | 技术 | 版本 | 用途 |
|------|------|------|------|
| 前端框架 | Vue 3 | 3.4+ | 渐进式JavaScript框架 |
| 开发语言 | TypeScript | 5.0+ | 类型安全的JavaScript超集 |
| UI组件库 | Element Plus | 2.5+ | 基于Vue 3的组件库 |
| 状态管理 | Pinia | 2.1+ | Vue官方状态管理库 |
| 路由管理 | Vue Router | 4.2+ | Vue官方路由管理器 |
| HTTP客户端 | Axios | 1.6+ | Promise based HTTP client |
| 图标库 | @element-plus/icons-vue | 2.3+ | Element Plus图标库 |

### 目录结构

```
src/modules/menu/
├── components/              # 组件目录
│   ├── MenuTree.vue        # 菜单树形表格
│   ├── MenuForm.vue        # 菜单表单
│   ├── IconSelector.vue    # 图标选择器
│   └── MenuDetail.vue      # 菜单详情弹窗
├── composables/             # 组合式函数
│   ├── useMenu.ts          # 菜单业务逻辑
│   ├── useMenuTree.ts      # 菜单树处理
│   ├── useMenuDict.ts      # 数据字典集成
│   └── useMenuPermission.ts # 权限控制
├── stores/                  # 状态管理
│   └── menuStore.ts        # 菜单状态管理
├── api/                     # API接口
│   └── index.ts            # 菜单API
├── types/                   # 类型定义
│   └── index.ts            # 菜单类型定义
├── config/                  # 配置文件
│   ├── dict.ts             # 数据字典配置
│   ├── permissions.ts      # 权限配置
│   └── icons.ts            # 图标配置
└── utils/                   # 工具函数
    ├── validator.ts        # 验证函数
    ├── transformer.ts      # 数据转换
    └── routeLoader.ts      # 路由加载
```

---

## 📊 数据模型

### 菜单表 (sys_menu)

| 字段名 | 类型 | 长度 | 必填 | 说明 |
|-------|------|------|------|------|
| id | BIGINT | - | ✅ | 主键ID |
| menu_code | VARCHAR | 50 | ✅ | 菜单编号 (MENU+4位序号) |
| menu_name | VARCHAR | 100 | ✅ | 菜单名称 |
| menu_type | VARCHAR | 20 | ✅ | 菜单类型 (directory/menu/button) |
| parent_id | BIGINT | - | ✅ | 父级菜单ID (0表示一级) |
| menu_level | INT | - | ✅ | 菜单层级 (1-3) |
| route_path | VARCHAR | 200 | ❌ | 路由路径 |
| component_path | VARCHAR | 200 | ❌ | 组件路径 |
| redirect_path | VARCHAR | 200 | ❌ | 重定向路径 |
| menu_icon | VARCHAR | 100 | ❌ | 菜单图标 |
| permission | VARCHAR | 100 | ❌ | 权限标识 |
| sort_order | INT | - | ✅ | 排序号 |
| visible | TINYINT | 1 | ✅ | 是否显示 (0隐藏/1显示) |
| status | TINYINT | 1 | ✅ | 状态 (0禁用/1启用) |
| is_cache | TINYINT | 1 | ✅ | 是否缓存 (0不缓存/1缓存) |
| is_frame | TINYINT | 1 | ✅ | 是否外链 (0否/1是) |
| frame_url | VARCHAR | 500 | ❌ | 外链URL |
| menu_target | VARCHAR | 20 | ❌ | 打开方式 (_self/_blank) |
| is_system | TINYINT | 1 | ✅ | 是否系统菜单 (0否/1是) |
| remark | VARCHAR | 500 | ❌ | 备注 |
| created_at | DATETIME | - | ✅ | 创建时间 |
| updated_at | DATETIME | - | ✅ | 更新时间 |

### 关键索引

- PRIMARY KEY (`id`)
- UNIQUE KEY `uk_menu_code` (`menu_code`)
- INDEX `idx_parent_id` (`parent_id`)
- INDEX `idx_menu_type` (`menu_type`)
- INDEX `idx_status` (`status`)

---

## 🚀 核心功能

### 1. 菜单CRUD操作

#### 创建菜单

```typescript
import { useMenu } from '@/modules/menu/composables/useMenu'

const { openCreateDialog, handleSave } = useMenu()

// 打开新增对话框 (一级菜单)
openCreateDialog(0)

// 打开新增对话框 (子菜单)
openCreateDialog(parentMenuId)

// 保存菜单
await handleSave()
```

#### 编辑菜单

```typescript
const { openEditDialog, handleSave } = useMenu()

// 打开编辑对话框
await openEditDialog(menu)

// 保存修改
await handleSave()
```

#### 删除菜单

```typescript
const { handleDelete } = useMenu()

// 删除菜单 (有保护机制)
await handleDelete(menu)

// 系统菜单不能删除
// 有子菜单的菜单不能删除
```

### 2. 菜单树处理

#### 构建树形结构

```typescript
import { useMenuTree } from '@/modules/menu/composables/useMenuTree'

const { buildTree, flattenTree, getMenuLevel } = useMenuTree()

// 构建树
const tree = buildTree(menuList)

// 展平树
const flat = flattenTree(tree)

// 获取层级
const level = getMenuLevel(menuList, menuId)
```

#### 获取可选父级菜单

```typescript
const { getAvailableParents } = useMenuTree()

// 获取可选择的父级菜单 (排除自己和自己的子菜单)
const parentOptions = getAvailableParents(menuList, excludeId)
```

### 3. 图标选择

```vue
<template>
  <icon-selector v-model="form.menuIcon" />
</template>

<script setup>
import IconSelector from '@/modules/menu/components/IconSelector.vue'
</script>
```

### 4. 动态路由加载

```typescript
import { loadDynamicRoutes } from '@/modules/menu/utils/routeLoader'

// 在应用启动时加载
await loadDynamicRoutes()
```

### 5. 权限控制

```typescript
import { useMenuPermission } from '@/modules/menu/composables/useMenuPermission'

const {
  canCreate,
  canEdit,
  canDelete,
  canEditMenu,
  canDeleteMenu
} = useMenuPermission()

// 检查权限
if (canCreate.value) {
  // 显示创建按钮
}
```

---

## 📖 使用指南

### 用户操作流程

#### 新增一级菜单

1. 点击"新增菜单"按钮
2. 父级菜单选择"无(一级菜单)"
3. 填写菜单基本信息:
   - 菜单名称 (必填)
   - 菜单类型 (必填)
   - 菜单图标 (可选)
   - 排序号 (必填)
4. 配置路由信息 (菜单类型):
   - 路由路径 (必填)
   - 组件路径 (必填)
   - 权限标识 (可选)
5. 点击"确定"保存

#### 新增子菜单

1. 右键点击父菜单,选择"添加子菜单"
2. 父级菜单自动选中
3. 填写子菜单信息
4. 配置子菜单专属属性
5. 点击"确定"保存

#### 编辑菜单

1. 右键点击菜单,选择"编辑"
2. 修改菜单信息
3. 系统自动验证数据有效性
4. 点击"确定"保存

#### 删除菜单

1. 右键点击菜单,选择"删除"
2. 系统检查是否有子菜单
3. 如有子菜单,提示用户先处理子菜单
4. 二次确认删除操作
5. 执行删除

### 业务规则

#### 层级约束

- 菜单层级不能超过3级
- 不能选择自己作为父级菜单
- 不能选择自己的子菜单作为父级菜单

#### 删除约束

- 有子菜单的菜单不能删除
- 系统菜单不能删除
- 删除前必须先删除或移动子菜单

#### 类型约束

- 目录类型不能配置路由和组件路径
- 菜单类型必须配置路由路径
- 按钮类型必须配置权限标识

#### 系统菜单约束

- 系统核心菜单不能删除
- 系统菜单不能修改父级关系
- 系统菜单可以编辑基本信息

---

## 💻 开发指南

### 开发环境准备

```bash
# 安装依赖
npm install

# 启动开发服务器
npm run dev

# 构建生产版本
npm run build
```

### 开发新功能

1. **创建组件**

```bash
# 在 src/modules/menu/components 下创建新组件
touch src/modules/menu/components/NewComponent.vue
```

2. **定义类型**

```typescript
// src/modules/menu/types/index.ts
export interface NewType {
  id: number
  name: string
}
```

3. **创建Composable**

```typescript
// src/modules/menu/composables/useNewFeature.ts
import { ref } from 'vue'

export function useNewFeature() {
  const data = ref()

  // 业务逻辑

  return { data }
}
```

4. **添加API**

```typescript
// src/modules/menu/api/index.ts
export function newApi() {
  return request({
    url: '/menus/new-feature',
    method: 'get'
  })
}
```

### 单元测试

```typescript
// tests/unit/menu.spec.ts
import { describe, it, expect } from 'vitest'
import { buildTree } from '@/modules/menu/composables/useMenuTree'

describe('MenuTree', () => {
  it('should build tree structure', () => {
    const items = [
      { id: 1, parentId: 0, name: 'Parent' },
      { id: 2, parentId: 1, name: 'Child' }
    ]

    const tree = buildTree(items)

    expect(tree).toHaveLength(1)
    expect(tree[0].children).toHaveLength(1)
  })
})
```

### 性能优化建议

1. **菜单数据缓存**
   - 使用Pinia缓存菜单列表
   - 实现本地存储缓存 (30分钟)
   - 避免频繁请求后端

2. **树形结构优化**
   - 使用懒加载子菜单
   - 虚拟滚动处理大量数据
   - 懒加载图标组件

3. **权限缓存**
   - 缓存权限检查结果
   - 使用计算属性避免重复计算

4. **路由优化**
   - 动态导入组件
   - 路由懒加载
   - keepAlive缓存

---

## 🔗 集成说明

### 与权限模块集成

```typescript
// 菜单权限定义
export const MENU_PERMISSIONS = {
  VIEW: 'menu:view',
  VIEW_ALL: 'menu:view_all',
  CREATE: 'menu:create',
  EDIT: 'menu:edit',
  DELETE: 'menu:delete',
  SORT: 'menu:sort',
  ENABLE: 'menu:enable',
  EXPORT: 'menu:export'
}

// 权限检查
const canEdit = authStore.hasPermission(MENU_PERMISSIONS.EDIT)
```

### 与数据字典集成

```typescript
// 字典类型定义
export const MENU_DICT_TYPES = {
  MENU_TYPE: 'menu_type',        // 菜单类型
  MENU_STATUS: 'menu_status',    // 菜单状态
  MENU_TARGET: 'menu_target'     // 链接打开方式
}

// 使用字典
const menuTypeOptions = dictStore.getDictItems(MENU_DICT_TYPES.MENU_TYPE)
```

### 与路由模块集成

```typescript
// 菜单转路由
const routes = buildRouteTree(menuList)

// 动态加载
routes.forEach(route => {
  router.addRoute(route)
})
```

---

## ❓ 常见问题

### Q1: 如何自定义图标?

A: 目前支持Element Plus内置图标。P2版本将支持自定义SVG图标上传。

### Q2: 菜单层级为什么限制为3级?

A: 考虑到UI展示效果和用户体验,超过3级的菜单结构会过于复杂,不利于操作。

### Q3: 如何修改菜单的排序?

A: 通过修改"排序号"字段,数字越小越靠前。P2版本将支持拖拽排序。

### Q4: 删除菜单时提示"有子菜单"怎么办?

A: 需要先删除或移动子菜单后,才能删除父菜单。

### Q5: 系统菜单可以编辑吗?

A: 系统菜单可以编辑基本信息(名称、图标等),但不能删除和修改父级关系。

### Q6: 如何配置菜单缓存?

A: 在菜单表单中勾选"缓存"选项,系统会自动配置keepAlive。

### Q7: 外链菜单如何配置?

A:
1. 选择"外链"类型
2. 填写外链URL
3. 选择打开方式(当前页/新页)

### Q8: 菜单权限不生效怎么办?

A:
1. 检查权限标识是否正确配置
2. 确认用户角色是否具有该权限
3. 清除缓存重新登录

---

## 📈 版本规划

### v1.0.0 (当前版本)

**P0 - 核心功能**:
- ✅ 菜单CRUD操作
- ✅ 树形结构展示
- ✅ 菜单层级管理(最多3级)
- ✅ 基础验证规则
- ✅ 图标选择器
- ✅ 菜单权限关联
- ✅ 动态路由注册
- ✅ 菜单启用/禁用

**P1 - 重要功能**:
- ✅ 数据字典集成
- ✅ 权限管理集成
- ✅ 系统菜单保护
- ✅ 字段级权限控制
- ⭐ **菜单初始化功能**
- ⭐ **开发环境权限优化**
- ⭐ **Mock数据支持**
- ⭐ **本地存储持久化**

### v1.1.0 (规划中)

**P2 - 增强功能**:
- ⏳ 自定义图标上传
- ⏳ 菜单拖拽排序
- ⏳ 菜单导入导出
- ⏳ 操作记录审计
- ⏳ 批量操作

### v1.2.0 (未来版本)

**P3 - 高级功能**:
- 📅 菜单版本管理
- 📅 多语言支持
- 📅 主题定制
- 📅 菜单模板

---

## 📞 联系方式

**开发团队**: AI开发助手
**创建日期**: 2026-01-10
**文档版本**: v1.0.0

如有问题或建议,请联系开发团队或提交Issue。

---

**最后更新**: 2026-01-10
