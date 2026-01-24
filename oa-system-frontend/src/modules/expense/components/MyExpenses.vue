<template>
  <div class="my-expenses">
    <!-- 筛选栏 -->
    <div class="filter-bar">
      <el-select
        v-model="filterType"
        placeholder="全部类型"
        clearable
        style="width: 150px"
        @change="handleFilter"
      >
        <el-option
          v-for="item in expenseTypeOptions"
          :key="item.value"
          :label="item.label"
          :value="item.value"
        />
      </el-select>

      <el-select
        v-model="filterStatus"
        placeholder="全部状态"
        clearable
        style="width: 150px"
        @change="handleFilter"
      >
        <el-option
          v-for="item in expenseStatusOptions"
          :key="item.value"
          :label="item.label"
          :value="item.value"
        />
      </el-select>

      <el-date-picker
        v-model="dateRange"
        type="daterange"
        range-separator="至"
        start-placeholder="开始日期"
        end-placeholder="结束日期"
        value-format="YYYY-MM-DD"
        style="width: 280px"
        @change="handleFilter"
      />

      <el-button type="primary" @click="handleCreate">
        <el-icon><Plus /></el-icon>
        新建报销
      </el-button>
    </div>

    <!-- 报销列表表格 -->
    <div v-loading="expenseStore.loading" class="table-container">
      <el-table
        :data="expenseStore.myExpenses"
        stripe
        border
        style="width: 100%"
        @row-click="handleRowClick"
      >
        <el-table-column prop="id" label="报销单号" width="180" />
        <el-table-column prop="type" label="类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getTypeTagType(row.type)">
              {{ getExpenseTypeName(row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="amount" label="金额" width="120" align="right">
          <template #default="{ row }">
            <span class="amount-text">{{ formatAmount(row.amount) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getExpenseStatusType(row.status)" size="large">
              {{ getExpenseStatusName(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="applyDate" label="申请日期" width="120">
          <template #default="{ row }">
            {{ formatDate(row.applyDate) }}
          </template>
        </el-table-column>
        <el-table-column prop="expenseDate" label="费用日期" width="120">
          <template #default="{ row }">
            {{ formatDate(row.expenseDate) }}
          </template>
        </el-table-column>
        <el-table-column prop="departmentName" label="部门" width="120" />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click.stop="handleView(row)">
              查看详情
            </el-button>
            <el-button
              v-if="canEdit(row.status)"
              link
              type="primary"
              size="small"
              @click.stop="handleEdit(row)"
            >
              编辑
            </el-button>
            <el-button
              v-if="canSubmit(row.status)"
              link
              type="success"
              size="small"
              @click.stop="handleSubmit(row)"
            >
              提交
            </el-button>
            <el-button
              v-if="canCancel(row.status)"
              link
              type="warning"
              size="small"
              @click.stop="handleCancel(row)"
            >
              撤销
            </el-button>
            <el-button
              v-if="canDelete(row.status)"
              link
              type="danger"
              size="small"
              @click.stop="handleDelete(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty
        v-if="expenseStore.myExpenses.length === 0 && !expenseStore.loading"
        description="暂无报销记录"
      />
    </div>

    <!-- 分页 -->
    <el-pagination
      v-model:current-page="expenseStore.pagination.page"
      v-model:page-size="expenseStore.pagination.pageSize"
      :total="expenseStore.pagination.total"
      :page-sizes="[10, 20, 50]"
      layout="total, sizes, prev, pager, next, jumper"
      class="pagination"
      @size-change="handleSizeChange"
      @current-change="handlePageChange"
    />

    <!-- 详情抽屉 -->
    <ExpenseDetail
      v-model="showDetail"
      :expense-id="currentExpenseId"
      @edit="handleEdit"
    />

    <!-- 编辑对话框 -->
    <ExpenseForm
      v-model="showForm"
      :expense-data="currentExpense"
      @success="handleFormSuccess"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { useExpenseStore } from '../store'
import { useDictStore } from '@/modules/dict/store'
import {
  formatDate,
  formatAmount,
  getExpenseTypeName,
  getExpenseStatusName,
  getExpenseStatusType,
  canEdit,
  canDelete,
  canSubmit,
  canCancel
} from '../utils'
import type { Expense } from '../types'
import ExpenseDetail from './ExpenseDetail.vue'
import ExpenseForm from './ExpenseForm.vue'

const expenseStore = useExpenseStore()
const dictStore = useDictStore()

const filterType = ref<string>()
const filterStatus = ref<string>()
const dateRange = ref<[string, string]>()
const showDetail = ref(false)
const showForm = ref(false)
const currentExpenseId = ref<string>()
const currentExpense = ref<Expense | null>(null)

// 字典选项
const expenseTypeOptions = ref<Array<{ label: string; value: string }>>([])
const expenseStatusOptions = ref<Array<{ label: string; value: string }>>([])

onMounted(async () => {
  await loadDictData()
  loadExpenses()
})

// 加载字典数据
async function loadDictData() {
  try {
    // 加载费用类型字典
    const typeDict = await dictStore.fetchDictData('expense_type')
    expenseTypeOptions.value = typeDict.items

    // 加载报销状态字典
    const statusDict = await dictStore.fetchDictData('expense_status')
    expenseStatusOptions.value = statusDict.items
  } catch (error) {
    console.error('加载字典数据失败:', error)
  }
}

async function loadExpenses() {
  try {
    await expenseStore.loadMyExpenses({
      type: filterType.value,
      status: filterStatus.value,
      startDate: dateRange.value?.[0],
      endDate: dateRange.value?.[1]
    })
  } catch (error: any) {
    ElMessage.error(error.message || '加载失败')
  }
}

function handleFilter() {
  expenseStore.setFilter({
    type: filterType.value,
    status: filterStatus.value,
    startDate: dateRange.value?.[0],
    endDate: dateRange.value?.[1]
  })
  loadExpenses()
}

function handleCreate() {
  currentExpense.value = null
  showForm.value = true
}

function handleView(expense: Expense) {
  currentExpenseId.value = expense.id
  showDetail.value = true
}

function handleRowClick(row: Expense) {
  handleView(row)
}

async function handleEdit(expense: Expense) {
  try {
    // 获取完整的报销单详情（包含version字段）
    const { getExpense } = await import('../api')
    const fullExpense = await getExpense(expense.id)
    currentExpense.value = fullExpense
    showForm.value = true
  } catch (error: any) {
    ElMessage.error(error.message || '获取报销单详情失败')
  }
}

async function handleSubmit(expense: Expense) {
  try {
    await ElMessageBox.confirm(
      `确定要提交报销单 ${expense.id} 吗?`,
      '提交确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      }
    )

    await expenseStore.submitExpense(expense.id)
    ElMessage.success('提交成功')
    loadExpenses()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '提交失败')
    }
  }
}

async function handleCancel(expense: Expense) {
  try {
    await ElMessageBox.confirm(
      `确定要撤销报销单 ${expense.id} 吗?`,
      '撤销确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await expenseStore.cancelExpense(expense.id)
    ElMessage.success('撤销成功')
    loadExpenses()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '撤销失败')
    }
  }
}

async function handleDelete(expense: Expense) {
  try {
    await ElMessageBox.confirm(
      `确定要删除报销单 ${expense.id} 吗?此操作不可恢复。`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await expenseStore.deleteExpense(expense.id)
    ElMessage.success('删除成功')
    loadExpenses()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

function handleFormSuccess() {
  showForm.value = false
  currentExpense.value = null
  loadExpenses()
}

function handleSizeChange() {
  loadExpenses()
}

function handlePageChange() {
  loadExpenses()
}

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
</script>

<style scoped lang="scss">
.my-expenses {
  .filter-bar {
    display: flex;
    gap: 12px;
    margin-bottom: 20px;
    flex-wrap: wrap;
  }

  .table-container {
    min-height: 400px;
    margin-bottom: 20px;

    .el-table {
      cursor: pointer;

      :deep(.el-table__row) {
        &:hover {
          background-color: #F5F7FA;
        }
      }
    }

    .amount-text {
      font-weight: 600;
      color: #F56C6C;
      font-size: 15px;
    }
  }

  .pagination {
    display: flex;
    justify-content: flex-end;
    margin-top: 20px;
  }
}
</style>
