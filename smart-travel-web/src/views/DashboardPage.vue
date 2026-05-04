<template>
  <div class="dashboard-page">
    <div class="header-section">
      <h1>数字文旅教育平台 Web 演示端</h1>
      <p v-if="userInfo" class="welcome-text">欢迎回来，{{ userInfo.nickname || userInfo.phone || '用户' }}</p>
      <p v-else class="welcome-text">欢迎体验智能旅游平台</p>
    </div>

    <el-row :gutter="20">
      <el-col :span="24">
        <el-card class="box-card mb-20">
          <template #header>
            <div class="card-header">
              <span>核心技术栈概览</span>
            </div>
          </template>
          <div class="tech-tags">
            <el-tag effect="dark" type="success">Vue 3</el-tag>
            <el-tag effect="dark">Element Plus</el-tag>
            <el-tag effect="dark" type="warning">Spring Boot</el-tag>
            <el-tag effect="dark" type="info">MySQL</el-tag>
            <el-tag effect="dark" type="danger">Redis</el-tag>
            <el-tag effect="dark" type="primary">RabbitMQ</el-tag>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <h3 class="section-title">功能体验快捷入口</h3>
    <el-row :gutter="20">
      <el-col :span="8" v-for="entry in features" :key="entry.name" class="mb-20">
        <el-card shadow="hover" class="feature-card" @click="goTo(entry.path)">
          <div class="feature-content">
            <h4>{{ entry.name }}</h4>
            <p>{{ entry.desc }}</p>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getCurrentUser } from '../api/user'

const router = useRouter()
const userInfo = ref(null)

// 导航功能列表
const features = [
  { name: '景点浏览', desc: '查看热门与全量景点信息', path: '/spots' },
  { name: '附近景点', desc: '基于 LBS 搜索周边景点', path: '/nearby' },
  { name: '用户打卡', desc: '到达指定地点进行打卡签到', path: '/checkin' },
  { name: '秒杀抢购', desc: '参与高并发限量门票秒杀', path: '/seckill' },
  { name: '我的订单', desc: '查看购票与历史打卡明细', path: '/orders' },
  { name: '后台工具', desc: '用于环境预热与缓存初始化', path: '/admin-tools' }
]

// 路由跳转
const goTo = (path) => {
  router.push(path)
}

// 尝试获取当前用户信息
const loadUserInfo = async () => {
  try {
    const res = await getCurrentUser()
    // 兼容取值，防止后端返回结构不一致
    userInfo.value = res.data || res || null
  } catch (err) {
    // 静默处理。因为此时后端可能还没写好这个接口，不要让报错弹窗阻挡体验。
    console.warn('获取用户信息失败，后端可能暂未实现该接口')
  }
}

onMounted(() => {
  loadUserInfo()
})
</script>

<style scoped>
.dashboard-page {
  padding: 10px;
}
.header-section {
  margin-bottom: 30px;
}
.header-section h1 {
  margin-bottom: 10px;
  color: #303133;
}
.welcome-text {
  color: #606266;
  font-size: 16px;
}
.mb-20 {
  margin-bottom: 20px;
}
.tech-tags .el-tag {
  margin-right: 10px;
  margin-bottom: 10px;
  font-size: 14px;
}
.section-title {
  margin-top: 10px;
  margin-bottom: 20px;
  color: #303133;
  border-left: 4px solid #409eff;
  padding-left: 10px;
}
.feature-card {
  cursor: pointer;
  transition: all 0.3s;
  height: 100px;
}
.feature-card:hover {
  transform: translateY(-5px);
  border-color: #409eff;
}
.feature-content h4 {
  margin-top: 0;
  margin-bottom: 10px;
  color: #409eff;
  font-size: 18px;
}
.feature-content p {
  margin: 0;
  color: #909399;
  font-size: 14px;
}
</style>
