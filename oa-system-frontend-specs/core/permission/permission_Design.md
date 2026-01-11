# 权限控制模块 - UI/UX设计规范

> **文档类型**: UI/UX设计规范
> **设计风格**: Element Plus
> **核心组件**: el-tree, el-transfer, el-table
> **响应式设计**: 支持
> **创建日期**: 2026-01-10
> **最后更新**: 2026-01-10

---

## 📋 目录

- [1. 页面布局](#1-页面布局)
- [2. 组件选择](#2-组件选择)
- [3. 交互规范](#3-交互规范)
- [4. 样式规范](#4-样式规范)
- [5. 响应式设计](#5-响应式设计)
- [6. 可访问性](#6-可访问性)

---

## 1. 页面布局

### 1.1 角色管理页布局

```
┌─────────────────────────────────────────────────┐
│  PageHeader: 角色管理    [新增角色] [从模板创建]   │
├─────────────────────────────────────────────────┤
│  Tab: [角色列表] [权限配置] [成员管理]             │
├───────────┬─────────────────────────────────────┤
│           │                                      │
│ Filter    │  角色表格                             │
│ Panel     │  - 角色名称                          │
│           │  - 角色编码                          │
│           │  - 角色类型                          │
│           │  - 成员数量                          │
│           │  - 状态                              │
│           │  - 操作                              │
│           │                                      │
├───────────┴─────────────────────────────────────┤
│  统计卡片: 总角色数 | 系统角色 | 自定义角色 | 启用 │
└─────────────────────────────────────────────────┘
```

**统计卡片详细说明**:
```
┌─────────────┬─────────────┬─────────────┬─────────────┐
│ 总角色数     │ 系统角色     │ 自定义角色   │ 启用角色    │
│ 👤 15       │ 🔒 3        │ 🔓 12       │ ✅ 13       │
│             │             │             │             │
└─────────────┴─────────────┴─────────────┴─────────────┘
```

**布局说明**:
- **顶部**: 页面标题 + 主要操作按钮
- **导航**: Tab切换不同管理视图
- **左侧**: 筛选面板(可折叠)
- **中间**: 主内容区(表格)
- **底部**: 统计信息卡片

### 1.2 权限管理页布局

```
┌─────────────────────────────────────────────────┐
│  PageHeader: 权限管理    [新增权限] [刷新]        │
├─────────────────────────────────────────────────┤
│  Tab: [菜单权限] [按钮权限] [接口权限] [数据权限]  │
├───────────┬─────────────────────────────────────┤
│           │                                      │
│ Filter    │  权限树 (el-tree)                    │
│ Panel     │  📁 系统管理                          │
│           │    📄 用户管理                        │
│           │    📄 角色管理                        │
│           │  📁 员工管理                          │
│           │    📄 员工列表                        │
│           │    🔘 添加员工                        │
│           │    🔘 编辑员工                        │
│           │                                      │
├───────────┴─────────────────────────────────────┤
│  统计卡片: 总权限 | 菜单权限 | 按钮权限 | 接口权限 │
└─────────────────────────────────────────────────┘
```

**统计卡片详细说明**:
```
┌─────────────┬─────────────┬─────────────┬─────────────┐
│ 总权限数     │ 菜单权限     │ 按钮权限     │ 接口权限    │
│ 📋 128      │ 📁 32       │ 🔘 64       │ 🔗 32       │
│             │             │             │             │
└─────────────┴─────────────┴─────────────┴─────────────┘
```

### 1.3 角色权限配置对话框布局

```
┌──────────────────────────────────────────────────┐
│  配置角色权限: 系统管理员                [X]      │
├──────────────────────────────────────────────────┤
│  左侧                           │  右侧          │
│  ┌────────────────────┐       │  ┌────────────┐ │
│  │ 可用权限树          │       │  │ 已选权限    │ │
│  │                    │       │  │            │ │
│  │ ☑ 系统管理          │  ───→ │  │ 系统管理    │ │
│  │   ☑ 用户管理        │       │  │ 用户管理    │ │
│  │   ☑ 角色管理        │       │  │ 角色管理    │ │
│  │ ☑ 员工管理          │       │  │ 员工管理    │ │
│  │   ☑ 员工列表        │       │  │ 员工列表    │ │
│  │   ☐ 添加员工        │       │  │            │ │
│  │   ☐ 编辑员工        │       │  │            │ │
│  │                    │       │  │            │ │
│  └────────────────────┘       │  └────────────┘ │
│                                │                 │
├──────────────────────────────────────────────────┤
│  搜索权限: [_____________]  展开/收起  全选/全不选 │
├──────────────────────────────────────────────────┤
│           [取消] [保存配置]                       │
└──────────────────────────────────────────────────┘
```

### 1.4 用户角色分配对话框布局

```
┌──────────────────────────────────────────────────┐
│  分配角色: 张三                          [X]      │
├──────────────────────────────────────────────────┤
│  可选角色                          │  已选角色    │
│  ┌────────────────────┐       │  ┌────────────┐ │
│  │ □ 系统管理员        │  ───→ │  │ 部门管理员  │ │
│  │ ☑ 部门管理员        │       │  │ 普通员工    │ │
│  │ ☑ 普通员工          │       │  │            │ │
│  │ □ 财务管理员        │       │  │            │ │
│  │ □ 人事专员          │       │  │            │ │
│  │                    │       │  │            │ │
│  └────────────────────┘       │  └────────────┘ │
│                                                 │
│  有效期: ☐ 启用  开始日期 [____] 结束日期 [____] │
├──────────────────────────────────────────────────┤
│           [取消] [确定]                          │
└──────────────────────────────────────────────────┘
```

---

## 2. 组件选择

### 2.1 核心组件清单

| 功能 | 组件 | 说明 | 文档链接 |
|------|------|------|----------|
| 角色表格 | el-table | Element Plus表格 | https://element-plus.org/zh-CN/component/table.html |
| 权限树 | el-tree | 树形控件 | https://element-plus.org/zh-CN/component/tree.html |
| 权限配置 | el-transfer | 穿梭框 | https://element-plus.org/zh-CN/component/transfer.html |
| 角色表单 | el-form | 表单组件 | https://element-plus.org/zh-CN/component/form.html |
| 角色标签 | el-tag | 标签 | https://element-plus.org/zh-CN/component/tag.html |
| 权限搜索 | el-input | 输入框 | https://element-plus.org/zh-CN/component/input.html |
| 筛选面板 | el-card | 卡片容器 | https://element-plus.org/zh-CN/component/card.html |
| 统计卡片 | el-statistic | 统计数值 | https://element-plus.org/zh-CN/component/statistic.html |
| 确认对话框 | el-message-box | 消息弹框 | https://element-plus.org/zh-CN/component/message-box.html |
| 权限图标 | el-icon | 图标组件 | https://element-plus.org/zh-CN/component/icon.html |

### 2.2 角色表格配置

```vue
<el-table
  :data="roleList"
  v-loading="loading"
  :height="tableHeight"
  stripe
  border
>
  <el-table-column prop="name" label="角色名称" min-width="150">
    <template #default="{ row }">
      <div class="role-name">
        <el-icon v-if="row.type === 'system'"><Lock /></el-icon>
        <el-icon v-else><User /></el-icon>
        <span>{{ row.name }}</span>
      </div>
    </template>
  </el-table-column>

  <el-table-column prop="code" label="角色编码" width="150">
    <template #default="{ row }">
      <el-tag type="info" size="small">{{ row.code }}</el-tag>
    </template>
  </el-table-column>

  <el-table-column prop="type" label="角色类型" width="120">
    <template #default="{ row }">
      <el-tag :type="row.type === 'system' ? 'danger' : 'primary'" size="small">
        {{ row.type === 'system' ? '系统角色' : '自定义角色' }}
      </el-tag>
    </template>
  </el-table-column>

  <el-table-column prop="userCount" label="成员数" width="100" align="center">
    <template #default="{ row }">
      <el-badge :value="row.userCount || 0" :max="999" />
    </template>
  </el-table-column>

  <el-table-column prop="sort" label="排序" width="80" align="center" />

  <el-table-column prop="status" label="状态" width="100" align="center">
    <template #default="{ row }">
      <el-tag :type="row.status === 'active' ? 'success' : 'info'">
        {{ row.status === 'active' ? '启用' : '停用' }}
      </el-tag>
    </template>
  </el-table-column>

  <el-table-column prop="createdAt" label="创建时间" width="180" />

  <el-table-column label="操作" width="280" fixed="right">
    <template #default="{ row }">
      <el-button link type="primary" @click="handleConfigPermission(row)">
        配置权限
      </el-button>
      <el-button link type="primary" @click="handleViewMembers(row)">
        成员
      </el-button>
      <el-button link type="primary" @click="handleEdit(row)">
        编辑
      </el-button>
      <el-button link type="primary" @click="handleCopy(row)">
        复制
      </el-button>
      <el-button
        link
        type="danger"
        @click="handleDelete(row)"
        :disabled="row.type === 'system' || row.userCount > 0"
      >
        删除
      </el-button>
    </template>
  </el-table-column>
</el-table>
```

### 2.3 权限树配置

```vue
<el-tree
  ref="permissionTreeRef"
  :data="permissionTree"
  :props="{
    label: 'name',
    children: 'children',
    disabled: 'disabled'
  }"
  node-key="id"
  show-checkbox
  :default-checked-keys="selectedPermissionIds"
  :filter-node-method="filterNode"
  highlight-current
  :expand-on-click-node="false"
>
  <template #default="{ node, data }">
    <div class="permission-node">
      <div class="node-left">
        <el-icon class="node-icon">
          <Folder v-if="data.type === 'menu'" />
          <Operation v-else-if="data.type === 'button'" />
          <Connection v-else-if="data.type === 'api'" />
          <Grid v-else-if="data.type === 'data'" />
        </el-icon>
        <span class="node-label">{{ data.name }}</span>
      </div>
      <div class="node-right">
        <el-tag size="small" :type="getPermissionTypeTag(data.type)">
          {{ getPermissionTypeName(data.type) }}
        </el-tag>
        <span class="node-code">{{ data.code }}</span>
      </div>
    </div>
  </template>
</el-tree>
```

### 2.4 权限配置穿梭框

```vue
<el-transfer
  v-model="selectedPermissionIds"
  :data="allPermissions"
  :props="{
    key: 'id',
    label: 'name',
    disabled: 'disabled'
  }"
  filterable
  :filter-method="filterMethod"
  filter-placeholder="搜索权限"
  :titles="['可选权限', '已选权限']"
  :button-texts="['移除', '添加']"
  :render-content="renderTransferItem"
/>
```

### 2.5 角色表单组件

```vue
<el-form
  ref="formRef"
  :model="formData"
  :rules="formRules"
  label-width="100px"
  class="role-form"
>

  <el-form-item label="角色名称" prop="name">
    <el-input
      v-model="formData.name"
      placeholder="请输入角色名称"
      maxlength="50"
      show-word-limit
      clearable
    />
  </el-form-item>

  <el-form-item label="角色编码" prop="code">
    <el-input
      v-model="formData.code"
      placeholder="请输入角色编码(字母、数字、下划线)"
      maxlength="50"
      clearable
    >
      <template #prefix>
        <el-icon><Key /></el-icon>
      </template>
    </el-input>
  </el-form-item>

  <el-form-item label="角色类型" prop="type">
    <el-radio-group v-model="formData.type">
      <el-radio label="custom">自定义角色</el-radio>
      <el-radio label="system" :disabled="!canCreateSystemRole">系统角色</el-radio>
    </el-radio-group>
  </el-form-item>

  <el-form-item label="排序号" prop="sort">
    <el-input-number
      v-model="formData.sort"
      :min="0"
      :max="999"
      placeholder="请输入排序号"
    />
  </el-form-item>

  <el-form-item label="角色描述" prop="description">
    <el-input
      v-model="formData.description"
      type="textarea"
      :rows="4"
      placeholder="请输入角色描述"
      maxlength="500"
      show-word-limit
    />
  </el-form-item>

  <el-form-item label="权限配置" prop="permissionIds" v-if="isEditMode">
    <el-button @click="openPermissionDialog">
      <el-icon><Setting /></el-icon>
      配置权限 (已选 {{ formData.permissionIds?.length || 0 }} 项)
    </el-button>
  </el-form-item>

</el-form>
```

### 2.6 权限表单组件

```vue
<el-form
  ref="formRef"
  :model="formData"
  :rules="formRules"
  label-width="120px"
  class="permission-form"
>

  <el-form-item label="权限名称" prop="name">
    <el-input
      v-model="formData.name"
      placeholder="请输入权限名称"
      maxlength="50"
      show-word-limit
      clearable
    />
  </el-form-item>

  <el-form-item label="权限编码" prop="code">
    <el-input
      v-model="formData.code"
      placeholder="如: employee:view:list"
      maxlength="100"
      clearable
    >
      <template #append>
        <el-button @click="generateCode">自动生成</el-button>
      </template>
    </el-input>
  </el-form-item>

  <el-form-item label="权限类型" prop="type">
    <el-select v-model="formData.type" placeholder="请选择权限类型">
      <el-option label="菜单权限" value="menu">
        <div class="option-item">
          <el-icon><Menu /></el-icon>
          <span>菜单权限 - 控制菜单和路由访问</span>
        </div>
      </el-option>
      <el-option label="按钮权限" value="button">
        <div class="option-item">
          <el-icon><Operation /></el-icon>
          <span>按钮权限 - 控制页面内按钮操作</span>
        </div>
      </el-option>
      <el-option label="接口权限" value="api">
        <div class="option-item">
          <el-icon><Connection /></el-icon>
          <span>接口权限 - 控制后端API访问</span>
        </div>
      </el-option>
      <el-option label="数据权限" value="data">
        <div class="option-item">
          <el-icon><Grid /></el-icon>
          <span>数据权限 - 控制数据访问范围</span>
        </div>
      </el-option>
    </el-select>
  </el-form-item>

  <el-form-item label="所属模块" prop="module">
    <el-select
      v-model="formData.module"
      placeholder="请选择所属模块"
      filterable
      allow-create
    >
      <el-option
        v-for="module in modules"
        :key="module"
        :label="module"
        :value="module"
      />
    </el-select>
  </el-form-item>

  <el-form-item label="上级权限" prop="parentId">
    <el-tree-select
      v-model="formData.parentId"
      :data="permissionTree"
      :props="{ label: 'name', value: 'id' }"
      placeholder="请选择上级权限"
      check-strictly
      clearable
      filterable
    />
  </el-form-item>

  <!-- 菜单权限特有字段 -->
  <template v-if="formData.type === 'menu'">
    <el-form-item label="路由路径" prop="path">
      <el-input
        v-model="formData.path"
        placeholder="如: /employee"
        clearable
      >
        <template #prefix>/</template>
      </el-input>
    </el-form-item>

    <el-form-item label="组件路径" prop="component">
      <el-input
        v-model="formData.component"
        placeholder="如: @/views/employee/List"
        clearable
      />
    </el-form-item>

    <el-form-item label="图标" prop="icon">
      <el-input
        v-model="formData.icon"
        placeholder="如: User"
        clearable
      >
        <template #prefix>
          <el-icon v-if="formData.icon">
            <component :is="formData.icon" />
          </el-icon>
          <el-icon v-else><Picture /></el-icon>
        </template>
      </el-input>
    </el-form-item>
  </template>

  <!-- 接口权限特有字段 -->
  <template v-if="formData.type === 'api'">
    <el-form-item label="接口地址" prop="apiPath">
      <el-input
        v-model="formData.apiPath"
        placeholder="如: /api/employees"
        clearable
      >
        <template #prepend>API</template>
      </el-input>
    </el-form-item>

    <el-form-item label="请求方式" prop="apiMethod">
      <el-select v-model="formData.apiMethod" placeholder="请选择请求方式">
        <el-option label="GET" value="GET" />
        <el-option label="POST" value="POST" />
        <el-option label="PUT" value="PUT" />
        <el-option label="DELETE" value="DELETE" />
        <el-option label="PATCH" value="PATCH" />
      </el-select>
    </el-form-item>
  </template>

  <!-- 数据权限特有字段 -->
  <template v-if="formData.type === 'data'">
    <el-form-item label="数据范围" prop="dataScope">
      <el-select v-model="formData.dataScope" placeholder="请选择数据范围">
        <el-option label="全部数据" value="all">
          <div class="option-item">
            <el-icon><Globe /></el-icon>
            <span>全部数据 - 可查看所有数据</span>
          </div>
        </el-option>
        <el-option label="本部门及下级" value="dept_and_sub">
          <div class="option-item">
            <el-icon><Office-building /></el-icon>
            <span>本部门及下级 - 可查看本部门和下级部门数据</span>
          </div>
        </el-option>
        <el-option label="本部门" value="dept">
          <div class="option-item">
            <el-icon><Office-building /></el-icon>
            <span>本部门 - 仅可查看本部门数据</span>
          </div>
        </el-option>
        <el-option label="仅本人" value="self">
          <div class="option-item">
            <el-icon><User /></el-icon>
            <span>仅本人 - 只能查看自己的数据</span>
          </div>
        </el-option>
      </el-select>
    </el-form-item>
  </template>

  <el-form-item label="排序号" prop="sort">
    <el-input-number
      v-model="formData.sort"
      :min="0"
      :max="999"
      placeholder="请输入排序号"
    />
  </el-form-item>

</el-form>
```

---

## 3. 交互规范

### 3.1 页面交互流程

#### 3.1.1 配置角色权限
```
用户操作: 点击"配置权限"按钮
交互反馈:
  - 打开权限配置对话框
  - 树形展示所有可用权限
  - 勾选该角色已有权限
  - 支持搜索权限
  - 支持展开/收起权限树
  - 点击父节点自动勾选所有子节点
  - 显示已选权限数量
  - 保存时显示加载状态
  - 成功后清除权限缓存
```

#### 3.1.2 分配用户角色
```
用户操作: 点击"分配角色"按钮
交互反馈:
  - 打开角色选择对话框
  - 左侧显示所有可用角色
  - 右侧显示已选角色
  - 支持搜索角色
  - 可设置角色有效期
  - 显示角色描述
  - 保存时显示加载状态
  - 成功后清除用户权限缓存
```

#### 3.1.3 复制角色
```
用户操作: 点击"复制"按钮
交互反馈:
  - 打开复制角色对话框
  - 自动填充原角色信息
  - 角色名称添加"(副本)"后缀
  - 角色编码自动生成新编码
  - 复制原角色的所有权限
  - 可编辑角色信息
  - 保存时显示加载状态
```

#### 3.1.4 删除角色
```
用户操作: 点击"删除"按钮
交互反馈:
  - 显示二次确认对话框
  - 提示删除影响(成员数量)
  - 系统角色禁止删除
  - 有成员的角色禁止删除
  - 确认后显示删除进度
  - 成功后从列表中移除
```

### 3.2 权限树交互

#### 3.2.1 树形控件交互
```
- 点击复选框: 勾选/取消勾选权限
- 点击父节点: 自动勾选/取消所有子节点
- 点击展开图标: 展开/收起子权限
- 悬浮节点: 高亮显示并显示详细信息
- 右键节点: 显示操作菜单(编辑、删除)
- 搜索框: 实时过滤权限节点
```

#### 3.2.2 权限树搜索
```typescript
// 搜索过滤
function filterNode(value: string, data: Permission) {
  if (!value) return true
  return data.name.includes(value) || data.code.includes(value)
}

// 监听搜索输入
watch(searchKeyword, (val) => {
  treeRef.value?.filter(val)
})
```

### 3.3 加载状态

#### 3.3.1 列表加载
```vue
<el-table v-loading="loading" element-loading-text="加载中...">
```

#### 3.3.2 权限树加载
```vue
<el-tree v-loading="treeLoading" element-loading-text="加载权限树...">
```

#### 3.3.3 按钮加载
```vue
<el-button :loading="submitting" @click="handleSubmit">
  保存配置
</el-button>
```

### 3.4 反馈提示

#### 3.4.1 成功提示
```typescript
ElMessage.success({
  message: '角色权限配置成功',
  duration: 2000,
  showClose: true
})
```

#### 3.4.2 权限冲突提示
```typescript
ElMessage.warning({
  message: '检测到权限冲突,已自动调整',
  duration: 3000,
  showClose: true
})
```

#### 3.4.3 错误提示
```typescript
ElMessage.error({
  message: '操作失败: ' + error.message,
  duration: 3000,
  showClose: true
})
```

---

## 4. 样式规范

### 4.1 颜色系统

```scss
// 主色调
$primary-color: #409EFF;        // Element Plus 主色
$success-color: #67C23A;        // 成功色
$warning-color: #E6A23C;        // 警告色
$danger-color: #F56C6C;         // 危险色
$info-color: #909399;           // 信息色

// 权限类型颜色
$permission-menu-color: #409EFF;      // 菜单权限
$permission-button-color: #67C23A;    // 按钮权限
$permission-api-color: #E6A23C;       // 接口权限
$permission-data-color: #F56C6C;      // 数据权限

// 角色类型颜色
$role-system-color: #F56C6C;          // 系统角色
$role-custom-color: #409EFF;          // 自定义角色

// 文字颜色
$text-primary: #303133;         // 主要文字
$text-regular: #606266;         // 常规文字
$text-secondary: #909399;       // 次要文字
$text-placeholder: #C0C4CC;     // 占位文字

// 边框颜色
$border-base: #DCDFE6;          // 基础边框
$border-light: #E4E7ED;         // 浅色边框

// 背景颜色
$bg-color: #F5F7FA;             // 页面背景
$fill-base: #F0F2F5;            // 基础填充
```

### 4.2 字体规范

```scss
// 字体家族
$font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto,
              'Helvetica Neue', Arial, sans-serif;

// 字体大小
$font-size-extra-large: 20px;   // 超大号
$font-size-large: 18px;         // 大号
$font-size-medium: 16px;        // 中号
$font-size-base: 14px;          // 基础字号
$font-size-small: 13px;         // 小号
$font-size-extra-small: 12px;   // 超小号
```

### 4.3 间距规范

```scss
// 基础间距单位
$spacing-base: 8px;

// 间距尺寸
$spacing-xs: $spacing-base * 0.5;    // 4px
$spacing-sm: $spacing-base * 1;      // 8px
$spacing-md: $spacing-base * 2;      // 16px
$spacing-lg: $spacing-base * 3;      // 24px
$spacing-xl: $spacing-base * 4;      // 32px
```

### 4.4 组件样式

#### 4.4.1 角色表格样式

```scss
.role-table {
  .role-name {
    display: flex;
    align-items: center;
    gap: 8px;

    .el-icon {
      font-size: 18px;
      color: $primary-color;
    }
  }

  .system-role .el-icon {
    color: $role-system-color;
  }

  .custom-role .el-icon {
    color: $role-custom-color;
  }
}
```

#### 4.4.2 权限树样式

```scss
.permission-tree {
  .permission-node {
    display: flex;
    align-items: center;
    justify-content: space-between;
    width: 100%;
    padding: 4px 0;

    .node-left {
      display: flex;
      align-items: center;
      gap: 8px;

      .node-icon {
        font-size: 16px;
        color: $info-color;
      }

      .node-label {
        font-size: $font-size-base;
        color: $text-primary;
      }
    }

    .node-right {
      display: flex;
      align-items: center;
      gap: 8px;

      .node-code {
        font-size: $font-size-small;
        color: $text-secondary;
        font-family: 'Courier New', monospace;
      }
    }
  }

  // 不同权限类型的图标颜色
  .el-tree-node__content {
    &:hover {
      background-color: $fill-base;
    }
  }
}
```

#### 4.4.3 权限配置对话框样式

```scss
.permission-config-dialog {
  .config-content {
    display: flex;
    gap: $spacing-lg;
    height: 500px;

    .permission-panel {
      flex: 1;
      border: 1px solid $border-base;
      border-radius: 4px;
      padding: $spacing-md;

      .panel-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: $spacing-md;
        padding-bottom: $spacing-sm;
        border-bottom: 1px solid $border-light;

        .panel-title {
          font-size: $font-size-medium;
          font-weight: 600;
        }
      }

      .panel-body {
        height: calc(100% - 60px);
        overflow-y: auto;
      }

      .panel-footer {
        margin-top: $spacing-md;
        padding-top: $spacing-sm;
        border-top: 1px solid $border-light;

        .selected-count {
          font-size: $font-size-small;
          color: $text-secondary;
        }
      }
    }
  }

  .config-actions {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: $spacing-lg;

    .search-box {
      flex: 1;
      margin-right: $spacing-md;
    }

    .tree-actions {
      display: flex;
      gap: $spacing-sm;
    }
  }
}
```

#### 4.4.4 角色表单样式

```scss
.role-form {
  padding: $spacing-lg;

  .el-form-item {
    margin-bottom: $spacing-lg;
  }

  .form-actions {
    display: flex;
    justify-content: flex-end;
    gap: $spacing-sm;
    margin-top: $spacing-xl;

    .permission-preview {
      margin-right: auto;
      padding: $spacing-sm $spacing-md;
      background-color: $fill-base;
      border-radius: 4px;

      .preview-text {
        font-size: $font-size-small;
        color: $text-secondary;

        .count {
          color: $primary-color;
          font-weight: 600;
        }
      }
    }
  }
}
```

#### 4.4.5 统计卡片样式

```scss
.stats-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: $spacing-md;
  margin-top: $spacing-lg;

  .stat-card {
    padding: $spacing-lg;
    background: white;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
    transition: all 0.3s ease;

    &:hover {
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
      transform: translateY(-2px);
    }

    .stat-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: $spacing-sm;

      .stat-label {
        font-size: $font-size-base;
        color: $text-secondary;
      }

      .stat-icon {
        font-size: 32px;
        color: $info-color;
        opacity: 0.5;
      }
    }

    .stat-value {
      font-size: $font-size-extra-large;
      font-weight: 600;
      color: $primary-color;
    }

    .stat-footer {
      margin-top: $spacing-sm;
      font-size: $font-size-small;
      color: $text-secondary;
    }
  }
}
```

### 4.5 动画效果

#### 4.5.1 权限树展开/收起动画

```scss
.el-tree {
  .el-tree-node__expand-icon {
    transition: transform 0.3s ease;

    &.expanded {
      transform: rotate(90deg);
    }
  }
}
```

#### 4.5.2 穿梭框动画

```scss
.el-transfer {
  .el-transfer-panel {
    transition: all 0.3s ease;

    &.is-filterable {
      .el-transfer-panel__filter {
        transition: all 0.3s ease;
      }
    }
  }

  .el-transfer__buttons {
    .el-button {
      transition: all 0.3s ease;

      &:hover {
        transform: scale(1.1);
      }
    }
  }
}
```

---

## 5. 响应式设计

### 5.1 断点定义

```scss
$breakpoint-xs: 480px;   // 超小屏
$breakpoint-sm: 768px;   // 小屏
$breakpoint-md: 992px;   // 中屏
$breakpoint-lg: 1200px;  // 大屏
$breakpoint-xl: 1920px;  // 超大屏
```

### 5.2 响应式布局

#### 5.2.1 权限配置对话框响应式

```scss
.permission-config-dialog {
  @media (max-width: $breakpoint-md) {
    .config-content {
      flex-direction: column;
      height: auto;

      .permission-panel {
        height: 300px;
      }
    }

    .config-actions {
      flex-direction: column;

      .search-box {
        width: 100%;
        margin-right: 0;
        margin-bottom: $spacing-sm;
      }

      .tree-actions {
        width: 100%;
        justify-content: space-between;
      }
    }
  }
}
```

#### 5.2.2 角色表格响应式

```scss
.role-table {
  @media (max-width: $breakpoint-sm) {
    font-size: $font-size-small;

    .el-table__cell {
      padding: $spacing-xs;
    }

    // 隐藏次要列
    .hide-on-mobile {
      display: none;
    }
  }
}
```

### 5.3 移动端适配

#### 5.3.1 触摸优化

```scss
// 增大点击区域
@media (max-width: $breakpoint-sm) {
  .el-button {
    min-height: 44px;
    padding: 10px 16px;
  }

  .el-tree-node__content {
    min-height: 44px;
  }
}
```

---

## 6. 可访问性

### 6.1 键盘导航

```typescript
// 支持键盘快捷键
onMounted(() => {
  document.addEventListener('keydown', handleKeyboard)
})

function handleKeyboard(e: KeyboardEvent) {
  if (e.ctrlKey && e.key === 'n') {
    e.preventDefault()
    handleAddRole()
  }

  if (e.key === 'Escape') {
    handleCloseDialog()
  }
}
```

### 6.2 ARIA标签

```vue
<el-table
  aria-label="角色列表"
  :aria-busy="loading"
>
  <el-table-column
    prop="name"
    aria-label="角色名称"
  />
</el-table>

<el-button
  aria-label="创建新角色"
  @click="handleAdd"
>
  新增角色
</el-button>
```

### 6.3 颜色对比度

```scss
// 确保文字和背景有足够的对比度
.high-contrast {
  color: #000000;
  background: #FFFFFF;
}
```

### 6.4 焦点管理

```scss
*:focus-visible {
  outline: 2px solid $primary-color;
  outline-offset: 2px;
}
```

---

## 7. 性能优化

### 7.1 虚拟滚动

```vue
<!-- 大量权限时使用虚拟滚动 -->
<el-tree
  virtual
  :data="permissionTree"
  :height="600"
>
```

### 7.2 防抖与节流

```typescript
import { debounce } from 'lodash-es'

// 搜索防抖
const searchPermissions = debounce((keyword: string) => {
  filterPermissions(keyword)
}, 300)
```

---

**文档版本**: v1.0.0
**创建人**: AI开发助手
**最后更新**: 2026-01-10
