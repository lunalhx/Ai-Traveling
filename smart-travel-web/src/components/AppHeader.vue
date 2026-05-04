<template>
  <div class="app-header">
    <div class="logo">Smart Travel Web</div>
    <div class="actions">
      <el-button type="danger" size="small" @click="handleLogout">退出登录</el-button>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { removeToken } from '../utils/auth'

const router = useRouter()

const handleLogout = () => {
  removeToken()
  router.push('/login')
}
</script>

<style scoped>
.app-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 100%;
  color: #fff;
}
.logo {
  font-size: 20px;
  font-weight: bold;
}
</style>
