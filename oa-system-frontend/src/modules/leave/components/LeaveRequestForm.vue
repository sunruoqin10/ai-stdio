<template>
  <el-dialog
    v-model="visible"
    :title="isEdit ? '编辑请假申请' : '新建请假申请'"
    width="700px"
    @close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="120px"
      @submit.prevent="handleSubmit"
    >
      <!-- 基本信息 -->
      <el-divider content-position="left">基本信息</el-divider>
      <el-form-item label="申请人">
        <span>{{ currentUser.name }}</span>
      </el-form-item>
      <el-form-item label="部门">
        <span>{{ currentUser.department }}</span>
      </el-form-item>

      <!-- 年假余额提示 -->
      <el-form-item v-if="form.type === 'annual'" label="年假余额">
        <el-alert
          :type="balanceWarningType"
          :closable="false"
          show-icon
        >
          <template #title>
            <span>{{ balanceWarningMessage }}</span>
          </template>
        </el-alert>
      </el-form-item>

      <!-- 请假信息 -->
      <el-divider content-position="left">请假信息</el-divider>

      <el-form-item label="请假类型" prop="type">
        <el-select
          v-model="form.type"
          placeholder="请选择请假类型"
          style="width: 100%"
          @change="handleTypeChange"
        >
          <el-option label="年假" value="annual" />
          <el-option label="病假" value="sick" />
          <el-option label="事假" value="personal" />
          <el-option label="调休" value="comp_time" />
          <el-option label="婚假" value="marriage" />
          <el-option label="产假" value="maternity" />
        </el-select>
      </el-form-item>

      <el-form-item label="开始时间" prop="startTime">
        <el-date-picker
          v-model="form.startTime"
          type="datetime"
          placeholder="选择开始时间"
          format="YYYY-MM-DD HH:mm"
          value-format="YYYY-MM-DD HH:mm:ss"
          style="width: 100%"
          @change="handleDateChange"
        />
      </el-form-item>

      <el-form-item label="结束时间" prop="endTime">
        <el-date-picker
          v-model="form.endTime"
          type="datetime"
          placeholder="选择结束时间"
          format="YYYY-MM-DD HH:mm"
          value-format="YYYY-MM-DD HH:mm:ss"
          style="width: 100%"
          @change="handleDateChange"
        />
      </el-form-item>

      <!-- 时长计算提示 -->
      <el-form-item label="请假时长">
        <el-alert
          type="info"
          :closable="false"
          show-icon
        >
          <template #title>
            <span v-if="form.duration">{{ form.duration }}个工作日</span>
            <span v-else>请选择开始和结束时间</span>
          </template>
        </el-alert>
      </el-form-item>

      <el-form-item label="请假事由" prop="reason">
        <el-input
          v-model="form.reason"
          type="textarea"
          :rows="4"
          placeholder="请输入请假事由(至少5个字)"
          maxlength="500"
          show-word-limit
        />
      </el-form-item>

      <el-form-item label="附件">
        <el-upload
          v-model:file-list="fileList"
          :action="uploadUrl"
          :on-success="handleUploadSuccess"
          :on-remove="handleUploadRemove"
          :limit="5"
          :on-exceed="handleExceed"
          :before-upload="beforeUpload"
          list-type="picture-card"
          accept="image/*,.pdf,.doc,.docx"
        >
          <el-icon><Plus /></el-icon>
          <template #tip>
            <div class="el-upload__tip">
              最多上传5个文件,每个不超过10MB
            </div>
          </template>
        </el-upload>
        <div v-if="needsAttachment" class="attachment-tip">
          <el-icon color="#E6A23C"><Warning /></el-icon>
          <span>建议上传相关证明材料</span>
        </div>
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" @click="handleSaveDraft" :loading="loading">
        保存草稿
      </el-button>
      <el-button type="success" @click="handleSubmit" :loading="loading">
        提交申请
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, watch, computed } from 'vue'
import { ElMessage, type FormInstance, type FormRules, type UploadUserFile, type UploadProps } from 'element-plus'
import { Plus, Warning } from '@element-plus/icons-vue'
import { useLeaveStore } from '../store'
import { useAuthStore } from '@/modules/auth/store'
import { calculateDuration, getBalanceWarningType, getBalanceWarningMessage, needsAttachment as needsAttachmentUtil } from '../utils'
import type { LeaveForm, LeaveRequest } from '../types'

interface Props {
  modelValue: boolean
  leaveRequest?: LeaveRequest | null
}

interface User {
  id: string
  name: string
  department: string
}

const props = withDefaults(defineProps<Props>(), {
  leaveRequest: null
})

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  'success': []
}>()

const leaveStore = useLeaveStore()
const authStore = useAuthStore()
const formRef = ref<FormInstance>()
const loading = ref(false)
const visible = ref(false)
const fileList = ref<UploadUserFile[]>([])
const uploadUrl = '/api/upload'

// 获取当前登录用户
const currentUser = computed(() => ({
  id: authStore.userInfo?.id || '',
  name: authStore.userInfo?.name || '',
  department: authStore.userInfo?.departmentName || ''
}))

const form = reactive<LeaveForm>({
  type: 'annual',
  startTime: '',
  endTime: '',
  duration: 0,
  reason: '',
  attachments: [],
  version: undefined
})

const rules: FormRules = {
  type: [{ required: true, message: '请选择请假类型', trigger: 'change' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }],
  reason: [
    { required: true, message: '请输入请假事由', trigger: 'blur' },
    { min: 5, message: '请假事由至少5个字', trigger: 'blur' }
  ]
}

const isEdit = computed(() => !!props.leaveRequest)
const needsAttachment = computed(() => needsAttachmentUtil(form.type))

// 年假余额警告
const balanceWarningType = computed(() => {
  if (form.type !== 'annual') return 'info'
  // 这里应该从store获取实际余额
  return 'warning'
})

const balanceWarningMessage = computed(() => {
  if (form.type !== 'annual') return ''
  // 这里应该从store获取实际余额
  return '年假余额3天,请合理安排'
})

watch(
  () => props.modelValue,
  (val) => {
    visible.value = val
    if (val && props.leaveRequest) {
      // 编辑模式,填充表单
      Object.assign(form, {
        type: props.leaveRequest.type,
        startTime: props.leaveRequest.startTime,
        endTime: props.leaveRequest.endTime,
        duration: props.leaveRequest.duration,
        reason: props.leaveRequest.reason,
        attachments: props.leaveRequest.attachments || [],
        version: props.leaveRequest.version
      })
      // TODO: 填充附件列表
    } else if (val) {
      // 新建模式,重置表单
      resetForm()
    }
  }
)

watch(visible, (val) => {
  emit('update:modelValue', val)
})

async function handleTypeChange() {
  // 类型切换时重新计算时长
  await handleDateChange()
}

async function handleDateChange() {
  if (form.startTime && form.endTime) {
    try {
      // 模拟节假日数据
      const holidays = []
      form.duration = calculateDuration(form.startTime, form.endTime, holidays)
    } catch (error: any) {
      ElMessage.error(error.message || '计算时长失败')
    }
  }
}

async function handleSaveDraft() {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    loading.value = true
    try {
      if (isEdit.value && props.leaveRequest) {
        // 编辑模式：只传递需要更新的字段，确保包含 version
        const updateData = {
          type: form.type,
          startTime: form.startTime,
          endTime: form.endTime,
          duration: form.duration,
          reason: form.reason,
          attachments: form.attachments,
          version: form.version
        }
        await leaveStore.updateRequest(props.leaveRequest.id, updateData)
        ElMessage.success('保存成功')
      } else {
        await leaveStore.createRequest(form)
        ElMessage.success('草稿保存成功')
      }
      emit('success')
      handleClose()
    } catch (error: any) {
      ElMessage.error(error.message || '保存失败')
    } finally {
      loading.value = false
    }
  })
}

async function handleSubmit() {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    // 检查年假余额
    if (form.type === 'annual') {
      const balance = 3 // 应该从store获取
      if (form.duration && form.duration > balance) {
        ElMessage.error(`年假余额不足,剩余${balance}天,需要${form.duration}天`)
        return
      }
    }

    loading.value = true
    try {
      let request: LeaveRequest
      if (isEdit.value && props.leaveRequest) {
        // 编辑模式：只传递需要更新的字段，确保包含 version
        const updateData = {
          type: form.type,
          startTime: form.startTime,
          endTime: form.endTime,
          duration: form.duration,
          reason: form.reason,
          attachments: form.attachments,
          version: form.version
        }
        request = await leaveStore.updateRequest(props.leaveRequest.id, updateData)
      } else {
        request = await leaveStore.createRequest(form)
      }

      // 提交申请
      await leaveStore.submitRequest(request.id)
      ElMessage.success('提交成功')
      emit('success')
      handleClose()
    } catch (error: any) {
      ElMessage.error(error.message || '提交失败')
    } finally {
      loading.value = false
    }
  })
}

const handleUploadSuccess: UploadProps['onSuccess'] = (response, file) => {
  if (response.code === 0) {
    form.attachments = form.attachments || []
    form.attachments.push(response.data.url)
    ElMessage.success('上传成功')
  }
}

const handleUploadRemove: UploadProps['onRemove'] = (file) => {
  const index = form.attachments?.indexOf(file.response?.data?.url)
  if (index && index > -1) {
    form.attachments?.splice(index, 1)
  }
}

const handleExceed: UploadProps['onExceed'] = () => {
  ElMessage.warning('最多只能上传5个文件')
}

const beforeUpload: UploadProps['beforeUpload'] = (file) => {
  const isLt10M = file.size / 1024 / 1024 < 10
  if (!isLt10M) {
    ElMessage.error('上传文件大小不能超过 10MB')
    return false
  }
  return true
}

function resetForm() {
  formRef.value?.resetFields()
  Object.assign(form, {
    type: 'annual',
    startTime: '',
    endTime: '',
    duration: 0,
    reason: '',
    attachments: [],
    version: undefined
  })
  fileList.value = []
}

function handleClose() {
  resetForm()
  emit('update:modelValue', false)
}
</script>

<style scoped lang="scss">
.attachment-tip {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 8px;
  font-size: 13px;
  color: #E6A23C;
}
</style>
