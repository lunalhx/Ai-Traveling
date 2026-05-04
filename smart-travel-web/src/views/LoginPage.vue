<template>
  <div class="login-page">
    <el-card class="login-card">
      <template #header>
        <div class="card-header">
          <h2>登录 Smart Travel</h2>
        </div>
      </template>

      <el-form :model="form" ref="loginForm" :rules="rules" label-width="80px" @keyup.enter="handleLogin">
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>

        <el-form-item label="验证码" prop="code">
          <div class="code-container">
            <el-input v-model="form.code" placeholder="请输入验证码" />
            <el-button 
              type="primary" 
              @click="handleSendCode" 
              :disabled="!form.phone || countdown > 0" 
              :loading="sendCodeLoading"
              class="send-btn">
              {{ countdown > 0 ? `${countdown}s 后重新获取` : '获取验证码' }}
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
import { reactive, ref, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { sendCode, login } from '../api/user'
import { setToken } from '../utils/auth'

const router = useRouter()
const loading = ref(false)
const loginForm = ref(null)

const sendCodeLoading = ref(false)
const countdown = ref(0)
let timer = null

const form = reactive({
  phone: '',
  code: ''
})

const rules = reactive({
  phone: [
    { required: true, message: '手机号不能为空', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '验证码不能为空', trigger: 'blur' },
    { pattern: /^\d{6}$/, message: '验证码必须为 6 位数字', trigger: 'blur' }
  ]
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})

const handleSendCode = () => {
  if (!loginForm.value) return
  loginForm.value.validateField('phone', async (isValid) => {
    if (isValid) {
      if (countdown.value > 0 || sendCodeLoading.value) return
      
      sendCodeLoading.value = true
      try {
        const res = await sendCode(form.phone)
        ElMessage.success('验证码已发送')
        
        // 严格区分开发环境：如果后端返回了验证码，仅在开发环境下打印，绝不影响正式打包后的体验
        if (import.meta.env.DEV && res.data) {
          setTimeout(() => {
            ElMessage.info({
              message: `[开发测试] 您的验证码是: ${res.data}`,
              duration: 10000,
              showClose: true
            })
          }, 300)
        }

        countdown.value = 60
        timer = setInterval(() => {
          countdown.value--
          if (countdown.value <= 0) {
            clearInterval(timer)
          }
        }, 1000)
      } catch (error) {
        // 请求失败报错已在 request.js 中统一处理
      } finally {
        sendCodeLoading.value = false
      }
    } else {
      ElMessage.warning('请先正确输入手机号')
    }
  })
}

const handleLogin = () => {
  if (!loginForm.value) return
  loginForm.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const res = await login({ phone: form.phone, code: form.code })
        
        // 提取后端返回的 token。
        // 根据之前的检查，后端返回的 token 是一个直接挂载在 data 属性上的字符串
        const token = typeof res.data === 'string' ? res.data : res.data?.token
        
        if (token) {
          setToken(token) // 保存 token 到 localStorage
          ElMessage.success('登录成功')
          router.push('/dashboard') // 跳转至大盘页
        } else {
          ElMessage.error('系统异常：未从后端响应中获取到 Token')
        }
      } catch (err) {
        // 请求失败报错已在 request.js 的响应拦截器中统一提示处理
      } finally {
        loading.value = false
      }
    } else {
      ElMessage.warning('请检查表单填写的内容')
    }
  })
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
