<template>
  <el-dialog
    :model-value="props.modelValue"
    :title="isEdit ? '编辑资产' : '新增资产'"
    width="600px"
    @update:model-value="handleDialogVisibleChange"
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

      <el-form-item label="资产状态" prop="status">
        <el-select v-model="form.status" placeholder="请选择资产状态" style="width: 100%">
          <el-option
            v-for="option in statusOptions"
            :key="option.value"
            :label="option.label"
            :value="option.value"
          />
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

      <el-form-item label="资产图片" prop="images">
        <el-upload
          v-model:file-list="fileList"
          :action="uploadAction"
          :http-request="handlePreviewUpload"
          list-type="picture-card"
          :on-preview="handlePicturePreview"
          :on-remove="handleRemove"
          :before-upload="beforeUpload"
          :limit="10"
          accept="image/*"
        >
          <el-icon><Plus /></el-icon>
        </el-upload>
        <div class="upload-tip">最多上传10张图片，支持jpg、png格式</div>
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

  <!-- 图片预览对话框 -->
  <el-dialog v-model="previewVisible" title="图片预览" width="800px">
    <img :src="previewUrl" style="width: 100%" />
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import type { FormInstance, FormRules, UploadUserFile, UploadRequestOptions } from 'element-plus'
import { useAssetStore } from '../store'
import { uploadFile } from '../api'
import { getImageUrl } from '../utils'
import type { Asset, AssetForm } from '../types'
import { useDictStore } from '@/modules/dict/store'

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
const dictStore = useDictStore()
const formRef = ref<FormInstance>()
const loading = ref(false)

const statusOptions = computed(() => {
  return dictStore.dictItems
    .filter(item => item.dictTypeCode === 'asset_status' && item.status === 'enabled')
    .map(item => ({
      label: item.label,
      value: item.value
    }))
})

// 图片上传相关
const fileList = ref<UploadUserFile[]>([])
const uploadAction = '/api/upload'
const previewVisible = ref(false)
const previewUrl = ref('')

// 处理对话框显示状态变化
const handleDialogVisibleChange = (val: boolean) => {
  emit('update:modelValue', val)
}

onMounted(async () => {
  try {
    await dictStore.fetchDictItems({ dictTypeCode: 'asset_status', page: 1, pageSize: 100 })
  } catch (error) {
    console.error('加载资产状态字典失败:', error)
  }
})

const isEdit = computed(() => !!props.asset)

const form = reactive<AssetForm>({
  name: '',
  category: 'electronic',
  status: 'stock',
  brandModel: '',
  purchaseDate: '',
  purchasePrice: 0,
  location: '',
  images: [],
  notes: ''
})

const rules: FormRules = {
  name: [{ required: true, message: '请输入资产名称', trigger: 'blur' }],
  category: [{ required: true, message: '请选择资产类别', trigger: 'change' }],
  purchaseDate: [{ required: true, message: '请选择购置日期', trigger: 'change' }],
  purchasePrice: [{ required: true, message: '请输入购置金额', trigger: 'blur' }]
}

// 上传前校验
const beforeUpload = (file: File) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!')
    return false
  }
  return true
}

// 预览上传 - 仅生成预览URL，不实际上传
const handlePreviewUpload = async (options: UploadRequestOptions) => {
  const file = options.file as File
  const url = URL.createObjectURL(file)
  options.onSuccess?.(url)
}

// 移除图片
const handleRemove = (file: UploadUserFile) => {
  const index = fileList.value.findIndex(f => f.uid === file.uid)
  if (index > -1) {
    fileList.value.splice(index, 1)
  }
}

// 预览图片
const handlePicturePreview = (file: UploadUserFile) => {
  previewUrl.value = file.url ? getImageUrl(file.url) : ''
  previewVisible.value = true
}

// 上传所有图片文件
const uploadAllImages = async () => {
  const uploadedUrls: string[] = []
  const filesToUpload = fileList.value.filter(file => file.raw)
  
  for (const file of filesToUpload) {
    try {
      const url = await uploadFile(file.raw as File)
      uploadedUrls.push(url)
    } catch (error: any) {
      throw new Error(`图片 ${file.name} 上传失败: ${error.message}`)
    }
  }
  
  return uploadedUrls
}

watch(
  () => props.modelValue,
  (val) => {
    if (val && props.asset) {
      // 处理 images 字段 - 可能是 JSON 字符串或数组
      let images: string[] = []
      if (props.asset.images) {
        if (typeof props.asset.images === 'string') {
          try {
            images = JSON.parse(props.asset.images)
          } catch {
            images = []
          }
        } else if (Array.isArray(props.asset.images)) {
          images = props.asset.images
        }
      }

      Object.assign(form, {
        id: props.asset.id,
        version: props.asset.version,
        name: props.asset.name,
        category: props.asset.category,
        status: props.asset.status,
        brandModel: props.asset.brandModel,
        purchaseDate: props.asset.purchaseDate.split('T')[0],
        purchasePrice: props.asset.purchasePrice,
        location: props.asset.location,
        images: images,
        notes: props.asset.notes
      })

      // 设置文件列表
      if (images && images.length > 0) {
        fileList.value = images.map((url, index) => ({
          name: `image-${index}`,
          url: getImageUrl(url),
          uid: Date.now() + index
        }))
      } else {
        fileList.value = []
      }
    } else if (val) {
      // 新增时清空图片列表
      fileList.value = []
      form.images = []
      delete form.id
      delete form.version
    }
  },
  { immediate: true }
)

async function handleSubmit() {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    loading.value = true
    try {
      // 上传所有新图片
      const newImageUrls = await uploadAllImages()
      
      // 合并已存在的图片URL和新上传的图片URL
      const existingUrls = fileList.value
        .filter(file => !file.raw && file.url)
        .map(file => {
          // 如果是新上传的本地URL (blob: 开头)，不包含在existingUrls中
          if (file.url?.startsWith('blob:')) {
            return null
          }
          // 将完整URL转换回存储的格式
          const url = file.url || ''
          if (url.startsWith('/api/uploads/')) {
            return url.substring(4)
          }
          return url.startsWith('/uploads/') ? url : null
        })
        .filter((url): url is string => url !== null)
      
      form.images = [...existingUrls, ...newImageUrls]

      if (isEdit.value) {
        await assetStore.update(form.id!, form)
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
  fileList.value = []
  form.images = []
  emit('update:modelValue', false)
}
</script>

<style scoped>
.upload-tip {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}
</style>
