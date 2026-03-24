<script setup>
import { ref } from 'vue'

// 当前定时任务已执行次数，由后端返回
const runCount = ref<number | null>(null)
const loading = ref(false)
const errorMessage = ref('')

const handleQuery = async () => {
  loading.value = true
  errorMessage.value = ''
  try {
    // 约定后端提供接口，例如：
    // GET http://localhost:8080/task/count
    // 返回形如：{ "count": 5 }
    const res = await fetch('http://localhost:8080/task/count')
    if (!res.ok) {
      throw new Error(`查询失败：${res.status}`)
    }
    const data = await res.json()
    if (data == null || typeof data.count !== 'number') {
      throw new Error('返回数据中缺少 count 字段或类型不正确')
    }
    runCount.value = data.count
  } catch (e) {
    console.error(e)
    errorMessage.value = '无法查询定时任务执行次数，请检查后端服务是否正常运行'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="home-page">
    <h1 class="title">定时任务执行情况</h1>

    <div class="card">
      <p class="label">当前定时任务已执行次数：</p>
      <p class="count">
        {{ runCount !== null ? runCount : '尚未查询' }}
      </p>

      <button class="btn" type="button" @click="handleQuery" :disabled="loading">
        {{ loading ? '查询中...' : '查询' }}
      </button>

      <p v-if="errorMessage" class="error-text">
        {{ errorMessage }}
      </p>
    </div>
  </div>
</template>

<style scoped>
.home-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: #f5f7fb;
  padding: 2rem;
  color: #111827;
}

.title {
  margin-bottom: 1.5rem;
  font-size: 1.8rem;
  font-weight: 600;
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
  text-align: center;
}

.label {
  font-size: 0.95rem;
  color: #6b7280;
  margin-bottom: 0.5rem;
}

.count {
  font-size: 2rem;
  font-weight: 700;
  margin-bottom: 1.25rem;
}

.btn {
  padding: 0.6rem 1.2rem;
  border-radius: 0.6rem;
  border: none;
  background: linear-gradient(135deg, #4f46e5, #6366f1);
  color: #ffffff;
  font-size: 0.95rem;
  font-weight: 600;
  cursor: pointer;
  transition: transform 0.1s ease, box-shadow 0.1s ease, filter 0.15s ease;
  min-width: 100px;
}

.btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
  box-shadow: none;
}

.btn:not(:disabled):hover {
  filter: brightness(1.03);
  box-shadow: 0 6px 16px rgba(79, 70, 229, 0.35);
}

.btn:not(:disabled):active {
  transform: translateY(1px);
  box-shadow: 0 3px 10px rgba(79, 70, 229, 0.3);
}

.error-text {
  margin-top: 0.75rem;
  font-size: 0.9rem;
  color: #ef4444;
}
</style>


