<template>
  <div class="dict-management-page">
    <PageHeader title="数据字典管理">
      <template #extra>
        <el-button type="primary" @click="handleAddDictType">
          <el-icon><Plus /></el-icon>
          新增字典类型
        </el-button>
        <el-button @click="handleExpandAll">
          全部展开
        </el-button>
        <el-button @click="handleCollapseAll">
          全部折叠
        </el-button>
      </template>
    </PageHeader>

    <div class="content-container">
      <!-- 左侧字典树 -->
      <div class="dict-tree-panel">
        <el-card shadow="never">
          <el-tree
            ref="treeRef"
            :data="treeData"
            :props="treeProps"
            node-key="id"
            highlight-current
            default-expand-all
            :empty-text="dictStore.loading ? '加载中...' : '暂无数据'"
            @node-click="handleNodeClick"
          >
            <template #default="{ node, data }">
              <span class="custom-tree-node">
                <el-icon v-if="data.category === 'system' && data.id && data.id.includes('dict_type_')" class="system-icon">
                  <Lock />
                </el-icon>
                <span class="node-label">{{ node.label }}</span>
                <el-tag v-if="data.itemCount !== undefined" size="small" type="info">
                  {{ data.itemCount }}
                </el-tag>
              </span>
            </template>
          </el-tree>
        </el-card>
      </div>

      <!-- 右侧内容区 -->
      <div class="main-content">
        <!-- 筛选面板 -->
        <el-card shadow="never" class="filter-card">
          <el-form :inline="true" :model="filterForm" class="filter-form">
            <el-form-item label="关键词">
              <el-input
                v-model="filterForm.keyword"
                placeholder="搜索编码/名称"
                clearable
                style="width: 200px"
                @input="handleSearch"
              >
                <template #prefix>
                  <el-icon><Search /></el-icon>
                </template>
              </el-input>
            </el-form-item>

            <el-form-item label="类别">
              <el-select
                v-model="filterForm.category"
                placeholder="全部类别"
                clearable
                style="width: 150px"
                @change="handleFilter"
              >
                <el-option label="系统字典" value="system" />
                <el-option label="业务字典" value="business" />
              </el-select>
            </el-form-item>

            <el-form-item label="状态">
              <el-select
                v-model="filterForm.status"
                placeholder="全部状态"
                clearable
                style="width: 120px"
                @change="handleFilter"
              >
                <el-option label="启用" value="enabled" />
                <el-option label="禁用" value="disabled" />
              </el-select>
            </el-form-item>

            <el-form-item>
              <el-button type="primary" @click="handleFilter">
                查询
              </el-button>
              <el-button @click="handleReset">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <!-- 字典类型表格 -->
        <el-card shadow="never" class="table-card">
          <DictTypeTable
            :data="dictStore.dictTypes"
            :loading="dictStore.loading"
            :total="dictStore.total"
            :current-page="dictStore.currentPage"
            :page-size="dictStore.pageSize"
            @edit="handleEditDictType"
            @view-items="handleViewItems"
            @page-change="handlePageChange"
            @size-change="handleSizeChange"
          />
        </el-card>

        <!-- 统计面板 -->
        <el-card shadow="never" class="stats-card">
          <template #header>
            <span>统计信息</span>
          </template>
          <div class="stats-list">
            <div class="stat-item">
              <span class="stat-label">字典类型总数:</span>
              <span class="stat-value">{{ dictStore.dictTypes.length }}</span>
            </div>
            <div class="stat-item">
              <span class="stat-label">系统字典:</span>
              <span class="stat-value system">{{ dictStore.systemDictTypes.length }}</span>
            </div>
            <div class="stat-item">
              <span class="stat-label">业务字典:</span>
              <span class="stat-value business">{{ dictStore.businessDictTypes.length }}</span>
            </div>
          </div>
        </el-card>
      </div>
    </div>

    <!-- 字典类型表单对话框 -->
    <DictTypeForm
      v-model="dictTypeFormVisible"
      :dict-type="currentDictType"
      @success="handleFormSuccess"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { Plus, Search, Lock } from '@element-plus/icons-vue'
import { useDebounceFn } from '@vueuse/core'
import PageHeader from '@/components/common/PageHeader.vue'
import DictTypeTable from '../components/DictTypeTable.vue'
import DictTypeForm from '../components/DictTypeForm.vue'
import type { DictType, DictFilter } from '../types'
import { useDictStore } from '../store'

const router = useRouter()
const dictStore = useDictStore()

const treeRef = ref()
const dictTypeFormVisible = ref(false)
const currentDictType = ref<DictType | null>(null)

// 树形配置
const treeProps = {
  children: 'children',
  label: 'name'
}

// 构建树形数据
const treeData = computed(() => {
  // 安全检查:确保dictTree是数组
  if (!Array.isArray(dictStore.dictTree) || dictStore.dictTree.length === 0) {
    return []
  }

  const systemDicts = dictStore.dictTree.filter(d => d.category === 'system')
  const businessDicts = dictStore.dictTree.filter(d => d.category === 'business')

  const result: any[] = []

  if (systemDicts.length > 0) {
    result.push({
      id: 'system-root',
      code: 'system',
      name: '系统字典',
      category: 'system',
      itemCount: systemDicts.reduce((sum, d) => sum + (d.itemCount || 0), 0),
      children: systemDicts.map(d => ({
        id: d.id,
        code: d.code,
        name: d.name,
        category: d.category,
        itemCount: d.itemCount
      }))
    })
  }

  if (businessDicts.length > 0) {
    result.push({
      id: 'business-root',
      code: 'business',
      name: '业务字典',
      category: 'business',
      itemCount: businessDicts.reduce((sum, d) => sum + (d.itemCount || 0), 0),
      children: businessDicts.map(d => ({
        id: d.id,
        code: d.code,
        name: d.name,
        category: d.category,
        itemCount: d.itemCount
      }))
    })
  }

  console.log('树形数据:', result)
  return result
})

// 筛选表单
const filterForm = reactive<DictFilter>({
  keyword: '',
  category: undefined,
  status: undefined
})

// 初始化
onMounted(async () => {
  await Promise.all([
    fetchDictTypes(),
    fetchDictTree()
  ])
  console.log('dictStore.dictTree:', dictStore.dictTree)
  console.log('systemDicts:', dictStore.dictTree.filter(d => d.category === 'system'))
  console.log('businessDicts:', dictStore.dictTree.filter(d => d.category === 'business'))
  console.log('最终treeData:', treeData.value)
})

// 获取字典类型列表
async function fetchDictTypes() {
  await dictStore.fetchDictTypes({
    ...filterForm,
    page: dictStore.currentPage,
    pageSize: dictStore.pageSize
  })
}

// 获取字典树
async function fetchDictTree() {
  await dictStore.fetchDictTree()
}

// 防抖搜索
const handleSearch = useDebounceFn(() => {
  handleFilter()
}, 300)

// 筛选
function handleFilter() {
  dictStore.setPage(1)
  fetchDictTypes()
}

// 重置
function handleReset() {
  filterForm.keyword = ''
  filterForm.category = undefined
  filterForm.status = undefined
  dictStore.setPage(1)
  fetchDictTypes()
}

// 页码改变
function handlePageChange(page: number) {
  dictStore.setPage(page)
  fetchDictTypes()
}

// 每页数量改变
function handleSizeChange(size: number) {
  dictStore.setPageSize(size)
  fetchDictTypes()
}

// 节点点击
function handleNodeClick(data: any) {
  // 只点击叶子节点(字典类型)
  if (!data.children || data.children.length === 0 || data.id.includes('dict_type_')) {
    filterForm.keyword = data.code
    handleFilter()
  }
}

// 展开所有
function handleExpandAll() {
  const allKeys = treeRef.value?.store.getAllNodeKeys()
  allKeys?.forEach((key: any) => {
    const node = treeRef.value?.store.nodesMap[key]
    node?.expand()
  })
}

// 折叠所有
function handleCollapseAll() {
  const allKeys = treeRef.value?.store.getAllNodeKeys()
  allKeys?.forEach((key: any) => {
    const node = treeRef.value?.store.nodesMap[key]
    node?.collapse()
  })
}

// 新增字典类型
function handleAddDictType() {
  currentDictType.value = null
  dictTypeFormVisible.value = true
}

// 编辑字典类型
function handleEditDictType(dictType: DictType) {
  currentDictType.value = dictType
  dictTypeFormVisible.value = true
}

// 查看字典项
function handleViewItems(dictType: DictType) {
  router.push({
    name: 'DictItemManagement',
    params: { dictTypeCode: dictType.code },
    state: { dictType }
  })
}

// 表单提交成功
function handleFormSuccess() {
  dictTypeFormVisible.value = false
  currentDictType.value = null
  Promise.all([
    fetchDictTypes(),
    fetchDictTree()
  ])
}
</script>

<style scoped lang="scss">
.dict-management-page {
  .content-container {
    display: grid;
    grid-template-columns: 250px 1fr;
    gap: 20px;

    .dict-tree-panel {
      :deep(.el-card) {
        height: calc(100vh - 200px);
        overflow-y: auto;
      }

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

    .main-content {
      display: flex;
      flex-direction: column;
      gap: 20px;

      .filter-card {
        .filter-form {
          margin-bottom: -10px;
        }
      }

      .table-card {
        flex: 1;
      }

      .stats-card {
        .stats-list {
          display: flex;
          flex-direction: column;
          gap: 12px;

          .stat-item {
            display: flex;
            align-items: center;
            gap: 8px;

            .stat-label {
              color: var(--el-text-color-secondary);
              font-size: 14px;
            }

            .stat-value {
              font-size: 18px;
              font-weight: 600;
              color: var(--el-color-primary);

              &.system {
                color: var(--el-color-warning);
              }

              &.business {
                color: var(--el-color-success);
              }
            }
          }
        }
      }
    }
  }
}

// 响应式布局
@media (max-width: 1200px) {
  .dict-management-page {
    .content-container {
      grid-template-columns: 250px 1fr;

      .stats-card {
        display: none;
      }
    }
  }
}

@media (max-width: 768px) {
  .dict-management-page {
    .content-container {
      grid-template-columns: 1fr;

      .dict-tree-panel {
        display: none;
      }
    }
  }
}
</style>
