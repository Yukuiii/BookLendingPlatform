import { request } from '../utils/request'

/**
 * 分页查询我的通知。
 *
 * @param {{current?: number, size?: number, readStatus?: number|null}} params 查询参数
 * @returns {Promise<any>} 通知分页结果
 */
export function pageMyNotifications(params = {}) {
  const searchParams = new URLSearchParams()

  Object.entries(params).forEach(([key, value]) => {
    if (value === undefined || value === null || value === '') {
      return
    }
    searchParams.append(key, String(value))
  })

  const queryString = searchParams.toString()
  const requestPath = queryString ? `/notifications/my/page?${queryString}` : '/notifications/my/page'

  return request(requestPath, {
    method: 'GET',
  })
}

/**
 * 标记通知为已读。
 *
 * @param {number|string} notificationId 通知ID
 * @returns {Promise<any>} 更新后的通知
 */
export function markMyNotificationRead(notificationId) {
  return request(`/notifications/${encodeURIComponent(notificationId)}/read`, {
    method: 'POST',
  })
}
