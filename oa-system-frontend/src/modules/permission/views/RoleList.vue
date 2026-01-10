<template>
  <div class="role-list-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <span class="title">角色管理</span>
        <el-tag class="tag" type="info">权限控制</el-tag>
      </div>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        新增角色
      </el-button>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-cards">
      <div class="stat-card">
        <div class="stat-header">
          <span class="stat-label">总角色数</span>
          <el-icon class="stat-icon" color="#409EFF"><User /></el-icon>
        </div>
        <div class="stat-value">{{ roleStats.total }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-header">
          <span class="stat-label">系统角色</span>
          <el-icon class="stat-icon" color="#F56C6C"><Lock /></el-icon>
        </div>
        <div class="stat-value">{{ roleStats.system }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-header">
          <span class="stat-label">自定义角色</span>
          <el-icon class="stat-icon" color="#67C23A"><Unlock /></el-icon>
        </div>
        <div class="stat-value">{{ roleStats.custom }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-header">
          <span class="stat-label">启用角色</span>
          <el-icon class="stat-icon" color="#409EFF"><CircleCheck /></el-icon>
        </div>
        <div class="stat-value">{{ roleStats.active }}</div>
      </div>
    </div>

    <!-- 筛选条件 -->
    <el-card class="filter-card">
      <el-form :inline="true" :model="filterForm" class="filter-form">
        <el-form-item label="关键词">
          <el-input
            v-model="filterForm.keyword"
            placeholder="角色名称/编码"
            clearable
            @clear="handleSearch"
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="角色类型">
          <el-select
            v-model="filterForm.type"
            placeholder="全部类型"
            clearable
            @change="handleSearch"
          >
            <el-option label="系统角色" value="system" />
            <el-option label="自定义角色" value="custom" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select
            v-model="filterForm.status"
            placeholder="全部状态"
            clearable
            @change="handleSearch"
          >
            <el-option label="启用" value="active" />
            <el-option label="停用" value="disabled" />
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

    <!-- 角色列表 -->
    <el-card class="table-card">
      <el-table
        :data="roleList"
        v-loading="loading"
        stripe
        border
        :height="tableHeight"
      >
        <el-table-column prop="name" label="角色名称" min-width="150">
          <template #default="{ row }">
            <div class="role-name">
              <el-icon v-if="row.type === 'system'" color="#F56C6C"><Lock /></el-icon>
              <el-icon v-else color="#409EFF"><User /></el-icon>
              <span>{{ row.name }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="code" label="角色编码" width="150">
          <template #default="{ row }">
            <el-tag type="info" size="small">{{ row.code }}</el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="type" label="角色类型" width="120">
          <template #default="{ row }">
            <el-tag :type="row.type === 'system' ? 'danger' : 'primary'" size="small">
              {{ row.type === 'system' ? '系统角色' : '自定义角色' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="userCount" label="成员数" width="100" align="center">
          <template #default="{ row }">
            <el-badge :value="row.userCount || 0" :max="999" />
          </template>
        </el-table-column>

        <el-table-column prop="sort" label="排序" width="80" align="center" />

        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 'active' ? 'success' : 'info'" size="small">
              {{ row.status === 'active' ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />

        <el-table-column label="操作" width="320" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleConfigPermission(row)">
              <el-icon><Setting /></el-icon>
              配置权限
            </el-button>
            <el-button link type="primary" @click="handleViewMembers(row)">
              <el-icon><UserFilled /></el-icon>
              成员
            </el-button>
            <el-button link type="primary" @click="handleEdit(row)">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button link type="primary" @click="handleCopy(row)">
              <el-icon><DocumentCopy /></el-icon>
              复制
            </el-button>
            <el-button
              link
              type="danger"
              @click="handleDelete(row)"
              :disabled="row.isPreset || (row.userCount && row.userCount > 0)"
            >
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 角色表单对话框 -->
    <role-form
      v-model="formVisible"
      :role-data="currentRole"
      @success="handleFormSuccess"
    />

    <!-- 权限配置对话框 -->
    <permission-config-dialog
      v-model="permissionDialogVisible"
      :role-id="currentRoleId"
      :role-name="currentRoleName"
      @success="handlePermissionConfigSuccess"
    />

    <!-- 角色成员对话框 -->
    <role-members-dialog
      v-model="membersDialogVisible"
      :role-id="currentRoleId"
      :role-name="currentRoleName"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus,
  Search,
  RefreshLeft,
  User,
  Lock,
  Unlock,
  CircleCheck,
  Setting,
  UserFilled,
  Edit,
  DocumentCopy,
  Delete
} from '@element-plus/icons-vue'
import { usePermissionStore } from '../store'
import type { Role, RoleFilter } from '../types'
import RoleForm from '../components/RoleForm.vue'
import PermissionConfigDialog from '../components/PermissionConfigDialog.vue'
import RoleMembersDialog from '../components/RoleMembersDialog.vue'

// Store
const permissionStore = usePermissionStore()

// 状态
const loading = ref(false)
const roleList = ref<Role[]>([])
const filterForm = reactive<RoleFilter>({
  keyword: '',
  type: undefined,
  status: undefined
})

// 表单对话框
const formVisible = ref(false)
const currentRole = ref<Role | undefined>(undefined)

// 权限配置对话框
const permissionDialogVisible = ref(false)
const currentRoleId = ref('')
const currentRoleName = ref('')

// 成员对话框
const membersDialogVisible = ref(false)

// 表格高度
const tableHeight = ref(500)

// 角色统计
const roleStats = computed(() => permissionStore.getRoleStats())

// ==================== 方法 ====================

/**
 * 加载角色列表
 */
async function loadRoles() {
  loading.value = true
  try {
    await permissionStore.loadRoles()
    roleList.value = permissionStore.allRoles

    // 前端筛选
    if (filterForm.keyword || filterForm.type || filterForm.status) {
      roleList.value = roleList.value.filter(role => {
        let match = true

        if (filterForm.keyword) {
          const keyword = filterForm.keyword.toLowerCase()
          match = match &&
            (role.name.toLowerCase().includes(keyword) ||
             role.code.toLowerCase().includes(keyword))
        }

        if (filterForm.type) {
          match = match && role.type === filterForm.type
        }

        if (filterForm.status) {
          match = match && role.status === filterForm.status
        }

        return match
      })
    }
  } catch (error) {
    ElMessage.error('加载角色列表失败')
  } finally {
    loading.value = false
  }
}

/**
 * 搜索
 */
function handleSearch() {
  loadRoles()
}

/**
 * 重置
 */
function handleReset() {
  filterForm.keyword = ''
  filterForm.type = undefined
  filterForm.status = undefined
  loadRoles()
}

/**
 * 新增角色
 */
function handleAdd() {
  currentRole.value = undefined
  formVisible.value = true
}

/**
 * 编辑角色
 */
function handleEdit(role: Role) {
  currentRole.value = { ...role }
  formVisible.value = true
}

/**
 * 复制角色
 */
async function handleCopy(role: Role) {
  try {
    await ElMessageBox.prompt('请输入新角色名称', '复制角色', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputValue: `${role.name}(副本)`,
      inputPattern: /^.{2,50}$/,
      inputErrorMessage: '角色名称长度在 2 到 50 个字符'
    })

    // TODO: 调用复制角色API
    ElMessage.success('角色复制成功')
    loadRoles()
  } catch {
    // 用户取消
  }
}

/**
 * 删除角色
 */
async function handleDelete(role: Role) {
  if (role.isPreset) {
    ElMessage.warning('系统预置角色不能删除')
    return
  }

  if (role.userCount && role.userCount > 0) {
    ElMessage.warning('该角色有成员,不能删除')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确定要删除角色"${role.name}"吗?删除后无法恢复!`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    // TODO: 调用删除角色API
    ElMessage.success('删除成功')
    loadRoles()
  } catch {
    // 用户取消
  }
}

/**
 * 配置权限
 */
function handleConfigPermission(role: Role) {
  currentRoleId.value = role.id
  currentRoleName.value = role.name
  permissionDialogVisible.value = true
}

/**
 * 查看成员
 */
function handleViewMembers(role: Role) {
  currentRoleId.value = role.id
  currentRoleName.value = role.name
  membersDialogVisible.value = true
}

/**
 * 表单提交成功
 */
function handleFormSuccess() {
  formVisible.value = false
  loadRoles()
}

/**
 * 权限配置成功
 */
function handlePermissionConfigSuccess() {
  permissionDialogVisible.value = false
  // 清除权限缓存
  permissionStore.clearCache()
}

/**
 * 计算表格高度
 */
function calculateTableHeight() {
  const windowHeight = window.innerHeight
  const headerHeight = 100
  const statsHeight = 120
  const filterHeight = 80
  const padding = 100
  tableHeight.value = windowHeight - headerHeight - statsHeight - filterHeight - padding
}

// ==================== 生命周期 ====================

onMounted(() => {
  loadRoles()
  calculateTableHeight()
  window.addEventListener('resize', calculateTableHeight)
})
</script>

<style scoped lang="scss">
.role-list-container {
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

  .filter-card {
    margin-bottom: 20px;

    .filter-form {
      margin-bottom: 0;
    }
  }

  .table-card {
    .role-name {
      display: flex;
      align-items: center;
      gap: 8px;

      .el-icon {
        font-size: 16px;
      }
    }
  }
}
</style>
