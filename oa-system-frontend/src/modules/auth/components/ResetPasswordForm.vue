<template>
  <div class="reset-password-container">
    <el-button :icon="ArrowLeft" class="back-button" @click="router.push('/login')">
      返回登录
    </el-button>

    <el-card class="reset-card">
      <h2>找回密码</h2>

      <el-steps :active="currentStep" simple class="steps">
        <el-step title="验证身份" />
        <el-step title="重置密码" />
        <el-step title="完成" />
      </el-steps>

      <!-- Step 1: 验证身份 -->
      <div v-if="currentStep === 0" class="step-content">
        <el-radio-group v-model="verifyType" class="verify-type-selector">
          <el-radio value="email" size="large">
            <el-icon><Message /></el-icon>
            邮箱验证
          </el-radio>
          <el-radio value="mobile" size="large">
            <el-icon><Phone /></el-icon>
            手机验证
          </el-radio>
        </el-radio-group>

        <el-form
          ref="step1FormRef"
          :model="step1Form"
          :rules="step1Rules"
          label-position="top"
        >
          <el-form-item :label="verifyType === 'email' ? '注册邮箱' : '手机号'" prop="account">
            <el-input
              v-model="step1Form.account"
              :placeholder="verifyType === 'email' ? '请输入注册邮箱' : '请输入手机号'"
              :prefix-icon="verifyType === 'email' ? Message : Phone"
            />
          </el-form-item>

          <el-form-item label="验证码" prop="code">
            <div class="code-input-wrapper">
              <el-input
                v-model="step1Form.code"
                placeholder="请输入验证码"
                maxlength="6"
              />
              <el-button :disabled="countdown > 0 || sending" :loading="sending" @click="sendCode">
                {{ sending ? '发送中...' : countdown > 0 ? `${countdown}s后重发` : '获取验证码' }}
              </el-button>
            </div>
          </el-form-item>

          <el-button type="primary" size="large" class="next-button" @click="validateStep1">
            下一步
          </el-button>
        </el-form>
      </div>

      <!-- Step 2: 重置密码 -->
      <div v-if="currentStep === 1" class="step-content">
        <el-form
          ref="step2FormRef"
          :model="step2Form"
          :rules="step2Rules"
          label-position="top"
        >
          <el-form-item label="新密码" prop="newPassword">
            <el-input
              v-model="step2Form.newPassword"
              type="password"
              placeholder="请输入新密码"
              show-password
              @input="checkPasswordStrength"
            />
            <div v-if="step2Form.newPassword && passwordStrength.hint" class="password-hint">
              {{ passwordStrength.hint }}
            </div>
          </el-form-item>

          <el-form-item label="确认新密码" prop="confirmPassword">
            <el-input
              v-model="step2Form.confirmPassword"
              type="password"
              placeholder="请再次输入新密码"
              show-password
            />
          </el-form-item>

          <el-form-item label="密码强度">
            <el-progress
              :percentage="passwordStrength.score"
              :color="passwordStrength.color"
              :stroke-width="8"
            />
            <span class="strength-text">{{ passwordStrength.text }}</span>
          </el-form-item>

          <div class="button-group">
            <el-button size="large" @click="prevStep">上一步</el-button>
            <el-button type="primary" size="large" @click="resetPassword">下一步</el-button>
          </div>
        </el-form>
      </div>

      <!-- Step 3: 完成 -->
      <div v-if="currentStep === 2" class="step-content success-step">
        <el-result
          icon="success"
          title="密码修改成功!"
          sub-title="您的密码已成功修改,请使用新密码登录系统"
        >
          <template #extra>
            <el-button type="primary" size="large" @click="goToLogin"> 立即登录 </el-button>
            <p class="countdown-text">({{ countdown }}秒后自动跳转到登录页...)</p>
          </template>
        </el-result>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Message, Phone } from '@element-plus/icons-vue'
import { sendCode as sendVerificationCode, resetPassword as resetPasswordApi } from '../api'
import type { FormInstance, FormRules } from 'element-plus'

const router = useRouter()
const currentStep = ref(0)
const verifyType = ref<'email' | 'mobile'>('email')
const countdown = ref(0)
const sending = ref(false)
let countdownTimer: number | undefined

const step1FormRef = ref<FormInstance>()
const step2FormRef = ref<FormInstance>()

const step1Form = reactive({
  account: '',
  code: '',
})

const step2Form = reactive({
  newPassword: '',
  confirmPassword: '',
})

// Step 1 表单验证规则
const step1Rules: FormRules = {
  account: [
    { required: true, message: '请输入邮箱或手机号', trigger: 'blur' },
    {
      validator: (_rule, value, callback) => {
        const isEmail = /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value)
        const isMobile = /^1[3-9]\d{9}$/.test(value)

        if (!isEmail && !isMobile) {
          callback(new Error('请输入正确的邮箱或手机号'))
        } else {
          callback()
        }
      },
      trigger: 'blur',
    },
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 6, message: '验证码长度为6位', trigger: 'blur' },
  ],
}

// Step 2 表单验证规则
const step2Rules: FormRules = {
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 8, max: 20, message: '长度在 8 到 20 个字符', trigger: 'blur' },
    {
      validator: (_rule, value, callback) => {
        // 必须包含大小写字母和数字
        const hasUpperCase = /[A-Z]/.test(value)
        const hasLowerCase = /[a-z]/.test(value)
        const hasNumber = /\d/.test(value)

        if (!hasUpperCase || !hasLowerCase || !hasNumber) {
          callback(new Error('密码必须包含大小写字母和数字'))
        } else {
          callback()
        }
      },
      trigger: 'blur',
    },
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (_rule, value, callback) => {
        if (value !== step2Form.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur',
    },
  ],
}

// 密码强度计算
const passwordStrength = computed(() => {
  const password = step2Form.newPassword
  if (!password) return { score: 0, text: '', color: '', hint: '' }

  let score = 0
  const hint: string[] = []

  // 长度检查
  if (password.length >= 8) score += 25
  else hint.push('至少8位字符')

  // 包含小写字母
  if (/[a-z]/.test(password)) score += 25
  else hint.push('包含小写字母')

  // 包含大写字母
  if (/[A-Z]/.test(password)) score += 25
  else hint.push('包含大写字母')

  // 包含数字
  if (/\d/.test(password)) score += 25
  else hint.push('包含数字')

  // 包含特殊字符加分
  if (/[!@#$%^&*]/.test(password)) score += 10

  score = Math.min(score, 100)

  let text = '弱'
  let color = '#f5222d'

  if (score >= 80) {
    text = '强'
    color = '#52c41a'
  } else if (score >= 60) {
    text = '中'
    color = '#faad14'
  }

  return {
    score,
    text,
    color,
    hint: hint.length > 0 ? `缺少:${hint.join('、')}` : '✅ 包含大小写字母和数字',
  }
})

/**
 * 发送验证码
 */
async function sendCode() {
  try {
    await step1FormRef.value?.validateField('account')

    sending.value = true

    await sendVerificationCode({
      type: verifyType.value,
      account: step1Form.account,
      scene: 'forgot_password',
    })

    ElMessage.success('验证码已发送,请注意查收')

    // 开始倒计时
    countdown.value = 60
    countdownTimer = window.setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) {
        clearInterval(countdownTimer)
      }
    }, 1000)
  } catch (error: any) {
    console.error('发送验证码失败:', error)

    if (error.response?.data) {
      const { code, message } = error.response.data

      if (code === 1006) {
        ElMessage.warning('发送过于频繁,请1分钟后再试')
      } else if (code === 1007) {
        ElMessage.error('该账号未注册')
      } else {
        ElMessage.error(message || '发送失败,请稍后重试')
      }
    } else {
      ElMessage.error('发送失败,请稍后重试')
    }
  } finally {
    sending.value = false
  }
}

/**
 * 验证Step 1
 */
async function validateStep1() {
  try {
    await step1FormRef.value?.validate()
    // 验证通过,进入下一步
    currentStep.value = 1
  } catch (error) {
    console.error('验证失败:', error)
  }
}

/**
 * 检查密码强度
 */
function checkPasswordStrength() {
  // 密码强度通过computed自动计算
}

/**
 * 重置密码
 */
async function resetPassword() {
  try {
    await step2FormRef.value?.validate()

    await resetPasswordApi({
      type: verifyType.value,
      account: step1Form.account,
      code: step1Form.code,
      newPassword: step2Form.newPassword,
    })

    ElMessage.success('密码修改成功')

    // 进入完成步骤
    currentStep.value = 2

    // 5秒后自动跳转登录页
    countdown.value = 5
    countdownTimer = window.setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) {
        clearInterval(countdownTimer)
        goToLogin()
      }
    }, 1000)
  } catch (error: any) {
    console.error('密码修改失败:', error)

    if (error.response?.data) {
      const { code, message } = error.response.data

      if (code === 1010) {
        ElMessage.error('验证码错误')
      } else if (code === 1008) {
        ElMessage.error('密码不符合安全策略')
      } else if (code === 1009) {
        ElMessage.error('新密码不能与旧密码相同')
      } else {
        ElMessage.error(message || '密码修改失败,请稍后重试')
      }
    } else {
      ElMessage.error('密码修改失败,请稍后重试')
    }
  }
}

/**
 * 上一步
 */
function prevStep() {
  currentStep.value = 0
}

/**
 * 跳转登录页
 */
function goToLogin() {
  router.push('/login')
}

/**
 * 组件卸载时清除定时器
 */
onUnmounted(() => {
  if (countdownTimer) {
    clearInterval(countdownTimer)
  }
})
</script>

<style scoped lang="scss">
.reset-password-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.back-button {
  position: absolute;
  top: 20px;
  left: 20px;
}

.reset-card {
  width: 480px;
  border-radius: 8px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);

  h2 {
    font-size: 24px;
    font-weight: 500;
    color: #333;
    margin-bottom: 24px;
  }

  .steps {
    margin-bottom: 32px;
  }
}

.step-content {
  padding: 20px 0;
}

.verify-type-selector {
  display: flex;
  gap: 16px;
  margin-bottom: 24px;
  width: 100%;

  :deep(.el-radio) {
    flex: 1;
    padding: 16px;
    border: 2px solid #dcdfe6;
    border-radius: 8px;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;

    &.is-checked {
      border-color: #1890ff;
      background: #e6f7ff;
    }

    .el-icon {
      font-size: 24px;
    }
  }
}

.code-input-wrapper {
  display: flex;
  gap: 12px;

  .el-input {
    flex: 1;
  }

  .el-button {
    white-space: nowrap;
  }
}

.password-hint {
  font-size: 12px;
  margin-top: 4px;

  &.strength-weak {
    color: #f5222d;
  }

  &.strength-medium {
    color: #faad14;
  }

  &.strength-strong {
    color: #52c41a;
  }
}

.strength-text {
  margin-left: 12px;
  font-size: 14px;
  font-weight: 500;
}

.button-group {
  display: flex;
  gap: 12px;
  margin-top: 24px;

  .el-button {
    flex: 1;
  }
}

.next-button {
  width: 100%;
  margin-top: 24px;
}

.success-step {
  text-align: center;
  padding: 40px 20px;

  .countdown-text {
    margin-top: 16px;
    color: #999;
    font-size: 14px;
  }
}

// 响应式设计
@media (max-width: 480px) {
  .reset-card {
    width: 100%;
    margin: 0 16px;
  }

  .verify-type-selector {
    flex-direction: column;
  }
}
</style>
