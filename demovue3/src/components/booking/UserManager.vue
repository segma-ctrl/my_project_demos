<script setup>
import { ref, onMounted, computed } from 'vue'

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

const searchQuery = ref('')
const selectedRoles = ref([0, 1, 2]) // 默认全选
const showRoleDropdown = ref(false)
const showImportModal = ref(false)
const isDragging = ref(false)
const selectedFile = ref(null)

// 切换角色选择
const toggleRole = (roleValue) => {
  const index = selectedRoles.value.indexOf(roleValue)
  if (index > -1) {
    if (selectedRoles.value.length > 1) {
      selectedRoles.value.splice(index, 1)
    }
  } else {
    selectedRoles.value.push(roleValue)
  }
}

const isAllSelected = computed(() => selectedRoles.value.length === 3)

const toggleAllRoles = () => {
  if (isAllSelected.value) {
    selectedRoles.value = [0] // 默认保留一项
  } else {
    selectedRoles.value = [0, 1, 2]
  }
}

// 模糊搜索 + 角色多选筛选
const filteredUserList = computed(() => {
  let list = userList.value
  
  // 角色筛选 (多选)
  list = list.filter(user => selectedRoles.value.includes(parseInt(user.role)))
  
  // 关键词搜索
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    list = list.filter(user => 
      user.id.toString().toLowerCase().includes(query) || 
      user.username.toLowerCase().includes(query)
    )
  }
  
  return list
})

const handleFileUpload = async () => {
  if (!selectedFile.value) return
  
  const file = selectedFile.value
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
        closeImportModal() // 成功后关闭
        fetchUsers()
      } else {
        alert(result.msg || '导入失败，请检查文件格式')
      }
    } else {
      alert('批量导入请求失败')
    }
  } catch (e) {
    alert('发生网络错误')
  } finally {
    importing.value = false
  }
}

const closeImportModal = () => {
  showImportModal.value = false
  selectedFile.value = null
  importing.value = false
}

// 选择文件逻辑 (不立即上传)
const onFileSelected = (file) => {
  if (!file) return
  const fileName = file.name.toLowerCase()
  if (!fileName.endsWith('.xlsx') && !fileName.endsWith('.xls')) {
    alert('请上传有效的 Excel 文件 (.xlsx 或 .xls)')
    return
  }
  selectedFile.value = file
}

// 拖拽处理
const handleDrop = (e) => {
  isDragging.value = false
  const file = e.dataTransfer.files[0]
  onFileSelected(file)
}

const onFileChange = (e) => {
  const file = e.target.files[0]
  onFileSelected(file)
  e.target.value = ''
}

const deleteUser = async (id) => {
  if (!confirm(`确定要永久删除用户 (ID: ${id}) 吗？`)) return
  
  try {
    const res = await fetch(`http://localhost:8080/api/admin/user/delete/${id}`, {
      method: 'POST'
    })
    
    if (res.ok) {
      const result = await res.json()
      if (result.code === 200) {
        alert('删除成功')
        fetchUsers()
      } else {
        alert(result.msg || '删除失败')
      }
    } else {
      alert('请求失败：后端端点不可达')
    }
  } catch (e) {
    alert('网络错误，无法连接到删除接口')
    console.error(e)
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
        <button class="primary-btn" @click="openAddDrawer">
           <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="5" x2="12" y2="19"></line><line x1="5" y1="12" x2="19" y2="12"></line></svg>
           新增单个用户
        </button>
        <button class="primary-btn import-trigger-btn" @click="showImportModal = true">
           <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v4"></path><polyline points="17 8 12 3 7 8"></polyline><line x1="12" y1="3" x2="12" y2="15"></line></svg>
           批量导入入口
        </button>
      </div>
    </div>

    <!-- 搜索与筛选工具栏 -->
    <div class="search-row">
      <div class="search-container">
        <svg class="search-icon" viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"><circle cx="11" cy="11" r="8"></circle><line x1="21" y1="21" x2="16.65" y2="16.65"></line></svg>
        <input type="text" v-model="searchQuery" placeholder="搜索用户 ID 或名称..." class="search-input" />
      </div>
      
      <div class="filter-container">
        <div class="dropdown-wrapper">
          <button class="dropdown-btn" @click="showRoleDropdown = !showRoleDropdown">
            <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><polygon points="22 3 2 3 10 12.46 10 19 14 21 14 12.46 22 3"></polygon></svg>
            <span>身份筛选 (已选 {{ selectedRoles.length }})</span>
            <svg class="chevron" :class="{ open: showRoleDropdown }" viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"><polyline points="6 9 12 15 18 9"></polyline></svg>
          </button>
          
          <div class="dropdown-menu" :class="{ active: showRoleDropdown }" @click.stop>
            <div class="dropdown-item all-option" @click="toggleAllRoles">
              <div class="checkbox" :class="{ checked: isAllSelected }"></div>
              <span>全选 / 反选</span>
            </div>
            <div class="divider"></div>
            <div 
              v-for="role in userRoles" 
              :key="role.value" 
              class="dropdown-item" 
              @click="toggleRole(role.value)"
            >
              <div class="checkbox" :class="{ checked: selectedRoles.includes(role.value) }"></div>
              <span>{{ role.label }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 列表展示 -->
    <div v-if="loading" class="status-state">
      <div class="spinner"></div>
      <p>正在同步全库用户索引...</p>
    </div>

    <div v-else-if="filteredUserList.length === 0" class="empty-state">
      <div class="icon">🔍</div>
      <p>{{ searchQuery ? '未找到符合条件的搜索结果' : '暂无用户信息' }}</p>
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
            <th class="action-col">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in filteredUserList" :key="item.id">
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
            <td>
              <button class="delete-btn" @click="deleteUser(item.id)">
                <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><polyline points="3 6 5 6 21 6"></polyline><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path><line x1="10" y1="11" x2="10" y2="17"></line><line x1="14" y1="11" x2="14" y2="17"></line></svg>
                删除
              </button>
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

    <!-- 批量导入弹窗 -->
    <div class="modal-overlay" :class="{ active: showImportModal }" @click="closeImportModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>批量导入用户数据</h3>
          <button class="close-btn" @click="closeImportModal">×</button>
        </div>
        
        <div class="modal-body">
          <div class="modal-info">
            <p>请下载标准的 Excel 模板，按要求填写后上传。系统将自动解析 ID、姓名及角色信息。</p>
            <button class="template-link-btn" @click="downloadTemplate">
              <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v4"></path><polyline points="7 10 12 15 17 10"></polyline><line x1="12" y1="15" x2="12" y2="3"></line></svg>
              获取下载标准模板.xlsx
            </button>
          </div>

          <div 
            class="upload-dropzone" 
            :class="{ dragging: isDragging, uploading: importing, 'has-file': !!selectedFile }"
            @dragover.prevent="isDragging = true"
            @dragleave.prevent="isDragging = false"
            @drop.prevent="handleDrop"
            @click="!selectedFile && triggerFileUpload()"
          >
            <input type="file" ref="fileInput" style="display: none" accept=".xlsx, .xls" @change="onFileChange">
            
            <div class="dropzone-content" v-if="!importing && !selectedFile">
              <div class="drop-icon">📂</div>
              <p class="main-text">点击或将模板文件拖拽到此处</p>
              <p class="sub-text">支持扩展名：.xlsx, .xls</p>
            </div>
            
            <div class="file-preview" v-else-if="!importing && selectedFile">
               <div class="file-icon">📊</div>
               <div class="file-info">
                 <p class="file-name">{{ selectedFile.name }}</p>
                 <p class="file-size">{{ (selectedFile.size / 1024).toFixed(1) }} KB</p>
               </div>
               <button class="change-file-btn" @click.stop="triggerFileUpload">更换文件</button>
            </div>
            
            <div class="dropzone-status" v-else>
              <div class="spinner-large"></div>
              <p>正在分析数据并实时同步到云库...</p>
            </div>
          </div>
        </div>
        
        <div class="modal-footer">
          <button class="cancel-btn" @click="closeImportModal">取消</button>
          <button 
            class="primary-btn confirm-upload-btn" 
            :disabled="!selectedFile || importing"
            @click="handleFileUpload"
          >
            {{ importing ? '正在导入...' : '确认开始上传' }}
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
  justify-content: flex-end;
  align-items: center;
  gap: 1rem;
}

.search-row {
  display: flex;
  align-items: center;
  gap: 1.5rem;
  margin-bottom: 2rem;
  background: #f8fafc;
  padding: 1.5rem;
  border-radius: 1rem;
  border: 1px solid #f1f5f9;
}

.search-container {
  display: flex;
  align-items: center;
  border: 1.5px solid #e2e8f0;
  border-radius: 0.75rem;
  padding: 0 1.25rem;
  flex: 1;
  transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
  background: white;
}

.filter-container {
  display: flex;
  align-items: center;
}

.dropdown-wrapper {
  position: relative;
}

.dropdown-btn {
  background: white;
  border: 1.5px solid #e2e8f0;
  padding: 0.6rem 1rem;
  border-radius: 0.75rem;
  font-size: 0.85rem;
  font-weight: 700;
  color: #1e293b;
  display: flex;
  align-items: center;
  gap: 0.6rem;
  cursor: pointer;
  transition: all 0.2s;
  min-width: 160px;
}

.dropdown-btn:hover {
  border-color: #4f46e5;
  background: #f5f3ff;
}

.dropdown-btn .chevron {
  margin-left: auto;
  transition: transform 0.3s;
}

.dropdown-btn .chevron.open {
  transform: rotate(180deg);
}

.dropdown-menu {
  position: absolute;
  top: calc(100% + 8px);
  left: 0;
  width: 180px;
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 0.75rem;
  box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.1);
  padding: 0.5rem;
  z-index: 1000;
  opacity: 0;
  visibility: hidden;
  transform: translateY(10px);
  transition: all 0.2s cubic-bezier(0.16, 1, 0.3, 1);
}

.dropdown-menu.active {
  opacity: 1;
  visibility: visible;
  transform: translateY(0);
}

.dropdown-item {
  padding: 0.6rem 0.8rem;
  border-radius: 0.5rem;
  display: flex;
  align-items: center;
  gap: 0.75rem;
  cursor: pointer;
  font-size: 0.85rem;
  color: #475569;
  transition: all 0.2s;
}

.dropdown-item:hover {
  background: #f8fafc;
  color: #4f46e5;
}

.checkbox {
  width: 18px;
  height: 18px;
  border: 2px solid #cbd5e1;
  border-radius: 4px;
  position: relative;
  transition: all 0.2s;
}

.checkbox.checked {
  background: #4f46e5;
  border-color: #4f46e5;
}

.checkbox.checked::after {
  content: "";
  position: absolute;
  left: 5px;
  top: 1px;
  width: 4px;
  height: 8px;
  border: solid white;
  border-width: 0 2px 2px 0;
  transform: rotate(45deg);
}

.divider {
  height: 1px;
  background: #f1f5f9;
  margin: 0.4rem 0.2rem;
}

.all-option {
  font-weight: 800;
  color: #1e293b;
}

.search-container:focus-within {
  border-color: #4f46e5;
  box-shadow: 0 4px 15px rgba(79, 70, 229, 0.1);
  background: white;
}

.search-icon {
  color: #94a3b8;
  margin-right: 0.5rem;
}

.search-input {
  border: none;
  background: none;
  padding: 0.6rem 0;
  font-size: 0.9rem;
  outline: none;
  width: 100%;
  color: #1e293b;
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

.modern-table th.action-col {
  text-align: center;
}

.delete-btn {
  background: #f8fafc;
  color: #94a3b8;
  border: 1px solid #f1f5f9;
  padding: 0.4rem 0.8rem;
  border-radius: 0.5rem;
  font-size: 0.8rem;
  font-weight: 700;
  display: flex;
  align-items: center;
  gap: 0.4rem;
  cursor: pointer;
  transition: all 0.2s;
  margin: 0 auto;
}

.delete-btn:hover {
  background: #fff1f2;
  color: #e11d48;
  border-color: #fecdd3;
  transform: translateY(-2px);
}

/* 弹窗样式 */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.75);
  backdrop-filter: blur(8px);
  z-index: 3000;
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  visibility: hidden;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.modal-overlay.active {
  opacity: 1;
  visibility: visible;
}

.modal-content {
  background: white;
  width: 500px;
  border-radius: 1.25rem;
  overflow: hidden;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
  transform: scale(0.9) translateY(20px);
  transition: all 0.4s cubic-bezier(0.16, 1, 0.3, 1);
}

.modal-overlay.active .modal-content {
  transform: scale(1) translateY(0);
}

.modal-header {
  padding: 1.5rem;
  background: #f8fafc;
  border-bottom: 1px solid #f1f5f9;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.modal-header h3 {
  font-weight: 800;
  background: linear-gradient(90deg, #4f46e5, #818cf8);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.modal-info {
  padding: 1.5rem;
  background: #eff6ff;
  margin: 1.5rem;
  border-radius: 0.75rem;
  border: 1px solid #dbeafe;
}

.modal-info p {
  font-size: 0.85rem;
  color: #1e40af;
  line-height: 1.6;
  margin-bottom: 1rem;
}

.template-link-btn {
  background: white;
  color: #2563eb;
  border: 1.5px solid #2563eb;
  padding: 0.5rem 1rem;
  border-radius: 0.5rem;
  font-size: 0.85rem;
  font-weight: 700;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  cursor: pointer;
  transition: all 0.2s;
}

.template-link-btn:hover {
  background: #2563eb;
  color: white;
}

.upload-dropzone {
  margin: 0 1.5rem 1.5rem;
  height: 180px;
  border: 2px dashed #e2e8f0;
  border-radius: 1rem;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-direction: column;
  transition: all 0.3s;
  cursor: pointer;
}

.upload-dropzone.dragging {
  border-color: #4f46e5;
  background: #f5f3ff;
  transform: scale(1.02);
}

.upload-dropzone.uploading {
  border-style: solid;
  border-color: #e2e8f0;
  cursor: not-allowed;
}

.upload-dropzone.has-file {
  border-style: solid;
  border-color: #dbeafe;
  background: #f8fafc;
  cursor: default;
}

.file-preview {
  display: flex;
  align-items: center;
  gap: 1.5rem;
  width: 100%;
  padding: 0 2rem;
}

.file-icon {
  font-size: 3rem;
}

.file-info {
  flex: 1;
}

.file-name {
  font-weight: 800;
  color: #1e293b;
  margin-bottom: 0.25rem;
  word-break: break-all;
}

.file-size {
  font-size: 0.8rem;
  color: #94a3b8;
}

.change-file-btn {
  background: white;
  color: #4f46e5;
  border: 1px solid #e2e8f0;
  padding: 0.5rem 1rem;
  border-radius: 0.5rem;
  font-size: 0.8rem;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s;
}

.change-file-btn:hover {
  background: #f1f5f9;
  border-color: #cbd5e1;
}

.drop-icon {
  font-size: 2.5rem;
  margin-bottom: 1rem;
}

.main-text {
  font-weight: 800;
  color: #334155;
  margin-bottom: 0.5rem;
}

.sub-text {
  font-size: 0.8rem;
  color: #94a3b8;
}

.spinner-large {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f4f6;
  border-top: 4px solid #4f46e5;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 1rem;
}

.modal-footer {
  padding: 1.25rem 1.5rem;
  background: #f1f5f9;
  display: flex;
  justify-content: flex-end;
}

.cancel-btn {
  padding: 0.75rem 1.5rem;
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 0.75rem;
  font-weight: 700;
  color: #64748b;
  cursor: pointer;
  margin-right: 1rem;
}

.confirm-upload-btn {
  min-width: 160px;
}

.import-trigger-btn {
  background: linear-gradient(135deg, #4f46e5, #818cf8) !important;
}
</style>
