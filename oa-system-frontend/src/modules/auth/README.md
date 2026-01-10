# 登录认证模块

> 严格按照 `specs/auth/login/` 三层架构规范实现

## 📋 实现内容

### ✅ 已完成功能

#### 1. TypeScript类型定义
- **文件**: `src/modules/auth/types/index.ts`
- **内容**:
  - `LoginRequest` - 登录请求参数
  - `LoginResponse` - 登录响应数据
  - `UserInfo` - 用户信息
  - `Role` - 角色信息
  - `RefreshTokenRequest/Response` - Token刷新
  - `ResetPasswordRequest` - 重置密码
  - `SendCodeRequest` - 发送验证码
  - `LoginLog` - 登录日志
  - `UserSession` - 用户会话
  - `CaptchaResponse` - 验证码响应
  - `ApiResponse` - API响应基础格式

#### 2. API接口封装
- **文件**: `src/modules/auth/api/index.ts`
- **接口**:
  - `login()` - 用户登录
  - `logout()` - 退出登录
  - `refreshToken()` - 刷新Token
  - `getCaptcha()` - 获取验证码
  - `sendCode()` - 发送验证码
  - `resetPassword()` - 重置密码
  - `getSessions()` - 获取活跃会话
  - `deleteSession()` - 删除会话
  - `getLoginLogs()` - 获取登录日志

#### 3. Pinia状态管理
- **文件**: `src/modules/auth/store/index.ts`
- **状态**:
  - `userInfo` - 用户信息
  - `accessToken` - 访问Token
  - `refreshToken` - 刷新Token
  - `isLoggedIn` - 是否已登录
  - `userRoles` - 用户角色列表
  - `userPermissions` - 用户权限列表
  - `isAdmin` - 是否管理员
- **方法**:
  - `login()` - 登录
  - `logout()` - 退出登录
  - `refreshAccessToken()` - 刷新Token
  - `fetchUserInfo()` - 获取用户信息
  - `isAuthenticated()` - 检查认证状态
  - `hasPermission()` - 检查权限
  - `hasRole()` - 检查角色
  - `clearAuthState()` - 清除认证状态

#### 4. Token存储工具
- **文件**: `src/modules/auth/utils/token.ts`
- **功能**:
  - Token存储到LocalStorage
  - Token过期时间管理
  - Token即将过期检查(剩余5分钟)
  - Token已过期检查

#### 5. 登录表单组件
- **文件**: `src/modules/auth/components/LoginForm.vue`
- **功能**:
  - 账号密码输入
  - 支持员工编号/邮箱/手机号登录
  - 记住我功能
  - 忘记密码链接
  - 验证码(失败3次后显示)
  - 表单验证
  - 错误处理
  - 响应式设计

#### 6. 重置密码组件
- **文件**: `src/modules/auth/components/ResetPasswordForm.vue`
- **功能**:
  - 三步骤流程(验证身份 → 重置密码 → 完成)
  - 邮箱/手机验证方式选择
  - 验证码发送与倒计时
  - 密码强度检测
  - 密码实时验证
  - 5秒自动跳转登录页

#### 7. 页面视图
- **文件**:
  - `src/modules/auth/views/Login.vue` - 登录页面
  - `src/modules/auth/views/ResetPassword.vue` - 重置密码页面
- **设计**:
  - 居中卡片布局
  - 渐变背景
  - 响应式设计
  - 符合UI/UX设计规范

#### 8. 路由配置
- **路由**:
  - `/login` - 登录页
  - `/reset-password` - 重置密码页
- **守卫**:
  - 已登录用户访问登录页自动跳转首页
  - 未登录用户访问其他页面跳转登录页
  - 支持redirect参数(登录后跳转回原页面)

#### 9. Axios拦截器
- **请求拦截器**:
  - 自动添加Token到请求头
  - `Authorization: Bearer ${token}`
- **响应拦截器**:
  - 401错误自动刷新Token
  - Token刷新失败自动跳转登录页
  - 统一错误处理

## 📂 文件结构

```
src/modules/auth/
├── types/
│   └── index.ts              # TypeScript类型定义
├── api/
│   └── index.ts              # API接口封装
├── store/
│   └── index.ts              # Pinia Store
├── utils/
│   └── token.ts              # Token存储工具
├── components/
│   ├── LoginForm.vue         # 登录表单组件
│   └── ResetPasswordForm.vue # 重置密码表单组件
├── views/
│   ├── Login.vue             # 登录页面
│   └── ResetPassword.vue     # 重置密码页面
└── index.ts                  # 统一导出
```

## 🚀 使用方法

### 1. 登录

```typescript
import { useAuthStore } from '@/modules/auth'

const authStore = useAuthStore()

try {
  await authStore.login({
    username: 'zhangsan',
    password: 'Password123',
    remember: true
  })
  // 登录成功,自动跳转
} catch (error) {
  // 登录失败
  console.error(error)
}
```

### 2. 退出登录

```typescript
import { useAuthStore } from '@/modules/auth'

const authStore = useAuthStore()
await authStore.logout()
```

### 3. 检查权限

```typescript
import { useAuthStore } from '@/modules/auth'

const authStore = useAuthStore()

// 检查单个权限
if (authStore.hasPermission('employee:view')) {
  // 有权限
}

// 检查角色
if (authStore.hasRole('admin')) {
  // 是管理员
}

// 检查是否有任意权限
if (authStore.hasAnyPermission(['employee:view', 'employee:edit'])) {
  // 有至少一个权限
}
```

### 4. 在组件中使用

```vue
<script setup lang="ts">
import { useAuthStore } from '@/modules/auth'

const authStore = useAuthStore()

// 用户信息
const userInfo = computed(() => authStore.userInfo)

// 是否已登录
const isLoggedIn = computed(() => authStore.isLoggedIn)

// 是否管理员
const isAdmin = computed(() => authStore.isAdmin)
</script>

<template>
  <div v-if="isLoggedIn">
    欢迎你,{{ userInfo?.name }}!
  </div>
</template>
```

## 🔐 安全特性

### 1. Token安全
- ✅ Access Token存储在LocalStorage
- ✅ Token有效期管理(2小时)
- ✅ Refresh Token支持(7天)
- ✅ Token即将过期自动刷新
- ✅ Token过期后自动跳转登录页

### 2. 密码安全
- ✅ 密码长度验证(8-20位)
- ✅ 密码复杂度验证(大小写字母+数字)
- ✅ 密码强度实时检测
- ✅ 新密码与确认密码一致性验证

### 3. 登录安全
- ✅ 密码连续错误5次锁定账号
- ✅ 连续失败3次显示验证码
- ✅ 验证码有效期5分钟
- ✅ 验证码发送频率限制(60秒)

### 4. 请求安全
- ✅ 所有API请求自动携带Token
- ✅ 401错误自动刷新Token并重试
- ✅ 刷新失败自动清除状态并跳转登录页
- ✅ 统一错误处理和提示

## 📝 后续开发建议

### 后端API对接
目前所有API接口都已定义好,需要后端实现以下接口:

1. `POST /api/auth/login` - 用户登录
2. `POST /api/auth/logout` - 退出登录
3. `POST /api/auth/refresh` - 刷新Token
4. `GET /api/auth/captcha` - 获取验证码
5. `POST /api/auth/send-code` - 发送验证码
6. `POST /api/auth/reset-password` - 重置密码
7. `GET /api/auth/sessions` - 获取活跃会话
8. `DELETE /api/auth/sessions/:id` - 删除会话
9. `GET /api/auth/login-logs` - 获取登录日志

### 待实现功能(P2)
根据规范,以下功能为P2优先级,可选实现:
- 异地登录检测
- 二次验证(2FA)
- 多设备管理
- 登录行为分析

## 📚 相关文档

- [功能需求规范](../../../../../specs/auth/login/login_Functional.md)
- [技术实现规范](../../../../../specs/auth/login/login_Technical.md)
- [UI/UX设计规范](../../../../../specs/auth/login/login_Design.md)

---

**版本**: v1.0.0
**创建日期**: 2026-01-10
**实现规范**: `specs/auth/login/` 三层架构
