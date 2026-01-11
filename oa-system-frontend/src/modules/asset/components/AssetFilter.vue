<template>
  <el-form :inline="true" :model="filterForm" class="asset-filter">
    <el-form-item label="关键字">
      <el-input
        v-model="filterForm.keyword"
        placeholder="搜索资产名称/编号"
        clearable
        style="width: 200px"
        @change="handleSearch"
      />
    </el-form-item>

    <el-form-item label="类别">
      <el-select v-model="filterForm.category" placeholder="全部" clearable style="width: 150px" @change="handleSearch">
        <el-option label="电子设备" value="electronic" />
        <el-option label="办公家具" value="furniture" />
        <el-option label="图书资料" value="book" />
        <el-option label="其他" value="other" />
      </el-select>
    </el-form-item>

    <el-form-item label="状态">
      <el-select v-model="filterForm.status" placeholder="全部" clearable style="width: 150px" @change="handleSearch">
        <el-option label="库存中" value="stock" />
        <el-option label="使用中" value="in_use" />
        <el-option label="已借出" value="borrowed" />
        <el-option label="维修中" value="maintenance" />
        <el-option label="已报废" value="scrapped" />
      </el-select>
    </el-form-item>

    <el-form-item>
      <el-button type="primary" @click="handleSearch">查询</el-button>
      <el-button @click="handleReset">重置</el-button>
    </el-form-item>
  </el-form>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import type { AssetFilter } from '../types'

const emit = defineEmits<{
  filter: [filter: AssetFilter]
  reset: []
}>()

const filterForm = reactive<AssetFilter>({
  keyword: '',
  category: undefined,
  status: undefined
})

function handleSearch() {
  emit('filter', { ...filterForm })
}

function handleReset() {
  filterForm.keyword = ''
  filterForm.category = undefined
  filterForm.status = undefined
  emit('reset')
}
</script>

<style scoped lang="scss">
.asset-filter {
  :deep(.el-form-item) {
    margin-bottom: 12px;
  }
}
</style>
