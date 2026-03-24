<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'

import WelcomeIndex from '../components/booking/WelcomeIndex.vue'
import BookingManager from '../components/booking/BookingManager.vue'
import SystemSettings from '../components/booking/SystemSettings.vue'
import UserProfile from '../components/booking/UserProfile.vue'

const router = useRouter()
const user = ref(null)
const activeTab = ref('index') // 当前选中的标签页

onMounted(() => {
  const userData = localStorage.getItem('user')
  if (!userData) {
    alert('请先登录')
    router.push('/')
    return
  }
  user.value = JSON.parse(userData)
})

const getRoleName = (role) => {
  const roles = {
    0: '学生',
    1: '教师',
    2: '管理员'
  }
  return roles[role] || '未知角色'
}

const handleLogout = () => {
  localStorage.removeItem('user')
  router.push('/')
}
</script>

<template>
  <div class="main-layout" v-if="user">
    <!-- 顶部导航栏 -->
    <nav class="navbar">
      <div class="nav-container">
        <div class="nav-left">
          <div class="logo-box">
            <img src="/logo.png" alt="CUMT Logo" class="logo-img" />
            <span class="system-name">高校教学预约管理平台</span>
          </div>
        </div>

        <div class="nav-center">
          <div 
            class="nav-item" 
            :class="{ active: activeTab === 'index' }" 
            @click="activeTab = 'index'"
          >
            首页
          </div>
          <div 
            class="nav-item" 
            :class="{ active: activeTab === 'booking' }" 
            @click="activeTab = 'booking'"
          >
            预约管理
          </div>
          <div 
            class="nav-item" 
            :class="{ active: activeTab === 'profile' }" 
            @click="activeTab = 'profile'"
          >
            个人信息
          </div>
          <div 
            v-if="user.role === 2" 
            class="nav-item" 
            :class="{ active: activeTab === 'settings' }" 
            @click="activeTab = 'settings'"
          >
            系统设置
          </div>
        </div>

        <div class="nav-right">
          <div class="user-profile">
            <span class="username">{{ user.username }}</span>
            <span class="role-tag">{{ getRoleName(user.role) }}</span>
          </div>
          <button class="logout-btn" @click="handleLogout">退出登录</button>
        </div>
      </div>
    </nav>

    <!-- 主内容区 -->
    <main class="content-body">
      <!-- 首页内容 -->
      <div v-if="activeTab === 'index'" class="tab-pane animate-fade-in">
        <WelcomeIndex :user="user" :getRoleName="getRoleName" />
      </div>

      <!-- 预约管理内容 -->
      <div v-if="activeTab === 'booking'" class="tab-pane animate-fade-in">
        <BookingManager :user="user" />
      </div>

      <!-- 系统设置内容 -->
      <div v-if="activeTab === 'settings'" class="tab-pane animate-fade-in">
        <SystemSettings />
      </div>

      <!-- 个人信息内容 -->
      <div v-if="activeTab === 'profile'" class="tab-pane animate-fade-in">
        <UserProfile :user="user" :getRoleName="getRoleName" />
      </div>
    </main>
  </div>
</template>

<style scoped>
.main-layout {
  min-height: 100vh;
  background-color: #f3f4f6;
  color: #1f2937;
  font-family: 'Inter', -apple-system, sans-serif;
}

/* 导航栏样式 */
.navbar {
  height: 64px;
  background: white;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 50;
}

.nav-container {
  max-width: 1280px;
  height: 100%;
  margin: 0 auto;
  padding: 0 1.5rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.logo-box {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.logo-img {
  height: 40px;
  width: auto;
  object-fit: contain;
}

.system-name {
  font-size: 1.125rem;
  font-weight: 700;
  color: #111827;
}

.nav-center {
  display: flex;
  gap: 1rem;
}

.nav-item {
  padding: 0.5rem 1rem;
  font-size: 0.95rem;
  font-weight: 500;
  color: #4b5563;
  border-radius: 0.5rem;
  cursor: pointer;
  transition: all 0.2s ease;
}

.nav-item:hover {
  background: #f3f4f6;
  color: #4f46e5;
}

.nav-item.active {
  background: #eef2ff;
  color: #4f46e5;
  font-weight: 600;
}

.nav-right {
  display: flex;
  align-items: center;
  gap: 1.5rem;
}

.user-profile {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}

.username {
  font-size: 0.875rem;
  font-weight: 600;
  color: #111827;
}

.role-tag {
  font-size: 0.75rem;
  color: #6b7280;
}

.logout-btn {
  padding: 0.4rem 0.8rem;
  font-size: 0.875rem;
  font-weight: 500;
  color: #ef4444;
  border: 1px solid #fee2e2;
  border-radius: 0.4rem;
  background: white;
  cursor: pointer;
  transition: all 0.2s;
}

.logout-btn:hover {
  background: #fef2f2;
}

/* 内容区样式 */
.content-body {
  max-width: 1280px;
  margin: 2rem auto;
  padding: 0 1.5rem;
}

.tab-pane {
  background: white;
  border-radius: 1rem;
  padding: 2.5rem;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05);
}

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

.page-header h2 {
  font-size: 1.5rem;
  font-weight: 700;
  margin-bottom: 0.5rem;
}

.page-header p {
  color: #6b7280;
  margin-bottom: 2rem;
}

.empty-state {
  text-align: center;
  padding: 4rem 0;
  color: #9ca3af;
  border: 2px dashed #e5e7eb;
  border-radius: 0.75rem;
}

/* 动画 */
.animate-fade-in {
  animation: fadeIn 0.3s ease-out;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
