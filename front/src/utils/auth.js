const AUTH_STORAGE_KEY = 'book-lending-current-user'

/**
 * 获取当前登录用户。
 *
 * @returns {object|null} 当前用户信息
 */
export function getCurrentUser() {
  const rawUser = localStorage.getItem(AUTH_STORAGE_KEY)
  if (!rawUser) {
    return null
  }

  try {
    return JSON.parse(rawUser)
  } catch {
    localStorage.removeItem(AUTH_STORAGE_KEY)
    return null
  }
}

/**
 * 保存当前登录用户。
 *
 * @param {object} user 用户信息
 */
export function setCurrentUser(user) {
  localStorage.setItem(AUTH_STORAGE_KEY, JSON.stringify(user))
}

/**
 * 清除当前登录用户。
 */
export function clearCurrentUser() {
  localStorage.removeItem(AUTH_STORAGE_KEY)
}
