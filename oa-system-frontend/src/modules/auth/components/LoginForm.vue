<template>
  <div class="login-form-container">
    <el-card class="login-card">
      <div class="card-header">
        <h2>用户登录</h2>
      </div>

      <el-form
        ref="formRef"
        :model="formData"
        :rules="rules"
        label-position="top"
        @submit.prevent="handleLogin"
      >
        <!-- 账号输入 -->
        <el-form-item prop="username">
          <el-input
            v-model="formData.username"
            placeholder="员工编号/邮箱/手机号"
            :prefix-icon="User"
            clearable
            size="large"
          />
        </el-form-item>

        <!-- 密码输入 -->
        <el-form-item prop="password">
          <el-input
            v-model="formData.password"
            type="password"
            placeholder="请输入密码"
            :prefix-icon="Lock"
            show-password
            size="large"
            @keyup.enter="handleLogin"
          />
        </el-form-item>

        <!-- 记住我 + 忘记密码 -->
        <div class="form-actions">
          <el-checkbox v-model="formData.remember">记住我</el-checkbox>
          <router-link to="/reset-password" class="link">
            忘记密码?
          </router-link>
        </div>

        <!-- 验证码(失败3次后显示) -->
        <el-form-item v-if="showCaptcha" prop="captcha">
          <div class="captcha-wrapper">
            <el-input
              v-model="formData.captcha"
              placeholder="请输入验证码"
              maxlength="4"
              size="large"
            />
            <img
              :src="captchaImage"
              alt="验证码"
              class="captcha-image"
              @click="refreshCaptcha"
            />
          </div>
        </el-form-item>

        <!-- 登录按钮 -->
        <el-button
          type="primary"
          size="large"
          :loading="loading"
          native-type="submit"
          class="login-button"
        >
          登 录
        </el-button>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { useAuthStore } from '../store'
import { getCaptcha } from '../api'
import type { FormInstance, FormRules } from 'element-plus'

const router = useRouter()
const authStore = useAuthStore()
const formRef = ref<FormInstance>()
const loading = ref(false)
const showCaptcha = ref(false)
const loginAttempts = ref(0)
const captchaKey = ref('')
const captchaImage = ref('')

const formData = reactive({
  username: '',
  password: '',
  captcha: '',
  captchaKey: '',
  remember: false,
})

// 表单验证规则
const rules: FormRules = {
  username: [
    { required: true, message: '请输入账号', trigger: 'blur' },
    { min: 3, max: 50, message: '长度在 3 到 50 个字符', trigger: 'blur' },
    {
      validator: (_rule, value, callback) => {
        if (!value) {
          callback()
          return
        }

        // 支持多种登录方式
        // 1. 员工编号 (可以是EMP开头的长编号,也可以是简短的用户名如admin)
        // 2. 邮箱格式
        // 3. 手机号格式
        const isEmployeeNo = /^EMP\d{17}$/.test(value) || /^[a-zA-Z0-9_]{3,20}$/.test(value)
        const isEmail = /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value)
        const isMobile = /^1[3-9]\d{9}$/.test(value)

        if (!isEmployeeNo && !isEmail && !isMobile) {
          callback(new Error('请输入正确的员工编号/邮箱/手机号'))
        } else {
          callback()
        }
      },
      trigger: 'blur',
    },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 8, max: 20, message: '长度在 8 到 20 个字符', trigger: 'blur' },
  ],
  captcha: [
    {
      validator: (_rule, value, callback) => {
        // 失败次数>=3时,验证码必填
        if (showCaptcha.value && !value) {
          callback(new Error('请输入验证码'))
        } else {
          callback()
        }
      },
      trigger: 'blur',
    },
  ],
}

/**
 * 刷新验证码
 */
async function refreshCaptcha() {
  try {
    const response = await getCaptcha()
    if (response.code === 200 && response.data) {
      captchaKey.value = response.data.captchaKey
      captchaImage.value = response.data.captchaImage
      formData.captchaKey = response.data.captchaKey
    }
  } catch (error) {
    console.error('获取验证码失败:', error)
  }
}

/**
 * 处理登录
 */
async function handleLogin() {
  console.log('[Login] 开始登录流程')
  console.log('[Login] 表单数据:', formData)

  try {
    // 表单验证
    await formRef.value?.validate()
    console.log('[Login] 表单验证通过')

    loading.value = true

    // 调用登录接口
    console.log('[Login] 调用authStore.login')
    await authStore.login(formData)
    console.log('[Login] authStore.login 完成')

    // 登录成功
    ElMessage.success({
      message: '登录成功,欢迎回来!',
      duration: 2000,
    })

    // 跳转到首页或原访问页面
    const redirect = (router.currentRoute.value.query.redirect as string) || '/'
    console.log('[Login] 跳转到:', redirect)
    router.push(redirect)
  } catch (error: any) {
    console.error('登录失败:', error)

    // 增加失败次数
    loginAttempts.value++

    // 处理错误响应
    const errorData = error.response?.data || error.data || error

    if (errorData) {
      const { code, data, message } = errorData

      if (code === 1002) {
        // 密码错误
        const attemptsLeft = data?.attemptsLeft || 0
        ElMessage.error({
          message: attemptsLeft > 0 ? `密码错误,还剩${attemptsLeft}次机会` : message,
          duration: 3000,
        })

        // 失败3次后显示验证码
        if (loginAttempts.value >= 3) {
          showCaptcha.value = true
          refreshCaptcha()
        }
      } else if (code === 1003) {
        // 账号锁定
        const lockedUntil = data?.lockedUntil || '30分钟'
        ElMessageBox.alert(`账号已被锁定,预计${lockedUntil}解锁`, '登录失败', {
          type: 'error',
          confirmButtonText: '知道了',
        })
      } else if (code === 200) {
        // 成功但被catch捕获
        ElMessage.error('登录失败,请检查账号密码')
      } else {
        ElMessage.error(message || '登录失败,请稍后重试')
      }
    } else {
      ElMessage.error('登录失败,请稍后重试')
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
.login-form-container {
  width: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
}

.login-card {
  width: 420px;
  border-radius: 8px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);

  .card-header {
    margin-bottom: 24px;

    h2 {
      font-size: 24px;
      font-weight: 500;
      color: #333;
      margin: 0;
    }
  }
}

.form-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.captcha-wrapper {
  display: flex;
  gap: 12px;

  .el-input {
    flex: 1;
  }

  .captcha-image {
    width: 120px;
    height: 40px;
    cursor: pointer;
    border-radius: 4px;
    border: 1px solid #dcdfe6;
    transition: border-color 0.3s;

    &:hover {
      border-color: #1890ff;
    }
  }
}

.login-button {
  width: 100%;
  height: 44px;
  font-size: 16px;
  margin-bottom: 24px;
}

// 响应式设计
@media (max-width: 480px) {
  .login-card {
    width: 100%;
    margin: 0 16px;
  }

  .form-actions {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
}
</style>
