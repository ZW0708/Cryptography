<script setup lang="ts">
import { ref, computed } from 'vue'
import TermTip from '@/components/TermTip.vue'

const emit = defineEmits<{ solved: [text: string] }>()

const ORIGINAL = [0x32, 0x88, 0x31, 0xe0, 0x43, 0x5a, 0x31, 0x37, 0xf6, 0x30, 0x98, 0x07, 0xa8, 0x8d, 0xa2, 0x34]
const KEY_PRESETS = [
  { label: '0x2B', value: 0x2b },
  { label: '0x7C', value: 0x7c },
  { label: '0xFF', value: 0xff },
  { label: '0x01', value: 0x01 }
]

const keyByte = ref(0x2b)
const state = ref<number[]>([...ORIGINAL])
const stepIndex = ref(0)
const changed = ref<number[]>([])
const message = ref('')
const submitted = ref(false)

const at = (arr: number[], i: number) => arr[i] ?? 0

function subBytes(s: number[]) {
  return s.map((b) => (b + 0x3a) & 0xff)
}

function shiftRows(s: number[]) {
  const next = [...s]
  for (let r = 0; r < 4; r++) {
    const row = [at(s, r * 4), at(s, r * 4 + 1), at(s, r * 4 + 2), at(s, r * 4 + 3)]
    for (let c = 0; c < 4; c++) next[r * 4 + c] = row[(c + r) % 4] ?? 0
  }
  return next
}

function mixColumns(s: number[]) {
  const next = [...s]
  for (let c = 0; c < 4; c++) {
    const a = at(s, c)
    const b = at(s, 4 + c)
    const d = at(s, 8 + c)
    const e = at(s, 12 + c)
    next[c] = a ^ b
    next[4 + c] = b ^ d
    next[8 + c] = d ^ e
    next[12 + c] = e ^ a
  }
  return next
}

function addRoundKey(s: number[], k: number) {
  return s.map((b) => b ^ k)
}

const STEPS = [
  {
    name: 'SubBytes 字节替代',
    apply: (s: number[]) => subBytes(s),
    explain: '每个字节查表替换（非线性混淆）——注意：16 个字节全部变了。'
  },
  {
    name: 'ShiftRows 行移位',
    apply: (s: number[]) => shiftRows(s),
    explain: '第 1/2/3 行分别循环左移 1/2/3 位——字节开始换位置。'
  },
  {
    name: 'MixColumns 列混合',
    apply: (s: number[]) => mixColumns(s),
    explain: '每一列内部互相混合——一个字节的变化从此散布到整列（扩散）。'
  },
  {
    name: 'AddRoundKey 轮密钥加',
    apply: (s: number[]) => addRoundKey(s, keyByte.value),
    explain: '与密钥逐字节异或——密钥正式介入：密钥不同，密文完全不同。'
  }
]

const changedSet = computed(() => new Set(changed.value))

const diffSet = computed(() => {
  const set = new Set<number>()
  state.value.forEach((b, i) => {
    if (b !== (ORIGINAL[i] ?? 0)) set.add(i)
  })
  return set
})

const diffCount = computed(() => diffSet.value.size)
const diffPct = computed(() => Math.round((diffCount.value / 16) * 100))

function applyStep(i: number) {
  if (i !== stepIndex.value) {
    message.value = '顺序不对：AES 一轮是 字节替代 → 行移位 → 列混合 → 轮密钥加。请重置后按序操作。'
    reset()
    return
  }
  const prev = [...state.value]
  const next = STEPS[i].apply(state.value)
  changed.value = next.map((b, idx) => (b !== prev[idx] ? idx : -1)).filter((x) => x >= 0)
  state.value = next
  stepIndex.value++
  message.value =
    stepIndex.value === 4
      ? `✓ 一轮加密完成：与明文相比 ${diffCount.value}/16 个字节已改变（${diffPct.value}%）——这就是扩散的威力。`
      : STEPS[i].explain
}

function reset() {
  state.value = [...ORIGINAL]
  stepIndex.value = 0
  changed.value = []
  message.value = ''
  submitted.value = false
}

function setKey(v: number) {
  if (stepIndex.value > 0) return
  keyByte.value = v
  reset()
  message.value = '密钥已更换为 ' + hex(v) + '：同样的四步操作，最后一步的结果会完全不同——密钥才是唯一的秘密。'
}

function hex(b: number) {
  return b.toString(16).padStart(2, '0').toUpperCase()
}

function confirm() {
  if (stepIndex.value < 4 || submitted.value) return
  submitted.value = true
  emit('solved', 'aes-demo')
}
</script>

<template>
  <div class="card">
    <div class="row wrap">
      <span class="badge badge-info">角色：加密者</span>
      <span class="spacer" />
      <span class="muted" style="font-size: 12px">目标：按正确顺序完成一轮 AES 积木</span>
    </div>

    <div class="intro-block">
      <strong style="font-size: 13px">📖 60 秒概念引入</strong>
      <p style="margin: 6px 0 0; line-height: 1.7">
        <TermTip term="对称加密" tip="加密和解密用同一把钥匙，速度快，适合保护大量数据。" />
        AES 是代表算法。它靠两件武器打散数据：
        <TermTip term="混淆" tip="让密文和密钥的关系复杂到看不出规律。" />、
        <TermTip term="扩散" tip="让明文的一点变化波及密文的一大片。" />；
        最后由<TermTip term="轮密钥加" tip="把密钥逐字节混入数据（异或运算）。" />让密钥成为唯一的秘密。
      </p>
    </div>

    <div class="section-title">🔑 选择本轮密钥</div>
    <div class="row wrap">
      <button
        v-for="p in KEY_PRESETS"
        :key="p.label"
        :class="{ secondary: keyByte !== p.value }"
        :disabled="stepIndex > 0"
        @click="setKey(p.value)"
      >
        {{ p.label }}
      </button>
      <span class="muted" style="font-size: 12px">换一把密钥，同样的操作会得到完全不同的密文</span>
    </div>

    <div class="section-title">数据状态（4×4 字节）</div>
    <div class="state-grid">
      <span
        v-for="(b, i) in state"
        :key="i"
        class="byte"
        :class="{ changed: changedSet.has(i), diff: diffSet.has(i) && !changedSet.has(i) }"
      >
        {{ hex(b) }}
      </span>
    </div>
    <p class="muted hint" style="margin-top: 6px">
      <span class="legend-dot changed-dot"></span> 这一步刚变化
      <span class="legend-dot diff-dot" style="margin-left: 12px"></span> 与明文不同（扩散范围）
    </p>

    <div class="timeline">
      <div
        v-for="(s, i) in STEPS"
        :key="'tl' + i"
        class="tl-step"
        :class="{ done: i < stepIndex, current: i === stepIndex }"
      >
        {{ i + 1 }}
      </div>
    </div>

    <div class="row wrap">
      <button
        v-for="(s, i) in STEPS"
        :key="s.name"
        :class="{ secondary: i !== stepIndex }"
        :disabled="i < stepIndex"
        @click="applyStep(i)"
      >
        {{ i + 1 }}. {{ s.name }}
      </button>
      <button class="secondary" @click="reset">重置</button>
    </div>

    <p v-if="message" class="msg">{{ message }}</p>

    <div v-if="stepIndex === 4" class="done-box">
      <p style="margin: 0 0 10px">
        与明文相比：<strong>{{ diffCount }}/16</strong> 个字节已改变（{{ diffPct }}%）
      </p>
      <div class="row wrap">
        <button class="secondary" @click="reset">🔁 换一把密钥再试</button>
        <button :disabled="submitted" @click="confirm">{{ submitted ? '已提交' : '确认并提交' }}</button>
      </div>
    </div>

    <p class="muted hint" style="margin-top: 12px">
      简化演示：这里用示意变换展示混淆与扩散，真实 AES 使用标准 S 盒与完整轮函数。
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

.section-title {
  margin-top: 18px;
  font-family: var(--mono);
  font-size: 13px;
  color: var(--primary);
  letter-spacing: 0.04em;
}

.state-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 6px;
  margin-top: 8px;
  max-width: 320px;
}

.byte {
  font-family: var(--mono);
  font-size: 14px;
  text-align: center;
  padding: 8px 0;
  background: #131d19;
  border: 1px solid var(--border);
  border-radius: 6px;
  color: var(--text);
  transition: background 0.2s, border-color 0.2s, color 0.2s;
}

.byte.changed {
  color: var(--primary);
  border-color: rgba(62, 242, 176, 0.45);
  background: var(--primary-light);
}

.byte.diff {
  color: var(--warning);
  border-color: rgba(255, 208, 107, 0.4);
  background: rgba(255, 208, 107, 0.08);
}

.legend-dot {
  display: inline-block;
  width: 10px;
  height: 10px;
  border-radius: 3px;
  vertical-align: middle;
  background: var(--primary);
  border: 1px solid rgba(62, 242, 176, 0.45);
}

.diff-dot {
  background: rgba(255, 208, 107, 0.25);
  border-color: rgba(255, 208, 107, 0.4);
}

.timeline {
  display: flex;
  gap: 6px;
  margin-top: 16px;
}

.tl-step {
  width: 26px;
  height: 26px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-family: var(--mono);
  font-size: 12px;
  border: 1px solid var(--border);
  color: var(--text-muted);
  background: #131d19;
}

.tl-step.current {
  border-color: var(--primary);
  color: var(--primary);
  box-shadow: 0 0 10px rgba(62, 242, 176, 0.3);
}

.tl-step.done {
  background: var(--primary-light);
  border-color: rgba(62, 242, 176, 0.45);
  color: var(--primary);
}

.msg {
  margin: 12px 0 0;
  font-size: 13px;
  line-height: 1.7;
  color: var(--info);
}

.done-box {
  margin-top: 14px;
  padding: 12px 14px;
  background: var(--primary-light);
  border: 1px solid rgba(62, 242, 176, 0.35);
  border-radius: 8px;
  font-size: 13px;
}

.done-box strong {
  color: var(--primary);
}

.hint {
  margin: 8px 0 0;
  font-size: 12px;
  line-height: 1.7;
}
</style>