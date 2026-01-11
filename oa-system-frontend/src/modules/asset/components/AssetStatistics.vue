<template>
  <div class="asset-statistics">
    <!-- 统计卡片 -->
    <el-row :gutter="16" class="stats-overview">
      <el-col :xs="12" :sm="8" :md="6" :lg="6" :xl="4">
        <div class="stat-card primary">
          <div class="stat-icon">
            <el-icon :size="40"><Box /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ statistics.total }}</div>
            <div class="stat-label">总资产数</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="8" :md="6" :lg="6" :xl="4">
        <div class="stat-card success">
          <div class="stat-icon">
            <el-icon :size="40"><SuccessFilled /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ statistics.byStatus.stock }}</div>
            <div class="stat-label">库存中</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="8" :md="6" :lg="6" :xl="4">
        <div class="stat-card info">
          <div class="stat-icon">
            <el-icon :size="40"><User /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ statistics.byStatus.in_use }}</div>
            <div class="stat-label">使用中</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="8" :md="6" :lg="6" :xl="4">
        <div class="stat-card warning">
          <div class="stat-icon">
            <el-icon :size="40"><Warning /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ statistics.borrowedCount }}</div>
            <div class="stat-label">借出中</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="8" :md="6" :lg="6" :xl="4">
        <div class="stat-card danger">
          <div class="stat-icon">
            <el-icon :size="40"><Tools /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ statistics.maintenanceCount }}</div>
            <div class="stat-label">维修中</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="8" :md="6" :lg="6" :xl="4">
        <div class="stat-card money">
          <div class="stat-icon">
            <el-icon :size="40"><Wallet /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ formatAmount(statistics.currentValue) }}</div>
            <div class="stat-label">当前价值</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="16" class="charts-section">
      <!-- 资产分类饼图 -->
      <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
        <el-card class="chart-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span class="card-title">资产分类统计</span>
              <el-button text @click="handleRefresh">
                <el-icon><Refresh /></el-icon>
              </el-button>
            </div>
          </template>
          <div ref="categoryChartRef" class="chart-container"></div>
        </el-card>
      </el-col>

      <!-- 资产状态饼图 -->
      <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
        <el-card class="chart-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span class="card-title">资产状态分布</span>
              <el-button text @click="handleRefresh">
                <el-icon><Refresh /></el-icon>
              </el-button>
            </div>
          </template>
          <div ref="statusChartRef" class="chart-container"></div>
        </el-card>
      </el-col>

      <!-- 折旧趋势图 -->
      <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
        <el-card class="chart-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span class="card-title">折旧趋势</span>
              <el-select v-model="depreciationMonths" size="small" style="width: 120px" @change="loadDepreciationTrend">
                <el-option label="最近6个月" :value="6" />
                <el-option label="最近12个月" :value="12" />
              </el-select>
            </div>
          </template>
          <div ref="depreciationChartRef" class="chart-container"></div>
        </el-card>
      </el-col>

      <!-- 借出趋势图 -->
      <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
        <el-card class="chart-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span class="card-title">借出趋势</span>
              <el-select v-model="borrowMonths" size="small" style="width: 120px" @change="loadBorrowTrend">
                <el-option label="最近6个月" :value="6" />
                <el-option label="最近12个月" :value="12" />
              </el-select>
            </div>
          </template>
          <div ref="borrowChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 详细数据表格 -->
    <el-row :gutter="16" class="table-section">
      <el-col :span="24">
        <el-card class="table-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span class="card-title">分类统计数据</span>
            </div>
          </template>
          <el-table :data="categoryData" border stripe>
            <el-table-column prop="category" label="类别" width="120">
              <template #default="{ row }">
                {{ getCategoryName(row.category) }}
              </template>
            </el-table-column>
            <el-table-column prop="count" label="数量" width="100" />
            <el-table-column prop="totalValue" label="总购置金额" width="150">
              <template #default="{ row }">
                {{ formatAmount(row.totalValue) }}
              </template>
            </el-table-column>
            <el-table-column prop="currentValue" label="当前价值" width="150">
              <template #default="{ row }">
                {{ formatAmount(row.currentValue) }}
              </template>
            </el-table-column>
            <el-table-column prop="depreciation" label="折旧金额" width="150">
              <template #default="{ row }">
                {{ formatAmount(row.depreciation) }}
              </template>
            </el-table-column>
            <el-table-column prop="depreciationRate" label="折旧率" width="100">
              <template #default="{ row }">
                {{ row.depreciationRate }}%
              </template>
            </el-table-column>
            <el-table-column label="占比" width="120">
              <template #default="{ row }">
                <el-progress
                  :percentage="row.percentage"
                  :color="getProgressColor(row.category)"
                />
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Box,
  SuccessFilled,
  User,
  Warning,
  Tools,
  Wallet,
  Refresh
} from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import type { ECharts } from 'echarts'
import { useAssetStore } from '../store'
import { formatAmount, getCategoryName } from '../utils'
import type { AssetStatistics, DepreciationTrend, BorrowTrend } from '../types'

const assetStore = useAssetStore()

const categoryChartRef = ref<HTMLElement>()
const statusChartRef = ref<HTMLElement>()
const depreciationChartRef = ref<HTMLElement>()
const borrowChartRef = ref<HTMLElement>()

let categoryChart: ECharts | null = null
let statusChart: ECharts | null = null
let depreciationChart: ECharts | null = null
let borrowChart: ECharts | null = null

const statistics = ref<AssetStatistics>({
  total: 0,
  byCategory: {
    electronic: 0,
    furniture: 0,
    book: 0,
    other: 0
  },
  byStatus: {
    stock: 0,
    in_use: 0,
    borrowed: 0,
    maintenance: 0,
    scrapped: 0
  },
  totalValue: 0,
  currentValue: 0,
  borrowedCount: 0,
  maintenanceCount: 0
})

const depreciationTrend = ref<DepreciationTrend>({ months: [], values: [] })
const borrowTrend = ref<BorrowTrend>({ months: [], counts: [] })

const depreciationMonths = ref(6)
const borrowMonths = ref(6)

// 分类统计数据
const categoryData = computed(() => {
  const data = [
    {
      category: 'electronic',
      count: statistics.value.byCategory.electronic,
      totalValue: 0,
      currentValue: 0,
      depreciation: 0,
      depreciationRate: 0,
      percentage: 0
    },
    {
      category: 'furniture',
      count: statistics.value.byCategory.furniture,
      totalValue: 0,
      currentValue: 0,
      depreciation: 0,
      depreciationRate: 0,
      percentage: 0
    },
    {
      category: 'book',
      count: statistics.value.byCategory.book,
      totalValue: 0,
      currentValue: 0,
      depreciation: 0,
      depreciationRate: 0,
      percentage: 0
    },
    {
      category: 'other',
      count: statistics.value.byCategory.other,
      totalValue: 0,
      currentValue: 0,
      depreciation: 0,
      depreciationRate: 0,
      percentage: 0
    }
  ]

  // 计算百分比
  const total = statistics.value.total
  data.forEach(item => {
    item.percentage = total > 0 ? Math.round((item.count / total) * 100) : 0
  })

  return data.filter(item => item.count > 0)
})

function getProgressColor(category: string): string {
  const colorMap: Record<string, string> = {
    electronic: '#409eff',
    furniture: '#67c23a',
    book: '#e6a23c',
    other: '#909399'
  }
  return colorMap[category] || '#409eff'
}

/**
 * 加载统计数据
 */
async function loadStatistics() {
  try {
    const data = await assetStore.loadStatistics()
    statistics.value = data
  } catch (error: any) {
    ElMessage.error(error.message || '加载统计数据失败')
  }
}

/**
 * 加载折旧趋势
 */
async function loadDepreciationTrend() {
  try {
    const data = await assetStore.loadDepreciationTrend(depreciationMonths.value)
    depreciationTrend.value = data
    renderDepreciationChart()
  } catch (error: any) {
    ElMessage.error(error.message || '加载折旧趋势失败')
  }
}

/**
 * 加载借出趋势
 */
async function loadBorrowTrend() {
  try {
    const data = await assetStore.loadBorrowTrend(borrowMonths.value)
    borrowTrend.value = data
    renderBorrowChart()
  } catch (error: any) {
    ElMessage.error(error.message || '加载借出趋势失败')
  }
}

/**
 * 渲染分类饼图
 */
function renderCategoryChart() {
  if (!categoryChartRef.value) return

  if (!categoryChart) {
    categoryChart = echarts.init(categoryChartRef.value)
  }

  const data = [
    { value: statistics.value.byCategory.electronic, name: '电子设备' },
    { value: statistics.value.byCategory.furniture, name: '办公家具' },
    { value: statistics.value.byCategory.book, name: '图书资料' },
    { value: statistics.value.byCategory.other, name: '其他' }
  ].filter(item => item.value > 0)

  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)'
    },
    legend: {
      bottom: '0',
      left: 'center'
    },
    series: [
      {
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: false,
          position: 'center'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 20,
            fontWeight: 'bold'
          }
        },
        labelLine: {
          show: false
        },
        data
      }
    ]
  }

  categoryChart.setOption(option)
}

/**
 * 渲染状态饼图
 */
function renderStatusChart() {
  if (!statusChartRef.value) return

  if (!statusChart) {
    statusChart = echarts.init(statusChartRef.value)
  }

  const data = [
    { value: statistics.value.byStatus.stock, name: '库存中' },
    { value: statistics.value.byStatus.in_use, name: '使用中' },
    { value: statistics.value.byStatus.borrowed, name: '已借出' },
    { value: statistics.value.byStatus.maintenance, name: '维修中' },
    { value: statistics.value.byStatus.scrapped, name: '已报废' }
  ].filter(item => item.value > 0)

  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)'
    },
    legend: {
      bottom: '0',
      left: 'center'
    },
    series: [
      {
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: false,
          position: 'center'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 20,
            fontWeight: 'bold'
          }
        },
        labelLine: {
          show: false
        },
        data
      }
    ]
  }

  statusChart.setOption(option)
}

/**
 * 渲染折旧趋势图
 */
function renderDepreciationChart() {
  if (!depreciationChartRef.value) return

  if (!depreciationChart) {
    depreciationChart = echarts.init(depreciationChartRef.value)
  }

  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      },
      formatter: (params: any) => {
        const param = params[0]
        return `${param.name}<br/>${param.seriesName}: ${formatAmount(param.value)}`
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: depreciationTrend.value.months,
      axisLabel: {
        rotate: 45
      }
    },
    yAxis: {
      type: 'value',
      axisLabel: {
        formatter: (value: number) => {
          return `¥${(value / 1000).toFixed(0)}k`
        }
      }
    },
    series: [
      {
        name: '当前价值',
        type: 'bar',
        data: depreciationTrend.value.values,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#83bff6' },
            { offset: 0.5, color: '#188df0' },
            { offset: 1, color: '#188df0' }
          ])
        },
        emphasis: {
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: '#2378f7' },
              { offset: 0.7, color: '#2378f7' },
              { offset: 1, color: '#83bff6' }
            ])
          }
        }
      }
    ]
  }

  depreciationChart.setOption(option)
}

/**
 * 渲染借出趋势图
 */
function renderBorrowChart() {
  if (!borrowChartRef.value) return

  if (!borrowChart) {
    borrowChart = echarts.init(borrowChartRef.value)
  }

  const option = {
    tooltip: {
      trigger: 'axis'
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: borrowTrend.value.months,
      boundaryGap: false
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '借出次数',
        type: 'line',
        smooth: true,
        data: borrowTrend.value.counts,
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(230, 162, 60, 0.5)' },
            { offset: 1, color: 'rgba(230, 162, 60, 0.1)' }
          ])
        },
        itemStyle: {
          color: '#e6a23c'
        },
        lineStyle: {
          width: 2
        }
      }
    ]
  }

  borrowChart.setOption(option)
}

/**
 * 刷新数据
 */
async function handleRefresh() {
  await loadStatistics()
  renderCategoryChart()
  renderStatusChart()
}

/**
 * 响应式处理
 */
function handleResize() {
  categoryChart?.resize()
  statusChart?.resize()
  depreciationChart?.resize()
  borrowChart?.resize()
}

onMounted(async () => {
  await loadStatistics()
  await loadDepreciationTrend()
  await loadBorrowTrend()

  await nextTick()
  renderCategoryChart()
  renderStatusChart()
  renderDepreciationChart()
  renderBorrowChart()

  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  categoryChart?.dispose()
  statusChart?.dispose()
  depreciationChart?.dispose()
  borrowChart?.dispose()

  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped lang="scss">
.asset-statistics {
  padding: 8px;
}

.stats-overview {
  margin-bottom: 16px;
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 20px;
  border-radius: 8px;
  background: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: all 0.3s;
  margin-bottom: 16px;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 16px rgba(0, 0, 0, 0.15);
  }

  &.primary .stat-icon {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  }

  &.success .stat-icon {
    background: linear-gradient(135deg, #84fab0 0%, #8fd3f4 100%);
  }

  &.info .stat-icon {
    background: linear-gradient(135deg, #a1c4fd 0%, #c2e9fb 100%);
  }

  &.warning .stat-icon {
    background: linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%);
  }

  &.danger .stat-icon {
    background: linear-gradient(135deg, #ff9a9e 0%, #fecfef 100%);
  }

  &.money .stat-icon {
    background: linear-gradient(135deg, #f5af19 0%, #f12711 100%);
  }

  .stat-icon {
    width: 70px;
    height: 70px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 12px;
    color: white;
    margin-right: 16px;
    flex-shrink: 0;
  }

  .stat-content {
    flex: 1;
    min-width: 0;

    .stat-value {
      font-size: 24px;
      font-weight: bold;
      color: #303133;
      margin-bottom: 4px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .stat-label {
      font-size: 13px;
      color: #909399;
    }
  }
}

.charts-section,
.table-section {
  margin-bottom: 16px;
}

.chart-card,
.table-card {
  height: 100%;

  .card-header {
    display: flex;
    align-items: center;
    justify-content: space-between;

    .card-title {
      font-size: 16px;
      font-weight: 600;
      color: #303133;
    }
  }
}

.chart-container {
  width: 100%;
  height: 300px;
}
</style>
