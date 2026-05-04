import request from '../utils/request'

export function loadSpotGeo() {
  return request({
    url: '/admin/spot/geo/load',
    method: 'post'
  })
}

export function loadSeckillStock(activityId) {
  return request({
    url: `/admin/seckill/stock/load/${activityId}`,
    method: 'post'
  })
}
