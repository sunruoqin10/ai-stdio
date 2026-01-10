<template>
  <el-dialog
    v-model="dialogVisible"
    :title="isEditMode ? '编辑权限' : '新增权限'"
    width="700px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="120px"
      class="permission-form"
    >
      <el-form-item label="权限名称" prop="name">
        <el-input
          v-model="formData.name"
          placeholder="请输入权限名称"
          maxlength="50"
          show-word-limit
          clearable
        />
      </el-form-item>

      <el-form-item label="权限编码" prop="code">
        <el-input
          v-model="formData.code"
          placeholder="如: employee:view:list"
          maxlength="100"
          clearable
        >
          <template #append>
            <el-button @click="generateCode">自动生成</el-button>
          </template>
        </el-input>
      </el-form-item>

      <el-form-item label="权限类型" prop="type">
        <el-select v-model="formData.type" placeholder="请选择权限类型" @change="handleTypeChange">
          <el-option label="菜单权限" value="menu">
            <div class="option-item">
              <el-icon><Menu /></el-icon>
              <span>菜单权限 - 控制菜单和路由访问</span>
            </div>
          </el-option>
          <el-option label="按钮权限" value="button">
            <div class="option-item">
              <el-icon><Operation /></el-icon>
              <span>按钮权限 - 控制页面内按钮操作</span>
            </div>
          </el-option>
          <el-option label="接口权限" value="api">
            <div class="option-item">
              <el-icon><Connection /></el-icon>
              <span>接口权限 - 控制后端API访问</span>
            </div>
          </el-option>
          <el-option label="数据权限" value="data">
            <div class="option-item">
              <el-icon><Grid /></el-icon>
              <span>数据权限 - 控制数据访问范围</span>
            </div>
          </el-option>
        </el-select>
      </el-form-item>

      <el-form-item label="所属模块" prop="module">
        <el-select
          v-model="formData.module"
          placeholder="请选择所属模块"
          filterable
          allow-create
        >
          <el-option
            v-for="module in moduleList"
            :key="module"
            :label="module"
            :value="module"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="上级权限" prop="parentId">
        <el-tree-select
          v-model="formData.parentId"
          :data="parentOptions"
          :props="{ label: 'name', value: 'id' }"
          placeholder="请选择上级权限"
          check-strictly
          clearable
          filterable
        />
      </el-form-item>

      <!-- 菜单权限特有字段 -->
      <template v-if="formData.type === 'menu'">
        <el-form-item label="路由路径" prop="path">
          <el-input
            v-model="formData.path"
            placeholder="如: /employee"
            clearable
          >
            <template #prefix>/</template>
          </el-input>
        </el-form-item>

        <el-form-item label="组件路径" prop="component">
          <el-input
            v-model="formData.component"
            placeholder="如: @/views/employee/List"
            clearable
          />
        </el-form-item>

        <el-form-item label="图标" prop="icon">
          <el-input
            v-model="formData.icon"
            placeholder="如: User"
            clearable
          >
            <template #prefix>
              <el-icon v-if="formData.icon">
                <component :is="formData.icon" />
              </el-icon>
              <el-icon v-else><Picture /></el-icon>
            </template>
          </el-input>
        </el-form-item>
      </template>

      <!-- 接口权限特有字段 -->
      <template v-if="formData.type === 'api'">
        <el-form-item label="接口地址" prop="apiPath">
          <el-input
            v-model="formData.apiPath"
            placeholder="如: /api/employees"
            clearable
          >
            <template #prepend>API</template>
          </el-input>
        </el-form-item>

        <el-form-item label="请求方式" prop="apiMethod">
          <el-select v-model="formData.apiMethod" placeholder="请选择请求方式">
            <el-option label="GET" value="GET" />
            <el-option label="POST" value="POST" />
            <el-option label="PUT" value="PUT" />
            <el-option label="DELETE" value="DELETE" />
            <el-option label="PATCH" value="PATCH" />
          </el-select>
        </el-form-item>
      </template>

      <!-- 数据权限特有字段 -->
      <template v-if="formData.type === 'data'">
        <el-form-item label="数据范围" prop="dataScope">
          <el-select v-model="formData.dataScope" placeholder="请选择数据范围">
            <el-option label="全部数据" value="all">
              <div class="option-item">
                <el-icon><Location /></el-icon>
                <span>全部数据 - 可查看所有数据</span>
              </div>
            </el-option>
            <el-option label="本部门及下级" value="dept_and_sub">
              <div class="option-item">
                <el-icon><Office-building /></el-icon>
                <span>本部门及下级 - 可查看本部门和下级部门数据</span>
              </div>
            </el-option>
            <el-option label="本部门" value="dept">
              <div class="option-item">
                <el-icon><Office-building /></el-icon>
                <span>本部门 - 仅可查看本部门数据</span>
              </div>
            </el-option>
            <el-option label="仅本人" value="self">
              <div class="option-item">
                <el-icon><User /></el-icon>
                <span>仅本人 - 只能查看自己的数据</span>
              </div>
            </el-option>
          </el-select>
        </el-form-item>
      </template>

      <el-form-item label="排序号" prop="sort">
        <el-input-number
          v-model="formData.sort"
          :min="0"
          :max="999"
          placeholder="请输入排序号"
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">
          确定
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Menu,
  Operation,
  Connection,
  Grid,
  Picture,
  OfficeBuilding,
  User,
  Location
} from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import type { Permission, PermissionForm } from '../types'
import { generatePermissionCode } from '../utils/permission'

// Props
interface Props {
  modelValue: boolean
  permissionData?: Permission
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: false,
  permissionData: undefined
})

// Emits
const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  'success': []
}>()

// Store
// const permissionStore = usePermissionStore()

// 状态
const formRef = ref<FormInstance>()
const submitting = ref(false)
const moduleList = ref<string[]>(['system', 'employee', 'department', 'meeting', 'approval'])

// 对话框显示状态
const dialogVisible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

// 是否为编辑模式
const isEditMode = computed(() => !!props.permissionData)

// 上级权限选项
const parentOptions = computed(() => {
  // TODO: 从权限树中获取上级权限选项
  return []
})

// 表单数据
const formData = reactive<PermissionForm>({
  name: '',
  code: '',
  type: 'menu',
  module: '',
  parentId: undefined,
  path: '',
  component: '',
  icon: '',
  apiPath: '',
  apiMethod: 'GET',
  dataScope: 'all',
  sort: 0
})

// 表单验证规则
const formRules: FormRules = {
  name: [
    { required: true, message: '请输入权限名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入权限编码', trigger: 'blur' },
    {
      pattern: /^[a-z0-9:_]+$/,
      message: '格式: 模块:操作:对象,如 employee:view:list',
      trigger: 'blur'
    }
  ],
  type: [
    { required: true, message: '请选择权限类型', trigger: 'change' }
  ],
  module: [
    { required: true, message: '请选择所属模块', trigger: 'change' }
  ]
}

// ==================== 方法 ====================

/**
 * 初始化表单
 */
function initForm() {
  if (props.permissionData) {
    Object.assign(formData, {
      name: props.permissionData.name,
      code: props.permissionData.code,
      type: props.permissionData.type,
      module: props.permissionData.module,
      parentId: props.permissionData.parentId,
      path: props.permissionData.path || '',
      component: props.permissionData.component || '',
      icon: props.permissionData.icon || '',
      apiPath: props.permissionData.apiPath || '',
      apiMethod: props.permissionData.apiMethod || 'GET',
      dataScope: props.permissionData.dataScope || 'all',
      sort: props.permissionData.sort
    })
  } else {
    Object.assign(formData, {
      name: '',
      code: '',
      type: 'menu',
      module: '',
      parentId: undefined,
      path: '',
      component: '',
      icon: '',
      apiPath: '',
      apiMethod: 'GET',
      dataScope: 'all',
      sort: 0
    })
  }
}

/**
 * 权限类型变更
 */
function handleTypeChange() {
  // 清空类型特有字段
  if (formData.type === 'menu') {
    formData.apiPath = ''
    formData.apiMethod = 'GET'
    formData.dataScope = 'all'
  } else if (formData.type === 'api') {
    formData.path = ''
    formData.component = ''
    formData.icon = ''
    formData.dataScope = 'all'
  } else if (formData.type === 'data') {
    formData.path = ''
    formData.component = ''
    formData.icon = ''
    formData.apiPath = ''
    formData.apiMethod = 'GET'
  } else {
    formData.path = ''
    formData.component = ''
    formData.icon = ''
    formData.apiPath = ''
    formData.apiMethod = 'GET'
    formData.dataScope = 'all'
  }
}

/**
 * 自动生成权限编码
 */
function generateCode() {
  if (!formData.module || !formData.name) {
    ElMessage.warning('请先选择模块和输入权限名称')
    return
  }

  // 简化的编码生成逻辑
  formData.code = generatePermissionCode(
    formData.module,
    'view',
    formData.name.toLowerCase().replace(/\s+/g, '_')
  )
}

/**
 * 提交表单
 */
async function handleSubmit() {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    submitting.value = true

    // TODO: 调用创建或更新权限API
    await new Promise(resolve => setTimeout(resolve, 1000))

    ElMessage.success(isEditMode.value ? '更新成功' : '创建成功')
    emit('success')
  } catch (error) {
    // 验证失败
  } finally {
    submitting.value = false
  }
}

/**
 * 关闭对话框
 */
function handleClose() {
  formRef.value?.resetFields()
  dialogVisible.value = false
}

// ==================== 监听 ====================

watch(
  () => props.modelValue,
  (val) => {
    if (val) {
      initForm()
    }
  }
)

onMounted(() => {
  // 加载模块列表
  // TODO: 从API加载
})
</script>

<style scoped lang="scss">
.permission-form {
  padding: 0 20px;
  max-height: 600px;
  overflow-y: auto;

  :deep(.el-form-item) {
    margin-bottom: 22px;
  }

  .option-item {
    display: flex;
    align-items: center;
    gap: 8px;

    .el-icon {
      font-size: 16px;
    }
  }

  .dialog-footer {
    display: flex;
    justify-content: flex-end;
    gap: 12px;
  }
}
</style>
