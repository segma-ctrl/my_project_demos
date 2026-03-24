<script setup>
</script>

<template>
  <div class="system-settings">
    <div class="page-header">
      <h2>系统设置</h2>
      <p>管理员专用：配置系统核心参数与权限分配。</p>
    </div>
    
    <div class="settings-placeholder">
      <div class="empty-state">
        <div class="icon">🔧</div>
        <p>系统设置模块正在建设中...</p>
        <p class="hint">（仅管理员可见此模块，权限校验已生效）</p>
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

.empty-state {
  text-align: center;
  padding: 5rem 0;
  color: #9ca3af;
  border: 2px dashed #e5e7eb;
  border-radius: 1rem;
}

.empty-state .icon {
  font-size: 3rem;
  margin-bottom: 1rem;
}

.hint {
  font-size: 0.875rem;
  margin-top: 0.5rem;
}
</style>
