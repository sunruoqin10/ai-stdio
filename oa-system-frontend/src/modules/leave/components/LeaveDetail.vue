<template>
  <el-drawer
    v-model="visible"
    title="请假申请详情"
    size="600px"
    @close="handleClose"
  >
    <div v-if="request" class="leave-detail">
      <!-- 基本信息 -->
      <section class="detail-section">
        <h3 class="section-title">基本信息</h3>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="申请编号">
            {{ request.id }}
          </el-descriptions-item>
          <el-descriptions-item label="申请人">
            {{ request.applicantName }}
          </el-descriptions-item>
          <el-descriptions-item label="部门">
            {{ request.departmentName }}
          </el-descriptions-item>
          <el-descriptions-item label="请假类型">
            <el-tag :type="getTypeTagType(request.type)">
              {{ getLeaveTypeName(request.type) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="开始时间">
            {{ formatDateTime(request.startTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="结束时间">
            {{ formatDateTime(request.endTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="请假时长" :span="2">
            {{ request.duration }}个工作日
          </el-descriptions-item>
          <el-descriptions-item label="申请状态" :span="2">
            <el-tag :type="getLeaveStatusType(request.status)" size="large">
              {{ getLeaveStatusName(request.status) }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>
      </section>

      <!-- 请假事由 -->
      <section class="detail-section">
        <h3 class="section-title">请假事由</h3>
        <div class="reason-content">
          {{ request.reason }}
        </div>
      </section>

      <!-- 审批记录 -->
      <section v-if="request.approvers && request.approvers.length > 0" class="detail-section">
        <h3 class="section-title">审批记录</h3>
        <el-timeline>
          <el-timeline-item
            v-for="(approver, index) in request.approvers"
            :key="index"
            :timestamp="approver.timestamp ? formatDateTime(approver.timestamp) : '待审批'"
            placement="top"
            :type="getTimelineType(approver.status)"
          >
            <el-card>
              <div class="approver-info">
                <div class="approver-header">
                  <span class="approver-name">{{ approver.approverName }}</span>
                  <el-tag :type="getApprovalTagType(approver.status)" size="small">
                    {{ getApprovalStatusName(approver.status) }}
                  </el-tag>
                </div>
                <div v-if="approver.opinion" class="approver-opinion">
                  <span class="opinion-label">审批意见:</span>
                  <span class="opinion-text">{{ approver.opinion }}</span>
                </div>
              </div>
            </el-card>
          </el-timeline-item>
        </el-timeline>
      </section>

      <!-- 时间信息 -->
      <section class="detail-section">
        <h3 class="section-title">时间信息</h3>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="创建时间">
            {{ formatDateTime(request.createdAt) }}
          </el-descriptions-item>
          <el-descriptions-item label="更新时间">
            {{ formatDateTime(request.updatedAt) }}
          </el-descriptions-item>
        </el-descriptions>
      </section>

      <!-- 附件 -->
      <section v-if="request.attachments && request.attachments.length > 0" class="detail-section">
        <h3 class="section-title">附件</h3>
        <div class="attachments">
          <div
            v-for="(file, index) in request.attachments"
            :key="index"
            class="attachment-item"
          >
            <div class="attachment-preview">
              <template v-if="isImageFile(file) && !imageLoadErrors.has(file)">
                <el-image
                  :src="file"
                  :preview-src-list="request.attachments.filter(f => isImageFile(f) && !imageLoadErrors.has(f))"
                  fit="cover"
                  class="attachment-image"
                  @error="handleImageError(file)"
                >
                  <template #error>
                    <div class="image-error">
                      <el-icon :size="32"><Picture /></el-icon>
                      <span class="error-text">加载失败</span>
                    </div>
                  </template>
                </el-image>
              </template>
              <template v-else>
                <div class="file-icon">
                  <el-icon :size="48"><Document /></el-icon>
                </div>
              </template>
            </div>
            <div class="attachment-info">
              <span class="file-name" :title="getFileName(file)">{{ getFileName(file) }}</span>
              <el-button
                type="primary"
                size="small"
                :icon="Download"
                @click="handleDownload(file)"
                class="download-button"
              >
                下载
              </el-button>
            </div>
          </div>
        </div>
      </section>
    </div>

    <template #footer>
      <el-button @click="handleClose">关闭</el-button>
      <el-button
        v-if="canEdit(request?.status)"
        type="primary"
        @click="handleEdit"
      >
        编辑
      </el-button>
    </template>
  </el-drawer>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { Download, Document, Picture } from '@element-plus/icons-vue'
import { useLeaveStore } from '../store'
import {
  formatDateTime,
  getLeaveTypeName,
  getLeaveStatusName,
  getLeaveStatusType,
  canEdit,
  getApprovalStatusName
} from '../utils'
import type { LeaveRequest, ApprovalStatus } from '../types'
import axios from 'axios'

interface Props {
  modelValue: boolean
  requestId?: string | null
}

const props = withDefaults(defineProps<Props>(), {
  requestId: null
})

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  'edit': [request: LeaveRequest]
}>()

const leaveStore = useLeaveStore()
const visible = ref(false)
const request = ref<LeaveRequest | null>(null)
const imageLoadErrors = ref<Set<string>>(new Set())

function isImageFile(url: string): boolean {
  const imageExtensions = ['.jpg', '.jpeg', '.png', '.gif', '.bmp', '.webp', '.svg']
  const lowerUrl = url.toLowerCase()
  return imageExtensions.some(ext => lowerUrl.endsWith(ext))
}

function getFileName(url: string): string {
  return url.split('/').pop() || 'unknown'
}

function handleImageError(url: string) {
  imageLoadErrors.value.add(url)
}

watch(
  () => props.modelValue,
  async (val) => {
    visible.value = val
    if (val && props.requestId) {
      request.value = await leaveStore.loadRequest(props.requestId)
    }
  }
)

watch(visible, (val) => {
  emit('update:modelValue', val)
})

function getTypeTagType(type: string): string {
  const typeMap: Record<string, string> = {
    annual: 'success',
    sick: 'warning',
    personal: 'info',
    comp_time: 'primary',
    marriage: 'danger',
    maternity: 'danger'
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
  if (request.value) {
    emit('edit', request.value)
  }
}

async function handleDownload(fileUrl: string) {
  try {
    const fileName = fileUrl.split('/').pop() || 'download'
    const response = await axios.get(fileUrl, {
      responseType: 'blob'
    })
    
    const url = window.URL.createObjectURL(new Blob([response.data]))
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', fileName)
    document.body.appendChild(link)
    link.click()
    
    window.URL.revokeObjectURL(url)
    document.body.removeChild(link)
  } catch (error) {
    console.error('下载文件失败:', error)
  }
}
</script>

<style scoped lang="scss">
.leave-detail {
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

.attachments {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;

  .attachment-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
    width: 140px;

    .attachment-preview {
      width: 100%;
      height: 100px;
      border: 1px solid #DCDFE6;
      border-radius: 4px;
      overflow: hidden;
      display: flex;
      align-items: center;
      justify-content: center;
      background: #F5F7FA;

      .attachment-image {
        width: 100%;
        height: 100%;
        cursor: pointer;
      }

      .image-error {
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        width: 100%;
        height: 100%;
        color: #909399;
        gap: 8px;

        .error-text {
          font-size: 12px;
        }
      }

      .file-icon {
        display: flex;
        align-items: center;
        justify-content: center;
        width: 100%;
        height: 100%;
        color: #409EFF;
      }
    }

    .attachment-info {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 6px;
      width: 100%;

      .file-name {
        font-size: 12px;
        color: #606266;
        text-align: center;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        width: 100%;
        display: block;
      }

      .download-button {
        min-width: 80px;
      }
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
