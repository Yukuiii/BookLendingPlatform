import { getCurrentUser } from './auth'

const DEFAULT_API_BASE_URL = (
  import.meta.env.VITE_API_BASE_URL ||
  (import.meta.env.DEV ? '/api' : 'http://127.0.0.1:8080')
).replace(/\/$/, '')

/**
 * 发送 JSON 请求。
 *
 * @param {string} path 请求路径
 * @param {RequestInit} options 请求配置
 * @returns {Promise<any>} 响应结果
 */
export async function request(path, options = {}) {
  const currentUser = getCurrentUser()
  const userId = currentUser?.userId

  const requestOptions = {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...(userId != null ? { 'X-User-Id': String(userId) } : {}),
      ...(options.headers || {}),
    },
  }

  try {
    const response = await fetch(`${DEFAULT_API_BASE_URL}${path}`, requestOptions)
    const payload = await parseResponse(response)

    if (!response.ok) {
      throw new Error(payload?.message || '请求失败，请稍后重试')
    }

    return payload
  } catch (error) {
    if (error instanceof TypeError) {
      throw new Error('无法连接后端服务，请确认后端已启动')
    }
    if (error instanceof Error && error.message) {
      throw error
    }
    throw new Error('请求失败，请稍后重试')
  }
}

/**
 * 解析响应体。
 *
 * @param {Response} response Fetch 响应对象
 * @returns {Promise<any>} 解析后的结果
 */
async function parseResponse(response) {
  if (response.status === 204) {
    return null
  }

  const contentType = response.headers.get('content-type') || ''
  if (contentType.includes('application/json')) {
    return response.json()
  }

  return response.text()
}
