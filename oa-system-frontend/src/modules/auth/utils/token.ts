/**
 * Token存储工具
 * 用于管理Access Token和Refresh Token的存储和获取
 */

const TOKEN_KEY = 'access_token'
const REFRESH_TOKEN_KEY = 'refresh_token'
const TOKEN_EXPIRES_KEY = 'token_expires'

/**
 * 存储Access Token
 * @param token 访问Token
 * @param expiresIn 过期时间(秒)
 */
export function setAccessToken(token: string, expiresIn: number): void {
  const expiresAt = Date.now() + expiresIn * 1000
  localStorage.setItem(TOKEN_KEY, token)
  localStorage.setItem(TOKEN_EXPIRES_KEY, expiresAt.toString())
}

/**
 * 获取Access Token
 * @returns Access Token或null
 */
export function getAccessToken(): string | null {
  return localStorage.getItem(TOKEN_KEY)
}

/**
 * 存储Refresh Token
 * @param token 刷新Token
 */
export function setRefreshToken(token: string): void {
  localStorage.setItem(REFRESH_TOKEN_KEY, token)
}

/**
 * 获取Refresh Token
 * @returns Refresh Token或null
 */
export function getRefreshToken(): string | null {
  return localStorage.getItem(REFRESH_TOKEN_KEY)
}

/**
 * 清除所有Token
 */
export function clearTokens(): void {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(REFRESH_TOKEN_KEY)
  localStorage.removeItem(TOKEN_EXPIRES_KEY)
}

/**
 * 检查Token是否即将过期(剩余时间少于5分钟)
 * @returns 是否即将过期
 */
export function isTokenExpiringSoon(): boolean {
  const expiresAt = localStorage.getItem(TOKEN_EXPIRES_KEY)
  if (!expiresAt) return false

  const expirationTime = parseInt(expiresAt, 10)
  const fiveMinutes = 5 * 60 * 1000 // 5分钟(毫秒)

  return expirationTime - Date.now() < fiveMinutes
}

/**
 * 检查Token是否已过期
 * @returns 是否已过期
 */
export function isTokenExpired(): boolean {
  const expiresAt = localStorage.getItem(TOKEN_EXPIRES_KEY)
  if (!expiresAt) return true

  const expirationTime = parseInt(expiresAt, 10)
  return Date.now() >= expirationTime
}

/**
 * 获取Token过期时间(秒)
 * @returns 剩余秒数
 */
export function getTokenExpiresIn(): number {
  const expiresAt = localStorage.getItem(TOKEN_EXPIRES_KEY)
  if (!expiresAt) return 0

  const expirationTime = parseInt(expiresAt, 10)
  const remaining = Math.max(0, expirationTime - Date.now())

  return Math.floor(remaining / 1000)
}
