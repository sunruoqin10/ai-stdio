<template>
  <el-dialog
    v-model="visible"
    :title="isEdit ? '编辑报销单' : '新建报销单'"
    width="900px"
    @close="handleClose"
    :close-on-click-modal="false"
  >
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="120px"
      class="expense-form"
    >
      <!-- 基本信息 -->
      <el-divider content-position="left">
        <span class="section-title">第一步: 基本信息</span>
      </el-divider>

      <el-form-item label="申请人">
        <span>{{ currentUser.name }} ({{ currentUser.department }})</span>
      </el-form-item>

      <el-form-item label="报销类型" prop="type">
        <el-select
          v-model="form.type"
          placeholder="请选择报销类型"
          style="width: 100%"
        >
          <el-option label="差旅费" value="travel" />
          <el-option label="招待费" value="entertainment" />
          <el-option label="办公用品" value="office" />
          <el-option label="交通费" value="transport" />
          <el-option label="通信费" value="communication" />
          <el-option label="其他" value="other" />
        </el-select>
      </el-form-item>

      <el-form-item label="费用发生日期" prop="expenseDate">
        <el-date-picker
          v-model="form.expenseDate"
          type="date"
          placeholder="选择费用发生日期"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
          style="width: 100%"
        />
      </el-form-item>

      <el-form-item label="报销事由" prop="reason">
        <el-input
          v-model="form.reason"
          type="textarea"
          :rows="3"
          placeholder="请详细说明报销事由(至少10个字)"
          maxlength="500"
          show-word-limit
        />
      </el-form-item>

      <!-- 费用明细 -->
      <el-divider content-position="left">
        <span class="section-title">第二步: 费用明细</span>
      </el-divider>

      <div class="expense-items-container">
        <el-table
          :data="form.items"
          border
          style="width: 100%"
          :header-cell-style="{ background: '#F5F7FA', color: '#606266' }"
        >
          <el-table-column type="index" label="序号" width="60" align="center" />
          <el-table-column label="费用说明" min-width="180">
            <template #default="{ row }">
              <el-input
                v-model="row.description"
                placeholder="请输入费用说明"
                size="small"
              />
            </template>
          </el-table-column>
          <el-table-column label="金额" width="150">
            <template #default="{ row }">
              <el-input-number
                v-model="row.amount"
                :precision="2"
                :min="0"
                :controls="false"
                placeholder="0.00"
                style="width: 100%"
                size="small"
                @change="calculateTotal"
              />
            </template>
          </el-table-column>
          <el-table-column label="日期" width="160">
            <template #default="{ row }">
              <el-date-picker
                v-model="row.date"
                type="date"
                placeholder="选择日期"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
                style="width: 100%"
                size="small"
              />
            </template>
          </el-table-column>
          <el-table-column label="分类" width="130">
            <template #default="{ row }">
              <el-select v-model="row.category" placeholder="选择" size="small">
                <el-option label="交通费" value="transport" />
                <el-option label="住宿费" value="hotel" />
                <el-option label="餐费" value="meal" />
                <el-option label="办公费" value="office" />
                <el-option label="其他" value="other" />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="80" align="center" fixed="right">
            <template #default="{ $index }">
              <el-button
                type="danger"
                link
                size="small"
                @click="removeItem($index)"
              >
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="subtotal-row">
          <span>费用明细小计:</span>
          <span class="amount">¥{{ itemsTotal.toFixed(2) }}</span>
        </div>

        <el-button
          type="primary"
          link
          :icon="Plus"
          @click="addItem"
          class="add-item-btn"
        >
          + 添加明细
        </el-button>
      </div>

      <!-- 发票管理 -->
      <el-divider content-position="left">
        <span class="section-title">第三步: 发票上传</span>
      </el-divider>

      <div class="invoice-container">
        <div
          v-for="(invoice, index) in form.invoices"
          :key="invoice.id"
          class="invoice-item"
        >
          <div class="invoice-header">
            <span class="invoice-title">发票 {{ index + 1 }}</span>
            <el-button
              type="danger"
              link
              size="small"
              @click="removeInvoice(index)"
            >
              删除
            </el-button>
          </div>

          <el-form
            :model="invoice"
            :ref="(el: any) => setInvoiceFormRef(el, index)"
            label-width="100px"
            class="invoice-form"
          >
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="发票图片">
                  <el-upload
                    :action="uploadUrl"
                    :on-success="(response, file) => handleUploadSuccess(response, file, index)"
                    :on-remove="(file) => handleUploadRemove(file, index)"
                    :on-preview="handlePreview"
                    :before-upload="beforeUpload"
                    list-type="picture-card"
                    :limit="1"
                    :file-list="invoice.fileList"
                    accept=".jpg,.jpeg,.png,.pdf"
                  >
                    <el-icon><Plus /></el-icon>
                  </el-upload>
                  <div class="upload-tip">支持JPG/PNG/PDF,最大5MB</div>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="发票类型" :prop="`type`" :rules="invoiceRules.type">
                  <el-select
                    v-model="invoice.type"
                    placeholder="请选择发票类型"
                    style="width: 100%"
                  >
                    <el-option label="增值税专用发票" value="vat_special" />
                    <el-option label="增值税普通发票" value="vat_common" />
                    <el-option label="电子发票" value="electronic" />
                    <el-option label="其他" value="other" />
                  </el-select>
                </el-form-item>

                <el-form-item label="发票号码" :prop="`number`" :rules="invoiceRules.number">
                  <el-input
                    v-model="invoice.number"
                    placeholder="请输入发票号码"
                  >
                    <template #append>
                      <el-button
                        :icon="View"
                        :loading="invoice.ocrLoading"
                        @click="handleOCR(invoice)"
                      >
                        OCR识别
                      </el-button>
                    </template>
                  </el-input>
                </el-form-item>

                <el-form-item label="发票金额" :prop="`amount`" :rules="invoiceRules.amount">
                  <el-input-number
                    v-model="invoice.amount"
                    :precision="2"
                    :min="0"
                    :controls="false"
                    placeholder="0.00"
                    style="width: 100%"
                    @change="calculateTotal"
                  />
                </el-form-item>

                <el-form-item label="开票日期" :prop="`date`" :rules="invoiceRules.date">
                  <el-date-picker
                    v-model="invoice.date"
                    type="date"
                    placeholder="选择开票日期"
                    format="YYYY-MM-DD"
                    value-format="YYYY-MM-DD"
                    style="width: 100%"
                  />
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
        </div>

        <div class="invoice-total-row">
          <span>发票总金额:</span>
          <span class="amount">¥{{ invoiceTotal.toFixed(2) }}</span>
        </div>

        <el-button
          type="primary"
          link
          :icon="Plus"
          @click="addInvoice"
          class="add-invoice-btn"
        >
          + 上传发票
        </el-button>
      </div>

      <!-- 总计 -->
      <div class="total-section">
        <el-alert
          :type="totalMatch ? 'success' : 'warning'"
          :closable="false"
          show-icon
        >
          <template #title>
            <div class="total-amount-display">
              <div class="total-row">
                <span>报销总金额:</span>
                <span class="amount total">¥{{ itemsTotal.toFixed(2) }}</span>
              </div>
              <div v-if="!totalMatch" class="warning-text">
                发票总金额(¥{{ invoiceTotal.toFixed(2) }})与费用明细不一致
              </div>
            </div>
          </template>
        </el-alert>
      </div>
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button
          type="warning"
          :icon="Document"
          :loading="loading"
          @click="handleSaveDraft"
        >
          保存草稿
        </el-button>
        <el-button
          type="primary"
          :icon="Check"
          :loading="loading"
          @click="handleSubmit"
        >
          提交申请
        </el-button>
      </div>
    </template>

    <!-- 图片预览对话框 -->
    <el-dialog v-model="previewVisible" title="发票预览" width="600px">
      <img :src="previewUrl" alt="发票预览" style="width: 100%" />
    </el-dialog>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, watch, computed, nextTick } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules, type UploadUserFile, type UploadProps, type UploadFile } from 'element-plus'
import { Plus, Document, Check, View } from '@element-plus/icons-vue'
import type { ExpenseForm, ExpenseItem, Invoice } from '../types'
import { useExpenseStore } from '../store'

const expenseStore = useExpenseStore()

interface Props {
  modelValue: boolean
  expenseData?: any
}

interface User {
  id: string
  name: string
  department: string
}

const props = withDefaults(defineProps<Props>(), {
  expenseData: null
})

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  'success': []
}>()

const formRef = ref<FormInstance>()
const invoiceFormRefs = ref<(FormInstance | null)[]>([])
const loading = ref(false)
const visible = ref(false)
const uploadUrl = '/api/upload'
const previewVisible = ref(false)
const previewUrl = ref('')

// 模拟当前用户
const currentUser = ref<User>({
  id: 'EMP000001',
  name: '张三',
  department: '技术部'
})

const form = reactive<ExpenseForm>({
  type: '',
  expenseDate: '',
  reason: '',
  items: [],
  invoices: [],
  version: undefined
})

// 费用明细验证规则
const itemRules = {
  description: [
    { required: true, message: '请输入费用说明', trigger: 'blur' },
    { min: 2, max: 100, message: '长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  amount: [
    { required: true, message: '请输入金额', trigger: 'blur' },
    { type: 'number', min: 0.01, message: '金额必须大于0', trigger: 'blur' }
  ],
  date: [
    { required: true, message: '请选择日期', trigger: 'change' }
  ],
  category: [
    { required: true, message: '请选择分类', trigger: 'change' }
  ]
}

// 发票验证规则
const invoiceRules = {
  type: [{ required: true, message: '请选择发票类型', trigger: 'change' }],
  number: [
    { required: true, message: '请输入发票号码', trigger: 'blur' },
    { pattern: /^\d{8}$|^\d{20}$/, message: '发票号码格式不正确(8位或20位数字)', trigger: 'blur' }
  ],
  amount: [
    { required: true, message: '请输入发票金额', trigger: 'blur' },
    { type: 'number', min: 0.01, message: '金额必须大于0', trigger: 'blur' }
  ],
  date: [
    { required: true, message: '请选择开票日期', trigger: 'change' }
  ]
}

// 表单验证规则
const rules: FormRules = {
  type: [{ required: true, message: '请选择报销类型', trigger: 'change' }],
  expenseDate: [{ required: true, message: '请选择费用发生日期', trigger: 'change' }],
  reason: [
    { required: true, message: '请输入报销事由', trigger: 'blur' },
    { min: 10, max: 500, message: '长度在 10 到 500 个字符', trigger: 'blur' }
  ]
}

const isEdit = computed(() => !!props.expenseData)

// 计算费用明细小计
const itemsTotal = computed(() => {
  return form.items.reduce((sum, item) => sum + (item.amount || 0), 0)
})

// 计算发票总金额
const invoiceTotal = computed(() => {
  return form.invoices.reduce((sum, invoice) => sum + (invoice.amount || 0), 0)
})

// 检查金额是否匹配
const totalMatch = computed(() => {
  const tolerance = 0.01 // 允许0.01元的误差
  return Math.abs(itemsTotal.value - invoiceTotal.value) <= tolerance
})

watch(
  () => props.modelValue,
  (val) => {
    visible.value = val
    if (val && props.expenseData) {
      // 编辑模式,填充表单
      Object.assign(form, {
        type: props.expenseData.type || '',
        expenseDate: props.expenseData.expenseDate || '',
        reason: props.expenseData.reason || '',
        items: props.expenseData.items || [],
        version: props.expenseData.version
      })
      
      // 处理发票数据，添加fileList字段
      form.invoices = (props.expenseData.invoices || []).map((invoice: any) => ({
        ...invoice,
        fileList: invoice.imageUrl ? [{
          uid: `invoice_${invoice.id || Date.now()}`,
          name: `invoice_${invoice.id || Date.now()}`,
          url: invoice.imageUrl,
          status: 'success'
        }] : []
      }))
    } else if (val) {
      // 新建模式,初始化表单
      initForm()
    }
  }
)

watch(visible, (val) => {
  emit('update:modelValue', val)
})

// 初始化表单
function initForm() {
  Object.assign(form, {
    type: '',
    expenseDate: '',
    reason: '',
    items: [],
    invoices: [],
    version: undefined
  })
  // 默认添加一条明细
  addItem()
  formRef.value?.clearValidate()
}

// 设置发票表单ref
function setInvoiceFormRef(el: any, index: number) {
  if (el) {
    invoiceFormRefs.value[index] = el
  }
}

// 添加费用明细
function addItem() {
  const newItem: ExpenseItem = {
    id: Date.now() + Math.random(),
    description: '',
    amount: 0,
    date: form.expenseDate || '',
    category: ''
  }
  form.items.push(newItem)
}

// 删除费用明细
function removeItem(index: number) {
  if (form.items.length <= 1) {
    ElMessage.warning('至少保留一条费用明细')
    return
  }
  form.items.splice(index, 1)
  calculateTotal()
}

// 添加发票
function addInvoice() {
  const newInvoice: Invoice = {
    id: Date.now() + Math.random(),
    type: '',
    number: '',
    amount: 0,
    date: form.expenseDate || '',
    imageUrl: '',
    fileList: [],
    ocrLoading: false
  }
  form.invoices.push(newInvoice)
}

// 删除发票
function removeInvoice(index: number) {
  form.invoices.splice(index, 1)
  calculateTotal()
}

// 计算总金额
function calculateTotal() {
  // 总金额通过computed自动计算,这里可以添加额外的逻辑
}

// 上传前验证
const beforeUpload: UploadProps['beforeUpload'] = (file) => {
  const isLt5M = file.size / 1024 / 1024 < 5
  const isImage = file.type === 'image/jpeg' || file.type === 'image/png' || file.type === 'application/pdf'

  if (!isImage) {
    ElMessage.error('只能上传JPG/PNG/PDF格式的文件')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('上传文件大小不能超过5MB')
    return false
  }
  return true
}

// 上传成功回调
function handleUploadSuccess(response: any, file: UploadFile, index: number) {
  if (response.code === 0 && (response.data?.url || response.url)) {
    form.invoices[index].imageUrl = response.data?.url || response.url
    form.invoices[index].fileList = [file]
    ElMessage.success('上传成功')
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}

// 删除文件
function handleUploadRemove(file: UploadFile, index: number) {
  form.invoices[index].imageUrl = ''
  form.invoices[index].fileList = []
}

// 预览图片
function handlePreview(file: UploadFile) {
  previewUrl.value = file.url || (file.response?.data?.url) || ''
  if (previewUrl.value) {
    previewVisible.value = true
  } else {
    ElMessage.warning('预览失败，无法获取图片URL')
  }
}

// OCR识别
async function handleOCR(invoice: Invoice) {
  if (!invoice.imageUrl) {
    ElMessage.warning('请先上传发票图片')
    return
  }

  invoice.ocrLoading = true
  try {
    // 调用实际的OCR API
    const ocrInvoice = await import('../api').then(m => m.ocrInvoice)
    const result = await ocrInvoice(invoice.imageUrl)

    // 填充识别结果
    if (result.type) invoice.type = result.type
    if (result.number) invoice.number = result.number
    if (result.amount) invoice.amount = result.amount
    if (result.date) invoice.date = result.date

    ElMessage.success('OCR识别成功')
  } catch (error: any) {
    ElMessage.error(error.message || 'OCR识别失败,请手动输入')
  } finally {
    invoice.ocrLoading = false
  }
}

// 验证发票表单
async function validateInvoiceForms(): Promise<boolean> {
  if (form.invoices.length === 0) {
    ElMessage.warning('请至少上传一张发票')
    return false
  }

  for (let i = 0; i < invoiceFormRefs.value.length; i++) {
    const formRef = invoiceFormRefs.value[i]
    if (formRef) {
      try {
        await formRef.validate()
      } catch (error) {
        ElMessage.error(`请完善第${i + 1}张发票的信息`)
        return false
      }
    }
  }

  return true
}

// 转换表单数据为后端期望的格式
function transformFormData(formData: any) {
  // 转换发票字段
  const transformedInvoices = formData.invoices.map((invoice: any) => ({
    invoiceType: invoice.type,
    invoiceNumber: invoice.number,
    amount: invoice.amount,
    invoiceDate: invoice.date,
    imageUrl: invoice.imageUrl
  }))

  // 转换费用明细字段
  const transformedItems = formData.items.map((item: any) => ({
    description: item.description,
    amount: item.amount,
    expenseDate: item.date,
    category: item.category
  }))

  return {
    type: formData.type,
    reason: formData.reason,
    expenseDate: formData.expenseDate,
    items: transformedItems,
    invoices: transformedInvoices,
    version: formData.version  // 包含版本号用于乐观锁
  }
}

// 保存草稿
async function handleSaveDraft() {
  if (!formRef.value) return

  // 验证基本信息
  const basicValid = await formRef.value.validate().catch(() => false)
  if (!basicValid) {
    ElMessage.warning('请完善基本信息')
    return
  }

  // 验证费用明细
  if (form.items.length === 0) {
    ElMessage.warning('请至少添加一条费用明细')
    return
  }

  for (let i = 0; i < form.items.length; i++) {
    const item = form.items[i]
    if (!item.description || !item.amount || !item.date || !item.category) {
      ElMessage.warning(`请完善第${i + 1}条费用明细`)
      return
    }
  }

  loading.value = true
  try {
    const transformedData = transformFormData(form)
    console.log('=== 前端发送的数据 ===')
    console.log('isEdit:', isEdit.value)
    console.log('expenseData.id:', props.expenseData?.id)
    console.log('form.version:', form.version)
    console.log('transformedData:', transformedData)
    console.log('====================')

    if (isEdit.value) {
      // 编辑模式
      await expenseStore.updateExpense(props.expenseData.id, transformedData)
    } else {
      // 新建模式
      await expenseStore.createExpense(transformedData)
    }

    ElMessage.success('草稿保存成功')
    emit('success')
    handleClose()
  } catch (error: any) {
    ElMessage.error(error.message || '保存失败')
  } finally {
    loading.value = false
  }
}

// 提交申请
async function handleSubmit() {
  if (!formRef.value) return

  // 验证基本信息
  const basicValid = await formRef.value.validate().catch(() => false)
  if (!basicValid) {
    return
  }

  // 验证费用明细
  if (form.items.length === 0) {
    ElMessage.warning('请至少添加一条费用明细')
    return
  }

  for (let i = 0; i < form.items.length; i++) {
    const item = form.items[i]
    if (!item.description || !item.amount || !item.date || !item.category) {
      ElMessage.warning(`请完善第${i + 1}条费用明细`)
      return
    }
  }

  // 验证发票
  const invoiceValid = await validateInvoiceForms()
  if (!invoiceValid) {
    return
  }

  // 验证金额一致性
  if (!totalMatch.value) {
    try {
      await ElMessageBox.confirm(
        '发票总金额与费用明细不一致,是否继续提交?',
        '金额差异提示',
        {
          confirmButtonText: '继续提交',
          cancelButtonText: '返回修改',
          type: 'warning'
        }
      )
    } catch {
      return
    }
  }

  loading.value = true
  try {
    const transformedData = transformFormData(form)
    if (isEdit.value) {
      // 编辑模式
      await expenseStore.updateExpense(props.expenseData.id, transformedData)
      await expenseStore.submitExpense(props.expenseData.id)
    } else {
      // 新建模式
      const newExpense = await expenseStore.createExpense(transformedData)
      await expenseStore.submitExpense(newExpense.id)
    }

    ElMessage.success('提交成功')
    emit('success')
    handleClose()
  } catch (error: any) {
    ElMessage.error(error.message || '提交失败')
  } finally {
    loading.value = false
  }
}

// 关闭对话框
function handleClose() {
  visible.value = false
  emit('update:modelValue', false)
}
</script>

<style scoped lang="scss">
.expense-form {
  .section-title {
    font-size: 16px;
    font-weight: 500;
    color: #303133;
  }

  .expense-items-container {
    .subtotal-row {
      margin-top: 16px;
      padding: 12px 16px;
      background: #F5F7FA;
      border-radius: 4px;
      display: flex;
      justify-content: space-between;
      align-items: center;
      font-size: 14px;
      color: #606266;

      .amount {
        font-size: 18px;
        font-weight: bold;
        color: #F56C6C;
      }
    }

    .add-item-btn {
      margin-top: 12px;
    }
  }

  .invoice-container {
    .invoice-item {
      margin-bottom: 20px;
      padding: 16px;
      background: #F5F7FA;
      border-radius: 8px;
      border: 1px solid #DCDFE6;

      &:last-child {
        margin-bottom: 0;
      }

      .invoice-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 16px;
        padding-bottom: 12px;
        border-bottom: 1px solid #DCDFE6;

        .invoice-title {
          font-size: 14px;
          font-weight: 500;
          color: #303133;
        }
      }

      .invoice-form {
        .upload-tip {
          font-size: 12px;
          color: #909399;
          margin-top: 4px;
          line-height: 1.5;
        }
      }
    }

    .invoice-total-row {
      margin-top: 16px;
      padding: 12px 16px;
      background: #E6F7FF;
      border: 1px solid #91D5FF;
      border-radius: 4px;
      display: flex;
      justify-content: space-between;
      align-items: center;
      font-size: 14px;
      color: #606266;

      .amount {
        font-size: 18px;
        font-weight: bold;
        color: #409EFF;
      }
    }

    .add-invoice-btn {
      margin-top: 12px;
    }
  }

  .total-section {
    margin-top: 20px;

    .total-amount-display {
      .total-row {
        display: flex;
        justify-content: space-between;
        align-items: center;
        font-size: 16px;
        font-weight: 500;

        .amount {
          font-size: 24px;
          font-weight: bold;
          color: #F56C6C;

          &.total {
            color: #F56C6C;
          }
        }
      }

      .warning-text {
        margin-top: 8px;
        font-size: 13px;
        color: #E6A23C;
        font-weight: normal;
      }
    }
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

// Element Plus样式覆盖
:deep(.el-table) {
  .el-input-number {
    .el-input__inner {
      text-align: left;
    }
  }

  .el-input,
  .el-select {
    width: 100%;
  }
}

:deep(.el-upload--picture-card) {
  width: 100px;
  height: 100px;
}

:deep(.el-upload-list--picture-card) {
  .el-upload-list__item {
    width: 100px;
    height: 100px;
  }
}

:deep(.el-divider__text) {
  background-color: transparent;
}

:deep(.el-alert) {
  .el-alert__title {
    line-height: 1.5;
  }
}
</style>
