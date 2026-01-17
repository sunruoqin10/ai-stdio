# 资产管理模块前后端联调问题修复总结

## 问题描述

前端调用后端借用资产API时出现验证错误：
```
Field error in object 'assetBorrowRequest' on field 'borrowDate':
rejected value [null];
借出日期不能为空
```

## 根本原因

前端和后端的表单字段不匹配：

### 后端要求字段
- **AssetBorrowRequest**:
  - `borrowerId`: String (必填)
  - `borrowDate`: LocalDate (必填) ⚠️ 前端缺失
  - `expectedReturnDate`: LocalDate (必填)
  - `notes`: String (可选)

- **AssetReturnRequest**:
  - `actualReturnDate`: LocalDate (必填) ⚠️ 前端字段不对
  - `notes`: String (可选)

### 前端原有字段
- **BorrowForm**:
  - `borrowerId`: String
  - `expectedReturnDate`: String
  - `notes`: String
  - ❌ **缺少 `borrowDate` 字段**

- **ReturnForm**:
  - `condition`: 'good' | 'damaged' | 'lost' ⚠️ 后端不需要
  - `notes`: String
  - ❌ **缺少 `actualReturnDate` 字段**

## 修复方案

### 1. 更新类型定义 (`types/index.ts`)

```typescript
// ✅ 修复后的 BorrowForm
export interface BorrowForm {
  borrowerId: string
  borrowDate: string    // 新增：借出日期
  expectedReturnDate: string
  notes?: string
}

// ✅ 修复后的 ReturnForm
export interface ReturnForm {
  actualReturnDate: string  // 替换 condition 字段
  notes?: string
}
```

### 2. 更新 BorrowDialog 组件

**新增借出日期字段**:
```vue
<el-form-item label="借出日期" prop="borrowDate">
  <el-date-picker
    v-model="form.borrowDate"
    type="date"
    placeholder="选择借出日期"
    :disabled-date="disabledBorrowDate"
    value-format="YYYY-MM-DD"
  />
</el-form-item>
```

**添加日期验证逻辑**:
```typescript
// 借出日期不能晚于今天
function disabledBorrowDate(time: Date) {
  return time.getTime() > Date.now()
}

// 归还日期不能早于借出日期
function disabledDate(time: Date) {
  if (form.borrowDate) {
    const borrowDateTime = new Date(form.borrowDate).getTime()
    return time.getTime() < borrowDateTime
  }
  return time.getTime() < Date.now() - 24 * 60 * 60 * 1000
}
```

**设置默认值**:
```typescript
const form = reactive<BorrowForm>({
  borrowerId: '',
  borrowDate: new Date().toISOString().split('T')[0] || '', // 默认今天
  expectedReturnDate: '',
  notes: ''
})
```

### 3. 更新 ReturnDialog 组件

**移除资产状态选择，改为归还日期选择**:
```vue
<!-- 移除了 condition 单选框 -->
<el-form-item label="归还日期" prop="actualReturnDate">
  <el-date-picker
    v-model="form.actualReturnDate"
    type="date"
    placeholder="选择归还日期"
    :disabled-date="disabledReturnDate"
    value-format="YYYY-MM-DD"
  />
</el-form-item>
```

**更新表单数据和验证规则**:
```typescript
const form = reactive<ReturnForm>({
  actualReturnDate: new Date().toISOString().split('T')[0] || '', // 默认今天
  notes: ''
})

const rules: FormRules = {
  actualReturnDate: [{ required: true, message: '请选择归还日期', trigger: 'change' }]
}

// 归还日期不能晚于今天
function disabledReturnDate(time: Date) {
  return time.getTime() > Date.now()
}
```

## 测试要点

### ✅ 借用资产流程
1. 选择一个"在库"状态的资产
2. 点击"借用"按钮
3. 填写表单：
   - 选择借用人
   - 选择借出日期（默认今天，不可选未来日期）
   - 选择预计归还日期（必须晚于借出日期）
   - 填写备注（可选）
4. 提交后检查：
   - 资产状态变为"借出"
   - 创建借用记录

### ✅ 归还资产流程
1. 选择一个"借出"状态的资产
2. 点击"归还"按钮
3. 填写表单：
   - 选择归还日期（默认今天，不可选未来日期）
   - 填写备注（可选）
4. 提交后检查：
   - 资产状态变为"在库"
   - 借用记录状态更新为"已归还"
   - 记录实际归还日期

## 文件修改清单

| 文件 | 修改内容 |
|------|---------|
| `types/index.ts` | 更新 BorrowForm 和 ReturnForm 接口定义 |
| `components/BorrowDialog.vue` | 添加借出日期字段，新增日期验证逻辑 |
| `components/ReturnDialog.vue` | 移除资产状态选择，添加归还日期字段 |

## 后续建议

1. **添加日期范围验证**: 借用期限不超过90天
2. **显示借用天数**: 选择归还日期后自动计算借用天数
3. **逾期提醒**: 归还日期接近时显示警告
4. **历史记录优化**: 在借用历史中显示实际借用天数

---

**修复时间**: 2026-01-17
**状态**: ✅ 已完成修复
