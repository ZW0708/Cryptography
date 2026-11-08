import { defineStore } from 'pinia'
import { ref } from 'vue'
import { authApi } from '@/api/auth'
import type { AuthResponse } from '@/types'

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string | null>(localStorage.getItem('cq_token'))
  const nickname = ref<string | null>(localStorage.getItem('cq_user'))
  const role = ref<string | null>(localStorage.getItem('cq_role'))

  function persist(auth: AuthResponse) {
    token.value = auth.token
    nickname.value = auth.nickname
    role.value = auth.role
    localStorage.setItem('cq_token', auth.token)
    localStorage.setItem('cq_user', auth.nickname)
    localStorage.setItem('cq_role', auth.role)
  }

  async function register(n: string, p: string) {
    const { data } = await authApi.register(n, p)
    persist(data.data)
  }

  async function login(n: string, p: string) {
    const { data } = await authApi.login(n, p)
    persist(data.data)
  }

  function logout() {
    token.value = null
    nickname.value = null
    role.value = null
    localStorage.removeItem('cq_token')
    localStorage.removeItem('cq_user')
    localStorage.removeItem('cq_role')
  }

  const isLoggedIn = () => !!token.value
  const isAdmin = () => role.value === 'ADMIN'

  return { token, nickname, role, register, login, logout, isLoggedIn, isAdmin }
})