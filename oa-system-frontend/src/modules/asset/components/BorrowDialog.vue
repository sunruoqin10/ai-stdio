<template>
  <el-dialog
    v-model="visible"
    title="资产借出"
    width="500px"
    @close="handleClose"
  >
    <div v-if="asset" class="borrow-dialog">
      <div class="asset-info">
        <el-image
          v-if="asset.images && asset.images.length > 0"
          :src="asset.images[0]"
          fit="cover"
          class="asset-thumb"
        />
        <div v-else class="asset-thumb placeholder">
          <el-icon :size="32"><Picture /></el-icon>
        </div>
        <div class="asset-detail">
          <div class="asset-name">{{ asset.name }}</div>
          <div class="asset-meta">
            <span>资产编号: {{ asset.id }}</span>
            <span>{{ getCategoryName(asset.category) }}</span>
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
          </el-select>
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
import { ref, reactive, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Picture } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { useAssetStore } from '../store'
import { getCategoryName } from '../utils'
import type { Asset, BorrowForm } from '../types'

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

// 模拟用户列表
const userList = ref<User[]>([
  { id: 'EMP000001', name: '张三', avatar: 'https://i.pravatar.cc/150?img=1' },
  { id: 'EMP000002', name: '李四', avatar: 'https://i.pravatar.cc/150?img=2' },
  { id: 'EMP000003', name: '王五', avatar: 'https://i.pravatar.cc/150?img=3' },
  { id: 'EMP000004', name: '赵六', avatar: 'https://i.pravatar.cc/150?img=4' },
  { id: 'EMP000005', name: '孙七', avatar: 'https://i.pravatar.cc/150?img=5' }
])

const form = reactive<BorrowForm>({
  borrowerId: '',
  expectedReturnDate: '',
  notes: ''
})

const rules: FormRules = {
  borrowerId: [{ required: true, message: '请选择借用人', trigger: 'change' }],
  expectedReturnDate: [{ required: true, message: '请选择预计归还日期', trigger: 'change' }]
}

watch(
  () => props.modelValue,
  (val) => {
    visible.value = val
    if (!val) {
      resetForm()
    }
  }
)

watch(visible, (val) => {
  emit('update:modelValue', val)
})

// 禁用今天之前的日期
function disabledDate(time: Date) {
  return time.getTime() < Date.now() - 24 * 60 * 60 * 1000
}

async function handleSubmit() {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return
    if (!props.asset) return

    loading.value = true
    try {
      await assetStore.borrow(props.asset.id, form)
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
