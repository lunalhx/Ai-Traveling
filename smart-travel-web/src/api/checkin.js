import request from '../utils/request'

export function checkin(data) {
  return request({
    url: '/checkin',
    method: 'post',
    data
  })
}

export function getTodayCheckinStatus(params) {
  return request({
    url: '/checkin/status',
    method: 'get',
    params
  })
}

export function getCheckinRecords(params) {
  return request({
    url: '/checkin/records',
    method: 'get',
    params
  })
}
