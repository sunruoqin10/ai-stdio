<template>
  <div class="meeting-module">
    <!-- 统计卡片 -->
    <el-row :gutter="16" class="stats-row">
      <el-col :span="6">
        <el-card class="stats-card" shadow="hover">
          <div class="stats-content">
            <div class="stats-icon available">
              <el-icon><OfficeBuilding /></el-icon>
            </div>
            <div class="stats-info">
              <div class="stats-value">{{ availableRoomsCount }}</div>
              <div class="stats-label">可用会议室</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card class="stats-card" shadow="hover">
          <div class="stats-content">
            <div class="stats-icon ongoing">
              <el-icon><VideoCamera /></el-icon>
            </div>
            <div class="stats-info">
              <div class="stats-value">{{ ongoingMeetingsCount }}</div>
              <div class="stats-label">进行中会议</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card class="stats-card" shadow="hover">
          <div class="stats-content">
            <div class="stats-icon today">
              <el-icon><Calendar /></el-icon>
            </div>
            <div class="stats-info">
              <div class="stats-value">{{ todayMeetingsCount }}</div>
              <div class="stats-label">今日会议</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card class="stats-card" shadow="hover">
          <div class="stats-content">
            <div class="stats-icon pending">
              <el-icon><Clock /></el-icon>
            </div>
            <div class="stats-info">
              <div class="stats-value">{{ pendingCount }}</div>
              <div class="stats-label">待审批</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 内容区域 -->
    <el-card class="content-card" shadow="never">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="我的预定" name="my">
          <my-bookings />
        </el-tab-pane>

        <el-tab-pane label="全部会议" name="all">
          <meeting-list />
        </el-tab-pane>

        <el-tab-pane label="待审批" name="approval">
          <approval-management />
        </el-tab-pane>

        <el-tab-pane label="会议室管理" name="rooms">
          <room-management />
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { OfficeBuilding, VideoCamera, Calendar, Clock } from '@element-plus/icons-vue'
import { useMeetingStore } from '../store'
import MeetingList from '../components/MeetingList.vue'
import MyBookings from '../components/MyBookings.vue'
import ApprovalManagement from '../components/ApprovalManagement.vue'
import RoomManagement from '../components/RoomManagement.vue'

// Store
const meetingStore = useMeetingStore()

// 当前标签页
const activeTab = ref('my')

// 计算属性
const availableRoomsCount = computed(() => meetingStore.availableRoomsCount)
const ongoingMeetingsCount = computed(() => meetingStore.ongoingMeetingsCount)
const todayMeetingsCount = computed(() => meetingStore.todayMeetingsCount)
const pendingCount = computed(() => meetingStore.pendingCount)

// 标签页切换
function handleTabChange(tabName: string) {
  console.log('切换到标签页:', tabName)
}

// 初始化
onMounted(async () => {
  try {
    await meetingStore.initialize()
  } catch (error) {
    console.error('初始化失败:', error)
  }
})
</script>

<style scoped>
.meeting-module {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 16px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.stats-row {
  margin-bottom: 0;
}

.stats-card {
  margin-bottom: 0;
}

.stats-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stats-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  color: #fff;
}

.stats-icon.available {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.stats-icon.ongoing {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.stats-icon.today {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.stats-icon.pending {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.stats-info {
  flex: 1;
}

.stats-value {
  font-size: 32px;
  font-weight: bold;
  color: #303133;
  line-height: 1;
  margin-bottom: 8px;
}

.stats-label {
  font-size: 14px;
  color: #909399;
}

.content-card {
  flex: 1;
  min-height: 600px;
}

:deep(.el-tabs__content) {
  padding: 16px 0;
}
</style>
