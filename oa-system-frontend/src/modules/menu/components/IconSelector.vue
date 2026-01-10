<template>
  <el-select
    v-model="selectedIcon"
    filterable
    :filter-method="filterIcons"
    placeholder="请选择图标"
    @change="handleChange"
  >
    <el-option
      v-for="icon in filteredIcons"
      :key="icon.name"
      :label="icon.title"
      :value="icon.name"
    >
      <div class="icon-option">
        <el-icon :size="20">
          <component :is="icon.component" />
        </el-icon>
        <span class="icon-label">{{ icon.title }}</span>
      </div>
    </el-option>
  </el-select>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { ICON_LIST } from '../config/icons'

interface Props {
  modelValue: string
}

interface Emits {
  (e: 'update:modelValue', value: string): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const selectedIcon = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const searchText = ref('')

const filteredIcons = computed(() => {
  if (!searchText.value) {
    return ICON_LIST
  }

  const search = searchText.value.toLowerCase()
  return ICON_LIST.filter(icon =>
    icon.name.toLowerCase().includes(search)
  )
})

function filterIcons(query: string) {
  searchText.value = query
}

function handleChange(value: string) {
  emit('update:modelValue', value)
}
</script>

<style scoped>
.icon-option {
  display: flex;
  align-items: center;
  gap: 8px;
}

.icon-label {
  margin-left: 8px;
}
</style>
