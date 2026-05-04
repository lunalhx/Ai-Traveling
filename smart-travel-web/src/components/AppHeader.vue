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
import { ElMessageBox, ElMessage } from 'element-plus'

const router = useRouter()

const handleLogout = () => {
  ElMessageBox.confirm(
    '确定要退出登录吗？',
    '提示',
    {
      confirmButtonText: '确定退出',
      cancelButtonText: '取消',
      type: 'warning'
    }
  )
    .then(() => {
      // 确认退出：由于后端没有明确的登出接口，前端直接清除本地 token 即可
      removeToken()
      ElMessage.success('已安全退出')
      router.push('/login')
    })
    .catch(() => {
      // 取消操作：用户点击取消或关闭弹窗，不做任何处理
    })
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
