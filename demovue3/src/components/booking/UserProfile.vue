<script setup>
import { ref } from 'vue'

const props = defineProps({
  user: {
    type: Object,
    required: true
  },
  getRoleName: {
    type: Function,
    required: true
  }
})

const pwdForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const submitting = ref(false)

const handleChangePassword = async () => {
  if (!pwdForm.value.oldPassword || !pwdForm.value.newPassword) {
    alert('请填写完整的新旧密码信息')
    return
  }
  if (pwdForm.value.newPassword !== pwdForm.value.confirmPassword) {
    alert('两次输入的新密码不一致，请仔细核对重新输入！')
    return
  }
  if (pwdForm.value.oldPassword === pwdForm.value.newPassword) {
    alert('新设定的密码不能与旧密码相同！')
    return
  }

  submitting.value = true
  try {
    const payload = {
      username: props.user.username,
      oldPassword: pwdForm.value.oldPassword,
      newPassword: pwdForm.value.newPassword
    }

    const res = await fetch('http://localhost:8080/api/changePassword', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    })

    if (res.ok) {
      const result = await res.json()
      // 判断后端是否操作成功
      if (result.code === 200 || !result.msg || (result.msg && result.msg.indexOf('失败') === -1)) {
        alert('恭喜你，密码修改成功！请使用新密码重新登录。')
        // 修改密码后通常强制登出
        localStorage.removeItem('user')
        window.location.reload() 
      } else {
        alert(result.msg || '修改失败，请检查旧密码是否正确。')
      }
    } else {
      // 捕获后端的请求体绑定异常等500错误
      alert('请求后端失败，你的旧密码可能有误，或者请检查服务器控制台报错。') 
    }
  } catch (e) {
    alert('请求通信失败，请检查网络连接')
    console.error(e)
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="user-profile">
    <div class="page-header">
      <h2>个人中心</h2>
      <p>管理个人基本信息与账号安全防线。</p>
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
        <span class="system-tag">高校教学预约管理平台</span>
      </div>
    </div>
    
    <div class="security-card">
      <div class="section-title">
        <div class="title-icon">🔒</div>
        <h3>账号安全与修改密码</h3>
      </div>
      <p class="section-desc">定期更改强密码可有效保护您在平台资产及资源预约过程中的数据安全。为了确认身份，操作时需提供原密码进行鉴权。</p>

      <div class="form-container">
         <div class="form-group">
            <label class="required">当前原密码</label>
            <input type="password" v-model="pwdForm.oldPassword" class="modern-input" placeholder="请输入您现在正在使用的登录密码" />
         </div>
         <div class="form-group">
            <label class="required">设定新密码</label>
            <input type="password" v-model="pwdForm.newPassword" class="modern-input" placeholder="建议包含字母和数字组合的新验证串" />
         </div>
         <div class="form-group">
            <label class="required">确认新密码</label>
            <input type="password" v-model="pwdForm.confirmPassword" class="modern-input" placeholder="请再次输入刚才设定的新密码以防手误" />
         </div>
         <div class="form-action">
            <button class="update-btn" :disabled="submitting" @click="handleChangePassword">
               {{ submitting ? '后端验证校验中...' : '提交密文并更新' }}
            </button>
         </div>
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
  font-weight: 800;
}

.system-tag {
  color: #10b981 !important;
  font-weight: 700;
}

/* 安全密码模块 */
.security-card {
  padding: 2rem;
  background: #ffffff;
  border-radius: 1.25rem;
  border: 1px solid #e2e8f0;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05);
}

.section-title {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.5rem;
}

.section-title .title-icon {
  font-size: 1.25rem;
  background: #fdf2f8;
  padding: 0.5rem;
  border-radius: 0.75rem;
  color: #d97706;
}

.section-title h3 {
  font-size: 1.25rem;
  font-weight: 800;
  color: #0f172a;
  margin: 0;
}

.section-desc {
  color: #64748b;
  font-size: 0.9rem;
  margin-bottom: 2rem;
  margin-left: 2.75rem;
}

.form-container {
  max-width: 500px;
  margin-left: 2.75rem;
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.form-group label {
  display: block;
  font-size: 0.85rem;
  font-weight: 700;
  color: #475569;
  margin-bottom: 0.5rem;
}

.required::after {
  content: " *";
  color: #ef4444;
}

.modern-input {
  width: 100%;
  padding: 0.85rem 1rem;
  border: 1.5px solid #e2e8f0;
  background: #f8fafc;
  border-radius: 0.75rem;
  outline: none;
  font-size: 0.95rem;
  font-weight: 600;
  color: #1e293b;
  transition: all 0.2s;
  box-sizing: border-box;
}

.modern-input:focus {
  border-color: #4f46e5;
  background: white;
  box-shadow: 0 0 0 3px #e0e7ff;
}

.modern-input::placeholder {
  color: #94a3b8;
  font-weight: 400;
}

.form-action {
  margin-top: 0.5rem;
  padding-top: 1.5rem;
  border-top: 1px dashed #e2e8f0;
}

.update-btn {
  padding: 0.85rem 1.75rem;
  background: #4f46e5;
  color: white;
  border: none;
  border-radius: 0.85rem;
  font-size: 0.95rem;
  font-weight: 800;
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: 0 4px 6px rgba(79, 70, 229, 0.25);
}

.update-btn:hover:not(:disabled) {
  background: #4338ca;
  transform: translateY(-1px);
}

.update-btn:disabled {
  background: #cbd5e1;
  color: #f1f5f9;
  cursor: not-allowed;
  box-shadow: none;
}
</style>
