import axios from 'axios'
import { ElMessage } from 'element-plus'
import { getToken, removeToken } from './auth'
import router from '../router'

// 1. 创建 axios 实例
const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 15000 // 设置合理的超时时间（15秒）
})

// 2. 请求拦截器
request.interceptors.request.use(
  config => {
    // 读取 auth.js 中的 token
    const token = getToken()
    if (token) {
      // 如果 token 存在，请求头自动添加 authorization
      config.headers['Authorization'] = token
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 3. 响应拦截器
request.interceptors.response.use(
  response => {
    // 兼容后端 Result 格式，获取业务数据
    const res = response.data

    // 如果后端返回 success=false，说明业务逻辑出错
    if (res.success === false) {
      // 使用 Element Plus 提示错误信息
      ElMessage.error(res.message || res.msg || '操作失败')
      // 抛出错误，阻止代码继续往下执行进入 then
      return Promise.reject(new Error(res.message || res.msg || '操作失败'))
    }

    // 成功则直接返回核心业务数据，页面中不再需要 response.data.data
    return res
  },
  error => {
    // 处理 HTTP 状态码层面的错误
    if (error.response) {
      // 如果响应状态是 401（未授权）
      if (error.response.status === 401) {
        // 清除失效的 token
        removeToken()
        // 提示用户并跳转到登录页
        ElMessage.error('登录已失效，请重新登录')
        router.push('/login')
      } else {
        // 其他 HTTP 错误码（如 404, 500 等）统一直观报错
        ElMessage.error(error.response.data?.message || error.response.data?.msg || '网络请求失败')
      }
    } else {
      // 处理超时或完全断网的情况
      ElMessage.error('网络异常或服务器未响应')
    }
    
    return Promise.reject(error)
  }
)

export default request
