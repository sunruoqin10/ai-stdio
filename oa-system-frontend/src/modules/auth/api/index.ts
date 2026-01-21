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

// 修改密码请求参数接口
interface ChangePasswordRequest {
  currentPassword: string
  newPassword: string
}

/**
 * 用户登录
 * @param data 登录请求参数
 * @returns 登录响应数据
 */
export async function login(data: LoginRequest): Promise<ApiResponse<LoginResponse>> {
  return http.post<ApiResponse<LoginResponse>>('/auth/login', data)
}

/**
 * 退出登录
 * @returns 退出响应
 */
export async function logout(): Promise<ApiResponse<void>> {
  return http.post<ApiResponse<void>>('/auth/logout')
}

/**
 * 刷新Token
 * @param data 刷新Token请求参数
 * @returns 新Token信息
 */
export async function refreshToken(data: RefreshTokenRequest): Promise<ApiResponse<RefreshTokenResponse>> {
  return http.post<ApiResponse<RefreshTokenResponse>>('/auth/refresh', data)
}

/**
 * 获取图形验证码
 * @returns 验证码数据
 */
export async function getCaptcha(): Promise<ApiResponse<CaptchaResponse>> {
  return http.get<ApiResponse<CaptchaResponse>>('/auth/captcha')
}

/**
 * 发送验证码(邮箱/手机)
 * @param data 发送验证码请求参数
 * @returns 发送结果
 */
export async function sendCode(data: SendCodeRequest): Promise<ApiResponse<void>> {
  return http.post<ApiResponse<void>>('/auth/send-code', data)
}

/**
 * 重置密码
 * @param data 重置密码请求参数
 * @returns 重置结果
 */
export async function resetPassword(data: ResetPasswordRequest): Promise<ApiResponse<void>> {
  return http.post<ApiResponse<void>>('/auth/reset-password', data)
}

/**
 * 获取活跃会话列表
 * @param page 页码
 * @param size 每页数量
 * @returns 会话列表
 */
export function getSessions(page: number = 1, size: number = 10): Promise<ApiResponse<{ records: UserSession[], total: number, size: number, current: number, pages: number }>> {
  return http.get<ApiResponse<{ records: UserSession[], total: number, size: number, current: number, pages: number }>>('/auth/sessions', {
    params: { page, size }
  })
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
 * @param page 页码
 * @param size 每页数量
 * @param startDate 开始日期
 * @param endDate 结束日期
 * @returns 登录日志列表
 */
export function getLoginLogs(
  page: number = 1,
  size: number = 10,
  startDate?: string,
  endDate?: string
): Promise<ApiResponse<{ records: LoginLog[], total: number, size: number, current: number, pages: number }>> {
  return http.get<ApiResponse<{ records: LoginLog[], total: number, size: number, current: number, pages: number }>>('/auth/login-logs', {
    params: { page, size, startDate, endDate }
  })
}

/**
 * 修改密码
 * @param data 修改密码请求参数
 * @returns 修改结果
 */
export async function changePassword(data: ChangePasswordRequest): Promise<ApiResponse<void>> {
  return http.post<ApiResponse<void>>('/auth/change-password', data)
}
