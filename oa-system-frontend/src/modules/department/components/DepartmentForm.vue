<template>
  <el-dialog
    v-model="visible"
    :title="isEdit ? '编辑部门' : '新增部门'"
    width="600px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="formData"
      :rules="rules"
      label-width="100px"
      class="department-form"
    >
      <el-form-item label="部门名称" prop="name">
        <el-input
          v-model="formData.name"
          placeholder="请输入部门名称"
          maxlength="50"
          show-word-limit
          clearable
        />
      </el-form-item>

      <el-form-item label="部门简称" prop="shortName">
        <el-input
          v-model="formData.shortName"
          placeholder="请输入部门简称"
          maxlength="20"
          show-word-limit
          clearable
        />
      </el-form-item>

      <el-form-item label="上级部门" prop="parentId">
        <el-tree-select
          v-model="formData.parentId"
          :data="departmentTree"
          :props="{ label: 'name', value: 'id' }"
          placeholder="请选择上级部门"
          check-strictly
          clearable
          filterable
        />
      </el-form-item>

      <el-form-item label="部门负责人" prop="leaderId">
        <el-select
          v-model="formData.leaderId"
          placeholder="请选择部门负责人"
          filterable
          remote
          :remote-method="searchEmployees"
          :loading="employeeLoading"
          clearable
        >
          <el-option
            v-for="emp in employeeList"
            :key="emp.id"
            :label="emp.name"
            :value="emp.id"
          >
            <span>{{ emp.name }}</span>
            <span class="text-secondary">({{ emp.departmentId }})</span>
          </el-option>
        </el-select>
      </el-form-item>

      <el-form-item label="排序号" prop="sort">
        <el-input-number
          v-model="formData.sort"
          :min="0"
          :max="999"
          placeholder="请输入排序号"
        />
      </el-form-item>

      <el-form-item label="成立时间" prop="establishedDate">
        <el-date-picker
          v-model="formData.establishedDate"
          type="date"
          placeholder="请选择成立时间"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
        />
      </el-form-item>

      <el-form-item label="部门描述" prop="description">
        <el-input
          v-model="formData.description"
          type="textarea"
          :rows="4"
          placeholder="请输入部门描述"
          maxlength="500"
          show-word-limit
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="handleSubmit">
        确定
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, watch, computed } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import type { DepartmentForm, Department } from '../types'
import { useDepartmentStore } from '../store'
import * as employeeApi from '@/modules/employee/api'

// ==================== Props ====================

interface Props {
  modelValue: boolean
  department?: Department | null
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: false,
  department: null
})

// ==================== Emits ====================

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  'success': []
}>()

// ==================== Store ====================

const departmentStore = useDepartmentStore()

// ==================== 响应式数据 ====================

const visible = ref(false)
const formRef = ref<FormInstance>()
const submitting = ref(false)
const employeeLoading = ref(false)
const employeeList = ref<any[]>([])

const isEdit = computed(() => !!props.department)

const formData = reactive<DepartmentForm>({
  name: '',
  shortName: '',
  parentId: null,
  leaderId: undefined,
  sort: 0,
  establishedDate: '',
  description: ''
})

const departmentTree = ref<Department[]>([])

// ==================== 表单验证规则 ====================

const rules: FormRules = {
  name: [
    { required: true, message: '请输入部门名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  shortName: [
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  leaderId: [
    { required: true, message: '请选择部门负责人', trigger: 'change' }
  ]
}

// ==================== 方法 ====================

/**
 * 搜索员工
 */
async function searchEmployees(keyword: string) {
  if (!keyword) {
    employeeList.value = []
    return
  }

  employeeLoading.value = true
  try {
    // 调用员工模块API搜索员工
    const result = await employeeApi.getEmployeeList({
      keyword,
      page: 1,
      pageSize: 20
    })

    // 转换数据格式
    employeeList.value = result.list.map(emp => ({
      id: emp.id,
      name: emp.name,
      departmentId: emp.departmentId
    }))
  } catch (error: any) {
    console.error('搜索员工失败:', error)
    ElMessage.error(error.message || '搜索员工失败')
    employeeList.value = []
  } finally {
    employeeLoading.value = false
  }
}

/**
 * 加载部门树
 */
async function loadDepartmentTree() {
  try {
    const tree = await departmentStore.getDepartmentTree()
    departmentTree.value = tree
  } catch (error) {
    console.error('加载部门树失败:', error)
  }
}

/**
 * 提交表单
 */
async function handleSubmit() {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
  } catch (validationError) {
    console.log('表单验证失败:', validationError)
    return
  }

  // 额外检查:确保必填字段有值
  if (!formData.leaderId) {
    ElMessage.warning('请选择部门负责人')
    return
  }

  submitting.value = true
  try {
    if (isEdit.value && props.department) {
      await departmentStore.update(props.department.id, formData)
      ElMessage.success('部门更新成功')
    } else {
      await departmentStore.create(formData)
      ElMessage.success('部门创建成功')
    }

    emit('success')
    handleClose()
  } catch (error: any) {
    console.error('操作失败:', error)
    ElMessage.error(error.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

/**
 * 关闭对话框
 */
function handleClose() {
  formRef.value?.resetFields()
  emit('update:modelValue', false)
}

/**
 * 初始化表单数据
 */
function initFormData() {
  if (props.department) {
    Object.assign(formData, {
      name: props.department.name,
      shortName: props.department.shortName,
      parentId: props.department.parentId,
      leaderId: props.department.leaderId,
      sort: props.department.sort,
      establishedDate: props.department.establishedDate,
      description: props.department.description
    })

    // 如果有负责人,加载到选择列表
    if (props.department.leaderId && props.department.leaderName) {
      employeeList.value = [{
        id: props.department.leaderId,
        name: props.department.leaderName,
        departmentId: props.department.id
      }]
    }
  } else {
    Object.assign(formData, {
      name: '',
      shortName: '',
      parentId: null,
      leaderId: undefined,
      sort: 0,
      establishedDate: '',
      description: ''
    })
    employeeList.value = []
  }
}

// ==================== 监听 ====================

watch(
  () => props.modelValue,
  async (val) => {
    visible.value = val
    if (val) {
      await loadDepartmentTree()
      initFormData()
    }
  },
  { immediate: true }
)
</script>

<style scoped lang="scss">
.department-form {
  padding: 20px;

  .el-form-item {
    margin-bottom: 22px;
  }

  .text-secondary {
    color: var(--el-text-color-secondary);
    font-size: 12px;
    margin-left: 8px;
  }
}

:deep(.el-select) {
  width: 100%;
}

:deep(.el-tree-select) {
  width: 100%;
}
</style>
