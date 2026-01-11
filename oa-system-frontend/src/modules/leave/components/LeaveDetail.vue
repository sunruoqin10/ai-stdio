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
          <el-image
            v-for="(file, index) in request.attachments"
            :key="index"
            :src="file"
            :preview-src-list="request.attachments"
            fit="cover"
            class="attachment-image"
          />
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
  gap: 12px;
  flex-wrap: wrap;

  .attachment-image {
    width: 100px;
    height: 100px;
    border-radius: 4px;
    cursor: pointer;
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
