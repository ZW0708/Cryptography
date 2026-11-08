<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import TermTip from '@/components/TermTip.vue'

const emit = defineEmits<{ solved: [text: string] }>()

const ORIGINAL = '转账 100 元给 Alice'
const message = ref(ORIGINAL)
const currentHash = ref('')
const originalHash = ref('')
const verified = ref<null | boolean>(null)
const submitted = ref(false)
const busy = ref(false)

const step1Done = ref(false)
const step2Done = ref(false)

async function sha256Hex(text: string): Promise<string> {
  const data = new TextEncoder().encode(text)
  const digest = await crypto.subtle.digest('SHA-256', data)
  return Array.from(new Uint8Array(digest))
    .map((b) => b.toString(16).padStart(2, '0'))
    .join('')
}

onMounted(async () => {
  originalHash.value = await sha256Hex(ORIGINAL)
  currentHash.value = originalHash.value
})

async function verify() {
  busy.value = true
  try {
    currentHash.value = await sha256Hex(message.value)
    verified.value = currentHash.value === originalHash.value
    if (verified.value) step1Done.value = true
    else step2Done.value = true
  } finally {
    busy.value = false
  }
}

function randomTamper() {
  if (!message.value) return
  const idx = Math.floor(Math.random() * message.value.length)
  const pool = '0123456789abcdefxyz'
  const ch = pool[Math.floor(Math.random() * pool.length)] ?? 'x'
  const replaced = message.value[idx] === ch ? 'z' : ch
  message.value = message.value.slice(0, idx) + replaced + message.value.slice(idx + 1)
  verified.value = null
}

function reset() {
  message.value = ORIGINAL
  currentHash.value = originalHash.value
  verified.value = null
  submitted.value = false
}

function confirm() {
  if (!step1Done.value || !step2Done.value || submitted.value) return
  submitted.value = true
  emit('solved', 'sign-tamper-detected')
}

const segPairs = computed(() => {
  if (!originalHash.value || !currentHash.value) return []
  const pairs = []
  for (let i = 0; i < 64; i += 8) {
    const a = originalHash.value.slice(i, i + 8)
    const b = currentHash.value.slice(i, i + 8)
    pairs.push({ a, b, match: a === b })
  }
  return pairs
})

const diffSegments = computed(() => segPairs.value.filter((p) => !p.match).length)
</script>

<template>
  <div class="card">
    <div class="row wrap">
      <span class="badge badge-warning">角色：接收方 / 篡改者</span>
      <span class="spacer" />
      <span class="muted" style="font-size: 12px">目标：先看懂验签，再让篡改被抓住</span>
    </div>

    <div class="intro-block">
      <strong style="font-size: 13px">📖 60 秒概念引入</strong>
      <p style="margin: 6px 0 0; line-height: 1.7">
        <TermTip term="数字签名" tip="发送方用「私钥」对消息哈希做的身份印记；任何人都能用「公钥」验证它。" />
        的工作方式：发送方对消息算
        <TermTip term="哈希" tip="消息的固定长度「指纹」，改动一点，指纹整段大变。" />
        并用<TermTip term="私钥" tip="只有发送方掌握的钥匙，用来生成签名。" />签名；
        接收方用<TermTip term="公钥" tip="人人可得的钥匙，只能验证、不能伪造签名。" />核对哈希是否一致。
      </p>
    </div>

    <div class="row wrap" style="margin-bottom: 4px">
      <span :class="step1Done ? 'badge badge-success' : 'badge badge-muted'">① 体验正常验签</span>
      <span :class="step2Done ? 'badge badge-success' : 'badge badge-muted'">② 体验篡改被拦</span>
    </div>

    <div class="sig-box">
      <div class="sig-label">原始公告（发送方发出）</div>
      <code>{{ ORIGINAL }}</code>
      <div class="sig-label" style="margin-top: 10px">签名（私钥对下方哈希封装）</div>
      <code class="hash">{{ originalHash || '计算中…' }}</code>
    </div>

    <label>你手上的公告（可编辑）</label>
    <textarea v-model="message" rows="2" placeholder="完整消息，或动手篡改它"></textarea>
    <div class="row wrap" style="margin-top: 10px">
      <button :disabled="busy" @click="verify">{{ busy ? '验证中…' : '🔍 验证签名' }}</button>
      <button class="secondary" @click="randomTamper">🎲 随机改一个字符</button>
      <button class="secondary" @click="reset">重置</button>
    </div>

    <div v-if="originalHash && currentHash" class="compare-box">
      <div class="section-title" style="margin-top: 0">哈希逐段对比（每段 8 位）</div>
      <div class="cmp-row">
        <span class="cmp-label">签名中</span>
        <span v-for="(p, i) in segPairs" :key="'a' + i" class="seg" :class="p.match ? 'match' : 'miss'">{{ p.a }}</span>
      </div>
      <div class="cmp-row">
        <span class="cmp-label">当前消息</span>
        <span v-for="(p, i) in segPairs" :key="'b' + i" class="seg" :class="p.match ? 'match' : 'miss'">{{ p.b }}</span>
      </div>
      <div v-if="verified === true" class="verdict ok">
        ✓ 全部 8 段一致：消息完整、来源可信（正常流程）。
      </div>
      <div v-else-if="verified === false" class="verdict bad">
        ✗ 有 {{ diffSegments }}/8 段不一致：消息已被篡改，签名当场失效。改的明明只有一个字，哈希却整段对不上——这就是雪崩效应在保护你。
      </div>
      <div v-else class="verdict neutral">点击「验证签名」，看两串哈希如何逐段比对。</div>
    </div>

    <div class="flow-hint">
      <p :class="step1Done ? 'done-p' : 'todo-p'">
        ① 消息未改动时验证一次（应验签成功）<span v-if="step1Done"> ✓</span>
      </p>
      <p :class="step2Done ? 'done-p' : 'todo-p'">
        ② 改动消息后再验证一次（应验签失败）<span v-if="step2Done"> ✓</span>
      </p>
    </div>

    <div v-if="step1Done && step2Done" class="ok-row">
      <button :disabled="submitted" @click="confirm">
        {{ submitted ? '已提交' : '✅ 两个视角都体验了，提交通关' }}
      </button>
      <span class="muted" style="font-size: 12px">签名保护完整性与身份，而非机密性。</span>
    </div>

    <p class="muted hint" style="margin-top: 12px">
      简化演示：真实签名用私钥对消息哈希加密、公钥验签，并含证书链验证；这里用哈希比对模拟防篡改效果。
    </p>
  </div>
</template>

<style scoped>
.intro-block {
  margin-top: 12px;
  padding: 10px 14px;
  background: var(--surface-2);
  border: 1px solid var(--border);
  border-radius: 8px;
}

.sig-box {
  margin-top: 14px;
  padding: 12px 14px;
  background: #131d19;
  border: 1px solid var(--border);
  border-radius: 8px;
}

.sig-label {
  font-size: 11px;
  color: var(--text-muted);
  font-family: var(--mono);
  letter-spacing: 0.05em;
}

.sig-box code {
  display: block;
  margin-top: 3px;
  color: var(--text);
  word-break: break-all;
}

.sig-box code.hash {
  color: var(--primary);
  font-size: 12px;
}

.compare-box {
  margin-top: 14px;
  padding: 12px 14px;
  background: #131d19;
  border: 1px solid var(--border);
  border-radius: 8px;
}

.cmp-row {
  display: flex;
  align-items: center;
  gap: 4px;
  flex-wrap: wrap;
  margin-bottom: 6px;
}

.cmp-label {
  width: 64px;
  flex-shrink: 0;
  font-size: 11px;
  color: var(--text-muted);
  font-family: var(--mono);
}

.seg {
  font-family: var(--mono);
  font-size: 11px;
  padding: 2px 5px;
  border-radius: 4px;
}

.seg.match {
  color: var(--primary);
  background: rgba(62, 242, 176, 0.1);
}

.seg.miss {
  color: var(--danger);
  background: rgba(255, 107, 121, 0.12);
  text-decoration: line-through;
}

.verdict {
  margin-top: 8px;
  font-size: 13px;
  line-height: 1.7;
}

.verdict.ok {
  color: var(--primary);
}

.verdict.bad {
  color: var(--danger);
}

.verdict.neutral {
  color: var(--text-muted);
}

.flow-hint {
  margin-top: 14px;
}

.done-p {
  color: var(--primary);
  font-size: 13px;
  margin: 4px 0;
}

.todo-p {
  color: var(--text-muted);
  font-size: 13px;
  margin: 4px 0;
}

.ok-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 16px;
  flex-wrap: wrap;
}

.section-title {
  font-family: var(--mono);
  font-size: 12px;
  color: var(--text-muted);
  letter-spacing: 0.04em;
}

.hint {
  margin: 8px 0 0;
  font-size: 12px;
  line-height: 1.7;
}
</style>