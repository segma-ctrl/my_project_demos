<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const username = ref('')
const password = ref('')
const role = ref(0) // 默认学生

const handleRegister = () => {
  router.push('/register')
}

const handleLogin = async () => {
  if (!username.value.trim() || !password.value.trim()) {
    alert('用户名和密码不能为空')
    return
  }

  try {
    const res = await fetch('http://localhost:8080/api/login', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        username: username.value,
        password: password.value,
        role: role.value,
      }),
    })

    if (!res.ok) {
      alert('登录失败，请检查后端服务')
      return
    }

    const result = await res.json()
    if (result.status) {
      alert(result.message)
      // 存储登录状态
      localStorage.setItem('user', JSON.stringify({
        username: username.value,
        role: role.value
      }))
      await router.push('/home')
    } else {
      alert(result.message || '登录失败')
    }
  } catch (e) {
    console.error(e)
    alert('登录请求发送失败，请检查后端服务是否启动')
  }
}
</script>

<template>
  <div class="login-page">
    <h1 class="title">用户登录</h1>

    <div class="card">
      <div class="form-item">
        <label for="username" class="label">用户名</label>
        <input
          id="username"
          v-model="username"
          type="text"
          class="input"
          placeholder="请输入用户名"
        />
      </div>

      <div class="form-item">
        <label for="password" class="label">密码</label>
        <input
          id="password"
          v-model="password"
          type="password"
          class="input"
          placeholder="请输入密码"
        />
      </div>

      <div class="form-item">
        <label class="label">登录角色</label>
        <div class="radio-group">
          <label class="radio-label">
            <input type="radio" v-model="role" :value="0" /> 学生
          </label>
          <label class="radio-label">
            <input type="radio" v-model="role" :value="1" /> 教师
          </label>
          <label class="radio-label">
            <input type="radio" v-model="role" :value="2" /> 管理员
          </label>
        </div>
      </div>

      <div class="btn-group">
        <button type="button" class="btn secondary" @click="handleRegister">
          注册
        </button>
        <button type="button" class="btn primary" @click="handleLogin">
          登录
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: #f5f7fb;
  padding: 2rem;
}

.title {
  margin-bottom: 1.5rem;
  font-size: 2rem;
  font-weight: 700;
  color: #1a202c;
  letter-spacing: -0.025em;
}

.card {
  width: 100%;
  max-width: 440px;
  padding: 2.5rem;
  border-radius: 1.25rem;
  background: #ffffff;
  box-shadow: 
    0 20px 25px -5px rgba(0, 0, 0, 0.1),
    0 10px 10px -5px rgba(0, 0, 0, 0.04);
}

.form-item {
  display: flex;
  flex-direction: column;
  margin-bottom: 1.5rem;
}

.label {
  margin-bottom: 0.5rem;
  font-size: 0.875rem;
  font-weight: 600;
  color: #4a5568;
}

.input {
  padding: 0.75rem 1rem;
  border-radius: 0.5rem;
  border: 1px solid #e2e8f0;
  outline: none;
  font-size: 1rem;
  transition: all 0.2s;
}

.input:focus {
  border-color: #4f46e5;
  ring: 2px solid rgba(79, 70, 229, 0.1);
}

.radio-group {
  display: flex;
  gap: 1.5rem;
  padding: 0.5rem 0;
}

.radio-label {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.95rem;
  color: #2d3748;
  cursor: pointer;
}

.btn-group {
  display: flex;
  gap: 1rem;
  margin-top: 1rem;
}

.btn {
  flex: 1;
  padding: 0.75rem;
  border-radius: 0.5rem;
  border: none;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.btn.primary {
  background: #4f46e5;
  color: white;
}

.btn.primary:hover {
  background: #4338ca;
}

.btn.secondary {
  background: #f7fafc;
  color: #4a5568;
  border: 1px solid #e2e8f0;
}

.btn.secondary:hover {
  background: #edf2f7;
}
</style>


