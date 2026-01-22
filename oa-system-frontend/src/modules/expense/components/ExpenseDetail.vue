<template>
  <el-drawer
    v-model="visible"
    title="报销单详情"
    size="700px"
    @close="handleClose"
  >
    <div v-if="expense" class="expense-detail">
      <!-- 基本信息 -->
      <section class="detail-section">
        <h3 class="section-title">基本信息</h3>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="报销单号">
            {{ expense.id }}
          </el-descriptions-item>
          <el-descriptions-item label="申请人">
            {{ expense.applicantName }}
          </el-descriptions-item>
          <el-descriptions-item label="部门">
            {{ expense.departmentName }}
          </el-descriptions-item>
          <el-descriptions-item label="报销类型">
            <el-tag :type="getTypeTagType(expense.type)">
              {{ getExpenseTypeName(expense.type) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="报销金额">
            <span class="amount-large">{{ formatAmount(expense.amount) }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="报销状态">
            <el-tag :type="getExpenseStatusType(expense.status)" size="large">
              {{ getExpenseStatusName(expense.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="申请日期">
            {{ formatDate(expense.applyDate) }}
          </el-descriptions-item>
          <el-descriptions-item label="费用日期">
            {{ formatDate(expense.expenseDate) }}
          </el-descriptions-item>
        </el-descriptions>
      </section>

      <!-- 费用明细 -->
      <section class="detail-section">
        <h3 class="section-title">费用明细</h3>
        <el-table
          :data="expenseItems"
          border
          size="small"
        >
          <el-table-column prop="description" label="费用描述" />
          <el-table-column prop="category" label="分类" width="120" />
          <el-table-column prop="date" label="日期" width="120">
            <template #default="{ row }">
              {{ formatDate(row.date) }}
            </template>
          </el-table-column>
          <el-table-column prop="amount" label="金额" width="120" align="right">
            <template #default="{ row }">
              {{ formatAmount(row.amount) }}
            </template>
          </el-table-column>
        </el-table>
      </section>

      <!-- 发票信息 -->
      <section class="detail-section">
        <h3 class="section-title">发票信息</h3>
        <el-table
          :data="expenseInvoices"
          border
          size="small"
        >
          <el-table-column prop="type" label="发票类型" width="150">
            <template #default="{ row }">
              {{ getInvoiceTypeName(row.type) }}
            </template>
          </el-table-column>
          <el-table-column prop="number" label="发票号码" width="150" />
          <el-table-column prop="amount" label="金额" width="120" align="right">
            <template #default="{ row }">
              {{ formatAmount(row.amount) }}
            </template>
          </el-table-column>
          <el-table-column prop="date" label="开票日期" width="120">
            <template #default="{ row }">
              {{ formatDate(row.date) }}
            </template>
          </el-table-column>
          <el-table-column label="验证状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.verified ? 'success' : 'warning'" size="small">
                {{ row.verified ? '已验证' : '未验证' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="发票图片" width="100">
            <template #default="{ row }">
              <el-image
                v-if="row.imageUrl"
                :src="row.imageUrl"
                :preview-src-list="[row.imageUrl]"
                fit="cover"
                style="width: 60px; height: 60px; border-radius: 4px; cursor: pointer"
              />
            </template>
          </el-table-column>
        </el-table>
      </section>

      <!-- 报销事由 -->
      <section class="detail-section">
        <h3 class="section-title">报销事由</h3>
        <div class="reason-content">
          {{ expense.reason }}
        </div>
      </section>

      <!-- 审批记录 -->
      <section v-if="hasApprovalRecords" class="detail-section">
        <h3 class="section-title">审批记录</h3>
        <el-timeline>
          <!-- 部门审批 -->
          <el-timeline-item
            v-if="expense.departmentApproval"
            :timestamp="expense.departmentApproval.timestamp ? formatDateTime(expense.departmentApproval.timestamp) : '待审批'"
            placement="top"
            :type="getTimelineType(expense.departmentApproval.status)"
          >
            <el-card>
              <div class="approver-info">
                <div class="approver-header">
                  <span class="approver-name">{{ expense.departmentApproval.approverName || '部门主管' }}</span>
                  <el-tag :type="getApprovalTagType(expense.departmentApproval.status)" size="small">
                    部门审批 - {{ getApprovalStatusName(expense.departmentApproval.status) }}
                  </el-tag>
                </div>
                <div v-if="expense.departmentApproval.opinion" class="approver-opinion">
                  <span class="opinion-label">审批意见:</span>
                  <span class="opinion-text">{{ expense.departmentApproval.opinion }}</span>
                </div>
              </div>
            </el-card>
          </el-timeline-item>

          <!-- 财务审批 -->
          <el-timeline-item
            v-if="expense.financeApproval"
            :timestamp="expense.financeApproval.timestamp ? formatDateTime(expense.financeApproval.timestamp) : '待审批'"
            placement="top"
            :type="getTimelineType(expense.financeApproval.status)"
          >
            <el-card>
              <div class="approver-info">
                <div class="approver-header">
                  <span class="approver-name">{{ expense.financeApproval.approverName || '财务人员' }}</span>
                  <el-tag :type="getApprovalTagType(expense.financeApproval.status)" size="small">
                    财务审批 - {{ getApprovalStatusName(expense.financeApproval.status) }}
                  </el-tag>
                </div>
                <div v-if="expense.financeApproval.opinion" class="approver-opinion">
                  <span class="opinion-label">审批意见:</span>
                  <span class="opinion-text">{{ expense.financeApproval.opinion }}</span>
                </div>
              </div>
            </el-card>
          </el-timeline-item>
        </el-timeline>
      </section>

      <!-- 打款信息 -->
      <section v-if="expense.status === 'paid'" class="detail-section">
        <h3 class="section-title">打款信息</h3>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="打款日期">
            {{ formatDate(expense.paymentDate || '') }}
          </el-descriptions-item>
          <el-descriptions-item label="打款状态">
            <el-tag type="success">已打款</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="打款凭证" :span="2">
            <el-image
              v-if="expense.paymentProof"
              :src="expense.paymentProof"
              :preview-src-list="[expense.paymentProof]"
              fit="cover"
              style="width: 100px; height: 100px; border-radius: 4px; cursor: pointer"
            />
            <span v-else>暂无凭证</span>
          </el-descriptions-item>
        </el-descriptions>
      </section>

      <!-- 时间信息 -->
      <section class="detail-section">
        <h3 class="section-title">时间信息</h3>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="创建时间">
            {{ formatDateTime(expense.createdAt) }}
          </el-descriptions-item>
          <el-descriptions-item label="更新时间">
            {{ formatDateTime(expense.updatedAt) }}
          </el-descriptions-item>
        </el-descriptions>
      </section>
    </div>

    <template #footer>
      <el-button @click="handleClose">关闭</el-button>
      <el-button
        v-if="canEdit(expense?.status)"
        type="primary"
        @click="handleEdit"
      >
        编辑
      </el-button>
    </template>
  </el-drawer>
</template>

<script setup lang="ts">
import { ref, watch, computed } from 'vue'
import { useExpenseStore } from '../store'
import {
  formatDate,
  formatDateTime,
  formatAmount,
  getExpenseTypeName,
  getExpenseStatusName,
  getExpenseStatusType,
  getInvoiceTypeName,
  canEdit
} from '../utils'
import type { Expense, ApprovalStatus } from '../types'

interface Props {
  modelValue: boolean
  expenseId?: string | null
}

const props = withDefaults(defineProps<Props>(), {
  expenseId: null
})

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  'edit': [expense: Expense]
}>()

const expenseStore = useExpenseStore()
const visible = ref(false)
const expense = ref<Expense | null>(null)

// 是否有审批记录
const hasApprovalRecords = computed(() => {
  return expense.value?.departmentApproval || expense.value?.financeApproval
})

// 处理费用明细，确保始终是数组
const expenseItems = computed(() => {
  return expense.value?.items || []
})

// 处理发票信息，确保始终是数组
const expenseInvoices = computed(() => {
  return expense.value?.invoices || []
})

watch(
  () => props.modelValue,
  async (val) => {
    visible.value = val
    if (val && props.expenseId) {
      expense.value = await expenseStore.loadExpense(props.expenseId)
    }
  }
)

watch(visible, (val) => {
  emit('update:modelValue', val)
})

function getTypeTagType(type: string): string {
  const typeMap: Record<string, string> = {
    travel: 'primary',
    hospitality: 'danger',
    office: 'success',
    transport: 'warning',
    other: 'info'
  }
  return typeMap[type] || 'info'
}

function getTimelineType(status: ApprovalStatus): 'primary' | 'success' | 'danger' | 'info' {
  if (status === 'approved') return 'success'
  if (status === 'rejected') return 'danger'
  return 'primary'
}

function getApprovalTagType(status: ApprovalStatus): string {
  if (status === 'approved') return 'success'
  if (status === 'rejected') return 'danger'
  return 'info'
}

function getApprovalStatusName(status: ApprovalStatus): string {
  const statusMap: Record<ApprovalStatus, string> = {
    pending: '待审批',
    approved: '已通过',
    rejected: '已驳回'
  }
  return statusMap[status] || status
}

function handleClose() {
  emit('update:modelValue', false)
}

function handleEdit() {
  if (expense.value) {
    emit('edit', expense.value)
  }
}
</script>

<style scoped lang="scss">
.expense-detail {
  padding: 0 8px;
}

.detail-section {
  margin-bottom: 24px;

  &:last-child {
    margin-bottom: 0;
  }

  .section-title {
    font-size: 16px;
    font-weight: 600;
    color: #303133;
    margin: 0 0 16px 0;
    padding-bottom: 8px;
    border-bottom: 2px solid #409EFF;
  }
}

.amount-large {
  font-size: 18px;
  font-weight: 700;
  color: #F56C6C;
}

.reason-content {
  padding: 12px;
  background: #F5F7FA;
  border-radius: 4px;
  line-height: 1.6;
  color: #606266;
}

.approver-info {
  .approver-header {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 8px;

    .approver-name {
      font-size: 14px;
      font-weight: 600;
      color: #303133;
    }
  }

  .approver-opinion {
    font-size: 13px;
    color: #606266;

    .opinion-label {
      font-weight: 500;
      color: #909399;
    }

    .opinion-text {
      margin-left: 8px;
    }
  }
}

:deep(.el-timeline-item__timestamp) {
  color: #909399;
  font-size: 13px;
}

:deep(.el-card) {
  margin-bottom: 0;
}
</style>
