import request from '../utils/request'

export function getCategories() {
  return request({
    url: '/spot/categories',
    method: 'get'
  })
}

export function getSpotList(params) {
  return request({
    url: '/spot/list',
    method: 'get',
    params
  })
}

export function getSpotDetail(id) {
  return request({
    url: `/spot/${id}`,
    method: 'get'
  })
}

export function getHotSpots(params) {
  return request({
    url: '/spot/hot',
    method: 'get',
    params
  })
}

export function getNearbySpots(params) {
  return request({
    url: '/spot/nearby',
    method: 'get',
    params
  })
}
