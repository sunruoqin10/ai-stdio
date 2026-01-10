# 🚀 权限控制模块 - 快速启动指南

## ✅ 已完成的配置

路由已经集成完毕！现在你可以直接访问权限控制页面。

## 🎯 访问方式

### 方式一：直接访问 URL（最简单）

启动开发服务器后，直接在浏览器地址栏输入：

```
http://localhost:5173/permission/role      # 角色管理
http://localhost:5173/permission/permission  # 权限管理
```

### 方式二：从员工页面跳转

在任何页面中，可以使用以下代码跳转：

```vue
<template>
  <!-- 使用 router-link 跳转 -->
  <router-link to="/permission/role">角色管理</router-link>

  <!-- 或使用编程式导航 -->
  <el-button @click="goToRole">跳转到角色管理</el-button>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

function goToRole() {
  router.push('/permission/role')
}
</script>
```

## 🛠️ 启动步骤

### 1. 启动开发服务器

```bash
cd oa-system-frontend
npm run dev
```

### 2. 访问权限页面

开发服务器启动后，终端会显示本地地址：

```
VITE v5.x.x  ready in xxx ms

➜  Local:   http://localhost:5173/
➜  Network: use --host to expose
```

直接在浏览器访问：
- **角色管理**: http://localhost:5173/permission/role
- **权限管理**: http://localhost:5173/permission/permission

## 📊 页面预览

### 角色管理页面 (/permission/role)

功能包括：
- ✅ 角色列表展示（表格）
- ✅ 统计卡片（总角色数、系统角色、自定义角色、启用角色）
- ✅ 新增角色
- ✅ 编辑角色
- ✅ 复制角色
- ✅ 删除角色
- ✅ 配置权限（权限树对话框）
- ✅ 查看成员
- ✅ 搜索和筛选

### 权限管理页面 (/permission/permission)

功能包括：
- ✅ 权限树展示
- ✅ Tab 切换（菜单/按钮/接口/数据/全部）
- ✅ 统计卡片（总权限数、各类型权限数量）
- ✅ 新增权限
- ✅ 编辑权限
- ✅ 删除权限
- ✅ 搜索和筛选
- ✅ 展开/收起权限树

## 🧪 Mock 数据

开发环境下，系统会自动使用 Mock 数据：

### Mock 用户
- **用户ID**: USER001
- **角色**: 超级管理员 (admin)
- **权限**: 所有权限

### Mock 角色 (3个)
1. **超级管理员** (ROLE0001)
   - 编码: admin
   - 类型: 系统角色
   - 描述: 拥有系统所有权限
   - 成员数: 2

2. **部门管理员** (ROLE0002)
   - 编码: dept_admin
   - 类型: 自定义角色
   - 描述: 管理本部门相关业务
   - 成员数: 5

3. **普通员工** (ROLE0003)
   - 编码: employee
   - 类型: 自定义角色
   - 描述: 普通员工权限
   - 成员数: 20

### Mock 权限树
包含以下模块的完整权限树：
- **系统管理**: 用户管理、角色管理等
- **员工管理**: 员工列表、添加、编辑、删除等
- **部门管理**: 部门管理等

## 🎨 组件使用示例

### 在其他页面中引用权限组件

```vue
<template>
  <div>
    <!-- 使用角色表单组件 -->
    <role-form
      v-model="roleFormVisible"
      :role-data="currentRole"
      @success="handleSuccess"
    />

    <!-- 使用权限配置对话框 -->
    <permission-config-dialog
      v-model="permissionDialogVisible"
      :role-id="roleId"
      :role-name="roleName"
      @success="handleSuccess"
    />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import RoleForm from '@/modules/permission/components/RoleForm.vue'
import PermissionConfigDialog from '@/modules/permission/components/PermissionConfigDialog.vue'

const roleFormVisible = ref(false)
const permissionDialogVisible = ref(false)
const currentRole = ref()
const roleId = ref('')
const roleName = ref('')

function handleSuccess() {
  console.log('操作成功')
}
</script>
```

### 使用权限指令

```vue
<template>
  <div>
    <!-- 权限指令：只有拥有指定权限的用户才能看到按钮 -->
    <el-button v-auth="'system:user:add'" @click="addUser">
      添加用户
    </el-button>

    <!-- 角色指令：只有指定角色的用户才能看到 -->
    <el-button v-role="'admin'" @click="adminAction">
      管理员操作
    </el-button>

    <!-- 超级管理员指令 -->
    <el-button v-super-admin @click="superAdminAction">
      超级管理员操作
    </el-button>
  </div>
</template>
```

### 使用权限验证函数

```vue
<script setup>
import { usePermission } from '@/modules/permission/store'

const { hasPermission, hasRole, isSuperAdmin } = usePermission()

// 检查权限
if (hasPermission('system:user:add')) {
  console.log('有添加用户权限')
}

// 检查角色
if (hasRole('admin')) {
  console.log('是管理员')
}

// 检查是否是超级管理员
if (isSuperAdmin) {
  console.log('是超级管理员')
}
</script>
```

## 📝 注意事项

### 1. 权限守卫默认关闭

当前路由配置中，权限守卫已被注释掉，方便开发测试：

```typescript
// src/router/index.ts
// 设置路由权限守卫（开发环境下可以注释掉以简化测试）
// setupPermissionGuard(router)
```

**生产环境部署时**，请取消注释以启用权限验证：

```typescript
// 启用权限守卫
setupPermissionGuard(router)
```

### 2. 布局组件

当前路由使用独立页面，不需要父级布局组件。

如果需要使用统一的布局（如侧边栏、顶部导航等），可以：

1. 创建 `src/layouts/LayoutMain.vue` 布局组件
2. 修改 `src/router/permission.routes.ts`，启用嵌套路由配置

### 3. 样式依赖

确保项目中已安装以下依赖：

```bash
npm install element-plus @element-plus/icons-vue
```

## 🔍 常见问题

### Q: 页面显示空白？

**A**: 检查浏览器控制台是否有错误信息：
1. 确认所有依赖已安装
2. 确认路由配置正确
3. 清除浏览器缓存重试

### Q: 组件导入报错？

**A**: 确保 TypeScript 配置正确，检查 `tsconfig.json` 中的路径别名配置：

```json
{
  "compilerOptions": {
    "paths": {
      "@/*": ["./src/*"]
    }
  }
}
```

### Q: Mock 数据不显示？

**A**: 确保在开发环境下运行：
```bash
NODE_ENV=development npm run dev
```

## 📚 更多文档

- [详细访问指南](./docs/permission-access-guide.md)
- [权限模块 README](./src/modules/permission/README.md)
- [规范文档](./specs/core/permission/)

## 🎉 开始使用

现在你可以直接访问权限控制页面了！

1. 启动开发服务器：`npm run dev`
2. 访问：http://localhost:5173/permission/role
3. 开始体验权限管理功能！

祝使用愉快！🚀
