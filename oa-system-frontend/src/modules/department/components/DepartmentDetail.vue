<template>
  <el-drawer
    v-model="visible"
    title="部门详情"
    size="600px"
    @close="handleClose"
  >
    <div v-if="loading" class="loading-container">
      <el-icon class="is-loading"><Loading /></el-icon>
    </div>

    <div v-else-if="department" class="detail-container">
      <!-- 基本信息 -->
      <div class="info-section">
        <h3 class="section-title">基本信息</h3>
        <div class="info-list">
          <div class="info-item">
            <span class="info-label">部门编号:</span>
            <span class="info-value">{{ department.id }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">部门名称:</span>
            <span class="info-value">{{ department.name }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">部门简称:</span>
            <span class="info-value">{{ department.shortName || '-' }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">上级部门:</span>
            <span class="info-value">{{ parentDepartment?.name || '-' }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">部门层级:</span>
            <el-tag size="small">Level {{ department.level }}</el-tag>
          </div>
          <div class="info-item">
            <span class="info-label">排序号:</span>
            <span class="info-value">{{ department.sort }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">部门状态:</span>
            <el-tag :type="department.status === 'active' ? 'success' : 'info'">
              {{ department.status === 'active' ? '正常' : '停用' }}
            </el-tag>
          </div>
          <div class="info-item">
            <span class="info-label">成立时间:</span>
            <span class="info-value">{{ department.establishedDate || '-' }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">部门描述:</span>
            <span class="info-value">{{ department.description || '-' }}</span>
          </div>
        </div>
      </div>

      <!-- 负责人信息 -->
      <div class="info-section">
        <h3 class="section-title">负责人信息</h3>
        <div class="leader-info" v-if="department.leader">
          <el-avatar :size="60" :src="department.leader.avatar">
            {{ department.leader.name.charAt(0) }}
          </el-avatar>
          <div class="leader-details">
            <div class="leader-name">{{ department.leader.name }}</div>
            <div class="leader-id">工号: {{ department.leader.id }}</div>
          </div>
        </div>
        <div v-else class="empty-leader">
          <el-empty description="未设置负责人" :image-size="80" />
        </div>
      </div>

      <!-- 统计信息 -->
      <div class="info-section">
        <h3 class="section-title">统计信息</h3>
        <el-row :gutter="16">
          <el-col :span="12">
            <div class="stat-card">
              <div class="stat-label">部门人数</div>
              <div class="stat-value">{{ department.employeeCount || 0 }}</div>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="stat-card">
              <div class="stat-label">子部门数</div>
              <div class="stat-value">{{ childDepartments.length }}</div>
            </div>
          </el-col>
        </el-row>
      </div>

      <!-- 子部门列表 -->
      <div v-if="childDepartments.length > 0" class="info-section">
        <h3 class="section-title">子部门列表</h3>
        <el-scrollbar max-height="200px">
          <div class="child-list">
            <div
              v-for="child in childDepartments"
              :key="child.id"
              class="child-item"
              @click="handleViewChild(child)"
            >
              <div class="child-name">{{ child.name }}</div>
              <div class="child-count">{{ child.employeeCount || 0 }}人</div>
            </div>
          </div>
        </el-scrollbar>
      </div>

      <!-- 操作按钮 -->
      <div class="action-buttons">
        <el-button type="primary" @click="handleEdit">编辑</el-button>
        <el-button @click="handleAddChild">添加子部门</el-button>
        <el-button @click="handleMove">移动</el-button>
        <el-button type="danger" @click="handleDelete">删除</el-button>
      </div>
    </div>
  </el-drawer>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { Loading } from '@element-plus/icons-vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import type { Department } from '../types'
import { useDepartmentStore } from '../store'

// ==================== Props ====================

interface Props {
  modelValue: boolean
  departmentId?: string | null
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: false,
  departmentId: null
})

// ==================== Emits ====================

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  'edit': [department: Department]
  'addChild': [parent: Department]
  'move': [department: Department]
  'delete': [department: Department]
  'viewChild': [departmentId: string]
}>()

// ==================== Store ====================

const departmentStore = useDepartmentStore()

// ==================== 响应式数据 ====================

const visible = ref(false)
const loading = ref(false)
const department = ref<Department | null>(null)

const parentDepartment = computed(() => {
  if (!department.value?.parentId) return null
  return departmentStore.list.find(d => d.id === department.value.parentId)
})

const childDepartments = computed(() => {
  if (!department.value) return []
  return departmentStore.list.filter(d => d.parentId === department.value!.id)
})

// ==================== 方法 ====================

/**
 * 加载部门详情
 */
async function loadDepartmentDetail() {
  if (!props.departmentId) return

  loading.value = true
  try {
    const data = await departmentStore.loadDetail(props.departmentId)
    department.value = data
  } finally {
    loading.value = false
  }
}

/**
 * 编辑部门
 */
function handleEdit() {
  if (!department.value) return
  emit('edit', department.value)
}

/**
 * 添加子部门
 */
function handleAddChild() {
  if (!department.value) return
  emit('addChild', department.value)
}

/**
 * 移动部门
 */
function handleMove() {
  if (!department.value) return
  emit('move', department.value)
}

/**
 * 删除部门
 */
async function handleDelete() {
  if (!department.value) return

  try {
    await ElMessageBox.confirm(
      '确定要删除该部门吗?删除后将无法恢复。',
      '删除确认',
      {
        type: 'warning',
        confirmButtonText: '确定',
        cancelButtonText: '取消'
      }
    )

    emit('delete', department.value)
    handleClose()
  } catch {
    // 用户取消
  }
}

/**
 * 查看子部门
 */
function handleViewChild(child: Department) {
  emit('viewChild', child.id)
}

/**
 * 关闭抽屉
 */
function handleClose() {
  department.value = null
  emit('update:modelValue', false)
}

// ==================== 监听 ====================

watch(
  () => props.modelValue,
  (val) => {
    visible.value = val
    if (val && props.departmentId) {
      loadDepartmentDetail()
    }
  },
  { immediate: true }
)

watch(
  () => props.departmentId,
  (newId) => {
    if (newId && visible.value) {
      loadDepartmentDetail()
    }
  }
)
</script>

<style scoped lang="scss">
.loading-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 400px;
  font-size: 32px;
  color: var(--el-color-primary);
}

.detail-container {
  padding: 20px;
}

.info-section {
  margin-bottom: 32px;

  .section-title {
    font-size: 16px;
    font-weight: 500;
    margin-bottom: 16px;
    padding-bottom: 8px;
    border-bottom: 2px solid var(--el-color-primary);
  }
}

.info-list {
  .info-item {
    display: flex;
    margin-bottom: 12px;
    align-items: center;

    .info-label {
      width: 100px;
      color: var(--el-text-color-secondary);
      flex-shrink: 0;
    }

    .info-value {
      flex: 1;
      color: var(--el-text-color-primary);
    }
  }
}

.leader-info {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: var(--el-fill-color-light);
  border-radius: 8px;

  .leader-details {
    flex: 1;

    .leader-name {
      font-size: 18px;
      font-weight: 500;
      margin-bottom: 8px;
    }

    .leader-id {
      font-size: 14px;
      color: var(--el-text-color-secondary);
    }
  }
}

.empty-leader {
  padding: 20px;
  background: var(--el-fill-color-light);
  border-radius: 8px;
}

.stat-card {
  padding: 20px;
  background: var(--el-fill-color-light);
  border-radius: 8px;
  text-align: center;

  .stat-label {
    font-size: 14px;
    color: var(--el-text-color-secondary);
    margin-bottom: 8px;
  }

  .stat-value {
    font-size: 28px;
    font-weight: bold;
    color: var(--el-color-primary);
  }
}

.child-list {
  .child-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 16px;
    margin-bottom: 8px;
    background: var(--el-fill-color-light);
    border-radius: 6px;
    cursor: pointer;
    transition: all 0.3s;

    &:hover {
      background: var(--el-color-primary-light-9);
      transform: translateX(4px);
    }

    .child-name {
      font-weight: 500;
    }

    .child-count {
      font-size: 12px;
      color: var(--el-text-color-secondary);
    }
  }
}

.action-buttons {
  display: flex;
  gap: 12px;
  padding-top: 20px;
  border-top: 1px solid var(--el-border-color);

  .el-button {
    flex: 1;
  }
}
</style>
