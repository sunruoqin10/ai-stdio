<template>
  <div class="dict-item-management-page">
    <PageHeader :title="pageTitle">
      <template #extra>
        <el-button @click="handleBack">
          <el-icon><ArrowLeft /></el-icon>
          返回
        </el-button>
        <el-button type="primary" @click="handleAddDictItem">
          <el-icon><Plus /></el-icon>
          新增字典项
        </el-button>
      </template>
    </PageHeader>

    <div class="content-container">
      <!-- 字典类型信息 -->
      <el-card shadow="never" class="dict-type-info">
        <el-descriptions :column="4" border>
          <el-descriptions-item label="字典类型">
            {{ currentDictType?.name }}
          </el-descriptions-item>
          <el-descriptions-item label="字典编码">
            <el-tag size="small" type="info">{{ currentDictType?.code }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="字典类别">
            <el-tag v-if="currentDictType?.category === 'system'" type="warning" size="small">
              <el-icon><Lock /></el-icon>
              系统字典
            </el-tag>
            <el-tag v-else type="primary" size="small">业务字典</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="字典项数量">
            <el-tag type="success" size="small">
              {{ dictItems.length }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>
      </el-card>

      <!-- 筛选面板 -->
      <el-card shadow="never" class="filter-card">
        <el-form :inline="true" :model="filterForm" class="filter-form">
          <el-form-item label="关键词">
            <el-input
              v-model="filterForm.keyword"
              placeholder="搜索标签/值"
              clearable
              style="width: 200px"
              @input="handleSearch"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
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

      <!-- 字典项列表 -->
      <el-card shadow="never" class="list-card">
        <template #header>
          <div class="card-header">
            <span>字典项列表</span>
            <span class="tip-text">拖拽行可调整顺序</span>
          </div>
        </template>
        <DictItemList
          :items="dictItems"
          :loading="dictStore.loading"
          @edit="handleEditDictItem"
          @refresh="fetchDictItems"
        />
      </el-card>
    </div>

    <!-- 字典项表单对话框 -->
    <DictItemForm
      v-model="dictItemFormVisible"
      :dict-item="currentDictItem"
      :dict-type="currentDictType"
      @success="handleFormSuccess"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Plus, Search, Lock } from '@element-plus/icons-vue'
import { useDebounceFn } from '@vueuse/core'
import PageHeader from '@/components/common/PageHeader.vue'
import DictItemList from '../components/DictItemList.vue'
import DictItemForm from '../components/DictItemForm.vue'
import type { DictItem, DictType, DictFilter } from '../types'
import { useDictStore } from '../store'

const router = useRouter()
const route = useRoute()
const dictStore = useDictStore()

const dictItemFormVisible = ref(false)
const currentDictItem = ref<DictItem | null>(null)
const currentDictType = ref<DictType | null>(null)
const dictItems = ref<DictItem[]>([])

// 筛选表单
const filterForm = reactive<DictFilter>({
  keyword: '',
  status: undefined
})

// 页面标题
const pageTitle = computed(() => {
  return currentDictType.value
    ? `${currentDictType.value.name} - 字典项管理`
    : '字典项管理'
})

// 初始化
onMounted(async () => {
  // 从路由状态或params获取字典类型
  const dictTypeCode = route.params.dictTypeCode as string

  console.log('dictTypeCode:', dictTypeCode)
  console.log('dictStore.dictTypes:', dictStore.dictTypes)
  console.log('dictStore.dictTree:', dictStore.dictTree)

  if (dictTypeCode) {
    // 先确保dictTypes已加载
    if (dictStore.dictTypes.length === 0) {
      await dictStore.fetchDictTypes({ page: 1, pageSize: 100 })
    }

    // 通过编码查找字典类型
    const dictType = dictStore.dictTypes.find(dt => dt.code === dictTypeCode)
    console.log('找到的dictType:', dictType)

    if (dictType) {
      currentDictType.value = dictType
      dictStore.setCurrentDictType(dictType)
      await fetchDictItems()
    } else {
      // 如果在dictTypes中找不到,尝试从dictTree查找
      const dictTypeFromTree = dictStore.dictTree.find(dt => dt.code === dictTypeCode)
      if (dictTypeFromTree) {
        currentDictType.value = dictTypeFromTree as any
        dictStore.setCurrentDictType(dictTypeFromTree as any)
        await fetchDictItems()
      } else {
        console.error('找不到字典类型:', dictTypeCode)
        ElMessage.error('找不到指定的字典类型')
        handleBack()
      }
    }
  } else if (history.state && history.state.dictType) {
    // 从路由状态获取
    currentDictType.value = history.state.dictType
    dictStore.setCurrentDictType(history.state.dictType)
    await fetchDictItems()
  } else {
    // 如果没有字典类型信息,返回列表页
    handleBack()
  }
})

// 获取字典项列表
async function fetchDictItems() {
  if (!currentDictType.value) {
    console.log('currentDictType为空,无法获取字典项')
    return
  }

  console.log('开始获取字典项, dictTypeCode:', currentDictType.value.code)

  const result = await dictStore.fetchDictItems({
    dictTypeCode: currentDictType.value.code,
    ...filterForm,
    page: 1,
    pageSize: 1000 // 字典项通常不会太多,一次加载全部
  })

  console.log('获取到的字典项:', result.list)
  dictItems.value = result.list.sort((a, b) => a.sortOrder - b.sortOrder)
  console.log('排序后的dictItems:', dictItems.value)
}

// 防抖搜索
const handleSearch = useDebounceFn(() => {
  handleFilter()
}, 300)

// 筛选
function handleFilter() {
  fetchDictItems()
}

// 重置
function handleReset() {
  filterForm.keyword = ''
  filterForm.status = undefined
  fetchDictItems()
}

// 返回
function handleBack() {
  router.back()
}

// 新增字典项
function handleAddDictItem() {
  currentDictItem.value = null
  dictItemFormVisible.value = true
}

// 编辑字典项
function handleEditDictItem(item: DictItem) {
  currentDictItem.value = item
  dictItemFormVisible.value = true
}

// 表单提交成功
function handleFormSuccess() {
  dictItemFormVisible.value = false
  currentDictItem.value = null
  fetchDictItems()
}
</script>

<style scoped lang="scss">
.dict-item-management-page {
  .content-container {
    display: flex;
    flex-direction: column;
    gap: 20px;

    .dict-type-info {
      :deep(.el-descriptions) {
        .el-descriptions__label {
          width: 120px;
          font-weight: 500;
        }
      }
    }

    .filter-card {
      .filter-form {
        margin-bottom: -10px;
      }
    }

    .list-card {
      .card-header {
        display: flex;
        justify-content: space-between;
        align-items: center;

        .tip-text {
          font-size: 12px;
          color: var(--el-text-color-secondary);
        }
      }
    }
  }
}
</style>
