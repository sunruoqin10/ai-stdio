<template>
  <div class="room-management">
    <el-table v-loading="loading" :data="rooms" stripe>
      <el-table-column prop="name" label="会议室名称" width="150" />
      <el-table-column prop="location" label="位置" width="120" />
      <el-table-column prop="floor" label="楼层" width="80" />
      <el-table-column prop="capacity" label="容纳人数" width="100" />
      <el-table-column label="设备" min-width="200">
        <template #default="{ row }">
          <el-tag
            v-for="eq in row.equipments"
            :key="eq.id"
            size="small"
            style="margin-right: 5px; margin-bottom: 5px"
          >
            {{ getEquipmentTypeName(eq.type) }} × {{ eq.quantity }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="getRoomStatusType(row.status)">
            {{ getRoomStatusName(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleView(row)">查看</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 会议室详情对话框 -->
    <el-dialog
      v-model="detailVisible"
      title="会议室详情"
      width="600px"
    >
      <el-descriptions v-if="currentRoom" :column="2" border>
        <el-descriptions-item label="会议室名称" :span="2">
          {{ currentRoom.name }}
        </el-descriptions-item>
        <el-descriptions-item label="位置">
          {{ currentRoom.location }}
        </el-descriptions-item>
        <el-descriptions-item label="楼层">
          {{ currentRoom.floor }}楼
        </el-descriptions-item>
        <el-descriptions-item label="容量">
          {{ currentRoom.capacity }}人
        </el-descriptions-item>
        <el-descriptions-item label="面积">
          {{ currentRoom.area }}㎡
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getRoomStatusType(currentRoom.status)">
            {{ getRoomStatusName(currentRoom.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="设备" :span="2">
          <div style="display: flex; flex-wrap: wrap; gap: 8px">
            <el-tag
              v-for="eq in currentRoom.equipments"
              :key="eq.id"
              :type="eq.available ? 'success' : 'info'"
            >
              {{ getEquipmentTypeName(eq.type) }} × {{ eq.quantity }}
              {{ !eq.available ? '(不可用)' : '' }}
            </el-tag>
          </div>
        </el-descriptions-item>
        <el-descriptions-item label="描述" :span="2">
          {{ currentRoom.description || '暂无描述' }}
        </el-descriptions-item>
      </el-descriptions>

      <template #footer>
        <el-button type="primary" @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useMeetingStore } from '../store'
import {
  getRoomStatusName,
  getRoomStatusType,
  getEquipmentTypeName
} from '../utils'
import type { MeetingRoom } from '../types'

const meetingStore = useMeetingStore()
const loading = ref(false)
const rooms = computed(() => meetingStore.rooms)

// 详情对话框
const detailVisible = ref(false)
const currentRoom = ref<MeetingRoom | null>(null)

async function loadData() {
  loading.value = true
  try {
    await meetingStore.loadRooms()
  } finally {
    loading.value = false
  }
}

function handleView(row: MeetingRoom) {
  currentRoom.value = row
  detailVisible.value = true
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.room-management {
  min-height: 400px;
}
</style>
