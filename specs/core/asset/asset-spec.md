# 资产管理模块开发规范

> **模块类型**: 核心基础
> **复杂度**: ⭐⭐⭐ (3星)
> **预计工期**: 1-1.5天 (AI辅助)
> **参考模块**: `src/modules/employee/`
> **创建日期**: 2026-01-09

---

## 1. 功能概述

管理公司的固定资产、办公设备等资源,支持资产的入库、借用、归还、维修、报废等全生命周期管理。

## 2. 核心功能

### 2.1 多视图展示
- **表格视图**: el-table展示所有资产
- **看板视图**: 按状态分组(库存中/使用中/借出/维修中/报废),支持拖拽
- **画廊视图**: 卡片网格布局

### 2.2 资产CRUD
- 新增资产(自动生成资产编号)
- 编辑资产信息
- 删除资产(二次确认)
- 批量导入

### 2.3 借还管理
- 借出资产(选择借用人、借出日期、预计归还日期)
- 归还资产
- 到期提醒(借出前3天自动提醒)

### 2.4 统计分析
- ECharts饼图: 资产分类占比
- ECharts柱状图: 月度折旧趋势
- ECharts折线图: 借出趋势

## 3. 数据结构

```typescript
interface Asset {
  id: string                    // 资产编号: ASSET+序号
  name: string                  // 资产名称
  category: string              // 类别: electronic/furniture/book/other
  brandModel?: string           // 品牌/型号
  purchaseDate: string          // 购置日期
  purchasePrice: number         // 购置金额
  currentValue?: number         // 当前价值(自动计算)
  status: 'stock' | 'in_use' | 'borrowed' | 'maintenance' | 'scrapped'
  userId?: string               // 使用人/保管人ID
  location?: string             // 存放位置
  borrowDate?: string           // 借出日期
  expectedReturnDate?: string   // 预计归还日期
  maintenanceRecord?: string    // 维护记录
  images?: string[]             // 资产图片
  createdAt: string
  updatedAt: string
}

interface AssetFilter {
  keyword?: string
  category?: string
  status?: string
  userId?: string
}
```

## 4. 自动计算规则

```typescript
// 当前价值 = 购置金额 × (1 - 月折旧率 × 月数)
function calculateCurrentValue(
  purchasePrice: number,
  purchaseDate: string,
  depreciationRate: number = 0.01  // 月折旧率1%
): number {
  const months = getMonthsDiff(purchaseDate, new Date())
  const currentValue = purchasePrice * (1 - (depreciationRate * months) / 12)
  return Math.max(0, Math.round(currentValue * 100) / 100)
}

// 到期提醒
function checkReturnReminder(asset: Asset): boolean {
  if (!asset.expectedReturnDate) return false
  const daysUntilDue = getDaysDiff(new Date(), asset.expectedReturnDate)
  return daysUntilDue <= 3 && daysUntilDue >= 0
}
```

## 5. 看板拖拽实现

```vue
<script setup lang="ts">
import { ref } from 'vue'
import VueDraggable from 'vuedraggable'

const columns = [
  { key: 'stock', title: '库存中' },
  { key: 'in_use', title: '使用中' },
  { key: 'borrowed', title: '已借出' },
  { key: 'maintenance', title: '维修中' }
]

const kanbanData = ref({
  stock: [],
  in_use: [],
  borrowed: [],
  maintenance: []
})

function onDragEnd(event: any) {
  // 更新资产状态
  const { item, to, from } = event
  const asset = item.dataset
  const newStatus = to.dataset.status

  updateAssetStatus(asset.id, newStatus)
}
</script>

<template>
  <div class="asset-kanban">
    <div
      v-for="column in columns"
      :key="column.key"
      class="kanban-column"
      :data-status="column.key"
    >
      <div class="column-header">{{ column.title }}</div>
      <VueDraggable
        :model-value="kanbanData[column.key]"
        group="assets"
        @end="onDragEnd"
      >
        <div v-for="asset in kanbanData[column.key]" :key="asset.id" class="asset-card">
          {{ asset.name }}
        </div>
      </VueDraggable>
    </div>
  </div>
</template>
```

## 6. ECharts图表配置

```typescript
// 饼图 - 资产分类
const pieOption = {
  tooltip: { trigger: 'item' },
  legend: { orient: 'vertical', left: 'left' },
  series: [{
    type: 'pie',
    radius: '50%',
    data: [
      { value: electronicCount, name: '电子设备' },
      { value: furnitureCount, name: '办公家具' },
      { value: bookCount, name: '图书资料' },
      { value: otherCount, name: '其他' }
    ]
  }]
}

// 柱状图 - 月度折旧
const barOption = {
  xAxis: { type: 'category', data: months },
  yAxis: { type: 'value' },
  series: [{
    type: 'bar',
    data: depreciationData,
    itemStyle: { color: '#1890FF' }
  }]
}
```

---

**文档版本**: v1.0.0
