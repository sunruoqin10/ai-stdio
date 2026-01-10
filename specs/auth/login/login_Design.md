# 登录UI/UX设计规范

> **规范类型**: 第三层 - UI/UX设计规范
> **模块**: 登录认证
> **版本**: v1.0.0
> **创建日期**: 2026-01-10

---

## 📋 目录

- [1. 页面布局](#1-页面布局)
- [2. 组件选择](#2-组件选择)
- [3. 交互规范](#3-交互规范)
- [4. 样式规范](#4-样式规范)
- [5. 响应式设计](#5-响应式设计)

---

## 1. 页面布局

### 1.1 登录页布局

```
┌────────────────────────────────────────────────────────────┐
│                                                            │
│           [Company Logo]                                   │
│                                                            │
│           OA办公系统                                       │
│                                                            │
│   ┌───────────────────────────────────────────────────┐   │
│   │                                                   │   │
│   │   用户登录                         [注册账号]      │   │
│   │   ──────────────────────────────────────────      │   │
│   │                                                   │   │
│   │   账号                                             │   │
│   │   ┌─────────────────────────────────────────┐     │   │
│   │   │ 员工编号/邮箱/手机号                     │     │   │
│   │   └─────────────────────────────────────────┘     │   │
│   │                                                   │   │
│   │   密码                              [忘记密码?]    │   │
│   │   ┌─────────────────────────────────────────┐     │   │
│   │   │ ••••••••                        [👁]    │     │   │
│   │   └─────────────────────────────────────────┘     │   │
│   │                                                   │   │
│   │   ☐ 记住我                                        │   │
│   │                                                   │   │
│   │   ┌─────────────────────────────────────────┐     │   │
│   │   │         登 录                            │     │   │
│   │   └─────────────────────────────────────────┘     │   │
│   │                                                   │   │
│   └───────────────────────────────────────────────────┘   │
│                                                            │
│           © 2026 Company Name                             │
│                                                            │
└────────────────────────────────────────────────────────────┘
```

**布局说明**:
- **居中布局**: 登录表单垂直水平居中
- **卡片宽度**: 420px固定宽度
- **背景**: 渐变背景或品牌图片
- **Logo**: 顶部居中,尺寸120x60px
- **标题**: "用户登录",左对齐,字体大小24px
- **辅助链接**: 右对齐(注册账号、忘记密码)
- **表单项**: 垂直排列,间距24px
- **登录按钮**: 宽度100%,高度44px,主色调

### 1.2 找回密码页布局

```
┌────────────────────────────────────────────────────────────┐
│                                                            │
│   ← 返回登录                                               │
│                                                            │
│   ┌───────────────────────────────────────────────────┐   │
│   │                                                   │   │
│   │   找回密码                                         │   │
│   │   ──────────────────────────────────────────      │   │
│   │                                                   │   │
│   │   Step 1: 验证身份  ▶ Step 2: 重置密码  ▶ Step 3 │   │
│   │   ──────────────────────────────────────────      │   │
│   │                                                   │   │
│   │   选择验证方式                                     │   │
│   │   ⦿ 邮箱验证  ○ 手机验证                          │   │
│   │                                                   │   │
│   │   注册邮箱                                         │   │
│   │   ┌─────────────────────────────────────────┐     │   │
│   │   │ zhangsan@company.com                     │     │   │
│   │   └─────────────────────────────────────────┘     │   │
│   │                                                   │   │
│   │   验证码                              [获取验证码] │   │
│   │   ┌──────────────────────┬──────────────┐        │   │
│   │   │ 请输入验证码          │   60s后重发  │        │   │
│   │   └──────────────────────┴──────────────┘        │   │
│   │                                                   │   │
│   │   ┌─────────────────────────────────────────┐     │   │
│   │   │              下一步                      │     │   │
│   │   └─────────────────────────────────────────┘     │   │
│   │                                                   │   │
│   └───────────────────────────────────────────────────┘   │
│                                                            │
└────────────────────────────────────────────────────────────┘
```

**布局说明**:
- **步骤指示器**: 3步(验证身份 → 重置密码 → 完成)
- **返回按钮**: 左上角"← 返回登录"
- **卡片宽度**: 480px(比登录页宽)
- **表单项**: 间距24px
- **验证码输入框**: 输入框+按钮横向布局
- **倒计时**: 60秒倒计时,倒计时结束后按钮可点击

### 1.3 密码重置页布局

```
┌────────────────────────────────────────────────────────────┐
│                                                            │
│   ┌───────────────────────────────────────────────────┐   │
│   │                                                   │   │
│   │   Step 1: 验证身份  ▶ Step 2: 重置密码  ▶ Step 3 │   │
│   │   ──────────────────────────────────────────      │   │
│   │                                                   │   │
│   │   新密码                                           │   │
│   │   ┌─────────────────────────────────────────┐     │   │
│   │   │ ••••••••                        [👁]    │     │   │
│   │   └─────────────────────────────────────────┘     │   │
│   │   ✅ 包含大小写字母和数字                          │   │
│   │                                                   │   │
│   │   确认新密码                                       │   │
│   │   ┌─────────────────────────────────────────┐     │   │
│   │   │ ••••••••                        [👁]    │     │   │
│   │   └─────────────────────────────────────────┘     │   │
│   │                                                   │   │
│   │   密码强度: ████████░░ 强                         │   │
│   │                                                   │   │
│   │   ┌─────────────────────────────────────────┐     │   │
│   │   │              下一步                      │     │   │
│   │   └─────────────────────────────────────────┘     │   │
│   │                                                   │   │
│   └───────────────────────────────────────────────────┘   │
│                                                            │
└────────────────────────────────────────────────────────────┘
```

**布局说明**:
- **密码强度指示器**: 进度条+文字(弱/中/强)
- **实时验证**: 输入时实时显示验证结果
- **密码可见性切换**: 眼睛图标切换显示/隐藏

### 1.4 完成页布局

```
┌────────────────────────────────────────────────────────────┐
│                                                            │
│   ┌───────────────────────────────────────────────────┐   │
│   │                                                   │   │
│   │          ✅                                      │   │
│   │                                                   │   │
│   │      密码修改成功!                                │   │
│   │                                                   │   │
│   │   您的密码已成功修改,请使用新密码登录系统         │   │
│   │                                                   │   │
│   │   ┌─────────────────────────────────────────┐     │   │
│   │   │          立即登录                        │     │   │
│   │   └─────────────────────────────────────────┘     │   │
│   │                                                   │   │
│   │   (5秒后自动跳转到登录页...)                       │   │
│   │                                                   │   │
│   └───────────────────────────────────────────────────┘   │
│                                                            │
└────────────────────────────────────────────────────────────┘
```

**布局说明**:
- **成功图标**: 大号绿色勾选图标(80px)
- **标题**: "密码修改成功!",居中,字体大小28px
- **描述**: 灰色文字,说明操作结果
- **操作按钮**: 主色调,宽度200px
- **倒计时提示**: 灰色小字,5秒后自动跳转

---

## 2. 组件选择

### 2.1 Element Plus组件映射

| 功能 | 组件 | 说明 | 配置 |
|------|------|------|------|
| 登录表单 | el-form | 表单容器 | :model, :rules, label-position="top" |
| 账号输入 | el-input | 文本输入 | placeholder, prefix-icon, clearable |
| 密码输入 | el-input | 密码输入 | type="password", show-password |
| 记住我 | el-checkbox | 复选框 | v-model |
| 登录按钮 | el-button | 按钮 | type="primary", loading, native-type="submit" |
| 验证码输入 | el-input | 文本输入 | maxlength="6" |
| 验证码图片 | img | 图像 | :src, @click |
| 倒计时按钮 | el-button | 按钮 | :disabled |
| 步骤条 | el-steps | 步骤指示 | :active, simple |
| 单选框 | el-radio | 单选 | v-model, label |
| 密码强度 | el-progress | 进度条 | :percentage, :color |
| 消息提示 | el-message | 消息通知 | - |
| 图标 | Element Plus Icons | 图标库 | User, Lock, View, Hide |

### 2.2 自定义组件

#### 2.2.1 LoginForm.vue

```vue
<template>
  <div class="login-container">
    <div class="login-header">
      <img src="/logo.png" alt="Logo" class="logo" />
      <h1 class="title">OA办公系统</h1>
    </div>

    <el-card class="login-card">
      <div class="card-header">
        <h2>用户登录</h2>
        <router-link to="/register" class="link">注册账号</router-link>
      </div>

      <el-form
        ref="formRef"
        :model="formData"
        :rules="rules"
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
          <router-link to="/forgot-password" class="link">
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

    <div class="login-footer">
      <p>© 2026 Company Name. All rights reserved.</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { User, Lock } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const formRef = ref()
const loading = ref(false)
const showCaptcha = ref(false)
const loginAttempts = ref(0)

const formData = reactive({
  username: '',
  password: '',
  captcha: '',
  remember: false
})

const rules = {
  username: [
    { required: true, message: '请输入账号', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ],
  captcha: [
    {
      validator: (rule: any, value: any, callback: any) => {
        if (showCaptcha.value && !value) {
          callback(new Error('请输入验证码'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

const handleLogin = async () => {
  try {
    await formRef.value.validate()
    loading.value = true

    // TODO: 调用登录接口
    await login(formData)

    // 登录成功,跳转首页
    router.push('/')
  } catch (error) {
    // 登录失败
    loginAttempts.value++
    if (loginAttempts.value >= 3) {
      showCaptcha.value = true
      refreshCaptcha()
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
.login-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.login-header {
  text-align: center;
  margin-bottom: 40px;

  .logo {
    width: 120px;
    height: 60px;
  }

  .title {
    font-size: 28px;
    color: #ffffff;
    margin-top: 16px;
    font-weight: 500;
  }
}

.login-card {
  width: 420px;
  border-radius: 8px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;

    h2 {
      font-size: 24px;
      font-weight: 500;
      color: #333;
      margin: 0;
    }

    .link {
      color: #1890ff;
      text-decoration: none;
      font-size: 14px;

      &:hover {
        text-decoration: underline;
      }
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
  }
}

.login-button {
  width: 100%;
  height: 44px;
  font-size: 16px;
  margin-bottom: 24px;
}

.login-footer {
  margin-top: 40px;
  text-align: center;

  p {
    color: rgba(255, 255, 255, 0.8);
    font-size: 14px;
    margin: 0;
  }
}
</style>
```

#### 2.2.2 ResetPasswordForm.vue

```vue
<template>
  <div class="reset-password-container">
    <el-button
      :icon="ArrowLeft"
      class="back-button"
      @click="router.push('/login')"
    >
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
          <el-form-item label="注册邮箱" prop="account">
            <el-input
              v-model="step1Form.account"
              placeholder="请输入注册邮箱"
              :prefix-icon="Message"
            />
          </el-form-item>

          <el-form-item label="验证码" prop="code">
            <div class="code-input-wrapper">
              <el-input
                v-model="step1Form.code"
                placeholder="请输入验证码"
                maxlength="6"
              />
              <el-button
                :disabled="countdown > 0"
                @click="sendCode"
              >
                {{ countdown > 0 ? `${countdown}s后重发` : '获取验证码' }}
              </el-button>
            </div>
          </el-form-item>

          <el-button
            type="primary"
            size="large"
            class="next-button"
            @click="nextStep"
          >
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
            <div v-if="passwordStrength.hint" class="password-hint">
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
            <el-button
              type="primary"
              size="large"
              @click="resetPassword"
            >
              下一步
            </el-button>
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
            <el-button type="primary" size="large" @click="goToLogin">
              立即登录
            </el-button>
            <p class="countdown-text">
              ({{ countdown }}秒后自动跳转到登录页...)
            </p>
          </template>
        </el-result>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowLeft, Message, Phone } from '@element-plus/icons-vue'

const router = useRouter()
const currentStep = ref(0)
const verifyType = ref('email')
const countdown = ref(0)

const passwordStrength = computed(() => {
  const password = step2Form.newPassword
  if (!password) return { score: 0, text: '', color: '', hint: '' }

  let score = 0
  let hint = []

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
    hint: hint.length > 0 ? hint.join('、') : '✅ 包含大小写字母和数字'
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
  color: #52c41a;
  margin-top: 4px;
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
</style>
```

---

## 3. 交互规范

### 3.1 加载状态

**登录按钮加载**:
```vue
<el-button
  type="primary"
  :loading="loading"
  :disabled="loading"
  @click="handleLogin"
>
  {{ loading ? '登录中...' : '登 录' }}
</el-button>
```

**验证码发送加载**:
```vue
<el-button
  :disabled="countdown > 0 || sending"
  :loading="sending"
  @click="sendCode"
>
  {{ sending ? '发送中...' : countdown > 0 ? `${countdown}s后重发` : '获取验证码' }}
</el-button>
```

### 3.2 错误处理

**表单验证错误**:
```typescript
const handleLogin = async () => {
  try {
    await formRef.value.validate()
    // 提交登录
  } catch (error) {
    // 表单验证失败,自动显示错误提示
    console.log('表单验证失败', error)
  }
}
```

**登录失败处理**:
```typescript
try {
  await login(formData)
  ElMessage.success('登录成功')
} catch (error: any) {
  if (error.code === 1002) {
    // 密码错误
    ElMessage.error({
      message: `密码错误,还剩${error.data.attemptsLeft}次机会`,
      duration: 3000
    })
  } else if (error.code === 1003) {
    // 账号锁定
    ElMessageBox.alert(
      `账号已被锁定,预计${error.data.lockedUntil}解锁`,
      '登录失败',
      { type: 'error' }
    )
  } else {
    ElMessage.error(error.message || '登录失败,请稍后重试')
  }
}
```

### 3.3 操作反馈

**成功提示**:
```typescript
// 登录成功
ElMessage.success({
  message: '登录成功,欢迎回来!',
  duration: 2000
})

// 密码修改成功
ElNotification.success({
  title: '操作成功',
  message: '密码已成功修改,请使用新密码登录',
  duration: 3000
})
```

**失败提示**:
```typescript
// 验证码错误
ElMessage.error({
  message: '验证码错误,请重新输入',
  duration: 3000
})

// 发送频率限制
ElMessage.warning({
  message: '发送过于频繁,请1分钟后再试',
  duration: 3000
})
```

**二次确认**:
```vue
<!-- 退出登录确认 -->
<el-popconfirm
  title="确定要退出登录吗?"
  confirm-button-text="确定"
  cancel-button-text="取消"
  @confirm="handleLogout"
>
  <template #reference>
    <el-button>退出登录</el-button>
  </template>
</el-popconfirm>
```

### 3.4 特殊交互

**密码可见性切换**:
```vue
<el-input
  v-model="password"
  :type="showPassword ? 'text' : 'password'"
  show-password
  @change="(val: boolean) => showPassword = val"
/>
```

**验证码倒计时**:
```typescript
const countdown = ref(0)
const timer = ref<number>()

const sendCode = async () => {
  try {
    await sendVerificationCode(account.value)
    ElMessage.success('验证码已发送')

    // 开始倒计时
    countdown.value = 60
    timer.value = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) {
        clearInterval(timer.value)
      }
    }, 1000)
  } catch (error) {
    ElMessage.error('发送失败,请稍后重试')
  }
}

// 组件卸载时清除定时器
onUnmounted(() => {
  clearInterval(timer.value)
})
```

**回车登录**:
```vue
<el-input
  v-model="password"
  @keyup.enter="handleLogin"
/>
```

**自动跳转**:
```typescript
// 密码修改成功后5秒自动跳转
let seconds = 5
const jumpTimer = setInterval(() => {
  seconds--
  if (seconds <= 0) {
    clearInterval(jumpTimer)
    router.push('/login')
  }
}, 1000)
```

---

## 4. 样式规范

### 4.1 颜色系统

```scss
// 主题色
$primary-color: #1890FF;
$success-color: #52C41A;
$warning-color: #FAAD14;
$error-color: #F5222D;
$info-color: #13C2C2;

// 登录页专用色
$login-bg-start: #667eea;
$login-bg-end: #764ba2;

// 文字颜色
$text-primary: #333333;
$text-regular: #666666;
$text-secondary: #999999;
$text-placeholder: #CCCCCC;

// 边框颜色
$border-base: #DCDFE6;
$border-light: #E4E7ED;

// 背景颜色
$bg-color: #F5F7FA;
$bg-color-page: #FFFFFF;
$card-bg: rgba(255, 255, 255, 0.95);

// 密码强度颜色
$strength-weak: #f5222d;
$strength-medium: #faad14;
$strength-strong: #52c41a;
```

### 4.2 字体系统

```scss
// 字号
$font-size-extra-large: 24px;   // 标题
$font-size-large: 18px;         // 副标题
$font-size-medium: 16px;        // 正文
$font-size-base: 14px;          // 基础
$font-size-small: 12px;         // 小字

// 字重
$font-weight-normal: 400;
$font-weight-medium: 500;
$font-weight-bold: 700;

// 行高
$line-height-base: 1.5;
$line-height-small: 1.2;
```

### 4.3 间距系统

```scss
// 间距
$spacing-extra-small: 8px;
$spacing-small: 12px;
$spacing-base: 16px;
$spacing-large: 24px;
$spacing-extra-large: 32px;

// 组件内边距
$padding-component-vertical: 12px;
$padding-component-horizontal: 16px;
```

### 4.4 圆角系统

```scss
$border-radius-base: 4px;
$border-radius-large: 8px;
$border-radius-circle: 50%;
```

### 4.5 阴影系统

```scss
$box-shadow-card: 0 8px 24px rgba(0, 0, 0, 0.12);
$box-shadow-input: 0 0 0 2px rgba(24, 144, 255, 0.2);
$box-shadow-focus: 0 0 0 2px rgba(24, 144, 255, 0.2);
```

---

## 5. 响应式设计

### 5.1 断点系统

```scss
$breakpoint-xs: 480px;   // 超小屏
$breakpoint-sm: 576px;   // 小屏
$breakpoint-md: 768px;   // 中屏
$breakpoint-lg: 992px;   // 大屏
$breakpoint-xl: 1200px;  // 超大屏
```

### 5.2 响应式布局

**登录卡片响应式**:
```vue
<template>
  <el-card :class="['login-card', responsiveClass]">
    <!-- 登录表单 -->
  </el-card>
</template>

<style scoped lang="scss">
.login-card {
  width: 420px;

  @media (max-width: 480px) {
    width: 100%;
    margin: 0 16px;
  }
}
</style>
```

**表单项响应式**:
```vue
<template>
  <div class="form-actions">
    <el-checkbox v-model="remember">记住我</el-checkbox>
    <router-link to="/forgot-password">忘记密码?</router-link>
  </div>
</template>

<style scoped lang="scss">
.form-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;

  @media (max-width: 480px) {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
}
</style>
```

**第三方登录响应式**:
```scss
.third-party-buttons {
  display: flex;
  gap: 12px;
  justify-content: center;

  @media (max-width: 480px) {
    flex-direction: column;

    .third-party-button {
      width: 100%;
    }
  }
}
```

### 5.3 移动端优化

**移动端专用样式**:
```scss
@media (max-width: 768px) {
  .login-container {
    padding: 12px;
  }

  .login-card {
    width: 100%;
    box-shadow: none;
  }

  .login-header {
    .logo {
      width: 80px;
      height: 40px;
    }

    .title {
      font-size: 20px;
    }
  }

  .form-actions {
    font-size: 14px;
  }

  .login-button {
    height: 48px; // 更大的点击区域
    font-size: 18px;
  }
}
```

**触摸优化**:
```vue
<el-button
  size="large"
  class="touch-optimized"
>
  登 录
</el-button>

<style scoped>
.touch-optimized {
  min-height: 48px; // iOS推荐最小触摸区域
  min-width: 48px;
}
</style>
```

---

**文档版本**: v1.0.0
**创建人**: AI开发助手
**最后更新**: 2026-01-10
