# 权限控制模块

基于 RBAC 模型的权限管理系统，提供角色管理、权限管理、用户角色分配等功能。

## 📁 目录结构

```
permission/
├── api/
│   └── index.ts              # API 接口定义
├── components/
│   ├── RoleForm.vue          # 角色表单组件
│   ├── PermissionForm.vue    # 权限表单组件
│   ├── PermissionConfigDialog.vue   # 权限配置对话框
│   └── RoleMembersDialog.vue # 角色成员对话框
├── views/
│   ├── RoleList.vue          # 角色管理页面
│   └── PermissionList.vue    # 权限管理页面
├── store/
│   └── index.ts              # Pinia Store
├── types/
│   └── index.ts              # TypeScript 类型定义
├── utils/
│   └── permission.ts         # 权限工具函数
├── directives/
│   └── auth.ts               # 权限指令
└── README.md                 # 本文件
```

## 🚀 功能特性

- ✅ **RBAC 权限模型** - 用户-角色-权限三层架构
- ✅ **四种权限类型** - 菜单、按钮、接口、数据权限
- ✅ **权限树形管理** - 可视化权限树展示和配置
- ✅ **角色权限配置** - 灵活的角色权限分配
- ✅ **权限指令** - v-auth、v-role、v-super-admin 指令
- ✅ **路由权限验证** - 自动路由守卫和权限验证
- ✅ **权限缓存** - 提升性能的权限缓存机制
- ✅ **Mock 数据** - 开发环境 Mock 数据支持

## 📦 安装使用

### 1. 在 main.ts 中注册权限指令

```typescript
import { createApp } from 'vue'
import App from './App.vue'
import { setupAuthDirective } from '@/modules/permission/directives/auth'

const app = createApp(App)

// 注册权限指令
setupAuthDirective(app)

app.mount('#app')
```

### 2. 在路由中配置权限守卫

```typescript
// router/index.ts
import { createRouter } from 'vue-router'
import { setupPermissionGuard } from '@/router/permission'

const router = createRouter({ ... })

// 设置路由权限守卫
setupPermissionGuard(router)

export default router
```

### 3. 添加权限路由

```typescript
// router/index.ts
import permissionRoutes from '@/router/permission.routes'

const router = createRouter({
  routes: [
    ...permissionRoutes,
    // 其他路由...
  ]
})
```

## 💡 使用示例

### 在组件中使用权限

```vue
<template>
  <div>
    <!-- 使用 v-auth 指令控制按钮显示 -->
    <el-button v-auth="'system:user:add'" @click="handleAdd">
      添加用户
    </el-button>

    <!-- 使用 v-role 指令 -->
    <el-button v-role="'admin'" @click="handleAdminAction">
      管理员操作
    </el-button>

    <!-- 使用 v-super-admin 指令 -->
    <el-button v-super-admin @click="handleSuperAdminAction">
      超级管理员操作
    </el-button>
  </div>
</template>

<script setup lang="ts">
import { usePermission } from '@/modules/permission/store'

const { hasPermission, hasRole, isSuperAdmin } = usePermission()

// 在代码中检查权限
function checkPermission() {
  if (hasPermission('system:user:add')) {
    console.log('有添加用户权限')
  }

  if (hasRole('admin')) {
    console.log('是管理员角色')
  }

  if (isSuperAdmin) {
    console.log('是超级管理员')
  }
}
</script>
```

### 在路由配置中设置权限

```typescript
{
  path: '/system/user',
  name: 'UserList',
  component: () => import('@/views/system/UserList.vue'),
  meta: {
    title: '用户管理',
    permission: 'system:user:list' // 需要的权限码
  }
}
```

### 使用权限工具函数

```typescript
import { hasPermission, hasAnyPermission, hasRole } from '@/modules/permission/utils/permission'
import { usePermissionStore } from '@/modules/permission/store'

const permissionStore = usePermissionStore()

// 检查单个权限
if (hasPermission(permissionStore.userPermissions, 'system:user:add')) {
  // 有权限
}

// 检查多个权限(任意一个)
if (hasAnyPermission(permissionStore.userPermissions, ['system:user:add', 'system:user:edit'])) {
  // 有任意一个权限
}

// 检查角色
if (hasRole(permissionStore.userPermissions, 'admin')) {
  // 是管理员角色
}
```

## 🔑 权限编码规范

权限编码格式：`模块:操作:对象`

示例：
- `system:user:list` - 系统模块-用户列表
- `system:user:add` - 系统模块-添加用户
- `employee:edit` - 员工模块-编辑员工
- `department:delete` - 部门模块-删除部门

## 📋 权限类型说明

### 1. 菜单权限 (menu)
控制页面菜单和路由访问，配置路由路径、组件路径、图标等。

### 2. 按钮权限 (button)
控制页面内按钮的显示和操作，如添加、编辑、删除等。

### 3. 接口权限 (api)
控制后端 API 的访问，配置接口路径和请求方法。

### 4. 数据权限 (data)
控制数据的访问范围：
- `all` - 全部数据
- `dept` - 本部门数据
- `dept_and_sub` - 本部门及下级部门数据
- `self` - 仅本人数据

## 🎨 核心组件说明

### RoleList.vue
角色管理页面，提供角色的增删改查、权限配置、成员管理等功能。

### PermissionList.vue
权限管理页面，提供权限的树形展示、增删改查、按类型筛选等功能。

### PermissionConfigDialog.vue
权限配置对话框，通过树形控件为角色分配权限。

### RoleForm.vue
角色表单组件，用于创建和编辑角色。

### PermissionForm.vue
权限表单组件，用于创建和编辑权限，根据权限类型显示不同字段。

## 🔄 数据流

1. **加载权限** -> 用户登录后调用 `loadUserPermissions()` 加载用户权限
2. **权限验证** -> 路由守卫、权限指令、权限函数验证权限
3. **权限缓存** -> 权限数据缓存到 sessionStorage 和 Pinia Store
4. **权限刷新** -> 权限变更后调用 `refreshPermissions()` 刷新缓存

## 🧪 开发模式

开发模式下使用 Mock 数据，无需后端接口即可测试权限功能：

```typescript
// 在 api/index.ts 中
if (import.meta.env.DEV) {
  const mockData = getMockUserPermissions()
  // 使用 Mock 数据
}
```

## 🔧 配置说明

### 权限缓存配置

默认缓存时间：30 分钟

可以在 `utils/permission.ts` 中修改：

```typescript
class PermissionCache {
  private defaultTTL = 30 * 60 * 1000 // 修改缓存时间
}
```

### 白名单配置

在 `router/permission.ts` 中配置无需权限的路由：

```typescript
const whiteList = ['/login', '/404', '/403']
```

## 📚 API 接口

所有 API 接口定义在 `api/index.ts` 中，包括：

- 角色管理：增删改查、权限配置、成员管理
- 权限管理：增删改查、权限树、模块列表
- 用户角色：分配角色、移除角色
- 用户权限：获取用户所有权限

## 🛠️ 常见问题

### Q: 如何添加新的权限类型？
A: 在 `types/index.ts` 中修改 `PermissionType` 枚举，并更新相关组件。

### Q: 如何自定义权限验证逻辑？
A: 修改 `utils/permission.ts` 中的权限验证函数，或创建自定义指令。

### Q: 如何处理权限变更？
A: 权限变更后调用 `permissionStore.refreshPermissions()` 清除缓存并重新加载。

### Q: 如何批量配置角色权限？
A: 使用权限配置对话框的树形控件，支持父子节点联动选择。

## 📝 更新日志

### v1.0.0 (2026-01-10)
- ✨ 初始版本发布
- ✨ 实现 RBAC 权限模型
- ✨ 实现权限指令和路由守卫
- ✨ 实现角色和权限管理页面
- ✨ 实现 Mock 数据支持

## 📄 License

MIT
