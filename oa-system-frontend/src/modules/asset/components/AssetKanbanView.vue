<template>
  <div class="asset-kanban">
    <el-scrollbar>
      <div class="kanban-board">
        <div
          v-for="(column, status) in columns"
          :key="status"
          class="kanban-column"
          :class="`column-${status}`"
        >
          <div class="column-header">
            <div class="column-title">
              <span class="status-dot" :class="`status-${status}`"></span>
              {{ getStatusName(status) }}
            </div>
            <el-badge :value="column.length" class="column-badge" />
          </div>

          <VueDraggable
            v-model="columns[status]"
            :group="{ name: 'assets' }"
            :animation="200"
            ghost-class="ghost-card"
            item-key="id"
            @change="onDragChange($event, status)"
          >
            <template #item="{ element: asset }">
              <div class="kanban-card" @click="handleCardClick(asset)">
                <div class="card-header">
                  <el-tag :type="getStatusType(asset.category)" size="small">
                    {{ getCategoryName(asset.category) }}
                  </el-tag>
                  <el-dropdown trigger="click" @command="(cmd) => handleCommand(cmd, asset)">
                    <el-icon class="more-icon" @click.stop>
                      <MoreFilled />
                    </el-icon>
                    <template #dropdown>
                      <el-dropdown-menu>
                        <el-dropdown-item command="view">查看详情</el-dropdown-item>
                        <el-dropdown-item command="edit">编辑</el-dropdown-item>
                        <el-dropdown-item command="delete" divided>删除</el-dropdown-item>
                      </el-dropdown-menu>
                    </template>
                  </el-dropdown>
                </div>

                <div class="card-body">
                  <div class="asset-name" :title="asset.name">{{ asset.name }}</div>
                  <div v-if="asset.brandModel" class="asset-model">
                    {{ asset.brandModel }}
                  </div>

                  <div v-if="asset.images && asset.images.length > 0" class="asset-thumb">
                    <el-image
                      :src="getImageUrl(asset.images[0])"
                      fit="cover"
                      :preview-src-list="asset.images.map(url => getImageUrl(url))"
                    />
                  </div>

                  <div class="asset-info">
                    <div class="info-item">
                      <el-icon><Location /></el-icon>
                      <span>{{ asset.location || '未设置' }}</span>
                    </div>
                  </div>

                  <!-- 借用信息 -->
                  <div v-if="asset.status === 'borrowed'" class="borrow-info">
                    <div class="info-item">
                      <el-icon><User /></el-icon>
                      <span>{{ asset.userName }}</span>
                    </div>
                    <div v-if="asset.expectedReturnDate" class="return-date">
                      <el-icon><Calendar /></el-icon>
                      <span
                        :class="{
                          'overdue': checkOverdue(asset),
                          'due-soon': checkReturnReminder(asset)
                        }"
                      >
                        {{ formatDate(asset.expectedReturnDate) }}
                      </span>
                    </div>
                  </div>

                  <!-- 使用人信息 -->
                  <div v-else-if="asset.userId" class="user-info">
                    <el-avatar :src="asset.userAvatar" :size="24" />
                    <span>{{ asset.userName }}</span>
                  </div>

                  <!-- 价值信息 -->
                  <div class="price-info">
                    <div class="price-current">
                      {{ formatAmount(asset.currentValue || 0) }}
                    </div>
                    <div class="price-original">
                      原价: {{ formatAmount(asset.purchasePrice) }}
                    </div>
                  </div>
                </div>

                <!-- 过期提醒 -->
                <div v-if="asset.status === 'borrowed' && checkOverdue(asset)" class="card-alert">
                  <el-icon class="alert-icon"><Warning /></el-icon>
                  <span>已逾期</span>
                </div>

                <div v-else-if="asset.status === 'borrowed' && checkReturnReminder(asset)" class="card-alert warning">
                  <el-icon class="alert-icon"><Clock /></el-icon>
                  <span>即将到期</span>
                </div>
              </div>
            </template>
          </VueDraggable>
        </div>
      </div>
    </el-scrollbar>

    <!-- 详情抽屉 -->
    <AssetDetail v-model="detailVisible" :asset="currentAsset" @edit="handleEdit" />

    <!-- 编辑对话框 -->
    <AssetForm v-model="formVisible" :asset="currentAsset" @success="handleFormSuccess" />

    <!-- 借用对话框 -->
    <BorrowDialog v-model="borrowVisible" :asset="currentAsset" @success="handleBorrowSuccess" />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch } from 'vue'
import { VueDraggable } from 'vue-draggable-plus'
import { ElMessage, ElMessageBox } from 'element-plus'
import { MoreFilled, Location, User, Calendar, Warning, Clock } from '@element-plus/icons-vue'
import { useAssetStore } from '../store'
import { formatDate, formatAmount, getCategoryName, getStatusName, getStatusType, checkReturnReminder, checkOverdue, isValidStatusTransition, getImageUrl } from '../utils'
import type { Asset, AssetStatus } from '../types'
import AssetDetail from './AssetDetail.vue'
import AssetForm from './AssetForm.vue'
import BorrowDialog from './BorrowDialog.vue'

const assetStore = useAssetStore()

const detailVisible = ref(false)
const formVisible = ref(false)
const borrowVisible = ref(false)
const currentAsset = ref<Asset | null>(null)

// 按状态分组的资产
const columns = reactive<Record<string, Asset[]>>({
  stock: [],
  in_use: [],
  borrowed: [],
  maintenance: [],
  scrapped: []
})

// 从store同步数据
watch(
  () => assetStore.assets,
  (assets) => {
    // 清空所有列
    Object.keys(columns).forEach(key => {
      columns[key] = []
    })

    // 重新分组
    assets.forEach(asset => {
      if (columns[asset.status]) {
        columns[asset.status].push(asset)
      }
    })
  },
  { immediate: true, deep: true }
)

// 拖拽结束处理
function onDragChange(event: any, targetStatus: AssetStatus) {
  const { added } = event
  if (!added) return

  const asset = added.element as Asset
  const oldStatus = asset.status
  const newStatus = targetStatus

  // 检查状态转换是否合法
  if (!isValidStatusTransition(oldStatus, newStatus)) {
    ElMessage.warning(`不能从"${getStatusName(oldStatus)}"转为"${getStatusName(newStatus)}"`)
    // 恢复原状
    setTimeout(() => {
      columns[oldStatus].push(asset)
      columns[newStatus].splice(added.newIndex, 1)
    }, 0)
    return
  }

  // 更新资产状态
  updateAssetStatus(asset, newStatus)
}

async function updateAssetStatus(asset: Asset, newStatus: AssetStatus) {
  try {
    await assetStore.update(asset.id, {
      ...asset,
      status: newStatus
    })
    ElMessage.success('状态更新成功')
  } catch (error: any) {
    ElMessage.error(error.message || '状态更新失败')
    // 失败时恢复原状
    columns[asset.status].push(asset)
    columns[newStatus] = columns[newStatus].filter(a => a.id !== asset.id)
  }
}

function handleCardClick(asset: Asset) {
  currentAsset.value = asset
  detailVisible.value = true
}

function handleCommand(command: string, asset: Asset) {
  currentAsset.value = asset

  switch (command) {
    case 'view':
      detailVisible.value = true
      break
    case 'edit':
      formVisible.value = true
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

function handleEdit(asset: Asset) {
  currentAsset.value = asset
  detailVisible.value = false
  formVisible.value = true
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
</script>

<style scoped lang="scss">
.asset-kanban {
  height: calc(100vh - 320px);
  min-height: 400px;
}

.kanban-board {
  display: flex;
  gap: 16px;
  padding: 8px;
  height: 100%;
  min-width: max-content;
}

.kanban-column {
  flex: 0 0 320px;
  display: flex;
  flex-direction: column;
  background: #f5f7fa;
  border-radius: 8px;
  padding: 12px;
  max-height: 100%;

  &.column-stock {
    background: #f0f9ff;
  }

  &.column-in_use {
    background: #f0f5ff;
  }

  &.column-borrowed {
    background: #fef9f0;
  }

  &.column-maintenance {
    background: #fef0f0;
  }

  &.column-scrapped {
    background: #f5f5f5;
  }
}

.column-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
  padding: 0 4px;
}

.column-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;

  &.status-stock {
    background: #67c23a;
  }

  &.status-in_use {
    background: #409eff;
  }

  &.status-borrowed {
    background: #e6a23c;
  }

  &.status-maintenance {
    background: #f56c6c;
  }

  &.status-scrapped {
    background: #909399;
  }
}

:deep(.el-badge__content) {
  background-color: #909399;
}

.kanban-card {
  background: white;
  border-radius: 6px;
  padding: 12px;
  margin-bottom: 12px;
  cursor: pointer;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  transition: all 0.2s;
  position: relative;

  &:hover {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    transform: translateY(-2px);
  }

  &.ghost-card {
    opacity: 0.5;
    background: #e5e7eb;
  }
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.more-icon {
  color: #909399;
  cursor: pointer;
  padding: 4px;
  border-radius: 4px;

  &:hover {
    background: #f5f7fa;
    color: #409eff;
  }
}

.card-body {
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
  }

  .asset-thumb {
    margin-bottom: 8px;
    border-radius: 4px;
    overflow: hidden;

    :deep(.el-image) {
      width: 100%;
      height: 120px;
      display: block;
    }
  }

  .asset-info {
    margin-bottom: 8px;

    .info-item {
      display: flex;
      align-items: center;
      gap: 6px;
      font-size: 12px;
      color: #606266;
      margin-bottom: 4px;

      .el-icon {
        font-size: 14px;
        color: #909399;
      }
    }
  }

  .borrow-info {
    padding: 8px;
    background: #fef9f0;
    border-radius: 4px;
    margin-bottom: 8px;

    .info-item {
      display: flex;
      align-items: center;
      gap: 6px;
      font-size: 12px;
      color: #606266;
      margin-bottom: 4px;

      .el-icon {
        font-size: 14px;
        color: #e6a23c;
      }
    }

    .return-date {
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

  .user-info {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 6px;
    background: #f0f5ff;
    border-radius: 4px;
    margin-bottom: 8px;

    span {
      font-size: 12px;
      color: #606266;
    }
  }

  .price-info {
    padding-top: 8px;
    border-top: 1px dashed #dcdfe6;

    .price-current {
      font-size: 16px;
      font-weight: 600;
      color: #f56c6c;
      margin-bottom: 2px;
    }

    .price-original {
      font-size: 11px;
      color: #909399;
    }
  }
}

.card-alert {
  position: absolute;
  top: -6px;
  right: -6px;
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 8px;
  background: #f56c6c;
  color: white;
  font-size: 11px;
  border-radius: 10px 0 10px 0;
  box-shadow: 0 2px 4px rgba(245, 108, 108, 0.3);

  &.warning {
    background: #e6a23c;
    box-shadow: 0 2px 4px rgba(230, 162, 60, 0.3);
  }

  .alert-icon {
    font-size: 12px;
  }
}
</style>
