<template>
  <div class="permission-list-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <span class="title">权限管理</span>
        <el-tag class="tag" type="info">权限控制</el-tag>
      </div>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        新增权限
      </el-button>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-cards">
      <div class="stat-card">
        <div class="stat-header">
          <span class="stat-label">总权限数</span>
          <el-icon class="stat-icon" color="#409EFF"><Key /></el-icon>
        </div>
        <div class="stat-value">{{ permStats.total }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-header">
          <span class="stat-label">菜单权限</span>
          <el-icon class="stat-icon" color="#67C23A"><Menu /></el-icon>
        </div>
        <div class="stat-value">{{ permStats.menu }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-header">
          <span class="stat-label">按钮权限</span>
          <el-icon class="stat-icon" color="#E6A23C"><Operation /></el-icon>
        </div>
        <div class="stat-value">{{ permStats.button }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-header">
          <span class="stat-label">接口权限</span>
          <el-icon class="stat-icon" color="#F56C6C"><Connection /></el-icon>
        </div>
        <div class="stat-value">{{ permStats.api }}</div>
      </div>
    </div>

    <!-- Tab切换 -->
    <el-card class="tab-card">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="菜单权限" name="menu">
          <span class="tab-text">菜单权限</span>
        </el-tab-pane>
        <el-tab-pane label="按钮权限" name="button">
          <span class="tab-text">按钮权限</span>
        </el-tab-pane>
        <el-tab-pane label="接口权限" name="api">
          <span class="tab-text">接口权限</span>
        </el-tab-pane>
        <el-tab-pane label="数据权限" name="data">
          <span class="tab-text">数据权限</span>
        </el-tab-pane>
        <el-tab-pane label="全部权限" name="all">
          <span class="tab-text">全部权限</span>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 筛选条件 -->
    <el-card class="filter-card">
      <el-form :inline="true" :model="filterForm" class="filter-form">
        <el-form-item label="关键词">
          <el-input
            v-model="filterForm.keyword"
            placeholder="权限名称/编码"
            clearable
            @clear="handleSearch"
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="所属模块">
          <el-select
            v-model="filterForm.module"
            placeholder="全部模块"
            clearable
            @change="handleSearch"
          >
            <el-option
              v-for="module in modules"
              :key="module"
              :label="module"
              :value="module"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><RefreshLeft /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 权限树 -->
    <el-card class="tree-card">
      <div class="tree-toolbar">
        <div class="tree-actions">
          <el-button size="small" @click="expandAll">
            <el-icon><Plus /></el-icon>
            展开全部
          </el-button>
          <el-button size="small" @click="collapseAll">
            <el-icon><Minus /></el-icon>
            收起全部
          </el-button>
        </div>
      </div>

      <el-scrollbar max-height="600px">
        <el-tree
          ref="treeRef"
          :data="permissionTree"
          :props="treeProps"
          node-key="id"
          :expand-on-click-node="false"
          :default-expand-all="false"
          class="permission-tree"
        >
          <template #default="{ node, data }">
            <div class="tree-node">
              <div class="node-content">
                <el-icon class="node-icon">
                  <Folder v-if="data.type === 'menu'" />
                  <Operation v-else-if="data.type === 'button'" />
                  <Connection v-else-if="data.type === 'api'" />
                  <Grid v-else-if="data.type === 'data'" />
                </el-icon>
                <span class="node-label">{{ data.name }}</span>
                <el-tag :type="getPermissionTypeTag(data.type)" size="small" class="node-tag">
                  {{ getPermissionTypeName(data.type) }}
                </el-tag>
              </div>
              <div class="node-actions">
                <span class="node-code">{{ data.code }}</span>
                <el-button
                  v-if="data.type === activeTab || activeTab === 'all'"
                  link
                  type="primary"
                  size="small"
                  @click="handleEdit(data)"
                >
                  编辑
                </el-button>
                <el-button
                  link
                  type="danger"
                  size="small"
                  @click="handleDelete(data)"
                >
                  删除
                </el-button>
              </div>
            </div>
          </template>
        </el-tree>
      </el-scrollbar>
    </el-card>

    <!-- 权限表单对话框 -->
    <permission-form
      v-model="formVisible"
      :permission-data="currentPermission"
      @success="handleFormSuccess"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus,
  Search,
  RefreshLeft,
  Minus,
  Key,
  Menu,
  Operation,
  Connection,
  Folder,
  Grid
} from '@element-plus/icons-vue'
import { usePermissionStore } from '../store'
import type { Permission, PermissionFilter } from '../types'
import { getPermissionTypeTag, getPermissionTypeName } from '../utils/permission'
import PermissionForm from '../components/PermissionForm.vue'

// Store
const permissionStore = usePermissionStore()

// 状态
const activeTab = ref('all')
const permissionTree = ref<Permission[]>([])
const modules = ref<string[]>([])
const filterForm = reactive<PermissionFilter>({
  keyword: '',
  module: undefined
})

// 表单对话框
const formVisible = ref(false)
const currentPermission = ref<Permission | undefined>(undefined)

// Tree ref
const treeRef = ref()

// 权限统计
const permStats = computed(() => permissionStore.getPermissionStats())

// 树配置
const treeProps = {
  children: 'children',
  label: 'name'
}

// ==================== 方法 ====================

/**
 * 加载权限树
 */
async function loadPermissions() {
  try {
    await permissionStore.loadAllPermissions()

    let permissions = permissionStore.allPermissions

    // 按类型过滤
    if (activeTab.value !== 'all') {
      permissions = filterPermissionsByType(permissions, activeTab.value as any)
    }

    // 搜索过滤
    if (filterForm.keyword) {
      permissions = filterPermissionsByKeyword(permissions, filterForm.keyword)
    }

    // 模块过滤
    if (filterForm.module) {
      permissions = filterPermissionsByModule(permissions, filterForm.module)
    }

    permissionTree.value = permissions
    modules.value = Array.from(new Set(permissionStore.allPermissions.map(p => p.module)))
  } catch (error) {
    ElMessage.error('加载权限列表失败')
  }
}

/**
 * 按类型过滤
 */
function filterPermissionsByType(permissions: Permission[], type: string): Permission[] {
  const result: Permission[] = []

  function traverse(nodes: Permission[]) {
    nodes.forEach(node => {
      if (node.type === type || hasChildOfType(node, type)) {
        const filtered = { ...node }
        if (node.children?.length) {
          filtered.children = []
          node.children.forEach(child => {
            const filteredChild = filterPermissionsByType([child], type)[0]
            if (filteredChild) {
              filtered.children!.push(filteredChild)
            }
          })
        }
        if (node.type === type || filtered.children?.length) {
          result.push(filtered)
        }
      }
    })
  }

  function hasChildOfType(node: Permission, type: string): boolean {
    if (!node.children?.length) return false
    return node.children.some(child => child.type === type || hasChildOfType(child, type))
  }

  traverse(permissions)
  return result
}

/**
 * 按关键词过滤
 */
function filterPermissionsByKeyword(permissions: Permission[], keyword: string): Permission[] {
  const lowerKeyword = keyword.toLowerCase()

  function filter(nodes: Permission[]): Permission[] {
    return nodes.reduce((acc: Permission[], node) => {
      const matchName = node.name.toLowerCase().includes(lowerKeyword)
      const matchCode = node.code.toLowerCase().includes(lowerKeyword)

      const filteredChildren = node.children ? filter(node.children) : []

      if (matchName || matchCode || filteredChildren.length > 0) {
        acc.push({
          ...node,
          children: filteredChildren.length > 0 ? filteredChildren : node.children
        })
      }

      return acc
    }, [])
  }

  return filter(permissions)
}

/**
 * 按模块过滤
 */
function filterPermissionsByModule(permissions: Permission[], module: string): Permission[] {
  function filter(nodes: Permission[]): Permission[] {
    return nodes.reduce((acc: Permission[], node) => {
      if (node.module === module) {
        const filteredChildren = node.children ? filter(node.children) : []
        acc.push({
          ...node,
          children: filteredChildren
        })
      } else if (node.children?.length) {
        const filteredChildren = filter(node.children)
        if (filteredChildren.length > 0) {
          acc.push({
            ...node,
            children: filteredChildren
          })
        }
      }
      return acc
    }, [])
  }

  return filter(permissions)
}

/**
 * Tab切换
 */
function handleTabChange() {
  loadPermissions()
}

/**
 * 搜索
 */
function handleSearch() {
  loadPermissions()
}

/**
 * 重置
 */
function handleReset() {
  filterForm.keyword = ''
  filterForm.module = undefined
  loadPermissions()
}

/**
 * 展开全部
 */
function expandAll() {
  const nodes = treeRef.value?.store.nodesMap
  Object.values(nodes || {}).forEach((node: any) => {
    node.expanded = true
  })
}

/**
 * 收起全部
 */
function collapseAll() {
  const nodes = treeRef.value?.store.nodesMap
  Object.values(nodes || {}).forEach((node: any) => {
    node.expanded = false
  })
}

/**
 * 新增权限
 */
function handleAdd() {
  currentPermission.value = undefined
  formVisible.value = true
}

/**
 * 编辑权限
 */
function handleEdit(permission: Permission) {
  currentPermission.value = { ...permission }
  formVisible.value = true
}

/**
 * 删除权限
 */
async function handleDelete(permission: Permission) {
  try {
    await ElMessageBox.confirm(
      `确定要删除权限"${permission.name}"吗?`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    // TODO: 调用删除权限API
    ElMessage.success('删除成功')
    loadPermissions()
  } catch {
    // 用户取消
  }
}

/**
 * 表单提交成功
 */
function handleFormSuccess() {
  formVisible.value = false
  loadPermissions()
}

// ==================== 生命周期 ====================

onMounted(() => {
  loadPermissions()
})
</script>

<style scoped lang="scss">
.permission-list-container {
  padding: 20px;

  .page-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 20px;

    .header-content {
      display: flex;
      align-items: center;
      gap: 12px;

      .title {
        font-size: 20px;
        font-weight: 600;
        color: #303133;
      }

      .tag {
        font-size: 12px;
      }
    }
  }

  .stats-cards {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
    gap: 16px;
    margin-bottom: 20px;

    .stat-card {
      padding: 20px;
      background: white;
      border-radius: 8px;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
      transition: all 0.3s ease;

      &:hover {
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
        transform: translateY(-2px);
      }

      .stat-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 12px;

        .stat-label {
          font-size: 14px;
          color: #909399;
        }

        .stat-icon {
          font-size: 28px;
          opacity: 0.5;
        }
      }

      .stat-value {
        font-size: 28px;
        font-weight: 600;
        color: #409EFF;
      }
    }
  }

  .tab-card {
    margin-bottom: 20px;

    .tab-text {
      font-size: 14px;
    }
  }

  .filter-card {
    margin-bottom: 20px;

    .filter-form {
      margin-bottom: 0;
    }
  }

  .tree-card {
    .tree-toolbar {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16px;

      .tree-actions {
        display: flex;
        gap: 8px;
      }
    }

    .permission-tree {
      :deep(.el-tree-node__content) {
        height: auto;
        min-height: 40px;
        padding: 8px 0;

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

        .node-content {
          display: flex;
          align-items: center;
          gap: 8px;
          flex: 1;

          .node-icon {
            font-size: 16px;
            color: #909399;
          }

          .node-label {
            font-size: 14px;
            color: #303133;
            font-weight: 500;
          }

          .node-tag {
            margin-left: 8px;
          }
        }

        .node-actions {
          display: flex;
          align-items: center;
          gap: 12px;

          .node-code {
            font-size: 12px;
            color: #909399;
            font-family: 'Courier New', monospace;
          }
        }
      }
    }
  }
}
</style>
