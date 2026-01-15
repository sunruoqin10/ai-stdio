<template>
  <el-dialog
    v-model="visible"
    :title="isEdit ? '编辑字典项' : '新增字典项'"
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
      class="dict-item-form"
    >
      <el-form-item label="所属字典类型" prop="dictTypeCode">
        <el-input
          v-if="fixedDictType"
          :value="`${fixedDictType.name} (${fixedDictType.code})`"
          disabled
          style="width: 100%"
        />
        <el-select
          v-else
          v-model="formData.dictTypeCode"
          placeholder="请选择字典类型"
          :disabled="isEdit"
          style="width: 100%"
        >
          <el-option
            v-for="dictType in dictTypes"
            :key="dictType.code"
            :label="`${dictType.name} (${dictType.code})`"
            :value="dictType.code"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="项标签" prop="label">
        <el-input
          v-model="formData.label"
          placeholder="如: 在职"
        />
      </el-form-item>

      <el-form-item label="项值" prop="value">
        <el-input
          v-model="formData.value"
          placeholder="如: active"
        >
          <template #suffix>
            <el-tooltip content="在同一字典类型下必须唯一" placement="top">
              <el-icon><QuestionFilled /></el-icon>
            </el-tooltip>
          </template>
        </el-input>
      </el-form-item>

      <el-form-item label="颜色类型" prop="colorType" required>
        <el-radio-group v-model="formData.colorType" class="color-type-group">
          <el-radio label="primary">
            <span class="color-preview color-primary">Primary</span>
          </el-radio>
          <el-radio label="success">
            <span class="color-preview color-success">Success</span>
          </el-radio>
          <el-radio label="warning">
            <span class="color-preview color-warning">Warning</span>
          </el-radio>
          <el-radio label="danger">
            <span class="color-preview color-danger">Danger</span>
          </el-radio>
          <el-radio label="info">
            <span class="color-preview color-info">Info</span>
          </el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item label="预览效果">
        <DictColorTag
          :label="formData.label || '标签'"
          :color-type="formData.colorType"
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

      <el-form-item label="状态" prop="status">
        <el-radio-group v-model="formData.status">
          <el-radio label="enabled">启用</el-radio>
          <el-radio label="disabled">禁用</el-radio>
        </el-radio-group>
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
import DictColorTag from './DictColorTag.vue'
import type { DictItem, DictItemForm, DictType } from '../types'
import { useDictStore } from '../store'
import { generateSortOrder, stringifyExtProps } from '../utils'

interface Props {
  modelValue: boolean
  dictItem?: DictItem | null
  dictType?: DictType | null // 固定的字典类型(新增时使用)
}

const props = withDefaults(defineProps<Props>(), {
  dictItem: null,
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

const isEdit = computed(() => !!props.dictItem)

// 是否固定字典类型
const fixedDictType = computed(() => props.dictType)

// 获取字典类型列表
const dictTypes = computed(() => dictStore.dictTypes)

// 监听对话框打开，确保字典类型列表已加载
watch(visible, async (newVal) => {
  if (newVal && !fixedDictType.value && dictTypes.value.length === 0) {
    await dictStore.fetchDictTypes({ page: 1, pageSize: 100 })
    // 加载完成后,如果当前是新增模式且dictTypeCode为空,设置第一个字典类型
    if (!props.dictItem && !formData.value.dictTypeCode && dictTypes.value.length > 0) {
      formData.value.dictTypeCode = dictTypes.value[0].code
    }
  }
})

// 表单数据
const formData = ref<DictItemForm>({
  dictTypeCode: '',
  label: '',
  value: '',
  colorType: 'info',
  sortOrder: 10,
  status: 'enabled',
  extProps: {}
})

// 原始值(用于编辑时验证)
const originalValue = ref('')

// 表单验证规则
const rules: FormRules = {
  dictTypeCode: [
    { required: true, message: '请选择字典类型', trigger: 'change' }
  ],
  label: [
    { required: true, message: '请输入项标签', trigger: 'blur' },
    { min: 1, max: 50, message: '长度在 1 到 50 个字符', trigger: 'blur' }
  ],
  value: [
    { required: true, message: '请输入项值', trigger: 'blur' },
    { min: 1, max: 50, message: '长度在 1 到 50 个字符', trigger: 'blur' }
  ],
  sortOrder: [
    { required: true, message: '请输入排序序号', trigger: 'blur' },
    { type: 'number', message: '排序序号必须为数字', trigger: 'blur' }
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

// 监听字典项变化,初始化表单
watch(
  () => props.dictItem,
  (val) => {
    if (val) {
      // 编辑模式
      formData.value = {
        dictTypeCode: val.dictTypeCode,
        label: val.label,
        value: val.value,
        colorType: val.colorType || 'info',
        sortOrder: val.sortOrder,
        status: val.status,
        extProps: val.extProps || {}
      }
      originalValue.value = val.value
      extPropsJson.value = val.extProps
        ? JSON.stringify(val.extProps, null, 2)
        : ''
    } else {
      // 新增模式
      let dictTypeCode = fixedDictType.value?.code || ''
      // 如果没有固定字典类型,尝试从列表中获取第一个
      if (!dictTypeCode && dictTypes.value.length > 0) {
        dictTypeCode = dictTypes.value[0].code
      }
      const existingItems = dictStore.currentDictItems
      const sortOrder = generateSortOrder(existingItems)

      formData.value = {
        dictTypeCode,
        label: '',
        value: '',
        colorType: 'info',
        sortOrder,
        status: 'enabled',
        extProps: {}
      }
      originalValue.value = ''
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

    // 获取字典类型
    let dictType: DictType | undefined
    if (fixedDictType.value) {
      // 如果有固定字典类型，直接使用
      dictType = fixedDictType.value
    } else {
      // 否则从列表中查找
      dictType = dictTypes.value.find(
        dt => dt.code === formData.value.dictTypeCode
      )
    }

    if (!dictType) {
      ElMessage.error('字典类型不存在')
      return
    }

    // 验证项值唯一性
    if (formData.value.value !== originalValue.value) {
      const exists = await dictStore.checkDictValueExists(
        dictType.id,
        formData.value.value,
        isEdit.value ? props.dictItem?.id : undefined
      )
      if (exists) {
        ElMessage.error('该字典类型下已存在相同的项值')
        return
      }
    }

    submitting.value = true

    // 准备提交数据，将 extProps 对象转换为 JSON 字符串
    const submitData = {
      ...formData.value,
      extProps: stringifyExtProps(formData.value.extProps)
    }

    if (isEdit.value && props.dictItem) {
      await dictStore.updateDictItem(props.dictItem.id, submitData)
      ElMessage.success('更新成功')
    } else {
      await dictStore.createDictItem(submitData)
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

  // 重置表单数据,确保 dictTypeCode 有默认值
  let dictTypeCode = fixedDictType.value?.code || ''
  if (!dictTypeCode && dictTypes.value.length > 0) {
    dictTypeCode = dictTypes.value[0].code
  }
  formData.value = {
    dictTypeCode,
    label: '',
    value: '',
    colorType: 'info',
    sortOrder: 10,
    status: 'enabled',
    extProps: {}
  }

  extPropsJson.value = ''
  extPropsError.value = ''
}
</script>

<style scoped lang="scss">
.dict-item-form {
  .color-type-group {
    display: flex;
    flex-wrap: wrap;
    gap: 12px;

    :deep(.el-radio) {
      margin-right: 0;

      .el-radio__label {
        padding-left: 8px;
      }
    }
  }

  .color-preview {
    display: inline-block;
    padding: 4px 12px;
    border-radius: 4px;
    color: #fff;
    font-size: 12px;
    font-weight: 500;

    &.color-primary {
      background-color: var(--el-color-primary);
    }

    &.color-success {
      background-color: var(--el-color-success);
    }

    &.color-warning {
      background-color: var(--el-color-warning);
    }

    &.color-danger {
      background-color: var(--el-color-danger);
    }

    &.color-info {
      background-color: var(--el-color-info);
    }
  }

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
