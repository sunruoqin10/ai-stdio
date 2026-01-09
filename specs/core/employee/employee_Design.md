# 员工管理UI/UX设计规范

> **规范类型**: 第三层 - UI/UX设计规范
> **模块**: 员工管理
> **版本**: v1.0.0
> **创建日期**: 2026-01-09

---

## 📋 目录

- [1. 组件选择](#1-组件选择)
- [2. 页面布局](#2-页面布局)
- [3. 交互规范](#3-交互规范)
- [4. 样式规范](#4-样式规范)

---

## 1. 组件选择

### 1.1 核心组件清单

| 功能 | 组件 | 说明 |
|------|------|------|
| 数据展示 | el-table | 表格组件 |
| 卡片展示 | el-card | 卡片组件 |
| 数据录入 | el-form | 表单组件 |
| 步骤指示 | el-steps | 步骤条 |
| 对话框 | el-dialog | 对话框 |
| 确认操作 | el-popconfirm | 气泡确认框 |
| 状态标签 | StatusTag | 自定义组件 |
| 页面头部 | PageHeader | 自定义组件 |

### 1.2 组件使用说明

#### 1.2.1 el-table - 表格组件

**用途**: 员工列表数据展示

**配置要求**:
```vue
<el-table
  :data="employeeList"
  stripe
  border
  v-loading="loading"
  :default-sort="{ prop: 'joinDate', order: 'descending' }"
>
  <!-- 列配置 -->
</el-table>
```

#### 1.2.2 el-card - 卡片组件

**用途**: 员工卡片视图、信息卡片

**配置要求**:
```vue
<el-card
  :body-style="{ padding: '20px' }"
  shadow="hover"
>
  <!-- 卡片内容 -->
</el-card>
```

#### 1.2.3 el-form - 表单组件

**用途**: 员工信息录入

**配置要求**:
```vue
<el-form
  ref="formRef"
  :model="formData"
  :rules="rules"
  label-width="120px"
  label-position="right"
>
  <!-- 表单项 -->
</el-form>
```

#### 1.2.4 el-steps - 步骤条

**用途**: 新增员工表单步骤指示

**配置要求**:
```vue
<el-steps
  :active="currentStep"
  finish-status="success"
  align-center
>
  <el-step title="基本信息" />
  <el-step title="详细信息" />
  <el-step title="确认提交" />
</el-steps>
```

#### 1.2.5 StatusTag - 自定义状态标签

**用途**: 员工状态、试用期状态展示

**组件路径**: `@/components/common/StatusTag.vue`

**使用示例**:
```vue
<StatusTag :status="employee.status" type="employee" />
<StatusTag :status="employee.probationStatus" type="probation" />
```

#### 1.2.6 PageHeader - 自定义页面头部

**用途**: 页面标题和操作按钮

**组件路径**: `@/components/common/PageHeader.vue`

**使用示例**:
```vue
<PageHeader
  title="员工管理"
  :buttons="headerButtons"
/>
```

---

## 2. 页面布局

### 2.1 员工列表页

#### 2.1.1 布局结构

```
┌─────────────────────────────────────────────────┐
│  PageHeader: 员工管理    [新增] [导入] [导出]    │
├───────────┬───────────────────────────┬─────────┤
│           │                           │         │
│ Filter    │    Data Table             │  Stats  │
│ Panel     │    - Table View           │  Panel  │
│ (可折叠)  │    - Card View            │         │
│           │                           │         │
│ 筛选条件: │    姓名  部门  职位        │  总数   │
│ - 关键词  │    状态  操作             │  在职   │
│ - 状态    │                           │  试用期 │
│ - 部门    │                           │  新入职 │
│ - 职位    │                           │         │
│ - ...     │                           │         │
├───────────┴───────────────────────────┴─────────┤
│  Pagination: 上一页 [1] [2] [3] ... 下一页     │
└─────────────────────────────────────────────────┘
```

#### 2.1.2 布局要求

- 筛选面板可折叠
- 支持表格/卡片视图切换
- 统计面板固定在右侧
- 分页器在底部居中

#### 2.1.3 响应式设计

```typescript
// 断点配置
const breakpoints = {
  xs: '< 768px',    // 小屏幕: 单列布局
  sm: '≥ 768px',    // 平板: 筛选面板可折叠
  md: '≥ 992px',    // 中等屏幕: 标准布局
  lg: '≥ 1200px',   // 大屏幕: 统计面板固定
  xl: '≥ 1920px'    // 超大屏幕: 最大宽度限制
}
```

### 2.2 员工详情页

#### 2.2.1 布局结构

```
┌─────────────────────────────────────────────────┐
│  ← 员工详情          [编辑] [更多操作 ▼]         │
├─────────────────┬───────────────────────────────┤
│                 │  Tabs:                       │
│  Info Card      │  [基本信息] [工作信息] [操作记录] │
│  ┌───────────┐ │  ─────────────────────────────│
│  │   头像    │ │                                 │
│  │  (150px)  │ │  基本信息 Tab:                  │
│  └───────────┘ │  - 英文名: Tom                  │
│               │  - 性别: 男                     │
│  EMP0001      │  - 出生日期: 1990-01-01          │
│  张三         │  - 手机: 138****8000             │
│  软件工程师   │  - 邮箱: zhangsan@company.com    │
│               │  - 办公位置: 3楼A区              │
│  [技术部]     │  - 紧急联系人: 李四              │
│  ● 在职      │                                 │
│               │  编辑模式时显示输入框            │
├─────────────────┴───────────────────────────────┤
│  [保存] [取消] [发送邮件] [办理离职] [删除]      │
└─────────────────────────────────────────────────┘
```

#### 2.2.2 布局要求

- 左侧信息卡片固定(300px)
- 右侧标签页可滚动
- 支持编辑模式切换
- 操作按钮固定在底部

#### 2.2.3 信息卡片样式

```vue
<!-- 员工信息卡片 -->
<div class="employee-card">
  <!-- 头像 -->
  <div class="avatar-wrapper">
    <el-avatar :size="150" :src="employee.avatar">
      {{ employee.name.charAt(0) }}
    </el-avatar>
  </div>

  <!-- 基本信息 -->
  <div class="info-section">
    <h2 class="employee-id">{{ employee.id }}</h2>
    <h1 class="employee-name">{{ employee.name }}</h1>
    <el-tag class="position-tag">{{ employee.position }}</el-tag>
    <el-link class="department-link" type="primary">
      {{ employee.departmentName }}
    </el-link>
    <StatusTag :status="employee.status" type="employee" />
  </div>
</div>
```

### 2.3 员工表单

#### 2.3.1 布局结构

```
┌─────────────────────────────────────────────────┐
│  新增员工                          [X]          │
├─────────────────────────────────────────────────┤
│  Step 1: 基本信息  ▶ Step 2: 详细信息  ▶ Step 3 │
├─────────────────────────────────────────────────┤
│                                                  │
│  姓名 *                                          │
│  [________________]                              │
│                                                  │
│  性别 *                                          │
│  (•) 男  ( ) 女                                  │
│                                                  │
│  联系电话 *                                      │
│  [________________]  ✅ 格式正确                 │
│                                                  │
│  邮箱 *                                          │
│  [________________]  ✅ 邮箱可用                 │
│                                                  │
│  部门 *                                          │
│  [请选择部门 ▼]                                  │
│                                                  │
│  ...                                             │
├─────────────────────────────────────────────────┤
│  [上一步] [下一步] [取消]                        │
└─────────────────────────────────────────────────┘
```

#### 2.3.2 布局要求

- 步骤式表单(3步)
- 实时验证反馈
- 必填项红色星号标识
- 错误提示清晰明确

#### 2.3.3 表单布局配置

```vue
<el-form
  ref="formRef"
  :model="formData"
  :rules="rules"
  label-width="120px"
  label-position="right"
  class="employee-form"
>
  <el-row :gutter="20">
    <el-col :span="12">
      <el-form-item label="姓名" prop="name">
        <el-input v-model="formData.name" placeholder="请输入姓名" />
      </el-form-item>
    </el-col>
    <el-col :span="12">
      <el-form-item label="性别" prop="gender">
        <el-radio-group v-model="formData.gender">
          <el-radio label="male">男</el-radio>
          <el-radio label="female">女</el-radio>
        </el-radio-group>
      </el-form-item>
    </el-col>
  </el-row>
</el-form>
```

---

## 3. 交互规范

### 3.1 列表页交互

#### 3.1.1 搜索交互

```typescript
// 防抖搜索 - 300ms延迟
const handleSearch = debounce(async (keyword: string) => {
  loading.value = true
  try {
    await fetchList({ keyword })
  } finally {
    loading.value = false
  }
}, 300)
```

**交互要求**:
- 输入时显示加载状态
- 300ms防抖延迟
- 实时显示搜索结果数量
- 支持回车键触发搜索

#### 3.1.2 筛选交互

```typescript
// 筛选条件变化时自动刷新
watch(filterConditions, async (newFilter) => {
  loading.value = true
  try {
    await fetchList(newFilter)
  } finally {
    loading.value = false
  }
}, { deep: true })
```

**交互要求**:
- 筛选面板可折叠/展开
- 筛选条件变化时自动刷新
- 显示当前筛选条件标签
- 支持快速清除单个筛选条件
- 支持一键清除所有筛选条件

#### 3.1.3 视图切换交互

```typescript
// 表格/卡片视图切换
const viewMode = ref<'table' | 'card'>('table')

// 切换时保存滚动位置
const switchViewMode = (mode: 'table' | 'card') => {
  const scrollTop = window.pageYOffset
  viewMode.value = mode
  nextTick(() => {
    window.scrollTo(0, scrollTop)
  })
}
```

**交互要求**:
- 切换动画流畅
- 保存滚动位置
- 保持当前筛选条件
- 保持当前排序状态

#### 3.1.4 分页交互

```typescript
// 分页切换
const handlePageChange = async (page: number) => {
  loading.value = true
  try {
    await fetchList({ ...filterConditions, page })
    // 切换后滚动到顶部
    window.scrollTo({ top: 0, behavior: 'smooth' })
  } finally {
    loading.value = false
  }
}
```

**交互要求**:
- 切换后滚动到页面顶部
- 显示加载动画
- 支持快速跳转到指定页
- 显示总记录数和当前页范围

### 3.2 详情页交互

#### 3.2.1 编辑模式切换

```typescript
// 切换编辑模式
const toggleEditMode = async () => {
  if (isEditMode.value) {
    // 保存
    const valid = await formRef.value?.validate()
    if (valid) {
      await updateEmployee(employee.value.id, formData.value)
      isEditMode.value = false
      ElMessage.success('保存成功')
    }
  } else {
    // 进入编辑模式
    isEditMode.value = true
    Object.assign(formData.value, employee.value)
  }
}
```

**交互要求**:
- 查看模式: 纯文本显示
- 编辑模式: 表单输入
- 切换时显示平滑过渡动画
- 未保存时提示用户确认

#### 3.2.2 标签页切换

```typescript
// 标签页切换
const activeTab = ref('basic')

// 切换时保存滚动位置
const handleTabChange = (tabName: string) => {
  activeTab.value = tabName
}
```

**交互要求**:
- 切换动画流畅
- 保持各标签页独立状态
- 支持键盘快捷键切换

### 3.3 表单交互

#### 3.3.1 步骤式表单

```typescript
// 下一步
const handleNext = async () => {
  const valid = await formRef.value?.validate()
  if (valid) {
    if (currentStep.value < 3) {
      currentStep.value++
    }
  }
}

// 上一步
const handlePrev = () => {
  if (currentStep.value > 1) {
    currentStep.value--
  }
}

// 提交
const handleSubmit = async () => {
  const valid = await formRef.value?.validate()
  if (valid) {
    loading.value = true
    try {
      await createEmployee(formData.value)
      ElMessage.success('创建成功')
      router.push('/employee/list')
    } finally {
      loading.value = false
    }
  }
}
```

**交互要求**:
- 每步独立验证
- 禁止跳过未验证的步骤
- 显示当前步骤进度
- 支持返回上一步修改

#### 3.3.2 实时验证

```typescript
// 实时验证反馈
const rules = {
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' },
    {
      validator: async (rule, value, callback) => {
        if (value) {
          const exists = await checkEmailExists(value)
          if (exists) {
            callback(new Error('该邮箱已被使用'))
          } else {
            ElMessage.success('邮箱可用')
            callback()
          }
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}
```

**交互要求**:
- 失去焦点时验证
- 显示验证图标(✅/❌)
- 错误提示清晰明确
- 成功提示可选

### 3.4 操作确认交互

#### 3.4.1 删除确认

```vue
<el-popconfirm
  title="确定要删除该员工吗?此操作不可恢复!"
  confirm-button-text="确定"
  cancel-button-text="取消"
  @confirm="handleDelete"
>
  <template #reference>
    <el-button type="danger" link>删除</el-button>
  </template>
</el-popconfirm>
```

**交互要求**:
- 二次确认对话框
- 明确提示操作后果
- 支持取消操作
- 执行后显示反馈

#### 3.4.2 离职确认

```vue
<el-dialog
  v-model="resignDialogVisible"
  title="办理离职"
  width="500px"
>
  <el-form :model="resignForm" label-width="100px">
    <el-form-item label="离职原因">
      <el-input
        v-model="resignForm.reason"
        type="textarea"
        placeholder="请输入离职原因"
      />
    </el-form-item>
  </el-form>

  <template #footer>
    <el-button @click="resignDialogVisible = false">取消</el-button>
    <el-button type="primary" @click="handleResign">确定</el-button>
  </template>
</el-dialog>
```

**交互要求**:
- 要求输入离职原因
- 明确提示离职影响
- 支持取消操作
- 执行后通知相关人员

---

## 4. 样式规范

### 4.1 颜色规范

#### 4.1.1 主题色

```scss
// 主题色
$primary-color: #409EFF;
$success-color: #67C23A;
$warning-color: #E6A23C;
$danger-color: #F56C6C;
$info-color: #909399;

// 中性色
$text-primary: #303133;
$text-regular: #606266;
$text-secondary: #909399;
$text-placeholder: #C0C4CC;

// 边框色
$border-base: #DCDFE6;
$border-light: #E4E7ED;
$border-lighter: #EBEEF5;
$border-extra-light: #F2F6FC;

// 背景色
$bg-color: #F5F7FA;
$bg-color-page: #FFFFFF;
```

#### 4.1.2 状态标签颜色

```typescript
// src/components/common/StatusTag.vue
const statusConfig = {
  // 员工状态
  active: { text: '在职', color: 'success' },
  resigned: { text: '离职', color: 'info' },
  suspended: { text: '停薪留职', color: 'warning' },

  // 试用期状态
  probation: { text: '试用期内', color: 'warning' },
  regular: { text: '已转正', color: 'success' },

  // 性别
  male: { text: '男', color: 'primary' },
  female: { text: '女', color: 'danger' }
}
```

### 4.2 字体规范

```scss
// 字体大小
$font-size-extra-large: 20px;
$font-size-large: 18px;
$font-size-medium: 16px;
$font-size-base: 14px;
$font-size-small: 13px;
$font-size-extra-small: 12px;

// 字体粗细
$font-weight-normal: 400;
$font-weight-medium: 500;
$font-weight-bold: 700;

// 行高
$line-height-base: 1.5;
$line-height-title: 1.2;
```

### 4.3 间距规范

```scss
// 间距系统
$spacing-extra-small: 4px;
$spacing-small: 8px;
$spacing-base: 16px;
$spacing-medium: 20px;
$spacing-large: 24px;
$spacing-extra-large: 32px;

// 页面边距
$page-padding: 20px;
$page-padding-large: 40px;
```

### 4.4 圆角规范

```scss
// 圆角
$border-radius-base: 4px;
$border-radius-small: 2px;
$border-radius-large: 8px;
$border-radius-circle: 50%;
```

### 4.5 阴影规范

```scss
// 阴影
$box-shadow-base: 0 2px 4px rgba(0, 0, 0, 0.12), 0 0 6px rgba(0, 0, 0, 0.04);
$box-shadow-light: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
$box-shadow-dark: 0 2px 12px 0 rgba(0, 0, 0, 0.2);
```

### 4.6 组件样式示例

#### 4.6.1 员工卡片样式

```scss
.employee-card {
  text-align: center;
  padding: $spacing-large;

  .avatar-wrapper {
    margin-bottom: $spacing-base;

    .el-avatar {
      border: 4px solid $bg-color;
      box-shadow: $box-shadow-base;
    }
  }

  .info-section {
    .employee-id {
      font-size: $font-size-base;
      color: $text-secondary;
      margin-bottom: $spacing-small;
    }

    .employee-name {
      font-size: $font-size-extra-large;
      font-weight: $font-weight-bold;
      color: $text-primary;
      margin-bottom: $spacing-base;
    }

    .position-tag {
      margin-bottom: $spacing-small;
    }

    .department-link {
      display: block;
      margin-bottom: $spacing-small;
    }
  }
}
```

#### 4.6.2 员工表格样式

```scss
.employee-table {
  .el-table__row {
    cursor: pointer;

    &:hover {
      background-color: $bg-color;
    }
  }

  .status-tag {
    margin-right: $spacing-small;
  }

  .action-buttons {
    .el-button {
      margin-left: $spacing-small;
    }
  }
}
```

#### 4.6.3 员工表单样式

```scss
.employee-form {
  max-width: 800px;
  margin: 0 auto;
  padding: $spacing-large;

  .form-section-title {
    font-size: $font-size-large;
    font-weight: $font-weight-bold;
    margin-bottom: $spacing-base;
    padding-bottom: $spacing-small;
    border-bottom: 2px solid $border-light;
  }

  .validation-icon {
    margin-left: $spacing-small;

    &.success {
      color: $success-color;
    }

    &.error {
      color: $danger-color;
    }
  }

  .form-actions {
    text-align: center;
    margin-top: $spacing-large;

    .el-button {
      min-width: 120px;
      margin: 0 $spacing-small;
    }
  }
}
```

### 4.7 动画规范

```scss
// 过渡动画
$transition-base: all 0.3s ease;
$transition-fast: all 0.15s ease;
$transition-slow: all 0.5s ease;

// 自定义动画
@keyframes slideIn {
  from {
    transform: translateX(-100%);
    opacity: 0;
  }
  to {
    transform: translateX(0);
    opacity: 1;
  }
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}
```

### 4.8 响应式样式

```scss
// 响应式断点
$breakpoint-xs: 768px;
$breakpoint-sm: 992px;
$breakpoint-md: 1200px;
$breakpoint-lg: 1920px;

// 响应式混入
@mixin respond-to($breakpoint) {
  @if $breakpoint == xs {
    @media (max-width: $breakpoint-xs) { @content; }
  }
  @else if $breakpoint == sm {
    @media (min-width: $breakpoint-xs) { @content; }
  }
  @else if $breakpoint == md {
    @media (min-width: $breakpoint-sm) { @content; }
  }
  @else if $breakpoint == lg {
    @media (min-width: $breakpoint-md) { @content; }
  }
}

// 使用示例
.employee-list {
  @include respond-to(xs) {
    .filter-panel {
      display: none;
    }

    .stats-panel {
      position: static;
    }
  }
}
```

---

## 附录

### A. 设计资源

**图标库**:
- Element Plus Icons
- Font Awesome

**图片占位符**:
- 头像: 默认使用员工姓名首字母
- 缺省图: 使用Element Plus Empty组件

### B. 可访问性要求

- 支持键盘导航
- 支持屏幕阅读器
- 颜色对比度符合WCAG 2.1标准
- 焦点状态清晰可见

### C. 浏览器兼容性

- Chrome/Edge: 最新版
- Firefox: 最新版
- Safari: 最新版
- IE: 不支持

---

**文档版本**: v1.0.0
**创建人**: AI开发助手
**最后更新**: 2026-01-09
