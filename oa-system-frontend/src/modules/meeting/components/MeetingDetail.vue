<template>
  <el-drawer
    v-model="visible"
    title="会议详情"
    size="600px"
    @close="handleClose"
  >
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="10" animated />
    </div>

    <div v-else-if="booking" class="detail-container">
      <!-- 基本信息 -->
      <el-descriptions title="基本信息" :column="2" border>
        <el-descriptions-item label="会议主题" :span="2">
          <div style="display: flex; align-items: center; gap: 8px">
            {{ booking.title }}
            <el-tag
              v-if="booking.level !== 'normal'"
              :type="getMeetingLevelType(booking.level)"
              size="small"
            >
              {{ getMeetingLevelName(booking.level) }}
            </el-tag>
            <el-tag v-if="booking.isPrivate" type="info" size="small">
              私密
            </el-tag>
          </div>
        </el-descriptions-item>

        <el-descriptions-item label="状态">
          <el-tag :type="getBookingStatusType(booking.status)">
            {{ getBookingStatusName(booking.status) }}
          </el-tag>
        </el-descriptions-item>

        <el-descriptions-item label="会议室">
          {{ booking.roomName }}
        </el-descriptions-item>

        <el-descriptions-item label="开始时间" :span="2">
          {{ formatDateTime(booking.startTime) }}
        </el-descriptions-item>

        <el-descriptions-item label="结束时间" :span="2">
          {{ formatDateTime(booking.endTime) }}
        </el-descriptions-item>

        <el-descriptions-item label="会议时长">
          {{ formatDuration(booking.duration) }}
        </el-descriptions-item>

        <el-descriptions-item label="组织者">
          {{ booking.organizerName }}
        </el-descriptions-item>

        <el-descriptions-item label="联系电话">
          {{ booking.organizerPhone }}
        </el-descriptions-item>

        <el-descriptions-item label="部门">
          {{ booking.departmentName }}
        </el-descriptions-item>
      </el-descriptions>

      <!-- 会议议程 -->
      <el-divider content-position="left">会议议程</el-divider>
      <div class="agenda-content">
        {{ booking.agenda || '暂无议程' }}
      </div>

      <!-- 参会人员 -->
      <el-divider content-position="left">参会人员 ({{ booking.attendees.length }}人)</el-divider>
      <div class="attendees-list">
        <el-tag
          v-for="attendee in booking.attendees"
          :key="attendee.userId"
          :type="attendee.required ? '' : 'info'"
          style="margin-right: 8px; margin-bottom: 8px"
        >
          {{ attendee.userName }}
          {{ attendee.required ? '(必须)' : '(可选)' }}
          <span v-if="attendee.status === 'accepted'" style="color: #67c23a">✓</span>
          <span v-else-if="attendee.status === 'declined'" style="color: #f56c6c">✗</span>
          <span v-else style="color: #909399">?</span>
        </el-tag>
      </div>

      <!-- 审批记录 -->
      <template v-if="booking.approval">
        <el-divider content-position="left">审批记录</el-divider>
        <el-timeline>
          <el-timeline-item
            :timestamp="formatDateTime(booking.approval.timestamp!)"
            placement="top"
          >
            <el-card>
              <div>
                <strong>审批人:</strong> {{ booking.approval.approverName }}
              </div>
              <div>
                <strong>结果:</strong>
                <el-tag
                  :type="booking.approval.status === 'approved' ? 'success' : 'danger'"
                  size="small"
                >
                  {{ booking.approval.status === 'approved' ? '通过' : '驳回' }}
                </el-tag>
              </div>
              <div v-if="booking.approval.opinion">
                <strong>意见:</strong> {{ booking.approval.opinion }}
              </div>
            </el-card>
          </el-timeline-item>
        </el-timeline>
      </template>

      <!-- 签到签退 -->
      <template v-if="booking.actualStartTime || booking.actualEndTime">
        <el-divider content-position="left">签到记录</el-divider>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="签到时间">
            {{ booking.actualStartTime ? formatDateTime(booking.actualStartTime) : '未签到' }}
          </el-descriptions-item>
          <el-descriptions-item label="签退时间">
            {{ booking.actualEndTime ? formatDateTime(booking.actualEndTime) : '未签退' }}
          </el-descriptions-item>
          <el-descriptions-item label="签到人">
            {{ booking.checkInUser || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="签退人">
            {{ booking.checkOutUser || '-' }}
          </el-descriptions-item>
        </el-descriptions>
      </template>

      <!-- 评价 -->
      <template v-if="booking.rating">
        <el-divider content-position="left">会议评价</el-divider>
        <div class="rating-content">
          <el-rate
            v-model="booking.rating"
            disabled
            show-score
            text-color="#ff9900"
          />
          <div v-if="booking.feedback" class="feedback-text">
            {{ booking.feedback }}
          </div>
        </div>
      </template>

      <!-- 操作按钮 -->
      <div class="action-buttons">
        <el-button
          v-if="canEdit(booking.status)"
          type="primary"
          @click="handleEdit"
        >
          编辑
        </el-button>

        <el-button
          v-if="canCancel(booking.status)"
          type="warning"
          @click="handleCancel"
        >
          取消预定
        </el-button>

        <el-button
          v-if="canCheckIn(booking)"
          type="success"
          @click="handleCheckIn"
        >
          签到
        </el-button>

        <el-button
          v-if="canCheckOut(booking)"
          type="info"
          @click="handleCheckOut"
        >
          签退
        </el-button>

        <el-button
          v-if="canRate(booking)"
          type="primary"
          @click="handleRate"
        >
          评价
        </el-button>
      </div>
    </div>

    <!-- 评价对话框 -->
    <el-dialog v-model="ratingDialogVisible" title="会议评价" width="500px">
      <el-form :model="ratingForm" label-width="80px">
        <el-form-item label="评分">
          <el-rate v-model="ratingForm.rating" />
        </el-form-item>
        <el-form-item label="反馈意见">
          <el-input
            v-model="ratingForm.feedback"
            type="textarea"
            :rows="4"
            placeholder="请输入您的反馈意见"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="ratingDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitRating">提交</el-button>
      </template>
    </el-dialog>
  </el-drawer>
</template>

<script setup lang="ts">
import { ref, watch, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useMeetingStore } from '../store'
import {
  formatDateTime,
  formatDuration,
  formatTime,
  getBookingStatusName,
  getBookingStatusType,
  getMeetingLevelName,
  getMeetingLevelType,
  canEdit,
  canCancel,
  canCheckIn,
  canCheckOut,
  canRate
} from '../utils'
import type { MeetingBooking } from '../types'

// Props
interface Props {
  modelValue: boolean
  bookingId: string
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: false,
  bookingId: ''
})

// Emits
const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  'refresh': []
}>()

// Store
const meetingStore = useMeetingStore()

// 抽屉显示状态
const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

// 加载状态
const loading = ref(false)

// 预定详情
const booking = ref<MeetingBooking | null>(null)

// 评价对话框
const ratingDialogVisible = ref(false)
const ratingForm = ref({
  rating: 5,
  feedback: ''
})

// 加载详情
async function loadDetail() {
  if (!props.bookingId) return

  loading.value = true
  try {
    const data = await meetingStore.loadBookingDetail(props.bookingId)
    booking.value = data
  } catch (error) {
    console.error('加载详情失败:', error)
    ElMessage.error('加载详情失败')
  } finally {
    loading.value = false
  }
}

// 关闭
function handleClose() {
  visible.value = false
  booking.value = null
}

// 编辑
function handleEdit() {
  emit('update:modelValue', false)
  // TODO: 触发编辑事件
}

// 取消
async function handleCancel() {
  if (!booking.value) return

  try {
    await ElMessageBox.confirm('确定要取消这个会议预定吗?', '提示', {
      type: 'warning'
    })

    await meetingStore.cancelBooking(booking.value.id)
    ElMessage.success('取消成功')
    emit('refresh')
    handleClose()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消失败:', error)
      ElMessage.error('取消失败')
    }
  }
}

// 签到
async function handleCheckIn() {
  if (!booking.value) return

  try {
    await meetingStore.checkIn({
      bookingId: booking.value.id,
      actualStartTime: new Date().toISOString()
    })
    ElMessage.success('签到成功')
    loadDetail()
    emit('refresh')
  } catch (error: any) {
    console.error('签到失败:', error)
    ElMessage.error(error.message || '签到失败')
  }
}

// 签退
async function handleCheckOut() {
  if (!booking.value) return

  try {
    await meetingStore.checkOut(booking.value.id)
    ElMessage.success('签退成功')
    loadDetail()
    emit('refresh')
  } catch (error: any) {
    console.error('签退失败:', error)
    ElMessage.error(error.message || '签退失败')
  }
}

// 评价
function handleRate() {
  ratingForm.value = {
    rating: 5,
    feedback: ''
  }
  ratingDialogVisible.value = true
}

// 提交评价
async function submitRating() {
  if (!booking.value) return

  try {
    await meetingStore.submitRating({
      bookingId: booking.value.id,
      rating: ratingForm.value.rating,
      feedback: ratingForm.value.feedback
    })
    ElMessage.success('评价成功')
    ratingDialogVisible.value = false
    loadDetail()
  } catch (error: any) {
    console.error('评价失败:', error)
    ElMessage.error(error.message || '评价失败')
  }
}

// 监听打开
watch(() => props.modelValue, (val) => {
  if (val && props.bookingId) {
    loadDetail()
  }
})
</script>

<style scoped>
.loading-container {
  padding: 20px;
}

.detail-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.agenda-content {
  padding: 12px;
  background-color: #f5f7fa;
  border-radius: 4px;
  white-space: pre-wrap;
  word-break: break-word;
  min-height: 60px;
}

.attendees-list {
  padding: 12px;
  background-color: #f5f7fa;
  border-radius: 4px;
  min-height: 60px;
}

.rating-content {
  padding: 12px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.feedback-text {
  margin-top: 12px;
  padding: 8px;
  background-color: #fff;
  border-radius: 4px;
}

.action-buttons {
  display: flex;
  gap: 12px;
  padding-top: 16px;
  border-top: 1px solid #ebeef5;
}

:deep(.el-divider__text) {
  background-color: transparent;
}
</style>
