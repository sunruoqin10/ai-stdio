<template>
  <span v-if="status" class="status-tag" :class="tagClass">
    <slot>{{ statusText }}</slot>
  </span>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { EmployeeStatus, ProbationStatus, Gender } from '@/modules/employee/types'

interface Props {
  status?: string
  type?: 'employee' | 'probation' | 'gender' | 'asset' | 'auto'
  customClass?: string
}

const props = withDefaults(defineProps<Props>(), {
  status: '',
  type: 'auto',
})

// 员工状态配置
const employeeStatusConfig: Record<string, { type: string; text: string }> = {
  // 使用枚举值 (全小写)
  active: { type: 'success', text: '在职' },
  resigned: { type: 'info', text: '离职' },
  suspended: { type: 'warning', text: '停薪留职' },

  // 兼容旧值 (驼峰命名)
  Active: { type: 'success', text: '在职' },
  Inactive: { type: 'default', text: '离职' },
  Probation: { type: 'warning', text: '试用期内' },
  Leave: { type: 'error', text: '停薪留职' },
}

// 试用期状态配置
const probationStatusConfig: Record<string, { type: string; text: string }> = {
  // 使用枚举值 (全小写)
  probation: { type: 'warning', text: '试用期内' },
  regular: { type: 'success', text: '已转正' },
  resigned: { type: 'info', text: '已离职' },

  // 兼容旧值 (驼峰命名)
  Probation: { type: 'warning', text: '试用期内' },
  Regular: { type: 'success', text: '已转正' },
  Resigned: { type: 'info', text: '已离职' },
}

// 性别配置
const genderConfig: Record<string, { type: string; text: string }> = {
  // 使用枚举值 (全小写)
  male: { type: 'primary', text: '男' },
  female: { type: 'danger', text: '女' },

  // 兼容旧值 (驼峰命名)
  Male: { type: 'primary', text: '男' },
  Female: { type: 'danger', text: '女' },
}

// 资产状态配置
const assetStatusConfig: Record<string, { type: string; text: string }> = {
  'in-stock': { type: 'success', text: '库存中' },
  'in-use': { type: 'primary', text: '使用中' },
  borrowed: { type: 'warning', text: '借出' },
  maintenance: { type: 'error', text: '维修中' },
  scrapped: { type: 'default', text: '报废' },
}

const tagClass = computed(() => {
  if (props.customClass) return props.customClass

  let config

  // 根据类型选择配置
  if (props.type === 'employee') {
    config = employeeStatusConfig[props.status]
  } else if (props.type === 'probation') {
    config = probationStatusConfig[props.status]
  } else if (props.type === 'gender') {
    config = genderConfig[props.status]
  } else if (props.type === 'asset') {
    config = assetStatusConfig[props.status]
  } else {
    // auto 模式：自动检测
    config =
      employeeStatusConfig[props.status] ||
      probationStatusConfig[props.status] ||
      genderConfig[props.status] ||
      assetStatusConfig[props.status]
  }

  const type = config?.type || 'default'
  return `status-${type}`
})

const statusText = computed(() => {
  let config

  // 根据类型选择配置
  if (props.type === 'employee') {
    config = employeeStatusConfig[props.status]
  } else if (props.type === 'probation') {
    config = probationStatusConfig[props.status]
  } else if (props.type === 'gender') {
    config = genderConfig[props.status]
  } else if (props.type === 'asset') {
    config = assetStatusConfig[props.status]
  } else {
    // auto 模式：自动检测
    config =
      employeeStatusConfig[props.status] ||
      probationStatusConfig[props.status] ||
      genderConfig[props.status] ||
      assetStatusConfig[props.status]
  }

  return config?.text || props.status
})
</script>

<style lang="scss" scoped>
@use '@/assets/styles/variables.scss' as *;
@use '@/assets/styles/mixins.scss' as *;

.status-tag {
  display: inline-flex;
  align-items: center;
  padding: 2px 8px;
  font-size: $font-size-sm;
  border-radius: $border-radius-base;
  line-height: 1;
  white-space: nowrap;

  &.status-success {
    background-color: rgba($success-color, 0.1);
    color: $success-color;
  }

  &.status-warning {
    background-color: rgba($warning-color, 0.1);
    color: $warning-color;
  }

  &.status-error {
    background-color: rgba($error-color, 0.1);
    color: $error-color;
  }

  &.status-info {
    background-color: rgba($info-color, 0.1);
    color: $info-color;
  }

  &.status-default {
    background-color: rgba($text-secondary, 0.1);
    color: $text-secondary;
  }

  &.status-primary {
    background-color: rgba($primary-color, 0.1);
    color: $primary-color;
  }

  &.status-danger {
    background-color: rgba($error-color, 0.1);
    color: $error-color;
  }
}
</style>
