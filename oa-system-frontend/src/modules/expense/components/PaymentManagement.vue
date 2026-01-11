<template>
  <div class="payment-management">
    <!-- 筛选条件 -->
    <div class="filter-section">
      <el-form :inline="true" :model="filterForm" class="filter-form">
        <el-form-item label="打款状态">
          <el-select v-model="filterForm.status" placeholder="全部状态" clearable @change="handleFilter">
            <el-option label="待打款" value="pending" />
            <el-option label="已完成" value="completed" />
            <el-option label="打款失败" value="failed" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期范围">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            @change="handleDateChange"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleFilter">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 打款列表 -->
    <el-card class="table-card">
      <el-table
        v-loading="expenseStore.loading"
        :data="expenseStore.paymentList"
        border
        stripe
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="expenseId" label="报销单号" width="150" />
        <el-table-column prop="amount" label="打款金额" width="120" align="right">
          <template #default="{ row }">
            <span class="amount-text">{{ formatAmount(row.amount) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="paymentMethod" label="打款方式" width="120">
          <template #default="{ row }">
            {{ getPaymentMethodName(row.paymentMethod) }}
          </template>
        </el-table-column>
        <el-table-column prop="paymentDate" label="打款日期" width="120">
          <template #default="{ row }">
            {{ formatDate(row.paymentDate) }}
          </template>
        </el-table-column>
        <el-table-column prop="bankAccount" label="银行账号" width="180" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getPaymentStatusType(row.status)">
              {{ getPaymentStatusName(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="150" show-overflow-tooltip />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 'pending'"
              type="primary"
              size="small"
              @click="handleUploadProof(row)"
            >
              上传凭证
            </el-button>
            <el-button
              type="info"
              size="small"
              @click="handleViewDetail(row)"
            >
              查看详情
            </el-button>
            <el-button
              v-if="row.status === 'completed'"
              type="success"
              size="small"
              @click="handleViewProof(row)"
            >
              查看凭证
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 批量操作 -->
      <div v-if="selectedPayments.length > 0" class="batch-actions">
        <el-button type="primary" @click="handleBatchUploadProof">
          批量上传凭证
        </el-button>
        <el-button @click="handleClearSelection">
          清空选择
        </el-button>
        <span class="selection-info">已选择 {{ selectedPayments.length }} 项</span>
      </div>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 上传凭证对话框 -->
    <el-dialog
      v-model="showUploadDialog"
      title="上传打款凭证"
      width="500px"
    >
      <el-form :model="uploadForm" label-width="100px">
        <el-form-item label="凭证图片">
          <el-upload
            class="proof-uploader"
            :action="uploadAction"
            :show-file-list="false"
            :on-success="handleUploadSuccess"
            :before-upload="beforeUpload"
          >
            <img v-if="uploadForm.proof" :src="uploadForm.proof" class="proof-image" />
            <el-icon v-else class="proof-uploader-icon"><Plus /></el-icon>
          </el-upload>
          <div class="upload-tip">只能上传JPG/PNG文件，且不超过5MB</div>
        </el-form-item>
        <el-form-item label="备注">
          <el-input
            v-model="uploadForm.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入备注信息"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showUploadDialog = false">取消</el-button>
        <el-button type="primary" @click="handleConfirmUpload" :loading="uploading">
          确认上传
        </el-button>
      </template>
    </el-dialog>

    <!-- 查看凭证对话框 -->
    <el-dialog
      v-model="showViewDialog"
      title="打款凭证"
      width="600px"
    >
      <div class="proof-view">
        <el-image
          :src="currentProof"
          fit="contain"
          style="width: 100%"
        />
      </div>
    </el-dialog>

    <!-- 报销单详情 -->
    <ExpenseDetail
      v-model="showDetail"
      :expense-id="currentExpenseId"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { useExpenseStore } from '../store'
import { formatAmount, formatDate, getPaymentMethodName, getPaymentStatusName } from '../utils'
import ExpenseDetail from './ExpenseDetail.vue'
import type { PaymentRecord } from '../types'

const expenseStore = useExpenseStore()

// 筛选表单
const filterForm = ref({
  status: undefined as string | undefined,
  startDate: '',
  endDate: ''
})

const dateRange = ref<string[]>([])

// 分页
const pagination = ref({
  page: 1,
  pageSize: 10,
  total: 0
})

// 选中的项
const selectedPayments = ref<PaymentRecord[]>([])

// 上传对话框
const showUploadDialog = ref(false)
const uploading = ref(false)
const uploadForm = ref({
  proof: '',
  remark: ''
})
const currentPaymentId = ref<number | null>(null)

// 上传配置
const uploadAction = computed(() => {
  // TODO: 替换为实际的上传接口地址
  return '/api/upload'
})

// 查看凭证对话框
const showViewDialog = ref(false)
const currentProof = ref('')

// 报销单详情
const showDetail = ref(false)
const currentExpenseId = ref<string | null>(null)

onMounted(() => {
  loadPayments()
})

async function loadPayments() {
  try {
    const result = await expenseStore.loadPayments({
      status: filterForm.value.status as any,
      page: pagination.value.page,
      size: pagination.value.pageSize
    })
    pagination.value.total = result.total
  } catch (error: any) {
    ElMessage.error(error.message || '加载打款列表失败')
  }
}

function handleDateChange(value: string[]) {
  if (value && value.length === 2) {
    filterForm.value.startDate = value[0]
    filterForm.value.endDate = value[1]
  }
}

function handleFilter() {
  pagination.value.page = 1
  loadPayments()
}

function handleReset() {
  filterForm.value = {
    status: undefined,
    startDate: '',
    endDate: ''
  }
  dateRange.value = []
  pagination.value.page = 1
  loadPayments()
}

function handleSelectionChange(selection: PaymentRecord[]) {
  selectedPayments.value = selection
}

function handleClearSelection() {
  selectedPayments.value = []
}

function handleSizeChange(size: number) {
  pagination.value.pageSize = size
  loadPayments()
}

function handleCurrentChange(page: number) {
  pagination.value.page = page
  loadPayments()
}

function handleUploadProof(row: PaymentRecord) {
  currentPaymentId.value = row.id || null
  uploadForm.value = {
    proof: row.proof || '',
    remark: row.remark || ''
  }
  showUploadDialog.value = true
}

async function handleBatchUploadProof() {
  if (selectedPayments.value.length === 0) {
    ElMessage.warning('请先选择要上传凭证的记录')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确认为选中的 ${selectedPayments.value.length} 条记录批量上传凭证吗?`,
      '批量上传凭证',
      {
        type: 'warning'
      }
    )

    // TODO: 实现批量上传逻辑
    ElMessage.success('批量上传成功')
    loadPayments()
  } catch {
    // 用户取消
  }
}

function beforeUpload(file: File) {
  const isJPG = file.type === 'image/jpeg' || file.type === 'image/png'
  const isLt5M = file.size / 1024 / 1024 < 5

  if (!isJPG) {
    ElMessage.error('只能上传JPG/PNG格式的图片!')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过5MB!')
    return false
  }
  return true
}

function handleUploadSuccess(response: any) {
  // TODO: 根据实际接口返回格式调整
  uploadForm.value.proof = response.url || response.data?.url
  ElMessage.success('图片上传成功')
}

async function handleConfirmUpload() {
  if (!uploadForm.value.proof) {
    ElMessage.warning('请先上传凭证图片')
    return
  }

  if (!currentPaymentId.value) {
    ElMessage.error('无效的打款记录')
    return
  }

  try {
    uploading.value = true
    await expenseStore.uploadPaymentProof(currentPaymentId.value, uploadForm.value.proof)
    ElMessage.success('凭证上传成功')
    showUploadDialog.value = false
    loadPayments()
  } catch (error: any) {
    ElMessage.error(error.message || '上传失败')
  } finally {
    uploading.value = false
  }
}

function handleViewProof(row: PaymentRecord) {
  if (!row.proof) {
    ElMessage.warning('暂无打款凭证')
    return
  }
  currentProof.value = row.proof
  showViewDialog.value = true
}

function handleViewDetail(row: PaymentRecord) {
  currentExpenseId.value = row.expenseId
  showDetail.value = true
}

function getPaymentStatusType(status: string): string {
  const typeMap: Record<string, string> = {
    pending: 'warning',
    completed: 'success',
    failed: 'danger'
  }
  return typeMap[status] || 'info'
}
</script>

<style scoped lang="scss">
.payment-management {
  padding: 0;
}

.filter-section {
  margin-bottom: 20px;
  padding: 20px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);

  .filter-form {
    margin: 0;

    :deep(.el-form-item) {
      margin-bottom: 0;
    }
  }
}

.table-card {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);

  :deep(.el-card__body) {
    padding: 20px;
  }
}

.amount-text {
  font-weight: 600;
  color: #F56C6C;
}

.batch-actions {
  margin-top: 16px;
  padding: 12px;
  background: #F5F7FA;
  border-radius: 4px;
  display: flex;
  align-items: center;
  gap: 12px;

  .selection-info {
    margin-left: auto;
    font-size: 13px;
    color: #909399;
  }
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.proof-uploader {
  :deep(.el-upload) {
    border: 1px dashed #d9d9d9;
    border-radius: 6px;
    cursor: pointer;
    position: relative;
    overflow: hidden;
    transition: all 0.3s;

    &:hover {
      border-color: #409EFF;
    }
  }
}

.proof-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 178px;
  height: 178px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.proof-image {
  width: 178px;
  height: 178px;
  display: block;
  object-fit: cover;
}

.upload-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 8px;
}

.proof-view {
  display: flex;
  justify-content: center;
  align-items: center;
}
</style>
