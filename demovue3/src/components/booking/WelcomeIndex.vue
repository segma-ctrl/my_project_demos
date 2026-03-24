<script setup>
defineProps({
  user: {
    type: Object,
    required: true
  },
  getRoleName: {
    type: Function,
    required: true
  }
})
</script>

<template>
  <div class="welcome-section">
    <div class="welcome-header">
      <h1>欢迎回来，{{ user.username }}</h1>
      <p>今天是 {{ new Date().toLocaleDateString() }}，祝您使用愉快。</p>
    </div>
    <div class="status-grid">
      <div class="status-card">
        <h3>身份信息</h3>
        <p>当前权限：{{ getRoleName(user.role) }}</p>
      </div>
      <div class="status-card">
        <h3>系统公告</h3>
        <p>系统运行正常，欢迎使用预约功能。</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.welcome-header h1 {
  font-size: 2rem;
  font-weight: 800;
  margin-bottom: 0.5rem;
}

.welcome-header p {
  color: #6b7280;
  margin-bottom: 2rem;
}

.status-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 1.5rem;
}

.status-card {
  padding: 1.5rem;
  background: #f9fafb;
  border-radius: 0.75rem;
  border: 1px solid #f3f4f6;
  transition: transform 0.2s;
}

.status-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05);
}

.status-card h3 {
  font-size: 1rem;
  font-weight: 600;
  margin-bottom: 0.5rem;
}

.status-card p {
  font-size: 0.9rem;
  color: #4b5563;
}
</style>
