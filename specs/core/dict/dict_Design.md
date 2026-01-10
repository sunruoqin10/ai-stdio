# 数据字典UI/UX设计规范

> **规范类型**: 第三层 - UI/UX设计规范
> **模块**: 数据字典
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
| 树形展示 | el-tree | 树形组件 |
| 数据录入 | el-form | 表单组件 |
| 对话框 | el-dialog | 对话框 |
| 确认操作 | el-popconfirm | 气泡确认框 |
| 颜色选择 | el-color-picker | 颜色选择器 |
| 拖拽排序 | draggable | 拖拽组件 |
| 标签页 | el-tabs | 标签页组件 |

### 1.2 组件使用说明

#### 1.2.1 el-tree - 树形组件

**用途**: 字典类型树形展示

**配置要求**:
```vue
<el-tree
  :data="dictTreeData"
  :props="treeProps"
  node-key="id"
  highlight-current
  default-expand-all
  @node-click="handleNodeClick"
>
  <template #default="{ node, data }">
    <span class="custom-tree-node">
      <el-icon v-if="data.category === 'system'"><Lock /></el-icon>
      <span>{{ node.label }}</span>
      <el-tag size="small" type="info">{{ data.itemCount }}</el-tag>
    </span>
  </template>
</el-tree>
```

#### 1.2.2 draggable - 拖拽组件

**用途**: 字典项拖拽排序

**配置要求**:
```vue
<draggable
  v-model="dictItemList"
  item-key="id"
  @end="handleDragEnd"
>
  <template #item="{ element }">
    <div class="dict-item-row">
      <el-icon class="drag-handle"><Rank /></el-icon>
      <span>{{ element.label }}</span>
      <span>{{ element.value }}</span>
    </div>
  </template>
</draggable>
```

#### 1.2.3 DictColorTag - 自定义颜色标签

**用途**: 字典项颜色预览

**组件路径**: `@/components/common/DictColorTag.vue`

**使用示例**:
```vue
<DictColorTag
  :label="dictItem.label"
  :color-type="dictItem.colorType"
/>
```

---

## 2. 页面布局

### 2.1 字典管理主页

#### 2.1.1 布局结构

```
┌─────────────────────────────────────────────────┐
│  PageHeader: 数据字典    [新增字典类型] [导入]  │
├───────────┬───────────────────────────┬─────────┤
│           │                           │         │
│ Dict Tree │    Dict Type Table        │  Stats  │
│ (250px)   │    - 编号                  │  Panel  │
│           │    - 字典编码              │         │
│ 📁 系统字典│    - 字典名称              │  总数   │
│   ├ 员工状态│    - 类别(系统/业务)      │  系统字典│
│   ├ 资产状态│    - 项数量               │  业务字典│
│   └ 审批状态│    - 状态                │         │
│ 📁 业务字典│    - 创建时间              │         │
│   ├ 项目优先级│    - 操作                │         │
│   └ 客户等级│    (编辑/删除/查看项)     │         │
│           │                           │         │
├───────────┴───────────────────────────┴─────────┤
│  Pagination: 上一页 [1] [2] [3] ... 下一页     │
└─────────────────────────────────────────────────┘
```

#### 2.1.2 布局要求

- 左侧字典树固定宽度(250px),可折叠
- 中间表格区域自适应宽度
- 右侧统计面板固定宽度(200px)
- 分页器在底部居中

#### 2.1.3 响应式设计

```typescript
// 断点配置
const breakpoints = {
  xs: '< 768px',    // 小屏幕: 单列布局
  sm: '≥ 768px',    // 平板: 树形可折叠
  md: '≥ 992px',    // 中等屏幕: 标准布局
  lg: '≥ 1200px'    // 大屏幕: 显示统计面板
}
```

### 2.2 字典项管理页

#### 2.2.1 布局结构

```
┌─────────────────────────────────────────────────┐
│  ← 员工状态              [新增字典项] [批量操作▼]│
├─────────────────────────────────────────────────┤
│  字典类型: 员工状态                             │
│  字典编码: employee_status                      │
│  字典类别: 系统字典 🔒                          │
├─────────────────────────────────────────────────┤
│                                                  │
│  ┌────────────────────────────────────────────┐ │
│  │ ⋮⋮  在职      active  success  [10] 启用  │ │
│  │ ⋮⋮  离职      resigned  info  [20] 启用   │ │
│  │ ⋮⋮  试用期    probation  warning  [30] 启用│ │
│  └────────────────────────────────────────────┘ │
│                                                  │
│  拖拽行可调整顺序                                │
├─────────────────────────────────────────────────┤
│  [保存排序] [批量启用] [批量禁用] [批量删除]     │
└─────────────────────────────────────────────────┘
```

#### 2.2.2 布局要求

- 顶部显示字典类型信息
- 中间为可拖拽的字典项列表
- 每行显示: 拖拽手柄、标签、值、颜色、排序、状态、操作
- 底部操作按钮

#### 2.2.3 字典项行样式

```vue
<!-- 字典项行 -->
<div class="dict-item-row" :class="{ disabled: item.status === 'disabled' }">
  <!-- 拖拽手柄 -->
  <el-icon class="drag-handle"><Rank /></el-icon>

  <!-- 标签 -->
  <span class="item-label">{{ item.label }}</span>

  <!-- 值 -->
  <el-tag size="small" class="item-value">{{ item.value }}</el-tag>

  <!-- 颜色预览 -->
  <DictColorTag :label="item.label" :color-type="item.colorType" />

  <!-- 排序 -->
  <el-input-number
    v-model="item.sortOrder"
    :min="0"
    size="small"
    class="sort-input"
  />

  <!-- 状态 -->
  <el-switch
    v-model="item.status"
    active-value="enabled"
    inactive-value="disabled"
  />

  <!-- 操作 -->
  <div class="action-buttons">
    <el-button link type="primary" size="small" @click="handleEdit(item)">
      编辑
    </el-button>
    <el-popconfirm
      title="确定要删除该字典项吗?"
      @confirm="handleDelete(item)"
    >
      <template #reference>
        <el-button link type="danger" size="small">删除</el-button>
      </template>
    </el-popconfirm>
  </div>
</div>
```

### 2.3 字典类型表单

#### 2.3.1 布局结构

```
┌─────────────────────────────────────────────────┐
│  新增字典类型                        [X]         │
├─────────────────────────────────────────────────┤
│                                                  │
│  字典编码 *                                      │
│  [employee________________]                      │
│  💡 只能包含小写字母、数字和下划线,且以字母开头   │
│                                                  │
│  字典名称 *                                      │
│  [员工状态_________________]                     │
│                                                  │
│  字典类别 *                                      │
│  (•) 系统字典  ( ) 业务字典                      │
│                                                  │
│  字典描述                                        │
│  [_________________________________________]    │
│  [_________________________________________]    │
│                                                  │
│  排序序号                                        │
│  [100____________]                               │
│                                                  │
│  扩展属性                                        │
│  {                                               │
│    "key1": "value1",                             │
│    "key2": "value2"                              │
│  }                                               │
│                                                  │
├─────────────────────────────────────────────────┤
│              [取消]  [确定]                      │
└─────────────────────────────────────────────────┘
```

#### 2.3.2 表单布局配置

```vue
<el-form
  ref="formRef"
  :model="formData"
  :rules="rules"
  label-width="120px"
  label-position="right"
  class="dict-type-form"
>
  <el-form-item label="字典编码" prop="code">
    <el-input
      v-model="formData.code"
      placeholder="如: employee_status"
    >
      <template #suffix>
        <el-tooltip content="只能包含小写字母、数字和下划线,且以字母开头">
          <el-icon><QuestionFilled /></el-icon>
        </el-tooltip>
      </template>
    </el-input>
  </el-form-item>

  <el-form-item label="字典名称" prop="name">
    <el-input
      v-model="formData.name"
      placeholder="请输入字典名称"
    />
  </el-form-item>

  <el-form-item label="字典类别" prop="category">
    <el-radio-group v-model="formData.category">
      <el-radio label="system">系统字典</el-radio>
      <el-radio label="business">业务字典</el-radio>
    </el-radio-group>
  </el-form-item>

  <el-form-item label="字典描述" prop="description">
    <el-input
      v-model="formData.description"
      type="textarea"
      :rows="3"
      placeholder="请输入字典描述(可选)"
    />
  </el-form-item>

  <el-form-item label="排序序号" prop="sortOrder">
    <el-input-number
      v-model="formData.sortOrder"
      :min="0"
      :step="10"
    />
  </el-form-item>

  <el-form-item label="扩展属性" prop="extProps">
    <el-input
      v-model="extPropsJson"
      type="textarea"
      :rows="5"
      placeholder='请输入JSON格式的扩展属性,如: {"key1": "value1"}'
      @blur="validateExtProps"
    />
  </el-form-item>
</el-form>
```

### 2.4 字典项表单

#### 2.4.1 布局结构

```
┌─────────────────────────────────────────────────┐
│  新增字典项                          [X]         │
├─────────────────────────────────────────────────┤
│                                                  │
│  所属字典类型 *                                  │
│  [员工状态 (employee_status) ▼]                  │
│                                                  │
│  项标签 *                                        │
│  [在职____________________]                      │
│                                                  │
│  项值 *                                          │
│  [active________________]                        │
│  💡 在同一字典类型下必须唯一                      │
│                                                  │
│  颜色类型 *                                      │
│  ● primary  ○ success  ○ warning                │
│  ○ danger     ○ info                             │
│                                                  │
│  预览效果                                        │
│  [ 在职 ]  (success)                            │
│                                                  │
│  排序序号 *                                      │
│  [10_____________]                               │
│                                                  │
│  状态                                            │
│  [● 启用  ○ 禁用]                                │
│                                                  │
├─────────────────────────────────────────────────┤
│              [取消]  [确定]                      │
└─────────────────────────────────────────────────┘
```

#### 2.4.2 颜色选择器

```vue
<el-form-item label="颜色类型" prop="colorType">
  <el-radio-group v-model="formData.colorType" class="color-type-group">
    <el-radio label="primary">
      <span class="color-preview color-primary">Primary</span>
    </el-radio>
    <el-radio label="success">
      <span class="color-preview color-success">Success</span>
    </el-radio>
    <el-radio label="warning">
      <span class="color-preview color-warning">Warning</span>
    </el-radio>
    <el-radio label="danger">
      <span class="color-preview color-danger">Danger</span>
    </el-radio>
    <el-radio label="info">
      <span class="color-preview color-info">Info</span>
    </el-radio>
  </el-radio-group>
</el-form-item>

<el-form-item label="预览效果">
  <DictColorTag
    :label="formData.label || '标签'"
    :color-type="formData.colorType"
  />
</el-form-item>
```

---

## 3. 交互规范

### 3.1 字典树交互

#### 3.1.1 节点点击

```typescript
// 点击树节点,刷新右侧列表
const handleNodeClick = (data: DictTreeNode) => {
  selectedDictType.value = data

  // 刷新右侧表格
  fetchDictTypeList({
    dictTypeCode: data.code
  })
}
```

**交互要求**:
- 点击节点高亮显示
- 右侧表格自动刷新
- 面包屑导航更新
- 支持键盘导航(上下箭头)

#### 3.1.2 树形展开/折叠

```typescript
// 全部展开
const expandAll = () => {
  const treeRef = ref<InstanceType<typeof ElTree>>()
  const allKeys = treeRef.value?.store.getAllNodeKeys()
  allKeys?.forEach(key => {
    treeRef.value?.store.nodesMap[key].expand()
  })
}

// 全部折叠
const collapseAll = () => {
  const treeRef = ref<InstanceType<typeof ElTree>>()
  const allKeys = treeRef.value?.store.getAllNodeKeys()
  allKeys?.forEach(key => {
    treeRef.value?.store.nodesMap[key].collapse()
  })
}
```

**交互要求**:
- 提供"全部展开"/"全部折叠"按钮
- 记住展开/折叠状态
- 刷新后恢复之前的状态

### 3.2 拖拽排序交互

#### 3.2.1 拖拽实现

```typescript
import draggable from 'vuedraggable'

// 拖拽结束
const handleDragEnd = async () => {
  // 更新排序序号
  dictItemList.value.forEach((item, index) => {
    item.sortOrder = (index + 1) * 10
  })

  // 提交到服务器
  await updateDictItemSort(
    dictItemList.value.map(item => ({
      id: item.id,
      sortOrder: item.sortOrder
    }))
  )

  ElMessage.success('排序已更新')
}
```

**交互要求**:
- 拖拽时显示虚线框
- 拖拽手柄明显可见
- 拖拽过程流畅
- 拖拽结束后自动保存
- 显示保存加载状态

#### 3.2.2 排序动画

```scss
.dict-item-list {
  .dict-item-row {
    transition: all 0.3s ease;

    &.dragging {
      opacity: 0.5;
      background-color: $bg-color;
    }

    &.drag-over {
      border-top: 2px solid $primary-color;
    }
  }
}
```

### 3.3 表单交互

#### 3.3.1 实时验证

```typescript
// 字典编码格式验证
const validateCode = (rule: any, value: string, callback: any) => {
  if (!value) {
    callback(new Error('请输入字典编码'))
  } else if (!/^[a-z][a-z0-9_]*$/.test(value)) {
    callback(new Error('只能包含小写字母、数字和下划线,且以字母开头'))
  } else if (value !== originalCode.value) {
    // 异步验证唯一性
    checkCodeExists(value).then(exists => {
      if (exists) {
        callback(new Error('该字典编码已存在'))
      } else {
        ElMessage.success('字典编码可用')
        callback()
      }
    })
  } else {
    callback()
  }
}
```

**交互要求**:
- 失去焦点时验证
- 显示验证图标(✅/❌)
- 错误提示清晰明确
- 成功提示可选

#### 3.3.2 颜色预览

```typescript
// 实时预览颜色效果
const colorPreview = computed(() => {
  return {
    backgroundColor: getColorValue(formData.value.colorType),
    color: '#fff'
  }
})
```

**交互要求**:
- 选择颜色后立即显示预览
- 预览标签与实际效果一致
- 支持所有颜色类型

### 3.4 搜索交互

#### 3.4.1 防抖搜索

```typescript
// 防抖搜索 - 300ms延迟
const handleSearch = debounce(async (keyword: string) => {
  loading.value = true
  try {
    await fetchDictList({ keyword })
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
- 高亮显示匹配结果

#### 3.4.2 高亮显示

```vue
<template>
  <span v-html="highlightText(dict.name, searchKeyword)" />
</template>

<script setup lang="ts">
function highlightText(text: string, keyword: string): string {
  if (!keyword) return text

  const regex = new RegExp(`(${keyword})`, 'gi')
  return text.replace(regex, '<mark>$1</mark>')
}
</script>

<style scoped>
mark {
  background-color: yellow;
  padding: 0 2px;
}
</style>
```

### 3.5 操作确认交互

#### 3.5.1 删除确认

```vue
<el-popconfirm
  title="确定要删除该字典类型吗?"
  width="300"
  confirm-button-text="确定"
  cancel-button-text="取消"
  :icon="WarningFilled"
  icon-color="#F56C6C"
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

#### 3.5.2 系统字典删除提示

```vue
<el-popconfirm
  v-if="dictType.category === 'system'"
  title="系统字典不可删除!"
  width="300"
  confirm-button-text="知道了"
  :show-cancel="false"
  :icon="Lock"
  icon-color="#E6A23C"
>
  <template #reference>
    <el-button type="danger" link disabled>
      <el-icon><Lock /></el-icon>
      删除
    </el-button>
  </template>
</el-popconfirm>
```

---

## 4. 样式规范

### 4.1 颜色规范

#### 4.1.1 主题色

```scss
// 字典颜色类型
$dict-primary: #409EFF;    // 主要色
$dict-success: #67C23A;    // 成功色
$dict-warning: #E6A23C;    // 警告色
$dict-danger: #F56C6C;     // 危险色
$dict-info: #909399;       // 信息色

// 字典类别标识
$dict-system: #E6A23C;     // 系统字典
$dict-business: #409EFF;   // 业务字典
```

#### 4.1.2 颜色预览样式

```scss
.color-preview {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 4px;
  color: #fff;
  font-size: 12px;
  font-weight: 500;

  &.color-primary {
    background-color: $dict-primary;
  }

  &.color-success {
    background-color: $dict-success;
  }

  &.color-warning {
    background-color: $dict-warning;
  }

  &.color-danger {
    background-color: $dict-danger;
  }

  &.color-info {
    background-color: $dict-info;
  }
}
```

### 4.2 字体规范

```scss
// 字体大小
$font-size-base: 14px;
$font-size-small: 13px;
$font-size-large: 16px;

// 字体粗细
$font-weight-normal: 400;
$font-weight-medium: 500;
$font-weight-bold: 600;
```

### 4.3 间距规范

```scss
// 间距系统
$spacing-small: 8px;
$spacing-base: 16px;
$spacing-large: 24px;

// 组件内间距
$dict-item-padding: 12px;
$dict-form-gap: 20px;
```

### 4.4 组件样式示例

#### 4.4.1 字典树样式

```scss
.dict-tree {
  .el-tree-node__content {
    height: 40px;
    padding-right: 10px;

    &:hover {
      background-color: $bg-color;
    }
  }

  .custom-tree-node {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: space-between;
    font-size: 14px;

    .el-icon {
      margin-right: 6px;
      color: $dict-warning;
    }

    .el-tag {
      margin-left: 8px;
    }
  }
}
```

#### 4.4.2 字典项行样式

```scss
.dict-item-row {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: $dict-item-padding;
  border-bottom: 1px solid $border-light;
  transition: all 0.3s ease;

  &:hover {
    background-color: $bg-color;
  }

  &.disabled {
    opacity: 0.6;
    background-color: $bg-color-disabled;
  }

  .drag-handle {
    cursor: move;
    color: $text-secondary;
    font-size: 18px;

    &:hover {
      color: $primary-color;
    }
  }

  .item-label {
    flex: 1;
    font-weight: 500;
  }

  .item-value {
    font-family: 'Courier New', monospace;
  }

  .sort-input {
    width: 100px;
  }

  .action-buttons {
    display: flex;
    gap: 8px;
  }
}
```

#### 4.4.3 颜色选择器样式

```scss
.color-type-group {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;

  :deep(.el-radio) {
    margin-right: 0;

    .el-radio__label {
      padding-left: 8px;
    }
  }
}
```

### 4.5 动画规范

```scss
// 过渡动画
$transition-base: all 0.3s ease;
$transition-fast: all 0.15s ease;

// 拖拽动画
@keyframes dragDrop {
  0% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-5px);
  }
  100% {
    transform: translateY(0);
  }
}

.dict-item-row.dragging {
  animation: dragDrop 0.3s ease;
}
```

### 4.6 响应式样式

```scss
// 响应式断点
$breakpoint-xs: 768px;
$breakpoint-sm: 992px;
$breakpoint-md: 1200px;

// 响应式布局
.dict-management-page {
  display: grid;
  grid-template-columns: 250px 1fr 200px;
  gap: 20px;

  @media (max-width: $breakpoint-md) {
    grid-template-columns: 250px 1fr;

    .stats-panel {
      display: none;
    }
  }

  @media (max-width: $breakpoint-xs) {
    grid-template-columns: 1fr;

    .dict-tree {
      display: none;
    }
  }
}
```

### 4.7 可访问性要求

- 支持键盘导航(Tab键、方向键)
- 支持屏幕阅读器
- 颜色对比度符合WCAG 2.1标准
- 焦点状态清晰可见
- 拖拽操作提供键盘替代方案

---

## 附录

### A. 设计资源

**图标库**:
- Element Plus Icons
- 拖拽手柄: Rank
- 系统字典: Lock
- 删除: Delete
- 编辑: Edit

**颜色预览**:
- 实时预览标签效果
- 支持5种颜色类型
- 背景色+前景色组合

### B. 组件文件结构

```
src/modules/dict/
├── components/
│   ├── DictTree.vue          # 字典树组件
│   ├── DictTypeTable.vue     # 字典类型表格
│   ├── DictItemList.vue      # 字典项列表(可拖拽)
│   ├── DictTypeForm.vue      # 字典类型表单
│   ├── DictItemForm.vue      # 字典项表单
│   └── DictColorTag.vue      # 颜色标签组件
├── views/
│   ├── DictManagement.vue    # 字典管理主页
│   └── DictItemList.vue      # 字典项管理页
└── styles/
    └── dict.scss              # 字典模块样式
```

### C. 浏览器兼容性

- Chrome/Edge: 最新版
- Firefox: 最新版
- Safari: 最新版
- IE: 不支持

---

**文档版本**: v1.0.0
**创建人**: AI开发助手
**最后更新**: 2026-01-09
