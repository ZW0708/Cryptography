<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import PasswordInput from '@/components/PasswordInput.vue'
import { useToast } from '@/composables/useToast'

const nickname = ref('')
const password = ref('')
const confirmPassword = ref('')
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
  if (!/[a-zA-Z]/.test(password.value) || !/\d/.test(password.value)) return '密码必须包含字母和数字'
  if (password.value === nickname.value) return '密码不能与账号相同'
  return ''
})

const confirmErr = computed(() => {
  if (!confirmPassword.value) return ''
  if (confirmPassword.value !== password.value) return '两次密码不一致'
  return ''
})

const canSubmit = computed(
  () =>
    nickname.value.length >= 2 &&
    nickname.value.length <= 32 &&
    password.value.length >= 6 &&
    password.value.length <= 64 &&
    /[a-zA-Z]/.test(password.value) &&
    /\d/.test(password.value) &&
    password.value !== nickname.value &&
    confirmPassword.value === password.value &&
    !loading.value
)

async function onRegister() {
  if (!canSubmit.value) return
  loading.value = true
  try {
    await auth.register(nickname.value, password.value)
    toast('注册成功，欢迎开始闯关', 'success')
    router.push('/levels')
  } catch (e: any) {
    toast(e.response?.data?.message ?? '注册失败，请稍后重试', 'error')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="auth-wrap">
    <div class="card auth-card">
      <h2>🔐 注册</h2>
      <p class="muted" style="font-size: 13px">仅需账号 + 密码，BCrypt 哈希存储，不收集其他信息</p>

      <label>账号</label>
      <input v-model="nickname" placeholder="2-32 个字符" @keyup.enter="onRegister" />
      <p v-if="nicknameErr" class="field-error">{{ nicknameErr }}</p>

      <label>密码（≥6 位，含字母 + 数字）</label>
      <PasswordInput v-model="password" placeholder="至少 6 位" @keyup.enter="onRegister" />
      <p v-if="passwordErr" class="field-error">{{ passwordErr }}</p>

      <label>确认密码</label>
      <PasswordInput v-model="confirmPassword" placeholder="再次输入密码" @keyup.enter="onRegister" />
      <p v-if="confirmErr" class="field-error">{{ confirmErr }}</p>

      <button class="lg auth-submit" :disabled="!canSubmit" @click="onRegister">
        {{ loading ? '注册中…' : '注册并开始' }}
      </button>
      <p class="muted center" style="margin-top: 14px; font-size: 13px">
        已有账号？<RouterLink to="/login">去登录</RouterLink>
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