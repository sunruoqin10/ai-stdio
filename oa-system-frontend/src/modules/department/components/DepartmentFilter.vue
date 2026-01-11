<template>
  <el-card class="filter-card" shadow="never">
    <template #header>
      <div class="card-header">
        <span>筛选条件</span>
        <el-button link type="primary" @click="handleReset">重置</el-button>
      </div>
    </template>

    <el-form :model="filterForm" label-width="80px" class="filter-form">
      <el-form-item label="关键词">
        <el-input
          v-model="filterForm.keyword"
          placeholder="搜索部门名称/简称"
          clearable
          @change="handleFilterChange"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
      </el-form-item>

      <el-form-item label="状态">
        <el-select
          v-model="filterForm.status"
          placeholder="请选择状态"
          clearable
          @change="handleFilterChange"
        >
          <el-option
            v-for="item in statusOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="层级">
        <el-select
          v-model="filterForm.level"
          placeholder="请选择层级"
          clearable
          @change="handleFilterChange"
        >
          <el-option label="一级部门" :value="1" />
          <el-option label="二级部门" :value="2" />
          <el-option label="三级部门" :value="3" />
          <el-option label="四级部门" :value="4" />
          <el-option label="五级部门" :value="5" />
        </el-select>
      </el-form-item>

      <el-form-item label="负责人">
        <el-select
          v-model="filterForm.leaderId"
          placeholder="请选择负责人"
          filterable
          clearable
          @change="handleFilterChange"
        >
          <el-option
            v-for="leader in leaders"
            :key="leader.id"
            :label="leader.name"
            :value="leader.id"
          />
        </el-select>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { Search } from '@element-plus/icons-vue'
import type { DepartmentFilter } from '../types'
import { useDepartmentStore } from '../store'
import { getDictList } from '../api'

// ==================== Emits ====================

const emit = defineEmits<{
  'filter': [filter: DepartmentFilter]
  'reset': []
}>()

// ==================== Store ====================

const departmentStore = useDepartmentStore()

// ==================== 响应式数据 ====================

const filterForm = reactive<DepartmentFilter>({
  keyword: '',
  status: undefined,
  level: undefined,
  leaderId: undefined
})

const statusOptions = ref<any[]>([])
const leaders = computed(() => {
  // 从部门列表中提取所有负责人
  const leaderMap = new Map()
  departmentStore.list.forEach(dept => {
    if (dept.leader && dept.leaderId) {
      leaderMap.set(dept.leaderId, dept.leader)
    }
  })
  return Array.from(leaderMap.values())
})

// ==================== 方法 ====================

/**
 * 加载数据字典
 */
async function loadDictData() {
  try {
    const data = await getDictList('department_status')
    statusOptions.value = data
  } catch (error) {
    console.error('加载字典数据失败:', error)
  }
}

/**
 * 处理筛选条件变化
 */
function handleFilterChange() {
  emit('filter', { ...filterForm })
}

/**
 * 重置筛选条件
 */
function handleReset() {
  filterForm.keyword = ''
  filterForm.status = undefined
  filterForm.level = undefined
  filterForm.leaderId = undefined
  emit('reset')
}

// ==================== 生命周期 ====================

onMounted(() => {
  loadDictData()
})
</script>

<style scoped lang="scss">
.filter-card {
  :deep(.el-card__header) {
    padding: 16px 20px;
    background: var(--el-fill-color-light);
  }

  :deep(.el-card__body) {
    padding: 20px;
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-weight: 500;
  }
}

.filter-form {
  :deep(.el-form-item) {
    margin-bottom: 18px;
  }

  :deep(.el-select),
  :deep(.el-input) {
    width: 100%;
  }
}
</style>
