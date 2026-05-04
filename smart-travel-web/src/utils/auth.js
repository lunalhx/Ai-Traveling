/**
 * 统一管理的 Token 存储 Key
 */
const TOKEN_KEY = 'smart_travel_token'

/**
 * 获取本地存储中的 Token
 * @returns {string|null} token 字符串或 null
 */
export function getToken() {
  return localStorage.getItem(TOKEN_KEY)
}

/**
 * 将 Token 保存到本地存储
 * @param {string} token 后端返回的 token 字符串
 */
export function setToken(token) {
  localStorage.setItem(TOKEN_KEY, token)
}

/**
 * 从本地存储中移除 Token
 */
export function removeToken() {
  localStorage.removeItem(TOKEN_KEY)
}

/**
 * 判断用户是否已登录 (是否存在 Token)
 * @returns {boolean}
 */
export function isLoggedIn() {
  return !!getToken()
}
