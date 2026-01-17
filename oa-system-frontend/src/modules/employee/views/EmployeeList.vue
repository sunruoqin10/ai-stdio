<template>
  <div class="employee-list-page">
    <PageHeader title="员工名录" :show-add="true" add-text="新增员工" @add="handleAdd" />

    <div class="page-content">
      <el-row :gutter="16">
        <!-- 左侧筛选面板 -->
        <el-col
          :xs="24"
          :sm="24"
          :md="leftFilterVisible ? 6 : 0"
          :lg="leftFilterVisible ? 5 : 0"
          class="filter-col"
        >
          <transition name="slide-fade">
            <FilterPanel
              v-if="leftFilterVisible"
              @search="handleSearch"
              @reset="handleReset"
            />
          </transition>
        </el-col>

        <!-- 中间数据表格 -->
        <el-col
          :xs="24"
          :sm="24"
          :md="middleColSpan"
          :lg="middleColSpan"
          class="table-col"
        >
          <el-card shadow="never">
            <!-- 顶部操作区 -->
            <template #header>
              <div class="table-header">
                <div class="header-left">
                  <el-button
                    @click="leftFilterVisible = !leftFilterVisible"
                    circle
                    size="small"
                    title="切换筛选面板"
                  >
                    <el-icon>
                      <DArrowLeft v-if="leftFilterVisible" />
                      <DArrowRight v-else />
                    </el-icon>
                  </el-button>
                  <el-input
                    v-model="keyword"
                    placeholder="搜索姓名/工号/手机号"
                    clearable
                    style="width: 250px"
                    @keyup.enter="handleQuickSearch"
                    @clear="handleQuickSearch"
                  >
                    <template #prefix>
                      <el-icon><Search /></el-icon>
                    </template>
                    <template #append>
                      <el-button :icon="Search" @click="handleQuickSearch" />
                    </template>
                  </el-input>
                </div>
                <div class="header-right">
                  <el-button-group>
                    <el-button
                      :type="viewMode === 'table' ? 'primary' : ''"
                      @click="viewMode = 'table'"
                    >
                      <el-icon><List /></el-icon>
                      表格
                    </el-button>
                    <el-button
                      :type="viewMode === 'card' ? 'primary' : ''"
                      @click="viewMode = 'card'"
                    >
                      <el-icon><Grid /></el-icon>
                      卡片
                    </el-button>
                  </el-button-group>
                  <el-button :icon="Download">导出列表</el-button>
                  <el-button
                    @click="rightStatsVisible = !rightStatsVisible"
                    circle
                    size="small"
                    title="切换统计面板"
                  >
                    <el-icon>
                      <DArrowRight v-if="rightStatsVisible" />
                      <DArrowLeft v-else />
                    </el-icon>
                  </el-button>
                </div>
              </div>
            </template>

            <!-- 表格视图 -->
            <el-table
              v-if="viewMode === 'table'"
              v-loading="store.loading"
              :data="store.employeeList"
              stripe
              @row-click="handleRowClick"
            >
              <el-table-column type="selection" width="50" />
              <el-table-column label="头像" width="60">
                <template #default="{ row }">
                  <el-avatar :src="row.avatar" :size="40" />
                </template>
              </el-table-column>
              <el-table-column prop="employeeNo" label="员工编号" width="120">
                <template #default="{ row }">
                  <el-link type="primary" @click.stop="handleViewDetail(row.id)">
                    {{ row.employeeNo }}
                  </el-link>
                </template>
              </el-table-column>
              <el-table-column prop="name" label="姓名" width="100" />
              <el-table-column prop="departmentName" label="部门" width="120" />
              <el-table-column label="职位" width="120">
                <template #default="{ row }">
                  {{ row.positionLabel || row.position }}
                </template>
              </el-table-column>
              <el-table-column prop="phone" label="联系电话" width="130" />
              <el-table-column label="试用期" width="100">
                <template #default="{ row }">
                  <StatusTag :status="row.probationStatus" :label="row.probationStatusLabel" />
                </template>
              </el-table-column>
              <el-table-column label="状态" width="80">
                <template #default="{ row }">
                  <StatusTag :status="row.status" :label="row.statusLabel" />
                </template>
              </el-table-column>
              <el-table-column prop="joinDate" label="入职日期" width="120" />
              <el-table-column label="操作" width="150" fixed="right" align="center">
                <template #default="{ row }">
                  <div class="action-buttons">
                    <el-button link type="primary" size="small" @click.stop="handleEdit(row)">
                      编辑
                    </el-button>
                    <el-dropdown @command="(cmd) => handleCommand(cmd, row)" trigger="click">
                      <el-button link type="primary" size="small" @click.stop>
                        更多<el-icon class="el-icon--right"><arrow-down /></el-icon>
                      </el-button>
                      <template #dropdown>
                        <el-dropdown-menu>
                          <el-dropdown-item command="delete">删除</el-dropdown-item>
                          <el-dropdown-item command="reset">重置密码</el-dropdown-item>
                        </el-dropdown-menu>
                      </template>
                    </el-dropdown>
                  </div>
                </template>
              </el-table-column>
            </el-table>

            <!-- 卡片视图 -->
            <div v-else class="card-view">
              <el-row :gutter="16">
                <el-col
                  v-for="item in store.employeeList"
                  :key="item.id"
                  :xs="24"
                  :sm="12"
                  :md="8"
                  :lg="6"
                >
                  <div class="employee-card" @click="handleViewDetail(item.id)">
                    <div class="card-avatar">
                      <el-avatar :src="item.avatar" :size="80" />
                    </div>
                    <div class="card-info">
                      <div class="card-name">{{ item.name }}</div>
                      <div class="card-no">{{ item.employeeNo }}</div>
                      <div class="card-position">{{ item.positionLabel || item.position }}</div>
                      <div class="card-department">{{ item.departmentName }}</div>
                      <div class="card-status">
                        <StatusTag :status="item.status" :label="item.statusLabel" />
                        <StatusTag :status="item.probationStatus" :label="item.probationStatusLabel" />
                      </div>
                    </div>
                  </div>
                </el-col>
              </el-row>
            </div>

            <!-- 分页 -->
            <div class="pagination-container">
              <el-pagination
                v-model:current-page="store.pagination.page"
                v-model:page-size="store.pagination.pageSize"
                :page-sizes="[20, 50, 100]"
                :total="store.total"
                layout="total, sizes, prev, pager, next, jumper"
                @size-change="handleSizeChange"
                @current-change="handlePageChange"
              />
            </div>
          </el-card>
        </el-col>

        <!-- 右侧统计面板 -->
        <el-col
          :xs="24"
          :sm="24"
          :md="rightStatsVisible ? 6 : 0"
          :lg="rightStatsVisible ? 5 : 0"
          class="stats-col"
        >
          <transition name="slide-fade">
            <el-card
              v-if="rightStatsVisible"
              shadow="never"
              class="statistics-card"
            >
              <template #header>
                <span>快捷统计</span>
              </template>
              <div class="stat-item">
                <div class="stat-label">总员工数</div>
                <div class="stat-value">{{ store.total }}</div>
              </div>
              <el-divider />
              <div class="stat-item">
                <div class="stat-label">在职人数</div>
                <div class="stat-value stat-success">
                  {{ store.employeeList.filter(e => e.status === 'active').length }}
                  <el-icon><TrendCharts /></el-icon>
                </div>
              </div>
              <el-divider />
              <div class="stat-item">
                <div class="stat-label">试用期人数</div>
                <div class="stat-value stat-warning">
                  {{ store.employeeList.filter(e => e.probationStatus === 'probation').length }}
                </div>
              </div>
              <el-divider />
              <div class="stat-item">
                <div class="stat-label">本月新入职</div>
                <div class="stat-value">1</div>
              </div>
            </el-card>
          </transition>
        </el-col>
      </el-row>
    </div>

    <!-- 新增/编辑员工弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="800px"
      @close="handleDialogClose"
    >
      <EmployeeForm
        v-if="dialogVisible"
        :employee="currentEmployee"
        @submit="handleSubmit"
        @cancel="handleDialogClose"
      />
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search,
  Download,
  List,
  Grid,
  ArrowDown,
  TrendCharts,
  DArrowLeft,
  DArrowRight
} from '@element-plus/icons-vue'
import { useEmployeeStore } from '../store'
import PageHeader from '@/components/common/PageHeader.vue'
import StatusTag from '@/components/common/StatusTag.vue'
import FilterPanel from '../components/FilterPanel.vue'
import EmployeeForm from '../components/EmployeeForm.vue'
import type { Employee, EmployeeFilter, EmployeeForm as EmployeeFormType } from '../types'

const router = useRouter()
const store = useEmployeeStore()

const viewMode = ref<'table' | 'card'>('table')
const keyword = ref('')
const dialogVisible = ref(false)
const currentEmployee = ref<Employee | undefined>(undefined)

// 左右面板可见性状态
const leftFilterVisible = ref(true)
const rightStatsVisible = ref(true)

const dialogTitle = computed(() => (currentEmployee.value ? '编辑员工' : '新增员工'))

// 计算中间列的跨度
const middleColSpan = computed(() => {
  let span = 14 // 默认跨度
  if (!leftFilterVisible.value) span += 5
  if (!rightStatsVisible.value) span += 5
  // 最大跨度为 24
  return Math.min(span, 24)
})

onMounted(() => {
  store.fetchEmployeeList()
})

function handleAdd() {
  currentEmployee.value = undefined
  dialogVisible.value = true
}

function handleEdit(employee: Employee) {
  currentEmployee.value = employee
  dialogVisible.value = true
}

function handleViewDetail(id: string) {
  router.push(`/employee/${id}`)
}

function handleRowClick(row: Employee) {
  handleViewDetail(row.id)
}

async function handleCommand(command: string, employee: Employee) {
  if (command === 'delete') {
    try {
      await ElMessageBox.confirm(
        `确定要删除员工 ${employee.name} 吗?`,
        '确认删除',
        {
          type: 'warning',
        }
      )

      const success = await store.deleteEmployee(employee.id)
      if (success) {
        ElMessage.success('删除成功')
      } else {
        ElMessage.error('删除失败')
      }
    } catch {
      // 用户取消
    }
  } else if (command === 'reset') {
    ElMessage.info('重置密码功能开发中')
  }
}

async function handleSubmit(data: EmployeeFormType) {
  try {
    const success = currentEmployee.value
      ? await store.updateEmployee(currentEmployee.value.id, data)
      : await store.createEmployee(data)

    if (success) {
      ElMessage.success(currentEmployee.value ? '更新成功' : '创建成功')
      dialogVisible.value = false
    } else {
      ElMessage.error(currentEmployee.value ? '更新失败' : '创建失败')
    }
  } catch (error) {
    console.error('提交失败:', error)
    ElMessage.error('操作失败，请重试')
  }
}

function handleDialogClose() {
  dialogVisible.value = false
  currentEmployee.value = undefined
}

// 快速搜索（从顶部搜索框触发）
function handleQuickSearch() {
  // 重置到第一页
  store.changePage(1)
  // 更新筛选条件并获取列表
  store.updateFilter({ keyword: keyword.value })
  store.fetchEmployeeList()
}

function handleSearch(filter?: EmployeeFilter) {
  if (filter) {
    store.updateFilter(filter)
  } else {
    store.updateFilter({ keyword: keyword.value })
  }
  store.fetchEmployeeList()
}

function handleReset() {
  keyword.value = ''
  store.resetFilter()
  store.fetchEmployeeList()
}

function handlePageChange(page: number) {
  store.changePage(page)
  store.fetchEmployeeList()
}

function handleSizeChange(size: number) {
  store.changePage(1, size)
  store.fetchEmployeeList()
}
</script>

<style lang="scss" scoped>
@use '@/assets/styles/variables.scss' as *;
@use '@/assets/styles/mixins.scss' as *;

.employee-list-page {
  .page-content {
    .table-header {
      @include flex-between;
    }
  }

  // 面板切换按钮样式
  .header-left,
  .header-right {
    display: flex;
    align-items: center;
    gap: $spacing-sm;
  }

  // 过渡动画
  .slide-fade-enter-active {
    transition: all 0.3s ease-out;
  }

  .slide-fade-leave-active {
    transition: all 0.3s cubic-bezier(1, 0.5, 0.8, 1);
  }

  .slide-fade-enter-from,
  .slide-fade-leave-to {
    transform: translateX(20px);
    opacity: 0;
  }

  // 面板列样式优化
  .filter-col,
  .stats-col,
  .table-col {
    transition: all 0.3s ease;
  }

  // 确保表格卡片能够充分利用空间
  .table-col {
    :deep(.el-card) {
      height: 100%;
      display: flex;
      flex-direction: column;

      .el-card__body {
        flex: 1;
        overflow: auto;
      }
    }
  }

  .action-buttons {
    display: flex;
    align-items: center;
    gap: 8px;
    justify-content: center;
  }

  .card-view {
    .employee-card {
      @include card-base;
      padding: $spacing-lg;
      margin-bottom: $spacing-lg;
      cursor: pointer;
      transition: $transition-base;

      &:hover {
        @include card-hover;
      }

      .card-avatar {
        @include flex-center;
        margin-bottom: $spacing-md;
      }

      .card-info {
        text-align: center;

        .card-name {
          font-size: $font-size-lg;
          font-weight: $font-weight-bold;
          color: $text-title;
          margin-bottom: $spacing-xs;
        }

        .card-no {
          font-size: $font-size-sm;
          color: $text-secondary;
          margin-bottom: $spacing-sm;
        }

        .card-position,
        .card-department {
          font-size: $font-size-base;
          color: $text-primary;
          margin-bottom: $spacing-xs;
        }

        .card-status {
          @include flex-center;
          gap: $spacing-sm;
          margin-top: $spacing-md;
        }
      }
    }
  }

  .pagination-container {
    @include flex-center;
    margin-top: $spacing-xl;
  }

  .statistics-card {
    position: sticky;
    top: $spacing-lg;

    .stat-item {
      text-align: center;

      .stat-label {
        font-size: $font-size-base;
        color: $text-secondary;
        margin-bottom: $spacing-sm;
      }

      .stat-value {
        font-size: $font-size-title-xl;
        font-weight: $font-weight-bold;
        color: $text-title;

        &.stat-success {
          color: $success-color;
        }

        &.stat-warning {
          color: $warning-color;
        }

        .el-icon {
          margin-left: $spacing-xs;
        }
      }
    }
  }
}
</style>
