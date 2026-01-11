<template>
  <el-dialog
    v-model="visible"
    :title="isEdit ? '编辑资产' : '新增资产'"
    width="600px"
    @close="handleClose"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
      <el-form-item label="资产名称" prop="name">
        <el-input v-model="form.name" placeholder="请输入资产名称" />
      </el-form-item>

      <el-form-item label="资产类别" prop="category">
        <el-select v-model="form.category" placeholder="请选择资产类别" style="width: 100%">
          <el-option label="电子设备" value="electronic" />
          <el-option label="办公家具" value="furniture" />
          <el-option label="图书资料" value="book" />
          <el-option label="其他" value="other" />
        </el-select>
      </el-form-item>

      <el-form-item label="品牌/型号" prop="brandModel">
        <el-input v-model="form.brandModel" placeholder="请输入品牌/型号" />
      </el-form-item>

      <el-form-item label="购置日期" prop="purchaseDate">
        <el-date-picker
          v-model="form.purchaseDate"
          type="date"
          placeholder="选择日期"
          style="width: 100%"
          value-format="YYYY-MM-DD"
        />
      </el-form-item>

      <el-form-item label="购置金额" prop="purchasePrice">
        <el-input-number v-model="form.purchasePrice" :min="0" :precision="2" style="width: 100%" />
      </el-form-item>

      <el-form-item label="存放位置" prop="location">
        <el-input v-model="form.location" placeholder="请输入存放位置" />
      </el-form-item>

      <el-form-item label="备注" prop="notes">
        <el-input v-model="form.notes" type="textarea" :rows="3" placeholder="请输入备注" />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" @click="handleSubmit" :loading="loading">
        确定
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { useAssetStore } from '../store'
import type { Asset, AssetForm } from '../types'

interface Props {
  modelValue: boolean
  asset?: Asset | null
}

const props = withDefaults(defineProps<Props>(), {
  asset: null
})

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  'success': []
}>()

const assetStore = useAssetStore()
const formRef = ref<FormInstance>()
const loading = ref(false)
const visible = ref(false)

const isEdit = computed(() => !!props.asset)

const form = reactive<AssetForm>({
  name: '',
  category: 'electronic',
  brandModel: '',
  purchaseDate: '',
  purchasePrice: 0,
  location: '',
  notes: ''
})

const rules: FormRules = {
  name: [{ required: true, message: '请输入资产名称', trigger: 'blur' }],
  category: [{ required: true, message: '请选择资产类别', trigger: 'change' }],
  purchaseDate: [{ required: true, message: '请选择购置日期', trigger: 'change' }],
  purchasePrice: [{ required: true, message: '请输入购置金额', trigger: 'blur' }]
}

watch(
  () => props.modelValue,
  (val) => {
    visible.value = val
    if (val && props.asset) {
      Object.assign(form, {
        name: props.asset.name,
        category: props.asset.category,
        brandModel: props.asset.brandModel,
        purchaseDate: props.asset.purchaseDate.split('T')[0],
        purchasePrice: props.asset.purchasePrice,
        location: props.asset.location,
        notes: props.asset.notes
      })
    }
  },
  { immediate: true }
)

watch(visible, (val) => {
  emit('update:modelValue', val)
})

async function handleSubmit() {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    loading.value = true
    try {
      if (isEdit.value && props.asset) {
        await assetStore.update(props.asset.id, form)
        ElMessage.success('更新成功')
      } else {
        await assetStore.create(form)
        ElMessage.success('创建成功')
      }
      emit('success')
      handleClose()
    } catch (error: any) {
      ElMessage.error(error.message || '操作失败')
    } finally {
      loading.value = false
    }
  })
}

function handleClose() {
  formRef.value?.resetFields()
  emit('update:modelValue', false)
}
</script>
