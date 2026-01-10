/**
 * 登录认证模块 - TypeScript类型定义
 * 基于 specs/auth/login/login_Technical.md 第1节
 */

/**
 * 登录请求参数
 */
export interface LoginRequest {
  /** 账号(员工编号/邮箱/手机号) */
  username: string
  /** 密码 */
  password: string
  /** 验证码(连续失败3次后必填) */
  captcha?: string
  /** 验证码Key */
  captchaKey?: string
  /** 是否记住登录 */
  remember?: boolean
}

/**
 * 登录响应数据
 */
export interface LoginResponse {
  /** 访问Token */
  accessToken: string
  /** 刷新Token */
  refreshToken: string
  /** Token类型(固定为Bearer) */
  tokenType: 'Bearer'
  /** 过期时间(秒) */
  expiresIn: number
  /** 用户信息 */
  userInfo: UserInfo
}

/**
 * 用户信息
 */
export interface UserInfo {
  /** 用户ID */
  id: string
  /** 员工编号 */
  employeeNo: string
  /** 姓名 */
  name: string
  /** 邮箱 */
  email: string
  /** 手机号 */
  phone: string
  /** 头像 */
  avatar?: string
  /** 部门ID */
  departmentId: string
  /** 部门名称 */
  departmentName: string
  /** 职位 */
  position: string
  /** 角色列表 */
  roles: Role[]
  /** 权限列表 */
  permissions: string[]
}

/**
 * 角色信息
 */
export interface Role {
  /** 角色ID */
  id: string
  /** 角色名称 */
  name: string
  /** 角色编码 */
  code: string
  /** 角色类型 */
  type: 'system' | 'department' | 'custom'
}

/**
 * Token刷新请求
 */
export interface RefreshTokenRequest {
  /** 刷新Token */
  refreshToken: string
}

/**
 * Token刷新响应
 */
export interface RefreshTokenResponse {
  /** 新的访问Token */
  accessToken: string
  /** 新的刷新Token */
  refreshToken: string
  /** 过期时间(秒) */
  expiresIn: number
}

/**
 * 找回密码请求
 */
export interface ResetPasswordRequest {
  /** 找回方式(email/mobile) */
  type: 'email' | 'mobile'
  /** 邮箱或手机号 */
  account: string
  /** 验证码 */
  code: string
  /** 新密码 */
  newPassword: string
}

/**
 * 发送验证码请求
 */
export interface SendCodeRequest {
  /** 发送类型(email/mobile) */
  type: 'email' | 'mobile'
  /** 邮箱或手机号 */
  account: string
  /** 验证类型(forgot_password/login) */
  scene: 'forgot_password' | 'login'
}

/**
 * 登录日志
 */
export interface LoginLog {
  /** 日志ID */
  id: string
  /** 用户ID */
  userId: string
  /** 用户名 */
  username: string
  /** 登录IP */
  ip: string
  /** 登录地点 */
  location?: string
  /** 设备信息 */
  device: string
  /** 浏览器 */
  browser: string
  /** 操作系统 */
  os: string
  /** 登录状态 */
  status: 'success' | 'failed'
  /** 失败原因 */
  failureReason?: string
  /** 登录时间 */
  loginTime: string
}

/**
 * 用户会话
 */
export interface UserSession {
  /** 会话ID */
  id: string
  /** 用户ID */
  userId: string
  /** Access Token */
  accessToken: string
  /** Refresh Token */
  refreshToken: string
  /** 过期时间 */
  expiresAt: string
  /** 登录IP */
  ip: string
  /** 设备信息 */
  device: string
  /** 登录时间 */
  loginTime: string
  /** 最后活跃时间 */
  lastActiveTime: string
}

/**
 * 验证码响应
 */
export interface CaptchaResponse {
  /** 验证码Key */
  captchaKey: string
  /** 验证码图片(Base64) */
  captchaImage: string
}

/**
 * API响应基础格式
 */
export interface ApiResponse<T = any> {
  /** 响应码 */
  code: number
  /** 响应消息 */
  message: string
  /** 响应数据 */
  data?: T
}

/**
 * 登录错误响应数据
 */
export interface LoginErrorData {
  /** 剩余尝试次数 */
  attemptsLeft?: number
  /** 锁定到期时间 */
  lockedUntil?: string
}
