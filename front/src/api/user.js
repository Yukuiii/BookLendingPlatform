import { request } from '../utils/request'

/**
 * 查询当前用户个人信息。
 *
 * @returns {Promise<any>} 个人信息
 */
export function getCurrentUserProfile() {
  return request('/users/me', {
    method: 'GET',
  })
}

/**
 * 修改当前用户个人信息。
 *
 * @param {{realName: string, major?: string | null, email: string, phone: string}} payload 修改参数
 * @returns {Promise<any>} 修改后的个人信息
 */
export function updateCurrentUserProfile(payload) {
  return request('/users/me', {
    method: 'PUT',
    body: JSON.stringify(payload),
  })
}

/**
 * 修改当前用户密码。
 *
 * @param {{oldPassword: string, newPassword: string}} payload 修改密码参数
 * @returns {Promise<any>} 修改结果
 */
export function changeCurrentUserPassword(payload) {
  return request('/users/me/password', {
    method: 'PUT',
    body: JSON.stringify(payload),
  })
}

