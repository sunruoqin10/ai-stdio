<template>
  <el-dialog
    v-model="visible"
    title="资产归还"
    width="500px"
    @close="handleClose"
  >
    <div v-if="currentAsset" class="return-dialog">
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

      <!-- 借用信息 -->
      <div class="borrow-info-section">
        <div class="info-title">借用信息</div>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="借用人">
            <div class="user-display">
              <el-avatar :src="currentAsset.userAvatar" :size="24" />
              <span>{{ currentAsset.userName }}</span>
            </div>
          </el-descriptions-item>
          <el-descriptions-item label="借出日期">
            {{ formatDate(currentAsset.borrowDate!) }}
          </el-descriptions-item>
          <el-descriptions-item label="预计归还">
            {{ formatDate(currentAsset.expectedReturnDate!) }}
          </el-descriptions-item>
          <el-descriptions-item label="借用状态">
            <el-tag v-if="checkOverdue(currentAsset)" type="danger" size="small">
              已逾期
            </el-tag>
            <el-tag v-else-if="checkReturnReminder(currentAsset)" type="warning" size="small">
              即将到期
            </el-tag>
            <el-tag v-else type="success" size="small">
              正常
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>
      </div>

      <el-divider />

      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="归还日期" prop="actualReturnDate">
          <el-date-picker
            v-model="form.actualReturnDate"
            type="date"
            placeholder="选择归还日期"
            style="width: 100%"
            :disabled-date="disabledReturnDate"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>

        <el-form-item label="归还备注" prop="notes">
          <el-input
            v-model="form.notes"
            type="textarea"
            :rows="3"
            placeholder="请描述资产状况或损坏情况(可选)"
          />
        </el-form-item>
      </el-form>
    </div>

    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" @click="handleSubmit" :loading="loading">
        确认归还
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
import {
  formatDate,
  getCategoryName,
  checkReturnReminder,
  checkOverdue
} from '../utils'
import type { Asset, ReturnForm } from '../types'

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

// 本地存储的资产引用（避免props.asset在异步操作中变为undefined）
const currentAsset = ref<Asset | null>(null)

const form = reactive<ReturnForm>({
  actualReturnDate: new Date().toISOString().split('T')[0] || '', // 默认为今天
  notes: ''
})

const rules: FormRules = {
  actualReturnDate: [{ required: true, message: '请选择归还日期', trigger: 'change' }]
}

watch(
  () => props.modelValue,
  (val) => {
    visible.value = val
    if (val) {
      // 对话框打开时，保存资产引用
      currentAsset.value = props.asset || null
    } else {
      // 对话框关闭时清空
      currentAsset.value = null
      resetForm()
    }
  }
)

watch(visible, (val) => {
  emit('update:modelValue', val)
})

// 禁用今天之后的日期作为归还日期
function disabledReturnDate(time: Date) {
  return time.getTime() > Date.now()
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
      await assetStore.returnAsset(currentAsset.value.id, form)
      ElMessage.success('归还成功')
      emit('success')
      handleClose()
    } catch (error: any) {
      ElMessage.error(error.message || '归还失败')
    } finally {
      loading.value = false
    }
  })
}

function resetForm() {
  formRef.value?.resetFields()
  form.actualReturnDate = new Date().toISOString().split('T')[0] || '' // 重置为今天
  form.notes = ''
}

function handleClose() {
  resetForm()
  emit('update:modelValue', false)
}
</script>

<style scoped lang="scss">
.return-dialog {
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

.borrow-info-section {
  .info-title {
    font-size: 14px;
    font-weight: 600;
    color: #303133;
    margin-bottom: 12px;
  }
}

.user-display {
  display: flex;
  align-items: center;
  gap: 8px;
}

:deep(.el-divider) {
  margin: 16px 0;
}
</style>
