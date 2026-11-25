<script setup lang="ts">
import { ref } from 'vue'
import { aiApi } from '@/api/ai'
import type { ToolTrace } from '@/types'

const props = defineProps<{
  levelId: number
  puzzleState?: Record<string, string>
  levelTitle?: string
}>()
const emit = defineEmits<{ close: []; 'hint-used': [] }>()

interface ChatItem {
  role: 'user' | 'assistant'
  content: string
  hintLevel?: number
  intent?: string
  channel?: string
  tools?: ToolTrace[]
}

const input = ref('')
const hintLevel = ref(0)
const loading = ref(false)
const messages = ref<ChatItem[]>([])
const expanded = ref<number | null>(null)

const QUICK_QUESTIONS = ['我卡住了', '这关考什么概念', '下一步该做什么']

async function ask(text?: string) {
  const question = (text ?? input.value).trim() || '请给点提示'
  if (loading.value) return
  messages.value.push({ role: 'user', content: question })
  input.value = ''
  loading.value = true
  try {
    const { data } = await aiApi.chat(
      props.levelId,
      question,
      hintLevel.value,
      props.puzzleState ?? {}
    )
    const res = data.data
    hintLevel.value = res.hintLevel
    messages.value.push({
      role: 'assistant',
      content: res.content,
      hintLevel: res.hintLevel,
      intent: res.intent,
      channel: res.channel,
      tools: res.tools
    })
    emit('hint-used')
  } catch (e: any) {
    messages.value.push({
      role: 'assistant',
      content: e.response?.data?.message ?? 'AI 服务暂时不可用，请稍后再试。'
    })
  } finally {
    loading.value = false
  }
}

function toggleTools(index: number) {
  expanded.value = expanded.value === index ? null : index
}
</script>

<template>
  <div class="agent-mask" @click.self="emit('close')">
    <aside class="agent-panel">
      <header class="agent-head">
        <div>
          <strong>AI 助教</strong>
          <p class="muted">
            {{ levelTitle || '当前关卡' }} · L{{ hintLevel }} / 3 · 会查进度与知识库，不直接给答案
          </p>
        </div>
        <button class="secondary sm" @click="emit('close')">✕</button>
      </header>

      <div class="agent-body">
        <div v-if="messages.length === 0" class="agent-empty">
          <p>描述你卡在哪一步，或点下面快捷提问。我会先判断卡点，再调用工具给你方向。</p>
          <div class="quick-row">
            <button
              v-for="q in QUICK_QUESTIONS"
              :key="q"
              class="secondary sm"
              :disabled="loading"
              @click="ask(q)"
            >
              {{ q }}
            </button>
          </div>
        </div>

        <div
          v-for="(m, i) in messages"
          :key="i"
          class="bubble"
          :class="m.role === 'user' ? 'bubble-user' : 'bubble-agent'"
        >
          <p class="bubble-text">{{ m.content }}</p>
          <div v-if="m.role === 'assistant' && m.tools && m.tools.length" class="trace">
            <button class="trace-toggle" @click="toggleTools(i)">
              🧰 调用了 {{ m.tools.length }} 个工具 · {{ m.intent }} · {{ m.channel }} · L{{ m.hintLevel }}
            </button>
            <ul v-if="expanded === i" class="trace-list">
              <li v-for="t in m.tools" :key="t.tool">
                <code>{{ t.tool }}</code>（{{ t.durationMs }}ms）{{ t.summary }}
              </li>
            </ul>
          </div>
        </div>

        <p v-if="loading" class="muted agent-loading">正在分析你的状态并调用工具…</p>
      </div>

      <footer class="agent-foot">
        <input
          v-model="input"
          placeholder="比如：密文里哪个字母出现最多？"
          :disabled="loading"
          @keyup.enter="ask()"
        />
        <button :disabled="loading" @click="ask()">{{ loading ? '…' : '发送' }}</button>
      </footer>
    </aside>
  </div>
</template>

<style scoped>
.agent-mask {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
  z-index: 1000;
  display: flex;
  justify-content: flex-end;
}

.agent-panel {
  width: 420px;
  max-width: 92vw;
  height: 100%;
  display: flex;
  flex-direction: column;
  background: var(--surface);
  border-left: 1px solid var(--border);
  box-shadow: var(--shadow);
}

.agent-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  padding: 16px;
  border-bottom: 1px solid var(--border);
}

.agent-head p {
  margin: 4px 0 0;
  font-size: 12px;
}

.agent-body {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.agent-empty {
  color: var(--text-muted);
  font-size: 13px;
}

.quick-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 10px;
}

.bubble {
  border-radius: var(--radius);
  padding: 10px 12px;
  font-size: 14px;
  border: 1px solid var(--border);
}

.bubble-text {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-word;
}

.bubble-user {
  align-self: flex-end;
  background: var(--primary-light);
  border-color: rgba(62, 242, 176, 0.35);
  max-width: 88%;
}

.bubble-agent {
  background: var(--surface-2);
  max-width: 94%;
}

.trace {
  margin-top: 8px;
  border-top: 1px dashed var(--border);
  padding-top: 6px;
}

.trace-toggle {
  background: none;
  border: none;
  padding: 0;
  color: var(--info);
  font-size: 12px;
  cursor: pointer;
  text-align: left;
}

.trace-list {
  margin: 6px 0 0;
  padding-left: 18px;
  font-size: 12px;
  color: var(--text-muted);
}

.trace-list code {
  color: var(--primary);
}

.agent-loading {
  font-size: 12px;
}

.agent-foot {
  display: flex;
  gap: 8px;
  padding: 12px 16px 16px;
  border-top: 1px solid var(--border);
}

.agent-foot input {
  flex: 1;
  min-width: 0;
}
</style>