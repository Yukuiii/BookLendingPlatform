const AUTH_STORAGE_KEY = 'book-lending-current-user'
const AUTH_CHANGE_EVENT = 'book-lending-auth-user-changed'

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
  window.dispatchEvent(new Event(AUTH_CHANGE_EVENT))
}

/**
 * 清除当前登录用户。
 */
export function clearCurrentUser() {
  localStorage.removeItem(AUTH_STORAGE_KEY)
  window.dispatchEvent(new Event(AUTH_CHANGE_EVENT))
}

/**
 * 监听当前登录用户变化。
 *
 * @param {() => void} listener 监听回调
 * @returns {() => void} 取消监听函数
 */
export function onCurrentUserChange(listener) {
  window.addEventListener(AUTH_CHANGE_EVENT, listener)
  return () => {
    window.removeEventListener(AUTH_CHANGE_EVENT, listener)
  }
}
