<script setup>
import { ref, onMounted } from 'vue'
import { apiUrl } from '../../config/api.js'

const resourceList = ref([])
const loading = ref(false)

// 新增资源表单相关的响应式变量
const showAddDrawer = ref(false)
const isExpanded = ref(false)
const submitting = ref(false)
const newResource = ref({
  resourceName: '',
  type: 1,
  location: '',
  capacity: 30,
  description: ''
})

const resourceTypes = [
  { label: '教室', value: 1 },
  { label: '实验室', value: 2 },
  { label: '报告厅', value: 3 },
  { label: '设备', value: 4 }
]

const fetchResources = async () => {
  loading.value = true
  try {
    const res = await fetch(apiUrl('/api/admin/list'))
    if (res.ok) {
      resourceList.value = await res.json()
    }
  } catch (e) {
    console.error('获取资源列表失败', e)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchResources()
})

const getTypeName = (type) => {
  const t = resourceTypes.find(i => i.value === parseInt(type))
  return t ? t.label : '未知类型'
}

// 帮助函数：判断资源当前是否可用
const isAvailable = (status) => {
  return status === 1 || status === 'AVAILABLE'
}

// 打开抽屉
const openAddDrawer = () => {
  newResource.value = { resourceName: '', type: 1, location: '', capacity: 30, description: '' }
  isExpanded.value = false
  showAddDrawer.value = true
}

// 提交新资源
const submitAddResource = async () => {
  if (!newResource.value.resourceName || !newResource.value.location) {
    alert('请填写完整必填项（资源名称和地点）')
    return
  }
  
  submitting.value = true
  try {
    const payload = {
      resourceName: newResource.value.resourceName,
      type: newResource.value.type,
      location: newResource.value.location,
      capacity: [1, 2, 3].includes(parseInt(newResource.value.type)) ? newResource.value.capacity : null,
      description: newResource.value.description,
      status: 1 // 默认 AVAILABLE 可以用 1 或后端能解析的值
    }
    
    const res = await fetch(apiUrl('/api/admin/add'), {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    })
    
    if (res.ok) {
      const result = await res.json()
      if (result.code === 200) {
        alert('资源添加成功')
        showAddDrawer.value = false
        fetchResources()
      } else {
        alert(result.msg || '添加失败')
      }
    } else {
      alert('请求后端接口失败')
    }
  } catch (e) {
    alert('发生网络错误')
    console.error(e)
  } finally {
    submitting.value = false
  }
}

// 切换资源状态（启用/停用）
const toggleStatus = async (item) => {
  const currentAvailable = isAvailable(item.status)
  const actionLabel = currentAvailable ? '停用' : '启用'
  const endpoint = currentAvailable ? 'disable' : 'enable'
  
  if (!confirm(`确定要${actionLabel}资源 "${item.resourceName}" 吗？\n状态改变后会直接影响前端用户的预约展示。`)) return
  
  try {
    const res = await fetch(apiUrl(`/api/admin/${endpoint}`), {
      method: 'POST',
      // 这里如果后端以 String 接收，直接传字符串并声明为 json 以免跨域报错
      headers: { 'Content-Type': 'application/json' },
      // 如果后端需要的是纯字符串或 JSON 里的属性，这里可能需要调整。
      // 因为是 @RequestBody String id，我们把 id 直接作为 body 内容。如果遇到序列化错误，可试着 toString()
      body: item.id.toString()
    })
    
    if (res.ok) {
      const result = await res.json()
      if (result.code === 200) {
        alert(`${actionLabel}成功`)
        fetchResources()
      } else {
        alert(result.msg || `${actionLabel}失败`)
      }
    } else {
      alert('请求后端失败')
    }
  } catch (e) {
    alert('发生网络问题')
    console.error(e)
  }
}
</script>

<template>
  <div class="resource-manager">
    <div class="page-header">
      <div class="header-left">
        <h2>系统资源底座管理</h2>
        <p>在这里全盘掌控所有可供预约的硬件设施，支持一键启停和上架新资源。</p>
      </div>
      <div class="header-right">
        <button class="primary-btn" @click="openAddDrawer">
          <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="5" x2="12" y2="19"></line><line x1="5" y1="12" x2="19" y2="12"></line></svg>
          登记新资源
        </button>
      </div>
    </div>

    <!-- 列表展示区域 -->
    <div v-if="loading" class="status-state">
      <div class="spinner"></div>
      <p>正在拉取全量资源库数据...</p>
    </div>

    <div v-else-if="resourceList.length === 0" class="empty-state">
      <div class="icon">🏫</div>
      <p>您的资源库目前还是空的。</p>
      <p class="hint">点击右上方按钮开始录入第一项资源吧。</p>
    </div>

    <div v-else class="resource-list-container">
      <table class="modern-table">
        <thead>
          <tr>
            <th>库编号</th>
            <th>资源概览 (名称/位置)</th>
            <th>物理类型</th>
            <th>容纳规模</th>
            <th>当前系统状态</th>
            <th>操作面板</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in resourceList" :key="item.id">
            <td>
              <div class="id-tag">ID:{{ item.id }}</div>
            </td>
            <td>
               <div class="res-name">{{ item.resourceName }}</div>
               <div class="res-location">
                 <svg viewBox="0 0 24 24" width="12" height="12" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"></path><circle cx="12" cy="10" r="3"></circle></svg>
                 {{ item.location }}
               </div>
            </td>
            <td>
              <span class="type-badge">{{ getTypeName(item.type) }}</span>
            </td>
            <td>
              <span class="capacity-text">{{ item.capacity }} 人</span>
            </td>
            <td>
              <div class="status-tag" :class="isAvailable(item.status) ? 'status-active' : 'status-disabled'">
                <span class="dot"></span>
                {{ isAvailable(item.status) ? '可预约 (正常)' : '已停用 (维护)' }}
              </div>
            </td>
            <td class="action-cell">
              <!-- 由于启停是危险操作，做出明显样式区分 -->
              <button 
                v-if="isAvailable(item.status)"
                class="ghost-btn-sm danger"
                @click="toggleStatus(item)"
              >
                下架停用
              </button>
              <button 
                v-else
                class="ghost-btn-sm success"
                @click="toggleStatus(item)"
              >
                恢复上架
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 这里沿用预约界面的侧边抽屉风格作为添加表单 -->
    <div class="drawer-overlay" :class="{ active: showAddDrawer }" @click="showAddDrawer = false">
      <div class="drawer-content" :class="{ active: showAddDrawer, 'is-expanded': isExpanded }" @click.stop>
        <div class="drawer-header">
          <div style="display: flex; align-items: center; gap: 1.5rem;">
            <h3>登记新设施资源</h3>
            <div class="expand-mode-toggle">
               <div class="toggle-bg" :class="{ right: isExpanded }"></div>
               <button class="mode-btn" :class="{ active: !isExpanded }" @click="isExpanded = false" title="紧凑视图">
                  <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M4 6h16M4 12h16M4 18h7" /></svg>
                  <span>极简</span>
               </button>
               <button class="mode-btn" :class="{ active: isExpanded }" @click="isExpanded = true" title="大屏扩展">
                  <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2zM9 4v16M15 4v16" /></svg>
                  <span>宽屏</span>
               </button>
            </div>
          </div>
          <button class="close-btn" @click="showAddDrawer = false">
            <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><line x1="18" y1="6" x2="6" y2="18"></line><line x1="6" y1="6" x2="18" y2="18"></line></svg>
          </button>
        </div>
        
        <div class="drawer-body">
          <div class="form-container" :class="{ 'grid-layout': isExpanded }">
            <div class="filter-group form-item-full">
              <label class="required">资源中文名称</label>
              <div class="input-wrapper">
                <input type="text" v-model="newResource.resourceName" class="filter-input" placeholder="例如：主楼 A305 多媒体教室" />
              </div>
            </div>

            <div class="filter-group">
              <label class="required">所属类型</label>
              <div class="input-wrapper">
                <select v-model="newResource.type" class="filter-select">
                  <option v-for="t in resourceTypes" :key="t.value" :value="t.value">{{ t.label }}</option>
                </select>
              </div>
            </div>

            <div class="filter-group">
              <label class="required">物理地点</label>
              <div class="input-wrapper">
                <input type="text" v-model="newResource.location" class="filter-input" placeholder="例如：软件学院 3 区 2 层" />
              </div>
            </div>

            <div class="filter-group" v-if="[1, 2, 3].includes(parseInt(newResource.type))">
              <label>最高容纳人数</label>
              <div class="input-wrapper">
                <input type="number" v-model="newResource.capacity" class="filter-input" min="1" max="1000" />
              </div>
              <p class="hint">该数值将用于前台用户的筛选项判断。</p>
            </div>

            <div class="filter-group form-item-full">
              <label>资源详细描述（选填）</label>
              <div class="input-wrapper">
                <textarea v-model="newResource.description" class="filter-input text-area" placeholder="针对该设备的详细参数说明、或对该教室的特色说明..."></textarea>
              </div>
            </div>
            
            <div class="notice-box form-item-full">
              <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"></circle><line x1="12" y1="8" x2="12" y2="12"></line><line x1="12" y1="16" x2="12.01" y2="16"></line></svg>
              <span>注意：新资源录入后将默认直接分配为“正常可预约”状态，立即可见于前台。</span>
            </div>
          </div>
        </div>

        <div class="drawer-footer">
          <button class="submit-action-btn" :disabled="submitting" @click="submitAddResource">
            <span v-if="submitting">正在保存数据...</span>
            <span v-else>确认保存此资源</span>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* 保持与整体系统高度一致的设计语言 */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
}

.header-left h2 {
  font-size: 1.6rem;
  font-weight: 800;
  margin-bottom: 0.5rem;
  color: #0f172a;
}

.header-left p {
  color: #64748b;
  font-size: 0.95rem;
}

.primary-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.8rem 1.75rem;
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  color: white;
  border: none;
  border-radius: 1rem;
  font-size: 0.95rem;
  font-weight: 700;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(16, 185, 129, 0.25);
  transition: all 0.25s;
}

.primary-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 16px rgba(16, 185, 129, 0.35);
}

.status-state, .empty-state {
  text-align: center;
  padding: 5rem 0;
  color: #94a3b8;
  background: white;
  border-radius: 1.25rem;
  border: 1px dashed #cbd5e1;
}

.empty-state .icon {
  font-size: 4rem;
  margin-bottom: 1rem;
  opacity: 0.8;
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

/* 高级表格样式 */
.modern-table {
  width: 100%;
  border-collapse: separate;
  border-spacing: 0;
  background: white;
  border-radius: 1.25rem;
  overflow: hidden;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05);
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
  border-bottom: 1px solid #e2e8f0;
}

.modern-table td {
  padding: 1.25rem 1.5rem;
  border-bottom: 1px dashed #f1f5f9;
  vertical-align: middle;
}

tbody tr:hover td {
  background-color: #f8fafc;
}

.id-tag {
  font-family: 'JetBrains Mono', monospace;
  font-size: 0.8rem;
  color: #64748b;
  background: #f1f5f9;
  padding: 0.3rem 0.6rem;
  border-radius: 0.5rem;
  font-weight: 700;
  display: inline-block;
}

.res-name {
  font-weight: 800;
  color: #1e293b;
  font-size: 1.05rem;
  margin-bottom: 0.3rem;
}

.res-location {
  display: flex;
  align-items: center;
  gap: 0.2rem;
  color: #94a3b8;
  font-size: 0.8rem;
  font-weight: 500;
}

.type-badge {
  background: #e0e7ff;
  color: #4338ca;
  padding: 0.4rem 0.85rem;
  border-radius: 2rem;
  font-size: 0.8rem;
  font-weight: 800;
}

.capacity-text {
  font-weight: 800;
  color: #475569;
}

/* 状态圆点标签 */
.status-tag {
  display: inline-flex;
  align-items: center;
  gap: 0.4rem;
  padding: 0.4rem 0.8rem;
  border-radius: 2rem;
  font-size: 0.8rem;
  font-weight: 700;
}

.status-active {
  background: #f0fdf4;
  color: #059669;
}

.status-active .dot {
  width: 6px; height: 6px;
  background: #10b981;
  border-radius: 50%;
  box-shadow: 0 0 0 2px rgba(16, 185, 129, 0.2);
}

.status-disabled {
  background: #fef2f2;
  color: #e11d48;
}

.status-disabled .dot {
  width: 6px; height: 6px;
  background: #ef4444;
  border-radius: 50%;
  box-shadow: 0 0 0 2px rgba(239, 68, 68, 0.2);
}

/* 操作按钮 */
.ghost-btn-sm {
  background: transparent;
  padding: 0.5rem 1rem;
  border-radius: 0.75rem;
  font-weight: 800;
  font-size: 0.8rem;
  cursor: pointer;
  transition: all 0.2s;
}

.ghost-btn-sm.danger {
  color: #ef4444;
  border: 1.5px solid #ffe4e6;
}
.ghost-btn-sm.danger:hover { background: #fef2f2; border-color: #fecdd3; }

.ghost-btn-sm.success {
  color: #10b981;
  border: 1.5px solid #d1fae5;
}
.ghost-btn-sm.success:hover { background: #f0fdf4; border-color: #a7f3d0; }

/* 右侧表单抽屉样式 */
.drawer-overlay {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.4);
  backdrop-filter: blur(4px);
  z-index: 1000;
  opacity: 0;
  visibility: hidden;
  transition: all 0.3s;
}

.drawer-overlay.active {
  opacity: 1;
  visibility: visible;
}

.drawer-content {
  position: absolute;
  top: 0;
  right: -500px;
  width: 450px;
  height: 100%;
  background: #f8fafc;
  display: flex;
  flex-direction: column;
  transition: right 0.4s cubic-bezier(0.16, 1, 0.3, 1);
  box-shadow: -10px 0 40px rgba(0, 0, 0, 0.1);
}

.drawer-content.active {
  right: 0;
}

.drawer-content.is-expanded {
  width: 800px !important;
}

/* 开关切换钮 */
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

.drawer-header {
  padding: 1.5rem 2rem;
  background: white;
  border-bottom: 1px solid #f1f5f9;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.drawer-header h3 {
  font-size: 1.25rem;
  font-weight: 800;
  color: #0f172a;
  margin: 0;
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
.close-btn:hover { background: #fef2f2; color: #ef4444; }

.drawer-body {
  flex: 1;
  padding: 2rem;
  overflow-y: auto;
}

.form-container {
  background: white;
  padding: 1.5rem;
  border-radius: 1.25rem;
  border: 1px solid #e2e8f0;
}

.form-container.grid-layout {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0 1.5rem;
}

.form-item-full {
  grid-column: 1 / -1;
}

.text-area {
  min-height: 100px;
  resize: vertical;
  line-height: 1.5;
}

.filter-group { margin-bottom: 1.5rem; }
.filter-group label {
  display: block;
  font-size: 0.85rem;
  font-weight: 700;
  color: #475569;
  margin-bottom: 0.5rem;
}
.required::after { content: " *"; color: #ef4444; }

.filter-input, .filter-select {
  width: 100%;
  padding: 0.85rem 1rem;
  border: 1.5px solid #e2e8f0;
  background: #f8fafc;
  border-radius: 0.85rem;
  outline: none;
  font-size: 0.95rem;
  font-weight: 600;
  transition: border-color 0.2s;
  box-sizing: border-box;
}
.filter-input:focus, .filter-select:focus { border-color: #10b981; }

.filter-select {
  appearance: none;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='none' stroke='%2364748b' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'%3E%3Cpolyline points='6 9 12 15 18 9'%3E%3C/polyline%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 1rem center;
  background-size: 1em;
}

.hint {
  font-size: 0.75rem;
  color: #94a3b8;
  margin-top: 0.4rem;
}

.notice-box {
  display: flex;
  align-items: flex-start;
  gap: 0.5rem;
  background: #fffbeb;
  padding: 1rem;
  border-radius: 0.75rem;
  color: #b45309;
  font-size: 0.85rem;
  font-weight: 600;
  line-height: 1.5;
}

.drawer-footer {
  padding: 1.5rem 2rem;
  background: white;
  border-top: 1px solid #f1f5f9;
}

.submit-action-btn {
  width: 100%;
  padding: 1rem;
  background: #10b981;
  color: white;
  border: none;
  border-radius: 1rem;
  font-weight: 800;
  font-size: 1rem;
  cursor: pointer;
  transition: all 0.2s;
}
.submit-action-btn:hover:not(:disabled) { background: #059669; }
.submit-action-btn:disabled { background: #94a3b8; cursor: not-allowed; }

.spinner {
  width: 28px;
  height: 28px;
  border: 3px solid #e2e8f0;
  border-top: 3px solid #10b981;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto;
}
@keyframes spin { 100% { transform: rotate(360deg); } }
</style>
