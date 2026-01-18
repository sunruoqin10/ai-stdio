/**
 * 用户认证状态管理 - Pinia Store
 * 基于 specs/auth/login/login_Technical.md 第1节和第5节
 */

import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { UserInfo, LoginRequest, LoginResponse } from '../types'
import * as authApi from '../api'
import {
  setAccessToken,
  getAccessToken,
  setRefreshToken,
  getRefreshToken,
  clearTokens,
  isTokenExpiringSoon,
  isTokenExpired,
} from '../utils/token'

export const useAuthStore = defineStore('auth', () => {
  // ==================== 状态 ====================

  /** 用户信息 */
  const userInfo = ref<UserInfo | null>(null)

  /** Access Token */
  const accessToken = ref<string | null>(getAccessToken())

  /** Refresh Token */
  const refreshToken = ref<string | null>(getRefreshToken())

  /** 是否已登录 */
  const isLoggedIn = computed(() => !!accessToken.value && !!userInfo.value)

  /** 用户角色列表 */
  const userRoles = computed(() => userInfo.value?.roles || [])

  /** 用户权限列表 */
  const userPermissions = computed(() => userInfo.value?.permissions || [])

  /** 是否有管理员角色 */
  const isAdmin = computed(() =>
    userRoles.value.some((role) => role.code === 'admin' || role.type === 'system')
  )

  // ==================== Actions ====================

  /**
   * 用户登录
   * @param loginData 登录数据
   * @returns 登录响应
   */
  async function login(loginData: LoginRequest): Promise<LoginResponse> {
    try {
      const response = await authApi.login(loginData)

      if (response.code === 200 && response.data) {
        const { accessToken: newToken, refreshToken: newRefreshToken, userInfo: newUserInfo } =
          response.data

        // 存储Token
        setAccessToken(newToken, response.data.expiresIn)
        setRefreshToken(newRefreshToken)

        // 存储用户信息到 localStorage
        localStorage.setItem('user_info', JSON.stringify(newUserInfo))
        
        // 存储用户ID到 localStorage(用于权限管理)
        localStorage.setItem('userId', newUserInfo.id)

        // 更新状态
        accessToken.value = newToken
        refreshToken.value = newRefreshToken
        userInfo.value = newUserInfo

        return response.data
      } else {
        throw new Error(response.message || '登录失败')
      }
    } catch (error: any) {
      console.error('登录失败:', error)
      throw error
    }
  }

  /**
   * 退出登录
   */
  async function logout() {
    try {
      // 调用退出接口
      await authApi.logout()
    } catch (error) {
      console.error('退出登录失败:', error)
    } finally {
      // 清除本地状态
      clearAuthState()
    }
  }

  /**
   * 刷新Token
   * @returns 是否刷新成功
   */
  async function refreshAccessToken(): Promise<boolean> {
    try {
      const currentRefreshToken = getRefreshToken()

      if (!currentRefreshToken) {
        throw new Error('没有Refresh Token')
      }

      const response = await authApi.refreshToken({ refreshToken: currentRefreshToken })

      if (response.code === 200 && response.data) {
        const { accessToken: newToken, refreshToken: newRefreshToken } = response.data

        // 更新Token
        setAccessToken(newToken, response.data.expiresIn)
        setRefreshToken(newRefreshToken)

        // 如果用户信息存在,更新localStorage
        if (userInfo.value) {
          localStorage.setItem('user_info', JSON.stringify(userInfo.value))
        }

        // 更新状态
        accessToken.value = newToken
        refreshToken.value = newRefreshToken

        return true
      } else {
        throw new Error(response.message || 'Token刷新失败')
      }
    } catch (error: any) {
      console.error('Token刷新失败:', error)
      // 刷新失败,清除认证状态
      clearAuthState()
      return false
    }
  }

  /**
   * 获取用户信息(用于Token已存在但页面刷新后恢复用户信息)
   */
  async function fetchUserInfo(): Promise<void> {
    // TODO: 实现获取用户信息的API
    // const response = await authApi.getUserInfo()
    // userInfo.value = response.data

    // 从localStorage恢复用户信息
    const storedUserInfo = localStorage.getItem('user_info')
    if (storedUserInfo) {
      try {
        userInfo.value = JSON.parse(storedUserInfo)
      } catch (error) {
        console.error('解析用户信息失败:', error)
      }
    }
  }

  /**
   * 同步恢复用户信息(用于初始化时立即恢复)
   */
  function restoreUserInfo(): void {
    const storedUserInfo = localStorage.getItem('user_info')
    
    if (storedUserInfo) {
      try {
        userInfo.value = JSON.parse(storedUserInfo)
        
        // 确保userId也设置到localStorage
        if (userInfo.value?.id) {
          localStorage.setItem('userId', userInfo.value.id)
        }
      } catch (error) {
        console.error('解析用户信息失败:', error)
      }
    }
  }

  /**
   * 检查Token是否需要刷新
   * @returns 是否需要刷新
   */
  function shouldRefreshToken(): boolean {
    return isTokenExpiringSoon() && !!refreshToken.value
  }

  /**
   * 检查是否已认证
   * @returns 是否已认证
   */
  function isAuthenticated(): boolean {
    if (!accessToken.value) return false
    if (isTokenExpired()) return false
    if (!userInfo.value) return false
    return true
  }

  /**
   * 清除认证状态
   */
  function clearAuthState() {
    // 清除Token
    clearTokens()

    // 清除状态
    accessToken.value = null
    refreshToken.value = null
    userInfo.value = null

    // 清除用户信息缓存
    localStorage.removeItem('user_info')
    localStorage.removeItem('userId')
  }

  /**
   * 检查用户是否有指定权限
   * @param permission 权限码
   * @returns 是否有权限
   */
  function hasPermission(permission: string): boolean {
    return userPermissions.value.includes(permission)
  }

  /**
   * 检查用户是否有指定角色
   * @param roleCode 角色编码
   * @returns 是否有角色
   */
  function hasRole(roleCode: string): boolean {
    return userRoles.value.some((role) => role.code === roleCode)
  }

  /**
   * 检查用户是否有任意一个权限
   * @param permissions 权限码数组
   * @returns 是否有任意权限
   */
  function hasAnyPermission(permissions: string[]): boolean {
    return permissions.some((permission) => hasPermission(permission))
  }

  /**
   * 检查用户是否有所有权限
   * @param permissions 权限码数组
   * @returns 是否有所有权限
   */
  function hasAllPermissions(permissions: string[]): boolean {
    return permissions.every((permission) => hasPermission(permission))
  }

  // ==================== 初始化 ====================

  // 如果Token存在,尝试恢复用户信息(使用同步恢复,确保立即生效)
  if (accessToken.value && !isTokenExpired()) {
    restoreUserInfo()
  } else if (isTokenExpired()) {
    clearAuthState()
  }

  return {
    // 状态
    userInfo,
    accessToken,
    refreshToken,
    isLoggedIn,
    userRoles,
    userPermissions,
    isAdmin,

    // Actions
    login,
    logout,
    refreshAccessToken,
    fetchUserInfo,
    restoreUserInfo,
    shouldRefreshToken,
    isAuthenticated,
    clearAuthState,
    hasPermission,
    hasRole,
    hasAnyPermission,
    hasAllPermissions,
  }
})
