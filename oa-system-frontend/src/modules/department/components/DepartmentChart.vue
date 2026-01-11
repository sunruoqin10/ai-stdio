<template>
  <div class="department-chart">
    <div v-loading="loading" class="chart-container">
      <div v-if="treeData && treeData.length > 0" class="org-tree-wrapper">
        <div
          v-for="root in treeData"
          :key="root.id"
          class="org-tree"
        >
          <TreeNode
            :node="root"
            @node-click="handleNodeClick"
          />
        </div>
      </div>
      <el-empty v-else description="暂无部门数据" :image-size="200" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import TreeNode from './TreeNode.vue'
import type { Department } from '../types'

// ==================== Props ====================

interface Props {
  departments: Department[]
  loading?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  loading: false
})

// ==================== Emits ====================

const emit = defineEmits<{
  'nodeClick': [department: Department]
}>()

// ==================== 响应式数据 ====================

const treeData = ref<any[]>([])

// ==================== 方法 ====================

/**
 * 转换为树形数据格式
 */
function buildTreeData() {
  console.log('buildTreeData - departments:', props.departments)
  console.log('buildTreeData - departments length:', props.departments?.length)

  if (!props.departments || props.departments.length === 0) {
    treeData.value = []
    return
  }

  function transformNode(dept: Department): any {
    return {
      ...dept,
      label: dept.name,
      expanded: true
    }
  }

  treeData.value = props.departments.map(transformNode)
  console.log('Tree data result:', treeData.value)
}

/**
 * 处理节点点击
 */
function handleNodeClick(node: any, event: Event) {
  console.log('Node clicked:', node)
  event?.stopPropagation()

  // 在树形结构中查找部门
  function findDept(treeList: any[], id: string): any {
    for (const dept of treeList) {
      if (dept.id === id) return dept
      if (dept.children) {
        const found = findDept(dept.children, id)
        if (found) return found
      }
    }
    return null
  }

  const department = findDept(props.departments, node.id)
  if (department) {
    emit('nodeClick', department)
  }
}

// ==================== 监听 ====================

watch(
  () => props.departments,
  () => {
    buildTreeData()
  },
  { deep: true, immediate: true }
)
</script>

<style scoped lang="scss">
.department-chart {
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;

  .chart-container {
    width: 100%;
    min-height: 600px;
    background: #fff;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
    padding: 40px;
    overflow: auto;

    .org-tree-wrapper {
      display: flex;
      justify-content: center;
      align-items: flex-start;
      min-height: 500px;
      padding: 20px;
    }
  }
}
</style>
