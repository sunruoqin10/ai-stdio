<template>
  <el-dialog
    v-model="visible"
    :title="isEdit ? '编辑会议预定' : '创建会议预定'"
    width="700px"
    @close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="100px"
      v-loading="loading"
    >
      <!-- 基本信息 -->
      <el-divider content-position="left">基本信息</el-divider>

      <el-form-item label="会议主题" prop="title">
        <el-input
          v-model="form.title"
          placeholder="请输入会议主题"
          maxlength="100"
          show-word-limit
        />
      </el-form-item>

      <el-form-item label="会议室" prop="roomId">
        <el-select
          v-model="form.roomId"
          placeholder="请选择会议室"
          style="width: 100%"
          @change="handleRoomChange"
        >
          <el-option
            v-for="room in availableRooms"
            :key="room.id"
            :label="`${room.name} (${room.location}) - 容纳${room.capacity}人`"
            :value="room.id"
          >
            <div style="display: flex; justify-content: space-between; align-items: center">
              <span>{{ room.name }} ({{ room.location }})</span>
              <el-tag size="small" :type="getRoomStatusType(room.status)">
                {{ getRoomStatusName(room.status) }}
              </el-tag>
            </div>
          </el-option>
        </el-select>
      </el-form-item>

      <el-form-item label="会议日期" prop="date">
        <el-date-picker
          v-model="form.date"
          type="date"
          placeholder="选择日期"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
          :disabled-date="disabledDate"
          style="width: 100%"
        />
      </el-form-item>

      <el-form-item label="开始时间" prop="startTime">
        <el-time-picker
          v-model="form.startTime"
          format="HH:mm"
          value-format="HH:mm"
          placeholder="选择开始时间"
          :disabled-hours="disabledHours"
          :disabled-minutes="disabledMinutes"
          style="width: 100%"
        />
      </el-form-item>

      <el-form-item label="结束时间" prop="endTime">
        <el-time-picker
          v-model="form.endTime"
          format="HH:mm"
          value-format="HH:mm"
          placeholder="选择结束时间"
          :disabled-hours="disabledHours"
          :disabled-minutes="disabledMinutes"
          style="width: 100%"
        />
      </el-form-item>

      <!-- 冲突提示 -->
      <el-alert
        v-if="hasConflict"
        title="时间冲突"
        type="error"
        :closable="false"
        style="margin-bottom: 20px"
      >
        <template #default>
          <div>该时间段已被以下会议占用:</div>
          <div
            v-for="conflict in conflicts"
            :key="conflict.id"
            style="margin-top: 5px; font-size: 12px"
          >
            {{ formatDateTime(conflict.startTime) }} - {{ formatTime(conflict.endTime) }} {{ conflict.title }}
          </div>
        </template>
      </el-alert>

      <el-form-item label="联系电话" prop="organizerPhone">
        <el-input
          v-model="form.organizerPhone"
          placeholder="请输入联系电话"
          maxlength="11"
        />
      </el-form-item>

      <!-- 会议详情 -->
      <el-divider content-position="left">会议详情</el-divider>

      <el-form-item label="会议等级" prop="level">
        <el-radio-group v-model="form.level">
          <el-radio label="normal">普通</el-radio>
          <el-radio label="important">重要</el-radio>
          <el-radio label="urgent">紧急</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item label="私密会议">
        <el-switch v-model="form.isPrivate" />
        <span style="margin-left: 10px; font-size: 12px; color: #909399">
          私密会议仅参会人员可见
        </span>
      </el-form-item>

      <el-form-item label="会议议程">
        <el-input
          v-model="form.agenda"
          type="textarea"
          :rows="3"
          placeholder="请输入会议议程(可选)"
          maxlength="500"
          show-word-limit
        />
      </el-form-item>

      <el-form-item label="参会人员" prop="attendeeIds">
        <el-select
          v-model="form.attendeeIds"
          multiple
          filterable
          placeholder="请选择参会人员"
          style="width: 100%"
        >
          <el-option
            v-for="user in mockUsers"
            :key="user.id"
            :label="user.name"
            :value="user.id"
          />
        </el-select>
        <div style="font-size: 12px; color: #909399; margin-top: 5px">
          已选择 {{ form.attendeeIds.length }} 人
        </div>
      </el-form-item>

      <!-- 重复设置 -->
      <el-divider content-position="left">重复设置</el-divider>

      <el-form-item label="重复">
        <el-select
          v-model="recurrenceType"
          placeholder="不重复"
          style="width: 200px"
          @change="handleRecurrenceChange"
        >
          <el-option label="不重复" value="none" />
          <el-option label="每天" value="daily" />
          <el-option label="每周" value="weekly" />
          <el-option label="每月" value="monthly" />
        </el-select>

        <template v-if="recurrenceType !== 'none'">
          <el-input-number
            v-model="recurrenceInterval"
            :min="1"
            :max="30"
            style="width: 120px; margin-left: 10px"
          />
          <span style="margin-left: 10px">次后结束</span>
        </template>
      </el-form-item>

      <el-form-item label="提醒时间">
        <el-select v-model="form.reminder" placeholder="选择提醒时间" style="width: 200px">
          <el-option label="不提醒" value="none" />
          <el-option label="提前15分钟" value="15min" />
          <el-option label="提前30分钟" value="30min" />
          <el-option label="提前1小时" value="1hour" />
          <el-option label="提前1天" value="1day" />
        </el-select>
      </el-form-item>

      <!-- 会议室信息展示 -->
      <template v-if="selectedRoom">
        <el-divider content-position="left">会议室信息</el-divider>
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="位置">{{ selectedRoom.location }}</el-descriptions-item>
          <el-descriptions-item label="楼层">{{ selectedRoom.floor }}楼</el-descriptions-item>
          <el-descriptions-item label="容量">{{ selectedRoom.capacity }}人</el-descriptions-item>
          <el-descriptions-item label="面积">{{ selectedRoom.area }}㎡</el-descriptions-item>
          <el-descriptions-item label="设备" :span="2">
            <el-tag
              v-for="eq in selectedRoom.equipments"
              :key="eq.id"
              size="small"
              style="margin-right: 5px; margin-bottom: 5px"
            >
              {{ getEquipmentTypeName(eq.type) }} × {{ eq.quantity }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="描述" :span="2">
            {{ selectedRoom.description || '暂无描述' }}
          </el-descriptions-item>
        </el-descriptions>
      </template>
    </el-form>

    <template #footer>
      <span class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          {{ isEdit ? '保存' : '提交预定' }}
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { useMeetingStore } from '../store'
import {
  getRoomStatusName,
  getRoomStatusType,
  getEquipmentTypeName,
  formatDateTime,
  formatTime
} from '../utils'
import type { BookingForm, MeetingRoom, MeetingBooking } from '../types'

// Props
interface Props {
  modelValue: boolean
  bookingData?: MeetingBooking | null
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: false,
  bookingData: null
})

// Emits
const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  success: []
}>()

// Store
const meetingStore = useMeetingStore()

// 表单引用
const formRef = ref<FormInstance>()

// 对话框显示状态
const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

// 是否为编辑模式
const isEdit = computed(() => !!props.bookingData)

// 加载状态
const loading = ref(false)
const submitting = ref(false)

// 表单数据
const form = reactive<BookingForm>({
  title: '',
  roomId: '',
  date: '',
  startTime: '',
  endTime: '',
  organizerPhone: '',
  agenda: '',
  level: 'normal',
  isPrivate: false,
  attendeeIds: [],
  reminder: '30min'
})

// 重复设置
const recurrenceType = ref<'none' | 'daily' | 'weekly' | 'monthly'>('none')
const recurrenceInterval = ref(1)

// 可用会议室列表
const availableRooms = computed(() => {
  return meetingStore.rooms.filter(r => r.status === 'available')
})

// 选中的会议室
const selectedRoom = computed(() => {
  if (!form.roomId) return null
  return meetingStore.rooms.find(r => r.id === form.roomId)
})

// 冲突信息
const hasConflict = ref(false)
const conflicts = ref<MeetingBooking[]>([])

// 模拟用户数据(TODO: 从用户服务获取)
const mockUsers = ref([
  { id: 'U001', name: '张三' },
  { id: 'U002', name: '李四' },
  { id: 'U003', name: '王五' },
  { id: 'U004', name: '赵六' },
  { id: 'U005', name: '孙七' }
])

// 表单验证规则
const rules: FormRules = {
  title: [
    { required: true, message: '请输入会议主题', trigger: 'blur' },
    { min: 2, max: 100, message: '长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  roomId: [
    { required: true, message: '请选择会议室', trigger: 'change' }
  ],
  date: [
    { required: true, message: '请选择会议日期', trigger: 'change' }
  ],
  startTime: [
    { required: true, message: '请选择开始时间', trigger: 'change' }
  ],
  endTime: [
    { required: true, message: '请选择结束时间', trigger: 'change' },
    {
      validator: (rule, value, callback) => {
        if (form.date && form.startTime && value) {
          const start = new Date(`${form.date} ${form.startTime}`)
          const end = new Date(`${form.date} ${value}`)
          if (end <= start) {
            callback(new Error('结束时间必须晚于开始时间'))
            return
          }
          const duration = (end.getTime() - start.getTime()) / 60000
          if (duration > 8 * 60) {
            callback(new Error('会议时长不能超过8小时'))
            return
          }
        }
        callback()
      },
      trigger: 'change'
    }
  ],
  organizerPhone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  attendeeIds: [
    { required: true, message: '请选择参会人员', trigger: 'change' },
    { type: 'array', min: 1, message: '至少选择一名参会人员', trigger: 'change' }
  ]
}

// 禁用日期(不能选择过去的日期)
function disabledDate(time: Date) {
  // 禁用今天之前的日期
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  return time.getTime() < today.getTime()
}

// 禁用小时(非工作时间)
function disabledHours() {
  // 工作时间 9:00-18:00,禁用其他时间
  const hours: number[] = []
  for (let i = 0; i < 9; i++) {
    hours.push(i)
  }
  for (let i = 18; i < 24; i++) {
    hours.push(i)
  }
  return hours
}

// 禁用分钟
function disabledMinutes(hour: number) {
  // 只允许整点和半点
  const minutes: number[] = []
  for (let i = 1; i < 60; i++) {
    if (i !== 30) {
      minutes.push(i)
    }
  }
  return minutes
}

// 会议室变更
function handleRoomChange() {
  checkConflicts()
}

// 重复设置变更
function handleRecurrenceChange() {
  if (recurrenceType.value === 'none') {
    form.recurrence = undefined
  } else {
    // 计算结束日期(30天后)
    const endDate = new Date()
    endDate.setDate(endDate.getDate() + 30)
    form.recurrence = {
      type: recurrenceType.value,
      interval: recurrenceInterval.value,
      endDate: endDate.toISOString().split('T')[0]
    }
  }
}

// 检查时间冲突
async function checkConflicts() {
  if (!form.roomId || !form.date || !form.startTime || !form.endTime) {
    hasConflict.value = false
    conflicts.value = []
    return
  }

  const startDateTime = `${form.date} ${form.startTime}`
  const endDateTime = `${form.date} ${form.endTime}`

  // TODO: 调用API检查冲突
  // const result = await meetingStore.checkRoomAvailability(...)
  // 暂时使用本地数据检查
  conflicts.value = meetingStore.bookings.filter(booking => {
    if (booking.roomId !== form.roomId) return false
    if (booking.status === 'cancelled' || booking.status === 'rejected') return false
    return checkTimeConflict(
      { startTime: startDateTime, endTime: endDateTime },
      { startTime: booking.startTime, endTime: booking.endTime }
    )
  })

  hasConflict.value = conflicts.value.length > 0
}

// 时间冲突检查工具函数
function checkTimeConflict(
  booking1: { startTime: string; endTime: string },
  booking2: { startTime: string; endTime: string }
): boolean {
  const start1 = new Date(booking1.startTime)
  const end1 = new Date(booking1.endTime)
  const start2 = new Date(booking2.startTime)
  const end2 = new Date(booking2.endTime)
  return start1 < end2 && end1 > start2
}

// 关闭对话框
function handleClose() {
  visible.value = false
  resetForm()
}

// 重置表单
function resetForm() {
  formRef.value?.resetFields()
  Object.assign(form, {
    title: '',
    roomId: '',
    date: '',
    startTime: '',
    endTime: '',
    organizerPhone: '',
    agenda: '',
    level: 'normal',
    isPrivate: false,
    attendeeIds: [],
    reminder: '30min'
  })
  recurrenceType.value = 'none'
  recurrenceInterval.value = 1
  hasConflict.value = false
  conflicts.value = []
}

// 提交表单
async function handleSubmit() {
  if (!formRef.value) return

  try {
    await formRef.value.validate()

    // 检查冲突
    if (hasConflict.value) {
      ElMessage.error('所选时间段存在冲突,请调整时间')
      return
    }

    submitting.value = true

    // 处理重复设置
    if (recurrenceType.value !== 'none') {
      const endDate = new Date()
      endDate.setDate(endDate.getDate() + 30 * recurrenceInterval.value)
      form.recurrence = {
        type: recurrenceType.value,
        interval: recurrenceInterval.value,
        endDate: endDate.toISOString()
      }
    }

    if (isEdit.value && props.bookingData) {
      // 编辑模式
      await meetingStore.updateBooking(props.bookingData.id, form)
      ElMessage.success('更新成功')
    } else {
      // 创建模式
      await meetingStore.createBooking(form)
      ElMessage.success('预定成功,等待审批')
    }

    emit('success')
    handleClose()
  } catch (error: any) {
    if (error !== false) { // 表单验证失败时error为false
      console.error('提交失败:', error)
      ElMessage.error(error.message || '操作失败,请重试')
    }
  } finally {
    submitting.value = false
  }
}

// 监听对话框打开
watch(() => props.modelValue, (val) => {
  if (val && props.bookingData) {
    // 编辑模式:回填数据
    const booking = props.bookingData
    const startDate = new Date(booking.startTime)
    const endDate = new Date(booking.endTime)

    form.title = booking.title
    form.roomId = booking.roomId
    form.date = startDate.toISOString().split('T')[0]
    form.startTime = startDate.toTimeString().slice(0, 5)
    form.endTime = endDate.toTimeString().slice(0, 5)
    form.organizerPhone = booking.organizerPhone
    form.agenda = booking.agenda || ''
    form.level = booking.level
    form.isPrivate = booking.isPrivate
    form.attendeeIds = booking.attendees.map(a => a.userId)
    form.reminder = booking.reminder

    if (booking.recurrence) {
      recurrenceType.value = booking.recurrence.type
      recurrenceInterval.value = booking.recurrence.interval
    }
  } else if (val) {
    // 创建模式:设置默认日期为今天
    form.date = new Date().toISOString().split('T')[0]
  }
})
</script>

<style scoped>
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

:deep(.el-divider__text) {
  background-color: transparent;
}
</style>
