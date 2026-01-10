/**
 * Mock API 适配器 - 拦截axios请求并返回Mock数据
 */

import type { InternalAxiosRequestConfig, AxiosResponse } from 'axios'
import { mockApiHandlers } from './mock'

/**
 * 创建Mock适配器
 */
export function createMockAdapter() {
  return {
    /**
     * 请求拦截器 - 判断是否应该返回Mock数据
     */
    handler: async (config: InternalAxiosRequestConfig): Promise<AxiosResponse> | null => {
      // 只在开发环境且请求路径匹配时返回Mock数据
      if (!import.meta.env.DEV) {
        return null
      }

      const url = config.url || ''

      // 匹配登录接口
      if (url.match(/\/api\/auth\/login$/) && config.method === 'post') {
        const data = await mockApiHandlers.login(config.data)
        return createMockResponse(data)
      }

      // 匹配退出登录接口
      if (url.match(/\/api\/auth\/logout$/) && config.method === 'post') {
        const data = await mockApiHandlers.logout()
        return createMockResponse(data)
      }

      // 匹配刷新Token接口
      if (url.match(/\/api\/auth\/refresh$/) && config.method === 'post') {
        const data = await mockApiHandlers.refreshToken(config.data)
        return createMockResponse(data)
      }

      // 匹配获取验证码接口
      if (url.match(/\/api\/auth\/captcha$/) && config.method === 'get') {
        const data = await mockApiHandlers.getCaptcha()
        return createMockResponse(data)
      }

      // 匹配发送验证码接口
      if (url.match(/\/api\/auth\/send-code$/) && config.method === 'post') {
        const data = await mockApiHandlers.sendCode(config.data)
        return createMockResponse(data)
      }

      // 匹配重置密码接口
      if (url.match(/\/api\/auth\/reset-password$/) && config.method === 'post') {
        const data = await mockApiHandlers.resetPassword(config.data)
        return createMockResponse(data)
      }

      // 其他请求不处理
      return null
    },
  }
}

/**
 * 创建Mock响应
 */
function createMockResponse(data: any): AxiosResponse {
  return {
    data,
    status: 200,
    statusText: 'OK',
    headers: {},
    config: {} as any,
  } as AxiosResponse
}

/**
 * 安装Mock适配器到axios实例
 */
export function installMockAdapter(axiosInstance: any) {
  const adapter = createMockAdapter()

  // 请求拦截器
  axiosInstance.interceptors.request.use(
    async (config: InternalAxiosRequestConfig) => {
      const mockResponse = await adapter.handler(config)

      if (mockResponse) {
        // 返回一个会立即resolve的Promise
        return Promise.reject({
          $mock: true,
          response: mockResponse,
        })
      }

      return config
    },
    (error) => Promise.reject(error)
  )

  // 响应拦截器 - 处理Mock响应
  axiosInstance.interceptors.response.use(
    (response) => response,
    (error) => {
      // 如果是Mock响应,直接返回
      if (error.$mock) {
        return error.response
      }
      return Promise.reject(error)
    }
  )
}
