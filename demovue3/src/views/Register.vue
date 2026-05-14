<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { apiUrl } from '../config/api.js'

const router = useRouter()

const id = ref('')
const name = ref('')
const username = ref('')
const password = ref('')
const phoneNumber = ref('')
const role = ref(0) // 默认学生

const resetForm = () => {
  id.value = ''
  name.value = ''
  username.value = ''
  password.value = ''
  phoneNumber.value = ''
}

const handleSubmit = async () => {
  // 前端校验
  if (!id.value.trim() || !name.value.trim() || !username.value.trim() || !password.value.trim()) {
    alert('学号/工号、姓名、用户名和密码为必填项')
    return
  }

  try {
    const res = await fetch(apiUrl('/api/register'), {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        id: id.value,
        name: name.value,
        username: username.value,
        password: password.value,
        phoneNumber: phoneNumber.value,
        role: role.value,
      }),
    })

    if (!res.ok) {
      alert('注册失败，请检查后端服务')
      return
    }

    const result = await res.json()
    if (result.status) {
      alert(result.message)
      await router.push('/')
    } else {
      alert(result.message || '注册失败')
    }
  } catch (e) {
    console.error(e)
    alert('注册请求发送失败，请检查后端服务是否启动')
  }
}
</script>

<template>
  <div class="register-page">
    <h1 class="title">用户注册</h1>

    <div class="card">
      <div class="form-grid">
        <div class="form-item">
          <label for="id" class="label">学号/工号（必填）</label>
          <input id="id" v-model="id" type="text" class="input" placeholder="请输入对应身份ID" />
        </div>

        <div class="form-item">
          <label for="name" class="label">真实姓名（必填）</label>
          <input id="name" v-model="name" type="text" class="input" placeholder="请输入姓名" />
        </div>

        <div class="form-item">
          <label for="username" class="label">用户名（设置登录账号）</label>
          <input id="username" v-model="username" type="text" class="input" placeholder="请输入用户名" />
        </div>

        <div class="form-item">
          <label for="password" class="label">密码（设置登录密码）</label>
          <input id="password" v-model="password" type="password" class="input" placeholder="请输入密码" />
        </div>

        <div class="form-item">
          <label for="phone" class="label">手机号</label>
          <input id="phone" v-model="phoneNumber" type="text" class="input" placeholder="请输入手机号" />
        </div>

        <div class="form-item">
          <label class="label">注册身份</label>
          <div class="radio-group">
            <label class="radio-label">
              <input type="radio" v-model="role" :value="0" /> 学生
            </label>
            <label class="radio-label">
              <input type="radio" v-model="role" :value="1" /> 教师
            </label>
          </div>
        </div>
      </div>

      <button type="button" class="btn primary" @click="handleSubmit">
        提交注册
      </button>
      <button type="button" class="btn text-btn" @click="router.push('/')">
        已有账号？去登录
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
  margin-bottom: 2rem;
  font-size: 2rem;
  font-weight: 700;
  color: #1a202c;
}

.card {
  width: 100%;
  max-width: 560px;
  padding: 2.5rem;
  border-radius: 1.25rem;
  background: #ffffff;
  box-shadow: 
    0 20px 25px -5px rgba(0, 0, 0, 0.1),
    0 10px 10px -5px rgba(0, 0, 0, 0.04);
}

.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1.25rem;
  margin-bottom: 2rem;
}

@media (max-width: 480px) {
  .form-grid {
    grid-template-columns: 1fr;
  }
}

.form-item {
  display: flex;
  flex-direction: column;
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
  font-size: 0.95rem;
  transition: all 0.2s;
}

.input:focus {
  border-color: #4f46e5;
  box-shadow: 0 0 0 3px rgba(79, 70, 229, 0.1);
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

.btn {
  width: 100%;
  padding: 0.875rem;
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
  margin-bottom: 1rem;
}

.btn.primary:hover {
  background: #4338ca;
  transform: translateY(-1px);
}

.btn.text-btn {
  background: transparent;
  color: #4f46e5;
  font-size: 0.875rem;
}

.btn.text-btn:hover {
  text-decoration: underline;
}
</style>


