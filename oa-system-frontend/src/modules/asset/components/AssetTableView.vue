<template>
  <div class="asset-table-view">
    <el-table
      v-loading="assetStore.loading"
      :data="assetStore.assets"
      stripe
      border
      style="width: 100%"
    >
      <el-table-column prop="id" label="资产编号" width="130" />

      <el-table-column label="资产信息" min-width="200">
        <template #default="{ row }">
          <div class="asset-info">
            <el-image
              v-if="row.images?.length"
              :src="getImageUrl(row.images[0])"
              :preview-src-list="row.images.map(url => getImageUrl(url))"
              fit="cover"
              class="asset-thumb"
            />
            <div v-else class="asset-thumb placeholder">
              <el-icon><Picture /></el-icon>
            </div>
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
          <div v-if="row.userName" class="user-info">
            <el-avatar :src="row.userAvatar" :size="24" />
            <span>{{ row.userName }}</span>
          </div>
          <span v-else>-</span>
        </template>
      </el-table-column>

      <el-table-column label="借出信息" width="180">
        <template #default="{ row }">
          <div v-if="row.status === 'borrowed'">
            <div>借出: {{ formatDate(row.borrowDate!) }}</div>
            <div :class="{ 'text-warning': isOverdue(row), 'text-success': !isOverdue(row) }">
              应还: {{ formatDate(row.expectedReturnDate!) }}
            </div>
          </div>
          <span v-else>-</span>
        </template>
      </el-table-column>

      <el-table-column label="操作" width="280" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" size="small" @click="handleView(row)">
            查看
          </el-button>
          <el-button link type="primary" size="small" @click="handleEdit(row)">
            编辑
          </el-button>
          <el-button
            v-if="row.status === 'stock'"
            link
            type="success"
            size="small"
            @click="handleBorrow(row)"
          >
            借出
          </el-button>
          <el-button
            v-if="row.status === 'borrowed'"
            link
            type="warning"
            size="small"
            @click="handleReturn(row)"
          >
            归还
          </el-button>
          <el-button link type="danger" size="small" @click="handleDelete(row)">
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <el-pagination
      v-model:current-page="assetStore.pagination.page"
      v-model:page-size="assetStore.pagination.pageSize"
      :total="assetStore.pagination.total"
      :page-sizes="[10, 20, 50, 100]"
      layout="total, sizes, prev, pager, next, jumper"
      style="margin-top: 20px; justify-content: flex-end"
      @size-change="handleSizeChange"
      @current-change="handlePageChange"
    />

    <!-- 资产详情抽屉 -->
    <AssetDetail
      v-model="showDetail"
      :asset="currentAsset"
      @edit="handleEditFromDetail"
    />

    <!-- 编辑表单 -->
    <AssetForm
      v-model="showForm"
      :asset="currentAsset"
      @success="handleFormSuccess"
    />

    <!-- 借出对话框 -->
    <BorrowDialog
      v-model="showBorrow"
      :asset="currentAsset"
      @success="handleBorrowSuccess"
    />

    <!-- 归还对话框 -->
    <ReturnDialog
      v-model="showReturn"
      :asset="currentAsset"
      @success="handleReturnSuccess"
    />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import { Picture } from '@element-plus/icons-vue'
import { useAssetStore } from '../store'
import {
  formatDate,
  getCategoryName,
  getStatusName,
  getStatusType,
  checkOverdue,
  getImageUrl
} from '../utils'
import type { Asset } from '../types'
import AssetDetail from './AssetDetail.vue'
import AssetForm from './AssetForm.vue'
import BorrowDialog from './BorrowDialog.vue'
import ReturnDialog from './ReturnDialog.vue'

const assetStore = useAssetStore()

// 对话框显示状态
const showDetail = ref(false)
const showForm = ref(false)
const showBorrow = ref(false)
const showReturn = ref(false)
const currentAsset = ref<Asset | null>(null)

function handleView(asset: Asset) {
  currentAsset.value = asset
  showDetail.value = true
}

function handleEdit(asset: Asset) {
  currentAsset.value = asset
  showForm.value = true
}

function handleEditFromDetail(asset: Asset) {
  currentAsset.value = asset
  showDetail.value = false
  showForm.value = true
}

function handleBorrow(asset: Asset) {
  currentAsset.value = asset
  showBorrow.value = true
}

function handleReturn(asset: Asset) {
  currentAsset.value = asset
  showReturn.value = true
}

async function handleDelete(asset: Asset) {
  try {
    await ElMessageBox.confirm(
      `确定要删除资产 "${asset.name}" 吗?此操作不可恢复。`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    await assetStore.remove(asset.id)
    ElMessage.success('删除成功')
  } catch {
    // 用户取消
  }
}

function handleFormSuccess() {
  showForm.value = false
  currentAsset.value = null
}

function handleBorrowSuccess() {
  showBorrow.value = false
  currentAsset.value = null
}

function handleReturnSuccess() {
  showReturn.value = false
  currentAsset.value = null
}

function isOverdue(asset: Asset): boolean {
  return checkOverdue(asset)
}

function handleSizeChange() {
  assetStore.loadList()
}

function handlePageChange() {
  assetStore.loadList()
}
</script>

<style scoped lang="scss">
.asset-table-view {
  .asset-info {
    display: flex;
    align-items: center;
    gap: 12px;

    .asset-thumb {
      width: 50px;
      height: 50px;
      border-radius: 4px;
      flex-shrink: 0;

      &.placeholder {
        display: flex;
        align-items: center;
        justify-content: center;
        background: #f5f7fa;
        color: #909399;

        .el-icon {
          font-size: 24px;
        }
      }
    }

    .asset-details {
      flex: 1;
      min-width: 0;

      .asset-name {
        font-weight: 500;
        margin-bottom: 4px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .asset-category {
        font-size: 12px;
        color: #999;
      }
    }
  }

  .user-info {
    display: flex;
    align-items: center;
    gap: 6px;

    span {
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }

  .text-muted {
    color: #999;
    font-size: 12px;
  }

  .text-warning {
    color: #e6a23c;
    font-size: 12px;
  }

  .text-success {
    color: #67c23a;
    font-size: 12px;
  }
}
</style>
