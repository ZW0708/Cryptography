<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { knowledgeApi } from '@/api/knowledge'
import CipherSkeleton from '@/components/CipherSkeleton.vue'
import type { KnowledgeItem } from '@/types'

const route = useRoute()
const router = useRouter()

const items = ref<KnowledgeItem[]>([])
const selectedCode = ref('')
const loading = ref(false)
const error = ref('')

const selected = computed(() => items.value.find((i) => i.code === selectedCode.value) ?? null)

async function load() {
  loading.value = true
  error.value = ''
  try {
    const { data } = await knowledgeApi.list()
    items.value = data.data
    const q = typeof route.query.code === 'string' ? route.query.code : ''
    if (q && items.value.some((i) => i.code === q)) {
      selectedCode.value = q
    } else if (items.value.length) {
      selectedCode.value = items.value[0].code
    }
  } catch (e: any) {
    error.value = e.response?.data?.message ?? '加载知识库失败'
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
        <h2 style="margin-bottom: 4px">📚 密码知识库</h2>
        <p class="muted" style="margin: 0">按关卡整理的知识讲解，先读懂概念，再去挑战会更容易。</p>
      </div>
      <RouterLink to="/levels"><button class="secondary">返回关卡</button></RouterLink>
    </div>

    <p v-if="error" class="field-error">{{ error }}</p>
    <CipherSkeleton v-if="loading" :rows="4" />

    <div v-else class="kb-layout">
      <aside class="card kb-nav">
        <div
          v-for="it in items"
          :key="it.code"
          class="kb-item"
          :class="{ active: it.code === selectedCode }"
          @click="selectedCode = it.code"
        >
          <strong>{{ it.title }}</strong>
          <div class="muted kb-concept">{{ it.concept }}</div>
        </div>
        <p v-if="!items.length" class="muted">暂无知识条目</p>
      </aside>

      <section class="card kb-content">
        <template v-if="selected">
          <h3 style="margin-top: 0">{{ selected.title }}</h3>
          <p class="kb-concept-name">{{ selected.concept }}</p>
          <p class="kb-overview">{{ selected.overview }}</p>

          <div v-for="(s, idx) in selected.sections" :key="idx" class="kb-section">
            <h4>{{ s.heading }}</h4>
            <p>{{ s.content }}</p>
          </div>

          <div v-if="selected.tips && selected.tips.length" class="kb-tips">
            <strong style="font-size: 13px">💡 小贴士</strong>
            <ul>
              <li v-for="(t, i) in selected.tips" :key="i">{{ t }}</li>
            </ul>
          </div>

          <div class="kb-answer-hint">
            <div class="row">
              <strong style="font-size: 13px">🔑 想看思路与答案？</strong>
              <span class="spacer" />
              <button class="secondary sm" @click="router.push('/answers?code=' + selected.code)">
                前往答案页
              </button>
            </div>
            <p class="muted" style="margin: 6px 0 0; font-size: 12px">
              答案已移至独立页面：通关本关后解锁，用于复盘思路与答案。
            </p>
          </div>

          <div style="margin-top: 18px">
            <button @click="router.push('/levels')">去闯关</button>
          </div>
        </template>
        <p v-else class="muted">请从左侧选择知识条目</p>
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

.kb-layout {
  display: flex;
  gap: 20px;
  align-items: flex-start;
}

.kb-nav {
  width: 230px;
  flex-shrink: 0;
}

.kb-item {
  padding: 9px 11px;
  margin-bottom: 6px;
  border: 1px solid var(--border);
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.15s, border-color 0.15s, box-shadow 0.15s;
}

.kb-item:hover {
  background: var(--surface-2);
}

.kb-item.active {
  border-color: var(--primary);
  background: var(--primary-light);
  box-shadow: 0 0 14px rgba(62, 242, 176, 0.15);
}

.kb-concept {
  font-size: 12px;
  margin-top: 2px;
}

.kb-content {
  flex: 1;
  min-width: 0;
}

.kb-concept-name {
  color: var(--primary);
  font-size: 13px;
  margin-top: -6px;
}

.kb-overview {
  line-height: 1.8;
}

.kb-section {
  margin-top: 14px;
}

.kb-section h4 {
  margin: 0 0 4px;
}

.kb-section p {
  margin: 0;
  line-height: 1.8;
  color: var(--text);
}

.kb-tips {
  margin-top: 16px;
  background: var(--primary-light);
  border: 1px solid rgba(62, 242, 176, 0.25);
  border-radius: 8px;
  padding: 10px 14px;
}

.kb-tips ul {
  margin: 6px 0 0;
  padding-left: 18px;
  line-height: 1.8;
  color: var(--text);
}

.kb-answer-hint {
  margin-top: 16px;
  border: 1px dashed rgba(62, 242, 176, 0.4);
  border-radius: 8px;
  padding: 10px 14px;
}

.field-error {
  color: var(--danger);
  font-size: 13px;
}

@media (max-width: 768px) {
  .page-head {
    flex-direction: column;
  }

  .kb-layout {
    flex-direction: column;
  }

  .kb-nav {
    width: 100%;
  }
}
</style>