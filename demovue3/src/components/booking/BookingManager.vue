<script setup>
defineProps({
  user: {
    type: Object,
    required: true
  }
})
</script>

<template>
  <div class="booking-manager">
    <div class="page-header">
      <h2>预约管理</h2>
      <p v-if="user.role === 0">在这里查看您的申请历史或发起新的预约。</p>
      <p v-else-if="user.role === 1">在这里审批学生的预约申请。</p>
      <p v-else>资源管理员后台，支持对预约记录的全量巡检。</p>
    </div>
    
    <div class="placeholder-content">
      <div class="empty-state">
        <div class="icon">🚀</div>
        <p>预约列表与申请功能正在开发中...</p>
        <p class="hint">（当前正在进行组件化重构，敬请期待）</p>
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
