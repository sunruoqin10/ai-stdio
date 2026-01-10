<template>
  <div class="page-header">
    <div class="header-left">
      <el-button v-if="showBack" :icon="ArrowLeft" circle @click="handleBack" />
      <h2 class="page-title">{{ title }}</h2>
      <span v-if="subtitle" class="page-subtitle">{{ subtitle }}</span>
    </div>
    <div class="header-right">
      <slot name="actions">
        <el-button v-if="showAdd" type="primary" :icon="Plus" @click="handleAdd">
          {{ addText }}
        </el-button>
      </slot>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ArrowLeft, Plus } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'

interface Props {
  title: string
  subtitle?: string
  showBack?: boolean
  showAdd?: boolean
  addText?: string
}

const props = withDefaults(defineProps<Props>(), {
  showBack: false,
  showAdd: false,
  addText: '新增',
})

const emit = defineEmits<{
  (e: 'back'): void
  (e: 'add'): void
}>()

const router = useRouter()

const handleBack = () => {
  emit('back')
  // 如果没有自定义返回逻辑,默认返回上一页
  router.back()
}

const handleAdd = () => {
  emit('add')
}
</script>

<style lang="scss" scoped>
@use '@/assets/styles/variables.scss' as *;
@use '@/assets/styles/mixins.scss' as *;

.page-header {
  @include flex-between;
  margin-bottom: $spacing-xl;

  .header-left {
    display: flex;
    align-items: center;
    gap: $spacing-md;

    .page-title {
      font-size: $font-size-title-xl;
      font-weight: $font-weight-bold;
      color: $text-title;
      margin: 0;
    }

    .page-subtitle {
      font-size: $font-size-base;
      color: $text-secondary;
    }
  }

  .header-right {
    display: flex;
    gap: $spacing-sm;
  }
}
</style>
