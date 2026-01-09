<template>
  <span v-if="status" class="status-tag" :class="tagClass">
    <slot>{{ statusText }}</slot>
  </span>
</template>

<script setup lang="ts">
import { computed } from 'vue'

interface Props {
  status?: string
  type?: 'success' | 'warning' | 'error' | 'info' | 'default' | 'primary'
  customClass?: string
}

const props = withDefaults(defineProps<Props>(), {
  status: '',
  type: 'default',
})

const statusMap: Record<string, { type: Props['type']; text: string }> = {
  // 员工状态
  active: { type: 'success', text: '在职' },
  inactive: { type: 'default', text: '离职' },
  probation: { type: 'warning', text: '试用期内' },
  leave: { type: 'error', text: '停薪留职' },
  regular: { type: 'success', text: '已转正' },

  // 资产状态
  'in-stock': { type: 'success', text: '库存中' },
  'in-use': { type: 'primary', text: '使用中' },
  borrowed: { type: 'warning', text: '借出' },
  maintenance: { type: 'error', text: '维修中' },
  scrapped: { type: 'default', text: '报废' },
}

const tagClass = computed(() => {
  if (props.customClass) return props.customClass

  const statusInfo = statusMap[props.status]
  const type = statusInfo?.type || props.type
  return `status-${type}`
})

const statusText = computed(() => {
  const statusInfo = statusMap[props.status]
  return statusInfo?.text || props.status
})
</script>

<style lang="scss" scoped>
@use '@/assets/styles/mixins.scss' as *;

.status-tag {
  display: inline-flex;
  align-items: center;
  padding: 2px 8px;
  font-size: $font-size-sm;
  border-radius: $border-radius-base;
  line-height: 1;

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
}
</style>
