<template>
  <el-form
    ref="formRef"
    :model="formData"
    :rules="rules"
    label-width="120px"
    class="employee-form"
  >
    <el-steps :active="currentStep" align-center class="form-steps">
      <el-step title="基本信息" />
      <el-step title="详细信息" />
      <el-step title="确认提交" />
    </el-steps>

    <!-- 步骤1: 基本信息 (必填) -->
    <div v-show="currentStep === 0" class="step-content">
      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="姓名" prop="name">
            <el-input v-model="formData.name" placeholder="请输入姓名" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="性别" prop="gender">
            <el-radio-group v-model="formData.gender">
              <el-radio :value="Gender.MALE">男</el-radio>
              <el-radio :value="Gender.FEMALE">女</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="英文名">
            <el-input v-model="formData.englishName" placeholder="请输入英文名(可选)" />
          </el-form-item>
        </el-col>
        <el-col :span="12"></el-col>
      </el-row>

      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="联系电话" prop="phone">
            <el-input v-model="formData.phone" placeholder="请输入手机号" />
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
          <el-form-item label="入职日期" prop="joinDate">
            <el-date-picker
              v-model="formData.joinDate"
              type="date"
              placeholder="选择日期"
              value-format="YYYY-MM-DD"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
      </el-row>
    </div>

    <!-- 步骤2: 详细信息 (可选) -->
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
              v-model="formData.managerId"
              placeholder="请选择直属上级"
              filterable
              clearable
              style="width: 100%"
            >
              <el-option
                v-for="emp in employees"
                :key="emp.id"
                :label="`${emp.name} - ${emp.departmentName}`"
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
        <div class="upload-tip">支持上传 JPG/PNG 格式图片，大小不超过 2MB</div>
      </el-form-item>
    </div>

    <!-- 步骤3: 确认提交 -->
    <div v-show="currentStep === 2" class="step-content">
      <el-descriptions title="员工信息确认" :column="2" border>
        <el-descriptions-item label="姓名">{{ formData.name }}</el-descriptions-item>
        <el-descriptions-item label="性别">
          {{ formData.gender === Gender.MALE ? '男' : '女' }}
        </el-descriptions-item>
        <el-descriptions-item label="英文名">{{ formData.englishName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ formData.phone }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ formData.email }}</el-descriptions-item>
        <el-descriptions-item label="部门">{{ getDepartmentName(formData.departmentId) }}</el-descriptions-item>
        <el-descriptions-item label="职位">{{ formData.position }}</el-descriptions-item>
        <el-descriptions-item label="入职日期">{{ formData.joinDate }}</el-descriptions-item>
        <el-descriptions-item label="出生日期">{{ formData.birthDate || '-' }}</el-descriptions-item>
        <el-descriptions-item label="办公位置">{{ formData.officeLocation || '-' }}</el-descriptions-item>
        <el-descriptions-item label="紧急联系人">{{ formData.emergencyContact || '-' }}</el-descriptions-item>
        <el-descriptions-item label="紧急联系电话">{{ formData.emergencyPhone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="直属上级">{{ getManagerName(formData.managerId) }}</el-descriptions-item>
        <el-descriptions-item label="试用期结束日期">{{ getProbationEndDate() }}</el-descriptions-item>
      </el-descriptions>
    </div>

    <!-- 底部按钮 -->
    <div class="form-actions">
      <el-button v-if="currentStep > 0" @click="handlePrev">上一步</el-button>
      <el-button v-if="currentStep < 2" type="primary" @click="handleNext">下一步</el-button>
      <el-button v-if="currentStep === 2" type="primary" :loading="submitting" @click="handleSubmit">提交</el-button>
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
import { Gender } from '../types'
import { calculateProbationEndDate } from '../utils'
import * as employeeApi from '../api'

interface Props {
  employee?: Employee
}

const props = defineProps<Props>()

const emit = defineEmits<{
  (e: 'submit', data: EmployeeForm): void
  (e: 'cancel'): void
}>()

const formRef = ref<FormInstance>()
const currentStep = ref(0)
const submitting = ref(false)
const departments = ref<Array<{ id: string; name: string }>>([])
const employees = ref<Employee[]>([])

const formData = reactive<EmployeeForm>({
  name: '',
  gender: Gender.MALE,
  englishName: '',
  phone: '',
  email: '',
  departmentId: '',
  position: '',
  joinDate: '',
  birthDate: '',
  officeLocation: '',
  emergencyContact: '',
  emergencyPhone: '',
  managerId: '',
  avatar: '',
  probationEndDate: '',
})

const rules: FormRules = {
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' },
  ],
  gender: [
    { required: true, message: '请选择性别', trigger: 'change' },
  ],
  phone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' },
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' },
  ],
  departmentId: [
    { required: true, message: '请选择部门', trigger: 'change' },
  ],
  position: [
    { required: true, message: '请输入职位', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' },
  ],
  joinDate: [
    { required: true, message: '请选择入职日期', trigger: 'change' },
    {
      validator: (rule: any, value: string, callback: any) => {
        if (new Date(value) > new Date()) {
          callback(new Error('入职日期不能晚于今天'))
        } else {
          callback()
        }
      },
      trigger: 'change',
    },
  ],
}

onMounted(async () => {
  // 加载部门列表
  departments.value = await employeeApi.getDepartmentList()

  // 加载员工列表(用于选择上级)
  const { list } = await employeeApi.getEmployeeList({ page: 1, pageSize: 1000 })
  employees.value = list

  // 如果是编辑模式,填充表单数据
  if (props.employee) {
    Object.assign(formData, {
      name: props.employee.name,
      gender: props.employee.gender,
      englishName: props.employee.englishName,
      phone: props.employee.phone,
      email: props.employee.email,
      departmentId: props.employee.departmentId,
      position: props.employee.position,
      joinDate: props.employee.joinDate,
      birthDate: props.employee.birthDate,
      officeLocation: props.employee.officeLocation,
      emergencyContact: props.employee.emergencyContact,
      emergencyPhone: props.employee.emergencyPhone,
      managerId: props.employee.managerId,
      avatar: props.employee.avatar,
      probationEndDate: props.employee.probationEndDate,
    })
  } else {
    // 新增模式,设置默认入职日期为今天
    formData.joinDate = new Date().toISOString().split('T')[0]
  }
})

function handleNext() {
  if (currentStep.value === 0) {
    // 验证必填字段
    formRef.value?.validateField(['name', 'gender', 'phone', 'email', 'departmentId', 'position', 'joinDate'], (valid) => {
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
    submitting.value = true
    emit('submit', formData)
  } catch {
    submitting.value = false
  }
}

function handleCancel() {
  emit('cancel')
}

function getDepartmentName(id: string) {
  const dept = departments.value.find(d => d.id === id)
  return dept?.name || '-'
}

function getManagerName(id?: string) {
  if (!id) return '-'
  const emp = employees.value.find(e => e.id === id)
  return emp?.name || '-'
}

function getProbationEndDate() {
  if (formData.probationEndDate) {
    return formData.probationEndDate
  }
  // 自动计算试用期结束日期
  return calculateProbationEndDate(formData.joinDate)
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
@use '@/assets/styles/variables.scss' as *;
@use '@/assets/styles/mixins.scss' as *;

.employee-form {
  .form-steps {
    margin-bottom: 24px;
  }

  .step-content {
    min-height: 300px;
    padding: 16px 0;
  }

  .form-actions {
    @include flex-center;
    gap: 8px;
    margin-top: 20px;
    padding-top: 20px;
    border-top: 1px solid #e8e8e8;
  }

  .avatar-uploader {
    .avatar {
      width: 178px;
      height: 178px;
      display: block;
      border-radius: 4px;
    }

    :deep(.el-upload) {
      border: 1px dashed #d9d9d9;
      border-radius: 4px;
      cursor: pointer;
      position: relative;
      overflow: hidden;
      transition: all 0.3s cubic-bezier(0.645, 0.045, 0.355, 1);

      &:hover {
        border-color: #1890ff;
      }
    }

    .avatar-uploader-icon {
      font-size: 28px;
      color: #bfbfbf;
      width: 178px;
      height: 178px;
      @include flex-center;
    }
  }

  .upload-tip {
    font-size: 12px;
    color: #8c8c8c;
    margin-top: 8px;
  }
}
</style>
