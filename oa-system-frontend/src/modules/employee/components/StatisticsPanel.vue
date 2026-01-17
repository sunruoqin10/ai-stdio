<template>
  <el-card shadow="never" class="statistics-panel" v-loading="loading">
    <template #header>
      <div class="panel-header">
        <span>员工统计</span>
        <el-button :icon="Refresh" circle @click="handleRefresh" />
      </div>
    </template>

    <!-- 统计卡片 -->
    <el-row :gutter="16" class="stats-cards">
      <el-col :xs="12" :sm="12" :md="12" :lg="6">
        <div class="stat-card stat-total">
          <div class="stat-icon">
            <el-icon :size="24"><User /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ statistics?.total || 0 }}</div>
            <div class="stat-label">总员工数</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="12" :md="12" :lg="6">
        <div class="stat-card stat-active">
          <div class="stat-icon">
            <el-icon :size="24"><SuccessFilled /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ statistics?.active || 0 }}</div>
            <div class="stat-label">在职人数</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="12" :md="12" :lg="6">
        <div class="stat-card stat-probation">
          <div class="stat-icon">
            <el-icon :size="24"><WarningFilled /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ statistics?.probation || 0 }}</div>
            <div class="stat-label">试用期人数</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="12" :md="12" :lg="6">
        <div class="stat-card stat-new">
          <div class="stat-icon">
            <el-icon :size="24"><TrendCharts /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ statistics?.newThisMonth || 0 }}</div>
            <div class="stat-label">本月新入职</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-divider />

    <!-- 图表区域 -->
    <el-row :gutter="16">
      <!-- 部门分布饼图 -->
      <el-col :xs="24" :sm="24" :md="12">
        <div class="chart-container">
          <div class="chart-title">部门分布</div>
          <div ref="departmentChartRef" class="chart" style="height: 250px"></div>
        </div>
      </el-col>

      <!-- 员工状态分布 -->
      <el-col :xs="24" :sm="24" :md="12">
        <div class="chart-container">
          <div class="chart-title">状态分布</div>
          <div ref="statusChartRef" class="chart" style="height: 250px"></div>
        </div>
      </el-col>
    </el-row>

    <!-- 试用期提醒 -->
    <template v-if="probationEmployees.length > 0">
      <el-divider />
      <div class="probation-alert">
        <div class="alert-header">
          <el-icon class="alert-icon"><Warning /></el-icon>
          <span>试用期提醒</span>
        </div>
        <el-table :data="probationEmployees" size="small" max-height="200">
          <el-table-column prop="name" label="姓名" width="100" />
          <el-table-column prop="departmentName" label="部门" />
          <el-table-column prop="position" label="职位" />
          <el-table-column prop="probationEndDate" label="试用期结束日期" width="130" />
          <el-table-column label="剩余天数" width="100">
            <template #default="{ row }">
              <el-tag :type="getProbationTagType(row)" size="small">
                {{ getProbationRemainingDays(row) }} 天
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </template>
  </el-card>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import {
  User,
  Refresh,
  SuccessFilled,
  WarningFilled,
  TrendCharts,
  Warning,
} from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import type { ECharts } from 'echarts'
import { useEmployeeStore } from '../store'
import { getProbationRemainingDays } from '../utils'
import type { Employee } from '../types'

const store = useEmployeeStore()

const loading = ref(false)
const departmentChartRef = ref<HTMLElement>()
const statusChartRef = ref<HTMLElement>()

let departmentChart: ECharts | null = null
let statusChart: ECharts | null = null

// 统计数据
const statistics = computed(() => store.statistics)

// 试用期员工列表（动态计算：试用期结束日期在今天或之后）
const probationEmployees = computed(() => {
  const today = new Date()
  today.setHours(0, 0, 0, 0)

  return store.employeeList.filter(emp => {
    if (!emp.probationEndDate || emp.status !== 'active') return false
    const probationEndDate = new Date(emp.probationEndDate)
    probationEndDate.setHours(0, 0, 0, 0)
    return probationEndDate >= today
  })
})

// 获取试用期剩余天数
function getProbationRemainingDays(employee: Employee) {
  if (!employee.probationEndDate) return 0
  return getProbationRemainingDays(employee.probationEndDate)
}

// 获取试用期标签类型
function getProbationTagType(employee: Employee) {
  const days = getProbationRemainingDays(employee)
  if (days <= 7) return 'danger'
  if (days <= 15) return 'warning'
  return 'success'
}

// 初始化部门分布图表
function initDepartmentChart() {
  if (!departmentChartRef.value) return

  departmentChart = echarts.init(departmentChartRef.value)

  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)',
    },
    legend: {
      bottom: '0',
      left: 'center',
    },
    series: [
      {
        name: '部门分布',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2,
        },
        label: {
          show: false,
          position: 'center',
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 16,
            fontWeight: 'bold',
          },
        },
        labelLine: {
          show: false,
        },
        data: statistics.value?.byDepartment.map(dept => ({
          value: dept.count,
          name: dept.departmentName,
        })) || [],
      },
    ],
  }

  departmentChart.setOption(option)
}

// 初始化状态分布图表
function initStatusChart() {
  if (!statusChartRef.value) return

  statusChart = echarts.init(statusChartRef.value)

  const data = [
    { value: statistics.value?.active || 0, name: '在职' },
    { value: statistics.value?.resigned || 0, name: '离职' },
    { value: statistics.value?.probation || 0, name: '试用期' },
  ]

  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)',
    },
    legend: {
      bottom: '0',
      left: 'center',
    },
    series: [
      {
        name: '员工状态',
        type: 'pie',
        radius: '70%',
        data: data,
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)',
          },
        },
      },
    ],
  }

  statusChart.setOption(option)
}

// 刷新统计数据
async function handleRefresh() {
  loading.value = true
  try {
    await store.fetchStatistics()
    await store.fetchEmployeeList()
    ElMessage.success('统计数据已更新')
  } catch {
    ElMessage.error('统计数据更新失败')
  } finally {
    loading.value = false
  }
}

// 窗口大小改变时重绘图表
function handleResize() {
  departmentChart?.resize()
  statusChart?.resize()
}

onMounted(async () => {
  // 获取统计数据
  loading.value = true
  try {
    await store.fetchStatistics()
    await store.fetchEmployeeList()
  } finally {
    loading.value = false
  }

  // 初始化图表
  await nextTick()
  initDepartmentChart()
  initStatusChart()

  // 监听窗口大小改变
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  departmentChart?.dispose()
  statusChart?.dispose()
  window.removeEventListener('resize', handleResize)
})

// 监听统计数据变化，更新图表
watch(statistics, () => {
  if (departmentChart) {
    departmentChart.setOption({
      series: [
        {
          data: statistics.value?.byDepartment.map(dept => ({
            value: dept.count,
            name: dept.departmentName,
          })) || [],
        },
      ],
    })
  }

  if (statusChart) {
    const data = [
      { value: statistics.value?.active || 0, name: '在职' },
      { value: statistics.value?.resigned || 0, name: '离职' },
      { value: statistics.value?.probation || 0, name: '试用期' },
    ]
    statusChart.setOption({
      series: [{ data }],
    })
  }
}, { deep: true })
</script>

<style lang="scss" scoped>
@use '@/assets/styles/variables.scss' as *;
@use '@/assets/styles/mixins.scss' as *;

.statistics-panel {
  .panel-header {
    @include flex-between;
  }

  .stats-cards {
    .stat-card {
      @include flex-center;
      justify-content: flex-start;
      gap: 12px;
      padding: 16px;
      border-radius: 6px;
      background: linear-gradient(135deg, #f5f7fa 0%, #ffffff 100%);
      border: 1px solid #e8e8e8;
      transition: all 0.3s cubic-bezier(0.645, 0.045, 0.355, 1);

      &:hover {
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
        transform: translateY(-2px);
      }

      .stat-icon {
        @include flex-center;
        width: 48px;
        height: 48px;
        border-radius: 50%;
        background-color: rgba(24, 144, 255, 0.1);
        color: #1890ff;
      }

      .stat-content {
        flex: 1;

        .stat-value {
          font-size: 24px;
          font-weight: 600;
          color: #262626;
          line-height: 1;
          margin-bottom: 4px;
        }

        .stat-label {
          font-size: 12px;
          color: #8c8c8c;
        }
      }

      &.stat-total .stat-icon {
        background-color: rgba(24, 144, 255, 0.1);
        color: #1890ff;
      }

      &.stat-active .stat-icon {
        background-color: rgba(82, 196, 26, 0.1);
        color: #52c41a;
      }

      &.stat-probation .stat-icon {
        background-color: rgba(250, 173, 20, 0.1);
        color: #faad14;
      }

      &.stat-new .stat-icon {
        background-color: rgba(19, 194, 194, 0.1);
        color: #13c2c2;
      }
    }
  }

  .chart-container {
    .chart-title {
      font-size: 14px;
      font-weight: 500;
      color: #262626;
      margin-bottom: 12px;
      text-align: center;
    }

    .chart {
      width: 100%;
    }
  }

  .probation-alert {
    .alert-header {
      @include flex-center;
      gap: 8px;
      margin-bottom: 12px;
      font-size: 14px;
      font-weight: 500;
      color: #faad14;

      .alert-icon {
        font-size: 16px;
      }
    }
  }
}
</style>
