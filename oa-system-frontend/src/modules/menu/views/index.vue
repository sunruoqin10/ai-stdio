<template>
  <div class="menu-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>菜单管理</h2>
    </div>

    <!-- 操作栏 -->
    <div class="toolbar">
      <el-button
        v-if="!isInitialized"
        type="warning"
        :icon="Setting"
        @click="handleInitMenus"
      >
        初始化系统菜单
      </el-button>

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
import { Plus, Download, Refresh, Setting } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import MenuTree from '../components/MenuTree.vue'
import MenuForm from '../components/MenuForm.vue'
import { useMenuStore } from '../stores/menuStore'
import { useMenuDict } from '../composables/useMenuDict'
import { useMenuPermission } from '../composables/useMenuPermission'
import type { MenuItem, MenuQuery, MenuForm as MenuFormType } from '../types'
import { getInitMenusWithCode, isMenuInitialized, setMenuInitialized } from '../data/initMenus'

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

// 是否已初始化
const isInitialized = ref(false)

// 初始化
onMounted(() => {
  // 检查是否已初始化
  isInitialized.value = isMenuInitialized()
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

// 初始化系统菜单
async function handleInitMenus() {
  try {
    await ElMessageBox.confirm(
      '确定要初始化系统菜单吗？这将创建系统的所有基础菜单数据。',
      '初始化确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    // 获取初始化数据
    const initMenus = getInitMenusWithCode()

    // 用于存储创建的菜单ID映射
    const menuIdMap = new Map<string, number>()

    // 逐个创建菜单
    let successCount = 0
    let failCount = 0

    for (const menu of initMenus) {
      try {
        // 检查是否需要设置父级ID
        const menuData: MenuFormType = { ...menu }

        // 根据菜单名称调整父子关系
        if (menu.menuName === '员工详情') {
          const employeeMenu = initMenus.find(m => m.menuName === '员工名录')
          if (employeeMenu && menuIdMap.has('员工名录')) {
            menuData.parentId = menuIdMap.get('员工名录')!
          }
        } else if (menu.menuName === '字典项管理') {
          const dictMenu = initMenus.find(m => m.menuName === '数据字典管理')
          if (dictMenu && menuIdMap.has('数据字典管理')) {
            menuData.parentId = menuIdMap.get('数据字典管理')!
          }
        } else if (menu.menuName === '角色管理' || menu.menuName === '权限管理') {
          const permissionDir = initMenus.find(m => m.menuName === '权限管理' && m.menuType === 'directory')
          if (permissionDir && menuIdMap.has('权限管理目录')) {
            menuData.parentId = menuIdMap.get('权限管理目录')!
          }
        }

        const result = await menuStore.createMenu(menuData)
        const createdMenu = (result as any)?.data || result

        if (createdMenu?.id) {
          // 使用特殊的key来存储ID，避免同名菜单冲突
          const mapKey = menu.menuName === '权限管理' && menu.menuType === 'menu'
            ? '权限管理_子'
            : menu.menuName === '权限管理' && menu.menuType === 'directory'
            ? '权限管理目录'
            : menu.menuName

          menuIdMap.set(mapKey, createdMenu.id)
          successCount++
        }
      } catch (error) {
        console.error(`创建菜单失败: ${menu.menuName}`, error)
        failCount++
      }
    }

    // 标记为已初始化
    setMenuInitialized(true)
    isInitialized.value = true

    // 显示结果
    if (failCount === 0) {
      ElMessage.success(`菜单初始化成功！共创建 ${successCount} 个菜单`)
    } else {
      ElMessage.warning(`菜单初始化完成！成功 ${successCount} 个，失败 ${failCount} 个`)
    }

    // 刷新菜单列表
    await loadMenuList(filterForm.value)
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('菜单初始化失败')
      console.error('初始化菜单时出错:', error)
    }
  }
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
