<template>
  <div class="menu-tree">
    <el-tree
      :data="menuData"
      node-key="id"
      default-expand-all
      :expand-on-click-node="false"
      v-loading="loading"
      class="menu-tree-component"
    >
      <template #default="{ node, data }">
        <div class="custom-tree-node">
          <!-- 菜单信息 -->
          <div class="node-info">
            <div class="node-header">
              <!-- 图标 -->
              <el-icon v-if="data.menuIcon" class="menu-icon">
                <component :is="getIconComponent(data.menuIcon)" />
              </el-icon>

              <!-- 菜单名称 -->
              <span class="menu-name">{{ data.menuName }}</span>

              <!-- 系统标签 -->
              <el-tag v-if="data.isSystem" type="warning" size="small" style="margin-left: 8px">
                系统
              </el-tag>
            </div>

            <!-- 菜单详情 -->
            <div class="node-details">
              <el-tag :type="getMenuTypeTagType(data.menuType)" size="small">
                {{ getMenuTypeLabel(data.menuType) }}
              </el-tag>

              <span class="detail-item">
                <el-icon :size="14"><Sort /></el-icon>
                排序: {{ data.sortOrder }}
              </span>

              <el-tag :type="data.status ? 'success' : 'info'" size="small">
                {{ data.status ? '启用' : '禁用' }}
              </el-tag>

              <span class="detail-item">
                <el-icon :size="14" :color="data.visible ? '#67C23A' : '#909399'">
                  <component :is="data.visible ? 'View' : 'Hide'" />
                </el-icon>
                {{ data.visible ? '可见' : '隐藏' }}
              </span>

              <span v-if="data.routePath" class="detail-item route-path">
                <el-icon :size="14"><Link /></el-icon>
                {{ data.routePath }}
              </span>

              <span v-if="data.permission" class="detail-item permission">
                <el-icon :size="14"><Key /></el-icon>
                {{ data.permission }}
              </span>
            </div>
          </div>

          <!-- 操作按钮 -->
          <div class="node-actions">
            <el-button
              v-if="canEditMenu(data)"
              link
              type="primary"
              size="small"
              @click="handleEdit(data)"
            >
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>

            <el-button
              v-if="canAddChild(data)"
              link
              type="primary"
              size="small"
              @click="handleAddChild(data)"
            >
              <el-icon><Plus /></el-icon>
              新增
            </el-button>

            <el-button
              v-if="canEnableMenu(data)"
              link
              :type="data.status ? 'warning' : 'success'"
              size="small"
              @click="handleToggleStatus(data)"
            >
              <el-icon><component :is="data.status ? 'Hide' : 'View'" /></el-icon>
              {{ data.status ? '禁用' : '启用' }}
            </el-button>

            <el-button
              v-if="canDeleteMenu(data)"
              link
              type="danger"
              size="small"
              @click="handleDelete(data)"
            >
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </div>
        </div>
      </template>
    </el-tree>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import {
  Edit,
  Delete,
  Plus,
  Sort,
  Link,
  Key,
  View,
  Hide
} from '@element-plus/icons-vue'
import type { MenuItem } from '../types'
import { useMenuDict } from '../composables/useMenuDict'
import { useMenuPermission } from '../composables/useMenuPermission'
import { getIconComponent } from '../config/icons'

interface Props {
  data: MenuItem[]
  loading?: boolean
}

interface Emits {
  (e: 'edit', row: MenuItem): void
  (e: 'addChild', row: MenuItem): void
  (e: 'toggleStatus', row: MenuItem): void
  (e: 'delete', row: MenuItem): void
}

const props = withDefaults(defineProps<Props>(), {
  loading: false
})

const emit = defineEmits<Emits>()

const { getMenuTypeLabel } = useMenuDict()
const { canEditMenu, canDeleteMenu, canEnableMenu } = useMenuPermission()

const menuData = computed(() => props.data)

function getMenuTypeTagType(type: string) {
  const typeMap: Record<string, string> = {
    directory: '',
    menu: 'success',
    button: 'warning'
  }
  return typeMap[type] || ''
}

function canAddChild(row: MenuItem): boolean {
  // 目录和菜单可以添加子菜单
  if (row.menuType === 'button') return false

  // 最多3级
  if (row.menuLevel >= 3) return false

  return true
}

function handleEdit(row: MenuItem) {
  emit('edit', row)
}

function handleAddChild(row: MenuItem) {
  emit('addChild', row)
}

function handleToggleStatus(row: MenuItem) {
  emit('toggleStatus', row)
}

function handleDelete(row: MenuItem) {
  emit('delete', row)
}
</script>

<style scoped>
.menu-tree {
  width: 100%;
}

.menu-tree-component {
  background-color: #fff;
}

.custom-tree-node {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 0;
  border-bottom: 1px solid #EBEEF5;
}

.custom-tree-node:last-child {
  border-bottom: none;
}

.node-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.node-header {
  display: flex;
  align-items: center;
  gap: 8px;
}

.menu-icon {
  color: #409EFF;
  font-size: 18px;
}

.menu-name {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

.node-details {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.detail-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #909399;
}

.route-path {
  color: #409EFF;
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.permission {
  color: #67C23A;
  max-width: 150px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.node-actions {
  display: flex;
  align-items: center;
  gap: 4px;
  padding-left: 16px;
}

:deep(.el-tree-node__content) {
  height: auto;
  padding: 8px 0;
}

:deep(.el-tree-node__content:hover) {
  background-color: #F5F7FA;
}

:deep(.el-tree-node__expand-icon) {
  padding: 6px;
}

:deep(.el-button) {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}
</style>
