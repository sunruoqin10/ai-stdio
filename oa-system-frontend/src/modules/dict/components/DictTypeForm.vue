<template>
  <el-dialog
    v-model="visible"
    :title="isEdit ? '编辑字典类型' : '新增字典类型'"
    width="600px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="formData"
      :rules="rules"
      label-width="120px"
      label-position="right"
      class="dict-type-form"
    >
      <el-form-item label="字典编码" prop="code">
        <el-input
          v-model="formData.code"
          placeholder="如: employee_status"
          :disabled="isEdit"
        >
          <template #suffix>
            <el-tooltip content="只能包含小写字母、数字和下划线,且以字母开头" placement="top">
              <el-icon><QuestionFilled /></el-icon>
            </el-tooltip>
          </template>
        </el-input>
      </el-form-item>

      <el-form-item label="字典名称" prop="name">
        <el-input
          v-model="formData.name"
          placeholder="请输入字典名称"
        />
      </el-form-item>

      <el-form-item label="字典类别" prop="category">
        <el-radio-group v-model="formData.category">
          <el-radio label="system">系统字典</el-radio>
          <el-radio label="business">业务字典</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item label="字典描述" prop="description">
        <el-input
          v-model="formData.description"
          type="textarea"
          :rows="3"
          placeholder="请输入字典描述(可选)"
        />
      </el-form-item>

      <el-form-item label="排序序号" prop="sortOrder">
        <el-input-number
          v-model="formData.sortOrder"
          :min="0"
          :step="10"
          :max="9999"
        />
      </el-form-item>

      <el-form-item label="扩展属性" prop="extProps">
        <el-input
          v-model="extPropsJson"
          type="textarea"
          :rows="5"
          placeholder='请输入JSON格式的扩展属性,如: {"key1": "value1"}'
          @blur="validateExtProps"
        />
        <div v-if="extPropsError" class="error-text">
          {{ extPropsError }}
        </div>
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
import { ref, computed, watch } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { QuestionFilled } from '@element-plus/icons-vue'
import type { DictType, DictTypeForm } from '../types'
import { useDictStore } from '../store'
import { isValidDictCode, stringifyExtProps } from '../utils'

interface Props {
  modelValue: boolean
  dictType?: DictType | null
}

const props = withDefaults(defineProps<Props>(), {
  dictType: null
})

interface Emits {
  (e: 'update:modelValue', value: boolean): void
  (e: 'success'): void
}

const emit = defineEmits<Emits>()

const dictStore = useDictStore()
const formRef = ref<FormInstance>()
const submitting = ref(false)
const extPropsJson = ref('')
const extPropsError = ref('')

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const isEdit = computed(() => !!props.dictType)

// 表单数据
const formData = ref<DictTypeForm>({
  code: '',
  name: '',
  description: '',
  category: 'business',
  status: 'enabled',
  sortOrder: 0,
  extProps: {}
})

// 原始编码(用于编辑时验证)
const originalCode = ref('')

// 表单验证规则
const rules: FormRules = {
  code: [
    { required: true, message: '请输入字典编码', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (!isValidDictCode(value)) {
          callback(new Error('只能包含小写字母、数字和下划线,且以字母开头'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  name: [
    { required: true, message: '请输入字典名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  category: [
    { required: true, message: '请选择字典类别', trigger: 'change' }
  ],
  description: [
    { max: 200, message: '最多 200 个字符', trigger: 'blur' }
  ]
}

// 验证扩展属性JSON格式
function validateExtProps() {
  if (!extPropsJson.value.trim()) {
    formData.value.extProps = {}
    extPropsError.value = ''
    return
  }

  try {
    formData.value.extProps = JSON.parse(extPropsJson.value)
    extPropsError.value = ''
  } catch (error) {
    extPropsError.value = '扩展属性格式不正确,请输入有效的JSON'
  }
}

// 监听字典类型变化,初始化表单
watch(
  () => props.dictType,
  (val) => {
    if (val) {
      // 编辑模式
      formData.value = {
        code: val.code,
        name: val.name,
        description: val.description,
        category: val.category,
        status: val.status,
        sortOrder: val.sortOrder || 0,
        extProps: val.extProps || {}
      }
      originalCode.value = val.code
      extPropsJson.value = val.extProps
        ? JSON.stringify(val.extProps, null, 2)
        : ''
    } else {
      // 新增模式
      formData.value = {
        code: '',
        name: '',
        description: '',
        category: 'business',
        status: 'enabled',
        sortOrder: 0,
        extProps: {}
      }
      originalCode.value = ''
      extPropsJson.value = ''
    }
    extPropsError.value = ''
  },
  { immediate: true }
)

// 提交表单
async function handleSubmit() {
  if (!formRef.value) return

  try {
    await formRef.value.validate()

    // 验证扩展属性
    validateExtProps()
    if (extPropsError.value) {
      ElMessage.error(extPropsError.value)
      return
    }

    // 验证编码唯一性
    if (formData.value.code !== originalCode.value) {
      const exists = await dictStore.checkDictCodeExists(
        formData.value.code,
        isEdit.value ? props.dictType?.id : undefined
      )
      if (exists) {
        ElMessage.error('该字典编码已存在')
        return
      }
    }

    submitting.value = true

    // 准备提交数据，将 extProps 对象转换为 JSON 字符串
    const submitData = {
      ...formData.value,
      extProps: stringifyExtProps(formData.value.extProps)
    }

    if (isEdit.value && props.dictType) {
      await dictStore.updateDictType(props.dictType.id, submitData)
      ElMessage.success('更新成功')
    } else {
      await dictStore.createDictType(submitData)
      ElMessage.success('创建成功')
    }

    emit('success')
    handleClose()
  } catch (error) {
    console.error('表单验证失败:', error)
  } finally {
    submitting.value = false
  }
}

// 关闭对话框
function handleClose() {
  visible.value = false
  formRef.value?.resetFields()
  extPropsJson.value = ''
  extPropsError.value = ''
}
</script>

<style scoped lang="scss">
.dict-type-form {
  .error-text {
    color: var(--el-color-danger);
    font-size: 12px;
    margin-top: 4px;
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>
