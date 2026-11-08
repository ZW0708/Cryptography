<script setup lang="ts">
import { onMounted } from 'vue'
import { useProgressStore } from '@/stores/progress'
import { useRouter } from 'vue-router'
import CipherSkeleton from '@/components/CipherSkeleton.vue'
import TiltCard from '@/components/TiltCard.vue'

const store = useProgressStore()
const router = useRouter()

onMounted(() => store.fetchOverview())

function pct() {
  const o = store.overview
  if (!o) return 0
  return Math.round((o.clearedCount / Math.max(1, o.totalLevels)) * 100)
}
</script>

<template>
  <div>
    <div class="page-head">
      <div>
        <h2 style="margin-bottom: 4px">关卡列表</h2>
        <p v-if="store.overview" class="muted" style="margin: 0">
          已完成 {{ store.overview.clearedCount }} / {{ store.overview.totalLevels }} 关
        </p>
      </div>
      <button class="secondary" @click="router.push('/knowledge')">📚 知识库</button>
    </div>

    <div v-if="store.overview" class="card">
      <div class="progress"><span :style="{ width: pct() + '%' }" /></div>
      <p class="muted" style="margin: 8px 0 0">完成度 {{ pct() }}%</p>
    </div>

    <CipherSkeleton v-if="store.loading" :rows="5" />

    <div v-else class="level-list">
      <TiltCard v-for="lv in store.overview?.levels" :key="lv.id">
        <div class="card hover level-card">
          <div class="row">
            <h3 style="margin: 0">{{ lv.title }}</h3>
            <span class="stars" :title="'难度 ' + lv.difficulty">{{ '★'.repeat(lv.difficulty) }}</span>
            <span class="spacer" />
            <span v-if="lv.cleared" class="badge badge-success">✓ 已通关</span>
            <span v-else-if="lv.unlocked" class="badge badge-info">可挑战</span>
            <span v-else class="badge badge-muted">🔒 未解锁</span>
          </div>
          <p class="muted" style="margin: 8px 0 4px">{{ lv.cryptoConcept }}</p>
          <p style="margin: 0 0 14px">{{ lv.introText }}</p>
          <button :disabled="!lv.unlocked" @click="router.push(`/levels/${lv.id}`)">
            {{ lv.cleared ? '再次挑战' : '进入关卡' }}
          </button>
        </div>
      </TiltCard>
    </div>
  </div>
</template>

<style scoped>
.page-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 16px;
}

.stars {
  color: var(--warning);
  font-size: 14px;
  letter-spacing: 1px;
}

.level-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.level-card {
  margin-bottom: 0;
}

@media (max-width: 768px) {
  .page-head {
    flex-direction: column;
  }
}
</style>