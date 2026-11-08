<script setup lang="ts">
import { ref } from 'vue'
import TermTip from '@/components/TermTip.vue'

const emit = defineEmits<{ solved: [text: string] }>()

const mode = ref<'demo' | 'attack'>('demo')
const demoStep = ref(0)
const stage = ref(0)
const failedAttempt = ref('')
const showAsk = ref(false)
const submitted = ref(false)

const DEMO_STEPS = [
  { title: '约定公开参数', body: 'Alice 和 Bob 先公开约定两个小数字：p = 23，g = 5。公开没关系，这两个数本身不是秘密。' },
  { title: 'Alice 生成公钥', body: 'Alice 私下选 a = 6（私钥），算出公钥 A = 5⁶ mod 23 = 8，把 8 发给 Bob。私钥 6 不出门。' },
  { title: 'Bob 生成公钥', body: 'Bob 私下选 b = 15（私钥），算出公钥 B = 5¹⁵ mod 23 = 19，把 19 发给 Alice。' },
  { title: '各自算出同一把密钥', body: 'Alice 用收到的 19 算：19⁶ mod 23 = 2；Bob 用收到的 8 算：8¹⁵ mod 23 = 2。' }
]

function nextDemo() {
  if (demoStep.value < 4) demoStep.value++
}

function startAttack() {
  mode.value = 'attack'

}

function backToDemo() {
  mode.value = 'demo'
}

const flying = ref<null | { label: string; hostile: boolean; seg: 1 | 2; reverse?: boolean }>(null)
const busy = ref(false)

function sleep(ms: number) {
  return new Promise((r) => setTimeout(r, ms))
}

async function interceptA() {
  if (stage.value !== 0 || busy.value) return
  busy.value = true
  flying.value = { label: 'A', hostile: false, seg: 1 }
  await sleep(950)
  flying.value = { label: "A'", hostile: true, seg: 2 }
  await sleep(950)
  flying.value = null
  stage.value = 1
  busy.value = false
}

async function interceptB() {
  if (stage.value !== 1 || busy.value) return
  busy.value = true
  flying.value = { label: 'B', hostile: false, seg: 2, reverse: true }
  await sleep(950)
  flying.value = { label: "B'", hostile: true, seg: 1, reverse: true }
  await sleep(950)
  flying.value = null
  stage.value = 2
  busy.value = false
}

async function negotiate() {
  if (stage.value !== 2 || busy.value) return
  busy.value = true
  await sleep(500)
  stage.value = 3
  busy.value = false
}

function passThrough() {
  failedAttempt.value = '你放行了公开参数，Alice 和 Bob 直接协商成功（共享密钥 = 2）——攻击失败，请重置后重来。'
}

function resetAll() {
  mode.value = 'demo'
  demoStep.value = 0
  stage.value = 0
  failedAttempt.value = ''
  showAsk.value = false
  submitted.value = false
}

function confirm() {
  if (stage.value < 3 || submitted.value) return
  submitted.value = true
  emit('solved', 'mitm-success')
}
</script>

<template>
  <div class="card">
    <div class="row wrap">
      <span class="badge" :class="mode === 'demo' ? 'badge-info' : 'badge-danger'">
        {{ mode === 'demo' ? '观摩模式：Alice ↔ Bob' : '角色：中间人（就是你）' }}
      </span>
      <span class="spacer" />
      <span class="muted" style="font-size: 12px">
        {{ mode === 'demo' ? '先看懂正常的密钥交换' : '目标：拆散他们的密钥' }}
      </span>
    </div>

    <div class="intro-block">
      <strong style="font-size: 13px">📖 60 秒概念引入</strong>
      <p style="margin: 6px 0 0; line-height: 1.7">
        <TermTip term="Diffie-Hellman" tip="一种让双方在公开信道上「商量出同一把密钥」的方法，全程不用把密钥发出去。" />
        让双方不用传密钥，也能算出同一把
        <TermTip term="共享密钥" tip="只有通信双方知道的秘密数字，用来加密后续消息。" />。
        但如果有人在中间偷换
        <TermTip term="公钥" tip="可以公开的数字；与之配对的私钥才需要保密。" />，
        就变成了<TermTip term="中间人攻击" tip="攻击者夹在双方中间，分别与两端建立密钥，从而偷看、篡改所有消息。" />。
      </p>
    </div>

    <!-- 阶段一：正常交换演示 -->
    <template v-if="mode === 'demo'">
      <div class="section-title">阶段一：正常的 DH 密钥交换（小数字演示）</div>

      <div class="demo-steps">
        <div
          v-for="(s, i) in DEMO_STEPS"
          :key="i"
          class="demo-step"
          :class="{ done: demoStep > i, current: demoStep === i }"
        >
          <div class="step-head">{{ i + 1 }}. {{ s.title }}</div>
          <p v-if="demoStep > i" class="step-body">{{ s.body }}</p>
        </div>
      </div>

      <div v-if="demoStep >= 4" class="key-row">
        <span class="key-badge shared">Alice 的密钥 s = 2</span>
        <span class="key-badge shared">Bob 的密钥 s = 2</span>
      </div>
      <p v-if="demoStep >= 4" class="ok">
        双方从头到尾都没把「2」传出去，却各自算出了同一个数——这就是 DH 的魔法。
      </p>

      <div class="row wrap" style="margin-top: 12px">
        <button v-if="demoStep < 4" @click="nextDemo">
          {{ demoStep === 0 ? '开始演示' : '下一步' }}
        </button>
        <button v-else @click="startAttack">进入攻击模式 →</button>
        <button class="secondary" @click="resetAll">重置</button>
      </div>
    </template>

    <!-- 阶段二：中间人攻击 -->
    <template v-else>
      <div class="section-title">阶段二：你来当中间人</div>

      <div class="channel">
        <span class="ch-node alice">Alice</span>
        <div class="wire">
          <span
            v-if="flying && flying.seg === 1"
            class="packet"
            :class="{ hostile: flying.hostile, reverse: flying.reverse }"
          >{{ flying.label }}</span>
        </div>
        <span class="ch-node atk">攻击者</span>
        <div class="wire">
          <span
            v-if="flying && flying.seg === 2"
            class="packet"
            :class="{ hostile: flying.hostile, reverse: flying.reverse }"
          >{{ flying.label }}</span>
        </div>
        <span class="ch-node bob">Bob</span>
      </div>

      <div class="lanes">
        <div class="lane">
          <div class="lane-head alice">Alice</div>
          <div class="param" :class="{ danger: stage >= 1 }">
            <div class="param-label">公开发送</div>
            <code>{{ stage >= 1 ? "A' = 21（被你替换）" : 'A = 8' }}</code>
          </div>
          <div class="param" :class="{ warn: stage >= 3 }">
            <div class="param-label">她以为的共享密钥</div>
            <code>{{ stage >= 3 ? 'K = 18（其实是和你）' : '—' }}</code>
          </div>
        </div>
        <div class="lane">
          <div class="lane-head attacker">攻击者（你）</div>
          <div class="param">
            <div class="param-label">你手里的密钥</div>
            <code>{{ stage >= 3 ? '18 和 7（两把都有）' : '尚未协商' }}</code>
          </div>
        </div>
        <div class="lane">
          <div class="lane-head bob">Bob</div>
          <div class="param" :class="{ danger: stage >= 2 }">
            <div class="param-label">公开回送</div>
            <code>{{ stage >= 2 ? "B' = 21（被你替换）" : 'B = 19' }}</code>
          </div>
          <div class="param" :class="{ warn: stage >= 3 }">
            <div class="param-label">他以为的共享密钥</div>
            <code>{{ stage >= 3 ? 'K = 7（其实是和你）' : '—' }}</code>
          </div>
        </div>
      </div>

      <div class="row wrap" style="margin-top: 12px">
        <template v-if="stage === 0">
          <button :disabled="busy" @click="interceptA">拦截并替换 A（8 → 21）</button>
          <button class="secondary" :disabled="busy" @click="passThrough">放行</button>
        </template>
        <template v-else-if="stage === 1">
          <button :disabled="busy" @click="interceptB">拦截并替换 B（19 → 21）</button>
        </template>
        <template v-else-if="stage === 2">
          <button :disabled="busy" @click="negotiate">分别协商密钥</button>
        </template>
        <template v-else>
          <button :disabled="submitted" @click="confirm">
            {{ submitted ? '已提交' : '✅ 完成攻击并提交' }}
          </button>
          <button class="secondary" @click="backToDemo">← 回看正常交换</button>
          <button class="secondary" @click="resetAll">重置</button>
        </template>
      </div>

      <p v-if="failedAttempt" class="warn">{{ failedAttempt }}</p>

      <div v-if="stage === 3" class="result-box">
        <p style="margin: 0">
          攻击完成：Alice 只和你共享 <strong class="k1">18</strong>，Bob 只和你共享
          <strong class="k2">7</strong>。他们俩之间反而没有共同密钥——你两把都有，可以解密、篡改、再转发。
        </p>
      </div>

      <div v-if="stage === 3" class="ask-box">
        <template v-if="!showAsk">
          <strong style="font-size: 13px">🤔 收尾设问：怎么才能防住这种攻击？</strong>
          <div style="margin-top: 8px">
            <button class="secondary sm" @click="showAsk = true">查看防住它的钥匙</button>
          </div>
        </template>
        <template v-else>
          <p style="margin: 0; line-height: 1.8">
            答案是给公钥加上「身份证明」：用
            <TermTip term="数字签名" tip="私钥对数据做的印记，任何人可用对应公钥验证它没被改、确实出自本人。" />
            证明「这个公钥真的属于 Alice / Bob」，再由可信机构签发
            <TermTip term="数字证书" tip="像身份证一样，把公钥和身份绑定，由 CA 机构担保。" />。
            浏览器访问 HTTPS 网站时，正是靠证书防住中间人。
          </p>
          <div style="margin-top: 10px">
            <button :disabled="submitted" @click="confirm">
              {{ submitted ? '已提交' : '我明白了，完成攻击模拟并提交' }}
            </button>
          </div>
        </template>
      </div>
    </template>
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

.demo-steps {
  margin-top: 10px;
}

.demo-step {
  border: 1px solid var(--border);
  border-radius: 8px;
  padding: 9px 12px;
  margin-bottom: 6px;
  background: var(--surface-2);
  transition: border-color 0.2s, background 0.2s;
}

.demo-step.current {
  border-color: rgba(62, 242, 176, 0.45);
}

.demo-step.done {
  border-left: 3px solid var(--primary);
}

.step-head {
  font-size: 13px;
  font-weight: 600;
}

.step-body {
  margin: 4px 0 0;
  font-size: 13px;
  line-height: 1.7;
  color: var(--text);
}

.key-row {
  display: flex;
  gap: 10px;
  margin-top: 12px;
  flex-wrap: wrap;
}

.key-badge {
  font-family: var(--mono);
  font-size: 13px;
  padding: 4px 12px;
  border-radius: 999px;
}

.key-badge.shared {
  background: var(--primary-light);
  color: var(--primary);
  border: 1px solid rgba(62, 242, 176, 0.45);
  box-shadow: 0 0 12px rgba(62, 242, 176, 0.2);
}

.lanes {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  gap: 10px;
  margin-top: 10px;
}

.lane {
  border: 1px solid var(--border);
  border-radius: 8px;
  padding: 10px;
  background: #131d19;
}

.lane-head {
  font-family: var(--mono);
  font-size: 13px;
  text-align: center;
  margin-bottom: 8px;
}

.lane-head.alice,
.lane-head.bob {
  color: var(--primary);
}

.lane-head.attacker {
  color: var(--danger);
}

.param {
  border: 1px dashed var(--border);
  border-radius: 6px;
  padding: 8px;
  margin-top: 6px;
  transition: border-color 0.2s, background 0.2s;
}

.param.danger {
  border-color: rgba(255, 107, 121, 0.55);
  background: rgba(255, 107, 121, 0.08);
}

.param.warn {
  border-color: rgba(255, 208, 107, 0.5);
  background: rgba(255, 208, 107, 0.06);
}

.param-label {
  font-size: 11px;
  color: var(--text-muted);
  font-family: var(--mono);
}

.param code {
  display: block;
  margin-top: 3px;
  font-size: 12px;
  color: var(--text);
  word-break: break-all;
}

.warn {
  margin: 12px 0 0;
  color: var(--warning);
  font-size: 13px;
  line-height: 1.7;
}

.ok {
  margin: 12px 0 0;
  color: var(--primary);
  font-size: 13px;
  line-height: 1.7;
}

.result-box {
  margin-top: 14px;
  padding: 10px 14px;
  background: rgba(255, 107, 121, 0.08);
  border: 1px solid rgba(255, 107, 121, 0.4);
  border-radius: 8px;
  font-size: 13px;
  line-height: 1.8;
}

.k1 {
  color: var(--info);
}

.k2 {
  color: var(--warning);
}

.ask-box {
  margin-top: 12px;
  padding: 12px 14px;
  background: var(--primary-light);
  border: 1px solid rgba(62, 242, 176, 0.35);
  border-radius: 8px;
  font-size: 13px;
}

.channel {
  display: flex;
  align-items: center;
  gap: 0;
  margin-top: 10px;
  padding: 10px 8px;
  background: #131d19;
  border: 1px solid var(--border);
  border-radius: 8px;
}

.ch-node {
  font-family: var(--mono);
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 6px;
  white-space: nowrap;
}

.ch-node.alice,
.ch-node.bob {
  color: var(--primary);
}

.ch-node.atk {
  color: var(--danger);
}

.wire {
  position: relative;
  flex: 1;
  height: 2px;
  background: var(--border);
  min-width: 36px;
}

.packet {
  position: absolute;
  top: 50%;
  left: 0;
  transform: translateY(-50%);
  min-width: 26px;
  text-align: center;
  font-family: var(--mono);
  font-size: 12px;
  font-weight: 700;
  padding: 2px 5px;
  border-radius: 6px;
  background: var(--primary-light);
  border: 1px solid rgba(62, 242, 176, 0.45);
  color: var(--primary);
  animation: pkt-fly 0.9s linear both;
}

.packet.hostile {
  background: rgba(255, 107, 121, 0.15);
  border-color: rgba(255, 107, 121, 0.55);
  color: var(--danger);
}

.packet.reverse {
  animation-direction: reverse;
}

@keyframes pkt-fly {
  from {
    left: 0;
  }
  to {
    left: calc(100% - 32px);
  }
}

@media (max-width: 768px) {
  .lanes {
    grid-template-columns: 1fr;
  }
}
</style>