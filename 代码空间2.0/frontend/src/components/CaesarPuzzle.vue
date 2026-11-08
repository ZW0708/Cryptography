<script setup lang="ts">
import { ref, computed, onBeforeUnmount } from 'vue'
import TermTip from '@/components/TermTip.vue'
import { useToast } from '@/composables/useToast'

const emit = defineEmits<{ solved: [text: string] }>()
const { toast } = useToast()

const CIPHER = 'PHHW PH DW WKH ROG OLEUDUB DW VLA RFORFN. EULQJ WKH VHFUHW ERRN'
const PLAIN = 'MEET ME AT THE OLD LIBRARY AT SIX OCLOCK. BRING THE SECRET BOOK'
const ANSWER = 'MEETMEATTHEOLDLIBRARYATSIXOCLOCKBRINGTHESECRETBOOK'
const CORRECT_SHIFT = 3

const innerRotation = computed(() => shift.value * (360 / 26))

function wheelPos(radius: number, i: number) {
  const angle = ((i - 1) * 360) / 26 - 90
  const rad = (angle * Math.PI) / 180
  return { x: 130 + radius * Math.cos(rad), y: 130 + radius * Math.sin(rad) + 4 }
}

const SENTENCES = [
  { c: 'PHHW PH DW WKH ROG OLEUDUB DW VLA RFORFN', p: 'MEET ME AT THE OLD LIBRARY AT SIX OCLOCK' },
  { c: 'EULQJ WKH VHFUHW ERRN', p: 'BRING THE SECRET BOOK' }
]

const EN_FREQ = [
  { letter: 'E', pct: '12.7%' },
  { letter: 'T', pct: '9.1%' },
  { letter: 'A', pct: '8.2%' },
  { letter: 'O', pct: '7.5%' },
  { letter: 'I', pct: '7.0%' },
  { letter: 'N', pct: '6.7%' },
  { letter: 'S', pct: '6.3%' },
  { letter: 'H', pct: '6.1%' },
  { letter: 'R', pct: '6.0%' }
]

const shift = ref(0)
const submitted = ref(false)
const bruteRunning = ref(false)
let bruteTimer: number | null = null

function shiftChar(c: string, k: number) {
  if (!/[A-Z]/.test(c)) return c
  return String.fromCharCode(((c.charCodeAt(0) - 65 - k + 26) % 26) + 65)
}

function decryptText(t: string, k: number) {
  return t.replace(/[A-Z]/g, (c) => shiftChar(c, k))
}

const decrypted = computed(() => decryptText(CIPHER, shift.value))
const readable = computed(() => decrypted.value === PLAIN)

const freq = computed(() => {
  const counts: Record<string, number> = {}
  CIPHER.replace(/[^A-Z]/g, '')
    .split('')
    .forEach((c) => {
      counts[c] = (counts[c] || 0) + 1
    })
  return 'ABCDEFGHIJKLMNOPQRSTUVWXYZ'
    .split('')
    .map((l) => ({ letter: l, count: counts[l] || 0 }))
    .sort((a, b) => b.count - a.count)
})

const maxCount = computed(() => Math.max(1, ...freq.value.map((f) => f.count)))
const topLetter = computed(() => (freq.value[0] ? freq.value[0].letter : '-'))

function bump(delta: number) {
  shift.value = (shift.value + delta + 26) % 26
}

function setShiftFromInput(e: Event) {
  const v = Number((e.target as HTMLInputElement).value)
  shift.value = Math.min(25, Math.max(0, Number.isFinite(v) ? Math.round(v) : 0))
}

function bruteForce() {
  if (bruteRunning.value) return
  bruteRunning.value = true
  let k = 0
  bruteTimer = window.setInterval(() => {
    shift.value = k
    k++
    if (k > 25) {
      if (bruteTimer) clearInterval(bruteTimer)
      bruteTimer = null
      bruteRunning.value = false
      shift.value = CORRECT_SHIFT
      toast('暴力破解试了全部 25 种位移；而频率分析只要盯住 1 个高频字母就能定位——这就是密码分析的核心思路', 'info', 6000)
    }
  }, 140)
}

function confirm() {
  if (!readable.value || submitted.value) return
  submitted.value = true
  emit('solved', ANSWER)
}

onBeforeUnmount(() => {
  if (bruteTimer) clearInterval(bruteTimer)
})
</script>

<template>
  <div class="card">
    <div class="row wrap">
      <span class="badge badge-info">角色：密文分析员</span>
      <span class="spacer" />
      <span class="muted" style="font-size: 12px">任务：还原这段有真实含义的英文密文</span>
    </div>

    <div class="intro-block">
      <strong style="font-size: 13px">📖 60 秒概念引入</strong>
      <p style="margin: 6px 0 0; line-height: 1.7">
        <TermTip term="凯撒密码" tip="把字母表中每个字母整体平移固定位数的加密法，平移量就是密钥。" />
        是一种<TermTip term="替换密码" tip="用别的字母替换原字母来隐藏原文，替换规则就是密钥。" />：
        每个字母整体平移 k 位。它的死穴是不改变字母的
        <TermTip term="字母频率" tip="每种字母在英文里出现的机会不同：E 最常见，其次 T、A、O…" />，
        所以可以用「频率分析」反推位移。
      </p>
    </div>

    <div class="cipher-block">
      <div class="block-label">截获密文</div>
      <div class="cipher-text">{{ CIPHER }}</div>
    </div>

    <div class="section-title">① 频率统计工具</div>
    <div class="freq-layout">
      <div class="freq-bars">
        <div v-for="f in freq" :key="f.letter" class="bar-row" :class="{ top: f.count === maxCount && f.count > 0 }">
          <span class="bar-letter">{{ f.letter }}</span>
          <div class="bar-track">
            <div class="bar-fill" :style="{ width: (f.count / maxCount) * 100 + '%' }"></div>
          </div>
          <span class="bar-count">{{ f.count }}</span>
        </div>
      </div>
      <div class="freq-ref">
        <div class="ref-title">标准英文词频对照</div>
        <div v-for="f in EN_FREQ" :key="f.letter" class="ref-row">
          <span class="ref-letter">{{ f.letter }}</span>
          <span class="muted">{{ f.pct }}</span>
        </div>
        <p class="muted hint" style="margin-top: 8px">
          密文最高频是 <strong class="accent">{{ topLetter }}</strong
          >——对照上表，它很可能对应哪个字母？
        </p>
      </div>
    </div>

    <div class="section-title">② 调整位移，实时解密</div>
    <div class="shift-row">
      <button class="secondary sm" @click="bump(-1)">−</button>
      <input type="range" min="0" max="25" v-model.number="shift" />
      <button class="secondary sm" @click="bump(1)">＋</button>
      <input
        class="shift-num"
        type="number"
        min="0"
        max="25"
        :value="shift"
        @change="setShiftFromInput"
      />
      <span class="shift-value">k = {{ shift }}</span>
    </div>

    <p class="muted hint" style="margin-top: 4px">内环 = 明文字母 · 外环 = 密文字母 · 转动内环对齐频率线索</p>
    <svg viewBox="0 0 260 260" class="wheel" role="img" aria-label="凯撒转盘：外环密文字母，内环明文字母">
      <circle cx="130" cy="130" r="112" class="wheel-ring" />
      <circle cx="130" cy="130" r="80" class="wheel-ring inner" />
      <text
        v-for="i in 26"
        :key="'o' + i"
        :x="wheelPos(97, i).x"
        :y="wheelPos(97, i).y"
        text-anchor="middle"
        class="wheel-ch"
      >{{ String.fromCharCode(64 + i) }}</text>
      <g :transform="'rotate(' + innerRotation + ' 130 130)'">
        <text
          v-for="i in 26"
          :key="'i' + i"
          :x="wheelPos(64, i).x"
          :y="wheelPos(64, i).y"
          text-anchor="middle"
          class="wheel-ch inner"
          :transform="'rotate(' + -innerRotation + ' ' + wheelPos(64, i).x + ' ' + (wheelPos(64, i).y - 4) + ')'"
        >{{ String.fromCharCode(64 + i) }}</text>
      </g>
      <polygon points="130,6 123,18 137,18" class="wheel-pointer" />
      <circle cx="130" cy="130" r="15" class="wheel-hub" />
      <text x="130" y="135" text-anchor="middle" class="wheel-k">k={{ shift }}</text>
    </svg>

    <div class="section-title">③ 逐句对照</div>
    <div v-for="(s, i) in SENTENCES" :key="i" class="sentence">
      <div class="s-line muted">{{ s.c }}</div>
      <div class="s-line" :class="{ ok: decryptText(s.c, shift) === s.p }">
        {{ decryptText(s.c, shift) }}
      </div>
    </div>

    <div class="result" :class="{ ok: readable }">{{ decrypted }}</div>

    <div class="row wrap" style="margin-top: 12px">
      <button class="secondary" :disabled="bruteRunning" @click="bruteForce">
        {{ bruteRunning ? '暴力破解中…' : '🧨 试试暴力破解（对比）' }}
      </button>
      <span v-if="readable" class="badge badge-success">✓ 明文已恢复可读</span>
    </div>

    <div v-if="readable" class="ok-row">
      <button :disabled="submitted" @click="confirm">{{ submitted ? '已提交' : '确认并提交' }}</button>
      <span class="muted" style="font-size: 12px">明文恢复可读即通关。</span>
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

.block-label {
  font-size: 12px;
  color: var(--text-muted);
  font-family: var(--mono);
  letter-spacing: 0.05em;
}

.cipher-block {
  margin-top: 12px;
  padding: 12px 14px;
  background: #131d19;
  border: 1px solid var(--border);
  border-radius: 8px;
}

.cipher-text {
  margin-top: 4px;
  font-family: var(--mono);
  font-size: 15px;
  letter-spacing: 0.14em;
  color: var(--text);
  word-break: break-all;
  line-height: 1.9;
}

.section-title {
  margin-top: 18px;
  font-family: var(--mono);
  font-size: 13px;
  color: var(--primary);
  letter-spacing: 0.04em;
}

.freq-layout {
  display: flex;
  gap: 20px;
  margin-top: 8px;
  align-items: flex-start;
}

.freq-bars {
  flex: 1;
  min-width: 0;
}

.bar-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 3px;
}

.bar-row.top .bar-letter,
.bar-row.top .bar-count {
  color: var(--primary);
  font-weight: 700;
}

.bar-letter {
  width: 14px;
  font-family: var(--mono);
  font-size: 12px;
  color: var(--text-muted);
}

.bar-track {
  flex: 1;
  height: 8px;
  background: #131d19;
  border: 1px solid var(--border);
  border-radius: 999px;
  overflow: hidden;
}

.bar-fill {
  height: 100%;
  background: linear-gradient(90deg, var(--primary), var(--info));
  border-radius: 999px;
  transition: width 0.25s;
}

.bar-count {
  width: 22px;
  text-align: right;
  font-family: var(--mono);
  font-size: 12px;
  color: var(--text-muted);
}

.freq-ref {
  width: 180px;
  flex-shrink: 0;
  padding: 10px 12px;
  background: var(--surface-2);
  border: 1px solid var(--border);
  border-radius: 8px;
}

.ref-title {
  font-size: 12px;
  font-family: var(--mono);
  color: var(--text-muted);
  margin-bottom: 6px;
}

.ref-row {
  display: flex;
  justify-content: space-between;
  font-family: var(--mono);
  font-size: 12px;
  line-height: 1.7;
}

.ref-letter {
  color: var(--text);
  font-weight: 700;
}

.shift-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 8px;
}

.shift-row input[type='range'] {
  flex: 1;
  min-width: 0;
}

.shift-num {
  width: 64px;
  flex-shrink: 0;
}

.shift-value {
  font-family: var(--mono);
  font-size: 13px;
  color: var(--info);
  white-space: nowrap;
}

.wheel {
  display: block;
  margin: 10px auto 0;
  width: min(300px, 100%);
}

.wheel-ring {
  fill: none;
  stroke: var(--border);
}

.wheel-ring.inner {
  stroke-dasharray: 4 4;
}

.wheel-ch {
  font-family: var(--mono);
  font-size: 12px;
  fill: var(--text-muted);
}

.wheel-ch.inner {
  fill: var(--primary);
}

.wheel-pointer {
  fill: var(--warning);
}

.wheel-hub {
  fill: var(--surface-2);
  stroke: var(--border);
}

.wheel-k {
  font-family: var(--mono);
  font-size: 12px;
  fill: var(--info);
}

.sentence {
  margin-top: 8px;
  padding: 8px 12px;
  background: var(--surface-2);
  border: 1px solid var(--border);
  border-radius: 8px;
}

.s-line {
  font-family: var(--mono);
  font-size: 13px;
  letter-spacing: 0.08em;
  line-height: 1.9;
  word-break: break-all;
  transition: color 0.2s;
}

.s-line.ok {
  color: var(--primary);
}

.result {
  margin-top: 12px;
  padding: 12px 14px;
  font-family: var(--mono);
  font-size: 18px;
  letter-spacing: 0.14em;
  color: var(--text);
  background: #131d19;
  border: 1px solid var(--border);
  border-radius: 8px;
  word-break: break-all;
  line-height: 1.8;
  transition: color 0.2s, border-color 0.2s, box-shadow 0.2s;
}

.result.ok {
  color: var(--primary);
  border-color: rgba(62, 242, 176, 0.45);
  box-shadow: 0 0 18px rgba(62, 242, 176, 0.18);
  text-shadow: 0 0 14px rgba(62, 242, 176, 0.35);
}

.ok-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 12px;
}

.accent {
  color: var(--primary);
}

.hint {
  margin: 6px 0 0;
  font-size: 12px;
  line-height: 1.7;
}

@media (max-width: 768px) {
  .freq-layout {
    flex-direction: column;
  }

  .freq-ref {
    width: 100%;
  }
}
</style>