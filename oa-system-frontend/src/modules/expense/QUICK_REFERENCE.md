# ExpenseForm 组件快速参考

## 文件路径
```
c:\Users\sunru\Downloads\oa-system\oa-system-frontend\src\modules\expense\components\ExpenseForm.vue
```

## 快速使用

### 1. 导入组件
```vue
<script setup>
import ExpenseForm from '@/modules/expense/components/ExpenseForm.vue'
import { ref } from 'vue'

const formVisible = ref(false)
const currentExpense = ref(null)
</script>
```

### 2. 使用组件
```vue
<template>
  <expense-form
    v-model="formVisible"
    :expense-data="currentExpense"
    @success="handleSuccess"
  />
</template>
```

### 3. 打开表单
```typescript
// 新建模式
function openCreateForm() {
  currentExpense.value = null
  formVisible.value = true
}

// 编辑模式
function openEditForm(expense) {
  currentExpense.value = expense
  formVisible.value = true
}
```

## Props说明

| Prop | 类型 | 必填 | 说明 |
|------|------|------|------|
| modelValue | boolean | 是 | 控制对话框显示/隐藏 |
| expenseData | ExpenseRequest | 否 | 编辑的数据对象 |

## Events说明

| Event | 参数 | 说明 |
|-------|------|------|
| update:modelValue | boolean | 对话框状态变化 |
| success | - | 保存或提交成功 |

## 主要功能

### ✅ 基本信息管理
- 报销类型选择
- 费用发生日期
- 报销事由输入

### ✅ 费用明细管理
- 添加/删除明细
- 费用说明、金额、日期、分类
- 实时小计计算

### ✅ 发票管理
- 多发票上传
- 图片预览
- OCR识别
- 发票信息录入
- 实时金额合计

### ✅ 表单验证
- 必填项验证
- 格式验证
- 金额一致性检查

### ✅ 操作功能
- 保存草稿
- 提交申请
- 实时计算

## 验证规则

### 基本信息
- 报销类型: 必填
- 费用发生日期: 必填
- 报销事由: 必填,10-500字符

### 费用明细
- 费用说明: 必填,2-100字符
- 金额: 必填,大于0
- 日期: 必填
- 分类: 必填

### 发票
- 发票类型: 必填
- 发票号码: 必填,8位或20位数字
- 发票金额: 必填,大于0
- 开票日期: 必填

## 样式类名

```scss
.expense-form                    // 主容器
.section-title                   // 分区标题
.expense-items-container         // 费用明细容器
.invoice-container              // 发票容器
.total-section                  // 总金额区域
```

## 数据结构

### 表单数据
```typescript
{
  type: string                  // 报销类型
  expenseDate: string           // 费用日期
  reason: string                // 报销事由
  items: Array<{                // 费用明细
    id: number
    description: string
    amount: number
    date: string
    category: string
  }>
  invoices: Array<{             // 发票列表
    id: number
    type: string
    number: string
    amount: number
    date: string
    imageUrl: string
    fileList: any[]
    ocrLoading: boolean
  }>
}
```

## 计算属性

```typescript
itemsTotal      // 费用明细小计
invoiceTotal    // 发票总金额
totalMatch      // 金额是否匹配
```

## 关键方法

```typescript
addItem()              // 添加费用明细
removeItem(index)      // 删除费用明细
addInvoice()           // 添加发票
removeInvoice(index)   // 删除发票
handleOCR(invoice)     // OCR识别
handleSaveDraft()      // 保存草稿
handleSubmit()         // 提交申请
handleClose()          // 关闭表单
```

## 注意事项

1. ⚠️ 需要集成实际的API接口
2. ⚠️ 需要从用户store获取真实用户信息
3. ⚠️ OCR功能需要后端支持
4. ⚠️ 文件上传需要配置上传URL
5. ⚠️ 至少保留一条费用明细
6. ⚠️ 至少需要一张发票才能提交

## 相关文件

```
expense/
├── components/
│   └── ExpenseForm.vue          # 主组件 (898行)
├── types.ts                     # 类型定义
├── README.md                    # 详细文档
├── COMPONENT_SUMMARY.md         # 创建总结
└── examples/
    └── ExpenseFormExample.vue   # 使用示例
```

## 下一步

1. 集成API接口
2. 配置文件上传
3. 实现OCR功能
4. 添加权限控制
5. 完善错误处理
6. 编写单元测试

## 技术支持

查看完整文档:
- 详细文档: `README.md`
- 创建总结: `COMPONENT_SUMMARY.md`
- 使用示例: `examples/ExpenseFormExample.vue`
- 类型定义: `types.ts`
