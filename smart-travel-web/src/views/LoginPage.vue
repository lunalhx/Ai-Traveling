<template>
  <div class="login-page">
    <el-card class="login-card">
      <template #header>
        <div class="card-header">
          <h2>登录 Smart Travel</h2>
        </div>
      </template>

      <el-form :model="form" ref="loginForm" label-width="80px">
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>

        <el-form-item label="验证码" prop="code">
          <div class="code-container">
            <el-input v-model="form.code" placeholder="请输入验证码" />
            <el-button type="primary" @click="handleSendCode" :disabled="!form.phone" class="send-btn">
              获取验证码
            </el-button>
          </div>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleLogin" class="submit-btn" :loading="loading">
            登录
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { sendCode, login } from '../api/user'
import { setToken } from '../utils/auth'

const router = useRouter()
const loading = ref(false)

const form = reactive({
  phone: '',
  code: ''
})

const handleSendCode = async () => {
  if (!form.phone) {
    ElMessage.warning('请先输入手机号')
    return
  }
  try {
    await sendCode(form.phone)
    ElMessage.success('验证码发送成功')
  } catch (err) {
    // 错误在 request 拦截器已自动提示，此处无需重复
  }
}

const handleLogin = async () => {
  if (!form.phone || !form.code) {
    ElMessage.warning('请填写手机号和验证码')
    return
  }
  
  loading.value = true
  try {
    const res = await login({ phone: form.phone, code: form.code })
    
    // 提取后端返回的 token。兼容 res.data 直接是字符串，或 res.data.token 的情况
    const token = typeof res.data === 'string' ? res.data : res.data?.token
    
    if (token) {
      setToken(token) // 将 token 存入 localStorage
      ElMessage.success('登录成功')
      router.push('/dashboard') // 跳转至后台大盘
    } else {
      ElMessage.error('未获取到授权 Token，请检查后端返回结构是否与当前代码匹配')
    }
  } catch (err) {
    // 报错信息同样已在 request.js 统一处理
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #f0f2f5;
}
.login-card {
  width: 400px;
}
.card-header {
  text-align: center;
}
.card-header h2 {
  margin: 0;
}
.code-container {
  display: flex;
  width: 100%;
}
.send-btn {
  margin-left: 10px;
}
.submit-btn {
  width: 100%;
}
</style>
