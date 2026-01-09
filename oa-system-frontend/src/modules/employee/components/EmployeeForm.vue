<template>
  <el-form
    ref="formRef"
    :model="formData"
    :rules="rules"
    label-width="100px"
    class="employee-form"
  >
    <el-steps :active="currentStep" align-center class="form-steps">
      <el-step title="基本信息" />
      <el-step title="详细信息" />
      <el-step title="确认提交" />
    </el-steps>

    <!-- 步骤1: 基本信息 -->
    <div v-show="currentStep === 0" class="step-content">
      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="员工编号" prop="employeeNo">
            <el-input v-model="formData.employeeNo" placeholder="请输入员工编号" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="姓名" prop="name">
            <el-input v-model="formData.name" placeholder="请输入姓名" />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="性别" prop="gender">
            <el-radio-group v-model="formData.gender">
              <el-radio label="male">男</el-radio>
              <el-radio label="female">女</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="英文名">
            <el-input v-model="formData.englishName" placeholder="请输入英文名" />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="联系电话" prop="phone">
            <el-input v-model="formData.phone" placeholder="请输入联系电话" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="邮箱" prop="email">
            <el-input v-model="formData.email" placeholder="请输入邮箱" />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="部门" prop="departmentId">
            <el-select
              v-model="formData.departmentId"
              placeholder="请选择部门"
              style="width: 100%"
            >
              <el-option
                v-for="dept in departments"
                :key="dept.id"
                :label="dept.name"
                :value="dept.id"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="职位" prop="position">
            <el-input v-model="formData.position" placeholder="请输入职位" />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="入职日期" prop="entryDate">
            <el-date-picker
              v-model="formData.entryDate"
              type="date"
              placeholder="选择日期"
              value-format="YYYY-MM-DD"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
      </el-row>
    </div>

    <!-- 步骤2: 详细信息 -->
    <div v-show="currentStep === 1" class="step-content">
      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="出生日期">
            <el-date-picker
              v-model="formData.birthDate"
              type="date"
              placeholder="选择日期"
              value-format="YYYY-MM-DD"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="办公位置">
            <el-input v-model="formData.officeLocation" placeholder="请输入办公位置" />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="紧急联系人">
            <el-input v-model="formData.emergencyContact" placeholder="请输入紧急联系人" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="紧急联系电话">
            <el-input v-model="formData.emergencyPhone" placeholder="请输入紧急联系电话" />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="直属上级">
            <el-select
              v-model="formData.superiorId"
              placeholder="请选择直属上级"
              filterable
              clearable
              style="width: 100%"
            >
              <el-option
                v-for="emp in employees"
                :key="emp.id"
                :label="emp.name"
                :value="emp.id"
              />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="头像上传">
        <el-upload
          class="avatar-uploader"
          :show-file-list="false"
          :on-success="handleAvatarSuccess"
          :before-upload="beforeAvatarUpload"
        >
          <img v-if="formData.avatar" :src="formData.avatar" class="avatar" />
          <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
        </el-upload>
      </el-form-item>
    </div>

    <!-- 步骤3: 确认提交 -->
    <div v-show="currentStep === 2" class="step-content">
      <el-descriptions title="员工信息确认" :column="2" border>
        <el-descriptions-item label="员工编号">{{ formData.employeeNo }}</el-descriptions-item>
        <el-descriptions-item label="姓名">{{ formData.name }}</el-descriptions-item>
        <el-descriptions-item label="性别">
          {{ formData.gender === 'male' ? '男' : '女' }}
        </el-descriptions-item>
        <el-descriptions-item label="英文名">{{ formData.englishName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ formData.phone }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ formData.email }}</el-descriptions-item>
        <el-descriptions-item label="部门">{{ getDepartmentName(formData.departmentId) }}</el-descriptions-item>
        <el-descriptions-item label="职位">{{ formData.position }}</el-descriptions-item>
        <el-descriptions-item label="入职日期">{{ formData.entryDate }}</el-descriptions-item>
        <el-descriptions-item label="出生日期">{{ formData.birthDate || '-' }}</el-descriptions-item>
        <el-descriptions-item label="办公位置">{{ formData.officeLocation || '-' }}</el-descriptions-item>
        <el-descriptions-item label="紧急联系人">{{ formData.emergencyContact || '-' }}</el-descriptions-item>
      </el-descriptions>
    </div>

    <!-- 底部按钮 -->
    <div class="form-actions">
      <el-button v-if="currentStep > 0" @click="handlePrev">上一步</el-button>
      <el-button v-if="currentStep < 2" type="primary" @click="handleNext">下一步</el-button>
      <el-button v-if="currentStep === 2" type="primary" @click="handleSubmit">提交</el-button>
      <el-button @click="handleCancel">取消</el-button>
    </div>
  </el-form>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import type { FormInstance, FormRules, UploadProps } from 'element-plus'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import type { Employee, EmployeeForm } from '../types'
import * as employeeApi from '../api'

interface Props {
  employee?: Employee
}

const props = defineProps<Props>()

const emit = defineEmits<{
  (e: 'submit', data: Partial<Employee>): void
  (e: 'cancel'): void
}>()

const formRef = ref<FormInstance>()
const currentStep = ref(0)
const departments = ref<Array<{ id: string; name: string }>>([])
const employees = ref<Employee[]>([])

const formData = reactive<EmployeeForm>({
  employeeNo: '',
  name: '',
  englishName: '',
  gender: 'male',
  phone: '',
  email: '',
  departmentId: '',
  department: '',
  position: '',
  entryDate: '',
  birthDate: '',
  officeLocation: '',
  avatar: '',
  emergencyContact: '',
  emergencyPhone: '',
  superiorId: '',
  superior: '',
})

const rules: FormRules = {
  employeeNo: [{ required: true, message: '请输入员工编号', trigger: 'blur' }],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  gender: [{ required: true, message: '请选择性别', trigger: 'change' }],
  phone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' },
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱', trigger: 'blur' },
  ],
  departmentId: [{ required: true, message: '请选择部门', trigger: 'change' }],
  position: [{ required: true, message: '请输入职位', trigger: 'blur' }],
  entryDate: [{ required: true, message: '请选择入职日期', trigger: 'change' }],
}

onMounted(async () => {
  departments.value = await employeeApi.getDepartmentList()
  const { list } = await employeeApi.getEmployeeList({ page: 1, pageSize: 1000 })
  employees.value = list

  // 如果是编辑模式,填充表单数据
  if (props.employee) {
    Object.assign(formData, {
      ...props.employee,
    })
  } else {
    // 新增模式,自动生成员工编号
    const { list } = await employeeApi.getEmployeeList({ page: 1, pageSize: 1000 })
    const maxNo = list.reduce((max, emp) => {
      const num = parseInt(emp.employeeNo.replace('EMP', ''))
      return num > max ? num : max
    }, 0)
    formData.employeeNo = `EMP${String(maxNo + 1).padStart(3, '0')}`
  }
})

function handleNext() {
  if (currentStep.value === 0) {
    formRef.value?.validateField(['employeeNo', 'name', 'gender', 'phone', 'email', 'departmentId', 'position', 'entryDate'], (valid) => {
      if (valid) {
        currentStep.value++
      }
    })
  } else {
    currentStep.value++
  }
}

function handlePrev() {
  if (currentStep.value > 0) {
    currentStep.value--
  }
}

async function handleSubmit() {
  try {
    await formRef.value?.validate()

    // 获取部门名称
    const dept = departments.value.find(d => d.id === formData.departmentId)
    if (dept) {
      formData.department = dept.name
    }

    // 获取上级名称
    if (formData.superiorId) {
      const superior = employees.value.find(e => e.id === formData.superiorId)
      if (superior) {
        formData.superior = superior.name
      }
    }

    emit('submit', formData)
  } catch {
    ElMessage.error('请填写必填项')
  }
}

function handleCancel() {
  emit('cancel')
}

function getDepartmentName(id: string) {
  const dept = departments.value.find(d => d.id === id)
  return dept?.name || '-'
}

const handleAvatarSuccess: UploadProps['onSuccess'] = (response) => {
  formData.avatar = response.url
}

const beforeAvatarUpload: UploadProps['beforeUpload'] = (rawFile) => {
  if (!rawFile.type.startsWith('image/')) {
    ElMessage.error('只能上传图片文件!')
    return false
  } else if (rawFile.size / 1024 / 1024 > 2) {
    ElMessage.error('图片大小不能超过 2MB!')
    return false
  }
  return true
}
</script>

<style lang="scss" scoped>
@use '@/assets/styles/mixins.scss' as *;

.employee-form {
  .form-steps {
    margin-bottom: $spacing-xxl;
  }

  .step-content {
    min-height: 300px;
    padding: $spacing-lg 0;
  }

  .form-actions {
    @include flex-center;
    gap: $spacing-sm;
    margin-top: $spacing-xl;
    padding-top: $spacing-xl;
    border-top: 1px solid $border-color-light;
  }

  .avatar-uploader {
    .avatar {
      width: 178px;
      height: 178px;
      display: block;
      border-radius: $border-radius-base;
    }

    :deep(.el-upload) {
      border: 1px dashed $border-color;
      border-radius: $border-radius-base;
      cursor: pointer;
      position: relative;
      overflow: hidden;
      transition: $transition-base;

      &:hover {
        border-color: $primary-color;
      }
    }

    .avatar-uploader-icon {
      font-size: 28px;
      color: $text-placeholder;
      width: 178px;
      height: 178px;
      @include flex-center;
    }
  }
}
</style>
