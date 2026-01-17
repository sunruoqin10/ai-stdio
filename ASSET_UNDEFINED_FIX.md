# 修复借用对话框中 asset 属性访问错误

## 问题描述

点击【确认借出】时，前端报错：
```
can't access property "asset", result is undefined
```

## 根本原因

### Props 在异步操作中的生命周期问题

**原始代码：**
```vue
<script setup>
const props = defineProps<{
  modelValue: boolean
  asset?: Asset | null  // 可选属性
}>()

async function handleSubmit() {
  // ...
  if (!props.asset) return  // ❌ props.asset 可能在异步操作中变为 undefined
  await assetStore.borrow(props.asset.id, form)
}
</script>
```

**问题分析：**
1. `asset` 是可选属性（`asset?: Asset | null`）
2. 在对话框打开和提交之间，可能存在状态变化
3. Vue 3 的 props 可能在某些情况下变为 `undefined`
4. 直接使用 `props.asset.id` 会导致错误

## 解决方案

### 使用本地 ref 保存资产引用

**核心思想：**
- 在对话框打开时（`modelValue` 变为 `true`），立即保存 `props.asset` 到本地 ref
- 所有后续操作都使用本地 ref，避免访问可能变化的 props

**修复后的代码：**

```vue
<script setup>
const props = defineProps<{
  modelValue: boolean
  asset?: Asset | null
}>()

// 本地存储的资产引用（避免props.asset在异步操作中变为undefined）
const currentAsset = ref<Asset | null>(null)

// 监听对话框打开事件
watch(
  () => props.modelValue,
  (val) => {
    if (val) {
      // ✅ 对话框打开时，立即保存资产引用
      currentAsset.value = props.asset || null
    } else {
      // 对话框关闭时清空
      currentAsset.value = null
      resetForm()
    }
  }
)

async function handleSubmit() {
  // ...
  // ✅ 使用本地 ref 而不是 props
  if (!currentAsset.value) {
    ElMessage.error('资产信息不存在')
    return
  }

  await assetStore.borrow(currentAsset.value.id, form)
}
</script>

<template>
  <!-- ✅ 使用 currentAsset 而不是 props.asset -->
  <div v-if="currentAsset" class="borrow-dialog">
    <div class="asset-name">{{ currentAsset.name }}</div>
    <!-- ... -->
  </div>
</template>
```

## 修改内容

### 1. 添加本地 ref

```typescript
const currentAsset = ref<Asset | null>(null)
```

### 2. 更新 watch 监听器

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

### 3. 更新模板

```vue
<!-- 之前 -->
<div v-if="asset" class="borrow-dialog">
  <div class="asset-name">{{ asset.name }}</div>
</div>

<!-- 之后 -->
<div v-if="currentAsset" class="borrow-dialog">
  <div class="asset-name">{{ currentAsset.name }}</div>
</div>
```

### 4. 更新 handleSubmit 函数

```typescript
async function handleSubmit() {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return
    if (!currentAsset.value) {  // ✅ 使用本地 ref
      ElMessage.error('资产信息不存在')
      return
    }

    loading.value = true
    try {
      await assetStore.borrow(currentAsset.value.id, form)  // ✅ 使用本地 ref
      ElMessage.success('借出成功')
      emit('success')
      handleClose()
    } catch (error: any) {
      ElMessage.error(error.message || '借出失败')
    } finally {
      loading.value = false
    }
  })
}
```

## 为什么这样修复

### Props vs 本地 Ref

| 特性 | Props | 本地 Ref |
|------|-------|----------|
| 来源 | 父组件传递 | 组件内部定义 |
| 只读性 | ✅ 只读 | ✅ 可读写 |
| 响应式 | ✅ 响应式 | ✅ 响应式 |
| 生命周期 | 受父组件影响 | 完全控制 |
| 稳定性 | 可能变化 | 稳定可靠 |

### 关键优势

1. **避免异步问题**：在对话框打开时立即保存引用，避免后续变化
2. **更好的错误处理**：可以检查 `currentAsset.value` 是否存在
3. **代码更清晰**：不需要每次访问 `props.asset`
4. **性能优化**：避免重复访问 props 属性

## 测试验证

### 测试步骤

1. 打开资产管理页面
2. 选择一个"在库"状态的资产
3. 点击【借出】按钮
4. 检查对话框是否正确显示资产信息
5. 填写表单
6. 点击【确认借出】
7. 确认不再出现 `asset is undefined` 错误

### 预期结果

✅ 对话框正确显示资产信息
✅ 所有字段都能正常访问
✅ 点击【确认借出】不再报错
✅ 成功调用后端 API

## 相关文件修改

| 文件 | 修改内容 |
|------|---------|
| `BorrowDialog.vue` | ✅ 添加 `currentAsset` ref |
| `BorrowDialog.vue` | ✅ 更新 `watch` 监听器 |
| `BorrowDialog.vue` | ✅ 更新模板中的资产引用 |
| `BorrowDialog.vue` | ✅ 更新 `handleSubmit` 函数 |

## 最佳实践

### Vue 3 组件通信建议

1. **Props 向下传递**：父组件 → 子组件
2. **Events 向上传递**：子组件 → 父组件
3. **本地状态管理**：组件内部状态使用 ref
4. **避免直接访问 props**：在复杂逻辑中应先保存到本地 ref

### 对话框组件模式

```typescript
// 推荐模式
const currentData = ref(null)

watch(() => props.modelValue, (val) => {
  if (val) {
    currentData.value = props.data  // 打开时保存
  } else {
    currentData.value = null       // 关闭时清空
  }
})
```

---

**修复时间**: 2026-01-17
**状态**: ✅ 已完成
