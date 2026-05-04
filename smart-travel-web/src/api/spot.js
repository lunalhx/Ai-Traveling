import request from '../utils/request'

/**
 * 获取景点分类列表
 * 对应后端接口: GET /spot/category/list
 */
export function getCategories() {
  return request({
    url: '/spot/category/list',
    method: 'get'
  })
}

/**
 * 分页获取景点列表
 * 对应后端接口: GET /spot/list
 * @param {Object} params 包含 categoryId、keyword、page、pageSize
 */
export function getSpotList(params) {
  return request({
    url: '/spot/list',
    method: 'get',
    params
  })
}

/**
 * 获取热门景点列表
 * 对应后端接口: GET /spot/hot
 * @param {Object} params 包含 limit
 */
export function getHotSpots(params) {
  return request({
    url: '/spot/hot',
    method: 'get',
    params
  })
}

/**
 * 获取景点详情
 * 对应后端接口: GET /spot/{id}
 * @param {number|string} id 景点ID
 */
export function getSpotDetail(id) {
  return request({
    url: `/spot/${id}`,
    method: 'get'
  })
}

/**
 * 获取附近景点列表
 * 对应后端接口: GET /spot/nearby
 * @param {Object} params 包含 longitude, latitude, radius 等
 */
export function getNearbySpots(params) {
  return request({
    url: '/spot/nearby',
    method: 'get',
    params
  })
}
