<script setup>
import { ref, onMounted, computed } from 'vue'
import { apiUrl } from '../../config/api.js'

const allApprovalList = ref([])
const loading = ref(false)

const activeFilter = ref(0) // 默认停留待审批
const statusFilters = [
  { label: '待审批', value: 0 },
  { label: '已通过', value: 1 },
  { label: '已驳回', value: 2 },
  { label: '全部记录', value: 'all' }
]

const filteredList = computed(() => {
  if (activeFilter.value === 'all') return allApprovalList.value
  return allApprovalList.value.filter(item => {
    let sCode = item.status
    if (sCode === 'PENDING') sCode = 0
    if (sCode === 'APPROVED') sCode = 1
    if (sCode === 'REJECTED') sCode = 2
    if (sCode === 'CANCELED') sCode = 3
    return sCode === activeFilter.value
  })
})

const getStatusText = (status) => {
  let sCode = status
  if (sCode === 'PENDING') sCode = 0
  if (sCode === 'APPROVED') sCode = 1
  if (sCode === 'REJECTED') sCode = 2
  if (sCode === 'CANCELED') sCode = 3
  if (sCode === 0) return '待审批'
  if (sCode === 1) return '已通过'
  if (sCode === 2) return '已驳回'
  if (sCode === 3) return '已取消'
  return '未知状态'
}

const fetchApprovalBookings = async () => {
  loading.value = true
  try {
    const res = await fetch(apiUrl('/api/approval/list'))
    if (res.ok) {
      allApprovalList.value = await res.json()
    }
  } catch (e) {
    console.error('获取审批列表失败', e)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchApprovalBookings()
})

const handleApprove = async (id) => {
  if (!confirm('确定要通过该预约申请吗？')) return
  
  try {
    const res = await fetch(apiUrl('/api/approval/approve'), {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(id)
    })
    
    if (res.ok) {
      const result = await res.json()
      if (result.code === 200) {
        alert('审批通过成功')
        fetchApprovalBookings()
      } else {
        alert(result.msg || '操作失败')
      }
    } else {
      alert('请求后端失败，请检查服务状态')
    }
  } catch (e) {
    alert('请求失败，请检查网络')
    console.error(e)
  }
}

const handleReject = async (id) => {
  if (!confirm('确定要在此时驳回该预约申请吗？这将会使该资源重新变为空闲状态。')) return
  
  try {
    const res = await fetch(apiUrl('/api/approval/reject'), {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(id)
    })
    
    if (res.ok) {
      const result = await res.json()
      if (result.code === 200) {
        alert('已成功驳回该申请并且释放资源')
        fetchApprovalBookings()
      } else {
        alert(result.msg || '驳回操作执行失败')
      }
    } else {
      alert('请求后端失败，请检查服务状态')
    }
  } catch (e) {
    alert('请求失败，请检查您的网络连接')
    console.error(e)
  }
}
</script>

<template>
  <div class="approval-manager">
    <div class="page-header">
      <h2>预约审批工作台</h2>
      <p>管理员可以在这里查看所有预约申请并进行快速审批或追溯历史。</p>
    </div>

    <!-- 状态筛选栏 -->
    <div class="status-filter-bar">
      <button 
        v-for="filter in statusFilters" 
        :key="filter.value"
        class="status-tab"
        :class="{ active: activeFilter === filter.value }"
        @click="activeFilter = filter.value"
      >
        {{ filter.label }}
      </button>
    </div>

    <div v-if="loading" class="status-state">
      <div class="spinner"></div> <!-- 假设这跟全局有相同的 css 或可以用文字代替 -->
      <p>正在拉取最新审批数据...</p>
    </div>

    <div v-else-if="filteredList.length === 0" class="empty-state">
      <div class="icon">✨</div>
      <p>太棒了！未找到该状态下的预约记录。</p>
      <p class="hint">可以去关注其他状态的数据啦</p>
    </div>

    <div v-else class="approval-list">
      <table class="modern-table">
        <thead>
          <tr>
            <th>单号 / 申请人</th>
            <th>资源信息</th>
            <th>使用时间窗口</th>
            <th>核心用途</th>
            <th>联系电话</th>
            <th>状态 / 操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in filteredList" :key="item.id">
            <td>
              <div class="booking-id-tag">NO.{{ item.id }}</div>
              <div class="user-badge">{{ item.userName || item.userId || '未知用户' }}</div>
            </td>
            <td>
              <div class="res-name">{{ item.resourceName || '关联资源 '+item.resourceId }}</div>
            </td>
            <td class="time-col">
              <div class="date">{{ item.bookingDate }}</div>
              <div class="time">{{ item.startTime?.substring(0,5) }} 至 {{ item.endTime?.substring(0,5) }}</div>
            </td>
            <td class="purpose-col" :title="item.purpose">{{ item.purpose || '（无备注）' }}</td>
            <td>{{ item.applicantPhone || '未填写' }}</td>
            <td class="action-cell">
              <template v-if="item.status === 0 || item.status === 'PENDING'">
                <button class="approve-btn" @click="handleApprove(item.id)">
                    <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"><polyline points="20 6 9 17 4 12"></polyline></svg>
                    同意
                </button>
                <button class="reject-btn" @click="handleReject(item.id)">驳回</button>
              </template>
              <template v-else>
                <span class="status-text" :class="'status-' + (item.status === 'APPROVED' ? 1 : item.status === 'REJECTED' ? 2 : item.status)">
                  {{ getStatusText(item.status) }}
                </span>
              </template>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<style scoped>
.page-header h2 {
  font-size: 1.6rem;
  font-weight: 800;
  margin-bottom: 0.5rem;
  color: #0f172a;
}
.page-header p {
  color: #64748b;
  margin-bottom: 2rem;
  font-size: 0.95rem;
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

.status-text {
  font-weight: 700;
  font-size: 0.9rem;
  padding: 0.4rem 0.8rem;
  border-radius: 2rem;
  display: inline-block;
  text-align: center;
}

.status-1 { color: #10b981; background: #f0fdf4; }
.status-2 { color: #ef4444; background: #fef2f2; }
.status-3 { color: #64748b; background: #f1f5f9; }

.status-state, .empty-state {
  text-align: center;
  padding: 5rem 0;
  color: #94a3b8;
  background: white;
  border-radius: 1.25rem;
  border: 1px dashed #cbd5e1;
  box-shadow: 0 1px 3px rgba(0,0,0,0.02);
}

.empty-state .icon {
  font-size: 4rem;
  margin-bottom: 1rem;
}
.empty-state p {
  font-size: 1.15rem;
  font-weight: 700;
  color: #334155;
  margin-bottom: 0.5rem;
}
.empty-state .hint {
  font-size: 0.9rem;
  font-weight: 400;
  color: #94a3b8;
}

.modern-table {
  width: 100%;
  border-collapse: separate;
  border-spacing: 0;
  background: white;
  border-radius: 1.25rem;
  overflow: hidden;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05), 0 2px 4px -2px rgba(0, 0, 0, 0.05);
  border: 1px solid #f1f5f9;
}

.modern-table th {
  background: #f8fafc;
  padding: 1.25rem 1.5rem;
  text-align: left;
  font-size: 0.8rem;
  font-weight: 800;
  color: #64748b;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  border-bottom: 1px solid #e2e8f0;
}

.modern-table td {
  padding: 1.5rem;
  border-bottom: 1px dashed #f1f5f9;
  vertical-align: middle;
}

tbody tr:hover td {
  background-color: #f8fafc;
}

.booking-id-tag {
  font-family: 'JetBrains Mono', monospace;
  font-size: 0.75rem;
  color: #94a3b8;
  margin-bottom: 0.4rem;
}

.user-badge {
  background: #eef2ff;
  color: #4f46e5;
  padding: 0.35rem 0.8rem;
  border-radius: 2rem;
  font-weight: 700;
  font-size: 0.85rem;
  display: inline-block;
}

.res-name {
  font-weight: 800;
  color: #1e293b;
  font-size: 1.05rem;
}

.time-col {
  display: flex;
  flex-direction: column;
  gap: 0.3rem;
}
.time-col .date {
  font-weight: 700;
  color: #334155;
}
.time-col .time {
  font-size: 0.85rem;
  color: #64748b;
  font-weight: 600;
  background: #f8fafc;
  padding: 0.2rem 0.5rem;
  border-radius: 0.4rem;
  display: inline-block;
  width: fit-content;
}

.purpose-col {
  max-width: 200px;
  line-height: 1.5;
  color: #475569;
  font-size: 0.9rem;
}

.action-cell {
  display: flex;
  gap: 0.75rem;
}

.approve-btn {
  display: flex;
  align-items: center;
  gap: 0.3rem;
  background: #10b981;
  color: white;
  border: none;
  padding: 0.6rem 1.2rem;
  border-radius: 0.75rem;
  font-weight: 700;
  font-size: 0.9rem;
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: 0 2px 4px rgba(16, 185, 129, 0.2);
}
.approve-btn:hover { 
  background: #059669; 
  transform: translateY(-1px);
}

.reject-btn {
  background: #fff1f2;
  color: #e11d48;
  border: 1.5px solid #ffe4e6;
  padding: 0.6rem 1.2rem;
  border-radius: 0.75rem;
  font-weight: 700;
  font-size: 0.9rem;
  cursor: pointer;
  transition: all 0.2s;
}
.reject-btn:hover { 
  background: #ffe4e6; 
}
</style>
