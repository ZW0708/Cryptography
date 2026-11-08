<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useProgressStore } from '@/stores/progress'
import { levelApi } from '@/api/level'
import { answerApi } from '@/api/answers'
import { useToast } from '@/composables/useToast'
import HintDialog from '@/components/HintDialog.vue'
import ConceptCard from '@/components/ConceptCard.vue'
import CipherConfetti from '@/components/CipherConfetti.vue'
import CaesarPuzzle from '@/components/CaesarPuzzle.vue'
import HashCollisionPuzzle from '@/components/HashCollisionPuzzle.vue'
import MitmPuzzle from '@/components/MitmPuzzle.vue'
import AesBlocksPuzzle from '@/components/AesBlocksPuzzle.vue'
import SignaturePuzzle from '@/components/SignaturePuzzle.vue'

const route = useRoute()
const router = useRouter()
const store = useProgressStore()
const { toast } = useToast()

const levelId = computed(() => Number(route.params.id))
const showHint = ref(false)
const showSummary = ref(false)
const summaryText = ref('')
const hintUsed = ref(0)
const submitting = ref(false)

const level = computed(() => store.findLevel(levelId.value))

const puzzleMap: Record<string, any> = {
  L1_CAESAR: CaesarPuzzle,
  L2_HASH: HashCollisionPuzzle,
  L3_MITM: MitmPuzzle,
  L4_AES: AesBlocksPuzzle,
  L5_SIGN: SignaturePuzzle
}

const puzzle = computed(() => (level.value ? puzzleMap[level.value.code] ?? null : null))

const GOALS: Record<string, string> = {
  L1_CAESAR: '用频率分析还原整段明文，让解密结果恢复可读',
  L2_HASH: '体验雪崩效应，并亲身感受「找相同前缀」有多难',
  L3_MITM: '先看懂正常密钥交换，再扮演中间人完成一次攻击',
  L4_AES: '按正确顺序完成一轮 AES 积木，观察扩散效果',
  L5_SIGN: '篡改公告消息，让签名验证失败'
}
const goal = computed(() => (level.value ? GOALS[level.value.code] ?? '' : ''))

onMounted(async () => {
  if (!store.overview) await store.fetchOverview()
})

async function reportAnswerState() {
  try {
    const { data } = await answerApi.list()
    const st = data.data.find((a) => a.code === level.value?.code)
    if (!st) {
      toast('答案不对，再试试', 'error')
      return
    }
    if (st.unlocked) {
      toast('答案已解锁，可在「答案」页查看', 'info')
    } else {
      const left = Math.max(0, st.unlockThreshold - st.failedAttempts)
      toast(`答案不对，已答错 ${st.failedAttempts} 次，再答错 ${left} 次解锁答案`, 'error')
    }
  } catch {
    toast('答案不对，再试试', 'error')
  }
}

async function doSubmit(text: string) {
  const val = text.trim()
  if (!val || submitting.value) return
  submitting.value = true
  try {
    const { data } = await levelApi.submit(levelId.value, val, 0, hintUsed.value)
    if (data.data) {
      toast('通关成功！', 'success')
      const s = await levelApi.summary(levelId.value)
      summaryText.value = s.data.data.summaryCard
      showSummary.value = true
      await store.fetchOverview()
    } else {
      await reportAnswerState()
    }
  } catch (e: any) {
    toast(e.response?.data?.message ?? '提交失败', 'error')
  } finally {
    submitting.value = false
  }
}

function onSolved(text: string) {
  doSubmit(text)
}
</script>

<template>
  <div>
    <div class="row wrap" style="margin-bottom: 16px">
      <button class="secondary" @click="router.push('/levels')">← 返回关卡</button>
      <button v-if="level" class="secondary" @click="router.push('/knowledge?code=' + level.code)">
        📚 知识库
      </button>
      <button v-if="level" class="secondary" @click="router.push('/answers?code=' + level.code)">
        🔑 查看答案
      </button>
    </div>

    <div v-if="level" class="card">
      <div class="row wrap">
        <h2 style="margin: 0">{{ level.title }}</h2>
        <span class="badge badge-info">{{ level.cryptoConcept }}</span>
        <span class="spacer" />
        <span class="stars" :title="'难度 ' + level.difficulty">{{ '★'.repeat(level.difficulty) }}</span>
      </div>
      <p style="margin: 12px 0 0">{{ level.introText }}</p>
      <p v-if="goal" class="goal">🎯 通关目标：{{ goal }}</p>
    </div>

    <!-- 可视化角色扮演解题：完成操作后由组件提交，不直接输入答案 -->
    <component :is="puzzle" v-if="puzzle" @solved="onSolved" />
    <div v-else class="card"><p class="muted" style="margin: 0">该关卡交互组件待实现。</p></div>

    <div class="card">
      <div class="row wrap">
        <button class="secondary" @click="showHint = true">💡 求助 AI 提示</button>
        <span class="muted" style="font-size: 12px">
          完成可视化操作后点击「提交」即可通关；通关后可在「答案」页复盘思路与答案。
        </span>
      </div>
    </div>

    <HintDialog
      v-if="showHint && level"
      :level-id="level.id"
      @close="showHint = false"
      @hint-used="hintUsed++"
    />
    <ConceptCard
      v-if="showSummary && level"
      :title="level.title"
      :text="summaryText"
      @close="showSummary = false"
    />
    <CipherConfetti v-if="showSummary" />
  </div>
</template>

<style scoped>
.stars {
  color: var(--warning);
  letter-spacing: 1px;
}

.goal {
  color: var(--warning);
  font-size: 13px;
  margin: 10px 0 0;
}
</style>