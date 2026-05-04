import { createRouter, createWebHistory } from 'vue-router'
import MainLayout from '../layout/MainLayout.vue'
import { isLoggedIn } from '../utils/auth'

const routes = [
  { path: '/login', component: () => import('../views/LoginPage.vue') },
  {
    path: '/',
    component: MainLayout,
    redirect: '/dashboard',
    children: [
      { path: 'dashboard', component: () => import('../views/DashboardPage.vue') },
      { path: 'spots', component: () => import('../views/SpotListPage.vue') },
      { path: 'spots/:id', component: () => import('../views/SpotDetailPage.vue') },
      { path: 'nearby', component: () => import('../views/NearbyPage.vue') },
      { path: 'checkin', component: () => import('../views/CheckinPage.vue') },
      { path: 'checkin-records', component: () => import('../views/CheckinRecordPage.vue') },
      { path: 'seckill', component: () => import('../views/SeckillPage.vue') },
      { path: 'orders', component: () => import('../views/OrderPage.vue') },
      { path: 'admin-tools', component: () => import('../views/AdminToolsPage.vue') }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 不需要登录即可访问的路由白名单
const whiteList = ['/login']

// 全局路由守卫
router.beforeEach((to, from, next) => {
  // 通过封装好的工具函数，从 localStorage 校验 token 是否存在
  const hasToken = isLoggedIn()

  if (hasToken) {
    if (to.path === '/login') {
      // 已经有 token，依然试图访问登录页时，重定向到首页（避免死循环）
      next({ path: '/dashboard' })
    } else {
      // 有 token 且访问业务页面，正常放行
      next()
    }
  } else {
    if (whiteList.includes(to.path)) {
      // 没有 token，但试图访问的页面在白名单内（如 /login），正常放行
      next()
    } else {
      // 没有 token 且试图访问业务页面，强制拦截并跳转到登录页
      next({ path: '/login' })
    }
  }
})

export default router
