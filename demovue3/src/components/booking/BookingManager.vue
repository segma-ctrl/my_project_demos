<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { apiUrl } from '../../config/api.js'

const props = defineProps({
  user: {
    type: Object,
    required: true
  }
})

// 控制侧边栏显示
const showDrawer = ref(false)
// 控制侧边栏是否展开为宽屏模式
const isExpanded = ref(false)
// 加载状态
const loading = ref(false)
const bookingsLoading = ref(false)
const submitting = ref(false)
// 资源列表数据
const resourceList = ref([])
// 我的预约列表数据
const myBookings = ref([])

// 预约状态过滤配置
const statusFilters = [
  { label: '全部申请', value: 'all' },
  { label: '审核中', value: 'PENDING' },
  { label: '预约成功', value: 'APPROVED' },
  { label: '未通过', value: 'REJECTED' },
  { label: '已取消', value: 'CANCELED' }
]
const activeStatusFilter = ref('all')

const filteredMyBookings = computed(() => {
  if (activeStatusFilter.value === 'all') return myBookings.value
  return myBookings.value.filter(booking => {
    let sCode = booking.status
    if (sCode === 0) sCode = 'PENDING'
    if (sCode === 1) sCode = 'APPROVED'
    if (sCode === 2) sCode = 'REJECTED'
    if (sCode === 3) sCode = 'CANCELED'
    return sCode === activeStatusFilter.value
  })
})

// 筛选条件响应式状态
const filters = reactive({
  type: 'all',
  date: '',
  startTime: '08:00',
  endTime: '10:00',
  capacity: 'any',
  searchText: '',
  selectedResourceId: null,
  purpose: '',
  phone: ''
})

const resourceTypes = [
  { label: '全部', value: 'all' },
  { label: '教室', value: 1 },
  { label: '实验室', value: 2 },
  { label: '报告厅', value: 3 }, 
  { label: '设备', value: 4 }
]

const capacityOptions = [
  { label: '不限人数', value: 'any' },
  { label: '10人以下', value: '0-10' },
  { label: '10~30人', value: '10-30' },
  { label: '30~50人', value: '30-50' },
  { label: '50~80人', value: '50-80' },
  { label: '80~120人', value: '80-120' },
  { label: '120人以上', value: '120+' }
]

// 生成 07:00 到 22:30 的 30分钟步长时间序列
const generateTimeSlots = () => {
  const times = []
  let hour = 7
  let minute = 0
  while (hour < 22 || (hour === 22 && minute <= 30)) {
    const h = hour.toString().padStart(2, '0')
    const m = minute.toString().padStart(2, '0')
    times.push(`${h}:${m}`)
    minute += 30
    if (minute === 60) {
      hour++
      minute = 0
    }
  }
  return times
}

const timeOptions = generateTimeSlots()

const bookStatusMap = {
  0: { label: '审核中', id:'pending', color: '#f59e0b', bg: '#fff7ed', icon: '⏳' },
  'PENDING': { label: '审核中', id:'pending', color: '#f59e0b', bg: '#fff7ed', icon: '⏳' },
  1: { label: '预约成功', id:'approved', color: '#10b981', bg: '#f0fdf4', icon: '✅' },
  'APPROVED': { label: '预约成功', id:'approved', color: '#10b981', bg: '#f0fdf4', icon: '✅' },
  2: { label: '未通过', id:'rejected', color: '#ef4444', bg: '#fef2f2', icon: '❌' },
  'REJECTED': { label: '未通过', id:'rejected', color: '#ef4444', bg: '#fef2f2', icon: '❌' },
  3: { label: '已取消', id:'canceled', color: '#6b7280', bg: '#f3f4f6', icon: '🗑️' },
  'CANCELED': { label: '已取消', id:'canceled', color: '#6b7280', bg: '#f3f4f6', icon: '🗑️' }
}

onMounted(() => {
  fetchMyBookings()
})

const fetchMyBookings = async () => {
  bookingsLoading.value = true
  try {
    const res = await fetch(apiUrl('/api/booking/list'), {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: props.user.username 
    })
    if (res.ok) {
      myBookings.value = await res.json()
    }
  } catch (e) {
    console.error('获取预约记录失败', e)
  } finally {
    bookingsLoading.value = false
  }
}

// 计算过滤后的结果
const filteredResources = computed(() => {
  return resourceList.value.filter(item => {
    if (filters.type !== 'all' && item.type !== filters.type) return false
    if (filters.searchText && !(item.resourceName || '').toLowerCase().includes(filters.searchText.toLowerCase())) return false
    if (filters.capacity !== 'any') {
      const cap = item.capacity
      if (filters.capacity === '0-10' && cap > 10) return false
      if (filters.capacity === '10-30' && (cap < 10 || cap > 30)) return false
      if (filters.capacity === '30-50' && (cap < 30 || cap > 50)) return false
      if (filters.capacity === '50-80' && (cap < 50 || cap > 80)) return false
      if (filters.capacity === '80-120' && (cap < 80 || cap > 120)) return false
      if (filters.capacity === '120+' && cap < 120) return false
    }
    return true
  })
})

const fetchResources = async () => {
  if (!filters.date || !isValidTimeRange.value) {
    resourceList.value = []
    return
  }

  loading.value = true
  try {
    const payload = {}
    if (filters.type && filters.type !== 'all') payload.type = filters.type
    if (filters.date) payload.date = filters.date
    if (filters.date && filters.startTime) payload.startTime = filters.startTime + ':00'
    if (filters.date && filters.endTime) payload.endTime = filters.endTime + ':00'

    const res = await fetch(apiUrl('/api/resource'), {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    })
    
    if (res.ok) {
      resourceList.value = await res.json()
    } else {
      console.error('后端过滤接口请求失败', res.status)
    }
  } catch (e) {
    console.error('获取资源失败', e)
  } finally {
    loading.value = false
  }
}

import { watch } from 'vue'
watch(
  () => [filters.type, filters.date, filters.startTime, filters.endTime],
  () => {
    fetchResources()
    // 筛选条件改变后清除选中的资源ID，避免提交无效单
    filters.selectedResourceId = null
  }
)

const openBooking = () => {
  showDrawer.value = true
  fetchResources()
}

const closeDrawer = () => {
  showDrawer.value = false
  isExpanded.value = false
  resetFilters()
}

const toggleExpand = () => {
  isExpanded.value = !isExpanded.value
}

const getResourceTypeName = (type) => {
  const t = resourceTypes.find(i => i.value === type)
  return t ? t.label : '其他'
}

const getStatusStyle = (status) => {
  let sCode = status;
  if (status === 'PENDING') sCode = 0;
  else if (status === 'APPROVED') sCode = 1;
  else if (status === 'REJECTED') sCode = 2;
  else if (status === 'CANCELED') sCode = 3;
  
  return bookStatusMap[sCode] || { label: '未知状态', color: '#64748b', bg: '#f1f5f9', icon: '❓' }
}

const isValidTimeRange = computed(() => {
  return filters.startTime < filters.endTime
})

const resetFilters = () => {
  Object.assign(filters, { 
    type: 'all', 
    date: '', 
    startTime: '08:00', 
    endTime: '10:00', 
    capacity: 'any', 
    searchText: '',
    selectedResourceId: null,
    purpose: '',
    phone: ''
  })
}

const selectResource = (id) => {
  filters.selectedResourceId = id
}

const submitBooking = async () => {
  if (!filters.selectedResourceId || !filters.date || !isValidTimeRange.value) return
  
  submitting.value = true
  try {
    const res = await fetch(apiUrl('/api/booking/add'), {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        userId: props.user.username,
        userName: props.user.name || props.user.username, // 冗余字段：保存真实姓名或用户名
        resourceId: filters.selectedResourceId,
        resourceName: resourceList.value.find(r => r.id === filters.selectedResourceId)?.resourceName, // 冗余字段：保存当时的资源名称
        bookingDate: filters.date,
        startTime: filters.startTime + ":00",
        endTime: filters.endTime + ":00",
        purpose: filters.purpose,
        applicantPhone: filters.phone,
        status: 0 // PENDING
      })
    })
    
    if (res.ok) {
      alert('预约申请已提交，请等待管理员审批！')
      closeDrawer()
      fetchMyBookings()
    } else {
      alert('预约失败: 请检查网络或联系管理员')
    }
  } catch (e) {
    alert('连接服务器失败，请稍后重试')
    console.error(e)
  } finally {
    submitting.value = false
  }
}

const cancelBooking = async (id) => {
  if (!window.confirm('确定要撤销这条预约申请吗？')) return
  
  try {
    const res = await fetch(apiUrl('/api/booking/cancel'), {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(id)
    })
    
    if (res.ok) {
      const result = await res.json()
      if (result.status) {
        alert(result.message || '撤销成功')
        fetchMyBookings()
      } else {
        alert(result.message || '撤销失败')
      }
    } else {
      alert('撤销请求失败: 请检查后端服务')
    }
  } catch (e) {
    alert('连接服务器失败，请稍后重试')
    console.error(e)
  }
}
</script>

<template>
  <div class="booking-manager">
    <div class="page-header">
      <div class="header-left">
        <h2>预约管理</h2>
        <p v-if="user.role === 0">在这里查看您的申请历史或发起新的预约。</p>
        <p v-else-if="user.role === 1">在这里查看您的教学预约或发起排课申请。</p>
        <p v-else>资源管理员后台，支持对预约记录的全量巡检。</p>
      </div>
      <div class="header-right" v-if="user.role === 0 || user.role === 1">
        <button class="primary-btn" @click="openBooking">
          <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="5" x2="12" y2="19"></line><line x1="5" y1="12" x2="19" y2="12"></line></svg>
          发起新预约
        </button>
      </div>
    </div>
    
    <div class="bookings-list-container">
      <!-- 状态筛选栏 -->
      <div class="status-filter-bar" v-if="myBookings.length > 0 || activeStatusFilter !== 'all'">
        <button 
          v-for="filter in statusFilters" 
          :key="filter.value"
          class="status-tab"
          :class="{ active: activeStatusFilter === filter.value }"
          @click="activeStatusFilter = filter.value"
        >
          {{ filter.label }}
        </button>
      </div>

      <div v-if="bookingsLoading" class="list-loading">
         <div class="spinner"></div>
         <p>正在为您同步最新的预约记录...</p>
      </div>
      
      <div v-else-if="myBookings.length === 0" class="placeholder-content">
        <div class="empty-state">
          <div class="icon">📅</div>
          <p>您目前还没有任何预约记录</p>
          <p class="hint">点击右上方“发起新预约”开始您的第一次预约吧！</p>
        </div>
      </div>

      <div v-else-if="filteredMyBookings.length === 0" class="placeholder-content">
        <div class="empty-state">
          <div class="icon">🔍</div>
          <p>未找到该状态下的预约记录</p>
          <p class="hint">请尝试切换其他状态分类查看</p>
        </div>
      </div>

      <div v-else class="bookings-stream">
        <div v-for="booking in filteredMyBookings" :key="booking.id" class="modern-booking-card">
           <div class="card-side-accent" :style="{ backgroundColor: getStatusStyle(booking.status).color }"></div>
           
           <div class="card-main-content">
              <div class="card-top">
                 <div class="status-tag" :style="{ backgroundColor: getStatusStyle(booking.status).bg, color: getStatusStyle(booking.status).color }">
                    <span class="s-icon">{{ getStatusStyle(booking.status).icon }}</span>
                    {{ getStatusStyle(booking.status).label }}
                 </div>
                 <div class="booking-meta">
                   <div class="booking-id">NO.{{ booking.id || 'N/A' }}</div>
                   <div class="create-time" v-if="booking.createTime">申请于: {{ booking.createTime }}</div>
                 </div>
              </div>
              
              <div class="card-center">
                 <div class="res-preview">
                    <div class="res-icon" :style="{ color: getStatusStyle(booking.status).color, backgroundColor: getStatusStyle(booking.status).bg }">
                      <svg viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 21h18"></path><path d="M9 8h1"></path><path d="M9 12h1"></path><path d="M9 16h1"></path><path d="M14 8h1"></path><path d="M14 12h1"></path><path d="M14 16h1"></path><path d="M5 21V5a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2v16"></path></svg>
                    </div>
                    <div class="res-details">
                       <h4 class="res-name">{{ booking.resourceName || '正在加载资源...' }}</h4>
                       <span class="res-sub">资源ID: {{ booking.resourceId || '#' }}</span>
                    </div>
                 </div>
                 
                 <div class="time-block">
                    <div class="time-item">
                       <span class="label">预约日期</span>
                       <span class="val">{{ booking.bookingDate || '-- / -- / --' }}</span>
                    </div>
                    <div class="time-divider"></div>
                    <div class="time-item">
                       <span class="label">使用时段</span>
                       <span class="val">{{ booking.startTime ? booking.startTime.substring(0,5) : '--:--' }} - {{ booking.endTime ? booking.endTime.substring(0,5) : '--:--' }}</span>
                    </div>
                 </div>
              </div>

              <div class="card-bottom">
                 <div class="purpose-peek" v-if="booking.purpose">
                    <span class="p-label">用途:</span> {{ booking.purpose }}
                 </div>
                 <div class="action-row">
                    <button class="ghost-btn-sm">申请详情</button>
                    <button v-if="booking.status === 0 || booking.status === 'PENDING'" class="danger-btn-sm" @click="cancelBooking(booking.id)">撤销</button>
                 </div>
              </div>
           </div>
        </div>
      </div>
    </div>

    <!-- 侧边预约抽屉 (Drawer) -->
    <div class="drawer-overlay" :class="{ active: showDrawer }" @click="closeDrawer">
      <div class="drawer-content" :class="{ active: showDrawer, 'is-expanded': isExpanded }" @click.stop>
        <div class="drawer-header">
          <div class="header-main">
            <h3>发起资源预约</h3>
            <div class="expand-mode-toggle">
               <div class="toggle-bg" :class="{ right: isExpanded }"></div>
               <button class="mode-btn" :class="{ active: !isExpanded }" @click="isExpanded = false" title="紧凑视图">
                  <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M4 6h16M4 12h16M4 18h7" /></svg>
                  <span>极简</span>
               </button>
               <button class="mode-btn" :class="{ active: isExpanded }" @click="isExpanded = true" title="大屏筛选器">
                  <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2zM9 4v16M15 4v16" /></svg>
                  <span>沉浸</span>
               </button>
            </div>
          </div>
          <button class="close-btn" @click="closeDrawer">
            <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><line x1="18" y1="6" x2="6" y2="18"></line><line x1="6" y1="6" x2="18" y2="18"></line></svg>
          </button>
        </div>
        
        <div class="drawer-body">
          <div class="booking-layout-container" :class="{ 'grid-layout': isExpanded }">
            <div class="search-section">
              <div class="section-title">
                <span class="title-icon">🎯</span> 资源检索组合
              </div>
              
              <div class="filter-group">
                <label>资源类型</label>
                <div class="type-tags">
                  <span 
                    v-for="type in resourceTypes" 
                    :key="type.value"
                    class="type-tag"
                    :class="{ active: filters.type === type.value }"
                    @click="filters.type = type.value"
                  >
                    {{ type.label }}
                  </span>
                </div>
              </div>

              <div class="filter-group">
                <label class="required">预约日期</label>
                <div class="input-wrapper">
                  <input type="date" v-model="filters.date" class="filter-input" />
                </div>
              </div>

              <div class="filter-group">
                <label class="required">预约时间</label>
                <div class="time-range-picker">
                   <select v-model="filters.startTime" class="filter-select half">
                      <option v-for="t in timeOptions" :key="t" :value="t">{{ t }}</option>
                   </select>
                   <span class="range-sep">至</span>
                   <select v-model="filters.endTime" class="filter-select half" :class="{ error: !isValidTimeRange }">
                      <option v-for="t in timeOptions" :key="t" :value="t">{{ t }}</option>
                   </select>
                </div>
                <p v-if="!isValidTimeRange" class="error-hint">结束时间必须晚于开始时间</p>
              </div>

              <div class="filter-group">
                <label>容纳人数</label>
                <div class="input-wrapper">
                  <select v-model="filters.capacity" class="filter-select">
                    <option v-for="opt in capacityOptions" :key="opt.value" :value="opt.value">
                      {{ opt.label }}
                    </option>
                  </select>
                </div>
              </div>

              <div class="filter-group">
                <label>搜索关键词</label>
                <div class="input-wrapper">
                  <input type="text" v-model="filters.searchText" placeholder="资源名称、编号..." class="filter-input" />
                </div>
              </div>

              <button class="reset-btn-styled" @click="resetFilters">
                <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M1 4v6h6M23 20v-6h-6" /><path d="M20.49 9A9 9 0 0 0 5.64 5.64L1 10m22 4l-4.64 4.36A9 9 0 0 1 3.51 15" /></svg>
                重置筛选条件
              </button>
            </div>

            <div class="main-content-section">
              <div class="section-title">
                <span class="title-icon">📊</span> 可预约资源列表 ({{ filteredResources.length }})
              </div>

              <div v-if="loading" class="loading-state">
                 <div class="spinner"></div>
                 <p>努力加载资源中...</p>
              </div>

              <div v-else-if="filteredResources.length === 0" class="empty-results">
                <div class="empty-icon-lg">📂</div>
                <p>没有找到符合条件的资源，请修改刚才的筛选条件</p>
              </div>

              <div class="results-container" v-else>
                <div class="refined-resource-list">
                  <div 
                    v-for="item in filteredResources" 
                    :key="item.id" 
                    class="refined-card"
                    :class="{ selected: filters.selectedResourceId === item.id }"
                    @click="selectResource(item.id)"
                  >
                    <div class="card-main">
                       <div class="type-indicator" :class="'type-' + item.type">
                          {{ getResourceTypeName(item.type).substring(0, 1) }}
                       </div>
                       <div class="middle-box">
                          <h4 class="res-name">{{ item.resourceName }}</h4>
                          <div class="meta-info">
                             <span class="meta-item">
                               <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"></path><circle cx="12" cy="10" r="3"></circle></svg>
                               {{ item.location }}
                             </span>
                             <span class="meta-item">
                               <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path><circle cx="9" cy="7" r="4"></circle><path d="M23 21v-2a4 4 0 0 0-3-3.87"></path><path d="M16 3.13a4 4 0 0 1 0 7.75"></path></svg>
                               {{ item.capacity }} 人
                             </span>
                          </div>
                       </div>
                       <div class="action-box">
                          <div v-if="filters.selectedResourceId === item.id" class="selected-badge">
                            <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"><polyline points="20 6 9 17 4 12"></polyline></svg>
                            已选定
                          </div>
                          <button v-else class="select-btn-premium">选择资源</button>
                       </div>
                    </div>
                  </div>
                </div>
              </div>

              <!-- 填写预约详情 -->
              <transition name="fade">
                <div class="booking-details-form" v-if="filters.selectedResourceId">
                   <div class="section-title">
                      <span class="title-icon">📝</span> 补充详细预约信息
                   </div>
                   <div class="form-grid">
                      <div class="filter-group">
                         <label>联系电话（选填）</label>
                         <input type="tel" v-model="filters.phone" class="filter-input" placeholder="请输入您的联系电话" />
                      </div>
                      <div class="filter-group">
                         <label>预约核心用途</label>
                         <textarea v-model="filters.purpose" class="filter-input text-area" placeholder="请详细说明您申请该资源的具体用途（必填考量）..."></textarea>
                      </div>
                   </div>
                </div>
              </transition>
            </div>
          </div>
        </div>

        <div class="drawer-footer">
          <button 
            class="submit-action-btn" 
            :disabled="!filters.selectedResourceId || !filters.date || !isValidTimeRange || submitting" 
            @click="submitBooking"
          >
            <span v-if="submitting" class="flex-center gap-2">
              <div class="spinner-sm"></div> 提交中...
            </span>
            <span v-else class="flex-center gap-2">
               确认并提交预约申请规则
               <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="5" y1="12" x2="19" y2="12"></line><polyline points="12 5 19 12 12 19"></polyline></svg>
            </span>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* 基础与通用布局 */
.booking-manager {
  max-width: 1200px;
  margin: 0 auto;
  color: #1e293b;
}

.flex-center {
  display: flex;
  align-items: center;
  justify-content: center;
}
.gap-2 { gap: 0.5rem; }

/* 页面头部及精美主按钮 */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
}

.page-header h2 {
  font-size: 1.85rem;
  font-weight: 800;
  color: #0f172a;
  letter-spacing: -0.02em;
  margin-bottom: 0.25rem;
}

.page-header p {
  color: #64748b;
  font-size: 0.95rem;
}

.primary-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.8rem 1.75rem;
  background: linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%);
  color: white;
  border: none;
  border-radius: 1rem;
  font-size: 0.95rem;
  font-weight: 700;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(79, 70, 229, 0.25);
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
}

.primary-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 16px rgba(124, 58, 237, 0.35);
}

/* 状态过滤栏 */
.status-filter-bar {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 1.5rem;
  background: white;
  padding: 0.5rem;
  border-radius: 1rem;
  box-shadow: 0 1px 3px rgba(0,0,0,0.02);
  border: 1px solid #f1f5f9;
  width: fit-content;
}

.status-tab {
  padding: 0.6rem 1.25rem;
  background: transparent;
  color: #64748b;
  border: none;
  border-radius: 0.75rem;
  font-weight: 700;
  font-size: 0.9rem;
  cursor: pointer;
  transition: all 0.2s;
}

.status-tab:hover {
  background: #f8fafc;
  color: #334155;
}

.status-tab.active {
  background: #f1f5f9;
  color: #4f46e5;
  box-shadow: inset 0 2px 4px rgba(0,0,0,0.02);
}

/* 占位提示与加载 */
.list-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 4rem 0;
  color: #64748b;
  font-weight: 500;
  gap: 1rem;
}

.placeholder-content {
  padding: 5rem 0;
  display: flex;
  justify-content: center;
}

.empty-state {
  text-align: center;
  background: white;
  padding: 3rem 5rem;
  border-radius: 1.5rem;
  box-shadow: 0 1px 3px rgba(0,0,0,0.02);
  border: 1px dashed #cbd5e1;
}

.empty-state .icon {
  font-size: 3rem;
  margin-bottom: 1rem;
  opacity: 0.8;
}

.empty-state p {
  font-size: 1.1rem;
  font-weight: 700;
  color: #334155;
  margin-bottom: 0.5rem;
}

.empty-state .hint {
  font-size: 0.9rem;
  color: #94a3b8;
  font-weight: 400;
}

/* 预约流卡片设计 (Modern Booking Stream) */
.bookings-stream {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.modern-booking-card {
  background: white;
  border-radius: 1.25rem;
  display: flex;
  overflow: hidden;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.02);
  border: 1px solid #f1f5f9;
}

.modern-booking-card:hover {
  transform: translateY(-4px) scale(1.002);
  box-shadow: 0 12px 24px -6px rgba(30, 41, 59, 0.08);
  border-color: #e2e8f0;
}

.card-side-accent {
  width: 6px;
  flex-shrink: 0;
  transition: background 0.3s;
}

.card-main-content {
  flex: 1;
  padding: 1.5rem;
}

.card-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
}

.status-tag {
  display: flex;
  align-items: center;
  gap: 0.4rem;
  padding: 0.4rem 0.85rem;
  border-radius: 2rem;
  font-size: 0.8rem;
  font-weight: 800;
  letter-spacing: 0.02em;
}

.booking-id {
  font-family: 'JetBrains Mono', Menlo, monospace;
  color: #94a3b8;
  font-weight: 700;
  font-size: 0.8rem;
  background: #f8fafc;
  padding: 0.3rem 0.6rem;
  border-radius: 0.5rem;
}

.booking-meta {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 0.2rem;
}

.create-time {
  font-size: 0.75rem;
  color: #94a3b8;
  font-weight: 500;
}

.card-center {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 2rem;
  margin-bottom: 1.5rem;
}

.res-preview {
  display: flex;
  align-items: center;
  gap: 1.25rem;
  flex: 1;
}

.res-icon {
  width: 52px;
  height: 52px;
  border-radius: 1.1rem;
  display: flex;
  align-items: center;
  justify-content: center;
}

.res-name {
  font-size: 1.15rem;
  font-weight: 800;
  color: #0f172a;
  margin: 0 0 0.3rem 0;
}

.res-sub {
  font-size: 0.8rem;
  color: #64748b;
  font-weight: 600;
}

.time-block {
  display: flex;
  align-items: center;
  gap: 2.5rem;
  background: #f8fafc;
  padding: 0.85rem 1.5rem;
  border-radius: 1rem;
  border: 1px solid #f1f5f9;
}

.time-item {
  display: flex;
  flex-direction: column;
  gap: 0.3rem;
}

.time-item .label {
  font-size: 0.7rem;
  color: #94a3b8;
  font-weight: 800;
  letter-spacing: 0.05em;
  text-transform: uppercase;
}

.time-item .val {
  font-size: 0.95rem;
  font-weight: 800;
  color: #1e293b;
}

.time-divider {
  width: 1.5px;
  height: 24px;
  background: #e2e8f0;
}

.card-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 1.25rem;
  border-top: 1px dashed #e2e8f0;
}

.purpose-peek {
  font-size: 0.85rem;
  color: #64748b;
  font-weight: 600;
  max-width: 65%;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  background: #f8fafc;
  padding: 0.4rem 0.8rem;
  border-radius: 0.5rem;
}

.p-label {
  font-weight: 800;
  color: #475569;
}

.action-row {
  display: flex;
  gap: 0.75rem;
}

.ghost-btn-sm {
  background: transparent;
  border: 1.5px solid #f1f5f9;
  padding: 0.5rem 1rem;
  border-radius: 0.75rem;
  font-weight: 800;
  color: #4f46e5;
  font-size: 0.8rem;
  cursor: pointer;
  transition: all 0.2s;
}

.ghost-btn-sm:hover {
  background: #f5f3ff;
  border-color: #e0e7ff;
}

.danger-btn-sm {
  background: #fff1f2;
  border: 1.5px solid #ffe4e6;
  padding: 0.5rem 1rem;
  border-radius: 0.75rem;
  font-weight: 800;
  color: #e11d48;
  font-size: 0.8rem;
  cursor: pointer;
  transition: all 0.2s;
}

.danger-btn-sm:hover {
  background: #ffe4e6;
  border-color: #fecdd3;
}

/* 侧边抽屉设计 (Premium Drawer) */
.drawer-overlay {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.4);
  backdrop-filter: blur(8px);
  z-index: 1000;
  opacity: 0;
  visibility: hidden;
  transition: opacity 0.3s ease, visibility 0.3s ease;
}

.drawer-overlay.active {
  opacity: 1;
  visibility: visible;
}

.drawer-content {
  position: absolute;
  top: 0;
  right: -100%;
  width: 550px;
  height: 100%;
  background: #f8fafc;
  transition: all 0.4s cubic-bezier(0.16, 1, 0.3, 1);
  display: flex;
  flex-direction: column;
  z-index: 1001;
  box-shadow: -20px 0 60px rgba(0, 0, 0, 0.15);
}

.drawer-content.active {
  right: 0;
}

.drawer-content.is-expanded {
  width: 85% !important;
}

.drawer-header {
  padding: 1.25rem 2rem;
  background: white;
  border-bottom: 1px solid #f1f5f9;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-shrink: 0;
}

.drawer-header h3 {
  font-size: 1.25rem;
  font-weight: 800;
  color: #0f172a;
  margin: 0;
}

.header-main {
  display: flex;
  align-items: center;
  gap: 1.5rem;
}

/* 带有悬浮滑块的酷炫分段开关 */
.expand-mode-toggle {
  position: relative;
  display: flex;
  padding: 0.25rem;
  background: #f1f5f9;
  border-radius: 0.85rem;
  gap: 0.2rem;
  box-shadow: inset 0 2px 4px rgba(0,0,0,0.02);
}

.toggle-bg {
  position: absolute;
  top: 0.25rem;
  left: 0.25rem;
  width: calc(50% - 0.25rem);
  height: calc(100% - 0.5rem);
  background: white;
  border-radius: 0.65rem;
  box-shadow: 0 2px 6px rgba(0,0,0,0.05);
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  z-index: 1;
}

.toggle-bg.right {
  transform: translateX(100%);
}

.mode-btn {
  position: relative;
  z-index: 2;
  display: flex;
  align-items: center;
  gap: 0.4rem;
  padding: 0.5rem 1rem;
  border: none;
  background: transparent;
  color: #64748b;
  font-size: 0.8rem;
  font-weight: 700;
  cursor: pointer;
  transition: color 0.3s ease;
}

.mode-btn.active {
  color: #4f46e5;
}

.close-btn {
  background: #f8fafc;
  border: 1px solid #f1f5f9;
  color: #94a3b8;
  width: 36px;
  height: 36px;
  border-radius: 0.75rem;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
}

.close-btn:hover {
  background: #fef2f2;
  color: #ef4444;
  border-color: #fee2e2;
}

/* 抽屉主体区域布局 */
.drawer-body {
  flex: 1;
  padding: 2rem;
  overflow-y: auto;
}

.booking-layout-container {
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

.booking-layout-container.grid-layout {
  flex-direction: row;
  align-items: flex-start;
}

/* 搜索筛选表单区 */
.search-section {
  flex: 0 0 auto;
  background: white;
  padding: 1.75rem;
  border-radius: 1.25rem;
  border: 1px solid #e2e8f0;
  box-shadow: 0 4px 6px -1px rgba(0,0,0,0.02);
}

.grid-layout .search-section {
  flex: 0 0 340px;
}

.main-content-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.section-title {
  font-size: 1.05rem;
  font-weight: 800;
  color: #0f172a;
  margin-bottom: 1.5rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.filter-group {
  margin-bottom: 1.5rem;
}

.filter-group label {
  display: block;
  font-size: 0.85rem;
  font-weight: 700;
  color: #475569;
  margin-bottom: 0.75rem;
}

.required::after {
  content: " *";
  color: #ef4444;
}

/* 胶囊标签样式 */
.type-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 0.6rem;
}

.type-tag {
  padding: 0.5rem 1rem;
  background: #f8fafc;
  border: 1.5px solid #e2e8f0;
  border-radius: 2rem;
  cursor: pointer;
  font-size: 0.8rem;
  font-weight: 700;
  color: #64748b;
  transition: all 0.2s;
}

.type-tag:hover {
  border-color: #cbd5e1;
  color: #334155;
}

.type-tag.active {
  background: #4f46e5;
  border-color: #4f46e5;
  color: white;
  box-shadow: 0 4px 6px rgba(79, 70, 229, 0.15);
}

/* 输入框样式统一 */
.input-wrapper {
  position: relative;
}

.filter-input, .filter-select {
  width: 100%;
  padding: 0.75rem 1rem;
  border: 1.5px solid #e2e8f0;
  background: #f8fafc;
  border-radius: 0.85rem;
  outline: none;
  font-size: 0.9rem;
  color: #1e293b;
  font-weight: 600;
  transition: all 0.2s;
}

.filter-input:focus, .filter-select:focus {
  border-color: #818cf8;
  background: white;
  box-shadow: 0 0 0 3px #e0e7ff;
}

.filter-select {
  appearance: none;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='none' stroke='%2364748b' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'%3E%3Cpolyline points='6 9 12 15 18 9'%3E%3C/polyline%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 1rem center;
  background-size: 1em;
}

.time-range-picker {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.filter-select.half { flex: 1; min-width: 0; }
.range-sep { color: #94a3b8; font-weight: 700; font-size: 0.85rem; }
.filter-select.error { border-color: #ef4444; background: #fef2f2; }
.error-hint { color: #ef4444; font-size: 0.8rem; font-weight: 600; margin-top: 0.5rem; }

.reset-btn-styled {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  padding: 0.85rem;
  background: white;
  color: #f43f5e;
  border: 1.5px solid #ffe4e6;
  border-radius: 0.85rem;
  font-weight: 800;
  cursor: pointer;
  transition: all 0.2s;
  margin-top: 0.5rem;
}

.reset-btn-styled:hover {
  background: #fff1f2;
  border-color: #fecdd3;
}

/* 筛选出的资源列表 */
.refined-resource-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.refined-card {
  background: white;
  border: 1.5px solid #e2e8f0;
  border-radius: 1.25rem;
  padding: 1.25rem;
  cursor: pointer;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
}

.refined-card:hover {
  transform: translateX(4px);
  border-color: #cbd5e1;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
}

.refined-card.selected {
  border-color: #4f46e5;
  background: #f8fafc;
  box-shadow: 0 0 0 1px #4f46e5;
}

.card-main {
  display: flex;
  align-items: center;
  gap: 1.25rem;
}

.type-indicator {
  width: 50px;
  height: 50px;
  border-radius: 14px;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 800;
  font-size: 1.25rem;
  flex-shrink: 0;
  box-shadow: 0 4px 6px rgba(0,0,0,0.1);
}

.type-1 { background: linear-gradient(135deg, #6366f1, #818cf8); }
.type-2 { background: linear-gradient(135deg, #10b981, #34d399); }
.type-3 { background: linear-gradient(135deg, #f59e0b, #fbbf24); }

.middle-box { flex: 1; }

.res-name {
  font-size: 1.1rem;
  font-weight: 800;
  color: #0f172a;
  margin: 0 0 0.4rem 0;
}

.meta-info {
  display: flex;
  gap: 1.25rem;
  font-size: 0.85rem;
  color: #64748b;
  font-weight: 600;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 0.4rem;
}

.select-btn-premium {
  padding: 0.6rem 1.25rem;
  background: #f8fafc;
  color: #4f46e5;
  border: 1.5px solid #e0e7ff;
  border-radius: 0.75rem;
  font-weight: 800;
  font-size: 0.85rem;
  transition: all 0.2s;
  cursor: pointer;
}

.refined-card:hover .select-btn-premium {
  background: white;
  border-color: #a5b4fc;
}

.selected-badge {
  display: flex;
  align-items: center;
  gap: 0.4rem;
  color: #10b981;
  font-weight: 800;
  font-size: 0.9rem;
  padding: 0.5rem 1rem;
  background: #f0fdf4;
  border-radius: 0.75rem;
}

/* 填写预约详情形态 */
.booking-details-form {
  margin-top: 1rem;
  padding: 1.75rem;
  background: white;
  border-radius: 1.25rem;
  border: 1px solid #e0e7ff;
  box-shadow: 0 4px 6px rgba(79, 70, 229, 0.05);
}

.text-area {
  min-height: 120px;
  resize: vertical;
  line-height: 1.5;
}

/* 底部提交区 */
.drawer-footer {
  padding: 1.5rem 2rem;
  background: white;
  border-top: 1px solid #f1f5f9;
  flex-shrink: 0;
}

.submit-action-btn {
  width: 100%;
  padding: 1.15rem;
  background: #4f46e5;
  color: white;
  border: none;
  border-radius: 1rem;
  font-size: 1.05rem;
  font-weight: 800;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 4px 12px rgba(79, 70, 229, 0.3);
}

.submit-action-btn:hover:not(:disabled) {
  background: #4338ca;
  box-shadow: 0 6px 16px rgba(79, 70, 229, 0.4);
  transform: translateY(-2px);
}

.submit-action-btn:disabled {
  background: #e2e8f0;
  color: #94a3b8;
  box-shadow: none;
  cursor: not-allowed;
  transform: none;
}

/* 动效与全局组件 */
.fade-enter-active, .fade-leave-active { transition: opacity 0.3s, transform 0.3s; }
.fade-enter-from, .fade-leave-to { opacity: 0; transform: translateY(10px); }

.empty-results {
  text-align: center;
  padding: 4rem 0;
  color: #94a3b8;
  font-weight: 500;
}

.empty-icon-lg {
  font-size: 3.5rem;
  margin-bottom: 1rem;
  opacity: 0.5;
  filter: grayscale(1);
}

.spinner {
  width: 28px;
  height: 28px;
  border: 3px solid #e2e8f0;
  border-top: 3px solid #4f46e5;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

.spinner-sm {
  width: 18px;
  height: 18px;
  border: 2px solid rgba(255,255,255,0.3);
  border-top: 2px solid white;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin { 100% { transform: rotate(360deg); } }
</style>
