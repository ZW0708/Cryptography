<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import PasswordInput from '@/components/PasswordInput.vue'
import { useToast } from '@/composables/useToast'

const nickname = ref('')
const password = ref('')
const loading = ref(false)
const auth = useAuthStore()
const router = useRouter()
const { toast } = useToast()

const nicknameErr = computed(() => {
  if (!nickname.value) return ''
  if (nickname.value.length < 2) return '账号至少 2 个字符'
  if (nickname.value.length > 32) return '账号最多 32 个字符'
  return ''
})

const passwordErr = computed(() => {
  if (!password.value) return ''
  if (password.value.length < 6) return '密码至少 6 位'
  if (password.value.length > 64) return '密码最多 64 位'
  return ''
})

const canSubmit = computed(
  () =>
    nickname.value.length >= 2 &&
    nickname.value.length <= 32 &&
    password.value.length >= 6 &&
    password.value.length <= 64 &&
    !loading.value
)

async function onLogin() {
  if (!canSubmit.value) return
  loading.value = true
  try {
    await auth.login(nickname.value, password.value)
    toast('登录成功', 'success')
    router.push('/levels')
  } catch (e: any) {
    toast(e.response?.data?.message ?? '登录失败，请检查网络', 'error')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="auth-wrap">
    <div class="card auth-card">
      <h2>🔐 登录</h2>
      <p class="muted" style="font-size: 13px">使用账号和密码继续闯关</p>

      <label>账号</label>
      <input v-model="nickname" placeholder="2-32 个字符" @keyup.enter="onLogin" />
      <p v-if="nicknameErr" class="field-error">{{ nicknameErr }}</p>

      <label>密码</label>
      <PasswordInput v-model="password" placeholder="至少 6 位" @keyup.enter="onLogin" />
      <p v-if="passwordErr" class="field-error">{{ passwordErr }}</p>

      <button class="lg auth-submit" :disabled="!canSubmit" @click="onLogin">
        {{ loading ? '登录中…' : '登录' }}
      </button>
      <p class="muted center" style="margin-top: 14px; font-size: 13px">
        没有账号？<RouterLink to="/register">去注册</RouterLink>
      </p>
    </div>
  </div>
</template>

<style scoped>
.auth-wrap {
  display: flex;
  justify-content: center;
  padding-top: 24px;
}

.auth-card {
  width: 100%;
  max-width: 400px;
}

.auth-card h2 {
  margin-bottom: 4px;
}

.auth-submit {
  width: 100%;
  margin-top: 20px;
}

.field-error {
  color: var(--warning);
  font-size: 12px;
  margin: 6px 0 0;
}
</style>