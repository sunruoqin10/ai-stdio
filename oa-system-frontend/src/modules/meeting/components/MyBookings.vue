<template>
  <div class="my-bookings">
    <el-table v-loading="loading" :data="myBookings" stripe>
      <el-table-column prop="title" label="会议主题" min-width="180" />
      <el-table-column prop="roomName" label="会议室" width="120" />
      <el-table-column label="会议时间" width="180">
        <template #default="{ row }">
          {{ formatDate(row.startTime) }}
          <div style="font-size: 12px; color: #909399">
            {{ formatTime(row.startTime) }} - {{ formatTime(row.endTime) }}
          </div>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="getBookingStatusType(row.status)">
            {{ getBookingStatusName(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleView(row)">查看</el-button>
          <el-button v-if="canEdit(row.status)" link type="primary" @click="handleEdit(row)">
            编辑
          </el-button>
          <el-button v-if="canCancel(row.status)" link type="warning" @click="handleCancel(row)">
            取消
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div v-if="myBookings.length === 0 && !loading" class="empty-container">
      <el-empty description="暂无预定记录" />
    </div>

    <!-- 详情抽屉 -->
    <meeting-detail
      v-model="detailVisible"
      :booking-id="currentBookingId"
      @refresh="loadData"
    />

    <!-- 预定对话框 -->
    <booking-dialog
      v-model="dialogVisible"
      :booking-data="currentBooking"
      @success="loadData"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useMeetingStore } from '../store'
import MeetingDetail from './MeetingDetail.vue'
import BookingDialog from './BookingDialog.vue'
import {
  formatDate,
  formatTime,
  getBookingStatusName,
  getBookingStatusType,
  canEdit,
  canCancel
} from '../utils'
import type { MeetingBooking } from '../types'

const meetingStore = useMeetingStore()
const loading = ref(false)
const myBookings = computed(() => meetingStore.myBookings)

// 详情抽屉
const detailVisible = ref(false)
const currentBookingId = ref('')

// 编辑对话框
const dialogVisible = ref(false)
const currentBooking = ref<MeetingBooking | null>(null)

async function loadData() {
  loading.value = true
  try {
    await meetingStore.loadMyBookings()
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

function handleEdit(row: MeetingBooking) {
  currentBooking.value = row
  dialogVisible.value = true
}

async function handleCancel(row: MeetingBooking) {
  try {
    await ElMessageBox.confirm('确定要取消这个会议预定吗?', '提示', {
      type: 'warning'
    })
    await meetingStore.cancelBooking(row.id)
    ElMessage.success('取消成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('取消失败')
    }
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.my-bookings {
  min-height: 400px;
}

.empty-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
}
</style>
