<template>
  <div class="asset-gallery">
    <el-row :gutter="16">
      <el-col
        v-for="asset in assets"
        :key="asset.id"
        :xs="24"
        :sm="12"
        :md="8"
        :lg="6"
        :xl="4"
        class="gallery-col"
      >
        <div class="gallery-card" @click="handleCardClick(asset)">
          <!-- 图片区域 -->
          <div class="card-image">
            <el-image
              :src="getAssetImageUrl(asset)"
              fit="cover"
              :preview-src-list="asset.images?.map(url => getImageUrl(url)) || []"
              :initial-index="0"
              class="gallery-image"
            >
              <template #error>
                <div class="image-slot">
                  <el-icon :size="48"><Picture /></el-icon>
                  <span>暂无图片</span>
                </div>
              </template>
            </el-image>

            <!-- 状态标签 -->
            <div class="card-badges">
              <el-tag :type="getStatusType(asset.status)" size="small">
                {{ getStatusName(asset.status) }}
              </el-tag>
              <el-tag :type="getCategoryTagType(asset.category)" size="small">
                {{ getCategoryName(asset.category) }}
              </el-tag>
            </div>

            <!-- 过期提醒 -->
            <div
              v-if="asset.status === 'borrowed' && checkOverdue(asset)"
              class="alert-badge overdue"
            >
              <el-icon><Warning /></el-icon>
              <span>已逾期</span>
            </div>
            <div
              v-else-if="asset.status === 'borrowed' && checkReturnReminder(asset)"
              class="alert-badge due-soon"
            >
              <el-icon><Clock /></el-icon>
              <span>即将到期</span>
            </div>
          </div>

          <!-- 内容区域 -->
          <div class="card-content">
            <div class="asset-name" :title="asset.name">{{ asset.name }}</div>
            <div v-if="asset.brandModel" class="asset-model">
              {{ asset.brandModel }}
            </div>

            <div class="asset-meta">
              <div class="meta-item">
                <el-icon><Location /></el-icon>
                <span>{{ asset.location || '未设置' }}</span>
              </div>
            </div>

            <!-- 借用信息 -->
            <div v-if="asset.status === 'borrowed'" class="borrow-section">
              <div class="borrower-info">
                <el-avatar :src="asset.userAvatar" :size="20" />
                <span>{{ asset.userName }}</span>
              </div>
              <div v-if="asset.expectedReturnDate" class="return-info">
                <span class="return-label">归还:</span>
                <span :class="{ overdue: checkOverdue(asset), 'due-soon': checkReturnReminder(asset) }">
                  {{ formatDate(asset.expectedReturnDate) }}
                </span>
              </div>
            </div>

            <!-- 使用人信息 -->
            <div v-else-if="asset.userId" class="user-section">
              <el-avatar :src="asset.userAvatar" :size="20" />
              <span>{{ asset.userName }}</span>
            </div>

            <!-- 价格信息 -->
            <div class="price-section">
              <div class="price-current">
                <span class="price-value">{{ formatAmount(asset.currentValue || 0) }}</span>
                <span class="price-label">当前价值</span>
              </div>
              <div class="price-depreciation">
                折旧: {{ calculateDepreciationRate(asset) }}%
              </div>
            </div>
          </div>

          <!-- 操作按钮 -->
          <div class="card-actions" @click.stop>
            <el-button-group>
              <el-tooltip content="查看详情" placement="top">
                <el-button size="small" @click="handleView(asset)">
                  <el-icon><View /></el-icon>
                </el-button>
              </el-tooltip>
              <el-tooltip content="编辑" placement="top">
                <el-button size="small" @click="handleEdit(asset)">
                  <el-icon><Edit /></el-icon>
                </el-button>
              </el-tooltip>
              <el-dropdown trigger="click" @command="(cmd) => handleCommand(cmd, asset)">
                <el-button size="small">
                  <el-icon><MoreFilled /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item
                      v-if="asset.status === 'stock'"
                      command="borrow"
                      icon="Share"
                    >
                      借出
                    </el-dropdown-item>
                    <el-dropdown-item
                      v-if="asset.status === 'borrowed'"
                      command="return"
                      icon="Back"
                    >
                      归还
                    </el-dropdown-item>
                    <el-dropdown-item command="delete" divided icon="Delete">
                      删除
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </el-button-group>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 详情抽屉 -->
    <AssetDetail v-model="detailVisible" :asset="currentAsset" @edit="handleEditFromDetail" />

    <!-- 编辑对话框 -->
    <AssetForm v-model="formVisible" :asset="currentAsset" @success="handleFormSuccess" />

    <!-- 借用对话框 -->
    <BorrowDialog v-model="borrowVisible" :asset="currentAsset" @success="handleBorrowSuccess" />

    <!-- 归还对话框 -->
    <ReturnDialog v-model="returnVisible" :asset="currentAsset" @success="handleReturnSuccess" />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Picture,
  Location,
  View,
  Edit,
  MoreFilled,
  Warning,
  Clock
} from '@element-plus/icons-vue'
import { useAssetStore } from '../store'
import {
  formatDate,
  formatAmount,
  getCategoryName,
  getStatusName,
  getStatusType,
  checkReturnReminder,
  checkOverdue,
  getImageUrl
} from '../utils'
import type { Asset } from '../types'
import AssetDetail from './AssetDetail.vue'
import AssetForm from './AssetForm.vue'
import BorrowDialog from './BorrowDialog.vue'
import ReturnDialog from './ReturnDialog.vue'

const assetStore = useAssetStore()

const detailVisible = ref(false)
const formVisible = ref(false)
const borrowVisible = ref(false)
const returnVisible = ref(false)
const currentAsset = ref<Asset | null>(null)

const assets = assetStore.assets

function getAssetImageUrl(asset: Asset): string {
  return asset.images?.[0] ? getImageUrl(asset.images[0]) : ''
}

function getCategoryTagType(category: string): string {
  const typeMap: Record<string, string> = {
    electronic: 'primary',
    furniture: 'success',
    book: 'warning',
    other: 'info'
  }
  return typeMap[category] || 'info'
}

function calculateDepreciationRate(asset: Asset): number {
  if (!asset.currentValue) return 0
  const rate = ((asset.purchasePrice - asset.currentValue) / asset.purchasePrice) * 100
  return Math.round(rate * 100) / 100
}

function handleCardClick(asset: Asset) {
  currentAsset.value = asset
  detailVisible.value = true
}

function handleView(asset: Asset) {
  currentAsset.value = asset
  detailVisible.value = true
}

function handleEdit(asset: Asset) {
  currentAsset.value = asset
  formVisible.value = true
}

function handleEditFromDetail(asset: Asset) {
  currentAsset.value = asset
  detailVisible.value = false
  formVisible.value = true
}

function handleCommand(command: string, asset: Asset) {
  currentAsset.value = asset

  switch (command) {
    case 'borrow':
      borrowVisible.value = true
      break
    case 'return':
      returnVisible.value = true
      break
    case 'delete':
      handleDelete(asset)
      break
  }
}

async function handleDelete(asset: Asset) {
  try {
    await ElMessageBox.confirm(
      `确定要删除资产"${asset.name}"吗?此操作不可恢复。`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await assetStore.remove(asset.id)
    ElMessage.success('删除成功')
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

function handleFormSuccess() {
  detailVisible.value = false
  formVisible.value = false
  currentAsset.value = null
}

function handleBorrowSuccess() {
  borrowVisible.value = false
  currentAsset.value = null
}

function handleReturnSuccess() {
  returnVisible.value = false
  currentAsset.value = null
}
</script>

<style scoped lang="scss">
.asset-gallery {
  padding: 8px;
}

.gallery-col {
  margin-bottom: 16px;
}

.gallery-card {
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: all 0.3s;
  cursor: pointer;
  height: 100%;
  display: flex;
  flex-direction: column;

  &:hover {
    box-shadow: 0 8px 16px rgba(0, 0, 0, 0.15);
    transform: translateY(-4px);
  }
}

.card-image {
  position: relative;
  width: 100%;
  height: 180px;
  overflow: hidden;
  background: #f5f7fa;

  .gallery-image {
    width: 100%;
    height: 100%;
  }

  .image-slot {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    height: 100%;
    color: #909399;

    .el-icon {
      margin-bottom: 8px;
    }
  }
}

.card-badges {
  position: absolute;
  top: 8px;
  left: 8px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.alert-badge {
  position: absolute;
  top: 8px;
  right: 8px;
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 10px;
  border-radius: 12px;
  color: white;
  font-size: 12px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);

  &.overdue {
    background: #f56c6c;
  }

  &.due-soon {
    background: #e6a23c;
  }

  .el-icon {
    font-size: 14px;
  }
}

.card-content {
  padding: 12px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.asset-name {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.asset-model {
  font-size: 12px;
  color: #909399;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.asset-meta {
  margin-bottom: 8px;

  .meta-item {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 12px;
    color: #606266;

    .el-icon {
      font-size: 14px;
      color: #909399;
    }

    span {
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }
}

.borrow-section {
  padding: 8px;
  background: #fef9f0;
  border-radius: 4px;
  margin-bottom: 8px;

  .borrower-info {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 12px;
    color: #606266;
    margin-bottom: 4px;
  }

  .return-info {
    font-size: 11px;
    color: #909399;

    .return-label {
      margin-right: 4px;
    }

    span {
      &.overdue {
        color: #f56c6c;
        font-weight: 600;
      }

      &.due-soon {
        color: #e6a23c;
        font-weight: 600;
      }
    }
  }
}

.user-section {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 8px;
  background: #f0f5ff;
  border-radius: 4px;
  margin-bottom: 8px;
  font-size: 12px;
  color: #606266;
}

.price-section {
  margin-top: auto;
  padding-top: 8px;
  border-top: 1px dashed #dcdfe6;

  .price-current {
    display: flex;
    align-items: baseline;
    justify-content: space-between;
    margin-bottom: 4px;

    .price-value {
      font-size: 18px;
      font-weight: 600;
      color: #f56c6c;
    }

    .price-label {
      font-size: 11px;
      color: #909399;
    }
  }

  .price-depreciation {
    font-size: 11px;
    color: #909399;
  }
}

.card-actions {
  padding: 8px 12px;
  border-top: 1px solid #ebeef5;
  background: #fafafa;
}

:deep(.el-button-group) {
  display: flex;
  width: 100%;

  .el-button {
    flex: 1;
  }
}
</style>
