<script setup lang="ts">
import { onMounted, computed } from 'vue'
import { useProgressStore } from '@/stores/progress'
import ProgressRing from '@/components/ProgressRing.vue'
import RollingNumber from '@/components/RollingNumber.vue'
import CipherSkeleton from '@/components/CipherSkeleton.vue'

const store = useProgressStore()

onMounted(() => store.fetchOverview())

const pct = computed(() => {
  const o = store.overview
  if (!o) return 0
  return Math.round((o.clearedCount / Math.max(1, o.totalLevels)) * 100)
})
</script>

<template>
  <div>
    <h2>学习进度</h2>

    <CipherSkeleton v-if="store.loading" :rows="5" />

    <template v-else-if="store.overview">
      <div class="card">
        <div class="row wrap">
          <ProgressRing :value="pct" label="总进度" />
          <div style="flex: 1; min-width: 0">
            <strong>总进度</strong>
            <p class="muted" style="margin: 6px 0 0">
              已完成 <RollingNumber :value="store.overview.clearedCount" /> /
              {{ store.overview.totalLevels }} 关
            </p>
            <p class="muted" style="margin: 2px 0 0">完成度 {{ pct }}%</p>
          </div>
        </div>
      </div>

      <div class="card">
        <h3>各关卡状态</h3>
        <div v-for="lv in store.overview.levels" :key="lv.id" class="status-row">
          <span>{{ lv.title }}</span>
          <span class="spacer" />
          <span v-if="lv.cleared" class="badge badge-success">已通关</span>
          <span v-else-if="lv.unlocked" class="badge badge-info">可挑战</span>
          <span v-else class="badge badge-muted">未解锁</span>
        </div>
      </div>
    </template>

    <p v-else class="empty">暂无进度数据</p>
  </div>
</template>

<style scoped>
.status-row {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 0;
  border-bottom: 1px solid var(--border);
}

.status-row:last-child {
  border-bottom: none;
}
</style>