# ExpenseForm 组件文档

## 组件概述

`ExpenseForm.vue` 是费用报销模块的核心表单组件,用于创建和编辑报销申请单。该组件实现了完整的报销流程,包括基本信息录入、费用明细管理、发票上传和OCR识别等功能。

## 功能特性

### 1. 基本信息管理
- **报销类型选择**: 支持差旅费、招待费、办公用品、交通费、通信费、其他等类型
- **费用发生日期**: 日期选择器
- **报销事由**: 多行文本输入,10-500字符限制
- **申请人信息**: 自动显示当前用户信息

### 2. 费用明细管理
- **动态添加/删除明细**: 支持添加多条费用明细
- **明细字段**:
  - 费用说明: 文本输入
  - 金额: 数字输入,精确到分
  - 日期: 日期选择
  - 分类: 交通费、住宿费、餐费、办公费、其他
- **实时计算小计**: 自动计算费用明细总金额

### 3. 发票管理
- **多发票支持**: 可添加多张发票
- **发票上传**:
  - 支持JPG/PNG/PDF格式
  - 单文件最大5MB
  - 图片预览功能
- **发票信息录入**:
  - 发票类型: 增值税专用发票、增值税普通发票、电子发票等
  - 发票号码: 8位或20位数字验证
  - 发票金额: 数字输入
  - 开票日期: 日期选择
- **OCR识别**: 一键识别发票信息,自动填充表单
- **实时计算发票总金额**

### 4. 金额验证
- **金额一致性检查**: 自动对比费用明细与发票总金额
- **差异提示**: 不一致时显示警告信息
- **提交确认**: 金额差异时需要确认才能提交

### 5. 表单验证
- **实时验证**: 失焦时触发验证
- **必填项检查**: 所有必填字段的完整验证
- **格式验证**: 发票号码、金额格式等
- **自定义验证规则**:
  - 报销事由: 10-500字符
  - 发票号码: 正则验证
  - 金额: 必须大于0

### 6. 草稿与提交
- **保存草稿**: 保存未完成的报销单
- **提交申请**: 完整验证后提交审批
- **加载状态**: 提交过程中显示加载动画

## 使用方法

### 基础用法

```vue
<template>
  <expense-form
    v-model="formVisible"
    :expense-data="currentExpense"
    @success="handleSuccess"
  />
</template>

<script setup lang="ts">
import { ref } from 'vue'
import ExpenseForm from '@/modules/expense/components/ExpenseForm.vue'

const formVisible = ref(false)
const currentExpense = ref(null)

function handleSuccess() {
  // 刷新列表
  loadData()
}
</script>
```

### Props

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| modelValue | boolean | - | 控制对话框显示/隐藏 |
| expenseData | ExpenseRequest \| null | null | 编辑模式下的报销数据 |

### Events

| 事件名 | 参数 | 说明 |
|--------|------|------|
| update:modelValue | (value: boolean) | 对话框显示状态变化时触发 |
| success | - | 保存或提交成功后触发 |

## 数据结构

### ExpenseForm

```typescript
interface ExpenseForm {
  type: ExpenseType              // 报销类型
  expenseDate: string            // 费用发生日期
  reason: string                 // 报销事由
  items: ExpenseItem[]           // 费用明细
  invoices: Invoice[]            // 发票列表
}
```

### ExpenseItem

```typescript
interface ExpenseItem {
  id: number                     // 明项ID
  description: string            // 费用说明
  amount: number                 // 金额
  date: string                   // 日期
  category: ExpenseCategory      // 分类
}
```

### Invoice

```typescript
interface Invoice {
  id: number                     // 发票ID
  type: InvoiceType              // 发票类型
  number: string                 // 发票号码
  amount: number                 // 发票金额
  date: string                   // 开票日期
  imageUrl: string               // 发票图片URL
  fileList: any[]                // 文件列表
  ocrLoading: boolean            // OCR识别状态
}
```

## 表单验证规则

### 基本信息验证

| 字段 | 规则 |
|------|------|
| type | 必填 |
| expenseDate | 必填 |
| reason | 必填, 10-500字符 |

### 费用明细验证

| 字段 | 规则 |
|------|------|
| description | 必填, 2-100字符 |
| amount | 必填, 大于0 |
| date | 必填 |
| category | 必填 |

### 发票验证

| 字段 | 规则 |
|------|------|
| type | 必填 |
| number | 必填, 8位或20位数字 |
| amount | 必填, 大于0 |
| date | 必填 |

## 样式定制

组件使用了以下Element Plus样式覆盖:

```scss
// 表格样式
:deep(.el-table) {
  .el-input-number .el-input__inner {
    text-align: left;
  }
}

// 上传组件样式
:deep(.el-upload--picture-card) {
  width: 100px;
  height: 100px;
}

// 分割线样式
:deep(.el-divider__text) {
  background-color: transparent;
}
```

## API集成

组件中预留了以下API调用点,需要根据实际后端接口实现:

### 1. 保存草稿

```typescript
// TODO: 替换为实际API调用
await expenseApi.saveDraft(form)
```

### 2. 提交申请

```typescript
// TODO: 替换为实际API调用
await expenseApi.submit(form)
```

### 3. OCR识别

```typescript
// TODO: 替换为实际API调用
const result = await ocrApi.recognize(invoice.imageUrl)
invoice.number = result.invoiceNumber
invoice.amount = result.amount
```

### 4. 文件上传

```typescript
// TODO: 配置实际的上传地址
const uploadUrl = '/api/upload'
```

## 注意事项

1. **用户信息**: 当前使用模拟数据,需要从用户store获取真实用户信息
2. **API集成**: 所有API调用都需要替换为实际的后端接口
3. **文件上传**: 需要配置正确的上传URL和处理逻辑
4. **OCR功能**: OCR识别功能需要后端支持
5. **权限控制**: 根据实际需求添加编辑/删除权限控制
6. **数据回显**: 编辑模式需要正确回显所有表单数据和图片

## 依赖组件

- Element Plus: UI组件库
- Vue 3 Composition API: 响应式API
- TypeScript: 类型支持

## 后续优化建议

1. **性能优化**:
   - 大量发票时考虑虚拟滚动
   - 图片懒加载优化

2. **功能增强**:
   - 支持发票批量上传
   - 添加常用报销模板
   - 支持费用明细导入Excel

3. **用户体验**:
   - 添加表单自动保存功能
   - 优化移动端显示
   - 添加更多提示和帮助信息

4. **数据验证**:
   - 添加发票去重检查
   - 添加金额合理性检查
   - 添加日期逻辑验证

## 版本历史

- v1.0.0 (2026-01-10): 初始版本
