/**
 * 菜单管理模块 - 业务逻辑组合函数
 */

import { ref, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useMenuStore } from '../stores/menuStore'
import { useAuthStore } from '@/modules/auth/store'
import { MENU_PERMISSIONS } from '../config/permissions'
import type { MenuForm, MenuItem } from '../types'

export function useMenu() {
  const menuStore = useMenuStore()
  const authStore = useAuthStore()

  const dialogVisible = ref(false)
  const dialogType = ref<'create' | 'edit'>('create')
  const formData = ref<Partial<MenuForm>>({})
  const formRef = ref()

  // 权限检查
  const canCreate = computed(() => authStore.hasPermission(MENU_PERMISSIONS.CREATE))
  const canEdit = computed(() => authStore.hasPermission(MENU_PERMISSIONS.EDIT))
  const canDelete = computed(() => authStore.hasPermission(MENU_PERMISSIONS.DELETE))
  const canEnable = computed(() => authStore.hasPermission(MENU_PERMISSIONS.ENABLE))

  // 打开新增对话框
  function openCreateDialog(parentId = 0) {
    dialogType.value = 'create'
    formData.value = {
      parentId,
      menuName: '',
      menuType: 'menu' as any,
      sortOrder: 0,
      visible: true,
      status: true,
      isCache: false,
      isFrame: false
    }
    dialogVisible.value = true
  }

  // 打开编辑对话框
  async function openEditDialog(menu: MenuItem) {
    dialogType.value = 'edit'
    formData.value = { ...menu }
    dialogVisible.value = true
  }

  // 保存菜单
  async function handleSave() {
    try {
      await formRef.value?.validate()

      if (dialogType.value === 'create') {
        await menuStore.createMenu(formData.value as MenuForm)
        ElMessage.success('创建成功')
      } else {
        await menuStore.updateMenu(formData.value!.id!, formData.value as MenuForm)
        ElMessage.success('更新成功')
      }

      dialogVisible.value = false
      await menuStore.fetchMenuList()
    } catch (error) {
      ElMessage.error('保存失败')
      throw error
    }
  }

  // 删除菜单
  async function handleDelete(menu: MenuItem) {
    if (!canDelete.value) {
      ElMessage.warning('您没有删除权限')
      return
    }

    if (menu.isSystem) {
      ElMessage.warning('系统菜单不能删除')
      return
    }

    if (menu.children?.length) {
      ElMessage.warning('请先删除子菜单')
      return
    }

    try {
      await ElMessageBox.confirm('确认删除该菜单吗?', '提示', {
        type: 'warning'
      })

      await menuStore.deleteMenu(menu.id)
      ElMessage.success('删除成功')
      await menuStore.fetchMenuList()
    } catch (error) {
      if (error !== 'cancel') {
        ElMessage.error('删除失败')
      }
    }
  }

  // 切换状态
  async function handleToggleStatus(menu: MenuItem) {
    if (!canEnable.value) {
      ElMessage.warning('您没有操作权限')
      return
    }

    try {
      const newStatus = !menu.status
      await menuStore.toggleStatus(menu.id, newStatus)
      ElMessage.success(newStatus ? '已启用' : '已禁用')
      await menuStore.fetchMenuList()
    } catch (error) {
      ElMessage.error('操作失败')
    }
  }

  return {
    // 状态
    dialogVisible,
    dialogType,
    formData,
    formRef,

    // 权限
    canCreate,
    canEdit,
    canDelete,
    canEnable,

    // 操作
    openCreateDialog,
    openEditDialog,
    handleSave,
    handleDelete,
    handleToggleStatus
  }
}
