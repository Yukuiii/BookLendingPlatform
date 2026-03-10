import { request } from '../utils/request'

/**
 * 调用登录接口。
 *
 * @param {{username: string, password: string, userType: number}} payload 登录参数
 * @returns {Promise<any>} 登录响应
 */
export function login(payload) {
  return request('/auth/login', {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

/**
 * 调用注册接口。
 *
 * @param {{username: string, realName: string, major?: string | null, password: string, phone: string, identityCard: string, email: string}} payload 注册参数
 * @returns {Promise<any>} 注册响应
 */
export function register(payload) {
  return request('/auth/register', {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}
