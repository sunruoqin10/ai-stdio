<template>
  <div class="approval-management">
    <!-- 筛选栏 -->
    <div class="filter-bar">
      <el-input
        v-model="keyword"
        placeholder="搜索申请人或事由"
        clearable
        style="width: 250px"
        @input="handleSearch"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>

      <el-select
        v-model="filterDepartment"
        placeholder="全部部门"
        clearable
        style="width: 150px"
        @change="handleFilter"
      >
        <el-option label="技术部" value="DEPT001" />
        <el-option label="销售部" value="DEPT002" />
        <el-option label="市场部" value="DEPT003" />
      </el-select>

      <el-select
        v-model="filterType"
        placeholder="全部类型"
        clearable
        style="width: 150px"
        @change="handleFilter"
      >
        <el-option label="年假" value="annual" />
        <el-option label="病假" value="sick" />
        <el-option label="事假" value="personal" />
        <el-option label="调休" value="comp_time" />
      </el-select>
    </div>

    <!-- 待审批列表 -->
    <div v-loading="leaveStore.loading" class="approval-list">
      <el-empty v-if="leaveStore.pendingApprovals.length === 0" description="暂无待审批申请" />

      <div
        v-for="request in leaveStore.pendingApprovals"
        :key="request.id"
        class="approval-card"
      >
        <!-- 卡片头部 -->
        <div class="card-header">
          <div class="applicant-info">
            <el-avatar :size="40">{{ request.applicantName.charAt(0) }}</el-avatar>
            <div class="applicant-detail">
              <div class="applicant-name">{{ request.applicantName }}</div>
              <div class="applicant-dept">{{ request.departmentName }}</div>
            </div>
          </div>
          <div class="leave-summary">
            <el-tag :type="getTypeTagType(request.type)">
              {{ getLeaveTypeName(request.type) }}
            </el-tag>
            <span class="duration">{{ request.duration }}天</span>
          </div>
        </div>

        <!-- 卡片内容 -->
        <div class="card-content">
          <div class="time-range">
            <el-icon><Calendar /></el-icon>
            <span>{{ formatDate(request.startTime) }} ~ {{ formatDate(request.endTime) }}</span>
          </div>
          <div class="reason">
            <span class="reason-label">事由:</span>
            <span class="reason-text">{{ request.reason }}</span>
          </div>

          <!-- 审批流程 -->
          <div class="approval-flow">
            <div class="flow-title">审批流程:</div>
            <div class="flow-steps">
              <div
                v-for="(approver, index) in request.approvers"
                :key="index"
                class="flow-step"
                :class="{
                  'current': approver.approvalLevel === request.currentApprovalLevel,
                  'completed': approver.status === 'approved'
                }"
              >
                <div class="step-number">{{ index + 1 }}</div>
                <div class="step-info">
                  <div class="step-name">{{ approver.approverName }}</div>
                  <div class="step-role">{{ getApprovalLevelName(approver.approvalLevel) }}</div>
                </div>
                <div class="step-status">
                  <el-icon v-if="approver.status === 'approved'" class="success"><Check /></el-icon>
                  <el-icon v-else-if="approver.approvalLevel === request.currentApprovalLevel" class="current">
                    <Clock />
                  </el-icon>
                  <el-icon v-else class="pending"><MoreFilled /></el-icon>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 操作按钮 -->
        <div class="card-footer">
          <el-button size="small" @click="handleView(request)">
            查看详情
          </el-button>
          <el-button
            type="success"
            size="small"
            @click="handleApprove(request)"
          >
            通过
          </el-button>
          <el-button
            type="danger"
            size="small"
            @click="handleReject(request)"
          >
            驳回
          </el-button>
        </div>
      </div>
    </div>

    <!-- 审批对话框 -->
    <el-dialog
      v-model="showApprovalDialog"
      :title="approvalTitle"
      width="500px"
    >
      <el-form
        ref="approvalFormRef"
        :model="approvalForm"
        label-width="80px"
      >
        <el-form-item label="审批意见">
          <el-input
            v-model="approvalForm.opinion"
            type="textarea"
            :rows="4"
            :placeholder="approvalPlaceholder"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="showApprovalDialog = false">取消</el-button>
        <el-button
          :type="approvalAction === 'approve' ? 'success' : 'danger'"
          @click="handleConfirmApproval"
          :loading="loading"
        >
          确认{{ approvalAction === 'approve' ? '通过' : '驳回' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 详情抽屉 -->
    <LeaveDetail
      v-model="showDetail"
      :request-id="currentRequestId"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Calendar, Check, Clock, MoreFilled } from '@element-plus/icons-vue'
import { useLeaveStore } from '../store'
import {
  formatDate,
  getLeaveTypeName,
  getApprovalLevelName,
  getTypeTagType
} from '../utils'
import type { LeaveRequest } from '../types'
import LeaveDetail from './LeaveDetail.vue'

const leaveStore = useLeaveStore()

const keyword = ref('')
const filterDepartment = ref<string>()
const filterType = ref<string>()
const showDetail = ref(false)
const showApprovalDialog = ref(false)
const currentRequestId = ref<string>()
const currentRequest = ref<LeaveRequest | null>(null)
const approvalAction = ref<'approve' | 'reject'>('approve')
const loading = ref(false)

const approvalFormRef = ref()
const approvalForm = reactive({
  opinion: ''
})

const approvalTitle = computed(() => {
  return approvalAction.value === 'approve' ? '通过申请' : '驳回申请'
})

const approvalPlaceholder = computed(() => {
  return approvalAction.value === 'approve'
    ? '请填写通过意见(可选)'
    : '请填写驳回理由(必填)'
})

onMounted(() => {
  loadApprovals()
})

async function loadApprovals() {
  try {
    await leaveStore.loadPendingApprovals({
      departmentId: filterDepartment.value
    })
  } catch (error: any) {
    ElMessage.error(error.message || '加载失败')
  }
}

function handleSearch() {
  // 前端搜索过滤
  const filtered = leaveStore.pendingApprovals.filter(req =>
    req.applicantName.includes(keyword.value) ||
    req.reason.includes(keyword.value)
  )
  // 更新列表显示
}

function handleFilter() {
  loadApprovals()
}

function handleView(request: LeaveRequest) {
  currentRequestId.value = request.id
  showDetail.value = true
}

function handleApprove(request: LeaveRequest) {
  currentRequest.value = request
  approvalAction.value = 'approve'
  approvalForm.opinion = ''
  showApprovalDialog.value = true
}

function handleReject(request: LeaveRequest) {
  currentRequest.value = request
  approvalAction.value = 'reject'
  approvalForm.opinion = ''
  showApprovalDialog.value = true
}

async function handleConfirmApproval() {
  if (approvalAction.value === 'reject' && !approvalForm.opinion) {
    ElMessage.warning('请填写驳回理由')
    return
  }

  loading.value = true
  try {
    await leaveStore.approveRequest(currentRequest.value!.id, {
      status: approvalAction.value === 'approve' ? 'approved' : 'rejected',
      opinion: approvalForm.opinion
    })

    ElMessage.success(approvalAction.value === 'approve' ? '已通过' : '已驳回')
    showApprovalDialog.value = false
    loadApprovals()
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
.approval-management {
  .filter-bar {
    display: flex;
    gap: 12px;
    margin-bottom: 20px;
  }

  .approval-list {
    min-height: 400px;

    .approval-card {
      background: white;
      border: 1px solid #EBEEF5;
      border-radius: 8px;
      padding: 16px;
      margin-bottom: 16px;

      .card-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 16px;

        .applicant-info {
          display: flex;
          gap: 12px;

          .applicant-detail {
            .applicant-name {
              font-size: 15px;
              font-weight: 600;
              color: #303133;
              margin-bottom: 4px;
            }

            .applicant-dept {
              font-size: 13px;
              color: #909399;
            }
          }
        }

        .leave-summary {
          display: flex;
          align-items: center;
          gap: 12px;

          .duration {
            font-size: 18px;
            font-weight: 600;
            color: #409EFF;
          }
        }
      }

      .card-content {
        .time-range {
          display: flex;
          align-items: center;
          gap: 8px;
          margin-bottom: 12px;
          font-size: 14px;
          color: #606266;

          .el-icon {
            color: #409EFF;
          }
        }

        .reason {
          margin-bottom: 16px;
          font-size: 14px;
          color: #606266;

          .reason-label {
            font-weight: 500;
            color: #909399;
          }

          .reason-text {
            margin-left: 8px;
          }
        }

        .approval-flow {
          padding: 12px;
          background: #F5F7FA;
          border-radius: 6px;

          .flow-title {
            font-size: 13px;
            color: #909399;
            margin-bottom: 8px;
          }

          .flow-steps {
            display: flex;
            flex-direction: column;
            gap: 8px;

            .flow-step {
              display: flex;
              align-items: center;
              gap: 12px;
              padding: 8px;
              border-radius: 4px;
              background: white;
              transition: all 0.3s;

              &.current {
                background: #ECF5FF;
                border: 1px solid #409EFF;
              }

              &.completed {
                opacity: 0.7;
              }

              .step-number {
                width: 28px;
                height: 28px;
                border-radius: 50%;
                background: #409EFF;
                color: white;
                display: flex;
                align-items: center;
                justify-content: center;
                font-size: 13px;
                font-weight: 600;
              }

              .step-info {
                flex: 1;

                .step-name {
                  font-size: 14px;
                  color: #303133;
                  margin-bottom: 2px;
                }

                .step-role {
                  font-size: 12px;
                  color: #909399;
                }
              }

              .step-status {
                .el-icon {
                  font-size: 18px;

                  &.success {
                    color: #67C23A;
                  }

                  &.current {
                    color: #409EFF;
                    animation: pulse 1.5s infinite;
                  }

                  &.pending {
                    color: #DCDFE6;
                  }
                }
              }
            }
          }
        }
      }

      .card-footer {
        border-top: 1px solid #EBEEF5;
        padding-top: 12px;
        display: flex;
        gap: 8px;
      }
    }
  }
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
}
</style>
