<template>
  <el-dialog
    v-model="visible"
    title="资产借出"
    width="500px"
    @close="handleClose"
  >
    <div v-if="currentAsset" class="borrow-dialog">
      <div class="asset-info">
        <el-image
          v-if="currentAsset.images && currentAsset.images.length > 0"
          :src="currentAsset.images[0]"
          fit="cover"
          class="asset-thumb"
        />
        <div v-else class="asset-thumb placeholder">
          <el-icon :size="32"><Picture /></el-icon>
        </div>
        <div class="asset-detail">
          <div class="asset-name">{{ currentAsset.name }}</div>
          <div class="asset-meta">
            <span>资产编号: {{ currentAsset.id }}</span>
            <span>{{ getCategoryName(currentAsset.category) }}</span>
          </div>
        </div>
      </div>

      <el-divider />

      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="借用人" prop="borrowerId">
          <el-select
            v-model="form.borrowerId"
            placeholder="请选择借用人"
            filterable
            :loading="loadingUsers"
            style="width: 100%"
          >
            <el-option
              v-for="user in userList"
              :key="user.id"
              :label="user.name"
              :value="user.id"
            >
              <div class="user-option">
                <el-avatar :src="user.avatar" :size="24" />
                <span>{{ user.name }}</span>
                <span class="user-id">({{ user.id }})</span>
              </div>
            </el-option>
            <el-option v-if="!loadingUsers && userList.length === 0" disabled value="">
              暂无可用的员工
            </el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="借出日期" prop="borrowDate">
          <el-date-picker
            v-model="form.borrowDate"
            type="date"
            placeholder="选择借出日期"
            style="width: 100%"
            :disabled-date="disabledBorrowDate"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>

        <el-form-item label="预计归还" prop="expectedReturnDate">
          <el-date-picker
            v-model="form.expectedReturnDate"
            type="date"
            placeholder="选择归还日期"
            style="width: 100%"
            :disabled-date="disabledDate"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>

        <el-form-item label="借用备注" prop="notes">
          <el-input
            v-model="form.notes"
            type="textarea"
            :rows="3"
            placeholder="请输入借用备注(可选)"
          />
        </el-form-item>
      </el-form>
    </div>

    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" @click="handleSubmit" :loading="loading">
        确认借出
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, watch, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Picture } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { useAssetStore } from '../store'
import { getCategoryName } from '../utils'
import type { Asset, BorrowForm } from '../types'
import { getEmployeeList } from '@/modules/employee/api'
import { EmployeeStatus } from '@/modules/employee/types'

interface User {
  id: string
  name: string
  avatar: string
}

interface Props {
  modelValue: boolean
  asset?: Asset | null
}

const props = withDefaults(defineProps<Props>(), {
  asset: null
})

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  'success': []
}>()

const assetStore = useAssetStore()
const formRef = ref<FormInstance>()
const loading = ref(false)
const visible = ref(false)
const loadingUsers = ref(false)

// 本地存储的资产引用（避免props.asset在异步操作中变为undefined）
const currentAsset = ref<Asset | null>(null)

// 从后端获取真实的用户列表
const userList = ref<User[]>([])

// 加载员工列表
async function loadUserList() {
  loadingUsers.value = true
  try {
    const result = await getEmployeeList({
      page: 1,
      pageSize: 100, // 获取前100个员工
      status: EmployeeStatus.ACTIVE // 只获取在职员工
    })

    userList.value = result.list.map(emp => ({
      id: emp.id,
      name: emp.name,
      avatar: emp.avatar || 'https://i.pravatar.cc/150?img=1' // 使用默认头像
    }))
  } catch (error: any) {
    console.error('加载员工列表失败:', error)
    ElMessage.error('加载员工列表失败')
    // 使用空列表
    userList.value = []
  } finally {
    loadingUsers.value = false
  }
}

const form = reactive<BorrowForm>({
  borrowerId: '',
  borrowDate: new Date().toISOString().split('T')[0] || '', // 默认为今天
  expectedReturnDate: '',
  notes: ''
})

const rules: FormRules = {
  borrowerId: [{ required: true, message: '请选择借用人', trigger: 'change' }],
  borrowDate: [{ required: true, message: '请选择借出日期', trigger: 'change' }],
  expectedReturnDate: [{ required: true, message: '请选择预计归还日期', trigger: 'change' }]
}

watch(
  () => props.modelValue,
  (val) => {
    visible.value = val
    if (val) {
      // 对话框打开时，保存资产引用
      currentAsset.value = props.asset || null
    } else {
      currentAsset.value = null
      resetForm()
    }
  }
)

watch(visible, (val) => {
  emit('update:modelValue', val)
})

// 组件挂载时加载员工列表
onMounted(() => {
  loadUserList()
})

// 禁用今天之后的日期作为借出日期
function disabledBorrowDate(time: Date) {
  return time.getTime() > Date.now()
}

// 禁用今天之前的日期作为归还日期
function disabledDate(time: Date) {
  // 如果已选择借出日期，则归还日期不能早于借出日期
  if (form.borrowDate) {
    const borrowDateTime = new Date(form.borrowDate).getTime()
    return time.getTime() < borrowDateTime
  }
  return time.getTime() < Date.now() - 24 * 60 * 60 * 1000
}

async function handleSubmit() {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return
    if (!currentAsset.value) {
      ElMessage.error('资产信息不存在')
      return
    }

    loading.value = true
    try {
      await assetStore.borrow(currentAsset.value.id, form)
      ElMessage.success('借出成功')
      emit('success')
      handleClose()
    } catch (error: any) {
      ElMessage.error(error.message || '借出失败')
    } finally {
      loading.value = false
    }
  })
}

function resetForm() {
  formRef.value?.resetFields()
  form.borrowerId = ''
  form.borrowDate = new Date().toISOString().split('T')[0] || '' // 重置为今天
  form.expectedReturnDate = ''
  form.notes = ''
}

function handleClose() {
  resetForm()
  emit('update:modelValue', false)
}
</script>

<style scoped lang="scss">
.borrow-dialog {
  padding: 8px;
}

.asset-info {
  display: flex;
  gap: 12px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 8px;
}

.asset-thumb {
  width: 80px;
  height: 80px;
  border-radius: 6px;
  overflow: hidden;
  flex-shrink: 0;

  &.placeholder {
    display: flex;
    align-items: center;
    justify-content: center;
    background: #e5e7eb;
    color: #909399;
  }

  :deep(.el-image) {
    width: 100%;
    height: 100%;
  }
}

.asset-detail {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.asset-name {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
}

.asset-meta {
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 13px;
  color: #909399;
}

.user-option {
  display: flex;
  align-items: center;
  gap: 8px;

  .user-id {
    font-size: 12px;
    color: #909399;
  }
}

:deep(.el-divider) {
  margin: 16px 0;
}
</style>
