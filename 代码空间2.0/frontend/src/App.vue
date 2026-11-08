<script setup lang="ts">
import { ref } from 'vue'
import { RouterView, RouterLink, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import ToastHost from '@/components/ToastHost.vue'
import HexRain from '@/components/HexRain.vue'

const auth = useAuthStore()
const router = useRouter()
const menuOpen = ref(false)

function closeMenu() {
  menuOpen.value = false
}

function onLogout() {
  auth.logout()
  closeMenu()
  router.push('/')
}
</script>

<template>
  <div class="app-shell">
    <header class="app-header">
      <div class="container header-inner">
        <RouterLink to="/" class="brand" @click="closeMenu">
          <span class="brand-mark">🔐</span>
          <span>密码学闯关</span>
          <span class="caret">▍</span>
        </RouterLink>

        <button class="ghost menu-toggle" aria-label="打开菜单" @click="menuOpen = !menuOpen">☰</button>

        <nav class="nav" :class="{ open: menuOpen }">
          <RouterLink to="/" @click="closeMenu">首页</RouterLink>
          <RouterLink to="/levels" @click="closeMenu">关卡</RouterLink>
          <RouterLink to="/knowledge" @click="closeMenu">知识库</RouterLink>
          <RouterLink to="/answers" @click="closeMenu">答案</RouterLink>
          <RouterLink to="/dashboard" @click="closeMenu">进度</RouterLink>
          <RouterLink v-if="auth.isAdmin()" to="/admin" @click="closeMenu">管理</RouterLink>

          <span class="divider" />

          <template v-if="auth.isLoggedIn()">
            <span class="user">$ {{ auth.nickname }}</span>
            <button class="ghost sm" @click="onLogout">退出</button>
          </template>
          <template v-else>
            <RouterLink to="/login" @click="closeMenu">登录</RouterLink>
            <RouterLink to="/register" @click="closeMenu">注册</RouterLink>
          </template>
        </nav>
      </div>
    </header>

    <main class="app-main">
      <div class="container">
        <RouterView v-slot="{ Component }">
          <Transition name="page" mode="out-in">
            <component :is="Component" />
          </Transition>
        </RouterView>
      </div>
    </main>

    <footer class="app-footer">
      <div class="container">// 华为开发者大赛 · 金陵数创·工程 · 简化演示，实际算法更复杂</div>
    </footer>

    <HexRain />
    <ToastHost />
  </div>
</template>

<style scoped>
.app-shell {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  position: relative;
  z-index: 1;
}

.app-header {
  position: sticky;
  top: 0;
  z-index: 100;
  background: rgba(17, 26, 23, 0.86);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid var(--border);
  box-shadow: 0 1px 0 rgba(62, 242, 176, 0.12), 0 8px 24px rgba(0, 0, 0, 0.35);
}

.header-inner {
  display: flex;
  align-items: center;
  gap: 16px;
  height: 56px;
}

.brand {
  display: flex;
  align-items: center;
  gap: 6px;
  font-family: var(--mono);
  font-weight: 700;
  color: var(--text);
  font-size: 15px;
  letter-spacing: 0.03em;
  white-space: nowrap;
}

.brand:hover {
  text-decoration: none;
  color: var(--primary);
}

.caret {
  color: var(--primary);
  animation: blink 1.1s steps(2, start) infinite;
}

@keyframes blink {
  50% {
    opacity: 0;
  }
}

.nav {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-left: auto;
}

.nav a {
  color: var(--text-muted);
  font-size: 13px;
  font-family: var(--mono);
  letter-spacing: 0.02em;
}

.nav a:hover {
  color: var(--text);
  text-decoration: none;
}

.nav a.router-link-active {
  color: var(--primary);
  text-shadow: 0 0 12px rgba(62, 242, 176, 0.4);
}

.user {
  color: var(--text-muted);
  font-size: 13px;
  font-family: var(--mono);
  white-space: nowrap;
}

.divider {
  width: 1px;
  height: 18px;
  background: var(--border);
}

.menu-toggle {
  display: none;
  margin-left: auto;
}

.app-main {
  flex: 1;
  padding: 28px 0 40px;
}

.app-footer {
  border-top: 1px solid var(--border);
  padding: 16px 0;
  color: var(--text-muted);
  font-size: 12px;
  font-family: var(--mono);
  text-align: center;
  opacity: 0.8;
}

@media (max-width: 768px) {
  .menu-toggle {
    display: inline-flex;
  }

  .nav {
    display: none;
    position: absolute;
    top: 56px;
    left: 0;
    right: 0;
    flex-direction: column;
    align-items: stretch;
    gap: 0;
    background: #131d19;
    border-bottom: 1px solid var(--border);
    padding: 8px 20px 16px;
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.5);
  }

  .nav.open {
    display: flex;
  }

  .nav a,
  .nav .user {
    padding: 10px 4px;
  }

  .divider {
    display: none;
  }
}

.page-enter-active,
.page-leave-active {
  transition: opacity 0.22s ease, transform 0.22s ease;
}

.page-enter-from {
  opacity: 0;
  transform: translateY(8px);
}

.page-leave-to {
  opacity: 0;
  transform: translateY(-6px);
}

.app-header::after {
  content: "";
  position: absolute;
  left: 0;
  right: 0;
  bottom: -1px;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(62, 242, 176, 0.7), transparent);
  background-size: 200% 100%;
  animation: flow 4s linear infinite;
  pointer-events: none;
}

@keyframes flow {
  0% {
    background-position: 0% 0;
  }
  100% {
    background-position: 200% 0;
  }
}
</style>