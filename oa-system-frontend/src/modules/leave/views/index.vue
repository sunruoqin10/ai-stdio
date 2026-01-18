<template>
  <div class="leave-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">请假管理</h2>
      </div>
      <div class="header-actions">
        <el-button type="primary" @click="handleCreate">
          <el-icon><Plus /></el-icon>
          新建申请
        </el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-cards">
      <el-row :gutter="16">
        <el-col :xs="12" :sm="8" :md="6" :lg="6" :xl="4">
          <div class="stat-card primary" @click="handleTabChange('my-requests')">
            <div class="stat-icon">
              <el-icon :size="32"><Document /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ leaveStore.myRequests.length }}</div>
              <div class="stat-label">我的申请</div>
            </div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="8" :md="6" :lg="6" :xl="4">
          <div class="stat-card warning" @click="handleTabChange('approvals')">
            <div class="stat-icon">
              <el-icon :size="32"><Clock /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ leaveStore.pendingCount }}</div>
              <div class="stat-label">待审批</div>
            </div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="8" :md="6" :lg="6" :xl="4">
          <div class="stat-card success">
            <div class="stat-icon">
              <el-icon :size="32"><SuccessFilled /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ leaveStore.approvedCount }}</div>
              <div class="stat-label">已通过</div>
            </div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="8" :md="6" :lg="6" :xl="4">
          <div class="stat-card info">
            <div class="stat-icon">
              <el-icon :size="32"><InfoFilled /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ leaveStore.leaveBalance?.annualRemaining || 0 }}</div>
              <div class="stat-label">年假余额(天)</div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 标签页 -->
    <el-tabs v-model="activeTab" class="leave-tabs" @tab-change="handleTabChange">
      <el-tab-pane label="我的申请" name="my-requests">
        <MyRequests />
      </el-tab-pane>
      <el-tab-pane name="approvals">
        <template #label>
          <span>待审批</span>
          <el-badge v-if="leaveStore.pendingCount > 0" :value="leaveStore.pendingCount" class="tab-badge" />
        </template>
        <ApprovalManagement />
      </el-tab-pane>
    </el-tabs>

    <!-- 新建申请对话框 -->
    <LeaveRequestForm
      v-model="showForm"
      @success="handleFormSuccess"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { Plus, Document, Clock, SuccessFilled, InfoFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useLeaveStore } from '../store'
import { useAuthStore } from '@/modules/auth/store'
import MyRequests from '../components/MyRequests.vue'
import ApprovalManagement from '../components/ApprovalManagement.vue'
import LeaveRequestForm from '../components/LeaveRequestForm.vue'

const leaveStore = useLeaveStore()
const authStore = useAuthStore()

const activeTab = ref('my-requests')
const showForm = ref(false)

// 获取当前登录用户ID
const currentUserId = computed(() => authStore.userInfo?.id)

onMounted(async () => {
  await loadData()
})

async function loadData() {
  if (!currentUserId.value) {
    ElMessage.warning('请先登录')
    return
  }

  try {
    // 加载我的申请列表
    await leaveStore.loadMyRequests()

    // 加载待审批列表
    await leaveStore.loadPendingApprovals()

    // 加载年假余额
    await leaveStore.loadLeaveBalance(currentUserId.value, new Date().getFullYear())
  } catch (error: any) {
    ElMessage.error(error.message || '加载数据失败')
  }
}

function handleTabChange(tab: string) {
  activeTab.value = tab
  leaveStore.setTab(tab as 'my-requests' | 'approvals')
}

function handleCreate() {
  showForm.value = true
}

function handleFormSuccess() {
  showForm.value = false
  loadData()
}
</script>

<style scoped lang="scss">
.leave-page {
  padding: 20px;
  background: #F5F7FA;
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

.stats-cards {
  margin-bottom: 20px;

  .stat-card {
    display: flex;
    align-items: center;
    padding: 20px;
    background: #fff;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
    margin-bottom: 16px;
    cursor: pointer;
    transition: all 0.3s;

    &:hover {
      transform: translateY(-4px);
      box-shadow: 0 8px 16px rgba(0, 0, 0, 0.15);
    }

    &.primary .stat-icon {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    }

    &.warning .stat-icon {
      background: linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%);
    }

    &.success .stat-icon {
      background: linear-gradient(135deg, #84fab0 0%, #8fd3f4 100%);
    }

    &.info .stat-icon {
      background: linear-gradient(135deg, #a1c4fd 0%, #c2e9fb 100%);
    }

    .stat-icon {
      width: 60px;
      height: 60px;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: 12px;
      color: white;
      margin-right: 16px;
      flex-shrink: 0;
    }

    .stat-content {
      flex: 1;

      .stat-value {
        font-size: 24px;
        font-weight: bold;
        color: #303133;
        margin-bottom: 4px;
      }

      .stat-label {
        font-size: 13px;
        color: #909399;
      }
    }
  }
}

.leave-tabs {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  padding: 20px;

  :deep(.el-tabs__header) {
    margin: 0 0 20px 0;
  }

  :deep(.el-tabs__nav-wrap::after) {
    display: none;
  }

  .tab-badge {
    margin-left: 8px;
  }
}
</style>
