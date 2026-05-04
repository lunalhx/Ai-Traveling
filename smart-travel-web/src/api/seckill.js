import request from '../utils/request'

export function getActivityList(params) {
  return request({
    url: '/seckill/activities',
    method: 'get',
    params
  })
}

export function getActivityDetail(id) {
  return request({
    url: `/seckill/activity/${id}`,
    method: 'get'
  })
}

export function seckill(activityId) {
  return request({
    url: `/seckill/${activityId}`,
    method: 'post'
  })
}

export function getSeckillResult(activityId) {
  return request({
    url: `/seckill/result/${activityId}`,
    method: 'get'
  })
}
