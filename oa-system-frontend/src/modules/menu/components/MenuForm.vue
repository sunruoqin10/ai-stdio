<template>
  <el-dialog
    v-model="dialogVisible"
    :title="dialogType === 'create' ? '新增菜单' : '编辑菜单'"
    width="800px"
    @close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="formData"
      :rules="rules"
      label-width="120px"
    >
      <!-- 基本信息 -->
      <el-divider content-position="left">基本信息</el-divider>

      <el-form-item label="菜单名称" prop="menuName">
        <el-input
          v-model="formData.menuName"
          placeholder="请输入菜单名称"
          maxlength="50"
          show-word-limit
        />
      </el-form-item>

      <el-form-item label="菜单类型" prop="menuType">
        <el-radio-group v-model="formData.menuType">
          <el-radio label="directory">目录</el-radio>
          <el-radio label="menu">菜单</el-radio>
          <el-radio label="button">按钮</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item label="父级菜单" prop="parentId">
        <el-tree-select
          v-model="formData.parentId"
          :data="parentMenuOptions"
          :props="{ label: 'menuName', value: 'id' }"
          placeholder="请选择父级菜单"
          check-strictly
          :render-after-expand="false"
        />
      </el-form-item>

      <!-- 路由配置 -->
      <el-divider content-position="left">路由配置</el-divider>

      <el-form-item
        v-if="formData.menuType !== 'directory'"
        label="路由路径"
        prop="routePath"
      >
        <el-input
          v-model="formData.routePath"
          placeholder="请输入路由路径,如: /system/user"
        />
      </el-form-item>

      <el-form-item
        v-if="formData.menuType === 'menu'"
        label="组件路径"
        prop="componentPath"
      >
        <el-input
          v-model="formData.componentPath"
          placeholder="请输入组件路径,如: system/user/index"
        />
      </el-form-item>

      <el-form-item label="重定向路径">
        <el-input
          v-model="formData.redirectPath"
          placeholder="请输入重定向路径"
        />
      </el-form-item>

      <!-- 菜单属性 -->
      <el-divider content-position="left">菜单属性</el-divider>

      <el-form-item label="菜单图标">
        <icon-selector v-model="formData.menuIcon" />
      </el-form-item>

      <el-form-item
        v-if="formData.menuType === 'button'"
        label="权限标识"
        prop="permission"
      >
        <el-input
          v-model="formData.permission"
          placeholder="请输入权限标识,如: system:user:view"
        />
      </el-form-item>

      <el-form-item label="排序号" prop="sortOrder">
        <el-input-number
          v-model="formData.sortOrder"
          :min="0"
          :max="9999"
          controls-position="right"
        />
      </el-form-item>

      <el-form-item label="显示">
        <el-switch v-model="formData.visible" />
      </el-form-item>

      <el-form-item label="状态">
        <el-switch v-model="formData.status" />
      </el-form-item>

      <el-form-item label="缓存">
        <el-switch v-model="formData.isCache" />
      </el-form-item>

      <el-form-item label="外链">
        <el-switch v-model="formData.isFrame" />
      </el-form-item>

      <el-form-item v-if="formData.isFrame" label="外链URL">
        <el-input
          v-model="formData.frameUrl"
          placeholder="请输入外链URL"
        />
      </el-form-item>

      <el-form-item v-if="formData.isFrame" label="打开方式">
        <el-select v-model="formData.menuTarget" placeholder="请选择">
          <el-option label="当前页" value="_self" />
          <el-option label="新页" value="_blank" />
        </el-select>
      </el-form-item>

      <!-- 其他信息 -->
      <el-divider content-position="left">其他信息</el-divider>

      <el-form-item label="备注">
        <el-input
          v-model="formData.remark"
          type="textarea"
          :rows="3"
          placeholder="请输入备注"
          maxlength="200"
          show-word-limit
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" @click="handleConfirm">确定</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import IconSelector from './IconSelector.vue'
import { useMenuStore } from '../stores/menuStore'
import { useMenuTree } from '../composables/useMenuTree'
import { MenuType } from '../types'

interface Props {
  visible: boolean
  type: 'create' | 'edit'
  data?: any
}

interface Emits {
  (e: 'update:visible', value: boolean): void
  (e: 'confirm'): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const menuStore = useMenuStore()
const { getAvailableParents } = useMenuTree()

const formRef = ref<FormInstance>()
const formData = ref<any>({
  parentId: 0,
  menuName: '',
  menuType: MenuType.MENU,
  sortOrder: 0,
  visible: true,
  status: true,
  isCache: false,
  isFrame: false
})

const rules: FormRules = {
  menuName: [
    { required: true, message: '请输入菜单名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  menuType: [
    { required: true, message: '请选择菜单类型', trigger: 'change' }
  ],
  parentId: [
    { required: true, message: '请选择父级菜单', trigger: 'change' }
  ],
  routePath: [
    { required: true, message: '请输入路由路径', trigger: 'blur' }
  ],
  componentPath: [
    { required: true, message: '请输入组件路径', trigger: 'blur' }
  ],
  permission: [
    { required: true, message: '请输入权限标识', trigger: 'blur' }
  ],
  sortOrder: [
    { required: true, message: '请输入排序号', trigger: 'blur' }
  ]
}

const dialogVisible = computed({
  get: () => props.visible,
  set: (value) => emit('update:visible', value)
})

// 获取可选的父级菜单
const parentMenuOptions = computed(() => {
  return getAvailableParents(menuStore.menuList, props.data?.id)
})

// 监听数据变化
watch(() => props.data, (newData) => {
  if (newData) {
    formData.value = { ...newData }
  } else {
    resetForm()
  }
}, { immediate: true })

// 监听类型变化,重置某些字段
watch(() => formData.value.menuType, (newType) => {
  if (newType === 'directory') {
    formData.value.routePath = undefined
    formData.value.componentPath = undefined
    formData.value.permission = undefined
  } else if (newType === 'menu') {
    formData.value.permission = undefined
  } else if (newType === 'button') {
    formData.value.routePath = undefined
    formData.value.componentPath = undefined
  }
})

function resetForm() {
  formData.value = {
    parentId: 0,
    menuName: '',
    menuType: MenuType.MENU,
    sortOrder: 0,
    visible: true,
    status: true,
    isCache: false,
    isFrame: false
  }
  formRef.value?.clearValidate()
}

function handleClose() {
  dialogVisible.value = false
  resetForm()
}

async function handleConfirm() {
  try {
    await formRef.value?.validate()
    emit('confirm')
  } catch (error) {
    console.error('表单验证失败:', error)
  }
}
</script>

<style scoped>
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

:deep(.el-divider) {
  margin: 20px 0;
}

:deep(.el-divider__text) {
  font-weight: 500;
  color: #303133;
}
</style>
