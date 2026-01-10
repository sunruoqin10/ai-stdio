import axios, { AxiosError } from 'axios'
import type { AxiosInstance, AxiosRequestConfig, AxiosResponse, InternalAxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/modules/auth/store'

// 创建 axios 实例
const service: AxiosInstance = axios.create({
  baseURL: '/api',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json',
  },
})

// 请求拦截器
service.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    // 添加Token到请求头
    const authStore = useAuthStore()
    const token = authStore.accessToken

    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }

    return config
  },
  (error) => {
    console.error('Request error:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  (response: AxiosResponse) => {
    const res = response.data

    // 这里假设后端返回格式为 { code: number, data: any, message: string }
    // 如果是 Blob 数据,直接返回
    if (response.config.responseType === 'blob') {
      return response
    }

    return res
  },
  async (error: AxiosError) => {
    console.error('Response error:', error)

    const authStore = useAuthStore()

    // 处理401未授权错误
    if (error.response?.status === 401) {
      const originalRequest = error.config as InternalAxiosRequestConfig & { _retry?: boolean }

      // 如果没有重试过,尝试刷新Token
      if (!originalRequest._retry) {
        originalRequest._retry = true

        try {
          // 尝试刷新Token
          const success = await authStore.refreshAccessToken()

          if (success) {
            // 刷新成功,重试原请求
            const newToken = authStore.accessToken
            if (newToken) {
              originalRequest.headers.Authorization = `Bearer ${newToken}`
            }
            return service(originalRequest)
          } else {
            // 刷新失败,跳转登录页
            ElMessage.error('登录已过期,请重新登录')
            authStore.clearAuthState()
            window.location.href = '/login'
            return Promise.reject(error)
          }
        } catch (refreshError) {
          console.error('Token刷新失败:', refreshError)
          ElMessage.error('登录已过期,请重新登录')
          authStore.clearAuthState()
          window.location.href = '/login'
          return Promise.reject(refreshError)
        }
      } else {
        // 已经重试过,直接跳转登录页
        ElMessage.error('登录已过期,请重新登录')
        authStore.clearAuthState()
        window.location.href = '/login'
      }
    } else if (error.response) {
      const { status, data } = error.response

      switch (status) {
        case 400:
          ElMessage.error((data as any)?.message || '请求参数错误')
          break
        case 403:
          ElMessage.error('拒绝访问')
          break
        case 404:
          ElMessage.error('请求的资源不存在')
          break
        case 500:
          ElMessage.error('服务器错误')
          break
        default:
          ElMessage.error((data as any)?.message || '网络错误')
      }
    } else {
      ElMessage.error('网络连接失败')
    }

    return Promise.reject(error)
  }
)

// 封装请求方法
export const http = {
  get<T = any>(url: string, config?: AxiosRequestConfig): Promise<T> {
    return service.get(url, config)
  },

  post<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
    return service.post(url, data, config)
  },

  put<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
    return service.put(url, data, config)
  },

  delete<T = any>(url: string, config?: AxiosRequestConfig): Promise<T> {
    return service.delete(url, config)
  },

  patch<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
    return service.patch(url, data, config)
  },
}

export default service
