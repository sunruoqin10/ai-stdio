/**
 * 登录认证模块 - API接口封装
 * 基于 specs/auth/login/login_Technical.md 第2节
 */

import { http } from '@/utils/request'
import type {
  LoginRequest,
  LoginResponse,
  RefreshTokenRequest,
  RefreshTokenResponse,
  ResetPasswordRequest,
  SendCodeRequest,
  CaptchaResponse,
  ApiResponse,
  UserSession,
  LoginLog,
} from '../types'

/**
 * 用户登录
 * @param data 登录请求参数
 * @returns 登录响应数据
 */
export async function login(data: LoginRequest): Promise<ApiResponse<LoginResponse>> {
  // 开发环境使用Mock数据
  if (import.meta.env.DEV) {
    const { mockApiHandlers } = await import('./mock')
    console.log('[Mock API] login', data)
    return await mockApiHandlers.login(data)
  }
  return http.post<ApiResponse<LoginResponse>>('/auth/login', data)
}

/**
 * 退出登录
 * @returns 退出响应
 */
export async function logout(): Promise<ApiResponse<void>> {
  // 开发环境使用Mock数据
  if (import.meta.env.DEV) {
    const { mockApiHandlers } = await import('./mock')
    console.log('[Mock API] logout')
    return await mockApiHandlers.logout()
  }
  return http.post<ApiResponse<void>>('/auth/logout')
}

/**
 * 刷新Token
 * @param data 刷新Token请求参数
 * @returns 新Token信息
 */
export async function refreshToken(data: RefreshTokenRequest): Promise<ApiResponse<RefreshTokenResponse>> {
  // 开发环境使用Mock数据
  if (import.meta.env.DEV) {
    const { mockApiHandlers } = await import('./mock')
    console.log('[Mock API] refreshToken')
    return await mockApiHandlers.refreshToken(data)
  }
  return http.post<ApiResponse<RefreshTokenResponse>>('/auth/refresh', data)
}

/**
 * 获取图形验证码
 * @returns 验证码数据
 */
export async function getCaptcha(): Promise<ApiResponse<CaptchaResponse>> {
  // 开发环境使用Mock数据
  if (import.meta.env.DEV) {
    const { mockApiHandlers } = await import('./mock')
    console.log('[Mock API] getCaptcha')
    return await mockApiHandlers.getCaptcha()
  }
  return http.get<ApiResponse<CaptchaResponse>>('/auth/captcha')
}

/**
 * 发送验证码(邮箱/手机)
 * @param data 发送验证码请求参数
 * @returns 发送结果
 */
export async function sendCode(data: SendCodeRequest): Promise<ApiResponse<void>> {
  // 开发环境使用Mock数据
  if (import.meta.env.DEV) {
    const { mockApiHandlers } = await import('./mock')
    console.log('[Mock API] sendCode', data)
    return await mockApiHandlers.sendCode(data)
  }
  return http.post<ApiResponse<void>>('/auth/send-code', data)
}

/**
 * 重置密码
 * @param data 重置密码请求参数
 * @returns 重置结果
 */
export async function resetPassword(data: ResetPasswordRequest): Promise<ApiResponse<void>> {
  // 开发环境使用Mock数据
  if (import.meta.env.DEV) {
    const { mockApiHandlers } = await import('./mock')
    console.log('[Mock API] resetPassword', data)
    return await mockApiHandlers.resetPassword(data)
  }
  return http.post<ApiResponse<void>>('/auth/reset-password', data)
}

/**
 * 获取活跃会话列表
 * @returns 会话列表
 */
export function getSessions(): Promise<ApiResponse<{ total: number; list: UserSession[] }>> {
  return http.get<ApiResponse<{ total: number; list: UserSession[] }>>('/auth/sessions')
}

/**
 * 删除指定会话
 * @param sessionId 会话ID
 * @returns 删除结果
 */
export function deleteSession(sessionId: string): Promise<ApiResponse<void>> {
  return http.delete<ApiResponse<void>>(`/auth/sessions/${sessionId}`)
}

/**
 * 获取登录日志
 * @param params 查询参数
 * @returns 登录日志列表
 */
export function getLoginLogs(params?: {
  page?: number
  pageSize?: number
  startDate?: string
  endDate?: string
}): Promise<ApiResponse<{ total: number; list: LoginLog[] }>> {
  return http.get<ApiResponse<{ total: number; list: LoginLog[] }>>('/auth/login-logs', {
    params,
  })
}
