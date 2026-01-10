<template>
  <div class="dict-tree">
    <el-tree
      ref="treeRef"
      :data="treeData"
      :props="treeProps"
      node-key="id"
      highlight-current
      default-expand-all
      :empty-text="loading ? '加载中...' : '暂无数据'"
      @node-click="handleNodeClick"
    >
      <template #default="{ node, data }">
        <span class="custom-tree-node">
          <el-icon v-if="data.category === 'system'" class="system-icon">
            <Lock />
          </el-icon>
          <span class="node-label">{{ node.label }}</span>
          <el-tag v-if="data.itemCount !== undefined" size="small" type="info">
            {{ data.itemCount }}
          </el-tag>
        </span>
      </template>
    </el-tree>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { ElTree } from 'element-plus'
import { Lock } from '@element-plus/icons-vue'
import type { DictTreeNode } from '../types'
import { formatDictCategory } from '../utils'

interface Props {
  /** 树数据 */
  data: DictTreeNode[]
  /** 加载状态 */
  loading?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  loading: false
})

interface Emits {
  (e: 'node-click', data: DictTreeNode): void
}

const emit = defineEmits<Emits>()

const treeRef = ref<InstanceType<typeof ElTree>>()

// 树形配置
const treeProps = {
  children: 'children',
  label: 'name'
}

// 构建树形数据
const treeData = computed(() => {
  // 构建分类节点
  const systemDicts = props.data.filter(d => d.category === 'system')
  const businessDicts = props.data.filter(d => d.category === 'business')

  const result: DictTreeNode[] = []

  if (systemDicts.length > 0) {
    result.push({
      id: 'system-root',
      code: 'system',
      name: '系统字典',
      category: 'system',
      itemCount: systemDicts.reduce((sum, d) => sum + (d.itemCount || 0), 0),
      children: systemDicts
    } as any)
  }

  if (businessDicts.length > 0) {
    result.push({
      id: 'business-root',
      code: 'business',
      name: '业务字典',
      category: 'business',
      itemCount: businessDicts.reduce((sum, d) => sum + (d.itemCount || 0), 0),
      children: businessDicts
    } as any)
  }

  return result
})

// 节点点击处理
function handleNodeClick(data: DictTreeNode) {
  // 只点击叶子节点(字典类型)
  if (!data.children || data.children.length === 0) {
    emit('node-click', data)
  }
}

// 展开所有节点
function expandAll() {
  const allKeys = treeRef.value?.store.getAllNodeKeys()
  allKeys?.forEach(key => {
    const node = treeRef.value?.store.nodesMap[key]
    node?.expand()
  })
}

// 折叠所有节点
function collapseAll() {
  const allKeys = treeRef.value?.store.getAllNodeKeys()
  allKeys?.forEach(key => {
    const node = treeRef.value?.store.nodesMap[key]
    node?.collapse()
  })
}

// 设置当前选中节点
function setCurrentKey(key: string) {
  treeRef.value?.setCurrentKey(key)
}

// 暴露方法
defineExpose({
  expandAll,
  collapseAll,
  setCurrentKey
})
</script>

<style scoped lang="scss">
.dict-tree {
  padding: 10px 0;

  :deep(.el-tree-node__content) {
    height: 40px;
    padding-right: 10px;

    &:hover {
      background-color: var(--el-fill-color-light);
    }
  }

  .custom-tree-node {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: space-between;
    font-size: 14px;
    padding-right: 8px;

    .system-icon {
      margin-right: 6px;
      color: var(--el-color-warning);
      font-size: 16px;
    }

    .node-label {
      flex: 1;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .el-tag {
      margin-left: 8px;
    }
  }
}
</style>
