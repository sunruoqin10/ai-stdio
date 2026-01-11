<template>
  <div class="expense-statistics">
    <!-- 筛选条件 -->
    <div class="filter-section">
      <el-form :inline="true" :model="filterForm" class="filter-form">
        <el-form-item label="日期范围">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            @change="handleDateChange"
          />
        </el-form-item>
        <el-form-item label="报销类型">
          <el-select v-model="filterForm.type" placeholder="全部类型" clearable @change="handleFilter">
            <el-option label="差旅费" value="travel" />
            <el-option label="招待费" value="hospitality" />
            <el-option label="办公用品" value="office" />
            <el-option label="交通费" value="transport" />
            <el-option label="其他费用" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item label="部门">
          <el-select v-model="filterForm.departmentId" placeholder="全部部门" clearable @change="handleFilter">
            <el-option label="技术部" value="DEPT001" />
            <el-option label="市场部" value="DEPT002" />
            <el-option label="人事部" value="DEPT003" />
            <el-option label="财务部" value="DEPT004" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleFilter">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button type="success" @click="handleExport" :loading="exporting">
            <el-icon><Download /></el-icon>
            导出报表
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 统计概览 -->
    <div class="overview-cards">
      <el-row :gutter="16">
        <el-col :xs="24" :sm="12" :md="6">
          <div class="overview-card total">
            <div class="card-icon">
              <el-icon :size="32"><Money /></el-icon>
            </div>
            <div class="card-content">
              <div class="card-value">{{ formatAmount(overviewData.totalAmount) }}</div>
              <div class="card-label">总金额</div>
            </div>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <div class="overview-card count">
            <div class="card-icon">
              <el-icon :size="32"><Document /></el-icon>
            </div>
            <div class="card-content">
              <div class="card-value">{{ overviewData.totalCount }}</div>
              <div class="card-label">报销单数</div>
            </div>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <div class="overview-card avg">
            <div class="card-icon">
              <el-icon :size="32"><TrendCharts /></el-icon>
            </div>
            <div class="card-content">
              <div class="card-value">{{ formatAmount(overviewData.avgAmount) }}</div>
              <div class="card-label">平均金额</div>
            </div>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <div class="overview-card paid">
            <div class="card-icon">
              <el-icon :size="32"><SuccessFilled /></el-icon>
            </div>
            <div class="card-content">
              <div class="card-value">{{ formatAmount(overviewData.paidAmount) }}</div>
              <div class="card-label">已打款金额</div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 图表区域 -->
    <div class="charts-section">
      <el-row :gutter="16">
        <el-col :xs="24" :md="12">
          <el-card class="chart-card">
            <template #header>
              <div class="card-header">
                <span class="card-title">类型分布</span>
              </div>
            </template>
            <div class="chart-container">
              <div class="type-stats">
                <div
                  v-for="item in typeStats"
                  :key="item.type"
                  class="type-stat-item"
                >
                  <div class="stat-header">
                    <span class="stat-label">{{ getExpenseTypeName(item.type) }}</span>
                    <span class="stat-percentage">{{ item.percentage.toFixed(1) }}%</span>
                  </div>
                  <el-progress
                    :percentage="item.percentage"
                    :color="getTypeColor(item.type)"
                    :show-text="false"
                  />
                  <div class="stat-detail">
                    <span class="stat-count">{{ item.count }}单</span>
                    <span class="stat-amount">{{ formatAmount(item.totalAmount) }}</span>
                  </div>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>

        <el-col :xs="24" :md="12">
          <el-card class="chart-card">
            <template #header>
              <div class="card-header">
                <span class="card-title">部门统计</span>
              </div>
            </template>
            <div class="chart-container">
              <el-table
                :data="departmentStats"
                size="small"
                :show-header="true"
                max-height="300"
              >
                <el-table-column prop="departmentName" label="部门" width="100" />
                <el-table-column prop="count" label="单数" width="60" align="center" />
                <el-table-column prop="totalAmount" label="总金额" align="right">
                  <template #default="{ row }">
                    {{ formatAmount(row.totalAmount) }}
                  </template>
                </el-table-column>
                <el-table-column prop="avgAmount" label="平均金额" align="right">
                  <template #default="{ row }">
                    {{ formatAmount(row.avgAmount) }}
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 月度趋势 -->
    <el-card class="trend-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">月度趋势</span>
        </div>
      </template>
      <div class="trend-chart">
        <el-table
          :data="monthlyStats"
          size="default"
          :show-header="true"
        >
          <el-table-column prop="month" label="月份" width="120" />
          <el-table-column prop="count" label="单数" width="100" align="center" />
          <el-table-column prop="totalAmount" label="总金额" align="right">
            <template #default="{ row }">
              <span class="amount-text">{{ formatAmount(row.totalAmount) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="平均金额" align="right">
            <template #default="{ row }">
              {{ formatAmount(row.totalAmount / row.count) }}
            </template>
          </el-table-column>
          <el-table-column label="占比" width="120">
            <template #default="{ row }">
              <el-tag size="small" type="primary">
                {{ ((row.totalAmount / overviewData.totalAmount) * 100).toFixed(1) }}%
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>

    <!-- 员工排行 -->
    <el-card class="ranking-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">员工报销排行</span>
        </div>
      </template>
      <div class="ranking-list">
        <el-table
          :data="employeeStats"
          size="default"
          :show-header="true"
        >
          <el-table-column label="排名" width="80" align="center">
            <template #default="{ $index }">
              <el-tag v-if="$index < 3" :type="$index === 0 ? 'danger' : $index === 1 ? 'warning' : 'success'">
                {{ $index + 1 }}
              </el-tag>
              <span v-else>{{ $index + 1 }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="employeeName" label="员工姓名" width="150" />
          <el-table-column prop="count" label="单数" width="100" align="center" />
          <el-table-column prop="totalAmount" label="总金额" align="right">
            <template #default="{ row }">
              <span class="amount-text">{{ formatAmount(row.totalAmount) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="平均金额" align="right">
            <template #default="{ row }">
              {{ formatAmount(row.totalAmount / row.count) }}
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Download, Money, Document, TrendCharts, SuccessFilled } from '@element-plus/icons-vue'
import { formatAmount, getExpenseTypeName } from '../utils'
import type { StatsQueryParams, DepartmentStats, TypeStats, MonthlyStats, EmployeeStats } from '../types'

// 筛选表单
const filterForm = ref<StatsQueryParams>({
  startDate: '',
  endDate: '',
  type: undefined,
  departmentId: undefined
})

const dateRange = ref<string[]>([])
const exporting = ref(false)

// 概览数据
const overviewData = ref({
  totalAmount: 0,
  totalCount: 0,
  avgAmount: 0,
  paidAmount: 0
})

// 类型统计
const typeStats = ref<TypeStats[]>([])

// 部门统计
const departmentStats = ref<DepartmentStats[]>([])

// 月度统计
const monthlyStats = ref<MonthlyStats[]>([])

// 员工统计
const employeeStats = ref<EmployeeStats[]>([])

onMounted(() => {
  // 设置默认日期范围（最近6个月）
  const endDate = new Date()
  const startDate = new Date()
  startDate.setMonth(startDate.getMonth() - 6)

  const formatDate = (date: Date) => {
    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    return `${year}-${month}-${day}`
  }

  dateRange.value = [formatDate(startDate), formatDate(endDate)]
  filterForm.value.startDate = dateRange.value[0]
  filterForm.value.endDate = dateRange.value[1]

  loadData()
})

async function loadData() {
  try {
    // TODO: 调用实际的API接口
    // 这里使用模拟数据
    loadMockData()
  } catch (error: any) {
    ElMessage.error(error.message || '加载数据失败')
  }
}

function loadMockData() {
  // 模拟概览数据
  overviewData.value = {
    totalAmount: 156800.00,
    totalCount: 87,
    avgAmount: 1802.30,
    paidAmount: 128500.00
  }

  // 模拟类型统计
  typeStats.value = [
    { type: 'travel', totalAmount: 65800, count: 28, percentage: 41.96 },
    { type: 'hospitality', totalAmount: 42500, count: 18, percentage: 27.10 },
    { type: 'transport', totalAmount: 28200, count: 25, percentage: 17.98 },
    { type: 'office', totalAmount: 15800, count: 12, percentage: 10.08 },
    { type: 'other', totalAmount: 4500, count: 4, percentage: 2.88 }
  ]

  // 模拟部门统计
  departmentStats.value = [
    { departmentId: 'DEPT001', departmentName: '技术部', totalAmount: 58600, count: 32, avgAmount: 1831.25 },
    { departmentId: 'DEPT002', departmentName: '市场部', totalAmount: 48200, count: 26, avgAmount: 1853.85 },
    { departmentId: 'DEPT003', departmentName: '人事部', totalAmount: 25600, count: 15, avgAmount: 1706.67 },
    { departmentId: 'DEPT004', departmentName: '财务部', totalAmount: 24400, count: 14, avgAmount: 1742.86 }
  ]

  // 模拟月度统计
  monthlyStats.value = [
    { month: '2024-07', totalAmount: 24500, count: 12 },
    { month: '2024-08', totalAmount: 28600, count: 14 },
    { month: '2024-09', totalAmount: 22800, count: 11 },
    { month: '2024-10', totalAmount: 26500, count: 13 },
    { month: '2024-11', totalAmount: 31200, count: 18 },
    { month: '2024-12', totalAmount: 23200, count: 19 }
  ]

  // 模拟员工统计
  employeeStats.value = [
    { employeeId: 'EMP001', employeeName: '张三', totalAmount: 18600, count: 8 },
    { employeeId: 'EMP002', employeeName: '李四', totalAmount: 15800, count: 7 },
    { employeeId: 'EMP003', employeeName: '王五', totalAmount: 14200, count: 6 },
    { employeeId: 'EMP004', employeeName: '赵六', totalAmount: 12800, count: 9 },
    { employeeId: 'EMP005', employeeName: '钱七', totalAmount: 11500, count: 5 }
  ]
}

function handleDateChange(value: string[]) {
  if (value && value.length === 2) {
    filterForm.value.startDate = value[0]
    filterForm.value.endDate = value[1]
  }
}

function handleFilter() {
  loadData()
}

function handleReset() {
  filterForm.value = {
    startDate: dateRange.value[0],
    endDate: dateRange.value[1],
    type: undefined,
    departmentId: undefined
  }
  loadData()
}

async function handleExport() {
  try {
    exporting.value = true
    // TODO: 调用实际的导出接口
    await new Promise(resolve => setTimeout(resolve, 1000))
    ElMessage.success('报表导出成功')
  } catch (error: any) {
    ElMessage.error(error.message || '导出失败')
  } finally {
    exporting.value = false
  }
}

function getTypeColor(type: string): string {
  const colorMap: Record<string, string> = {
    travel: '#409EFF',
    hospitality: '#F56C6C',
    transport: '#E6A23C',
    office: '#67C23A',
    other: '#909399'
  }
  return colorMap[type] || '#909399'
}
</script>

<style scoped lang="scss">
.expense-statistics {
  padding: 0;
}

.filter-section {
  margin-bottom: 20px;
  padding: 20px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);

  .filter-form {
    margin: 0;

    :deep(.el-form-item) {
      margin-bottom: 0;
    }
  }
}

.overview-cards {
  margin-bottom: 20px;

  .overview-card {
    display: flex;
    align-items: center;
    padding: 20px;
    background: #fff;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
    margin-bottom: 16px;
    transition: all 0.3s;

    &:hover {
      transform: translateY(-4px);
      box-shadow: 0 8px 16px rgba(0, 0, 0, 0.15);
    }

    &.total .card-icon {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    }

    &.count .card-icon {
      background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
    }

    &.avg .card-icon {
      background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
    }

    &.paid .card-icon {
      background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
    }

    .card-icon {
      width: 60px;
      height: 60px;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: 12px;
      color: white;
      margin-right: 16px;
      flex-shrink: 0;
    }

    .card-content {
      flex: 1;

      .card-value {
        font-size: 24px;
        font-weight: bold;
        color: #303133;
        margin-bottom: 4px;
      }

      .card-label {
        font-size: 13px;
        color: #909399;
      }
    }
  }
}

.charts-section {
  margin-bottom: 20px;

  .chart-card {
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);

    :deep(.el-card__header) {
      padding: 16px 20px;
      border-bottom: 1px solid #EBEEF5;
    }

    :deep(.el-card__body) {
      padding: 20px;
    }

    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;

      .card-title {
        font-size: 16px;
        font-weight: 600;
        color: #303133;
      }
    }

    .chart-container {
      min-height: 300px;
    }
  }
}

.type-stats {
  .type-stat-item {
    margin-bottom: 20px;

    &:last-child {
      margin-bottom: 0;
    }

    .stat-header {
      display: flex;
      justify-content: space-between;
      margin-bottom: 8px;

      .stat-label {
        font-size: 14px;
        font-weight: 500;
        color: #606266;
      }

      .stat-percentage {
        font-size: 14px;
        font-weight: 600;
        color: #409EFF;
      }
    }

    .stat-detail {
      display: flex;
      justify-content: space-between;
      margin-top: 8px;
      font-size: 12px;
      color: #909399;
    }
  }
}

.trend-card,
.ranking-card {
  margin-bottom: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);

  :deep(.el-card__header) {
    padding: 16px 20px;
    border-bottom: 1px solid #EBEEF5;
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .card-title {
      font-size: 16px;
      font-weight: 600;
      color: #303133;
    }
  }
}

.amount-text {
  font-weight: 600;
  color: #F56C6C;
}

:deep(.el-progress-bar__outer) {
  background-color: #EBEEF5;
}
</style>
