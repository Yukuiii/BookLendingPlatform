import { request } from '../utils/request'

/**
 * 查询当前用户个性化设置。
 *
 * @returns {Promise<any>} 个性化设置
 */
export function getCurrentUserPreference() {
  return request('/user-preferences/me', {
    method: 'GET',
  })
}

/**
 * 查询个性化设置可选项。
 *
 * @returns {Promise<any>} 可选项
 */
export function getCurrentUserPreferenceOptions() {
  return request('/user-preferences/options', {
    method: 'GET',
  })
}

/**
 * 修改当前用户个性化设置。
 *
 * @param {object} payload 修改参数
 * @returns {Promise<any>} 修改后的个性化设置
 */
export function updateCurrentUserPreference(payload) {
  return request('/user-preferences/me', {
    method: 'PUT',
    body: JSON.stringify(payload),
  })
}
