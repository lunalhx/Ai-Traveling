import request from '../utils/request'

/**
 * 发送手机验证码
 * 对应后端接口: POST /user/code?phone=xxx
 * @param {string} phone 手机号
 */
export function sendCode(phone) {
  return request({
    url: '/user/code',
    method: 'post',
    params: { phone }
  })
}

/**
 * 用户登录
 * 对应后端接口: POST /user/login
 * @param {Object} data 包含 phone 和 code
 */
export function login(data) {
  return request({
    url: '/user/login',
    method: 'post',
    data
  })
}

/**
 * 获取当前登录用户信息
 * 对应后端接口: GET /user/me
 */
export function getCurrentUser() {
  return request({
    url: '/user/me',
    method: 'get'
  })
}
