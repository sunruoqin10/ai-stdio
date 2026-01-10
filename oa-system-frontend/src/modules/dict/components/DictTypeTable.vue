<template>
  <div class="dict-type-table">
    <el-table
      v-loading="loading"
      :data="data"
      border
      stripe
      style="width: 100%"
      @row-click="handleRowClick"
    >
      <el-table-column type="index" label="编号" width="80" align="center" />

      <el-table-column
        prop="code"
        label="字典编码"
        min-width="150"
        show-overflow-tooltip
      >
        <template #default="{ row }">
          <el-tag size="small" type="info">{{ row.code }}</el-tag>
        </template>
      </el-table-column>

      <el-table-column
        prop="name"
        label="字典名称"
        min-width="150"
        show-overflow-tooltip
      />

      <el-table-column
        prop="category"
        label="类别"
        width="120"
        align="center"
      >
        <template #default="{ row }">
          <el-tag v-if="row.category === 'system'" type="warning" size="small">
            <el-icon style="margin-right: 4px"><Lock /></el-icon>
            系统字典
          </el-tag>
          <el-tag v-else type="primary" size="small">业务字典</el-tag>
        </template>
      </el-table-column>

      <el-table-column
        prop="itemCount"
        label="项数量"
        width="100"
        align="center"
      >
        <template #default="{ row }">
          <el-tag type="success" size="small">
            {{ row.itemCount || 0 }}
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column
        prop="status"
        label="状态"
        width="100"
        align="center"
      >
        <template #default="{ row }">
          <StatusTag :status="row.status === 'enabled' ? 'active' : 'inactive'" />
        </template>
      </el-table-column>

      <el-table-column
        prop="sortOrder"
        label="排序"
        width="100"
        align="center"
      />

      <el-table-column
        prop="createdAt"
        label="创建时间"
        width="180"
        align="center"
      />

      <el-table-column
        label="操作"
        width="280"
        align="center"
        fixed="right"
      >
        <template #default="{ row }">
          <el-button
            link
            type="primary"
            size="small"
            @click.stop="handleViewItems(row)"
          >
            查看项
          </el-button>
          <el-button
            link
            type="primary"
            size="small"
            @click.stop="handleEdit(row)"
          >
            编辑
          </el-button>
          <el-popconfirm
            v-if="row.category === 'system'"
            title="系统字典不可删除!"
            width="300"
            confirm-button-text="知道了"
            :show-cancel="false"
            :icon="Lock"
            icon-color="#E6A23C"
          >
            <template #reference>
              <el-button
                link
                type="danger"
                size="small"
                disabled
                @click.stop
              >
                <el-icon><Lock /></el-icon>
                删除
              </el-button>
            </template>
          </el-popconfirm>
          <el-popconfirm
            v-else
            title="确定要删除该字典类型吗?"
            width="300"
            confirm-button-text="确定"
            cancel-button-text="取消"
            @confirm.stop="handleDelete(row)"
          >
            <template #reference>
              <el-button link type="danger" size="small" @click.stop>
                删除
              </el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <div v-if="total > 0" class="pagination-container">
      <el-pagination
        :current-page="currentPage"
        :page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Lock } from '@element-plus/icons-vue'
import StatusTag from '@/components/common/StatusTag.vue'
import type { DictType } from '../types'
import { useDictStore } from '../store'

interface Props {
  data: DictType[]
  loading?: boolean
  total: number
  currentPage: number
  pageSize: number
}

const props = defineProps<Props>()

interface Emits {
  (e: 'edit', dictType: DictType): void
  (e: 'delete', dictType: DictType): void
  (e: 'view-items', dictType: DictType): void
  (e: 'page-change', page: number): void
  (e: 'size-change', size: number): void
}

const emit = defineEmits<Emits>()

const dictStore = useDictStore()

// 行点击
function handleRowClick(row: DictType) {
  // 可以在这里实现行点击后的操作
}

// 查看字典项
function handleViewItems(row: DictType) {
  emit('view-items', row)
}

// 编辑
function handleEdit(row: DictType) {
  emit('edit', row)
}

// 删除
async function handleDelete(row: DictType) {
  try {
    await dictStore.deleteDictType(row.id)
    ElMessage.success('删除成功')
  } catch (error: any) {
    ElMessage.error(error.message || '删除失败')
  }
}

// 页码改变
function handlePageChange(page: number) {
  emit('page-change', page)
}

// 每页数量改变
function handleSizeChange(size: number) {
  emit('size-change', size)
}
</script>

<style scoped lang="scss">
.dict-type-table {
  .pagination-container {
    display: flex;
    justify-content: center;
    margin-top: 20px;
  }

  :deep(.el-table__row) {
    cursor: pointer;

    &:hover {
      background-color: var(--el-fill-color-light);
    }
  }
}
</style>
