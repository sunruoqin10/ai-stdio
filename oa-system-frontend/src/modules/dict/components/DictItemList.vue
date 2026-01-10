<template>
  <div class="dict-item-list">
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="5" animated />
    </div>

    <draggable
      v-else
      v-model="localItems"
      item-key="id"
      handle=".drag-handle"
      @end="handleDragEnd"
    >
      <template #item="{ element }">
        <div
          class="dict-item-row"
          :class="{ disabled: element.status === 'disabled' }"
        >
          <!-- 拖拽手柄 -->
          <el-icon class="drag-handle">
            <Rank />
          </el-icon>

          <!-- 标签 -->
          <span class="item-label">{{ element.label }}</span>

          <!-- 值 -->
          <el-tag size="small" class="item-value">
            {{ element.value }}
          </el-tag>

          <!-- 颜色预览 -->
          <DictColorTag
            :label="element.label"
            :color-type="element.colorType"
            size="small"
          />

          <!-- 排序 -->
          <el-input-number
            v-model="element.sortOrder"
            :min="0"
            size="small"
            :step="10"
            class="sort-input"
            @change="handleSortChange(element)"
          />

          <!-- 状态 -->
          <el-switch
            v-model="element.status"
            active-value="enabled"
            inactive-value="disabled"
            @change="handleStatusChange(element)"
          />

          <!-- 操作 -->
          <div class="action-buttons">
            <el-button
              link
              type="primary"
              size="small"
              @click="handleEdit(element)"
            >
              编辑
            </el-button>
            <el-popconfirm
              title="确定要删除该字典项吗?"
              width="300"
              confirm-button-text="确定"
              cancel-button-text="取消"
              @confirm="handleDelete(element)"
            >
              <template #reference>
                <el-button link type="danger" size="small">
                  删除
                </el-button>
              </template>
            </el-popconfirm>
          </div>
        </div>
      </template>
    </draggable>

    <el-empty
      v-if="!loading && localItems.length === 0"
      description="暂无字典项"
    />

    <!-- 批量操作栏 -->
    <div v-if="localItems.length > 0" class="batch-actions">
      <el-button type="primary" @click="handleSaveSort">
        保存排序
      </el-button>
      <el-button @click="handleBatchEnable">
        批量启用
      </el-button>
      <el-button @click="handleBatchDisable">
        批量禁用
      </el-button>
      <el-button type="danger" @click="handleBatchDelete">
        批量删除
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Rank } from '@element-plus/icons-vue'
import draggable from 'vuedraggable'
import DictColorTag from './DictColorTag.vue'
import type { DictItem } from '../types'
import { useDictStore } from '../store'

interface Props {
  items: DictItem[]
  loading?: boolean
}

const props = defineProps<Props>()

interface Emits {
  (e: 'edit', item: DictItem): void
  (e: 'refresh'): void
}

const emit = defineEmits<Emits>()

const dictStore = useDictStore()

// 本地数据(用于拖拽)
const localItems = ref<DictItem[]>([])

// 监听props变化
watch(
  () => props.items,
  (val) => {
    localItems.value = [...val]
  },
  { immediate: true, deep: true }
)

// 拖拽结束
async function handleDragEnd() {
  // 自动更新排序序号
  localItems.value.forEach((item, index) => {
    item.sortOrder = (index + 1) * 10
  })
}

// 保存排序
async function handleSaveSort() {
  try {
    const sortData = localItems.value.map(item => ({
      id: item.id,
      sortOrder: item.sortOrder
    }))

    await dictStore.updateDictItemSort(sortData)
    ElMessage.success('排序已保存')
  } catch (error: any) {
    ElMessage.error(error.message || '保存排序失败')
  }
}

// 排序改变
function handleSortChange(item: DictItem) {
  // 排序改变时可以自动保存或等待用户点击保存按钮
}

// 状态改变
async function handleStatusChange(item: DictItem) {
  try {
    await dictStore.updateDictItem(item.id, {
      status: item.status as 'enabled' | 'disabled'
    })
    ElMessage.success('状态已更新')
    emit('refresh')
  } catch (error: any) {
    ElMessage.error(error.message || '更新状态失败')
  }
}

// 编辑
function handleEdit(item: DictItem) {
  emit('edit', item)
}

// 删除
async function handleDelete(item: DictItem) {
  try {
    await dictStore.deleteDictItem(item.id)
    ElMessage.success('删除成功')
    emit('refresh')
  } catch (error: any) {
    ElMessage.error(error.message || '删除失败')
  }
}

// 批量启用
async function handleBatchEnable() {
  try {
    await ElMessageBox.confirm(
      '确定要启用所有禁用的字典项吗?',
      '批量启用',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const disabledIds = localItems.value
      .filter(item => item.status === 'disabled')
      .map(item => item.id)

    if (disabledIds.length === 0) {
      ElMessage.info('没有需要启用的字典项')
      return
    }

    await dictStore.batchUpdateDictItemStatus(disabledIds, 'enabled')
    ElMessage.success('批量启用成功')
    emit('refresh')
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '批量启用失败')
    }
  }
}

// 批量禁用
async function handleBatchDisable() {
  try {
    await ElMessageBox.confirm(
      '确定要禁用所有启用的字典项吗?',
      '批量禁用',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const enabledIds = localItems.value
      .filter(item => item.status === 'enabled')
      .map(item => item.id)

    if (enabledIds.length === 0) {
      ElMessage.info('没有需要禁用的字典项')
      return
    }

    await dictStore.batchUpdateDictItemStatus(enabledIds, 'disabled')
    ElMessage.success('批量禁用成功')
    emit('refresh')
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '批量禁用失败')
    }
  }
}

// 批量删除
async function handleBatchDelete() {
  try {
    await ElMessageBox.confirm(
      '确定要删除所有字典项吗?此操作不可恢复!',
      '批量删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'error'
      }
    )

    const ids = localItems.value.map(item => item.id)

    await dictStore.batchDeleteDictItems(ids)
    ElMessage.success('批量删除成功')
    emit('refresh')
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '批量删除失败')
    }
  }
}
</script>

<style scoped lang="scss">
.dict-item-list {
  .loading-container {
    padding: 20px;
  }

  .dict-item-row {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 12px;
    border-bottom: 1px solid var(--el-border-color-light);
    transition: all 0.3s ease;
    background-color: var(--el-bg-color);

    &:hover {
      background-color: var(--el-fill-color-light);
    }

    &.disabled {
      opacity: 0.6;
      background-color: var(--el-fill-color);
    }

    .drag-handle {
      cursor: move;
      color: var(--el-text-color-secondary);
      font-size: 18px;
      flex-shrink: 0;

      &:hover {
        color: var(--el-color-primary);
      }
    }

    .item-label {
      flex: 1;
      font-weight: 500;
      min-width: 100px;
    }

    .item-value {
      font-family: 'Courier New', monospace;
      flex-shrink: 0;
    }

    .sort-input {
      width: 100px;
      flex-shrink: 0;
    }

    .action-buttons {
      display: flex;
      gap: 8px;
      flex-shrink: 0;
    }
  }

  .batch-actions {
    display: flex;
    gap: 10px;
    padding: 16px 0;
    border-top: 1px solid var(--el-border-color-light);
    margin-top: 16px;
  }
}
</style>
