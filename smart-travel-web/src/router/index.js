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

// 全局路由守卫
router.beforeEach((to, from, next) => {
  const hasToken = isLoggedIn()

  if (to.path === '/login') {
    // 如果已经登录，还试图访问登录页，强制跳回首页
    if (hasToken) {
      next({ path: '/dashboard' })
    } else {
      // 未登录访问登录页，正常放行
      next()
    }
  } else {
    // 访问其他页面
    if (!hasToken) {
      // 没有登录（没有 token），强制拦截到登录页
      next({ path: '/login' })
    } else {
      // 已经登录，正常放行
      next()
    }
  }
})

export default router
