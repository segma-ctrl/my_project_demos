<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'

import WelcomeIndex from '../components/booking/WelcomeIndex.vue'
import BookingManager from '../components/booking/BookingManager.vue'
import SystemSettings from '../components/booking/SystemSettings.vue'
import UserProfile from '../components/booking/UserProfile.vue'

const router = useRouter()
const user = ref(null)
const activeTab = ref('index') // 当前选中的标签页
const isSidebarCollapsed = ref(false)

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

// 计算面包屑内容
const breadcrumbs = computed(() => {
  const base = '系统控制台'
  const mapping = {
    index: '首页',
    booking: '预约管理',
    profile: '个人信息中心',
    settings: '系统高级设置'
  }
  return [base, mapping[activeTab.value]]
})

const navItems = computed(() => {
  const items = [
    { id: 'index', label: '首页', icon: '🏠' },
    { id: 'booking', label: '预约管理', icon: '📅' },
    { id: 'profile', label: '个人信息', icon: '👤' }
  ]
  if (user.value?.role === 2) {
    items.push({ id: 'settings', label: '系统设置', icon: '⚙️' })
  }
  return items
})
</script>

<template>
  <div class="main-layout" v-if="user">
    <!-- 左侧侧边栏 -->
    <aside class="sidebar" :class="{ collapsed: isSidebarCollapsed }">
      <div class="sidebar-header">
        <div class="logo-box">
          <img src="/logo.png" alt="Logo" class="logo-img" />
          <span v-if="!isSidebarCollapsed" class="system-name">高校教学预约<br/>管理平台</span>
        </div>
      </div>

      <nav class="sidebar-nav">
        <div 
          v-for="item in navItems" 
          :key="item.id"
          class="nav-item" 
          :class="{ active: activeTab === item.id }" 
          @click="activeTab = item.id"
        >
          <span class="nav-icon">{{ item.icon }}</span>
          <span v-if="!isSidebarCollapsed" class="nav-label">{{ item.label }}</span>
          <div v-if="activeTab === item.id" class="active-indicator"></div>
        </div>
      </nav>

      <div class="sidebar-footer">
        <button class="collapse-toggle" @click="isSidebarCollapsed = !isSidebarCollapsed">
           {{ isSidebarCollapsed ? '→' : '← 收起导航' }}
        </button>
      </div>
    </aside>

    <!-- 右侧内容区 -->
    <div class="main-container">
      <!-- 顶部条 (Top Bar) -->
      <header class="top-bar">
        <div class="top-left">
          <div class="breadcrumbs">
             <span v-for="(item, index) in breadcrumbs" :key="index" class="breadcrumb-item">
               {{ item }}
               <span v-if="index < breadcrumbs.length - 1" class="separator">/</span>
             </span>
          </div>
        </div>

        <div class="top-right">
          <div class="user-info">
            <div class="user-text">
              <span class="username">{{ user.username }}</span>
              <span class="role-tag">{{ getRoleName(user.role) }}</span>
            </div>
            <div class="user-avatar">{{ user.username.charAt(0).toUpperCase() }}</div>
          </div>
          <button class="logout-btn" @click="handleLogout">退出</button>
        </div>
      </header>

      <!-- 核心内容展示 -->
      <main class="content-view">
        <div class="content-card animate-fade-in">
          <!-- 首页内容 -->
          <div v-if="activeTab === 'index'">
            <WelcomeIndex :user="user" :getRoleName="getRoleName" />
          </div>

          <!-- 预约管理内容 -->
          <div v-if="activeTab === 'booking'">
            <BookingManager :user="user" />
          </div>

          <!-- 系统设置内容 -->
          <div v-if="activeTab === 'settings'">
            <SystemSettings />
          </div>

          <!-- 个人信息内容 -->
          <div v-if="activeTab === 'profile'">
            <UserProfile :user="user" :getRoleName="getRoleName" />
          </div>
        </div>
      </main>
    </div>
  </div>
</template>

<style scoped>
.main-layout {
  display: flex;
  min-height: 100vh;
  background-color: #f8fafc;
  color: #1e293b;
  font-family: 'Inter', -apple-system, sans-serif;
}

/* 侧边栏样式 */
.sidebar {
  width: 260px;
  background: #ffffff;
  border-right: 1px solid #e2e8f0;
  display: flex;
  flex-direction: column;
  transition: width 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  z-index: 100;
}

.sidebar.collapsed {
  width: 80px;
}

.sidebar-header {
  height: 100px;
  display: flex;
  align-items: center;
  padding: 0 1rem;
  border-bottom: 1px solid #f1f5f9;
}

.logo-box {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  overflow: hidden;
}

.logo-img {
  height: 50px;
  min-width: 50px;
}

.system-name {
  font-size: 1.15rem;
  font-weight: 800;
  color: #4f46e5;
  line-height: 1.3;
}

.sidebar-nav {
  flex: 1;
  padding: 1.5rem 0.75rem;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.nav-item {
  height: 50px;
  display: flex;
  align-items: center;
  padding: 0 1rem;
  border-radius: 0.75rem;
  cursor: pointer;
  position: relative;
  transition: all 0.2s;
  color: #64748b;
}

.nav-item:hover {
  background: #f1f5f9;
  color: #1e293b;
}

.nav-item.active {
  background: #eef2ff;
  color: #4f46e5;
  font-weight: 600;
}

.nav-icon {
  font-size: 1.25rem;
  min-width: 32px;
  display: flex;
  justify-content: center;
}

.nav-label {
  margin-left: 0.75rem;
  white-space: nowrap;
}

.active-indicator {
  position: absolute;
  left: 0;
  top: 15%;
  height: 70%;
  width: 4px;
  background: #4f46e5;
  border-radius: 0 4px 4px 0;
}

.sidebar-footer {
  padding: 1rem;
  border-top: 1px solid #f1f5f9;
}

.collapse-toggle {
  width: 100%;
  padding: 0.6rem;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 0.5rem;
  color: #64748b;
  font-size: 0.85rem;
  cursor: pointer;
}

/* 右侧主容器 */
.main-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  height: 100vh;
  overflow: hidden;
}

/* 顶部条 */
.top-bar {
  height: 70px;
  background: white;
  border-bottom: 1px solid #e2e8f0;
  padding: 0 2rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-shrink: 0;
}

.breadcrumbs {
  font-size: 0.9rem;
  color: #64748b;
}

.breadcrumb-item {
  display: inline-flex;
  align-items: center;
}

.separator {
  margin: 0 0.5rem;
  color: #cbd5e1;
}

.breadcrumb-item:last-child {
  color: #1e293b;
  font-weight: 600;
}

.top-right {
  display: flex;
  align-items: center;
  gap: 1.5rem;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.user-text {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}

.username {
  font-size: 0.9rem;
  font-weight: 700;
}

.role-tag {
  font-size: 0.75rem;
  color: #64748b;
}

.user-avatar {
  width: 40px;
  height: 40px;
  background: #4f46e5;
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  box-shadow: 0 4px 6px -1px rgba(79, 70, 229, 0.3);
}

.logout-btn {
  padding: 0.4rem 0.8rem;
  background: #fff1f2;
  color: #e11d48;
  border: 1px solid #ffe4e6;
  border-radius: 0.5rem;
  font-size: 0.85rem;
  font-weight: 600;
  cursor: pointer;
}

/* 内容区域 */
.content-view {
  flex: 1;
  padding: 2rem;
  overflow-y: auto;
  background-image: radial-gradient(#e2e8f0 0.5px, transparent 0.5px);
  background-size: 20px 20px;
}

.content-card {
  background: white;
  border-radius: 1.25rem;
  padding: 2rem;
  min-height: 100%;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05), 0 2px 4px -2px rgba(0, 0, 0, 0.05);
}

/* 动画 */
.animate-fade-in {
  animation: fadeIn 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

/* 自适应隐藏菜单文本 */
@media (max-width: 1024px) {
  .sidebar { width: 80px; }
  .nav-label, .system-name { display: none; }
}
</style>
