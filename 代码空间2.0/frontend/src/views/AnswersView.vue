<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { answerApi } from '@/api/answers'
import CipherSkeleton from '@/components/CipherSkeleton.vue'
import type { AnswerView } from '@/types'

const route = useRoute()
const router = useRouter()

const items = ref<AnswerView[]>([])
const selectedCode = ref('')
const loading = ref(false)
const error = ref('')

const selected = computed(() => items.value.find((i) => i.code === selectedCode.value) ?? null)

const progressPct = computed(() => {
  if (!selected.value) return 0
  const { failedAttempts, unlockThreshold } = selected.value
  return Math.min(100, Math.round((failedAttempts / Math.max(1, unlockThreshold)) * 100))
})

function statusText(a: AnswerView) {
  if (a.cleared) return '已通关'
  if (a.unlocked) return '已解锁'
  return '锁定中'
}

function badgeClass(a: AnswerView) {
  if (a.cleared) return 'badge badge-success'
  if (a.unlocked) return 'badge badge-info'
  return 'badge badge-muted'
}

async function load() {
  loading.value = true
  error.value = ''
  try {
    const { data } = await answerApi.list()
    items.value = data.data
    const q = typeof route.query.code === 'string' ? route.query.code : ''
    if (q && items.value.some((i) => i.code === q)) {
      selectedCode.value = q
    } else if (items.value.length) {
      selectedCode.value = items.value[0].code
    }
  } catch (e: any) {
    error.value = e.response?.data?.message ?? '加载答案失败'
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<template>
  <div>
    <div class="page-head">
      <div>
        <h2 style="margin-bottom: 4px">🔑 答案与解析</h2>
        <p class="muted" style="margin: 0">通关本关后解锁复盘材料：解题思路、参考答案与题目解析。</p>
      </div>
      <RouterLink to="/levels"><button class="secondary">返回关卡</button></RouterLink>
    </div>

    <p v-if="error" class="field-error">{{ error }}</p>
    <CipherSkeleton v-if="loading" :rows="4" />

    <div v-else class="ans-layout">
      <aside class="card ans-nav">
        <div
          v-for="a in items"
          :key="a.code"
          class="ans-item"
          :class="{ active: a.code === selectedCode }"
          @click="selectedCode = a.code"
        >
          <strong>{{ a.title }}</strong>
          <div class="ans-meta">
            <span :class="badgeClass(a)">{{ statusText(a) }}</span>
            <span v-if="!a.unlocked" class="muted">通关后解锁</span>
          </div>
        </div>
        <p v-if="!items.length" class="muted">暂无数据</p>
      </aside>

      <section class="card ans-content">
        <template v-if="selected">
          <div class="row">
            <h3 style="margin: 0">{{ selected.title }}</h3>
            <span class="spacer" />
            <span :class="badgeClass(selected)">{{ statusText(selected) }}</span>
          </div>

          <template v-if="selected.unlocked">
            <div class="ans-block">
              <strong>🧭 解题思路</strong>
              <ol>
                <li v-for="(s, i) in selected.solution || []" :key="i">{{ s }}</li>
              </ol>
            </div>

            <div class="ans-block ans-answer">
              <strong>🔑 参考答案</strong>
              <p class="answer-text">{{ selected.answer }}</p>
            </div>

            <div class="ans-block">
              <strong>📖 题目解析</strong>
              <p class="ans-analysis">{{ selected.analysis }}</p>
            </div>
          </template>

          <div v-else class="locked">
            <div class="lock-icon">🔒</div>
            <p style="margin: 6px 0"><strong>答案已锁定</strong></p>
            <p class="muted" style="margin: 0">
              通关本关后自动解锁——先去把这一关解出来吧，复盘时再看会更有收获。
            </p>
            <button style="margin-top: 16px" @click="router.push('/levels')">去闯关</button>
          </div>
        </template>
        <p v-else class="muted">请从左侧选择关卡</p>
      </section>
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

.ans-layout {
  display: flex;
  gap: 20px;
  align-items: flex-start;
}

.ans-nav {
  width: 240px;
  flex-shrink: 0;
}

.ans-item {
  padding: 9px 11px;
  margin-bottom: 6px;
  border: 1px solid var(--border);
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.15s, border-color 0.15s, box-shadow 0.15s;
}

.ans-item:hover {
  background: var(--surface-2);
}

.ans-item.active {
  border-color: var(--primary);
  background: var(--primary-light);
  box-shadow: 0 0 14px rgba(62, 242, 176, 0.15);
}

.ans-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 6px;
  font-size: 12px;
}

.ans-content {
  flex: 1;
  min-width: 0;
}

.ans-block {
  margin-top: 16px;
  background: var(--surface-2);
  border: 1px solid var(--border);
  border-radius: 8px;
  padding: 10px 14px;
}

.ans-block ol {
  margin: 6px 0 0;
  padding-left: 20px;
  line-height: 1.8;
  color: var(--text);
}

.ans-answer {
  border-color: rgba(62, 242, 176, 0.4);
  border-style: dashed;
}

.answer-text {
  margin: 8px 0 0;
  font-family: var(--mono);
  font-size: 16px;
  font-weight: 700;
  color: var(--primary);
  letter-spacing: 0.04em;
  text-shadow: 0 0 14px rgba(62, 242, 176, 0.35);
}

.ans-analysis {
  margin: 6px 0 0;
  line-height: 1.8;
  color: var(--text);
}

.locked {
  text-align: center;
  padding: 16px 8px 8px;
}

.lock-icon {
  font-size: 34px;
}

.field-error {
  color: var(--danger);
  font-size: 13px;
}

@media (max-width: 768px) {
  .page-head {
    flex-direction: column;
  }

  .ans-layout {
    flex-direction: column;
  }

  .ans-nav {
    width: 100%;
  }
}
</style>