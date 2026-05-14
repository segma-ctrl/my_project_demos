<script setup>
import { ref, onMounted, computed } from 'vue'
import { apiUrl } from '../../config/api.js'
import { message, Modal } from 'ant-design-vue'
import { 
  UserAddOutlined, 
  CloudUploadOutlined, 
  DownloadOutlined, 
  SearchOutlined, 
  DeleteOutlined,
  ExclamationCircleOutlined,
  InboxOutlined
} from '@ant-design/icons-vue'

// --- 数据定义 ---
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
  { label: '学生', value: 0, color: 'success' },
  { label: '教师', value: 1, color: 'processing' },
  { label: '管理员', value: 2, color: 'error' }
]

// 表格列定义
const columns = [
  { title: '学号/工号', dataIndex: 'id', key: 'id', width: 150 },
  { title: '用户名', dataIndex: 'username', key: 'username' },
  { title: '系统权限', dataIndex: 'role', key: 'role', slotName: 'role' },
  { title: '联系电话', dataIndex: 'phoneNumber', key: 'phoneNumber' },
  { title: '创建日期', dataIndex: 'createTime', key: 'createTime' },
  { title: '操作', key: 'action', width: 120, align: 'center' }
]

// --- 搜索与筛选 ---
const searchQuery = ref('')
const selectedRoles = ref([0, 1, 2])

const filteredUserList = computed(() => {
  return userList.value.filter(user => {
    const matchesSearch = !searchQuery.value || 
      user.id.toString().toLowerCase().includes(searchQuery.value.toLowerCase()) ||
      user.username.toLowerCase().includes(searchQuery.value.toLowerCase())
    const matchesRole = selectedRoles.value.includes(parseInt(user.role))
    return matchesSearch && matchesRole
  })
})

// --- API 调用 ---
const fetchUsers = async () => {
  loading.value = true
  try {
    const res = await fetch(apiUrl('/api/admin/user/list'))
    const result = await res.json()
    if (result.code === 200) {
      userList.value = result.data
    } else {
      message.error(result.msg || '数据同步失败')
    }
  } catch (e) {
    message.error('无法连接到后端服务')
  } finally {
    loading.value = false
  }
}

const submitAddUser = async () => {
  submitting.value = true
  try {
    const res = await fetch(apiUrl('/api/admin/user/add'), {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(newUser.value)
    })
    const result = await res.json()
    if (result.code === 200) {
      message.success('用户创建成功')
      showAddDrawer.value = false
      fetchUsers()
    } else {
      message.error(result.msg || '创建失败')
    }
  } catch (e) {
    message.error('网络错误，请稍后重试')
  } finally {
    submitting.value = false
  }
}

const deleteUser = (id) => {
  Modal.confirm({
    title: '确认删除用户?',
    icon: createVNode(ExclamationCircleOutlined),
    content: `用户 ID: ${id} 将被永久移除，此操作不可撤销。`,
    okText: '确认删除',
    okType: 'danger',
    cancelText: '取消',
    async onOk() {
      try {
        const res = await fetch(apiUrl(`/api/admin/user/delete/${id}`), { method: 'POST' })
        const result = await res.json()
        if (result.code === 200) {
          message.success('用户已成功删除')
          fetchUsers()
        } else {
          message.error(result.msg || '删除失败')
        }
      } catch (e) {
        message.error('删除请求由于网络原因未成功')
      }
    }
  })
}

// --- 批量导入逻辑 ---
const showImportModal = ref(false)
const importing = ref(false)
const selectedFile = ref(null)

const downloadTemplate = () => {
  window.open(apiUrl('/api/admin/user/downloadTemplate'), '_blank')
}

const handleFileUpload = async () => {
  if (!selectedFile.value) return
  const formData = new FormData()
  formData.append('file', selectedFile.value)
  importing.value = true
  try {
    const res = await fetch(apiUrl('/api/admin/user/import'), { method: 'POST', body: formData })
    const result = await res.json()
    if (result.code === 200) {
      message.success('批量数据导入成功')
      showImportModal.value = false
      selectedFile.value = null
      fetchUsers()
    } else {
      message.error(result.msg || '导入解析失败')
    }
  } catch (e) {
    message.error('文件上传过程中发生网络错误')
  } finally {
    importing.value = false
  }
}

const beforeUpload = (file) => {
  const isExcel = file.type === 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' || file.name.endsWith('.xlsx') || file.name.endsWith('.xls')
  if (!isExcel) {
    message.error('只能上传 Excel 文件 (.xlsx 或 .xls)')
    return false
  }
  selectedFile.value = file
  return false // 阻止辅助上传逻辑，由我们手动处理
}

onMounted(fetchUsers)

// 辅助渲染逻辑
import { createVNode } from 'vue'
const getRoleLabel = (val) => userRoles.find(r => r.value === parseInt(val))
</script>

<template>
  <div class="user-management-page">
    <!-- 头部区域 -->
    <div class="page-header">
      <div class="title-section">
        <h1>用户权限管理</h1>
        <p>配置系统核心用户档案，统一管理学生与教职工的访问权限</p>
      </div>
      <div class="action-section">
        <a-space>
          <a-button type="primary" size="large" @click="showAddDrawer = true">
            <template #icon><UserAddOutlined /></template>
            新增用户
          </a-button>
          <a-button size="large" @click="showImportModal = true">
            <template #icon><CloudUploadOutlined /></template>
            批量导入
          </a-button>
        </a-space>
      </div>
    </div>

    <!-- 过滤器区域 -->
    <a-card class="filter-card" :body-style="{ padding: '20px' }">
      <a-row :gutter="24">
        <a-col :span="16">
          <a-input
            v-model:value="searchQuery"
            placeholder="输入用户 ID 或姓名进行自动检索..."
            size="large"
            allow-clear
            class="premium-search"
          >
            <template #prefix><SearchOutlined style="color: #4f46e5" /></template>
          </a-input>
        </a-col>
        <a-col :span="8">
          <div class="filter-group">
            <a-select
              v-model:value="selectedRoles"
              mode="multiple"
              placeholder="按权限过滤"
              style="width: 100%"
              size="large"
              allow-clear
              class="premium-select"
            >
              <a-select-option v-for="r in userRoles" :key="r.value" :value="r.value">
                {{ r.label }}
              </a-select-option>
            </a-select>
          </div>
        </a-col>
      </a-row>
    </a-card>

    <!-- 表格区域 -->
    <a-table 
      :columns="columns" 
      :data-source="filteredUserList" 
      :loading="loading"
      row-key="id"
      class="user-table"
      :pagination="{ pageSize: 10, showTotal: (total) => `共 ${total} 位用户` }"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'role'">
          <a-tag :color="getRoleLabel(record.role).color" style="border-radius: 6px; font-weight: 600;">
            {{ getRoleLabel(record.role).label }}
          </a-tag>
        </template>
        <template v-else-if="column.key === 'createTime'">
          {{ record.createTime ? new Date(record.createTime).toLocaleDateString() : '—' }}
        </template>
        <template v-else-if="column.key === 'id'">
          <span class="id-text">{{ record.id }}</span>
        </template>
        <template v-else-if="column.key === 'action'">
          <a-tooltip title="删除用户">
            <a-button type="text" danger shape="circle" @click="deleteUser(record.id)">
              <template #icon><DeleteOutlined /></template>
            </a-button>
          </a-tooltip>
        </template>
      </template>
    </a-table>

    <!-- 新增用户抽屉 -->
    <a-drawer
      title="录入新用户信息"
      :width="480"
      v-model:visible="showAddDrawer"
      :footer-style="{ textAlign: 'right' }"
      @close="showAddDrawer = false"
    >
      <a-form layout="vertical">
        <a-form-item label="学号/工号 (唯一标识)" required>
          <a-input v-model:value="newUser.id" placeholder="请输入 ID" size="large" />
        </a-form-item>
        <a-form-item label="系统用户名" required>
          <a-input v-model:value="newUser.username" placeholder="请输入姓名" size="large" />
        </a-form-item>
        <a-form-item label="初始登录密码" required>
          <a-input-password v-model:value="newUser.password" placeholder="建议包含字母和数字" size="large" />
        </a-form-item>
        <a-form-item label="赋予权限角色">
          <a-radio-group v-model:value="newUser.role" button-style="solid">
            <a-radio-button v-for="r in userRoles" :key="r.value" :value="r.value">
              {{ r.label }}
            </a-radio-button>
          </a-radio-group>
        </a-form-item>
        <a-form-item label="联系电话">
          <a-input v-model:value="newUser.phoneNumber" placeholder="请输入手机号" size="large" />
        </a-form-item>
      </a-form>
      <template #footer>
        <a-space>
          <a-button @click="showAddDrawer = false">取消</a-button>
          <a-button type="primary" :loading="submitting" @click="submitAddUser">确认提交库房</a-button>
        </a-space>
      </template>
    </a-drawer>

    <!-- 批量导入弹窗 -->
    <a-modal
      v-model:visible="showImportModal"
      title="批量导入档案"
      @ok="handleFileUpload"
      :confirm-loading="importing"
      ok-text="确认开始上传"
      cancel-text="取消"
      width="600px"
    >
      <div class="import-modal-body">
        <a-alert
          message="导入须知"
          description="请务必使用系统提供的 Excel 模板进行填写，否则解析逻辑可能会失效。"
          type="info"
          show-icon
          style="margin-bottom: 20px"
        />
        <div class="template-download">
          <a-button type="dashed" block @click="downloadTemplate">
            <template #icon><DownloadOutlined /></template>
            下载官方导入模板.xlsx
          </a-button>
        </div>
        <div class="upload-zone">
          <a-upload-dragger
            name="file"
            :multiple="false"
            :before-upload="beforeUpload"
            :show-upload-list="true"
            @remove="selectedFile = null"
          >
            <p class="ant-upload-drag-icon">
              <InboxOutlined />
            </p>
            <p class="ant-upload-text">点击或将文件拖拽到此处上传</p>
            <p class="ant-upload-hint">支持 .xlsx 或 .xls 格式</p>
          </a-upload-dragger>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<style scoped>
.user-management-page {
  padding: 32px;
  background: transparent;
  min-height: calc(100vh - 100px);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 32px;
}

.title-section h1 {
  font-size: 26px;
  font-weight: 800;
  color: #1e293b;
  margin-bottom: 4px;
}

.title-section p {
  color: #64748b;
  font-size: 15px;
}

.filter-card {
  margin-bottom: 32px;
  border-radius: 16px;
  border: none;
  background: white;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
}

.premium-search :deep(.ant-input) {
  border-radius: 12px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
}

.premium-search :deep(.ant-input:focus) {
  background: white;
  border-color: #4f46e5;
  box-shadow: 0 0 0 4px rgba(79, 70, 229, 0.1);
}

.premium-select :deep(.ant-select-selector) {
  border-radius: 12px !important;
  background: #f8fafc !important;
  border: 1px solid #e2e8f0 !important;
}

.premium-select:hover :deep(.ant-select-selector) {
  border-color: #4f46e5 !important;
}

.premium-select :deep(.ant-select-selection-item) {
  background: #eef2ff !important;
  color: #4f46e5 !important;
  font-weight: 700;
  border: 1px solid #c7d2fe !important;
  border-radius: 6px;
}

.user-table {
  background: white;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.04);
}

:deep(.ant-table-thead > tr > th) {
  background: #f8fafc;
  font-weight: 700;
  color: #475569;
  padding: 16px;
}

:deep(.ant-table-tbody > tr > td) {
  padding: 16px;
}

:deep(.ant-table-row:hover td) {
  background-color: #f5f7ff !important;
}

.id-text {
  font-family: 'JetBrains Mono', monospace;
  font-weight: 800;
  color: #4f46e5;
  background: #eef2ff;
  padding: 4px 8px;
  border-radius: 6px;
  font-size: 13px;
}

/* Button & Action styling */
:deep(.ant-btn-primary) {
  background: #4f46e5;
  border: none;
  font-weight: 700;
  height: 48px;
  padding: 0 24px;
  border-radius: 12px;
}

:deep(.ant-btn-primary:hover) {
  background: #4338ca;
  box-shadow: 0 4px 12px rgba(79, 70, 229, 0.3);
}

:deep(.ant-radio-button-wrapper-checked) {
  background: #4f46e5 !important;
  border-color: #4f46e5 !important;
  color: white !important;
}
</style>
