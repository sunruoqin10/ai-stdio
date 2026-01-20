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
          :http-request="handlePreviewUpload"
          :on-preview="handlePicturePreview"
          :on-remove="handleUploadRemove"
          :limit="5"
          :on-exceed="handleExceed"
          :before-upload="beforeUpload"
          :show-file-list="false"
          accept="image/*,.pdf,.doc,.docx"
        >
          <el-icon><Plus /></el-icon>
          <template #tip>
            <div class="el-upload__tip">
              最多上传5个文件,每个不超过10MB
            </div>
          </template>
        </el-upload>
        <!-- 手动渲染文件列表，添加下载功能 -->
        <div class="custom-upload-list" v-if="fileList.length > 0">
          <div
            v-for="(file, index) in fileList"
            :key="file.uid || index"
            class="custom-upload-item"
          >
            <el-image
              v-if="file.url && file.url.match(/\.(jpg|jpeg|png|gif)$/i)"
              :src="file.url"
              class="custom-upload-thumbnail"
              fit="cover"
            />
            <div v-else class="custom-upload-icon">
              <el-icon v-if="file.name?.match(/\.pdf$/i)"><Document /></el-icon>
              <el-icon v-else-if="file.name?.match(/\.doc|docx$/i)"><Document /></el-icon>
              <el-icon v-else><Document /></el-icon>
            </div>
            <div class="custom-upload-info">
              <span class="custom-upload-name">{{ file.name }}</span>
            </div>
            <div class="custom-upload-actions">
              <el-button
                type="text"
                size="small"
                @click="handlePicturePreview(file)"
              >
                <el-icon><ZoomIn /></el-icon>
              </el-button>
              <el-button
                type="text"
                size="small"
                @click="handleDownload(file)"
              >
                <el-icon><Download /></el-icon>
              </el-button>
              <el-button
                type="text"
                size="small"
                @click="handleUploadRemove(file)"
              >
                <el-icon><Delete /></el-icon>
              </el-button>
            </div>
          </div>
        </div>
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

  <!-- 图片预览对话框 -->
  <el-dialog v-model="previewVisible" title="图片预览" width="800px">
    <img :src="previewUrl" style="width: 100%" />
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, watch, computed } from 'vue'
import { ElMessage, type FormInstance, type FormRules, type UploadUserFile, type UploadProps, type UploadRequestOptions } from 'element-plus'
import { Plus, Warning, Delete, ZoomIn, Download, Document } from '@element-plus/icons-vue'
import { useLeaveStore } from '../store'
import { useAuthStore } from '@/modules/auth/store'
import { calculateDuration, getBalanceWarningType, getBalanceWarningMessage, needsAttachment as needsAttachmentUtil, getImageUrl } from '../utils'
import type { LeaveForm, LeaveRequest } from '../types'
import { uploadFile } from '../api'

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
const uploadUrl = '/upload'
const previewVisible = ref(false)
const previewUrl = ref('')

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
      let attachments: string[] = []
      
      // 处理可能的JSON字符串格式
      if (props.leaveRequest.attachments) {
        if (typeof props.leaveRequest.attachments === 'string') {
          try {
            attachments = JSON.parse(props.leaveRequest.attachments)
          } catch (error) {
            console.error('Failed to parse attachments JSON:', error)
            attachments = [props.leaveRequest.attachments] // 如果解析失败，作为单个URL处理
          }
        } else if (Array.isArray(props.leaveRequest.attachments)) {
          attachments = props.leaveRequest.attachments
        }
      }
      
      console.log('Processed attachments array:', attachments)
      
      Object.assign(form, {
        type: props.leaveRequest.type,
        startTime: props.leaveRequest.startTime,
        endTime: props.leaveRequest.endTime,
        duration: props.leaveRequest.duration,
        reason: props.leaveRequest.reason,
        attachments: attachments,
        version: props.leaveRequest.version
      })
      
      // 填充附件列表
      const processedFiles = attachments.map((url, index) => {
        if (!url) return null
        
        // 尝试从URL中提取文件名
        const urlParts = url.split('/')
        const fileName = urlParts[urlParts.length - 1]
        
        // 猜测文件类型
        let fileType = ''
        const ext = fileName.split('.').pop()?.toLowerCase()
        if (['jpg', 'jpeg', 'png', 'gif'].includes(ext || '')) {
          fileType = `image/${ext}`
        } else if (ext === 'pdf') {
          fileType = 'application/pdf'
        } else if (['doc', 'docx'].includes(ext || '')) {
          fileType = 'application/msword'
        }
        
        const processedUrl = getImageUrl(url)
        console.log('Processed attachment:', {
          originalUrl: url,
          processedUrl: processedUrl,
          fileName: fileName,
          fileType: fileType,
          uid: index
        })
        
        return {
          name: fileName,
          url: processedUrl,
          uid: index,
          type: fileType,
          status: 'success' as const
        } as UploadUserFile
      }).filter((file): file is UploadUserFile => file !== null)
      
      console.log('Final fileList:', processedFiles)
      fileList.value = processedFiles
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
      // 上传所有新文件
      const newFileUrls = await uploadAllFiles()
      
      // 合并已存在的附件URL和新上传的附件URL
      const existingUrls = fileList.value
        .filter(file => !file.raw && file.url)
        .map(file => {
          // 如果是新上传的本地URL (blob: 开头)，不包含在existingUrls中
          if (file.url?.startsWith('blob:')) {
            return null
          }
          // 将完整URL转换回存储的格式
          const url = file.url || ''
          if (url.startsWith('/api/uploads/')) {
            return url.substring(4)
          }
          return url.startsWith('/uploads/') ? url : null
        })
        .filter((url): url is string => url !== null)
      
      form.attachments = [...existingUrls, ...newFileUrls]

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
      // 上传所有新文件
      const newFileUrls = await uploadAllFiles()
      
      // 合并已存在的附件URL和新上传的附件URL
      const existingUrls = fileList.value
        .filter(file => !file.raw && file.url)
        .map(file => {
          // 如果是新上传的本地URL (blob: 开头)，不包含在existingUrls中
          if (file.url?.startsWith('blob:')) {
            return null
          }
          // 将完整URL转换回存储的格式
          const url = file.url || ''
          if (url.startsWith('/api/uploads/')) {
            return url.substring(4)
          }
          return url.startsWith('/uploads/') ? url : null
        })
        .filter((url): url is string => url !== null)
      
      form.attachments = [...existingUrls, ...newFileUrls]

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



const handleUploadRemove: UploadProps['onRemove'] = (file) => {
  // 移除文件列表中的文件
  const index = fileList.value.findIndex(f => f.uid === file.uid)
  if (index > -1) {
    fileList.value.splice(index, 1)
  }
  
  // 如果是已上传的文件，从attachments中移除
  if (file.url && !file.url.startsWith('blob:')) {
    const attachmentUrl = file.url.startsWith('/api/uploads/') 
      ? file.url.substring(4) 
      : file.url.startsWith('/uploads/') 
        ? file.url 
        : file.url;
    
    const attachmentIndex = form.attachments?.indexOf(attachmentUrl)
    if (attachmentIndex !== undefined && attachmentIndex > -1) {
      form.attachments?.splice(attachmentIndex, 1)
    }
  }
}

const handleExceed: UploadProps['onExceed'] = () => {
  ElMessage.warning('最多只能上传5个文件')
}

// 上传前校验
const beforeUpload = (file: File) => {
  const isLt10M = file.size / 1024 / 1024 < 10
  
  if (!isLt10M) {
    ElMessage.error('文件大小不能超过 10MB!')
    return false
  }
  
  // 获取文件扩展名
  const ext = file.name.split('.').pop()?.toLowerCase()
  const allowedExts = ['jpg', 'jpeg', 'png', 'gif', 'pdf', 'doc', 'docx']
  
  if (!allowedExts.includes(ext || '')) {
    ElMessage.error('只允许上传图片、PDF和Word文件!')
    return false
  }
  
  return true
}

// 预览上传 - 仅生成预览URL，不实际上传
const handlePreviewUpload = async (options: UploadRequestOptions) => {
  const file = options.file as File
  const url = URL.createObjectURL(file)
  options.onSuccess?.(url)
}

// 图片预览
const handlePicturePreview = (file: UploadUserFile) => {
  const fileName = file.name || ''
  const ext = fileName.split('.').pop()?.toLowerCase()
  if (['jpg', 'jpeg', 'png', 'gif'].includes(ext || '')) {
    previewUrl.value = file.url ? getImageUrl(file.url) : ''
    previewVisible.value = true
  } else {
    ElMessage.info('该文件类型不支持预览')
  }
}

// 文件下载
const handleDownload = async (file: UploadUserFile) => {
  if (!file.url) {
    ElMessage.error('文件下载地址不存在')
    return
  }
  
  try {
    let filePath = ''
    
    // 从URL中提取文件路径
    if (file.url.startsWith('/uploads/')) {
      // 对于形如 /uploads/2023/10/01/file.jpg 的URL
      filePath = file.url.substring(8) // 去掉 /uploads/ 前缀
    } else if (file.url.startsWith('/api/uploads/')) {
      // 对于形如 /api/uploads/2023/10/01/file.jpg 的URL
      filePath = file.url.substring(12) // 去掉 /api/uploads/ 前缀
    } else {
      // 对于其他URL格式，直接使用
      filePath = file.url
    }
    
    // 构建下载URL
    const downloadUrl = `${window.location.origin}/api/upload/download?filePath=${encodeURIComponent(filePath)}`
    
    // 创建下载链接
    const link = document.createElement('a')
    link.href = downloadUrl
    link.download = file.name || 'download'
    link.target = '_blank'
    
    // 触发下载
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    
    ElMessage.success('文件下载已开始')
  } catch (error) {
    console.error('文件下载失败:', error)
    ElMessage.error('文件下载失败，请重试')
  }
}

// 上传所有文件
const uploadAllFiles = async () => {
  const uploadedUrls: string[] = []
  const filesToUpload = fileList.value.filter(file => file.raw)
  
  for (const file of filesToUpload) {
    try {
      const url = await uploadFile(file.raw as File)
      uploadedUrls.push(url)
    } catch (error: any) {
      throw new Error(`文件 ${file.name} 上传失败: ${error.message}`)
    }
  }
  
  return uploadedUrls
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

// 自定义上传列表样式
.custom-upload-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 12px;
}

.custom-upload-item {
  position: relative;
  width: 120px;
  height: 120px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  overflow: hidden;
  background-color: #f5f7fa;
}

.custom-upload-thumbnail {
  width: 100%;
  height: 100%;
}

.custom-upload-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  font-size: 24px;
  color: #909399;
}

.custom-upload-info {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  padding: 4px;
  background-color: rgba(0, 0, 0, 0.5);
  color: #fff;
  font-size: 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.custom-upload-actions {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: rgba(0, 0, 0, 0.5);
  opacity: 0;
  transition: opacity 0.3s;
}

.custom-upload-item:hover .custom-upload-actions {
  opacity: 1;
}

.custom-upload-actions .el-button {
  margin: 0 4px;
  color: #fff;
}
</style>
