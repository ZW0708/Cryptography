<script setup lang="ts">
import { ref } from 'vue'
import { aiApi } from '@/api/ai'

const props = defineProps<{ levelId: number }>()
const emit = defineEmits<{ close: []; 'hint-used': [] }>()

const question = ref('')
const hintLevel = ref(0)
const content = ref('')
const escalate = ref(false)
const loading = ref(false)

async function ask() {
  loading.value = true
  try {
    const { data } = await aiApi.hint(props.levelId, question.value || '请给点提示', hintLevel.value)
    hintLevel.value = data.data.hintLevel
    content.value = data.data.content
    escalate.value = data.data.escalateAvailable
    emit('hint-used')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div
    style="position: fixed; inset: 0; background: rgba(0, 0, 0, 0.65); display: flex; align-items: center; justify-content: center; z-index: 1000"
    @click.self="emit('close')"
  >
    <div class="card" style="width: 420px; max-width: 90vw; margin: 0">
      <h3>
        AI 渐进式提示
        <button class="secondary sm" style="float: right" @click="emit('close')">✕</button>
      </h3>
      <p class="muted" style="font-size: 12px">L1 剧情引导 → L2 概念讲解 → L3 关键提示，永不直接给答案</p>
      <input v-model="question" placeholder="你的问题（索要答案会被拒）" @keyup.enter="ask" />
      <div style="margin-top: 12px">
        <button :disabled="loading" @click="ask">{{ loading ? '思考中…' : '请求提示' }}</button>
        <button
          v-if="escalate && content"
          class="secondary"
          style="margin-left: 8px"
          @click="ask"
        >
          升级提示 →
        </button>
      </div>
      <p
        v-if="content"
        style="margin-top: 12px; padding: 10px; background: var(--surface-2); border: 1px solid var(--border); border-radius: 8px; color: var(--text)"
      >
        [L{{ hintLevel }}] {{ content }}
      </p>
    </div>
  </div>
</template>