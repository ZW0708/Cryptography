<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useProgressStore } from '@/stores/progress'
import GlitchText from '@/components/GlitchText.vue'
import TypeWriter from '@/components/TypeWriter.vue'
import ProgressRing from '@/components/ProgressRing.vue'
import RollingNumber from '@/components/RollingNumber.vue'

const INTRO =
  '面向非技术用户的密码学科普闯关：亲手破解、亲眼所见，理解公钥为什么能公开、哈希为什么不可逆、没有认证的密钥交换为什么危险。'

const auth = useAuthStore()
const progress = useProgressStore()
const router = useRouter()

const playIntro = ref(!localStorage.getItem('cq_intro_played'))

onMounted(() => {
  if (playIntro.value) localStorage.setItem('cq_intro_played', '1')
  if (auth.isLoggedIn()) progress.fetchOverview()
})

function pct() {
  const o = progress.overview
  if (!o) return 0
  return Math.round((o.clearedCount / Math.max(1, o.totalLevels)) * 100)
}
</script>

<template>
  <div>
    <section class="hero card">
      <h1>🔐 <GlitchText text="密码学闯关" /></h1>
      <p class="muted sub">
        <TypeWriter v-if="playIntro" :text="INTRO" :speed="38" />
        <template v-else>{{ INTRO }}</template>
      </p>
      <div class="row wrap" style="margin-top: 16px">
        <template v-if="auth.isLoggedIn()">
          <button class="lg" @click="router.push('/levels')">继续闯关</button>
          <button class="secondary lg" @click="router.push('/knowledge')">📚 知识库</button>
        </template>
        <template v-else>
          <button class="lg" @click="router.push('/register')">开始闯关（注册）</button>
          <button class="secondary lg" @click="router.push('/login')">登录</button>
        </template>
      </div>
    </section>

    <section v-if="auth.isLoggedIn() && progress.overview" class="card">
      <div class="row wrap">
        <ProgressRing :value="pct()" label="学习进度" />
        <div style="flex: 1; min-width: 0">
          <strong>学习进度</strong>
          <p class="muted" style="margin: 6px 0 0">
            已完成 <RollingNumber :value="progress.overview.clearedCount" /> /
            {{ progress.overview.totalLevels }} 关
          </p>
          <p class="muted" style="margin: 2px 0 0">完成度 {{ pct() }}%</p>
        </div>
      </div>
    </section>

    <section class="features">
      <div class="card hover feature">
        <div class="icon">🧩</div>
        <h3>5 个交互关卡</h3>
        <p class="muted">凯撒破译、哈希雪崩、中间人攻击、AES 积木、数字签名。</p>
      </div>
      <div class="card hover feature">
        <div class="icon">📚</div>
        <h3>配套知识库</h3>
        <p class="muted">每关配知识讲解，先理解概念，再去挑战。</p>
      </div>
      <div class="card hover feature">
        <div class="icon">🤖</div>
        <h3>AI 提示</h3>
        <p class="muted">卡关时逐级获得更具体的提示。</p>
      </div>
    </section>
  </div>
</template>

<style scoped>
.hero {
  padding: 32px;
}

.hero h1 {
  font-size: 28px;
}

.sub {
  min-height: 3.6em;
  line-height: 1.8;
}

.features {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.feature {
  margin-bottom: 0;
}

.feature .icon {
  font-size: 24px;
}

.feature h3 {
  margin: 8px 0 6px;
  font-size: 16px;
}

@media (max-width: 768px) {
  .hero {
    padding: 22px;
  }

  .hero h1 {
    font-size: 23px;
  }

  .features {
    grid-template-columns: 1fr;
  }
}
</style>