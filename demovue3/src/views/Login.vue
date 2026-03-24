<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const username = ref('')
const password = ref('')

const handleRegister = () => {
  router.push('/register')
}

const handleLogin = async () => {
  if (!username.value.trim() || !password.value.trim()) {
    alert('用户名和密码不能为空')
    return
  }

  try {
    const res = await fetch('http://localhost:8080/reg/login', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        username: username.value,
        password: password.value,
      }),
    })

    const text = await res.text()

    if (!res.ok) {
      // HTTP 状态异常，直接视为失败
      alert(text || '登录失败，请稍后重试')
      return
    }

    // 根据后端返回的字符串判断是否登录成功
    // 假设后端登录成功时返回 "success"（你可以根据实际返回修改这里的字符串）
    if (text === '登录成功') {
      alert(text)
      await router.push('/home')
    } else {
      alert(text)
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
  font-size: 1.8rem;
  font-weight: 600;
  color: #111827;
}

.card {
  width: 100%;
  max-width: 420px;
  padding: 1.75rem 2rem;
  border-radius: 1rem;
  background: #ffffff;
  box-shadow:
    0 10px 30px rgba(15, 23, 42, 0.08),
    0 1px 2px rgba(15, 23, 42, 0.04);
}

.form-item {
  display: flex;
  flex-direction: column;
  margin-bottom: 1.25rem;
}

.label {
  margin-bottom: 0.4rem;
  font-size: 0.9rem;
  color: #6b7280;
}

.input {
  padding: 0.55rem 0.75rem;
  border-radius: 0.5rem;
  border: 1px solid #d1d5db;
  outline: none;
  font-size: 0.95rem;
  transition: border-color 0.15s ease, box-shadow 0.15s ease;
}

.input:focus {
  border-color: #4f46e5;
  box-shadow: 0 0 0 1px rgba(79, 70, 229, 0.18);
}

.btn-group {
  display: flex;
  justify-content: space-between;
  gap: 0.75rem;
  margin-top: 0.5rem;
}

.btn {
  flex: 1;
  padding: 0.6rem 0.8rem;
  border-radius: 0.6rem;
  border: none;
  font-size: 0.95rem;
  font-weight: 600;
  cursor: pointer;
  transition: transform 0.1s ease, box-shadow 0.1s ease, filter 0.15s ease;
}

.btn.primary {
  background: linear-gradient(135deg, #4f46e5, #6366f1);
  color: #ffffff;
}

.btn.secondary {
  background: #e5e7eb;
  color: #374151;
}

.btn:hover {
  filter: brightness(1.03);
  box-shadow: 0 6px 16px rgba(15, 23, 42, 0.18);
}

.btn:active {
  transform: translateY(1px);
  box-shadow: 0 3px 10px rgba(15, 23, 42, 0.16);
}
</style>

