import request from '../utils/request'

export function sendCode(phone) {
  return request({
    url: '/user/code',
    method: 'post',
    params: { phone }
  })
}

export function login(data) {
  return request({
    url: '/user/login',
    method: 'post',
    data
  })
}

export function getCurrentUser() {
  return request({
    url: '/user/me',
    method: 'get'
  })
}
