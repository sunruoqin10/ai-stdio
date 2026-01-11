<template>
  <div class="meeting-list">
    <!-- 筛选栏 -->
    <el-card class="filter-card" shadow="never">
      <el-form :inline="true" :model="filterForm" class="filter-form">
        <el-form-item label="状态">
          <el-select v-model="filterForm.status" placeholder="全部" clearable style="width: 120px">
            <el-option label="待审批" value="pending" />
            <el-option label="已通过" value="approved" />
            <el-option label="已驳回" value="rejected" />
            <el-option label="已取消" value="cancelled" />
          </el-select>
        </el-form-item>

        <el-form-item label="会议室">
          <el-select v-model="filterForm.roomId" placeholder="全部" clearable style="width: 180px">
            <el-option
              v-for="room in rooms"
              :key="room.id"
              :label="room.name"
              :value="room.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="日期范围">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 240px"
          />
        </el-form-item>

        <el-form-item label="关键词">
          <el-input
            v-model="filterForm.keyword"
            placeholder="搜索会议主题"
            clearable
            style="width: 200px"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleFilter">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 数据表格 -->
    <el-card class="table-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span>会议列表</span>
          <el-button type="primary" @click="handleCreate">
            <el-icon><Plus /></el-icon>
            预定会议室
          </el-button>
        </div>
      </template>

      <el-table
        v-loading="loading"
        :data="tableData"
        stripe
        style="width: 100%"
      >
        <el-table-column prop="title" label="会议主题" min-width="180">
          <template #default="{ row }">
            <div>
              <div>{{ row.title }}</div>
              <el-tag
                v-if="row.level !== 'normal'"
                :type="getMeetingLevelType(row.level)"
                size="small"
                style="margin-top: 5px"
              >
                {{ getMeetingLevelName(row.level) }}
              </el-tag>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="roomName" label="会议室" width="120" />

        <el-table-column label="会议时间" width="180">
          <template #default="{ row }">
            <div>{{ formatDate(row.startTime) }}</div>
            <div style="color: #909399; font-size: 12px">
              {{ formatTime(row.startTime) }} - {{ formatTime(row.endTime) }}
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="organizerName" label="组织者" width="100" />

        <el-table-column label="参会人数" width="80" align="center">
          <template #default="{ row }">
            <el-badge :value="row.attendees.length" :max="99" />
          </template>
        </el-table-column>

        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getBookingStatusType(row.status)">
              {{ getBookingStatusName(row.status) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleView(row)">
              查看
            </el-button>

            <el-button
              v-if="canEdit(row.status)"
              link
              type="primary"
              @click="handleEdit(row)"
            >
              编辑
            </el-button>

            <el-button
              v-if="canCancel(row.status)"
              link
              type="warning"
              @click="handleCancel(row)"
            >
              取消
            </el-button>

            <el-button
              v-if="canCheckIn(row)"
              link
              type="success"
              @click="handleCheckIn(row)"
            >
              签到
            </el-button>

            <el-button
              v-if="canCheckOut(row)"
              link
              type="info"
              @click="handleCheckOut(row)"
            >
              签退
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 预定对话框 -->
    <booking-dialog
      v-model="dialogVisible"
      :booking-data="currentBooking"
      @success="handleSuccess"
    />

    <!-- 详情抽屉 -->
    <meeting-detail
      v-model="drawerVisible"
      :booking-id="currentBookingId"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus } from '@element-plus/icons-vue'
import { useMeetingStore } from '../store'
import BookingDialog from './BookingDialog.vue'
import MeetingDetail from './MeetingDetail.vue'
import {
  formatDate,
  formatTime,
  getBookingStatusName,
  getBookingStatusType,
  getMeetingLevelName,
  getMeetingLevelType,
  canEdit,
  canCancel,
  canCheckIn,
  canCheckOut
} from '../utils'
import type { MeetingBooking } from '../types'

// Store
const meetingStore = useMeetingStore()

// 加载状态
const loading = ref(false)

// 筛选表单
const filterForm = reactive({
  status: '',
  roomId: '',
  keyword: ''
})

// 日期范围
const dateRange = ref<[string, string] | null>(null)

// 表格数据
const tableData = computed(() => meetingStore.bookings)

// 会议室列表
const rooms = computed(() => meetingStore.rooms)

// 分页
const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

// 对话框显示
const dialogVisible = ref(false)

// 抽屉显示
const drawerVisible = ref(false)

// 当前编辑的预定
const currentBooking = ref<MeetingBooking | null>(null)

// 当前查看的预定ID
const currentBookingId = ref('')

// 加载数据
async function loadData() {
  loading.value = true
  try {
    const params: any = {
      page: pagination.page,
      size: pagination.pageSize
    }

    if (filterForm.status) params.status = filterForm.status
    if (filterForm.roomId) params.roomId = filterForm.roomId
    if (filterForm.keyword) params.keyword = filterForm.keyword

    if (dateRange.value) {
      params.startDate = dateRange.value[0]
      params.endDate = dateRange.value[1]
    }

    await meetingStore.loadBookings(params)
    pagination.total = meetingStore.bookingsTotal
  } catch (error) {
    console.error('加载数据失败:', error)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

// 筛选
function handleFilter() {
  pagination.page = 1
  loadData()
}

// 重置
function handleReset() {
  filterForm.status = ''
  filterForm.roomId = ''
  filterForm.keyword = ''
  dateRange.value = null
  pagination.page = 1
  loadData()
}

// 创建
function handleCreate() {
  currentBooking.value = null
  dialogVisible.value = true
}

// 查看
function handleView(row: MeetingBooking) {
  currentBookingId.value = row.id
  drawerVisible.value = true
}

// 编辑
function handleEdit(row: MeetingBooking) {
  currentBooking.value = row
  dialogVisible.value = true
}

// 取消
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
      console.error('取消失败:', error)
      ElMessage.error('取消失败')
    }
  }
}

// 签到
async function handleCheckIn(row: MeetingBooking) {
  try {
    await meetingStore.checkIn({
      bookingId: row.id,
      actualStartTime: new Date().toISOString()
    })
    ElMessage.success('签到成功')
    loadData()
  } catch (error: any) {
    console.error('签到失败:', error)
    ElMessage.error(error.message || '签到失败')
  }
}

// 签退
async function handleCheckOut(row: MeetingBooking) {
  try {
    await meetingStore.checkOut(row.id)
    ElMessage.success('签退成功')
    loadData()
  } catch (error: any) {
    console.error('签退失败:', error)
    ElMessage.error(error.message || '签退失败')
  }
}

// 成功回调
function handleSuccess() {
  loadData()
}

// 分页变化
function handleSizeChange() {
  loadData()
}

function handleCurrentChange() {
  loadData()
}

// 初始化
onMounted(() => {
  loadData()
  meetingStore.loadRooms()
})
</script>

<style scoped>
.meeting-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.filter-card {
  margin-bottom: 0;
}

.filter-form {
  margin-bottom: -8px;
}

.table-card {
  flex: 1;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pagination-container {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
