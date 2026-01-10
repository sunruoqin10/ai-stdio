<template>
  <div class="menu-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>菜单管理</h2>
    </div>

    <!-- 操作栏 -->
    <div class="toolbar">
      <el-button
        v-if="canCreate"
        type="primary"
        :icon="Plus"
        @click="handleCreate"
      >
        新增菜单
      </el-button>

      <el-button
        v-if="canExport"
        :icon="Download"
        @click="handleExport"
      >
        导出配置
      </el-button>

      <el-button
        :icon="Refresh"
        @click="handleRefresh"
      >
        刷新
      </el-button>
    </div>

    <!-- 筛选面板 -->
    <div class="filter-panel">
      <el-form :inline="true" :model="filterForm">
        <el-form-item label="菜单类型">
          <el-select
            v-model="filterForm.type"
            placeholder="请选择"
            clearable
            style="width: 150px"
          >
            <el-option
              v-for="item in menuTypeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="状态">
          <el-select
            v-model="filterForm.status"
            placeholder="请选择"
            clearable
            style="width: 150px"
          >
            <el-option label="启用" :value="true" />
            <el-option label="禁用" :value="false" />
          </el-select>
        </el-form-item>

        <el-form-item label="关键字">
          <el-input
            v-model="filterForm.keyword"
            placeholder="菜单名称/路由"
            clearable
            style="width: 200px"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleFilter">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 菜单树形表格 -->
    <div class="table-container">
      <menu-tree
        :data="menuList"
        :loading="loading"
        @edit="handleEdit"
        @add-child="handleAddChild"
        @toggle-status="handleToggleStatus"
        @delete="handleDelete"
      />
    </div>

    <!-- 菜单表单对话框 -->
    <menu-form
      v-model:visible="formDialogVisible"
      :type="formDialogType"
      :data="currentMenu"
      @confirm="handleFormConfirm"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { Plus, Download, Refresh } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import MenuTree from '../components/MenuTree.vue'
import MenuForm from '../components/MenuForm.vue'
import { useMenuStore } from '../stores/menuStore'
import { useMenuDict } from '../composables/useMenuDict'
import { useMenuPermission } from '../composables/useMenuPermission'
import type { MenuItem, MenuQuery } from '../types'

const menuStore = useMenuStore()
const { menuTypeOptions } = useMenuDict()
const { canCreate, canExport } = useMenuPermission()

// 筛选表单
const filterForm = ref<MenuQuery>({
  type: '',
  status: null,
  keyword: ''
})

// 表单对话框
const formDialogVisible = ref(false)
const formDialogType = ref<'create' | 'edit'>('create')
const currentMenu = ref<MenuItem | undefined>(undefined)

// 菜单列表
const menuList = computed(() => menuStore.treeMenuList)
const loading = computed(() => menuStore.loading)

// 初始化
onMounted(() => {
  loadMenuList()
})

// 加载菜单列表
async function loadMenuList(params?: MenuQuery) {
  try {
    await menuStore.fetchMenuList(params)
  } catch (error) {
    ElMessage.error('加载菜单列表失败')
  }
}

// 新增菜单
function handleCreate() {
  formDialogType.value = 'create'
  currentMenu.value = undefined
  formDialogVisible.value = true
}

// 编辑菜单
function handleEdit(row: MenuItem) {
  formDialogType.value = 'edit'
  currentMenu.value = row
  formDialogVisible.value = true
}

// 新增子菜单
function handleAddChild(row: MenuItem) {
  formDialogType.value = 'create'
  currentMenu.value = {
    ...row,
    id: undefined,
    parentId: row.id
  } as any
  formDialogVisible.value = true
}

// 切换状态
async function handleToggleStatus(row: MenuItem) {
  try {
    const newStatus = !row.status
    await menuStore.toggleStatus(row.id, newStatus)
    ElMessage.success(newStatus ? '已启用' : '已禁用')
    await loadMenuList(filterForm.value)
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

// 删除菜单
async function handleDelete(row: MenuItem) {
  try {
    await menuStore.deleteMenu(row.id)
    ElMessage.success('删除成功')
    await loadMenuList(filterForm.value)
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 表单确认
async function handleFormConfirm() {
  formDialogVisible.value = false
  await loadMenuList(filterForm.value)
}

// 筛选
function handleFilter() {
  loadMenuList(filterForm.value)
}

// 重置
function handleReset() {
  filterForm.value = {
    type: '',
    status: null,
    keyword: ''
  }
  loadMenuList()
}

// 刷新
function handleRefresh() {
  loadMenuList(filterForm.value)
}

// 导出
function handleExport() {
  ElMessage.info('导出功能开发中')
}
</script>

<style scoped>
.menu-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  font-size: 24px;
  font-weight: 500;
  color: #303133;
}

.toolbar {
  margin-bottom: 20px;
  display: flex;
  gap: 10px;
}

.filter-panel {
  background-color: #fff;
  padding: 20px;
  border-radius: 4px;
  margin-bottom: 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.table-container {
  background-color: #fff;
  padding: 20px;
  border-radius: 4px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

:deep(.el-form-item) {
  margin-bottom: 0;
}
</style>
