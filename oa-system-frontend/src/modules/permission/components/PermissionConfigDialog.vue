<template>
  <el-dialog
    v-model="dialogVisible"
    :title="`配置角色权限: ${roleName}`"
    width="900px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <div class="permission-config">
      <!-- 搜索和操作 -->
      <div class="config-actions">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索权限..."
          clearable
          class="search-input"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <div class="tree-actions">
          <el-button size="small" @click="expandAll">展开全部</el-button>
          <el-button size="small" @click="collapseAll">收起全部</el-button>
          <el-button size="small" @click="checkAll">全选</el-button>
          <el-button size="small" @click="uncheckAll">取消全选</el-button>
        </div>
      </div>

      <!-- 已选权限统计 -->
      <div class="selected-info">
        <el-tag type="info">已选择 {{ checkedPermissions.length }} 项权限</el-tag>
      </div>

      <!-- 权限树 -->
      <el-scrollbar max-height="500px">
        <el-tree
          ref="treeRef"
          :data="permissionTree"
          :props="treeProps"
          show-checkbox
          node-key="id"
          :default-checked-keys="checkedPermissions"
          :filter-node-method="filterNode"
          :expand-on-click-node="false"
          class="permission-tree"
        >
          <template #default="{ node, data }">
            <div class="tree-node">
              <div class="node-left">
                <el-icon class="node-icon">
                  <Folder v-if="data.type === 'menu'" />
                  <Operation v-else-if="data.type === 'button'" />
                  <Connection v-else-if="data.type === 'api'" />
                  <Grid v-else-if="data.type === 'data'" />
                </el-icon>
                <span class="node-label">{{ data.name }}</span>
              </div>
              <div class="node-right">
                <el-tag :type="getPermissionTypeTag(data.type)" size="small">
                  {{ getPermissionTypeName(data.type) }}
                </el-tag>
                <span class="node-code">{{ data.code }}</span>
              </div>
            </div>
          </template>
        </el-tree>
      </el-scrollbar>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">
          保存配置
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Folder, Operation, Connection, Grid } from '@element-plus/icons-vue'
import type { ElTree } from 'element-plus'
import { usePermissionStore } from '../store'
import type { Permission } from '../types'
import { getPermissionTypeTag, getPermissionTypeName } from '../utils/permission'

// Props
interface Props {
  modelValue: boolean
  roleId: string
  roleName: string
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: false,
  roleId: '',
  roleName: ''
})

// Emits
const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  'success': []
}>()

// Store
const permissionStore = usePermissionStore()

// 状态
const treeRef = ref<InstanceType<typeof ElTree>>()
const submitting = ref(false)
const searchKeyword = ref('')
const checkedPermissions = ref<string[]>([])

// 对话框显示状态
const dialogVisible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

// 权限树
const permissionTree = computed(() => permissionStore.allPermissions)

// 树配置
const treeProps = {
  children: 'children',
  label: 'name'
}

// ==================== 方法 ====================

/**
 * 加载角色权限
 */
async function loadRolePermissions() {
  if (!props.roleId) return

  try {
    // TODO: 调用API获取角色权限
    // const data = await getRolePermissions(props.roleId)
    // checkedPermissions.value = data

    // Mock数据
    checkedPermissions.value = permissionTree.value
      .filter(p => p.type === 'menu')
      .map(p => p.id)
  } catch (error) {
    ElMessage.error('加载角色权限失败')
  }
}

/**
 * 过滤节点
 */
function filterNode(value: string, data: Permission) {
  if (!value) return true
  return data.name.includes(value) || data.code.includes(value)
}

/**
 * 展开全部
 */
function expandAll() {
  const nodes = treeRef.value?.store.nodesMap
  Object.values(nodes || {}).forEach(node => {
    node.expanded = true
  })
}

/**
 * 收起全部
 */
function collapseAll() {
  const nodes = treeRef.value?.store.nodesMap
  Object.values(nodes || {}).forEach(node => {
    node.expanded = false
  })
}

/**
 * 全选
 */
function checkAll() {
  const allKeys = getAllNodeKeys(permissionTree.value)
  treeRef.value?.setCheckedKeys(allKeys)
}

/**
 * 取消全选
 */
function uncheckAll() {
  treeRef.value?.setCheckedKeys([])
}

/**
 * 获取所有节点key
 */
function getAllNodeKeys(permissions: Permission[]): string[] {
  const keys: string[] = []

  function traverse(nodes: Permission[]) {
    nodes.forEach(node => {
      keys.push(node.id)
      if (node.children?.length) {
        traverse(node.children)
      }
    })
  }

  traverse(permissions)
  return keys
}

/**
 * 提交保存
 */
async function handleSubmit() {
  try {
    submitting.value = true

    const checkedKeys = treeRef.value?.getCheckedKeys() || []

    // TODO: 调用API保存角色权限
    // await updateRolePermissions(props.roleId, checkedKeys)

    await new Promise(resolve => setTimeout(resolve, 1000))

    ElMessage.success('权限配置保存成功')
    emit('success')
  } catch (error) {
    ElMessage.error('保存失败')
  } finally {
    submitting.value = false
  }
}

/**
 * 关闭对话框
 */
function handleClose() {
  dialogVisible.value = false
}

// ==================== 监听 ====================

watch(
  () => props.modelValue,
  async (val) => {
    if (val && props.roleId) {
      await permissionStore.loadAllPermissions()
      await loadRolePermissions()
    }
  }
)

watch(searchKeyword, (val) => {
  treeRef.value?.filter(val)
})
</script>

<style scoped lang="scss">
.permission-config {
  .config-actions {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;

    .search-input {
      flex: 1;
      max-width: 300px;
      margin-right: 16px;
    }

    .tree-actions {
      display: flex;
      gap: 8px;
    }
  }

  .selected-info {
    margin-bottom: 16px;
    padding: 12px;
    background: #f5f7fa;
    border-radius: 4px;
  }

  .permission-tree {
    :deep(.el-tree-node__content) {
      height: 40px;

      &:hover {
        background-color: #f5f7fa;
      }
    }

    .tree-node {
      display: flex;
      align-items: center;
      justify-content: space-between;
      width: 100%;
      padding-right: 20px;

      .node-left {
        display: flex;
        align-items: center;
        gap: 8px;

        .node-icon {
          font-size: 16px;
          color: #909399;
        }

        .node-label {
          font-size: 14px;
          color: #303133;
        }
      }

      .node-right {
        display: flex;
        align-items: center;
        gap: 8px;

        .node-code {
          font-size: 12px;
          color: #909399;
          font-family: 'Courier New', monospace;
        }
      }
    }
  }

  .dialog-footer {
    display: flex;
    justify-content: flex-end;
    gap: 12px;
  }
}
</style>
