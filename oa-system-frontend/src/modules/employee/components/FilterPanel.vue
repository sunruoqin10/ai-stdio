<template>
  <div class="filter-panel">
    <el-form :model="form" label-width="80px" class="filter-form">
      <el-form-item label="关键词">
        <el-input
          v-model="form.keyword"
          placeholder="搜索姓名/工号/手机号"
          clearable
          @keyup.enter="handleSearch"
        />
      </el-form-item>

      <el-form-item label="员工状态">
        <el-select v-model="form.status" placeholder="请选择" clearable>
          <el-option label="在职" value="active" />
          <el-option label="离职" value="inactive" />
          <el-option label="停薪留职" value="leave" />
        </el-select>
      </el-form-item>

      <el-form-item label="部门">
        <el-select v-model="form.departmentIds" placeholder="请选择" clearable multiple>
          <el-option
            v-for="dept in departments"
            :key="dept.id"
            :label="dept.name"
            :value="dept.id"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="试用期">
        <el-select v-model="form.probationStatus" placeholder="请选择" clearable>
          <el-option label="试用期内" value="probation" />
          <el-option label="已转正" value="regular" />
        </el-select>
      </el-form-item>

      <el-form-item label="职位">
        <el-select v-model="form.position" placeholder="请选择" clearable>
          <el-option
            v-for="pos in positions"
            :key="pos"
            :label="pos"
            :value="pos"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="性别">
        <el-select v-model="form.gender" placeholder="请选择" clearable>
          <el-option label="男" value="male" />
          <el-option label="女" value="female" />
        </el-select>
      </el-form-item>

      <el-form-item label="入职时间">
        <el-date-picker
          v-model="form.entryDateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="YYYY-MM-DD"
        />
      </el-form-item>

      <el-form-item>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import type { EmployeeFilter } from '../types'
import * as employeeApi from '../api'

const emit = defineEmits<{
  (e: 'search', filter: EmployeeFilter): void
  (e: 'reset'): void
}>()

const form = ref<EmployeeFilter>({
  keyword: '',
  status: '',
  departmentIds: [],
  probationStatus: '',
  position: '',
  gender: '',
  entryDateRange: undefined,
})

const departments = ref<Array<{ id: string; name: string }>>([])
const positions = ref<string[]>([])

onMounted(async () => {
  // 加载部门和职位数据
  departments.value = await employeeApi.getDepartmentList()
  positions.value = await employeeApi.getPositionList()
})

function handleSearch() {
  emit('search', form.value)
}

function handleReset() {
  form.value = {
    keyword: '',
    status: '',
    departmentIds: [],
    probationStatus: '',
    position: '',
    gender: '',
    entryDateRange: undefined,
  }
  emit('reset')
}
</script>

<style lang="scss" scoped>
@use '@/assets/styles/mixins.scss' as *;

.filter-panel {
  background: $white;
  border-radius: $border-radius-xl;
  box-shadow: $shadow-base;
  padding: $card-padding;
  height: fit-content;

  .filter-form {
    .el-form-item {
      margin-bottom: $spacing-lg;
    }
  }
}
</style>
