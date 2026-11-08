<script setup lang="ts">
import { ref, computed, onBeforeUnmount } from 'vue'
import TermTip from '@/components/TermTip.vue'

const emit = defineEmits<{ solved: [text: string] }>()

/* ---------- ① 雪崩效应 ---------- */
const snowInput = ref('hello')
const snowHash = ref('')
let snowChanges = 0
const avalancheDone = ref(false)

async function sha256Hex(text: string): Promise<string> {
  const data = new TextEncoder().encode(text)
  const digest = await crypto.subtle.digest('SHA-256', data)
  return Array.from(new Uint8Array(digest))
    .map((b) => b.toString(16).padStart(2, '0'))
    .join('')
}

async function onSnowInput() {
  snowHash.value = await sha256Hex(snowInput.value || ' ')
  snowChanges++
  if (snowChanges >= 2 && !avalancheDone.value) {
    avalancheDone.value = true
  }
}

/* ---------- ② 碰撞难度挑战 ---------- */
const target = ref(genTarget())
const running = ref(false)
const attempts = ref(0)
const elapsedMs = ref(0)
const recent = ref<{ input: string; hash: string }[]>([])
const collisionDone = ref(false)
const hitInfo = ref<{ input: string; hash: string } | null>(null)

let stopFlag = false
let stopwatch: number | null = null
const MAX_TRIES = 40

function genTarget() {
  const arr = new Uint8Array(2)
  crypto.getRandomValues(arr)
  return Array.from(arr)
    .map((b) => b.toString(16).padStart(2, '0'))
    .join('')
}

function regenTarget() {
  if (running.value) return
  target.value = genTarget()
  resetRun()
}

function randomInput() {
  const arr = new Uint8Array(6)
  crypto.getRandomValues(arr)
  const alphabet = 'abcdefghijklmnopqrstuvwxyz0123456789'
  return Array.from(arr)
    .map((b) => alphabet[b % 36])
    .join('')
}

function sleep(ms: number) {
  return new Promise((r) => setTimeout(r, ms))
}

function resetRun() {
  attempts.value = 0
  elapsedMs.value = 0
  recent.value = []
  collisionDone.value = false
  hitInfo.value = null
}

function stopChallenge() {
  if (!running.value) return
  stopFlag = true
  running.value = false
  if (stopwatch) clearInterval(stopwatch)
  stopwatch = null
  if (attempts.value > 0) collisionDone.value = true
}

async function startChallenge() {
  if (running.value) return
  running.value = true
  stopFlag = false
  resetRun()
  const t0 = performance.now()
  stopwatch = window.setInterval(() => {
    elapsedMs.value = performance.now() - t0
  }, 100)

  while (running.value && !stopFlag && attempts.value < MAX_TRIES) {
    const input = randomInput()
    const hash = await sha256Hex(input)
    attempts.value++
    recent.value = [{ input, hash }, ...recent.value].slice(0, 5)

    if (hash.startsWith(target.value)) {
      hitInfo.value = { input, hash }
      finishRun()
      return
    }
    await sleep(140)
  }

  if (attempts.value >= MAX_TRIES) {
    collisionDone.value = true
  }
  finishRun()
}

function finishRun() {
  running.value = false
  if (stopwatch) clearInterval(stopwatch)
  stopwatch = null
}

function confirm() {
  if (!collisionDone.value || !avalancheDone.value || submitted.value) return
  submitted.value = true
  emit('solved', 'collision-found')
}

const submitted = ref(false)

function prefixClass(hash: string) {
  return hash.slice(0, 4) === target.value ? 'p4 hit' : 'p4'
}

onBeforeUnmount(() => {
  stopFlag = true
  running.value = false
  if (stopwatch) clearInterval(stopwatch)
})

const elapsedSec = computed(() => (elapsedMs.value / 1000).toFixed(1))
</script>

<template>
  <div class="card">
    <div class="row wrap">
      <span class="badge badge-warning">角色：攻击者</span>
      <span class="spacer" />
      <span class="muted" style="font-size: 12px">目标：体验哈希三大特性</span>
    </div>

    <div class="intro-block">
      <strong style="font-size: 13px">📖 60 秒概念引入</strong>
      <p style="margin: 6px 0 0; line-height: 1.7">
        <TermTip term="哈希" tip="把任意长度的输入压成固定长度的「指纹」（如 SHA-256 输出 64 个十六进制字符）。" />
        有三大特性：
        <TermTip term="单向不可逆" tip="能从原文算出指纹，却无法从指纹反推原文。" />、
        <TermTip term="雪崩效应" tip="输入改动一点点，指纹整段大变。" />、
        <TermTip term="碰撞困难" tip="想找到两段指纹相同（或前缀相同）的输入，难到只能穷举。" />。
      </p>
    </div>

    <!-- ① 雪崩效应体验区 -->
    <div class="section-title">
      ① 雪崩效应体验区
      <span v-if="avalancheDone" class="badge badge-success" style="margin-left: 8px">✓ 已体验</span>
    </div>
    <input v-model="snowInput" placeholder="随便输入点什么，然后改动一个字符试试" @input="onSnowInput" />
    <div class="hash-line">
      <span v-for="(ch, i) in snowHash" :key="i + '-' + ch" class="hash-ch">{{ ch }}</span>
      <span v-if="!snowHash" class="muted">在上方输入后，这里会显示 SHA-256 指纹</span>
    </div>
    <p class="muted hint">改动一个字 / 一个标点，观察指纹如何整段「跳变」——输入微变，输出剧变。</p>

    <!-- ② 碰撞难度模拟区 -->
    <div class="section-title">
      ② 碰撞难度挑战：找相同前缀
      <span v-if="collisionDone" class="badge badge-success" style="margin-left: 8px">✓ 已体验</span>
    </div>
    <div class="row wrap">
      <span class="target-badge">目标前缀：{{ target }}</span>
      <button class="secondary sm" :disabled="running" @click="regenTarget">换一个目标</button>
      <button v-if="!running" :disabled="!avalancheDone" @click="startChallenge">🎲 开始生成随机输入</button>
      <button v-else class="secondary" @click="stopChallenge">停止</button>
    </div>
    <p class="muted hint">
      系统会不断生成随机字符串并计算 SHA-256。目标：前 4 位恰好等于目标前缀。先试试你需要多少次。
    </p>

    <div class="stats-row">
      <span class="stat">已尝试 <strong>{{ attempts }}</strong> 次</span>
      <span class="stat">耗时 <strong>{{ elapsedSec }}</strong> s</span>
    </div>

    <div v-if="recent.length" class="crack-box">
      <TransitionGroup name="wf" tag="div" class="crack-list">
        <div v-for="r in recent" :key="r.input" class="crack-line">
          <span class="muted">#{{ r.input }}</span>
          <code><span :class="prefixClass(r.hash)">{{ r.hash.slice(0, 4) }}</span>{{ r.hash.slice(4, 20) }}…</code>
        </div>
      </TransitionGroup>
      <p class="muted hint" style="margin: 6px 0 0">绿色前 4 位 = 命中目标。目前一次都没中。</p>
    </div>

    <div v-if="hitInfo" class="found-box">
      <p style="margin: 0"><strong>🎉 居然命中了！</strong></p>
      <p class="mono-line" style="margin: 6px 0 0">输入 = {{ hitInfo.input }}，概率约 1/65536，运气极佳。</p>
      <p class="mono-line" style="margin: 2px 0 0">SHA-256 = {{ hitInfo.hash }}</p>
    </div>

    <div v-else-if="collisionDone" class="science-box">
      <strong style="font-size: 13px">🧪 你刚刚亲身验证了「碰撞困难」</strong>
      <p style="margin: 6px 0 0; line-height: 1.8">
        你连续生成了 <strong>{{ attempts }}</strong> 个随机输入，没有一个的前 4 位命中目标。
        前缀每加长一位，难度 ×16：前 6 位约 1677 万次；前 8 位约 43 亿种组合——普通家用电脑需要连续运行数年。
        这正是哈希算法安全性的基础。
      </p>
    </div>

    <div v-if="avalancheDone && collisionDone" class="ok-row">
      <button :disabled="submitted" @click="confirm">
        {{ submitted ? '已提交' : '✅ 完成两大环节，提交通关' }}
      </button>
      <span class="muted" style="font-size: 12px">两个环节都完成后可通关。</span>
    </div>
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

.section-title {
  margin-top: 18px;
  font-family: var(--mono);
  font-size: 13px;
  color: var(--primary);
  letter-spacing: 0.04em;
}

.hash-line {
  margin-top: 10px;
  padding: 10px 12px;
  background: #131d19;
  border: 1px solid var(--border);
  border-radius: 8px;
  font-family: var(--mono);
  font-size: 14px;
  color: var(--primary);
  word-break: break-all;
  line-height: 1.7;
  min-height: 42px;
}

.hash-ch {
  display: inline-block;
  animation: hashPop 0.3s ease;
}

@keyframes hashPop {
  from {
    color: var(--warning);
    transform: translateY(-3px);
  }
  to {
    color: var(--primary);
    transform: translateY(0);
  }
}

.target-badge {
  font-family: var(--mono);
  font-size: 14px;
  font-weight: 700;
  color: var(--warning);
  border: 1px dashed rgba(255, 208, 107, 0.5);
  border-radius: 8px;
  padding: 4px 12px;
  letter-spacing: 0.08em;
}

.stats-row {
  display: flex;
  gap: 16px;
  margin-top: 10px;
  font-family: var(--mono);
  font-size: 13px;
}

.stat strong {
  color: var(--info);
}

.crack-box {
  margin-top: 12px;
  padding: 10px 12px;
  background: #131d19;
  border: 1px solid var(--border);
  border-radius: 8px;
}

.crack-line {
  display: flex;
  gap: 10px;
  font-family: var(--mono);
  font-size: 12px;
  line-height: 1.8;
}

.crack-line code {
  color: var(--text-muted);
}

.p4 {
  color: var(--text);
}

.p4.hit {
  color: var(--primary);
  font-weight: 700;
}

.science-box,
.found-box {
  margin-top: 14px;
  padding: 12px 14px;
  border-radius: 8px;
  font-size: 13px;
}

.science-box {
  background: var(--primary-light);
  border: 1px solid rgba(62, 242, 176, 0.35);
}

.found-box {
  background: var(--primary-light);
  border: 1px solid rgba(62, 242, 176, 0.45);
  box-shadow: 0 0 18px rgba(62, 242, 176, 0.15);
}

.found-box .mono-line {
  color: var(--primary);
  font-family: var(--mono);
  font-size: 12px;
  word-break: break-all;
}

.crack-list {
  position: relative;
}

.wf-enter-active {
  transition: all 0.3s ease;
}

.wf-enter-from {
  opacity: 0;
  transform: translateY(-8px);
}

.wf-leave-active {
  transition: all 0.15s ease;
  position: absolute;
  width: 100%;
}

.wf-leave-to {
  opacity: 0;
}

.wf-move {
  transition: transform 0.3s ease;
}

.p4.hit {
  animation: hit-blink 0.8s ease infinite;
}

@keyframes hit-blink {
  0%,
  100% {
    color: var(--primary);
  }
  50% {
    color: var(--warning);
  }
}

.ok-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 16px;
}

.hint {
  margin: 8px 0 0;
  font-size: 12px;
  line-height: 1.7;
}
</style>