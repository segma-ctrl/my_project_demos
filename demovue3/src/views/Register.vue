<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const name = ref('')
const username = ref('')
const password = ref('')
const phone = ref('')

const resetForm = () => {
  name.value = ''
  username.value = ''
  password.value = ''
  phone.value = ''
}

const handleSubmit = async () => {
  // 前端校验：前三项必填
  if (!name.value.trim() || !username.value.trim() || !password.value.trim()) {
    alert('姓名、用户名和密码为必填项')
    return
  }

  try {
    const res = await fetch('http://localhost:8080/reg/add', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        name: name.value,
        username: username.value,
        password: password.value,
        phonenumber: phone.value,
      }),
    })

    const text=await res.text()
    if (!res.ok) {
      // 视为注册失败，刷新注册页面（这里用表单重置代替）
      resetForm()
      alert('注册失败，请稍后重试')
      return
    }
    if(text=='注册成功'){
      // 注册成功，跳回登录页面
      alert(text)
      await router.push('/')
    }else{
      alert(text)
    }

    
  } catch (e) {
    console.error(e)
    // 请求异常，同样刷新页面（表单重置）
    resetForm()
    alert('注册请求发送失败，请检查后端服务是否启动')
  }
}
</script>

<template>
  <div class="register-page">
    <h1 class="title">用户注册</h1>

    <div class="card">
      <div class="form-item">
        <label for="name" class="label">姓名（必填）</label>
        <input
          id="name"
          v-model="name"
          type="text"
          class="input"
          placeholder="请输入姓名"
        />
      </div>

      <div class="form-item">
        <label for="username" class="label">用户名（必填）</label>
        <input
          id="username"
          v-model="username"
          type="text"
          class="input"
          placeholder="请输入用户名"
        />
      </div>

      <div class="form-item">
        <label for="password" class="label">密码（必填）</label>
        <input
          id="password"
          v-model="password"
          type="password"
          class="input"
          placeholder="请输入密码"
        />
      </div>

      <div class="form-item">
        <label for="phone" class="label">手机号（选填）</label>
        <input
          id="phone"
          v-model="phone"
          type="text"
          class="input"
          placeholder="请输入手机号"
        />
      </div>

      <button type="button" class="btn primary" @click="handleSubmit">
        提交注册
      </button>
    </div>
  </div>
</template>

<style scoped>
.register-page {
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

.btn.primary {
  width: 100%;
  margin-top: 0.5rem;
  padding: 0.6rem 0.8rem;
  border-radius: 0.6rem;
  border: none;
  background: linear-gradient(135deg, #4f46e5, #6366f1);
  color: #ffffff;
  font-size: 0.95rem;
  font-weight: 600;
  cursor: pointer;
  transition: transform 0.1s ease, box-shadow 0.1s ease, filter 0.15s ease;
}

.btn.primary:hover {
  filter: brightness(1.03);
  box-shadow: 0 6px 16px rgba(79, 70, 229, 0.35);
}

.btn.primary:active {
  transform: translateY(1px);
  box-shadow: 0 3px 10px rgba(79, 70, 229, 0.3);
}
</style>

