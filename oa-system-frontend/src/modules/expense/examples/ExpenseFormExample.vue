<template>
  <div class="expense-form-example">
    <!-- 操作栏 -->
    <div class="toolbar">
      <el-button type="primary" :icon="Plus" @click="handleCreate">
        新建报销
      </el-button>
      <el-button :icon="Refresh" @click="handleRefresh">
        刷新
      </el-button>
    </div>

    <!-- 报销单列表 -->
    <el-table :data="expenseList" border style="width: 100%" v-loading="loading">
      <el-table-column prop="serialNumber" label="报销单号" width="150" />
      <el-table-column prop="type" label="报销类型" width="120">
        <template #default="{ row }">
          <el-tag>{{ getTypeLabel(row.type) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="totalAmount" label="金额" width="120">
        <template #default="{ row }">
          <span class="amount">¥{{ row.totalAmount.toFixed(2) }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="120">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)">
            {{ getStatusLabel(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="applyDate" label="申请日期" width="120" />
      <el-table-column prop="reason" label="报销事由" min-width="200" show-overflow-tooltip />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleView(row)">
            查看
          </el-button>
          <el-button
            v-if="row.status === 'draft'"
            link
            type="primary"
            @click="handleEdit(row)"
          >
            编辑
          </el-button>
          <el-button
            v-if="row.status === 'draft'"
            link
            type="success"
            @click="handleSubmit(row)"
          >
            提交
          </el-button>
          <el-button
            v-if="row.status === 'draft'"
            link
            type="danger"
            @click="handleDelete(row)"
          >
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <el-pagination
      v-model:current-page="pagination.page"
      v-model:page-size="pagination.pageSize"
      :total="pagination.total"
      :page-sizes="[10, 20, 50, 100]"
      layout="total, sizes, prev, pager, next, jumper"
      @size-change="loadData"
      @current-change="loadData"
      style="margin-top: 20px; justify-content: flex-end"
    />

    <!-- 报销表单对话框 -->
    <expense-form
      v-model="formVisible"
      :expense-data="currentExpense"
      @success="handleFormSuccess"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh } from '@element-plus/icons-vue'
import ExpenseForm from '../components/ExpenseForm.vue'
import { EXPENSE_TYPE_OPTIONS, EXPENSE_STATUS_OPTIONS } from '../types'
import type { ExpenseRequest } from '../types'

// 报销单列表
const expenseList = ref<ExpenseRequest[]>([])
const loading = ref(false)

// 分页
const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

// 表单对话框
const formVisible = ref(false)
const currentExpense = ref<ExpenseRequest | null>(null)

// 初始化
onMounted(() => {
  loadData()
})

// 加载数据
async function loadData() {
  loading.value = true
  try {
    // TODO: 替换为实际的API调用
    // const response = await expenseApi.getList({
    //   page: pagination.page,
    //   pageSize: pagination.pageSize
    // })

    // 模拟数据
    await new Promise(resolve => setTimeout(resolve, 500))
    expenseList.value = [
      {
        id: '1',
        serialNumber: 'EXP202601100001',
        applicantId: 'EMP000001',
        applicantName: '张三',
        department: '技术部',
        type: 'travel',
        expenseDate: '2026-01-05',
        reason: '参加客户会议,前往北京洽谈合作项目',
        items: [
          {
            id: 1,
            description: '北京往返机票',
            amount: 1200,
            date: '2026-01-05',
            category: 'transport'
          },
          {
            id: 2,
            description: '住宿费',
            amount: 400,
            date: '2026-01-05',
            category: 'hotel'
          }
        ],
        invoices: [
          {
            id: 1,
            type: 'vat_common',
            number: '12345678',
            amount: 1600,
            date: '2026-01-05',
            imageUrl: '',
            fileList: []
          }
        ],
        totalAmount: 1600,
        status: 'draft',
        applyDate: '2026-01-10'
      },
      {
        id: '2',
        serialNumber: 'EXP202601100002',
        applicantId: 'EMP000001',
        applicantName: '张三',
        department: '技术部',
        type: 'entertainment',
        expenseDate: '2026-01-08',
        reason: '招待重要客户,商谈合作事宜',
        items: [
          {
            id: 3,
            description: '餐饮费',
            amount: 800,
            date: '2026-01-08',
            category: 'meal'
          }
        ],
        invoices: [
          {
            id: 2,
            type: 'vat_common',
            number: '87654321',
            amount: 800,
            date: '2026-01-08',
            imageUrl: '',
            fileList: []
          }
        ],
        totalAmount: 800,
        status: 'dept_pending',
        applyDate: '2026-01-09'
      }
    ]
    pagination.total = 2
  } catch (error) {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

// 新建报销
function handleCreate() {
  currentExpense.value = null
  formVisible.value = true
}

// 编辑报销
function handleEdit(row: ExpenseRequest) {
  currentExpense.value = row
  formVisible.value = true
}

// 查看详情
function handleView(row: ExpenseRequest) {
  ElMessage.info('查看详情功能开发中')
  // TODO: 跳转到详情页或打开详情对话框
}

// 提交申请
async function handleSubmit(row: ExpenseRequest) {
  try {
    await ElMessageBox.confirm(
      '确定要提交该报销申请吗?',
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    // TODO: 调用提交API
    await new Promise(resolve => setTimeout(resolve, 500))
    ElMessage.success('提交成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('提交失败')
    }
  }
}

// 删除报销
async function handleDelete(row: ExpenseRequest) {
  try {
    await ElMessageBox.confirm(
      '确定要删除该报销单吗?删除后无法恢复!',
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    // TODO: 调用删除API
    await new Promise(resolve => setTimeout(resolve, 500))
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 刷新
function handleRefresh() {
  loadData()
}

// 表单成功回调
function handleFormSuccess() {
  loadData()
}

// 获取类型标签
function getTypeLabel(type: string) {
  const option = EXPENSE_TYPE_OPTIONS.find(item => item.value === type)
  return option?.label || type
}

// 获取状态标签
function getStatusLabel(status: string) {
  const option = EXPENSE_STATUS_OPTIONS.find(item => item.value === status)
  return option?.label || status
}

// 获取状态类型
function getStatusType(status: string) {
  const typeMap: Record<string, any> = {
    draft: '',
    dept_pending: 'warning',
    finance_pending: 'primary',
    paid: 'success',
    rejected: 'danger'
  }
  return typeMap[status] || ''
}
</script>

<style scoped lang="scss">
.expense-form-example {
  padding: 20px;

  .toolbar {
    margin-bottom: 20px;
    display: flex;
    gap: 12px;
  }

  .amount {
    color: #F56C6C;
    font-weight: bold;
  }

  :deep(.el-pagination) {
    display: flex;
  }
}
</style>
