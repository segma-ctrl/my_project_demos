<script setup>
import { ref, onMounted } from 'vue'

const userList = ref([])
const loading = ref(false)
const showAddDrawer = ref(false)
const submitting = ref(false)

const newUser = ref({
  id: '',
  username: '',
  password: '',
  role: 0,
  phoneNumber: ''
})

const userRoles = [
  { label: '学生', value: 0 },
  { label: '教师', value: 1 },
  { label: '管理员', value: 2 }
]

const fetchUsers = async () => {
  loading.value = true
  try {
    const res = await fetch('http://localhost:8080/api/admin/user/list')
    if (res.ok) {
      const result = await res.json()
      if (result.code === 200) {
        userList.value = result.data
      }
    }
  } catch (e) {
    console.error('获取用户列表失败', e)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchUsers()
})

const getRoleName = (role) => {
  const r = userRoles.find(i => i.value === parseInt(role))
  return r ? r.label : '未知角色'
}

const openAddDrawer = () => {
  newUser.value = { id: '', username: '', password: '', role: 0, phoneNumber: '' }
  showAddDrawer.value = true
}

const submitAddUser = async () => {
  if (!newUser.value.id || !newUser.value.username || !newUser.value.password) {
    alert('请填写完整必填项（ID、用户名和密码）')
    return
  }
  
  submitting.value = true
  try {
    const res = await fetch('http://localhost:8080/api/admin/user/add', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(newUser.value)
    })
    
    if (res.ok) {
      const result = await res.json()
      if (result.code === 200) {
        alert('用户添加成功')
        showAddDrawer.value = false
        fetchUsers()
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

const downloadTemplate = () => {
  window.open('http://localhost:8080/api/admin/user/downloadTemplate', '_blank')
}

const fileInput = ref(null)
const importing = ref(false)

const triggerFileUpload = () => {
  fileInput.value.click()
}

const handleFileUpload = async (event) => {
  const file = event.target.files[0]
  if (!file) return

  const formData = new FormData()
  formData.append('file', file)

  importing.value = true
  try {
    const res = await fetch('http://localhost:8080/api/admin/user/import', {
      method: 'POST',
      body: formData
    })
    
    if (res.ok) {
      const result = await res.json()
      if (result.code === 200) {
        alert('批量导入成功')
        fetchUsers()
      } else {
        alert(result.msg || '导入失败，请检查文件格式')
      }
    } else {
      alert('批量导入请求失败')
    }
  } catch (e) {
    alert('发生网络错误')
    console.error(e)
  } finally {
    importing.value = false
    // 清除选择，以便下次选择同一文件也能触发
    event.target.value = ''
  }
}
</script>

<template>
  <div class="user-manager">
    <div class="page-header">
      <div class="header-left">
        <h2>用户管理中心</h2>
        <p>在此管理系统用户信息，包括学生、教师和管理员。权限颗粒度精确到每个终端用户。</p>
      </div>
      <div class="header-right">
        <input type="file" ref="fileInput" style="display: none" accept=".xlsx, .xls" @change="handleFileUpload">
        <button class="secondary-btn" @click="downloadTemplate">
           <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v4"></path><polyline points="7 10 12 15 17 10"></polyline><line x1="12" y1="15" x2="12" y2="3"></line></svg>
           下载导入模板
        </button>
        <button class="primary-btn" :disabled="importing" @click="triggerFileUpload">
           <svg v-if="!importing" viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v4"></path><polyline points="17 8 12 3 7 8"></polyline><line x1="12" y1="3" x2="12" y2="15"></line></svg>
           <div v-else class="btn-spinner"></div>
           {{ importing ? '导入中...' : '批量导入用户' }}
        </button>
        <button class="primary-btn" @click="openAddDrawer">
           <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="5" x2="12" y2="19"></line><line x1="5" y1="12" x2="19" y2="12"></line></svg>
           新增单个用户
        </button>
      </div>
    </div>

    <!-- 列表展示 -->
    <div v-if="loading" class="status-state">
      <div class="spinner"></div>
      <p>正在同步全库用户索引...</p>
    </div>

    <div v-else-if="userList.length === 0" class="empty-state">
      <div class="icon">👥</div>
      <p>暂无用户信息</p>
    </div>

    <div v-else class="user-list-container">
      <table class="modern-table">
        <thead>
          <tr>
            <th>用户ID</th>
            <th>基本信息</th>
            <th>系统权限</th>
            <th>联系电话</th>
            <th>创建日期</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in userList" :key="item.id">
            <td>
              <div class="id-tag">{{ item.id }}</div>
            </td>
            <td>
              <div class="user-main">
                <div class="avatar-sm">{{ item.username.charAt(0).toUpperCase() }}</div>
                <div class="user-detail">
                  <div class="username">{{ item.username }}</div>
                </div>
              </div>
            </td>
            <td>
              <span class="role-badge" :class="'role-' + item.role">{{ getRoleName(item.role) }}</span>
            </td>
            <td>
              <div class="phone-text">{{ item.phoneNumber || '—' }}</div>
            </td>
            <td>
              <div class="date-text">{{ item.createTime ? new Date(item.createTime).toLocaleDateString() : '未知' }}</div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 添加用户抽屉 -->
    <div class="drawer-overlay" :class="{ active: showAddDrawer }" @click="showAddDrawer = false">
      <div class="drawer-content" :class="{ active: showAddDrawer }" @click.stop>
        <div class="drawer-header">
          <h3>新增系统用户</h3>
          <button class="close-btn" @click="showAddDrawer = false">×</button>
        </div>
        
        <div class="drawer-body">
          <div class="form-container">
            <div class="form-group">
              <label class="required">学号/工号 (ID)</label>
              <input type="text" v-model="newUser.id" class="form-input" placeholder="请输入唯一身份ID" />
            </div>

            <div class="form-group">
              <label class="required">系统用户名</label>
              <input type="text" v-model="newUser.username" class="form-input" placeholder="建议使用真实姓名或昵称" />
            </div>

            <div class="form-group">
              <label class="required">初始登录密码</label>
              <input type="password" v-model="newUser.password" class="form-input" placeholder="请设置安全系数高的密码" />
            </div>

            <div class="form-group">
              <label>系统赋予角色</label>
              <select v-model="newUser.role" class="form-select">
                <option v-for="r in userRoles" :key="r.value" :value="r.value">{{ r.label }}</option>
              </select>
            </div>

            <div class="form-group">
              <label>手机联系方式</label>
              <input type="text" v-model="newUser.phoneNumber" class="form-input" placeholder="请输入 11 位手机号" />
            </div>
          </div>
        </div>

        <div class="drawer-footer">
          <button class="submit-btn" :disabled="submitting" @click="submitAddUser">
            {{ submitting ? '正在云端入库...' : '确认创建用户' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.user-manager {
  animation: fadeIn 0.5s ease-out;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
}

.header-left h2 {
  font-size: 1.8rem;
  font-weight: 800;
  color: #1e293b;
  margin-bottom: 0.5rem;
  background: linear-gradient(90deg, #4f46e5, #818cf8);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.header-left p {
  color: #64748b;
  font-size: 0.95rem;
}

.header-right {
  display: flex;
  gap: 1rem;
}

.primary-btn {
  background: #4f46e5;
  color: white;
  border: none;
  padding: 0.75rem 1.5rem;
  border-radius: 0.75rem;
  font-weight: 700;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  cursor: pointer;
  box-shadow: 0 4px 10px rgba(79, 70, 229, 0.2);
  transition: all 0.2s;
}

.primary-btn:hover:not(:disabled) {
  background: #4338ca;
  transform: translateY(-2px);
}

.primary-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
  transform: none;
}

.secondary-btn {
  background: white;
  color: #4f46e5;
  border: 1px solid #e2e8f0;
  padding: 0.75rem 1.5rem;
  border-radius: 0.75rem;
  font-weight: 700;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  cursor: pointer;
  transition: all 0.2s;
}

.secondary-btn:hover {
  background: #f8fafc;
  border-color: #cbd5e1;
}


.btn-spinner {
  width: 14px;
  height: 14px;
  border: 2px solid rgba(255, 255, 255, 0.4);
  border-top: 2px solid white;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

/* 表格设计 */
.modern-table {
  width: 100%;
  border-collapse: separate;
  border-spacing: 0;
  background: white;
  border-radius: 1rem;
  overflow: hidden;
  border: 1px solid #f1f5f9;
}

.modern-table th {
  background: #f8fafc;
  padding: 1rem 1.5rem;
  text-align: left;
  font-size: 0.85rem;
  color: #64748b;
  font-weight: 800;
  border-bottom: 1px solid #e2e8f0;
}

.modern-table td {
  padding: 1.25rem 1.5rem;
  border-bottom: 1px solid #f1f5f9;
  vertical-align: middle;
}

.id-tag {
  font-family: 'JetBrains Mono', monospace;
  font-weight: 700;
  color: #64748b;
}

.user-main {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.avatar-sm {
  width: 36px;
  height: 36px;
  background: #eef2ff;
  color: #4f46e5;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 800;
  font-size: 0.9rem;
}

.username {
  font-weight: 700;
  color: #1e293b;
}

.role-badge {
  padding: 0.25rem 0.75rem;
  border-radius: 2rem;
  font-size: 0.75rem;
  font-weight: 800;
}

.role-0 { background: #dcfce7; color: #166534; } /* 学生 - 绿色 */
.role-1 { background: #dbeafe; color: #1e40af; } /* 教师 - 蓝色 */
.role-2 { background: #fef2f2; color: #991b1b; } /* 管理员 - 红色 */

.phone-text, .date-text {
  color: #64748b;
  font-size: 0.9rem;
}

/* 抽屉 & 表单 */
.drawer-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  backdrop-filter: blur(2px);
  z-index: 2000;
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
  right: -450px;
  width: 400px;
  height: 100%;
  background: white;
  display: flex;
  flex-direction: column;
  transition: right 0.3s cubic-bezier(0.16, 1, 0.3, 1);
  box-shadow: -10px 0 30px rgba(0,0,0,0.1);
}

.drawer-overlay.active .drawer-content {
  right: 0;
}

.drawer-header {
  padding: 1.5rem;
  border-bottom: 1px solid #f1f5f9;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.drawer-header h3 {
  font-weight: 800;
  color: #1e293b;
}

.close-btn {
  border: none;
  background: none;
  font-size: 1.5rem;
  color: #94a3b8;
  cursor: pointer;
}

.drawer-body {
  padding: 1.5rem;
  overflow-y: auto;
}

.form-group {
  margin-bottom: 1.5rem;
}

.form-group label {
  display: block;
  font-size: 0.85rem;
  font-weight: 700;
  color: #475569;
  margin-bottom: 0.5rem;
}

.required::after { content: "*"; color: #ef4444; margin-left: 4px; }

.form-input, .form-select {
  width: 100%;
  padding: 0.8rem;
  border: 1px solid #e2e8f0;
  border-radius: 0.5rem;
  outline: none;
  font-size: 0.9rem;
}

.form-input:focus { border-color: #4f46e5; }

.drawer-footer {
  padding: 1.5rem;
  border-top: 1px solid #f1f5f9;
}

.submit-btn {
  width: 100%;
  background: #4f46e5;
  color: white;
  border: none;
  padding: 1rem;
  border-radius: 0.75rem;
  font-weight: 800;
  cursor: pointer;
}

.status-state {
  text-align: center;
  padding: 4rem;
  color: #94a3b8;
}

.spinner {
  width: 30px;
  height: 30px;
  border: 3px solid #f3f4f6;
  border-top: 3px solid #4f46e5;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 1rem;
}

@keyframes spin { 100% { transform: rotate(360deg); } }
@keyframes fadeIn { from { opacity: 0; transform: translateY(10px); } to { opacity: 1; transform: translateY(0); } }
</style>
