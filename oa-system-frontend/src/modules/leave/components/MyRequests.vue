<template>
  <div class="my-requests">
    <!-- 筛选栏 -->
    <div class="filter-bar">
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
        <el-option label="婚假" value="marriage" />
        <el-option label="产假" value="maternity" />
      </el-select>

      <el-select
        v-model="filterStatus"
        placeholder="全部状态"
        clearable
        style="width: 150px"
        @change="handleFilter"
      >
        <el-option label="草稿" value="draft" />
        <el-option label="待审批" value="pending" />
        <el-option label="审批中" value="approving" />
        <el-option label="已通过" value="approved" />
        <el-option label="已驳回" value="rejected" />
        <el-option label="已撤销" value="cancelled" />
      </el-select>

      <el-button type="primary" @click="handleCreate">
        <el-icon><Plus /></el-icon>
        新建申请
      </el-button>
    </div>

    <!-- 申请列表 -->
    <div v-loading="leaveStore.loading" class="request-list">
      <el-empty v-if="leaveStore.myRequests.length === 0" description="暂无请假申请" />

      <div
        v-for="request in leaveStore.myRequests"
        :key="request.id"
        class="request-card"
        :class="`status-${request.status}`"
      >
        <!-- 卡片头部 -->
        <div class="card-header">
          <div class="header-left">
            <el-icon class="calendar-icon"><Calendar /></el-icon>
            <span class="date-range">
              {{ formatDate(request.startTime) }} ~ {{ formatDate(request.endTime) }}
            </span>
            <span class="duration">({{ request.duration }}天)</span>
          </div>
          <el-tag :type="getLeaveStatusType(request.status)" size="large">
            {{ getLeaveStatusName(request.status) }}
          </el-tag>
        </div>

        <!-- 卡片内容 -->
        <div class="card-content">
          <div class="leave-info">
            <span class="leave-type">{{ getLeaveTypeName(request.type) }}</span>
            <span class="separator">|</span>
            <span class="applicant">{{ request.applicantName }}</span>
            <span class="separator">|</span>
            <span class="department">{{ request.departmentName }}</span>
          </div>
          <div class="leave-reason">{{ request.reason }}</div>

          <!-- 审批进度 -->
          <div v-if="request.approvers && request.approvers.length > 0" class="approval-progress">
            <div class="progress-label">审批进度:</div>
            <div class="progress-steps">
              <div
                v-for="(approver, index) in request.approvers"
                :key="index"
                class="progress-step"
                :class="{
                  'completed': approver.status === 'approved',
                  'current': approver.status === 'pending' && request.currentApprovalLevel === approver.approvalLevel,
                  'rejected': approver.status === 'rejected'
                }"
              >
                <div class="step-icon">
                  <el-icon v-if="approver.status === 'approved'"><Check /></el-icon>
                  <el-icon v-else-if="approver.status === 'rejected'"><Close /></el-icon>
                  <span v-else>{{ index + 1 }}</span>
                </div>
                <div class="step-info">
                  <div class="step-name">{{ approver.approverName }}</div>
                  <div class="step-status">
                    <span v-if="approver.status === 'approved'">已通过</span>
                    <span v-else-if="approver.status === 'rejected'">已驳回</span>
                    <span v-else-if="approver.status === 'pending' && request.currentApprovalLevel === approver.approvalLevel">
                      审批中...
                    </span>
                    <span v-else>待审批</span>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 驳回理由 -->
          <div v-if="request.status === 'rejected'" class="reject-reason">
            <span class="reason-label">驳回理由:</span>
            <span class="reason-text">
              {{ request.approvers?.find(a => a.status === 'rejected')?.opinion || '未填写' }}
            </span>
          </div>
        </div>

        <!-- 卡片底部 -->
        <div class="card-footer">
          <el-button link type="primary" @click="handleView(request)">
            查看详情
          </el-button>
          <el-button
            v-if="canEdit(request.status)"
            link
            type="primary"
            @click="handleEdit(request)"
          >
            编辑
          </el-button>
          <el-button
            v-if="canCancel(request.status)"
            link
            type="warning"
            @click="handleCancel(request)"
          >
            撤销申请
          </el-button>
          <el-button
            v-if="canDelete(request.status)"
            link
            type="danger"
            @click="handleDelete(request)"
          >
            删除
          </el-button>
          <el-button
            v-if="canResubmit(request.status)"
            link
            type="success"
            @click="handleResubmit(request)"
          >
            重新提交
          </el-button>
        </div>
      </div>
    </div>

    <!-- 分页 -->
    <el-pagination
      v-model:current-page="leaveStore.pagination.page"
      v-model:page-size="leaveStore.pagination.pageSize"
      :total="leaveStore.pagination.total"
      :page-sizes="[10, 20, 50]"
      layout="total, sizes, prev, pager, next, jumper"
      style="margin-top: 20px; justify-content: flex-end"
      @size-change="handleSizeChange"
      @current-change="handlePageChange"
    />

    <!-- 详情对话框 -->
    <LeaveDetail
      v-model="showDetail"
      :request-id="currentRequestId"
      @edit="handleEditFromDetail"
    />

    <!-- 编辑对话框 -->
    <LeaveRequestForm
      v-model="showForm"
      :leave-request="currentRequest"
      @success="handleFormSuccess"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Calendar, Check, Close } from '@element-plus/icons-vue'
import { useLeaveStore } from '../store'
import {
  formatDate,
  getLeaveTypeName,
  getLeaveStatusName,
  getLeaveStatusType,
  canEdit,
  canCancel,
  canDelete,
  canResubmit
} from '../utils'
import type { LeaveRequest } from '../types'
import LeaveDetail from './LeaveDetail.vue'
import LeaveRequestForm from './LeaveRequestForm.vue'

const leaveStore = useLeaveStore()

const filterType = ref<string>()
const filterStatus = ref<string>()
const showDetail = ref(false)
const showForm = ref(false)
const currentRequestId = ref<string>()
const currentRequest = ref<LeaveRequest | null>(null)

onMounted(() => {
  loadRequests()
})

async function loadRequests() {
  try {
    await leaveStore.loadMyRequests({
      type: filterType.value,
      status: filterStatus.value
    })
  } catch (error: any) {
    ElMessage.error(error.message || '加载失败')
  }
}

function handleFilter() {
  leaveStore.setFilter({
    type: filterType.value,
    status: filterStatus.value
  })
  loadRequests()
}

function handleCreate() {
  currentRequest.value = null
  showForm.value = true
}

function handleView(request: LeaveRequest) {
  currentRequestId.value = request.id
  showDetail.value = true
}

async function handleEdit(request: LeaveRequest) {
  // 从列表点击编辑时，先加载完整数据（包含 version 字段）
  try {
    const fullRequest = await leaveStore.loadRequest(request.id)
    currentRequest.value = fullRequest
    showForm.value = true
  } catch (error: any) {
    ElMessage.error(error.message || '加载数据失败')
  }
}

function handleEditFromDetail(request: LeaveRequest) {
  showDetail.value = false
  currentRequest.value = request
  showForm.value = true
}

async function handleCancel(request: LeaveRequest) {
  try {
    await ElMessageBox.confirm(
      `确定要撤销请假申请吗?`,
      '撤销确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await leaveStore.cancelRequest(request.id)
    ElMessage.success('撤销成功')
    loadRequests()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '撤销失败')
    }
  }
}

async function handleDelete(request: LeaveRequest) {
  try {
    await ElMessageBox.confirm(
      `确定要删除请假申请吗?此操作不可恢复。`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await leaveStore.deleteRequest(request.id)
    ElMessage.success('删除成功')
    loadRequests()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

async function handleResubmit(request: LeaveRequest) {
  // 重新提交时，先加载完整数据（包含 version 字段）
  try {
    const fullRequest = await leaveStore.loadRequest(request.id)
    currentRequest.value = fullRequest
    showForm.value = true
  } catch (error: any) {
    ElMessage.error(error.message || '加载数据失败')
  }
}

function handleFormSuccess() {
  showForm.value = false
  currentRequest.value = null
  loadRequests()
}

function handleSizeChange() {
  loadRequests()
}

function handlePageChange() {
  loadRequests()
}
</script>

<style scoped lang="scss">
.my-requests {
  .filter-bar {
    display: flex;
    gap: 12px;
    margin-bottom: 20px;
  }

  .request-list {
    min-height: 400px;

    .request-card {
      background: white;
      border: 1px solid #EBEEF5;
      border-radius: 8px;
      padding: 16px;
      margin-bottom: 16px;
      transition: all 0.3s;

      &:hover {
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
      }

      &.status-approved {
        border-left: 4px solid #67C23A;
      }

      &.status-rejected {
        border-left: 4px solid #F56C6C;
      }

      &.status-pending,
      &.status-approving {
        border-left: 4px solid #409EFF;
      }

      &.status-draft {
        border-left: 4px solid #909399;
        border-style: dashed;
      }

      .card-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 12px;

        .header-left {
          display: flex;
          align-items: center;
          gap: 8px;

          .calendar-icon {
            font-size: 20px;
            color: #409EFF;
          }

          .date-range {
            font-size: 15px;
            font-weight: 500;
            color: #303133;
          }

          .duration {
            font-size: 13px;
            color: #909399;
          }
        }
      }

      .card-content {
        .leave-info {
          display: flex;
          align-items: center;
          gap: 8px;
          margin-bottom: 8px;
          font-size: 14px;
          color: #606266;

          .leave-type {
            font-weight: 500;
          }

          .separator {
            color: #DCDFE6;
          }
        }

        .leave-reason {
          color: #909399;
          font-size: 13px;
          margin-bottom: 12px;
          line-height: 1.5;
        }

        .approval-progress {
          margin-bottom: 12px;

          .progress-label {
            font-size: 13px;
            color: #909399;
            margin-bottom: 8px;
          }

          .progress-steps {
            display: flex;
            gap: 16px;

            .progress-step {
              display: flex;
              align-items: center;
              gap: 8px;

              .step-icon {
                width: 32px;
                height: 32px;
                border-radius: 50%;
                background: #F5F7FA;
                display: flex;
                align-items: center;
                justify-content: center;
                font-size: 14px;
                color: #909399;
                border: 2px solid #DCDFE6;
              }

              .step-info {
                .step-name {
                  font-size: 13px;
                  color: #303133;
                  margin-bottom: 2px;
                }

                .step-status {
                  font-size: 12px;
                  color: #909399;
                }
              }

              &.completed {
                .step-icon {
                  background: #F0F9FF;
                  border-color: #67C23A;
                  color: #67C23A;
                }
              }

              &.current {
                .step-icon {
                  background: #409EFF;
                  border-color: #409EFF;
                  color: white;
                  animation: pulse 1.5s infinite;
                }
              }

              &.rejected {
                .step-icon {
                  background: #FEF0F0;
                  border-color: #F56C6C;
                  color: #F56C6C;
                }
              }
            }
          }
        }

        .reject-reason {
          padding: 8px 12px;
          background: #FEF0F0;
          border-radius: 4px;
          font-size: 13px;

          .reason-label {
            font-weight: 500;
            color: #F56C6C;
          }

          .reason-text {
            color: #606266;
          }
        }
      }

      .card-footer {
        border-top: 1px solid #EBEEF5;
        padding-top: 12px;
        display: flex;
        gap: 12px;
      }
    }
  }
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.7;
  }
}
</style>
