# 权限控制页面访问指南

## 🚀 进入权限控制页面的方法

### 方法一：通过 URL 直接访问（推荐用于开发测试）

在开发环境中，由于使用了 Mock 数据和超级管理员权限，你可以直接访问以下 URL：

#### 1. 角色管理页面
```
http://localhost:5173/permission/role
```

#### 2. 权限管理页面
```
http://localhost:5173/permission/permission
```

### 方法二：通过侧边栏菜单访问（生产环境）

需要在系统主菜单中添加权限管理菜单项。步骤如下：

#### 1. 确保路由已注册

在 `src/router/index.ts` 中注册权限路由：

```typescript
import { createRouter, createWebHistory } from 'vue-router'
import permissionRoutes from '@/router/permission.routes'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      redirect: '/dashboard'
    },
    // 权限管理路由
    ...permissionRoutes,
    // 其他路由...
  ]
})

export default router
```

#### 2. 在菜单配置中添加权限管理项

找到菜单配置文件（通常在 `src/config/menu.ts` 或类似位置）：

```typescript
// src/config/menu.ts
export const menuConfig = [
  {
    path: '/dashboard',
    title: '首页',
    icon: 'Dashboard'
  },
  {
    path: '/system',
    title: '系统管理',
    icon: 'Setting',
    children: [
      {
        path: '/system/user',
        title: '用户管理',
        icon: 'User'
      },
      // 添加权限管理菜单
      {
        path: '/permission/role',
        title: '角色管理',
        icon: 'UserFilled',
        permission: 'system:role:list' // 需要的权限
      },
      {
        path: '/permission/permission',
        title: '权限管理',
        icon: 'Key',
        permission: 'system:permission:list' // 需要的权限
      }
    ]
  }
]
```

### 方法三：临时跳过权限验证（仅用于开发）

如果你想临时跳过权限验证进行测试，可以修改路由守卫：

```typescript
// src/router/permission.ts
export function setupPermissionGuard(router: Router) {
  router.beforeEach(async (to, from, next) => {
    // 开发模式：临时跳过权限验证
    if (import.meta.env.DEV) {
      next()
      return
    }

    // 生产环境：正常权限验证...
  })
}
```

## 🔑 开发环境快速访问

### 1. 启动开发服务器

```bash
cd oa-system-frontend
npm run dev
```

### 2. 访问权限页面

开发服务器启动后，直接在浏览器中访问：

- **角色管理**: http://localhost:5173/permission/role
- **权限管理**: http://localhost:5173/permission/permission

### 3. Mock 数据说明

开发环境下，系统会自动使用 Mock 数据：
- **Mock 用户**: 超级管理员 (admin)
- **Mock 角色**: 3个角色（超级管理员、部门管理员、普通员工）
- **Mock 权限**: 完整的权限树（系统管理、员工管理、部门管理等）

## 📋 页面功能说明

### 角色管理页面 (/permission/role)

**功能列表**:
- ✅ 查看角色列表
- ✅ 创建新角色
- ✅ 编辑角色信息
- ✅ 复制角色
- ✅ 删除角色（系统角色和有成员的角色不可删除）
- ✅ 配置角色权限
- ✅ 查看角色成员
- ✅ 搜索和筛选角色

**统计信息**:
- 总角色数
- 系统角色数
- 自定义角色数
- 启用角色数

### 权限管理页面 (/permission/permission)

**功能列表**:
- ✅ 树形展示所有权限
- ✅ 按类型筛选（菜单/按钮/接口/数据权限）
- ✅ 创建新权限
- ✅ 编辑权限信息
- ✅ 删除权限
- ✅ 搜索权限
- ✅ 展开/收起权限树

**Tab 分类**:
- 菜单权限：控制页面和路由访问
- 按钮权限：控制页面内按钮操作
- 接口权限：控制后端 API 访问
- 数据权限：控制数据访问范围
- 全部权限：查看所有权限

## 🛠️ 常见问题

### Q1: 访问页面时提示 403 或无权限？

**原因**: 路由守卫拦截，用户没有相应权限

**解决方案**:

1. **开发环境**: 确保使用 Mock 数据登录
```typescript
// 在浏览器控制台执行
localStorage.setItem('userId', 'USER001')
// 刷新页面
location.reload()
```

2. **生产环境**: 给当前用户分配相应角色和权限
```typescript
// 使用超级管理员账号登录
// 或调用 API 分配权限角色
```

### Q2: 页面显示空白或报错？

**检查清单**:

1. ✅ 确认所有依赖已安装
```bash
npm install element-plus @element-plus/icons-vue pinia
```

2. ✅ 确认路由已正确注册
```typescript
// router/index.ts 中应该包含:
import permissionRoutes from '@/router/permission.routes'
routes: [...permissionRoutes, ...]
```

3. ✅ 确认 Store 已注册
```typescript
// main.ts 中应该包含:
import { createPinia } from 'pinia'
const pinia = createPinia()
app.use(pinia)
```

4. ✅ 确认权限指令已注册
```typescript
// main.ts 中应该包含:
import { setupAuthDirective } from '@/modules/permission/directives/auth'
setupAuthDirective(app)
```

### Q3: 权限树不显示或数据为空？

**解决方案**:

1. 检查 Mock 数据加载
```typescript
// 确保在开发环境
console.log('Is DEV:', import.meta.env.DEV)

// 手动加载权限
import { usePermissionStore } from '@/modules/permission/store'
const permissionStore = usePermissionStore()
await permissionStore.loadAllPermissions()
```

2. 清除缓存重试
```typescript
localStorage.clear()
sessionStorage.clear()
location.reload()
```

### Q4: 如何快速测试权限功能？

**快速测试步骤**:

1. 启动开发服务器
```bash
npm run dev
```

2. 在浏览器中访问任一权限页面
```
http://localhost:5173/permission/role
```

3. 打开浏览器控制台，设置 Mock 用户
```javascript
localStorage.setItem('userId', 'USER001')
location.reload()
```

4. 测试各项功能：
   - 点击"新增角色"按钮
   - 点击"配置权限"查看权限树
   - 点击"成员"查看角色成员
   - 使用搜索和筛选功能

## 📝 下一步操作

### 1. 集成到主应用

将权限模块集成到主应用的导航菜单中：

```vue
<!-- Sidebar.vue 或类似组件 -->
<template>
  <el-menu>
    <el-menu-item index="/dashboard">
      <el-icon><Dashboard /></el-icon>
      <span>首页</span>
    </el-menu-item>

    <!-- 系统管理 -->
    <el-sub-menu index="system">
      <template #title>
        <el-icon><Setting /></el-icon>
        <span>系统管理</span>
      </template>
      <el-menu-item index="/permission/role">
        <el-icon><UserFilled /></el-icon>
        <span>角色管理</span>
      </el-menu-item>
      <el-menu-item index="/permission/permission">
        <el-icon><Key /></el-icon>
        <span>权限管理</span>
      </el-menu-item>
    </el-sub-menu>
  </el-menu>
</template>
```

### 2. 配置生产环境权限

生产环境需要连接真实的后端 API：

```typescript
// src/modules/permission/api/index.ts
// 根据环境变量切换 Mock 数据和真实 API
export function getRoleList(params?: RoleFilter) {
  if (import.meta.env.PROD) {
    // 生产环境：调用真实 API
    return request.get('/api/roles', { params })
  } else {
    // 开发环境：使用 Mock 数据
    const mockData = getMockRoleList()
    return Promise.resolve({ data: mockData })
  }
}
```

### 3. 实现权限菜单动态生成

根据用户权限动态生成菜单：

```typescript
// 根据用户权限过滤菜单
import { usePermissionStore } from '@/modules/permission/store'

const permissionStore = usePermissionStore()

const filteredMenus = menuConfig.filter(menu => {
  if (!menu.permission) return true
  return permissionStore.checkPermission(menu.permission)
})
```

## 🎉 总结

现在你可以通过以下方式访问权限控制页面：

1. **开发测试**: 直接访问 URL
   - http://localhost:5173/permission/role
   - http://localhost:5173/permission/permission

2. **生产环境**: 通过菜单导航访问
   - 首页 -> 系统管理 -> 角色管理
   - 首页 -> 系统管理 -> 权限管理

3. **快速设置**: 使用 Mock 用户
   ```javascript
   localStorage.setItem('userId', 'USER001')
   ```

祝使用愉快！🚀
