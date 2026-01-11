<template>
  <div class="asset-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">资产管理</h2>
      </div>
      <div class="header-actions">
        <el-button type="primary" @click="handleCreate">
          <el-icon><Plus /></el-icon>
          新增资产
        </el-button>
        <el-button @click="handleImport">
          <el-icon><Upload /></el-icon>
          批量导入
        </el-button>
        <el-button @click="handleExport">
          <el-icon><Download /></el-icon>
          导出
        </el-button>
      </div>
    </div>

    <!-- 操作栏 -->
    <div class="toolbar">
      <!-- 筛选区 -->
      <AssetFilter @filter="handleFilter" @reset="handleReset" />

      <!-- 视图切换 -->
      <div class="view-switcher">
        <el-radio-group v-model="assetStore.currentView" @change="handleViewChange">
          <el-radio-button value="table">
            <el-icon><List /></el-icon>
            表格
          </el-radio-button>
          <el-radio-button value="kanban">
            <el-icon><Grid /></el-icon>
            看板
          </el-radio-button>
          <el-radio-button value="gallery">
            <el-icon><Picture /></el-icon>
            画廊
          </el-radio-button>
        </el-radio-group>
      </div>

      <!-- 统计卡片 -->
      <div class="stats-cards">
        <el-row :gutter="16">
          <el-col :span="6">
            <div class="stat-card">
              <div class="stat-icon">
                <el-icon :size="32"><Box /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-label">总资产</div>
                <div class="stat-value">{{ assetStore.assets.length }}</div>
              </div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-card">
              <div class="stat-icon">
                <el-icon :size="32"><SuccessFilled /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-label">库存中</div>
                <div class="stat-value">{{ assetStore.assetsByStatus.stock?.length || 0 }}</div>
              </div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-card">
              <div class="stat-icon">
                <el-icon :size="32"><User /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-label">使用中</div>
                <div class="stat-value">{{ assetStore.assetsByStatus.in_use?.length || 0 }}</div>
              </div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-card">
              <div class="stat-icon">
                <el-icon :size="32"><Warning /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-label">已借出</div>
                <div class="stat-value">{{ assetStore.assetsByStatus.borrowed?.length || 0 }}</div>
              </div>
            </div>
          </el-col>
        </el-row>
      </div>
    </div>

    <!-- 内容区域 -->
    <div class="content-area">
      <!-- 表格视图 -->
      <AssetTableView v-if="assetStore.currentView === 'table'" />

      <!-- 看板视图 -->
      <AssetKanbanView v-else-if="assetStore.currentView === 'kanban'" />

      <!-- 画廊视图 -->
      <AssetGalleryView v-else-if="assetStore.currentView === 'gallery'" />
    </div>

    <!-- 资产表单对话框 -->
    <AssetForm
      v-model="showFormDialog"
      :asset="currentAsset"
      @success="handleFormSuccess"
    />

    <!-- 资产详情抽屉 -->
    <AssetDetail
      v-model="showDetailDrawer"
      :asset="currentAsset"
      @edit="handleEditFromDetail"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Plus, Upload, Download, List, Grid, Picture, Box, SuccessFilled, User, Warning } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useAssetStore } from '../store'
import AssetFilter from '../components/AssetFilter.vue'
import AssetForm from '../components/AssetForm.vue'
import AssetDetail from '../components/AssetDetail.vue'
import AssetTableView from '../components/AssetTableView.vue'
import AssetKanbanView from '../components/AssetKanbanView.vue'
import AssetGalleryView from '../components/AssetGalleryView.vue'
import type { Asset, AssetFilter as AssetFilterType } from '../types'

// ==================== Store ====================

const assetStore = useAssetStore()

// ==================== 响应式数据 ====================

const showFormDialog = ref(false)
const showDetailDrawer = ref(false)
const currentAsset = ref<Asset | null>(null)

// ==================== 方法 ====================

/**
 * 加载资产列表
 */
async function loadAssets() {
  try {
    await assetStore.loadList()
  } catch (error: any) {
    ElMessage.error(error.message || '加载资产列表失败')
  }
}

/**
 * 处理筛选
 */
function handleFilter(filter: AssetFilterType) {
  assetStore.setFilter(filter)
  loadAssets()
}

/**
 * 重置筛选
 */
function handleReset() {
  assetStore.resetFilter()
  loadAssets()
}

/**
 * 创建资产
 */
function handleCreate() {
  currentAsset.value = null
  showFormDialog.value = true
}

/**
 * 从详情页编辑
 */
function handleEditFromDetail(asset: Asset) {
  currentAsset.value = asset
  showDetailDrawer.value = false
  showFormDialog.value = true
}

/**
 * 导入资产
 */
function handleImport() {
  ElMessage.info('批量导入功能开发中...')
}

/**
 * 导出资产
 */
function handleExport() {
  ElMessage.info('导出功能开发中...')
}

/**
 * 视图切换
 */
function handleViewChange() {
  // 视图切换时的处理
}

/**
 * 表单提交成功
 */
function handleFormSuccess() {
  loadAssets()
}

// ==================== 生命周期 ====================

onMounted(async () => {
  await loadAssets()
})
</script>

<style scoped lang="scss">
.asset-page {
  padding: 20px;
  background: #f5f7fa;
  min-height: 100vh;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 20px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);

  .page-title {
    margin: 0;
    font-size: 20px;
    font-weight: 500;
    color: #303133;
  }

  .header-actions {
    display: flex;
    gap: 12px;
  }
}

.toolbar {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  padding: 20px;
  margin-bottom: 20px;
}

.view-switcher {
  margin-bottom: 20px;
}

.stats-cards {
  .stat-card {
    display: flex;
    align-items: center;
    padding: 20px;
    background: #f5f7fa;
    border-radius: 8px;
    transition: all 0.3s;

    &:hover {
      background: #ecf5ff;
      transform: translateY(-2px);
    }

    .stat-icon {
      width: 60px;
      height: 60px;
      display: flex;
      align-items: center;
      justify-content: center;
      background: #fff;
      border-radius: 50%;
      color: var(--el-color-primary);
      margin-right: 16px;
    }

    .stat-content {
      flex: 1;

      .stat-label {
        font-size: 14px;
        color: #909399;
        margin-bottom: 8px;
      }

      .stat-value {
        font-size: 28px;
        font-weight: bold;
        color: var(--el-color-primary);
      }
    }
  }
}

.content-area {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  padding: 20px;
  min-height: 600px;
}
</style>
