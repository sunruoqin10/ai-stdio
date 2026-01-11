<template>
  <div class="expense-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">费用报销管理</h2>
      </div>
      <div class="header-actions">
        <el-button type="primary" @click="handleCreate">
          <el-icon><Plus /></el-icon>
          新建报销
        </el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-cards">
      <el-row :gutter="16">
        <el-col :xs="12" :sm="6" :md="6" :lg="4" :xl="4">
          <div class="stat-card primary" @click="handleTabChange('my-expenses')">
            <div class="stat-icon">
              <el-icon :size="32"><Document /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ expenseStore.myExpenses.length }}</div>
              <div class="stat-label">我的申请</div>
            </div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="6" :md="6" :lg="4" :xl="4">
          <div class="stat-card warning" @click="handleTabChange('approvals')">
            <div class="stat-icon">
              <el-icon :size="32"><Clock /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ expenseStore.pendingCount }}</div>
              <div class="stat-label">待审批</div>
            </div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="6" :md="6" :lg="4" :xl="4">
          <div class="stat-card success">
            <div class="stat-icon">
              <el-icon :size="32"><SuccessFilled /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ expenseStore.approvedCount }}</div>
              <div class="stat-label">已通过</div>
            </div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="6" :md="6" :lg="4" :xl="4">
          <div class="stat-card info">
            <div class="stat-icon">
              <el-icon :size="32"><Money /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ formatAmount(expenseStore.totalAmount) }}</div>
              <div class="stat-label">总金额</div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 标签页 -->
    <el-tabs v-model="activeTab" class="expense-tabs" @tab-change="handleTabChange">
      <el-tab-pane label="我的报销" name="my-expenses">
        <MyExpenses />
      </el-tab-pane>
      <el-tab-pane name="approvals">
        <template #label>
          <span>待审批</span>
          <el-badge v-if="expenseStore.pendingCount > 0" :value="expenseStore.pendingCount" class="tab-badge" />
        </template>
        <ApprovalManagement />
      </el-tab-pane>
      <el-tab-pane label="报销统计" name="statistics">
        <ExpenseStatistics />
      </el-tab-pane>
      <el-tab-pane label="打款管理" name="payments">
        <PaymentManagement />
      </el-tab-pane>
    </el-tabs>

    <!-- 新建报销对话框 -->
    <ExpenseForm
      v-model="showForm"
      @success="handleFormSuccess"
    />

    <!-- 报销单详情 -->
    <ExpenseDetail
      v-model="showDetail"
      :expense-id="currentExpenseId"
      @edit="handleEditFromDetail"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Plus, Document, Clock, SuccessFilled, Money } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useExpenseStore } from '../store'
import { formatAmount } from '../utils'
import MyExpenses from '../components/MyExpenses.vue'
import ApprovalManagement from '../components/ApprovalManagement.vue'
import ExpenseStatistics from '../components/ExpenseStatistics.vue'
import PaymentManagement from '../components/PaymentManagement.vue'
import ExpenseForm from '../components/ExpenseForm.vue'
import ExpenseDetail from '../components/ExpenseDetail.vue'
import type { Expense } from '../types'

const expenseStore = useExpenseStore()

const activeTab = ref('my-expenses')
const showForm = ref(false)
const showDetail = ref(false)
const currentExpenseId = ref<string | null>(null)

onMounted(async () => {
  await loadData()
})

async function loadData() {
  try {
    // 加载我的报销列表
    await expenseStore.loadMyExpenses()

    // 加载待审批列表
    await expenseStore.loadPendingApprovals()
  } catch (error: any) {
    ElMessage.error(error.message || '加载数据失败')
  }
}

function handleTabChange(tab: string) {
  activeTab.value = tab
  expenseStore.setTab(tab as 'my-expenses' | 'approvals' | 'payments')

  // 根据标签页加载对应数据
  if (tab === 'payments') {
    expenseStore.loadPayments()
  }
}

function handleCreate() {
  showForm.value = true
}

function handleFormSuccess() {
  showForm.value = false
  loadData()
}

function handleEditFromDetail(expense: Expense) {
  showDetail.value = false
  // TODO: 打开编辑表单,填充expense数据
  ElMessage.info('编辑功能开发中')
}
</script>

<style scoped lang="scss">
.expense-page {
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

.expense-tabs {
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
