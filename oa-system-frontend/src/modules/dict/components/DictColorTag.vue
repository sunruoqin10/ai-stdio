<template>
  <el-tag
    :type="tagType"
    :style="customStyle"
    :size="size"
    :effect="effect"
  >
    {{ label }}
  </el-tag>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { DictColorType } from '../types'
import { getDictColor } from '../utils'

interface Props {
  /** 标签文本 */
  label: string
  /** 颜色类型 */
  colorType?: DictColorType
  /** 标签尺寸 */
  size?: 'large' | 'default' | 'small'
  /** 主题风格 */
  effect?: 'dark' | 'light' | 'plain'
}

const props = withDefaults(defineProps<Props>(), {
  colorType: 'info',
  size: 'default',
  effect: 'dark'
})

// 计算Element Plus的type
const tagType = computed<'success' | 'warning' | 'info' | 'primary' | 'danger'>(() => {
  return props.colorType as any
})

// 自定义样式
const customStyle = computed(() => {
  if (props.effect === 'dark') {
    return {
      backgroundColor: getDictColor(props.colorType),
      borderColor: 'transparent',
      color: '#fff'
    }
  }
  return {}
})
</script>

<style scoped lang="scss">
// 组件样式继承自 el-tag
</style>
