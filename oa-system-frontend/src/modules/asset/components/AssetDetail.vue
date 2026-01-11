<template>
  <el-drawer
    v-model="visible"
    :title="asset?.name || '资产详情'"
    size="600px"
    @close="handleClose"
  >
    <div v-if="asset" class="asset-detail">
      <!-- 基本信息 -->
      <section class="detail-section">
        <h3 class="section-title">基本信息</h3>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="资产编号">
            {{ asset.id }}
          </el-descriptions-item>
          <el-descriptions-item label="资产名称">
            {{ asset.name }}
          </el-descriptions-item>
          <el-descriptions-item label="资产类别">
            <el-tag size="small">{{ getCategoryName(asset.category) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="资产状态">
            <el-tag :type="getStatusType(asset.status)" size="small">
              {{ getStatusName(asset.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="品牌/型号" :span="2">
            {{ asset.brandModel || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="存放位置" :span="2">
            {{ asset.location || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="购置日期" :span="2">
            {{ formatDate(asset.purchaseDate) }}
          </el-descriptions-item>
        </el-descriptions>
      </section>

      <!-- 图片展示 -->
      <section v-if="asset.images && asset.images.length > 0" class="detail-section">
        <h3 class="section-title">资产图片</h3>
        <el-image
          v-for="(img, index) in asset.images"
          :key="index"
          :src="img"
          :preview-src-list="asset.images"
          :initial-index="index"
          fit="cover"
          class="asset-image"
        />
      </section>

      <!-- 价值信息 (根据权限控制) -->
      <section class="detail-section">
        <h3 class="section-title">价值信息</h3>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="购置金额">
            {{ formatAmount(asset.purchasePrice) }}
          </el-descriptions-item>
          <el-descriptions-item label="当前价值">
            {{ formatAmount(asset.currentValue || 0) }}
          </el-descriptions-item>
          <el-descriptions-item label="折旧金额" :span="2">
            {{ formatAmount(asset.purchasePrice - (asset.currentValue || 0)) }}
          </el-descriptions-item>
          <el-descriptions-item label="折旧率" :span="2">
            {{ calculateDepreciationRate(asset) }}%
          </el-descriptions-item>
        </el-descriptions>
      </section>

      <!-- 使用人信息 -->
      <section v-if="asset.userId" class="detail-section">
        <h3 class="section-title">使用人信息</h3>
        <div class="user-info">
          <el-avatar :src="asset.userAvatar" :size="60" />
          <div class="user-detail">
            <div class="user-name">{{ asset.userName }}</div>
            <div class="user-id">工号: {{ asset.userId }}</div>
          </div>
        </div>
      </section>

      <!-- 借用信息 -->
      <section v-if="asset.status === 'borrowed'" class="detail-section">
        <h3 class="section-title">借用信息</h3>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="借出日期">
            {{ formatDate(asset.borrowDate!) }}
          </el-descriptions-item>
          <el-descriptions-item label="预计归还">
            {{ formatDate(asset.expectedReturnDate!) }}
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag v-if="checkOverdue(asset)" type="danger" size="small">
              已逾期
            </el-tag>
            <el-tag v-else-if="checkReturnReminder(asset)" type="warning" size="small">
              即将到期
            </el-tag>
            <el-tag v-else type="success" size="small">
              正常
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>
      </section>

      <!-- 备注信息 -->
      <section v-if="asset.notes" class="detail-section">
        <h3 class="section-title">备注信息</h3>
        <div class="notes-content">
          {{ asset.notes }}
        </div>
      </section>

      <!-- 维护记录 -->
      <section v-if="hasMaintenanceRecords" class="detail-section">
        <h3 class="section-title">维护记录</h3>
        <el-timeline>
          <el-timeline-item
            v-for="(record, index) in maintenanceRecords"
            :key="index"
            :timestamp="record.date"
            placement="top"
          >
            <el-card>
              <h4>{{ record.title }}</h4>
              <p>{{ record.description }}</p>
              <p v-if="record.cost">费用: {{ formatAmount(record.cost) }}</p>
              <p class="operator">操作人: {{ record.operator }}</p>
            </el-card>
          </el-timeline-item>
        </el-timeline>
      </section>

      <!-- 操作记录 -->
      <section class="detail-section">
        <h3 class="section-title">操作记录</h3>
        <el-timeline>
          <el-timeline-item timestamp="创建时间" placement="top">
            {{ formatDateTime(asset.createdAt) }}
          </el-timeline-item>
          <el-timeline-item timestamp="更新时间" placement="top">
            {{ formatDateTime(asset.updatedAt) }}
          </el-timeline-item>
        </el-timeline>
      </section>
    </div>

    <template #footer>
      <el-button @click="handleClose">关闭</el-button>
      <el-button type="primary" @click="handleEdit">编辑</el-button>
    </template>
  </el-drawer>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import type { Asset } from '../types'
import {
  formatDate,
  formatAmount,
  getCategoryName,
  getStatusName,
  getStatusType,
  checkReturnReminder,
  checkOverdue
} from '../utils'

interface Props {
  modelValue: boolean
  asset?: Asset | null
}

interface MaintenanceRecord {
  date: string
  title: string
  description: string
  cost?: number
  operator: string
}

const props = withDefaults(defineProps<Props>(), {
  asset: null
})

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  edit: [asset: Asset]
}>()

const visible = ref(false)

// 模拟维护记录
const maintenanceRecords = computed<MaintenanceRecord[]>(() => {
  if (!props.asset || props.asset.status !== 'maintenance') return []
  return [
    {
      date: '2024-12-10',
      title: '送修',
      description: '发现故障,提交维修申请',
      operator: '张三'
    },
    {
      date: '2024-12-12',
      title: '维修中',
      description: '维修师傅正在检修',
      operator: '维修部'
    }
  ]
})

const hasMaintenanceRecords = computed(() => {
  return maintenanceRecords.value.length > 0
})

watch(
  () => props.modelValue,
  (val) => {
    visible.value = val
  }
)

watch(visible, (val) => {
  emit('update:modelValue', val)
})

function calculateDepreciationRate(asset: Asset): number {
  if (!asset.currentValue) return 0
  const rate = ((asset.purchasePrice - asset.currentValue) / asset.purchasePrice) * 100
  return Math.round(rate * 100) / 100
}

function formatDateTime(date: string): string {
  const d = new Date(date)
  return d.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

function handleClose() {
  emit('update:modelValue', false)
}

function handleEdit() {
  if (props.asset) {
    emit('edit', props.asset)
  }
}
</script>

<style scoped lang="scss">
.asset-detail {
  padding: 0 8px;
}

.detail-section {
  margin-bottom: 24px;

  &:last-child {
    margin-bottom: 0;
  }
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 16px 0;
  padding-bottom: 8px;
  border-bottom: 2px solid #409eff;
}

.asset-image {
  width: 100%;
  height: 200px;
  margin-bottom: 12px;
  border-radius: 4px;
  cursor: pointer;

  &:last-child {
    margin-bottom: 0;
  }
}

.user-info {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 4px;
}

.user-detail {
  flex: 1;
}

.user-name {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.user-id {
  font-size: 14px;
  color: #909399;
}

.notes-content {
  padding: 12px;
  background: #f5f7fa;
  border-radius: 4px;
  line-height: 1.6;
  color: #606266;
  white-space: pre-wrap;
}

.operator {
  font-size: 12px;
  color: #909399;
  margin: 0;
}

:deep(.el-timeline-item__timestamp) {
  color: #909399;
  font-size: 13px;
}

:deep(.el-card) {
  margin-bottom: 0;

  h4 {
    margin: 0 0 8px 0;
    font-size: 14px;
    color: #303133;
  }

  p {
    margin: 4px 0;
    font-size: 13px;
    color: #606266;
  }
}
</style>
