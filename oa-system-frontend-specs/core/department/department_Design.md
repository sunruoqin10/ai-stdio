# 部门管理模块 - UI/UX设计规范

> **文档类型**: UI/UX设计规范
> **设计风格**: Element Plus
> **可视化组件**: ECharts Graph
> **响应式设计**: 支持
> **创建日期**: 2026-01-09
> **最后更新**: 2026-01-09

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

### 1.1 部门列表页布局

```
┌─────────────────────────────────────────────────┐
│  PageHeader: 组织架构    [新增部门] [导出]       │
├─────────────────────────────────────────────────┤
│  Tab: [列表视图] [架构图]                         │
├───────────┬─────────────────────────────────────┤
│           │                                      │
│ Filter    │  树形表格 / ECharts图                 │
│ Panel     │  - 部门名称                          │
│           │  - 负责人                            │
│           │  - 人数                              │
│           │  - 操作                              │
│           │                                      │
├───────────┴─────────────────────────────────────┤
│  统计卡片: 总部门数 | 一级部门 | 最大层级         │
└─────────────────────────────────────────────────┘
```

**布局说明**:
- **顶部**: 页面标题 + 主要操作按钮
- **导航**: Tab切换列表视图和架构图
- **左侧**: 筛选面板(可折叠)
- **中间**: 主内容区(树形表格或图表)
- **底部**: 统计信息卡片

### 1.2 部门详情抽屉布局

```
┌────────────────────────────────────────────────┐
│  部门详情                              [X]       │
├────────────────────────────────────────────────┤
│  基本信息                                       │
│  - 部门名称: 技术部                            │
│  - 部门简称: 技术                              │
│  - 上级部门: 研发中心                          │
│  - 部门负责人: 张三                            │
│  - 部门人数: 25人                              │
│  - 成立时间: 2020-01-01                        │
│                                                 │
│  Tabs: [成员列表] [子部门] [操作记录]           │
│  ────────────────────────────────────────      │
│  [成员表格]                                    │
│                                                 │
├────────────────────────────────────────────────┤
│  [编辑] [删除] [添加子部门]                     │
└────────────────────────────────────────────────┘
```

**布局说明**:
- **头部**: 抽屉标题 + 关闭按钮
- **内容区**: 部门基本信息 + Tab切换内容
- **底部**: 操作按钮组

### 1.3 部门表单对话框布局

```
┌──────────────────────────────────────────┐
│  新增部门                      [X]       │
├──────────────────────────────────────────┤
│  基本信息                                 │
│  ┌─────────────────────────────────┐    │
│  │ 部门名称: [____________]         │    │
│  │ 部门简称: [____________]         │    │
│  │ 上级部门: [____________]         │    │
│  │ 负责人:   [____________]         │    │
│  │ 排序号:   [____]                 │    │
│  │ 成立时间: [____________]         │    │
│  │ 部门描述:                        │    │
│  │ [_________________________]      │    │
│  │ [_________________________]      │    │
│  └─────────────────────────────────┘    │
│                                           │
├──────────────────────────────────────────┤
│            [取消] [确定]                  │
└──────────────────────────────────────────┘
```

---

## 2. 组件选择

### 2.1 核心组件清单

| 功能 | 组件 | 说明 | 文档链接 |
|------|------|------|----------|
| 树形表格 | el-table | Element Plus树形表格 | https://element-plus.org/zh-CN/component/table.html |
| 架构图 | ECharts Graph | 可视化图表库 | https://echarts.apache.org/zh/option.html#series-graph |
| 部门表单 | el-form | 表单组件 | https://element-plus.org/zh-CN/component/form.html |
| 部门详情 | el-drawer | 抽屉组件 | https://element-plus.org/zh-CN/component/drawer.html |
| 员工选择 | el-select | 下拉选择器 | https://element-plus.org/zh-CN/component/select.html |
| 筛选面板 | el-card | 卡片容器 | https://element-plus.org/zh-CN/component/card.html |
| 统计卡片 | el-statistic | 统计数值 | https://element-plus.org/zh-CN/component/statistic.html |
| 确认对话框 | el-message-box | 消息弹框 | https://element-plus.org/zh-CN/component/message-box.html |

### 2.2 树形表格配置

```vue
<el-table
  :data="departmentList"
  row-key="id"
  :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
  default-expand-all
  :expand-row-keys="expandedKeys"
  :height="tableHeight"
  v-loading="loading"
>
  <el-table-column prop="name" label="部门名称" min-width="200">
    <template #default="{ row }">
      <div class="department-name">
        <el-icon v-if="row.icon"><component :is="row.icon" /></el-icon>
        <span>{{ row.name }}</span>
      </div>
    </template>
  </el-table-column>

  <el-table-column prop="leader.name" label="负责人" width="120">
    <template #default="{ row }">
      <el-tag v-if="row.leader" type="info">
        {{ row.leader.name }}
      </el-tag>
      <span v-else class="text-placeholder">未设置</span>
    </template>
  </el-table-column>

  <el-table-column prop="employeeCount" label="人数" width="80" align="center">
    <template #default="{ row }">
      <el-badge :value="row.employeeCount || 0" :max="999" />
    </template>
  </el-table-column>

  <el-table-column prop="level" label="层级" width="80" align="center">
    <template #default="{ row }">
      <el-tag size="small">Level {{ row.level }}</el-tag>
    </template>
  </el-table-column>

  <el-table-column prop="status" label="状态" width="80" align="center">
    <template #default="{ row }">
      <el-tag :type="row.status === 'active' ? 'success' : 'info'">
        {{ row.status === 'active' ? '正常' : '停用' }}
      </el-tag>
    </template>
  </el-table-column>

  <el-table-column label="操作" width="260" fixed="right">
    <template #default="{ row }">
      <el-button link type="primary" @click="handleAddChild(row)">
        添加子部门
      </el-button>
      <el-button link type="primary" @click="handleEdit(row)">
        编辑
      </el-button>
      <el-button link type="primary" @click="handleMove(row)">
        移动
      </el-button>
      <el-button link type="danger" @click="handleDelete(row)">
        删除
      </el-button>
    </template>
  </el-table-column>
</el-table>
```

### 2.3 ECharts组织架构图配置

```typescript
const graphOption = {
  tooltip: {
    trigger: 'item',
    formatter: (params: any) => {
      return `${params.data.name}<br/>人数: ${params.data.value}人`
    }
  },
  series: [{
    type: 'graph',
    layout: 'tree',
    symbol: 'rect',
    symbolSize: [140, 70],
    roam: true,  // 支持缩放拖拽
    expandAndCollapse: true,
    initialTreeDepth: 2,

    label: {
      show: true,
      position: 'inside',
      fontSize: 14,
      color: '#fff',
      formatter: (params: any) => {
        return `${params.data.name}\n${params.data.value}人`
      }
    },

    edgeSymbol: ['circle', 'arrow'],
    edgeSymbolSize: [4, 10],
    edgeShape: 'curve',

    data: graphData,
    links: graphLinks,

    itemStyle: {
      color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
        { offset: 0, color: '#409EFF' },
        { offset: 1, color: '#53A8FF' }
      ]),
      borderColor: '#409EFF',
      borderWidth: 2,
      borderRadius: [8, 8, 0, 0]
    },

    lineStyle: {
      color: '#C0C4CC',
      width: 2,
      curveness: 0.3
    },

    emphasis: {
      focus: 'adjacency',
      lineStyle: {
        width: 4
      },
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#67C23A' },
          { offset: 1, color: '#85CE61' }
        ])
      }
    },

    blur: {
      itemStyle: {
        opacity: 0.4
      },
      lineStyle: {
        opacity: 0.4
      },
      label: {
        opacity: 0.4
      }
    }
  }]
}
```

### 2.4 部门表单组件

```vue
<el-form
  ref="formRef"
  :model="formData"
  :rules="formRules"
  label-width="100px"
  class="department-form"
>

  <el-form-item label="部门名称" prop="name">
    <el-input
      v-model="formData.name"
      placeholder="请输入部门名称"
      maxlength="50"
      show-word-limit
      clearable
    />
  </el-form-item>

  <el-form-item label="部门简称" prop="shortName">
    <el-input
      v-model="formData.shortName"
      placeholder="请输入部门简称"
      maxlength="20"
      show-word-limit
      clearable
    />
  </el-form-item>

  <el-form-item label="上级部门" prop="parentId">
    <el-tree-select
      v-model="formData.parentId"
      :data="departmentTree"
      :props="{ label: 'name', value: 'id' }"
      placeholder="请选择上级部门"
      check-strictly
      clearable
      filterable
    />
  </el-form-item>

  <el-form-item label="部门负责人" prop="leaderId">
    <el-select
      v-model="formData.leaderId"
      placeholder="请选择部门负责人"
      filterable
      remote
      :remote-method="searchEmployees"
      :loading="employeeLoading"
      clearable
    >
      <el-option
        v-for="emp in employeeList"
        :key="emp.id"
        :label="emp.name"
        :value="emp.id"
      >
        <span>{{ emp.name }}</span>
        <span class="text-secondary">({{ emp.department?.name }})</span>
      </el-option>
    </el-select>
  </el-form-item>

  <el-form-item label="排序号" prop="sort">
    <el-input-number
      v-model="formData.sort"
      :min="0"
      :max="999"
      placeholder="请输入排序号"
    />
  </el-form-item>

  <el-form-item label="成立时间" prop="establishedDate">
    <el-date-picker
      v-model="formData.establishedDate"
      type="date"
      placeholder="请选择成立时间"
      format="YYYY-MM-DD"
      value-format="YYYY-MM-DD"
    />
  </el-form-item>

  <el-form-item label="部门描述" prop="description">
    <el-input
      v-model="formData.description"
      type="textarea"
      :rows="4"
      placeholder="请输入部门描述"
      maxlength="500"
      show-word-limit
    />
  </el-form-item>

</el-form>
```

---

## 3. 交互规范

### 3.1 页面交互流程

#### 3.1.1 查看部门详情
```
用户操作: 点击部门名称/行
交互反馈:
  - 打开详情抽屉(从右侧滑入)
  - 显示加载动画
  - 加载完成后展示详细信息
  - 高亮当前选中的部门
```

#### 3.1.2 新增部门
```
用户操作: 点击"新增部门"按钮
交互反馈:
  - 打开表单对话框(居中显示)
  - 自动聚焦到"部门名称"输入框
  - 实时验证表单字段
  - 保存时显示加载状态
  - 成功后关闭对话框并刷新列表
  - 显示成功提示消息
```

#### 3.1.3 编辑部门
```
用户操作: 点击"编辑"按钮
交互反馈:
  - 打开表单对话框并回显数据
  - 禁用修改上级部门(避免混乱)
  - 实时验证表单字段
  - 保存时显示加载状态
  - 成功后关闭对话框并更新列表
```

#### 3.1.4 移动部门
```
用户操作: 点击"移动"按钮
交互反馈:
  - 打开移动对话框
  - 显示树形选择器选择新上级部门
  - 实时验证移动合法性
  - 显示移动后的层级预览
  - 确认后执行移动操作
  - 成功后刷新列表并展开到新位置
```

#### 3.1.5 删除部门
```
用户操作: 点击"删除"按钮
交互反馈:
  - 显示二次确认对话框
  - 提示删除影响(子部门、成员)
  - 确认后显示删除进度
  - 成功后从列表中移除
  - 显示成功提示消息
```

### 3.2 图表交互

#### 3.2.1 组织架构图交互
```
- 滚轮缩放: 放大/缩小图表
- 鼠标拖拽: 移动图表位置
- 点击节点: 高亮节点及其路径
- 双击节点: 展开/收起子节点
- 悬浮节点: 显示详细信息tooltip
- 右键节点: 显示操作菜单
```

#### 3.2.2 树形表格交互
```
- 点击展开图标: 展开/收起子部门
- 双击行: 查看部门详情
- 右键行: 显示快捷操作菜单
- 悬浮行: 高亮显示
- 拖拽行: 移动部门位置(可选)
```

### 3.3 加载状态

#### 3.3.1 列表加载
```vue
<el-table v-loading="loading" element-loading-text="加载中...">
```

#### 3.3.2 按钮加载
```vue
<el-button :loading="submitting" @click="handleSubmit">
  保存
</el-button>
```

#### 3.3.3 图表加载
```vue
<div v-loading="chartLoading" class="chart-container">
  <div ref="chartRef" style="width: 100%; height: 600px"></div>
</div>
```

### 3.4 反馈提示

#### 3.4.1 成功提示
```typescript
ElMessage.success({
  message: '部门创建成功',
  duration: 2000,
  showClose: true
})
```

#### 3.4.2 错误提示
```typescript
ElMessage.error({
  message: '操作失败: ' + error.message,
  duration: 3000,
  showClose: true
})
```

#### 3.4.3 警告提示
```typescript
ElMessage.warning({
  message: '部门名称已存在',
  duration: 2500,
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

// 文字颜色
$text-primary: #303133;         // 主要文字
$text-regular: #606266;         // 常规文字
$text-secondary: #909399;       // 次要文字
$text-placeholder: #C0C4CC;     // 占位文字

// 边框颜色
$border-base: #DCDFE6;          // 基础边框
$border-light: #E4E7ED;         // 浅色边框
$border-lighter: #EBEEF5;       // 更浅边框
$border-extra-light: #F2F6FC;   // 极浅边框

// 背景颜色
$bg-color: #F5F7FA;             // 页面背景
$fill-base: #F0F2F5;            // 基础填充

// 部门层级颜色(用于区分不同层级)
$level-1-color: #409EFF;        // 一级部门
$level-2-color: #67C23A;        // 二级部门
$level-3-color: #E6A23C;        // 三级部门
$level-4-color: #F56C6C;        // 四级部门
$level-5-color: #909399;        // 五级部门
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

// 字体粗细
$font-weight-normal: 400;       // 常规
$font-weight-medium: 500;       // 中等
$font-weight-bold: 600;         // 粗体
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
$spacing-xxl: $spacing-base * 5;     // 40px
```

### 4.4 组件样式

#### 4.4.1 部门表格样式

```scss
.department-table {
  .department-name {
    display: flex;
    align-items: center;
    gap: 8px;

    .el-icon {
      font-size: 18px;
      color: $primary-color;
    }
  }

  .text-placeholder {
    color: $text-placeholder;
    font-style: italic;
  }

  .text-secondary {
    color: $text-secondary;
    font-size: $font-size-small;
  }

  // 层级缩进
  .el-table__row {
    .level-1 { padding-left: 0; }
    .level-2 { padding-left: 20px; }
    .level-3 { padding-left: 40px; }
    .level-4 { padding-left: 60px; }
    .level-5 { padding-left: 80px; }
  }
}
```

#### 4.4.2 部门表单样式

```scss
.department-form {
  padding: $spacing-lg;

  .el-form-item {
    margin-bottom: $spacing-lg;
  }

  .el-input,
  .el-select,
  .el-tree-select {
    width: 100%;
  }

  .form-actions {
    display: flex;
    justify-content: flex-end;
    gap: $spacing-sm;
    margin-top: $spacing-xl;
  }
}
```

#### 4.4.3 详情抽屉样式

```scss
.department-drawer {
  .drawer-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: $spacing-md;
    border-bottom: 1px solid $border-light;

    .drawer-title {
      font-size: $font-size-large;
      font-weight: $font-weight-bold;
    }
  }

  .drawer-body {
    padding: $spacing-lg;
  }

  .info-section {
    margin-bottom: $spacing-xl;

    .info-title {
      font-size: $font-size-medium;
      font-weight: $font-weight-medium;
      margin-bottom: $spacing-md;
      padding-bottom: $spacing-sm;
      border-bottom: 2px solid $primary-color;
    }

    .info-item {
      display: flex;
      margin-bottom: $spacing-sm;

      .info-label {
        width: 100px;
        color: $text-secondary;
      }

      .info-value {
        flex: 1;
        color: $text-primary;
      }
    }
  }

  .drawer-footer {
    display: flex;
    justify-content: flex-end;
    gap: $spacing-sm;
    padding: $spacing-md;
    border-top: 1px solid $border-light;
  }
}
```

#### 4.4.4 统计卡片样式

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

    .stat-label {
      font-size: $font-size-small;
      color: $text-secondary;
      margin-bottom: $spacing-xs;
    }

    .stat-value {
      font-size: $font-size-extra-large;
      font-weight: $font-weight-bold;
      color: $primary-color;
    }

    .stat-icon {
      font-size: 32px;
      color: $info-color;
    }
  }
}
```

### 4.5 动画效果

#### 4.5.1 列表展开/收起动画

```scss
.el-table {
  .el-table__expand-icon {
    transition: transform 0.3s ease;

    &.expanded {
      transform: rotate(90deg);
    }
  }

  .el-table__body tr {
    transition: background-color 0.3s ease;

    &:hover {
      background-color: $fill-base;
    }
  }
}
```

#### 4.5.2 抽屉滑入动画

```scss
.el-drawer {
  transition: transform 0.3s ease-in-out;

  &.rtl {
    animation: slideInRight 0.3s ease-in-out;
  }
}

@keyframes slideInRight {
  from {
    transform: translateX(100%);
  }
  to {
    transform: translateX(0);
  }
}
```

#### 4.5.3 对话框淡入动画

```scss
.el-dialog {
  animation: dialogFadeIn 0.3s ease;
}

@keyframes dialogFadeIn {
  from {
    opacity: 0;
    transform: scale(0.9);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}
```

---

## 5. 响应式设计

### 5.1 断点定义

```scss
// 断点
$breakpoint-xs: 480px;   // 超小屏
$breakpoint-sm: 768px;   // 小屏
$breakpoint-md: 992px;   // 中屏
$breakpoint-lg: 1200px;  // 大屏
$breakpoint-xl: 1920px;  // 超大屏
```

### 5.2 响应式布局

#### 5.2.1 列表页响应式

```scss
.department-list {
  @media (max-width: $breakpoint-md) {
    .filter-panel {
      position: fixed;
      left: -100%;
      top: 0;
      height: 100vh;
      width: 280px;
      z-index: 1000;
      transition: left 0.3s ease;

      &.open {
        left: 0;
      }
    }
  }

  @media (max-width: $breakpoint-sm) {
    .page-header {
      flex-direction: column;

      .header-actions {
        margin-top: $spacing-sm;
        width: 100%;

        .el-button {
          flex: 1;
        }
      }
    }
  }
}
```

#### 5.2.2 表格响应式

```scss
.el-table {
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

#### 5.2.3 图表响应式

```typescript
// ECharts响应式处理
function handleResize() {
  chartInstance?.resize()
}

onMounted(() => {
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})
```

### 5.3 移动端适配

#### 5.3.1 触摸优化

```scss
// 增大点击区域
.el-button {
  @media (max-width: $breakpoint-sm) {
    min-height: 44px;
    padding: 10px 16px;
  }
}

// 优化表格行点击
.el-table__row {
  @media (max-width: $breakpoint-sm) {
    .el-button {
      padding: 8px;
      font-size: $font-size-small;
    }
  }
}
```

#### 5.3.2 移动端布局调整

```vue
<template>
  <!-- 移动端显示卡片布局 -->
  <div v-if="isMobile" class="department-cards">
    <div v-for="dept in departments" :key="dept.id" class="dept-card">
      <div class="card-header">
        <h3>{{ dept.name }}</h3>
      </div>
      <div class="card-body">
        <p>负责人: {{ dept.leader?.name }}</p>
        <p>人数: {{ dept.employeeCount }}</p>
      </div>
      <div class="card-footer">
        <el-button size="small" @click="handleView(dept)">查看</el-button>
      </div>
    </div>
  </div>

  <!-- PC端显示表格布局 -->
  <el-table v-else :data="departments">
    <!-- 表格列 -->
  </el-table>
</template>
```

---

## 6. 可访问性

### 6.1 键盘导航

```typescript
// 支持键盘快捷键
onMounted(() => {
  document.addEventListener('keydown', handleKeyboard)

  // Ctrl+N: 新建部门
  // Ctrl+F: 搜索
  // ESC: 关闭对话框
})

function handleKeyboard(e: KeyboardEvent) {
  if (e.ctrlKey && e.key === 'n') {
    e.preventDefault()
    handleAddDepartment()
  }

  if (e.key === 'Escape') {
    handleCloseDialog()
  }
}
```

### 6.2 ARIA标签

```vue
<template>
  <el-table
    aria-label="部门列表"
    :aria-busy="loading"
  >
    <el-table-column
      prop="name"
      aria-label="部门名称"
    />
  </el-table>

  <el-button
    aria-label="添加新部门"
    @click="handleAdd"
  >
    新增部门
  </el-button>
</template>
```

### 6.3 颜色对比度

```scss
// 确保文字和背景有足够的对比度
// WCAG 2.1 AA级别要求:
// - 正常文字: 对比度至少 4.5:1
// - 大文字(18pt+或14pt粗体+): 对比度至少 3:1

.high-contrast {
  color: #000000;  // 黑色文字
  background: #FFFFFF;  // 白色背景
  // 对比度: 21:1 ✓
}
```

### 6.4 焦点管理

```scss
// 焦点可见性
*:focus-visible {
  outline: 2px solid $primary-color;
  outline-offset: 2px;
}

// 移除焦点样式(鼠标点击时)
button:focus:not(:focus-visible) {
  outline: none;
}
```

---

## 7. 性能优化

### 7.1 虚拟滚动

```vue
<!-- 大数据量时使用虚拟滚动 -->
<el-table
  :data="visibleDepartments"
  :height="600"
  virtual-scroll
>
```

### 7.2 图片懒加载

```vue
<template>
  <img
    v-lazy="department.icon"
    :alt="department.name"
    class="department-icon"
  />
</template>
```

### 7.3 防抖与节流

```typescript
import { debounce, throttle } from 'lodash-es'

// 搜索防抖
const searchDepartments = debounce((keyword: string) => {
  // 执行搜索
}, 300)

// 滚动节流
const handleScroll = throttle(() => {
  // 处理滚动
}, 100)
```

---

**文档版本**: v1.0.0
**创建人**: AI开发助手
**最后更新**: 2026-01-09
