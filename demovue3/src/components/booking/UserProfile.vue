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
  <div class="user-profile">
    <div class="page-header">
      <h2>个人中心</h2>
      <p>管理个人基本信息与账号安全。</p>
    </div>
    
    <div class="profile-card">
      <div class="info-item">
        <label>账户名</label>
        <span>{{ user.username }}</span>
      </div>
      <div class="info-item">
        <label>身份角色</label>
        <span class="role-badge">{{ getRoleName(user.role) }}</span>
      </div>
      <div class="info-item">
        <label>所属系统</label>
        <span>高校教学预约管理平台</span>
      </div>
    </div>
    
    <div class="placeholder-section">
      <div class="empty-state">
        <div class="icon">👤</div>
        <p>个人资料详细信息正在对接中...</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.page-header h2 {
  font-size: 1.5rem;
  font-weight: 700;
  margin-bottom: 0.5rem;
}

.page-header p {
  color: #6b7280;
  margin-bottom: 2rem;
}

.profile-card {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
  padding: 2rem;
  background: #fdfdfd;
  border-radius: 0.75rem;
  border: 1px solid #f1f5f9;
  margin-bottom: 2rem;
}

.info-item {
  display: grid;
  grid-template-columns: 120px 1fr;
  align-items: center;
}

.info-item label {
  color: #94a3b8;
  font-size: 0.9rem;
  font-weight: 500;
}

.info-item span {
  font-weight: 600;
  color: #1e293b;
}

.role-badge {
  display: inline-block;
  padding: 0.2rem 0.6rem;
  background: #e0e7ff;
  color: #4338ca;
  font-size: 0.8rem;
  border-radius: 4px;
}

.empty-state {
  text-align: center;
  padding: 3rem 0;
  color: #9ca3af;
  border: 2px dashed #f1f5f9;
  border-radius: 1rem;
}

.icon {
  font-size: 2.5rem;
  margin-bottom: 0.5rem;
}
</style>
