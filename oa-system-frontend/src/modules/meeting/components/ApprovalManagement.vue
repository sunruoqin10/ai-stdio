<template>
  <div class="approval-management">
    <el-table v-loading="loading" :data="pendingApprovals" stripe>
      <el-table-column prop="title" label="会议主题" min-width="180" />
      <el-table-column prop="organizerName" label="申请人" width="100" />
      <el-table-column prop="departmentName" label="部门" width="120" />
      <el-table-column prop="roomName" label="会议室" width="120" />
      <el-table-column label="会议时间" width="180">
        <template #default="{ row }">
          {{ formatDate(row.startTime) }}
          <div style="font-size: 12px; color: #909399">
            {{ formatTime(row.startTime) }} - {{ formatTime(row.endTime) }}
          </div>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleView(row)">查看</el-button>
          <el-button link type="success" @click="handleApprove(row)">通过</el-button>
          <el-button link type="danger" @click="handleReject(row)">驳回</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div v-if="pendingApprovals.length === 0 && !loading" class="empty-container">
      <el-empty description="暂无待审批申请" />
    </div>

    <!-- 详情抽屉 -->
    <meeting-detail
      v-model="detailVisible"
      :booking-id="currentBookingId"
      @refresh="loadData"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useMeetingStore } from '../store'
import MeetingDetail from './MeetingDetail.vue'
import {
  formatDate,
  formatTime,
  getBookingStatusName
} from '../utils'
import type { MeetingBooking } from '../types'

const meetingStore = useMeetingStore()
const loading = ref(false)
const pendingApprovals = computed(() => meetingStore.pendingApprovals)

// 详情抽屉
const detailVisible = ref(false)
const currentBookingId = ref('')

async function loadData() {
  loading.value = true
  try {
    await meetingStore.loadPendingApprovals()
  } catch (error) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

function handleView(row: MeetingBooking) {
  currentBookingId.value = row.id
  detailVisible.value = true
}

async function handleApprove(row: MeetingBooking) {
  try {
    await ElMessageBox.prompt('请输入审批意见(可选)', '通过预定', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPattern: /^.{0,200}$/,
      inputErrorMessage: '意见不能超过200字符'
    })

    await meetingStore.approveBooking(row.id, {
      status: 'approved',
      opinion: '同意'
    })
    ElMessage.success('审批成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('审批失败')
    }
  }
}

async function handleReject(row: MeetingBooking) {
  try {
    const { value } = await ElMessageBox.prompt('请输入驳回原因(必填)', '驳回预定', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPattern: /^.{2,200}$/,
      inputErrorMessage: '驳回原因必填且为2-200字符'
    })

    await meetingStore.approveBooking(row.id, {
      status: 'rejected',
      opinion: value
    })
    ElMessage.success('已驳回')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.approval-management {
  min-height: 400px;
}

.empty-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
}
</style>
