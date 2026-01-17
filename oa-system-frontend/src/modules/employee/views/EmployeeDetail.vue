<template>
  <div class="employee-detail-page">
    <PageHeader
      title="员工详情"
      :show-back="true"
      @back="handleBack"
    >
      <template #actions>
        <el-button v-if="!editing" type="primary" @click="handleEdit">编辑</el-button>
        <template v-else>
          <el-button type="primary" @click="handleSave">保存</el-button>
          <el-button @click="handleCancel">取消</el-button>
        </template>
        <el-dropdown @command="handleCommand">
          <el-button>
            更多<el-icon class="el-icon--right"><arrow-down /></el-icon>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="email">发送邮件</el-dropdown-item>
              <el-dropdown-item command="password">重置密码</el-dropdown-item>
              <el-dropdown-item command="leave" divided>办理离职</el-dropdown-item>
              <el-dropdown-item command="delete">删除员工</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </template>
    </PageHeader>

    <el-row :gutter="16" v-loading="store.loading">
      <!-- 左侧信息卡片 -->
      <el-col :xs="24" :sm="24" :md="8" :lg="6">
        <el-card shadow="never" class="info-card">
          <div class="avatar-section">
            <el-avatar :src="employee?.avatar" :size="150" />
            <el-button v-if="!editing" link type="primary" class="change-avatar">
              更换头像
            </el-button>
          </div>
          <el-divider />
          <div class="basic-info">
            <div class="info-no">{{ employee?.employeeNo }}</div>
            <div class="info-name">{{ employee?.name }}</div>
            <div class="info-position">
              <el-tag>{{ employee?.positionLabel || employee?.position }}</el-tag>
            </div>
            <div class="info-department">
              <el-link type="primary" @click="handleGoToDepartment">
                {{ employee?.department }}
              </el-link>
            </div>
            <div class="info-status">
              <StatusTag :status="employee?.status || ''" />
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 右侧详细信息 -->
      <el-col :xs="24" :sm="24" :md="16" :lg="18">
        <el-card shadow="never">
          <el-tabs v-model="activeTab">
            <!-- 基本信息 -->
            <el-tab-pane label="基本信息" name="basic">
              <el-form
                :model="form"
                label-width="100px"
                :disabled="!editing"
                class="detail-form"
              >
                <el-row :gutter="16">
                  <el-col :span="12">
                    <el-form-item label="英文名">
                      <el-input v-model="form.englishName" />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="性别">
                      <el-radio-group v-model="form.gender">
                        <el-radio label="male">男</el-radio>
                        <el-radio label="female">女</el-radio>
                      </el-radio-group>
                    </el-form-item>
                  </el-col>
                </el-row>
                <el-row :gutter="16">
                  <el-col :span="12">
                    <el-form-item label="出生日期">
                      <el-date-picker
                        v-model="form.birthDate"
                        type="date"
                        value-format="YYYY-MM-DD"
                        style="width: 100%"
                      />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="联系电话">
                      <el-input v-model="form.phone" />
                    </el-form-item>
                  </el-col>
                </el-row>
                <el-row :gutter="16">
                  <el-col :span="12">
                    <el-form-item label="邮箱">
                      <el-input v-model="form.email" />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="办公位置">
                      <el-input v-model="form.officeLocation" />
                    </el-form-item>
                  </el-col>
                </el-row>
                <el-row :gutter="16">
                  <el-col :span="12">
                    <el-form-item label="紧急联系人">
                      <el-input v-model="form.emergencyContact" />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="紧急联系电话">
                      <el-input v-model="form.emergencyPhone" />
                    </el-form-item>
                  </el-col>
                </el-row>
              </el-form>
            </el-tab-pane>

            <!-- 工作信息 -->
            <el-tab-pane label="工作信息" name="work">
              <el-form
                :model="form"
                label-width="100px"
                :disabled="!editing"
                class="detail-form"
              >
                <el-row :gutter="16">
                  <el-col :span="12">
                    <el-form-item label="入职日期">
                      <el-date-picker
                        v-model="form.joinDate"
                        type="date"
                        value-format="YYYY-MM-DD"
                        style="width: 100%"
                      />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="试用期结束日">
                      <el-date-picker
                        v-model="form.probationEndDate"
                        type="date"
                        value-format="YYYY-MM-DD"
                        style="width: 100%"
                      />
                    </el-form-item>
                  </el-col>
                </el-row>
                <el-row :gutter="16">
                  <el-col :span="12">
                    <el-form-item label="直属上级">
                      <el-select
                        v-model="form.managerId"
                        filterable
                        style="width: 100%"
                      >
                        <el-option
                          v-for="emp in employeeList"
                          :key="emp.id"
                          :label="emp.name"
                          :value="emp.id"
                        />
                      </el-select>
                    </el-form-item>
                  </el-col>
                </el-row>
                <el-form-item label="工龄">
                  <el-input :value="`${workYears} 年`" disabled />
                </el-form-item>
              </el-form>
            </el-tab-pane>

            <!-- 操作记录 -->
            <el-tab-pane label="操作记录" name="logs">
              <el-timeline>
                <el-timeline-item
                  v-for="log in logs"
                  :key="log.id"
                  :timestamp="log.createTime"
                  placement="top"
                >
                  <el-card>
                    <div class="log-header">
                      <span class="log-operation">{{ log.operation }}</span>
                      <span class="log-operator">{{ log.operator }}</span>
                    </div>
                  </el-card>
                </el-timeline-item>
              </el-timeline>
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowDown } from '@element-plus/icons-vue'
import { useEmployeeStore } from '../store'
import PageHeader from '@/components/common/PageHeader.vue'
import StatusTag from '@/components/common/StatusTag.vue'
import { calculateWorkYears } from '@/utils/format'
import type { Employee, EmployeeForm } from '../types'
import * as employeeApi from '../api'

const route = useRoute()
const router = useRouter()
const store = useEmployeeStore()

const employee = ref<Employee | null>(null)
const editing = ref(false)
const activeTab = ref('basic')
const logs = ref<any[]>([])
const employeeList = ref<Employee[]>([])

const form = ref<EmployeeForm>({
  employeeNo: '',
  name: '',
  gender: 'male',
  phone: '',
  email: '',
  department: '',
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

const workYears = computed(() => {
  if (!employee.value?.joinDate) return 0
  return calculateWorkYears(employee.value.joinDate)
})

onMounted(async () => {
  const id = route.params.id as string
  const data = await store.fetchEmployeeDetail(id)
  if (data) {
    employee.value = data
    Object.assign(form.value, data)
  }

  // 加载操作记录
  logs.value = await employeeApi.getOperationLogs(id)

  // 加载员工列表(用于选择上级)
  const { list } = await employeeApi.getEmployeeList({ page: 1, pageSize: 1000 })
  employeeList.value = list
})

function handleEdit() {
  editing.value = true
}

function handleCancel() {
  editing.value = false
  if (employee.value) {
    Object.assign(form.value, employee.value)
  }
}

async function handleSave() {
  if (!employee.value) return

  const success = await store.updateEmployee(employee.value.id, form.value)
  if (success) {
    employee.value = { ...employee.value, ...form.value }
    editing.value = false
    ElMessage.success('保存成功')
  } else {
    ElMessage.error('保存失败')
  }
}

function handleBack() {
  router.back()
}

function handleGoToDepartment() {
  // 跳转到部门详情页
  router.push('/department/org')
}

async function handleCommand(command: string) {
  switch (command) {
    case 'email':
      ElMessage.info('发送邮件功能开发中')
      break
    case 'password':
      ElMessage.info('重置密码功能开发中')
      break
    case 'leave':
      try {
        await ElMessageBox.confirm('确定要为该员工办理离职吗?', '确认操作', {
          type: 'warning',
        })
        ElMessage.success('操作成功')
      } catch {
        // 用户取消
      }
      break
    case 'delete':
      try {
        await ElMessageBox.confirm('确定要删除该员工吗? 此操作不可恢复!', '确认删除', {
          type: 'error',
        })
        const success = await store.deleteEmployee(employee.value!.id)
        if (success) {
          ElMessage.success('删除成功')
          router.push('/employee')
        }
      } catch {
        // 用户取消
      }
      break
  }
}
</script>

<style lang="scss" scoped>
@use '@/assets/styles/variables.scss' as *;
@use '@/assets/styles/mixins.scss' as *;

.employee-detail-page {
  .info-card {
    position: sticky;
    top: $spacing-lg;

    .avatar-section {
      @include flex-column-center;
      padding: $spacing-xl 0;

      .change-avatar {
        margin-top: $spacing-md;
      }
    }

    .basic-info {
      text-align: center;

      .info-no {
        font-size: $font-size-lg;
        color: $text-secondary;
        margin-bottom: $spacing-sm;
      }

      .info-name {
        font-size: $font-size-title-xl;
        font-weight: $font-weight-bold;
        color: $text-title;
        margin-bottom: $spacing-md;
      }

      .info-position {
        margin-bottom: $spacing-sm;
      }

      .info-department {
        margin-bottom: $spacing-md;
      }

      .info-status {
        @include flex-center;
      }
    }
  }

  .detail-form {
    padding: $spacing-lg 0;
  }

  .log-header {
    @include flex-between;
    align-items: center;

    .log-operation {
      font-weight: $font-weight-medium;
      color: $text-title;
    }

    .log-operator {
      font-size: $font-size-sm;
      color: $text-secondary;
    }
  }
}
</style>
