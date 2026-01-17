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
          <el-option
            v-for="item in employeeStatusOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="部门">
        <el-tree-select
          v-model="form.departmentIds"
          :data="departmentTree"
          :props="{ label: 'name', value: 'id', children: 'children' }"
          placeholder="请选择部门"
          multiple
          clearable
          check-strictly
          filterable
          collapse-tags
          collapse-tags-tooltip
        />
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
          <el-option
            v-for="item in genderOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="入职开始日期">
        <el-date-picker
          v-model="form.joinDateStart"
          type="date"
          placeholder="选择开始日期"
          value-format="YYYY-MM-DD"
          style="width: 100%"
        />
      </el-form-item>

      <el-form-item label="入职结束日期">
        <el-date-picker
          v-model="form.joinDateEnd"
          type="date"
          placeholder="选择结束日期"
          value-format="YYYY-MM-DD"
          style="width: 100%"
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
import { useDictStore } from '@/modules/dict/store'

const emit = defineEmits<{
  (e: 'search', filter: EmployeeFilter): void
  (e: 'reset'): void
}>()

const dictStore = useDictStore()

const form = ref<EmployeeFilter>({
  keyword: '',
  status: '',
  departmentIds: [],
  position: '',
  gender: '',
  joinDateStart: undefined,
  joinDateEnd: undefined,
})

const departmentTree = ref<Array<{ id: string; name: string; children?: any[] }>>([])
const positions = ref<string[]>([])

// 字典选项数据
const genderOptions = ref<Array<{ label: string; value: string }>>([])
const employeeStatusOptions = ref<Array<{ label: string; value: string }>>([])

onMounted(async () => {
  // 加载部门和职位数据
  departmentTree.value = await employeeApi.getDepartmentList()
  positions.value = await employeeApi.getPositionList()

  // 加载字典数据
  try {
    // 性别字典 (code: gender)
    const genderDict = await dictStore.fetchDictData('gender')
    genderOptions.value = genderDict.items

    // 员工状态字典 (code: employee_status)
    const employeeStatusDict = await dictStore.fetchDictData('employee_status')
    employeeStatusOptions.value = employeeStatusDict.items
  } catch (error) {
    console.error('加载字典数据失败:', error)
  }
})

function handleSearch() {
  emit('search', form.value)
}

function handleReset() {
  form.value = {
    keyword: '',
    status: '',
    departmentIds: [],
    position: '',
    gender: '',
    joinDateStart: undefined,
    joinDateEnd: undefined,
  }
  emit('reset')
}
</script>

<style lang="scss" scoped>
@use '@/assets/styles/variables.scss' as *;
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

  :deep(.el-select),
  :deep(.el-tree-select) {
    width: 100%;
  }
}
</style>
