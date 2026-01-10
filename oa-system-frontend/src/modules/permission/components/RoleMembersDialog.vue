<template>
  <el-dialog
    v-model="dialogVisible"
    :title="`角色成员: ${roleName}`"
    width="800px"
    @close="handleClose"
  >
    <div class="role-members">
      <!-- 操作栏 -->
      <div class="actions">
        <el-input
          v-model="keyword"
          placeholder="搜索成员..."
          clearable
          class="search-input"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button type="primary" @click="handleAddMember">
          <el-icon><Plus /></el-icon>
          添加成员
        </el-button>
      </div>

      <!-- 成员列表 -->
      <el-table :data="memberList" v-loading="loading" border>
        <el-table-column prop="name" label="姓名" width="150" />
        <el-table-column prop="code" label="工号" width="120" />
        <el-table-column prop="department" label="部门" />
        <el-table-column prop="position" label="职位" />
        <el-table-column prop="assignTime" label="分配时间" width="180" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button link type="danger" @click="handleRemoveMember(row)">
              移除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadMembers"
          @current-change="loadMembers"
        />
      </div>
    </div>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus } from '@element-plus/icons-vue'

// Props
interface Props {
  modelValue: boolean
  roleId: string
  roleName: string
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: false,
  roleId: '',
  roleName: ''
})

// Emits
const emit = defineEmits<{
  'update:modelValue': [value: boolean]
}>()

// 状态
const loading = ref(false)
const keyword = ref('')
const memberList = ref<any[]>([])
const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

// 对话框显示状态
const dialogVisible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

// ==================== 方法 ====================

/**
 * 加载成员列表
 */
async function loadMembers() {
  if (!props.roleId) return

  loading.value = true
  try {
    // TODO: 调用API获取角色成员列表
    // const data = await getRoleUsers(props.roleId, {
    //   page: pagination.page,
    //   size: pagination.size,
    //   keyword: keyword.value
    // })
    // memberList.value = data.list
    // pagination.total = data.total

    // Mock数据
    memberList.value = [
      {
        id: '1',
        name: '张三',
        code: 'EMP001',
        department: '技术部',
        position: '前端工程师',
        assignTime: '2026-01-01 10:00:00'
      },
      {
        id: '2',
        name: '李四',
        code: 'EMP002',
        department: '技术部',
        position: '后端工程师',
        assignTime: '2026-01-05 14:30:00'
      }
    ]
    pagination.total = 2
  } catch (error) {
    ElMessage.error('加载成员列表失败')
  } finally {
    loading.value = false
  }
}

/**
 * 添加成员
 */
function handleAddMember() {
  // TODO: 打开添加成员对话框
  ElMessage.info('功能开发中...')
}

/**
 * 移除成员
 */
async function handleRemoveMember(member: any) {
  try {
    await ElMessageBox.confirm(
      `确定要移除成员"${member.name}"吗?`,
      '移除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    // TODO: 调用API移除成员
    ElMessage.success('移除成功')
    loadMembers()
  } catch {
    // 用户取消
  }
}

/**
 * 关闭对话框
 */
function handleClose() {
  dialogVisible.value = false
}

// ==================== 监听 ====================

watch(
  () => props.modelValue,
  (val) => {
    if (val && props.roleId) {
      loadMembers()
    }
  }
)

watch(keyword, () => {
  pagination.page = 1
  loadMembers()
})
</script>

<style scoped lang="scss">
.role-members {
  .actions {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;

    .search-input {
      flex: 1;
      max-width: 300px;
    }
  }

  .pagination {
    margin-top: 16px;
    display: flex;
    justify-content: flex-end;
  }
}
</style>
