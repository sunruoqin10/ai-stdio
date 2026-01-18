<template>
  <div class="department-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">组织架构</h2>
      </div>
      <div class="header-actions">
        <el-button type="primary" @click="handleCreate">
          <el-icon><Plus /></el-icon>
          新增部门
        </el-button>
        <el-button @click="handleExport">
          <el-icon><Download /></el-icon>
          导出
        </el-button>
      </div>
    </div>

        <!-- 统计卡片 -->
    <div class="stats-cards">
      <el-row :gutter="16">
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-icon">
              <el-icon :size="32"><OfficeBuilding /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-label">总部门数</div>
              <div class="stat-value">{{ departmentStore.totalCount }}</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-icon">
              <el-icon :size="32"><FolderOpened /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-label">一级部门</div>
              <div class="stat-value">{{ departmentStore.level1Count }}</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-icon">
              <el-icon :size="32"><Histogram /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-label">最大层级</div>
              <div class="stat-value">{{ departmentStore.maxLevel }}</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-icon">
              <el-icon :size="32"><User /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-label">有负责人</div>
              <div class="stat-value">{{ departmentStore.withLeaderCount }}</div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>
    
    <!-- Tab切换 -->
    <el-tabs v-model="activeTab" class="page-tabs">
      <el-tab-pane label="列表视图" name="list">
        <div class="content-container">
          <!-- 筛选面板 -->
          <div class="filter-panel">
            <DepartmentFilterComponent @filter="handleFilter" @reset="handleReset" />
          </div>

          <!-- 部门表格 -->
          <div class="table-container">
            <el-table
              v-loading="departmentStore.loading"
              :data="displayList"
              row-key="id"
              :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
              :row-class-name="getRowClassName"
              default-expand-all
              :height="tableHeight"
              class="department-table"
            >
              <el-table-column prop="name" label="部门名称" min-width="200">
                <template #default="{ row }">
                  <div class="department-name">
                    <el-icon v-if="row.icon" class="dept-icon">
                      <component :is="row.icon" />
                    </el-icon>
                    <span class="dept-name-text">{{ row.name }}</span>
                    <el-tag v-if="row.level === 1" size="small" type="warning" effect="plain" class="level-tag">
                      一级
                    </el-tag>
                    <el-tag v-else-if="row.level === 2" size="small" type="primary" effect="plain" class="level-tag">
                      二级
                    </el-tag>
                    <el-tag v-else-if="row.level >= 3" size="small" type="info" effect="plain" class="level-tag">
                      L{{ row.level }}
                    </el-tag>
                  </div>
                </template>
              </el-table-column>

              <el-table-column prop="shortName" label="简称" width="120" />

              <el-table-column prop="leaderName" label="负责人" width="120">
                <template #default="{ row }">
                  <span v-if="row.leaderName" class="leader-name">{{ row.leaderName }}</span>
                  <span v-else class="text-placeholder">未设置</span>
                </template>
              </el-table-column>

              <el-table-column prop="employeeCount" label="人数" width="80" align="center">
                <template #default="{ row }">
                  <el-tag size="small">{{ row.employeeCount || 0 }}</el-tag>
                </template>
              </el-table-column>

              <el-table-column prop="level" label="层级" width="80" align="center">
                <template #default="{ row }">
                  <el-tag size="small" type="info">Level {{ row.level }}</el-tag>
                </template>
              </el-table-column>

              <el-table-column prop="status" label="状态" width="80" align="center">
                <template #default="{ row }">
                  <el-tag
                    :type="row.status === 'active' ? 'success' : 'info'"
                    size="small"
                  >
                    {{ row.status === 'active' ? '正常' : '停用' }}
                  </el-tag>
                </template>
              </el-table-column>

              <el-table-column label="操作" width="280" fixed="right">
                <template #default="{ row }">
                  <el-button link type="primary" @click="handleView(row)">
                    查看
                  </el-button>
                  <el-button link type="primary" @click="handleAddChild(row)">
                    添加子部门
                  </el-button>
                  <el-button link type="primary" @click="handleEdit(row)">
                    编辑
                  </el-button>
                  <el-button link type="primary" @click="handleMove(row)">
                    移动
                  </el-button>
                  <el-button link type="danger" @click="handleDelete(row)">
                    删除
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </div>
      </el-tab-pane>

      <el-tab-pane label="架构图" name="chart">
        <div class="chart-wrapper">
          <DepartmentChart
            :departments="departmentStore.tree"
            :loading="departmentStore.loading"
            @node-click="handleView"
          />
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 部门表单对话框 -->
    <DepartmentForm
      v-model="showFormDialog"
      :department="currentDepartment"
      @success="handleFormSuccess"
    />

    <!-- 部门详情抽屉 -->
    <DepartmentDetail
      v-model="showDetailDrawer"
      :department-id="currentDepartmentId"
      @edit="handleEdit"
      @addChild="handleAddChild"
      @move="handleMove"
      @delete="handleDelete"
      @view-child="handleViewChild"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { Plus, Download, OfficeBuilding, FolderOpened, Histogram, User } from '@element-plus/icons-vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import type { Department, DepartmentFilter } from '../types'
import { useDepartmentStore } from '../store'
import DepartmentFilterComponent from '../components/DepartmentFilter.vue'
import DepartmentForm from '../components/DepartmentForm.vue'
import DepartmentDetail from '../components/DepartmentDetail.vue'
import DepartmentChart from '../components/DepartmentChart.vue'

// ==================== Store ====================

const departmentStore = useDepartmentStore()

// ==================== 响应式数据 ====================

const activeTab = ref<'list' | 'chart'>('list')
const showFormDialog = ref(false)
const showDetailDrawer = ref(false)
const currentDepartment = ref<Department | null>(null)
const currentDepartmentId = ref<string | null>(null)

const tableHeight = ref(600)
const displayList = computed(() => {
  return activeTab.value === 'list' ? departmentStore.tree : []
})

// ==================== 方法 ====================

/**
 * 加载部门列表
 */
async function loadDepartments() {
  try {
    await departmentStore.loadList('tree')
    console.log('loadDepartments - departmentStore.list:', departmentStore.list)
    console.log('loadDepartments - departmentStore.tree:', departmentStore.tree)
  } catch (error: any) {
    ElMessage.error(error.message || '加载部门列表失败')
  }
}

/**
 * 处理筛选
 */
function handleFilter(filter: DepartmentFilter) {
  departmentStore.setFilter(filter)
  loadDepartments()
}

/**
 * 重置筛选
 */
function handleReset() {
  departmentStore.resetFilter()
  loadDepartments()
}

/**
 * 创建部门
 */
function handleCreate() {
  currentDepartment.value = null
  showFormDialog.value = true
}

/**
 * 查看部门详情
 */
function handleView(department: Department) {
  currentDepartment.value = department
  currentDepartmentId.value = department.id
  showDetailDrawer.value = true
}

/**
 * 编辑部门
 */
function handleEdit(department: Department) {
  currentDepartment.value = department
  showFormDialog.value = true
  showDetailDrawer.value = false
}

/**
 * 添加子部门
 */
function handleAddChild(parent: Department) {
  currentDepartment.value = null // 创建新部门
  // 可以在这里设置parentId
  showFormDialog.value = true
  showDetailDrawer.value = false
}

/**
 * 移动部门
 */
async function handleMove(department: Department) {
  try {
    await ElMessageBox.prompt('请输入新的上级部门ID', '移动部门', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPattern: /.+/,
      inputErrorMessage: '请输入有效的部门ID'
    })

    // TODO: 实现移动逻辑
    ElMessage.success('部门移动成功')
    showDetailDrawer.value = false
    loadDepartments()
  } catch {
    // 用户取消
  }
}

/**
 * 删除部门
 */
async function handleDelete(department: Department) {
  try {
    await ElMessageBox.confirm(
      `确定要删除部门 "${department.name}" 吗?删除后将无法恢复。`,
      '删除确认',
      {
        type: 'warning',
        confirmButtonText: '确定',
        cancelButtonText: '取消'
      }
    )

    await departmentStore.remove(department.id)
    ElMessage.success('删除成功')
    showDetailDrawer.value = false
    loadDepartments()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

/**
 * 查看子部门
 */
function handleViewChild(departmentId: string) {
  currentDepartmentId.value = departmentId
  // 保持详情抽屉打开
}

/**
 * 导出部门列表
 */
async function handleExport() {
  try {
    await departmentStore.exportList()
    ElMessage.success('导出成功')
  } catch (error: any) {
    ElMessage.error(error.message || '导出失败')
  }
}

/**
 * 表单提交成功
 */
function handleFormSuccess() {
  loadDepartments()
}

/**
 * 计算表格高度
 */
function calculateTableHeight() {
  const windowHeight = window.innerHeight
  const headerHeight = 120
  const tabsHeight = 55
  const statsHeight = 120
  const padding = 40
  tableHeight.value = windowHeight - headerHeight - tabsHeight - statsHeight - padding
}

/**
 * 获取表格行的类名(用于层级样式)
 */
function getRowClassName({ row }: { row: Department }) {
  return `department-level-${row.level}`
}

// ==================== 生命周期 ====================

onMounted(async () => {
  await loadDepartments()
  calculateTableHeight()
  window.addEventListener('resize', calculateTableHeight)
})
</script>

<style scoped lang="scss">
.department-page {
  padding: 20px;
  background: #f5f7fa;
  min-height: 100vh;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 20px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);

  .page-title {
    margin: 0;
    font-size: 20px;
    font-weight: 500;
    color: #303133;
  }

  .header-actions {
    display: flex;
    gap: 12px;
  }
}

.page-tabs {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  padding: 20px;

  :deep(.el-tabs__header) {
    margin-bottom: 20px;
  }
}

.content-container {
  display: flex;
  gap: 20px;

  .filter-panel {
    width: 280px;
    flex-shrink: 0;
  }

  .table-container {
    flex: 1;
  }
}

.chart-wrapper {
  width: 100%;
  min-height: 600px;
}

.stats-cards {
  margin-top: 20px;
  padding: 20px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);

  .stat-card {
    display: flex;
    align-items: center;
    padding: 20px;
    background: #f5f7fa;
    border-radius: 8px;
    transition: all 0.3s;

    &:hover {
      background: #ecf5ff;
      transform: translateY(-2px);
    }

    .stat-icon {
      width: 60px;
      height: 60px;
      display: flex;
      align-items: center;
      justify-content: center;
      background: #fff;
      border-radius: 50%;
      color: var(--el-color-primary);
      margin-right: 16px;
    }

    .stat-content {
      flex: 1;

      .stat-label {
        font-size: 14px;
        color: #909399;
        margin-bottom: 8px;
      }

      .stat-value {
        font-size: 28px;
        font-weight: bold;
        color: var(--el-color-primary);
      }
    }
  }
}

.department-table {
  .department-name {
    display: flex;
    align-items: center;
    gap: 8px;

    .dept-icon {
      font-size: 18px;
      color: var(--el-color-primary);
    }

    .dept-name-text {
      font-weight: 500;
      font-size: 14px;
    }

    .level-tag {
      margin-left: 8px;
      font-size: 12px;
      padding: 2px 6px;
      height: 20px;
      line-height: 16px;
    }
  }

  .leader-info {
    display: flex;
    align-items: center;
    gap: 8px;

    span {
      font-size: 13px;
    }
  }

  .text-placeholder {
    color: #c0c4cc;
    font-style: italic;
  }

  // 增强树形结构的层次感
  :deep(.el-table__expand-icon) {
    color: var(--el-color-primary);
    font-size: 14px;

    &.el-table__expand-icon--expanded {
      transform: rotate(90deg);
    }
  }

  // 为不同层级添加不同的视觉标识
  :deep(.department-level-1) {
    .dept-name-text {
      color: #303133;
      font-weight: 600;
      font-size: 15px;
    }

    // 为一级部门添加背景色
    td {
      background-color: #fafafa !important;
    }
  }

  :deep(.department-level-2) {
    .dept-name-text {
      color: #606266;
      font-weight: 500;
      font-size: 14px;
    }
  }

  :deep(.department-level-3) {
    .dept-name-text {
      color: #909399;
      font-weight: 400;
      font-size: 13px;
    }
  }

  :deep(.department-level-4),
  :deep(.department-level-5) {
    .dept-name-text {
      color: #a8abb2;
      font-weight: 400;
      font-size: 13px;
    }
  }
}

:deep(.el-table) {
  font-size: 14px;

  .el-table__cell {
    padding: 12px 0;
  }
}
</style>
