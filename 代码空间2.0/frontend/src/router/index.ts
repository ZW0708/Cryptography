import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', name: 'home', component: () => import('@/views/HomeView.vue') },
    { path: '/login', name: 'login', component: () => import('@/views/LoginView.vue') },
    { path: '/register', name: 'register', component: () => import('@/views/RegisterView.vue') },
    { path: '/levels', name: 'levels', component: () => import('@/views/LevelListView.vue') },
    { path: '/levels/:id', name: 'play', component: () => import('@/views/LevelPlayView.vue') },
    { path: '/dashboard', name: 'dashboard', component: () => import('@/views/DashboardView.vue') },
    { path: '/admin', name: 'admin', component: () => import('@/views/AdminView.vue') },
    { path: '/knowledge', name: 'knowledge', component: () => import('@/views/KnowledgeView.vue') },
    { path: '/answers', name: 'answers', component: () => import('@/views/AnswersView.vue') }
  ]
})

router.beforeEach((to) => {
  const auth = useAuthStore()
  if (to.name !== 'login' && to.name !== 'register' && to.name !== 'home' && !auth.token) {
    return { name: 'login' }
  }
  if (to.name === 'admin' && !auth.isAdmin()) {
    return { name: 'levels' }
  }
})

export default router