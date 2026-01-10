<template>
  <el-dialog
    v-model="dialogVisible"
    :title="isEditMode ? '编辑角色' : '新增角色'"
    width="600px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="100px"
      class="role-form"
    >
      <el-form-item label="角色名称" prop="name">
        <el-input
          v-model="formData.name"
          placeholder="请输入角色名称"
          maxlength="50"
          show-word-limit
          clearable
        />
      </el-form-item>

      <el-form-item label="角色编码" prop="code">
        <el-input
          v-model="formData.code"
          placeholder="请输入角色编码(字母、数字、下划线)"
          maxlength="50"
          clearable
          :disabled="isEditMode && roleData?.isPreset"
        >
          <template #prefix>
            <el-icon><Key /></el-icon>
          </template>
        </el-input>
      </el-form-item>

      <el-form-item label="角色类型" prop="type">
        <el-radio-group v-model="formData.type" :disabled="isEditMode && roleData?.isPreset">
          <el-radio label="custom">自定义角色</el-radio>
          <el-radio label="system">系统角色</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item label="排序号" prop="sort">
        <el-input-number
          v-model="formData.sort"
          :min="0"
          :max="999"
          placeholder="请输入排序号"
        />
      </el-form-item>

      <el-form-item label="角色描述" prop="description">
        <el-input
          v-model="formData.description"
          type="textarea"
          :rows="4"
          placeholder="请输入角色描述"
          maxlength="500"
          show-word-limit
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
import { ref, reactive, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Key } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import type { Role, RoleForm } from '../types'

// Props
interface Props {
  modelValue: boolean
  roleData?: Role
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: false,
  roleData: undefined
})

// Emits
const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  'success': []
}>()

// 状态
const formRef = ref<FormInstance>()
const submitting = ref(false)

// 对话框显示状态
const dialogVisible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

// 是否为编辑模式
const isEditMode = computed(() => !!props.roleData)

// 表单数据
const formData = reactive<RoleForm>({
  name: '',
  code: '',
  type: 'custom',
  sort: 0,
  description: ''
})

// 表单验证规则
const formRules: FormRules = {
  name: [
    { required: true, message: '请输入角色名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入角色编码', trigger: 'blur' },
    {
      pattern: /^[a-zA-Z0-9_]+$/,
      message: '只能包含字母、数字和下划线',
      trigger: 'blur'
    }
  ],
  type: [
    { required: true, message: '请选择角色类型', trigger: 'change' }
  ]
}

// ==================== 方法 ====================

/**
 * 初始化表单
 */
function initForm() {
  if (props.roleData) {
    Object.assign(formData, {
      name: props.roleData.name,
      code: props.roleData.code,
      type: props.roleData.type,
      sort: props.roleData.sort,
      description: props.roleData.description
    })
  } else {
    Object.assign(formData, {
      name: '',
      code: '',
      type: 'custom',
      sort: 0,
      description: ''
    })
  }
}

/**
 * 提交表单
 */
async function handleSubmit() {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    submitting.value = true

    // TODO: 调用创建或更新角色API
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
</script>

<style scoped lang="scss">
.role-form {
  padding: 0 20px;

  :deep(.el-form-item) {
    margin-bottom: 22px;
  }

  .dialog-footer {
    display: flex;
    justify-content: flex-end;
    gap: 12px;
  }
}
</style>
