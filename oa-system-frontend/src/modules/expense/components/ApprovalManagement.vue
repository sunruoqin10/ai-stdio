<template>
  <div class="approval-management">
    <!-- 筛选栏 -->
    <div class="filter-bar">
      <el-input
        v-model="keyword"
        placeholder="搜索报销人或事由"
        clearable
        style="width: 250px"
        @input="handleSearch"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>

      <el-select
        v-model="filterDepartment"
        placeholder="全部部门"
        clearable
        style="width: 150px"
        @change="handleFilter"
      >
        <el-option label="技术部" value="DEPT001" />
        <el-option label="销售部" value="DEPT002" />
        <el-option label="市场部" value="DEPT003" />
        <el-option label="财务部" value="DEPT004" />
      </el-select>

      <el-select
        v-model="filterType"
        placeholder="全部类型"
        clearable
        style="width: 150px"
        @change="handleFilter"
      >
        <el-option label="差旅费" value="travel" />
        <el-option label="招待费" value="hospitality" />
        <el-option label="办公用品" value="office" />
        <el-option label="交通费" value="transport" />
        <el-option label="其他费用" value="other" />
      </el-select>

      <el-select
        v-model="filterApprovalType"
        placeholder="审批类型"
        clearable
        style="width: 150px"
        @change="handleFilter"
      >
        <el-option label="部门审批" value="department" />
        <el-option label="财务审批" value="finance" />
      </el-select>
    </div>

    <!-- 待审批列表 -->
    <div v-loading="expenseStore.loading" class="approval-list">
      <el-empty v-if="expenseStore.pendingApprovals.length === 0" description="暂无待审批申请" />

      <div
        v-for="expense in filteredExpenses"
        :key="expense.id"
        class="approval-card"
      >
        <!-- 卡片头部 -->
        <div class="card-header">
          <div class="applicant-info">
            <el-avatar :size="40">{{ expense.applicantName.charAt(0) }}</el-avatar>
            <div class="applicant-detail">
              <div class="applicant-name">{{ expense.applicantName }}</div>
              <div class="applicant-dept">{{ expense.departmentName }}</div>
            </div>
          </div>
          <div class="expense-summary">
            <el-tag :type="getExpenseTypeTagType(expense.type)">
              {{ getExpenseTypeName(expense.type) }}
            </el-tag>
            <span class="amount">{{ formatAmount(expense.amount) }}</span>
          </div>
        </div>

        <!-- 卡片内容 -->
        <div class="card-content">
          <div class="expense-date">
            <el-icon><Calendar /></el-icon>
            <span>报销日期: {{ formatDate(expense.expenseDate) }}</span>
          </div>
          <div class="reason">
            <span class="reason-label">事由:</span>
            <span class="reason-text">{{ expense.reason }}</span>
          </div>

          <!-- 费用明细预览 -->
          <div class="items-preview">
            <div class="preview-title">费用明细 (共{{ expense.items.length }}项):</div>
            <div class="items-list">
              <div
                v-for="(item, index) in expense.items.slice(0, 2)"
                :key="index"
                class="item-row"
              >
                <span class="item-desc">{{ item.description }}</span>
                <span class="item-amount">{{ formatAmount(item.amount) }}</span>
              </div>
              <div v-if="expense.items.length > 2" class="more-items">
                还有{{ expense.items.length - 2 }}项...
              </div>
            </div>
          </div>

          <!-- 审批流程 -->
          <div class="approval-flow">
            <div class="flow-title">审批流程:</div>
            <div class="flow-steps">
              <!-- 部门审批 -->
              <div
                class="flow-step"
                :class="{
                  'current': expense.status === 'dept_pending',
                  'completed': expense.status === 'finance_pending' || expense.status === 'paid',
                  'rejected': expense.status === 'rejected' && expense.departmentApproval?.status === 'rejected'
                }"
              >
                <div class="step-number">1</div>
                <div class="step-info">
                  <div class="step-name">{{ expense.departmentApproval?.approverName || '部门主管' }}</div>
                  <div class="step-role">部门审批</div>
                </div>
                <div class="step-status">
                  <el-icon v-if="expense.departmentApproval?.status === 'approved'" class="success">
                    <Check />
                  </el-icon>
                  <el-icon v-else-if="expense.status === 'dept_pending'" class="current">
                    <Clock />
                  </el-icon>
                  <el-icon v-else-if="expense.departmentApproval?.status === 'rejected'" class="rejected">
                    <Close />
                  </el-icon>
                  <el-icon v-else class="pending"><MoreFilled /></el-icon>
                </div>
              </div>

              <!-- 财务审批 -->
              <div
                class="flow-step"
                :class="{
                  'current': expense.status === 'finance_pending',
                  'completed': expense.status === 'paid',
                  'rejected': expense.status === 'rejected' && expense.financeApproval?.status === 'rejected'
                }"
              >
                <div class="step-number">2</div>
                <div class="step-info">
                  <div class="step-name">{{ expense.financeApproval?.approverName || '财务人员' }}</div>
                  <div class="step-role">财务审批</div>
                </div>
                <div class="step-status">
                  <el-icon v-if="expense.financeApproval?.status === 'approved'" class="success">
                    <Check />
                  </el-icon>
                  <el-icon v-else-if="expense.status === 'finance_pending'" class="current">
                    <Clock />
                  </el-icon>
                  <el-icon v-else-if="expense.financeApproval?.status === 'rejected'" class="rejected">
                    <Close />
                  </el-icon>
                  <el-icon v-else class="pending"><MoreFilled /></el-icon>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 操作按钮 -->
        <div class="card-footer">
          <el-button size="small" @click="handleView(expense)">
            查看详情
          </el-button>
          <el-button
            type="success"
            size="small"
            @click="handleApprove(expense)"
            :disabled="!canApprove(expense)"
          >
            通过
          </el-button>
          <el-button
            type="danger"
            size="small"
            @click="handleReject(expense)"
            :disabled="!canApprove(expense)"
          >
            驳回
          </el-button>
        </div>
      </div>
    </div>

    <!-- 审批对话框 -->
    <el-dialog
      v-model="showApprovalDialog"
      :title="approvalTitle"
      width="500px"
    >
      <div v-if="currentExpense" class="approval-dialog-content">
        <!-- 报销单简要信息 -->
        <div class="expense-summary-info">
          <div class="info-row">
            <span class="label">报销人:</span>
            <span class="value">{{ currentExpense.applicantName }}</span>
          </div>
          <div class="info-row">
            <span class="label">部门:</span>
            <span class="value">{{ currentExpense.departmentName }}</span>
          </div>
          <div class="info-row">
            <span class="label">类型:</span>
            <span class="value">{{ getExpenseTypeName(currentExpense.type) }}</span>
          </div>
          <div class="info-row">
            <span class="label">金额:</span>
            <span class="value amount-highlight">{{ formatAmount(currentExpense.amount) }}</span>
          </div>
          <div class="info-row">
            <span class="label">事由:</span>
            <span class="value">{{ currentExpense.reason }}</span>
          </div>
        </div>

        <el-divider />

        <el-form
          ref="approvalFormRef"
          :model="approvalForm"
          label-width="80px"
        >
          <el-form-item label="审批意见">
            <el-input
              v-model="approvalForm.opinion"
              type="textarea"
              :rows="4"
              :placeholder="approvalPlaceholder"
              maxlength="200"
              show-word-limit
            />
          </el-form-item>
        </el-form>
      </div>

      <template #footer>
        <el-button @click="showApprovalDialog = false">取消</el-button>
        <el-button
          :type="approvalAction === 'approve' ? 'success' : 'danger'"
          @click="handleConfirmApproval"
          :loading="loading"
        >
          确认{{ approvalAction === 'approve' ? '通过' : '驳回' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 详情抽屉 -->
    <ExpenseDetail
      v-model="showDetail"
      :expense-id="currentExpenseId"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Calendar, Check, Clock, MoreFilled, Close } from '@element-plus/icons-vue'
import { useExpenseStore } from '../store'
import {
  formatDate,
  formatAmount,
  getExpenseTypeName,
  getExpenseStatusName,
  getExpenseStatusType,
  canDeptApprove,
  canFinanceApprove
} from '../utils'
import type { Expense, ExpenseType, ExpenseStatus } from '../types'
import ExpenseDetail from './ExpenseDetail.vue'

const expenseStore = useExpenseStore()

const keyword = ref('')
const filterDepartment = ref<string>()
const filterType = ref<ExpenseType>()
const filterApprovalType = ref<'department' | 'finance'>()
const showDetail = ref(false)
const showApprovalDialog = ref(false)
const currentExpenseId = ref<string>()
const currentExpense = ref<Expense | null>(null)
const approvalAction = ref<'approve' | 'reject'>('approve')
const loading = ref(false)

const approvalFormRef = ref()
const approvalForm = reactive({
  opinion: ''
})

// 筛选后的报销列表
const filteredExpenses = computed(() => {
  let expenses = [...expenseStore.pendingApprovals]

  // 关键字搜索
  if (keyword.value) {
    expenses = expenses.filter(exp =>
      exp.applicantName.includes(keyword.value) ||
      exp.reason.includes(keyword.value)
    )
  }

  // 部门筛选
  if (filterDepartment.value) {
    expenses = expenses.filter(exp => exp.departmentId === filterDepartment.value)
  }

  // 类型筛选
  if (filterType.value) {
    expenses = expenses.filter(exp => exp.type === filterType.value)
  }

  // 审批类型筛选
  if (filterApprovalType.value === 'department') {
    expenses = expenses.filter(exp => exp.status === 'dept_pending')
  } else if (filterApprovalType.value === 'finance') {
    expenses = expenses.filter(exp => exp.status === 'finance_pending')
  }

  return expenses
})

const approvalTitle = computed(() => {
  return approvalAction.value === 'approve' ? '通过报销申请' : '驳回报销申请'
})

const approvalPlaceholder = computed(() => {
  return approvalAction.value === 'approve'
    ? '请填写通过意见(可选)'
    : '请填写驳回理由(必填)'
})

onMounted(() => {
  loadApprovals()
})

async function loadApprovals() {
  try {
    await expenseStore.loadPendingApprovals({
      departmentId: filterDepartment.value,
      approvalType: filterApprovalType.value
    })
  } catch (error: any) {
    ElMessage.error(error.message || '加载失败')
  }
}

function handleSearch() {
  // 前端搜索通过 computed 自动更新
}

function handleFilter() {
  loadApprovals()
}

function handleView(expense: Expense) {
  currentExpenseId.value = expense.id
  showDetail.value = true
}

function handleApprove(expense: Expense) {
  currentExpense.value = expense
  approvalAction.value = 'approve'
  approvalForm.opinion = ''
  showApprovalDialog.value = true
}

function handleReject(expense: Expense) {
  currentExpense.value = expense
  approvalAction.value = 'reject'
  approvalForm.opinion = ''
  showApprovalDialog.value = true
}

// 判断是否可以审批
function canApprove(expense: Expense): boolean {
  return expense.status === 'dept_pending' || expense.status === 'finance_pending'
}

async function handleConfirmApproval() {
  if (approvalAction.value === 'reject' && !approvalForm.opinion) {
    ElMessage.warning('请填写驳回理由')
    return
  }

  if (!currentExpense.value) {
    ElMessage.error('未选择报销单')
    return
  }

  loading.value = true
  try {
    const expense = currentExpense.value
    const approvalData = {
      status: approvalAction.value === 'approve' ? 'approved' : 'rejected',
      opinion: approvalForm.opinion
    }

    // 根据状态判断是部门审批还是财务审批
    if (expense.status === 'dept_pending') {
      await expenseStore.departmentApprove(expense.id, approvalData)
    } else if (expense.status === 'finance_pending') {
      await expenseStore.financeApprove(expense.id, approvalData)
    }

    ElMessage.success(approvalAction.value === 'approve' ? '已通过' : '已驳回')
    showApprovalDialog.value = false
    loadApprovals()
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')
  } finally {
    loading.value = false
  }
}

// 获取报销类型标签类型
function getExpenseTypeTagType(type: ExpenseType): string {
  const typeMap: Record<ExpenseType, string> = {
    travel: 'primary',
    hospitality: 'warning',
    office: 'success',
    transport: 'info',
    other: ''
  }
  return typeMap[type] || ''
}
</script>

<style scoped lang="scss">
.approval-management {
  .filter-bar {
    display: flex;
    gap: 12px;
    margin-bottom: 20px;
  }

  .approval-list {
    min-height: 400px;

    .approval-card {
      background: white;
      border: 1px solid #EBEEF5;
      border-radius: 8px;
      padding: 16px;
      margin-bottom: 16px;

      .card-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 16px;

        .applicant-info {
          display: flex;
          gap: 12px;

          .applicant-detail {
            .applicant-name {
              font-size: 15px;
              font-weight: 600;
              color: #303133;
              margin-bottom: 4px;
            }

            .applicant-dept {
              font-size: 13px;
              color: #909399;
            }
          }
        }

        .expense-summary {
          display: flex;
          align-items: center;
          gap: 12px;

          .amount {
            font-size: 18px;
            font-weight: 600;
            color: #E6A23C;
          }
        }
      }

      .card-content {
        .expense-date {
          display: flex;
          align-items: center;
          gap: 8px;
          margin-bottom: 12px;
          font-size: 14px;
          color: #606266;

          .el-icon {
            color: #409EFF;
          }
        }

        .reason {
          margin-bottom: 16px;
          font-size: 14px;
          color: #606266;

          .reason-label {
            font-weight: 500;
            color: #909399;
          }

          .reason-text {
            margin-left: 8px;
          }
        }

        .items-preview {
          margin-bottom: 16px;
          padding: 12px;
          background: #F5F7FA;
          border-radius: 6px;

          .preview-title {
            font-size: 13px;
            color: #909399;
            margin-bottom: 8px;
          }

          .items-list {
            .item-row {
              display: flex;
              justify-content: space-between;
              font-size: 13px;
              color: #606266;
              margin-bottom: 4px;

              &:last-child {
                margin-bottom: 0;
              }

              .item-desc {
                flex: 1;
              }

              .item-amount {
                font-weight: 500;
                color: #E6A23C;
              }
            }

            .more-items {
              font-size: 12px;
              color: #909399;
              margin-top: 4px;
            }
          }
        }

        .approval-flow {
          padding: 12px;
          background: #F5F7FA;
          border-radius: 6px;

          .flow-title {
            font-size: 13px;
            color: #909399;
            margin-bottom: 8px;
          }

          .flow-steps {
            display: flex;
            flex-direction: column;
            gap: 8px;

            .flow-step {
              display: flex;
              align-items: center;
              gap: 12px;
              padding: 8px;
              border-radius: 4px;
              background: white;
              transition: all 0.3s;

              &.current {
                background: #ECF5FF;
                border: 1px solid #409EFF;
              }

              &.completed {
                opacity: 0.7;
              }

              &.rejected {
                background: #FEF0F0;
                border: 1px solid #F56C6C;
              }

              .step-number {
                width: 28px;
                height: 28px;
                border-radius: 50%;
                background: #409EFF;
                color: white;
                display: flex;
                align-items: center;
                justify-content: center;
                font-size: 13px;
                font-weight: 600;
              }

              .step-info {
                flex: 1;

                .step-name {
                  font-size: 14px;
                  color: #303133;
                  margin-bottom: 2px;
                }

                .step-role {
                  font-size: 12px;
                  color: #909399;
                }
              }

              .step-status {
                .el-icon {
                  font-size: 18px;

                  &.success {
                    color: #67C23A;
                  }

                  &.current {
                    color: #409EFF;
                    animation: pulse 1.5s infinite;
                  }

                  &.rejected {
                    color: #F56C6C;
                  }

                  &.pending {
                    color: #DCDFE6;
                  }
                }
              }
            }
          }
        }
      }

      .card-footer {
        border-top: 1px solid #EBEEF5;
        padding-top: 12px;
        display: flex;
        gap: 8px;
      }
    }
  }
}

.approval-dialog-content {
  .expense-summary-info {
    .info-row {
      display: flex;
      margin-bottom: 12px;

      &:last-child {
        margin-bottom: 0;
      }

      .label {
        width: 80px;
        font-weight: 500;
        color: #909399;
      }

      .value {
        flex: 1;
        color: #303133;

        &.amount-highlight {
          color: #E6A23C;
          font-weight: 600;
          font-size: 16px;
        }
      }
    }
  }
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
}
</style>
