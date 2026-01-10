/**
 * 登录认证模块 - Mock API
 * 用于开发和测试
 */

import type {
  LoginRequest,
  LoginResponse,
  RefreshTokenRequest,
  RefreshTokenResponse,
  CaptchaResponse,
  ApiResponse,
} from '../types'

// 模拟延迟
const delay = (ms: number = 500) => new Promise((resolve) => setTimeout(resolve, ms))

// 模拟用户数据库
const mockUsers = [
  {
    username: 'admin',
    employeeNo: 'admin', // 添加简短员工编号用于登录
    password: 'Admin123',
    id: 'EMP20260109001',
    name: '系统管理员',
    email: 'admin@company.com',
    phone: '13800138000',
    avatar: '',
    departmentId: 'DEPT001',
    departmentName: '技术部',
    position: '系统管理员',
    roles: [
      { id: '1', name: '系统管理员', code: 'admin', type: 'system' as const },
    ],
    permissions: ['*'], // 所有权限
  },
  {
    username: 'zhangsan',
    employeeNo: 'zhangsan', // 添加简短员工编号用于登录
    password: 'Password123',
    id: 'EMP20260109002',
    name: '张三',
    email: 'zhangsan@company.com',
    phone: '13800138001',
    avatar: '',
    departmentId: 'DEPT001',
    departmentName: '技术部',
    position: '软件工程师',
    roles: [
      { id: '2', name: '普通员工', code: 'employee', type: 'custom' as const },
    ],
    permissions: ['employee:view', 'employee:create', 'dict:view'],
  },
  {
    username: 'lisi',
    employeeNo: 'lisi', // 添加简短员工编号用于登录
    password: 'Password123',
    id: 'EMP20260109003',
    name: '李四',
    email: 'lisi@company.com',
    phone: '13800138002',
    avatar: '',
    departmentId: 'DEPT002',
    departmentName: '人事部',
    position: 'HR专员',
    roles: [
      { id: '3', name: 'HR', code: 'hr', type: 'department' as const },
    ],
    permissions: ['employee:view', 'employee:create', 'employee:edit', 'employee:delete', 'dict:view'],
  },
]

// 记录登录失败次数
const loginAttempts = new Map<string, number>()

/**
 * 生成Mock Token
 */
function generateToken(expiresIn: number = 7200): string {
  const header = btoa(JSON.stringify({ alg: 'HS256', typ: 'JWT' }))
  const payload = btoa(
    JSON.stringify({
      exp: Date.now() + expiresIn * 1000,
      iat: Date.now(),
    })
  )
  const signature = btoa('mock-signature')
  return `${header}.${payload}.${signature}`
}

/**
 * 生成Mock UUID
 */
function generateUUID(): string {
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, (c) => {
    const r = (Math.random() * 16) | 0
    const v = c === 'x' ? r : (r & 0x3) | 0x8
    return v.toString(16)
  })
}

/**
 * Mock用户登录
 */
export async function mockLogin(data: LoginRequest): Promise<ApiResponse<LoginResponse>> {
  await delay(800) // 模拟网络延迟

  const { username, password } = data

  // 查找用户 - 支持员工编号、用户名、邮箱、手机号登录
  const user = mockUsers.find(
    (u) => u.username === username || u.employeeNo === username || u.email === username || u.phone === username
  )

  console.log('[Mock Login] 查找用户:', username, '找到用户:', user ? user.name : '未找到')

  // 验证密码
  if (!user || user.password !== password) {
    // 记录失败次数
    const attempts = (loginAttempts.get(username) || 0) + 1
    loginAttempts.set(username, attempts)

    // 检查是否需要锁定(5次)
    if (attempts >= 5) {
      return {
        code: 1003,
        message: '账号已锁定,请30分钟后再试',
        data: {
          lockedUntil: new Date(Date.now() + 30 * 60 * 1000).toLocaleString(),
        },
      }
    }

    return {
      code: 1002,
      message: '密码错误,请重新输入',
      data: {
        attemptsLeft: 5 - attempts,
      },
    }
  }

  // 登录成功,重置失败次数
  loginAttempts.delete(username)

  // 生成Token
  const accessToken = generateToken(7200) // 2小时
  const refreshToken = generateUUID()

  return {
    code: 200,
    message: '登录成功',
    data: {
      accessToken,
      refreshToken,
      tokenType: 'Bearer',
      expiresIn: 7200,
      userInfo: user,
    },
  }
}

/**
 * Mock刷新Token
 */
export async function mockRefreshToken(
  data: RefreshTokenRequest
): Promise<ApiResponse<RefreshTokenResponse>> {
  await delay(300)

  // 简单验证Refresh Token格式(UUID)
  const uuidRegex = /^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i
  if (!uuidRegex.test(data.refreshToken)) {
    return {
      code: 1005,
      message: 'Refresh Token无效或已过期',
    }
  }

  const accessToken = generateToken(7200)
  const refreshToken = generateUUID()

  return {
    code: 200,
    message: '刷新成功',
    data: {
      accessToken,
      refreshToken,
      expiresIn: 7200,
    },
  }
}

/**
 * Mock获取验证码
 */
export async function mockGetCaptcha(): Promise<ApiResponse<CaptchaResponse>> {
  await delay(200)

  const captchaKey = `captcha_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`
  const captchaText = Math.random().toString(36).substr(2, 4).toUpperCase()

  // 简单的SVG验证码
  const svg = `
    <svg width="120" height="40" xmlns="http://www.w3.org/2000/svg">
      <rect width="120" height="40" fill="#f2f6fc"/>
      <text x="50%" y="50%" font-size="24" font-family="Arial" fill="#333" text-anchor="middle" dominant-baseline="middle">${captchaText}</text>
    </svg>
  `

  const captchaImage = `data:image/svg+xml;base64,${btoa(svg)}`

  return {
    code: 200,
    message: 'success',
    data: {
      captchaKey,
      captchaImage,
    },
  }
}

/**
 * Mock退出登录
 */
export async function mockLogout(): Promise<ApiResponse<void>> {
  await delay(300)
  return {
    code: 200,
    message: '退出成功',
  }
}

/**
 * Mock发送验证码
 */
export async function mockSendCode(data: { type: string; account: string; scene: string }): Promise<ApiResponse<void>> {
  await delay(500)

  // 模拟验证码发送频率限制
  const key = `${data.type}_${data.account}_${data.scene}`
  const lastSent = localStorage.getItem(`last_sent_${key}`)

  if (lastSent && Date.now() - parseInt(lastSent) < 60000) {
    return {
      code: 1006,
      message: '发送过于频繁,请1分钟后再试',
    }
  }

  // 记录发送时间
  localStorage.setItem(`last_sent_${key}`, Date.now().toString())

  // 保存验证码(用于测试,实际应该发送到邮箱/手机)
  const verifyCode = Math.floor(100000 + Math.random() * 900000).toString()
  localStorage.setItem(`verify_code_${data.account}`, verifyCode)

  console.log(`[Mock] 验证码: ${verifyCode}`) // 方便测试

  return {
    code: 200,
    message: '验证码已发送,请注意查收',
  }
}

/**
 * Mock重置密码
 */
export async function mockResetPassword(data: {
  type: string
  account: string
  code: string
  newPassword: string
}): Promise<ApiResponse<void>> {
  await delay(800)

  // 验证验证码
  const storedCode = localStorage.getItem(`verify_code_${data.account}`)
  if (!storedCode || storedCode !== data.code) {
    return {
      code: 1010,
      message: '验证码错误或已过期',
    }
  }

  // 清除验证码
  localStorage.removeItem(`verify_code_${data.account}`)

  // 更新用户密码
  const user = mockUsers.find((u) => u.email === data.account || u.phone === data.account)
  if (user) {
    user.password = data.newPassword
  }

  return {
    code: 200,
    message: '密码修改成功,请使用新密码登录',
  }
}

/**
 * 配置Mock API拦截器
 */
export function setupMockApi() {
  // 在开发环境下启用Mock数据
  if (import.meta.env.DEV) {
    console.log('[Mock] Mock API已启用')
    console.log('[Mock] 测试账号 (支持员工编号/用户名/邮箱/手机号登录):')
    console.log('  管理员 - admin / Admin123')
    console.log('  员工   - zhangsan / Password123')
    console.log('  HR     - lisi / Password123')
  }
}

// 导出Mock函数供axios适配器使用
export const mockApiHandlers = {
  login: mockLogin,
  logout: mockLogout,
  refreshToken: mockRefreshToken,
  getCaptcha: mockGetCaptcha,
  sendCode: mockSendCode,
  resetPassword: mockResetPassword,
}
