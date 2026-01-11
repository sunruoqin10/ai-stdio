# 资产管理模块 - UI/UX规范

> **文档类型**: UI/UX设计规范
> **模块**: 资产管理
> **创建日期**: 2026-01-09
> **版本**: v1.0.0

---

## 1. 页面布局

### 1.1 整体布局结构

```
┌─────────────────────────────────────────────────────────────┐
│  顶部导航栏                                                  │
├─────────────────────────────────────────────────────────────┤
│  左侧菜单 │  内容区域                                        │
│         │  ┌──────────────────────────────────────────────┐ │
│  - 资产  │  │  操作栏 + 筛选区                             │ │
│  - 分类  │  ├──────────────────────────────────────────────┤ │
│  - 统计  │  │  视图切换 + 主要内容区                       │ │
│         │  │  (表格/看板/画廊)                             │ │
│         │  └──────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

### 1.2 操作栏布局

```
┌─────────────────────────────────────────────────────────────┐
│  [+ 新增资产]  [批量导入]  [导出]    │  [🔍 搜索框]          │
│                                     │  [筛选] [刷新]       │
└─────────────────────────────────────────────────────────────┘
```

### 1.3 筛选区布局

```vue
<template>
  <el-form :inline="true" class="filter-form">
    <el-form-item label="关键字">
      <el-input
        v-model="filter.keyword"
        placeholder="搜索资产名称/编号"
        clearable
        style="width: 200px"
      />
    </el-form-item>

    <el-form-item label="类别">
      <el-select v-model="filter.category" placeholder="全部" clearable>
        <el-option label="电子设备" value="electronic" />
        <el-option label="办公家具" value="furniture" />
        <el-option label="图书资料" value="book" />
        <el-option label="其他" value="other" />
      </el-select>
    </el-form-item>

    <el-form-item label="状态">
      <el-select v-model="filter.status" placeholder="全部" clearable>
        <el-option label="库存中" value="stock" />
        <el-option label="使用中" value="in_use" />
        <el-option label="已借出" value="borrowed" />
        <el-option label="维修中" value="maintenance" />
        <el-option label="已报废" value="scrapped" />
      </el-select>
    </el-form-item>

    <el-form-item label="使用人">
      <el-select
        v-model="filter.userId"
        placeholder="全部"
        clearable
        filterable
        remote
        :remote-method="searchUsers"
      >
        <el-option
          v-for="user in users"
          :key="user.id"
          :label="user.name"
          :value="user.id"
        />
      </el-select>
    </el-form-item>

    <el-form-item>
      <el-button type="primary" @click="handleSearch">查询</el-button>
      <el-button @click="handleReset">重置</el-button>
    </el-form-item>
  </el-form>
</template>
```

---

## 2. 三种视图设计

### 2.1 表格视图 (Table View)

#### 组件选择
- **主组件**: `el-table` (Element Plus)
- **分页组件**: `el-pagination`
- **操作列**: 自定义列模板

#### 布局实现

```vue
<template>
  <div class="table-view">
    <el-table
      :data="assets"
      stripe
      border
      v-loading="loading"
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="55" />

      <el-table-column prop="id" label="资产编号" width="130" />

      <el-table-column label="资产信息" min-width="200">
        <template #default="{ row }">
          <div class="asset-info">
            <el-image
              v-if="row.images?.length"
              :src="row.images[0]"
              :preview-src-list="row.images"
              fit="cover"
              class="asset-thumb"
            />
            <div class="asset-details">
              <div class="asset-name">{{ row.name }}</div>
              <div class="asset-category">{{ getCategoryName(row.category) }}</div>
            </div>
          </div>
        </template>
      </el-table-column>

      <el-table-column prop="brandModel" label="品牌/型号" width="150" />

      <el-table-column label="购置信息" width="180">
        <template #default="{ row }">
          <div>日期: {{ formatDate(row.purchaseDate) }}</div>
          <div>价格: ¥{{ row.purchasePrice }}</div>
          <div class="text-muted">现值: ¥{{ row.currentValue }}</div>
        </template>
      </el-table-column>

      <el-table-column prop="location" label="存放位置" width="150" />

      <el-table-column label="状态" width="120">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)">
            {{ getStatusName(row.status) }}
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column label="使用人" width="120">
        <template #default="{ row }">
          {{ row.userName || '-' }}
        </template>
      </el-table-column>

      <el-table-column label="借出信息" width="180">
        <template #default="{ row }">
          <div v-if="row.status === 'borrowed'">
            <div>借出: {{ formatDate(row.borrowDate) }}</div>
            <div class="text-warning">
              应还: {{ formatDate(row.expectedReturnDate) }}
            </div>
          </div>
          <span v-else>-</span>
        </template>
      </el-table-column>

      <el-table-column label="操作" width="240" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleView(row)">
            查看
          </el-button>
          <el-button link type="primary" @click="handleEdit(row)">
            编辑
          </el-button>
          <el-button
            v-if="row.status === 'stock'"
            link
            type="success"
            @click="handleBorrow(row)"
          >
            借出
          </el-button>
          <el-button
            v-if="row.status === 'borrowed'"
            link
            type="warning"
            @click="handleReturn(row)"
          >
            归还
          </el-button>
          <el-button link type="danger" @click="handleDelete(row)">
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-model:current-page="pagination.page"
      v-model:page-size="pagination.pageSize"
      :total="pagination.total"
      :page-sizes="[10, 20, 50, 100]"
      layout="total, sizes, prev, pager, next, jumper"
      @size-change="handleSizeChange"
      @current-change="handlePageChange"
    />
  </div>
</template>

<style scoped>
.asset-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.asset-thumb {
  width: 50px;
  height: 50px;
  border-radius: 4px;
}

.asset-name {
  font-weight: 500;
  margin-bottom: 4px;
}

.asset-category {
  font-size: 12px;
  color: #999;
}

.text-muted {
  color: #999;
  font-size: 12px;
}

.text-warning {
  color: #e6a23c;
  font-size: 12px;
}
</style>
```

### 2.2 看板视图 (Kanban View)

#### 组件选择
- **主组件**: `vuedraggable` (拖拽库)
- **卡片组件**: `el-card`
- **列布局**: Flexbox/Grid

#### 布局实现

```vue
<template>
  <div class="kanban-view">
    <div
      v-for="column in kanbanColumns"
      :key="column.key"
      class="kanban-column"
      :class="`column-${column.key}`"
    >
      <!-- 列头 -->
      <div class="column-header">
        <div class="column-title">
          <span class="column-dot" :style="{ backgroundColor: column.color }" />
          {{ column.title }}
          <span class="column-count">{{ kanbanData[column.key].length }}</span>
        </div>
        <el-button
          link
          :icon="Plus"
          @click="handleAddAsset(column.key)"
        />
      </div>

      <!-- 拖拽区域 -->
      <VueDraggable
        v-model="kanbanData[column.key]"
        group="assets"
        :animation="300"
        ghost-class="ghost-card"
        drag-class="dragging-card"
        @end="(event) => handleDragEnd(event, column.key)"
      >
        <div
          v-for="asset in kanbanData[column.key]"
          :key="asset.id"
          class="asset-card"
          @click="handleView(asset)"
        >
          <!-- 卡片图片 -->
          <div class="card-image" v-if="asset.images?.length">
            <el-image
              :src="asset.images[0]"
              fit="cover"
              class="card-img"
            />
          </div>

          <!-- 卡片内容 -->
          <div class="card-content">
            <div class="card-title">{{ asset.name }}</div>
            <div class="card-meta">
              <span>{{ asset.brandModel }}</span>
            </div>
            <div class="card-price">
              ¥{{ asset.purchasePrice }}
            </div>
          </div>

          <!-- 卡片底部 -->
          <div class="card-footer">
            <div v-if="asset.status === 'borrowed'" class="due-date">
              <el-icon><Clock /></el-icon>
              {{ formatDate(asset.expectedReturnDate) }}
            </div>
            <div v-if="asset.userName" class="user-info">
              <el-avatar :size="24" :src="asset.userAvatar" />
              {{ asset.userName }}
            </div>
          </div>

          <!-- 逾期标记 -->
          <div v-if="isOverdue(asset)" class="overdue-badge">
            逾期
          </div>
        </div>
      </VueDraggable>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import VueDraggable from 'vuedraggable'
import { Plus, Clock } from '@element-plus/icons-vue'

const kanbanColumns = [
  { key: 'stock', title: '库存中', color: '#67C23A' },
  { key: 'in_use', title: '使用中', color: '#409EFF' },
  { key: 'borrowed', title: '已借出', color: '#E6A23C' },
  { key: 'maintenance', title: '维修中', color: '#F56C6C' }
]

const kanbanData = ref({
  stock: [],
  in_use: [],
  borrowed: [],
  maintenance: []
})

function handleDragEnd(event: any, targetColumn: string) {
  const { item, newIndex, oldIndex, from } = event

  if (newIndex === oldIndex && from === targetColumn) {
    return // 没有移动
  }

  const asset = item.dataset.asset
  const newStatus = targetColumn

  updateAssetStatus(asset.id, newStatus)
}
</script>

<style scoped>
.kanban-view {
  display: flex;
  gap: 20px;
  height: calc(100vh - 300px);
  overflow-x: auto;
  padding: 20px 0;
}

.kanban-column {
  flex: 1;
  min-width: 280px;
  max-width: 350px;
  background: #f5f7fa;
  border-radius: 8px;
  padding: 16px;
  display: flex;
  flex-direction: column;
}

.column-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding: 0 8px;
}

.column-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  font-size: 15px;
}

.column-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.column-count {
  font-size: 13px;
  color: #999;
  font-weight: normal;
}

.asset-card {
  background: white;
  border-radius: 8px;
  padding: 12px;
  margin-bottom: 12px;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
  position: relative;
}

.asset-card:hover {
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
  transform: translateY(-2px);
}

.ghost-card {
  opacity: 0.5;
  background: #e0e0e0;
}

.dragging-card {
  cursor: grabbing;
}

.card-image {
  width: 100%;
  height: 140px;
  margin-bottom: 12px;
  border-radius: 6px;
  overflow: hidden;
}

.card-img {
  width: 100%;
  height: 100%;
}

.card-content {
  margin-bottom: 12px;
}

.card-title {
  font-weight: 500;
  font-size: 14px;
  margin-bottom: 6px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card-meta {
  font-size: 12px;
  color: #666;
  margin-bottom: 4px;
}

.card-price {
  font-weight: 600;
  color: #409EFF;
  font-size: 16px;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  padding-top: 8px;
  border-top: 1px solid #eee;
}

.due-date {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #E6A23C;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #666;
}

.overdue-badge {
  position: absolute;
  top: 8px;
  right: 8px;
  background: #F56C6C;
  color: white;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}
</style>
```

### 2.3 画廊视图 (Gallery View)

#### 组件选择
- **布局组件**: CSS Grid
- **卡片组件**: `el-card`
- **图片组件**: `el-image`

#### 布局实现

```vue
<template>
  <div class="gallery-view">
    <div class="gallery-grid">
      <el-card
        v-for="asset in assets"
        :key="asset.id"
        class="asset-gallery-card"
        :body-style="{ padding: '0' }"
        @click="handleView(asset)"
      >
        <!-- 卡片图片区 -->
        <div class="gallery-image">
          <el-image
            v-if="asset.images?.length"
            :src="asset.images[0]"
            :preview-src-list="asset.images"
            fit="cover"
            class="gallery-img"
          />
          <div v-else class="gallery-placeholder">
            <el-icon :size="48"><Picture /></el-icon>
          </div>

          <!-- 状态标签 -->
          <el-tag
            :type="getStatusType(asset.status)"
            class="gallery-status"
            size="small"
          >
            {{ getStatusName(asset.status) }}
          </el-tag>
        </div>

        <!-- 卡片内容区 -->
        <div class="gallery-content">
          <div class="gallery-title">{{ asset.name }}</div>
          <div class="gallery-category">
            <el-tag size="small" type="info">
              {{ getCategoryName(asset.category) }}
            </el-tag>
            <span v-if="asset.brandModel">{{ asset.brandModel }}</span>
          </div>

          <div class="gallery-info">
            <div class="info-item">
              <span class="label">购置</span>
              <span>{{ formatDate(asset.purchaseDate) }}</span>
            </div>
            <div class="info-item">
              <span class="label">价格</span>
              <span class="price">¥{{ asset.purchasePrice }}</span>
            </div>
          </div>

          <div v-if="asset.status === 'borrowed'" class="gallery-borrow">
            <div class="borrow-row">
              <span class="label">借用人</span>
              <span>{{ asset.userName }}</span>
            </div>
            <div class="borrow-row">
              <span class="label">应还日期</span>
              <span :class="{ 'overdue': isOverdue(asset) }">
                {{ formatDate(asset.expectedReturnDate) }}
              </span>
            </div>
          </div>

          <!-- 快捷操作 -->
          <div class="gallery-actions">
            <el-button
              v-if="asset.status === 'stock'"
              type="primary"
              size="small"
              @click.stop="handleBorrow(asset)"
            >
              借出
            </el-button>
            <el-button
              v-if="asset.status === 'borrowed'"
              type="warning"
              size="small"
              @click.stop="handleReturn(asset)"
            >
              归还
            </el-button>
            <el-button
              size="small"
              @click.stop="handleEdit(asset)"
            >
              编辑
            </el-button>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<style scoped>
.gallery-view {
  padding: 20px;
}

.gallery-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}

.asset-gallery-card {
  cursor: pointer;
  transition: all 0.3s;
}

.asset-gallery-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 20px rgba(0,0,0,0.12);
}

.gallery-image {
  position: relative;
  width: 100%;
  height: 200px;
  overflow: hidden;
}

.gallery-img {
  width: 100%;
  height: 100%;
}

.gallery-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
  color: #ddd;
}

.gallery-status {
  position: absolute;
  top: 12px;
  right: 12px;
}

.gallery-content {
  padding: 16px;
}

.gallery-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.gallery-category {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
  font-size: 13px;
  color: #666;
}

.gallery-info {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
  padding: 12px 0;
  border-top: 1px solid #eee;
  border-bottom: 1px solid #eee;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 13px;
}

.info-item .label {
  color: #999;
}

.info-item .price {
  color: #409EFF;
  font-weight: 600;
}

.gallery-borrow {
  padding: 12px 0;
  background: #fffbf0;
  border-radius: 4px;
  margin: 12px 0;
}

.borrow-row {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  margin-bottom: 4px;
}

.borrow-row .label {
  color: #999;
}

.borrow-row .overdue {
  color: #F56C6C;
  font-weight: 600;
}

.gallery-actions {
  display: flex;
  gap: 8px;
  margin-top: 12px;
}

.gallery-actions .el-button {
  flex: 1;
}
</style>
```

---

## 3. 组件规范

### 3.1 视图切换组件

```vue
<template>
  <el-radio-group v-model="currentView" size="large">
    <el-tooltip content="表格视图" placement="top">
      <el-radio-button value="table">
        <el-icon><List /></el-icon>
      </el-radio-button>
    </el-tooltip>

    <el-tooltip content="看板视图" placement="top">
      <el-radio-button value="kanban">
        <el-icon><Grid /></el-icon>
      </el-radio-button>
    </el-tooltip>

    <el-tooltip content="画廊视图" placement="top">
      <el-radio-button value="gallery">
        <el-icon><PictureFilled /></el-icon>
      </el-radio-button>
    </el-tooltip>
  </el-radio-group>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { List, Grid, PictureFilled } from '@element-plus/icons-vue'

const currentView = ref<'table' | 'kanban' | 'gallery'>('table')
</script>
```

### 3.2 资产表单组件

```vue
<template>
  <el-form
    ref="formRef"
    :model="formData"
    :rules="formRules"
    label-width="120px"
  >
    <el-row :gutter="20">
      <el-col :span="12">
        <el-form-item label="资产名称" prop="name">
          <el-input
            v-model="formData.name"
            placeholder="请输入资产名称"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>
      </el-col>

      <el-col :span="12">
        <el-form-item label="资产类别" prop="category">
          <el-select
            v-model="formData.category"
            placeholder="请选择类别"
            style="width: 100%"
          >
            <el-option label="电子设备" value="electronic">
              <span style="float: left">电子设备</span>
              <span style="float: right; color: #999">
                <el-icon><Monitor /></el-icon>
              </span>
            </el-option>
            <el-option label="办公家具" value="furniture">
              <span style="float: left">办公家具</span>
              <span style="float: right; color: #999">
                <el-icon><OfficeBuilding /></el-icon>
              </span>
            </el-option>
            <el-option label="图书资料" value="book">
              <span style="float: left">图书资料</span>
              <span style="float: right; color: #999">
                <el-icon><Reading /></el-icon>
              </span>
            </el-option>
            <el-option label="其他" value="other">
              <span style="float: left">其他</span>
              <span style="float: right; color: #999">
                <el-icon><MoreFilled /></el-icon>
              </span>
            </el-option>
          </el-select>
        </el-form-item>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <el-col :span="12">
        <el-form-item label="品牌/型号" prop="brandModel">
          <el-input
            v-model="formData.brandModel"
            placeholder="请输入品牌/型号"
          />
        </el-form-item>
      </el-col>

      <el-col :span="12">
        <el-form-item label="存放位置" prop="location">
          <el-input
            v-model="formData.location"
            placeholder="请输入存放位置"
          />
        </el-form-item>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <el-col :span="12">
        <el-form-item label="购置日期" prop="purchaseDate">
          <el-date-picker
            v-model="formData.purchaseDate"
            type="date"
            placeholder="选择日期"
            :disabled-date="disabledDate"
            style="width: 100%"
          />
        </el-form-item>
      </el-col>

      <el-col :span="12">
        <el-form-item label="购置金额" prop="purchasePrice">
          <el-input-number
            v-model="formData.purchasePrice"
            :min="0.01"
            :precision="2"
            :step="100"
            style="width: 100%"
          />
        </el-form-item>
      </el-col>
    </el-row>

    <el-form-item label="资产图片" prop="images">
      <el-upload
        v-model:file-list="fileList"
        action="/api/upload"
        list-type="picture-card"
        :limit="5"
        :on-preview="handlePreview"
        :on-success="handleUploadSuccess"
        :on-remove="handleRemove"
      >
        <el-icon><Plus /></el-icon>
      </el-upload>
    </el-form-item>

    <el-form-item label="备注" prop="notes">
      <el-input
        v-model="formData.notes"
        type="textarea"
        :rows="4"
        placeholder="请输入备注信息"
        maxlength="500"
        show-word-limit
      />
    </el-form-item>
  </el-form>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules, UploadUserFile } from 'element-plus'

const formRef = ref<FormInstance>()
const formData = reactive({
  name: '',
  category: '',
  brandModel: '',
  location: '',
  purchaseDate: '',
  purchasePrice: 0,
  images: [],
  notes: ''
})

const fileList = ref<UploadUserFile[]>([])

const formRules: FormRules = {
  name: [
    { required: true, message: '请输入资产名称', trigger: 'blur' },
    { min: 2, max: 100, message: '长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  category: [
    { required: true, message: '请选择资产类别', trigger: 'change' }
  ],
  purchaseDate: [
    { required: true, message: '请选择购置日期', trigger: 'change' }
  ],
  purchasePrice: [
    { required: true, message: '请输入购置金额', trigger: 'blur' }
  ]
}

function disabledDate(time: Date) {
  return time.getTime() > Date.now()
}

function handlePreview(file: UploadUserFile) {
  window.open(file.url, '_blank')
}

function handleUploadSuccess(response: any, file: UploadUserFile) {
  if (response.success) {
    formData.images.push(response.data.url)
    ElMessage.success('上传成功')
  }
}

function handleRemove(file: UploadUserFile) {
  const index = formData.images.indexOf(file.url)
  if (index > -1) {
    formData.images.splice(index, 1)
  }
}
</script>
```

### 3.3 借还表单组件

```vue
<template>
  <el-form
    ref="borrowFormRef"
    :model="borrowForm"
    :rules="borrowRules"
    label-width="120px"
  >
    <el-form-item label="借用人" prop="borrowerId">
      <el-select
        v-model="borrowForm.borrowerId"
        filterable
        remote
        placeholder="搜索员工姓名"
        :remote-method="searchUsers"
        :loading="userLoading"
        style="width: 100%"
      >
        <el-option
          v-for="user in users"
          :key="user.id"
          :label="user.name"
          :value="user.id"
        >
          <span>{{ user.name }}</span>
          <span style="float: right; color: #999; font-size: 13px">
            {{ user.department }}
          </span>
        </el-option>
      </el-select>
    </el-form-item>

    <el-form-item label="借出日期" prop="borrowDate">
      <el-date-picker
        v-model="borrowForm.borrowDate"
        type="date"
        placeholder="选择借出日期"
        :disabled-date="disabledBorrowDate"
        style="width: 100%"
      />
    </el-form-item>

    <el-form-item label="预计归还日期" prop="expectedReturnDate">
      <el-date-picker
        v-model="borrowForm.expectedReturnDate"
        type="date"
        placeholder="选择归还日期"
        :disabled-date="disabledReturnDate"
        style="width: 100%"
      />
      <div class="form-tip">
        <el-icon><InfoFilled /></el-icon>
        借用期限最长30天
      </div>
    </el-form-item>

    <el-form-item label="借用备注" prop="notes">
      <el-input
        v-model="borrowForm.notes"
        type="textarea"
        :rows="3"
        placeholder="请输入借用备注"
        maxlength="200"
        show-word-limit
      />
    </el-form-item>
  </el-form>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { InfoFilled } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'

const borrowFormRef = ref<FormInstance>()
const borrowForm = reactive({
  borrowerId: '',
  borrowDate: new Date(),
  expectedReturnDate: '',
  notes: ''
})

const borrowRules: FormRules = {
  borrowerId: [
    { required: true, message: '请选择借用人', trigger: 'change' }
  ],
  borrowDate: [
    { required: true, message: '请选择借出日期', trigger: 'change' }
  ],
  expectedReturnDate: [
    { required: true, message: '请选择预计归还日期', trigger: 'change' },
    {
      validator: (rule, value, callback) => {
        if (!borrowForm.borrowDate) {
          callback()
          return
        }
        const borrowDate = new Date(borrowForm.borrowDate)
        const returnDate = new Date(value)
        const daysDiff = Math.ceil((returnDate.getTime() - borrowDate.getTime()) / (1000 * 60 * 60 * 24))

        if (daysDiff <= 0) {
          callback(new Error('归还日期必须晚于借出日期'))
        } else if (daysDiff > 30) {
          callback(new Error('借用期限最长为30天'))
        } else {
          callback()
        }
      },
      trigger: 'change'
    }
  ]
}

function disabledBorrowDate(time: Date) {
  return time.getTime() > Date.now()
}

function disabledReturnDate(time: Date) {
  if (!borrowForm.borrowDate) return false
  const borrowDate = new Date(borrowForm.borrowDate)
  const maxDate = new Date(borrowDate)
  maxDate.setDate(maxDate.getDate() + 30)
  return time.getTime() < borrowDate.getTime() || time.getTime() > maxDate.getTime()
}
</script>

<style scoped>
.form-tip {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 4px;
  font-size: 12px;
  color: #999;
}
</style>
```

---

## 4. 样式规范

### 4.1 颜色规范

```css
/* 主题色 */
--primary-color: #409EFF;
--success-color: #67C23A;
--warning-color: #E6A23C;
--danger-color: #F56C6C;
--info-color: #909399;

/* 状态色 */
--status-stock: #67C23A;
--status-in-use: #409EFF;
--status-borrowed: #E6A23C;
--status-maintenance: #F56C6C;
--status-scrapped: #909399;

/* 背景色 */
--bg-page: #f5f7fa;
--bg-card: #ffffff;
--bg-hover: #f5f7fa;

/* 文本色 */
--text-primary: #303133;
--text-regular: #606266;
--text-secondary: #909399;
--text-placeholder: #c0c4cc;

/* 边框色 */
--border-base: #dcdfe6;
--border-light: #e4e7ed;
--border-lighter: #ebeef5;
--border-extra-light: #f2f6fc;
```

### 4.2 字体规范

```css
/* 字体大小 */
--font-size-extra-large: 20px;
--font-size-large: 18px;
--font-size-medium: 16px;
--font-size-base: 14px;
--font-size-small: 13px;
--font-size-extra-small: 12px;

/* 字体粗细 */
--font-weight-normal: 400;
--font-weight-medium: 500;
--font-weight-semibold: 600;
--font-weight-bold: 700;
```

### 4.3 间距规范

```css
/* 内边距 */
--padding-extra-small: 4px;
--padding-small: 8px;
--padding-base: 12px;
--padding-medium: 16px;
--padding-large: 20px;
--padding-extra-large: 24px;

/* 外边距 */
--margin-extra-small: 4px;
--margin-small: 8px;
--margin-base: 12px;
--margin-medium: 16px;
--margin-large: 20px;
--margin-extra-large: 24px;

/* 圆角 */
--border-radius-small: 4px;
--border-radius-base: 6px;
--border-radius-large: 8px;
--border-radius-extra-large: 12px;
```

### 4.4 阴影规范

```css
/* 阴影层级 */
--shadow-base: 0 2px 4px rgba(0, 0, 0, 0.12), 0 0 6px rgba(0, 0, 0, 0.04);
--shadow-light: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
--shadow-medium: 0 4px 16px rgba(0, 0, 0, 0.12);
--shadow-heavy: 0 8px 24px rgba(0, 0, 0, 0.15);
```

---

## 5. 交互规范

### 5.1 加载状态

```vue
<template>
  <!-- 列表加载 -->
  <el-table v-loading="loading" element-loading-text="加载中..." />

  <!-- 按钮加载 -->
  <el-button :loading="submitting" type="primary">
    提交
  </el-button>

  <!-- 卡片加载 -->
  <el-card v-loading="detailLoading" />
</template>
```

### 5.2 空状态

```vue
<template>
  <el-empty
    v-if="assets.length === 0"
    description="暂无资产数据"
    :image-size="200"
  >
    <el-button type="primary" @click="handleAdd">
      立即添加
    </el-button>
  </el-empty>
</template>
```

### 5.3 确认对话框

```typescript
import { ElMessageBox } from 'element-plus'

// 删除确认
async function handleDelete(asset: Asset) {
  try {
    await ElMessageBox.confirm(
      `确定要删除资产【${asset.name}】吗？删除后不可恢复。`,
      '删除确认',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning',
        confirmButtonClass: 'el-button--danger'
      }
    )

    await deleteAsset(asset.id)
    ElMessage.success('删除成功')
  } catch {
    // 用户取消
  }
}

// 状态变更确认
async function handleStatusChange(asset: Asset, newStatus: string) {
  try {
    await ElMessageBox.confirm(
      `确定要将资产【${asset.name}】的状态变更为【${getStatusName(newStatus)}】吗？`,
      '状态变更确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await updateAssetStatus(asset.id, newStatus)
    ElMessage.success('状态变更成功')
  } catch {
    // 用户取消
  }
}
```

### 5.4 消息提示

```typescript
import { ElMessage } from 'element-plus'

// 成功提示
ElMessage.success('操作成功')

// 警告提示
ElMessage.warning('该资产已借出,无法再次借出')

// 错误提示
ElMessage.error('操作失败,请稍后重试')

// 信息提示
ElMessage.info('正在处理中...')

// 可关闭提示
ElMessage({
  message: '这是一条可关闭的消息',
  type: 'info',
  showClose: true,
  duration: 5000
})
```

### 5.5 响应式设计

```css
/* 断点定义 */
--breakpoint-xs: 480px;
--breakpoint-sm: 576px;
--breakpoint-md: 768px;
--breakpoint-lg: 992px;
--breakpoint-xl: 1200px;
--breakpoint-xxl: 1600px;

/* 响应式网格 */
@media (max-width: 768px) {
  .gallery-grid {
    grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  }

  .kanban-column {
    min-width: 250px;
  }
}

@media (max-width: 576px) {
  .gallery-grid {
    grid-template-columns: 1fr;
  }

  .filter-form {
    flex-direction: column;
  }
}
```

---

## 6. 可访问性规范

### 6.1 键盘导航

- Tab键: 焦点在可交互元素间切换
- Enter键: 确认/提交
- Escape键: 取消/关闭对话框
- 箭头键: 在列表/表格中导航

### 6.2 ARIA标签

```vue
<template>
  <!-- 按钮带图标 -->
  <el-button aria-label="添加新资产">
    <el-icon><Plus /></el-icon>
  </el-button>

  <!-- 图片带说明 -->
  <el-image
    :src="asset.image"
    :alt="`资产${asset.name}的照片`"
  />

  <!-- 表单输入带标签 -->
  <el-form-item>
    <template #label>
      <label for="asset-name">资产名称</label>
    </template>
    <el-input
      id="asset-name"
      v-model="asset.name"
      aria-required="true"
    />
  </el-form-item>
</template>
```

### 6.3 颜色对比度

- 文本与背景对比度 ≥ 4.5:1 (WCAG AA标准)
- 大号文本(≥18pt)对比度 ≥ 3:1
- 交互元素与背景对比度 ≥ 3:1

---

**文档版本**: v1.0.0
