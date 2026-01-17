# 修复归还对话框中 asset 属性访问错误

## 问题描述

点击【确认归还】时，前端报错：
```
can't access property "asset", result is undefined
```

同时存在 TypeScript 类型错误：
```
Argument of type '{ actualReturnDate: string; notes?: string | undefined; }' is not assignable to parameter of type '{ condition: "good" | "damaged" | "lost"; notes?: string | undefined; }'.
Property 'condition' is missing in type '{ actualReturnDate: string; notes?: string | undefined; }' but required in type '{ condition: "good" | "damaged" | "lost"; notes?: string | undefined; }'.
```

## 根本原因

### 问题 1: Props 在异步操作中的生命周期问题

与 BorrowDialog 相同的问题：
- `asset` 是可选属性（`asset?: Asset | null`）
- 在对话框打开和提交之间，可能存在状态变化
- Vue 3 的 props 可能在某些情况下变为 `undefined`
- 直接使用 `props.asset.id` 会导致错误

### 问题 2: Store 函数签名类型不匹配

**returnAsset 函数：**
Store 中的 `returnAsset` 函数仍使用旧的类型定义：
```typescript
// 旧的（错误）
async function returnAsset(id: string, data: { condition: 'good' | 'damaged' | 'lost'; notes?: string })

// 新的（正确）
async function returnAsset(id: string, data: ReturnForm)
```

**borrow 函数：**
Store 中的 `borrow` 函数缺少 `borrowDate` 字段：
```typescript
// 旧的（错误）
async function borrow(id: string, data: { borrowerId: string; expectedReturnDate: string; notes?: string })

// 新的（正确）
async function borrow(id: string, data: BorrowForm)
```

这导致 TypeScript 类型检查失败，前端调用时传递了 `borrowDate` 字段，但函数签名中没有声明该字段。

## 解决方案

### 1. 使用本地 ref 保存资产引用

**添加 `currentAsset` ref：**
```typescript
// 本地存储的资产引用（避免props.asset在异步操作中变为undefined）
const currentAsset = ref<Asset | null>(null)
```

**更新 watch 监听器：**
```typescript
watch(
  () => props.modelValue,
  (val) => {
    visible.value = val
    if (val) {
      // 对话框打开时，保存资产引用
      currentAsset.value = props.asset || null
    } else {
      // 对话框关闭时清空
      currentAsset.value = null
      resetForm()
    }
  }
)
```

**更新 handleSubmit 函数：**
```typescript
async function handleSubmit() {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return
    if (!currentAsset.value) {
      ElMessage.error('资产信息不存在')
      return
    }

    loading.value = true
    try {
      await assetStore.returnAsset(currentAsset.value.id, form)
      ElMessage.success('归还成功')
      emit('success')
      handleClose()
    } catch (error: any) {
      ElMessage.error(error.message || '归还失败')
    } finally {
      loading.value = false
    }
  })
}
```

### 2. 修复 Store 函数签名

**添加 `ReturnForm` 导入：**
```typescript
import type {
  Asset,
  AssetFilter,
  AssetForm,
  BorrowRecord,
  ReturnForm,  // ✅ 添加
  AssetStatistics
} from '../types'
```

**更新函数签名：**
```typescript
// 之前
async function returnAsset(id: string, data: { condition: 'good' | 'damaged' | 'lost'; notes?: string })

// 之后
async function returnAsset(id: string, data: ReturnForm)
```

## 修改内容

### ReturnDialog.vue

| 行号 | 修改内容 |
|------|---------|
| 128 | 添加 `currentAsset` ref |
| 143-150 | 更新 watch 监听器，在对话框打开时保存资产引用 |
| 168-171 | 更新 `handleSubmit` 函数，使用 `currentAsset` 替代 `props.asset` |
| 8 | 模板已更新，使用 `currentAsset` 替代 `asset` |

### store/index.ts

| 行号 | 修改内容 |
|------|---------|
| 13 | 添加 `ReturnForm` 类型导入 |
| 172 | 更新 `returnAsset` 函数签名为 `ReturnForm` 类型 |

## 测试验证

### 测试步骤

1. 打开资产管理页面
2. 选择一个"已借出"状态的资产
3. 点击【归还】按钮
4. 检查对话框是否正确显示资产信息
5. 填写归还日期和备注
6. 点击【确认归还】
7. 确认不再出现 `asset is undefined` 错误
8. 确认不再出现 TypeScript 类型错误

### 预期结果

✅ 对话框正确显示资产信息
✅ 所有字段都能正常访问
✅ 点击【确认归还】不再报错
✅ TypeScript 类型检查通过
✅ 成功调用后端 API
✅ 资产状态更新为"在库"

## 相关文件修改

| 文件 | 修改内容 |
|------|---------|
| `ReturnDialog.vue` | ✅ 添加 `currentAsset` ref |
| `ReturnDialog.vue` | ✅ 更新 `watch` 监听器 |
| `ReturnDialog.vue` | ✅ 更新 `handleSubmit` 函数 |
| `store/index.ts` | ✅ 添加 `ReturnForm` 类型导入 |
| `store/index.ts` | ✅ 更新 `returnAsset` 函数签名 |
| `store/index.ts` | ✅ 添加 `BorrowForm` 类型导入 |
| `store/index.ts` | ✅ 更新 `borrow` 函数签名（添加 `borrowDate` 字段） |

## 技术要点

### Vue 3 Props 生命周期

```typescript
// ❌ 错误做法：直接使用 props
async function handleSubmit() {
  if (!props.asset) return  // 可能在异步操作后变为 undefined
  await api.returnAsset(props.asset.id, form)
}

// ✅ 正确做法：使用本地 ref
const currentAsset = ref<Asset | null>(null)

watch(() => props.modelValue, (val) => {
  if (val) {
    currentAsset.value = props.asset || null  // 打开时立即保存
  }
})

async function handleSubmit() {
  if (!currentAsset.value) return  // 稳定可靠
  await api.returnAsset(currentAsset.value.id, form)
}
```

### TypeScript 类型一致性

确保前后端类型定义一致：

**前端类型 (types/index.ts)：**
```typescript
export interface ReturnForm {
  actualReturnDate: string // 实际归还日期
  notes?: string
}
```

**Store 函数 (store/index.ts)：**
```typescript
async function returnAsset(id: string, data: ReturnForm)
```

**组件使用 (ReturnDialog.vue)：**
```typescript
const form = reactive<ReturnForm>({
  actualReturnDate: new Date().toISOString().split('T')[0],
  notes: ''
})
```

## 前后端联调状态

### 已完成的修复

1. ✅ BorrowDialog.vue - asset undefined 错误
2. ✅ ReturnDialog.vue - asset undefined 错误
3. ✅ ReturnDialog.vue - TypeScript 类型错误
4. ✅ Store 类型定义与 API 类型一致

### 借还流程完整性

```
[在库资产] → 点击【借出】 → BorrowDialog → 确认借出 → [已借出]
                                                      ↓
[在库资产】 ← 点击【归还】 ← ReturnDialog ← 确认归还 ← [已借出]
```

✅ 借出流程：已修复
✅ 归还流程：已修复
✅ 类型安全：已确保

---

**修复时间**: 2026-01-17
**状态**: ✅ 已完成
**相关文档**: [ASSET_UNDEFINED_FIX.md](./ASSET_UNDEFINED_FIX.md)
